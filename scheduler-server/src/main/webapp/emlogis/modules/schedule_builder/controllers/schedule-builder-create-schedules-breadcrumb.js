var scheduleBuilder = angular.module('emlogis.schedule_builder');

scheduleBuilder.controller('ScheduleBuilderCreateSchedulesBreadcrumbCtrl',

  [
    '$rootScope',
    '$scope',
    '$modal',
    'applicationContext',
    'scheduleService',
    'crudDataService',
    'appFunc',
    '$q',
  function($rootScope, $scope, $modal, applicationContext, scheduleService, crudDataService, appFunc, $q) {

    console.log('Schedule Builder Create breadcrumb controller');

    /**
     * this variable will be used in
     */
    $scope.shared = scheduleService.getShared();
    var factory = _.clone(crudDataService);

       /**
     * New Schedule
     */
    $scope.newScheduleAction = function() {

      var schedule = {};

      appFunc.getSaveWorkDlg()
        .then(function (reason) {
          var working = applicationContext.getWorking();

          if (reason === DISCARD || reason === SKIP) {
            if (working.option !==null)
              working.option.editing = false;
            return working.restoreFunc();
          }
          else if (reason === SAVE) {
            return working.saveFunc();
          }
        },
        function (reject) {
          console.log('Cancel pressed');
          return $q.reject(reject);
        })
        .then( function (result) {

          var dlg = $modal.open({
            templateUrl: 'modules/schedule_builder/partials/schedule_builder_create_schedules_new_modal.html',
            windowClass: 'schedule-builder-breadcrumb',
            controller: function($scope, $modalInstance, schedule, applicationContext) {

              $scope.schedule = schedule;
              $scope.days = [7, 14, 21, 28];
              $scope.dateOptions = {
                startingDay: 1 //Leave it Monday at the moment
              };

              $scope.site = null;

              // When we go to login page we have to dismiss the modal.
              $scope.$on('event:auth-loginRequired', function () {
                $modalInstance.dismiss('cancel');
              });

              // Shows start date dialog box
              $scope.showDate = function($event) {
                $event.preventDefault();
                $event.stopPropagation();

                $scope.startDateOpened = true;
              };

              /**
               * Enable only first day of the week based on site settings
               * @param date
               * @param mode
               */
              $scope.calendarDisabled = function(date, mode) {
//                return ( mode === 'day' && ( date.getDay() !== $scope.dateOptions.startingDay ));
                return false;
              };

              // CreateAction
              $scope.create = function() {

                /**
                 * Perform Validation
                 */
                if (!$scope.schedule.startDate) {
                  applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_START_DATE', '', true);
                  return;
                }

                if (!$scope.schedule.length) {
                  applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SCHEDULE_LENGTH', '', true);
                  return;
                }
                if (!$scope.schedule.name) {
                  applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SCHEDULE_NAME', '', true);
                  return;
                }

                /**
                 * Check the teams selection
                 * teams under different sites should not be selected
                 */

                var teams = [];

                for (var i in $scope.teams) {

                  var ele = $scope.teams[i];
                  if (ele.ticked === true) {
                    teams.push(ele);
                  }

                }

                if (teams.length <= 0) {
                  applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SCHEDULE_TEAMS', '', true);
                  return;
                }

                /**
                 * Move startdate of schedule to site's firstdayof the week
                 */

                var firstDayIntOfWeek = 0;

                if ($scope.site.firstDayOfWeek === 'MONDAY') {
                  firstDayIntOfWeek = 1;
                }
                else if ($scope.site.firstDayOfWeek === 'TUESDAY') {
                  firstDayIntOfWeek = 2;
                }
                else if ($scope.site.firstDayOfWeek === 'WEDNESDAY') {
                  firstDayIntOfWeek = 3;
                }
                else if ($scope.site.firstDayOfWeek === 'THURSDAY') {
                  firstDayIntOfWeek = 4;
                }
                else if ($scope.site.firstDayOfWeek === 'FRIDAY') {
                  firstDayIntOfWeek = 5;
                }
                else if ($scope.site.firstDayOfWeek === 'SATURDAY') {
                  firstDayIntOfWeek = 6;
                }

                if (firstDayIntOfWeek !== $scope.schedule.startDate.getDay()) {
                  var dateDiff = ($scope.schedule.startDate.getDay() + 7 - firstDayIntOfWeek) % 7;
                  var date = new Date($scope.schedule.startDate.getTime() - dateDiff * 24 * 60 * 60 * 1000);
                  $scope.schedule.startDate = date;
                }


                $scope.schedule.teams = teams;
                $scope.schedule.site = $scope.site;

                $modalInstance.close($scope.schedule);
              };

              // Close Modal
              $scope.close = function(){
                $modalInstance.dismiss('cancel');
              };

              /**
               * Load all sites
               */
              function loadSites() {

                return factory.getElements('sites?limit=0&orderby=name&orderdir=ASC',{})
                  .then(function(entities){

                    /**
                     * resolves sites
                     */
                    return entities.data;
                  });
              }

              /**
               * Load all teams for the site
               */
              function loadTeams(site) {
                return factory.getElements('sites/' + site.id + '/teams?limit=0&orderby=name&orderdir=ASC', {})
                  .then(function(entities) {

                    $scope.site = site;
                    /**
                     * return teams;
                     */
                    return entities.data;
                  });
              }

              $scope.loadTeamsAction = function(site) {
                loadTeams(site)
                  .then(function(teams){

                    $scope.teams = [];
                    var ticked = true;

                    /**
                     * build $scope.sites
                     */
                    for(var i in teams){
                      if (i > 0) {
                        ticked = false;
                      }
                      $scope.teams.push(
                        {id: teams[i].id, name: teams[i].name, ticked: ticked}
                      );
                    }
                  });
              };


              /**
               * Initial Loading
               */

              loadSites()
                .then(function(sites) {

                  $scope.sites = [];
                  var ticked = true;
                  /**
                   * build $scope.sites
                   */
                  for(var i in sites){
                    if (i > 0){
                      ticked = false;
                    }
                    $scope.sites.push(
                      {
                        id: sites[i].id,
                        name: sites[i].name,
                        ticked: ticked,
                        firstDayOfWeek: sites[i].firstDayOfWeek,
                        timeZone: sites[i].timeZone
                      }
                    );
                  }

                  var site = null;
                  if (sites.length >0 ) {
                    site = sites[0];
                    return loadTeams(site);
                  }

                  return null;

                })
                .then(function(teams) {

                  $scope.teams = [];
                  var ticked = true;

                  /**
                   * build $scope.sites
                   */
                  for(var i in teams){
                    if (i > 0) {
                      ticked = false;
                    }
                    $scope.teams.push(
                      {id: teams[i].id, name: teams[i].name, ticked: ticked}
                    );
                  }

                });

            },
            resolve: {
              schedule: function() {
                return schedule;
              }
            }

          });

          /**
           * Initialize Variables
           */

          dlg.result.then(function(schedule){
            /**
             * Broadcast create New Schedule Event
             */

            $rootScope.$broadcast('event:schedule-builder-new-schedule', {schedule: schedule });

          }, function(reason) {
            console.log('dismissed');
          });
        })
      ;








    };

  }
]);
