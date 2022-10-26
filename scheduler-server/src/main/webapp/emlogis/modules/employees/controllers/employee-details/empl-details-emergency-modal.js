(function () {
  "use strict";

  var employees = angular.module('emlogis.employees');

  employees.controller('EmployeesDetailsEmergencyModalCtrl',
    ['$scope', '$rootScope', '$modalInstance', 'employeesDetailsService', 'employee',
      function ($scope, $rootScope, $modalInstance, employeesDetailsService, employee) {

        //--------------------------------------------------------------------
        // Defaults for Emergency Contact Modal
        //--------------------------------------------------------------------

        var ec = this;

        ec.employee = employee;
        ec.isEditing = false;
        ec.relationships = ['Brother', 'Daughter', 'Father', 'Friend', 'Husband', 'Mother', 'Peer', 'Sister', 'Son', 'Spouse', 'Wife'];



        //
        // If user navigates away from the page,
        // dismiss the modal

        $rootScope.$on('$stateChangeStart',
          function(){
            $modalInstance.dismiss('cancel');
          }
        );


        //--------------------------------------------------------------------
        // CRUD
        //--------------------------------------------------------------------

        ec.startEditing = function () {
          ec.employeeForEdit = angular.copy(ec.employee);
          ec.isEditing = true;
        };


        ec.cancelEditing = function () {
          ec.isEditing = false;
        };


        ec.saveEmergencyContact = function(){
          var newEc = {
            emergencyContact: ec.employeeForEdit.emergencyContact,
            ecRelationship: ec.employeeForEdit.ecRelationship,
            ecPhoneNumber: ec.employeeForEdit.ecPhoneNumber
          };

          return employeesDetailsService.updateEmployee(ec.employee, newEc).then(function(employee){
            ec.employee = employee;
            ec.isEditing = false;
          });
        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        ec.closeEcModal = function () {
          $modalInstance.close(ec.employee);
        };


        ec.cancelEcModal = function () {
          $modalInstance.dismiss('cancel');
        };

      }
    ]);
})();