var home = angular.module('emlogis.reports');

home.controller('ReportsCtrl', [
  '$scope',
  '$state',
  'ReportsiHubService',
  'ReportsService',
  'applicationContext',
  'angularLoad',
  function ($scope,
            $state,
            ReportsiHubService,
            ReportsService,
            applicationContext,
            angularLoad) {

    $scope.tabs = [];

    $scope.goState = function (tab) {
      $state.go.apply(this, tab.route);
    };

    $scope.active = function (route) {
      return $state.includes.apply(this, route);
    };

    $scope.$on("$stateChangeSuccess", function () {
      $scope.tabs.forEach(function (tab) {
        tab.active = $scope.active(tab.route);
      });
    });

    $scope.loadingReportsList = true;

    ReportsService.getIhubProperties().then(function (res) {
      ReportsiHubService.setIhubUrl(res.data.proxyHost, res.data.proxyIportalPort);
      ReportsService.loadJsapiPromise = angularLoad.loadScript(ReportsiHubService.getIhubUrl() + '/jsapi').then(function() {
        console.log("jsapi loaded ok");
        ReportsService.loginIntoIhub();
      }).catch(function() {
        applicationContext.setNotificationMsgWithValues('Cannot connect to IHub', 'danger', true);
        console.log("! jsapi load failed ");
        $scope.loadingReportsList = false;
      });
    }, function (err) {
      applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true, err.statusText);
    });

    loadReportsList();

    function loadReportsList() {
      ReportsService.reportsConfigPromise = ReportsService.loadReportsConfig().then(function (res) {
        ReportsService.setReportsConfig(res.data);
        $scope.loadingReportsList = false;
        var groups = res.data.groups;
        angular.forEach(groups, function (val, key) {
          var tab = {
            heading: val.heading,
            route: ['authenticated.reports.group', {groupId: val.id}]
          };
          $scope.tabs.push(tab);

        });
      });
    }



  }
]);
