angular.module('emlogis.employeeSchedules')
  .controller('EmployeeSchedulesDayViewScheduleCtrl',
  ['$scope', '$state', '$stateParams',
    function($scope, $state, $stateParams) {
      $scope.getSchedule($stateParams.scheduleId, $stateParams.dateTimeStamp, false);
    }]);