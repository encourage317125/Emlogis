angular.module('emlogis.profile').controller('ProfilePreferencesCtrl',
  ['$scope', '$filter', 'authService', 'dataService', 'applicationContext',
    function ($scope, $filter, authService, dataService, applicationContext) {

      var userId = authService.getSessionInfo().userId;
      $scope.employeePreferences = null;
      $scope.allSystemNotifications = false;
      $scope.notificationFormats = {
        HTML: "HTML",
        PLAIN_TEXT: "PLAIN_TEXT"
      };
      $scope.emailShouldBeSpecified = $filter('translate')("nav.employeePreferences.EMAIL_SHOULD_BE_SPECIFIED");
      $scope.phoneShouldBeSpecified = $filter('translate')("nav.employeePreferences.PHONE_SHOULD_BE_SPECIFIED");

      $scope.tabs[1].active = true;

      $scope.notificationConfig = function(method) {
        if (!$scope.employeePreferences) return {};
        return _.find($scope.employeePreferences.notificationConfigs, function(p) {
          return p.method === method;
        });
      };

      $scope.hasHTMLFormat = function(method) {
        return $scope.notificationConfig(method).format === $scope.notificationFormats.HTML;
      };

      $scope.changeNotificationFormat = function(method, format) {
        $scope.notificationConfig(method).format = format;
      };

      $scope.selectAllNotifications = function() {
        if (!$scope.employeePreferences) return;
        _.forOwn($scope.employeePreferences.notificationTypes, function(value, key) {
          $scope.employeePreferences.notificationTypes[key] = $scope.allSystemNotifications;
        });
      };

      $scope.$watch("employeePreferences.notificationTypes", function(types) {
        var disabledOption = _.findKey(types, function(value) {
          return value === false;
        });
        $scope.allSystemNotifications = disabledOption === undefined;
      }, true);

      $scope.updateEmployeePreferences = function() {
        dataService.updateEmployeePreferences(userId, $scope.employeePreferences).
          then(function() {
            applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
      };

      dataService.getEmployeePreferences(userId).
        then(function(res) {
          $scope.employeePreferences = res.data;
        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
    }
  ]);


