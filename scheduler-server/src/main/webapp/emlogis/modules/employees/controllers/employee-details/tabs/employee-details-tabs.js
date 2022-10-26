(function () {
  "use strict";

  angular.module('emlogis.employees').controller('EmployeeDetailsTabsCtrl', ['$scope',
    function ($scope) {
      $scope.tabs = [
        {heading: "employees.tabs.CURRENT_CALENDAR", route: 'authenticated.employees.detail.currentCalendar'},
        {heading: "employees.tabs.AVAILABILITY", route: 'authenticated.employees.detail.availability'},
        {heading: "employees.tabs.SETTINGS", route: 'authenticated.employees.detail.settings'}
      ];
    }]);
})();