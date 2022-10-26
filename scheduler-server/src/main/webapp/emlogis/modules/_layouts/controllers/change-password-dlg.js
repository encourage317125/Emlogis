angular.module('emlogis').controller('ChangePasswordDialogCtrl',
  ['$scope', '$modalInstance', 'dataService', 'applicationContext', 'authService',
    function ($scope, $modalInstance, dataService, applicationContext, authService) {

      $scope.passwords = {
        currentPassword: "",
        newPassword: "",
        repeatPassword: ""
      };

      $scope.passwordsMatch = function() {
        return $scope.passwords.newPassword && $scope.passwords.newPassword === $scope.passwords.repeatPassword;
      };

      $scope.changePassword = function() {
        var hashedPassword = CryptoJS.SHA256($scope.passwords.currentPassword + "." + authService.getSessionInfo().tenantId).toString();

        dataService.changePassword(hashedPassword, $scope.passwords.newPassword).
          then(function() {
            applicationContext.setNotificationMsgWithValues("Password changed", 'success', true);
            $modalInstance.dismiss();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };

    }
  ]);

