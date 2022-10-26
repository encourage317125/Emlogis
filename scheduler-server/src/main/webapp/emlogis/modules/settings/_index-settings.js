angular.module('emlogis.settings', ['ui.bootstrap', 'ui.router', 'http-auth-interceptor', 'ui.grid', 'ui.grid.moveColumns', 'ui.grid.pagination', 'ui.grid.selection', 'ui.grid.resizeColumns', 'emlogis.commonservices', 'emlogis.commonDirectives', 'frapontillo.bootstrap-switch', 'emlogis.rules'])
  .config(['$stateProvider', '$urlRouterProvider', '$locationProvider', '$httpProvider', '$breadcrumbProvider',
    function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, $breadcrumbProvider) {

      $urlRouterProvider.when('/settings', '/settings/general');
      $urlRouterProvider.when('/settings/', '/settings/general');
      $urlRouterProvider.when('/settings/accounts', '/settings/accounts/groups');
      $urlRouterProvider.when('/settings/accounts/', '/settings/accounts/groups');
      $urlRouterProvider.when('/settings/accounts/groups', '/settings/accounts/groups/list');
      $urlRouterProvider.when('/settings/accounts/groups/', '/settings/accounts/groups/list');
      $urlRouterProvider.when('/settings/accounts/users', '/settings/accounts/users/list');
      $urlRouterProvider.when('/settings/accounts/users/', '/settings/accounts/users/list');
      $urlRouterProvider.when('/settings/accounts/roles', '/settings/accounts/roles/list');
      $urlRouterProvider.when('/settings/accounts/roles/', '/settings/accounts/roles/list');

      function permissionsUserAccount (authService) {
        return authService.hasPermissionIn(
            ['Account_Mgmt','Account_View','Demand_Mgmt','Availability_RequestMgmt','Shift_RequestMgmt','Shift_Mgmt']);
      }
      //Admin Role RW, ScheduleCreator & ShiftMngr RO
      function permissionsSettings (authService) {
        return authService.hasPermissionIn(
          ['SystemConfiguration_View','SystemConfiguration_Mgmt','Demand_View','Demand_Mgmt','Shift_Mgmt']);
      }//Admin Role RW, ScheduleCreator & ShiftMngr RO
      function permissionsRoles (authService) {
        return authService.hasPermissionIn(['Role_Mgmt','Support']);
      }

      $stateProvider.state('authenticated.settings', {
        url: '/settings',
        abstract: true,
        views: {
          'content@authenticated': {
            templateUrl: 'modules/settings/partials/settings.html',
            controller: 'SettingsCtrl'
          },
          'breadcrumb@authenticated': {
            templateUrl: 'modules/settings/partials/settings-breadcrumb.html',
            controller: 'SettingsBreadcrumbCtrl'
          }
        },
        data: {
          ncyBreadcrumbLabel: '{{"nav.SETTINGS" | translate}}'
        }
      })
        .state('authenticated.settings.accounts', {
          url: '/accounts',
          views: {
            'settingsContent@authenticated.settings': {
              templateUrl: 'modules/settings/partials/accounts/settings-accounts.html',
              controller: 'SettingsAccountsCtrl'
            },
            'settingsBreadcrumb@authenticated.settings': {
              templateUrl: 'modules/settings/partials/accounts/settings-accounts-breadcrumb.html',
              controller: 'SettingsAccountsBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"settings.ACCOUNTS" | translate}}',
            permissions: function (authService) {
              return permissionsUserAccount(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.groups', {
          url: '/groups',
          views: {
            'settingsAccountsContent@authenticated.settings.accounts': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups.html',
              controller: 'SettingsAccountsGroupsCtrl'
            },
            'settingsAccountsBreadcrumb@authenticated.settings.accounts': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-breadcrumb.html',
              controller: 'SettingsAccountsGroupsBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"settings.accounts.GROUPS" | translate}}',
            permissions: function (authService) {
              return permissionsUserAccount(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.groups.list', {
          url: '/list',
          views: {
            'settingsAccountsGroupsContent@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-list.html',
              controller: 'SettingsAccountsGroupsListCtrl'
            },
            'settingsAccountsGroupsBreadcrumb@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-list-breadcrumb.html',
              controller: 'SettingsAccountsGroupsListBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.LIST" | translate}}',
            permissions: function (authService) {
              return permissionsUserAccount(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.groups.groupDetails', {
          url: '/:entityId/details',
          views: {
            'settingsAccountsGroupsContent@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-group-details.html',
              controller: 'SettingsAccountsGroupsGroupDetailsCtrl'
            },
            'settingsAccountsGroupsBreadcrumb@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-group-details-breadcrumb.html',
              controller: 'SettingsAccountsGroupsGroupDetailsBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAILS" | translate}}',
            permissions: function (authService) {
              return permissionsUserAccount(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.groups.groupEdit', {
          url: '/:entityId/edit',
          views: {
            'settingsAccountsGroupsContent@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-group-edit.html',
              controller: 'SettingsAccountsGroupsGroupEditCtrl'
            },
            'settingsAccountsGroupsBreadcrumb@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-group-edit-breadcrumb.html',
              controller: 'SettingsAccountsGroupsGroupEditBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.EDIT" | translate}}',
            permissions: function (authService) {
              return authService.hasPermission('Account_Mgmt');
            }
          }
        })
        .state('authenticated.settings.accounts.groups.create', {
          url: '/create',
          views: {
            'settingsAccountsGroupsContent@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-create.html',
              controller: 'SettingsAccountsGroupsCreateCtrl'
            },
            'settingsAccountsGroupsBreadcrumb@authenticated.settings.accounts.groups': {
              templateUrl: 'modules/settings/partials/accounts/groups/settings-accounts-groups-create-breadcrumb.html',
              controller: 'SettingsAccountsGroupsCreateBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.CREATE" | translate}}',
            permissions: function (authService) {
              return authService.hasPermission('Account_Mgmt');
            }
          }
        })
        .state('authenticated.settings.accounts.users', {
          url: '/users',
          views: {
            'settingsAccountsContent@authenticated.settings.accounts': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users.html',
              controller: 'SettingsAccountsUsersCtrl'
            },
            'settingsAccountsBreadcrumb@authenticated.settings.accounts': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users-breadcrumb.html',
              controller: 'SettingsAccountsUsersBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"settings.accounts.USERS" | translate}}',
            permissions: function (authService) {
              return permissionsUserAccount(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.users.list', {
          url: '/list',
          views: {
            'settingsAccountsUsersContent@authenticated.settings.accounts.users': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users-list.html',
              controller: 'SettingsAccountsUsersListCtrl'
            },
            'settingsAccountsUsersBreadcrumb@authenticated.settings.accounts.users': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users-list-breadcrumb.html',
              controller: 'SettingsAccountsUsersListBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.LIST" | translate}}',
            permissions: function (authService) {
              return permissionsUserAccount(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.users.userDetails', {
          url: '/:entityId/details',
          views: {
            'settingsAccountsUsersContent@authenticated.settings.accounts.users': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users-user-details.html',
              controller: 'SettingsAccountsUsersUserDetailsCtrl'
            },
            'settingsAccountsUsersBreadcrumb@authenticated.settings.accounts.users': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users-user-details-breadcrumb.html',
              controller: 'SettingsAccountsUsersUserDetailsBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAILS" | translate}}',
            permissions: function (authService) {
              return permissionsUserAccount(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.users.create', {
          url: '/create',
          views: {
            'settingsAccountsUsersContent@authenticated.settings.accounts.users': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users-create.html',
              controller: 'SettingsAccountsUsersCreateCtrl'
            },
            'settingsAccountsUsersBreadcrumb@authenticated.settings.accounts.users': {
              templateUrl: 'modules/settings/partials/accounts/users/settings-accounts-users-create-breadcrumb.html',
              controller: 'SettingsAccountsUsersCreateBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.CREATE" | translate}}',
            permissions: function (authService) {
              return authService.hasPermission('Account_Mgmt');
            }
          }
        })
        .state('authenticated.settings.accounts.roles', {
          url: '/roles',
          views: {
            'settingsAccountsContent@authenticated.settings.accounts': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles.html',
              controller: 'SettingsAccountsRolesCtrl'
            },
            'settingsAccountsBreadcrumb@authenticated.settings.accounts': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-breadcrumb.html',
              controller: 'SettingsAccountsRolesBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"settings.accounts.ROLES" | translate}}',
            permissions: function (authService) {
              return permissionsRoles(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.roles.list', {
          url: '/list',
          views: {
            'settingsAccountsRolesContent@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-list.html',
              controller: 'SettingsAccountsRolesListCtrl'
            },
            'settingsAccountsRolesBreadcrumb@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-list-breadcrumb.html',
              controller: 'SettingsAccountsRolesListBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.LIST" | translate}}',
            permissions: function (authService) {
              return permissionsRoles(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.roles.roleDetails', {
          url: '/:entityId/details',
          views: {
            'settingsAccountsRolesContent@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-role-details.html',
              controller: 'SettingsAccountsRolesRoleDetailsCtrl'
            },
            'settingsAccountsRolesBreadcrumb@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-role-details-breadcrumb.html',
              controller: 'SettingsAccountsRolesRoleDetailsBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.DETAILS" | translate}}',
            permissions: function (authService) {
              return permissionsRoles(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.roles.roleEdit', {
          url: '/:entityId/edit',
          views: {
            'settingsAccountsRolesContent@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-role-edit.html',
              controller: 'SettingsAccountsRolesRoleEditCtrl'
            },
            'settingsAccountsRolesBreadcrumb@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-role-edit-breadcrumb.html',
              controller: 'SettingsAccountsRolesRoleEditBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.EDIT" | translate}}',
            permissions: function (authService) {
              return permissionsRoles(authService);
            }
          }
        })
        .state('authenticated.settings.accounts.roles.create', {
          url: '/create',
          views: {
            'settingsAccountsRolesContent@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-create.html',
              controller: 'SettingsAccountsRolesCreateCtrl'
            },
            'settingsAccountsRolesBreadcrumb@authenticated.settings.accounts.roles': {
              templateUrl: 'modules/settings/partials/accounts/roles/settings-accounts-roles-create-breadcrumb.html',
              controller: 'SettingsAccountsRolesCreateBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.CREATE" | translate}}',
            permissions: function (authService) {
              return permissionsRoles(authService);
            }
          }
        })
        //.state('authenticated.settings.passwordPolicy', {
        //  url: '/password-policy',
        //  views: {
        //    'settingsContent@authenticated.settings': {
        //      templateUrl: 'modules/settings/partials/password-policy/settings-password-policy.html',
        //      controller: 'SettingsPasswordPolicyCtrl'
        //    },
        //    'settingsBreadcrumb@authenticated.settings': {
        //      templateUrl: 'modules/settings/partials/password-policy/settings-password-policy-breadcrumb.html',
        //      controller: 'SettingsPasswordPolicyBreadcrumbCtrl'
        //    }
        //  },
        //  data: {
        //    ncyBreadcrumbLabel: '{{"settings.PASSWORD_POLICY" | translate}}',
        //    permissions: function (authService) {
        //      return permissionsSettings(authService);
        //    }
        //  }
        //})
        ;
    }
  ])
  .run(['$rootScope', '$http', function ($rootScope, $http) {
    $rootScope.consts = {
      entityTypes: {
        group: 'group',
        user: 'user',
        role: 'role',
        accessControl: 'access control',
        permission: 'permission'
      }
    };
  }]);
