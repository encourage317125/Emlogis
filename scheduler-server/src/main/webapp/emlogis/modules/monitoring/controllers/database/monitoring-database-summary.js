angular.module('emlogis.monitoring').controller('MonitoringDatabaseSummaryCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', 'applicationContext',
    function ($scope, $q, MonitoringService, $state, applicationContext) {

      $scope.getDbSummary = function () {
        var deferred = $q.defer();
        MonitoringService.getDbSummary().then(function (response) {
          if (response.data) {
            deferred.resolve(response.data);
          } else {
            deferred.reject('Error Occurred while trying to get Object');
          }
        }, function (error) {
          applicationContext.setNotificationMsgWithValues(error.data.message, 'danger', true, error.statusText);
          deferred.reject('Error Occurred while trying to get Object');
        });
        return deferred.promise;
      };

      $scope.dbInfo = [
        {
          field: 'Organization',
          name: 'monitoring.CUSTOMERS'
        }, {
          field: 'Site',
          name: 'monitoring.SITES'
        }, {
          field: 'Team',
          name: 'monitoring.TEAMS'
        }, {
          field: 'Employee',
          name: 'monitoring.EMPLOYEES'
        }, {
          field: 'UserAccount',
          name: 'monitoring.ACCOUNTS'
        }, {
          field: 'GroupAccount',
          name: 'monitoring.GROUPS'
        }, {
          field: 'Role',
          name: 'monitoring.ROLES'
        }, {
          field: 'ACE',
          name: 'monitoring.ACE'
        }, {
          field: 'Skill',
          name: 'monitoring.SKILLS'
        }, {
          field: 'AvailabilityTimeFrame',
          name: 'monitoring.AVAILABILITY_RECORDS',
          subTypes: [
            {
              field: 'CIAvailabilityTimeFrame',
              name: 'monitoring.CI_AVAILABILITY'
            }, {
              field: 'CDAvailabilityTimeFrame',
              name: 'monitoring.CD_AVAILABILITY'
            }
          ]
        }, {
          field: 'Contract',
          name: 'monitoring.CONTRACTS',
          subTypes: [
            {
              field: 'SiteContract',
              name: 'monitoring.SITE_CONTRACT'
            }, {
              field: 'TeamContract',
              name: 'monitoring.TEAM_CONTRACT'
            }, {
              field: 'EmployeeContract',
              name: 'monitoring.EMPLOYEE_CONTRACT'
            }
          ]
        }, {
          field: 'ContractLine',
          name: 'monitoring.CONTRACT_LINES',
          subTypes: [
            {
              field: 'BooleanCL',
              name: 'monitoring.BOOLEAN_CL'
            }, {
              field: 'IntMinMaxCL',
              name: 'monitoring.INT_MIN_MAX_CL'
            }, {
              field: 'WeekdayRotationPatternCL',
              name: 'monitoring.WEEKDAY_ROTATION_PATTERN_CL'
            }, {
              field: 'WeekendWorkPatternCL',
              name: 'monitoring.WEEKEND_WORK_PATTERN_CL'
            }
          ]
        }, {
          field: 'ShiftLength',
          name: 'monitoring.SHIFT_LENGTHS'
        }, {
          field: 'ShiftType',
          name: 'monitoring.SHIFT_TYPES'
        }, {
          field: 'Schedule',
          name: 'monitoring.SCHEDULES',
          subTypes: [
            {
              field: 'posted',
              name: 'monitoring.SCHEDULES_POSTED'
            }, {
              field: 'production',
              name: 'monitoring.SCHEDULES_PRODUCTION'
            }, {
              field: 'simulation',
              name: 'monitoring.SCHEDULES_SIMULATION'
            }
          ]
        }, {
          field: 'ScheduleChange',
          name: 'monitoring.SCHEDULE_CHANGES'
        }, {
          field: 'ShiftPattern',
          name: 'monitoring.SHIFT_PATTERNS'
        }, {
          field: 'ShiftReq',
          name: 'monitoring.SHIFT_REQS'
        }, {
          field: 'Shift',
          name: 'monitoring.SHIFTS'
        }, {
          field: 'PatternElt',
          name: 'monitoring.SHIFT_REQS_ELTS'
        }, {
          field: 'PostedOpenShift',
          name: 'monitoring.POSTED_OPEN_SHIFTS',
          subTypes: [
            {
              field: 'current',
              name: 'monitoring.POSTED_OPEN_SHIFTS_CURRENT'
            }, {
              field: 'passed',
              name: 'monitoring.POSTED_OPEN_SHIFTS_PASSED'
            }
          ]
        }
      ];

      $scope.loadData = function () {
        $scope.getDbSummary()
          .then(function (dbInfo) {
            angular.forEach($scope.dbInfo, function (elt, index) {
              if (angular.isDefined(dbInfo[elt.field].total)) {
                elt.value = dbInfo[elt.field].total;
                angular.forEach(elt.subTypes, function (elt1, index1) {
                  elt1.value = dbInfo[elt.field][elt1.field];
                });
              } else {
                elt.value = dbInfo[elt.field];
              }
            });
          });
      };

      $scope.refreshInterval = 5;

      $scope.loadData();
      MonitoringService.refresh(function () {
        $scope.loadData();
      }, $scope.refreshInterval);
    }
  ]);