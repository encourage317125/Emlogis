angular.module('emlogis.settings').directive('entityList', ['$state', '$timeout', 'uiGridConstants','applicationContext',
  function ($state, $timeout, uiGridConstants,applicationContext) {

    return {
      restrict: 'E',
      scope: {
        entityDetails: '=',
        filterTxt: '=',
        getEntityList: '&',
        convertEntityToGridRow: '&'
      },
      controller: function ($scope) {
        function rowTemplate() {
          return '<div ng-dblclick="grid.appScope.viewEntity(row)" ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
        }

        $scope.entityList = [];
        $scope.entityListGridData = [];

        $scope.entityListGridOptions = {
          data: 'entityListGridData',
          rowTemplate: rowTemplate(),
          columnDefs: $scope.entityDetails.entityColumnDefs,
          enableGridMenu: true,
          enablePaginationControls: false
        };

        if ($scope.entityDetails.needPagination) {
          $scope.paginationOptions = {
            pageNumber: 1,
            pageSize: applicationContext.getGridPageItemSize(),
            orderBy: null,
            orderDir: null
          };
          $scope.entityListGridOptions = _.extend($scope.entityListGridOptions, {
//            paginationPageSizes: [25, 50, 75],
            paginationPageSize: applicationContext.getGridPageItemSize(),
            useExternalPagination: true,
            minRowsToShow: applicationContext.getGridPageItemSize(),
            paginationCurrentPage: 1,
            useExternalSorting: true,
            onRegisterApi: function (gridApi) {
              gridApi.core.on.sortChanged($scope, function (grid, sortColumns) {
                $scope.processOnSortChanged(grid, sortColumns);
              });
              gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
                $scope.processOnPaginationChanged(newPage, pageSize);
              });
            }
          });

          $scope.processOnSortChanged = function (grid, sortColumns) {
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
            $scope.getEntityList({gridOptions: $scope.entityListGridOptions, paginationOptions: $scope.paginationOptions}).then(function (response) {
              $scope.entityList = response.data;
              $scope.parseEntityListForGrid();
            }, function (err) {
              console.log(err);
              $scope.entityList = [];
              $scope.parseEntityListForGrid();
            });
          };

          $scope.processOnPaginationChanged = function (newPage, pageSize) {
            $scope.paginationOptions.pageNumber = newPage;
            $scope.paginationOptions.pageSize = pageSize;
            $scope.getEntityList({gridOptions: $scope.entityListGridOptions, paginationOptions: $scope.paginationOptions}).then(function (response) {
              $scope.entityList = response.data;
              $scope.parseEntityListForGrid();
            }, function (err) {
              console.log(err);
              $scope.entityList = [];
              $scope.parseEntityListForGrid();
            });
          };
        }

        /**
         * Fired when user dblclick on the grid
         */
        $scope.viewEntity = function(row) {

          $state.go($scope.entityDetails.entityDetailsStateName, {entityId: row.entity.id});
        };

        $scope.initializeEntityList = function() {
          if ($scope.entityDetails.needPagination) {
            $scope.getEntityList({gridOptions: $scope.entityListGridOptions, paginationOptions: $scope.paginationOptions}).then(function (response) {
              $scope.entityList = response.data;
              $scope.parseEntityListForGrid();
            }, function (err) {
              console.log(err);
              $scope.entityList = [];
              $scope.parseEntityListForGrid();
            });
          } else {
            $scope.getEntityList().then(function (response) {
              $scope.entityList = response.data;
              $scope.parseEntityListForGrid();
            }, function (err) {
              console.log(err);
              $scope.entityList = [];
              $scope.parseEntityListForGrid();
            });
          }
        };

        $scope.parseEntityListForGrid = function () {
          $scope.entityListGridData = [];
          angular.forEach($scope.entityList, function (entity) {
            var gridRow = $scope.convertEntityToGridRow({message: entity});

            $scope.entityListGridData.push(gridRow);
          });

          $timeout(function () {
            $('.entity-list').resize();
          }, 0);
        };
      },
      link: function (scope) {
        scope.$watch('filterTxt', function (newValue, oldValue) {
          if (newValue !== oldValue) {
            scope.initializeEntityList();
          }
        });
        scope.initializeEntityList();
      },
      templateUrl: 'modules/settings/partials/include/entity-list.html'
    };
  }
]);
