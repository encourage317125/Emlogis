(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesGeneralCtrl',
    ['$scope', '$http', '$q', 'rulesGeneralFactory', 'dataService', 'applicationContext',
    function ($scope, $http, $q, rulesGeneralFactory, dataService, applicationContext) {

      //--------------------------------------------------------------------
      // Defaults for General tab
      //--------------------------------------------------------------------

      $scope.page.editing = false;
      $scope.page.submitted = false;
      $scope.updatesCounter = 0;

      var orgInit = {},
          panelsInit = {};

      $scope.org = {};

      $scope.panels = {
        configuration: {                                           // Site Configuration panel
          title: "rules.general.SITE_CONFIGURATION",
          settings: [
            {
              function: "rules.general.BREAK_SHIFT_MIDNIGHT",
              description: "rules.general.BREAK_SHIFT_MIDNIGHT_DESC"
            },
            {
              function: "rules.general.BREAK_SHIFT_HOURS",
              description: "rules.general.BREAK_SHIFT_HOURS_DESC"
            },
            {
              function: "rules.general.REDUCE_MIN_HOURS_PTO",
              description: "rules.general.REDUCE_MIN_HOURS_PTO_DESC"
            },
            {
              function: "rules.general.ALLOW_CHAIN_TEAMS",
              description: "rules.general.ALLOW_CHAIN_TEAMS_DESC"
            },
            {
              function: "rules.general.ALLOW_CHAIN_SKILLS",
              description: "rules.general.ALLOW_CHAIN_SKILLS_DESC"
            },
            {
              function: "rules.general.ALLOW_CHAIN_MIDNIGHT",
              description: "rules.general.ALLOW_CHAIN_MIDNIGHT_DESC"
            },
            {
              function: "rules.general.FORCE_COMPLETION",
              description: "rules.general.FORCE_COMPLETION_DESC"
            }
          ],
          setConsecutiveLimit: {
            options: [-1, 1, 2, 3, 4, 5, 6, 7],
            function: "rules.general.CONSECUTIVE_LIMIT",
            description: "rules.general.CONSECUTIVE_LIMIT_DESC"
          },
          setProfileDayType: {
            function: "rules.general.PROFILE_DAY_TYPE",
            description: "rules.general.PROFILE_DAY_TYPE_DESC",
            options: ["DayShiftEnds", "DayShiftStarts", "ShiftMajority"]
          }
        },
        optimization: {                                            // Site Optimization panel
          title: "rules.general.SITE_OPTIMIZATION",
          settings: []
        },
        general: {                                                 // General Settings panel
          title: "rules.general.GENERAL",
          countries: $scope.countriesList
        }
      };



      //--------------------------------------------------------------------
      // Load Org data
      //--------------------------------------------------------------------

      //
      // GET Scheduling Settings
      // for Org-level

      var getSchedulingSettings = function () {
        return rulesGeneralFactory.getSchedulingSettings()
          .then(function (res) {
            var data = res.data;
            preparePanelsToDisplay(data);
          })
        ;
      };


      //
      // GET Organization Details

      var getOrgDetails = function () {
        return dataService.getOrgDetails()
          .then(function (res) {
            orgInit = res;
            $scope.org = angular.copy(orgInit);
          })
        ;
      };

      getSchedulingSettings();
      getOrgDetails();



      var preparePanelsToDisplay = function(data){

        // Set Site Configuration settings
        $scope.panels.configuration.settings[0].value = data.breakShiftAtMidnightForDisplay;
        $scope.panels.configuration.settings[1].value = data.breakShiftAtMidnightForHours;
        $scope.panels.configuration.settings[2].value = data.reduceMaximumHoursForPTO;
        $scope.panels.configuration.settings[3].value = data.allowChainingAccrossTeams;
        $scope.panels.configuration.settings[4].value = data.allowChainingAccrossSkills;
        $scope.panels.configuration.settings[5].value = data.allowChainingAccrossMidnight;
        $scope.panels.configuration.settings[6].value = data.forceCompletion;

        $scope.panels.configuration.setConsecutiveLimit.value = data.consecutiveLimitOf12hoursDays;    // digit
        $scope.panels.configuration.setProfileDayType.value = data.profileDayType;                     // string


        // Set Site Optimization settings
        $scope.panels.optimization.settings = [];

        for (var i = 0; i < data.optimizationSettings.length; i++) {                      // # of settings - not all?
          $scope.panels.optimization.settings.push( {} );
          $scope.panels.optimization.settings[i].value = data.optimizationSettings[i].value;
          $scope.panels.optimization.settings[i].type = data.optimizationSettings[i].type;
          $scope.panels.optimization.settings[i].name = data.optimizationSettings[i].name;

          $scope.panels.optimization.settings[i].function = 'rules.general.' + data.optimizationSettings[i].name;
          $scope.panels.optimization.settings[i].description = 'rules.general.' + data.optimizationSettings[i].name + '_DESC';

          if ( data.optimizationSettings[i].name === 'OptimizationPreference' ) {
            $scope.panels.optimization.settings[i].options = ["None", "COP", "CPO", "OCP", "OPC", "PCO", "POC"];
          }
        }
        //console.log($scope.panels.optimization);

        panelsInit = angular.copy($scope.panels);                                          // save initial Settings
      };



      //--------------------------------------------------------------------
      // CRUD
      //--------------------------------------------------------------------

      //
      // UPDATE Org level settings

      $scope.updateOrgSettings = function(){

        if ($scope.scheduleSettingsForm.$valid) {
          var deferred = $q.defer();
          $scope.updatesCounter = 0;

          //
          // Check if Scheduling Settings have been changed.
          // If yes - update Scheduling Settings

          if ( !angular.equals($scope.panels, panelsInit) ) {
            $scope.updatesCounter++;
            updateSchedulingSettings().then(function(){
              $scope.updatesCounter--;
            });
          }

          //
          // Check if Address details have been changed.
          // If yes - update Scheduling Settings

          if ( !angular.equals($scope.org, orgInit) ){
            $scope.updatesCounter++;
            updateOrgAddress().then(function(){
              $scope.updatesCounter--;
            });
          }

          //
          // Wait for both API calls to be resolved,
          // then update page options
          // and resolve promise

          var removeThisWatcher = $scope.$watch("updatesCounter", function(newVal, oldVal) {
            if (newVal === 0 && oldVal === 1) {
              $scope.page.editing = false;
              $scope.page.submitted = false;
              applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
              deferred.resolve();

              removeThisWatcher();                                           // remove this $watch
            }
          });

          return deferred.promise;
        }
      };



      //
      // UPDATE Scheduling settings on Org level

      var updateSchedulingSettings = function(){
        var deferred = $q.defer();

        // Prepare Site Configuration settings
        var newScheduleSettings = {};

        newScheduleSettings.breakShiftAtMidnightForDisplay = $scope.panels.configuration.settings[0].value;
        newScheduleSettings.breakShiftAtMidnightForHours = $scope.panels.configuration.settings[1].value;
        newScheduleSettings.reduceMaximumHoursForPTO = $scope.panels.configuration.settings[2].value;
        newScheduleSettings.allowChainingAccrossTeams = $scope.panels.configuration.settings[3].value;
        newScheduleSettings.allowChainingAccrossSkills = $scope.panels.configuration.settings[4].value;
        newScheduleSettings.allowChainingAccrossMidnight = $scope.panels.configuration.settings[5].value;
        newScheduleSettings.forceCompletion = $scope.panels.configuration.settings[6].value;
        newScheduleSettings.consecutiveLimitOf12hoursDays = $scope.panels.configuration.setConsecutiveLimit.value;    // digit
        newScheduleSettings.profileDayType = $scope.panels.configuration.setProfileDayType.value;                     // string


        // Prepare Site Optimization settings
        newScheduleSettings.optimizationSettings = [];

        for (var i = 0; i < $scope.panels.optimization.settings.length; i++) {
          newScheduleSettings.optimizationSettings.push( {} );
          newScheduleSettings.optimizationSettings[i] = {
            "type" : $scope.panels.optimization.settings[i].type,
            "name" : $scope.panels.optimization.settings[i].name,
            "value": $scope.panels.optimization.settings[i].value
          };
        }

        dataService.updScheduleSettings(newScheduleSettings).then( function(res){
          getSchedulingSettings();
          deferred.resolve('SUCCESS');

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          deferred.resolve('ERROR');
        });

        return deferred.promise;
      };



      //
      // UPDATE Address details on Org level

      var updateOrgAddress = function(){
        var deferred = $q.defer();

        var newAddress = {
          "name"             : $scope.org.name,
          "description"      : $scope.org.description,
          "inactivityPeriod" : $scope.org.inactivityPeriod,
          "address"          : $scope.org.address,
          "address2"         : $scope.org.address2,
          "city"             : $scope.org.city,
          "country"          : $scope.org.country,
          "geo"              : $scope.org.geo,
          "state"            : $scope.org.state,
          "timeZone"         : $scope.org.timeZone,
          "zip"              : $scope.org.zip
        };

        //
        // Update Org details

        dataService.updOrgDetails(newAddress).then( function(res) {
          $scope.scheduleSettingsForm.$setPristine(true);
          getOrgDetails();
          deferred.resolve('SUCCESS');

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          deferred.resolve('ERROR');
        });

        return deferred.promise;
      };



      //--------------------------------------------------------------------
      // Other methods
      //--------------------------------------------------------------------


      //
      // Sortable options
      // for drag-n-drop behavior in Optimization Settings table
      // Dependency: Angular UI Sortable

      $scope.sortableOptions = {
        update: function() {
          $scope.updateEditing();
        },
        sort: function(e) {
          if (!$scope.hasMgmtPermission()) {
            return false;
          }
        },
        axis: 'y',
        containment: 'parent',
        delay: 150,
        revert: true
      };



      //
      // Register call back function,
      // for confirmation Dialog & Save btn in Notification

      var working = applicationContext.getWorking();

      working.entityName = 'rules.GENERAL';
      working.option = $scope.page;
      working.saveFunc = $scope.updateOrgSettings;
      //working.restoreFunc = restoreOriginalSchedule;
    }
  ]);
})();