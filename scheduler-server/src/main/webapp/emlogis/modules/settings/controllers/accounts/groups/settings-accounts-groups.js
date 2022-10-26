angular.module('emlogis.settings').controller('SettingsAccountsGroupsCtrl', ['$scope', '$state', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {
    var entityFilter = applicationContext.getEntityFilter();

    entityFilter.url = 'groupaccounts/ops/quicksearch';
    entityFilter.searchFields = 'name';
    entityFilter.returnedFields = 'id,name';

    applicationContext.setEntityFilter(entityFilter);

    $scope.entityType = $scope.consts.entityTypes.group;
    $scope.entityName = "settings.accounts.groups.NAME";
    $scope.entityListStateName = 'authenticated.settings.accounts.groups.list';
    $scope.entityDetailsStateName = 'authenticated.settings.accounts.groups.groupDetails';
    $scope.entityEditStateName = 'authenticated.settings.accounts.groups.groupDetails';

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Account_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Account_View');
    };
  }
]);
