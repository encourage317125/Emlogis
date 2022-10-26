angular.module('emlogis.admin').controller('AdminNotificationDeliveryEmailBreadcrumbCtrl', ['$scope', '$state',
  function($scope, $state) {
    $scope.goToNewProviderState = function(providerType) {
      $state.go('authenticated.admin.notificationDelivery.email.createProvider', {providerType: providerType});
    };
  }
]);
