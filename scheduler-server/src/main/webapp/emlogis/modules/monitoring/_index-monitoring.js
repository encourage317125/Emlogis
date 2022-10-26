angular.module('emlogis.monitoring', ['ui.bootstrap', 'ui.router', 'http-auth-interceptor', 'ui.grid.pagination', 'ui.grid.selection', 'ui.grid.resizeColumns', 'emlogis.commonservices', 'emlogis.commonDirectives', 'frapontillo.bootstrap-switch'])
  .config(['$stateProvider', '$urlRouterProvider', '$locationProvider', '$httpProvider', '$breadcrumbProvider',
    function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, $breadcrumbProvider) {
      $stateProvider.state('authenticated.monitoring', {
        url: '/monitoring',
        abstract: true,
        views: {
          "content@authenticated": {
            templateUrl: "modules/monitoring/partials/monitoring.html",
            controller: 'MonitoringCtrl'
          },
          "breadcrumb@authenticated": {
            templateUrl: "modules/monitoring/partials/monitoring_breadcrumb.html",
            controller: 'MonitoringBreadcrumbCtrl'
          }
        },
        data: {
          ncyBreadcrumbLabel: '{{"nav.MONITORING" | translate}}',
          permissions: function (authService) {
            return authService.isTenantType('SvcProvider') && authService.hasPermission('SystemConfiguration_Mgmt');
          }
        }
      })
        .state('authenticated.monitoring.database', {
          url: '/database',
          views: {
            "monitoringContent@authenticated.monitoring": {
              templateUrl: "modules/monitoring/partials/database/monitoring_database.html",
              controller: 'MonitoringDatabaseCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.DATABASE" | translate}}'
          }
        })
        .state('authenticated.monitoring.database.summary', {
          url: '/summary',
          views: {
            "monitoringDatabaseContent@authenticated.monitoring.database": {
              templateUrl: "modules/monitoring/partials/database/monitoring_database_summary.html",
              controller: 'MonitoringDatabaseSummaryCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.DATABASE_SUMMARY" | translate}}'
          }
        })
        .state('authenticated.monitoring.database.percustomer', {
          url: '/percustomer',
          views: {
            "monitoringDatabaseContent@authenticated.monitoring.database": {
              templateUrl: "modules/monitoring/partials/database/monitoring_database_percustomer.html",
              controller: 'MonitoringDatabasePerCustomerCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.DATABASE_PER_CUSTOMER" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems', {
          url: '/subsystems',
          views: {
            "monitoringContent@authenticated.monitoring": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems.html",
              controller: 'MonitoringSubsystemsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.SUBSYSTEMS" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.hazelcast', {
          url: '/hazelcast',
          views: {
            "monitoringSubsystemsContent@authenticated.monitoring.subsystems": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_hazelcast.html",
              controller: 'MonitoringSubsystemsHazelcastCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.SUBSYSTEMS_HAZELCAST" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.notifications', {
          url: '/notifications',
          views: {
            "monitoringSubsystemsContent@authenticated.monitoring.subsystems": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_notifications.html",
              controller: 'MonitoringSubsystemsNotificationsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.SUBSYSTEMS_NOTIFICATIONS" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.activesessions', {
          url: '/activesessions',
          views: {
            "monitoringSubsystemsContent@authenticated.monitoring.subsystems": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_active_sessions.html",
              controller: 'MonitoringSubsystemsActiveSessionsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.SUBSYSTEMS_ACTIVE_SESSIONS" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.activesessions.details', {
          url: '/:id',
          views: {
            "monitoringContent@authenticated.monitoring": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_active_sessions_details.html",
              controller: 'MonitoringSubsystemsActiveSessionsDetailsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAILS" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.appservers', {
          url: '/appservers',
          views: {
            "monitoringSubsystemsContent@authenticated.monitoring.subsystems": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_appservers.html",
              controller: 'MonitoringSubsystemsAppServersCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.SUBSYSTEMS_APPSERVERS" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.appservers.details', {
          url: '/:id',
          views: {
            "monitoringContent@authenticated.monitoring": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_appservers_details.html",
              controller: 'MonitoringSubsystemsAppServersDetailsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAILS" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.engines', {
          url: '/engines',
          views: {
            "monitoringSubsystemsContent@authenticated.monitoring.subsystems": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_engines.html",
              controller: 'MonitoringSubsystemsEnginesCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"monitoring.SUBSYSTEMS_ENGINES" | translate}}'
          }
        })
        .state('authenticated.monitoring.subsystems.engines.details', {
          url: '/:id',
          views: {
            "monitoringContent@authenticated.monitoring": {
              templateUrl: "modules/monitoring/partials/subsystems/monitoring_subsystems_engines_details.html",
              controller: 'MonitoringSubsystemsEnginesDetailsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAILS" | translate}}'
          }
        })
      ;
    }]);


