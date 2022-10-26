angular.module('emlogis.settings').controller('SettingsBreadcrumbCtrl', ['$scope', '$translate', 'applicationContext', 'authService',
  function ($scope, $translate, applicationContext, authService) {
    // Update Module Information
    var module = applicationContext.getModule();

    // Call translate directive function. Put translated text;
    $translate('nav.SETTINGS')
      .then(function (translation) {
        module.name = translation;
      });
    module.href = '/settings';
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
