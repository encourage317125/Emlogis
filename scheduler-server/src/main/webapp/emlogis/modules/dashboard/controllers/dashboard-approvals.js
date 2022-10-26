var dashboard = angular.module('emlogis.dashboard');

dashboard.controller('DashboardApprovalsTimeOffMsgCtrl',
  [
    '$http',
    '$scope',
    '$modalInstance',
    'shift',
    function($http, $scope, $modalInstance, shift) {

      /**
       * Use shift name
       */
      $scope.shift = shift;
      // Close Modal
      $scope.close = function () {
        $modalInstance.dismiss('cancel');
      };
    }
  ]
);

dashboard.controller('DashboardApprovalsCtrl',
  [
    '$http',
    '$scope',
    '$state',
    '$q',
    '$sessionStorage',
    '$filter',
    '$timeout',
    '$modal',
    'appFunc',
    'applicationContext',
    'dataService',
    'crudDataService',
    'uiGridConstants',
    'DashboardService',
    function($http, $scope, $state, $q, $sessionStorage, $filter, $timeout, $modal,
             appFunc, applicationContext, dataService, crudDataService,
             uiGridConstants, DashboardService) {

      /**
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


      /**
       *
       * For the MultiSelect Dropdown
       */
      $scope.localLang = {
        selectAll  : 'Select All',
        selectNone : 'Select None',
        reset : 'Reset',
        search : 'Search...',
        nothingSelected : 'Nothing is selected'         //default-label is deprecated and replaced with this.
      };

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
            '</div>' ;
      }

      // Header Template
      function headerTemplate() {
        return '<div ng-style="{ height: col.headerRowHeight }" ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngHeaderCell" ng-header-cell></div>';
      }

      // ColumnDef Info
      // The columns with empty filed will not work for sort as of now
      $scope.columnDef = [
        {
          name: 'home.TEAM',
          field: 'submitterTeamName',
          headerCellTemplate: headerCellTemplate()
        },
        {
          name: 'home.EMPLOYEE',
          field: 'submitterName',
          headerCellTemplate: headerCellTemplate()
        },
        {
          name: 'home.START_DATE',
          field: 'employeeStartDate',
          headerCellTemplate: headerCellTemplate(),
          dateFormat: 'M/d/yy'
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
          dateFormat: 'M/d/yy'
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
          width: '20%',
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
           *  Dashboard Manager Approval Sort Changed
           */
          $scope.gridApi.core.on.sortChanged( $scope, function( grid, sortColumns ) {

            if (sortColumns.length === 0) {
              $scope.gridOptions.queryParams.orderdir = 'asc';
              $scope.gridOptions.queryParams.orderby = 'TEAM';
            } else {
              $scope.gridOptions.queryParams.orderdir = sortColumns[0].sort.direction;

              switch (sortColumns[0].field) {
                case "submitterTeamName":
                  $scope.gridOptions.queryParams.orderby = 'TEAM';
                  break;
                case "submitterName":
                  $scope.gridOptions.queryParams.orderby = 'EMPLOYEE';
                  break;
                case "employeeStartDate":
                  $scope.gridOptions.queryParams.orderby = 'EMPLOYEE_START_DATE';
                  break;
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

      //API CALL sites/ops/siteteams
      $scope.loadSiteTeams = function() {

        var deferred = $q.defer();

        factory.getElements('sites/ops/siteteams',{})
          .then(function(res){

            var entities = res.data;

            $scope.filter.siteTeams = [];
            var sites = [];
            var site = null;

            /**
             * Build tree structure
             */
            for (var i=0; i<entities.length; i++) {

              var entity = entities[i];
              site = _.findWhere(sites, {'id': entity.siteId});

              if (!site) {
                site = {id: entity.siteId, name: entity.siteName, teams: []};
                sites.push(site);
              }

              site.teams.push(entity);

            }

            /**
             * Build siteTeams array for dropdown list
             */
            for (i=0; i<sites.length; i++) {
              site = sites[i];

              // group start
              $scope.filter.siteTeams.push({
                name: '<strong>' + site.name + '</strong>',
                siteId: site.id,
                msSite: true
              });

              for (var j=0; j<site.teams.length; j++) {
                var team = site.teams[j];

                $scope.filter.siteTeams.push({
                  name: team.teamName,
                  teamId: team.teamId,
                  ticked: true
                });

              }

              // group end
              $scope.filter.siteTeams.push({
                msSite: false
              });

            }

            deferred.resolve(entities); // it will be not useful purpose

          },function(error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            deferred.reject(error);
          });

        return deferred.promise;

      };

      /**
       * Searches the requests and updates the grid: manager approval
       * @param pageNum
       * @returns {*}
       */
      $scope.loadRequests = function(pageNum) {

        //[POST] /requests/manager/ops/query
        var deferred = $q.defer();
        var selectedTeamIds = [];
        var selectedStatusIds = [];
        var selectedReqTypesIds = [];
        var startTime = null;
        var requestUrl = 'requests/manager/ops/query';

        _.each($scope.filter.selectedSiteTeams, function(team) {
          if (team !== null && team.teamId !== null){
            selectedTeamIds.push(team.teamId);}
          }
        );

        _.each($scope.filter.selectedStatuses, function(status) {selectedStatusIds.push(status.id);});
        _.each($scope.filter.selectedReqTypes, function(type) {selectedReqTypesIds.push(type.id);});

        if ($scope.filter.dayLength.day !== 0) {
          startTime = moment().subtract($scope.filter.dayLength.day, 'days').toDate().getTime();  // subtracts days
        }


        if (!pageNum) {
          pageNum = 1;
        }

        var urlParam = factory.prepareQueryParams($scope.gridOptions.queryParams,pageNum, numOfRows);

        if (selectedTeamIds.length === 0) {
          selectedTeamIds = null;
        }

        if (selectedReqTypesIds.length === 0) {
          selectedReqTypesIds = null;
        }

        if (selectedStatusIds.length === 0) {
          selectedStatusIds = null;
        }

        var param = {
          sites: null,
          teams: selectedTeamIds,
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
                  entity[column.field] = $filter('translate')(column.transPrefix+'.'+entity[column.field]) + ' : ' + entity[column.field];
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

//          clear curDetail View
            $scope.curRequest = null;
          }, function(error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            deferred.reject(error);
          });

        return deferred.promise;
      };


      $scope.loadStatuses = function() {

        //rest/workflow/dashboard/tasks/manager?offset=0&limit=20&orderby=submitted&orderdir=asc
        var deferred = $q.defer();
        $http.get(baseUrl + 'workflow/dashboard/statuses', {})
          .then(function (response) {
            var result = response.data;
            $scope.filter.statuses = result.data;

            _.each($scope.filter.statuses, function(status) {status.ticked = true;});

            deferred.resolve(response.data);

          }, function(error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            deferred.reject(error);
          });

        return deferred.promise;
      };

      $scope.loadReqTypes = function() {
        // WIP_REQUEST , SHIFT_SWAP_REQUEST , OPEN_SHIFT_REQUEST, PTO_REQUEST, AVAILABILITY_REQUEST
        $scope.filter.reqTypes = [
          {
            name:'WIP_REQUEST',
            id:'WIP_REQUEST',
            ticked: true
          },
          {
            name:'SHIFT_SWAP_REQUEST',
            id:'SHIFT_SWAP_REQUEST',
            ticked: true
          },
          {
            name:'OPEN_SHIFT_REQUEST',
            id:'OPEN_SHIFT_REQUEST',
            ticked: true
          },
          {
            name:'PTO_REQUEST',
            id:'PTO_REQUEST',
            ticked: true
          },
          {
            name:'AVAILABILITY_REQUEST',
            id:'AVAILABILITY_REQUEST',
            ticked: true
          }
        ];
//        var deferred = $q.defer();
//        $http.get(baseUrl + 'workflow/dashboard/types?orderby=name&orderdir=ASC', {})
//          .then(function (response) {
//            var result = response.data;
//            $scope.filter.reqTypes = result.data;
//
//            _.each($scope.filter.reqTypes, function(reqType) {reqType.ticked = true;});
//
//            deferred.resolve(response.data);
//
//          }, function(error) {
//            deferred.reject(error);
//          });
//
//        return deferred.promise;

      };

      /**
       * Load specific request detail: manager approvals
       * @param requestId
       * @returns {*}
       */
      $scope.loadCurRequest = function(requestId) {

        var deferred = $q.defer();
        $http.get(baseUrl + 'requests/manager/'+requestId, {})
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
                  recipient.dateActed = appFunc.convertToBrowserTimezone(recipient.dateActed, $scope.currentAccountInfo.timezone);

                  if (recipient.recipientShift) {
                    recipient.recipientShift.endDateTime = appFunc.convertToBrowserTimezone(recipient.recipientShift.endDateTime, $scope.currentAccountInfo.timezone);
                    recipient.recipientShift.startDateTime = appFunc.convertToBrowserTimezone(recipient.recipientShift.startDateTime, $scope.currentAccountInfo.timezone);
                  }

                  if (recipient.status === 'PEER_APPROVED')
                    return 'accepted';
                });

                $scope.curRequest.acceptedRecipientCount = counts.accepted ? counts.accepted: 0;
                $scope.curRequest.showAcceptedRecipientsOnly = true; // default : only shows accepted

              }
              else if ($scope.curRequest.type === 'TIME_OFF_REQUEST') {

                _.each($scope.curRequest.shifts, function(shift) {
                  shift.action = 'DROP_SHIFT';//select Drop by default
                  shift.endDateTime = appFunc.convertToBrowserTimezone(shift.endDateTime, $scope.currentAccountInfo.timezone);
                  shift.startDateTime = appFunc.convertToBrowserTimezone(shift.startDateTime, $scope.currentAccountInfo.timezone);
                });
              }
            }

            deferred.resolve($scope.curRequest);

//            if ($scope.curRequest.isRead === false) { // call mark as api
//              //requests/manager/{reqId}/ops/markas
//
//              return $http.post(
//                baseUrl + 'requests/manager/' + requestId + '/ops/markas', {isUnread: false}
//              );
//            }
//            else {
//
//              throw 'isRead';
//            }

          })
//          .then(function (response) {
//            $scope.curRequest.isRead = true;
//            angular.copy(response.data, applicationContext.getBadgeMsg());
//            deferred.resolve($scope.curRequest);
//          })
          .catch(function(error) {
            if (error === 'isRead') {
              deferred.resolve($scope.curRequest);
            }
            else {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
              deferred.reject(error);
            }
          });

        return deferred.promise;
      };

      /**
       * Init function: manager approval
       */

      $scope.init = function() {


        $scope.filter = {
          dayLength: null,
          statuses: DashboardService.getRequestStatuses(),
          reqTypes: DashboardService.getRequestTypes(),
          searchTxt: ''
        };

        /**
         * enable all reqTypes in manager approval
         */

        $scope.filter.reqTypes[0]['ticked'] = true;
        $scope.filter.reqTypes[1]['ticked'] = true;

        $scope.filter.dayLength = $scope.days[2]; // load last 30 days

        $scope.currentAccountInfo = JSON.parse($sessionStorage.info);

        /**
         * get Current Account Info
         */
//        DashboardService.getCurrentAccountInfo($scope.currentAccountType).then(function(response) {
//          if ($scope.currentAccountType === 'Employee') {
//            $scope.currentAccountInfo.accountId = response.data.employeeDto.id;
//          }
//          $scope.currentAccountInfo.timezone = response.data.siteTz.id;
//          $scope.currentAccountInfo.siteId = response.data.siteId;
//          $scope.currentAccountInfo.siteFirstDayOfweek = response.data.siteFirstDayOfweek;
//          $scope.currentAccountInfo.teams = response.data.teams;
//
//        }, function(err) {
//          applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
//          deferred.reject(error);
//        });


        $scope.loadSiteTeams()
          .then(function() {
            return DashboardService.getCurrentAccountInfo($scope.currentAccountType);
          })
          .then(function(response) {

            if ($scope.currentAccountType === 'Employee') {
              $scope.currentAccountInfo.accountId = response.data.employeeDto.id;
              $scope.currentAccountInfo.timezone = response.data.siteTz.id;
              $scope.currentAccountInfo.siteId = response.data.siteId;
              $scope.currentAccountInfo.siteFirstDayOfweek = response.data.siteFirstDayOfweek;
              $scope.currentAccountInfo.teams = response.data.teams;
            }
            else {
              $scope.currentAccountInfo.timezone = response.data.actualTimeZone.id;
            }

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
       * :Manager Approval
       */
      $scope.delayedLoadRequests = function() {
        $timeout(function() {$scope.loadRequests();}, 1000); //wait for the dropdown list update their selectedOutputs
      };

      /**
       * check today is just one day before to specific date
       * @param date
       * @returns {boolean}
       */
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
       * Update Request Status: Manager Approval
       * @param status
       */
      $scope.updateRequest = function(status) {

        var url = 'requests/manager/' +$scope.curRequest.requestId +'/ops/'+status;
        var param = {
          type: $scope.curRequest.type,
          comment: $scope.curRequest.comment
        };

        if ($scope.curRequest.type === 'SHIFT_SWAP_REQUEST') {

          if (status === 'approve' && !$scope.curRequest.curRecipient) {
            applicationContext.setNotificationMsgWithValues('Please select the recipient', '', true);
            return;
          }
          else if (status === 'approve'){
            param.employeeId = $scope.curRequest.curRecipient.peerId;
            param.shiftId = $scope.curRequest.curRecipient.recipientShift.id;
          }

        }
        else if ($scope.curRequest.type === 'WIP_REQUEST') {

          if (status === 'approve' && !$scope.curRequest.curRecipient) {
            applicationContext.setNotificationMsgWithValues('Please select the recipient', '', true);
            return;
          }
          else if (status === 'approve'){
            param.employeeId = $scope.curRequest.curRecipient.peerId;
          }

        }
        else if ($scope.curRequest.type === 'TIME_OFF_REQUEST') {

          param.shiftActions = [];

          for (var i =0; i<$scope.curRequest.shifts.length; i++) {
            var shift = $scope.curRequest.shifts[i];

            var employeeIds = null;
            var errMsg = '';

            if (shift.action === 'ASSIGN_SHIFT') {
              if (shift.teamMate) {
                employeeIds = [shift.teamMate.employeeId];
              }
              else {
                employeeIds = null;
              }

              errMsg = 'Assign Failed: ' + shift.id;
            }

            else if (shift.action === 'POST_AS_OPEN_SHIFT') {
              employeeIds = _.pluck(shift.eligibleTeammates, 'employeeId');
              errMsg = 'POST Failed: ' + shift.id;
            }

            /**
             * Display Dialog box when no employees selected in POST & FIll option
             */
            if ((shift.action === 'POST_AS_OPEN_SHIFT' || shift.action === 'ASSIGN_SHIFT') && (employeeIds === null || employeeIds.length === 0)) {

              break;

            }


            param.shiftActions.push({
                shiftId: shift.id,
                action: shift.action,
                employeeIds: employeeIds
              }
            );

          }

          /**
           * abnormally returned;
           */
          if (i < $scope.curRequest.shifts.length) {
            var dlg = $modal.open({
              templateUrl: 'modules/dashboard/partials/dashboard_approvals_timeoff_msg_dlg.html',
              controller: 'DashboardApprovalsTimeOffMsgCtrl',
              windowClass: 'dashboard-approvals',
              resolve: {
                shift: function() {
                  return $scope.curRequest.shifts[i];
                }
              }
            });

            return dlg;

          }

        }

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
                }, function(errror) {
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

      $scope.previewChange = function(request) {

        $modal.open({
          templateUrl: 'modules/dashboard/partials/availability_preview_modal.html',
          controller: 'AvailabilityPreviewModalCtrl',
          size: 'lg',
          resolve: {
            employeeId: function() {
              return request.submitterId;
            },
            siteTimeZone: function() {
              return $scope.currentAccountInfo.timezone;
            },
            firstDayOfWeek: function() {
              return $scope.currentAccountInfo.siteFirstDayOfweek;
            },
            previewParams: function() {
              var firstDate;
              if (request.availUpdate.effectiveStartDate) {
                // for ci timeframes
                firstDate = request.availUpdate.effectiveStartDate;
              } else {
                // for cd timeframes
                firstDate = request.availUpdate.selectedDates[0];
              }

              return {
                requestId: request.requestId,
                year: new Date(firstDate).getFullYear(),
                month: new Date(firstDate).getMonth()
              };
            }
          }
        });
      };

      $scope.showPreviewButton = function(request) {
        return request && request.type === "AVAILABILITY_REQUEST";
      };

      /**
       * return boolean whether action buttons can be shown or not.
       * @param request
       */
      $scope.canShowApproveButton = function(request) {

        if (!request) {
          return false;
        }

        if (request.type=== 'WIP_REQUEST' || request.type=== 'SHIFT_SWAP_REQUEST') {

          if (request.status !=='ADMIN_PENDING') {
            return false;
          }

          var approvedRecipient = _.findWhere(request.recipients, {'status': 'PEER_APPROVED'});

          if (approvedRecipient) {
            return true;
          }
          else {
            return false;
          }

        }
        else if (request.type=== 'TIME_OFF_REQUEST' || request.type=== 'OPEN_SHIFT_REQUEST' || request.type=== 'AVAILABILITY_REQUEST') {

          if (request.status ==='ADMIN_PENDING') {
            return true;
          }
          return false;
        }

      };

      /**
       * return boolean whether action buttons can be shown or not.
       * @param request
       * @returns {boolean}
       */
      $scope.canShowDeclineButton = function(request) {

        if (!request) {
          return false;
        }

        if (request.status ==='ADMIN_PENDING') {
          return true;
        }
        return false;

      };

      /**
       * Update
       * @param recipient
       */
      $scope.setCurRecipient = function(recipient) {
        $scope.curRequest.curRecipient = recipient;
      };


      /**
       * Get Request Employees from Shift : Manager Approval
       * @returns {*}
       */
      $scope.loadEligibleTeammates = function(shift) {

        if (shift.eligibleTeammates) {
          /**
           * Do not call again
           */
          return;
        }

        shift.callInProcess = true;

        shift.eligibleTeammates = [];
        var url = 'employees/' + shift.employeeId + '/ops/getwipeligibleemployees';

        var param = {
          shiftId: shift.id
        };

        $http.post(
            baseUrl + url , param)
          .then(function (response) {

            var result = response.data;
            shift.eligibleTeammates = result.eligibleTeammates;
            shift.callInProcess = false;

            /**
             * No Eligible teammates, switch to 'drop'
             */
            if (shift.eligibleTeammates.length < 1) {
              shift.action = 'DROP_SHIFT';
            }

          }, function(error) {
            shift.eligibleTeammates = null;
            shift.callInProcess = false;
            shift.action = 'DROP_SHIFT';
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            console.log(error);
          });


      };

      /**
       * Update one grid row after approve/deny the request: manager approval
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
        curRow.status = $filter('translate')('app.'+request.status) + ' : ' + request.status;
        curRow.isRead = request.isRead;

      };

    }
  ]
);
