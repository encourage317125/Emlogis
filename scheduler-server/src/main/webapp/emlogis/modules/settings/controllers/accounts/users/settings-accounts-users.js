angular.module('emlogis.settings').controller('SettingsAccountsUsersCtrl', ['$scope', '$state', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {
    var entityFilter = applicationContext.getEntityFilter();

    entityFilter.url = 'useraccounts/ops/quicksearch';
    entityFilter.searchFields = 'login,name';
    entityFilter.returnedFields = 'id,name';

    applicationContext.setEntityFilter(entityFilter);

    $scope.entityType = $scope.consts.entityTypes.user;
    $scope.entityName = "settings.accounts.users.NAME";
    $scope.entityListStateName = 'authenticated.settings.accounts.users.list';
    $scope.entityDetailsStateName = 'authenticated.settings.accounts.users.userDetails';
    $scope.entityEditStateName = 'authenticated.settings.accounts.users.userEdit';

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Account_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Account_View');
    };

    $scope.hasSupportPermission = function () {
      return $scope.hasPermission('Support');
    };

  }
]);
