angular.module('emlogis.admin').controller('AdminCustomersBreadcrumbCtrl', ['$scope', '$state',
  function($scope, $state) {
    $scope.entityName = "admin.customers.NAME";
    $scope.entityResource = "admin.CUSTOMERS";
    $scope.entityQuickSearchUrl = "serviceproviders/emlogisservice/orgs/ops/quicksearch";

    $scope.selectEntitySearchValue = function (item, model, label) {
      $state.go('authenticated.admin.customers.customerEdit', {tenantId: item.id});
    };

    $scope.goToNewEntityState = function () {
      $state.go('authenticated.admin.customers.create');
    };

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Tenant_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Tenant_View');
    };
  }
]);
