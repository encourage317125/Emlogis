(function () {
  "use strict";

  var filteringDirective = function () {

    return {
      restrict: 'E',
      replace: true,
      scope: {
        list: '=',
        filters: '='   // [{name: 'active', value: 'true', buttonLabel: 'ACTIVE'}]
      },
      templateUrl: 'modules/common/partials/filtering-directive.html',
      link: function(scope){

        scope.showControl = false;

        var checkList = scope.$watch('list', function(list) {
          if (_.isEmpty(list)) return;
          checkList(); // would clear the watch
          scope.showControl = true;
          scope.filter(null);
        });

        scope.filter = function(filter) {
          scope.selectedFilter = filter;

          _.each(scope.list, function(item) {
            item.filteredOut = filter && (item[filter.name] !== filter.value);
          });
        };

      }
    };
  };

  filteringDirective.$inject = [];
  angular.module('emlogis.commonDirectives').directive('filteringDirective', filteringDirective);

}());
