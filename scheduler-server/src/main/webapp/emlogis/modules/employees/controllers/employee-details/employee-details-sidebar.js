(function () {
  "use strict";

  var employees = angular.module('emlogis.employees');

  employees.controller('EmployeeDetailsSidebarCtrl', ['$scope', '$state', '$modal', 'employeesDetailsService',
    function ($scope, $state, $modal, employeesDetailsService) {

      //--------------------------------------------------------------------
      // On Ctrl load
      //--------------------------------------------------------------------

      var employeeInit,
          sidebar = this;

      sidebar.employee = null;
      sidebar.isEditingApprovals = false;


      employeesDetailsService.getEmployeeDetails($state.params.id).then(function(employee){
        updateSidebarModel(employee);
      });

      var updateSidebarModel = function(employee){
        sidebar.employee = employee;
        employeeInit = employeesDetailsService.getEmployeeInit();
        //console.log('+++ sidebar.employee updated', sidebar.employee);
      };


      //
      // watch for Employee Home Team is edited in profile section
      // and update Employee obj

      $scope.$watch(function () {
        if ( sidebar.employee ) {
          return employeesDetailsService.getEmployeeTeam();
        }
      }, function (newTeam, oldTeam) {
        if ( !newTeam || oldTeam === undefined || angular.equals(newTeam, oldTeam) ) {
          return;
        }
        employeesDetailsService.getEmployeeDetails(sidebar.employee.id).then(function(employee){
          updateSidebarModel(employee);
        });
      });


      //
      // watch for Employee Home Team is edited in profile section
      // and update Employee obj

      $scope.$watch(function () {
        if ( sidebar.employee ) {
          return employeesDetailsService.getEmployeeActivityType();
        }
      }, function (newActivity, oldActivity) {
        if ( !newActivity || oldActivity === undefined || angular.equals(newActivity, oldActivity) ) {
          return;
        }
        employeesDetailsService.getEmployeeDetails(sidebar.employee.id).then(function(employee){
          updateSidebarModel(employee);
        });
      });



      //--------------------------------------------------------------------
      // USER panel
      //--------------------------------------------------------------------

      sidebar.resetPassword = function() {
        employeesDetailsService.requestUserPasswordReset();
      };

      sidebar.toggleNotifications = function(enable) {
        employeesDetailsService.toggleUserNotifications(enable);
      };


      //--------------------------------------------------------------------
      // EMLOGIS panel
      //--------------------------------------------------------------------

      //
      // Update Approvals settings

      sidebar.updateEditingApprovals = function(){
        sidebar.isEditingApprovals = !angular.equals(sidebar.employee.autoApprovalsSettingDto, employeeInit.autoApprovalsSettingDto);
      };


      //
      // Update Approvals settings

      sidebar.saveApprovals = function(){
        var newApprovals = {
          availAutoApprove: sidebar.employee.autoApprovalsSettingDto.availAutoApprove,
          wipAutoApprove: sidebar.employee.autoApprovalsSettingDto.wipAutoApprove,
          swapAutoApprove: sidebar.employee.autoApprovalsSettingDto.swapAutoApprove
        };

        employeesDetailsService.updateEmployeeAutoapprovals(sidebar.employee, newApprovals)
          .then(function(employee){
            updateSidebarModel(employee);
            sidebar.isEditingApprovals = false;
          }, function(err) {
            // TODO: add modal type of errors
          })
        ;
      };


      //--------------------------------------------------------------------
      // PTO panel
      //--------------------------------------------------------------------


      //
      // get a list of PTOs for an Employee

      sidebar.getEmployeePTO = function(){
        employeesDetailsService.getEmployeePTO(sidebar.employee).then(function(res){

        });
      };

      sidebar.openPTOModal = function (size) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/employees/partials/employee-details/empl-details-ptos-modal.html',
          controller: 'EmployeesDetailsPTOsModalCtrl as pto',
          size: size,
          backdrop: 'static',
          resolve: {
            employee: function () {
              return sidebar.employee;
            }
          }
        });

        modalInstance.result.then(function (employee) {
          updateSidebarModel(employee);
        }, function () {
          //console.log('Modal dismissed at: ' + new Date());
        });
      };


      //--------------------------------------------------------------------
      // SKILLS panel
      //--------------------------------------------------------------------


      //
      // Skills modal

      sidebar.openSkillsModal = function (size) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/employees/partials/employee-details/empl-details-skills-modal.html',
          controller: 'EmployeesDetailsSkillsModalCtrl as es',
          size: size,
          backdrop: 'static',
          resolve: {
            employee: function () {
              return sidebar.employee;
            }
          }
        });

        modalInstance.result.then(function (employee) {
          updateSidebarModel(employee);
        }, function () {
          //console.log('Modal dismissed at: ' + new Date());
        });
      };



      //--------------------------------------------------------------------
      // TEAMS panel
      //--------------------------------------------------------------------


      //
      // Teams modal

      sidebar.openTeamsModal = function (size) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/employees/partials/employee-details/empl-details-teams-modal.html',
          controller: 'EmployeesDetailsTeamsModalCtrl as et',
          size: size,
          backdrop: 'static',
          resolve: {
            employee: function () {
              return sidebar.employee;
            }
          }
        });

        modalInstance.result.then(function (employee) {
          updateSidebarModel(employee);
        }, function () {
          //console.log('Modal dismissed at: ' + new Date());
        });
      };


    }]);
})();