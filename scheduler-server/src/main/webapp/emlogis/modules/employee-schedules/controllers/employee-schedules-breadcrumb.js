angular.module('emlogis.employeeSchedules').controller('EmployeeSchedulesBreadcrumbCtrl', ['$scope', '$state', '$translate', 'authService', 'applicationContext',
  function($scope, $state, $translate, authService, applicationContext) {

    var module = applicationContext.getModule();

    $translate('nav.EMPLOYEE_SCHEDULES')
      .then(function (translation) {
        module.name = translation;
      });
    module.href = '/employee-schedules';
    module.disableModuleBreadcrumb = false;
    applicationContext.setModule(module);

    $scope.hasMgmtPermission = function () {
      return authService.hasPermission('Tenant_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return authService.hasPermission('Tenant_View');
    };
  }
]);
