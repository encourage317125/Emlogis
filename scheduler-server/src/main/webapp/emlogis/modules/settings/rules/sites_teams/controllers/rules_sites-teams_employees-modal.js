(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesSitesTeamsEmployeesModalCtrl',
    ['$scope', '$timeout', '$modalInstance', 'uiGridConstants', 'applicationContext', 'dataService', 'rulesTeamsService', 'team',
      function ($scope, $timeout, $modalInstance, uiGridConstants, applicationContext, dataService, rulesTeamsService, team) {

        //--------------------------------------------------------------------
        // Defaults for Employees Modal
        //--------------------------------------------------------------------

        var te = this,
            totalUnnassociatedEmployees;

        te.team = team;
        te.numOfRows = 15;
        te.isEditing = false;
        te.queryParams = {
          orderby:'lastName',
          orderdir:'ASC'
        };

        te.unassociatedEmployees = null;
        te.unassociatedEmployeesInit = null;
        te.employeesToAdd = [];

        //
        // Load Unassociated Employees
        // that don't belong to this Team

        var loadTeamUnassociatedEmployees = function(teamId, queryParams, pageNum, perPage){
          return rulesTeamsService.getTeamUnassociatedEmployees(teamId, queryParams, pageNum, perPage).then( function(res){
            console.log('+++ Loaded getTeamMembership:', res);    //DEV mode

            prepareData(res);

            $timeout(function() {
              prepareEmployeesGrid(te.unassociatedEmployees);
            });

            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            })
          ;
        };

        loadTeamUnassociatedEmployees(te.team.id, te.queryParams, 1, te.numOfRows);

        var prepareData = function(res){
          totalUnnassociatedEmployees = res.total;
          te.unassociatedEmployeesInit = res.data;

          angular.forEach(te.unassociatedEmployeesInit, function(employee) {
            employee.isSelected = false;
          });

          te.unassociatedEmployees = angular.copy(te.unassociatedEmployeesInit);
        };



        //--------------------------------------------------------------------
        // Grid settings
        //--------------------------------------------------------------------

        var prepareEmployeesGrid = function(unassociatedEmployees){
          console.log('+++ unassociatedEmployees', unassociatedEmployees);

          te.gridOptions = {
            data: unassociatedEmployees,
            totalItems: totalUnnassociatedEmployees,
            minRowsToShow: unassociatedEmployees.length < te.numOfRows ? unassociatedEmployees.length : te.numOfRows,

            enableHorizontalScrollbar: 0,
            enableVerticalScrollbar: 0,
            enableColumnMenus: false,

            enableFiltering: true,
            useExternalFiltering: true,

            enableSorting: true,
            useExternalSorting: true,

            needPagination: true,
            useExternalPagination: true,
            enablePaginationControls: false,
            paginationPageSize: te.numOfRows,
            paginationCurrentPage: 1,

            enableSelectAll: true,
            enableRowSelection: true,
            enableFullRowSelection: true,                   // full row selection

            columnDefs: [
              {
                field: 'lastName',
                enableFiltering: true,
                minWidth: '150',
                sort: {
                  direction: uiGridConstants.ASC
                }
              },
              { field: 'firstName',        enableFiltering: false, minWidth: '100' },
              {
                field: 'isFloating',
                enableFiltering: false,
                minWidth: '90',
                enableSorting: false,
                cellTemplate: '<div>' +
                                '<span ng-show="row.isSelected">' +
                                  '<label class="eml-switch">' +
                                  '<input type="checkbox" ' +
                                          'class="eml-switch-input" ' +
                                          'ng-model="row.entity.isFloating" ' +
                                          'ng-checked="row.entity.isFloating"' +
                                          'ng-change="grid.appScope.te.updateEditing()">' +
                                  '<span class="eml-switch-label" ' +
                                        'data-on="{{ ::\'app.YES\' | translate }}" ' +
                                        'data-off="{{ ::\'app.NO\' | translate }}"></span>' +
                                  '<span class="eml-switch-handle"></span>' +
                                '</span>' +
                              '</div>'
              },
              { field: 'primarySkillName', enableFiltering: false, minWidth: '150' },
              { field: 'homeTeamName',     enableFiltering: false, minWidth: '150' }
              //{ field: 'employeeType',     enableFiltering: false, minWidth: '100' },
              //{ field: 'hireDate',         enableFiltering: false, minWidth: '100', cellFilter: 'date' }
            ],
            onRegisterApi: function(gridApi) {
              te.gridApi = gridApi;

              //
              // Row selection
              
              gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                row.entity.isSelected = row.isSelected;
                te.updateEditing();
              });


              //
              // Select All event

              gridApi.selection.on.rowSelectionChangedBatch($scope, function (arrRows) {
                angular.forEach(arrRows, function(row){
                  row.entity.isSelected = row.isSelected;
                });
                te.updateEditing();
              });

              
              //
              // Back-end filtering

              gridApi.core.on.filterChanged( $scope, function() {
                var grid = this.grid;
                var filterTerm = grid.columns[1].filters[0].term;
                //console.log('~~~ filter changed - grid', grid);

                if (filterTerm === null || filterTerm === '' || filterTerm === undefined ){
                  te.queryParams = {
                    orderby : te.queryParams.orderby,
                    orderdir: te.queryParams.orderdir
                  };
                } else {
                  var filterName = 'lastName';
                  te.queryParams.filter = filterName + " LIKE '" + filterTerm + "%'";
                }
                getPage();
              });


              //
              // Back-end sorting

              gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                if (sortColumns.length === 0) {
                  te.queryParams.orderdir = 'ASC';
                  te.queryParams.orderby = 'lastName';
                  
                } else {
                  te.queryParams.orderdir = sortColumns[0].sort.direction;

                  switch (sortColumns[0].field) {
                    case "lastName":
                      te.queryParams.orderby = 'lastName';
                      break;
                    case "firstName":
                      te.queryParams.orderby = 'firstName';
                      break;
                    case "isFloating":
                      te.queryParams.orderby = 'isFloating';
                      break;
                    case "employeeType":
                      te.queryParams.orderby = 'employeeType';
                      break;
                    case "primarySkillName":
                      te.queryParams.orderby = 'Skill.name';
                      break;
                    case "homeTeamName":
                      te.queryParams.orderby = 'Team.name';
                      break;
                    case "hireDate":
                      te.queryParams.orderby = 'hireDate';
                      break;
                    default:
                      te.queryParams.orderby = 'lastName';
                      break;
                  }
                }
                getPage();
              });


              //
              // Back-end pagination

              gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
                te.gridOptions.paginationCurrentPage = newPage;

                // Refresh Employees modal
                te.revertEmployeesToInit();                // bring Employees list to initial state
                gridApi.grid.selection.selectAll = false;  // to remove `ui-grid-all-selected` class that un-checks Select All
                gridApi.grid.selection.selectedCount = 0;

                getPage();
              });

              var getPage = function() {
                console.log('te.gridOptions.queryParams', te.queryParams);

                rulesTeamsService.getTeamUnassociatedEmployees(te.team.id, te.queryParams, te.gridOptions.paginationCurrentPage, te.numOfRows)
                  .then( function(res){
                    console.log('~~~ res grid upd', res);
                    refreshGrid(res);
                });
              };
            }
          };
        };


        //--------------------------------------------------------------------
        // CRUD
        //--------------------------------------------------------------------


        var refreshGrid = function(res){
          prepareData(res);
          te.gridOptions.totalItems = totalUnnassociatedEmployees;
          te.gridOptions.data = te.unassociatedEmployees;
          te.gridOptions.minRowsToShow = totalUnnassociatedEmployees < te.numOfRows ? totalUnnassociatedEmployees : te.numOfRows;
        };


        te.updateEditing = function(){
          te.isEditing = !angular.equals(te.unassociatedEmployeesInit, te.unassociatedEmployees);
        };

        te.revertEmployeesToInit = function(){
          te.unassociatedEmployees = angular.copy(te.unassociatedEmployeesInit);
          te.updateEditing();
        };


        te.associateEmployees = function(){
          //prepare selected rows
          var toAdd = te.gridApi.selection.getSelectedRows();
          console.log('toAdd', toAdd);
          var toAddDto = [];
          angular.forEach(toAdd, function(row){
            var dto = {
              isFloating: row.isFloating,
              //isHomeTeam: false,
              //isSchedulable: false,
              employeeId: row.employeeId
            };
            toAddDto.push(dto);
          });
          console.log('toAddDto', toAddDto);

          rulesTeamsService.addEmployeesTeamMembership(te.team.id, toAddDto).then(function(res){
            $modalInstance.close(te.team);

          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        te.closeModal = function () {
          $modalInstance.dismiss('cancel');
        };


        //
        // If user navigates away from the page,
        // dismiss the modal

        $scope.$on('$stateChangeStart', function(){
            $modalInstance.dismiss('cancel');
          }
        );

      }
    ]);
})();