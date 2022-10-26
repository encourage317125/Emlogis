var scheduleBuilder = angular.module('emlogis.schedule_builder');

scheduleBuilder.controller('ScheduleBuilderCreateSchedulesCtrl',
  [
    '$rootScope',
    '$scope',
    '$state',
    'applicationContext',
    'appFunc',
    'crudDataService',
    'dataService',
    '$modal',
    '$q',
    '$http',
    '$timeout',
    '$translate',
    '$stateParams',
    'scheduleService',
    'sseService',
    'dialogs',
    'uiGridConstants',
    function(
      $rootScope,
      $scope,
      $state,
      applicationContext,
      appFunc,
      crudDataService,
      dataService,
      $modal,
      $q,
      $http,
      $timeout,
      $translate,
      $stateParams,
      scheduleService,
      sseService,
      dialogs,
      uiGridConstants
      ) {

      console.log('Schedule Builder Create controller');

      $scope.tabs[0].active = true;
      angular.element('.eml-top-tabs').scope().tabChecked = true;

      var baseUrl = applicationContext.getBaseRestUrl();
      var factory = _.clone(crudDataService);

      $scope.option = {startDate: '', length: '', name: '', editing: false, calendar: [],
        teamsForFilter: [], teamsForSite: []
      };

      $scope.originOption = {startDate: '', length: '', name: '', editing: false, calendar: [],
        teamsForFilter: [], teamsForSite: []
      };

      $scope.daysInWeek = ['calendar.SUNDAY', 'calendar.MONDAY', 'calendar.TUESDAY', 'calendar.WEDNESDAY',
        'calendar.THURSDAY', 'calendar.FRIDAY', 'calendar.SATURDAY'];
      // days
      $scope.days = [7, 14, 21, 28];
      $scope.schedule = {};
      $scope.originSchedule = {};

//      $scope.dateOptions = {
//
//      };

      /**
       * Schedule Generating Variable
       * @type {{}}
       */
      $scope.generation = {};

      /**
       * Variables
       * 1. Shared.newScheduleOptionList: Used to create New Schedule
       * 3. $scope.availableShiftPatterns: shiftPatterns for the Site which can be used add shift pattern
       * 4. $scope.schedule: Schedule in main section
       * 5. $scope.site: The site which Schedule belongs
       * 6. $scope.option.teams: The teams which Schedule belongs
       * 7. $scope.schedule.patternElts: patternElts associated with the Schedule
       * 8. $scope.option.teamsForSite: The teams under site of schedule
       * 9. $scope.option.teamsForFilter: It will be initial copy of $scope.teams
       * 10. $scope.originSchedule: Original Schedule, can be used in restore
       */

      /**
       * Init Site / Team hierarchy
       */
      function initSiteTeamCollection() {
        //        loadSites().
        //          then(loadTeams).
        //          then(function(result) {
        //
        //            /**
        //             * We are safe to assign sites,teams to scope variable
        //             */
        //            $scope.sites = result.sites;
        //            $scope.teams = result.teams;
        //
        //            /**
        //             * Build site/teams relationship
        //             */
        //
        //          });


      }



      /**
       * it returns promise object of sites
       */
      function loadSites() {

        var deferred = $q.defer();

        factory.getElements("sites?limit=0&orderby=name&orderdir=ASC",{})
          .then(function (entities) {

            /**
             * resolves sites
             */
            deferred.resolve(entities.data);
          });

        return deferred.promise;
      }

      /**
       * Load all teams
       */
      function loadTeams(sites) {

        var deferred = $q.defer();

        factory.getElements('teams?limit=0&orderby=updated&orderdir=ASC', {})
          .then(function (entities) {

            /**
             * resolves teams
             */
            deferred.resolve({sites: sites, teams: entities.data});
          });
      }

      /**
       * accept list of shift patterns in create schedules module
       * generate shift pattern list to be used in quick pane
       */
      function buildShiftPatternsTree(shiftPatterns) {

        /**
         *
         * @type {Array}
         * it will contain site objects
         */
        $scope.shiftPatternsTree = [];

        angular.forEach($scope.treeMetaData, function (site) {

          /**
           * Insert New Site Node
           */
          $scope.shiftPatternsTree.push(site);
          site.teams = [];

          angular.forEach(site.children, function (team) {

            /**
             * Insert New Team Node
             */

            site.teams.push(team);
            team.skills = [];
            angular.forEach(team.children, function (skill) {

              /**
               * Insert New Skill Node
               */

              team.skills.push(skill);

              /**
               * Get Shift Patterns for general day
               */
              var generalPatterns = findShiftPatterns(shiftPatterns, site, team, skill, GENERAL);
              skill.generalPatterns = generalPatterns;

              /**
               * Load specific date patterns and organize by specific date
               */
              var specificPatterns = findShiftPatterns(shiftPatterns, site, team, skill, SPECIFIC);
              skill.specificPatterns = organizeSpecificPatterns(specificPatterns);

            });
          });


        });


      }

      /**
       * load Overall Structure of sites/teams/skills
       */
      function loadSiteTeamSkills() {

        var deferred = $q.defer();

        factory.getElements('sites/siteteamskills', {})
          .then(function (entities) {

            /**
             * resolve objects
             */
            deferred.resolve(entities.data);


          });

        return deferred.promise;
      }

      /**
       * call Api to retrieve teams which are associated with Schedule
       * @param scheduleId
       */
      function loadScheduleTeams(scheduleId) {
        var deferred = $q.defer();

        // No query parameters, it will show all shift patterns

        factory.getElements('schedules/' + scheduleId + '/teams?limit=0&orderby=name&orderdir=ASC', {})
          .then(function (entities) {

            deferred.resolve(entities.data);

          });

        return deferred.promise;
      }

      /**
       * call Api to retrieve shiftPatterns which are associated with Schedule
       * @param scheduleId
       */
      function loadSchedulePatternElts(scheduleId) {


        // No query parameters, it will show all shift patterns

        return factory.getElements('schedules/' + scheduleId + '/shiftpatterns?limit=0&orderby=name&orderdir=ASC', {});

      }

      /**
       * Load Shift Patterns, it will be used when user clicks 'Add Shift pattern'
       * Each shift pattern contains no shiftReqDto
       */
      function loadShiftPatterns() {


        // No query parameters, it will show all shift patterns
        return factory.getElements('shiftpatterns/list?limit=0&orderby=name&orderdir=ASC&filter=skill.isActive=true', {});
      }


      // Shows start date dialog box
      $scope.showDate = function ($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.startDateOpened = true;
      };


      // Load sites and teams into dropdown list
      // $scope.loadSites();
      // $scope.loadTeams();

      /**
       * This function will open settings dialog box
       */
      $scope.openSettings = function () {
        //Show Modal with settings for schedule, shift generation
        $modal.open({
          templateUrl: 'modules/schedule_builder/partials/schedule_builder_create_schedules_settings.html',
          controller: 'ScheduleBuilderCreateSettingsCtrl',
          windowClass: 'schedule-builder-create-settings',
          resolve: {
            selectedSchedule: function() {
              return $scope.schedule;
            }
          }
        });
      };

      $scope.openOverrides = function () {
        $modal.open({
          templateUrl: 'modules/common/partials/schedule-settings-overrides.html',
          controller: 'ScheduleBuilderCreateSettingsCtrl',
          windowClass: 'overrides-modal',
          resolve: {
            selectedSchedule: function() {
              return $scope.schedule;
            }
          }
        });
      };

      $scope.$on('event:schedule-builder-new-schedule', function (event, args) {

        var schedule = args.schedule;
        var newScheduleId = null;
        newSchedule(schedule)
          .then(function (result) {

            applicationContext.setNotificationMsgWithValues(result.name + ' build', '', true);
            console.log("Schedule Saved : " + result.id);
            newScheduleId = result.id;
            /**
             * Load Schedule
             */
            return $scope.loadSchedule(newScheduleId);

          }, function (error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
          })
        ;
      });

      /**
       * create new empty schedule
       * @param schedule
       */
      function newSchedule(schedule) {
        /**
         * Create New Schedule
         */
        /**
         * Save date type values site's timezone
         */

        var startDate = appFunc.getDateWithTimezone(
          schedule.startDate.getFullYear(),
          schedule.startDate.getMonth(),
          schedule.startDate.getDate(),
          schedule.site.timeZone
        );

//        var startDate = new Date(schedule.startDate);

        var dto = {
          startDate: startDate.getTime(),
          teamIds: [],
          scheduleType: 'ShiftPatternBased', // Constant
          scheduleLengthInDays: schedule.length,
          updateDto: {
            description: '',
            name: schedule.name
          }
        };

        for (var i in schedule.teams) {
          var team = schedule.teams[i];
          dto.teamIds.push(team.id);
        }

        return factory.createElement('schedules', dto);

      }

      /**
       * update schedule
       * name: contains schedule name / startDate, it can be used directly from save & save_as
       *
       * id: schedule id
       * @param schedule
       */
      function updateSchedule(id, option) {

        /**
         * Update Schedule & with shift patterns
         */

        /**
         * Save date type values site's timezone
         */

        var startDate = appFunc.getDateWithTimezone(
          option.startDate.getFullYear(),
          option.startDate.getMonth(),
          option.startDate.getDate(),
          $scope.site.timeZone
        );

        var dto = {
          scheduleUpdateDto: {},
          schedulePatternDtos: [],
          teamIds: []
        };

        for (var i in $scope.schedule.patternElts) {
          var patternElt = $scope.schedule.patternElts[i];
          dto.schedulePatternDtos.push({
              patternId: patternElt.patternId,
              cdDate: patternElt.cdDate,
              dayOffset: patternElt.dayOffset
            }
          );
        }

        dto.scheduleUpdateDto.name = option.name;
        dto.scheduleUpdateDto.startDate = startDate.getTime();
        dto.scheduleUpdateDto.scheduleLengthInDays = option.length;


        for (i in $scope.option.teams) {
          dto.teamIds.push($scope.option.teams[i].id);
        }

        return factory.updateElement('schedules', id + '/ops/fullupdate', dto);

      }

      /**
       * update Schedule Teams
       */
      function updateScheduleTeams() {
        /**
         * Update Schedule & with shift patterns
         */
        var teamsStr = $scope.option.teams.map(function (team) {
          return team.id;
        }).join(",");

        return $http.post(baseUrl + 'schedules/' + $scope.schedule.id + '/ops/setteams', teamsStr);
      }

      /**
       * Save Action from the main screen save button : create schedules
       */
      $scope.saveAction = function () {


        /**
         * Update Schedule & Save Patterns
         */
        return updateSchedule($scope.schedule.id, $scope.option)
          .then(function (result) {
            applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', '', true, result.name);
            /**
             * Load again
             */
            $scope.option.editing = false;
            return $scope.loadSchedule(result.id);
          },
          function (error) {

            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);

            var deferred = $q.defer();
            deferred.reject(error);
            return deferred.promise;
          }
        );

      };

      $scope.deleteAction = function () {

        if (!$scope.schedule) {
          applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_CREATE_ONE_SCHEDULE', '', true);
          return;
        }

        if ($scope.schedule.id) {

          return factory.deleteElement('schedules', $scope.schedule.id)
            .then(function (result) {

              applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', '', true);
              /**
               * Initialize Calendar, loadSchedules again
               */
              $scope.schedule = null;
              $scope.originSchedule = null;

              $scope.selectedTeamsForFilter.length = 0;

              $scope.option = {editing: false};
              $scope.originOption = {editing: false};
              $scope.option.teams = [];
              $scope.option.teamsForFilter = angular.copy($scope.option.teams);
//              loadSchedules()
//                .then(function (result) {
//                  $scope.schedules = result;
//
//                });


            }, function (error) {
              console.log(error);
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            });
        }

      };

      //      /**
      //       * Save existing $scope.schedule.patternElts
      //       * @param teams
      //       */
      //      function saveSchedulePatternElts() {
      //        /**
      //         * Save Schedule
      //         */
      //        var deferred = $q.defer();
      //        var startDate = new Date($scope.option.startDate);
      //
      //        var dto = [];
      //        for (var i in $scope.schedule.patternElts) {
      //          var patternElt = $scope.schedule.patternElts[i];
      //          dto.push({
      //              patternId: patternElt.patternId,
      //              cdDate: patternElt.cdDate
      //            }
      //          );
      //        }
      //
      //        return factory.updateElement('schedules', $scope.schedule.id + '/shiftpatterns',dto)
      //          .then(function (result){
      //            deferred.resolve(result);
      //          }, function (error) {
      //            deferred.reject(error);
      //          });
      //
      //      }

      //      /**
      //       * Handle several process of loading Schedule
      //       * @param schedule
      //       */
      //
      //      function handleLoadingSchedule(schedule) {
      //
      //      }

      function getTimeDiff(start, end) {
        var diff = {m: 0, s: 0};
        if (start === '-' || end === '-') {
          return diff;
        }
        if (start <= 0 || end <=0 || start > end) {
          return diff;
        }

        var d = parseInt((end-start) / 1000);

        diff.m = parseInt(d/60);
        diff.s = parseInt(d % 60);

        return diff;
      }

      /**
       * create schedule module
       * Load Schedule into main screen
       * @param schedule: schedule
       */
      $scope.loadSchedule = function (scheduleId) {


        return $http.get(baseUrl + 'schedules/' + scheduleId, {})
          .then(function (result) {

            $scope.option.editing = false;
            $scope.schedule = result.data;

            /**
             * Add a few more properties to schedule
             */

            $scope.schedule.totalExeTime = getTimeDiff($scope.schedule.executionStartDate, $scope.schedule.executionEndDate);


            var now = new Date();

            // not using timezone conversion because it is getting the differences
            $scope.generation.elapsedTime = getTimeDiff($scope.schedule.executionStartDate, now.getTime());
            $scope.generation.remainingTime = getTimeDiff(($scope.generation.elapsedTime.m * 60 + $scope.generation.elapsedTime.s)*1000, $scope.schedule.maxComputationTime*1000);

            /**
             * Share $scope.schedule with settings
             */
            var tmp = scheduleService.getShared();
            tmp.schedule = $scope.schedule;

            scheduleService.setShared(tmp);

            $scope.generation.display = true;

            $scope.originSchedule = angular.copy($scope.schedule);

            $scope.$emit('event:loadSchedule');

            return result.data;

          })
          .then(function (schedule) { //Load associated teams

            return loadScheduleTeams(schedule.id)
              .then(function (teams) {

                $scope.option.teams = [];

                /**
                 * build $scope.teams
                 */
                for (var i in teams) {

                  $scope.option.teams.push(
                    {id: teams[i].id, name: teams[i].name, ticked: true, active: teams[i].active}
                  );
                }

                $scope.option.teamsForFilter = angular.copy($scope.option.teams);


              });
          })
          .then(function () {
            /**
             *  Load teamsForSite of the schedule. iterate tree structure of shared
             *  Assume schedule has at least one team
             */


            var destTeam = $scope.option.teams[0];
            $scope.option.teamsForSite = [];

            var shared = scheduleService.getShared();

            for (var i in shared.sites) {
              var site = shared.sites[i];

              for (var j in site.children) {
                var team = site.children[j];
                if (typeof destTeam !== 'undefined' && destTeam.id === team.id) {


                  $scope.option.teamsForSite = [];
                  /**
                   * build $scope.teams
                   */
                  for (var k in site.children) {

                    for (var ind=0; ind < $scope.option.teamsForSite.length; ind++) {
                      if ($scope.option.teamsForSite[ind].name > site.children[k].name) {
                        break;
                      }
                    }

                    $scope.option.teamsForSite.splice(ind,0,{
                      id: site.children[k].id, name: site.children[k].name, ticked: true
                    });

                  }

                  return site;

                }
              }
            }

//            return site;

          })
          .then(function (site) { // Load Site Info

            // It will load Site detail object

            return $http.get(applicationContext.getBaseRestUrl() + 'sites/' + site.id, {})
              .then(function (result) {

                $scope.site = result.data;
                $scope.schedule.site = $scope.site;

                /**
                 * convert startDate to browser's time stack
                 */
                  $scope.schedule.startDate = appFunc.convertToBrowserTimezone($scope.schedule.startDate, $scope.site.timeZone);

                var daysOfWeek = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

                for (var i in daysOfWeek) {
                  if (daysOfWeek[i] === $scope.site.firstDayOfWeek) {
                    break;
                  }
                }

                /**
                 * Rebuild calendar headers
                 */
                $scope.daysInWeek = [];
                $scope.site.firstDayIntOfWeek = parseInt(i);
                for (i = 0; i < 7; i++) {
                  $scope.daysInWeek.push('calendar.' + daysOfWeek[(i + $scope.site.firstDayIntOfWeek) % 7]);
                }
              });

          })
          .then(function () { //Load associated shift patterns

            return loadSchedulePatternElts($scope.schedule.id);

          })
          .then(function (result) {

            $scope.schedule.patternElts = result.data;
            $scope.originSchedule.patternElts = angular.copy($scope.schedule.patternElts);

            /**
             * LoadShiftPatterns using /shiftpatterns/list and filter by siteId
             */
            return loadShiftPatterns()
              .then(function (result) {

                $scope.availableShiftPatterns = [];

                // set proper key & values;
                angular.forEach(result.data, function (shiftPattern, key) {

                  if (checkTeamExistsForShiftPattern(shiftPattern)) {
                    shiftPattern.id = shiftPattern.shiftPatternId;
                    shiftPattern.name = shiftPattern.shiftPatternName;
                    delete shiftPattern.shiftPatternId;
                    delete shiftPattern.shiftPatternName;

//                    $scope.availableShiftPatterns.push(shiftPattern);
                    /**
                     * Add shift patterns based on name sort
                     */

                    for (var i=0; i<$scope.availableShiftPatterns.length; i++) {
                      if ($scope.availableShiftPatterns[i].name > shiftPattern.name) {
                        break;
                      }
                    }

                    $scope.availableShiftPatterns.splice(i,0,shiftPattern);
                  }

                });

                // Check digest is in the progress

                //              $timeout(function() {
                //
                //                angular.element('#startDate').scope().$apply();
                //              }, 500);

                /**
                 * Update Main Section
                 */
                $scope.option.name = $scope.schedule.name;
                $scope.option.startDate = new Date($scope.schedule.startDate);

                var lenInWeek = Math.ceil($scope.schedule.scheduleLengthInDays / 7);

                $scope.option.length = $scope.days[lenInWeek - 1];

                $scope.originOption = angular.copy($scope.option);

                $scope.updateScheduleCalendar();

              });

          });

      };

      /**
       * checks whether this teamId belongs to $scope.teamsForSite or not
       * @param teamId
       */
      function checkTeamExistsForShiftPattern(shiftPattern) {

        for (var i in $scope.option.teamsForFilter) {
          var ele = $scope.option.teamsForFilter[i];
          if (ele.ticked === true && ele.id === shiftPattern.teamId) {
            return true;
          }
        }

        return false;
      }

      /**
       * check the condition when clicking add shift pattern
       * @param day
       * @param shiftPattern
       */
      $scope.isValidAvailableShiftPatternForDate = function (date, shiftPattern) {

        var generalDays = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

        if (checkTeamExistsForShiftPattern(shiftPattern) === false) {
          return false;
        }

        if (shiftPattern.shiftPatternCdDate !== null && shiftPattern.shiftPatternCdDate > 0) {

          var specificDate = new Date(shiftPattern.shiftPatternCdDate);
          if (specificDate.getYear() === date.getYear() && specificDate.getMonth() === date.getMonth() && specificDate.getDate() === date.getDate()) {
            return true;
          }
          else {
            return false;
          }
        }
        else {
          var generalDay = date.getDay();
          if (generalDays[generalDay] === shiftPattern.shiftPatternDayOfWeek) {
            /**
             * Check it is already included or not
             */
            if ($scope.schedule.patternElts) {
              for (var i=0; i < $scope.schedule.patternElts.length; i++) {
                var patternElt = $scope.schedule.patternElts[i];
                if (patternElt.patternId === shiftPattern.id) {

                  /**
                   * Checks the date as well
                   * @type {number}
                   */
                  var timeDiff = date.getTime() - $scope.option.startDate;
                  var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));

                  if (diffDays == patternElt.dayOffset) {
                    return false;
                  }
                }
              }
            }

            return true;
          }
          else {
            return false;
          }
        }
        return false;
      };

      /**
       * check the condition when clicking add shift pattern
       * @param day
       * @param shiftPattern
       */
      $scope.isEmptyValidAvailableShiftPatternForDate = function (date) {

        for (var i in $scope.availableShiftPatterns) {
          var ele = $scope.availableShiftPatterns[i];
          if ($scope.isValidAvailableShiftPatternForDate(date, ele) === true) {
            return false;
          }

        }
        return true;

      };

      /**
       * Check teamsForSite belongs to teams
       * @param team
       * @returns {boolean}
       */
      $scope.isNotContainedInTeams = function (team) {
        for (var i in $scope.option.teams) {
          var ele = $scope.option.teams[i];
          if (ele.id === team.id) {
            return false;
          }
        }

        return true;
      };

      /**
       * Add team to teams
       * @param team
       */
      $scope.addScheduleTeam = function (team) {

        $scope.option.teams.push(team);
        //$scope.dropDown.isopen = !$scope.dropDown.isopen;
        $scope.updateEditing();
      };

      /**
       * remove team from schedule.teams
       * @param team
       */
      $scope.removeScheduleTeam = function (team) {

        if ($scope.option.teams.length <= 1) {
          applicationContext.setNotificationMsgWithValues('schedule_builder.SCHEDULE_SHOULD_HAVE_AT_LEAST_ONE_TEAM', '', true);
          return;
        }

        var dlg = dialogs.confirm('app.PLEASE_CONFIRM', 'schedule_builder.TEAMS_WILL_BE_REMOVED_WILL_YOU_CONTINUE?');


        dlg.result.then(function (btn) {

          var i = $scope.option.teams.indexOf(team);
          if (i != -1) {
            $scope.option.teams.splice(i, 1);
          }
          //$scope.removeSchedulePatternEltOfTeam(team);
          $scope.updateEditing();

          }, function (btn) {
          /**
           * Rollback origin option
           */

          }
        );
      };

      /**
       * remove patternElt from schedule.patternElts
       * @param patternElt
       */
      $scope.removeSchedulePatternElt = function (patternElt) {

        var i = $scope.schedule.patternElts.indexOf(patternElt);
        if (i != -1) {
          $scope.schedule.patternElts.splice(i, 1);
        }

      };

      // remove the shift patterns according to option.teamsForFilter option
      $scope.delayedUpdateShiftPatterns = function() {

        $timeout(
          function() {

            for (var j = $scope.schedule.patternElts.length-1; j >= 0; j--)  {
              var patternElt = $scope.schedule.patternElts[j];

              if (typeof patternElt === "undefined") {
                return;
              }
              var existInFilterTeam = false;

              for (var i in $scope.option.teamsForFilter) {
                var team = $scope.option.teamsForFilter[i];

                if (team.ticked === true && team.name === patternElt.patternTeamName) {
                  existInFilterTeam = true;
                  break;
                }
              }

              if (existInFilterTeam === false) {
                $scope.schedule.patternElts.splice(j, 1);
              }
            }

          },
          1000
        );
      };

      //remove the schedule patterns which belongs to specfic team
      $scope.removeSchedulePatternEltOfTeam = function(team) {
        _.each($scope.schedule.patternElts, function(patternElt) {
          if (team.name === patternElt.patternTeamName) {
            var i = $scope.schedule.patternElts.indexOf(patternElt);
            if (i != -1) {
              $scope.schedule.patternElts.splice(i, 1);
            }
          }
        });
      };

      /**
       *
       * @param date
       * @param patternElt
       * @returns {boolean}
       */
      $scope.isValidScheduleShiftPatternForDate = function (date, patternElt) {

        var generalDays = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

        /**
         * Check cdDate is matching with PatternElt
         */

        /**
         * Check patternElt is belonging to teamsForFilter
         */
        var existInFilterTeam = false;

        for (var i in $scope.option.teamsForFilter) {
          var team = $scope.option.teamsForFilter[i];

          if (team.ticked === true && team.name === patternElt.patternTeamName) {
            existInFilterTeam = true;
            break;
          }
        }

        if (existInFilterTeam === false) {
          return false;
        }

        for (i = $scope.option.teamsForFilter; i >= 0; i--) {
          var ele = $scope.schedule.patternElts[i];
          if (ele.cdDate <= 0) { //This is dayOffset -based Pattern
            if (ele.dayOffset >= $scope.option.length) {
              $scope.schedule.patternElts.splice(i, 1);
            }
          }
        }

        if (patternElt.cdDate > 0) { //Calendar Specific Date

          var cdDate = new Date(patternElt.cdDate);
          if (cdDate.getYear() === date.getYear() && cdDate.getMonth() === date.getMonth() || cdDate.getDate() === date.getDate()) {
            return true;
          }
          else {
            return false;
          }
        }
        else {
          var timeDiff = date.getTime() - $scope.option.startDate;
          var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
          if (diffDays == patternElt.dayOffset) {
            return true;
          }
          else {
            return false;
          }
        }

        //        if (patternElt.patternCdDate !==null) {
        //
        //          var specificDate = new Date(patternElt.patternCdDate);
        //          if (specificDate.getYear() === date.getYear() && specificDate.getMonth() === date.getMonth() && specificDate.getDate() === date.getDate()) {
        //            return true;
        //          }
        //          else {
        //            return false;
        //          }
        //        }
        //        else {
        //          var generalDay = date.getDay();
        //          if (generalDays[generalDay] === patternElt.patternDayOfWeek) {
        //            return true;
        //          }
        //          else {
        //            return false;
        //          }
        //        }
        return false;
      };

      /**
       * Convert shiftpattern to patternElt and add to schedule.patternElts with updated attributes
       * @param date
       * @param shiftPattern
       */
      $scope.addShiftPattern = function (date, shiftPattern) {

        /**
         * create patternElt Obj
         */

        var patternElt = {};
        patternElt.cdDate = 0; //Calendar independent

        var timeDiff = date.getTime() - $scope.option.startDate.getTime(); //StartDate is integer value
        var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));

        patternElt.dayOffset = diffDays;
        patternElt.patternName = shiftPattern.name;
        patternElt.patternId = shiftPattern.id;
        patternElt.patternCdDate = shiftPattern.shiftPatternCdDate;
        patternElt.patternDayOfWeek = shiftPattern.shiftPatternDayOfWeek;
        patternElt.patternTeamName = shiftPattern.teamName;

        for (var ind=0; ind < $scope.schedule.patternElts.length; ind++) {
          if ($scope.schedule.patternElts[ind].patternName > patternElt.patternName) {
            break;
          }
        }

        $scope.schedule.patternElts.splice(ind,0,patternElt);

//        $scope.schedule.patternElts.push(patternElt);

      };

      $scope.updateOption = function () {

      };

      /**
       * Enable only first day of the week based on site settings
       * @param date
       * @param mode
       */
      $scope.calendarDisabled = function (date, mode) {

        var daysOfWeek = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
        if (!$scope.site)
          return false;

        return ( mode === 'day' && ( daysOfWeek[date.getDay()] !== $scope.site.firstDayOfWeek ));
      };

      /**
       * Build main screen calendar
       * @param totalWeek
       * @param firstDayOfWeek
       */
      function buildScheduleCalendar(totalWeek, firstDayOfWeek) {
        $scope.option.calendar = [];
        $scope.originOption.calendar = [];

        for (var i = 0; i < totalWeek; i++) {
          var week = [];
          for (var j = 0; j < 7; j++) {

            var date = new Date(firstDayOfWeek.getTime() + (i * 7 + j) * 24 * 60 * 60 * 1000);
            week.push(date);
          }
          $scope.option.calendar.push(week);
          $scope.originOption.calendar.push(week);
        }

      }

      /**
       * Show save as modal window.
       */
      $scope.saveAsAction = function () {

        var schedule = {name: ''};

        var dlg = $modal.open({
          templateUrl: 'modules/schedule_builder/partials/schedule_builder_create_schedules_save_as_modal.html',
          windowClass: 'schedule-builder',
          controller: function ($scope, $modalInstance, schedule, applicationContext) {

            $scope.schedule = schedule;
            // When we go to login page we have to dismiss the modal.
            $scope.$on('event:auth-loginRequired', function () {
              $modalInstance.dismiss('cancel');
            });

            // Save Action
            $scope.save = function () {
              $modalInstance.close($scope.schedule);
            };

            // Close Modal
            $scope.close = function () {
              $modalInstance.dismiss('cancel');
            };

          },
          resolve: {
            schedule: function () {
              return schedule;
            }
          }

        });

        dlg.result.then(function (schedule) {
          /**
           * Save As
           */
          var tmp = angular.copy($scope.schedule);
          tmp.name = schedule.name;

          var tmpOption = angular.copy($scope.option);
          tmpOption.name = schedule.name;

          newSchedule(tmp)
            .then(function (result) {

              return updateSchedule(result.id, tmpOption);

            }, function (error) {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            })
            .then(function (result) {
              $scope.option.editing = false;
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', '', true, result.name);

            })
            ;

          }, function (reason) {
            console.log('Save as dismissed');
          }
        );
      };

      /**
       * Show duplicate modal window.
       */
      $scope.duplicateAction = function () {

        var schedule = {name: $scope.option.name + ' - Copy'};
        var firstDayIntOfWeek = $scope.site.firstDayIntOfWeek;

        var dlg = $modal.open({
          templateUrl: 'modules/schedule_builder/partials/schedule_builder_create_schedules_duplicate_modal.html',
          windowClass: 'schedule-builder',
          controller: function ($scope, $modalInstance, schedule, applicationContext) {

            $scope.modes = [
              {
                display: 'Don’t carry Shifts over',
                value: 'NOASSIGNMENT'
              },
              {
                display: 'Carry over pre-assigned Shifts',
                value: 'PREASSIGNMENT'
              },
              {
                display: 'Carry over all Shifts',
                value: 'ALLASSIGNMENT'
              }
            ];

            $scope.schedule = schedule;
            $scope.schedule.mode = $scope.modes[0];
            $scope.dateOptions = {
              startingDay: firstDayIntOfWeek //Same as original schedule
            };

            // When we go to login page we have to dismiss the modal.
            $scope.$on('event:auth-loginRequired', function () {
              $modalInstance.dismiss('cancel');
            });

            // Duplicate Action
            $scope.duplicateAction = function () {
              $modalInstance.close($scope.schedule);
            };

            // Close Modal
            $scope.close = function () {
              $modalInstance.dismiss('cancel');
            };

            // Shows start date dialog box
            $scope.showDate = function ($event) {
              $event.preventDefault();
              $event.stopPropagation();
              $scope.startDateOpened = true;
            };


          },
          resolve: {
            schedule: function () {
              return schedule;
            }
          }

        });

        return dlg.result.then(function (schedule) {
            /**
             * Save As
             */
            var startDate = appFunc.getDateWithTimezone(
              schedule.startDate.getFullYear(),
              schedule.startDate.getMonth(),
              schedule.startDate.getDate(),
              $scope.site.timeZone
            );

            var dto= {
              name: schedule.name,
              startDate: startDate.getTime(),
              mode: schedule.mode.value
            };

            $http.post(baseUrl + 'schedules/' + $scope.schedule.id + '/ops/duplicate', dto)
              .then(function (result) {
                applicationContext.setNotificationMsgWithValues('app.DUPLICATED_SUCCESSFULLY', '', true, result.data.name);
                $state.go('authenticated.schedule_builder.create_schedules', {'id': result.data.id});
              })
              .catch(function(error) {
                if (error.message) {
                  applicationContext.setNotificationMsgWithValues(error.message, '', true, '');
                }
                else {
                  applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
                }
              });

          }, function (reason) {
            console.log('Save as dismissed');
          }
        );
      };



      /**
       * @param ui: true: it is from UI
       * Update schedule calendar in Main section
       */
      $scope.updateScheduleCalendar = function (ui) {

        /**
         * dirty checking
         */
        if (!$scope.option.startDate)
          return;

        if (ui === true ){
          $scope.option.editing = true;
        }

        if ($scope.option.length < $scope.originOption.length) {

          var dlg = dialogs.confirm('app.PLEASE_CONFIRM', 'schedule_builder.SCHEDULE_LENGTH_IS_SHRUNK.WILL_YOU_CONTINUE?');

          dlg.result.then(function (btn) {

            /**
             * Shrink patternElts
             */

            for (var i = $scope.schedule.patternElts.length - 1; i >= 0; i--) {
              var patternElt = $scope.schedule.patternElts[i];
              if (patternElt.cdDate <= 0) { //This is dayOffset -based Pattern
                if (patternElt.dayOffset >= $scope.option.length) {
                  $scope.schedule.patternElts.splice(i, 1);
                }
              }
            }

            var ret = reassignCalendarVariables();
            buildScheduleCalendar(ret.totalWeek, ret.firstDayOfWeek);

            $scope.originOption.length = $scope.option.length;


          }, function (btn) {
            /**
             * Rollback origin option
             */
            $scope.option.length = $scope.originOption.length;

            var ret = reassignCalendarVariables();
            buildScheduleCalendar(ret.totalWeek, ret.firstDayOfWeek);
          });

        }
        else {
          var ret = reassignCalendarVariables();
          buildScheduleCalendar(ret.totalWeek, ret.firstDayOfWeek);
        }

      };

      function reassignCalendarVariables() {

        var lenInWeek = Math.ceil($scope.option.length / 7);

        $scope.option.endDate = new Date($scope.option.startDate.getTime() + (( lenInWeek * 7 ) - 1) * 24 * 60 * 60 * 1000);

        /**
         * get First Day of the week
         */

        var day = $scope.option.startDate.getDay();
        var diff = day; // Start Day of week is Sunday


        var firstDayOfWeek = new Date($scope.option.startDate.getTime() - ((diff - $scope.site.firstDayIntOfWeek + 7) % 7) * 24 * 60 * 60 * 1000);

        var totalWeek = lenInWeek;
        if (day !== $scope.site.firstDayIntOfWeek) {
          totalWeek++;
        }

        return {totalWeek: totalWeek, firstDayOfWeek: firstDayOfWeek};
      }

      /**
       * It will show of 'save your work' dialog box and have a appropriate action: create schedules
       */
      function getSaveWorkDlg() {

        if ($scope.option.editing === false) {
          var deferred = $q.defer();

          deferred.resolve(SKIP);
          return deferred.promise;

        }
        else {

          var dlg = $modal.open({
            templateUrl: 'modules/_layouts/partials/authenticated_save_work_modal.html',
            windowClass: 'schedule-builder',
            controller: function ($scope, $modalInstance) {

              // When we go to login page we have to dismiss the modal.
              $scope.$on('event:auth-loginRequired', function () {
                $modalInstance.dismiss('cancel');
              });

              // Save Action
              $scope.save = function () {
                $modalInstance.close(SAVE);
              };

              // Discard Action
              $scope.discard = function () {
                $modalInstance.close(DISCARD);
              };

              // Close Modal
              $scope.close = function () {
                $modalInstance.dismiss('cancel');
              };


            }

          });

          return dlg.result.then(function (reason) {

            /**
             * here we just need Save, we don't need Discard, since it will be reloading anyway
             */
            if (reason === SAVE) {

              /**
               * Save, it will refresh every pattern.
               */
              return $scope.saveAction()
                .then(function (result) {
                  $scope.option.editing = false;
                  return reason;
                });

            }
            else if (reason === DISCARD) {
              /**
               * Rollback current pattern and proceed
               */
              $scope.option.editing = false;
              restoreOriginalSchedule();
              return reason;
            }

          }, function (reason) {

            console.log('dismissed');
            return $q.reject(reason);
          });
        }


      }

      /**
       * Generate/Execute a Schedule
       */
      $scope.executeScheduleAction = function () {

        getSaveWorkDlg().then(function (reason) {
          if (reason === DISCARD) {
            return;
          }
          console.log('Schedule Generation');

          $scope.generation.progress = 0;
          $scope.generation.hardScore = '';
          $scope.generation.mediumScore = '';
          $scope.generation.softScore = '';
          $scope.generation.progressInfo = '';

          var now = new Date();
          $scope.generation.elapsedTime = getTimeDiff($scope.schedule.executionStartDate, now.getTime());
          $scope.generation.remainingTime = getTimeDiff(($scope.generation.elapsedTime.m * 60 + $scope.generation.elapsedTime.s) * 1000, $scope.schedule.maxComputationTime * 1000);

          var dto = {
            maxComputationTime: $scope.schedule.maxComputationTime,
            maximumUnimprovedSecondsSpent: $scope.schedule.maximumUnimprovedSecondsSpent
          };

          dataService
            .executeSchedule($scope.schedule.id, dto)
            .then(function (elt) {
              console.log('--> executed[' + elt.name + ': ' + elt.id + ']');
              $scope.elt = elt;  // update view with updated schedule
            }, function (error) {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            });
        });

      };


      /*
       * Abort a Schedule Generation
       */
      $scope.abortGeneration = function () {

        dataService
          .abortSchedule($scope.schedule.id)
          .then(function (elt) {
            console.log('--> abort sent[' + elt.name + ': ' + elt.id + ']');
            $scope.elt = elt;  // update view with update schedule
          }, function (error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
          });
      };

      /**
       * Close Button of generation panel
       */
      $scope.hideGenerationPanel = function () {
        $scope.generation.display = false;
      };

      /**
       * Update Editing Status: Create Schedules
       */
      $scope.updateEditing = function () {
        if ($scope.option.editing !== true) {
          $scope.option.editing = true;
        }
      };

      /**
       * open schedule action, it will open up a dialog box: create schedules module
       */
      $scope.openScheduleAction = function() {

        getSaveWorkDlg().then(function (reason) {

          var dlg = $modal.open({
            templateUrl: 'scheduleSelectorModal.html',
            controller: 'ScheduleSelectorModalInstanceCtrl',
            size: 'lg',
            windowClass: 'schedules-modal',
            resolve: {
              checkboxModel: function() {
                return {
                  simulationGenerated: true,
                  production: false,
                  posted: false
                };
              },
              customFilter: function() {
                return {key: 'scheduleType', value: 'ShiftPatternBased'};
              }
            }
          });

          return dlg.result.then(function(selectedSchedule) {
            $state.go('authenticated.schedule_builder.create_schedules', {'id': selectedSchedule.id});
          }, function (reason) {
            console.log('dismissed');
            return $q.reject(reason);
          });

        });

      };



      /**
       * Redirect to Employee Schedule
       * @param schedule
       */
      $scope.goToEmployeeSchedule = function(schedule) {
        $state.go('authenticated.employeeSchedules.weekView.schedule', {'scheduleId': schedule.id});
      };

      /**
       * It
       */
      function restoreOriginalSchedule() {
        angular.copy($scope.originSchedule, $scope.schedule);
        angular.copy($scope.originOption, $scope.option);
        $scope.updateScheduleCalendar();
      }

      // register an event consumer to diplay the progression of the schedule execution
      // ideally we should unregister this listener when leaving this page.
      // possibly need to register a stateLeve event to do that

      //"<SysNotifications><jcso><><Schedule><Progress><bf2f0a13-b1c1-4db6-bd39-2dd351e39dd6>"

      sseService.registerConsumer({
        id: 'scheduleCtlrProgressHandler',
        selector: function (key) {
          if ($scope.schedule && $scope.schedule.id) {
            var keyselector = '<SysNotifications><.*><><Schedule><Progress><' + $scope.schedule.id + '>';
            var match = key.match(keyselector);
            console.log("Schedule Progress event:: " + key + " with: " + keyselector + " -> " + match);
            return key.match(keyselector);
          }
          return false;                    // for now, subscribe to all events
        },
        callback: function (key, serverEvent) {
          $scope.$apply(function () {     // use $scope.$apply to refresh the view
            console.log(serverEvent);
            $scope.generation.progress = serverEvent.progress;
            $scope.generation.hardScore = (serverEvent.hardScore == -999999 ? '' : serverEvent.hardScore);
            $scope.generation.mediumScore = (serverEvent.mediumScore == -999999 ? '' : serverEvent.mediumScore);
            $scope.generation.softScore = (serverEvent.softScore == -999999 ? '' : serverEvent.softScore);
            $scope.generation.progressInfo = serverEvent.msg;

            var now = new Date();
            $scope.generation.elapsedTime = getTimeDiff($scope.schedule.executionStartDate, now.getTime());
            $scope.generation.remainingTime = getTimeDiff(($scope.generation.elapsedTime.m * 60 + $scope.generation.elapsedTime.s) * 1000, $scope.schedule.maxComputationTime * 1000);
          });
        },
        scope: $scope,
        params: []
      });

      sseService.registerConsumer({
        id: 'scheduleCtlrUpdateHandler',
        selector: function (key) {
          if ($scope.schedule && $scope.schedule.id) {
            var keyselector = '<ObjLifecycle><.*><><Schedule><Update><' + $scope.schedule.id + '>';
            var match = key.match(keyselector);
            console.log("Schedule Update event: " + key + " with: " + keyselector + " -> " + match);
            return key.match(keyselector);
          }
          return false;                    // for now, subscribe to all events
        },
        callback: function (key, serverEvent) {
          $scope.$apply(function () {     // use $scope.$apply to refresh the view
            var updatedElt = serverEvent;
            // convert date attributes from longs to Js dates (done by dataservice layer normally)
            var dateAttributes = ['created', 'updated', 'startDate', 'endDate',
              'executionStartDate', 'requestSentDate', 'executionAckDate', 'responseReceivedDate', 'executionEndDate'
            ];

            for (var i = 0; i < dateAttributes.length; i++) {
              var dateAttr = dateAttributes[i];
              if (updatedElt[dateAttr] > 0) {
                $scope.schedule[dateAttr] = updatedElt[dateAttr];
              }
              else {
                $scope.schedule[dateAttr] = '-';
              }
            }

            var tmpPatternElts = $scope.schedule.patternElts;
            $scope.schedule = serverEvent;
            $scope.schedule.patternElts = tmpPatternElts;

            $scope.schedule.totalExeTime = getTimeDiff($scope.schedule.executionStartDate, $scope.schedule.executionEndDate);
            angular.copy($scope.schedule, $scope.originSchedule);
            console.log('update generation callback');
            console.log(serverEvent);
            // then update current elt in scope

          });
        },
        scope: $scope,
        params: []
      });

      /**
       * Load schedule from url with id
       */
      if ($stateParams.id && $stateParams.id !== '') {
        $scope.loadSchedule($stateParams.id);
      }
      else {
        //prevent to load selectorModal html
        $timeout( function(){ $scope.openScheduleAction(); }, 1000);
      }

      /**
       * Load tree structure of sites/teams/skills
       */
      loadSiteTeamSkills()
        .then(function (sites) {

          /**
           * the highest object levels are sites.
           */

          var tmp = scheduleService.getShared();
          tmp.sites = sites;

          /**
           * For the New schedule dropdown list it will show only new schedule dropdown
           */
          tmp.newScheduleOptionList = angular.copy(sites);
          tmp.option = $scope.option;
          scheduleService.setShared(tmp);
        });

      /**
       * Register call back function, for confirmation Dialog
       */
      var working = applicationContext.getWorking();

      working.option = $scope.option;
      working.restoreFunc = restoreOriginalSchedule;
      working.saveFunc = $scope.saveAction;
      working.entityName = 'schedule_builder.SCHEDULE';


    }
  ]

);
