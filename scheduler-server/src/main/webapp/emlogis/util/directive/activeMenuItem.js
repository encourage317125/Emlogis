/**
 * Created by Georgi K. on 10/9/14.
 */

app = angular.module('emlogis.directives');

app.directive('activeClassOn', [
    '$state', function($state) {
        return {
            restrict: 'A',
            controller: [
                '$scope', '$element', '$attrs', function($scope, $element, $attrs) {
                    var isMatch, routesToMatch;
                    routesToMatch = $attrs.activeClassOn.split(' ');
                    isMatch = function() {
                        return _.any(routesToMatch, function(route) {
                            return $state.includes(route);
                        });
                    };
                    $scope.$on('$stateChangeSuccess', function() {
                        if (isMatch()) {
                            return $element.addClass('active');
                        } else {
                            return $element.removeClass('active');
                        }
                    });
                    if (isMatch()) {
                        return $element.addClass('active');
                    } else {
                        return $element.removeClass('active');
                    }
                }
            ]
        };
    }
]);
