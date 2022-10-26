angular.module('emlogis.admin').controller('AdminNotificationDeliverySmsBreadcrumbCtrl', ['$scope', '$state',
  function($scope, $state) {
    $scope.goToNewProviderState = function(providerType) {
      $state.go('authenticated.admin.notificationDelivery.sms.createProvider', {providerType: providerType});
    };
  }
]);
