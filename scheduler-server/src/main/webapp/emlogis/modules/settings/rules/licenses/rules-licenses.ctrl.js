(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesLicensesCtrl', ['$scope', 'applicationContext',
    function ($scope, applicationContext) {
    console.log('+++ inside Rules Licenses & Certifications controller');

    $scope.page.editing = false;
    $scope.page.submitted = false;


    //--------------------------------------------------------------------
    // Setup Working for this page
    //--------------------------------------------------------------------

    var working = applicationContext.getWorking();

    working.entityName = 'rules.LICENSES_CERTIFICATIONS';
    working.option = $scope.page;
    working.saveFunc = $scope.save;
    //working.restoreFunc = restoreOriginalSchedule;

  }]);
})();