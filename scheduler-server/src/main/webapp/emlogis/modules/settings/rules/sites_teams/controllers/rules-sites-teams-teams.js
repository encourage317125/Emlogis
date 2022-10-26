(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesTeamsCtrl', ['$scope', '$filter', '$modal', '$log',
                                       'applicationContext', 'rulesTeamsService', 'dialogs', 'uiGridConstants', 'dataService',
    function ($scope, $filter, $modal, $log,
              applicationContext, rulesTeamsService, dialogs, uiGridConstants, dataService) {
    //console.log('+++ inside Rules - Teams controller');


    //--------------------------------------------------------------------
    // Defaults for Teams Ctrl
    //--------------------------------------------------------------------

    var numOfRows = 12;
    //$scope.allTeams = [];


    //
    // Alerts for Teams

    $scope.teamsAlerts = [
      /*{
       type: 'info',
       name: 'aboutTeams',
       display: true,
       msg: 'A Team is a group of Employees who have the same skills. Teams can mirror a geographic location (i.e. Front Office)...'
       }*/
    ];



    //
    // Listen to selected Site is being changed
    // and load Teams for newly selected Site

    $scope.$watch("selectedSite", function(newSite) {
      if (newSite) {
        //console.log('+++ Selected Site changed, newSite');
        $scope.siteDataIsLoading = true;                                              // Display loading spinner

        if ($scope.sitesTeamsTree){
          displaySiteChildrenFromTree(newSite.id);
        }
      }
    });

    $scope.$watch("sitesTeamsTree", function(newTree) {
      if (newTree) {
        displaySiteChildrenFromTree($scope.selectedSite.id);
      }
    });



    //--------------------------------------------------------------------
    // Teams related methods
    //--------------------------------------------------------------------


    //
    // Display initial details from Tree API
    // this will only display Panels names and Skills names

    var displaySiteChildrenFromTree = function(siteId){
      //console.log('+++ Displaying Panels and Skills tags for the selected Site...');

      var siteFromTree = _.find($scope.sitesTeamsTree, { 'id': siteId });
      $scope.selectedSiteChildren.site = {};                                           // First, clear the obj
      $scope.selectedSiteChildren.site = angular.copy(siteFromTree);                   // Then, save Site Tree data


      // Get all Teams for currently selected Site

      rulesTeamsService.getSiteTeams(siteId).then(function (res) {

        $scope.selectedSiteChildren.site.allTeamsInit = res.data;
        $scope.selectedSiteChildren.site.allTeams = angular.copy($scope.selectedSiteChildren.site.allTeamsInit);

        // Prepare allTeams object for Collapsing Panel

        angular.forEach($scope.selectedSiteChildren.site.allTeams, function(team) {
          team.isPanelCollapsed = true;                        // set the Panel collapsed value
          team.isWellCollapsed = true;                         // set the Edit Well collapsed value
          team.panelOpenedOnce = false;                        // to check is Panel for this Team was opened before
          team.panelName = team.name;                          // copy Team name to display in Panel header
          team.panelAbbreviation = team.abbreviation;          // copy Team abbreviation for Panel header
        });

        // Save initial Skills
        // from SiteTeamTree to allTeamsInit as children

        angular.forEach($scope.selectedSiteChildren.site.allTeamsInit, function(initTeam){
          initTeam.initSkills =_.result(_.find($scope.selectedSiteChildren.site.children, { 'id': initTeam.id}), 'children');
        });

        // TEMP TODO: until "Team.deletable" prop is not implemented
        angular.forEach($scope.selectedSiteChildren.site.allTeams, function(team){
          team.initSkills =_.result(_.find($scope.selectedSiteChildren.site.children, { 'id': team.id}), 'children');
        });
        // end of temp
      });
    };



    $scope.loadAllDetailsForTeam = function(team){

      // Load Skills associated to this Team
      rulesTeamsService.getTeamSkills(team.id).then(function(res){
        team.associatedSkills = res;
        angular.forEach(team.associatedSkills, function(skill){
          skill.active = skill.isActive;
        });
      });

      // Load Skills unassociated to this Team
      rulesTeamsService.getUnassociatedTeamSkills(team.id).then( function(res){
        team.skillsToAdd = res;
        angular.forEach(team.skillsToAdd, function(skill){
          skill.active = skill.isActive;
        });
      });

      // Load Employees for this Team
      displayEmployeesGrid(team);
    };


    // Add a new Team to a Site

    $scope.addNewTeam = function(){
      console.log('+++ ADD NEW SITE clicked...');                             // DEV mode

      var newTeam = {
        isPanelCollapsed: false,
        isWellCollapsed: false,
        panelOpenedOnce: true,
        panelName: 'New Team',
        active: true
      };
      $scope.selectedSiteChildren.site.allTeams.unshift(newTeam);

      $scope.updateEditing();        // and change Editing state to true
    };


    // Delete Team

    $scope.deleteTeam = function(team){

      // Confirm deletion
      var question = $filter('translate')("rules.site_teams.DELETE_TEAM") + team.name + '?';
      var dlg = dialogs.confirm('app.PLEASE_CONFIRM', question);                // Show modal window
      dlg.result.then(function (btn) {                                          // If user confirms, proceed

        // for an existing Team
        if (team.id) {
          return rulesTeamsService.deleteTeam(team.id).then( function(res){

            displaySiteChildrenFromTree($scope.selectedSite.id);  // Reload Teams
            $scope.$parent.loadAllSites();                        // Reload Site Info and SiteTeamsTree in sidebar
          });

        // for a new Team
        } else {
          $scope.selectedSiteChildren.site.allTeams.shift();
          $scope.page.editing = false;                            // Refresh editing state to default
        }
      });
    };



    //--------------------------------------------------------------------
    // Skills related methods
    //--------------------------------------------------------------------


    //
    // Remove a Skill from a given Team

    $scope.removeSkill = function(skill, team){
      //console.log( 'This will delete Skill: ' + skill.name + ' from Team: ' + team.name);

      var i = team.associatedSkills.indexOf(skill);     // Find index of Skill in Teams array
      if (i != -1) {                                    // If Skill is in Teams,
        team.associatedSkills.splice(i, 1);             // remove it from array
      }
      $scope.updateEditing();                  // and change Editing state to true

    };


    //
    // Attach a Skill from a given Team

    $scope.attachSkillToTeam = function(skill, team){
      //console.log( 'This will add a new Skill: ' + skill.name + ' to Team: ' + team.name );

      team.associatedSkills.push(skill);
      $scope.updateEditing();                  // and change Editing state to true
    };



    //--------------------------------------------------------------------
    // Employees related methods
    //--------------------------------------------------------------------

    //
    // Prepare Employees grid

    var displayEmployeesGrid = function(team){
      return rulesTeamsService.loadTeamEmployees(team.id, { orderby:'Employee.lastName', orderdir:'ASC' }, 1, numOfRows)
        .then( function(res){
          team.associatedEmployees = res;

          //
          // Employees table
          // placed inside each Team

          team.gridOptions = {
            data: team.associatedEmployees.data,
            totalItems: team.associatedEmployees.total,
            minRowsToShow: team.associatedEmployees.total < numOfRows ? team.associatedEmployees.total : numOfRows,

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
            paginationPageSize: numOfRows,
            paginationCurrentPage: 1,

            enableSelectAll: true,
            enableRowSelection: true,
            enableFullRowSelection: true,               // full row selection
            enableHighlighting: false,

            /*isRowSelectable: function(row) {            // prevent selection employees whos HomeTeam is current Team
              return row.entity.homeTeamId !== team.id;
            },
            rowTemplate: '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.uid" ' +
                              'class="ui-grid-cell" ' +
                              'ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader,' +
                                          ' \'not-selectable\': !row.enableSelection }"  ' +
                              'ui-grid-cell></div>',*/

            columnDefs: [
              {
                field: 'lastName',
                displayName: 'Last Name', // TODO translate
                enableFiltering: true,
                minWidth: '150',
                sort: {
                  direction: uiGridConstants.ASC
                }
              },
              { field: 'firstName',        enableFiltering: false, displayName: 'First Name' }, // TODO translate
              { field: 'isFloating',                          enableFiltering: false, displayName: 'Is Floating', enableSorting: false },
              { field: 'employeeType',     enableFiltering: false, displayName: 'Employee Type' },
              { field: 'primarySkillName', enableFiltering: false, displayName: 'Primary Skill' },
              { field: 'homeTeamName',                        enableFiltering: false, displayName: 'Home Team' },
              { field: 'hireDate',         enableFiltering: false, displayName: 'Hire Date', cellFilter: 'date' },
              { field: 'primarySkillId',   visible: false },
              { field: 'homeTeamId',                          visible: false },
              { field: 'employeeId',               visible: false }
            ],
            onRegisterApi: function(gridApi) {
              team.gridApi = gridApi;
              team.gridOptions.queryParams = {
                orderby:'lastName',
                orderdir:'ASC'
              };


              //
              // Back-end filtering

              team.gridApi.core.on.filterChanged( $scope, function() {
                var grid = this.grid;
                var filterTerm = grid.columns[1].filters[0].term;
                //console.log('~~~ filter changed - grid', grid);

                if (filterTerm === null || filterTerm === '' || filterTerm === undefined ){
                  team.gridOptions.queryParams = {
                    orderby : team.gridOptions.queryParams.orderby,
                    orderdir: team.gridOptions.queryParams.orderdir
                  };
                } else {
                  var filterName = 'Employee.lastName';
                  team.gridOptions.queryParams.filter = filterName + " LIKE '" + filterTerm + "%'";
                }
                getPage();
              });


              //
              // Back-end sorting

              team.gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                //console.log('~~~ sortColumns', sortColumns);
                //console.log('~~~ sorting changed - grid', grid);
                if (sortColumns.length === 0) {
                  team.gridOptions.queryParams.orderdir = 'ASC';
                  team.gridOptions.queryParams.orderby = 'lastName';
                } else {
                  team.gridOptions.queryParams.orderdir = sortColumns[0].sort.direction;

                  switch (sortColumns[0].field) {
                    case "lastName":
                      team.gridOptions.queryParams.orderby = 'Employee.lastName';
                      break;
                    case "firstName":
                      team.gridOptions.queryParams.orderby = 'Employee.firstName';
                      break;
                    case "isFloating":
                      team.gridOptions.queryParams.orderby = 'EmployeeTeam.isFloating';  // TODO: Yuriy is adding the logic
                      break;
                    case "employeeType":
                      team.gridOptions.queryParams.orderby = 'Employee.employeeType';
                      break;
                    case "primarySkillName":
                      team.gridOptions.queryParams.orderby = 'Skill.name';
                      break;
                    case "homeTeamName":
                      team.gridOptions.queryParams.orderby = 'Team.name';
                      break;
                    case "hireDate":
                      team.gridOptions.queryParams.orderby = 'Employee.hireDate';
                      break;
                    default:
                      team.gridOptions.queryParams.orderby = 'Employee.lastName';
                      break;
                  }
                }
                getPage();
              });


              //
              // Back-end pagination

              team.gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
                team.gridOptions.paginationCurrentPage = newPage;
                getPage();
              });

              var getPage = function() {
                //console.log('team.gridOptions.queryParams', team.gridOptions.queryParams);
                rulesTeamsService.loadTeamEmployees(team.id, team.gridOptions.queryParams, team.gridOptions.paginationCurrentPage, numOfRows)
                  .then(function(res){
                    refreshEmployeesGrid(team, res);
                  })
                ;
              };
            }
          };
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    //
    // Remove selected Employees
    // from this Team

    $scope.removeEmployeesFromTeam = function(team, emplIdsList){
      var toDelete = team.gridApi.selection.getSelectedRows();
      var toDeleteIds = [];
      angular.forEach(toDelete, function(row){
        toDeleteIds.push(row.employeeId);
      });

      rulesTeamsService.removeEmployeesTeamMembership(team.id, toDeleteIds).then(function(res){
        //displayEmployeesGrid(team);
        return rulesTeamsService.loadTeamEmployees(team.id, team.gridOptions.queryParams, team.gridOptions.paginationCurrentPage, numOfRows)
          .then(function(res){
            refreshEmployeesGrid(team, res);
          })
        ;
      });
    };


    var refreshEmployeesGrid = function(team, res){
      team.associatedEmployees = res;
      team.gridOptions.totalItems = res.total;
      team.gridOptions.data = res.data;
      team.gridOptions.minRowsToShow = res.total < numOfRows ? res.total : numOfRows;
      team.gridApi.core.refresh(); // UI-Grid not recalculate # of rows: https://github.com/angular-ui/ng-grid/issues/2531
    };



    //--------------------------------------------------------------------
    // Other related methods
    //--------------------------------------------------------------------

    $scope.openEmployeesModal = function (team) {
      var modalTeam = team;

      var modalInstance = $modal.open({
        templateUrl: 'modules/settings/rules/sites_teams/partials/include/rules_sites-teams_employees-modal.tmpl.html',
        controller: 'RulesSitesTeamsEmployeesModalCtrl as te',
        size: 'lg',
        resolve: {
          team: function () {
            return modalTeam;
          }
        }
      });

      modalInstance.result.then(function () {
        applicationContext.setNotificationMsgWithValues('app.ADDED_SUCCESSFULLY', 'success', true);
        //displayEmployeesGrid(team);
        rulesTeamsService.loadTeamEmployees(team.id, team.gridOptions.queryParams, team.gridOptions.paginationCurrentPage, numOfRows)
          .then(function(res){
            refreshEmployeesGrid(team, res);
          })
        ;
      }, function () {
        $log.info('Modal dismissed at: ' + new Date());
      });
    };


  }]);
})();