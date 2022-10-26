
(function () {

    var module =  angular.module('emlogis.browser');

    /* 
     * Controller for a single ShiftStructure entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per ShiftStructure API definition
     */
    module.controller(
        'CiavailabilityCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', '$modal', 'dialogs', 'alertsManager',
            function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, $modal, dialogs, alertsManager) {

            console.log("CiavailabilityCtlr: " + _.values($stateParams));
            console.log("CiavailabilityCtlr: " + getScopeHierachy($scope));


            if ($stateParams.parentEntityId) {
                // we have a parentEntittyId specified in state. 
                $scope.parentEntityId = $stateParams.parentEntityId;
            }

            $scope.daysOfTheWeek = [
                {label:'Sunday',     id:0, val:'Sunday'},
                {label:'Monday',     id:1, val:'Monday'},
                {label:'Tuesday',    id:2, val:'Tuesday'},
                {label:'Wednesday',  id:3, val:'Wednesday'},
                {label:'Thursday',   id:4, val:'Thursday'},
                {label:'Friday',     id:5, val:'Sunday'},
                {label:'Saturday',   id:6, val:'Saturday'}    
            ];

            /*
            * override postGetElement() to set some $scope attributes used by partial 
            */
            $scope.postGetElement = function() {
                var elt = $scope.elt;
                if (elt) {
                    var idx; 
                    idx = $scope.elt.dayOfTheWeek;
                    $scope.dayOfTheWeek = $scope.daysOfTheWeek[idx];
                }
            };

            EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);


            // create a default Availability record if create mode (and overrides defaults created by EditOrCreateEntityCtlr)
            if (!$scope.elt || !$scope.elt.id) {
                $scope.readonly = 'readonly';
                $scope.elt = {
                    clName: 'CIAvailabilityTimeFrame'//,
                    //availabilityType: $scope.availabilityTypes[0].val,
                    //dayOfTheWeek: $scope.daysOfTheWeek[1].val
                };
                $scope.availabilityType = $scope.availabilityTypes[0];
                $scope.dayOfTheWeek = $scope.daysOfTheWeek[1];
            }

            /*
            * Go to parent entity page 
            * workaround for                 
            * ui-sref="entity({entity: 'employee', id: parentEntityId })
            * which for some reason gets was getting null for parentEntityId 
            * However tgis seems to be working now.
            */
            /*
            $scope.backToParent = function() {
                $state.go('entity', {entity: $stateParams.parentEntity, id: $stateParams.parentEntityId},  {reload: false});
            }; 
            */

            // Override the method to build the create and update Dtos

            /*
            * getCreateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object create  
            */
            $scope.getCreateDto = function() {
                console.log('in CiavailabilityCtlr.getCreateDto()');
                // build the create Dto
                var elt = $scope.elt;                
                var dto = _.pick(elt, 'clName', 'absenceTypeId', 'reason',  'startTime', 'durationInMinutes', 'startDate', 'endDate');
                dto.employeeId = $scope.parentEntityId;
                dto.availabilityType = 'UnAvail';
                dto.dayOfTheWeek = $scope.dayOfTheWeek.id;
                return dto;
            };

            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {
                console.log('in CiavailabilityCtlr.getUpdateDto()');
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




