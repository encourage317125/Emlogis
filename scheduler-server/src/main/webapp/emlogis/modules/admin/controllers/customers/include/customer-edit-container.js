angular.module('emlogis.admin').controller('CustomerEditContainerCtrl', ['$scope', 'AdminNotificationDeliveryService', 'applicationContext', 'UtilsService',
  function($scope, AdminNotificationDeliveryService, applicationContext, UtilsService) {

    $scope.submitClicked = false;
    $scope.isNotificationCollapsed = false;
    $scope.isSmsProviderSettingsCollapsed = true;
    $scope.isEmailProviderSettingsCollapsed = true;
    $scope.isOptionalModulesCollapsed = false;
    $scope.availableTimeZoneValues = [];
    $scope.smsProviders = [];
    $scope.emailProviders = [];
    $scope.currentDate = new Date();

    $scope.invertNotificationCollapsedFlag = function() {
      $scope.isNotificationCollapsed = !$scope.isNotificationCollapsed;
    };

    $scope.invertSmsProviderSettingsCollapsedFlag = function() {
      $scope.isSmsProviderSettingsCollapsed = !$scope.isSmsProviderSettingsCollapsed;
    };

    $scope.invertEmailProviderSettingsCollapsedFlag = function() {
      $scope.isEmailProviderSettingsCollapsed = !$scope.isEmailProviderSettingsCollapsed;
    };

    $scope.invertOptionalModulesCollapsedFlag = function() {
      $scope.isOptionalModulesCollapsed = !$scope.isOptionalModulesCollapsed;
    };

    $scope.setCurrentSmsProvider = function() {
      var selectedProvider = _.find($scope.smsProviders, function(provider) {return provider.id === $scope.customerDetails.smsDeliveryTenantSettingsDto.providerId;});
      $scope.customerDetails.smsDeliveryTenantSettingsDto.providerName = selectedProvider.name;
      $scope.customerDetails.smsDeliveryTenantSettingsDto.providerType = selectedProvider.providerType;
    };

    $scope.setCurrentEmailProvider = function() {
      var selectedProvider = _.find($scope.emailProviders, function(provider) {return provider.id === $scope.customerDetails.emailDeliveryTenantSettingsDto.providerId;});
      $scope.customerDetails.emailDeliveryTenantSettingsDto.providerName = selectedProvider.name;
      $scope.customerDetails.emailDeliveryTenantSettingsDto.providerType = selectedProvider.providerType;
    };

    $scope.convertModuleStatus = function(module) {
      if (module.moduleStatus === 'Disabled') {
        module.moduleStatus = 'Trial';
      } else {
        module.moduleStatus = 'Disabled';
      }
    };

    $scope.setModuleExpirationDate = function(module) {
      var dayInMilliSeconds = 24 * 3600 * 1000;
      module.moduleExpirationDate = module.moduleExpirationDateObj.getTime();
      module.remaining = Math.ceil((module.moduleExpirationDate - $scope.currentDate.getTime())/dayInMilliSeconds);
      if (module.remaining < 0) {
        module.remaining = 0;
      }
    };

    $scope.getAvailableTimeZones = function() {
      UtilsService.getAvailableTimeZones().then(function(response) {
        $scope.availableTimeZoneValues = response.data;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

    $scope.getSmsProviders = function() {
      AdminNotificationDeliveryService.getSmsProviders().then(function(response) {
        $scope.smsProviders = response.data.result;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

    $scope.getEmailProviders = function() {
      AdminNotificationDeliveryService.getEmailProviders().then(function(response) {
        $scope.emailProviders = response.data.result;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

    $scope.getAvailableTimeZones();
    $scope.getSmsProviders();
    $scope.getEmailProviders();
  }
]);
