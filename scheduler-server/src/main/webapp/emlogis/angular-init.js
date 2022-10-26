'use strict';
/**
 * Global Constants
 * @type {string}
 */
var SAVE = 'SAVE';
var DISCARD = 'DISCARD';
var SKIP = 'SKIP';

// Main App declaration
// It loads library defined modules such as grid, translate, session and custom modules
var app = angular.module(
  'emlogis',
  ['ui.bootstrap', 'ui.router', 'ui.grid', 'isteven-multi-select', 'pascalprecht.translate',
    'ngStorage', 'ngMessages', 'ngAnimate',
    'emlogis.commonservices',
    'emlogis.commonDirectives',
    'emlogis.commonControllers',
    'ncy-angular-breadcrumb',
    'http-auth-interceptor',
    'emlogis.directives',
    'emlogis.impersonation',
    'emlogis.login',
    'emlogis.home',
    'emlogis.dashboard',
    'emlogis.reports',
    'emlogis.settings',
    'emlogis.admin',
    'emlogis.employeeSchedules',
    'emlogis.schedule_builder',
    'emlogis.employees',
    'emlogis.browser',
    'emlogis.monitoring',
    'emlogis.profile'
  ])
  .config(
  [
    '$stateProvider',
    '$urlRouterProvider',
    '$locationProvider',
    '$httpProvider',
    '$translateProvider',
    '$breadcrumbProvider',
    'dialogsProvider',
    'datepickerConfig',
    function ($stateProvider, $urlRouterProvider, $locationProvider,
              $httpProvider, $translateProvider, $breadcrumbProvider,
              dialogsProvider, datepickerConfig, event) {

      // remove weeks column from all datepickers in application (like in ui style guide)
      datepickerConfig.showWeeks = false;

      $urlRouterProvider.otherwise('/');

      // Specify Default Module Page
      $breadcrumbProvider.setOptions({
        templateUrl: 'modules/_layouts/partials/authenticated_breadcrumb.tpl.html'
      });

      // Default Url
      $stateProvider.
        state('index', {
          url: "/emlogis.html",
          templateUrl: "emlogis.html",
          abstract: true
        })
        // If the use already logs in (have token info) move to home page
        .state('guest', {
          abstract: true,
          templateUrl: "modules/_layouts/partials/guest.html",
          controller: function ($state, $sessionStorage) {
            if ($sessionStorage.info) {

              console.log("Existing Token ID Move to home page ");
              $state.go('authenticated.home');
            }

          }
        })
        .state('guest.login', {
          url: '/',
          views: {
            "content": {
              templateUrl: "modules/_main/partials/login.html",
              controller: 'LoginController'
            }
          }
        })
        .state('authenticated', {
          abstract: true,
          templateUrl: 'modules/_layouts/partials/authenticated.html',
          controller: function ($state, $sessionStorage, $rootScope, $scope,
                                authService, sseService, $location, applicationContext) {

            $scope.isLoggedIn = authService.isLoggedIn();
            var tokenId = authService.getToken();
            var baseurl = applicationContext.getBaseUrl();

            if ($rootScope.heartbitWorker == null) {
              // Session Lost pre existing heartbitworker object.
              // Try to recreate them
              $rootScope.startHeartBit(baseurl, tokenId);
            }


//                    if (( Object.keys($rootScope.consumers).length == 1 ) && ( $sessionStorage.consumers != null)){
//                        $rootScope.consumers = $sessionStorage.consumers;
//                    }

            // subscribe to SSE events
            if ($rootScope.sseServiceStarted != true) {
              sseService.startSSElistener(baseurl, tokenId);
              $rootScope.sseServiceStarted = true;
            }


            // Write Global Properties Username if empty, we have logged in,
            // We hit the refresh
            if (applicationContext.getUsername() == "") {
              applicationContext.setUsername($sessionStorage['username']);
            }

          }
        });

      // Language Translate Provider
      $translateProvider.useStaticFilesLoader({
        prefix: 'l10n/',
        suffix: '.json'
      });

      $translateProvider.preferredLanguage('en_US');

    }])
  .run([
    '$rootScope',
    '$state',
    '$location',
    '$q',
    '$modal',
    '$sessionStorage',
    'appContext',
    'authService',
    'applicationContext',
    'appFunc',
    'datepickerPopupConfig',
    '$templateCache',
    '$interpolate',
    '$translate',
    function ($rootScope, $state, $location, $q, $modal, $sessionStorage, appContext, authService,
              applicationContext, appFunc, datepickerPopupConfig, $templateCache, $interpolate, $translate) {

      $rootScope.hasStatePermissions = function(toState) {
        if (angular.isDefined(toState.data) && angular.isDefined(toState.data.permissions)) {
            return toState.data.permissions(authService);
        } else {
          return true;
        }
      };

      $rootScope.hasStatePermissionsByName = function(name) {
        if($state.get(name)) {
          return $rootScope.hasStatePermissions($state.get(name));
        } else {
          return false;
        }
      };

      $rootScope.authService = authService;

      /**
       * Check Login & Save Work Confirmation Dialog
       */
      $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {

        // / if route requires auth and user is not logged in

        if (( _.string.startsWith(toState.name, 'authenticated') === true) && (authService.isLoggedIn() !== true )) {
          // redirect back to login
          applicationContext.setAfterLoginUrl($location.path());
          event.preventDefault();
          $state.go('guest.login');
        }
        else if ($rootScope.hasStatePermissions(toState)) {

          var working = applicationContext.getWorking();

          var sessionInfo = authService.getSessionInfo();

          if (working.option && working.option.editing === true && sessionInfo.token !== null) {
            event.preventDefault();

            appFunc.getSaveWorkDlg().then(function (reason) {
                var working = applicationContext.getWorking();

                if (reason === DISCARD) {
                  if (working.option !== null)
                    working.option.editing = false;

                  $state.go(toState, toParams);

                }
                else if (reason === SAVE) {
                  working.saveFunc()
                    .then(function (result) {
                      $state.go(toState, toParams);
                    });
                }

              },
              function (reject) {
                console.log('Cancel pressed');
              }
            );
          }

        } else {
          event.preventDefault();
        }

      });


      /**
       *  Hide Page-level notification
       *  if used successfully navigated to another page
       */
      $rootScope.$on('$stateChangeSuccess', function () {
        var working = applicationContext.getWorking();
        var sessionInfo = authService.getSessionInfo();

        if (working.option && !working.option.editing && sessionInfo.token !== null) {
          var notificationMsg = applicationContext.getNotificationMsg();
          notificationMsg.content = '';
          notificationMsg.type = '';
          notificationMsg.visible = false;
          applicationContext.updateNotificationArea();
        }
      });


      // initialize entity2resource map
      // entity2resource is a map between an entity name, and its corresponding
      // REST resource and user friendly label
      // for instance, the entity2resource 'useraccount' attribute would have
      // value {restResource: "useraccounts", label: "User Account"}
      var entity2resource = appContext.get('entity2resource', {});
      _.defaults(entity2resource, {
        getResource: function (entity) {
          return this[entity].restResource;
        },
        getLabel: function (entity) {
          return this[entity].label;
        }
      });


      // Default transition state
      $rootScope.$on('event:auth-loginRequired', function (event) {
        event.preventDefault();
        $state.go('guest.login');

      });

      // if the session storage token id is not valid

      if ($sessionStorage.info) {
        //var sessionInfo = authService.getSessionInfo();
        var sessionInfo = JSON.parse($sessionStorage.info);

        authService.setSessionInfo(sessionInfo);

        //console.log("Refresh Token ID : " + sessionInfo.token);
      }
      else {
        $rootScope.$broadcast('event:auth-loginRequired', null);

      }

      // Tell the module what language to use by default
      if (!$sessionStorage['lang']) {
        $sessionStorage['lang'] = 'en_US';
      }


      $translate.use($sessionStorage['lang']);


      var startSym = $interpolate.startSymbol();
      var endSym = $interpolate.endSymbol();

      $templateCache.put('/dialogs/confirm.html', '<div class="modal-header dialog-header-confirm">' +
      '<button type="button" class="close" ng-click="no()">&times;</button><h4 class="modal-title">' +
      '<span class= "glyphicon glyphicon-check"></span> <span translate>' + startSym + 'header' + endSym +
      '</span></h4></div><div class="modal-body" ><span translate>' + startSym + 'msg' + endSym +
      '</span></div><div class="modal-footer"><button type="button" class="btn btn-default" ng-click="yes()">' +
      startSym + '"DIALOGS_YES" | translate' + endSym + '</button><button type="button" class="btn btn-primary" ' +
      'ng-click="no()">' + startSym + '"DIALOGS_NO" | translate' + endSym + '</button></div>');


      // Modify SelectAll header cell template in UI Grid
      // to add a tooltip

      $templateCache.put('ui-grid/selectionHeaderCell', '<div tooltip="Select all visible rows" tooltip-append-to-body="true" tooltip-placement="right">' +
                                                          '<div class="ui-grid-cell-contents" col-index="renderIndex">' +
                                                            '<ui-grid-selection-select-all-buttons ng-if="grid.options.enableSelectAll"></ui-grid-selection-select-all-buttons>' +
                                                          '</div>' +
                                                        '</div>');
    }]
  )

  //--------------------------------------------------------------------
  // Constants
  //--------------------------------------------------------------------

  .constant('emlogisConstants', {
    countries: [
      "USA",
      "Canada",
      "Philippines"
    ]
  })
;

app.controller('AppCtrl', [
  '$rootScope',
  '$scope',
  '$location',
  '$state',
  '$http',
  '$translate',
  '$sessionStorage',
  'stateManager',
  'authService',
  'appFunc',
  'sseService',
  'applicationContext',
  function ($rootScope, $scope, $location, $state, $http, $translate, $sessionStorage, stateManager,
            authService, appFunc, sseService, applicationContext) {

    $rootScope.heartbitWorker = null;

    // register a listener on login success to activate SSE
    $scope.$on('event:auth-loginConfirmed', function (event) {

      // we just logged in, subscribe to SSE events and send a
      // heartbit periodically
      // get session token and build base url for SSE subscribtion &
      // heartbit
      var tokenId = authService.getToken();
      var baseurl = applicationContext.getBaseUrl();

      /*
       *    $sessionStorage['impersonated'] has 3 statuses
       *   null : user just logs in
       *   true : impersonated to other user
       *   false : unimpersonated back to origin user
       */

      if ($sessionStorage['impersonated'] == null) {
        // subscribe to SSE events
        sseService.startSSElistener(baseurl, tokenId);
        $rootScope.sseServiceStarted = true;

        // start a webworker to send a heartbit to server every min
        $rootScope.startHeartBit(baseurl, tokenId);
      }


      // Move to authenticated page.

      if (($sessionStorage['impersonated'] != null) && ($state.current.name == 'authenticated.home')) {

        $state.go($state.current, null, {
          reload: true,
          inherit: false,
          notify: true
        });
      }
      else {
        if (applicationContext.getAfterLoginUrl() === '') {
          event.preventDefault();
          $state.go('authenticated.home');
        }
        else {

          $location.path(applicationContext.getAfterLoginUrl());
          applicationContext.setAfterLoginUrl('');
          console.log("After Login Url empty");
        }

      }

      /**
       * Clear the Notification Area
       */
      applicationContext.setNotificationMsgWithValues('', '', false);


    });

    $rootScope.startHeartBit = function (baseurl, tokenId) {

      if (typeof (Worker) !== "undefined") {
        // Web Workers supported
        if ($rootScope.heartbitWorker != null) {
          // Heartbit work lready active, so let's stop it first
          $rootScope.heartbitWorker.terminate();
          $rootScope.heartbitWorker = null;
        }

        $rootScope.heartbitWorker = new Worker("./heartbit.js");

        // Implement message failure from heartbit.

        $rootScope.heartbitWorker.addEventListener('message', function (e) {

          // In case function called more than in a row, we should show message only one time

          if ($sessionStorage.info != null) {
//                            delete  $sessionStorage.info;
//                            applicationContext.setUsername("");

            //alert(e.data);

            applicationContext.setNotificationMsgWithValues(e.data, 'danger', true, '');

            //$rootScope.logout();

            return;
          }


        }, false);

        var url = baseurl + 'sseheartbit?tokenId=' + tokenId;

        $rootScope.heartbitWorker.postMessage(url);
      }
    };

    // Change Language Settings
    $scope.setLang = function (langKey) {
      // We can change the language during runtime
      $translate.use(langKey);
      $sessionStorage['lang'] = langKey;

    };

    // Define Logout Function as globally

    $rootScope.logout = function (param) {
      var token = authService.getSessionInfo().token;

      if (token != null) {
        //stop badge pulling
        appFunc.stopBadgeRefresh();
        applicationContext.setBadgeMsg({});

        // If lost connection, will not call delete api
        if (param != null && param.lostConnection == true) {
          authService.logout(null, "CONNECTION_REFUSED");
        }
        else {
          $http.delete('../emlogis/rest/sessions')
            .success(function (data) {
              console.log('--> logout successful() token:' + data.token);
              authService.logout();
            })
            .error(function () {
              console.log('--> logout FAILED()');
              authService.logout();
            });
        }

      }
    };


    stateManager.onStateChangeSuccess();


  }

]);

app.controller('AlertsCtrl', ['$scope', 'alertsManager', '$timeout', function ($scope, alertsManager, $timeout) {
  $scope.alerts = alertsManager.alerts;
  $scope.closeAlert = function (index) {
    alertsManager.closeAlert(index);
  }
}]);