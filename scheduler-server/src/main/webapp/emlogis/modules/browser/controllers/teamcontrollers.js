
(function () {

    var module =  angular.module('emlogis.browser');

    /* 
     * Controller for a single Team entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per Team API definition
     */
    module.controller(
        'TeamCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', '$modal', 'dialogs', 'alertsManager',
        function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, $modal, dialogs, alertsManager) {

            console.log("TeamCtlr: " + getScopeHierachy($scope));

            var editCtlr = EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);

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
                console.log('in TeamCtlr.getCreateDto()');
                // build the create Dto
                var elt = $scope.elt;

                 // base create dto
                var dto = _.pick( elt, 'clName', 'siteId');
                // add update dto if relevant
                if (elt.name || elt.description) {
                    dto.updateDto =  _.pick( elt, 'name', 'description');
                } 
                return dto;
            };


            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {
                console.log('in TeamCtlr.getUpdateDto()');
                var elt = $scope.elt;
                var dto = _.pick( elt, 'clName', 'name', 'description');
                return dto;
            };

            $scope.vetoCreate = function() {
                if ($scope.elt.siteId) {
                    // sideId is present, one can create the Team
                    return false;
                }
                // check if SiteId is provided as parent entity
                if ($scope.parentEntityId) {
                    $scope.elt.siteId = $scope.parentEntityId;
                    return false;
                }

                // needs to select a Site
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
                    title: 'Please select a Site',
                    columns:  [ // grid columns
                        {name: 'name', label: 'Name', sortable: true},
                        {name: 'description', label: 'Description', sortable: true},
                        {name: 'id', label: 'Id',sortable: true}
                    ],
                    relatedEntityIcon: './img/glyphicons/png/site.png',
                    entity: $scope.entity,
                    relatedentity: 'site',
                    entityTypeLabel: '$scope.entityTypeLabel',
                    relatedentityTypeLabel: 'Site',
                    dataServiceResource: 'sites',
                    dataServiceGetEntitiesAPI: 'getElements'
                };

                $scope.opts.resolve.item = function() {
                    return angular.copy(subctlrConfig); // pass params to Dialog
                };

                var modalInstance = $modal.open($scope.opts);

                modalInstance.result.then(function(selectedEntity){
                    //on ok button press
                    $scope.elt.siteId = selectedEntity.id;
                    // how to trigger saveOrCreate() on base editCtlr ?
                    console.log("Fire create...");
                    $scope.saveOrCreate();
                },function(){
                    //on cancel button press
                    $scope.elt.siteId = false;  // clear selection on cancel
                    console.log("Modal Closed");
                });
                return true;
            };


    }]);

    var ModalInstanceCtrl = function($scope, $modalInstance, $modal, item) {
        console.log("ModaCtlr: " + getScopeHierachy($scope));

        $scope.item = item;
        $scope.ctlr = 'ModalInstanceCtrl';
        $scope.ok = function () {
            if (! $scope.selectedEntity) {
                $scope.cancel();    // in case user has closed wo selecting any item
            }
            $modalInstance.close($scope.selectedEntity);
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };

            
}());




