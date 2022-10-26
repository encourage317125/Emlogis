(function () {
  "use strict";

  var employees = angular.module('emlogis.employees');

  employees.controller('EmployeesDetailsPTOsModalCtrl',
    ['$scope', '$timeout', '$modalInstance', 'uiGridConstants', 'employeesDetailsService', 'employee',
      function ($scope, $timeout, $modalInstance, uiGridConstants, employeesDetailsService, employee) {

        //--------------------------------------------------------------------
        // Defaults for Employee Skills Modal
        //--------------------------------------------------------------------

        var pto = this,
            numOfRows = 15;

        pto.employee = employee;
        pto.allPTOs = null;
        pto.allPTOsInit = null;


        employeesDetailsService.getEmployeePTO(pto.employee).then(function(data) {
          pto.allPTOs = data;

          $timeout(function() {
            preparePTOsGrid(pto.allPTOs);
          });
        },
        function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });



        //--------------------------------------------------------------------
        // Grid settings
        //--------------------------------------------------------------------

        var preparePTOsGrid = function(allPTOs){

          pto.gridOptions = {
            data: allPTOs,
            totalItems: allPTOs.length,
            minRowsToShow: allPTOs.length < numOfRows ? allPTOs.length : numOfRows,
            needPagination: true,
            enableHorizontalScrollbar: 0,
            enableVerticalScrollbar: 1,
            enableColumnMenus: false,
            enableFiltering: false,
            enableSorting: true,
            columnDefs: [
              {
                field: 'startDateTime',
                displayName: 'Date',
                cellFilter: 'date', // :\'MM/dd/yyyy\'
                type: 'date',
                sort: {
                  direction: uiGridConstants.DESC
                }
              },
              {
                field: 'absenceTypeDto.name',
                displayName: 'Absence Type'
              },
              {
                field: 'reason',
                enableSorting: false
              },
              { field: 'id', visible: false }
            ],
            onRegisterApi: function(gridApi) {
              pto.gridApi = gridApi;

            }
          };
        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        pto.closeModal = function () {
          $modalInstance.close(pto.employee);
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