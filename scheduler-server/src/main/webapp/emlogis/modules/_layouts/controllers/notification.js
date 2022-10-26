var app = angular.module('emlogis');

// It will show the several notifications such as invalidated token, etc
app.controller('NotificationCtrl', ['$scope', '$location', '$state',
  '$http', 'applicationContext',
  function ($scope, $location, $state, $http,
            applicationContext) {

    $scope.msg = applicationContext.getNotificationMsg();


    // Close Alert
    $scope.closeAlert = function () {
      var msg = applicationContext.getNotificationMsg();
      msg.visible = false;
      applicationContext.setNotificationMsg(msg);
    };

    /**
     * It will logout from the system and move to login page,
     * after login it will show to saved page
     */
    $scope.moveToLogin = function (param) {
      applicationContext.setAfterLoginUrl($location.path());
      $scope.logout(param);
    };

    // Show Logout and Refresh button
    $scope.showLogoutButton = function () {
      return $scope.msg.visible === true && $scope.msg.type === 'login';
    };

    // Show Save button
    $scope.showSaveButton = function () {
      return $scope.msg.visible === true && $scope.msg.type === 'save';
    };

    $scope.saveAction = function () {
      var working = applicationContext.getWorking();
      return working.saveFunc();
    };


  }
]);



