/*global angular:true, browser:true */

/**
 * @license HTTP Auth Interceptor Module for AngularJS
 * (c) 2012 Witold Szczerba
 * License: MIT
 */
(function () {
  'use strict';

  angular.module('http-auth-interceptor', ['http-auth-interceptor-buffer'])

    .factory('authService', ['$rootScope', '$sessionStorage', 'httpBuffer', 'applicationContext',
      function ($rootScope, $sessionStorage, httpBuffer, applicationContext) {
        return {
          // empty session info by default.
          sessionInfo: {
            token: null,
            userName: null,
            permissions: {},
            roles: {}
          },

          ihubSessionInfo: {
            AuthId: null
          },

          /**
           * Call this function to indicate that authentication was successful and trigger a
           * retry of all deferred requests.
           * @param data an optional argument to pass on to $broadcast which may be useful for
           * example if you need to pass through details of the user that was logged in
           */
          loginConfirmed: function (data, configUpdater) {
            console.log('--> loginConfirmed() token:' + data.token + '/' + data.sessionId);


            // It is recommended to save the object as json format.
            /**
             * don't need lang, it causes some trouble
             */
//        delete data.lang;
            $sessionStorage.info = JSON.stringify(data);

            $sessionStorage.username = data.userName;
            var working = applicationContext.getWorking();
            working.option = null;

            // save session information
            this.sessionInfo = data;

            // workaround for Yuriy's chnages
            if (data.token === undefined) {
              this.sessionInfo.token = data.sessionId;
            }

            var updater = configUpdater || function (config) {
                return config;
              };

            // Do the normal steps
            $rootScope.$broadcast('event:auth-loginConfirmed', data);
            httpBuffer.retryAll(updater);
          },
          isLoggedIn: function () {
            return !_.isEmpty($sessionStorage.info);
          },


          /**
           * Call this function to indicate that authentication should not proceed.
           * All deferred requests will be abandoned or rejected (if reason is provided).
           * @param data an optional argument to pass on to $broadcast.
           * @param reason if provided, the requests are rejected; abandoned otherwise.
           */
          loginCancelled: function (data, reason) {

            this.clearSessionInfo();
            httpBuffer.rejectAll(reason);
            $rootScope.$broadcast('event:auth-loginCancelled', data);
          },

          /**
           * Call this function to indicate that logout has happened and login screen should be displayed again
           * All deferred requests will be abandoned or rejected (if reason is provided).
           * @param data an optional argument to pass on to $broadcast.
           * @param reason if provided, the requests are rejected; abandoned otherwise.
           */
          logout: function (data, reason) {
            this.clearSessionInfo();

            // destruct all $session variables
            delete $sessionStorage.info;

            // Clear sseService
            if ($rootScope.sseService !== null) {
              $rootScope.sseService.close();
              $rootScope.sseService = null;

            }

            // Stop Heart bit after logs out

            if ($rootScope.heartbitWorker !== null) {
              // Heartbit work already active, so let's stop it first

              $rootScope.heartbitWorker.terminate();
              $rootScope.heartbitWorker = null;
            }


            // clear applicationContext
            // Clear impersonated status to null so that it will be used for new session
            $sessionStorage.impersonated = null;


            applicationContext.setUsername("");


            httpBuffer.rejectAll(reason);
            $rootScope.$broadcast('event:auth-loginRequired', data);
          },
          clearSessionInfo: function () {
            this.sessionInfo = {
              token: null,
              userName: null,
              permissions: {},
              roles: {}
            };
          },
          getSessionInfo: function () {
//        if (this.sessionInfo.token === null) {
//          this.sessionInfo = JSON.parse($sessionStorage.info);
//        }
            return this.sessionInfo;
          },
          setSessionInfo: function (_sessionInfo) {
            this.sessionInfo = _sessionInfo;
            $sessionStorage.info = JSON.stringify(this.sessionInfo);
          },
          getUserName: function () {
            return this.sessionInfo.userName;
          },
          getImpersonatingUserName: function () {
            return this.sessionInfo.impersonatingUserName;
          },
          getToken: function () {
            return this.sessionInfo.token;
          },
          hasPermission: function (perm) {
            if (Array.isArray(perm)) {
              return this.hasPermissionIn(perm);
            }
            return this.sessionInfo.permissions[perm];
          },
          hasPermissionIn: function (perms) {
            for (var i = 0; i < perms.length; i++) {
              if (this.hasPermission(perms[i])) {
                return true;
              }
            }
            return false;
          },

          isUserAnEmployee: function () {
            return !_.isEmpty(this.getSessionInfo().employeeId);
          },

          isSchedulableEmployee: function () {
            return this.getSessionInfo().schedulableEmployee;
          },

          isTenantType: function (tenantType) {
            return this.getSessionInfo().tenantType === tenantType;
          },

          setihubSessionInfo: function (ihubSessionInfo) {
            this.ihubSessionInfo = ihubSessionInfo;
          },

          getihubAuthId: function () {
            return this.ihubSessionInfo.AuthId;
          }

        };
      }])

  /**
   * $http interceptor.
   * On 401 response (without 'ignoreAuthModule' option) stores the request
   * and broadcasts 'event:angular-auth-loginRequired'.
   */
    .config(['$httpProvider', function ($httpProvider) {
      $httpProvider.interceptors.push(['$rootScope', '$q', 'httpBuffer', 'authService', function ($rootScope, $q, httpBuffer, authService) {
        return {
          // Request Interceptor
          request: function (config) {
            var deferred = $q.defer();

            // if defined, set our application token into header 
            try {
              var svc = authService;
              var token = authService.getToken();
              if (token !== undefined) {
                config.headers.EmLogisToken = token;
              }
            } catch (e) {
              //console.log('--> sending request without token '  + config.url);
            }
            return config;
          }
        };
      }]);

      $httpProvider.interceptors.push(['$rootScope', '$sessionStorage', '$q', 'httpBuffer', 'authService', 'applicationContext',
        function ($rootScope, $sessionStorage, $q, httpBuffer, authService, applicationContext) {
          return {
            // Response Error Interceptor
            responseError: function (rejection) {

              //console.log('--> Got Failure response: ' + rejection.status + ' for url:' + rejection.config.url);
              var deferred = false;
              if (rejection.status === 401 && !rejection.config.ignoreAuthModule) {

                var msg = applicationContext.getNotificationMsg();
                msg.type = 'login';
                msg.content = 'Please login to access this resource.';
                msg.visible = true;
                applicationContext.setNotificationMsg(msg);

                // remove current session information
                //  authService.logout();

//                deferred = $q.defer();
                //httpBuffer.append(rejection.config, deferred);
                // $rootScope.$broadcast('event:auth-loginRequired', rejection);
//                return deferred.promise;

                // default behaviour
                return $q.reject(rejection);
              }
              if (rejection.status === 403) {

                // Show access forbidden
                alert("Your privileges don't provide you access to this feature.");

                //deferred = $q.defer();
              //  $rootScope.$broadcast('event:auth-loginConfirmed', rejection);
              //  return deferred.promise;
                return $q.reject(rejection);
              }

              // otherwise, default behaviour
              return $q.reject(rejection);
            }
          };
        }]);
    }]);

  /**
   * Private module, a utility, required internally by 'http-auth-interceptor'.
   */
  angular.module('http-auth-interceptor-buffer', [])

    .factory('httpBuffer', ['$injector', function ($injector) {
      /** Holds all the requests, so they can be re-requested in future. */
      var buffer = [];

      /** Service initialized later because of circular dependency problem. */
      var $http;

      function retryHttpRequest(config, deferred) {
        function successCallback(response) {
          deferred.resolve(response);
        }

        function errorCallback(response) {
          deferred.reject(response);
        }

        $http = $http || $injector.get('$http');
        $http(config).then(successCallback, errorCallback);
      }

      return {
        /**
         * Appends HTTP request configuration object with deferred response attached to buffer.
         */
        append: function (config, deferred) {
          buffer.push({
            config: config,
            deferred: deferred
          });
        },

        /**
         * Abandon or reject (if reason provided) all the buffered requests.
         */
        rejectAll: function (reason) {
          if (reason) {
            for (var i = 0; i < buffer.length; ++i) {
              buffer[i].deferred.reject(reason);
            }
          }
          buffer = [];
        },

        /**
         * Retries all the buffered requests clears the buffer.
         */
        retryAll: function (updater) {
          for (var i = 0; i < buffer.length; ++i) {
            retryHttpRequest(updater(buffer[i].config), buffer[i].deferred);
          }
          buffer = [];
        }
      };
    }]);
})();

