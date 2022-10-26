angular.module('emlogis.rules').controller('RulesSkillsCtrl',
    ['$scope', '$modal', '$translate', '$q', 'appFunc', 'crudDataService', 'dataService', 'dialogs', 'applicationContext',
    function($scope, $modal, $translate, $q, appFunc, crudDataService, dataService, dialogs, applicationContext) {

      var originalSkills = [],
          translations = {
            available: 'Available',
            neverEnd: 'Never End',
            skillName: 'Skill Name',
            abbreviation: 'Abbreviation',
            active: 'Active',
            inactive: 'Inactive'
          };

      $translate(["rules.skills.AVAILABLE",
                  "rules.skills.NEVER_END",
                  "rules.skills.SKILL_NAME",
                  "rules.skills.ABBREVIATION",
                  "rules.skills.ACTIVE",
                  "rules.skills.INACTIVE"]).
          then(function (translation) {
            translations.available =  translation["rules.skills.AVAILABLE"];
            translations.neverEnd =  translation["rules.skills.NEVER_END"];
            translations.skillName =  translation["rules.skills.SKILL_NAME"];
            translations.abbreviation =  translation["rules.skills.ABBREVIATION"];
            translations.active =  translation["rules.skills.ACTIVE"];
            translations.inactive =  translation["rules.skills.INACTIVE"];
          });


      $scope.skills = [];
      $scope.organization = null;
      $scope.skillsReceived = false;

      $scope.fieldsToSort = [
        {name: 'name', buttonLabel: translations.skillName},
        {name: 'abbreviation', buttonLabel: translations.abbreviation}
      ];

      $scope.fieldsToFilter = [
        {name: 'isActive', value: true, buttonLabel: translations.active},
        {name: 'isActive', value: false, buttonLabel: translations.inactive}
      ];

      // open calendar control
      $scope.openCalendar = function($event, sObj, propName) {
        $event.preventDefault();
        $event.stopPropagation();
        sObj[propName] = true;
      };

      // open modal window
      $scope.openModal = function (sObj) {
        var modalInstance = $modal.open({
          templateUrl: 'myModalContent.html',
          controller: 'SkillsModalCtrl',
          size: 'lg',
          resolve: {
            sObj: function() {
              return sObj;
            }
          }
        });
      };

      $scope.getTeamsForSkill = function(sObj) {
        if (sObj.teamsForSkill || !sObj.id) return;

        dataService.getTeamAssociations(sObj.id).
            then(function(res) {
              sObj.teamsForSkill = _.groupBy(res.data, function(t) {
                return t.siteName;
              });
            },
            function (err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            });
      };

      $scope.saveSkill = function($event, sObj) {
        if ($event) {
          $event.preventDefault();
          $event.stopPropagation();
        }

        // In case if skill is invalid
        if (!sObj.name || !sObj.abbreviation ||
            sObj.calendarStartDate === undefined ||
            sObj.calendarEndDate === undefined) {
          return $q.reject({});
        }

        if (sObj.id) {
          return updateSkill(sObj);
        } else {
          return createSkill(sObj);
        }
      };

      function updateSkill(sObj) {
        var startDate = 0, endDate = 0;

        if (sObj.calendarStartDate) {
          startDate = appFunc.getDateWithTimezone(
              sObj.calendarStartDate.getFullYear(),
              sObj.calendarStartDate.getMonth(),
              sObj.calendarStartDate.getDate(),
              $scope.organization.timeZone
          ).getTime();
        }

        if (sObj.calendarEndDate) {
          endDate = appFunc.getDateWithTimezone(
              sObj.calendarEndDate.getFullYear(),
              sObj.calendarEndDate.getMonth(),
              sObj.calendarEndDate.getDate(),
              $scope.organization.timeZone
          ).getTime();
        }

        return dataService.updateSkill(sObj.id, {
          name: sObj.name,
          abbreviation: sObj.abbreviation,
          description: sObj.description,
          startDate: startDate,
          endDate: endDate,
          isActive: sObj.isActive
        }).
        then(function(res) {
          applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
          sObj.originalName = sObj.name;

          if (res.data.startDate) {
            sObj.startDate = appFunc.convertToBrowserTimezone(res.data.startDate, $scope.organization.timeZone);
          } else {
            sObj.startDate = 0;
          }
          if (res.data.endDate) {
            sObj.endDate = appFunc.convertToBrowserTimezone(res.data.endDate, $scope.organization.timeZone);
          } else {
            sObj.endDate = 0;
          }
          originalSkills = _.cloneDeep($scope.skills);
        },
        function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
      }

      function createSkill(sObj) {
        var startDate = 0, endDate = 0;

        sObj.submitted = true;

        if (sObj.calendarStartDate) {
          startDate = appFunc.getDateWithTimezone(
              sObj.calendarStartDate.getFullYear(),
              sObj.calendarStartDate.getMonth(),
              sObj.calendarStartDate.getDate(),
              $scope.organization.timeZone
          ).getTime();
        }

        if (sObj.calendarEndDate) {
          endDate = appFunc.getDateWithTimezone(
              sObj.calendarEndDate.getFullYear(),
              sObj.calendarEndDate.getMonth(),
              sObj.calendarEndDate.getDate(),
              $scope.organization.timeZone
          ).getTime();
        }

        return dataService.createSkill({
          name: sObj.name,
          abbreviation: sObj.abbreviation,
          description: sObj.description,
          startDate: startDate,
          endDate: endDate,
          isActive: sObj.isActive
        }).
        then(function(res) {
          applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
          sObj.id = res.data.id;
          sObj.originalName = sObj.name;

          if (res.data.startDate) {
            sObj.startDate = appFunc.convertToBrowserTimezone(res.data.startDate, $scope.organization.timeZone);
          } else {
            sObj.startDate = 0;
          }
          if (res.data.endDate) {
            sObj.endDate = appFunc.convertToBrowserTimezone(res.data.endDate, $scope.organization.timeZone);
          } else {
            sObj.endDate = 0;
          }
          originalSkills = _.cloneDeep($scope.skills);
        },
        function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
      }

      $scope.showValidationMessages = function(sObj) {
        if (sObj.id) {
          return true;
        } else {
          return sObj.submitted;
        }
      };

      $scope.addRowForNewSkill = function() {

        var newSkillOpened = _.find($scope.skills, function(s) {
          return s.id === undefined;
        });

        // do not add new row if there is one already
        if (newSkillOpened) return;

        var newSkill = {
          name: null,
          abbreviation: null,
          description: null,
          calendarStartDate: moment().toDate(),
          calendarEndDate: null,
          isActive: true,
          isCollapsed: false
        };

        $scope.skills.unshift(newSkill);
      };

      $scope.resetSkill = function(sObj) {
        // if canceled editing a new skill - remove it from table
        if (!sObj.id) {
          removeNewSkills();
          return;
        }

        //if canceled editing an existing skill - restore it to previous state
        var originalSkill = _.clone(_.find(originalSkills, function(s) {
          return sObj.id == s.id;
        }));

        // do not reset skill teams because you add/remove them in another window
        originalSkill.teamsForSkill = sObj.teamsForSkill;

        // leave skill in expanded state
        originalSkill.isCollapsed = false;

        // replace holiday object with original one
        $scope.skills = _.map($scope.skills, function(s) {
          return (s.id === sObj.id) ? originalSkill : s;
        });
      };

      function removeNewSkills() {
        $scope.skills = _.reject($scope.skills, function(s) {
          return s.id === undefined;
        });
      }

      $scope.deleteSkill = function($event, sObj) {
        $event.preventDefault();
        $event.stopPropagation();

        // if canceled editing a new skill - remove it from table
        if (!sObj.id) {
          removeNewSkills();
          return;
        }

        var confirm = dialogs.confirm('app.PLEASE_CONFIRM', 'rules.skills.ARE_YOU_SURE_REMOVE_SKILL?');

        confirm.result.then(function (btn) {
          dataService.deleteSkill(sObj.id)
              .then(function (res) {
                applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
                $scope.skills = _.reject($scope.skills, function(s) {
                  return s.id === sObj.id;
                });
                originalSkills = _.cloneDeep($scope.skills);
              },
              function (err) {
                applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              });
        }, function (btn) {
          // no pressed. Do nothing
        });
      };

      $scope.toReadableFormat = function(date, noDateText) {
        if (date) {
          return moment(date).format("MM/DD/YY");
        } else {
          switch(noDateText) {
            case 'Available':
              noDateText = translations.available;
              break;
            case 'Never End':
              noDateText = translations.neverEnd;
              break;
          }
          return noDateText;
        }
      };

      dataService.getSkills({}, 1, -1)
        .then(
          function (res) {
            $scope.skills = res.data;
            $scope.skillsReceived = true;

            angular.forEach($scope.skills, function (s) {
              s.isCollapsed = true;
              s.originalName = s.name;
            });

            // GET Current organization details
            return $scope.organization || crudDataService.getElement('org', '');
          },
          function (err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          })
        .then(
          function(res) {
            $scope.organization = res;
            /**
             * convert dates to browser's time stack,
             */
            angular.forEach($scope.skills, function(s) {
              if (s.startDate) {
                s.calendarStartDate = appFunc.convertToBrowserTimezone(s.startDate, $scope.organization.timeZone);
              } else {
                s.calendarStartDate = null;
              }
              if (s.endDate) {
                s.calendarEndDate = appFunc.convertToBrowserTimezone(s.endDate, $scope.organization.timeZone);
              } else {
                s.calendarEndDate = null;
              }
            });

            originalSkills = _.cloneDeep($scope.skills);
          },
          function (err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });

      //--------------------------------------------------------------------
      // Setup a window for asking to save the changes
      //--------------------------------------------------------------------

      var working = applicationContext.getWorking();

      working.entityName = 'rules.SKILLS';
      working.option = {
        editing: false,
        submitted: false
      };
      working.saveFunc = function() {
        var promises = [];
        _.each($scope.unsavedSkills, function(s) {
          promises.push($scope.saveSkill(null, s));
        });
        working.option.editing = false;

        return $q.all(promises);
      };


      function skillsAreEqual(s1, s2) {
        var areEqual = true,
          propertiesToCheck = ['name', 'abbreviation', 'description', 'calendarStartDate', 'calendarEndDate'];
        _.each(propertiesToCheck, function(prop) {
          if (!_.isEqual(s1[prop], s2[prop])) {
            areEqual = false;
          }
        });
        return areEqual;
      }


      $scope.$watch('skills', function(skills) {
        $scope.unsavedSkills = [];
        working.option.editing = false;
        _.each(skills, function(s) {
          var originalSkill = _.find(originalSkills, 'id', s.id);
          if (s && originalSkill && !skillsAreEqual(s, originalSkill)) {
            $scope.unsavedSkills.push(s);
            working.option.editing = true;
          }
        });
      }, true);
}]);



angular.module('emlogis.rules').controller('SkillsModalCtrl',
  ['$scope', '$timeout', '$modalInstance', 'dataService', 'applicationContext', 'sObj',
  function ($scope, $timeout, $modalInstance, dataService, applicationContext, sObj) {

    $scope.sObj = sObj;
    sObj.gridOptions = null;

    dataService.getSiteTeamAssociations(sObj.id).
      then(function(res) {

        function rowTemplate() {
          return '<div>' +
                    '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" ' +
                      'class="ui-grid-cell" ' +
                      'ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader,          '  +
                      '            \'team-to-add\': grid.appScope.teamToAdd(row),         '  +
                      '            \'team-to-remove\': grid.appScope.teamToRemove(row)}"  '  +
                      ' ui-grid-cell>' +
                    '</div>' +
                  '</div>';
        }

        $scope.teamToAdd = function(row) {
          return row.entity.teamHasSkill === false && row.isSelected === true;
        };

        $scope.teamToRemove = function(row) {
          return row.entity.teamHasSkill === true && row.isSelected === false;
        };


        sObj.gridOptions = {
          data: res.data,
          enableHorizontalScrollbar: 0,
          enableVerticalScrollbar: 1,
          enableColumnMenus: false,
          enableFiltering: false,
          enableSorting: true,
          needPagination: false,
          enableSelectAll: true,
          enableRowSelection: true,
          columnDefs: [
            { field: 'teamName'},
            { field: 'siteName'},
            { field: 'teamDescription'},
            { field: 'teamid',   visible: false },
            { field: 'siteid',   visible: false }
          ],
          rowTemplate: rowTemplate(),
          onRegisterApi: function(gridApi) {
            $scope.gridApi = gridApi;

            $timeout(function() {
              _.each(sObj.gridOptions.data, function(t) {
                // convert string "true/false" to boolean
                t.teamHasSkill = (t.teamHasSkill === "true");
                if (t.teamHasSkill) {
                  $scope.gridApi.selection.selectRow(t);
                }
              });
            });
          }
        };
      },
      function (err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });


    $scope.updateTeamAssociations = function() {
      var selectedRows = $scope.gridApi.selection.getSelectedRows(),
          teams = _.pluck(selectedRows, 'teamId');

      dataService.updateTeamAssociations(sObj.id, teams).
          then(
            function(res) {
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
              sObj.teamsForSkill = _.groupBy(res.data, function(t) {
                return t.siteName;
              });
              $scope.cancel();
            },
            function (err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            });
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
}]);

