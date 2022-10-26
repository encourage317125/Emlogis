angular.module('emlogis.rules')
    .controller('RulesHolidaysCtrl', ['$scope', '$state', '$q', 'appFunc', 'crudDataService', 'dataService', 'dialogs', 'applicationContext',
    function($scope, $state, $q, appFunc, crudDataService, dataService, dialogs, applicationContext) {
      $scope.holidays = [];
      $scope.organization = null;
      $scope.year = moment().year();
      $scope.duplicateHolidays = {
        toYear: $scope.year + 1,
        showControl: false
      };

      var originalHolidays = null;

      $scope.minToYear = function() {
        return $scope.year + 1;
      };

      $scope.$watch("year", function(year) {
        $scope.duplicateHolidays.toYear = $scope.minToYear();
      });

      $scope.filterHolidaysByYear = function(holidays, year) {
        return _.filter(holidays, function(h) {
          var hYear = moment(h.effectiveStartDate).year();
          return hYear == year;
        });
      };

      $scope.addRowForNewHoliday = function() {

        var hasNewHoliday = _.find($scope.holidays, function(h) {
          return h.id === undefined;
        });

        // do not add new row if there is one already
        if (hasNewHoliday) return;

        var startOfYear = moment().year($scope.year),
            newHoliday = {
              name: null,
              description: null,
              effectiveStartDate: startOfYear.valueOf(),
              inEditMode: true
            },

            firstHolidayOfYear = _.findIndex($scope.holidays, function(h) {
              return moment(h.effectiveStartDate).year() == $scope.year;
            });

        // insert a new holiday row before first holiday of year
        $scope.holidays.splice(firstHolidayOfYear, 0, newHoliday);
      };

      $scope.showDuplicateControl = function() {
        $scope.duplicateHolidays.toYear = $scope.year + 1;
        $scope.duplicateHolidays.showControl = true;
      };

      $scope.saveHoliday = function(hObj) {
        if (!$scope.validHoliday(hObj)) {
          return $q.reject({});
        } else if (hObj.id) {
          return updateHoliday(hObj);
        } else {
          return createHoliday(hObj);
        }
      };

      $scope.validHoliday = function(hObj) {
        var validDate = hObj.calendarDate && moment(hObj.calendarDate).isValid();
        return (validDate && hObj.name);
      };

      function updateHoliday(hObj) {
        var date = moment(hObj.calendarDate).toDate(),

        startDate = appFunc.getDateWithTimezone(
                date.getFullYear(),
                date.getMonth(),
                date.getDate(),
                $scope.organization.timeZone
            ),
            endDate = moment(startDate.getTime()).add(1, 'days');

        return dataService.updateHoliday(hObj.id, {
          "name": hObj.name,
          "description": hObj.description,
          "effectiveStartDate": startDate.getTime(),
          "effectiveEndDate": endDate.valueOf()
        }).
        then(function(res) {
          applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
          hObj.effectiveStartDate = appFunc.convertToBrowserTimezone(res.data.effectiveStartDate, $scope.organization.timeZone);
          hObj.inEditMode = false;
          sortAndBackUpHolidays();
        },
        function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
      }

      function createHoliday(hObj) {
        var startDate = appFunc.getDateWithTimezone(
                hObj.calendarDate.getFullYear(),
                hObj.calendarDate.getMonth(),
                hObj.calendarDate.getDate(),
                $scope.organization.timeZone
            ),
            endDate = moment(startDate.getTime()).add(1, 'days');

        return dataService.createHoliday({
          name: hObj.name,
          updateDto: {
            description: hObj.description,
            effectiveStartDate: startDate.getTime(),
            effectiveEndDate: endDate.valueOf()
          }
        })
        .then(function(res) {
          applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
          hObj.id = res.data.id;
          hObj.effectiveStartDate = appFunc.convertToBrowserTimezone(res.data.effectiveStartDate, $scope.organization.timeZone);
          hObj.inEditMode = false;
          sortAndBackUpHolidays();
        },
        function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
      }

      $scope.deleteHoliday = function(hObj) {
        if (!hObj.id) {
          removeNewHolidays();
          return;
        }

        var confirm = dialogs.confirm('app.PLEASE_CONFIRM', 'rules.holidays.ARE_YOU_SURE_REMOVE_HOLIDAY?');

        confirm.result.then(function (btn) {
          dataService.deleteHoliday(hObj.id)
              .then(function (res) {
                applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
                $scope.holidays = _.reject($scope.holidays, function(h) {
                  return h.id === hObj.id;
                });
                originalHolidays = _.cloneDeep($scope.holidays);
              },
              function (err) {
                applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              });
        }, function (btn) {
          // no pressed. Do nothing
        });
      };

      // remove holidays which were not send to server
      function removeNewHolidays() {
        $scope.holidays = _.reject($scope.holidays, function(h) {
          return h.id === undefined;
        });
      }

      $scope.duplicateYear = function(fromYear, toYear) {
        dataService.duplicateYear(fromYear, toYear)
            .then(function(res) {
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
              $scope.year = toYear;
              $scope.duplicateHolidays.toYear = toYear + 1;
              $scope.duplicateHolidays.showControl = false;
              getHolidays();
            },
            function (err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            });
      };

      //
      // Switch to/from edit holiday mode
      $scope.edit = function(hObj, edit) {
        // cancel changes if pressed Esc while editing
        if (!edit) {
          resetHoliday(hObj);
        } else {
          var originalHoliday = _.find(originalHolidays, 'id', hObj.id);
          hObj.calendarDate = originalHoliday.calendarDate = toCalendarDateFormat(hObj.effectiveStartDate);
        }
        hObj.inEditMode = edit;
      };

      function resetHoliday(hObj) {
        // if canceled editing a new holiday - remove it from table
        if (!hObj.id) {
          removeNewHolidays();
          return;
        }

        //if canceled editing an existing holiday - restore it to previous state
        var originalHoliday = _.clone(_.find(originalHolidays, function(h) {
          return hObj.id == h.id;
        }));

        // replace holiday object with original one
        $scope.holidays = _.map($scope.holidays, function(h) {
          return (h.id === hObj.id) ? originalHoliday : h;
        });
      }

      $scope.toCellDateFormat = function(date) {
          return moment(date).format("MMM DD, dddd");
      };

      $scope.cellDate = function(date) {
        return moment(date).format("MMM DD");
      };

      $scope.cellDaYOfWeek = function(date) {
        return moment(date).format("dddd YYYY");
      };

      function toCalendarDateFormat(date) {
        return moment(date).format("MM/DD/YY");
      }

      // open calendar control
      $scope.open = function($event, hObj) {
        $event.preventDefault();
        $event.stopPropagation();
        hObj.opened = true;
      };

      function sortAndBackUpHolidays() {
        /**
         * sort by start date
         */
        $scope.holidays = _.sortBy($scope.holidays, function(h) {
          return h.effectiveStartDate;
        });
        /**
         * create a copy of holidays object to be able cancel editing
         */
        originalHolidays = _.cloneDeep($scope.holidays);
      }

      function getHolidays() {
        dataService.getHolidays()
            .then(function (res) {
              $scope.holidays = _.sortBy(res.data.result, function(h) {
                return h.effectiveStartDate;
              });
              // GET Current organization details
              return $scope.organization || crudDataService.getElement('org', '');
            })
            .then(function(res) {
              $scope.organization = res;
              /**
               * convert startDate to browser's time stack,
               * set display mode to read
               */
              angular.forEach($scope.holidays, function(h) {
                h.effectiveStartDate = appFunc.convertToBrowserTimezone(h.effectiveStartDate, $scope.organization.timeZone);
                h.inEditMode = false;
              });
              originalHolidays = _.cloneDeep($scope.holidays);
            });
      }

      getHolidays();

      //--------------------------------------------------------------------
      // Setup a window for asking to save the changes
      //--------------------------------------------------------------------

      var working = applicationContext.getWorking();

      working.entityName = 'rules.HOLIDAYS';
      working.option = {
        editing: false,
        submitted: false
      };
      working.saveFunc = function() {
        var promises = [];
        working.option.editing = false;

        _.each($scope.unsavedHolidays, function(h) {
          promises.push($scope.saveHoliday(h));
        });

        return $q.all(promises);
      };


      function holidaysAreEqual(h1, h2) {
        var areEqual = true,
          propertiesToCheck = ['name', 'description', 'calendarDate']; // 'effectiveStartDate'
        _.each(propertiesToCheck, function(prop) {
          if (!_.isEqual(h1[prop], h2[prop])) {
            areEqual = false;
          }
        });
        return areEqual;
      }


      $scope.$watch('holidays', function(holidays) {
        $scope.unsavedHolidays = [];
        working.option.editing = false;
        _.each(holidays, function(h) {
          var originalHoliday = _.find(originalHolidays, 'id', h.id);

          if (h && originalHoliday && !holidaysAreEqual(h, originalHoliday)) {
            $scope.unsavedHolidays.push(h);
            working.option.editing = true;
          }

        });
      }, true);

}]);
