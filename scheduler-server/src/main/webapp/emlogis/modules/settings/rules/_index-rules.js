angular.module('emlogis.rules',
  [
    'ui.bootstrap', 'ui.router', 'ui.sortable',
    'ui.grid', 'ui.grid.pagination', 'ui.grid.selection',
    'http-auth-interceptor'])
  .config(
  [
    '$stateProvider',
    '$urlRouterProvider',
    '$locationProvider',
    '$httpProvider',
    '$breadcrumbProvider',
    function ($stateProvider, $urlRouterProvider) {

      // Rules module

      $urlRouterProvider.when('/settings/rules', '/settings/rules/general');
      $urlRouterProvider.when('/settings/rules/', '/settings/rules/general');

      //Admin Role RW, ScheduleCreator & ShiftMngr RO
      function permissionsSettings (authService) {
        return authService.isTenantType('Customer') && authService.hasPermissionIn(
          ['SystemConfiguration_View','SystemConfiguration_Mgmt','Demand_View','Demand_Mgmt','Shift_Mgmt']);
      }

      $stateProvider.state('authenticated.rules', {
        url: '/settings/rules',
        abstract: true,
        views: {
          "content@authenticated": {
            templateUrl: "modules/settings/rules/rules.partial.html",
            controller: 'RulesCtrl'
          },
          "breadcrumb@authenticated": {
            templateUrl: "modules/settings/rules/rules_breadcrumb.partial.html",
            controller: 'RulesBreadcrumbCtrl'
          }
        },
        data: {
          permissions: function (authService) {
            return permissionsSettings(authService);
          }
        },
        resolve: {
          timeZones: function (dataService) {
            return dataService.getTimeZones().then( function(response) {
              return response;
            });
          },
          countriesList: function (emlogisConstants) {
            return emlogisConstants.countries;
          }
        }
      })
        .state('authenticated.rules.general', {                                   // General tab
          url: '/general',
          views: {
            "rulesContent@authenticated.rules": {
              templateUrl: "modules/settings/rules/general/rules_general.partial.html",
              controller: 'RulesGeneralCtrl'
            }
            //"rulesBreadcrumb@authenticated.rules": {
            //    //template: "<p>default breadcrumb</p>"
            //    templateUrl: "modules/rules/general/rules_general_breadcrumb.partial.html"
            //    //controller: "RulesGeneralBreadcrumbCtrl"
            //}
          },
          data: {
            ncyBreadcrumbLabel: '{{"rules.GENERAL" | translate}}'
          }
        })
        .state('authenticated.rules.site_teams', {                                // Site & Teams tab
          url: '/site_teams',
          views: {
            "rulesContent@authenticated.rules": {
              templateUrl: "modules/settings/rules/sites_teams/partials/rules_site_teams.partial.html",
              controller: 'RulesSiteTeamsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"rules.SITE_TEAMS" | translate}}'
          }
        })
        .state('authenticated.rules.site_teams.new_site', {                                // Site & Teams tab
          url: '/new',
          views: {
            "rulesContent@authenticated.rules": {
              templateUrl: "modules/settings/rules/sites_teams/partials/rules_site_teams_new.partial.html",
              controller: 'RulesSiteNewCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"rules.site_teams.NEW_SITE" | translate}}'
          }
        })
        .state('authenticated.rules.skills', {                                   // Skills tab
          url: '/skills',
          views: {
            "rulesContent@authenticated.rules": {
              templateUrl: "modules/settings/rules/skills/rules_skills.partial.html",
              controller: 'RulesSkillsCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"rules.SKILLS" | translate}}'
          }
        })
        //.state('authenticated.rules.licenses', {                                // Licenses & Certifications tab
        //  url: '/licenses',
        //  views: {
        //    "rulesContent@authenticated.rules": {
        //      templateUrl: "modules/settings/rules/licenses/rules_licenses.partial.html",
        //      controller: 'RulesLicensesCtrl'
        //    }
        //  },
        //  data: {
        //    ncyBreadcrumbLabel: '{{"rules.LICENSES_CERTIFICATIONS" | translate}}'
        //  }
        //})
        .state('authenticated.rules.holidays',{                        // Holidays tab
          url: '/timeoff_holidays',
          views:{
            "rulesContent@authenticated.rules": {
              templateUrl: "modules/settings/rules/holidays/rules_holidays.partial.html",
              controller: 'RulesHolidaysCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"rules.HOLIDAYS" | translate}}'
          }
        })
      ;

    }
  ])
;