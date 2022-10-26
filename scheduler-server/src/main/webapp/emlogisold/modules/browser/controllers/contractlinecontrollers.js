// TODO: allow the selection of more than one ContractLine on creation


(function () {

    var module =  angular.module('emlogis.browser');

    /* 
     * Controller for a single ContractLine entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per ContractLine API definition
     */
    module.controller(
        'ContractlineCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', '$modal',
        function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, $modal) {

            console.log("ContractLineCtlr: " + getScopeHierachy($scope));

            var editCtlr = EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService);

            if ($stateParams.parentEntityId) {
                // we have a parentEntittyId specified in state. 
                $scope.parentEntityId = $stateParams.parentEntityId;
            }

            $scope.ctlr = 'ContractLineCtrl';

            if (!$scope.elt || !$scope.elt.id) {
                // create a default contract line
                $scope.readonly = 'readonly';
                $scope.elt = {
                    contractLineType: 'HOURS_PER_WEEK',
                    minimumEnabled: true,
                    minimumValue: 40,
                    minimumWeight: 100,
                    maximumEnabled: true,
                    maximumValue: 40,
                    maximumWeight: 100
                };
            }

            $scope.type2class = {
                HOURS_PER_DAY: 'IntMinMaxCL',
                HOURS_PER_WEEK: 'IntMinMaxCL',
                HOURS_BETWEEN_SHIFTS: 'IntMinMaxCL',
                HOURS_PER_WEEK_PRIME_SKILL: 'IntMinMaxCL',
                DAYS_PER_WEEK: 'IntMinMaxCL',
                CONSECUTIVE_WORKING_DAYS: 'IntMinMaxCL',
                COMPLETE_WEEKENDS: 'BooleanCL'
            }

            // Override the method to build the create and update Dtos

            /*
            * getCreateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object create  
            */
            $scope.getCreateDto = function() {
                console.log('in ContractLineCtlr.getCreateDto()');
                // build the create Dto
                var elt = $scope.elt;
                elt.contractId = $scope.parentEntityId;

                var dto
                switch ($scope.type2class[elt.contractLineType]) {
                case 'IntMinMaxCL':
                    dto = _.pick(elt, 'clName',  'minimumEnabled', 'minimumValue', 'minimumWeight', 'maximumEnabled', 'maximumValue', 'maximumWeight');
                    break;
                case 'BooleanContractLine':
                    dto = _.pick(elt,  'enabled', 'weight');
                    break;
                default:
                    // invalid type
                    return null;
                }

                if (elt.clName) {
                    dto.clName = elt.clName;
                }
                dto.contractLineType = elt.contractLineType;
                dto.contractId = elt.contractId;
                dto.name = dto.contractLineType;

/*
                public final static String CATEGORY = "category";
    public final static String NAME = "name";
    public final static String CONTRACTLINETYPESTRING = "contractLineType";
    public final static String CONTRACTID = "contractId";
    public final static String MINIMUMENABLED = "minimumEnabled";
    public final static String MINIMUMVALUE = "minimumValue";
    public final static String MINIMUMWEIGHT = "minimumWeight";
    public final static String MAXIMUMENABLED = "maximumEnabled";
    public final static String MAXIMUMVALUE = "maximumValue";
    public final static String MAXIMUMWEIGHT = "maximumWeight";
    
    private String category;
    private String name;
    private String contractLineType;
    private String contractId;
    private boolean minimumEnabled;
    private int minimumValue;
    private int minimumWeight;
    private boolean maximumEnabled;
    private int maximumValue;
    private int maximumWeight;
*/
                return dto;
            };


            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {
                console.log('in ContractLineCtlr.getUpdateDto()');
                var elt = $scope.elt;
                // TODO !! PENDING IMPLEMENTATION
                //var dto =  _.pick( elt, 'clName', 'name', 'description', 'type');
                //return dto;
            };

            $scope.vetoCreate = function() {
                return false;
            };


    }]);
            
}());



