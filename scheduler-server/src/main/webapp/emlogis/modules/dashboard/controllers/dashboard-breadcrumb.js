var dashboard = angular.module('emlogis.dashboard');

dashboard.controller('DashboardBreadcrumbCtrl', ['$scope', '$translate', 'applicationContext',
  function ($scope, $translate, applicationContext) {

    //// Update Module Information
    //var module = applicationContext.getModule();
    //
    //// Call translate directive function. Put translated text;
    //$translate('nav.DASHBOARD')
    //  .then(function (translation) {
    //    module.name = translation;
    //  });
    //
    //
    //module.href = '/dashboard';
    //module.icoClass = '';
    //module.disableModuleBreadcrumb = false;
    //applicationContext.setModule(module);

    //console.log('home dashboard breadcrumb controller');
    //
    //// These variables are used in entities-list-breadcrumb.js file
    //
    //$scope.newEntity = "employees.NEW_EMPLOYEE";
    //$scope.entity = "employees.EMPLOYEE";
    //$scope.entityResource = "employees";


    if (applicationContext.getUsername() === ""){
      applicationContext.setUsername($sessionStorage.username);
    }

    $scope.username = applicationContext.getUsername();

    // Set Variables for accordion
    $scope.sideBar = {
      isManager_ApprovalsOpen : true,
      isTeam_Member_RequestsOpen : true
    };

    // Update Module Information

    var module = applicationContext.getModule();

    // Call translate directive function. Put translated text;
    $translate('nav.HOME')
      .then(function (translation) {
        module.name =  translation;
      });


    module.href = '/dashboard';
    module.icoClass = '';
    module.disableModuleBreadcrumb = false;
    applicationContext.setModule(module);

  }
]);
