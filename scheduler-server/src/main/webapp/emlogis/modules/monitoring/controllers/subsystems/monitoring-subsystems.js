angular.module('emlogis.monitoring').controller('MonitoringSubsystemsCtrl', [
  '$scope', '$state', 'MonitoringService',
  function ($scope, $state, MonitoringService) {

    $scope.tabs = [
      {heading: "monitoring.SUBSYSTEMS_HAZELCAST", route: 'authenticated.monitoring.subsystems.hazelcast'},
      {heading: "monitoring.SUBSYSTEMS_NOTIFICATIONS", route: 'authenticated.monitoring.subsystems.notifications'},
      {heading: "monitoring.SUBSYSTEMS_ACTIVE_SESSIONS", route: 'authenticated.monitoring.subsystems.activesessions'},
      {heading: "monitoring.SUBSYSTEMS_APPSERVERS", route: 'authenticated.monitoring.subsystems.appservers'},
      {heading: "monitoring.SUBSYSTEMS_ENGINES", route: 'authenticated.monitoring.subsystems.engines'}
    ];

    $scope.goState = function (tab) {
      $state.go(tab.route);
    };

    $scope.active = function (route) {
      return $state.includes(route);
    };

    $scope.$on("$stateChangeSuccess", function () {
      $scope.tabs.forEach(function (tab) {
        tab.active = $scope.active(tab.route);
      });
    });
  }
]);
