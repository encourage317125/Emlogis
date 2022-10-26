angular.module('emlogis.admin').controller('AdminBreadcrumbCtrl', ['$scope', '$translate', 'applicationContext', 'authService',
  function($scope, $translate, applicationContext, authService) {
    // Update Module Information
    var module = applicationContext.getModule();

    // Call translate directive function. Put translated text;
    $translate('nav.ADMIN')
      .then(function (translation) {
        module.name =  translation;
      });
    module.href = '/admin';
    module.icoClass = 'glyphicon glyphicon-cog';
    module.disableModuleBreadcrumb = false;
    applicationContext.setModule(module);

    $scope.hasPermissionIn = function (perms) {
      return authService.hasPermissionIn(perms);
    };

    $scope.hasPermission = function (perm) {
      return authService.hasPermission(perm);
    };
  }
]);
