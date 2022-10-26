// TODO: allow the selection of more than one ShiftStructure on creation


(function () {

    var module =  angular.module('emlogis.browser');

    // Image Base folder path
    var imagePath = "modules/browser/assets/images/";


    /* 
     * Controller for a single ShiftStructure entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per ShiftStructure API definition
     */
    module.controller(
        'ShiftstructureCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', '$modal', 'dialogs', 'alertsManager',
            function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, $modal, dialogs, alertsManager) {

            console.log("ShiftstructureCtlr: " + _.values($stateParams));
            console.log("ShiftstructureCtlr: " + getScopeHierachy($scope));

            EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);

            if ($stateParams.parentEntityId) {
                // we have a parentEntittyId specified in state. 
                $scope.parentEntityId = $stateParams.parentEntityId;
            }

            // Override the method to build the create and update Dtos

            /*
            * getCreateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object create  
            */
            $scope.getCreateDto = function() {
                console.log('in ShiftstructureCtlr.getCreateDto()');
                // build the create Dto
                var elt = $scope.elt;

                 // base create dto  // d.toDateString() // Date.parse("March 21, 2012")
                var startDate = Date.parse(elt.startDate); 
                var dto = {clName: elt.clName, startDate: startDate, teamId: elt.teamId};
                return dto;
            };


            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {
                console.log('in ShiftstructureCtlr.getUpdateDto()');
                var elt = $scope.elt;
                var dto =  _.pick( elt, 'clName', 'startDate');
                return dto;
            };

            $scope.vetoCreate = function() {
                if ($scope.elt.teamId && $scope.elt.startDate) {
                    // teamId is present, one can create the ShiftStructure
                    return false;
                }
                // needs to select a Team, unless parent team is specified
                if( !$scope.elt.teamId) {
                    if ($scope.parentEntityId) {
                        $scope.elt.teamId = $scope.parentEntityId;
                        return false;
                    }

                    $scope.opts = {
                        scope: $scope,  // use current scope for modal so as to get results more conveniently
                        backdrop: true,
                        backdropClick: true,
                        dialogFade: false,
                        keyboard: true,
                        templateUrl : 'modules/browser/partials/entity_selectentity.tpl.html',
                        controller : ModalInstanceCtrl,
                        size : 'lg',
                        resolve: {} // empty storage
                    };

                    var subctlrConfig = {
                        title: 'Please select a Team',
                        entity: $scope.entity,
                        relatedentity: 'team',
                        entityTypeLabel: '$scope.entityTypeLabel',
                        relatedentityTypeLabel: 'Team',
                        dataServiceResource: 'teams',
                        dataServiceGetEntitiesAPI: 'getElements',
                        columns: [ // grid columns
                            {
                                name: 'name',
                                label: 'Name',
                                sortable: true
                            }, {
                                name: 'abbreviation',
                                label: 'Abbreviation',
                                sortable: true
                            }, {
                                name: 'id',
                                label: 'Id',
                                sortable: true
                            }
                        ],
                        relatedEntityIcon: imagePath + 'glyphicons/png/team.png'
                    };

                    $scope.opts.resolve.item = function() {
                        return angular.copy(subctlrConfig); // pass params to Dialog
                    };

                    var modalInstance = $modal.open($scope.opts);

                    modalInstance.result.then(function(selectedEntity){
                        //on ok button press
                        $scope.elt.teamId = selectedEntity.id;
                        // how to trigger saveOrCreate() on base editCtlr ?
                        console.log("Fire create...");
                        $scope.saveOrCreate();
                    },function(){
                        //on cancel button press
                        $scope.elt.siteId = false;  // clear selection on cancel
                        console.log("Modal Closed");
                    });
                    return true;
                }
            };


    }]);

            
}());




