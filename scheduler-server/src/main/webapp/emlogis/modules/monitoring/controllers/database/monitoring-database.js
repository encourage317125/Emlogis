angular.module('emlogis.monitoring').controller('MonitoringDatabaseCtrl', [
  '$scope', '$state',
  function ($scope, $state) {

    $scope.tabs = [
      { heading: "monitoring.DATABASE_SUMMARY", route: 'authenticated.monitoring.database.summary' },
      { heading: "monitoring.DATABASE_PER_CUSTOMER", route: 'authenticated.monitoring.database.percustomer' }
    ];

    $scope.goState = function (tab) {
      $state.go(tab.route);
    };

    $scope.active = function (route) {
      return $state.is(route);
    };

    $scope.$on("$stateChangeSuccess", function () {
      $scope.tabs.forEach(function (tab) {
        tab.active = $scope.active(tab.route);
      });
    });
  }
]);
