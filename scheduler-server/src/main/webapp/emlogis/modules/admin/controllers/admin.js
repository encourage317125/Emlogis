angular.module('emlogis.admin').controller('AdminCtrl', ['$scope', 'authService',
    function($scope, authService) {
      $scope.hasPermissionIn = function (perms) {
        return authService.hasPermissionIn(perms);
      };

      $scope.hasPermission = function (perm) {
        return authService.hasPermission(perm);
      };
    }
]);
