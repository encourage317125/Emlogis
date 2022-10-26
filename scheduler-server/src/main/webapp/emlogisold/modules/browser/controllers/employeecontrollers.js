(function () {

    var module =  angular.module('emlogis.browser');

    /* 
     * Controller for a single Team entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per Team API definition
     */
    module.controller(
        'EmployeeCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', 'dialogs', 'alertsManager',
        function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager) {

            EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);

            // Override the method to build the create and update Dtos

            /*
            * getCreateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object create  
            */
            $scope.getCreateDto = function() {
                console.log('in EmployeeCtlr.getCreateDto()');
                // build the create Dto
                var elt = $scope.elt;

                 // base create dto
                var dto = _.pick( elt, 'clName', 'firstName', 'lastName', 'employeeIdentifier');
                // add update dto if relevant
                if (elt.middleName || elt.notificationEmail || elt.notificationSmsNumber) {
                    dto.updateDto =  _.pick( elt, 'middleName', 'notificationEmail', 'notificationSmsNumber');
                } 
                // add userAccount dto if relevant
                if (elt.login || elt.email) {
                    dto.userAccountDto =  _.pick( elt, 'login', 'email');
                    dto.userAccountDto.name = dto.userAccountDto.login;     // use login as name 
                } 
                return dto;
            };

            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {
                console.log('in EmployeeCtlr.getUpdateDto()');
                // TODO build the update Dto
                var elt = $scope.elt;

                 // base create dto
                var dto = _.pick( elt, 'clName',
                    'firstName', 'lastName', 'employeeIdentifier', 
                    'middleName', 'notificationEmail', 'notificationSmsNumber',
                    'isEngineSchedulable', 'isManuallySchedulable'
                );
                return dto;
            };

            /*
            * showEmployeeAccount() goes to Account page for account associated to Employee  
            */
            $scope.showEmployeeAccount = function() {

                dataService.getEmployeeAccount($scope.elt.id)
                .then(function (result) {
                    var accountId = result.id;
                    $state.go('entity', {entity: 'useraccount', id: accountId});
                },function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                }); 
            };

            /*
            * showEmployeeContract() goes to default account page 
            */
            $scope.showEmployeeContract = function() {

                dataService.getEmployeeContracts($scope.elt.id, {}, 1, 10)
                .then(function (result) {
                    // we assume only one contract comes back in this version
                    var contractId = result.data[0].id;
                    $state.go('entity', {entity: 'employeecontract', id: contractId});
                },function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                }); 
            };

        }
    ]);

            
}());

