angular.module('emlogis.admin').controller('AdminCustomersCreateCtrl', ['$scope', '$state', '$q', '$translate', 'AdminCustomersService', 'applicationContext',
  function($scope, $state, $q, $translate, AdminCustomersService, applicationContext) {

    $scope.customerDetails = {
      smsDeliveryTenantSettingsDto: {
        providerId: '---Please select---',
        providerName: '---Please select---',
        providerType: '',
        settings: {
          tenantCallNumber: ''
        }
      },
      emailDeliveryTenantSettingsDto: {
        providerId: '---Please select---',
        providerName: '---Please select---',
        providerType: '',
        settings: {
          tenantMailBox: ''
        }
      },
      timeZone: 'UTC'
    };
    $scope.datePickerOpened = {};
    $scope.openDatePicker = function($event, datePickerName) {
      $event.preventDefault();
      $event.stopPropagation();

      $scope.datePickerOpened[datePickerName] = true;
    };

    $scope.datePickerOptions = {
      formatYear: 'yyyy',
      startingDay: 1
    };
    $q.all([$translate('app.TRIAL'), $translate('app.DISABLED')]).then(function(responses) {
      $scope.availableStatusValues = [
        {label: responses[0], value: 'Trial'},
        {label: responses[1], value: 'Disabled'}
      ];
    });

    $scope.goToCustomerListState = function() {
      $state.go('authenticated.admin.customers.list');
    };

    $scope.createCustomer = function() {
      var createdCustomerDetails = {
        name: $scope.customerDetails.name,
        tenantId: $scope.customerDetails.tenantId,
        updateDto: {}
      };
      createdCustomerDetails.updateDto = angular.copy($scope.customerDetails);
      delete createdCustomerDetails.updateDto.name;
      delete createdCustomerDetails.updateDto.tenantId;
      delete createdCustomerDetails.updateDto.smsDeliveryTenantSettingsDto.providerName;
      delete createdCustomerDetails.updateDto.smsDeliveryTenantSettingsDto.providerType;
      delete createdCustomerDetails.updateDto.emailDeliveryTenantSettingsDto.providerName;
      delete createdCustomerDetails.updateDto.emailDeliveryTenantSettingsDto.providerType;

      AdminCustomersService.createCustomer(createdCustomerDetails).then(function(response) {
        applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
        $state.go('authenticated.admin.customers.customerEdit', {tenantId: response.data.tenantId});
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        //$state.go('authenticated.admin.customers.list');
      });
    };
  }
]);
