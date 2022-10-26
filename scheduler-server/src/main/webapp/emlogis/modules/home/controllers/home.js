var home = angular.module('emlogis.home');

home.controller('HomeCtrl', ['$scope', '$state', '$filter', 'authService', 'dataService', '$q',
  function ($scope, $state, $filter, authService, dataService, $q) {

    console.log('inside home controller');
    var home = this;

    // Set Username
    //
    //if (applicationContext.getUsername() === ""){
    //    applicationContext.setUsername($sessionStorage.username);
    //}
    //
    //$scope.username = applicationContext.getUsername();
    //
    //// Set Variables for accordion
    //$scope.sideBar = {
    //    isManager_ApprovalsOpen : true,
    //    isTeam_Member_RequestsOpen : true
    //};

    var session = authService.getSessionInfo();

    /*if (authService.hasPermission('Demand_Mgmt')) {          // if user is Schedule Creator
     $state.go('authenticated.schedule_builder.create_schedules');
     }
     else if (authService.hasPermissionIn(['Tenant_View', 'Tenant_Mgmt'])) {   // if user is a ServiceAdministrator
     $state.go('authenticated.admin.customers');
     }
     else if (authService.hasPermission('SystemConfiguration_Mgmt') &&       // if user is a customer Admin
     authService.hasPermission('Employee_Mgmt') && session.tenantType === 'Customer') {
     $state.go('authenticated.rules.site_teams');
     }
     else if (authService.hasPermission('SystemConfiguration_Mgmt') &&       // if user is a EmlogisService Admin
     session.tenantType === 'SvcProvider') {
     $state.go('authenticated.monitoring.database');
     }
     else if (authService.hasPermissionIn(['Availability_RequestMgmt', 'Shift_RequestMgmt', 'Shift_Mgmt'])) {   // if user is a ShiftManager
     $state.go('authenticated.dashboard.manager_approvals');
     }
     else if (session.employeeId !== null) {    // if user is a Employee
     $state.go('authenticated.dashboard.calendar');
     }
     else if (authService.hasPermission('Support')   // if user is a Support Employee
     && authService.hasPermission('Employee_View')) {
     $state.go('authenticated.employees.list');
     }*/


    var initCounters = function (dto) {

      home.statItems = [

        // Customers

        {
          title: $filter('translate')('home.CUSTOMERS'),
          num: dto.customerCount,
          visible: session.tenantType === 'SvcProvider',
          url: 'authenticated.admin.customers.list'
        },

        // Support

        {
          title: $filter('translate')('home.SERVERS'),
          num: dto.appServerCount,
          visible: session.tenantType === 'SvcProvider' ||
          (authService.hasPermission('Support') && authService.hasPermission('Employee_View')),
          url: 'authenticated.monitoring.subsystems.appservers'
        },
        {
          title: $filter('translate')('home.ENGINES'),
          num: dto.engineCount,
          visible: session.tenantType === 'SvcProvider' ||
          (authService.hasPermission('Support') && authService.hasPermission('Employee_View')),
          url: 'authenticated.monitoring.subsystems.engines'
        },

        // Settings

        {
          title: $filter('translate')('nav.SITES'),
          num: dto.siteCount,
          visible: true,
          url: 'authenticated.rules.site_teams'
        },
        {
          title: $filter('translate')('nav.TEAMS'),
          num: dto.teamCount,
          visible: true,
          url: 'authenticated.rules.site_teams'
        },
        {
          title: $filter('translate')('monitoring.SKILLS'),
          num: dto.skillCount,
          visible: true,
          url: 'authenticated.rules.skills'
        },


        // Schedules

        {
          title: $filter('translate')('employee_schedules.POSTED'),
          num: dto.postedScheduleCount,
          visible: true,
          url: 'authenticated.schedule_builder.create_schedules'
        },
        {
          title: $filter('translate')('employee_schedules.PRODUCTION'),
          num: dto.productionScheduleCount,
          visible: true,
          url: 'authenticated.schedule_builder.create_schedules'
        },
        {
          title: $filter('translate')('employee_schedules.SIMULATION'),
          num: dto.simulationScheduleCount,
          visible: true,
          url: 'authenticated.schedule_builder.create_schedules'
        },

        // Employees & Users

        {
          title: $filter('translate')('nav.EMPLOYEES'),
          num: dto.employeeCount,
          visible: true,
          url: 'authenticated.employees.list'
        },

        {
          title: $filter('translate')('app.USERS'),
          num: dto.userCount,
          visible: true,
          url: 'authenticated.settings.accounts.users'
        },
        {
          title: $filter('translate')('nav.GROUPS'),
          num: dto.groupCount,
          visible: true,
          url: 'authenticated.settings.accounts.groups'
        }

      ];
    };

    // If user is a Employee
    if (!!session.employeeId) {
      $state.go('authenticated.dashboard.calendar');

      // For everybody else
    } else {
      if (session.tenantType === 'Customer') {
        dataService.getOrgCounters()
          .then(function (res) {
            initCounters(res);
            if (authService.hasPermission('Support') && authService.hasPermission('Employee_View')){
              dataService.getServiceProviderCounters()
                .then(function (res2) {
                  home.statItems[1].num = res2.appServerCount;
                  home.statItems[2].num = res2.engineCount;
                });
            }
          });
      } else if (session.tenantType === 'SvcProvider') {
        dataService.getServiceProviderCounters()
          .then(function (res) {
            initCounters(res);
          });
      }

      home.hasPermissions = function (permission) {
        return authService.hasPermission(permission);
      };

    }

  }]);