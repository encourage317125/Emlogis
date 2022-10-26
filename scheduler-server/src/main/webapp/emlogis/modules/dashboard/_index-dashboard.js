angular.module('emlogis.dashboard', ['ui.bootstrap', 'ui.router', 'http-auth-interceptor'])
  .config(
  function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider) {

    // dashboard url

    $urlRouterProvider
    .when('/dashboard', ['$state', 'authService', function ($state, authService) {
        // check calendar is enabled or not
      if (dashboardPermissions(authService) && authService.isUserAnEmployee() &&
        authService.isSchedulableEmployee()) {
        $state.go('authenticated.dashboard.calendar');
      }
      else {
        $state.go('authenticated.dashboard.manager_approvals');
      }

    }]);

    $urlRouterProvider
      .when('/dashboard/', ['$state', 'authService', function ($state, authService) {
        // check calendar is enabled or not
        if (dashboardPermissions(authService) && authService.isUserAnEmployee() &&
          authService.isSchedulableEmployee()) {
          $state.go('authenticated.dashboard.calendar');
        }
        else {
          $state.go('authenticated.dashboard.manager_approvals');
        }

      }]);



    function dashboardPermissions(authService) {
      return authService.isTenantType('Customer') && (authService.isUserAnEmployee() ||
        authService.hasPermissionIn(['Availability_RequestMgmt', 'Shift_RequestMgmt']));
    }

    $stateProvider.state('authenticated.dashboard', {
      url: '/dashboard',
//      abstract: true,
      views: {
        "content@authenticated": {
          templateUrl: "modules/dashboard/partials/dashboard.html",
          controller: 'DashboardCtrl'
        },
        "breadcrumb@authenticated": {
          templateUrl: "modules/dashboard/partials/dashboard_breadcrumb.html",
          controller: 'DashboardBreadcrumbCtrl'
        }
      },
      data: {
        ncyBreadcrumbLabel: '{{"home.MY_DASHBOARD" | translate}}',
        permissions: dashboardPermissions
      }
    })
      .state('authenticated.dashboard.calendar', {
        url: '/calendar',
        data: {
          permissions: function (authService) {
            return dashboardPermissions(authService) && authService.isUserAnEmployee() &&
              authService.isSchedulableEmployee();
          }
        }
      })
      .state('authenticated.dashboard.manager_approvals', {
        url: '/manager_approvals',
        data: {
          permissions: function (authService) {
            return dashboardPermissions(authService) && authService.hasPermissionIn(['Shift_Mgmt',
                'Availability_RequestMgmt', 'Shift_RequestMgmt']);
          }
        }
      })
      .state('authenticated.dashboard.team_member_requests', {
        url: '/team_member_requests',
        data: {
          permissions: function (authService) {
            return dashboardPermissions(authService) && authService.isUserAnEmployee() &&
              authService.isSchedulableEmployee();
          }
        }
      })
      .state('authenticated.dashboard.my_requests', {
        url: '/my_requests',
        data: {
          permissions: function (authService) {
            return dashboardPermissions(authService) && authService.isUserAnEmployee() &&
              authService.isSchedulableEmployee();
          }
        }
      })
      .state('authenticated.dashboard.my_availability', {
        url: '/my_availability',
        data: {
          permissions: function (authService) {
            return dashboardPermissions(authService) && authService.isUserAnEmployee() &&
              authService.isSchedulableEmployee();
          }
        }
      });
      //.state('authenticated.dashboard.team_member_requests2', {
      //  url: '/team_member_requests2',
      //  data: {
      //    permissions: function (authService) {
      //      return dashboardPermissions(authService) && authService.isUserAnEmployee() &&
      //        authService.isSchedulableEmployee();
      //    }
      //  }
      //});
  }
);


