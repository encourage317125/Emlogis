(function () {
  "use strict";

  angular.module('emlogis.employees').controller('EmployeeDetailsAvailabilityCtrl', ['$scope', 'dataService', 'employeesDetailsService',
    function ($scope, dataService, employeesDetailsService) {

      $scope.getEmployeeDetails = employeesDetailsService.getEmployeeInit;

      $scope.$watch("getEmployeeDetails()", function(details) {
        if (!details) return;

        $scope.employeeId = details.id;
        $scope.siteTimeZone = details.siteInfo.timeZone;
        $scope.firstDayOfWeek = details.siteInfo.firstDayOfWeek;
        dataService.getAbsenceTypes(details.siteInfo.siteId).then(function(res) {
          $scope.absenceTypes = res.data;
        });
      });

    }]);
})();