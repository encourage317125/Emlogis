(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');
  
  rules.controller('RulesSiteNewCtrl', ['$scope', '$state', 'applicationContext', 'appFunc', 'rulesSitesService',
    function ($scope, $state, applicationContext, appFunc, rulesSitesService) {

    //--------------------------------------------------------------------
    // Defaults for New Site
    //--------------------------------------------------------------------

    $scope.selectedSiteDetails = {
      name: 'Create a new Site'
    };
    $scope.selectedSiteDetailsToDisplay = {};
    $scope.selectedSiteDetailsToDisplay.overtimeDto = {};
    $scope.page.editing = false;
    $scope.page.submitted = false;

    $scope.isEditWellCollapsed = false;


    //
    // Cancel creating Site
    // and return user back to existing Sites

    $scope.cancelNewSite = function () {
      $scope.page.editing = false;
      $state.go('authenticated.rules.site_teams');
    };

    
    //
    // Save New Site
    // if form is valid
    
    $scope.saveNewSite = function() {

      if ( $scope.siteForm.$valid ) {

        //console.log('+++ Adding a New Site. Stay tuned!');
        var date = 0;

        if ($scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate){
          date = appFunc.getDateWithTimezone(
            $scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate.getFullYear(),
            $scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate.getMonth(),
            $scope.selectedSiteDetailsToDisplay.twoWeeksOvertimeStartDate.getDate(),
            $scope.selectedSiteDetailsToDisplay.timeZone
          ).getTime();
        }

        var newSite = {
          "id": null,
          "updateDto": {
            "name":                       $scope.selectedSiteDetailsToDisplay.name,
            "abbreviation":               $scope.selectedSiteDetailsToDisplay.abbreviation,
            "description":                $scope.selectedSiteDetailsToDisplay.description,
            "timeZone":                   $scope.selectedSiteDetailsToDisplay.timeZone,
            "address":                    $scope.selectedSiteDetailsToDisplay.address,
            "address2":                   null,                                                            // ?
            "city":                       $scope.selectedSiteDetailsToDisplay.city,
            "state":                      $scope.selectedSiteDetailsToDisplay.state,
            "country":                    $scope.selectedSiteDetailsToDisplay.country,
            "zip":                        $scope.selectedSiteDetailsToDisplay.zip,
            "weekendDefinition":          "SATURDAY_SUNDAY",                                               // ?
            "firstDayOfWeek":             $scope.selectedSiteDetailsToDisplay.firstDayOfWeek,
            "isNotificationEnabled":      $scope.selectedSiteDetailsToDisplay.isNotificationEnabled,
            "shiftIncrements":            $scope.selectedSiteDetailsToDisplay.shiftIncrements,
            "shiftOverlaps":              $scope.selectedSiteDetailsToDisplay.shiftOverlaps,
            "maxConsecutiveShifts":       $scope.selectedSiteDetailsToDisplay.maxConsecutiveShifts,
            "timeOffBetweenShifts":       $scope.selectedSiteDetailsToDisplay.timeOffBetweenShifts,
            "enableWIPFragments":         $scope.selectedSiteDetailsToDisplay.enableWIPFragments || false,
            "twoWeeksOvertimeStartDate":  date,
            "overtimeDto": {
              "dailyOvertimeMins":        $scope.selectedSiteDetailsToDisplay.overtimeDto.dailyOvertimeMins || -1,
              "weeklyOvertimeMins":       $scope.selectedSiteDetailsToDisplay.overtimeDto.weeklyOvertimeMins || -1,
              "biweeklyOvertimeMins":     $scope.selectedSiteDetailsToDisplay.overtimeDto.biweeklyOvertimeMins || -1
            }
          }
        };


        return rulesSitesService.saveNewSite(newSite).then( function(response) {
          //console.log('+++ A New Site has just been created. Yay!', response);
          $scope.page.editing = false;
          $scope.page.submitted = false;
          applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
          $state.go('authenticated.rules.site_teams');

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          //$state.go('authenticated.rules.site_teams');
        });
      } else {
        $scope.page.submitted = true;
      }
    };

    //--------------------------------------------------------------------
    // Setup Working for this page
    //--------------------------------------------------------------------

    var working = applicationContext.getWorking();

    working.entityName = 'rules.NEW_SITE';
    working.option = $scope.page;
    working.saveFunc = $scope.saveNewSite;

  }]);
})();