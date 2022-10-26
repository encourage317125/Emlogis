angular.module('emlogis.employees').controller('EmployeesListCtrl',
    [
      '$scope', '$state', '$filter', '$cacheFactory', '$timeout', 'dataService', 'crudDataService',
      function($scope, $state, $filter, $cacheFactory, $timeout, dataService, crudDataService) {

        var numOfRows = 25,
            cache = $cacheFactory.get('EmployeesListCache') || $cacheFactory('EmployeesListCache'),
            cachedPaginationCurrentPage = cache.get("paginationCurrentPage"),
            cachedQueryParams = cache.get("queryParams"),
            cachedSelectedTeamIds = cache.get("selectedTeamIds");

        $scope.sites = [];
        $scope.teams = [];
        $scope.activityTypes = cache.get("selectedActivityTypes") || [
          { id: 1, name: $filter('translate')("employees.ACTIVE"), ticked: true},
          { id: 0, name: $filter('translate')("employees.INACTIVE"), ticked: false},
          { id: 2, name: $filter('translate')("employees.POOLED"), ticked: true}
        ];
        $scope.nameFilter = cachedQueryParams ? cachedQueryParams.employeenamefilter : null;

        $scope.selectedSites = [];
        $scope.selectedTeams = [];
        $scope.selectedActivityTypes = [];

        $scope.gridOptions = {
          data: [],
          totalItems: 0,
          minRowsToShow: 0,

          enableHorizontalScrollbar: 0,
          enableVerticalScrollbar: 0,
          enableColumnMenus: false,
          enableGridMenu: true,

          enableFiltering: true,
          useExternalFiltering: true,

          enableSorting: true,
          useExternalSorting: true,

          needPagination: true,
          useExternalPagination: true,
          enablePaginationControls: false,
          paginationPageSize: numOfRows,
          paginationCurrentPage: 1,

          rowTemplate: rowTemplate(),
          columnDefs: [
            { name: $filter('translate')("app.LAST_NAME"),              field: 'lastName',         enableFiltering: false },
            { name: $filter('translate')("app.FIRST_NAME"),             field: 'firstName',        enableFiltering: false },
            { name: $filter('translate')("employees.PRIMARY_JOB_ROLE"), field: 'primaryJobRole',   enableFiltering: false },
            { name: $filter('translate')("employees.HOME_SITE"),        field: 'homeSite',         enableFiltering: false },
            { name: $filter('translate')("employees.HOME_TEAM"),        field: 'homeTeam',         enableFiltering: false },
            { name: $filter('translate')("employees.HIRE_DATE"),        field: 'hireDate',         enableFiltering: false, cellFilter: 'date' },
            { name: $filter('translate')("employees.WORK_EMAIL"),       field: 'workEmail',        enableFiltering: false },
            { name: $filter('translate')("employees.MOBILE_PHONE"),     field: 'mobilePhone',      enableFiltering: false },
            { name: $filter('translate')("app.STATUS"),                 field: 'activityType',     enableFiltering: false }
          ],
          onRegisterApi: function(gridApi) {
            $scope.gridApi = gridApi;
            $scope.gridOptions.queryParams = cachedQueryParams || {
              orderby:'lastName',
              orderdir:'ASC'
            };

            var _this = this,
                fieldToSort = _.find(_this.columnDefs, function(c) {
                  return c.field == $scope.gridOptions.queryParams.orderby;
                });

            // Show sorting arrow in grid header
            fieldToSort.sort = {
              direction: $scope.gridOptions.queryParams.orderdir.toLowerCase()
            };

            // Back-end sorting
            $scope.gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
              if (sortColumns.length === 0) {
                $scope.gridOptions.queryParams.orderdir = 'ASC';
                $scope.gridOptions.queryParams.orderby = 'lastName';
              } else {
                $scope.gridOptions.queryParams.orderdir = sortColumns[0].sort.direction;
                $scope.gridOptions.queryParams.orderby =  sortColumns[0].field;
              }
              getPage();
            });

            // Back-end pagination
            $scope.gridApi.pagination.on.paginationChanged($scope, function (newPage) {
              $scope.gridOptions.paginationCurrentPage = newPage;
              getPage();
            });
          }
        };

        function rowTemplate() {
          return '<div ng-class="{\'row-hovered\' : hover}" ' +
                 '     ng-mouseenter="hover = true" ' +
                 '     ng-mouseleave="hover = false" ' +
                 '     ng-click="grid.appScope.selectEmployee(row.entity.id)">' +
                 '  <div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" ' +
                 '       class="ui-grid-cell" ' +
                 '       ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }"  ' +
                 '       ui-grid-cell>' +
                 '  </div>' +
                 '</div>';
        }

        $scope.selectEmployee = function(employeeId) {
          $state.go('authenticated.employees.detail', {id: employeeId});
        };

        $scope.haveEmployees = function() {
          return ($scope.gridOptions.data && $scope.gridOptions.totalItems > 0);
        };

        function getPage() {

          var teamIds = -1,
              activityTypeIds = -1;

          if (!_.isEmpty($scope.selectedSites)) {
            cache.put("selectedSiteId", _.pluck($scope.selectedSites, 'id'));
          }

          if (!_.isEmpty($scope.selectedTeams)) {
            teamIds = _.pluck($scope.selectedTeams, 'id');
            cache.put("selectedTeamIds", teamIds);
            teamIds = teamIds.join();
          }

          if (!_.isEmpty($scope.selectedActivityTypes)) {
            activityTypeIds = _.pluck($scope.selectedActivityTypes, 'id').join();
            cache.put("selectedActivityTypes", $scope.activityTypes);
          }

          $scope.gridOptions.queryParams.teamids = teamIds;
          $scope.gridOptions.queryParams.belonging = "Both";
          $scope.gridOptions.queryParams.activitytypefilter = activityTypeIds;
          $scope.gridOptions.queryParams.employeenamefilter = $scope.nameFilter || null;

          return dataService.getEmployees($scope.gridOptions.queryParams, $scope.gridOptions.paginationCurrentPage, numOfRows)
            .then(function(res) {
              $scope.gridOptions.data = res.data;
              $scope.gridOptions.totalItems = res.total;
              $scope.gridOptions.minRowsToShow = res.total < numOfRows ? res.total : numOfRows;

              cache.put("queryParams", $scope.gridOptions.queryParams);
              cache.put("paginationCurrentPage", $scope.gridOptions.paginationCurrentPage);
              setTimeout(function() {
                $(window).resize();
              });
            });
        }

        // Load teams into dropdown list based on the siteId
        $scope.loadTeamsForSite = function(siteId) {

          // for reset button
          if (!siteId) {
            siteId = _.find($scope.sites, 'ticked', true).id;
          }

          return crudDataService.getElements('sites/' + siteId + '/teams')
            .then(function(res) {
              $scope.teams = res.data;

              // if returned 'back to listing'
              if (cachedSelectedTeamIds) {
                _.each($scope.teams, function(t) {
                  t.ticked = _.includes(cachedSelectedTeamIds, t.id);
                });
                $scope.selectedTeams = _.filter($scope.teams, 'ticked', true);
                cachedSelectedTeamIds = null;
              } else {
                // if it's regular site selection from selectbox
                _.each($scope.teams, function(t) {
                  t.ticked = true;
                });
              }
           });
        };

        function init() {
          // Load sites into dropdown list
          crudDataService.getElements("sites", {}, 1, -1)
            .then(function(res) {
              $scope.sites = res.data;

              if (_.isEmpty($scope.sites)) return;

              var selectedSiteId = cache.get("selectedSiteId") || $scope.sites[0].id,
                  selectedSite = _.find($scope.sites, function(s) {
                    return s.id == selectedSiteId;
                  });

              selectedSite.ticked = true;
              return $scope.loadTeamsForSite(selectedSite.id);
            })
            .then(function() {
              if (cachedPaginationCurrentPage) {
                $scope.gridOptions.paginationCurrentPage = cachedPaginationCurrentPage;
              }
              return getPage();
            })
            .then(function() {
              $scope.$watch('selectedTeams', function(oldVal, newVal) {
                if (!_.isEqual(oldVal, newVal)) {
                  getPage();
                }
              });

              $scope.$watch('selectedActivityTypes', function(oldVal, newVal) {
                if (!_.isEqual(oldVal, newVal)) {
                  getPage();
                }
              });

              $scope.$watch("nameFilter", function(oldVal, newVal) {
                if (!_.isEqual(oldVal, newVal)) {
                  getPage();
                }
              });
            });
        }

        init();

      }
    ]
);
