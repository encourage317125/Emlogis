var profile = angular.module('emlogis.profile');

profile.controller('ProfileCtrl',
  [
    '$scope',
    '$rootScope',
    '$state',
    '$q',
    '$modal',
    'applicationContext',
    'appFunc',
    'crudDataService',
    'profileDetailService',
    'authService',
    function ($scope, $rootScope, $state, $q, $modal, applicationContext, appFunc,
              crudDataService, profileDetailService, authService) {

      console.log('Profile Controller');

      $scope.tabs = [
        {
          heading: "profile.EMPLOYEE_PROFILE",
          route: 'authenticated.profile.detail',
          active: false
        },
        {
          heading: "profile.PREFERENCES",
          route: 'authenticated.profile.preferences',
          active: false
        },
        {
          heading: "profile.CHANGE_PASSWORD",
          route: 'authenticated.profile.password',
          active: false
        }
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

        /**
         * It is tab library issue which shows confirmation dialog box twice, because active state is changed
         */
        if ($scope.tabChecked === true) {
          $scope.tabChecked = false;
          return;
        }

        /**
         * if create schedules are clicked, open with id
         */
        var param = null;
//        if (tab.route ==='authenticated.profile.detail' || tab.route ==='authenticated.schedule_builder.generation_report') {
//          var tmp = scheduleService.getShared();
//
//          if (tmp.schedule && tmp.schedule.id) {
//            param = {'id': tmp.schedule.id};
//          }
//        }

        /**
         * Profile: Tab Switch
         */
        appFunc.getSaveWorkDlg().then(function (reason) {
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
        $scope.tabs.forEach(function (tab) {
          tab.active = $scope.active(tab.route);
        });
      });

    }
  ]
);
