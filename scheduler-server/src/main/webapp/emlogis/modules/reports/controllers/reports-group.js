var home = angular.module('emlogis.reports');

home.controller('ReportsGroupCtrl', [

  '$scope',
  'ReportsService',
  'applicationContext',
  '$state',
  '$stateParams',
  '$q',
  function ($scope,
            ReportsService,
            applicationContext,
            $state,
            $stateParams,
            $q) {

    $q.when(ReportsService.reportsConfigPromise).then(function (res) {
      var tmp = ReportsService.getReportsConfig($stateParams.groupId);
      $scope.reports = tmp ? tmp.reports : null;
    });

    $scope.group = $stateParams.groupId;

    $scope.reportsModel = {current: undefined};

    $scope.changeReport = function (id) {
      $state.go('authenticated.reports.group.details', {id: id});
    };
  }
]);
