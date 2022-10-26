
var scheduleBuilder = angular.module('emlogis.schedule_builder');
var GENERAL = "GENERAL";
var SPECIFIC = "SPECIFIC";

scheduleBuilder.controller('ScheduleBuilderShiftPatternsCtrl',
  [
    '$scope',
    '$state',
    '$modal',
    '$translate',
    '$http',
    '$q',
    '$timeout',
    'applicationContext',
    'crudDataService',
    'appFunc',
    'dialogs',
    'patternGraph',
    'patternList',
    function(
      $scope, $state, $modal, $translate, $http, $q, $timeout, applicationContext,
      crudDataService,
      appFunc,
      dialogs,
      patternGraph,
      patternList
    ) {

      // const variable
      var MIN_PER_HOUR = 60;
      var demandCellMin = 30;
      var defaultMaxDemandCount = 5;

      // CrudDataService
      var factory = _.clone(crudDataService);

      /**
       * This variable will be used to indicate whether ui is on the process of loading shift patterns or not
       */
      $scope.shiftPatternsLoading = false;

      $scope.minCalendarDate = new Date();

      /**
       * Scope Variables in Shift Pattern
       * 1. $scope.patternCalendar.demandShiftLengths : checkbox which is used when user select demand-based
       * 2. $scope.patternCalendar.currentShiftPattern.demandHeaderItems : contains number of dots in graph for shift demands
       * 3. scaleMin : minutes in one cell in shift demand graph
       * 4. $scope.site.shiftTypes: ShiftTypes which is used for shift pattern generation
       * 5. demandShiftLengths: DemandShiftLengths which can be used
       * 6. $scope.sites: have all sites detailed objects
       * 7. $scope.teams: have teams for selected site which are displaying on dropdown list
       * 8. $scope.allTeams: all teams object
       * 9. $scope.skills: have skills for selected team which are displaying on dropdown list
       * 10. $scope.allSkills: all skills object
       * 11. $scope.shiftPatternDayCollection: shiftPatternHierarcy pattern list table
       * 12: graph: graph related object
       * 13: $scope.site.manualShiftLengths: manual-based pattern: dropdown of shift type of each row
       * 14: $scope.patternCalendar.currentShiftPattern.demands: automatic-based shift pattern demands: circles
       * 15: $scope.patternCalendar.currentShiftPattern.maxDemandCount
       */


      console.log('Schedule Builder Shift Patterns controller');
      angular.element('.eml-top-tabs').scope().tabChecked = true;

      // Activate Shift Pattern tab

      $scope.tabs[1].active = true;

      var graph = null;

      /**
       * Initialize all the variables
       */

      $scope.init = function() {

        $scope.patternCalendar = patternList.getPatternCalendar();

        // days
        $scope.days = [];
        for (var i=1; i < 31; i++){
          $scope.days.push(i+' days');
        }

        // Create empty current currentShiftPattern and currentShiftPatternDay
        $scope.patternCalendar.currentShiftPattern = {};
        $scope.patternCalendar.currentShiftPatternDay = {};

        /**
         * It contains fir
         * @type {null}
         */
        $scope.patternCalendar.originShiftPattern = {};

        // To be used for shift patterns generation
        $scope.patternCalendar.currentShiftPattern.shifts = []; // Shift value
        $scope.patternCalendar.currentShiftPattern.shiftIds = []; // Shift value
        $scope.patternCalendar.currentShiftPattern.hours = []; // Contains required and excess hours
        $scope.patternCalendar.currentShiftPattern.shiftLengths = [];

        // Set default dayType, cdDate
        $scope.patternCalendar.option = {dayType: GENERAL, cdDate: '', editing: false};

        $scope.graph = patternGraph.getGraph();

        /**
         * This function will also load all detail of sites, teams and skills.
         * And build initial dropdown list
         */

        $scope.site = null;
        $scope.team = null;
        $scope.skill = null;

        loadTreeMetaData()
          .then(function(entities) {
            /**
             * It loads meta data tree structure of site->teams->skills
             */
            $scope.shiftPatternMetaData = entities.data;

            /**
             * Load sites, it will load sites, site Ids are already loaded
             * and this function will add more details info to Sites variable
             */
            return $scope.loadSites();
          })
          .then(function(entities) {

            var sites = [];
            // Filter only name attributes
            for(var i in entities.data) {

              /**
               * get appropriate shiftpatternmetadata
               */
              var siteMeta = null;

              for (var j=0; j<$scope.shiftPatternMetaData.length; j++) {

                if ($scope.shiftPatternMetaData[j].id == entities.data[i].id) {
                  siteMeta =$scope.shiftPatternMetaData[j];
                  break;
                }
              }

              var daysOfWeek = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

              for (j in daysOfWeek) {
                if (daysOfWeek[j] === entities.data[i].firstDayOfWeek) {
                  break;
                }
              }

              var ticked = false;
              if (parseInt(i) ===0 ) {
                ticked = true;
              }

              sites.push(
                {id: entities.data[i].id, name: entities.data[i].name, ticked: ticked,
                  teams: [], metaData: siteMeta, timeZone: entities.data[i].timeZone,
                  firstDayOfWeek: entities.data[i].firstDayOfWeek,
                  firstDayIntOfWeek: parseInt(j)
                }
              );
            }

            // Save the filtered properties into scope variable: Shift Patterns
            $scope.sites = sites;


              //load all teams
            return $scope.loadAllTeams();

          })
          .then(function(entities) { //teams

            var allTeams = [];
            // Filter only name attributes
            for(var i in entities.data) {

              /**
               * get appropriate shiftpatternMetaData
               */
              var teamMeta = null;
              var site = null;

              for (var j=0; j<$scope.sites.length; j++) {
                site =$scope.sites[j];

                if (site.metaData ===null) {
                  continue;
                }

                for (var k=0; k < site.metaData.children.length; k++) {

                  if (site.metaData.children[k].id == entities.data[i].id) {
                    teamMeta =site.metaData.children[k];
                    break;
                  }
                }

                if (teamMeta !==null) {
                  break;
                }
              }

              var team = {
                id: entities.data[i].id,
                name: entities.data[i].name,
                abbreviation: entities.data[i].abbreviation,
                ticked: false,
                skills: [],
                metaData: teamMeta
              };

              allTeams.push(team);

              //add it to site
              if (site !==null) {
                site.teams.push(team);
              }
            }

            // Save the filtered properties into scope variable
            $scope.allTeams = allTeams;

            //set site
            return $scope.loadAllSkills();

          })
          .then(function(entities) { //entities = skills,

            /**
             * this function will build site->team->skill structure
             * @type {Array}
             */
            var allSkills = [];
            var team= null;
            var skillMeta = null;

            for(var i in entities.data) {

              var skill = null;

              for (var j=0; j<$scope.allTeams.length; j++) {

                team =$scope.allTeams[j];

                if (team.metaData ===null) {
                  continue;
                }

                for (var k=0; k < team.metaData.children.length; k++) {

                  if (team.metaData.children[k].id == entities.data[i].id) {
                    skillMeta =team.metaData.children[k];
                    break;
                  }
                }

                if (skillMeta !==null) { //skill can belong to multiple teams

                  skill = {
                    id: entities.data[i].id,
                    name: entities.data[i].name,
                    abbreviation: entities.data[i].abbreviation,
                    ticked: false
                  };

                  if (skill === null) {
                    allSkills.push(skill);
                  }

                  //add it to site
                  if (team !==null) {
                    team.skills.push(skill);
                  }

                  skillMeta = null; //reset skillMeta

                }
              }


            }

            // Save the filtered properties into scope variable
            $scope.allSkills = allSkills;

            /**
             * select default $scope.team
             */
            if ($scope.site && $scope.site.teams.length > 0) {
              $scope.team =$scope.site.teams[0];
            }


            // Make Default Selection for DropdownList

            $scope.teams = [];
            $scope.skills = [];

            if ($scope.sites.length > 0) {
              $scope.sites[0].ticked = true;
              $scope.site = $scope.sites[0];

              $scope.teams = $scope.site.teams;

              if ($scope.teams.length > 0) {
                $scope.teams[0].ticked = true;
                $scope.team = $scope.teams[0];

                $scope.skills = $scope.team.skills;

                if ($scope.skills.length > 0) {
                  $scope.skills[0].ticked = true;
                  $scope.skill = $scope.skills[0];
                }
              }
            }

            /**
             * remove metaDataVariable from sites,teams,skills after building structure
             * to save memory
             */

            angular.forEach($scope.sites, function(site,key) {

              delete site.metaData;
            });

            angular.forEach($scope.sites, function(site,key) {
              delete site.metaData;
            });

            angular.forEach($scope.allTeams, function(team,key) {
              delete team.metaData;
            });

            angular.forEach($scope.skills, function(skill,key) {
              delete skill.metaData;
            });

           return $scope.loadShiftPatterns(); //load shiftPatterns Meta Data

          })
          .then(function(entities) { // load shift lengths
            return $scope.loadShiftLengths($scope.site);
          })
          .then(function(entities) { //loadshifttypes
            return $scope.loadShiftTypes($scope.site);
          })
          .then(function(entities) { //shift patterns

//            processAssignShiftPatterns();
            patternList.processAssignShiftPatterns({
              site: $scope.site,
              team: $scope.team,
              skill: $scope.skill
            });
            setDefaultPatternInSkill();

          })
          .catch(function(error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
          })
        ;

        $scope.patternCalendar.option.patternType = 'Set';
        $scope.optimumCols = 24; // Time frames in dots panel

        /**
         * if true: it will display shiftReqDtos.
         * false: it will display generate shift patterns button
         * @type {boolean}
         */
        $scope.patternCalendar.option.demandPatternGenerated = false;

        // Clear Graph Area
        patternGraph.initializeGridTimes();

        /**
         * requiredHeaderItems: it will be used with automatic
         */

          // Required & Excess
        patternGraph.initializeRequiredHeaderItems();
        patternGraph.initializeExcessHeaderItems();

        patternGraph.emptyGridTimes();


        patternList.clearDemandHeaderItems($scope.patternCalendar.currentShiftPattern);

        /**
         * Max Rows in schedule demand creation, the dots graph
         */
        $scope.patternCalendar.currentShiftPattern.maxDemandCount = 5;

        patternList.clearDemands($scope.patternCalendar.currentShiftPattern, $scope.patternCalendar.currentShiftPattern.maxDemandCount);
        $scope.patternCalendar.option.demandCount = $scope.patternCalendar.currentShiftPattern.maxDemandCount; //default demand Count
        $scope.demandMouseDown = false;


//        // Load all demand based shift lengths
//        factory.getElements("shiftlengths/ops/query?orderby=id&limit=0&hasshifttype=true",{})
//          .then(function(entities){
//            $scope.demandShiftLengths = entities.data;
//          }
//        );

//        patternList.initShiftPatternDayCollection();

        $scope.patternCalendar.currentShiftPattern.excess = 0;
        $scope.patternCalendar.currentShiftPattern.base = 0;

        /**
        * Register call back function, for confirmation Dialog
        */
        var working = applicationContext.getWorking();

        working.option = $scope.patternCalendar.option;
        working.restoreFunc = restoreOriginalShiftPattern;
        working.saveFunc = processSave;
        working.entityName = 'schedule_builder.SHIFT_PATTERNS';

      };

      /**
       * getShiftPattern by patternId,
       * Mostly used for correct shiftpattern indication after loading
       * @param patternId
       * @returns {*}
       */
      function getShiftPatternFromMeta(patternId) {

        var pattern = null;
        angular.forEach($scope.shiftPatterns, function(ele, key) {

          if (ele.id === patternId) {
            pattern = ele;
          }

        });


        return pattern;
      }


      // Update Max Pattern Count per Day

      $scope.updateMaxDayPatternCount = function() {

        $scope.patternCalendar.maxDayPatternCount = 0;

        angular.forEach($scope.shiftPatternDayCollection, function(shiftPatternDay, key) {

          if (shiftPatternDay.patterns.length > $scope.patternCalendar.maxDayPatternCount){
            $scope.patternCalendar.maxDayPatternCount = shiftPatternDay.patterns.length;
          }

        });
      };

     

      // Load sites into dropdown list
      $scope.loadSites = function() {

        // No query parameters, it will show all sites
        return factory.getElements("sites?limit=0&orderby=name&orderdir=ASC",{});

      };

      /**
       * This function is called when user clicks discard button in save the work dialog
       */
      function restoreOriginalShiftPattern() {

        $scope.patternCalendar.option.editing = false;
        angular.copy($scope.patternCalendar.originShiftPattern, $scope.patternCalendar.currentShiftPattern);
        //$scope.patternCalendar.currentShiftPattern.selected = true;
        /**
         * reconfigure shifts: it should be reconfigured
         * @type {Array}
         */
        //$scope.patternCalendar.currentShiftPattern.shifts = [];
        //
        //for (var i in $scope.patternCalendar.originShiftPattern.shifts) {
        //  var shift = $scope.patternCalendar.originShiftPattern.shifts[i];
        //  $scope.patternCalendar.currentShiftPattern.shifts.push(patternList.getShiftType(shift.id));
        //
        //}
        //
        //patternList.processAssignShiftPatterns({
        //  site: $scope.site,
        //  team: $scope.team,
        //  skill: $scope.skill
        //});
        //setDefaultPatternInSkill();
      }



      /**
       * Internal Load Skills Process
       */
      function processLoadingSkills(team, defaultSkill) {

        $scope.skills = team.skills;

        if ( (defaultSkill || null) === null) {
          $scope.skill =null;
        }
        else {
          $scope.skill = findSkillInTeam($scope.team, defaultSkill);
        }


        if ($scope.skill === null && $scope.skills.length > 0) {
          $scope.skill = $scope.skills[0];
        }

        angular.forEach($scope.skills, function(skill, key) {
          skill.ticked = false;
        });

        if ($scope.skill !== null) {
          $scope.skill.ticked = true;
        }

      }

        /**
         * Internal Load Team Process: Shift Pattern
         */
      function processLoadingTeams(site, defaultTeam) {

        $scope.teams = site.teams;
        $scope.team = defaultTeam || null;

        //clear $scope.skills
        $scope.skills = [];

        if (defaultTeam === null && $scope.teams.length > 0) {
          $scope.team = $scope.teams[0];
        }

        angular.forEach($scope.teams, function(team, key) {
          team.ticked = false;
        });

        if ($scope.team !== null) {
          $scope.team.ticked = true;
//          $scope.loadSkills($scope.team);
          processLoadingSkills($scope.team);
        }

        if ($scope.teams.length ===0) {

          patternList.processAssignShiftPatterns({
            cleanOnly: true
          });

          $scope.skill = null;
        }

      }


      /**
       * call this function after dialog box action of save-discard modal
       */
      function assistLoadingTeams (site, defaultTeam) {
        //originalSite = angular.copy($scope.site);

        //originalSkill = angular.copy($scope.skill);
        //originalTeam = angular.copy($scope.team);

        site = findSiteInMetaStructure(site);

        //Load Shift lengths
        $scope.loadShiftLengths(site)
          .then (function () {
            return $scope.loadShiftTypes(site);
          })
          .then(function(entities) {
            patternList.setDefaultOptions();
            processLoadingTeams(site,defaultTeam);
            patternList.processAssignShiftPatterns({
              site: site,
              team: $scope.team,
              skill: $scope.skill
            });
            setDefaultPatternInSkill();

            /**
             * update $scope.site
             * @type {site}
             */
            $scope.site = site;
          });

      }


      /**
       * Load teams into dropdown list  based on the site : Shift Pattern
       * @param site: loads all teams for the site
       * @param defaultTeam: default selection item, if not, first item will be selected, null: it means from UI click
       */
      $scope.loadTeams = function(site, defaultTeam) {


        if ($scope.patternCalendar.option.editing ===  true ) {
          appFunc.getSaveWorkDlg()
            .then(function(reason){

              var working = applicationContext.getWorking();

              /**
               * here we just need Save, we don't need Discard, since it will be reloading anyway
               */
              if (reason === SAVE) {

                /**
                 * Save, and load teams
                 */
                //patternList.setDefaultOptions();
                //
                //return saveOnModalFromChangingFilters(originalSite, originalTeam, originalSkill);

                working.saveFunc(site, $scope.team, $scope.skill)
                  .then(function(result) {
                    assistLoadingTeams(site, defaultTeam);
                  }, function(error) {
                    //do nothing.
                  });

              }
              else if (reason === DISCARD) {
                /**
                 * Rollback current pattern and proceed
                 */
                $scope.patternCalendar.option.editing = false;
                //patternList.setDefaultOptions();
                restoreOriginalShiftPattern();
                assistLoadingTeams(site, defaultTeam);

              }


            }, function(reason) {
              /**
               * It is cancellation.
               * Just rollback site checking option
               */
              restoreSite($scope.site);

              console.log('dismissed');
            });
        }
        else {
          assistLoadingTeams(site, defaultTeam);
        }



        /**
         * This value will be used when user click 'cancel' in save work dialog box
         */
        //var originalSite = angular.copy($scope.site);
        //$scope.site = findSiteInMetaStructure(site);
        //var originalSkill = angular.copy($scope.skill);
        //var originalTeam = angular.copy($scope.team);
        //
        ////Load Shift lengths
        //$scope.loadShiftLengths($scope.site)
        //  .then (function () {
        //    return $scope.loadShiftTypes($scope.site);
        //  })
        //  .then(function(entities) {
        //    patternList.setDefaultOptions();
        //    processLoadingTeams($scope.site,defaultTeam);
        //    patternList.processAssignShiftPatterns({
        //      site: $scope.site,
        //      team: $scope.team,
        //      skill: $scope.skill
        //    });
        //    setDefaultPatternInSkill();
        //
        //  });
      };

      /**
       * Load all teams, mostly called at initialization process
       */
      $scope.loadAllTeams = function() {

        return factory.getElements('teams?limit=0&orderby=name&orderdir=ASC',{});

      };

      /**
       * Load all skills, mostly called at initialization process
       */
      $scope.loadAllSkills = function() {

        return factory.getElements('skills?limit=0&orderby=name&orderdir=ASC&filter=isActive=true',{});

      };


      /**
      * Load skills into dropdown list based on the team : ShiftPattern
      * @param team: loads all skills for the team
      * @param defaultSkill: default selection item, if not, first item will be selected, if null it is from UI Click
      */
      $scope.loadSkills = function(team, defaultSkill) {

        /**
         * This value will be used when user click 'cancel' in save work dialog box
         */

        var originalSite = angular.copy($scope.site);
        var originalSkill = angular.copy($scope.skill);
        var originalTeam = angular.copy($scope.team);

        /**
         * assign scope team variable
         */
        $scope.team = findTeam(team.id);

        /**
         * check shiftpattern editing status
         */
        if (defaultSkill === null && $scope.patternCalendar.option.editing) {

          appFunc.getSaveWorkDlg()
            .then(function(reason){

              var working = applicationContext.getWorking();

              /**
               * here we just need Save, we don't need Discard, since it will be reloading anyway
               */
              if (reason === SAVE) {

                  /**
                   * Save, it will refresh every pattern.
                   */
                  patternList.setDefaultOptions();
                return saveOnModalFromChangingFilters(originalSite, originalTeam, originalSkill);


              }
              else if (reason === DISCARD) {
                $scope.patternCalendar.option.editing = false;
                /**
                 * Rollback current pattern and proceed
                 */
                restoreOriginalShiftPattern();
                patternList.setDefaultOptions();
                processLoadingSkills(team, defaultSkill);

                patternList.processAssignShiftPatterns({
                  site: $scope.site,
                  team: $scope.team,
                  skill: $scope.skill
                });
                setDefaultPatternInSkill();

              }


          }, function(reason) {
            /**
             * It is cancellation.
             * Just rollback site checking option
             */
            restoreTeam(originalTeam);
            console.log('dismissed');
          });

          return;

        }
        else {
          patternList.setDefaultOptions();
        }

        processLoadingSkills(team, defaultSkill);

        patternList.processAssignShiftPatterns({
          site: $scope.site,
          team: $scope.team,
          skill: $scope.skill
        });
        setDefaultPatternInSkill();


      };


      /**
       * Load Shift Patterns into tree dropdown list, This is related to db,
       * build tree structure
       * It  contains metadata of shift patterns
       * Each shift pattern contains no shiftReqDto
       */
      $scope.loadShiftPatterns = function() {

        var deferred = $q.defer();
        $scope.shiftPatterns = [];
        $scope.shiftPatternsLoading = true;

        // No query parameters, it will show all shift patterns
        return factory.getElements("shiftpatterns/list?limit=0&orderby=name&orderdir=ASC",{})
          .then(function(entities){

            // set proper key & values;
            angular.forEach(entities.data, function(shift, key) {
              shift.id = shift.shiftPatternId;
              shift.name = shift.shiftPatternName;

              if (shift.shiftPatternCdDate) {
                shift.shiftPatternCdDate = appFunc.convertToBrowserTimezone(shift.shiftPatternCdDate, $scope.site.timeZone);
              }

              delete shift.shiftPatternId;
              delete shift.shiftPatternName;
            });


            /**
             * Check scope skills are loaded if not wait.
             * Skills are last node in site->team->skills, if it has value sites,teams variables have value
             */
            if ($scope.skills) {
              buildShiftPatternsTree(entities.data);
            }
            else {
              /**
               * Wait for skills variable becomes active
               */
              var skillsListener = $scope.$watch('skills', function(newValue, oldValue) {

                if ((typeof newValue !== 'undefined') && (typeof oldValue === 'undefined')) {
                  buildShiftPatternsTree(entities.data);

                  //destroy watch
                  skillsListener();
                }
                else {
                  return;
                }

              });
            }


            // Save the filtered properties into scope variable
            $scope.shiftPatterns = entities.data;
            $scope.shiftPatternsLoading = false;

          });
      };

        // Load Shift Types Names into dropdown list
      $scope.loadShiftTypes = function(site) {


        var deferred = $q.defer();

        if ((site || null) === null) {
          //returns empty array
          deferred.resolve([]);
          return deferred.promise;
        }
        else if ((site.shiftTypes || null) ===null ) {

          // No query parameters, it will show all sites
          return factory.getElements('sites/' + site.id +'/shifttypes?limit=0&orderby=shiftLength.lengthInMin&orderdir=ASC',{})
            .then(function(entities){

              var shiftTypes = [];
              var ticked = true;

              // Save the filtered properties into scope variable
              site.shiftTypes = entities.data;

              // This code should be removed, adding manual start point

              angular.forEach(site.shiftTypes, function(shift, key) {

                /**
                 * StartTime is in longValue (miliseconds)
                 *
                 * @type {number}
                 */
                shift.start = shift.startTime / 1000 / 60 / 60;
                /**
                 * Start, End time scale will be displayed like 12:00 Am, 4:30PM ,etc
                 */
                shift.startHourStr = $scope.getTimeString(shift.start);
                shift.endHourStr = $scope.getTimeString(shift.start + shift.shiftLengthLength / MIN_PER_HOUR);

              });

              /*
               Used to parse to shift pattern list directive, it should be wrapped inside JSON
               */
              $scope.patternCalendar.option.shiftTypes = site.shiftTypes;

            });
        }
        else {
          //restore pattern calendar
          $scope.patternCalendar.option.shiftTypes = site.shiftTypes;
          deferred.resolve( site.shiftTypes);
          return deferred.promise;
        }



      };

      /**
       * load Shift lengths of the site, if it is already loaded skip
       */

      $scope.loadShiftLengths = function(site) {

        var deferred = $q.defer();

        if ((site || null) === null) {
          deferred.resolve([]);
          return deferred.promise;
        }
        else if ((site.manualShiftLengths || null) === null ) {

          // No query parameters, it will show all shift patterns
          return factory.getElements('sites/' + site.id +'/shiftlengths?limit=0&orderby=lengthInMin&orderdir=ASC',{})
            .then(function(entities){
              $scope.patternCalendar.demandShiftLengths = entities.data;
              site.manualShiftLengths = angular.copy(entities.data);
              $scope.patternCalendar.option.manualShiftLengths = site.manualShiftLengths;
            });
        }
        else {
          // update pattern calendar and option
          $scope.patternCalendar.demandShiftLengths = angular.copy(site.manualShiftLengths);
          $scope.patternCalendar.option.manualShiftLengths = site.manualShiftLengths;

          deferred.resolve([]);
          return deferred.promise;
        }
      };

      $scope.getTimeString = function (scale){

        // set default date
        var dt = new Date(214,1,1,0,0,0);
        var newDateObj = new Date(dt.getTime() + scale * 60 * 60 * 1000); // add by hours
        return _.strftime (newDateObj, '%I:%M %p');

      };

      // Shows start date dialog box
      $scope.showDate = function($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.cdDateOpened = true;
      };



      //calculate base,net and excess
      $scope.calculateTotals = function() {

        $scope.patternCalendar.currentShiftPattern.base = 0;
        $scope.patternCalendar.currentShiftPattern.excess = 0;
        $scope.patternCalendar.currentShiftPattern.resources = 0;

        if (!$scope.patternCalendar.currentShiftPattern.shifts) {
          return;
        }
        for (var i = 0; i < $scope.patternCalendar.currentShiftPattern.shifts.length; i++) {
          var ele = $scope.patternCalendar.currentShiftPattern.shifts[i];
          if (!ele || !ele.id)
              continue;
          var hour = $scope.patternCalendar.currentShiftPattern.hours[i];
          $scope.patternCalendar.currentShiftPattern.base +=  hour.required * ele.shiftLengthLength / MIN_PER_HOUR;
          $scope.patternCalendar.currentShiftPattern.excess +=  hour.excess * ele.shiftLengthLength / MIN_PER_HOUR;
          $scope.patternCalendar.currentShiftPattern.resources += hour.required + hour.excess;
        }
      };


      // Add one Shift
      $scope.addShift = function() {

        // check all existing shifts are valid
        for (var i = 0; i < $scope.patternCalendar.currentShiftPattern.shifts.length; i++) {
          var shift = $scope.patternCalendar.currentShiftPattern.shifts[i];
          var hour = $scope.patternCalendar.currentShiftPattern.hours[i];

          if (!shift || !shift.id) {
            applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_VALID_SHIFT_VALUES', '', true);
            return;
          }

          if (!hour || hour.required <= 0 ) {
            applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_VALID_HOUR_VALUES', '', true);
            return;
          }


        }
        if ($scope.patternCalendar.option.editing !== true) {
          $scope.patternCalendar.option.editing = true;
        }

        $scope.patternCalendar.currentShiftPattern.shifts.push({});
        $scope.patternCalendar.currentShiftPattern.shiftIds.push('');
        $scope.patternCalendar.currentShiftPattern.shiftLengths.push({});
        $scope.patternCalendar.currentShiftPattern.hours.push({'required': 1, 'excess': 0});
      };

      // Remove current Shift
      $scope.removeShift = function($index) {
        if ($scope.patternCalendar.option.editing !== true) {
          $scope.patternCalendar.option.editing = true;
        }
        $scope.patternCalendar.currentShiftPattern.shifts.splice($index, 1);
        $scope.patternCalendar.currentShiftPattern.shiftIds.splice($index, 1);
        $scope.patternCalendar.currentShiftPattern.hours.splice($index, 1);
        $scope.patternCalendar.currentShiftPattern.shiftLengths.splice($index, 1);
      };

      // Get Current Skill Id
      function getSkill() {

        for (var i = 0; i < $scope.skills.length; i++) {
          var ele = $scope.skills[i];
          if (ele.ticked === true) {
            return ele;
          }
        }
      }

      // Get Current Team Id
      function getTeam() {

        for (var i = 0; i < $scope.teams.length; i++) {
          var ele = $scope.teams[i];
          if (ele.ticked === true) {
            return ele;
          }
        }
      }

      /**
       * update skill dropdown list
       * @param skill
       */
      function setSkillFilter(skill) {

        angular.forEach($scope.skills, function(ele) {
          if (ele.id == skill.id) {
            ele.ticked = true;
          }
          else {
            ele.ticked = false;
          }
        });
      }

      /**
       * update team dropdown list
       * @param team
       */
      function setTeamFilter(team) {

        angular.forEach($scope.teams, function(ele) {
          if (ele.id == team.id) {
            ele.ticked = true;
          }
          else {
            ele.ticked = false;
          }
        });
      }

        /**
         * Update Site Dropdown List
         * @param site
         */

      function setSiteFilter(site) {

        angular.forEach($scope.sites, function(ele) {
          if (ele.id == site.id) {
            ele.ticked = true;
          }
          else {
            ele.ticked = false;
          }
        });
      }


      /**
       * Shift Pattern Save:
       * @param site:
       * @param team: if not specified, scope.team will be used
       * @param skill: if not specified, scope.skill will be used
       */
      $scope.save = function(site, team,skill) {

        var deferred = $q.defer();

        processSave(site,team,skill)
          .then(function (result) {
            //update
            if ($scope.patternCalendar.currentShiftPattern.id) {
              $scope.patternCalendar.option.editing = false; //initialize the option editing variable

              applicationContext.setNotificationMsgWithValues($scope.patternCalendar.currentShiftPattern.name + ' build', '', true);
              console.log("ShiftPattern Updated : " + $scope.patternCalendar.currentShiftPattern.id);
            }
            else {

              $scope.patternCalendar.currentShiftPattern.id = result.id;
              result.shifts = [];
              result.shiftIds = [];
              result.hours = [];


              applicationContext.setNotificationMsgWithValues(result.name + ' build', '', true);
              console.log("ShiftPattern Saved : " + result.id);
            }

            /**
             * // Reload shift patterns and filters
             * This function initialize shift patterns variable
             */
            return $scope.loadShiftPatterns()
              .then(function(entities) {

                $scope.patternCalendar.option.editing = false;

                processUpdatingShiftPatternFilters($scope.site, $scope.team, $scope.skill);

                if ($scope.patternCalendar.currentShiftPattern.id) {
                  var cur = getShiftPatternFromMeta($scope.patternCalendar.currentShiftPattern.id);
                  if (cur !== null) {
                    if (cur.shiftPatternCdDate !==null) {
                      $scope.patternCalendar.currentShiftPatternDay = patternList.getShiftPatternDay(cur.shiftPatternCdDate, SPECIFIC, $scope.site.firstDayIntOfWeek);
                    }
                    else {
                      $scope.patternCalendar.currentShiftPatternDay = patternList.getShiftPatternDay(cur.shiftPatternDayOfWeek, GENERAL);
                    }

                    patternList.selectShiftPattern(cur);
                  }
                }

              });

          });

//        if (!$scope.patternCalendar.currentShiftPatternDay.day) {
//          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_ADD_SHIFT_PATTERN_UNDER_SPECIFIC_DAY', '', true);
//          deferred.reject('Error');
//          return deferred.promise;
//        }
//
//        /**
//         * these variables are using in update/save scenario
//         */
//        var i,shift,hour,dt, dtFormat;
//        var shiftLengths = [];
//        var ele = null;
//
//        /**
//         * Check whether it contains null shift
//         */
//        var nullShift = _.find(
//          $scope.patternCalendar.currentShiftPattern.shifts, function(shift){
//            return shift === null || !shift.id;
//          }
//        );
//
//        if (typeof nullShift !== 'undefined' && $scope.patternCalendar.currentShiftPattern.shifts.length>0) {
//          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_A_SHIFT', '', true);
//          deferred.reject('Error');
//          return deferred.promise;
//        }
//
//
//        var emptyHour = _.find(
//          $scope.patternCalendar.currentShiftPattern.hours, function(hour){
//            return hour === null || hour.required<=0 || hour.excess<0;
//          }
//        );
//
//        if (typeof emptyHour !== 'undefined' && $scope.patternCalendar.currentShiftPattern.hours.length>0) {
//          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_VALID_HOURS', '', true);
//          deferred.reject('Error');
//          return deferred.promise;
//        }
//
//        if ($scope.patternCalendar.currentShiftPattern.id) {
//          /**
//           * User can not update specific date nor day in general day
//           */
//          var updateDto = {
//            name: $scope.patternCalendar.currentShiftPattern.name,
//            shiftReqDtos: $scope.patternCalendar.currentShiftPattern.shiftReqDtos
//          };
//
//          if ($scope.patternCalendar.option.dayType === 'GENERAL') {
//            updateDto.dayOfWeek = $scope.patternCalendar.currentShiftPatternDay.day;
//          }
//          else {
//            if (!$scope.patternCalendar.option.cdDate) {
//              applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SPECIFIC_DATE', '', true );
//              deferred.reject('Error');
//              return deferred.promise;
//            }
//
//            updateDto.cdDate = appFunc.getDateWithTimezone(
//              $scope.patternCalendar.option.cdDate.getFullYear(),
//              $scope.patternCalendar.option.cdDate.getMonth(),
//              $scope.patternCalendar.option.cdDate.getDate(),
//              $scope.site.timeZone
//            ).getTime();
//
//          }
//
//          if ($scope.patternCalendar.option.patternType === 'Demand') { //demand based
//            updateDto.type = 'Demand';
//
//            /**
//             * build shiftLengthList
//             */
//            shiftLengths = [];
//            ele = null;
//
//            for (i in $scope.patternCalendar.demandShiftLengths) {
//              ele = $scope.patternCalendar.demandShiftLengths[i];
//              if (ele.checked === true) {
//                shiftLengths.push(ele.id);
//              }
//            }
//
//            updateDto.shiftLengthList = shiftLengths.join();
//
//            /**
//             * build shiftDemandDtos
//             */
//            updateDto.shiftDemandDtos = [];
//            fo// r (i in $scope.patternCalendar.currentShiftPattern.demandHeaderItems) {
//              ele = $scope.patternCalendar.currentShiftPattern.demandHeaderItems[i];
//              if (ele.value > 0) {
//                updateDto.shiftDemandDtos.push(
//                  {
//                    "startTime": i* demandCellMin * 60 * 1000,
//                    "lengthInMin" : demandCellMin,
//                    "employeeCount": ele.value
//                  }
//                );
//              }
//            }
//
//          }
//          else {
//            //Manual Based Pattern
//            updateDto.type = 'Set';
//            updateDto.shiftReqDtos = [];
//
//            /**
//             * build shiftReqDto for shift pattern
//             */
//            for (i in $scope.patternCalendar.currentShiftPattern.shifts) {
//              shift = $scope.patternCalendar.currentShiftPattern.shifts[i];
//              hour = $scope.patternCalendar.currentShiftPattern.hours[i];
//              updateDto.shiftReqDtos.push({
//                'employeeCount': hour.required,
//                'excessCount': hour.excess,
//                'shiftTypeId': shift.id
//              });
//            }
//          }
//
//          $scope.patternCalendar.option.editing = false; //initialize the option editing variable
//
//          return factory.updateElement('shiftpatterns',$scope.patternCalendar.currentShiftPattern.id, updateDto)
//            .then(function (result){
//
////            $scope.patternCalendar.currentShiftPattern = result;
//              console.log("ShiftPattern Updated : " + result.id);
//
//              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', '', true , result.name);
//              /**
//               * This function initialize shift patterns variable
//               */
//              return $scope.loadShiftPatterns()
//                .then(function() {
//
//                  $scope.patternCalendar.option.editing = false;
//
//                  processUpdatingShiftPatternFilters(site, $scope.team, $scope.skill);
//
//                  if ($scope.patternCalendar.currentShiftPattern.id) {
//                    var cur = getShiftPatternFromMeta($scope.patternCalendar.currentShiftPattern.id);
//                    if (cur !== null) {
//                      patternList.selectShiftPattern(cur);
//                    }
//                  }
//
//                });
//
//
//            }, function (error) {
//              console.log(error);
//
//              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
//              deferred.reject('Error');
//              return deferred.promise;
//            });
//
//        }
//        else {
//          /*
//          Create ShiftPattern on db
//           */
//          if (!team) {
//            team = getTeam();
//          }
//
//          if (!skill) {
//            skill = getSkill();
//          }
//
//          if (!skill) {
//            applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_A_SKILL', '', true);
//            deferred.reject('Error');
//            return deferred.promise;
//
//          }
//
//          var dto = {
//            skillId: skill.id,
//            teamId: team.id,
//            name : $scope.patternCalendar.currentShiftPattern.name,
//            updateDto: {
//              description : '',
//              shiftReqDtos: []
//            }
//          };
//
//          /**
//           * Put cdDate and dayOfWeek
//           */
//          if ($scope.patternCalendar.option.dayType === 'SPECIFIC') {
//            if (!$scope.patternCalendar.option.cdDate) {
//              applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SPECIFIC_DATE', '', true );
//              deferred.reject('Error');
//              return deferred.promise;
//            }
//
//            /**
//             * Save the cdDate with site timezone
//             */
//            dto.updateDto.cdDate = appFunc.getDateWithTimezone(
//              $scope.patternCalendar.option.cdDate.getFullYear(),
//              $scope.patternCalendar.option.cdDate.getMonth(),
//              $scope.patternCalendar.option.cdDate.getDate(),
//              $scope.site.timeZone
//            ).getTime();
//
//          }
//          else {
//            dto.updateDto.dayOfWeek = $scope.patternCalendar.currentShiftPatternDay.day;
//          }
//
//          /**
//           * Prepare param for creation for demand based and manual pattern
//           */
//          if ($scope.patternCalendar.option.patternType === 'Demand') { //demand based
//
//            dto.updateDto.type = 'Demand';
//
//            /**
//             * build shiftLengthList
//             */
//            shiftLengths = [];
//            ele = null;
//
//            for (i in $scope.patternCalendar.demandShiftLengths) {
//              ele = $scope.pat ternCalendar.demandShiftLengths[i];
//              if (ele.checked === true) {
//                shiftLengths.push(ele.id);
//              }
//            }
//
//            dto.updateDto.shiftLengthList = shiftLengths.join();
//
//            /**
//             * build shiftDemandDtos
//             */
//            dto.updateDto.shiftDemandDtos = [];
//            for (i in $scope.patternCalendar.currentShiftPattern.demandHeaderItems) {
//              ele = $scope.patternCalendar.currentShiftPattern.demandHeaderItems[i];
//              if (ele.value > 0) {
//                dto.updateDto.shiftDemandDtos.push(
//                  {
//                    "startTime": i* demandCellMin * 60 * 1000,
//                    "lengthInMin" : demandCellMin,
//                    "employeeCount": ele.value
//                  }
//                );
//              }
//            }
//
//            //"shiftDemandDtos":[{"startTime":0, "lengthInMin":30, "employeeCount":1}
//
//
//          }
//          else { // Manual Shift Pattern
//            dto.updateDto.type = 'Set';
//
//            /**
//             * build shiftReqDto for shift pattern
//             */
//            dto.updateDto.shiftReqDtos = [];
//            for (i in $scope.patternCalendar.currentShiftPattern.shifts) {
//              shift = $scope.patternCalendar.currentShiftPattern.shifts[i];
//              hour = $scope.patternCalendar.currentShiftPattern.hours[i];
//              dto.updateDto.shiftReqDtos.push({
//                'employeeCount': hour.required,
//                'excessCount': hour.excess,
//                'shiftTypeId': shift.id
//              });
//            }
//
//          }
//
//          $scope.patternCalendar.option.editing = false; //initialize the option editing variable
//          return factory.createElement('shiftpatterns',dto)
//            .then(function (result){
//
//              $scope.patternCalendar.currentShiftPattern.id = result.id;
//              result.shifts = [];
//              result.shiftIds = [];
//              result.hours = [];
//
//              // Reload shift patterns and filters
//
//              applicationContext.setNotificationMsgWithValues(result.name + ' build', '', true);
//              console.log("ShiftPattern Saved : " + result.id);
//              /**
//               * This function initialize shift patterns variable
//               */
//              return $scope.loadShiftPatterns()
//                .then(function(entities) {
//
//                  $scope.patternCalendar.option.editing = false;
//
//                  processUpdatingShiftPatternFilters($scope.site, $scope.team, $scope.skill);
//
//                  if ($scope.patternCalendar.currentShiftPattern.id) {
//                    var cur = getShiftPatternFromMeta($scope.patternCalendar.currentShiftPattern.id);
//                    if (cur !== null) {
//                      patternList.selectShiftPattern(cur);
//                    }
//                  }
//
//                });
//
//
//
//            }, function (error) {
//              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
//              deferred.reject('gError');
//              return deferred.promise;
//            });
//        }
      };

      /**
       * Delete Current Shift Pattaz§§§§ern
       */
      $scope.delete = function() {
        if (typeof $scope.patternCalendar.currentShiftPattern.name === 'undefined') {
          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_CREATE_ONE_SHIFT_PATTERN', '', true);
          return;
        }

        var dlg = dialogs.confirm('Please Confirm','Do you really want to delete shift pattern ' + $scope.patternCalendar.currentShiftPattern.name +'?');

        dlg.result.then(function(btn){

          if ($scope.patternCalendar.currentShiftPattern.id) {

            factory.deleteElement('shiftpatterns', $scope.patternCalendar.currentShiftPattern.id)
              .then(function (result) {
                console.log('delete shiftpattern id:' + $scope.patternCalendar.currentShiftPattern.id + " :result " + result);
                applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY','',true);

                /**
                 * This function initialize shift patterns variable
                 */
                $scope.loadShiftPatterns()
                  .then(function() {
                    $scope.patternCalendar.option.editing = false;

                    processUpdatingShiftPatternFilters($scope.site, $scope.team, $scope.skill);

                    setDefaultPatternInSkill();

                  });

              }, function (error) {
                console.log(error);
                applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
              });
          }
          else {
            applicationContext.setNotificationMsgWithValues('Deleted on the browser', '', true);

            // refresh grid
            patternList.processAssignShiftPatterns({
              site: $scope.site,
              team: $scope.team,
              skill: $scope.skill
            });

            setDefaultPatternInSkill();
          }


        },function(btn){
          console.log("entity not deleted");
        });

      };

      /**
       * On Click Action on the shift graph
       * Listens an event
       * @param shift
       */
      $scope.selectShiftInGraph = function(shift) {

        if (!shift.id)
          return false;

        var dlg = $modal.open({
          templateUrl: 'modules/schedule_builder/partials/schedule_builder_shift_pattern_shift_graph_modal.html',
          windowClass: 'schedule-builder',
          controller: function($scope, $modalInstance, shift, hours) {

            $scope.isModalOpen = true;
            $scope.shift = shift;
            $scope.required = shift.required;
            $scope.excess = shift.excess;

            // When we go to login page we have to dismiss the modal.
            $scope.$on('event:auth-loginRequired', function () {

              //if ($scope.isModalOpen === true){
              $modalInstance.dismiss('cancel');
              //  $scope.isModalOpen = false;
              //}

            });

            // Update Shift in select-shift as well as in graph
            $scope.update = function(shift) {
              hours[shift.index].required = $scope.required;
              hours[shift.index].excess = $scope.excess;
              $modalInstance.close('update');
            };

            // Close Modal
            $scope.close = function(){
              $modalInstance.dismiss('cancel');
            };

            /**
             * In ShiftPattern Graph Modal Dialog
             */
            $scope.filterZero = function(evt) {

              var elem = null;

              if (evt.which !== 48 && evt.which !== 96) {
                return;
              }

              if (evt.srcElement)
                elem = evt.srcElement;
              else if (evt.target)
                elem = evt.target;

              if (elem !== null ){
                if (elem.value.indexOf('0') === 0) {
                  elem.value = Math.abs(parseInt(elem.value)) || 0;
                }
              }

            };

            /**
             *
             * updateShiftHours: In ShiftPattern Graph Modal Dialog
             */
            $scope.updateShiftHours = function() {

              $scope.required = Math.abs(parseInt($scope.required)) || 0;
              $scope.excess = Math.abs(parseInt($scope.excess)) || 0;

            };


          },
          resolve: {
            shift: function() {
              return shift;
            },
            hours: function() {
              return $scope.patternCalendar.currentShiftPattern.hours;
            }
          }

        });

        dlg.result.then(function(result){
          console.log(result);
          // Update graph
          patternList.generateShiftPattern();

        }, function(reason) {
          console.log('dismissed');
        });
      };

      function processUpdatingShiftPatternFilters(site, team, skill) {

        /**
         * update drop down lists
         */

        processLoadingTeams(site,team);
        processLoadingSkills(team,skill);
        //setSiteFilter(site);

        patternList.processAssignShiftPatterns({
          site: site,
          team: $scope.team,
          skill: $scope.skill
        });

      }
      /**
       * Update the filter parameters and refresh the grid from tree panel
       * @param site
       * @param team
       * @param skill
       */
      $scope.updateShiftPatternFilters = function (site, team, skill) {


        // if try to load same site,team,skill then reject
        if ($scope.site === site && $scope.team === team && $scope.skill === skill) {
          return;
        }

        if ($scope.patternCalendar.option.editing) {

          appFunc.getSaveWorkDlg()
            .then(function(reason){

              var working = applicationContext.getWorking();

              /**
               * here we just need Save, we don't need Discard, since it will be reloading anyway
               */
              if (reason === SAVE) {

                working.saveFunc($scope.site, $scope.team, $scope.skill)
                  .then(function(result) {
                    $scope.patternCalendar.option.editing = false;
                    assistUpdatingShiftPatternFilters(site, team, skill);

                  }, function(error) {
                    //do nothing.
                  });

              }
              else if (reason === DISCARD) {
                /**
                 * Rollback current pattern and proceed
                 */
                $scope.patternCalendar.option.editing = false;
                restoreOriginalShiftPattern();
                assistUpdatingShiftPatternFilters(site, team, skill);
              }


            }, function(reason) {
              /**
               * It is cancellation.
               * Just rollback site checking option
               */
              console.log('dismissed');
            });

        }
        else {
          assistUpdatingShiftPatternFilters(site, team, skill);
        }

      };


      /**
       * assist function of update shift pattern filters
       */
      function assistUpdatingShiftPatternFilters (site, team, skill) {


        /**
         * should load shiftlengths and shifttypes
         */
        $scope.loadShiftLengths(site)
          .then (function () {
            return $scope.loadShiftTypes(site);
          })
          .then(function() {
            $scope.team = team;
            $scope.skill = skill;


            processUpdatingShiftPatternFilters(site, team, skill);
            setSiteFilter(site);
            /**
             * Select default pattern
             */
            $scope.site = site;
            setDefaultPatternInSkill();

          });
      }

        // Duplicate current Shift Pattern
      $scope.duplicateShiftPattern = function () {

        if (!$scope.patternCalendar.currentShiftPattern.id) {
          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_CREATE_ONE_SHIFT_PATTERN', '', true);
          return;
        }

        var generalDays = [
          {id: 'Sunday', display: 'calendar.SUNDAY', checked: false},
          {id: 'Monday', display: 'calendar.MONDAY', checked: false},
          {id: 'Tuesday', display: 'calendar.TUESDAY', checked: false},
          {id: 'Wednesday', display: 'calendar.WEDNESDAY', checked: false},
          {id: 'Thursday', display: 'calendar.THURSDAY', checked: false},
          {id: 'Friday', display: 'calendar.FRIDAY', checked: false},
          {id: 'Saturday', display: 'calendar.SATURDAY', checked: false}
        ];

        /**
         * generalDays: Comma separated strings, cdDate: specificDate
         */
        var duplicate = {generalDays: '', cdDate: '', type: 'GENERAL'};

        var dlg = $modal.open({
          templateUrl: 'modules/schedule_builder/partials/schedule_builder_shift_pattern_duplicate_modal.html',
          windowClass: 'schedule-builder',
          controller: function($scope, $modalInstance, generalDays, duplicate) {

            $scope.generalDays = generalDays;
            $scope.duplicate = duplicate;

            // When we go to login page we have to dismiss the modal.
            $scope.$on('event:auth-loginRequired', function () {
                $modalInstance.dismiss('cancel');
            });

            $scope.open = function($event) {
                $event.preventDefault();
                $event.stopPropagation();
                $scope.dateOpened = true;
            };

            $scope.selectedGeneralDays = function () {

              var tmpArray = [];
              for (var i in generalDays) {
                var ele = generalDays[i];
                if (ele.checked === true) {
                  tmpArray.push(ele.id);
                }
              }

              duplicate.generalDays = tmpArray.join();
            };

            // DuplicateAction
            $scope.duplicateAction = function() {
              $modalInstance.close('duplicate');
            };

            // Close Modal
            $scope.close = function(){
              $modalInstance.dismiss('cancel');
            };

          },
          resolve: {
            generalDays: function() {
              return generalDays;
            },
            duplicate: function() {
              return duplicate;
            }
          }

        });

        dlg.result.then(function(reason) {
          //shiftpatterns/{patternId}/ops/duplicate
          var url = applicationContext.getBaseRestUrl() + 'shiftpatterns/' + $scope.patternCalendar.currentShiftPattern.id + '/ops/duplicate';
          var dto = {days:'', cdDate: 0};


          if (duplicate.type === GENERAL) {
              dto.days = duplicate.generalDays;
          }
          else {
              dto.cdDate = duplicate.cdDate.getTime();
          }

          $http.post(url,dto)
            .success(function(data) {
              applicationContext.setNotificationMsgWithValues('schedule_builder.DUPLICATED_SUCCESSFULLY', '', true , $scope.patternCalendar.currentShiftPattern.name);

              /**
               * This function initialize shift patterns variable
               */
              $scope.loadShiftPatterns()
                .then(function() {
                  $scope.patternCalendar.option.editing = false;

                  processUpdatingShiftPatternFilters($scope.site, $scope.team, $scope.skill);

                  if ($scope.patternCalendar.currentShiftPattern.id) {
                    var cur = getShiftPatternFromMeta($scope.patternCalendar.currentShiftPattern.id);
                    if (cur !== null) {
                      patternList.selectShiftPattern(cur);
                    }
                  }

                });

            })
            .error(function(error) {
                applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            });

        }, function(reason) {
            console.log('dismissed');
        });

      };

      /**
       * It will generate pattern name following some rules
       */
      $scope.genDefaultName = function() {

        /**
         * Check whether it contains null shift
         */
        var nullShift = _.find(
          $scope.patternCalendar.currentShiftPattern.shifts, function(shift){
            return shift === null || !shift.id;
          }
        );

        if(typeof nullShift  !== "undefined" && $scope.patternCalendar.currentShiftPattern.shifts.length>0) {
          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_A_SHIFT', '', true);
          return;
        }

        if ($scope.patternCalendar.option.editing !== true) {
          $scope.patternCalendar.option.editing = true;
        }

        var minStartTime = null, maxEndTime = null;
        var shifts = $scope.patternCalendar.currentShiftPattern.shifts;

        if (shifts.length > 0) {

          if (shifts[0] !==null && typeof shifts[0].start !== 'undefined' && shifts[0].start !== null ) {
            minStartTime = shifts[0].start;
            maxEndTime = shifts[0].start + shifts[0].shiftLengthLength / MIN_PER_HOUR;
          }

        }

        for (var i = 1; i<shifts.length; i++) {

          if (shifts[i] !== null && shifts[i].start !== null && typeof shifts[i].start !== 'undefined') {

            if (minStartTime > shifts[i].start) {
              minStartTime = shifts[i].start;
            }

            if (maxEndTime < shifts[i].start + shifts[i].shiftLengthLength / MIN_PER_HOUR) {
              maxEndTime = shifts[i].start + shifts[i].shiftLengthLength / MIN_PER_HOUR;
            }

          }

        }

        var defaultName = '';

        if (minStartTime !== null) {
          defaultName =  $scope.getTimeString(minStartTime) ;
        }
        else {
          defaultName = 'NoStart Time';
        }

        if (maxEndTime !== null) {
          defaultName += '-' + $scope.getTimeString(maxEndTime) ;
        }
        else {
          defaultName += '-' + 'NoEnd Time';
        }

        //patternCalendar.currentShiftPattern.name
        //        <startTime> - <end Time> <Skill>  * N (<Team>)

        if ($scope.skill === null) {
          defaultName += ' ' + 'No Skill';
        }
        else if ($scope.skill.abbreviation !== '' && $scope.skill.abbreviation !== null) {
          defaultName += ' ' + $scope.skill.abbreviation;
        }
        else {
          defaultName += ' ' + $scope.skill.name ;
        }

        defaultName += '  * ' + ( $scope.patternCalendar.currentShiftPattern.resources  || '0' ) +' ';

        if ($scope.team === null) {
          defaultName += +' ' + '(No Team)';
        }
        else if ($scope.team.abbreviation !== '' && $scope.team.abbreviation !== null) {
          defaultName += '(' + $scope.team.abbreviation + ')';
        }
        else {
          defaultName += '(' + $scope.team.name + ')';
        }

        $scope.patternCalendar.currentShiftPattern.name = defaultName;


      };


      /**
       * It is called when user change the item in skills
       * If the user change one of the site and team, eventually skill is changed,
       * and this this function will be fired
       * @param skill
       * @param clicked: true: it is clicked from user
       */
      $scope.assignShiftPatterns = function(skill ) {

        /**
         * This value will be used when user click 'cancel' in save work dialog box
         */
        var originalSite = angular.copy($scope.site);
        var originalSkill = angular.copy($scope.skill);
        var originalTeam = angular.copy($scope.team);


        /**
         * assign scope skill variable
         */
        $scope.skill = findSkillInTeam($scope.team,skill);


        /**
         * check shiftpattern editing status
         */
        if ($scope.patternCalendar.option.editing) {

          appFunc.getSaveWorkDlg()
            .then(function(reason) {

              var working = applicationContext.getWorking();

              if (reason === SAVE ) {
                patternList.setDefaultOptions();
                return saveOnModalFromChangingFilters(originalSite, originalTeam, originalSkill);
              }
              else if (reason === DISCARD) {
                $scope.patternCalendar.option.editing = false;
                patternList.setDefaultOptions();
                /**
                 * Rollback current pattern and proceed
                 */

                if (working.option !==null)
                  working.option.editing = false;
                working.restoreFunc();

                patternList.processAssignShiftPatterns({
                  site: $scope.site,
                  team: $scope.team,
                  skill: $scope.skill
                });
                setDefaultPatternInSkill();

              }

            }, function(reason) {
              /**
               * It is cancellation.
               * Just rollback site checking option
               */

              restoreSkill(originalSkill);
              console.log('dismissed');
            });

            return;

        }
        else {
          patternList.setDefaultOptions();

          patternList.processAssignShiftPatterns({
            site: $scope.site,
            team: $scope.team,
            skill: $scope.skill
          });
          setDefaultPatternInSkill();
        }

      };


      /**
       * @returns site in siteteamskills
       */
      function findSiteInMetaStructure(site) {

        for (var i in $scope.sites) {
          var ele = $scope.sites[i];
          if (ele.id == site.id) {
            return ele;
          }
        }
        return null;
      }

      /**
       * @returns team in dropdown list
       */
      function findTeam(teamId) {

        for (var i in $scope.teams) {

          var ele = $scope.teams[i];
          if (ele.id == teamId) {
            return ele;
          }
        }
        return null;
      }

      /**
       *
       * @param site
       * @returns {*}
       */
      function findSiteInTree(shiftPattern){

          for (var i in $scope.shiftPatternsTree) {
              var ele = $scope.shiftPatternsTree[i];
              if (ele.id == shiftPattern.siteId) {
                  return ele;
              }
          }

          return null;
      }


      /**
       *
       * @param team
       * @param skill
       * @returns {*}
       */
      function findSkillInTeam(team, skill) {

        if ((skill || null)=== null) {
          return null;
        }

        for (var i in team.skills) {
          var ele = team.skills[i];

          if (ele.id == skill.id) {
            return ele;
          }
        }

        return null;
      }

      /**
       * Returns array of shift patterns matching site,team,skill
       * @param shiftPatterns
       * @param site
       * @param team
       * @param skill
       * @param dayType
       */
      function findShiftPatterns(shiftPatterns, site,team,skill, dayType) {
        var res = [];

        angular.forEach(shiftPatterns, function(shiftPattern) {
          if ((shiftPattern.siteId === site.id) && (shiftPattern.teamId === team.id) && (shiftPattern.skillId === skill.id)) {

//                  if ((dayType === GENERAL) && (shiftPattern.shiftPatternDayOfWeek !== null)) {
            if ((dayType === GENERAL) && (shiftPattern.shiftPatternCdDate === null)) {
              res.push(shiftPattern);
            }

            if ((dayType === SPECIFIC) && (shiftPattern.shiftPatternCdDate !== null)) {
              res.push(shiftPattern);
            }

          }
        });
        return res;
      }

      /**
       * accept list of shift patterns in shift pattern module
       * generate shift pattern list to be used in quick pane
       */
      function buildShiftPatternsTree(shiftPatterns) {

        /**
         *
         * @type {Array}
         * it will contain site objects
         */
//        $scope.shiftPatternsTree = [];
        $scope.shiftPatternsTree = $scope.sites;

        angular.forEach($scope.sites, function(site) {

//          /**
//           * Insert New Site Node
//           */
//          $scope.shiftPatternsTree.push(site);


          angular.forEach(site.teams, function(team) {

            /**
             * Insert New Team Node
             */

//            site.teams.push(team);
//            team.skills = [];
            angular.forEach(team.skills, function(skill) {

              /**
               * Insert New Skill Node
               */

//                team.skills.push(skill);

              /**
               * Get Shift Patterns for general day
               */
              var generalPatterns = findShiftPatterns(shiftPatterns, site,team,skill, GENERAL );
              skill.generalPatterns = generalPatterns;

              /**
               * Load specific date patterns and organize by specific date
               */
              var specificPatterns = findShiftPatterns(shiftPatterns, site,team,skill, SPECIFIC );
//                skill.specificPatterns = organizeSpecificPatterns(specificPatterns); //maybe we will not use this method
              skill.specificPatterns = specificPatterns;

            });
          });



        });

      }

      /**
       * load Overall Structure of shift pattern metadata
       */
      function loadTreeMetaData() {

        return factory.getElements("sites/siteteamskills",{});
      }

      /**
       *
       * @param date
       * @returns {dateString}
       */
      function getDateString(date) {

        if (!date)
            return null;
        var year = date.getFullYear().toString();
        var month = (1 + date.getMonth()).toString();
        month = month.length > 1 ? month : '0' + month;
        var day = date.getDate().toString();
        day = day.length > 1 ? day : '0' + day;
        return year + '-' + month + '-' + day;

      }

      function organizeSpecificPatterns(specificPatterns) {

        /**
         * It will have following structure
         * [
         *  { date: '2014-11-02', patterns: [{#pattern1}, {#pattern2}]}
         *  { date: '2014-11-05', patterns: [{#pattern3}, {#pattern4}]}
         * ]
         */
        var organizedPatterns = [];
        /**
         * iterate specificPatterns
         */
        angular.forEach(specificPatterns, function(pattern) {

          var node = null;
          var nodeDateStr = getDateString(new Date(pattern.shiftPatternCdDate));
          var found = false;
          for (var i in organizedPatterns) {
            node = organizedPatterns[i];
            if (nodeDateStr === node.date ) {
              found = true;
              break;
            }
          }

          if (found === false ) {
            /**
             * If it is new node create a node with one value pattern inside
             */
            organizedPatterns.push({date: nodeDateStr, patterns: [pattern]});
          }
          else {
            /**
             * when it founds a node, append the pattern
             */
            node.patterns.push(pattern);
          }

        });

        return organizedPatterns;
      }

      $scope.updateSpecificDate = function() {
        console.log($scope.patternCalendar.option.cdDate);

        patternList.processAssignShiftPatterns({
          site: $scope.site,
          team: $scope.team,
          skill: $scope.skill
        });
      };


        /**
         * Save As Action. it will require one modal dialog with new Name and save
         */
        $scope.saveAs = function() {

          /**
           * Check whether current shift pattern has valid ID or not
           */
          if (!$scope.patternCalendar.currentShiftPattern.id) {
              applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_CREATE_ONE_SHIFT_PATTERN', '', true);
              return;
          }

          /**
           * Check whether it contains null shift
           */
          var nullShift = _.find(
            $scope.patternCalendar.currentShiftPattern.shifts, function(shift){
              return shift === null || !shift.id;
            }
          );

          if(typeof nullShift  !== "undefined" && $scope.patternCalendar.currentShiftPattern.shifts.length>0) {
            applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_A_SHIFT', '', true);
            return;
          }

          var pattern = {name: ''};

          var dlg = $modal.open({
            templateUrl: 'modules/schedule_builder/partials/schedule_builder_shift_pattern_save_as_modal.html',
            windowClass: 'schedule-builder',
            controller: function($scope, $modalInstance, pattern) {

              $scope.pattern = pattern;

              // When we go to login page we have to dismiss the modal.
              $scope.$on('event:auth-loginRequired', function () {
                $modalInstance.dismiss('cancel');
              });


              // DuplicateAction
              $scope.save = function() {
                $modalInstance.close('save');
              };

              // Close Modal
              $scope.close = function(){
                $modalInstance.dismiss('cancel');
              };


            },
            resolve: {
              pattern: function() {
                return pattern;
              }
            }

          });

          dlg.result.then(function(reason) {
            /**
             * Copy current shift pattern into new one and save it with different name
             * these variables are using in update/save scenario
             */
            var i,shift,hour;

            if(pattern.name.trim === '') {
              applicationContext.setNotificationMsgWithValues('schedule_builder.ENTER_SHIFT_PATTERN_NAME', '', true);
              return;
            }

            /*
             Create ShiftPattern on db
             */

            var dto = {
              skillId: $scope.skill.id,
              teamId: $scope.team.id,
              name : pattern.name,
              updateDto: {
                description : '',
                type: 'Set',
                shiftReqDtos: []
              }
            };

            /**
             * build shiftReqDto for shift pattern
             */

            for (i in $scope.patternCalendar.currentShiftPattern.shifts) {
              shift = $scope.patternCalendar.currentShiftPattern.shifts[i];
              hour = $scope.patternCalendar.currentShiftPattern.hours[i];
              dto.updateDto.shiftReqDtos.push({
                'employeeCount': hour.required,
                'excessCount': hour.excess,
                'shiftTypeId': shift.id
              });
            }

            if ($scope.patternCalendar.currentShiftPattern.shiftPatternCdDate) {
              /**
               * Save the cdDate with site timezone
               */
              dto.updateDto.cdDate = appFunc.getDateWithTimezone(
                $scope.patternCalendar.currentShiftPattern.shiftPatternCdDate.getFullYear(),
                $scope.patternCalendar.currentShiftPattern.shiftPatternCdDate.getMonth(),
                $scope.patternCalendar.currentShiftPattern.shiftPatternCdDate.getDate(),
                $scope.site.timeZone
              ).getTime();

            }

            if ($scope.patternCalendar.currentShiftPattern.shiftPatternDayOfWeek) {
              dto.updateDto.dayOfWeek = $scope.patternCalendar.currentShiftPattern.shiftPatternDayOfWeek;
            }


            factory.createElement('shiftpatterns',dto)
              .then(function (result){
                $scope.patternCalendar.currentShiftPattern.id = result.id;
                result.shifts = [];
                result.shiftIds = [];
                result.hours = [];

                /**
                 * This function initialize shift patterns variable
                 */
                $scope.loadShiftPatterns()
                  .then(function(entities) {

                    $scope.patternCalendar.option.editing = false;

                    processUpdatingShiftPatternFilters($scope.site, $scope.team, $scope.skill);

                    if ($scope.patternCalendar.currentShiftPattern.id) {
                      var cur = getShiftPatternFromMeta($scope.patternCalendar.currentShiftPattern.id);
                      if (cur !== null) {
                        patternList.selectShiftPattern(cur);
                      }
                    }

                  });


              }, function (error) {
                applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
              });


          }, function(reason) {
            console.log('dismissed');
          });

        };

        /**
         * It will show of 'save your work' dialog box and have a appropriate action
         */
        function showSaveWorkDlg() {

          var dlg = $modal.open({
            templateUrl: 'modules/_layouts/partials/authenticated_save_work_modal.html',
            windowClass: 'schedule-builder',
            controller: function($scope, $modalInstance) {

              // When we go to login page we have to dismiss the modal.
              $scope.$on('event:auth-loginRequired', function () {
                $modalInstance.dismiss('cancel');
              });

              // Save Action
              $scope.save = function() {
                $modalInstance.close('save');
              };

              // Discard Action
              $scope.discard = function() {
                $modalInstance.close('discard');
              };

              // Close Modal
              $scope.close = function(){
                $modalInstance.dismiss('cancel');
              };

            }

          });

          dlg.result.then(function(reason){
            console.log(reason);


          }, function(reason) {
            console.log('dismissed');
          });
        }

      /**
       * returns true, if team contains any patterns.
       * @param team
       * @returns {boolean}
       */
      $scope.canShowTreeTeam = function(team) {

        if (team.skills.length === 0) {
          return false;
        }

        for (var i in team.skills) {
          var skill = team.skills[i];
          if (skill.generalPatterns.length > 0) {
            return true;
          }

        }
        return false;

      };

      /**
       * returns true, if site contains any patterns.
       * @param team
       * @returns {boolean}
       */
      $scope.canShowTreeSite = function(site) {
        if (site.teams.length === 0) {
          return false;
        }

        for (var i in site.teams) {
          var team = site.teams[i];
          if ($scope.canShowTreeTeam(team) === true ) {
            return true;
          }
        }
        return false;

      };

      /**
       *
       */
      $scope.updateEditing = function() {
        /**
         * if it none of the patterns are not assigned to any of column, make it false.
         */
        if (!$scope.patternCalendar.currentShiftPatternDay.day) {
          $scope.patternCalendar.option.editing = false;
          return;
        }
        if ($scope.patternCalendar.option.editing !== true) {
          $scope.patternCalendar.option.editing = true;
        }
      };


      /**
       * Using in demand shift pattern dots header
       * @param $index
       * @param isGridTime
       * @returns {boolean}
       */
      $scope.inOptimumCols = function($index, isGridTime) {

        if (isGridTime === true) {
          return ($index < $scope.optimumCols );
        }
        else {
          return ($index < $scope.optimumCols * 2 );
        }

      };

      /**
       * Update Circles
       */
      $scope.updateDemandHeaderItems = function (headerItem, $index) {

        if ($scope.patternCalendar.option.editing !== true) {
          $scope.patternCalendar.option.editing = true;
        }

        /**
         * Update maxDemandCount, if the value is bigger than that
         */
        var i =0;
        var headerValue =parseInt(headerItem.value) || 0;

        if (parseInt($scope.patternCalendar.currentShiftPattern.maxDemandCount) < headerValue) {

          for (i=0; i<headerItem.value-$scope.patternCalendar.currentShiftPattern.maxDemandCount; i++) {
            var rowData = {
              row: i+1,
              data: new Array($scope.patternCalendar.currentShiftPattern.demandHeaderItems.length)
            };

            $scope.patternCalendar.currentShiftPattern.demands.unshift(rowData);
          }


          $scope.patternCalendar.currentShiftPattern.maxDemandCount= headerValue;
          $scope.patternCalendar.option.demandCount = $scope.patternCalendar.currentShiftPattern.maxDemandCount;

        }

        for (i=0; i<$scope.patternCalendar.currentShiftPattern.maxDemandCount; i++) {
          var demandRow = $scope.patternCalendar.currentShiftPattern.demands[i];
          demandRow.row = i+1;

          if (i< $scope.patternCalendar.currentShiftPattern.maxDemandCount - headerValue) {
            demandRow.data[$index] = 0; // empty
          }
          else {
            demandRow.data[$index] = 1; //show circle
          }
        }
      };

      /**
       * Update Circles
       */
      $scope.updateDemandCircles = function (demand, $index) {

        /**
         * Update maxDemandCount, if the value is bigger than that
         */
        if ($scope.demandMouseDown === false) {
          return;
        }

        var demandCount =demand.row;

        var difference = 1; // it will be 0 to add
        if (demand.data[$index] !==0 ) {
          difference = 0;
        }

        $scope.patternCalendar.option.demandPatternGenerated = false;
        $scope.patternCalendar.currentShiftPattern.shiftReqDtos = [];

        for (var i=0; i<$scope.patternCalendar.currentShiftPattern.maxDemandCount; i++) {
          var demandRow = $scope.patternCalendar.currentShiftPattern.demands[i];
          if (i <  parseInt(demandCount)-difference) {
            demandRow.data[$index] = 0; // empty
          }
          else {

            /**
             *
             */

            //if ($scope.patternCalendar.currentShiftPattern.demandHeaderItems[$index].allowed === false) {
            //  return;
            //}

            demandRow.data[$index] = 1; //show circle
          }
        }

        $scope.patternCalendar.currentShiftPattern.demandHeaderItems[$index].value = $scope.patternCalendar.currentShiftPattern.maxDemandCount - (parseInt(demandCount)-difference);
      };

      $scope.setDemandMouseDown = function(value,demand,$index) {
        $scope.demandMouseDown = value;

        if (value===true && $scope.patternCalendar.option.editing !== true) {
          $scope.patternCalendar.option.editing = true;
        }

        if (value === true) {
          $scope.updateDemandCircles(demand,$index);
        }

      };

      /**
       * Generate ShiftReqDtos from shift demand, only available for shift demand
       */
      $scope.generateShiftReqs = function() {

        var url = applicationContext.getBaseRestUrl() + 'shiftpatterns/ops/draftcomputeshiftreqs';

        var dto = {};

        /**
         * build shiftLengthList
         */
        var shiftLengths = [];
        var ele = null;

        for (var i in $scope.patternCalendar.demandShiftLengths) {
          ele = $scope.patternCalendar.demandShiftLengths[i];
          if (ele.checked === true) {
            shiftLengths.push(ele.id);
          }
        }

        dto.allowedShiftLengthIds = shiftLengths;

        /**
         * build shiftDemandDtos
         */
        dto.shiftDemandDtos = [];
        for (i in $scope.patternCalendar.currentShiftPattern.demandHeaderItems) {
          ele = $scope.patternCalendar.currentShiftPattern.demandHeaderItems[i];
          if (ele.value > 0) {
            dto.shiftDemandDtos.push(
              {
                "startTime": i* demandCellMin * 60 * 1000,
                "lengthInMin" : demandCellMin,
                "employeeCount": ele.value
              }
            );
          }
        }

        $http.post(url,dto)
          .success(function(result) { //result: array of shiftReqDtos

            $scope.patternCalendar.currentShiftPattern.shiftLengthList = shiftLengths.join();
            $scope.patternCalendar.currentShiftPattern.shifts = [];
            $scope.patternCalendar.currentShiftPattern.shiftIds = [];
            $scope.patternCalendar.currentShiftPattern.hours = [];
            $scope.patternCalendar.currentShiftPattern.shiftReqDtos = result;

            angular.forEach(result, function(shiftReqDto, key) {
              /*
               Set shift type id
               */
              //var shift = getShiftType(shiftReqDto.shiftTypeId);
              $scope.patternCalendar.currentShiftPattern.shifts.push(angular.copy(patternList.getShiftType(shiftReqDto.shiftTypeId)));
              $scope.patternCalendar.currentShiftPattern.shiftIds.push(shiftReqDto.shiftTypeId);
              $scope.patternCalendar.currentShiftPattern.hours.push({'required': shiftReqDto.employeeCount, 'excess': shiftReqDto.excessCount});
              //$scope.patternCalendar.currentShiftPattern.shiftLengths.push(patternList.getShiftLength(shiftReqDto.shiftLengthId));

            });

            /**
             * Update Option Pattern Type Value
             */
            if (!$scope.patternCalendar.currentShiftPattern.shifts || $scope.patternCalendar.currentShiftPattern.shifts.length ===0) {
              $scope.patternCalendar.option.demandPatternGenerated = false;
            }
            else {
              $scope.patternCalendar.option.demandPatternGenerated = true;
            }

            //update demands circle shifts graph area

            var ele = null;
            var j = 0;

            if ($scope.patternCalendar.option.demandPatternGenerated === true) {

              $timeout(function(){
                patternGraph.buildGraph($scope.patternCalendar.currentShiftPattern);

                patternList.clearDemands($scope.patternCalendar.currentShiftPattern, $scope.patternCalendar.currentShiftPattern.maxDemandCount);

                /**
                 * Apply shiftDemandDtos
                 */

                for (i=0; i<dto.shiftDemandDtos.length; i++) {

                  ele = dto.shiftDemandDtos[i];
                  var index = ele.startTime / ele.lengthInMin / 60 / 1000;

                  $scope.patternCalendar.currentShiftPattern.demandHeaderItems[index].value = ele.employeeCount;

                  for (j =0; j < $scope.patternCalendar.currentShiftPattern.maxDemandCount; j++) {

                    if (j< $scope.patternCalendar.currentShiftPattern.maxDemandCount - parseInt(ele.employeeCount)) {
                      $scope.patternCalendar.currentShiftPattern.demands[j].data[index] = 0; // empty
                    }
                    else {
                      $scope.patternCalendar.currentShiftPattern.demands[j].data[index] = 1; //show circle
                    }

                  }

                }

                /**
                 * compare requiredHeaderItems:ShiftReqDtos and demandHeaderItems:DemandHeaderItems
                 * and show dots in demands with yellow color
                 */

                for (i=0; i< $scope.graph.requiredHeaderItems.length; i++) {
                  var required = $scope.graph.requiredHeaderItems[i];
                  var demand = $scope.patternCalendar.currentShiftPattern.demandHeaderItems[i];
                  var diff = 0;

                  if (required.value > (demand.value || 0)) {
                    diff = required.value - (demand.value || 0);
                    /**
                     * Show yellow dots for the difference
                     */
                    var end = $scope.patternCalendar.currentShiftPattern.maxDemandCount - (demand.value || 0) ;
                    for (j =end -diff ; j < end; j++) {
                      $scope.patternCalendar.currentShiftPattern.demands[j].data[i] = 2; // empty
                    }


                  }
                }
              });

            }


          })
          .error(function(error) {
            applicationContext.setNotificationMsgWithValues(error.message, '', true, error.statusText);
          });
      };

      /**
       * return shifttype object with matching id of shiftlength
       */
      function getShiftTypeFromShiftLength(shiftLengthId) {

        for (var i =0; i<$scope.site.shiftTypes.length; i++) {

          var ele = $scope.site.shiftTypes[i];
          if (ele.shiftLengthId === shiftLengthId) {
            return ele;
          }

        }
        return null;
      }

      /**
       * Update ShiftDemandDtos -> allowable
       * if site == null, return blank
       */
      $scope.updateDemandShiftLengths = function(mouse) {
        
        // call Service function
        patternList.updateDemandShiftLengths(mouse);
        if (mouse === true) {
          $scope.patternCalendar.option.demandPatternGenerated = false;
        }
      };

      

      function setDefaultPatternInSkill() {
        var pattern = null;

        if ($scope.skill !==null && $scope.skill.generalPatterns.length > 0) {
          pattern = $scope.skill.generalPatterns[0];
        }
        else if ($scope.skill !==null  && $scope.skill.specificPatterns.length > 0) {
          pattern = $scope.skill.specificPatterns[0];
        }

        if (pattern === null) {
          patternList.assignEmptyShiftPattern();
        }
        else {
          patternList.selectShiftPattern(pattern);
        }

      }

      /**
       * Reset Sites Filter in dropdown box : ShiftPattern
       */
      $scope.resetSitesFilter = function() {

        if ($scope.sites.length > 0) {
          $scope.loadTeams($scope.sites[0], null);
        }
      };

      /**
       * Reset Teams Filter in dropdown box : ShiftPattern
       */
      $scope.resetTeamsFilter = function() {

        if ($scope.teams.length > 0) {
          $scope.loadSkills($scope.teams[0], null);
        }
      };

      /**
       * Reset Skills Filter in dropdown box : ShiftPattern
       */
      $scope.resetSkillsFilter = function() {

        if ($scope.skills.length > 0) {
          $scope.assignShiftPatterns($scope.skills[0]);
        }
      };


      /**
       * Shift Pattern Main Section
       * @param evt
       */
      $scope.filterZero = function(evt) {

        var elem = null;

        if (evt.which !== 48 && evt.which !== 96) {
          return;
        }

        if (evt.srcElement)
          elem = evt.srcElement;
        else if (evt.target)
          elem = evt.target;

        if (elem !== null ){
          if (elem.value.indexOf('0') === 0) {
            elem.value = Math.abs(parseInt(elem.value)) || 0;
          }
        }

      };

      /**
       * Saving Pattern on Save & Discard Modal by changing filters such as (Site,Team,Skill)
       */
      function saveOnModalFromChangingFilters(originalSite,originalTeam,originalSkill) {

        var working = applicationContext.getWorking();

        return working.saveFunc(originalSite, originalTeam, originalSkill)
          .then(
            function(result) {

              patternList.processAssignShiftPatterns({
                site: $scope.site,
                team: $scope.team,
                skill: $scope.skill
              });
              setDefaultPatternInSkill();
            },function (reason){

              /**
               * Change back the filters
               */

              restoreSite(originalSite);
              restoreTeam(originalTeam);
              restoreSkill(originalSkill);

            }
          );
      }

      /**
       * restoreSite filter in dropdown
       * @param originalSite
       */
      function restoreSite(originalSite) {
        if (originalSite && $scope.site && originalSite.id !== $scope.site.id) {

          $scope.site.ticked = false;
          $scope.site = findSiteInMetaStructure(originalSite);
          $scope.site.ticked = true;

        }
      }

      /**
       * restoreTeam filter in dropdown
       * @param originalTeam
       */
      function restoreTeam(originalTeam) {
        if (originalTeam && $scope.team && originalTeam.id !== $scope.team.id) {

          $scope.team.ticked = false;
          $scope.team = findTeam(originalTeam.id);
          $scope.team.ticked = true;

        }
      }

      /**
       * restoreSkill filter in dropdown
       * @param originalSkill
       */
      function restoreSkill(originalSkill) {
        if (originalSkill && $scope.skill && originalSkill.id !== $scope.skill.id) {
          $scope.skill.ticked = false;
          $scope.skill = findSkillInTeam($scope.team, originalSkill);
          $scope.skill.ticked = true;
        }

      }

      /**
       * updatePatternGraph
       */
      $scope.updatePatternGraph = function(index) {

        $scope.updateEditing();
        $scope.patternCalendar.currentShiftPattern.hours[index].required = Math.abs(parseInt($scope.patternCalendar.currentShiftPattern.hours[index].required)) || 0;
        $scope.patternCalendar.currentShiftPattern.hours[index].excess = Math.abs(parseInt($scope.patternCalendar.currentShiftPattern.hours[index].excess)) || 0;

        $scope.calculateTotals();
        patternList.generateShiftPattern();
      };


      $scope.updateShiftFromId = function(index) {
        $scope.patternCalendar.currentShiftPattern.shifts[index] = angular.copy(patternList.getShiftType($scope.patternCalendar.currentShiftPattern.shiftIds[index]));
      };

      $scope.setDemandPatternGenerated = function(value) {
        $scope.patternCalendar.option.demandPatternGenerated = value;
      };

      $scope.refreshWholeGraphs = function() {
        patternList.generateShiftPattern();
      };


      /**
       * Demand Shift lengths : Check
       */
      $scope.checkAllDemandShiftLengths = function() {
        patternList.checkAllDemandShiftLengths();
      };

      /**
       * Demand Shift lengths : Uncheck
       */
      $scope.uncheckAllDemandShiftLengths = function() {
        patternList.uncheckAllDemandShiftLengths();
      };

      /**
       * restrict shifttypes with shiftlength
       * @param shiftType
       */
      $scope.matchShiftTypes = function(shiftType, shiftLength){
        return shiftLength && shiftLength.id && shiftType && shiftType.shiftLengthId === shiftLength.id;
      };

      /**
       * Shift Pattern Save:
       * @param site:
       * @param team: if not specified, scope.team will be used
       * @param skill: if not specified, scope.skill will be used
       */

      function processSave(site,team,skill) {
        var deferred = $q.defer();

        if (!$scope.patternCalendar.currentShiftPatternDay.day) {
          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_ADD_SHIFT_PATTERN_UNDER_SPECIFIC_DAY', '', true);
          deferred.reject('Error');
          return deferred.promise;
        }

        /**
         * these variables are using in update/save scenario
         */
        var i,shift,hour,dt, dtFormat;
        var shiftLengths = [];
        var ele = null;

        /**
         * Check whether it contains null shift
         */
        var nullShift = _.find(
          $scope.patternCalendar.currentShiftPattern.shifts, function(shift){
            return shift === null || !shift.id;
          }
        );

        if (typeof nullShift !== 'undefined' && $scope.patternCalendar.currentShiftPattern.shifts.length>0) {
          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_A_SHIFT', '', true);
          deferred.reject('Error');
          return deferred.promise;
        }


        var emptyHour = _.find(
          $scope.patternCalendar.currentShiftPattern.hours, function(hour){
            return hour === null || !(hour.required>0 || hour.excess>0);
          }
        );

        if (typeof emptyHour !== 'undefined' && $scope.patternCalendar.currentShiftPattern.hours.length>0) {
          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_VALID_HOURS', '', true);
          deferred.reject('Error');
          return deferred.promise;
        }

        if ($scope.patternCalendar.currentShiftPattern.id) {
          /**
           * User can not update specific date nor day in general day
           */
          var updateDto = {
            name: $scope.patternCalendar.currentShiftPattern.name,
            shiftReqDtos: $scope.patternCalendar.currentShiftPattern.shiftReqDtos
          };

          if ($scope.patternCalendar.option.dayType === 'GENERAL') {
            updateDto.dayOfWeek = $scope.patternCalendar.currentShiftPatternDay.day;
          }
          else {
            if (!$scope.patternCalendar.option.cdDate) {
              applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SPECIFIC_DATE', '', true );
              deferred.reject('Error');
              return deferred.promise;
            }

            updateDto.cdDate = appFunc.getDateWithTimezone(
              $scope.patternCalendar.option.cdDate.getFullYear(),
              $scope.patternCalendar.option.cdDate.getMonth(),
              $scope.patternCalendar.option.cdDate.getDate(),
              $scope.site.timeZone
            ).getTime();

          }

          if ($scope.patternCalendar.option.patternType === 'Demand') { //demand based
            updateDto.type = 'Demand';

            /**
             * build shiftLengthList
             */
            shiftLengths = [];
            ele = null;

            for (i in $scope.patternCalendar.demandShiftLengths) {
              ele = $scope.patternCalendar.demandShiftLengths[i];
              if (ele.checked === true) {
                shiftLengths.push(ele.id);
              }
            }

            updateDto.shiftLengthList = shiftLengths.join();

            /**
             * build shiftDemandDtos
             */
            updateDto.shiftDemandDtos = [];
            for (i in $scope.patternCalendar.currentShiftPattern.demandHeaderItems) {
              ele = $scope.patternCalendar.currentShiftPattern.demandHeaderItems[i];
              if (ele.value > 0) {
                updateDto.shiftDemandDtos.push(
                  {
                    "startTime": i* demandCellMin * 60 * 1000,
                    "lengthInMin" : demandCellMin,
                    "employeeCount": ele.value
                  }
                );
              }
            }

          }
          else {
            //Manual Based Pattern
            updateDto.type = 'Set';
            updateDto.shiftReqDtos = [];

            /**
             * build shiftReqDto for shift pattern
             */
            for (i in $scope.patternCalendar.currentShiftPattern.shifts) {
              shift = $scope.patternCalendar.currentShiftPattern.shifts[i];
              hour = $scope.patternCalendar.currentShiftPattern.hours[i];
              updateDto.shiftReqDtos.push({
                'employeeCount': hour.required,
                'excessCount': hour.excess,
                'shiftTypeId': shift.id
              });
            }
          }


          return factory.updateElement('shiftpatterns',$scope.patternCalendar.currentShiftPattern.id, updateDto)
            .then(function (result){

              console.log("ShiftPattern Updated : " + result.id);

              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', '', true , result.name);

            }, function (error) {
              console.log(error);

              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
              deferred.reject('Error');
              return deferred.promise;
            });

        }
        else {
          /*
           Create ShiftPattern on db
           */
          if (!team) {
            team = getTeam();
          }

          if (!skill) {
            skill = getSkill();
          }

          if (!skill) {
            applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SELECT_A_SKILL', '', true);
            deferred.reject('Error');
            return deferred.promise;

          }

          var dto = {
            skillId: skill.id,
            teamId: team.id,
            name : $scope.patternCalendar.currentShiftPattern.name,
            updateDto: {
              description : '',
              shiftReqDtos: []
            }
          };

          /**
           * Put cdDate and dayOfWeek
           */
          if ($scope.patternCalendar.option.dayType === 'SPECIFIC') {
            if (!$scope.patternCalendar.option.cdDate) {
              applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SPECIFIC_DATE', '', true );
              deferred.reject('Error');
              return deferred.promise;
            }

            /**
             * Save the cdDate with site timezone
             */
            dto.updateDto.cdDate = appFunc.getDateWithTimezone(
              $scope.patternCalendar.option.cdDate.getFullYear(),
              $scope.patternCalendar.option.cdDate.getMonth(),
              $scope.patternCalendar.option.cdDate.getDate(),
              $scope.site.timeZone
            ).getTime();

          }
          else {
            dto.updateDto.dayOfWeek = $scope.patternCalendar.currentShiftPatternDay.day;
          }

          /**
           * Prepare param for creation for demand based and manual pattern
           */
          if ($scope.patternCalendar.option.patternType === 'Demand') { //demand based

            dto.updateDto.type = 'Demand';

            /**
             * build shiftLengthList
             */
            shiftLengths = [];
            ele = null;

            for (i in $scope.patternCalendar.demandShiftLengths) {
              ele = $scope.patternCalendar.demandShiftLengths[i];
              if (ele.checked === true) {
                shiftLengths.push(ele.id);
              }
            }

            dto.updateDto.shiftLengthList = shiftLengths.join();

            /**
             * build shiftDemandDtos
             */
            dto.updateDto.shiftDemandDtos = [];
            for (i in $scope.patternCalendar.currentShiftPattern.demandHeaderItems) {
              ele = $scope.patternCalendar.currentShiftPattern.demandHeaderItems[i];
              if (ele.value > 0) {
                dto.updateDto.shiftDemandDtos.push(
                  {
                    "startTime": i* demandCellMin * 60 * 1000,
                    "lengthInMin" : demandCellMin,
                    "employeeCount": ele.value
                  }
                );
              }
            }

            //"shiftDemandDtos":[{"startTime":0, "lengthInMin":30, "employeeCount":1}


          }
          else { // Manual Shift Pattern
            dto.updateDto.type = 'Set';

            /**
             * build shiftReqDto for shift pattern
             */
            dto.updateDto.shiftReqDtos = [];
            for (i in $scope.patternCalendar.currentShiftPattern.shifts) {
              shift = $scope.patternCalendar.currentShiftPattern.shifts[i];
              hour = $scope.patternCalendar.currentShiftPattern.hours[i];
              dto.updateDto.shiftReqDtos.push({
                'employeeCount': hour.required,
                'excessCount': hour.excess,
                'shiftTypeId': shift.id
              });
            }

          }


          return factory.createElement('shiftpatterns',dto)
            .then(function (result){

              applicationContext.setNotificationMsgWithValues(result.name + ' build', '', true);
              console.log("ShiftPattern Saved : " + result.id);

              return result;

            }, function (error) {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
              deferred.reject('Error');
              return deferred.promise;
            });
        }
      }

      // Listens for current Shift Pattern changed from shift-pattern-list directive
      //$scope.$watch('patternCalendar.currentShiftPattern', function(newValue, oldValue) {
      //
      //  /**
      //   * Update Option Pattern Type Value
      //   */
      //  if (!newValue.shifts || newValue.shifts.length ===0) {
      //    $scope.patternCalendar.option.demandPatternGenerated = false;
      //  }
      //  else {
      //    $scope.patternCalendar.option.demandPatternGenerated = true;
      //  }
      //
      //  if (!newValue.type) {
      //    $scope.patternCalendar.option.patternType = 'Set';
      //  }
      //  else {
      //    $scope.patternCalendar.option.patternType = newValue.type;
      //  }
      //
      //  console.log('old:' + oldValue.id + " new:" + newValue.id);
      //
      //  if (oldValue.id) {
      //
      //
      //    var oldPattern = patternList.getShiftPatternById(oldValue.id);
      //
      //    if (oldPattern !== null ) {
      //      oldPattern.selected = false;
      //    }
      //    else {
      //      oldValue.selected = false;
      //    }
      //
      //  }
      //  else {
      //    oldValue.selected = false;
      //  }
      //
      //  if (newValue.id) {
      //    var newPattern = patternList.getShiftPatternById(newValue.id);
      //    if (newPattern !== null ) {
      //      newPattern.selected = true;
      //    }
      //  }
      //  else {
      //    newValue.selected = true;
      //  }
      //
      //  clearDemands($scope.patternCalendar.currentShiftPattern.maxDemandCount);
      //  patternList.generateShiftPattern();
      //});

      /**
       * Listens for demand related attribute change
       */
      //$scope.$watch('patternCalendar.currentShiftPattern.shiftLengthList', function(newValue, oldValue) {
      //  patternList.generateShiftPattern();
      //});

      /**
       * Listens for demand yellow dots
       * Third parameter is true for value detection
       */
      //$scope.$watch('graph.requiredHeaderItems', function(newValue, oldValue) {
      //  patternList.generateShiftPattern();
      //}, true);


      /*
       Listen for required & excess change in select-shifts area
      Third parameter is true for value detection
       */

//      $scope.$watch('patternCalendar.currentShiftPattern.hours', function(newValue, oldValue) {
//        $scope.calculateTotals();
//        patternList.generateShiftPattern();
//      }, true);

      //$scope.$watch('patternCalendar.currentShiftPattern.shifts', function(newValue, oldValue) {
      //  /**
      //   * Update Option Pattern Type Value
      //   */
      //  if (!$scope.patternCalendar.currentShiftPattern.shifts || $scope.patternCalendar.currentShiftPattern.shifts.length ===0) {
      //    $scope.patternCalendar.option.demandPatternGenerated = false;
      //  }
      //  else {
      //    $scope.patternCalendar.option.demandPatternGenerated = true;
      //  }
      //
      //  $scope.calculateTotals();
      //  patternList.generateShiftPattern();
      //}, true);

      $scope.toggledShiftPatternsDropdown = function(open) {
        console.log('Dropdown is now: ', open);
      };

    }
  ]
);
