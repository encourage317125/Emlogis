angular.module('emlogis.dashboard').controller('AvailabilityPreviewModalCtrl',
  ['$scope', '$timeout', '$modalInstance', 'dataService', 'applicationContext', 'employeeId', 'siteTimeZone', 'firstDayOfWeek', 'previewParams',
    function ($scope, $timeout, $modalInstance, dataService, applicationContext, employeeId, siteTimeZone, firstDayOfWeek, previewParams) {

      $scope.employeeId = employeeId;
      $scope.siteTimeZone = siteTimeZone;
      $scope.firstDayOfWeek = firstDayOfWeek;
      $scope.previewParams = previewParams;

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };
    }]);