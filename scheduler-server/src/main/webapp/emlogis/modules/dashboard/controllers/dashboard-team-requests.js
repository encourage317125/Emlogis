var dashboard = angular.module('emlogis.dashboard');

dashboard.controller('DashboardTeamRequestsCtrl',
  [
    '$http',
    '$scope',
    '$state',
    '$q',
    '$filter',
    '$sessionStorage',
    '$timeout',
    'appFunc',
    'applicationContext',
    'crudDataService',
    'uiGridConstants',
    'DashboardService',
    function($http, $scope, $state, $q, $filter, $sessionStorage, $timeout,
             appFunc, applicationContext, crudDataService,
             uiGridConstants, DashboardService) {

      /**
       * Variables in Dashboard Approvals
       * 1. $scope.days: Filter Dropdown list in Filter
       * 2. $scope.filter.siteTeams: Filter Dropdown tree list in Filter
       * 3. $scope.filter.statuses: Filter Dropdown Statuses
       * 4. $scope.filter.dayLength: Filter ng-model of days
       */

      $scope.days = [
        {day: 7, title: 'LAST 7 DAYS'},
        {day: 14, title: 'LAST 14 DAYS'},
        {day: 30, title: 'LAST 30 DAYS'},
        {day: 0, title: 'ALL'}
      ];

      var baseUrl = applicationContext.getBaseRestUrl();
      var factory = _.clone(crudDataService);
      var numOfRows = 25;

      /*
       *This header cell template is almost same as default cell template except
       * it has translate directive inside so grid header will support i18n as well
       */
      var headerCellTemplate = function(){
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

      // Row Template: Homepage Dashboard Manager Request Approval
      function rowTemplate() {

        return '<div ng-class="{\'row-hovered\' : hover}" ' +
          '     ng-mouseenter="hover = true" ' +
          '     ng-mouseleave="hover = false" ' +
          '     ng-style="{ \'font-weight\': row.entity.isRead !== true ? \'bold\' : \'normal\' }" ' +
//            '     ng-click="grid.appScope.loadCurRequest(row.entity.requestId)">' +
          '     >' +
          '  <div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" ' +
          '       class="ui-grid-cell" ' +
          '       ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }"  ' +
          '       ui-grid-cell>' +
          '  </div>' +
          '</div>';
      }

      // Header Template
      function headerTemplate() {
        return '<div ng-style="{ height: col.headerRowHeight }" ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngHeaderCell" ng-header-cell></div>';
      }


      $scope.loadRequests = function(pageNum) {

        //[POST] /requests/peer/ops/query
        var deferred = $q.defer();
        var selectedStatusIds = [];
        var selectedReqTypesIds = [];
        var startTime = null;
        var requestUrl = 'requests/peer/ops/query';


        _.each($scope.filter.selectedStatuses, function(status) {selectedStatusIds.push(status.id);});

        if ($scope.filter.reqTypes.wip && $scope.filter.reqTypes.wip.ticked === true ) {
          selectedReqTypesIds.push('WIP_REQUEST');
        }

        if ($scope.filter.reqTypes.swap && $scope.filter.reqTypes.swap.ticked === true ) {
          selectedReqTypesIds.push('SHIFT_SWAP_REQUEST');
        }

        if ($scope.filter.dayLength.day !== 0) {
          startTime = moment().subtract($scope.filter.dayLength.day, 'days').toDate().getTime();  // subtracts days
        }


        if (!pageNum) {
          pageNum = 1;
        }

        var urlParam = factory.prepareQueryParams($scope.gridOptions.queryParams,pageNum, numOfRows);

        if (selectedReqTypesIds.length === 0) {
          selectedReqTypesIds = null;
        }

        if (selectedStatusIds.length === 0) {
          selectedStatusIds = null;
        }

        var param = {
          types: selectedReqTypesIds,
          statuses: selectedStatusIds,
          dateFrom: startTime,
          dateTo: null,
          fullTextSearch: $scope.filter.searchTxt,
          offset: urlParam.offset,
          limit: urlParam.limit,
          orderBy: urlParam.orderby,
          orderDir: urlParam.orderdir.toUpperCase()
        };

        console.log(urlParam);

        $http.post(
          baseUrl+requestUrl, param)
          .then(function (response) {

            var approvals = response.data;

//            / Convert appropriate result.
            angular.forEach(approvals.data, function(entity) {

              entity.submitted=entity.submitionDate;
              entity.reqDate=entity.eventDate;

              //iterate columns
              angular.forEach($scope.columnDef, function(column) {

                if (column.field === 'type') {
                  entity[column.field] = $filter('translate')(column.transPrefix+'.'+entity[column.field]);
                }
                else if (column.field === 'status') {
                  if ('PEER_APPROVED' === entity[column.field]) {
                    entity[column.field] = $filter('translate')('app.ACCEPTED');
                  }
                  else {
                    entity[column.field] = $filter('translate')(column.transPrefix+'.'+entity[column.field]);
                  }

                }

                if (!column.dateFormat) {
                  return;
                }

                var timezoneDate = appFunc.convertToBrowserTimezone(entity[column.field], $scope.currentAccountInfo.timezone);
                entity[column.field] = $filter('date')(timezoneDate, column.dateFormat);

              });
            });

            $scope.gridOptions.data = approvals.data;
            $scope.gridOptions.totalItems = approvals.total;

            deferred.resolve(approvals.data);

            //clear curDetail View
            $scope.curRequest = null;

          }, function(error) {
            deferred.reject(error);
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
          });

        return deferred.promise;
      };

      /**
       * Load Statues from backend: team requests
       * @returns {*}
       */
      $scope.loadStatuses = function() {

        var deferred = $q.defer();
        $http.get(baseUrl + 'workflow/dashboard/statuses', {})
          .then(function (response) {
            var result = response.data;
            $scope.filter.statuses = result.data;

            _.each($scope.filter.statuses, function(status) {status.ticked = true;});

            deferred.resolve(response.data);

          }, function(error) {
            deferred.reject(error);
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
          });

        return deferred.promise;
      };



      $scope.loadCurRequest = function(requestId) {

        var deferred = $q.defer();
        $http.get(baseUrl + 'requests/peer/'+requestId, {})
          .then(function (response) {

            var result = response.data;

            $scope.curRequest = result.data;

            if ($scope.curRequest) {
              /**
               * Converts the Dates
               */
              $scope.curRequest.dateOfAction = appFunc.convertToBrowserTimezone($scope.curRequest.dateOfAction, $scope.currentAccountInfo.timezone);
              $scope.curRequest.eventDate = appFunc.convertToBrowserTimezone($scope.curRequest.eventDate, $scope.currentAccountInfo.timezone);
              $scope.curRequest.expirationDate = appFunc.convertToBrowserTimezone($scope.curRequest.expirationDate, $scope.currentAccountInfo.timezone);
              $scope.curRequest.submitDate = appFunc.convertToBrowserTimezone($scope.curRequest.submitDate, $scope.currentAccountInfo.timezone);

              //update isread
              $scope.curRequest.isRead = true;

              if ($scope.curRequest.commentary.commentary) {
                _.each($scope.curRequest.commentary.commentary, function(comment) {
                  comment.datetime = appFunc.convertToBrowserTimezone(comment.datetime, $scope.currentAccountInfo.timezone);
                });
              }

              /**
               * Convert Commentary Date to Proper Timezone
               */
              if ($scope.curRequest.submitterShift) {
                $scope.curRequest.submitterShift.endDateTime= appFunc.convertToBrowserTimezone($scope.curRequest.submitterShift.endDateTime, $scope.currentAccountInfo.timezone);
                $scope.curRequest.submitterShift.startDateTime = appFunc.convertToBrowserTimezone($scope.curRequest.submitterShift.startDateTime, $scope.currentAccountInfo.timezone);
              }


              if ($scope.curRequest.type === 'SHIFT_SWAP_REQUEST' || $scope.curRequest.type === 'WIP_REQUEST') {
//                 Load EligibleTeammates

                /**
                 * Convert the timezones
                 */
                var counts = _.countBy($scope.curRequest.recipients, function (recipient) {

                  if ($scope.curRequest.type === 'SHIFT_SWAP_REQUEST' && recipient.status!='PEER_PENDING') {
                    recipient.checked = true;
                  }

                  recipient.dateActed = appFunc.convertToBrowserTimezone(recipient.dateActed, $scope.currentAccountInfo.timezone);

                  if (recipient.recipientShift) {
                    recipient.recipientShift.endDateTime = appFunc.convertToBrowserTimezone(recipient.recipientShift.endDateTime, $scope.currentAccountInfo.timezone);
                    recipient.recipientShift.startDateTime = appFunc.convertToBrowserTimezone(recipient.recipientShift.startDateTime, $scope.currentAccountInfo.timezone);
                  }

                  if (recipient.status === 'PEER_APPROVED')
                    return 'accepted';
                });

                $scope.curRequest.acceptedRecipientCount = counts.accepted ? counts.accepted: 0;

              }
              else if ($scope.curRequest.type === 'TIME_OFF_REQUEST') {

                _.each($scope.curRequest.shifts, function(shift) {
                  shift.action = 'DROP_SHIFT';//select Drop by default
                  shift.endDateTime = appFunc.convertToBrowserTimezone(shift.endDateTime, $scope.currentAccountInfo.timezone);
                  shift.startDateTime = appFunc.convertToBrowserTimezone(shift.startDateTime, $scope.currentAccountInfo.timezone);
                });
              }
            }

            deferred.resolve(response.data);

          }, function(error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            deferred.reject(error);
          });

        return deferred.promise;
      };

      /**
       * Init function: My Requests
       */
      $scope.init = function() {

        $scope.currentAccountInfo = JSON.parse($sessionStorage.info);

        // ColumnDef Info
        // The columns with empty filed will not work for sort as of now
        $scope.columnDef = [
          {
            name: 'home.REQUESTOR_NAME',
            field: 'submitterName',
            headerCellTemplate: headerCellTemplate(),
            transPrefix: 'home'
          },
          {
            name: 'home.REQ_TYPE',
            field: 'type',
            headerCellTemplate: headerCellTemplate(),
            transPrefix: 'home'
          },
          {
            name: 'home.SUBMITTED',
            field: 'submitDate',
            headerCellTemplate: headerCellTemplate(),
            dateFormat: 'M/d/yy' //
          },
          {
            name: 'home.REQ_DATE',
            field: 'reqDate',
            headerCellTemplate: headerCellTemplate(),
            dateFormat: 'M/d/yy'
          },
          {
            name: 'home.DESCRIPTION',
            field: 'description',


            headerCellTemplate: headerCellTemplate()
          },
          {
            name: 'home.EXPIRES',
            field: 'expirationDate',
            headerCellTemplate: headerCellTemplate(),
            dateFormat: 'M/d/yy'
          },
          {
            name: 'home.STATUS',
            field: 'status',
            transPrefix: 'app',
//            width: '10%',
            headerCellTemplate: headerCellTemplate()
          }
        ];

        // External Scope Module, This variable will be used when we have an action inside grid row
        $scope.gridModel = {

          // On a double click event , it will move to employee detail page to show employee detail
          onClickRow : function(row){
            row.isSelected = true;
          }

        };


        $scope.gridOptions = {

          enableColumnResizing: true,
          enableRowHeaderSelection: false,
          modifierKeysToMultiSelect: false,
          noUnselect: true,
//        enableGridMenu: true,
          minRowsToShow: numOfRows,
          columnDefs: $scope.columnDef, //
          rowTemplate: rowTemplate(), //Row Template,
          enableHorizontalScrollbar: 0,
          enableVerticalScrollbar: 0,
          enableColumnMenus: false,

//        enableFiltering: true,
          multiSelect: false,
          useExternalFiltering: true,

          enableSorting: true,
          useExternalSorting: true,

          needPagination: true,
          useExternalPagination: true,
          enablePaginationControls: false,
          paginationPageSize: numOfRows,
          paginationCurrentPage: 1,
          enableFullRowSelection: true,

          //enableSelectAll: true,
          enableRowSelection: true,
//        enableColumnMenus: true,
//        gridMenuTitleFilter: $translate, // Translate Grid Menu column name

          onRegisterApi: function( gridApi ) {
            $scope.gridApi = gridApi;

            gridApi.selection.on.rowSelectionChanged($scope, function (row) {

              if (row.isSelected) {
                console.log('selected');
                $scope.loadCurRequest(row.entity.requestId)
                  .then(function(response) {
                    $scope.reloadRequestInGrid($scope.curRequest);
                    console.log('one request in grid is updated');
                  }, function(errror) {
                    applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
                    console.log(error);
                  });
              }

            });


            /**
             * Dashboard My Requests Sort Changed
             */

            $scope.gridApi.core.on.sortChanged( $scope, function( grid, sortColumns ) {

              if (sortColumns.length === 0) {
                $scope.gridOptions.queryParams.orderdir = 'asc';
                $scope.gridOptions.queryParams.orderby = 'REQ_TYPE';
              } else {
                $scope.gridOptions.queryParams.orderdir = sortColumns[0].sort.direction;

                switch (sortColumns[0].field) {
                  case "type":
                    $scope.gridOptions.queryParams.orderby = 'REQ_TYPE';
                    break;
                  case "submitDate":
                    $scope.gridOptions.queryParams.orderby = 'SUBMITTED';
                    break;
                  case "reqDate":
                    $scope.gridOptions.queryParams.orderby = 'REQ_DATE';
                    break;
                  case "expirationDate":
                    $scope.gridOptions.queryParams.orderby = 'EXPIRES';
                    break;
                  case "status":
                    $scope.gridOptions.queryParams.orderby = 'REQ_STATUS';
                    break;
                  case "description":
                    $scope.gridOptions.queryParams.orderby = 'DESCRIPTION';
                    break;
                }
              }
              getPage();

            });

            //
            // Back-end pagination

            $scope.gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
              $scope.gridOptions.paginationCurrentPage = newPage;
              getPage();
            });

            var getPage = function() {
              return $scope.loadRequests($scope.gridOptions.paginationCurrentPage);
            };
          }
        };

        $scope.gridOptions.queryParams = {
          orderby:'SUBMITTED',
          orderdir:'DESC'
        };


        $scope.filter = {
          dayLength: null,
          statuses: DashboardService.getTeamRequestStatuses(),
//          reqTypes: DashboardService.getRequestTypes(),
          reqTypes: {
            wip: {
              ticked: true
            },
            swap: {
              ticked: true
            }
          },
          searchTxt: ''
        };



        $scope.filter.dayLength = $scope.days[2]; // load last 30 days

        DashboardService.getCurrentAccountInfo($scope.currentAccountType)
          .then(function(response) {
            if ($scope.currentAccountType === 'Employee') {
              $scope.currentAccountInfo.accountId = response.data.employeeDto.id;
            }
            $scope.currentAccountInfo.timezone = response.data.siteTz.id;
            $scope.currentAccountInfo.siteId = response.data.siteId;
            $scope.currentAccountInfo.siteFirstDayOfweek = response.data.siteFirstDayOfweek;
            $scope.currentAccountInfo.teams = response.data.teams;

            return $scope.loadRequests();
          })
          .catch(function(error) {
            if (error.message) {
              applicationContext.setNotificationMsgWithValues(error.message, '', true, '');
            }
            else {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            }
          });

      };

      /**
       * : My Requests
       */
      $scope.delayedLoadRequests = function() {
        $timeout(function() {$scope.loadRequests();}, 1000); //wait for the dropdown list update their selectedOutputs
      };

      $scope.isTomorrow = function(date){
        var tomorrow = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
        var day = tomorrow.getDate();
        var month = tomorrow.getMonth();
        var year = tomorrow.getFullYear();
        var dateT = new Date(date);

        if (day === dateT.getDate() && month === dateT.getMonth() && year === dateT.getFullYear()) {
          return true;
        }

        return false;


      };

      /**
       * Update Request Status: teamrequests
       * @param status
       */
      $scope.updateRequest = function(status) {


        var url = 'requests/peer/' +$scope.curRequest.requestId +'/ops/'+status;
        var param = {
          type: 'PEER',
          comment: $scope.curRequest.comment
        };

// $scope.curRequest.type === 'WIP_REQUEST'

        if (($scope.curRequest.type === 'SHIFT_SWAP_REQUEST'  ) && 'approve' === status) {
          param.shiftIdList = [];

          _.each($scope.curRequest.recipients, function(recipient) {
              if (true === recipient.checked) {
                param.shiftIdList.push(recipient.recipientShift.id);
              }
            });

        }
//        else if ($scope.curRequest.type === 'WIP_REQUEST') {
//          param.employeeId = $scope.curRequest.curRecipient.peerId;
//        }


        $http.post(
          baseUrl + url , param)
          .then(function (response) {
            /**
             * Reload Status
             */
            if (response.data.status === 'SUCCESS') {
              $scope.loadCurRequest($scope.curRequest.requestId)
                .then(function(response) {
                  $scope.reloadRequestInGrid($scope.curRequest);
                  console.log('one request in grid is updated');
                }, function(error) {
                  applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
                  console.log(error);
                });


            }
            console.log(response);

          }, function(error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            console.log(error);
          });

      };

      /**
       * return boolean whether action buttons can be shown or not.
       * team member requests
       * @param request
       */
      $scope.canShowApproveButton = function(request) {

        if (!request) {
          return false;
        }

        if (request.type=== 'WIP_REQUEST' || request.type=== 'SHIFT_SWAP_REQUEST') {

          if (request.status ==='PEER_PENDING') {
            return true;
          }
          return false;

        }


      };

      /**
       * return boolean whether action buttons can be shown or not.
       * decline button: team member requests
       * @param request
       * @returns {boolean}
       */
      $scope.canShowDeclineButton = function(request) {

        if (!request) {
          return false;
        }

        if (request.status ==='PEER_PENDING') {
          return true;
        }
        return false;

      };

//      /**
//       * Update
//       * @param recipient
//       */
//      $scope.setCurRecipient = function(recipient) {
//        $scope.curRequest.curRecipient = recipient;
//      };

      /**
       *
       * @param request: team requests
       */
      $scope.cancelRequest = function(request) {

//        /requests/submitter/{requestId}?

        return $http.delete(baseUrl + 'requests/peer/' + request.requestId, {})
          .then(function (response) {

            /**
             * Reload Status
             */
            if (response.data.status === 'SUCCESS') {
              $scope.loadCurRequest($scope.curRequest.requestId)
                .then(function(response) {
                  $scope.reloadRequestInGrid($scope.curRequest);
                  console.log('one request in grid is updated');
                }, function(errror) {
                  applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
                  console.log(error);
                });
            }
            else {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            }
            console.log(response);


          }, function(error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            console.log(error);
          });
      };

      /**
       * Update one grid row after approve/deny the request
       */
      $scope.reloadRequestInGrid = function(request) {

        var curRow = _.findWhere($scope.gridOptions.data, {'requestId': request.requestId});

        if (!curRow) {
          applicationContext.setNotificationMsgWithValues("can't find the request in the grid", '', true, 'error');
          return;
        }

        /**
         * update the fields
         */

        curRow.submitterTeamName = request.submitterTeamName;
        curRow.submitterName = request.submitterName;
        curRow.type = $filter('translate')('home.'+request.type);
        curRow.submitDate = $filter('date')(request.submitDate, 'M/d/yy');
        curRow.reqDate = $filter('date')(request.eventDate, 'M/d/yy');
        curRow.description = request.description;
        curRow.expirationDate = $filter('date')(request.expirationDate, 'M/d/yy');

        if ('PEER_APPROVED' === request.status) {
          curRow.status = $filter('translate')('app.ACCEPTED');
        }
        else {
          curRow.status = $filter('translate')('app.'+request.status);
        }
        curRow.isRead = request.isRead;
      };



    }
  ]
);
