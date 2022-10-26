angular.module('emlogis.admin').controller('AdminNotificationDeliveryCtrl', ['$scope',
  function($scope) {
    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Tenant_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Tenant_View');
    };
  }
]);
