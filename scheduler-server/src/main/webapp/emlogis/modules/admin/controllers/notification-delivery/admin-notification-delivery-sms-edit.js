angular.module('emlogis.admin').controller('AdminNotificationDeliverySmsEditCtrl', ['$scope', 'applicationContext', 'AdminNotificationDeliveryService',
  function($scope, applicationContext, AdminNotificationDeliveryService) {
    $scope.smsProviders = [];
    $scope.parsedSmsProviders = [];

    $scope.invertProviderCollapsedFlag = function(provider) {
      provider.isCollapsed = !provider.isCollapsed;
    };

    $scope.parseSmsProviders = function() {
      $scope.parsedSmsProviders = [];
      angular.forEach($scope.smsProviders, function(provider) {
        var parsedProvider = angular.copy(provider);
        parsedProvider.lastChecked = new Date(provider.lastChecked).toString();
        parsedProvider.activationChanged = new Date(provider.activationChanged).toString();
        parsedProvider.isCollapsed = false;
        parsedProvider.isTestCompleted = true;

        $scope.parsedSmsProviders.push(parsedProvider);
      });
    };

    $scope.testProvider = function(provider) {
      provider.isTestCompleted = false;
      AdminNotificationDeliveryService.testProvider(provider.id).then(function(response) {
        applicationContext.setNotificationMsgWithValues('admin.notification_delivery.TESTED_SUCCESSFULLY', 'success', true);
        var originalProvider = _.find($scope.emailProviders, function(iterator) {return iterator.id === provider.id;});
        originalProvider.status = response.data.status;
        originalProvider.statusInfo = response.data.statusInfo;
        provider.status = response.data.status;
        provider.statusInfo = response.data.statusInfo;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      }).finally(function() {
        provider.isTestCompleted = true;
      });
    };

    $scope.saveProvider = function(provider) {
      var savedProviderDetails = {
        name: provider.name,
        active: provider.active,
        settings: provider.settings
      };

      AdminNotificationDeliveryService.updateProvider(provider.id, savedProviderDetails).then(function(response) {
        applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      }).finally(function() {
        $scope.getSmsProviders();
      });
    };

    $scope.deleteProvider = function(provider) {
      AdminNotificationDeliveryService.deleteProvider(provider.id).then(function(response) {
        applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      }).finally(function() {
        $scope.getSmsProviders();
      });
    };

    $scope.getSmsProviders = function() {
      AdminNotificationDeliveryService.getSmsProviders().then(function(response) {
        $scope.smsProviders = response.data.result;
        $scope.parseSmsProviders();
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

    $scope.getSmsProviders();
  }
]);
