(function () {
  "use strict";

  var settings = angular.module('emlogis.settings');

  settings.controller('SettingsAccountsGroupsAclModalCtrl',
    [
      '$rootScope',
      '$scope',
      '$timeout',
      '$modalInstance',
      'uiGridConstants',
      'applicationContext',
      'dataService',
      'SettingsAccountsService',
      'group',
      'acl',
      function ($rootScope,$scope, $timeout, $modalInstance, uiGridConstants, applicationContext, dataService,
                SettingsAccountsService, group, acl) {

        //--------------------------------------------------------------------
        // Defaults for Employees Modal
        //--------------------------------------------------------------------

        var te = this;

        te.group = group;
        te.acl = acl;

        // checkbox action for all sites/teams
        te.updateAllSitesStatus = function() {

          if (te.acl.allSitesAccessType === 'RW') {
            _.each(te.acl.result, function(site) {
              site.accessType = 'RW';
              site.teamsDto.allTeamsAccessType = 'Void';

              _.each(site.teamsDto.teamDtos, function(team) {
                team.accessType = 'Void';
              });

            });
          }
          //else {
          //  _.each(te.acl.result, function(site) {
          //    site.accessType = 'Void';
          //    site.teamsDto.allTeamsAccessType = 'Void';
          //
          //    _.each(site.teamsDto.teamDtos, function(team) {
          //      team.accessType = 'Void';
          //    });
          //
          //  });
          //}

        };

        // checkbox action for site all teams
        te.updateSiteAllTeamsStatus = function(site){

          if (site.teamsDto.allTeamsAccessType === 'RW') {
            site.accessType = 'RW';
            _.each(site.teamsDto.teamDtos, function(team) {
              team.accessType = 'Void';
            });
          }
          else {
            site.accessType = 'Void';
            //_.each(site.teamsDto.teamDtos, function(team) {
            //  team.accessType = 'Void';
            //});
          }

        };

        // checkbox of individual team
        te.updateSiteTeamStatus = function(site,team) {
          var checkedTeam = _.findWhere(site.teamsDto.teamDtos, {'accessType': 'RW'});
          if (checkedTeam) {
            site.accessType = 'RW';
          }
          else {
            site.accessType = 'Void';
          }


          // update site-all-teams checkbox
          //site.teamsDto.allTeamsAccessType = te.getSiteAllTeamsStatus(site);

          // update all sites-teams check
          //te.acl.allSitesAccessType = te.getAllSitesStatus();

        };

        te.getAllSitesStatus = function() {
          var uncheckedSite = _.find(te.acl.result, function(site) {
            return site.teamsDto.allTeamsAccessType === 'Void';
          });

          if (uncheckedSite) {
            return 'Void';
          }
          else {
            return 'RW';
          }
        };

        te.getSiteAllTeamsStatus = function(site) {
          var uncheckedTeam = _.findWhere(site.teamsDto.teamDtos, {'accessType': 'Void'});
          if (uncheckedTeam) {
            return 'Void';
          }
          else {
            return 'RW';
          }
        };

        /**
         * Update Acl of groups: Settings->Accounts->Groups->Acl
         */
        te.updateGroupAcl = function() {

          SettingsAccountsService.operateOnRelatedEntity(null, $rootScope.consts.entityTypes.group, te.group.id, $rootScope.consts.entityTypes.accessControl, te.acl).then(function (response) {
            $modalInstance.close();
          }, function (err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
        };

        //--------------------------------------------------------------------
        // Modal methods: ACCOUNTS-GROUPS-ACL-MODAL.JS
        //--------------------------------------------------------------------

        te.closeModal = function () {
          $modalInstance.dismiss('cancel');
        };

        //
        // If user navigates away from the page,
        // dismiss the modal

        $scope.$on('$stateChangeStart', function(){
            $modalInstance.dismiss('cancel');
          }
        );

      }
    ]);
})();