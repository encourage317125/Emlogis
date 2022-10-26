angular.module('emlogis.reports')
  .service('ReportsService', ['$http', '$rootScope', '$q', 'applicationContext', 'authService', 'ReportsiHubService',
    function ($http, $rootScope, $q, applicationContext, authService, ReportsiHubService) {

      var reportsConfig = {};
      this.reportsConfigPromise = null;
      this.loadJsapiPromise = null;

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

      this.getAccountInfo = function () {
        return sendRequest('sessions/accountid', 'GET', null);
      };

      this.getReportsConfig = function () {

        return reportsConfig;
      };

      this.setReportsConfig = function (config) {
        reportsConfig = config;
      };

      this.getReportsConfig = function (groupId) {
        return _.find(reportsConfig.groups, function (group) {
          return group.id == groupId;
        });
      };

      this.getReportInfo = function (groupId, reportId) {
        var group = _.find(reportsConfig.groups, function (group) {
          return group.id == groupId;
        });
        var report = _.find(group.reports, function (report) {
          return report.id == reportId;
        });
        if (!report.ui && group.ui) {
          report.ui = group.ui;
        }
        if (!report.ui && !group.ui) {
          report.ui = reportsConfig.ui;
        }
        return report;
      };

      this.loadReportsConfig = function () {
        return $http.get('modules/reports/reports-config.json');
      };

      this.loginIntoIhub = function () {
        sendRequest('ihub/login', 'GET', {
        }).then(function (res) {
          authService.setihubSessionInfo(res.data);
          ReportsiHubService.getFolderId();
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true, err.statusText);
        });
      };

      this.getIhubProperties = function () {
        return sendRequest('ihub/ihubproperties', 'GET', null);
      };
    }
  ]);