var profile = angular.module('emlogis.profile');

profile.controller('ProfileDetailBreadcrumbCtrl',
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

    console.log('Profile Detail breadcrumb controller');
  }
]);
