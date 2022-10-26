(function () {
  "use strict";

  var settings = angular.module('emlogis.settings');

  settings.controller('SettingsAccountsGroupsUsersModalCtrl',
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
          orderby:'lastName',
          orderdir:'ASC'
        };

        te.unassociatedUsers = null;
        te.unassociatedUsersInit = null;
        te.usersToAdd = [];

        //
        // Load Unassociated Employees
        // that don't belong to this Team

        var loadGroupUnassociatedUsers = function(groupId, queryParams, pageNum, perPage){
          
          return dataService.getUnassociatedGroupMembers(groupId, queryParams, pageNum, perPage)
            .then(function(res) {
            console.log('+++ Loaded GroupUnassociatedUsers:', res);    //DEV mode

            prepareData(res);

            $timeout(function() {
              prepareUsersGrid(te.unassociatedUsers);
            });

            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            })
          ;
        };

        loadGroupUnassociatedUsers(te.group.id, te.queryParams, 1, te.numOfRows);

        var prepareData = function(res){
          totalUnnassociatedUsers = res.total;
          te.unassociatedUsersInit = res.data;

          angular.forEach(te.unassociatedUsersInit, function(user) {
            user.isSelected = false;

            if (typeof user.employeeId !== 'undefined' && user.employeeId !== null) {
              user.employeeAccount = user.firstName + ' ' + user.lastName;
            } else {
              user.employeeAccount = '?';
            }

          });

          te.unassociatedUsers = angular.copy(te.unassociatedUsersInit);
        };



        //--------------------------------------------------------------------
        // Grid settings
        //--------------------------------------------------------------------

        var prepareUsersGrid = function(unassociatedUsers){
          console.log('+++ unassociatedUsers', unassociatedUsers);

          te.gridOptions = {
            data: unassociatedUsers,
            totalItems: totalUnnassociatedUsers,
            minRowsToShow: unassociatedUsers.length < te.numOfRows ? unassociatedUsers.length : te.numOfRows,

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
                field: 'lastName',
                enableFiltering: true,
                minWidth: '150',
                sort: {
                  direction: uiGridConstants.ASC
                }
              },
              { field: 'firstName',        enableFiltering: false, minWidth: '100' },
              { field: 'login', enableFiltering: false, minWidth: '150' },
              { field: 'employeeAccount',     enableFiltering: false, minWidth: '150', enableSorting: false}
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
                  var filterName = 'lastName';
                  te.queryParams.filter = filterName + " LIKE '" + filterTerm + "%'";
                }
                getPage();
              });


              //
              // Back-end sorting

              gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                if (sortColumns.length === 0) {
                  te.queryParams.orderdir = 'ASC';
                  te.queryParams.orderby = 'lastName';
                  
                } else {
                  te.queryParams.orderdir = sortColumns[0].sort.direction;

                  switch (sortColumns[0].field) {
                    case "lastName":
                      te.queryParams.orderby = 'lastName';
                      break;
                    case "firstName":
                      te.queryParams.orderby = 'firstName';
                      break;
                    case "login":
                      te.queryParams.orderby = 'login';
                      break;
                    default:
                      te.queryParams.orderby = 'lastName';
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

                dataService.getUnassociatedGroupMembers(te.group.id, te.queryParams, te.gridOptions.paginationCurrentPage, te.numOfRows)
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
          te.gridOptions.data = te.unassociatedUsers;
          te.gridOptions.minRowsToShow = totalUnnassociatedUsers < te.numOfRows ? totalUnnassociatedUsers : te.numOfRows;
        };


        te.updateEditing = function(){
          te.isEditing = !angular.equals(te.unassociatedUsersInit, te.unassociatedUsers);
        };


        /**
         * Add users to groups: Settings->Accounts->Groups->Users
         */
        te.associateUsers = function(){

          //prepare selected rows
          var toAdd = te.gridApi.selection.getSelectedRows();
          console.log('toAdd', toAdd);

          var memberIdList = [];
          angular.forEach(toAdd, function(row){
            memberIdList.push(row.id);
          });
          console.log('toAddDto', {memberIdList: memberIdList});

          dataService.addMembersToGroup(te.group.id, memberIdList).then(function(res){

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