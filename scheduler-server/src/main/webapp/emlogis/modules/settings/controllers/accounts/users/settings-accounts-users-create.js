angular.module('emlogis.settings').controller('SettingsAccountsUsersCreateCtrl', ['$scope', '$state', 'applicationContext', 'dataService',
  function ($scope, $state, applicationContext, dataService) {

    $scope.userAccount = {
      firstName: null,
      lastName: null,
      login: null,
      workEmail: null
    };

    $scope.showValidationMessages = false;

    $scope.createUserAccount = function() {
      $scope.showValidationMessages = true;

      if (!$scope.userAccountForm.$valid) return;

      dataService.createUserAccount($scope.userAccount).
        then(function(res) {
          applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
          $state.go('authenticated.settings.accounts.users.userDetails', {'entityId': res.data.id});
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
    };
  }
]);
