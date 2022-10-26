(function () {
  'use strict';
  angular.module('emlogis.login', ['http-auth-interceptor'])
    .controller('LoginController', ['$scope', '$http', '$sessionStorage', '$location', 'authService', 'applicationContext', 'appFunc',
      function ($scope, $http, $sessionStorage, $location, authService, applicationContext, appFunc) {

        var host = $location.host(),
            tenantIdPart = host.substring(0, host.indexOf('.'));

        $scope.tenantId = '';
        $scope.userName = '';
        $scope.password = '';
        $scope.oldPassword = "";
        $scope.newPassword = "";
        $scope.repeatPassword = "";
        $scope.showTenantIdInput = false;

        function validTenantIdPart(tenantIdPart) {
          if (tenantIdPart.indexOf('uidev') !== -1 || tenantIdPart.indexOf('qa.') !== -1) return false;
          if (tenantIdPart === "cloud") return false;
          if (tenantIdPart.length < 3 || tenantIdPart.length > 32) return false;
          if (/^[a-z0-9-]*$/.test(tenantIdPart) === false) return false;
          if (tenantIdPart.indexOf("-") === 0 || tenantIdPart.indexOf("-") === tenantIdPart.length-1) return false;
          if (tenantIdPart.charAt(0) >= '0' && tenantIdPart.charAt(0) <= '9') return false;
          return true;
        }

        // production variant
        if (host.indexOf('emlogis.com') !== -1 && validTenantIdPart(tenantIdPart)) {
            $scope.tenantId = tenantIdPart;
        }
        // default values for dev cloud environment
        else {
          $scope.showTenantIdInput = true;
          $scope.tenantId = 'tmp';
          $scope.userName = 'alishabe';
          $scope.password = 'chgpwd';
        }

        $scope.forms = {
          0: "Login",
          1: "Forgot Password",
          2: "Change Password"
        };

        $scope.showForm = $scope.forms[0];

        $scope.login = function () {
          $sessionStorage.impersonated = null;

          $http.post('../emlogis/rest/sessions', {
            tenantId: $scope.tenantId,
            login: $scope.userName,
            password: CryptoJS.SHA256($scope.password + "." + $scope.tenantId).toString()
          })
          .success(function(data) {
            applicationContext.setUsername(data.userName);

            data.tenantId = $scope.tenantId;
            authService.loginConfirmed(data);

            // reset login info
            $scope.tenantId = '';
            $scope.userName = '';
            $scope.password = '';

            if (authService.hasPermissionIn(['Availability_RequestMgmt', 'Shift_RequestMgmt'])) {
              appFunc.badgeRefresh(true); // Manager = true;
            }
            else if (authService.hasPermissionIn(['Availability_Request', 'Shift_Request'])) {
              appFunc.badgeRefresh(false); // Manager = false;
            }
          })
          .error(function(err) {
              var msg = err.info || "Failed to login. Please check your credentials.";
              applicationContext.setNotificationMsgWithValues(msg, 'danger', true);
              // TODO: Check how exception will come
              if (err.exception === "ForceChangeOnFirstLogonException" || err.exception === "PendingPasswordChangeException") {
                $scope.showForm = $scope.forms[2];
              }
          });
        };

        $scope.requestPasswordReset = function() {
          $http.post('../emlogis/rest/sessions/ops/resetpassword', {
            tenantId: $scope.tenantId,
            login: $scope.userName
          })
          .success(function(data) {
            var message = data.info || "Instructions have been sent to the emai " + data.emailAddress;
            applicationContext.setNotificationMsgWithValues(message, 'success', true);
            $location.path('/');
          })
          .error(function(err) {
            var msg = err.info || err;
            applicationContext.setNotificationMsgWithValues(msg, 'danger', true);
          });
        };

        $scope.changePassword = function() {
          $http.post('../emlogis/rest/sessions/ops/chgpassword ', {
            tenantId: $scope.tenantId,
            login: $scope.userName,
            oldPassword: CryptoJS.SHA256($scope.oldPassword + "." + $scope.tenantId).toString(),
            newPassword: $scope.newPassword
          })
          .success(function() {
            applicationContext.setNotificationMsgWithValues("Password changed", 'success', true);
            $scope.password = $scope.newPassword;
            $scope.login();
          })
          .error(function(err) {
            var msg = err.info || err;
            applicationContext.setNotificationMsgWithValues(msg, 'danger', true);
          });
        };

        $scope.passwordsMatch = function() {
          return $scope.newPassword && $scope.newPassword === $scope.repeatPassword;
        };

        $scope.$watch("newPassword", function(p) {
          var margin = p ? "-3px" : "15px";
          $(".new-pwd").css("margin-bottom", margin);
        });

        $scope.$watch("repeatPassword", function(p) {
          var margin = $scope.passwordsMatch() ? "-3px" : "15px";
          $(".repeat-pwd").css("margin-bottom", margin);
        });

    }]);
})();
