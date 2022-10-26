var scheduleBuilder = angular.module('emlogis.schedule_builder');
//depricated controller
scheduleBuilder.controller('ScheduleBuilderOpenScheduleCtrl',
  [
    '$scope',
    '$modalInstance',
    '$translate',
    '$q',
    '$http',
    'crudDataService',
    'uiGridConstants',
    'scheduleFilter',
    'appFunc',
    function (
      $scope,
      $modalInstance,
      $translate,
      $q,
      $http,
      crudDataService,
      uiGridConstants,
      scheduleFilter,
      appFunc
      ) {

      var factory = _.clone(crudDataService);

      /**
       * Schedule Statuses
       * @type {{id: number, title: string}[]}
       */
      $scope.statuses = [
        {id:0, title: 'Simulation'},
        {id:1, title: 'Production'},
        {id:2, title: 'Posted'}
      ];

      // When we go to login page we have to dismiss the modal.
      $scope.$on('event:auth-loginRequired', function () {
        $modalInstance.dismiss('cancel');
      });


      // Close Modal
      $scope.close = function () {
        $modalInstance.dismiss('cancel');
      };

      /**
       * it returns promise object of sites
       */
      $scope.listSchedules = function () {

        var deferred = $q.defer();

        var filter = "name like '%" + $scope.browser.filterTxt + "%'";

        if ($scope.browser.filterStatus !== null) {
          filter = ';status=' + $scope.browser.filterStatus.id;
        }

        if (scheduleFilter.length > 0 ){
          filter = filter + ';' + scheduleFilter;
        }

        var schedulesListUrl = '../emlogis/rest/schedules/ops/query';//?filter=' + encodeURIComponent(filter);

        //schedulesListUrl += "&orderby="+$scope.browser.orderBy + "&orderdir="+$scope.browser.orderDir;

        $scope.preparePageParam();

        //schedulesListUrl += "&offset="+$scope.browser.offset + "&limit="+$scope.browser.limit;

        var params = {
          filter: filter,
          orderby: $scope.browser.orderBy,
          orderdir: $scope.browser.orderDir,
          limit: $scope.browser.limit,
          offset: $scope.browser.offset
        };

        $http.get(schedulesListUrl, {params: params})
          .then(function (response) {

            var resultSet = factory.toResultSet(response);

            $scope.gridOptions.data = resultSet.data;

            /**
             * convert startdate to browser timezone
             */

//            for (var i=0; i < $scope.gridOptions.data.length; i++) {
//              var schedule = $scope.gridOptions.data[i];
//              schedule.startDate = appFunc.convertToBrowserTimezone(schedule.startDate, $scope.site.timeZone);
//            }

            $scope.browser.totalRecords = resultSet.total;

            deferred.resolve(resultSet.data);
          });

        return deferred.promise;
      };

      $scope.browser = {
        filterTxt: "",
        filterStatus: null,
        orderBy: "startDate",
        orderDir: "desc",
        pageSize: 25,
        currentPage: 1
      };

      /*
       *This header cell template is almost same as default cell template except
       * it has translate directive inside so grid header will support i18n as well
       */
      var headerCellTemplate = function () {
        return "<div ng-class=\"{ 'sortable': sortable }\">" +
          "<div class=\"ui-grid-vertical-bar\">&nbsp;</div>" +
          "<div class=\"ui-grid-cell-contents\" col-index=\"renderIndex\" ><span translate>{{ col.name CUSTOM_FILTERS }} </span>" +
          "<span ui-grid-visible=\"col.sort.direction\" ng-class=\"{ 'ui-grid-icon-up-dir': col.sort.direction == asc, 'ui-grid-icon-down-dir': col.sort.direction == desc, 'ui-grid-icon-blank': !col.sort.direction }\">&nbsp;</span>" +
          "</div>" +
          "<div class=\"ui-grid-column-menu-button\" ng-if=\"grid.options.enableColumnMenus && !col.isRowHeader  && col.colDef.enableColumnMenu !== false\" class=\"ui-grid-column-menu-button\" ng-click=\"toggleMenu($event)\">" +
          "<i class=\"ui-grid-icon-angle-down\">&nbsp;</i>" +
          "</div>" +
          "<div ng-if=\"filterable\" class=\"ui-grid-filter-container\" ng-repeat=\"colFilter in col.filters\">" +
          "<input type=\"text\" class=\"ui-grid-filter-input\" ng-model=\"colFilter.term\" ng-click=\"$event.stopPropagation()\" ng-attr-placeholder=\"{{colFilter.placeholder || ''}}\">" +
          "<div class=\"ui-grid-filter-button\" ng-click=\"colFilter.term = null\">" +
          "<i class=\"ui-grid-icon-cancel right\" ng-show=\"!!colFilter.term\">&nbsp;</i> <!-- use !! because angular interprets 'f' as false -->" +
          "</div>" +
          "</div>" +
          "</div>";
      };

      // ColumnDef Info
      $scope.columnDef = [
        {
          name: 'app.NAME',
          field: 'name',
          headerCellTemplate: headerCellTemplate()
        },
        {
          name: 'schedule_builder.STATUS',
          field: 'status',
          headerCellTemplate: headerCellTemplate()
        },
        {
          name: 'schedule_builder.START_DATE',
          field: 'startDate',
          headerCellTemplate: headerCellTemplate(),
          cellTemplate: dateCellTemplate()
        },
        {
          name: 'schedule_builder.LENGTH',
          field: 'scheduleLengthInDays',
          headerCellTemplate: headerCellTemplate(),
          cellTemplate: lengthCellTemplate()
        },
        {
          name: 'schedule_builder.TEAMS',
          field: 'teamNames',
          headerCellTemplate: headerCellTemplate()
        },
        {
          name: 'schedule_builder.DESCRIPTION',
          field: 'description',
          headerCellTemplate: headerCellTemplate(),
          visible: false
        },
        {
          name: 'schedule_builder.GENERATION',
          enableSorting: false,
          enableColumnMenu: false,
          field: '',
          headerCellTemplate: headerCellTemplate(),
          cellTemplate: generationCellTemplate()
        },
        {
          name: 'app.ACTION',
          enableSorting: false,
          disableColumnMenu: true,
          disableHiding: true,
          headerCellTemplate: headerCellTemplate(),
          cellClass: 'action-cell',
          cellTemplate: actionCellTemplate()
        }
      ];

      // Row Template
      function rowTemplate() {
        return '<div title="This is row tooltip" ng-dblclick="grid.appScope.extScope.onDblClickRow(row.entity)" ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
      }

      // Action Cell Template

      function actionCellTemplate(row) {
        return '<div style="text-align: left; padding-left: 10px;"> <a ng-click="grid.appScope.extScope.closeWithSchedule(row.entity)" translate>app.OPEN</a> </div>';
        //return '<div style="text-align: left; padding-left: 10px;"> <a href ui-sref="authenticated.schedule_builder.create_schedules({ id: \'' + row.entity.id +'\' }" translate>app.OPEN</a> </div>';
      }

      /**
       * Schedule Length Field Template
       * @returns {string}
       */
      function lengthCellTemplate() {
        return '<div class="ui-grid-cell-contents"> {{COL_FIELD}}  {{"schedule_builder.DAYS" | translate}} </div>';
      }

      /**
       * Start Date Template
       * @returns {string}
       */
      function dateCellTemplate() {
        return '<div class="ui-grid-cell-contents"> {{COL_FIELD | date: "MM/dd/yyyy"}} </div>';
      }

      function generationCellTemplate() {
        return '<div class="ui-grid-cell-contents"> ' +
          '<span ng-if="row.entity.state ==\'Complete\' && row.entity.completion ==\'OK\' "><i class="fa fa-check"></i></span>' +
          '<span ng-if="row.entity.state ==\'Complete\' && (row.entity.completion ==\'Error\' || row.entity.completion ==\'Aborted\') "><i class="fa fa-remove"></i></span>' +
          '</div>';
      }

      $scope.gridOptions = {
        useExternalSorting: true,  // It will call backend api
        enableColumnResizing: true,
        enableGridMenu: true,
        enableColumnMenus: true,
        gridMenuTitleFilter: $translate, // Translate Grid Menu column name
        columnDefs: angular.copy($scope.columnDef),
        rowTemplate: rowTemplate(), //Row Template
        onRegisterApi: function (gridApi) {
          $scope.gridApi = gridApi;
          $scope.gridApi.core.on.sortChanged($scope, function (grid, sortColumns) {
            if (sortColumns.length === 0) {
              console.log('Impersonation column length : 0');
            }
            else {

              if (sortColumns[0].field == "id") {
                $scope.browser.orderBy = "primaryKey.id";

              }
              else {
                $scope.browser.orderBy = sortColumns[0].field;
              }


              if (sortColumns[0].sort.direction == uiGridConstants.DESC) {
                $scope.browser.orderDir = "DESC";
              }
              else {
                $scope.browser.orderDir = "ASC";
              }

              // search again
              $scope.listSchedules();

            }
          });
        }
      };


      // External Scope Module
      $scope.extScope = {

        closeWithSchedule: function (schedule) {
          console.log('--> Open Schedule' + schedule.name);
          $modalInstance.close(schedule);
        },
        onDblClickRow: function (schedule) {
          // On a double click event , it will move to employee detail page to show employee detail
          $modalInstance.close(schedule);
        }
      };

      // Prepare Page Offset and Limit
      $scope.preparePageParam = function () {

        $scope.browser.offset = ($scope.browser.currentPage - 1) * $scope.browser.pageSize;
        $scope.browser.limit = $scope.browser.pageSize;
      };

      // Keydown event on page user filter input box
      $scope.userFilterKeyDown = function ($event) {

        //detect Enter Key
        if ($event.keyCode == 13) {
          $scope.listSchedules();
        }
      };

      // change page and refresh view
      $scope.pageChanged = function () {
        $scope.listSchedules();
      };

      // Auto Complete List
      $scope.getName = function (val) {


        var filter = "name like '%" + $scope.browser.filterTxt + "%'";

        if (scheduleFilter.length > 0 ){
          filter = filter + ';' + scheduleFilter;
        }

        var params = {
          filter: filter
        };

        var schedulesListUrl = '../emlogis/rest/schedules/ops/query?limit=0';


        return $http.get(schedulesListUrl, {params: params}).then(function (response) {

          var resultSet = factory.toResultSet(response);

          return resultSet.data.map(function (item) {
            return item.name;
          });

        });
      };

      // Call Search function when dialog box loads
      /**
       * Load all schedules and it will be used in selector
       */
      $scope.listSchedules();

    }
  ]);
