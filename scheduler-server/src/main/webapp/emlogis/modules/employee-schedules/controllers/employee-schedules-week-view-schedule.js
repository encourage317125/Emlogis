angular.module('emlogis.employeeSchedules')
  .controller('EmployeeSchedulesWeekViewScheduleCtrl',
  ['$scope', '$state', '$stateParams',
    function($scope, $state, $stateParams) {
      $scope.getSchedule($stateParams.scheduleId, null, false);
    }]);