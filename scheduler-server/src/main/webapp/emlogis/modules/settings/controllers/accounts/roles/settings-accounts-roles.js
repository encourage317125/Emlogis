angular.module('emlogis.settings').controller('SettingsAccountsRolesCtrl', ['$scope', '$state', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {
    var entityFilter = applicationContext.getEntityFilter();

    entityFilter.url = 'roles/ops/quicksearch';
    entityFilter.searchFields = 'name,label';
    entityFilter.returnedFields = 'id,name';

    applicationContext.setEntityFilter(entityFilter);

    $scope.entityType = $scope.consts.entityTypes.role;
    $scope.entityName = "settings.accounts.roles.NAME";
    $scope.entityListStateName = 'authenticated.settings.accounts.roles.list';
    $scope.entityDetailsStateName = 'authenticated.settings.accounts.roles.roleDetails';
    $scope.entityEditStateName = 'authenticated.settings.accounts.roles.roleDetails';

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Role_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Role_View');
    };
  }
]);
