
(function () {

    var module =  angular.module('emlogis.browser');

    /* 
     * Controller for a single ShiftStructure entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per ShiftStructure API definition
     */
    module.controller(
        'CdavailabilityCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', '$modal', 'dialogs', 'alertsManager',
            function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, $modal, dialogs, alertsManager) {

            console.log("CdavailabilityCtlr: " + _.values($stateParams));
            console.log("CdavailabilityCtlr: " + getScopeHierachy($scope));


            if ($stateParams.parentEntityId) {
                // we have a parentEntittyId specified in state. 
                $scope.parentEntityId = $stateParams.parentEntityId;
            }

            $scope.availabilityTypes = [
                {label:'Availability',   id:0, val: 'Avail'},
                {label:'Unavailability', id:1, val: 'UnAvail'}
            ];

            /*
            * override postGetElement() to set some $scope attributes used by partial 
            */
            $scope.postGetElement = function() {
                var elt = $scope.elt;
                if (elt) {
                    var idx; 
                    idx = ($scope.elt.availabilityType  == 'Avail' ? 0 : 1);
                    $scope.availabilityType = $scope.availabilityTypes[idx];
                }
            };

            EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);


            // create a default Availability record if create mode (and overrides defaults created by EditOrCreateEntityCtlr)
            if (!$scope.elt || !$scope.elt.id) {
                $scope.readonly = 'readonly';
                $scope.elt = {
                    clName: 'CDAvailabilityTimeFrame'//,
                };
                $scope.availabilityType = $scope.availabilityTypes[0];
            }


            // Override the method to build the create and update Dtos

            /*
            * getCreateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object create  
            */
            $scope.getCreateDto = function() {
                console.log('in CdavailabilityCtlr.getCreateDto()');
                // build the create Dto
                var elt = $scope.elt;                
                var dto = _.pick(elt, 'clName', 'absenceTypeId', 'reason',  'startTime', 'durationInMinutes', 'startDate');
                dto.employeeId = $scope.parentEntityId;
                // set availabilityType by its value (control sets a $scope.availabilityTypes entry)
                dto.availabilityType = $scope.availabilityType.val;
                return dto;
            };

            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {
                console.log('in CdavailabilityCtlr.getUpdateDto()');
                return $scope.getCreateDto(); // dto is same as for create
            };

            $scope.vetoCreate = function() {
                if ($scope.availabilityType.val == 'Avail' || $scope.elt.absenceTypeId) {
                    return false;
                }
                // Absence requested but absence type unspecified. need to select one
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
                    title: 'Please select an Absence Type',
                    entity: $scope.entity,
                    relatedentity: 'absencetype',
                    entityTypeLabel: '$scope.entityTypeLabel',
                    relatedentityTypeLabel: 'Absence Type',
                    dataServiceResource: 'absencetypes',
                    dataServiceGetEntitiesAPI: 'getElements',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/unknown.png'
                };
                $scope.opts.resolve.item = function() {
                    return angular.copy(subctlrConfig); // pass params to Dialog
                };

                var modalInstance = $modal.open($scope.opts);

                modalInstance.result.then(function(selectedEntity){
                    //on ok button press
                    $scope.elt.absenceTypeId = selectedEntity.id;
                    // how to trigger saveOrCreate() on base editCtlr ?
                    console.log("Fire create...");
                    $scope.saveOrCreate();
                },function(){
                    //on cancel button press
                    delete $scope.elt.absenceTypeId;  // clear selection on cancel
                    console.log("Modal Closed");
                });
                return true;
            };
    }]);
            
}());




