angular.module('emlogis.admin').controller('AdminCustomersListCtrl', ['$scope', '$q', 'applicationContext', 'AdminCustomersService',
    function($scope, $q, applicationContext, AdminCustomersService) {

      $scope.tabs = [
        {
          heading: 'admin.customers.LIST_VIEW',
          selected: true,
          templateUrl: 'modules/admin/partials/customers/include/customers-list-view.html'
        }, {
          heading: 'admin.customers.MAP_VIEW',
          selected: false,
          templateUrl: 'modules/admin/partials/customers/include/customers-map-view.html'
        }
      ];

      $scope.selectSubTab = function(tab) {
        angular.forEach($scope.tabs, function(item) {
          item.selected = false;
        });
        tab.selected = true;
      };

      $scope.customerDetails = {
        customerColumnDefs: [
          { field: 'open/Edit',
            enableSorting: false,
            cellTemplate:'<button class="btn btn-default btn-edit-customer" ng-click="grid.appScope.editCustomer(row)" ng-class="{' + "'red-border-cell': grid.appScope.isIncludingRedCell(row)}" + '">{{"app.EDIT" | translate}} ></button>', width: '100' },
          { field: 'tenantId', visible: false },
          { field: 'name', width: '15%' },
          { field: 'geo', width: '5%' },
          { field: 'sites', width: '5%', enableSorting: false },
          { field: 'teams', width: '5%', enableSorting: false },
          { field: 'employees', width: '5%', enableSorting: false },
          { field: 'status', width: '10%', enableSorting: false },
          { field: 'remaining', visible: false },
          { field: 'expirationDate',
            enableSorting: false,
            cellTemplate: '<div class="ui-grid-cell-contents" ng-class="{' + "'red-content-cell': getExternalScopes().isIncludingRedCell(row)}" + '">{{row.entity.expiration}}</div>',
            width: '15%' },
          { field: 'expiration', visible: false },
          { field: 'lastActivity', width: '10%', enableSorting: false },
          { field: 'created', width: '15%' },
          { field: 'updated', width: '15%' }
        ],
        needPagination: true,
        customerEditStateName: 'authenticated.admin.customers.customerEdit'
      };

      $scope.getCustomerList = function(filterTxt) {
        var deferred = $q.defer();
        var offset = 0;
        var limit = 0;
        AdminCustomersService.getCustomerList(filterTxt, offset, limit).then(function(response) {
          if (response.data) {
            deferred.resolve({data: response.data.result});
          } else {
            deferred.reject('Error Occurred while trying to get Customer List');
          }
        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          deferred.reject('Error Occurred while trying to get Customer List');
        });
        return deferred.promise;
      };

      $scope.getCustomerListForPagination = function(filterTxt, customerListGridOptions, paginationOptions) {
        var deferred = $q.defer();
        var offset = (paginationOptions.pageNumber - 1) * paginationOptions.pageSize;
        var limit = paginationOptions.pageSize;
        var orderBy = paginationOptions.orderBy;
        var orderDir = paginationOptions.orderDir;
        AdminCustomersService.getCustomerList(filterTxt, offset, limit, orderBy, orderDir).then(function(response) {
          if (response.data) {
            customerListGridOptions.totalItems = response.data.total;
            deferred.resolve({data: response.data.result});
          } else {
            deferred.reject('Error Occurred while trying to get Customer List');
          }
        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          deferred.reject('Error Occurred while trying to get Customer List');
        });
        return deferred.promise;
      };
    }
]);
