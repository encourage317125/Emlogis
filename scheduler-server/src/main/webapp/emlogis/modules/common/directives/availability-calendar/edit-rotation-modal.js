angular.module('emlogis.commonDirectives').controller('EditRotationModal',
  ['$scope', '$modalInstance', 'dataService', 'dateRangeStart', 'dateRangeEnd', 'applicationContext', 'employeeId', 'selectedDay',
    function ($scope, $modalInstance, dataService, dateRangeStart, dateRangeEnd, applicationContext, employeeId, selectedDay) {

      $scope.selectedDay = _.capitalize(selectedDay) + "s";
      $scope.rotation = "NONE";

      $scope.updateWeekdayRotation = function() {
        dataService.updateWeekdayRotation(employeeId, dateRangeStart, dateRangeEnd, {
          dayOfWeek: selectedDay.toUpperCase(),
          weekdayRotationValue: $scope.rotation
        }).then(function() {
          applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
          $modalInstance.close("updated");
        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          $modalInstance.dismiss('cancel');
        });
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };

  }]);