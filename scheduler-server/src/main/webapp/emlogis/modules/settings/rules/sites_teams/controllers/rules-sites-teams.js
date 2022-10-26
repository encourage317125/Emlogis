(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesSiteTeamsCtrl', ['$scope', '$state', '$filter',
                                           'applicationContext', 'dialogs', 'appFunc',
                                           'dataService', 'rulesSitesService', 'rulesTeamsService',
    function ($scope, $state, $filter,
              applicationContext, dialogs, appFunc,
              dataService, rulesSitesService, rulesTeamsService) {

    //console.log('+++ inside Rules Site / Teams controller');


    //--------------------------------------------------------------------
    // Defaults for Sites & Teams tab
    //--------------------------------------------------------------------

    var i, allSitesInit;
    $scope.page.editing = false;
    $scope.page.submitted = false;
    $scope.updatesCounter = 0;
    $scope.isEditWellCollapsed = true;


    // Site
    $scope.allSites = [];
    $scope.siteDataIsLoading = true;
    $scope.sitesTeamsTree = null;
    $scope.selectedSite = null;

    // Teams
    $scope.selectedSiteChildren = {
      site: {}
    };



    //--------------------------------------------------------------------
    // Helpers
    //--------------------------------------------------------------------

    var getMaxOfArray = function(numArray) {
      return Math.max.apply(null, numArray);
    };


    var diffNotInArray = function(bigArray, smArray){
      var smArrayIds = {};
      _.forEach(smArray, function(obj) {
        smArrayIds[obj.id] = obj;
      });

      return bigArray.filter(function(obj){
        return !(obj.id in smArrayIds);
      });
    };



    //--------------------------------------------------------------------
    // Site related methods
    //--------------------------------------------------------------------


    //
    // Check if Site can be deleted by user
    // TEMP TODO change when API property is implemented

    $scope.isSiteDeletable = function(){
      return _.find($scope.selectedSiteChildren.site.allTeams, { 'isDeleted': false }) ? true : false;
    };



    //
    // GET the list of all Sites

    $scope.loadAllSites = function() {

      rulesSitesService.getAllSites()
        .then(function (res) {

          // Save response
          allSitesInit = res.data;
          $scope.allSites = angular.copy(allSitesInit);

          // and load SitesTeamsTree
          loadSitesTeamsTree();

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);

        })
        .finally(function() {

          //
          // Remove Sites that are deleted
          //_.remove($scope.allSites, { 'isDeleted': true });


          //
          // After all Sites were loaded
          // display the most recently updated Site

          var latestUpdate,
              datesSitesWereModified = [];                                         // create an array of dates

          for (i = 0; i < $scope.allSites.length; i++) {                         // push dates when Sites were updated
            datesSitesWereModified.push( $scope.allSites[i].updated.getTime() );
          }

          latestUpdate = getMaxOfArray(datesSitesWereModified);                  // find the most recent date

          for (i = 0; i < $scope.allSites.length; i++) {
            if ( $scope.allSites[i].updated.getTime() === latestUpdate ) {       // assign selected Site based on update
              $scope.selectedSite = $scope.allSites[i];
              break;
            }
          }
        })
      ;
    };



    //
    // GET all details for a Site

    $scope.loadSiteDetails = function(siteId) {

      rulesSitesService.getSiteDetails(siteId)
        .then( function (res) {
          console.log('+++ getSiteDetails res', res.data);
          $scope.selectedSiteDetails = res.data;
          $scope.selectedSiteDetails.twoWeeksOvertimeStartDate = appFunc.convertToBrowserTimezone(res.data.twoWeeksOvertimeStartDate, res.data.timeZone);

          // Hide '-1' in input fields
          $scope.selectedSiteDetails.overtimeDto.biweeklyOvertimeMins = $filter('hideMinus')($scope.selectedSiteDetails.overtimeDto.biweeklyOvertimeMins);
          $scope.selectedSiteDetails.overtimeDto.weeklyOvertimeMins = $filter('hideMinus')($scope.selectedSiteDetails.overtimeDto.weeklyOvertimeMins);
          $scope.selectedSiteDetails.overtimeDto.dailyOvertimeMins = $filter('hideMinus')($scope.selectedSiteDetails.overtimeDto.dailyOvertimeMins);

          // Convert to hours
          $scope.selectedSiteDetails.overtimeDto.biweeklyOvertimeMins = $filter('minsToHoursFloat')($scope.selectedSiteDetails.overtimeDto.biweeklyOvertimeMins);
          $scope.selectedSiteDetails.overtimeDto.weeklyOvertimeMins = $filter('minsToHoursFloat')($scope.selectedSiteDetails.overtimeDto.weeklyOvertimeMins);
          $scope.selectedSiteDetails.overtimeDto.dailyOvertimeMins = $filter('minsToHoursFloat')($scope.selectedSiteDetails.overtimeDto.dailyOvertimeMins);

          displaySiteDetails();

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        })
        .finally(function(){
          $scope.siteDataIsLoading = false;                                      // hide loading spinner
        })
      ;
    };



    //
    // GET all details for a Site

    var displaySiteDetails = function() {
      $scope.selectedSiteDetailsToDisplay = angular.copy($scope.selectedSiteDetails);

      // Resetting the page state
      $scope.page.submitted = false;
      $scope.page.editing = false;
    };



    // Load Sites Teams tree

    var loadSitesTeamsTree = function() {

      rulesSitesService.getSitesTeamsTree()
        .then(function (res) {
          $scope.sitesTeamsTree = res.data;

          $scope.sitesTeamsTree = $filter('orderBy')($scope.sitesTeamsTree, 'name'); // sort Sites in ABC order
          angular.forEach($scope.sitesTeamsTree, function(site) {                    // sort Teams in ABC order
            site.children = $filter('orderBy')(site.children, 'name');
          });

          //console.log('+++ Sites & Teams tree', $scope.sitesTeamsTree);              // DEV mode

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);

        })
      ;
    };



    $scope.loadAllSites();



    //--------------------------------------------------------------------
    // CRUD
    //--------------------------------------------------------------------

    //
    // Add new Site
    // and move user to /new state

    $scope.addNewSiteInit = function () {
      console.log('+++ ADD NEW SITE clicked...');
      $state.go('authenticated.rules.site_teams.new_site');
      $scope.selectedSiteDetailsToDisplay = {};
    };



    //
    // Save button clicked

    $scope.save = function(){
      console.log('+++ SAVE clicked');

      // First,
      // check each Team of the selected Site
      // to find all modification that were made for Team Details, Skills and Employees.

      // Check,
      // if a New Team was added to a Site

      if ($scope.selectedSiteChildren.site.allTeamsInit.length < $scope.selectedSiteChildren.site.allTeams.length){

        // For a new Team check if Team name was filled.
        // If not - display notification and cancel Saving process
        if ( !$scope.selectedSiteChildren.site.allTeams[0].name ) {
          applicationContext.setNotificationMsgWithValues('Please enter a name for a New Team!', 'danger', true);
          $scope.page.submitted = true;
          return;
        }

        // If New Team has a name filled, proceed
        $scope.updatesCounter++;
        addNewTeam();
      }


      // Next,
      // check the selected Site:
      // if Site's Details were modified - update Site details

      if ( !angular.equals($scope.selectedSiteDetailsToDisplay, $scope.selectedSiteDetails) ) {

        if ( rulesSitesService.getSiteForm().$valid ) {
          $scope.updatesCounter++;
          updateSite();

        } else {
          $scope.page.submitted = true;
          $scope.isEditWellCollapsed = false;
          applicationContext.setNotificationMsgWithValues('Please enter all required details for selected Site!', 'danger', true);
          return;
        }
      }


      // Now,
      // Compare initial and new states of every Team,
      // in order to:

      angular.forEach($scope.selectedSiteChildren.site.allTeamsInit, function(initTeam) {
        var newTeam = _.find($scope.selectedSiteChildren.site.allTeams, { 'id': initTeam.id });


        // Check,
        // if Team Details were modified

        var initTeamDetails = {                                                           // initial Team details dto
          name:         initTeam.name,
          abbreviation: initTeam.abbreviation,
          description:  initTeam.description,
          active:       initTeam.active
        };

        var newTeamDetails = {                                                            // current Team details dto
          name:         newTeam.name,
          abbreviation: newTeam.abbreviation,
          description:  newTeam.description,
          active:       newTeam.active
        };

        if ( !angular.equals(initTeamDetails, newTeamDetails) ) {                         // if initial and current DTOs differ

          if ( newTeamDetails.name && newTeamDetails.name.length <= 50 &&
               newTeamDetails.abbreviation && newTeamDetails.abbreviation.length <= 5 ) {

            $scope.updatesCounter++;
            console.log('newTeamDetails', newTeamDetails);
            rulesTeamsService.putTeamDetails(newTeam.id, newTeamDetails).then( function(){   // update Team details
                $scope.updatesCounter--;
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              })
            ;

          } else {
            applicationContext.setNotificationMsgWithValues('Please make sure you entered all Team details properly!', 'danger', true);
            $scope.page.editing = false;
            $scope.page.submitted = false;
            return;
          }
        }


        // Check,
        // if list of Skills was modified.

        if (newTeam.associatedSkills){
          if (initTeam.initSkills.length > newTeam.associatedSkills.length){                      // If some Skills were removed:
            var skillsToRemove = diffNotInArray(initTeam.initSkills, newTeam.associatedSkills);   // find all deleted Skills,
            //console.log('~~~ skillsToRemove', skillsToRemove);

            angular.forEach(skillsToRemove, function(skill) {                                     // for every added Skill
              $scope.updatesCounter++;                                                            // increase counter for every API call,
              rulesTeamsService.removeSkillFromTeam(newTeam.id, skill.id).then(function(){        // remove this Skill from the Team,
                $scope.updatesCounter--;                                                          // decrease counter for every API call resolved.
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              });
            });

          } else if (initTeam.initSkills.length < newTeam.associatedSkills.length) {              // If some Skills were added:
            var skillsToAdd = diffNotInArray(newTeam.associatedSkills, initTeam.initSkills);      // find all added Skills,
            //console.log('~~~ skillsToAdd', skillsToAdd);

            angular.forEach(skillsToAdd, function(skill) {                                        // for every added Skill
              $scope.updatesCounter++;                                                            // increase counter for every API call,
              rulesTeamsService.addTeamSkill(newTeam.id, skill.id).then(function(){               // attach this Skill to the Team,
                $scope.updatesCounter--;                                                          // decrease counter for every API call resolved.
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              });
            });

          } else if (initTeam.initSkills.length === newTeam.associatedSkills.length){             // If number of Skills is the same
            angular.forEach(newTeam.associatedSkills, function(skill) {
              var skillIsAssociated = _.find(initTeam.initSkills, { 'id': skill.id });            // Check if any Skills were removed
              if (!skillIsAssociated) {
                $scope.updatesCounter++;                                                          // increase counter for every API call,
                rulesTeamsService.addTeamSkill(newTeam.id, skill.id).then(function(){             // remove this Skill from the Team,
                  $scope.updatesCounter--;                                                        // decrease counter for every API call resolved.
                }, function (err) {
                  applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
                });
              }
            });

            angular.forEach(initTeam.initSkills, function(skill) {                                // Check if any Skills were removed
              var skillIsKept = _.find(newTeam.associatedSkills, { 'id': skill.id });
              if (!skillIsKept) {
                $scope.updatesCounter++;                                                          // increase counter for every API call,
                rulesTeamsService.removeSkillFromTeam(newTeam.id, skill.id).then(function(){      // remove this Skill from the Team,
                  $scope.updatesCounter--;                                                        // decrease counter for every API call resolved.
                }, function (err) {
                  applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
                });
              }
            });
          }
        }

      });

      // Finally,
      // wait for when all API calls are resolved
      // and after that - reload SitesTeamsTree

      var removeThisWatcher = $scope.$watch("updatesCounter", function(newVal, oldVal) {
        if (newVal === 0 && oldVal === 1) {
          $scope.loadAllSites();
          applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
          $scope.page.editing = false;
          $scope.page.submitted = false;
          removeThisWatcher();                                           // remove this $watch
        }
      });
    };



    //
    // Add a New Team to a Site  TODO: in progress

    var addNewTeam = function(){

      var newTeamDto = {
        "id":               null,
        "siteId":           $scope.selectedSiteChildren.site.id,
        "updateDto": {
          "name":           $scope.selectedSiteChildren.site.allTeams[0].name,                  // required
          "abbreviation":   $scope.selectedSiteChildren.site.allTeams[0].abbreviation,          // required
          "description":    $scope.selectedSiteChildren.site.allTeams[0].description || null,
          "active":         $scope.selectedSiteChildren.site.allTeams[0].active,
          "startDate":      0,
          "endDate":        0
        }
      };

      console.log('newTeamDto', newTeamDto);

      rulesTeamsService.addNewTeam(newTeamDto)
        .then( function(res){
          $scope.updatesCounter--;
          applicationContext.setNotificationMsgWithValues('New Team was successfully added!', 'success', true);

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        })
      ;
    };



    //
    // Update the selected Site

    var updateSite = function () {
      console.log('+++ UPDATING the Site...');

      // Prepare to save
      //var date = new Date($scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate).getTime(); // TODO: Time Zone?
      var saveSiteId = $scope.selectedSiteDetailsToDisplay.id;

      var date;
      if ($scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate) {
        date = appFunc.getDateWithTimezone(
          $scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate.getFullYear(),
          $scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate.getMonth(),
          $scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate.getDate(),
          $scope.selectedSiteDetailsToDisplay.timeZone
        ).getTime();
      } else {
        date = null;
      }


      var updatedSite = {
        "properties": {},
        "name":                       $scope.selectedSiteDetailsToDisplay.name,
        "description":                $scope.selectedSiteDetailsToDisplay.description,
        "weekendDefinition":          $scope.selectedSiteDetailsToDisplay.weekendDefinition,
        "firstDayOfWeek":             $scope.selectedSiteDetailsToDisplay.firstDayOfWeek,
        "isNotificationEnabled":      $scope.selectedSiteDetailsToDisplay.isNotificationEnabled,
        "timeZone":                   $scope.selectedSiteDetailsToDisplay.timeZone,
        "abbreviation":               $scope.selectedSiteDetailsToDisplay.abbreviation,
        "address":                    $scope.selectedSiteDetailsToDisplay.address,
        "address2":                   $scope.selectedSiteDetailsToDisplay.address2,
        "city":                       $scope.selectedSiteDetailsToDisplay.city,
        "state":                      $scope.selectedSiteDetailsToDisplay.state,
        "country":                    $scope.selectedSiteDetailsToDisplay.country,
        "zip":                        $scope.selectedSiteDetailsToDisplay.zip,
        "shiftIncrements":            $scope.selectedSiteDetailsToDisplay.shiftIncrements,
        "shiftOverlaps":              $scope.selectedSiteDetailsToDisplay.shiftOverlaps,
        "maxConsecutiveShifts":       $scope.selectedSiteDetailsToDisplay.maxConsecutiveShifts,
        "timeOffBetweenShifts":       $scope.selectedSiteDetailsToDisplay.timeOffBetweenShifts,
        "enableWIPFragments":         $scope.selectedSiteDetailsToDisplay.enableWIPFragments,
        "twoWeeksOvertimeStartDate":  date,
        "overtimeDto": {
          "dailyOvertimeMins":        $filter('hoursToMins')($scope.selectedSiteDetailsToDisplay.overtimeDto.dailyOvertimeMins) || -1,
          "weeklyOvertimeMins":       $filter('hoursToMins')($scope.selectedSiteDetailsToDisplay.overtimeDto.weeklyOvertimeMins) || -1,
          "biweeklyOvertimeMins":     $filter('hoursToMins')($scope.selectedSiteDetailsToDisplay.overtimeDto.biweeklyOvertimeMins) || -1
        }
      };

      return rulesSitesService.updateSite($scope.selectedSiteDetailsToDisplay.id, updatedSite)
        .then(function (res) {
          //console.log('updateSite res', res);
          $scope.updatesCounter--;

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        })
        .finally(function() {
          //$scope.page.submitted = false;
          $scope.loadSiteDetails(saveSiteId);

          // And reload the Sites list
          // in case Site's name was modified
          $scope.loadAllSites();
        })
      ;
    };



    // Delete Site

    $scope.deleteSite = function(site){

      // Confirm deletion
      var question = $filter('translate')("rules.site_teams.DELETE_SITE") + site.name + '?';
      var dlg = dialogs.confirm('app.PLEASE_CONFIRM', question);                // Show modal window
      dlg.result.then(function (btn) {                                          // If user confirms, proceed
        return dataService.deleteSite(site.id)
          .then(function(res){
            //Reload all Sites
            $scope.loadAllSites();
            applicationContext.setNotificationMsgWithValues('rules.site_teams.SITE_DELETED', 'success', true);

          }, function (err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
      });
    };



    //--------------------------------------------------------------------
    // Alerts related methods
    //--------------------------------------------------------------------


    //
    // Alerts actions

    $scope.addAlert = function (alertsArrey) {
      alertsArrey.push({type: 'warning', msg: 'Another warning alert!'});
    };

    $scope.closeAlert = function (alertsArrey, index) {
      alertsArrey.splice(index, 1);
    };



    //--------------------------------------------------------------------
    // Sidebar related methods
    //--------------------------------------------------------------------


    //
    // Toggle Sidebar
    $scope.toggleSidebar = function(){
      $scope.sidebarVisible = !$scope.sidebarVisible;
    };


    //
    // Change Selected Site with its ID
    $scope.setSelectedSite = function(siteId) {
      angular.forEach($scope.allSites, function(site) {
        if (site.id === siteId) $scope.selectedSite = site;
      });
    };


    //
    // Choose team in sidebar
    $scope.displayTeam = function(siteId, teamId) {
      if ( $scope.selectedSiteChildren.site.id != siteId ) {
        $scope.setSelectedSite(siteId);
        openTeam(teamId); // TODO promise
      } else {
        openTeam(teamId);
      }
    };

    var openTeam = function(teamId){
      angular.forEach($scope.selectedSiteChildren.site.allTeams, function(team) {
        if (team.id === teamId && !team.isSelected) {
          console.log('team', team);

          team.isPanelCollapsed = false;          // open panel
          team.panelOpenedOnce = true;            // team was opened at least once
          team.isSelected = true;                 //

          // scroll to team panel
          // add class to panel
          // load team details

        } else if (team.id !== teamId){
          team.isSelected = false;
          team.isPanelCollapsed = true;
        }
      });
    };



    //--------------------------------------------------------------------
    // Setup Working for this page
    //--------------------------------------------------------------------

    var working = applicationContext.getWorking();

    working.entityName = 'rules.SITE_TEAMS';
    working.option = $scope.page;
    working.saveFunc = $scope.save;
    //working.restoreFunc = restoreOriginalSchedule;

    console.log('working', working);


  }]);
})();