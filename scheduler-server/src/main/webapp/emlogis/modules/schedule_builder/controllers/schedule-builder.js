var scheduleBuilder = angular.module('emlogis.schedule_builder');

scheduleBuilder.controller('ScheduleBuilderCtrl',
  [
    '$scope',
    '$rootScope',
    '$state',
    '$q',
    '$modal',
    'applicationContext',
    'appFunc',
    'scheduleService',
    'stateManager',
    function ($scope, $rootScope, $state, $q, $modal, applicationContext, appFunc,
              scheduleService, stateManager) {

      console.log('Schedule Builder controller');

      $scope.hasReport = function () {
        var temp = scheduleService.getShared();
        //todo check this
        //return temp.schedule.executionEndDate > 0;
        return temp.schedule.requestGenerationDuration > 0;
      };

      $scope.tabs = [
        {
          heading: "schedule_builder.CREATE_SCHEDULES",
          route: 'authenticated.schedule_builder.create_schedules',
          active: false
        },
        {
          heading: "schedule_builder.SHIFT_PATTERNS",
          route: 'authenticated.schedule_builder.shift_patterns',
          active: false
        },
        {
          heading: "schedule_builder.SCHEDULE_GENERATION_REPORT",
          route: 'authenticated.schedule_builder.generation_report',
          active: false,
          hide: true
        }
        //{ heading: "schedule_builder.SHIFT_BIDDING", route: 'authenticated.schedule_builder.shift_bidding', active:false }
      ];

      /**
       * Custom variable to be prevent showing save_work dialog twice
       * @type {boolean}
       */
      $scope.tabChecked = false;

      /**
       * Tabset Tab Click LInk
       * @param tab
       */
      $scope.goState = function (tab) {
        console.log('Schedule builder Tab: goState');
        /**
         * It is tab library issue which shows confirmation dialog box twice, because active state is changed
         */
        //if ($scope.tabChecked === true) {
        //  $scope.tabChecked = false;
        //  return;
        //}

        //prevent double loading
        if (tab.route === $state.current.name) {
          return;
        }

        /**
         * if create schedules are clicked, open with id
         */
        var param = null;
        if (tab.route === 'authenticated.schedule_builder.create_schedules' || tab.route === 'authenticated.schedule_builder.generation_report') {
          var tmp = scheduleService.getShared();

          if (tmp.schedule && tmp.schedule.id) {
            param = {'id': tmp.schedule.id};
          }
          else {
            param = {'id': ''};
          }
        }

        /**
         * Schedule Builder: Tab Switch
         */
        appFunc.getSaveWorkDlg().then(
          function (reason) {
            var working = applicationContext.getWorking();

            if (reason === DISCARD || reason === SKIP) {
              if (working.option !== null)
                working.option.editing = false;
              $state.go(tab.route, param);
            }
            else if (reason === SAVE) {
              working.saveFunc()
                .then(function (result) {
                  $state.go(tab.route, param);
                }, function (error) {
                  console.log('Saving is failed because of error');
                  cancelTabSwitch();
                }
              );
            }
            else {
              /**
               * the last condition is discard which we don't need to care, because state will be changed
               */
              if (working.option !== null)
                working.option.editing = false;
              $state.go(tab.route, param);
            }
          },
          function (reject) {
            /**
             * Cancel Pressed, roll back the tab states
             */
            console.log('Cancel pressed');
            cancelTabSwitch();
          }
        );
      };

      /**
       * Cancel Tab Switch
       */
      function cancelTabSwitch() {
        if ($state.$current.name.indexOf('create_schedules') > -1) {
          $scope.tabs[0].active = true;
          $scope.tabs[1].active = false;
        }
        else {
          $scope.tabs[0].active = false;
          $scope.tabs[1].active = true;
        }
        $scope.tabChecked = true;
        console.log('Rollback Tab Switch');
      }

      $scope.active = function (route) {
        return $state.is(route);
      };

      $scope.$on("$stateChangeSuccess", function () {
        console.log('on $state change success: schedule builder');
        $scope.tabs.forEach(function (tab) {

          if (tab.active !== true) {
            tab.active = $scope.active(tab.route);
          }

        });
      });

      $scope.$on('event:loadSchedule', function (event, args) {
        $scope.tabs[2].hide = !$scope.hasReport();
      });
    }
  ]
);

/**
 * Create Schedule page has common objects between breadcrumb and main section,
 * and this scheduleService is serving that purpose
 */
scheduleBuilder.service('scheduleService', function () {

  /**
   * createOptionSites: will be used in New Schedule Dropdown
   * option: it includes Name, startDate, etc
   */
  var shared = {sites: [], newScheduleOptionList: [], option: {}, schedule: {}};
  return {
    getShared: function () {
      return shared;
    },
    setShared: function (value) {
      shared = value;
    }
  };
});

scheduleBuilder.filter('toTranslate', function () {
  return function (val, transaltePath) {
    return transaltePath + val;
  };
});