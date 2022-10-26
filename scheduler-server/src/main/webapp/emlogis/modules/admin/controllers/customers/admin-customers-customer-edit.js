angular.module('emlogis.admin').controller('AdminCustomersCustomerEditCtrl', [
  '$scope',
  '$state',
  '$q',
  '$stateParams',
  '$translate',
  'AdminCustomersService',
  'applicationContext',
  'dialogs',
  '$filter',
  function ($scope,
            $state,
            $q,
            $stateParams,
            $translate,
            AdminCustomersService,
            applicationContext,
            dialogs,
            $filter) {

    $scope.tenantId = $stateParams.tenantId;
    $scope.customer = null;
    $scope.customerDetails = {};
    $scope.overviewInEditMode = false;
    $scope.overviewEditButtonLabel = 'app.EDIT_STATUS';
    $scope.datePickerOpened = {};
    $scope.currentDate = new Date();
    $scope.openDatePicker = function ($event, datePickerName) {
      $event.preventDefault();
      $event.stopPropagation();

      $scope.datePickerOpened[datePickerName] = true;
    };

    $scope.datePickerOptions = {
      formatYear: 'yyyy',
      startingDay: 1
    };
    $q.all([$translate('app.TRIAL'), $translate('app.DISABLED')]).then(function (responses) {
      $scope.availableStatusValues = [
        {label: responses[0], value: 'Trial'},
        {label: responses[1], value: 'Disabled'}
      ];
    });

    $scope.goToCustomerListState = function () {
      $state.go('authenticated.admin.customers.list');
    };

    $scope.switchEditMode = function () {
      $scope.overviewInEditMode = !$scope.overviewInEditMode;
      if ($scope.overviewInEditMode) {
        $scope.overviewEditButtonLabel = 'app.END_EDIT';
      } else {
        $scope.overviewEditButtonLabel = 'app.EDIT_STATUS';
      }
    };

    $scope.setExpirationRelatedValues = function () {
      var dayInMilliSeconds = 24 * 3600 * 1000;
      var expirationDateInLong = $scope.customerDetails.productLicenseInfo.moduleExpirationDateObj.getTime();
      var todayInLong = $scope.currentDate.getTime();
      $scope.customerDetails.productLicenseInfo.moduleExpirationDate = $scope.customerDetails.productLicenseInfo.moduleExpirationDateObj.getTime();
      $scope.customerDetails.productLicenseInfo.remaining = Math.ceil((expirationDateInLong - todayInLong) / dayInMilliSeconds);
      if ($scope.customerDetails.productLicenseInfo.remaining < 0) {
        $scope.customerDetails.productLicenseInfo.remaining = 0;
      }
    };

    $scope.fillCustomerDetails = function () {
      $scope.customerDetails = angular.copy($scope.customer);
      $scope.customerDetails.created = new Date($scope.customer.created);
      $scope.customerDetails.productLicenseInfo.moduleExpirationDateObj = new Date($scope.customer.productLicenseInfo.moduleExpirationDate);
      var dayInMilliSeconds = 24 * 3600 * 1000;
      $scope.customerDetails.productLicenseInfo.remaining = Math.ceil(($scope.customer.productLicenseInfo.moduleExpirationDate - $scope.currentDate.getTime()) / dayInMilliSeconds);
      if ($scope.customerDetails.productLicenseInfo.remaining < 0) {
        $scope.customerDetails.productLicenseInfo.remaining = 0;
      }
      $scope.customerDetails.smsDeliveryTenantSettingsDto = {
        providerId: $scope.customer.smsDeliveryTenantSettingsDto.providerId,
        providerName: $scope.customer.smsDeliveryTenantSettingsDto.providerName,
        providerType: $scope.customer.smsDeliveryTenantSettingsDto.providerType,
        settings: {
          tenantCallNumber: $scope.customer.smsDeliveryTenantSettingsDto.settings.tenantCallNumber
        }
      };
      $scope.customerDetails.emailDeliveryTenantSettingsDto = {
        providerId: $scope.customer.emailDeliveryTenantSettingsDto.providerId,
        providerName: $scope.customer.emailDeliveryTenantSettingsDto.providerName,
        providerType: $scope.customer.emailDeliveryTenantSettingsDto.providerType,
        settings: {
          tenantMailBox: $scope.customer.emailDeliveryTenantSettingsDto.settings.tenantMailBox
        }
      };
      angular.forEach($scope.customerDetails.modulesLicenseInfo, function (module) {
        module.moduleExpirationDateObj = new Date(module.moduleExpirationDate);
        module.remaining = Math.ceil((module.moduleExpirationDate - $scope.currentDate.getTime()) / dayInMilliSeconds);
        if (module.remaining < 0) {
          module.remaining = 0;
        }
      });
    };

    $scope.saveCustomer = function () {
      var savedCustomerDetails = angular.copy($scope.customerDetails);
      delete savedCustomerDetails.tenantId;
      delete savedCustomerDetails.clName;
      delete savedCustomerDetails.created;
      delete savedCustomerDetails.updated;
      delete savedCustomerDetails.productLicenseInfo.clName;
      delete savedCustomerDetails.productLicenseInfo.moduleName;
      delete savedCustomerDetails.productLicenseInfo.moduleExpirationDateObj;
      delete savedCustomerDetails.productLicenseInfo.remaining;
      angular.forEach(savedCustomerDetails.modulesLicenseInfo, function (module) {
        delete module.moduleExpirationDateObj;
        delete module.clName;
        delete module.remaining;
      });
      delete savedCustomerDetails.emailDeliveryTenantSettingsDto.providerName;
      delete savedCustomerDetails.emailDeliveryTenantSettingsDto.providerType;
      delete savedCustomerDetails.smsDeliveryTenantSettingsDto.providerName;
      delete savedCustomerDetails.smsDeliveryTenantSettingsDto.providerType;
      delete savedCustomerDetails.nbOfSites;
      delete savedCustomerDetails.nbOfTeams;
      delete savedCustomerDetails.nbOfEmployees;
      delete savedCustomerDetails.lastLoggingDate;
      delete savedCustomerDetails.lastLoggingUserName;

      AdminCustomersService.updateCustomer($scope.tenantId, savedCustomerDetails).then(function (response) {
        applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      }).finally(function () {
        $scope.initEditDetails();
      });
    };

    $scope.deleteCustomer = function () {
      // Confirm deletion
      var question = $filter('translate')("admin.customers.DELETE_CUSTOMER") + $scope.customerDetails.name + '?';
      var dlg = dialogs.confirm('app.PLEASE_CONFIRM', question);

      dlg.result.then(function () {
        AdminCustomersService.deleteCustomer($scope.tenantId).then(function (response) {
          applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
          $state.go('authenticated.admin.customers.list');
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          $scope.initEditDetails();
        });
      });
    };

    $scope.initEditDetails = function () {
      AdminCustomersService.getCustomerDetails($scope.tenantId).then(function (response) {
        $scope.customer = response.data;
        $scope.fillCustomerDetails();
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

    $scope.initEditDetails();
  }
]);
