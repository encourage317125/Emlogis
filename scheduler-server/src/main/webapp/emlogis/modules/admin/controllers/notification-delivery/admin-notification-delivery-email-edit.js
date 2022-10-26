angular.module('emlogis.admin').controller('AdminNotificationDeliveryEmailEditCtrl', ['$scope', 'applicationContext', 'AdminNotificationDeliveryService',
  function($scope, applicationContext, AdminNotificationDeliveryService) {
    $scope.emailProviders = [];
    $scope.parsedEmailProviders = [];

    $scope.invertProviderCollapsedFlag = function(provider) {
      provider.isCollapsed = !provider.isCollapsed;
    };

    $scope.parseEmailProviders = function() {
      $scope.parsedEmailProviders = [];
      angular.forEach($scope.emailProviders, function(provider) {
        var parsedProvider = angular.copy(provider);
        parsedProvider.lastChecked = new Date(provider.lastChecked).toString();
        parsedProvider.activationChanged = new Date(provider.activationChanged).toString();
        parsedProvider.isCollapsed = false;
        parsedProvider.isTestCompleted = true;

        $scope.parsedEmailProviders.push(parsedProvider);
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
        $scope.getEmailProviders();
      });
    };

    $scope.deleteProvider = function(provider) {
      AdminNotificationDeliveryService.deleteProvider(provider.id).then(function(response) {
        applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      }).finally(function() {
        $scope.getEmailProviders();
      });
    };

    $scope.getEmailProviders = function() {
      AdminNotificationDeliveryService.getEmailProviders().then(function(response) {
        $scope.emailProviders = response.data.result;
        $scope.parseEmailProviders();
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

    $scope.getEmailProviders();
  }
]);
