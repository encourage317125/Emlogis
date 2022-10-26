(function () {
  "use strict";

  angular.module('emlogis.employees').controller('EmployeeDetailsCalendarCtrl',
    ['$scope', '$state', '$q', 'dataService', 'employeesDetailsService',
    function ($scope, $state, $q, dataService, employeesDetailsService) {

      $scope.accountInfoDeferred = $q.defer();
      $scope.getEmployeeDetails = employeesDetailsService.getEmployeeInit;

      $scope.$watch("getEmployeeDetails()", function(details) {
        if (!details) return;

        $scope.accountInfoDeferred.resolve({
          timezone: details.siteInfo.timeZone,
          siteFirstDayOfweek: details.siteInfo.firstDayOfWeek,
          teams: details.teamInfo
        });
      });

      $scope.getAccountInfo = function() {
        return $scope.accountInfoDeferred.promise;
      };

      $scope.getEmployeeCalendarView = function(startDate, endDate) {
        var employeeId = $state.params.id,
            queryParams = {
              params: {
                startdate: startDate,
                enddate: endDate,
                scheduleStatus: 'Posted',
                returnedfields: "id, startDateTime, endDateTime, excess, skillAbbrev, skillName, teamName"
              }
            };
        return dataService.getEmployeeCalendarView(employeeId, queryParams);
      };
    }]);
})();