var profile = angular.module('emlogis.profile');

profile.controller('ProfilePreferencesBreadcrumbCtrl',
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

    console.log('Profile Preferences breadcrumb controller');
  }
]);
