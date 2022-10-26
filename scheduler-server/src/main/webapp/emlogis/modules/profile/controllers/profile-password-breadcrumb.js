var profile = angular.module('emlogis.profile');

profile.controller('ProfilePasswordBreadcrumbCtrl',
  [
    '$rootScope',
    '$scope',
    '$modal',
    'applicationContext',
    'scheduleService',
    'crudDataService',
    'appFunc',
    '$q',
  function($rootScope, $scope, $modal, applicationContext, scheduleService, crudDataService, appFunc, $q) {

    console.log('Profile Password breadcrumb controller');
  }
]);
