angular.module('emlogis.employees', ['ui.bootstrap', 'ui.calendar', 'ui.router', 'http-auth-interceptor',
  'ui.grid.resizeColumns', 'ui.grid.pagination', 'ui.grid.pinning' ])
  .config(
  [
    '$stateProvider', '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {

      $urlRouterProvider.when('/employees/:id/detail', '/employees/:id/detail/calendar');

      $stateProvider.state('authenticated.employees', {
        url: '/employees',
        abstract: true,
        views: {
          "content@authenticated": {
            templateUrl: "modules/employees/partials/employees.html",
            controller: 'EmployeesCtrl'
          },
          "breadcrumb@authenticated": {
            templateUrl: "modules/employees/partials/employee_breadcrumb.html",
            controller: 'EmployeesBreadcrumbCtrl'
          }
        },
        data: {
          permissions: function (authService) {
            return authService.isTenantType('Customer') && authService.hasPermissionIn(
                ['Demand_View','Demand_Mgmt','Availability_RequestMgmt','Shift_RequestMgmt','Shift_Mgmt',
                  'SystemConfiguration_Mgmt','Employee_Mgmt']);
          }
        }
      })
        .state('authenticated.employees.list', {
          url: '/list',
          views: {
            "employeeContent@authenticated.employees": {
              templateUrl: "modules/employees/partials/employees_list.html",
              controller: 'EmployeesListCtrl'
            },
            "employeeBreadcrumb@authenticated.employees": {
              templateUrl: "modules/employees/partials/employee_breadcrumb.html",
              controller: "EmployeesBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.LIST"|translate}}'
          }
        })
        .state('authenticated.employees.detail', {
          url: '/:id/detail',
          views: {
            "employeeContent@authenticated.employees": {
              templateUrl: "modules/employees/partials/employee-details/employees_detail.html",
              controller: 'EmployeesDetailCtrl'
            },
            "employeeBreadcrumb@authenticated.employees": {
              templateUrl: "modules/employees/partials/employee_breadcrumb.html",
              controller: "EmployeesBreadcrumbCtrl"
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAIL"|translate}}'
          }
        })
        .state('authenticated.employees.detail.currentCalendar', {
          url: '/calendar',
          views: {
            "detailsTabContent@authenticated.employees.detail": {
              templateUrl: "modules/employees/partials/employee-details/tabs/current_calendar.html",
              controller: 'EmployeeDetailsCalendarCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"employees.tabs.CURRENT_CALENDAR"|translate}}'
          }
        })
        .state('authenticated.employees.detail.availability', {
          url: '/availability',
          views: {
            "detailsTabContent@authenticated.employees.detail": {
              templateUrl: "modules/employees/partials/employee-details/tabs/availability.html",
              controller: 'EmployeeDetailsAvailabilityCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"employees.tabs.AVAILABILITY"|translate}}'
          }
        })
        .state('authenticated.employees.detail.settings', {
          url: '/settings',
          views: {
            "detailsTabContent@authenticated.employees.detail": {
              templateUrl: "modules/employees/partials/employee-details/tabs/settings.html",
              controller: 'EmployeeDetailsSettingsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"employees.tabs.SETTINGS"|translate}}'
          }
        });


    }
  ]
)
.filter('wage', function () {
  return function (number) {
    if (number > 0) {
      number = number/100;
      return '$' + number;
    } else {
      return '';
    }
  };
})
.filter('gender', function () {
  return function (str) {
    switch (str) {
      case '1':
        return 'employees.MALE';
      case '2':
        return 'employees.FEMALE';
      case '0':
        return 'employees.NA';
      default:
        return str;
    }
  };
})
.filter('employeeType', function () {
  return function (str) {
    return 'employees.' + str;
  };
})
.filter('minutesToDays', function () {
  return function (minutes) {
    var days = minutes / 24 / 60;
    var hours = minutes / 60 % 24;
    var minutesLeft = minutes % 60;
    return days > 0 ? days +  'd '  : '' +
           hours > 0 ? hours + 'h ' : '' +
           minutesLeft > 0 ? minutesLeft + 'm' : '';
  };
});
