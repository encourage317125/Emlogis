var scheduleBuilder = angular.module('emlogis.schedule_builder');

scheduleBuilder.controller('ScheduleBuilderGenerationReportCtrl',
  [
    '$scope',
    '$state',
    '$stateParams',
    '$http',
    '$filter',
    'applicationContext',
    'dataService',
    '$modal',
    'scheduleService',
    'authService',
    'uiGridConstants',
    function ($scope, $state, $stateParams, $http, $filter, applicationContext, dataService, $modal, scheduleService,
              authService, uiGridConstants) {

      $scope.Math = window.Math;

      /**
       * open schedule action, it will open up a dialog box
       */
      $scope.openScheduleAction = function () {
        var dlg = $modal.open({
          templateUrl: 'scheduleSelectorModal.html',
          controller: 'ScheduleSelectorModalInstanceCtrl',
          size: 'lg',
          windowClass: 'schedules-modal',
          resolve: {
            checkboxModel: function () {
              return {
                simulationGenerated: true,
                production: false,
                posted: false
              };
            },
            customFilter: function() {
              return {key: 'states', value: [5]};
            }
          }
        });
        return dlg.result.then(function(selectedSchedule) {
          var tmp = scheduleService.getShared();
          tmp.schedule = null;
          scheduleService.setShared(tmp);
          $state.go('authenticated.schedule_builder.generation_report', {'id': selectedSchedule.id});
        }, function (reason) {
          console.log('dismissed');
          //return $q.reject(reason);
        });
      };

      var temp = scheduleService.getShared();
      $scope.schedule = temp.schedule;

      $scope.isScheduleEmpty = function () {
        return _.isEmpty($scope.schedule);
      };

      if ($scope.isScheduleEmpty() || (!$scope.isScheduleEmpty() && $scope.schedule.shiftGenerationDuration === -1)) {
        if ($stateParams.id !== undefined) {
          if (($stateParams.id !== '')) {
            $http.get(applicationContext.getBaseRestUrl() + 'schedules/' + $stateParams.id, {})
              .then(function (res) {
                $scope.schedule = res.data;
                var tmp = scheduleService.getShared();
                tmp.schedule = $scope.schedule;
                scheduleService.setShared(tmp);
                $scope.loadReport();
              });
          } else {
            $scope.openScheduleAction();
          }
        }
      }

      $scope.isValid = function () {
        return $scope.schedule && ($scope.schedule.state === 'Complete') && ($scope.schedule.executionStartDate > 0) && ($scope.schedule.executionEndDate > 0)
          && ($scope.schedule.executionStartDate < $scope.schedule.executionEndDate);
      };

      $scope.initVars = function () {
        $scope.employeeSummaryGridData = [];
        $scope.employeeGridData = [];
        $scope.openShiftsGridData = [];
        $scope.summaryBySkillGridData = [];
        $scope.scheduleExecutionReport = {};
      };

      $scope.loadReport = function () {
        $scope.initVars();
        if (!$scope.isScheduleEmpty()) {
          getReport();
          var executionDuration = $scope.schedule.executionEndDate - $scope.schedule.executionStartDate;
          $scope.executionDuration = new Date(executionDuration);
        }
      };

      $scope.loadReport();


      $scope.selectMainTab = function (tabName) {
        $scope.selectedMainTab = tabName;
      };

      $scope.selectSubTab = function (tabName) {
        $scope.selectedSubTab = tabName;
      };

      var pageSize = 25;

      $scope.summaryBySkillOptions = {
        enableColumnResizing: true,
        enableScrollbars: false,
        paginationPageSize: pageSize,
        enablePaginationControls: false,
        data: 'summaryBySkillGridData',
        rowHeight: 70,
        //rowTemplate: defaultRowTemplate(),
        columnDefs: [
          {field: 'name', displayName: 'Summary By Skill', minWidth: 150},
          {field: 'resources', displayName: 'Resources', minWidth: 150},
          {field: 'resourcesAssignments', displayName: 'Assigned Resources', minWidth: 150},
          {field: 'shifts', displayName: 'Shifts', minWidth: 150, cellTemplate: skillCountsTpl()},
          {field: 'shiftsDetails', visible: false},
          {
            field: 'assignedShifts',
            displayName: 'Assigned Shifts',
            minWidth: 150,
            cellTemplate: skillCountsShiftsAssignTpl()
          },
          {field: 'assignedShiftsDetails', visible: false},
          {field: 'openShifts', displayName: 'Open Shifts', minWidth: 150, cellTemplate: skillCountsTpl()},
          {field: 'openShiftsDetails', visible: false},
          {field: 'hours', displayName: 'Hours', minWidth: 150, cellTemplate: skillCountsTpl()},
          {field: 'hoursDetails', visible: false},
          {field: 'assignedHours', displayName: 'Assigned Hours', minWidth: 150, cellTemplate: skillCountsTpl()},
          {field: 'assignedHoursDetails', visible: false},
          {field: 'resourcesHours', displayName: 'Resources Hours', minWidth: 150}
        ]
      };

      $scope.employeeGridOptions = {
        enableColumnResizing: true,
        enableScrollbars: false,
        paginationPageSize: pageSize,
        enablePaginationControls: false,
        data: 'employeeGridData | filter: employeeScheduleSearchText',
        rowTemplate: defaultRowTemplate(),
        columnDefs: [
          {field: 'employeeName', displayName: 'Employee', minWidth: 150},
          {field: 'employeeSkills', displayName: 'Employee Skills', minWidth: 150, cellTemplate: skillCellTpl()},
          {field: 'totalHours'},
          {field: 'availableHours'}
        ]
      };

      $scope.employeeGridOptions.onRegisterApi = onRegApi;

      $scope.employeeSummaryGridOptions = {
        enableScrollbars: false,
        paginationPageSize: pageSize,
        enablePaginationControls: false,
        data: 'employeeSummaryGridData | filter: employeeSummarySearchText',
        rowTemplate: defaultRowTemplate(),
        columnDefs: [
          {field: 'employeeName', displayName: 'Employee'},
          {field: 'employeeSkills', displayName: 'Employee Skills', cellTemplate: skillCellTpl()},
          {field: 'totalHours'},
          {field: 'availableHours'},
          {field: 'hardScore', cellTemplate: cellTpl('col.field')},
          {field: 'mediumScore', cellTemplate: cellTpl('col.field')},
          {field: 'softScore'}
        ]
      };

      $scope.employeeSummaryGridOptions.onRegisterApi = onRegApi;

      $scope.openShiftsGridOptions = {
        enableScrollbars: false,
        useExternalPagination: true,
        useExternalSorting: true,
        paginationPageSize: pageSize,
        enablePaginationControls: false,
        data: 'openShiftsGridData | filter: tempsearchtext',
        rowTemplate: defaultRowTemplate(),
        columnDefs: [
          {field: 'id', minWidth: 150},
          {field: 'startDateTime', minWidth: 150},
          {field: 'endDateTime', minWidth: 150},
          {field: 'teamName', minWidth: 150},
          {field: 'shiftLengthName', minWidth: 150},
          {field: 'skillName', minWidth: 150}
        ],
        paginationCurrentPage: 1,
        queryParams: {
          filter: 'assignmentType is null',
          returnedfields: 'id,startDateTime,endDateTime,teamName,shiftLengthName,skillName'
        }
      };

      $scope.openShiftsGridOptions.onRegisterApi = function (gridApi) {
        $scope.gridApi = gridApi;
        getOpenShifts();
        $scope.gridApi.core.on.sortChanged($scope, function (grid, sortColumns) {

          $scope.openShiftsGridOptions.queryParams.orderby = sortColumns[0] ? sortColumns[0].field : undefined;
          $scope.openShiftsGridOptions.queryParams.orderdir = sortColumns[0] ? sortColumns[0].sort.direction.toUpperCase() : undefined;
          getOpenShifts();
        });
        $scope.gridApi.pagination.on.paginationChanged($scope, function (newPage) {
          //$scope.openShiftsGridOptions.paginationPageSize = pageSize;
          $scope.openShiftsGridOptions.paginationCurrentPage = newPage;
          getOpenShifts();
        });
      };

      function getOpenShifts() {
        var params = _.pick($scope.openShiftsGridOptions.queryParams, 'returnedfields', 'filter', 'orderby', 'orderdir');
        params.offset = ($scope.openShiftsGridOptions.paginationCurrentPage - 1) * $scope.openShiftsGridOptions.paginationPageSize;
        params.limit = $scope.openShiftsGridOptions.paginationPageSize;
        dataService.getScheduleShiftsOps($scope.schedule.id, params)
          .then(function (res) {
            createOpenShiftsGridData(res.data.result);
            $scope.openShiftsGridOptions.totalItems = res.data.total;
          }, function (err) {
            console.log("=====> Error: " + err.data.message);
          });
      }

      $scope.tplGridOptions = {
        enableScrollbars: false,
        paginationPageSize: pageSize,
        enablePaginationControls: false,
        data: null,
        rowTemplate: defaultRowTemplate(),
        columnDefs: [
          {field: 'employeeName', displayName: 'Employee', enableFiltering: true},
          {
            field: 'employeeSkills',
            displayName: 'Employee Skills',
            enableFiltering: true,
            cellTemplate: skillCellTpl()
          },
          {field: 'weightsDetails', visible: false}
        ]
      };

      function onRegApi(gridApi) {
        $scope.gridApi = gridApi;
      }

      function cellTpl(constrName) {
        return '<div title="{{grid.appScope.extScope.getDetails(row.entity[col.field+\'Details\'])}}" ' +
          'class="gridCell">' +
          '<span class="{{grid.appScope.extScope.isHard(row.entity[col.field], ' + constrName + ')}}">{{row.entity[col.field]}}</span> ' +
          '<span>{{grid.appScope.extScope.getUnit(' + constrName + ', row.entity[col.field])}} ' +
          '{{grid.appScope.extScope.getDetails(row.entity[col.field+"Details"])}}</span></div>';
      }

      function skillCellTpl() {
        return '<div  ' +
          'class="gridCell">' +
          '<span style="font-weight: bold">{{row.entity[col.field][0].join()}}</span> ' +
          '<span>/ {{row.entity[col.field][1].join()}}</span></div>';
      }

      function skillCountsTpl() {
        return '<div class="gridCell"> ' +
          '<div>Total: {{row.entity[col.field+\'Details\'].total}}</div>' +
          '<div>Regular: {{row.entity[col.field+\'Details\'].regular}}</div>' +
          '<div ng-if="row.entity[col.field+\'Details\'].excess>0">Excess: {{row.entity[col.field+\'Details\'].excess}}</div>' +
          '</div>';
      }

      function skillCountsShiftsAssignTpl() {
        return '<div class="gridCell"> ' +
          '<div >' + //row.entity[col.field+\'Details\'].total/row.entity[\'shiftsDetails\'].total<0.6
          'Total: <span class="{{grid.appScope.getShiftCountColor(row.entity[col.field+\'Details\'].total, row.entity[\'shiftsDetails\'].total)}}">' +
          '{{row.entity[col.field+\'Details\'].total }}</span> {{grid.appScope.getShiftPersent(row.entity[col.field+\'Details\'].total, row.entity[\'shiftsDetails\'].total)}}' +
          '</div>' +
          '<div>Regular: {{row.entity[col.field+\'Details\'].regular}}</div>' +
          '<div ng-if="row.entity[col.field+\'Details\'].excess>0">Excess: {{row.entity[col.field+\'Details\'].excess}}</div>' +
          '</div>';
      }

      function defaultRowTemplate() {
        return '<div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
      }

      function headerCellTemplate() {
        return "<div ng-class=\"{ 'sortable': sortable }\">" +
          "<div class=\"ui-grid-vertical-bar\">&nbsp;</div>" +
          "<div class=\"ui-grid-cell-contents\" col-index=\"renderIndex\" ><span translate>{{grid.appScope.extScope.getTranslateName(col.name) CUSTOM_FILTERS }} </span>" +
          "<span>(<span class=\"{{grid.appScope.extScope.isHard(grid.appScope.extScope.getTotal(col.field), col.field)}}\">{{grid.appScope.extScope.getTotal(col.field)}}</span>)" +
          "<span ui-grid-visible=\"col.sort.direction\" class='ui-grid-icon-my' ng-class=\"{ 'ui-grid-icon-up-dir': col.sort.direction == asc, 'ui-grid-icon-down-dir': col.sort.direction == desc, 'ui-grid-icon-blank': !col.sort.direction }\">&nbsp;</span></span>" +
          "</div>" +
          "<div class=\"ui-grid-column-menu-button\" ng-if=\"grid.options.enableColumnMenus && !col.isRowHeader  && col.colDef.enableColumnMenu !== false\" class=\"ui-grid-column-menu-button\" ng-click=\"toggleMenu($event)\">" +
          "<i class=\"ui-grid-icon-angle-down\">&nbsp;</i>" +
          "</div>" +
          "<div ng-if=\"filterable\" class=\"ui-grid-filter-container\" ng-repeat=\"colFilter in col.filters\">" +
          "<input type=\"text\" class=\"ui-grid-filter-input\" ng-model=\"colFilter.term\" ng-click=\"$event.stopPropagation()\" ng-attr-placeholder=\"{{colFilter.placeholder || ''}}\">" +
          "<div class=\"ui-grid-filter-button\" ng-click=\"colFilter.term = null\">" +
          "<i class=\"ui-grid-icon-cancel right\" ng-show=\"!!colFilter.term\">&nbsp;</i> <!-- use !! because angular interprets 'f' as false -->" +
          "</div>" +
          "</div>" +
          "</div>";
      }

      function getReport() {

        if ($scope.schedule.returnedOpenShifts == -1 || $scope.schedule.returnedAssignedShifts == -1) {
          return;
        } else {
          $scope.readyEmployeeGridData = false;
          dataService.getScheduleReport($scope.schedule.id)
            .then(function (report) {
              $scope.$emit('event:loadSchedule');

              $scope.report = report;
              $scope.report.completionReport = angular.fromJson(report.completionReport);
              createSummaryBySkillData();
              if ($scope.report.completionReport !== null) {
                createEmployeeScheduleGridData();
                createEmployeeSummaryGridData();
                createScheduleExecutionGridOptions();
              }
              $scope.readyEmployeeGridData = true;
            }, function (error) {
              $scope.readyEmployeeGridData = true;
              console.log("=====> Error: " + error.data.message);
            });
        }
      }

      function createSummaryBySkillData() {
        angular.forEach($scope.report.summaryBySkill, function (elem, index) {
          var gridRow = {};
          //gridRow.id = elem.id;
          gridRow.name = elem.name;
          gridRow.resources = elem.report.resources;
          gridRow.resourcesAssignments = elem.report.resourcesAssignments;
          gridRow.shifts = elem.report.shifts.total;
          gridRow.shiftsDetails = elem.report.shifts;
          gridRow.assignedShifts = elem.report.assignedShifts.total;
          gridRow.assignedShiftsDetails = elem.report.assignedShifts;
          gridRow.openShiftsDetails = {
            regular: gridRow.shiftsDetails.regular - gridRow.assignedShiftsDetails.regular,
            excess: gridRow.shiftsDetails.excess - gridRow.assignedShiftsDetails.excess,
            total: gridRow.shiftsDetails.total - gridRow.assignedShiftsDetails.total
          };
          gridRow.openShifts = gridRow.openShiftsDetails.total;
          gridRow.hours = elem.report.hours.total;
          gridRow.hoursDetails = elem.report.hours;
          gridRow.assignedHours = elem.report.assignedHours.total;
          gridRow.assignedHoursDetails = elem.report.assignedHours;
          gridRow.resourcesHours = elem.report.resourcesHours[0];
          $scope.summaryBySkillGridData.push(gridRow);
        });
      }

      // create model for employee Schedule Report
      function createEmployeeScheduleGridData() {
        $scope.employeeGridOptions.columnDefs = [
          {field: 'employeeName', displayName: 'Employee', minWidth: 150},
          {field: 'employeeSkills', displayName: 'Employee Skills', minWidth: 150, cellTemplate: skillCellTpl()},
          {field: 'totalHours'},
          {field: 'availableHours'}
        ];
        var i;
        var completionReportAll = [];
        for (i = 0; i < $scope.report.completionReport.length; i++) {
          if ($scope.report.completionReport[i].constraintName !== 'requiredEmployeeSizePerShift') {
            $scope.employeeGridOptions.columnDefs.push({
                field: $scope.report.completionReport[i].constraintName,
                cellTemplate: cellTpl('col.field'),
                headerCellTemplate: headerCellTemplate(),
                minWidth: 100
              }
            );
            $scope.employeeGridOptions.columnDefs.push({
                field: $scope.report.completionReport[i].constraintName + 'Details',
                visible: false
              }
            );
            for (var j = 0; j < $scope.report.completionReport[i].constraintMatchSet.length; j++) {
              completionReportAll.push($scope.report.completionReport[i].constraintMatchSet[j]);
            }
          }
        }
        var completionReportAllGrouped = _.groupBy(
          completionReportAll,
          function (elem) {
            return elem.employeeId;
          }
        );
        $scope.report.employeeScheduleReport = _.map(
          completionReportAllGrouped, iteratorReport);

        angular.forEach($scope.report.employeeScheduleReport, function (elem, index) {
          var gridRow = {};
          if (elem.employeeId !== '') {
            gridRow.employeeName = elem.employeeName;
            gridRow.employeeSkills = elem.employeeSkills;
            gridRow.totalHours = $scope.getEmployeeTotalHours(elem.employeeId);
            gridRow.availableHours = $scope.getEmployeeAvailableHours(elem.employeeId);
            for (i = 0; i < $scope.report.completionReport.length; i++) {
              var cName = $scope.report.completionReport[i].constraintName;
              if (elem.weightsPerConstr[cName]) {
                gridRow[cName + 'Details'] = elem.weightsPerConstr[cName];
                gridRow[cName] = _.reduce(elem.weightsPerConstr[cName], function (memo, num) {
                  return memo + num;
                }, 0);
              }
            }
            $scope.employeeGridData.push(gridRow);
          }

        });

      }

      function createEmployeeSummaryGridData() {
        var i;
        angular.forEach($scope.report.employees, function (empl, index) {
          var gridRow = {};

          gridRow.employeeName = empl.firstName + ' ' + empl.lastName;
          gridRow.employeeSkills = $scope.getSkills(empl);
          gridRow.totalHours = empl.totalHours;
          gridRow.availableHours = empl.availableHours;

          var emplInReport = $scope.findInArrayByProperty($scope.report.employeeScheduleReport, 'employeeId', empl.id);
          if (emplInReport) {
            gridRow.hardScore = 0;
            gridRow.softScore = 0;
            gridRow.mediumScore = 0;
            for (i = 0; i < $scope.report.completionReport.length; i++) {
              var cName = $scope.report.completionReport[i].constraintName;
              if (emplInReport.weightsCountPerConstr[cName]) {
                if (emplInReport.weightsCountPerConstr[cName] < 0) {
                  if (_.contains($scope.mediumConstraints, cName)) {
                    gridRow.mediumScore += emplInReport.weightsCountPerConstr[cName];
                  } else {
                    gridRow.hardScore += emplInReport.weightsCountPerConstr[cName];
                  }
                } else {
                  gridRow.softScore += emplInReport.weightsCountPerConstr[cName];
                }
              }
            }
          }

          $scope.employeeSummaryGridData.push(gridRow);
        });
      }

      // create model for schedule Execution Report
      function createScheduleExecutionGridOptions() {
        var i;
        var constraintMatchSetGrouped = [];
        for (i = 0; i < $scope.report.completionReport.length; i++) {
          constraintMatchSetGrouped[i] = _.groupBy(
            $scope.report.completionReport[i].constraintMatchSet,
            function (elem) {
              return elem.employeeId;
            }
          );
          $scope.report.completionReport[i].scheduleExecReport = _.map(
            constraintMatchSetGrouped[i], iteratorReport);


          $scope.report.completionReport[i].gridOptions = angular.copy($scope.tplGridOptions);
          $scope.report.completionReport[i].gridOptions.columnDefs.push({
            field: 'weights',
            displayName: 'Weights',
            cellTemplate: cellTpl('\'' + $scope.report.completionReport[i].constraintName + '\'')
          });
          $scope.report.completionReport[i].gridOptions.onRegisterApi = onRegApi;
          $scope.scheduleExecutionReport[$scope.report.completionReport[i].constraintName] =
            $scope.report.completionReport[i].scheduleExecReport;
          $scope.report.completionReport[i].gridOptions.data = 'scheduleExecutionReport.' +
            $scope.report.completionReport[i].constraintName + ' | filter: tempsearchtext';

        }
      }

      function createOpenShiftsGridData(shifts) {
        $scope.openShiftsGridData.length = 0;
        angular.forEach(shifts, function (shift, index) {
          var gridRow = {};

          gridRow.id = shift[0];
          gridRow.teamName = shift[3];
          gridRow.shiftLengthName = shift[4];
          gridRow.startDateTime = new Date(shift[1]);
          gridRow.endDateTime = new Date(shift[2]);
          gridRow.skillName = shift[5];

          $scope.openShiftsGridData.push(gridRow);
        });
      }

      // iterator for mapping object model of reports to array
      function iteratorReport(obj, key) {
        var weights = [];
        var weightsPerConstr = {};
        var weightsCountPerConstr = {};
        var isHard = false;
        angular.forEach(obj, function (val, index) {
          weights.push(val.weight);
          if (!weightsPerConstr[val.constraintName]) {
            weightsPerConstr[val.constraintName] = [];
            weightsCountPerConstr[val.constraintName] = val.weight;
            weightsPerConstr[val.constraintName].push(val.weight);
          } else {
            weightsPerConstr[val.constraintName].push(val.weight);
            weightsCountPerConstr[val.constraintName] += val.weight;
          }
          if (val.weight < 0 && !isHard) isHard = true;
        });
        return {
          employeeId: key,
          employeeName: $scope.getEmployeeName(key),
          employeeSkills: $scope.getEmployeeSkills(key),
          constraints: obj,
          weights: _.reduce(weights, function (memo, num) {
            return memo + num;
          }, 0),
          weightsDetails: weights,
          weightsPerConstr: weightsPerConstr,
          weightsCountPerConstr: weightsCountPerConstr,
          isHard: isHard
        };
      }

      $scope.findInArrayByProperty = function (list, prop, val) {
        return _.find(list, function (e) {
          return e[prop] == val;
        });
      };

      $scope.getHardTotal = function () {
        if ($scope.readyEmployeeGridData) {
          var counts = _.countBy($scope.report.employeeScheduleReport, function (obj) {
            return obj.isHard;
          });
          return counts.true;
        }
      };

      $scope.scheduleOverview = {
        'shifts': {
          'name': 'Shifts in Schedule'
        },
        'hours': {
          'name': 'Hours in Schedule'
        },
        'shiftsAssignments': {
          'name': 'Assigned Shifts'
        },
        'hoursAssignments': {
          'name': 'Assigned Hours'
        }
      };

      $scope.summaryBySkill = {
        'shifts': {
          'name': 'Shifts',
          'regularexcess': true
        },
        'hours': {
          'name': 'Hours',
          'regularexcess': true
        },
        'resources': {
          'name': 'Resources',
          'singlevalue': true
        },
        'resourcesAssignments': {
          'name': 'Assigned Resources',
          'singlevalue': true
        },
        'resourcesHours': {
          'name': 'Resources Hours',
          'minmax': true
        },
        'assignedShifts': {
          'name': 'Assigned Shifts',
          'regularexcess': true
        },
        'assignedHours': {
          'name': 'Assigned Hours',
          'regularexcess': true
        }
      };

      $scope.getEmployeeName = function (employeeId) {
        var emp = _.find($scope.report.employees, function (e) {
          return e.id == employeeId;
        });
        if (emp) return emp.firstName + " " + emp.lastName;
      };

      $scope.getEmployeeSkills = function (employeeId) {
        var emp = _.find($scope.report.employees, function (e) {
          return e.id == employeeId;
        });
        if (emp) {
          return $scope.getSkills(emp);
        }
      };

      $scope.getSkills = function (employee) {
        if (employee.skills.length > 0) {
          var skills = _.partition(employee.skills, function (e) {
            return e.isPrimary;
          });

          var primary = [];
          if (skills[0][0] !== undefined) {
            primary = [skills[0][0].name];
          }
          var notPrimary = [];
          angular.forEach(skills[1], function (val, index) {
            notPrimary.push(val.name);
          });
          return [primary, notPrimary];
        }
      };

      $scope.getEmployeeTotalHours = function (employeeId) {
        var emp = _.find($scope.report.employees, function (e) {
          return e.id == employeeId;
        });
        if (emp) return emp.totalHours;
      };

      $scope.getEmployeeAvailableHours = function (employeeId) {
        var emp = _.find($scope.report.employees, function (e) {
          return e.id == employeeId;
        });
        if (emp) return emp.availableHours;
      };

      // Close Modal
      $scope.close = function () {
        //$modalInstance.dismiss('cancel');
        $modalInstance.dismiss('cancel');
      };

      $scope.hasPermission = function (perm) {
        return authService.hasPermission(perm);
      };

      $scope.toUpperCaseUnderscore = function (string) {
        return string.replace(/([A-Z])/g, function ($1) {
          return "_" + $1.toLowerCase();
        }).toUpperCase();
      };

      $scope.getTotal = function (constraintName) {
        for (var i = 0; i < $scope.report.completionReport.length; i++) {
          if ($scope.report.completionReport[i].constraintName == constraintName) {
            return $scope.report.completionReport[i].weightTotal;
          }
        }
      };

      $scope.mediumConstraints = [
        "requiredEmployeeSizePerShift",
        "minHoursPrimeSkillPerWeek",
        "minHoursPerWeek",
        "minHoursPerDay"
      ];

      $scope.extScope = {
        isHard: function (val, constName) {
          if (val < 0) {
            if (_.contains($scope.mediumConstraints, constName) || constName === 'mediumScore') {
              return 'mediumCell';
            } else return 'hardCell';
          } else return 'softCell';
        },
        getTotal: function (constraintName) {
          for (var i = 0; i < $scope.report.completionReport.length; i++) {
            if ($scope.report.completionReport[i].constraintName == constraintName) {
              return $scope.report.completionReport[i].weightTotal;
            }
          }
        },
        getTranslateName: function (name) {
          return 'constraints.' + $scope.toUpperCaseUnderscore(name);
        },
        getDetails: function (array) {
          if (array && array.length > 0) {
            return '(' + array.join() + ')';
          }
        },
        getUnit: function (field, val) {
          if (val) {
            var unit = '';
            if (field.indexOf("Hours") > -1) {
              unit = 'hours';
            }
            if (field.indexOf("Days") > -1) {
              unit = 'days';
            }
            return unit;
          }
        }
      };

      $scope.showValue = function (val) {
        if (angular.isArray(val)) {
          if (val.length == 2) {
            if (val[0] > 0) {
              return val[0] + " (-" + val[1] + "/-" + Math.round(val[1] / (val[0] + val[1]) * 100) + "%)";
            } else {
              return val[0];
            }
          } else if (val.length == 1) {
            return val[0];
          }
        } else {
          return val;
        }
      };

      $scope.getShiftCountColor = function (assigned, total) {
        if (total > 0 && assigned / total != 1) {
          if (assigned / total < 0.6) {
            return 'hardCell';
          } else if (assigned / total >= 0.6 && assigned / total < 0.9) {
            return 'mediumCell';
          } else if (assigned / total > 0.9) {
            return 'goodCell';
          }
        } else return '';
      };

      $scope.getShiftPersent = function (assigned, total) {
        if (total > 0 && assigned / total != 1) {
          return " (-" + (total - assigned) + "/-" + Math.round((total - assigned) / total * 100) + "%)";
        }
      };

    }
  ]
);
