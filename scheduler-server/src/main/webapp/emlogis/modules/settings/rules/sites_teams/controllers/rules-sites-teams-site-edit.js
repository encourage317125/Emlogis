(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');
  
  rules.controller('RulesSiteEditCtrl', ['$scope', 'rulesSitesService', function ($scope, rulesSitesService) {
    //console.log('+++ inside Rules - Site Edit controller');

    //
    // Defaults for Sites

    $scope.siteOptions = {
      weekDays              : ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"],
      shiftIncrements       : [15, 30, 60],
      shiftOverlaps         : [0, 15, 30, 60],
      maxConsecutiveShifts  : [1, 2, 3, 4, 5, 6, 7],
      datepickerOpened      : false
    };


    //
    // Listen to selected Site is being changed
    // and load details for newly selected Site

    $scope.$watch("selectedSite", function(newValue) {
      if (newValue) {
        //console.log('+++ Selected Site ID: ' + newValue.id + ' loading Site details...');
        $scope.loadSiteDetails(newValue.id);
        $scope.siteForm.$setPristine(true);
        rulesSitesService.setSiteForm($scope.siteForm);
      }
    });



    //
    // Open date picker

    $scope.siteOptions.openDatepicker = function($event) {
      $event.preventDefault();
      $event.stopPropagation();
      $scope.siteOptions.datepickerOpened = true;
    };


  }]);
})();