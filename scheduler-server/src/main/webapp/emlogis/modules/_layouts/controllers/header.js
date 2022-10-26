// This Controller is header navigation bar controller

angular.module('emlogis').controller('HeaderCtrl',
  [
    '$scope',
    '$rootScope',
    '$location',
    '$stateParams',
    '$state',
    '$http',
    '$sessionStorage',
    '$modal',
    'authService',
    'sseService',
    'applicationContext',
    'appFunc',
    'scheduleService',
    function ($scope, $rootScope, $location, $stateParams, $state, $http, $sessionStorage,
              $modal, authService, sseService, applicationContext, appFunc, scheduleService) {


      $scope.location = $location;
      $scope.searchEntity = "Employee";
      $scope.searchEntityLabel = "Search";
      $scope.searchQuery = "";

      $scope.onLoginPage = function () {
        return $location.path() === "/";
      };

      $scope.taskLabel = 'Tasks';
      $scope.accountMgmtTaskLabel = 'Account Mgmt';

      // Navbar should be collapsed by default
      $scope.navbarCollapsed = true;

      /**
       * badge notification settings
       */
      $scope.currentAccountInfo = $sessionStorage.info && JSON.parse($sessionStorage.info);

      if ($scope.currentAccountInfo && $scope.currentAccountInfo.roles.employeerole) {

        console.log('badMsg: header');
        $scope.badgeMsg = applicationContext.getBadgeMsg();

        if (authService.hasPermissionIn(['Availability_RequestMgmt', 'Shift_RequestMgmt'])) {
          appFunc.badgeRefresh(true); // Manager = true;
        }
        else if (authService.hasPermissionIn(['Availability_Request', 'Shift_Request'])) {
          appFunc.badgeRefresh(false); // Manager = false;
        }
        if(!$scope.$$phase) {
          $scope.$apply();
        }


      }

      $scope.setSearchEntity = function (entity, label) {
        $scope.searchEntity = entity;
        $scope.searchEntityLabel = label;
      };

      $scope.fireSearch = function () {
        var x = 1;
        var currentstate = $state.current;
        var to = 'search.query';
        $state.go(to, {entity: $scope.searchEntity, q: $scope.searchQuery});
      };

      $scope.hasPermission = function (perm) {
        return authService.hasPermission(perm);
      };

      $scope.hasPermissionIn = function (perms) {
        return authService.hasPermissionIn(perms);
      };

      $scope.getUserName = function () {
        var impersonatingUser = authService.getImpersonatingUserName();
        if (impersonatingUser !== null) {
          impersonatingUser = '(' + impersonatingUser + ')';
        }
        else {
          impersonatingUser = '';
        }
        return (authService.getUserName() !== null ? impersonatingUser + authService.getUserName() : '...');
      };


      // Show Users List
      $scope.showImpersonationUsersList = function () {
        //Show Modal with users list

        $modal.open({
          templateUrl: 'modules/impersonation/partials/users_list.html',
          controller: 'ImpersonationManageCtrl',
          size: 'lg',
          windowClass: 'impersonation-users-window'
        });

        //            //keep track of dismissal of the activeDialog
        //            $scope.impersonationModal.result.then(function(reason) {
        //                console.log('Impersonation dialog closed: ' + reason);
        //                //delete $scope.impersonationModal;
        //            }, function(reason) {
        //                console.log('Impersonation dialog dismissed: ' + reason);
        //                //delete $scope.impersonationModal;
        //            });
      };


      $scope.isUserAnEmployee = function () {
        return authService.isUserAnEmployee();
      };

      // returns true/false if the user logged in
      $scope.isLoggedIn = function () {
        return !_.isEmpty(applicationContext.getUsername());
      };

      $scope.isTenantType = function (tenantType) {
        return authService.isTenantType(tenantType);
      };

      // Check Impersonated or not, Defines in HeaderCtrl
      $scope.isImpersonated = function () {
        return $sessionStorage.impersonated === true;
      };

      // Unimpersonate back to origin user
      $scope.unimpersonate = function () {
        $http.post('../emlogis/rest/sessions/ops/unimpersonate')
          .success(function (data) {

            //$http.defaults.headers.common.EmLogisToken = data.token;
            console.log('--> Unimpersonate successfull');

            // reset login info  and just restart as if we were in a new session
            $scope.tenantId = '';
            $scope.login = '';
            $scope.password = '';

            applicationContext.setUsername(data.userName);
            $sessionStorage.impersonated = false;

            authService.loginConfirmed(data);
          })
          .error(function (data) {
            // TODO show login error
            console.log('--> UnImpersonation FAILED()');

            // not much we can do, our initial session  is probably closed,
            // Move to login page

            // TODO should we clear data ?
            $rootScope.logout();
          });
      };

      $scope.showChangePassword = function () {
        if ($scope.isUserAnEmployee()) {
          $state.go("authenticated.profile.password");
        } else {
          showChangePasswordDialogBox();
        }
      };

      function showChangePasswordDialogBox() {
        $modal.open({
          templateUrl: 'modules/_layouts/partials/change_password_dlg.html',
          controller: 'ChangePasswordDialogCtrl'
        });
      }

      $scope.getPrevScheduleId = function() {
        var tmp = scheduleService.getShared();

        if (tmp.schedule && tmp.schedule.id) {
          return tmp.schedule.id;
        }
        else {
          return '';
        }
      };


    }
  ]
);

