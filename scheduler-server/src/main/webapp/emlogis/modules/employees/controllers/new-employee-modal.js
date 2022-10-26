(function () {
  "use strict";

  var employees = angular.module('emlogis.employees');

  employees.controller('NewEmployeeModalCtrl',
    ['$scope', '$rootScope', '$filter', '$modalInstance', 'employeesDetailsService', 'homeTeamId',
      function ($scope, $rootScope, $filter, $modalInstance, employeesDetailsService, homeTeamId) {

        //--------------------------------------------------------------------
        // Defaults for Emergency Contact Modal
        //--------------------------------------------------------------------

        var siteTeamsTreeInit,
            newbie = this;

        newbie.submitted = false;
        newbie.sitesTeamsTree = [];
        newbie.loginEqualsId = true;

        newbie.dto = {
          firstName: null,
          lastName: null,
          employeeIdentifier: null,
          userAccountDto: {
            login: '',
            email: null,
            inactivityPeriod: 0,
            language: 'en',
            status: 'Active'
          },
          employeeTeamCreateDto: {
            isHomeTeam: true,
            teamId: ''
          }
        };


        //
        // Get SitesTeamsTree

        employeesDetailsService.getSitesTeamsTree().then( function(data){
          siteTeamsTreeInit = data;
          angular.forEach(siteTeamsTreeInit, function(site){
            angular.forEach(site.children, function(team){
              team.site = site;
              newbie.sitesTeamsTree.push(team);
            });
          });
          newbie.sitesTeamsTree = $filter('orderBy')(newbie.sitesTeamsTree, 'name');
        });


        //
        // If user navigates away from the page,
        // dismiss the modal

        $rootScope.$on('$stateChangeStart',
          function(){
            $modalInstance.dismiss('cancel');
          }
        );


        //--------------------------------------------------------------------
        // CRUD
        //--------------------------------------------------------------------


        //
        // Create a new Employee

        newbie.createEmployee = function(){

          if ( $scope.newbieForm.$valid ) {
            newbie.dto.userAccountDto.login = newbie.loginEqualsId ? newbie.dto.employeeIdentifier : newbie.dto.userAccountDto.login;

            return employeesDetailsService.createEmployee(newbie.dto).then( function(employee){
              $modalInstance.close(employee);
            });
          } else {
            newbie.submitted = true;
          }
        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        newbie.closeModal = function(){
          $modalInstance.close();
        };


        newbie.cancelModal = function(){
          $modalInstance.dismiss('cancel');
        };

      }
    ]);
})();