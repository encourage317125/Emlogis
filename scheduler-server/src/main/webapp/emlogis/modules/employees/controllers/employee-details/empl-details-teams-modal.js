(function () {
  "use strict";

  var employees = angular.module('emlogis.employees');

  employees.controller('EmployeesDetailsTeamsModalCtrl',
    ['$scope', '$rootScope', '$timeout', '$modalInstance', 'dataService', 'employeesDetailsService', 'applicationContext', 'employee',
      function ($scope, $rootScope, $timeout, $modalInstance, dataService, employeesDetailsService, applicationContext, employee) {

        //--------------------------------------------------------------------
        // Defaults for Employee Teams Modal
        //--------------------------------------------------------------------

        var et = this,
            numOfRows = 15;

        et.employee = employee;
        et.isEditing = false;
        et.allTeams = null;
        et.allTeamsInit = null;



        employeesDetailsService.getSiteTeams().then(function(data) {
          et.allTeamsInit = data;
          //console.log('et.allTeamsInit', data);

          angular.forEach(et.allTeamsInit, function(team) {
            var employeeTeamInfo = _.find(et.employee.teamInfo, { 'teamId': team.id });
            team.isEmployeeAttachedToTeam = employeeTeamInfo ? true : false;
            team.isSelected = employeeTeamInfo ? true : false;

            if ( employeeTeamInfo ) {
              team.isFloating = employeeTeamInfo.isFloating;
              team.isSchedulable = employeeTeamInfo.isSchedulable;
              team.isHomeTeam = employeeTeamInfo.isHomeTeam;
            }
          });

          et.allTeams = angular.copy(et.allTeamsInit);

          $timeout(function() {
            prepareTeamsGrid(et.allTeams);
          });
        },
        function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });

        //--------------------------------------------------------------------
        // Grid settings
        //--------------------------------------------------------------------

        var prepareTeamsGrid = function(allTeams){
          function rowTemplate() {
            return '<div>' +
              '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" ' +
              'class="ui-grid-cell" ' +
              'ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader,          '  +
              '            \'row-to-add\': grid.appScope.et.teamToAdd(row),         '  +
              '            \'row-to-remove\': grid.appScope.et.teamToRemove(row),  '  +
              '            \'row-home-team\': grid.appScope.et.isHomeTeam(row) }" ' +
              ' ui-grid-cell>' +
              '</div>' +
              '</div>';
          }

          et.teamsToRemove = [];
          et.teamsToAdd = [];

          et.teamToAdd = function(row) {
            return row.entity.isEmployeeAttachedToTeam === false && row.isSelected === true;
          };

          et.teamToRemove = function(row) {
            return row.entity.isEmployeeAttachedToTeam === true && row.isSelected === false;
          };

          et.isHomeTeam = function(row) {
            return row.entity.isHomeTeam;
          };


          et.gridOptions = {
            data: allTeams,
            totalItems: allTeams.length,
            minRowsToShow: allTeams.length < numOfRows ? allTeams.length : numOfRows,
            needPagination: false,
            enableHorizontalScrollbar: 0,
            enableVerticalScrollbar: 1,
            enableColumnMenus: false,
            enableFiltering: false,
            enableSorting: true,
            enableSelectAll: true,
            enableRowSelection: true,

            columnDefs: [
              { field: 'name', width: '55%'},
              {
                field: 'isFloating',
                cellTemplate: '<div>' +
                                  '<span ng-show="row.isSelected">' +
                                    '<label class="eml-switch">' +
                                      '<input type="checkbox" ' +
                                              'class="eml-switch-input" ' +
                                              'ng-model="row.entity.isFloating" ' +
                                              'ng-checked="row.entity.isFloating"' +
                                              'ng-change="grid.appScope.et.updateEditing()">' +
                                      '<span class="eml-switch-label" ' +
                                              'data-on="{{ ::\'app.YES\' | translate }}" ' +
                                              'data-off="{{ ::\'app.NO\' | translate }}"></span>' +
                                    '<span class="eml-switch-handle"></span>' +
                                  '</span>' +
                              '</div>'
              },
              { field: 'teamId',          visible: false },
              { field: 'isHomeTeam',      visible: false }
            ],
            rowTemplate: rowTemplate(),
            onRegisterApi: function(gridApi) {
              et.gridApi = gridApi;

              $timeout(function() {
                _.each(allTeams, function(team) {
                  if (team.isEmployeeAttachedToTeam) {
                    et.gridApi.selection.selectRow(team);
                  }
                });
              });

              gridApi.selection.on.rowSelectionChanged($scope, function (row) {

                // prevent Home Team to be deselected
                if ( !row.isSelected && row.entity.isHomeTeam ) {
                  et.gridApi.selection.selectRow(row.entity);
                  return;
                }

                row.entity.isSelected = row.isSelected;
                et.updateEditing();

                // Update teamsToRemove list
                var isInRemoveList = _.indexOf(et.teamsToRemove, row.entity.id) > -1;
                if (row.isSelected && isInRemoveList) _.pull(et.teamsToRemove, row.entity.id);
                if (!row.isSelected && !isInRemoveList) et.teamsToRemove.push(row.entity.id);

                // Update teamsToAdd list
                var prepareTeam = function(teamsObj){
                  return {
                    teamId:         teamsObj.id,
                    isFloating:     teamsObj.isFloating,
                    isSchedulable:  teamsObj.isSchedulable
                  };
                };
                var isInAddList = _.find(et.teamsToAdd, {'teamId': row.entity.id});
                if (row.isSelected && !isInAddList) et.teamsToAdd.push( prepareTeam(row.entity) );
                if (!row.isSelected && isInAddList) {
                  _.remove(et.teamsToAdd, function(team) {
                    return team.teamId === row.entity.id;
                  });
                }
              });

            }
          };
        };


        //--------------------------------------------------------------------
        // CRUD
        //--------------------------------------------------------------------


        et.updateEditing = function(){
          et.isEditing = !angular.equals(et.allTeamsInit, et.allTeams);
          //console.log('+++ et.isEditing', et.isEditing);
        };


        et.saveTeams = function(){

          // 1. check what teamsToRemove Employee initially had
          var removeCollection = [];
          angular.forEach(et.teamsToRemove, function(teamId){
            var teamExisted = _.find(et.employee.teamInfo, {'teamId': teamId});
            if ( teamExisted ) removeCollection.push(teamId);
          });

          // 2. check what teamsToAdd Employee initially didn't have
          var addCollection = [],
            updateCollection = [];

          angular.forEach(et.teamsToAdd, function(team){
            // 2.1. get current team settings
            var teamModel = _.find(et.allTeams, {'id': team.teamId });
            team.isFloating = teamModel.isFloating ? teamModel.isFloating : false;
            team.isSchedulable = teamModel.isSchedulable ? teamModel.isSchedulable : false;

            // 2.2. check if team was attached to employee on modal load
            var teamExisted = _.find(et.employee.teamInfo, {'teamId': team.teamId});

            // 2.3. push to the corresponding array
            if ( teamExisted ) {
              if ( !angular.equals(teamExisted, team) ) {
                updateCollection.push(team);
              }
            } else {
              addCollection.push(team);
            }
          });

          // 3. create a dto for API
          var dto = {
            removeCollection: removeCollection,
            addCollection: addCollection,
            updateCollection: updateCollection
          };

          return employeesDetailsService.updateEmployeeTeams(et.employee, dto).then(function(employee){
            et.employee = employee;
            et.isEditing = false;
            et.closeModal();
          });

        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        et.closeModal = function () {
          $modalInstance.close(et.employee);
        };


        //
        // If user navigates away from the page,
        // dismiss the modal

        $rootScope.$on('$stateChangeStart', function(){
            $modalInstance.dismiss('cancel');
          }
        );

      }
    ]);
})();