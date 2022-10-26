angular.module('emlogis.employeeSchedules')
  .controller('EmployeeSchedulesDayViewCtrl',
  ['$scope', '$timeout',
    function($scope, $timeout) {
      $scope.setViewMode('day');
      $timeout(function() {
        if ($scope.selectedSchedule === null && $scope.scheduleInfoLoaded === null) {
          $scope.openSiteSchedulesModal();
        }
      }, 500);
    }]);