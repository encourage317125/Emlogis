var dashboard = angular.module('emlogis.dashboard');

dashboard.controller('DashboardCtrl',
  [
    '$scope',
    '$state',
    '$q',
    '$http',
    '$sessionStorage',
    'applicationContext',
    'crudDataService',
    'uiGridConstants',
    'DashboardService',
    'authService',
    function ($scope, $state, $q, $http, $sessionStorage, applicationContext, crudDataService,
              uiGridConstants, DashboardService, authService) {

      console.log('dashboard controller');

      // Entities List Url

      //$scope.entitiesListUrl = "../emlogis/rest/employees"
      var baseUrl = applicationContext.getBaseRestUrl();
      $scope.entityResource = "employees";
      $scope.filterField = "lastName";

      /* Link scope browser variable with globalproperties so that whenever there is an update
       * from entitiesListCtrl it will reflect this Controller browser variable as well.
       */
      $scope.browser = applicationContext.getBrowser();

      /*
       * Multi Select All Dropdown list
       */

      $scope.button = {
        toggle: false

      };

      $scope.filter = {
        activeEmployees: false
      };


      $scope.tabs = [
        {
          heading: 'home.MY_CALENDAR',
          selected: true,
          route: 'authenticated.dashboard.calendar'
        }, {
          heading: 'home.MANAGER_APPROVALS',
          selected: false,
          route: 'authenticated.dashboard.manager_approvals'
        }, {
          heading: 'home.TEAM_MEMBER_REQUESTS',
          selected: false,
          route: 'authenticated.dashboard.team_member_requests'
        }, {
          heading: 'home.MY_REQUESTS',
          selected: false,
          route: 'authenticated.dashboard.my_requests'
        }, {
          heading: 'home.MY_AVAILABILITY',
          selected: false,
          route: 'authenticated.dashboard.my_availability'
        }
      ];

      $scope.tabs.forEach(function (tab) {
        tab.visible = $scope.hasStatePermissionsByName(tab.route);
      });

      //$scope.selectedTab = $scope.tabs[0];

      $scope.selectSubTab = function (tab) {
        $state.go(tab.route);
      };

      $scope.active = function (route) {
        return $state.is(route);
      };

      $scope.$on("$stateChangeSuccess", function () {
        $scope.selectedTab = null;
        $scope.tabs.forEach(function (tab) {
          tab.selected = $scope.active(tab.route);
          if (tab.selected && !$scope.selectedTab) {
            $scope.selectedTab = tab;
          }
        });
      });

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
      // The columns with empty filed will not work for sort as of now
      $scope.columnDef = [
        {
          name: 'app.LAST_NAME',
          field: 'lastName',
          headerCellTemplate: headerCellTemplate()
        },
        {
          name: 'app.FIRST_NAME',
          field: 'firstName',
          headerCellTemplate: headerCellTemplate()
        },
        {
          name: 'Primary Job Role',
          field: ''
        },
        {
          name: 'Home Site',
          field: ''
        },
        {
          name: 'Home Team',
          field: ''
        },
        {
          name: 'Primary Shift',
          field: ''
        },
        {
          name: 'Hire Date',
          field: ''
        },
        {
          name: 'Work Email',
          field: ''
        },
        {
          name: 'Mobile Phone',
          field: ''
        }
      ];


      // External Scope Module, This variable will be used when we have an action inside grid row
      $scope.gridModel = {

        // On a double click event , it will move to employee detail page to show employee detail
        onDblClickRow: function (entity) {
          $state.go('authenticated.employees.detail', {id: entity.id});
        }

      };

      $scope.currentAccountInfo = JSON.parse($sessionStorage.info);
      $scope.currentAccountType = '';
      if ($scope.currentAccountInfo.roles.employeerole) {
        $scope.currentAccountType = 'Employee';
      } else {
        $scope.tabs[0].visible = false;
      }
      $scope.teamMemberRequests = [];
      $scope.teamMemberRequestsLoaded = false;

      $scope.getAccountInfo = function () {
        var deferred = $q.defer();

        DashboardService.getCurrentAccountInfo($scope.currentAccountType).then(function (response) {
          if ($scope.currentAccountType === 'Employee') {
            $scope.currentAccountInfo.accountId = response.data.employeeDto.id;
          }
          $scope.currentAccountInfo.timezone = response.data.siteTz.id;
          $scope.currentAccountInfo.calendarSyncUrl = response.data.calendarSyncUrl;
          $scope.currentAccountInfo.siteId = response.data.siteId;
          $scope.currentAccountInfo.siteFirstDayOfweek = response.data.siteFirstDayOfweek;
          $scope.currentAccountInfo.teams = response.data.teams;
          deferred.resolve($scope.currentAccountInfo);
        }, function (err) {
          deferred.reject(err);
        });

        return deferred.promise;
      };

      $scope.getAccountAvailability = function (startDate, endDate) {
        var options = {
          scheduleStatus: 'Posted',
          startDate: startDate,
          endDate: endDate
        };

        if ($scope.currentAccountType === 'Employee') {
          options.returnedFields = 'empUnavailabilities, orgHolidays';
        }

        return DashboardService.getCurrentAccountAvailability($scope.currentAccountType, options);
      };

      $scope.getAccountShifts = function (startDate, endDate) {
        var options = {
          scheduleStatus: 'Posted',
          startDate: startDate,
          endDate: endDate
        };

        if ($scope.currentAccountType === 'Employee') {
          options.returnedFields = 'id,startDateTime,endDateTime,excess,skillAbbrev,skillName,teamName';
        }

        return DashboardService.getAccountShifts($scope.currentAccountType, $scope.currentAccountInfo.accountId, options);
      };

      $scope.getAccountAvailabilityAndShifts = function (startDate, endDate) {
        var options = {
          scheduleStatus: 'Posted',
          startDate: startDate,
          endDate: endDate,
          timezone: $scope.currentAccountInfo.timezone
        };

        if ($scope.currentAccountType === 'Employee') {
          options.returnedFields = 'id,startDateTime,endDateTime,excess,skillAbbrev,skillName,teamName,comment';
        }

        return DashboardService.getAccountAvailabilityAndShifts($scope.currentAccountType, $scope.currentAccountInfo.accountId, options);
      };

      $scope.getEligibleEntities = function (tabType, shiftId) {
        return DashboardService.getEligibleEntities(tabType, $scope.currentAccountType,
          $scope.currentAccountInfo.accountId, shiftId);
      };

      $scope.processSubmittedEligibleEntities = function (tabType, originatorShift, selectedEligibleEntities) {
        return DashboardService.submitSelectedEligibleEntities(tabType, $scope.currentAccountType,
          $scope.currentAccountInfo.accountId, originatorShift, selectedEligibleEntities);
      };

      $scope.getAccountOpenShifts = function (startDate, endDate) {
        var options = {
          startDate: startDate,
          endDate: endDate
        };

        return DashboardService.getCurrentAccountOpenShifts($scope.currentAccountType, options);
      };

      $scope.processSubmittedOpenShifts = function (tickInfo, selectedOpenShifts) {
        return DashboardService.submitSelectedOpenShifts($scope.currentAccountType,
          $scope.currentAccountInfo.accountId, tickInfo, selectedOpenShifts);
      };

      $scope.processSubmittedPtoRequest = function (requestData) {
        return DashboardService.submitPtoRequest($scope.currentAccountType,
          $scope.currentAccountInfo.accountId, requestData);
      };

      $scope.teamMemberRequestsShowConf = {
        sortOptions: {
          by: 'SUBMITTED',
          order: 'DESC'
        },
        filterOptions: {
          period: 'ALL',
          status: 'ALL'
        }
      };

      $scope.teamMemberRequestsPaginationOptions = {
        totalItems: 0,
        paginationPageSize: 20,
        paginationCurrentPage: 1,
        numPages: 0
      };

      $scope.$watch('teamMemberRequestsShowConf.sortOptions.by', function (newValue, oldValue) {
        if (typeof oldValue !== 'undefined' && oldValue !== null && newValue !== oldValue) {
          $scope.getAccountTeamMemberRequests();
        }
      });

      $scope.$watch('teamMemberRequestsShowConf.sortOptions.order', function (newValue, oldValue) {
        if (typeof oldValue !== 'undefined' && oldValue !== null && newValue !== oldValue) {
          $scope.getAccountTeamMemberRequests();
        }
      });

      $scope.$watch('teamMemberRequestsShowConf.filterOptions.period', function (newValue, oldValue) {
        if (typeof oldValue !== 'undefined' && oldValue !== null && newValue !== oldValue) {
          $scope.getAccountTeamMemberRequests();
        }
      });

      $scope.$watch('teamMemberRequestsShowConf.filterOptions.status', function (newValue, oldValue) {
        if (typeof oldValue !== 'undefined' && oldValue !== null && newValue !== oldValue) {
          $scope.getAccountTeamMemberRequests();
        }
      });

      $scope.getStatusLabelOfTeamMemberRequest = function (status) {
        return DashboardService.getStatusLabelOfTeamMemberRequest(status);
      };

      $scope.getAccountTeamMemberRequests = function () {
        var statusesVal = null;
        var dateFromVal = null;
        var dateToVal = null;
        var orderByVal = null;
        var orderDirVal = null;

        if ($scope.teamMemberRequestsShowConf.filterOptions.status !== 'ALL') {
          statusesVal = DashboardService.getStatusValuesOfTeamMemberRequests($scope.teamMemberRequestsShowConf.filterOptions.status);
        }

        var currentDate = moment.tz(new Date().getTime(), $scope.currentAccountInfo.timezone);
        if ($scope.teamMemberRequestsShowConf.filterOptions.period === 'LAST_MONTH') {
          dateFromVal = currentDate.month(currentDate.month() - 1).date(1).unix() * 1000;
        } else if ($scope.teamMemberRequestsShowConf.filterOptions.period === 'LAST_WEEK') {
          dateFromVal = currentDate.day(-7).unix() * 1000;
        }

        orderByVal = $scope.teamMemberRequestsShowConf.sortOptions.by;
        orderDirVal = $scope.teamMemberRequestsShowConf.sortOptions.order;

        var payLoad = {
          types: ['SHIFT_SWAP_REQUEST', 'WIP_REQUEST'],
          statuses: statusesVal,
          dateFrom: dateFromVal,
          dateTo: dateToVal,
          fullTextSearch: null,
          limit: $scope.teamMemberRequestsPaginationOptions.paginationPageSize,
          offset: ($scope.teamMemberRequestsPaginationOptions.paginationCurrentPage - 1) * $scope.teamMemberRequestsPaginationOptions.paginationPageSize,
          orderBy: orderByVal,
          orderDir: orderDirVal
        };

        $scope.teamMemberRequestsLoaded = false;
        DashboardService.getAccountTeamMemberRequests(payLoad).then(function (response) {
          $scope.teamMemberRequestsPaginationOptions.totalItems = response.data.total;
          $scope.teamMemberRequests = [];
          angular.forEach(response.data.data, function (teamMemberRequest) {
            teamMemberRequest.isCollapsed = true;

            teamMemberRequest.submitterShift.start = teamMemberRequest.submitterShift.startDateTime;
            teamMemberRequest.submitterShift.end = teamMemberRequest.submitterShift.endDateTime;
            teamMemberRequest.submitterShift.start = moment.tz(teamMemberRequest.submitterShift.start, $scope.currentAccountInfo.timezone);
            teamMemberRequest.submitterShift.end = moment.tz(teamMemberRequest.submitterShift.end, $scope.currentAccountInfo.timezone);
            teamMemberRequest.submitterShift.date = teamMemberRequest.submitterShift.start.format('MMMM DD, YYYY');
            teamMemberRequest.submitterShift.timeDuration = teamMemberRequest.submitterShift.start.format('hh A') +
              ' - ' + teamMemberRequest.submitterShift.end.format('hh A');
            if (teamMemberRequest.type === 'SHIFT_SWAP_REQUEST' && typeof teamMemberRequest.recipientShift !== 'undefined' && teamMemberRequest.recipientShift !== null) {
              teamMemberRequest.recipientShift.start = teamMemberRequest.recipientShift.startDateTime;
              teamMemberRequest.recipientShift.end = teamMemberRequest.recipientShift.endDateTime;
              teamMemberRequest.recipientShift.start = moment.tz(teamMemberRequest.recipientShift.start, $scope.currentAccountInfo.timezone);
              teamMemberRequest.recipientShift.end = moment.tz(teamMemberRequest.recipientShift.end, $scope.currentAccountInfo.timezone);
              teamMemberRequest.recipientShift.date = teamMemberRequest.recipientShift.start.format('MMMM DD, YYYY');
              teamMemberRequest.recipientShift.timeDuration = teamMemberRequest.recipientShift.start.format('hh A') +
                ' - ' + teamMemberRequest.recipientShift.end.format('hh A');
            }
            teamMemberRequest.requestDateFormatted = moment.tz(teamMemberRequest.submitDate, $scope.currentAccountInfo.timezone).format('MM/DD/YYYY');
            $scope.teamMemberRequests.push(teamMemberRequest);
          });
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        }).finally(function () {
          $scope.teamMemberRequestsLoaded = true;
        });
      };

      //$scope.$watch('selectedTab', function () {
      //  if ($scope.selectedTab === $scope.tabs[2]) {
      //    $scope.getAccountTeamMemberRequests();
      //  }
      //});

      $scope.respondToAccountTeamMemberRequest = function (teamMemberRequest, acceptance) {
        DashboardService.respondToAccountTeamMemberRequest(teamMemberRequest, acceptance).then(function (response) {
          applicationContext.setNotificationMsgWithValues('home.REQUEST_SUBMITTED_SUCCESSFULLY', 'success', true);
          if (acceptance) {
            teamMemberRequest.status = response.data.data.status;
          } else {
            $scope.getAccountTeamMemberRequests();
          }
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        });
      };

      $scope.hasPermission = function (perm) {
        return authService.hasPermission(perm);
      };

      $scope.hasPermissionIn = function (perms) {
        return authService.hasPermissionIn(perms);
      };

      $scope.badgeMsg = applicationContext.getBadgeMsg();


      // Save Filter Info as GlobalPropertiesV1brant15!
      var entityFilter = applicationContext.getEntityFilter();
      entityFilter.url = $scope.entitiesListUrl;
      entityFilter.field = $scope.filterField;
      applicationContext.setEntityFilter(entityFilter);
    }
  ]
);
