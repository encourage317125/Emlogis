var employees = angular.module('emlogis.employees');

employees.controller('EmployeesDetailCtrl', ['$scope', '$state',
  function ($scope, $state) {
    //console.log('employees detail controller id: "' + $state.params.id);
    $scope.employeeId = $state.params.id;
  }
]);
