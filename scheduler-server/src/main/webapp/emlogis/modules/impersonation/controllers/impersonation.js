var app = angular.module('emlogis.impersonation');

/* parameter users is defined in angular-init.js file when app opens the diaglog
 * It is defined as 'Resolve'
 */
app.controller('ImpersonationManageCtrl', ['$scope', '$modalInstance', '$http', '$modal',
    '$sessionStorage', 'authService',  'applicationContext', 'crudDataService', 'uiGridConstants',
  function($scope, $modalInstance, $http, $modal,
           $sessionStorage, authService, applicationContext, crudDataService, uiGridConstants) {

    $scope.browser = {
      filterTxt : "",
      orderBy : "id",
      orderDir : "asc",
      pageSize : 20,
      currentPage : 1
    };

    $scope.isModalOpen = true;

    /*
     *This header cell template is almost same as default cell template except
     * it has translate directive inside so grid header will support i18n as well
     */
    var headerCellTemplate = function(){
      return "<div ng-class=\"{ 'sortable': sortable }\">" +
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
        name: 'impersonation.LOGIN_ID',
        field: 'id',
        headerCellTemplate: headerCellTemplate()
      },
      {
        name:'app.NAME',
        field: 'name',
        headerCellTemplate: headerCellTemplate()
      },
      {
        name:'app.LOGIN',
        field: 'login',
        headerCellTemplate: headerCellTemplate()
      },
      {
        name: 'app.ACTION',
        enableSorting: false,
        disableColumnMenu: true,
        disableHiding: true,
        headerCellTemplate: headerCellTemplate(),
        cellClass: 'action-cell',
        cellTemplate:actionCellTemplate()
      }
    ];

    // Row Template
    function rowTemplate() {
      return '<div title="This is row tooltip"  ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
    }


    // Action Cell Template

    function actionCellTemplate() {
      var action="";

      // Check 2 permissions related to impersonation
      if (authService.hasPermission('ImpersonateViewOnly')){
        action = '<a ng-click="getExternalScopes().impersonateView(row.entity)" translate>impersonation.IMPERSONATE_VIEW</a>';
      }

      if (authService.hasPermission('Impersonate')){
        action += ' | <a ng-click="getExternalScopes().impersonate(row.entity)" translate>impersonation.IMPERSONATE</a>';
      }

      return action;
    }

    $scope.gridOptions = {
      useExternalSorting: true,  // It will call backend api
      enableColumnResizing: true,
      enableGridMenu: true,
      columnDefs: angular.copy($scope.columnDef),
      rowTemplate: rowTemplate(), //Row Template
      enablePaginationControls: false,
      onRegisterApi: function( gridApi ) {
        $scope.gridApi = gridApi;
        $scope.gridApi.core.on.sortChanged( $scope, function( grid, sortColumns ) {
          if( sortColumns.length === 0 ){
            console.log('Impersonation column length : 0');
          }
          else {

            if (sortColumns[0].field == "id"){
              $scope.browser.orderBy = "primaryKey.id";

            }
            else{
              $scope.browser.orderBy = sortColumns[0].field;
            }


            if (sortColumns[0].sort.direction == uiGridConstants.DESC) {
              $scope.browser.orderDir = "DESC";
            }
            else {
              $scope.browser.orderDir = "ASC";
            }

            // search again
            $scope.listUsers();

          }
        });
      }
    };


    // External Scope Module
    $scope.gridModel = {

      impersonate : function(user){

        console.log('--> ImpersonatingTo(' + user.name + ')...');

        $http.post('../emlogis/rest/sessions/ops/impersonate', user.login)
          .success(function(data) {

            //$http.defaults.headers.common.EmLogisToken = data.token;
            console.log('--> Impersonation successful()');

            // reset login info  and just restart as if we were in a new session

            $scope.$close();
            applicationContext.setUsername(data.userName);
            $sessionStorage.impersonated = true;

            authService.loginConfirmed( data);
          })
          .error(function() {
            // TODO show login error
            console.log('--> Impersonation FAILED()');

            // not much we can do, our initial session  is probably closed,
            // Move to login page

            // TODO should we clear data ?
            authService.loginCancelled();
          });
      },

      impersonateView : function(user){

        console.log('--> Impersonating View To(' + user.name + ')...');

        $http.post('../emlogis/rest/sessions/ops/impersonateviewonly', user.login)
          .success(function(data) {

            //$http.defaults.headers.common.EmLogisToken = data.token;
            console.log('--> Impersonation successful()');

            // reset login info  and just restart as if we were in a new session

            $scope.$close();
            applicationContext.setUsername(data.userName);
            $sessionStorage.impersonated = true;

            authService.loginConfirmed( data);
          })
          .error(function() {
            // TODO show login error
            console.log('--> ImpersonationViewOnly FAILED()');

            // not much we can do, our initial session  is probably closed,
            // Move to login page

            // TODO should we clear data ?
            authService.loginCancelled();
          });
      }
    };

    // When we go to login page we have to dismiss the modal.
    $scope.$on('event:auth-loginRequired', function () {

      if ($scope.isModalOpen === true){
        $modalInstance.dismiss('cancel');
        $scope.isModalOpen = false;
      }

    });


    // Prepare Page Offset and Limit
    $scope.preparePageParam = function(){

      $scope.browser.offset = ($scope.browser.currentPage-1) * $scope.browser.pageSize;
      $scope.browser.limit = $scope.browser.pageSize;
    };

    // Retrieve users from backend

    $scope.listUsers = function(){

      var factory = _.clone(crudDataService);

      var filter = "name like '%" + $scope.browser.filterTxt + "%'";
      var userAccountsListUrl = '../emlogis/rest/useraccounts?filter=' + encodeURIComponent(filter);

      userAccountsListUrl += "&orderby="+$scope.browser.orderBy + "&orderdir="+$scope.browser.orderDir;

      $scope.preparePageParam();

      userAccountsListUrl += "&offset="+$scope.browser.offset + "&limit="+$scope.browser.limit;

      console.log(userAccountsListUrl);

      return $http.get(userAccountsListUrl).then(function (response) {

        var resultSet = factory.toResultSet(response);

        $scope.gridOptions.data = resultSet.data;
        $scope.browser.totalRecords = resultSet.total;

        console.log("users : Impersonation ");
        return resultSet;

      });
    };

    // Keydown event on page user filter input box
    $scope.userFilterKeyDown = function($event) {

      //detect Enter Key
      if ($event.keyCode == 13){
        $scope.listUsers();

      }
    };

    // change page and refresh view
    $scope.pageChanged = function () {
      $scope.listUsers();
    };


    // Refresh Data
    $scope.refresh = function(){
      $scope.listUsers();
    };

    // Close Modal
    $scope.close = function(){
      //$modalInstance.dismiss('cancel');
      $modalInstance.dismiss('cancel');
    };


    // Auto Complete List
    $scope.getName = function(val) {

      var factory = _.clone(crudDataService);
      var filter = "name like '%" + val + "%'";
      var userAccountsListUrl = '../emlogis/rest/useraccounts?filter=' + encodeURIComponent(filter);

      return $http.get(userAccountsListUrl).then(function (response) {

        var resultSet = factory.toResultSet(response);

        return resultSet.data.map(function(item){
            return item.name;
        });

      });

    };

    // Call Search function when dialog box loads
    $scope.listUsers();

  }
]);

