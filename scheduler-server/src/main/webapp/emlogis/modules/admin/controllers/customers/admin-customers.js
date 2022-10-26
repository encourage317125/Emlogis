angular.module('emlogis.admin').controller('AdminCustomersCtrl', ['$scope', 'applicationContext',
  function($scope, applicationContext) {
    var entityFilter = applicationContext.getEntityFilter();

    entityFilter.url = 'serviceproviders/emlogisservice/orgs/ops/quicksearch';
    entityFilter.searchFields = 'name';
    entityFilter.returnedFields = 'tenantId,name';

    applicationContext.setEntityFilter(entityFilter);

    $scope.hasMgmtPermission = function () {
      return $scope.hasPermission('Tenant_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return $scope.hasPermission('Tenant_View');
    };
  }
]);
