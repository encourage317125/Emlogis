angular.module('emlogis.admin').controller('CustomersListViewCtrl', ['$scope',
  function($scope) {

    $scope.convertCustomerToGridRow = function(customer) {
      var gridRow = {};

      gridRow.tenantId = customer.tenantId;
      gridRow.name = customer.name;
      gridRow.geo = customer.geo;
      gridRow.sites = customer.nbOfSites;
      gridRow.teams = customer.nbOfTeams;
      gridRow.employees = customer.nbOfEmployees;
      gridRow.status = customer.productLicenseInfo.moduleStatus;
      gridRow.expiration = new Date(customer.productLicenseInfo.moduleExpirationDate).toString();
      var dayInMilliSeconds = 24 * 3600 * 1000;
      gridRow.remaining = (customer.productLicenseInfo.moduleExpirationDate - new Date().getTime())/dayInMilliSeconds;
      if (gridRow.remaining < 0) {
        gridRow.remaining = 0;
      }
      gridRow.lastActivity = customer.lastLoggingDate;
      gridRow.created = new Date(customer.created).toString();
      gridRow.updated = new Date(customer.updated).toString();

      return gridRow;
    };

    $scope.filter = {
      filterTxt: '',
      confirmedFilterTxt: ''
    };

    $scope.filterList = function() {
      $scope.filter.confirmedFilterTxt = $scope.filter.filterTxt;
    };

    $scope.getCustomerListViewForPagination = function(gridOptions, paginationOptions) {
      return $scope.getCustomerListForPagination($scope.filter.confirmedFilterTxt, gridOptions, paginationOptions);
    };

    $scope.getCustomerListView = function() {
      return $scope.getCustomerList($scope.filter.confirmedFilterTxt);
    };
  }
]);