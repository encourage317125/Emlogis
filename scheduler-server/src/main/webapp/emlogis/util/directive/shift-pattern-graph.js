

var app = angular.module('emlogis.directives');


app.directive('shiftPatternGraph',['patternGraph',
  function(patternGraph) {
    return {
      restrict: 'AEC',
      scope: {
        selectShiftInGraph: '&'
      },
      templateUrl: 'util/directive/templates/shift-pattern-graph.tpl.html',
      link:function ($scope, $element, $attrs, $controller){

        // const variable
        var MIN_PER_HOUR = 60;
        var MIN_PER_SCALE = 30;

        console.log('Shift Pattern Graph Directive');

        // Shared variable
        $scope.graph = patternGraph.getGraph();

        $scope.inOptimumColsHeader = function($index, isGridTime) {

          if (isGridTime === true) {
            return ($index < $scope.graph.optimumCols );
          }
          else {
            return ($index < $scope.graph.optimumCols * 2 );
          }

        };

        $scope.inOptimumColsShifts = function(shift) {

          return (shift.x + shift.shiftLengthLength / MIN_PER_HOUR * 2 <= $scope.graph.optimumCols * 2);

        };


      }
    };
  }
]);
