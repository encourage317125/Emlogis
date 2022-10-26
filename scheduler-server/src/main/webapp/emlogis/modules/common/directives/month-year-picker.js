(function () {
  "use strict";
  
  var monthYearPicker = function () {

    return {

      restrict: 'E',
      replace: true,
      scope: {
        year: '=',
        minYear: '=',
        maxYear: '=',
        month: '='
      },
      templateUrl: 'modules/common/partials/month-year-picker.html',
      link: function(scope) {

        var months = {
          0: "January",
          1: "February",
          2: "March",
          3: "April",
          4: "May",
          5: "June",
          6: "July",
          7: "August",
          8: "September",
          9: "October",
          10: "November",
          11: "December"
        };

        function maxYearReached() {
          return scope.maxYear && scope.year === scope.maxYear;
        }

        function minYearReached() {
          return scope.minYear && scope.year === scope.minYear;
        }

        function nextYear() {
          if (!maxYearReached()) {
            scope.year++;
          }
        }

        function prevYear() {
          if (!minYearReached()) {
            scope.year--;
          }
        }

        function nextMonth() {
          if (scope.month == 11) {
            if (maxYearReached()) return;
            scope.month = 0;
            scope.year++;
          } else {
            scope.month++;
          }
        }

        function prevMonth() {
          if (scope.month === 0) {
            if (minYearReached()) return;
            scope.month = 11;
            scope.year--;
          } else {
            scope.month--;
          }
        }

        scope.year = scope.year || moment().year();

        scope.next = function() {
          if (typeof scope.month === "number") {
            nextMonth();
          } else {
            nextYear();
          }
        };

        scope.previous = function() {
          if (typeof scope.month === "number") {
            prevMonth();
          } else {
            prevYear();
          }
        };

        scope.valueToDisplay = function() {
          if (typeof scope.month === "number") {
            return months[scope.month] + " " + scope.year;
          } else {
            return scope.year;
          }
        };
      }
    };
  };


  monthYearPicker.$inject = [];
  angular.module('emlogis.commonDirectives').directive('monthYearPicker', monthYearPicker);

}());
