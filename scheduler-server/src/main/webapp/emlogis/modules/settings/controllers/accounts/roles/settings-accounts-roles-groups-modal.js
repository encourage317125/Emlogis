(function () {
  "use strict";

  var settings = angular.module('emlogis.settings');

  settings.controller('SettingsAccountsRolesGroupsModalCtrl',
    [
      '$scope',
      '$timeout',
      '$modalInstance',
      'uiGridConstants',
      'applicationContext',
      'dataService',
      'rulesTeamsService',
      'role',
      function ($scope, $timeout, $modalInstance, uiGridConstants, applicationContext, dataService,
                rulesTeamsService, role) {

        //--------------------------------------------------------------------
        // Defaults for Employees Modal
        //--------------------------------------------------------------------

        var te = this,
            totalUnassociatedGroups;

        te.role = role;
        te.numOfRows = 15;
        te.isEditing = false;
        te.queryParams = {
          orderby:'name',
          orderdir:'ASC'
        };

        te.unassociatedGroups = null;
        te.unassociatedGroupsInit = null;
        te.usersToAdd = [];

        //
        // Load Unassociated Employees
        // that don't belong to this Team

        var loadRoleUnassociatedGroups = function(roleId, queryParams, pageNum, perPage){
          
          return dataService.getUnassociatedRolesGroups('roles', roleId, queryParams, pageNum, perPage)
            .then(function(res) {
            console.log('+++ Loaded RoleUnassociatedGroups:', res);    //DEV mode

            prepareData(res);

            $timeout(function() {
              prepareObjectsGrid(te.unassociatedGroups);
            });

            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            })
          ;
        };

        loadRoleUnassociatedGroups(te.role.id, te.queryParams, 1, te.numOfRows);

        var prepareData = function(res){
          totalUnassociatedGroups = res.total;
          te.unassociatedGroupsInit = res.data;

          angular.forEach(te.unassociatedGroupsInit, function(obj) {
            obj.isSelected = false;
          });

          te.unassociatedGroups = angular.copy(te.unassociatedGroupsInit);
        };



        //--------------------------------------------------------------------
        // Grid settings
        //--------------------------------------------------------------------

        var prepareObjectsGrid = function(unassociatedGroups){
          console.log('+++ unassociatedRoles', unassociatedGroups);

          te.gridOptions = {
            data: unassociatedGroups,
            totalItems: totalUnassociatedGroups,
            minRowsToShow: unassociatedGroups.length < te.numOfRows ? unassociatedGroups.length : te.numOfRows,

            enableHorizontalScrollbar: 0,
            enableVerticalScrollbar: 0,
            enableColumnMenus: false,

            enableFiltering: true,
            useExternalFiltering: true,

            enableSorting: true,
            useExternalSorting: true,

            needPagination: true,
            useExternalPagination: true,
            enablePaginationControls: false,
            paginationPageSize: te.numOfRows,
            paginationCurrentPage: 1,

            enableSelectAll: true,
            enableRowSelection: true,
            enableFullRowSelection: true,                   // full row selection

            columnDefs: [
              {
                field: 'name',
                enableFiltering: true,
                minWidth: '150',
                sort: {
                  direction: uiGridConstants.ASC
                }
              },
              { field: 'description',enableFiltering: false, minWidth: '100' }
            ],
            onRegisterApi: function(gridApi) {
              te.gridApi = gridApi;

              //
              // Row selection
              
              gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                row.entity.isSelected = row.isSelected;
                te.updateEditing();
              });

              gridApi.selection.on.rowSelectionChangedBatch($scope,function(rows){
                _.each(rows, function(row){
                  row.entity.isSelected = row.isSelected;
                });
                te.updateEditing();
              });

              //
              // Back-end filtering

              gridApi.core.on.filterChanged( $scope, function() {
                var grid = this.grid;
                var filterTerm = grid.columns[1].filters[0].term;
                //console.log('~~~ filter changed - grid', grid);

                if (filterTerm === null || filterTerm === '' || filterTerm === undefined ){
                  te.queryParams = {
                    orderby : te.queryParams.orderby,
                    orderdir: te.queryParams.orderdir
                  };
                } else {
                  var filterName = 'name';
                  te.queryParams.filter = filterName + " LIKE '" + filterTerm + "%'";
                }
                getPage();
              });


              //
              // Back-end sorting

              gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                if (sortColumns.length === 0) {
                  te.queryParams.orderdir = 'ASC';
                  te.queryParams.orderby = 'name';
                  
                } else {
                  te.queryParams.orderdir = sortColumns[0].sort.direction;

                  switch (sortColumns[0].field) {
                    case "name":
                      te.queryParams.orderby = 'name';
                      break;
                    case "description":
                      te.queryParams.orderby = 'description';
                      break;
                    default:
                      te.queryParams.orderby = 'getUnassociatedRolesAccounts';
                      break;
                  }
                }
                getPage();
              });


              //
              // Back-end pagination

              gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
                te.gridOptions.paginationCurrentPage = newPage;
                getPage();
              });

              var getPage = function() {
                console.log('te.gridOptions.queryParams', te.queryParams);

                dataService.getUnassociatedRolesGroups('roles', te.role.id, te.queryParams, te.gridOptions.paginationCurrentPage, te.numOfRows)
                  .then( function(res){
                    console.log('~~~ res grid upd', res);
                    refreshGrid(res);
                });
              };

            }
          };
        };


        //--------------------------------------------------------------------
        // CRUD
        //--------------------------------------------------------------------


        var refreshGrid = function(res){
          prepareData(res);
          te.gridOptions.totalItems = totalUnassociatedGroups;
          te.gridOptions.data = te.unassociatedGroups;
          te.gridOptions.minRowsToShow = totalUnassociatedGroups < te.numOfRows ? totalUnassociatedGroups : te.numOfRows;
        };


        te.updateEditing = function(){
          te.isEditing = !angular.equals(te.unassociatedGroupsInit, te.unassociatedGroups);
        };


        /**
         * Add groups to roles: Settings->Accounts->Roles->Groups
         */
        te.associate = function(){

          //prepare selected rows
          var toAdd = te.gridApi.selection.getSelectedRows();
          console.log('toAdd', toAdd);

          var dto = [];
          angular.forEach(toAdd, function(row){
            dto.push(row.id);
          });
          console.log('toAddDto', {groupIdList: dto});

          dataService.addGroupsToRole(te.role.id, dto).then(function(res){

            te.closeModal();

          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        te.closeModal = function () {
          $modalInstance.close(te.group);
        };


        //
        // If user navigates away from the page,
        // dismiss the modal

        $scope.$on('$stateChangeStart', function(){
            $modalInstance.dismiss('cancel');
          }
        );

      }
    ]);
})();