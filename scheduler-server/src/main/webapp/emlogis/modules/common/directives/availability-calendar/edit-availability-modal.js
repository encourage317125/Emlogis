angular.module('emlogis.commonDirectives').controller('EditAvailabilityModal',
  ['$scope', '$modalInstance', 'dataService', 'applicationContext', 'managerView', 'employeeId', 'availabilityType',
    'datesSelected', 'dateRangeStart', 'dateRangeEnd', 'timeZone', 'absenceTypes', 'appFunc',
    function ($scope, $modalInstance, dataService, applicationContext, managerView, employeeId, availabilityType,
              datesSelected, dateRangeStart, dateRangeEnd, timeZone, absenceTypes, appFunc) {

      $scope.managerView = managerView;
      $scope.availabilityType = availabilityType;
      $scope.datesToDisplay = "";

      if (managerView) {
        $scope.absenceTypes = absenceTypes;
        $scope.absenceType = absenceTypes[0];
      }

      if (availabilityType === "ci") {
        $scope.datesToDisplay = _.map(datesSelected, function(d) {
          return _.capitalize(d) + "s";
        });
      } else {
        $scope.datesToDisplay = _.map(datesSelected, function(d) {
          return moment(d).format("M/D");
        });
        datesSelected = _.map(datesSelected, function(d) {
          var date = moment(d);
          return appFunc.getDateWithTimezone(
            date.year(),
            date.month(),
            date.date(),
            timeZone
          ).getTime();
        });
      }

      $scope.datesToDisplay = $scope.datesToDisplay.join(", ");

      $scope.startDate = {
        model: new Date(),
        opened: false,
        open: function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.startDate.opened = true;
        }
      };
      
      $scope.endDate = {
        model: null,
        opened: false,
        open: function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.endDate.opened = true;
        }
      };

      $scope.action = "AVAILABLE_FOR_DAY";

      $scope.timeFrames = [{
        startTime: moment().hour(8).minute(0).toDate(),
        endTime:  moment().hour(16).minute(0).toDate()
      }];

      registerTimeFrameCorrector(0);

      function registerTimeFrameCorrector(index) {
        $scope.$watch("timeFrames[" + index + "].startTime", function(startTime) {
          if (!startTime) return;
          if ($scope.timeFrames[index].endTime < startTime) {
            $scope.timeFrames[index].endTime = startTime;
          }
        });

        $scope.$watch("timeFrames[" + index + "].endTime", function(endTime) {
          if (!endTime) return;

          if (endTime.getHours() === 0 && endTime.getMinutes() === 0) return;

          if ($scope.timeFrames[index].startTime > endTime) {
            $scope.timeFrames[index].startTime = endTime;
          }
        });
      }

      $scope.addTimeFrame = function() {
        if ($scope.timeFrames.length < 3) {
          $scope.timeFrames.push({
            startTime: moment().hour(8).minute(0).toDate(),
            endTime:  moment().hour(16).minute(0).toDate()
          });
          registerTimeFrameCorrector($scope.timeFrames.length - 1);
        }
      };

      $scope.removeTimeFrame = function(index) {
        $scope.timeFrames.splice(index, 1);
      };

      $scope.updateAvailability = function() {

        var timeFrames = _.map($scope.timeFrames, function(t) {
            return {
              startTime: moment.duration({ minutes: t.startTime.getMinutes(), hours: t.startTime.getHours()}).asMilliseconds(),
              endTime: moment.duration({ minutes: t.endTime.getMinutes(), hours: t.endTime.getHours()}).asMilliseconds()
            };
          }),
          startDate = appFunc.getDateWithTimezone(
            $scope.startDate.model.getFullYear(),
            $scope.startDate.model.getMonth(),
            $scope.startDate.model.getDate(),
            timeZone
          ).valueOf(),
          endDate = $scope.endDate.model && appFunc.getDateWithTimezone(
              $scope.endDate.model.getFullYear(),
              $scope.endDate.model.getMonth(),
              $scope.endDate.model.getDate(),
              timeZone
            ).valueOf();

        if ($scope.managerView) {
          if ($scope.availabilityType === "cd") {
            dataService.updateEmployeeCDAvailability(employeeId, dateRangeStart, dateRangeEnd, {
              action: $scope.action === "PTO" ? "UNAVAILABLE_FOR_DAY" : $scope.action,
              timeFrames: timeFrames,
              selectedDates: datesSelected,
              pto : $scope.action === "PTO",
              absenceTypeId : $scope.absenceType ? $scope.absenceType.id : null
            }).then(function(res) {
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
              $modalInstance.close(res.data);
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              $modalInstance.dismiss('cancel');
            });
          }
          else {
            dataService.updateEmployeeCIAvailability(employeeId, dateRangeStart, dateRangeEnd, {
              action: $scope.action,
              timeFrames: timeFrames,
              effectiveStartDate: startDate,
              effectiveEndDate: endDate,
              selectedDays: {
                sunday:     _.contains(datesSelected, "sunday"),
                monday:     _.contains(datesSelected, "monday"),
                tuesday:    _.contains(datesSelected, "tuesday"),
                wednesday:  _.contains(datesSelected, "wednesday"),
                thursday:   _.contains(datesSelected, "thursday"),
                friday:     _.contains(datesSelected, "friday"),
                saturday:   _.contains(datesSelected, "saturday")
              }
            }).then(function(res) {
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
              $modalInstance.close(res.data);
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              $modalInstance.dismiss('cancel');
            });
          }
        } else {
          if ($scope.availabilityType === "cd") {
            dataService.requestAvailabilityChange({
              "type": "AVAILABILITY_REQUEST",
              "submitterId": employeeId,
              "expiration": 0,
              "availUpdate":{
                "type": "AvailcalUpdateParamsCDAvailDto",
                "action": $scope.action,
                timeFrames: timeFrames,
                selectedDates: datesSelected
              }
            }).then(function(res) {
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
              $modalInstance.close(res.data.availCalDto);
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              $modalInstance.dismiss('cancel');
            });
          } else {
            dataService.requestAvailabilityChange({
              "type":"AVAILABILITY_REQUEST",
              "submitterId": employeeId,
              "expiration": 0,
              "availUpdate": {
                "type": "AvailcalUpdateParamsCIAvailDto",
                "action": $scope.action,
                "timeFrames": timeFrames,
                effectiveStartDate: startDate,
                effectiveEndDate: endDate,
                "selectedDays": {
                  "sunday":     _.contains(datesSelected, "sunday"),
                  "monday":     _.contains(datesSelected, "monday"),
                  "tuesday":    _.contains(datesSelected, "tuesday"),
                  "wednesday":  _.contains(datesSelected, "wednesday"),
                  "thursday":   _.contains(datesSelected, "thursday"),
                  "friday":     _.contains(datesSelected, "friday"),
                  "saturday":   _.contains(datesSelected, "saturday")
                }
              }
            }).then(function(res) {
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
              $modalInstance.close(res.data.availCalDto);
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              $modalInstance.dismiss('cancel');
            });
          }
        }
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };

    }]);