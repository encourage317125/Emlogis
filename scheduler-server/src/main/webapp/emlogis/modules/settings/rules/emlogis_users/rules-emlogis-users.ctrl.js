(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesEmlogisUsersCtrl', ['$scope', 'applicationContext',
    function ($scope, applicationContext) {
    console.log('inside Rules Emlogis Users controller');

    $scope.page.editing = false;
    $scope.page.submitted = false;


    //--------------------------------------------------------------------
    // Setup Working for this page
    //--------------------------------------------------------------------

    var working = applicationContext.getWorking();

    working.entityName = 'rules.EMLOGIS_USERS';
    working.option = $scope.page;
    working.saveFunc = $scope.save;
    //working.restoreFunc = restoreOriginalSchedule;

  }]);
})();
