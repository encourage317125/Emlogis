(function () {
  "use strict";

  //
  // Spin loader element
  // to be used across the app
  // SCSS is placed in emlogis-animation.scss

  var loader = function () {
    return {
      restrict: 'EA',
      replace: true,
      template: '<div class="circularG">' +
                  '<div class="circularG_1 circularG_0"></div>' +
                  '<div class="circularG_2 circularG_0"></div>' +
                  '<div class="circularG_3 circularG_0"></div>' +
                  '<div class="circularG_4 circularG_0"></div>' +
                  '<div class="circularG_5 circularG_0"></div>' +
                  '<div class="circularG_6 circularG_0"></div>' +
                  '<div class="circularG_7 circularG_0"></div>' +
                  '<div class="circularG_8 circularG_0"></div>' +
                '</div>'
    };
  };

  loader.$inject = [];
  angular.module('emlogis.commonDirectives').directive('loader', loader);

}());