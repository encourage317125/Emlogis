angular.module('emlogis.settings').controller('SettingsCtrl', ['$scope', '$state', '$http', '$filter', 'applicationContext', 'crudDataService', 'authService',
  function ($scope, $state, $http, $filter, applicationContext, crudDataService, authService) {

    $scope.hasPermissionIn = function (perms) {
      return authService.hasPermissionIn(perms);
    };

    $scope.hasPermission = function (perm) {
      return authService.hasPermission(perm);
    };

    $scope.isTenantType = function (tenantType) {
      return authService.isTenantType(tenantType);
    };
  }
]);
