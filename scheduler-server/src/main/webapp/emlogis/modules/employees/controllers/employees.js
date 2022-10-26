angular.module('emlogis.employees').controller('EmployeesCtrl',
  [
    '$scope', 'employeesDetailsService', function ($scope, employeesDetailsService) {

    // on module change
    // clear SiteTeams and Skills cache

    $scope.$on("$destroy", function () {
      employeesDetailsService.cleanExternalCache();
    });

  }
  ]
);
