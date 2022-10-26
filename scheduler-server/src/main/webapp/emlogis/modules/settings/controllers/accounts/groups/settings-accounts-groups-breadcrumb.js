angular.module('emlogis.settings').controller('SettingsAccountsGroupsBreadcrumbCtrl', ['$scope', '$state', '$translate', 'applicationContext',
  function ($scope, $state, $translate, applicationContext) {
    $scope.entityName = "settings.accounts.groups.NAME";
    $scope.entityResource = "settings.accounts.GROUPS";
    $scope.entityQuickSearchUrl = "groupaccounts/ops/quicksearch";

    $scope.selectEntitySearchValue = function (item, model, label) {
      $state.go('authenticated.settings.accounts.groups.groupDetails', {entityId: item.id});
    };

    $scope.goToNewEntityState = function () {
      $state.go('authenticated.settings.accounts.groups.create');
    };

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Account_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Account_View');
    };
  }
]);
