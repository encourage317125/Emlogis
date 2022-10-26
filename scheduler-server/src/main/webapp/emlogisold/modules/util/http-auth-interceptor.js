/*global angular:true, browser:true */

/**
 * @license HTTP Auth Interceptor Module for AngularJS
 * (c) 2012 Witold Szczerba
 * License: MIT
 */
(function () {
  'use strict';

  angular.module('http-auth-interceptor', ['http-auth-interceptor-buffer'])

  .factory('authService', ['$rootScope','httpBuffer', function($rootScope, httpBuffer) {
    return {
      // empty session info by default.
      sessionInfo: {
        token: null,
        userName:null,
        permissions:{}, 
        roles:{}
      },


      /**
       * Call this function to indicate that authentication was successfull and trigger a
       * retry of all deferred requests.
       * @param data an optional argument to pass on to $broadcast which may be useful for
       * example if you need to pass through details of the user that was logged in
       */
      loginConfirmed: function(data, configUpdater) {
        console.log('--> loginConfirmed() token:' + data.token + '/' + data.sessionId);

        // save session information
        this.sessionInfo = data;
        // workaround for Yuriy's chnages
        if( data.token === undefined){
          this.sessionInfo.token = data.sessionId;
        }

        var updater = configUpdater || function(config) {return config;};
        $rootScope.$broadcast('event:auth-loginConfirmed', data);
        httpBuffer.retryAll(updater);

      },

      /**
       * Call this function to indicate that authentication should not proceed.
       * All deferred requests will be abandoned or rejected (if reason is provided).
       * @param data an optional argument to pass on to $broadcast.
       * @param reason if provided, the requests are rejected; abandoned otherwise.
       */
      loginCancelled: function(data, reason) {
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
      logout: function(data, reason) {
        this.clearSessionInfo();
        httpBuffer.rejectAll(reason);
        $rootScope.$broadcast('event:auth-loginRequired', data);
      },

      clearSessionInfo: function(){

        this.sessionInfo = {
            token: null,
            userName:null,
            permissions:{}, 
            roles:{}
        };
      },

      getSessionInfo: function() {
        return this.sessionInfo;
      }, 

      getUserName: function() {
        return this.sessionInfo.userName;
      }, 

      getImpersonatingUserName: function() {
        return this.sessionInfo.impersonatingUserName;
      }, 

      getToken: function() {
        return this.sessionInfo.token;
      }, 

      hasPermission: function( perm) {
        if (Array.isArray(perm)) {
          return this.hasPermissionIn(perm);
        }
        return this.sessionInfo.permissions[perm];
      }, 

      hasPermissionIn: function(perms) {
        for (var i=0; i < perms.length; i++) {
          if (this.hasPermission(perms[i])) {
            return true;
          }
        } 
        return false;
      } 

    };
  }])

  /**
   * $http interceptor.
   * On 401 response (without 'ignoreAuthModule' option) stores the request
   * and broadcasts 'event:angular-auth-loginRequired'.
   */
  .config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push(['$rootScope', '$q', 'httpBuffer', 'authService', function($rootScope, $q, httpBuffer, authService) {
      return {
        // Request Interceptor
        request: function(config) {
            var deferred = $q.defer();
            // if defined, set our application token into header 
            try {
              var svc = authService;
              var token = authService.getToken();
              if ( token !== undefined) {
                config.headers.EmLogisToken = token;

                console.log('--> sending request' + config.url + ' with token:' + token);

              }
            } catch(e) {
                console.log('--> sending request without token '  + config.url);

            } 
            return config;
        }
      };
    }]);
    $httpProvider.interceptors.push(['$rootScope', '$q', 'httpBuffer', function($rootScope, $q, httpBuffer) {
      return {
        // Response Error Interceptor
        responseError: function(rejection) {

          console.log('--> Got Failure response: ' + rejection.status + ' for url:' + rejection.config.url);

          if (rejection.status === 401 && !rejection.config.ignoreAuthModule) {
            // remove current session information
            //authService.logout();

            var deferred = $q.defer();
            httpBuffer.append(rejection.config, deferred);
            $rootScope.$broadcast('event:auth-loginRequired', rejection);
            return deferred.promise;
          }
          if (rejection.status === 403 ) {
            // Show access forbidden 
            alert("Your privileges don't provide you access to this feature.");

            var deferred = $q.defer();
            return deferred.promise;
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

  .factory('httpBuffer', ['$injector', function($injector) {
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
      append: function(config, deferred) {
        buffer.push({
          config: config,
          deferred: deferred
        });
      },

      /**
       * Abandon or reject (if reason provided) all the buffered requests.
       */
      rejectAll: function(reason) {
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
      retryAll: function(updater) {
        for (var i = 0; i < buffer.length; ++i) {
          retryHttpRequest(updater(buffer[i].config), buffer[i].deferred);
        }
        buffer = [];
      }
    };
  }]);
})();

