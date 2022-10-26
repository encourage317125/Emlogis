var MonitoringEntityDetailsCtlr = function ($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter) {

  $scope.parentState = '';
  $scope.refreshInterval = 5;

  $scope.getEntityDetails = function () {
    var deferred = $q.defer();
    MonitoringService.getEntityDetails($scope.entityType, $stateParams.id)
      .then(function (response) {
        if (response.data) {
          deferred.resolve(response.data);
        } else {
          deferred.reject('Error Occurred while trying to get Object');
        }
      }, function (error) {
        applicationContext.setNotificationMsgWithValues(error.data.message, 'danger', true, error.statusText);
        deferred.reject('Error Occurred while trying to get Object');
      });
    return deferred.promise;
  };

  $scope.loadPromise = null;

  $scope.loadData = function () {
    $scope.loadPromise = $scope.getEntityDetails()
      .then(function (data) {
        $scope.entity = data;
      });
  };

  if (!$scope.vetoLoad) {
    $scope.loadData();
    MonitoringService.refresh(function () {
      $scope.loadData();
    }, $scope.refreshInterval);
  }

  $scope.close = function () {
    if ($scope.parentState)
      $state.go($scope.parentState);
  };

  $scope.keys = function (obj) {
    return obj ? _.without(Object.keys(obj), '$$hashKey') : [];
  };

  $scope.customFilter = function(value, filter, params) {
    return $filter(filter)(value, params);
  };

};


var MonitoringEntityListCtlr = function ($scope, $q, MonitoringService, $state, applicationContext) {

  $scope.refreshInterval = 5;
  $scope.getEntityList = function () {
    var deferred = $q.defer();
    MonitoringService.getEntityList($scope.entityType).then(function (response) {
      if (response.data) {
        deferred.resolve(response.data);
      } else {
        deferred.reject('Error Occurred while trying to get Object');
      }
    }, function (error) {
      applicationContext.setNotificationMsgWithValues(error.data.message, 'danger', true, error.statusText);
      deferred.reject('Error Occurred while trying to get Object');
    });
    return deferred.promise;
  };

  $scope.gridData = [];
  $scope.selectedEntities = [];
  $scope.gridOptions = {
    enableColumnResizing: true,
    enableScrollbars: false,
    paginationPageSize: 10,
    data: 'gridData | filter: filter.filterTxt',
    rowTemplate: defaultRowTemplate(),
    columnDefs: $scope.columnDefs,
    enableRowSelection: true,
    enableSelectAll: true,
    multiSelect: true
  };

  $scope.gridOptions.onRegisterApi = function(gridApi){
    gridApi.selection.on.rowSelectionChanged($scope, function (row) {
      $scope.processSelectedRow(row);
    });

    gridApi.selection.on.rowSelectionChangedBatch($scope, function (rows) {
      angular.forEach(rows, function (row) {
        $scope.processSelectedRow(row);
      });
    });
  };

  $scope.processSelectedRow = function (row) {
    if (row.isSelected) {
      $scope.selectedEntities.push(row.entity);
    } else {
      $scope.selectedEntities = _.filter($scope.selectedEntities, function(entity){ return entity[$scope.entityDetailsField] !== row.entity[$scope.entityDetailsField]; });
    }
  };

  function defaultRowTemplate() {
    if($scope.entityDetailsField){
      return '<div ng-dblclick="grid.appScope.viewEntity(row.entity.'+ $scope.entityDetailsField +')" ' +
        'ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
    } else {
      return '<div ' +
        'ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
    }
  }

  $scope.headerCellTemplate = function() {
    return "<div ng-class=\"{ 'sortable': sortable }\">" +
      "<div class=\"ui-grid-vertical-bar\">&nbsp;</div>" +
      "<div class=\"ui-grid-cell-contents\" col-index=\"renderIndex\" title='{{col.name CUSTOM_FILTERS | translate}}' ><span translate>{{col.name CUSTOM_FILTERS }} </span>" +
      "<span ui-grid-visible=\"col.sort.direction\" class='ui-grid-icon-my' ng-class=\"{ 'ui-grid-icon-up-dir': col.sort.direction == asc, 'ui-grid-icon-down-dir': col.sort.direction == desc, 'ui-grid-icon-blank': !col.sort.direction }\">&nbsp;</span></span>" +
      "</div>" +
      "<div ng-if=\"filterable\" class=\"ui-grid-filter-container\" ng-repeat=\"colFilter in col.filters\">" +
      "<input type=\"text\" class=\"ui-grid-filter-input\" ng-model=\"colFilter.term\" ng-click=\"$event.stopPropagation()\" ng-attr-placeholder=\"{{colFilter.placeholder || ''}}\">" +
      "<div class=\"ui-grid-filter-button\" ng-click=\"colFilter.term = null\">" +
      "<i class=\"ui-grid-icon-cancel right\" ng-show=\"!!colFilter.term\">&nbsp;</i> <!-- use !! because angular interprets 'f' as false -->" +
      "</div>" +
      "</div>" +
      "</div>";
  };

  $scope.dateCellTemplate = function() {
    return '<div class="ui-grid-cell-contents"> {{COL_FIELD | date: "yyyy/MM/dd HH:mm:ss"}} </div>';
  };

  function createGridData(data) {
    var gridData = [];
    angular.forEach(data, function (elem, index) {
      gridData.push(elem);
    });
    $scope.gridData = gridData;
  }
  $scope.loadPromise = null;
  $scope.loadData = function () {
    $scope.loadPromise = $scope.getEntityList()
      .then(function (data) {
        createGridData(data);
        if($scope.saveResult){
          $scope.entityList = data;
        }
      });
  };

  $scope.loadData();
  MonitoringService.refresh(function () {
    $scope.loadData();
  }, $scope.refreshInterval);

  $scope.viewEntity = function (id) {
    $state.go($state.current.name + '.details', {id: id});
  };
};
