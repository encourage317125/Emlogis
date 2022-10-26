angular.module('emlogis.settings').controller('SettingsAccountsUsersBreadcrumbCtrl', ['$scope', '$state', '$translate', 'applicationContext',
  function ($scope, $state, $translate, applicationContext) {
    $scope.entityName = "settings.accounts.users.NAME";
    $scope.entityResource = "settings.accounts.USERS";
    $scope.entityQuickSearchUrl = "useraccounts/ops/quicksearch";

    $scope.selectEntitySearchValue = function (item, model, label) {
      $state.go('authenticated.settings.accounts.users.userDetails', {entityId: item.id});
    };

    $scope.goToNewEntityState = function () {
      $state.go('authenticated.settings.accounts.users.create');
    };

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Account_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Account_View');
    };
  }
]);
