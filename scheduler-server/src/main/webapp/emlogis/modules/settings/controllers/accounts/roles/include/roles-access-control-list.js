angular.module('emlogis.settings').controller('RolesAccessControlListCtrl', ['$scope', '$state', '$stateParams', '$http', '$filter', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService', 'applicationContext',
  function ($scope, $state, $stateParams, $http, $filter, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService, applicationContext) {

    $scope.isInEdit = ($scope.editMode === 'Edit');
    $scope.allSites = {
      readAccess: false,
      writeAccess: false
    };
    $scope.sites = [];
    $scope.parsedSites = [];

    $scope.convertAccessTypeToAccess = function (accessType, dstElement) {
      if (accessType === 'RO') {
        dstElement.readAccess = true;
        dstElement.writeAccess = false;
      } else if (accessType === 'RW') {
        dstElement.readAccess = true;
        dstElement.writeAccess = true;
      } else if (accessType === 'Void') {
        dstElement.readAccess = false;
        dstElement.writeAccess = false;
      } else {
        // do other operations
      }
    };

    $scope.convertAccessToAccessType = function (srcElement, dstElement) {
      if (srcElement.readAccess && srcElement.writeAccess) {
        dstElement.accessType = 'RW';
      } else if (srcElement.readAccess && !srcElement.writeAccess) {
        dstElement.accessType = 'RO';
      } else if (!srcElement.readAccess && !srcElement.writeAccess) {
        dstElement.accessType = 'Void';
      } else {
        //do other operations
      }
    };

    $scope.parseEntityListForCheck = function () {
      $scope.parsedSites = [];
      angular.forEach($scope.sites, function (site) {
        var siteElement = {};
        siteElement.name = site.name;
        $scope.convertAccessTypeToAccess(site.accessType, siteElement);
        siteElement.originalEntity = site;
        siteElement.expanded = false;
        siteElement.allTeams = {};
        $scope.convertAccessTypeToAccess(site.teamsDto.allTeamsAccessType, siteElement.allTeams);
        siteElement.teams = [];
        angular.forEach(site.teamsDto.teamDtos, function (team) {
          var teamElement = {};
          teamElement.name = team.name;
          $scope.convertAccessTypeToAccess(team.accessType, teamElement);
          teamElement.originalEntity = team;
          siteElement.teams.push(teamElement);
        });
        $scope.parsedSites.push(siteElement);
      });
    };

    $scope.allSitesReadAccessChanged = function () {
      if (!$scope.allSites.readAccess) {
        if ($scope.allSites.writeAccess) {
          $scope.allSites.writeAccess = false;
          $scope.allSitesWriteAccessChanged();
        }
      }
      angular.forEach($scope.parsedSites, function (parsedSite) {
        parsedSite.readAccess = $scope.allSites.readAccess;
        $scope.convertAccessToAccessType(parsedSite, parsedSite.originalEntity);
        if (!parsedSite.readAccess) {
          parsedSite.allTeams.readAccess = false;
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'Void';
          angular.forEach(parsedSite.teams, function (parsedTeam) {
            parsedTeam.readAccess = parsedSite.readAccess;
            $scope.convertAccessToAccessType(parsedTeam, parsedTeam.originalEntity);
          });
        }
      });
    };

    $scope.allSitesWriteAccessChanged = function () {
      if ($scope.allSites.writeAccess) {
        if (!$scope.allSites.readAccess) {
          $scope.allSites.readAccess = true;
          $scope.allSitesReadAccessChanged();
        }
      }
      angular.forEach($scope.parsedSites, function (parsedSite) {
        parsedSite.writeAccess = $scope.allSites.writeAccess;
        $scope.convertAccessToAccessType(parsedSite, parsedSite.originalEntity);
      });
    };

    $scope.siteReadAccessChanged = function (parsedSite) {
      var findElement = _.find($scope.parsedSites, function (siteIterator) {
        return siteIterator.readAccess !== parsedSite.readAccess;
      });
      if (typeof findElement === 'undefined') {
        $scope.allSites.readAccess = parsedSite.readAccess;
      } else {
        $scope.allSites.readAccess = false;
      }

      if (!parsedSite.readAccess) {
        if (parsedSite.writeAccess) {
          parsedSite.writeAccess = false;
          $scope.siteWriteAccessChanged(parsedSite);
        }
      }
      $scope.convertAccessToAccessType(parsedSite, parsedSite.originalEntity);
      if (!parsedSite.readAccess) {
        parsedSite.allTeams.readAccess = false;
        parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'Void';
        angular.forEach(parsedSite.teams, function (parsedTeam) {
          parsedTeam.readAccess = parsedSite.readAccess;
          $scope.convertAccessToAccessType(parsedTeam, parsedTeam.originalEntity);
        });
      }
    };

    $scope.siteWriteAccessChanged = function (parsedSite) {
      var findElement = _.find($scope.parsedSites, function (siteIterator) {
        return siteIterator.writeAccess !== parsedSite.writeAccess;
      });
      if (typeof findElement === 'undefined') {
        $scope.allSites.writeAccess = parsedSite.writeAccess;
      } else {
        $scope.allSites.writeAccess = false;
      }

      if (parsedSite.writeAccess) {
        if (!parsedSite.readAccess) {
          parsedSite.readAccess = true;
          $scope.siteReadAccessChanged(parsedSite);
        }
      }
      $scope.convertAccessToAccessType(parsedSite, parsedSite.originalEntity);
    };

    $scope.allTeamsReadAccessChanged = function (parsedSite) {
      if (parsedSite.allTeams.readAccess) {
        if (!parsedSite.readAccess) {
          parsedSite.readAccess = true;
          $scope.siteReadAccessChanged(parsedSite);
        }
        if (parsedSite.allTeams.writeAccess) {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RW';
        } else {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RO';
        }
      } else {
        if (parsedSite.allTeams.writeAccess) {
          parsedSite.allTeams.writeAccess = false;
          $scope.allTeamsWriteAccessChanged(parsedSite);
        }
        parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'Void';
      }
      angular.forEach(parsedSite.teams, function (parsedTeam) {
        parsedTeam.readAccess = parsedSite.allTeams.readAccess;
        $scope.convertAccessToAccessType(parsedTeam, parsedTeam.originalEntity);
      });
    };

    $scope.allTeamsWriteAccessChanged = function (parsedSite) {
      if (parsedSite.allTeams.writeAccess) {
        if (!parsedSite.allTeams.readAccess) {
          parsedSite.allTeams.readAccess = true;
          $scope.allTeamsReadAccessChanged(parsedSite);
        }
        parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RW';
      } else {
        if (parsedSite.allTeams.readAccess) {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RO';
        } else {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'Void';
        }
      }
      angular.forEach(parsedSite.teams, function (parsedTeam) {
        parsedTeam.writeAccess = parsedSite.allTeams.writeAccess;
        $scope.convertAccessToAccessType(parsedTeam, parsedTeam.originalEntity);
      });
    };

    $scope.teamReadAccessChanged = function (parsedSite, parsedTeam) {
      if (!parsedTeam.readAccess) {
        if (parsedTeam.writeAccess) {
          parsedTeam.writeAccess = false;
          $scope.teamWriteAccessChanged(parsedSite, parsedTeam);
        }
      }
      $scope.convertAccessToAccessType(parsedTeam, parsedTeam.originalEntity);

      var findElement = _.find(parsedSite.teams, function (teamIterator) {
        return teamIterator.readAccess !== parsedTeam.readAccess;
      });
      if (typeof findElement === 'undefined') {
        parsedSite.allTeams.readAccess = parsedTeam.readAccess;
      } else {
        parsedSite.allTeams.readAccess = false;
      }
      if (parsedSite.allTeams.readAccess) {
        if (parsedSite.allTeams.writeAccess) {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RW';
        } else {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RO';
        }
      } else {
        parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'Void';
      }

      if (parsedTeam.readAccess) {
        if (!parsedSite.readAccess) {
          parsedSite.readAccess = true;
          $scope.siteReadAccessChanged(parsedSite);
        }
      }
    };

    $scope.teamWriteAccessChanged = function (parsedSite, parsedTeam) {
      if (parsedTeam.writeAccess) {
        if (!parsedTeam.readAccess) {
          parsedTeam.readAccess = true;
          $scope.teamReadAccessChanged(parsedSite, parsedTeam);
        }
      }
      $scope.convertAccessToAccessType(parsedTeam, parsedTeam.originalEntity);

      var findElement = _.find(parsedSite.teams, function (teamIterator) {
        return teamIterator.writeAccess !== parsedTeam.writeAccess;
      });
      if (typeof findElement === 'undefined') {
        parsedSite.allTeams.writeAccess = parsedTeam.writeAccess;
      } else {
        parsedSite.allTeams.writeAccess = false;
      }
      if (parsedSite.allTeams.writeAccess) {
        parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RW';
      } else {
        if (parsedSite.allTeams.readAccess) {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'RO';
        } else {
          parsedSite.originalEntity.teamsDto.allTeamsAccessType = 'Void';
        }
      }
    };

    $scope.updateAccessControl = function () {
      var accessType = null;
      if ($scope.allSites.readAccess && $scope.allSites.writeAccess) {
        accessType = 'RW';
      } else if ($scope.allSites.readAccess && !$scope.allSites.writeAccess) {
        accessType = 'RO';
      } else if (!$scope.allSites.readAccess && !$scope.allSites.writeAccess) {
        accessType = 'Void';
      }
      var payLoad = {
        allSitesAccessType: accessType,
        result: $scope.sites
      };
      SettingsAccountsService.operateOnRelatedEntity('set', $scope.entityType, $scope.entityId, $scope.relatedEntity.entityDetails.entityType, payLoad).then(function (response) {
        applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
        $scope.initializeList();
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    function comparator(firstElement, secondElement) {
      if (firstElement.name.toLowerCase() < secondElement.name.toLowerCase())
        return -1;
      if (firstElement.name.toLowerCase() > secondElement.name.toLowerCase())
        return 1;
      return 0;
    }

    $scope.initializeList = function () {
      $scope.relatedEntity.getEntityList().then(function (response) {
        if (response.data.allSitesAccessType === 'Void') {
          $scope.allSites.readAccess = false;
          $scope.allSites.writeAccess = false;
        } else if (response.data.allSitesAccessType === 'RO') {
          $scope.allSites.readAccess = true;
          $scope.allSites.writeAccess = false;
        } else if (response.data.allSitesAccessType === 'RW') {
          $scope.allSites.readAccess = true;
          $scope.allSites.writeAccess = true;
        } else {
          // do other operations
        }
        $scope.sites = response.data.result;
        $scope.sites.sort(comparator);
        angular.forEach($scope.sites, function (site) {
          site.teamsDto.teamDtos.sort(comparator);
        });
        $scope.parseEntityListForCheck();
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    $scope.initializeList();
  }
]);
