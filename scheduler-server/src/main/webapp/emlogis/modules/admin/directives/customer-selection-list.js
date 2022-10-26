angular.module('emlogis.admin').directive('customerSelectionList', ['$state', '$timeout', 'uiGridConstants',
  function($state, $timeout, uiGridConstants) {

    return {
      restrict: 'E',
      scope: {
        customerDetails: '=',
        filterTxt: '=',
        getCustomerList: '&',
        convertCustomerToGridRow: '&'
      },
      controller: function($scope) {
        function rowTemplate() {
          return '<div ng-dblclick="grid.appScope.editCustomer(row)" ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
        }

        $scope.customerList = [];
        $scope.customerListGridData = [];

        $scope.customerListGridOptions = {
          data: 'customerListGridData',
          rowTemplate: rowTemplate(),
          columnDefs: $scope.customerDetails.customerColumnDefs
        };

        if ($scope.customerDetails.needPagination) {
          $scope.paginationOptions = {
            pageNumber: 1,
            pageSize: 25,
            orderBy: null,
            orderDir: null
          };
          $scope.customerListGridOptions = _.extend($scope.customerListGridOptions, {
            paginationPageSizes: [25, 50, 75],
            paginationPageSize: 25,
            useExternalPagination: true,
            useExternalSorting: true,
            onRegisterApi: function(gridApi) {
              gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                $scope.processOnSortChanged(grid, sortColumns);
              });
              gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
                $scope.processOnPaginationChanged(newPage, pageSize);
              });
            }
          });

          $scope.processOnSortChanged = function(grid, sortColumns) {
            if (sortColumns.length === 0) {
              $scope.paginationOptions.orderBy = null;
              $scope.paginationOptions.orderDir = null;
            } else {
              $scope.paginationOptions.orderBy = sortColumns[0].field;
              if (sortColumns[0].sort.direction === uiGridConstants.ASC) {
                $scope.paginationOptions.orderDir = 'ASC';
              } else if (sortColumns[0].sort.direction === uiGridConstants.DESC) {
                $scope.paginationOptions.orderDir = 'DESC';
              } else {
                $scope.paginationOptions.orderBy = null;
                $scope.paginationOptions.orderDir = null;
              }
            }
            $scope.getCustomerList({gridOptions: $scope.customerListGridOptions, paginationOptions: $scope.paginationOptions}).then(function (response) {
              $scope.customerList = response.data;
              $scope.parseCustomerListForGrid();
            }, function (err) {
              console.log(err);
              $scope.customerList = [];
              $scope.parseCustomerListForGrid();
            });
          };

          $scope.processOnPaginationChanged = function(newPage, pageSize) {
            $scope.paginationOptions.pageNumber = newPage;
            $scope.paginationOptions.pageSize = pageSize;
            $scope.getCustomerList({gridOptions: $scope.customerListGridOptions, paginationOptions: $scope.paginationOptions}).then(function(response) {
              $scope.customerList = response.data;
              $scope.parseCustomerListForGrid();
            }, function(err) {
              console.log(err);
              $scope.customerList = [];
              $scope.parseCustomerListForGrid();
            });
          };
        }

        $scope.editCustomer = function(row) {
          $state.go($scope.customerDetails.customerEditStateName, {tenantId: row.entity.tenantId});
        };

        $scope.isIncludingRedCell = function(row) {
          if (row.entity.remaining < 30) {
            return true;
          } else {
            return false;
          }
        };

        $scope.initializeCustomerList = function() {
          if ($scope.customerDetails.needPagination) {
            $scope.getCustomerList({gridOptions: $scope.customerListGridOptions, paginationOptions: $scope.paginationOptions}).then(function (response) {
              $scope.customerList = response.data;
              $scope.parseCustomerListForGrid();
            }, function (err) {
              console.log(err);
              $scope.customerList = [];
              $scope.parseCustomerListForGrid();
            });
          } else {
            $scope.getCustomerList().then(function (response) {
              $scope.customerList = response.data;
              $scope.parseCustomerListForGrid();
            }, function (err) {
              console.log(err);
              $scope.customerList = [];
              $scope.parseCustomerListForGrid();
            });
          }
        };

        $scope.parseCustomerListForGrid = function() {
          $scope.customerListGridData = [];
          angular.forEach($scope.customerList, function(customer) {
            var gridRow = $scope.convertCustomerToGridRow({message: customer});

            $scope.customerListGridData.push(gridRow);
          });

          $timeout(function() {
            $('.customer-list').resize();
          }, 0);
        };
      },
      link: function(scope) {
        scope.$watch('filterTxt', function(newValue, oldValue) {
          if (newValue !== oldValue) {
            scope.initializeCustomerList();
          }
        });
        scope.initializeCustomerList();
      },
      templateUrl: 'modules/admin/partials/customers/include/customer-selection-list.html'
    };
  }
]);
