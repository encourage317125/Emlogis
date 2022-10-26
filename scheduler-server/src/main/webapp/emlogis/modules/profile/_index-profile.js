angular.module('emlogis.profile',
  [
    'ui.bootstrap',
    'ui.router',
    'http-auth-interceptor'
  ])
  .config(
  [
    '$stateProvider',
    '$urlRouterProvider',
    '$locationProvider',
    '$httpProvider',
    '$breadcrumbProvider',
    '$modalProvider',
    function ($stateProvider, $urlRouterProvider, $locationProvider,
              $httpProvider, $breadcrumbProvider, $modalProvider) {

      // Schedule Builder Module Url
      $modalProvider.options.animation = false;
      $stateProvider.state('authenticated.profile', {
        url: '/profile',
        abstract: true,
        views: {
          "content@authenticated": {
            templateUrl: "modules/profile/partials/profile.html",
            controller: 'ProfileCtrl'
          },
          "breadcrumb@authenticated": {
            templateUrl: "modules/profile/partials/profile_breadcrumb.html",
            controller: 'ProfileBreadcrumbCtrl'
          }
        }
      })
        .state('authenticated.profile.detail', {
          url: '/detail',
          views: {
            "profileContent@authenticated.profile": {
              templateUrl: "modules/profile/partials/profile_detail.html",
              controller: 'ProfileDetailCtrl as profile'
            },
            "profileBreadcrumb@authenticated.profile": {
              templateUrl: "modules/profile/partials/profile_detail_breadcrumb.html",
              controller: "ProfileDetailBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAIL" | translate}}',
            permissions: function (authService) {
              return authService.isUserAnEmployee() && authService.hasPermission('AccountProfile_Update');
            }
          }
        })
        .state('authenticated.profile.preferences', {
          url: '/preferences',
          views: {
            "profileContent@authenticated.profile": {
              templateUrl: "modules/profile/partials/profile_preferences.html",
              controller: 'ProfilePreferencesCtrl'
            },
            "profileBreadcrumb@authenticated.profile": {
              templateUrl: "modules/profile/partials/profile_preferences_breadcrumb.html",
              controller: "ProfilePreferencesBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"profile.PREFERENCES" | translate}}',
            permissions: function (authService) {
              return authService.hasPermission('AccountProfile_Update');
            }
          }
        })
        .state('authenticated.profile.password', {
          url: '/password',
          views: {
            "profileContent@authenticated.profile": {
              templateUrl: "modules/profile/partials/profile_password.html",
              controller: 'ProfilePasswordCtrl'
            },
            "profileBreadcrumb@authenticated.profile": {
              templateUrl: "modules/profile/partials/profile_password_breadcrumb.html",
              controller: "ProfilePasswordBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"profile.CHANGE_PASSWORD" | translate}}',
            permissions: function (authService) {
              return authService.hasPermission('AccountProfile_Update');
            }
          }
        });

    }
  ]
);


