angular.module('emlogis.schedule_builder',
  [
    'ui.bootstrap',
    'ui.router',
    'http-auth-interceptor',
    'ui.grid.resizeColumns',
    'emlogis.commonservices',
    'emlogis.commonDirectives',
    'emlogis.commonControllers'
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

      $urlRouterProvider.when('/schedule_builder', '/schedule_builder/create_schedules/');
      $urlRouterProvider.when('/schedule_builder/', '/schedule_builder/create_schedules/');
      // Schedule Builder Module Url
      $modalProvider.options.animation = false;
      $stateProvider.state('authenticated.schedule_builder', {
        url: '/schedule_builder',
        abstract: true,
        views: {
          "content@authenticated": {
            templateUrl: "modules/schedule_builder/partials/schedule_builder.html",
            controller: 'ScheduleBuilderCtrl'
          },
          "breadcrumb@authenticated": {
            templateUrl: "modules/schedule_builder/partials/schedule_builder_breadcrumb.html",
            controller: 'ScheduleBuilderBreadcrumbCtrl'
          }
        },
        data: {
          permissions: function (authService) {
            return authService.isTenantType('Customer') && authService.hasPermissionIn(['Demand_View', 'Demand_Mgmt']);
          }
        }
      })
        .state('authenticated.schedule_builder.create_schedules', {
          url: '/create_schedules/:id',
          views: {
            "scheduleBuilderContent@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/schedule_builder_create_schedules.html",
              controller: 'ScheduleBuilderCreateSchedulesCtrl'
            },
            "scheduleBuilderBreadcrumb@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/schedule_builder_create_schedules_breadcrumb.html",
              controller: "ScheduleBuilderCreateSchedulesBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"schedule_builder.CREATE_SCHEDULES" | translate}}'
          }
        })
        .state('authenticated.schedule_builder.shift_patterns', {
          url: '/shift_patterns',
          views: {
            "scheduleBuilderContent@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/schedule_builder_shift_patterns.html",
              controller: 'ScheduleBuilderShiftPatternsCtrl'
            },
            "scheduleBuilderBreadcrumb@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/schedule_builder_shift_patterns_breadcrumb.html",
              controller: "ScheduleBuilderShiftPatternsBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"schedule_builder.SHIFT_PATTERNS" | translate}}'
          }
        })
        .state('authenticated.schedule_builder.shift_bidding', {
          url: '/shift_bidding',
          views: {
            "scheduleBuilderContent@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/schedule_builder_shift_bidding.html",
              controller: 'ScheduleBuilderShiftBiddingCtrl'
            },
            "scheduleBuilderBreadcrumb@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/schedule_builder_shift_bidding_breadcrumb.html",
              controller: "ScheduleBuilderShiftBiddingBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"schedule_builder.SHIFT_BIDDING" | translate}}'
          }
        })
        .state('authenticated.schedule_builder.generation_report', {
          url: '/generation_report/:id',
          views: {
            "scheduleBuilderContent@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/reports/schedule_builder_generation_report.html",
              controller: 'ScheduleBuilderGenerationReportCtrl'
            },
            "scheduleBuilderBreadcrumb@authenticated.schedule_builder": {
              templateUrl: "modules/schedule_builder/partials/reports/schedule_builder_generation_report_breadcrumb.html"
//                        controller: "ScheduleBuilderGenerationReportBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"reports.SCHEDULE_GENERATION_REPORTS" | translate}}'
          }
        });

    }
  ]
);


