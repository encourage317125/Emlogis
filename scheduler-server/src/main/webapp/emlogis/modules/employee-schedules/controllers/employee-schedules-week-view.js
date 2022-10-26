angular.module('emlogis.employeeSchedules')
  .controller('EmployeeSchedulesWeekViewCtrl',
  ['$scope', '$timeout',
    function($scope, $timeout) {
      $scope.setViewMode('week');
      $timeout(function() {
        if ($scope.selectedSchedule === null && $scope.scheduleInfoLoaded === null) {
          $scope.openSiteSchedulesModal();
        }
      }, 500);
    }]);