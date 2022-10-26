// TODO: allow the selection of more than one ShiftStructure on creation


(function () {

    var module =  angular.module('emlogis.browser');
    var imagePath = "modules/browser/assets/images/";
    /* 
     * Controller for a single ShiftStructure entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per ShiftStructure API definition
     */
    module.controller(
        'ShiftreqCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', '$modal', 'dialogs', 'alertsManager',
            function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, $modal, dialogs, alertsManager) {

            console.log("ShiftreqCtlr: " + _.values($stateParams));
            console.log("ShiftreqCtlr: " + getScopeHierachy($scope));

            var edirCtrl = EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);

            $scope.ctlr = 'ShiftreqCtlr';
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

                // build the create Dto
                var elt = $scope.elt;
                var dto = _.pick( elt,  'clName', 'startTime', 'durationInMins', 'dayIndex', 'night', 'excess', 'shiftTypeId', 'skillId', 'employeeCount', 'skillProficiencyLevel');
                dto.startTime = Date.parse(elt.startTime);
//                // line below should not be required ...
//                // TODO to be removed when backend API is fixed

                dto.shiftStructureId = $scope.parentEntityId;
                return dto;
            };


            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {

                // TODO !! for now, just return the element as is
                var elt = $scope.elt;
                var dto = _.pick( elt, 'clName','startTime', 'durationInMins', 'dayIndex', 'night', 'excess', 'shiftTypeId', 'skillId', 'employeeCount', 'skillProficiencyLevel');
                 // line below should not be required ...
                // TODO to be removed when backend API is fixed
               dto.shiftStructureId = $scope.parentEntityId;
                return elt;
            };

            $scope.vetoCreate = function() {
                if ($scope.checkConfigComplete()) {
                    return false;
                }
                var subctlrConfig;
                var modalInstance;
                if( !$scope.elt.shiftTypeId) {
                    // needs to select a ShiftType.
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

                    subctlrConfig = {
                        title: 'Please select a Shift Type',
                        entity: $scope.entity,
                        relatedentity: 'shifttype',
                        entityTypeLabel: '$scope.entityTypeLabel',
                        relatedentityTypeLabel: 'Shift Type',
                        dataServiceResource: 'shifttypes',
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
                        relatedEntityIcon: imagePath + 'glyphicons/png/unknown.png'

                    };

                    $scope.opts.resolve.item = function() {
                        return angular.copy(subctlrConfig); // pass params to Dialog
                    };

                    modalInstance = $modal.open($scope.opts);

                    modalInstance.result.then(function(selectedEntity){
                        //on ok button press
                        $scope.elt.shiftTypeId = selectedEntity.id;
                        if ($scope.checkConfigComplete()) {
                            console.log("Fire create...");
                            $scope.saveOrCreate();
                        }
                    },function(){
                        //on cancel button press
                        $scope.elt.shiftTypeId = false;  // clear selection on cancel
                        console.log("Modal Closed");
                    });
                }


                if( !$scope.elt.skillId) {
                    // needs to select a Skill.
                    // we display the full list here, but should display only team skills
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

                    subctlrConfig = {
                        title: 'Please select a Skill (please pick a Skill that belongs to that Team)',
                        entity: $scope.entity,
                        relatedentity: 'skill',
                        entityTypeLabel: '$scope.entityTypeLabel',
                        relatedentityTypeLabel: 'Skill',
                        dataServiceResource: 'skills',
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
                        relatedEntityIcon: imagePath + 'glyphicons/png/skill.png'
                    };

                    $scope.opts.resolve.item = function() {
                        return angular.copy(subctlrConfig); // pass params to Dialog
                    };

                    modalInstance = $modal.open($scope.opts);

                    modalInstance.result.then(function(selectedEntity){
                        //on ok button press
                        $scope.elt.skillId = selectedEntity.id;
                        if ($scope.checkConfigComplete()) {
                            console.log("Fire create...");
                            $scope.saveOrCreate();
                        }
                    },function(){
                        //on cancel button press
                        $scope.elt.skillId = false;  // clear selection on cancel
                        console.log("Modal Closed");
                    });
                }
                return true;
            };

            $scope.checkConfigComplete = function() {
                if ($scope.elt.shiftTypeId && $scope.elt.skillId) {
                    return true;
                }
            };

            $scope.skillProficiencyLevels = [
                {name:'0', id:0},
                {name:'1', id:1},
                {name:'2', id:2},
                {name:'3', id:3},
                {name:'4', id:4},
                {name:'5', id:5}
            ];

    }]);

            
}());

angular.module("template/timepicker/timepicker.html", []).run(["$templateCache", function ($templateCache) {
    $templateCache.put("template/timepicker/timepicker.html",
            "<table>\n" +
            "	<tbody>\n" +
            "		<tr class=\"text-center\">\n" +
            "			<td><a ng-click=\"incrementHours()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-up\"></span></a></td>\n" +
            "			<td>&nbsp;</td>\n" +
            "			<td><a ng-click=\"incrementMinutes()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-up\"></span></a></td>\n" +
            "			<td ng-show=\"showMeridian\"></td>\n" +
            "		</tr>\n" +
            "		<tr>\n" +
            "			<td style=\"width:50px;\" class=\"form-group\">\n" +
            "				<input type=\"text\" ng-model=\"hours\" ng-change=\"updateHours()\" class=\"form-control text-center\" ng-mousewheel=\"incrementHours()\" ng-readonly=\"readonlyInput\" >\n" +
            "			</td>\n" +
            "			<td>:</td>\n" +
            "			<td style=\"width:50px;\" class=\"form-group\">\n" +
            "				<input type=\"text\" ng-model=\"minutes\" ng-change=\"updateMinutes()\" class=\"form-control text-center\" ng-readonly=\"readonlyInput\" >\n" +
            "			</td>\n" +
            "			<td ng-show=\"showMeridian\"><button type=\"button\" class=\"btn btn-default text-center\" ng-click=\"toggleMeridian()\">{{meridian}}</button></td>\n" +
            "		</tr>\n" +
            "		<tr class=\"text-center\">\n" +
            "			<td><a ng-click=\"decrementHours()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-down\"></span></a></td>\n" +
            "			<td>&nbsp;</td>\n" +
            "			<td><a ng-click=\"decrementMinutes()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-down\"></span></a></td>\n" +
            "			<td ng-show=\"showMeridian\"></td>\n" +
            "		</tr>\n" +
            "	</tbody>\n" +
            "</table>\n" +
            "");
}]);


