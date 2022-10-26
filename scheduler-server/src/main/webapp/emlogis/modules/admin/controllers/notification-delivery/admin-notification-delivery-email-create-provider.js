angular.module('emlogis.admin').controller('AdminNotificationDeliveryEmailCreateProviderCtrl', ['$scope', '$state', '$stateParams', 'applicationContext', 'AdminNotificationDeliveryService',
  function($scope, $state, $stateParams, applicationContext, AdminNotificationDeliveryService) {
    $scope.provider = {
      deliveryType: 'EMAIL',
      providerType: $stateParams.providerType,
      updateDto: {
        settings: {}
      }
    };

    $scope.createProvider = function() {
      AdminNotificationDeliveryService.createProvider($scope.provider).then(function(response) {
        applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      }).finally(function() {
        $scope.goToProviderListState();
      });
    };

    $scope.goToProviderListState = function() {
      $state.go('authenticated.admin.notificationDelivery.email.edit');
    };
  }
]);
