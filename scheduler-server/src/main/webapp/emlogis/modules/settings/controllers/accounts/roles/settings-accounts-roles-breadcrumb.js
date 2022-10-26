angular.module('emlogis.settings').controller('SettingsAccountsRolesBreadcrumbCtrl', ['$scope', '$state', '$translate', 'applicationContext',
  function ($scope, $state, $translate, applicationContext) {
    $scope.entityName = "settings.accounts.roles.NAME";
    $scope.entityResource = "settings.accounts.ROLES";
    $scope.entityQuickSearchUrl = "roles/ops/quicksearch";

    $scope.selectEntitySearchValue = function (item, model, label) {
      $state.go('authenticated.settings.accounts.roles.roleDetails', {entityId: item.id});
    };

    $scope.goToNewEntityState = function () {
      $state.go('authenticated.settings.accounts.roles.create');
    };

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Role_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Role_View');
    };
  }
]);
