(function () {
  "use strict";

  var employees = angular.module('emlogis.employees');

  employees.controller('EmployeesDetailsSkillsModalCtrl',
    ['$scope', '$rootScope', '$timeout', '$modalInstance', 'uiGridConstants', 'dataService', 'employeesDetailsService', 'applicationContext', 'employee',
      function ($scope, $rootScope, $timeout, $modalInstance, uiGridConstants, dataService, employeesDetailsService, applicationContext, employee) {

        //--------------------------------------------------------------------
        // Defaults for Employee Skills Modal
        //--------------------------------------------------------------------

        var es = this,
            numOfRows = 15;

        es.employee = employee;
        es.isEditing = false;
        es.allSkills = null;
        es.allSkillsInit = null;



        employeesDetailsService.getSkills().then(function(data) {
          es.allSkillsInit = data;

          angular.forEach(es.allSkillsInit, function(skill) {
            var employeeSkillInfo = _.find(es.employee.skillInfo, { 'skillId': skill.id });
            skill.isEmployeeHasSkill = employeeSkillInfo ? true : false;
            skill.isSelected = employeeSkillInfo ? true : false;

            if ( employeeSkillInfo ) {
              skill.isPrimarySkill = employeeSkillInfo.isPrimarySkill;
              skill.skillScore = employeeSkillInfo.skillScore;
            }
          });

          es.allSkills = angular.copy(es.allSkillsInit);

          $timeout(function() {
            prepareSkillsGrid(es.allSkills);
          });
        },
        function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });



        //--------------------------------------------------------------------
        // Grid settings
        //--------------------------------------------------------------------

        var prepareSkillsGrid = function(allSkills){
          function rowTemplate() {
            return '<div>' +
              '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" ' +
              'class="ui-grid-cell" ' +
              'ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader,          '  +
              '            \'row-to-add\': grid.appScope.es.skillToAdd(row),         '  +
              '            \'row-to-remove\': grid.appScope.es.skillToRemove(row)}"  '  +
              ' ui-grid-cell>' +
              '</div>' +
              '</div>';
          }

          es.scores = [ 1, 2, 3, 4, 5 ];
          es.skillsToRemove = [];
          es.skillsToAdd = [];

          es.skillToAdd = function(row) {
            return row.entity.isEmployeeHasSkill === false && row.isSelected === true;
          };

          es.skillToRemove = function(row) {
            return row.entity.isEmployeeHasSkill === true && row.isSelected === false;
          };

          es.gridOptions = {
            data: allSkills,
            totalItems: allSkills.length,
            minRowsToShow: allSkills.length < numOfRows ? allSkills.length : numOfRows,
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
                field: 'skillScore',
                cellTemplate: '<div class="text-center">' +
                                '<span ng-show="row.isSelected">' +
                                  '<select class="form-control eml-grid-select" ' +
                                          'ng-model="row.entity.skillScore" ' +
                                          'ng-options="score as score for score in grid.appScope.es.scores" ' +
                                          'ng-click="$event.stopPropagation()" ' +
                                          'ng-change="grid.appScope.es.updateEditing()">' +
                                  '</select>' +
                                '</span>' +
                              '</div>'
              },
              {
                field: 'isPrimarySkill',
                cellTemplate: '<div class="text-center">' +
                                '<span ng-show="row.isSelected">' +
                                  '<label class="eml-switch">' +
                                    '<input type="checkbox" ' +
                                            'class="eml-switch-input" ' +
                                            'ng-model="row.entity.isPrimarySkill" ' +
                                            'ng-checked="row.entity.isPrimarySkill"' +
                                            'ng-change="grid.appScope.es.updatePrimarySkill(row.entity.id)">' +
                                    '<span class="eml-switch-label" ' +
                                          'data-on="{{ ::\'app.YES\' | translate }}" ' +
                                          'data-off="{{ ::\'app.NO\' | translate }}"></span>' +
                                    '<span class="eml-switch-handle"></span>' +
                                '</span>' +
                              '</div>'
              },
              { field: 'id', visible: false }
            ],
            rowTemplate: rowTemplate(),
            onRegisterApi: function(gridApi) {
              es.gridApi = gridApi;

              $timeout(function() {
                _.each(allSkills, function(skill) {
                  if (skill.isEmployeeHasSkill) {
                    es.gridApi.selection.selectRow(skill);
                  }
                });
              });

              gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                row.entity.isSelected = row.isSelected;
                es.updateEditing();

                // Update skillsToRemove list
                var isInRemoveList = _.indexOf(es.skillsToRemove, row.entity.id) > -1;
                if (row.isSelected && isInRemoveList) _.pull(es.skillsToRemove, row.entity.id);
                if (!row.isSelected && !isInRemoveList) es.skillsToRemove.push(row.entity.id);

                // Update skillsToAdd list
                var prepareSkill = function(skillObj){
                  return {
                    skillId:      skillObj.id,
                    primarySkill: skillObj.isPrimarySkill,
                    skillScore:   skillObj.skillScore
                  };
                };
                var isInAddList = _.find(es.skillsToAdd, {'skillId': row.entity.id});
                if (row.isSelected && !isInAddList) es.skillsToAdd.push( prepareSkill(row.entity) );
                if (!row.isSelected && isInAddList) {
                  _.remove(es.skillsToAdd, function(skill) {
                    return skill.skillId === row.entity.id;
                  });
                }
              });
            }
          };
        };


        //--------------------------------------------------------------------
        // CRUD
        //--------------------------------------------------------------------

        es.updateEditing = function(){
          es.isEditing = !angular.equals(es.allSkillsInit, es.allSkills);
        };


        es.updatePrimarySkill = function(skillId){
          angular.forEach(es.allSkills, function(skill){
            if ( skill.id !== skillId) skill.isPrimarySkill = false;
          });
          es.updateEditing();
        };


        es.saveSkills = function(){

          // 1. check what skillsToRemove Employee initially had
          var removeCollection = [];
          angular.forEach(es.skillsToRemove, function(skillId){
            var skillExisted = _.find(es.employee.skillInfo, {'skillId': skillId});
            if ( skillExisted ) removeCollection.push(skillId);
          });

          // 2. check what skillsToAdd Employee initially didn't have
          var addCollection = [],
              updateCollection = [];

          angular.forEach(es.skillsToAdd, function(skill){
            // 2.1. get current skill settings
            var skillModel = _.find(es.allSkills, {'id': skill.skillId });
            skill.skillScore = skillModel.skillScore;
            skill.primarySkill = skillModel.isPrimarySkill;

            // 2.2. check if skill was attached to employee on modal load
            var skillExisted = _.find(es.employee.skillInfo, {'skillId': skill.skillId});

            // 2.3. push to the corresponding array
            if ( skillExisted ) {
              updateCollection.push(skill);
            } else {
              addCollection.push(skill);
            }
          });

          // 3. create a dto for API
          var dto = {
            removeCollection: removeCollection,
            addCollection: addCollection,
            updateCollection: updateCollection
          };

          return employeesDetailsService.updateEmployeeSkills(es.employee, dto).then(function(employee){
            es.employee = employee;
            es.isEditing = false;
            es.closeModal();
          });
        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        es.closeModal = function () {
          $modalInstance.close(es.employee);
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