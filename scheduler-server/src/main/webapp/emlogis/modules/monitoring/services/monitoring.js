angular.module('emlogis.monitoring')
  .service('MonitoringService', ['$http', '$rootScope', '$q', 'applicationContext', '$interval',
    function ($http, $rootScope, $q, applicationContext, $interval) {
      var baseUrl = applicationContext.getBaseRestUrl();

      function sendRequest(urlPart, method, requestPayload) {
        var apiUrl = baseUrl + urlPart;
        var req = {
          method: method,
          url: apiUrl
        };
        if (method === 'POST' || method === 'PUT') {
          req.data = requestPayload;
        }
        return $http(req);
      }

      this.getEntityDetails = function (entityType, entityId) {
        var urlPart;
        switch (entityType) {
          case 'engine':
            urlPart = 'engines/' + entityId;
            break;
          case 'appserver':
            urlPart = 'appservers/' + entityId;
            break;
          case 'hazelcast':
            urlPart = 'systeminfo/hzinfo';
            break;
          case 'notifications':
            urlPart = 'systeminfo/notificationinfo';
            break;
        }
        return sendRequest(urlPart, 'GET', null);
      };

      this.getEntityList = function (entityType) {
        var urlPart;
        switch (entityType) {
          case 'engine':
            urlPart = 'engines';
            break;
          case 'appserver':
            urlPart = 'appservers';
            break;
          case 'session':
            urlPart = 'sessions';
            break;
          case 'dbpercustomer':
            urlPart = 'systeminfo/dbpercustomer';
            break;
        }
        return sendRequest(urlPart, 'GET', null);
      };

      this.getDbSummary = function () {
        return sendRequest('systeminfo/dbsummary', 'GET', null);
      };

      this.shutdownEngine = function (id) {
        return sendRequest('engines/' + id, 'POST', null);
      };

      var shared = {sessions: []};

      this.getShared = function () {
        return shared;
      };

      this.setShared = function (value) {
        shared = value;
      };

      this.setSessions = function (data) {
        shared.sessions = data;
      };

      this.getSessionByToken = function (token) {
        return _.find(shared.sessions, function (elem) {
          return elem.token === token;
        });
      };

      var stop;
      this.refresh = function (func, refreshInterval) {
        if (angular.isDefined(stop)) return;
        stop = $interval(func, refreshInterval * 1000);
      };

      this.stopRefresh = function () {
        if (angular.isDefined(stop)) {
          $interval.cancel(stop);
          stop = undefined;
        }
      };
    }]);