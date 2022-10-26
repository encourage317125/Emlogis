angular.module('emlogis.settings').controller('SettingsAccountsGroupsListCtrl',
  [
    '$scope',
    '$state',
    '$http',
    '$filter',
    '$q',
    '$translate',
    'applicationContext',
    'crudDataService',
    'uiGridConstants',
    'SettingsAccountsService',
    'UtilsService',
    function ($scope, $state, $http, $filter, $q, $translate, applicationContext, crudDataService,
              uiGridConstants, SettingsAccountsService, UtilsService) {

      var baseUrl = applicationContext.getBaseRestUrl();
      var factory = _.clone(crudDataService);

      $scope.entityDetails = {
        entityType: $scope.consts.entityTypes.group,
        entityColumnDefs: [
          { field: 'id', visible: false },
          { field: 'name', width: '15%' },
          { field: 'description', width: '27%' },
          { field: 'members', width: '8%', enableSorting: false },
          { field: 'roles' },
          { field: 'created', width: '15%', visible: false },
          { field: 'updated', width: '15%', visible: false },
          { field: 'ownedBy', width: '12%', visible: false }
        ],
        needPagination: true,
        entityDetailsStateName: 'authenticated.settings.accounts.groups.groupDetails'
      };

      $scope.convertEntityToGridRow = function (group) {
        var gridRow = {};

        gridRow.id = group.id;
        gridRow.name = group.name;
        gridRow.description = group.description;
        gridRow.members = group.nbOfMembers;
        gridRow.roles = group.roles;
        gridRow.created = new Date(group.created).toString();
        gridRow.updated = new Date(group.updated).toString();
        gridRow.ownedBy = group.ownedBy;

        return gridRow;
      };

      /**
       * Team managers Grid: Additional Field
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

      $scope.columnDef = [
        {
          name: 'app.TEAM',
          field: 'teamName',
          headerCellTemplate: headerCellTemplate(),
          width: '20%'
        },
        {
          name: 'settings.accounts.MANAGERS',
          field: 'managerNames',
          headerCellTemplate: headerCellTemplate()
        }
      ];

      $scope.teamManagerGridOptions = {
        enableColumnResizing: true,
        enableRowHeaderSelection: false,
        modifierKeysToMultiSelect: false,
        noUnselect: true,
        enableGridMenu: true,
        columnDefs: $scope.columnDef, //
        rowTemplate: rowTemplate(), //Row Template,
        enableHorizontalScrollbar: 0,
        enableVerticalScrollbar: 0,
        enableColumnMenus: false,
        useExternalFiltering: true,

  //        enableColumnMenus: true,
          gridMenuTitleFilter: $translate, // Translate Grid Menu column name

        onRegisterApi: function( gridApi ) {
          $scope.gridApi = gridApi;

        }
      };

      /**
       * Load Team Managers
       */
      $scope.loadTeamManagers = function() {
  //      http://localhost:8080/scheduler-server/emlogis/rest/org/ops/getmanagersbyteams

        var deferred = $q.defer();

        factory.getElements('org/ops/getmanagersbyteams',{})
          .then(function (entities) {

            /**
             * resolves sites
             */
            var teams = entities.data;

            angular.forEach(teams, function(team) {

              team.managerNames = '';

              _.each(team.managers, function(manager, key) {

                if (key !== 0) {
                  team.managerNames +=', ';
                }

                team.managerNames += manager.name;
                if (manager.employeeId !== null ) {
                  team.managerNames += ' (Employee)';
                }

              });

            });

            $scope.teamManagerGridOptions.data = teams;

            deferred.resolve(teams);

          }, function(error) {
            deferred.reject(error);
            applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
          });



        return deferred.promise;
      };

      //load teamManagers
      if ($scope.isTenantType('Customer')){
        $scope.loadTeamManagers();
      }
    }
]);
