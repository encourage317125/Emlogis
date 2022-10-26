(function () {
  "use strict";

  var sortingDirective = function () {

    return {
      restrict: 'E',
      replace: true,
      scope: {
        list: '=',
        fields: '=',   // [{name: 'abbreviation', buttonLabel: 'ABBREVIATION'}]
        defaultField: '@',
        defaultOrder: '='
      },
      templateUrl: 'modules/common/partials/sorting-directive.html',
      link: function(scope){

        scope.showControl = false;

        var checkList = scope.$watch('list', function(list) {
          if (_.isEmpty(list)) return;
          checkList(); // would clear the watch
          scope.showControl = true;
          init();
        });

        function init() {
          scope.order = scope.defaultOrder || true;
          scope.selectedField = scope.defaultField || scope.fields[0];
          scope.sort();
        }

        scope.selectField = function(field) {
          scope.selectedField = field;
        };

        scope.sort = function() {
          scope.list = _.sortByOrder(scope.list, scope.selectedField.name, scope.order);
        };

      }
    };
  };


  sortingDirective.$inject = [];
  angular.module('emlogis.commonDirectives').directive('sortingDirective', sortingDirective);

}());
