var scheduleBuilder = angular.module('emlogis.schedule_builder');

scheduleBuilder.controller('ScheduleBuilderCreateSettingsCtrl',
  [
    '$scope',
    '$modalInstance',
    '$state',
    'applicationContext',
    'dataService',
    '$modal',
    'scheduleService',
    'authService',
    'selectedSchedule',
    function ($scope, $modalInstance, $state, applicationContext, dataService, $modal, scheduleService, authService, selectedSchedule) {

      console.log('Schedule Builder Create Settings controller');

      $scope.schedule = angular.copy(selectedSchedule);

      $scope.selectTab = function (tabName) {
        $scope.selectedTab = tabName;
      };

      $scope.isValid = function () {
        return ($scope.schedule.state === 'Complete') && ($scope.schedule.executionStartDate > 0) && ($scope.schedule.executionEndDate > 0)
          && ($scope.schedule.executionStartDate < $scope.schedule.executionEndDate);
      };

      $scope.maxComputationTimes = [
        {label: 'Infinite', id: 0, val: -1},
        {label: '1  min', id: 1, val: 60},
        {label: '5  mins', id: 2, val: 300},
        {label: '10 mins', id: 3, val: 600},
        {label: '15 mins', id: 4, val: 900},
        {label: '30 mins', id: 5, val: 1800}
      ];

      $scope.maximumUnimprovedSecondsSpents = [
        {label: 'Default (100sec)', id: 0, val: 0},
        {label: '10 secs', id: 1, val: 10},
        {label: '30 secs', id: 2, val: 30},
        {label: '1 min', id: 3, val: 60},
        {label: '1 min 40 secs', id: 4, val: 100},
        {label: '2 mins', id: 5, val: 120},
        {label: '3 mins', id: 6, val: 180}
      ];

      $scope.standardOptimizationsGoals = [
        'AVOID_DAILY_OVERTIME_RULE',
        'AVOID_TWO_WEEK_OVERTIME_RULE',
        'AVOID_WEEKLY_OVERTIME_RULE'
      ];

      $scope.optimizationsType = 'standard';

      $scope.overrideOptions = ["None", "COP", "CPO", "OCP", "OPC", "PCO", "POC"];

      $scope.overrideCop = function () {
        if(!$scope.settings.overrideOptimizationPreference){
          $scope.previousOptimizationPreferenceSetting = $scope.settings.optimizationPreferenceSetting;
          $scope.settings.optimizationPreferenceSetting = $scope.settings.siteOptimizationPreferenceSetting;
        } else {
          $scope.settings.optimizationPreferenceSetting = $scope.previousOptimizationPreferenceSetting;
        }
      };

      //filter for standard goals
      $scope.containsGoal = function (goal) {
        return _.contains($scope.standardOptimizationsGoals, goal);
      };

      $scope.isScheduleEmpty = function () {
        return _.isEmpty($scope.schedule);
      };

      var executionDuration = $scope.schedule.executionEndDate - $scope.schedule.executionStartDate;
      $scope.executionDuration = new Date(executionDuration);

      $scope.overridesData = [];

      $scope.overridesGridOptions = {
        enableColumnResizing: true,
        enableScrollbars: true,
        paginationPageSize: 25,
        paginationCurrentPage: 1,
        data: 'overridesData | filter: employeeSearchText',
        rowTemplate: defaultRowTemplate(),
        enableGridMenu: false,
        enablePaginationControls: false,
        columnDefs: [
          {
            field: 'employeeName',
            displayName: 'Employee',
            minWidth: 150,
            pinnedLeft:true,
            enableColumnMenu: false
          }, {
            field: 'MIN_HOURS_WEEK_OVERRIDE'
          }, {
            field: 'AVOID_OVERTIME'
          }, {
            field: 'TEAM_FLOAT_ON'
          }, {
            field: 'EMPLOYEE_UNAVAILABLE'
          }, {
            field: 'TIME_WINDOW_UNAVAILABLE_OVERRIDE'
          }, {
            field: 'PTO_OVERRIDE'
          }, {
            field: 'ALL_DAY_UNAVAILABLE_OVERRIDE'
          }, {
            field: 'WEEKDAY_ROTATION_OVERRIDE'
          }, {
            field: 'COUPLED_WEEKEND_OVERRIDE'
          }, {
            field: 'DAYS_OFF_AFTER_OVERRIDE'
          }, {
            field: 'DAYS_OFF_BEFORE_OVERRIDE'
          }, {
            field: 'MIN_HOURS_BETWEEN_DAYS_OVERRIDE',
            minWidth: 80
          }, {
            field: 'MIN_HOURS_DAY_OVERRIDE'
          }, {
            field: 'MAX_HOURS_DAY_OVERRIDE'
          }, {
            field: 'MAX_HOURS_WEEK_OVERRIDE'
          }, {
            field: 'MAX_DAYS_WEEK_OVERRIDE'
          }, {
            field: 'MAX_CONSECUTIVE_DAYS_OVERRIDE'
          }
        ]
      };

      angular.forEach($scope.overridesGridOptions.columnDefs, function (col, i) {
        if (col.field !== 'employeeName'){
          col.enableSorting = false;
          col.selected = true;
          if (!angular.isDefined(col.minWidth)) col.minWidth = 70;
          col.cellTemplate = cellTpl();
          col.headerCellTemplate = headerCellTemplate();
          col.enableHiding = false;
        }
      });
      $scope.overridesGridOptions.onRegisterApi = function (gridApi) {
        $scope.gridApi = gridApi;
      };

      function cellTpl() {
        return '<div> <input type="checkbox" data-ng-model="row.entity[col.field]" data-ng-change="grid.appScope.extScope.change(row.entity, col.field)"></div>';
      }

      function headerCellTemplate() {
        return "<div ng-class=\"{ 'sortable': sortable }\">" +
          "<div class=\"ui-grid-cell-contents\" col-index=\"renderIndex\" title='{{grid.appScope.extScope.getTranslateName(col.field) CUSTOM_FILTERS | translate}}' >" +
          "<span translate>{{ grid.appScope.extScope.getTranslateName(col.field) CUSTOM_FILTERS }} </span> <br> <input type=\"checkbox\" data-ng-model=\"grid.appScope.headers[col.field]\" ng-change=\"grid.appScope.extScope.changeAll(col.field)\" >" +
          "<span ng-if=\"col.enableSorting\" ui-grid-visible=\"col.sort.direction\" ng-class=\"{ 'ui-grid-icon-up-dir': col.sort.direction == asc, 'ui-grid-icon-down-dir': col.sort.direction == desc, 'ui-grid-icon-blank': !col.sort.direction }\">&nbsp;</span>" +
          "</div>" +
            //"<div class=\"ui-grid-column-menu-button\" ng-if=\"grid.options.enableColumnMenus && !col.isRowHeader  && col.colDef.enableColumnMenu !== false\" class=\"ui-grid-column-menu-button\" ng-click=\"toggleMenu($event)\">" +
            //"<i class=\"ui-grid-icon-angle-down\">&nbsp;</i>" +
            //"</div>" +
          "<div ng-if=\"filterable\" class=\"ui-grid-filter-container\" ng-repeat=\"colFilter in col.filters\">" +
          "<input type=\"text\" class=\"ui-grid-filter-input\" ng-model=\"colFilter.term\" ng-click=\"$event.stopPropagation()\" ng-attr-placeholder=\"{{colFilter.placeholder || ''}}\">" +
          "<div class=\"ui-grid-filter-button\" ng-click=\"colFilter.term = null\">" +
          "<i class=\"ui-grid-icon-cancel right\" ng-show=\"!!colFilter.term\">&nbsp;</i> <!-- use !! because angular interprets 'f' as false -->" +
          "</div>" +
          "</div>" +
          "</div>";
      }

      function defaultRowTemplate() {
        return '<div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
      }


      function getSettings() {

        dataService.getScheduleSettings($scope.schedule.id)
          .then(function (settings) {
            $scope.settings = settings;

            var maxComputationTime = $scope.settings.maxComputationTime;
            var options = $scope.maxComputationTimes;
            var i;
            for (i = 0; i < options.length; i++) {
              if (options[i].val == maxComputationTime) {
                $scope.maxComputationTime = options[i];
                break;
              }
            }

            var maximumUnimprovedSecondsSpent = $scope.settings.maximumUnimprovedSecondsSpent;
            options = $scope.maximumUnimprovedSecondsSpents;
            for (i = 0; i < options.length; i++) {
              if (options[i].val == maximumUnimprovedSecondsSpent) {
                $scope.maximumUnimprovedSecondsSpent = options[i];
                break;
              }
            }

            $scope.rules = $scope.settings.ruleWeightMultipliers;
            $scope.rulesIsEnable = angular.copy($scope.settings.ruleWeightMultipliers);
            for (i in $scope.rulesIsEnable) {
              if ($scope.rulesIsEnable[i] > 0) {
                $scope.rulesIsEnable[i] = true;
              } else {
                $scope.rulesIsEnable[i] = false;
              }
            }
            $scope.rulesBackup = {};

            $scope.disableEnableRule = function (rule) {
              if ($scope.rulesIsEnable[rule]) {
                $scope.rules[rule] = $scope.rulesBackup[rule] || 1;
              } else {
                $scope.rulesBackup[rule] = $scope.rules[rule];
                $scope.rules[rule] = 0;
              }
            };


            $scope.employees = settings.employees;

            angular.forEach($scope.employees, function (emp) {
              emp.employeeName = emp.firstName + ' ' + emp.lastName;
              angular.forEach($scope.overridesGridOptions.columnDefs, function (opt) {
                if (opt.field !== 'employeeName') {
                  emp[opt.field] = false;
                }
              });
            });

            var opts = settings.overrideOptions;
            // values of columns top checkboxes (select/clear all)
            $scope.headers = {};
            angular.forEach(settings.overrideOptions, function (opt, key) {
              $scope.headers[key] = false;
            });
            for (i in opts) {
              var j;
              switch (opts[i].scope) {
                case "None":
                  break;
                case "All":
                  $scope.headers[i] = true;
                  for (j = 0; j < $scope.employees.length; j++) {
                    $scope.employees[j][i] = true;
                  }
                  break;
                case "Select":
                  for (j = 0; j < $scope.employees.length; j++) {
                    if (_.contains(opts[i].employeeIds, $scope.employees[j].id)) {
                      $scope.employees[j][i] = true;
                    }

                  }
                  break;
              }
            }
            $scope.overridesGridOptions.totalItems = $scope.employees.length;
            $scope.overridesData = $scope.employees;

          }, function (error) {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          });
      }

      $scope.extScope = {
        change: function (emp, optName) {
          var option = $scope.settings.overrideOptions[optName];
          var ids = option.employeeIds;
          if (emp[optName]) {
            ids.push(
              emp.id
            );
          } else {
            if (option.scope == "All") {
              for (var j = 0; j < $scope.employees.length; j++) {
                ids.push(
                  $scope.employees[j].id
                );
              }
            }
            var index = ids.indexOf(emp.id);
            ids.splice(index, 1);
          }
          switch (ids.length) {
            case 0:
              option.scope = "None";
              $scope.headers[optName] = false;
              break;
            case $scope.employees.length:
              option.scope = "All";
              ids.length = 0;
              $scope.headers[optName] = true;
              break;
            default :
              option.scope = "Select";
              $scope.headers[optName] = false;
              break;
          }
        },

        clear: function (optName) {
          var option = $scope.settings.overrideOptions[optName];
          var ids = option.employeeIds;
          option.scope = "None";
          ids.length = 0;
          for (var j = 0; j < $scope.employees.length; j++) {
            $scope.employees[j][optName] = false;
          }
        },

        all: function (optName) {
          var option = $scope.settings.overrideOptions[optName];
          var ids = option.employeeIds;
          option.scope = "All";
          ids.length = 0;
          for (var j = 0; j < $scope.employees.length; j++) {
            $scope.employees[j][optName] = true;
          }
        },

        changeAll: function (colName) {
          if ($scope.headers[colName]) {
            this.all(colName);
          } else {
            this.clear(colName);
          }
        },
        getTranslateName: function (name) {
          return 'overrides.' + name;
        }

      };

      /**
       * Generate Shifts
       */
      $scope.generateShifts = function () {
        dataService
          .generateShifts($scope.schedule.id)
          .then(function (elt) {
            $scope.schedule = elt;   // updated view with
            getSettings();
          }, function (error) {
            //dialogs.error("Error", error.data.message, 'lg');
            console.log("=====> Error: " + error.data.message);
          });
      };

      $scope.save = function () {
        var dto = $scope.settings;
        //dto.preservePreAssignedShifts = $scope.schedule.preservePreAssignedShifts;
        //childScope is isolate scope of bootstrap tab
        var childScope = angular.element('#general-settings').scope();
        dto.maxComputationTime = childScope.maxComputationTime.val;
        dto.maximumUnimprovedSecondsSpent = childScope.maximumUnimprovedSecondsSpent.val;
        updateScheduleSettings($scope.schedule.id, dto, 'Schedule settings has been updated successfully');
      };

      $scope.saveOverrides = function () {
        var dto = $scope.settings;
        updateScheduleSettings($scope.schedule.id, dto, 'Overrides has been updated successfully');
      };

      function updateScheduleSettings(scheduleId, updateDto, successMsg) {
        dataService
          .updateScheduleSettings(scheduleId, updateDto)
          .then(function (res) {
            $scope.close();
            applicationContext.setNotificationMsgWithValues(successMsg, 'success', true);
          }, function (error) {
            $scope.close();
            applicationContext.setNotificationMsgWithValues(JSON.stringify(error.data), 'danger', true);
          });
      }

      // Close Modal
      $scope.close = function () {
        //$modalInstance.dismiss('cancel');
        $modalInstance.dismiss('cancel');
      };

      $scope.hasPermission = function (perm) {
        return authService.hasPermission(perm);
      };

      getSettings();
    }
  ]
);
