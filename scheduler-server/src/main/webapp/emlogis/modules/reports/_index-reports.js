angular.module('emlogis.reports',
  [
    'ui.bootstrap',
    'ui.router',
    'http-auth-interceptor',
    'angularLoad',
    'emlogis.commonservices',
    'emlogis.commonDirectives'])
  .config(['$stateProvider', '$urlRouterProvider', '$locationProvider', '$httpProvider', '$breadcrumbProvider',
    function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, $breadcrumbProvider) {

      $urlRouterProvider.when('/reports', '/reports/staffing');
      $urlRouterProvider.when('/reports/', '/reports/staffing');

      $stateProvider.state('authenticated.reports', {
        url: '/reports',
        abstract: true,
        views: {
          "content@authenticated": {
            templateUrl: "modules/reports/partials/reports.html",
            controller: 'ReportsCtrl'
          },
          "breadcrumb@authenticated": {
            templateUrl: "modules/reports/partials/reports_breadcrumb.html",
            controller: 'ReportsBreadcrumbCtrl'
          }
        },
        data: {
          permissions: function (authService) {
            return authService.isTenantType('Customer') &&
              authService.hasPermissionIn(['Demand_View','Demand_Mgmt','Shift_Mgmt','SystemConfiguration_View','SystemConfiguration_Mgmt']);
          }
        }
      })
        .state('authenticated.reports.group', {
          url: '/:groupId',
          views: {
            "reportContent@authenticated.reports": {
              templateUrl: "modules/reports/partials/reports-group.partial.html",
              controller: 'ReportsGroupCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{group}}'
          }
        })
        .state('authenticated.reports.group.details', {
          url: '/:id',
          views: {
            "birtReportContent@authenticated.reports.group": {
              templateUrl: "modules/reports/partials/reports-details.partial.html",
              controller: 'ReportsDetailsCtrl'
            }
          },
          data: {
            ncyBreadcrumbSkip: true // Never display this state in breadcrumb.
          }
        })

      ;
    }
  ]);


