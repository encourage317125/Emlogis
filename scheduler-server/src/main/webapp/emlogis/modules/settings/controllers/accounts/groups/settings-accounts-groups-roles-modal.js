(function () {
  "use strict";

  var settings = angular.module('emlogis.settings');

  settings.controller('SettingsAccountsGroupsRolesModalCtrl',
    [
      '$scope',
      '$timeout',
      '$modalInstance',
      'uiGridConstants',
      'applicationContext',
      'dataService',
      'rulesTeamsService',
      'group',
      function ($scope, $timeout, $modalInstance, uiGridConstants, applicationContext, dataService,
                rulesTeamsService, group) {

        //--------------------------------------------------------------------
        // Defaults for Employees Modal
        //--------------------------------------------------------------------

        var te = this,
            totalUnnassociatedUsers;

        te.group = group;
        te.numOfRows = 15;
        te.isEditing = false;
        te.queryParams = {
          filter: "primaryKey.id NOT Like 'acl-%' AND primaryKey.id NOT Like 'employeerole%'",   // filter out 'hidden' roles, ie those starting with acl-
          orderby:'name',
          orderdir:'ASC'
        };

        te.unassociatedRoles = null;
        te.unassociatedRolesInit = null;
        te.usersToAdd = [];

        //
        // Load Unassociated Employees
        // that don't belong to this Team

        var loadGroupUnassociatedRoles = function(groupId, queryParams, pageNum, perPage){
          
          return dataService.getUnassociatedGroupAccountRoles('groupaccounts', groupId, queryParams, pageNum, perPage)
            .then(function(res) {
            console.log('+++ Loaded GroupunassociatedRoles:', res);    //DEV mode

            prepareData(res);

            $timeout(function() {
              prepareObjectsGrid(te.unassociatedRoles);
            });

            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            })
          ;
        };

        loadGroupUnassociatedRoles(te.group.id, te.queryParams, 1, te.numOfRows);

        var prepareData = function(res){
          totalUnnassociatedUsers = res.total;
          te.unassociatedRolesInit = res.data;

          angular.forEach(te.unassociatedRolesInit, function(role) {
            role.isSelected = false;
          });

          te.unassociatedRoles = angular.copy(te.unassociatedRolesInit);
        };



        //--------------------------------------------------------------------
        // Grid settings
        //--------------------------------------------------------------------

        var prepareObjectsGrid = function(unassociatedRoles){
          console.log('+++ unassociatedRoles', unassociatedRoles);

          te.gridOptions = {
            data: unassociatedRoles,
            totalItems: totalUnnassociatedUsers,
            minRowsToShow: unassociatedRoles.length < te.numOfRows ? unassociatedRoles.length : te.numOfRows,

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
                      te.queryParams.orderby = 'getUnassociatedGroupAccountRoles';
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

                dataService.getUnassociatedGroupAccountRoles('groupaccounts', te.group.id, te.queryParams, te.gridOptions.paginationCurrentPage, te.numOfRows)
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
          te.gridOptions.totalItems = totalUnnassociatedUsers;
          te.gridOptions.data = te.unassociatedRoles;
          te.gridOptions.minRowsToShow = totalUnnassociatedUsers < te.numOfRows ? totalUnnassociatedUsers : te.numOfRows;
        };


        te.updateEditing = function(){
          te.isEditing = !angular.equals(te.unassociatedRolesInit, te.unassociatedRoles);
        };


        /**
         * Add users to groups: Settings->Accounts->Groups->Roles
         */
        te.associateRoles = function(){

          //prepare selected rows
          var toAdd = te.gridApi.selection.getSelectedRows();
          console.log('toAdd', toAdd);

          var dto = [];
          angular.forEach(toAdd, function(row){
            dto.push(row.id);
          });
          console.log('toAddDto', {roleIdList: dto});

          dataService.addRolesToGroup(te.group.id, dto).then(function(res){

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