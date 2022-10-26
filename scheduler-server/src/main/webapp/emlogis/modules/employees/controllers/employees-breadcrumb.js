var employees = angular.module('emlogis.employees');

employees.controller('EmployeesBreadcrumbCtrl', ['$scope', '$filter', '$state', '$modal', 'dataService', 'applicationContext',
    function($scope, $filter, $state, $modal, dataService, applicationContext) {

      $scope.homeTeamId = null;

      // Update Module Information
      var module = applicationContext.getModule();

      module.name = $filter('translate')('nav.EMPLOYEES');
      module.icoClass = '';
      module.href = '/employees';
      module.disableModuleBreadcrumb = true;
      applicationContext.setModule(module);



      $scope.selectEmployee = function(employee) {
        $state.go('authenticated.employees.detail', {id: employee.id});
      };

      $scope.searchEmployees = function(searchText) {
        var params = {
          search: searchText,
          searchfields: 'firstName,lastName',
          returnedfields: 'firstName,lastName,id',
          limit: 15
        };

        return dataService.searchEmployees(params)
            .then(function(res) {
              return _.map(res.data, function(e) {
                return {
                  fullName: e[0] + " " + e[1],
                  id: e[2]
                };
              });
            });
      };



      $scope.startCreatingEmployee = function(){
        var modalInstance = $modal.open({
          templateUrl:  'modules/employees/partials/new-employee-modal.html',
          controller:   'NewEmployeeModalCtrl as newbie',
          backdrop:     'static',
          size: 'sm',
          resolve: {
            homeTeamId: function () {
              return $scope.homeTeamId;
            }
          }
        });

        modalInstance.result.then(function (employee) {
          $scope.homeTeamId = employee.homeTeamId;
          $state.go('authenticated.employees.detail', {id: employee.id});
        }, function () {
          //console.log('Modal dismissed at: ' + new Date());
        });

      };

    }
]);
