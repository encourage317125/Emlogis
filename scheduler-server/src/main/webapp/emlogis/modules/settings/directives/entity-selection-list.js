angular.module('emlogis.settings').directive('entitySelectionList', ['$timeout', '$modal',
  function ($timeout, $modal) {

    return {
      restrict: 'E',
      scope: {
        originalEntityDetails: '=',
        entityDetails: '=',
        needOperationButtons: '=',
        getEntityList: '&',
        convertEntityToGridRow: '&',
        processSelectedRow: '&',
        addSelectionList: '&',
        removeSelectionList: '&'
      },
      controller: function ($scope) {
        $scope.entityList = [];
        $scope.entityListGridData = [];
        $scope.selectedEntities = [];

        $scope.entityListGridOptions = {
          data: 'entityListGridData',
          enableRowSelection: true,
          enableSelectAll: true,
          multiSelect: true,
          columnDefs: $scope.entityDetails.entityColumnDefs
        };

        if ($scope.entityDetails.needPagination) {
          $scope.paginationOptions = {
            pageNumber: 1,
            pageSize: 25,
            orderBy: null,
            orderDir: null
          };
          $scope.entityListGridOptions = _.extend($scope.entityListGridOptions, {
            paginationPageSizes: [25, 50, 75],
            paginationPageSize: 25,
            useExternalPagination: true,
            useExternalSorting: true,
            onRegisterApi: function (gridApi) {
              gridApi.core.on.sortChanged($scope, function (grid, sortColumns) {
                $scope.processOnSortChanged(grid, sortColumns);
              });
              gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
                $scope.processOnPaginationChanged(newPage, pageSize);
              });
              gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                if ($scope.needOperationButtons) {
                  $scope.processSelectedRowLocal(row);
                } else {
                  $scope.processSelectedRow({message: row});
                }
              });
              gridApi.selection.on.rowSelectionChangedBatch($scope, function (rows) {
                angular.forEach(rows, function (row) {
                  if ($scope.needOperationButtons) {
                    $scope.processSelectedRowLocal(row);
                  } else {
                    $scope.processSelectedRow({message: row});
                  }
                });
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
        } else {
          $scope.entityListGridOptions.onRegisterApi = function (gridApi) {
            gridApi.selection.on.rowSelectionChanged($scope, function (row) {
              if ($scope.needOperationButtons) {
                $scope.processSelectedRowLocal(row);
              } else {
                $scope.processSelectedRow({message: row});
              }
            });

            gridApi.selection.on.rowSelectionChangedBatch($scope, function (rows) {
              angular.forEach(rows, function (row) {
                if ($scope.needOperationButtons) {
                  $scope.processSelectedRowLocal(row);
                } else {
                  $scope.processSelectedRow({message: row});
                }
              });
            });
          };
        }

        $scope.processSelectedRowLocal = function (row) {
          if (row.isSelected) {
            $scope.selectedEntities.push(row.entity);
          } else {
            $scope.selectedEntities = _.filter($scope.selectedEntities, function(entity){ return entity.id !== row.entity.id; });
          }
        };

        $scope.openEntityListSelectionModal = function () {
          var modalInstance = $modal.open({
            templateUrl: 'listModalContent.html',
            controller: 'entitySelectionListModalCtrl',
            size: 'lg',
            resolve: {
              originalEntityDetails: function () {
                return $scope.originalEntityDetails;
              },
              entityDetails: function () {
                return {
                  entityType: $scope.entityDetails.entityType,
                  entitiesName: $scope.entityDetails.tabHeading,
                  needPagination: $scope.entityDetails.needPagination,
                  entityColumnDefs: $scope.entityDetails.entityColumnDefs
                };
              },
              convertEntityToGridRow: function () {
                return $scope.convertEntityToGridRow;
              }
            }
          });

          modalInstance.result.then(function (selectedEntityIds) {
            $scope.addSelectionList({entityDetails: $scope.entityDetails, selectedEntityIds: selectedEntityIds}).then(function (response) {
              $scope.initializeEntityList();
            });
          });
        };

        $scope.removeSelectedEntities = function () {
          $scope.removeSelectionList({entityDetails: $scope.entityDetails, selectedEntities: $scope.selectedEntities}).then(function (response) {
            $scope.initializeEntityList();
          });
        };

        $scope.initializeEntityList = function () {
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
            $('.entity-selection-list').resize();
          }, 0);
        };
      },
      link: function (scope) {
        scope.initializeEntityList();
      },
      templateUrl: 'modules/settings/partials/include/entity-selection-list.html'
    };
  }
]);
