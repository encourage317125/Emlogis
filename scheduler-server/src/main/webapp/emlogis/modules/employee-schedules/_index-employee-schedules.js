angular.module('emlogis.employeeSchedules', ['ui.bootstrap', 'ui.router', 'emlogis.commonservices', 'emlogis.commonDirectives', 'emlogis.commonControllers'])
  .config(['$stateProvider', '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {

      $urlRouterProvider.when('/employee-schedules', '/employee-schedules/week-view');
      $urlRouterProvider.when('/employee-schedules/', '/employee-schedules/week-view');

      $stateProvider.state('authenticated.employeeSchedules', {
        url: '/employee-schedules',
        abstract: true,
        views: {
          'content@authenticated': {
            templateUrl: 'modules/employee-schedules/partials/employee-schedules.html',
            controller: 'EmployeeSchedulesCtrl'
          },
          'breadcrumb@authenticated': {
            templateUrl: 'modules/employee-schedules/partials/employee-schedules-breadcrumb.html',
            controller: 'EmployeeSchedulesBreadcrumbCtrl'
          }
        },
        data: {
          ncyBreadcrumbLabel: '{{"nav.EMPLOYEE_SCHEDULES" | translate}}',
          permissions: function (authService) {
            return authService.isTenantType('Customer') &&
              authService.hasPermissionIn(['Demand_View', 'Demand_Mgmt', 'Shift_Mgmt']);
          }
        }
      })
        .state('authenticated.employeeSchedules.weekView', {
          url: '/week-view',
          views: {
            'employeeSchedulesContent@authenticated.employeeSchedules': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-week-view.html',
              controller: 'EmployeeSchedulesWeekViewCtrl'
            },
            'employeeSchedulesBreadcrumb@authenticated.employeeSchedules': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-week-view-breadcrumb.html',
              controller: 'EmployeeSchedulesWeekViewBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"nav.employee_schedules.WEEK_VIEW" | translate}}'
          }
        })
        .state('authenticated.employeeSchedules.weekView.schedule', {
          url: '/:scheduleId',
          views: {
            'employeeSchedulesWeekViewContent@authenticated.employeeSchedules.weekView': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-week-view-schedule.html',
              controller: 'EmployeeSchedulesWeekViewScheduleCtrl'
            },
            'employeeSchedulesWeekViewBreadcrumb@authenticated.employeeSchedules.weekView': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-week-view-schedule-breadcrumb.html',
              controller: 'EmployeeSchedulesWeekViewScheduleBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"nav.employee_schedules.SCHEDULE" | translate}}'
          }
        })
        .state('authenticated.employeeSchedules.dayView', {
          url: '/day-view',
          views: {
            'employeeSchedulesContent@authenticated.employeeSchedules': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-day-view.html',
              controller: 'EmployeeSchedulesDayViewCtrl'
            },
            'employeeSchedulesBreadcrumb@authenticated.employeeSchedules': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-day-view-breadcrumb.html',
              controller: 'EmployeeSchedulesDayViewBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"nav.employee_schedules.DAY_VIEW" | translate}}'
          }
        })
        .state('authenticated.employeeSchedules.dayView.schedule', {
          url: '/:scheduleId/:dateTimeStamp',
          views: {
            'employeeSchedulesDayViewContent@authenticated.employeeSchedules.dayView': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-day-view-schedule.html',
              controller: 'EmployeeSchedulesDayViewScheduleCtrl'
            },
            'employeeSchedulesDayViewBreadcrumb@authenticated.employeeSchedules.dayView': {
              templateUrl: 'modules/employee-schedules/partials/employee-schedules-day-view-schedule-breadcrumb.html',
              controller: 'EmployeeSchedulesDayViewScheduleBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"nav.employee_schedules.SCHEDULE" | translate}}'
          }
        });
    }
  ])
  .run([function() {

  }]);
