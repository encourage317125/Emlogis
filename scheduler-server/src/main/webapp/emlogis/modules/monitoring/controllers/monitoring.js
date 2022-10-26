angular.module('emlogis.monitoring').controller('MonitoringCtrl', ['$scope', 'MonitoringService',
  function($scope, MonitoringService) {
    $scope.$on("$stateChangeStart", function () {
      MonitoringService.stopRefresh();
    });
  }
]);

angular.module('emlogis.monitoring').directive('optionButtons', function($compile) {
  return {
    restrict: "E",
    replace: true,
    scope : {
      buttons: '='
    },

    link:  function (scope, element, attrs) {
      element.html('<button data-ng-repeat="btn in buttons" class="btn btn-default pull-right" ng-disabled="btn.disabled" data-ng-click="btn.onClick()">' +
      '      <span ng-bind-html="btn.title"></span>' +
      '</button>');
      $compile(element.contents())(scope);
    }
  };
});