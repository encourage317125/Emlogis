(function () {

// test
//var fn = function(name) { deleteElement(name); return "deleted: " + name;  function deleteElement(elt){console.log('delete ' +elt); }};

/*
    // utility and convenience methods and controlers for the browser module
*/

    var module =  angular.module('emlogis.browser');


    /*
     *  Controller for displaying the top level List of an Entity
     *
     * It drives the display of the selected Entity Detailed Information / Browser
     * (on selection of a row (element), it displays the corresponding information
     * and allows also to delete one entity from the list. 
     *
     * As of now, the only required parameters is the entity name provided via the $stateParams parameter
     *
     */

    module.controller(
        'EntityListCtlr', 
        ['$scope', '$stateParams', '$state', '$filter', 'authService', 'appContext', 'dataService', 'dialogs',
        function($scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs) {

            console.log('inside EntityListCtlr controller for: ' + $stateParams.entity + '/' + $stateParams.id);

            var listandcardpagingctlr = new ListAndCardPagingCtlr();
            listandcardpagingctlr.init.call(listandcardpagingctlr, $scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs);
            listandcardpagingctlr.getElements();

            // Delete Element details according to selected row
            // TODO (curently we don't have the ability to select one or several rows)
            /*
            $scope.deleteOrRemoveElt = function(element){
                $scope.selectedElement = element;
                console.log('--> TODO: deleteOrRemoveElt(' + element + ')' + ' => ' + element.name + ',' + element.id);
                
                //var to = "groupaccount";
                //$state.go(to, {entity: $stateParams.entity, id: element.id});
                var x =1;
            }; 
            */

            // delete the current row Element  
            $scope.deleteElement = function(element) {
                var dlg = dialogs.confirm('Please Confirm','Do you realy want to delete entity ' + element.name +'?');
                dlg.result.then(function(btn){
                    deleteElement(element);
                },function(btn){
                    console.log("entity not deleted");
                });
            };

            // ===========================================================================
            // Controller private methods

            // delete Element  
            function deleteElement(elt){

                dataService
                .deleteElement($scope.resource, elt.id)
                .then(function (result) {
                    console.log('--> deleted[' + elt.name + ': ' + elt.id + ']'); 
                    $scope.elements = _.without($scope.elements, elt); 
                    listandcardpagingctlr.getElements(); // force refresh
                }, function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                });               
            }
        }   

    ]);



    /*
     *  Controller for displaying/updating information about a single Entity / cretaing an entity
     *
     * Allows to 
     * - create a new entity
     * - update an existing entity 
     * - display related entities and navigate to these related entities
     *
     * As of now, the only required parameters is the entity name provided via the $stateParams parameter
     *
     */
    module.controller(
        'EntityCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService', 'dialogs', 'alertsManager',
        function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager) {

            console.log("EntityCtlr: " + _.values($stateParams));

            EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);

//            var ctlrConfig = $stateParams.ctlrConfig;
//            $state.go('entity.relatedentity', {relatedentity: ctlrConfig.defaultRelatedEntity});
        }          
    ]);

     /* 
     *  Controller for Listing the 'secundary' entities of 'primary' entity
     * Allows to  add / removee a 'secundary' to/from the 'primary' 
     *  AND / OR 
     * to create and associate / delete secundary entities to/from the primary one
     * Add / Create / Remove / Delete are driven by permissions (see below) specified in config
     * for short,
     * viewPermissions      allow to view an entity
     * addPermissions       allow to associate a selected secundary entity to the primary one
     * removePermissions    allow to un-associate a selected secundary entity from the primary one
     * createPermissions    allow to create and associate a secundary entity to the primary one
     * deletePermissions:   allow to un-associate selected secundary entity from the primary one and delete the secundary entity
     *
     * When invoked, this controler expects in the $stateParams the following attributes:
     * - entity: the name of the 'primary' entity (ex 'groupaccount' in add user to group scenario)
     * - relatedentity: the name of the 'secundary' entity (ex 'useraccount' in add user to group scenario)

     * AND in the  $stateParams.ctlrConfig object, the following attributes:
     *
     * //entityTypeLabel: user friendly label for the 'primary' entity
     * //relatedentityTypeLabel: user friendly label for the 'secundary' entity
     * 
     *
     * - dataServiceResource: the data service resource parameter for some APIs that need it like account role mngt  apis
     * that are generic and apply to UserAccounst as well GroupAccounts 
     * !! Leave unspecified for general cases !!
     *
     * - dataServiceGetAssociatedEntitiesAPI: name of dataservice API to invoke to get the list of 'secundary' entities 
     * currently unassociated  to the primary one. (ex: 'getGroupMembers' in add user to group scenario)
     *
     *
     *
     * Parameters for adding / removing related entities 
     * -------------------------------------------------
     *
     * - dataServiceGetUnassociatedEntitiesAPI: name of dataservice API to invoke to get the list of unassociated 'secundary' entities 
     * (ex: 'getGroupMembers' in add user to group scenario)
     *
     * - dataServiceAssociateEntityAPI: name of dataservice API to invoke to associate the 'secundary' entity to the primary one
     * (ex: 'addMemberToGroup' n add user to group scenario)
     *
     * - dataServiceUnassociateEntityAPI: name of dataservice API to invoke to associate the 'secundary' entity to the primary one
     * (ex: 'removeMemberFromGroup' in remove user from group scenario)
     *
     * - secundaryEntityAddPartial: name of partial to diplay the secundary 'pickup' list on Add operation
     *
     *
     * Parameters for creating / deleting related entities
     * ---------------------------------------------------
     *
     * - dataServiceDeleteAssociatedEntityAPI: name of dataservice API to invoke to create & assoc the 'secundary' entity to the primary one
     * (ex: 'addMemberToGroup' n add user to group scenario)
     *
     * - dataServiceDeleteAssociatedEntityAPI: name of dataservice API to invoke to un-associate & delete 'secundary' entity from the primary one
     * (ex: 'removeMemberFromGroup' in remove user from group scenario)
     *
     * - secundaryEntityAddPartial: name of partial to diplay the secundary 'pickup' list on Add operation
     *
     * - secundaryEntityCreatePartial: name of partial to diplay the secundary  entity creation
     */

    module.controller(
        'SecundaryEntityListCtlr', 
        ['$scope', '$stateParams', '$state', '$filter', 'authService', 'appContext', 'dataService', '$modal', 'dialogs',
        function($scope, $stateParams, $state, $filter, authService, appContext, dataService, $modal, dialogs) {

            console.log('inside SecundaryEntityListCtlr controller for: ' + $stateParams.entity + '/' + $stateParams.id + ' to: ' + $stateParams.relatedentity);
            $scope.relatedentity = $stateParams.relatedentity;
            var entity2resource = appContext.get('entity2resource');             
            $scope.relatedentityTypeLabel = entity2resource.getLabel($scope.relatedentity);
            $scope.entityId = $stateParams.id;
            var ctlrConfig = $stateParams.ctlrConfig;

            // get a list controller
            var listctlr = new ListAndCardPagingCtlr();
            listctlr.init.call(listctlr, $scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs);

            // override the getElemnts() ftn to get only Group members
            listctlr.getElements = function(){
                console.log('inside  redefined SecundaryEntityListCtlr.getElements()');
                console.log("inside  redifined getElements, $scope: " + $scope);
                console.log("inside  redifined getElements, resource: " + $scope.resource);

                var queryParams = {filter: $scope.q};

                if ($scope.orderby) {
                    queryParams.orderby = $scope.orderby;
                    if($scope.orderColumn.dto){
                        queryParams.orderby = $stateParams.relatedentity + '.' + $scope.orderby;
                    }
                    queryParams.orderdir = (!$scope.reverse ? 'ASC' : 'DESC');
                }

                var params = [$stateParams.id, queryParams, $scope.currentPage, $scope.pageSize];
                if (ctlrConfig.dataServiceResource) {
                    params.unshift(ctlrConfig.dataServiceResource)
                }
                dataService[ctlrConfig.dataServiceGetAssociatedEntitiesAPI]              
                .apply(this, params)
                .then(function (result) {
                    $scope.elements = result.data;
                    $scope.totalRecords = result.total;

                    // debug code
                    var i = 0;
                    for( i=0; i<$scope.elements.length; i++){
                        var elt = $scope.elements[i];
                        console.log('--> loaded[' + i + ']' + elt.clName + ': ' + elt.id);                  
                    }
                    // end debug
                    $scope.filterElements($scope.filterText); 
                }, function (error) {
                        dialogs.error("Error", error.data.message, 'lg');
                }); 
            };

            $scope.orderColumn;
            if(ctlrConfig.columns){
                $scope.orderColumn = ctlrConfig.columns[0];
            }

            $scope.setOrder = function (orderby) {
                if (orderby.name === $scope.orderby) {
                    $scope.reverse = !$scope.reverse;
                }
                $scope.orderby = orderby.name;
                $scope.orderColumn = orderby;
                $scope.this.getElements();
                var x  = 1;
            };

            listctlr.getElements();

            // un-associate the current related entity
            $scope.removeElement = function(elt) {
                var dlg = dialogs.confirm('Please Confirm','Do you realy want to delete relationship ' + elt.name +'?');
                dlg.result.then(function(btn){
                    removeElement(elt);
                    var x = 1;
                },function(btn){
                    console.log("entity not deleted");
                });
            };

            function removeElement(elt){

                var params = [$stateParams.id, elt.id];     // standard case
                if (ctlrConfig.unassociateIdAttribute) {
                    // related object id is in a subobject
                    var attrSpec = ctlrConfig.unassociateIdAttribute;
                    var relatedId = elt[attrSpec.o][attrSpec.id]; 
                    params = [$stateParams.id, relatedId];
                }
                if (ctlrConfig.dataServiceResource) {
                    params.unshift(ctlrConfig.dataServiceResource)
                }
                dataService[ctlrConfig.dataServiceUnassociateEntityAPI]
                    .apply(this, params)
                    .then(function (result) {
                        console.log('--> removed[' + elt.name + ': ' + elt.id + ']');
                        $scope.elements = _.without($scope.elements, elt);
                        listctlr.getElements(); // force refresh
                    }, function (error) {
                        dialogs.error("Error", error.data.message, 'lg');
                    });
            };

            // delete the current related entity
            $scope.deleteElement = function(elt) {

                $scope.toBeDeletedEltLabel = (elt.name ? elt.name : elt.id);
                if ($scope.secundaryEntity) {
                    // api call has form: api($parentId, id)
                    var params = [$stateParams.id, elt.id];     
                    dataService[ctlrConfig.dataServiceDeleteAssociatedEntityAPI]
                        .apply(this, params)
                        .then(function (result) {
                            var label = $scope.toBeDeletedEltLabel;
                            console.log('--> deleted[' + label + ']'); 

                            // update state
                            $scope.elements = _.without($scope.elements, elt);
                            listctlr.getElements(); // force refresh
                            //$stateParams.id = elt.id; // doesn't work
                            dialogs.notify("Success", "'" + label + "' successfully deleted.");
                        },function (error) {
                            var label = $scope.toBeDeletedEltLabel;
                            dialogs.error("Error", "Error while deleting: '" + label + "': " + error.data.message, 'lg');
                        });
                }
                else {
                    // pseudo top level entity, api call has standard form: deleteElement(id)
                    var entity2resource = appContext.get('entity2resource');             
                    var resource = entity2resource.getResource($scope.relatedentity);
                    dataService
                        .deleteElement(resource, elt.id)
                        .then(function (result) { 
                            var elt = result;
                            var label = $scope.toBeDeletedEltLabel;
                            console.log('--> deleted[' + label + ']'); 

                            // update state
                            $scope.elements = _.without($scope.elements, elt);
                            listctlr.getElements(); // force refresh
                            //$stateParams.id = elt.id; // doesn't work
                            dialogs.notify("Success", "'" + label + "' successfully deleted.");
                        },function (error) {
                            var label = $scope.toBeDeletedEltLabel;
                            dialogs.error("Error", "Error while deleting: '" + label + "': " + error.data.message, 'lg');
                        });
                }  
                var x = 1;
            };

            /*
            * showAddModal() invoked when user click on Add related entity button
            */
            $scope.showAddModal = function() {

                $scope.opts = {
                    backdrop: true,
                    backdropClick: true,
                    dialogFade: false,
                    keyboard: true,
                    templateUrl : ctlrConfig.secundaryEntityAddPartial,
                    controller : ModalInstanceCtrl,
                    size : 'lg',
                    resolve: {} // empty storage
                };

                var subctlrConfig = {
                    title: 'Add ' + $scope.relatedentityTypeLabel  + '(s) to ' +  $scope.entityTypeLabel,
                    entity: $scope.entity,
                    relatedentity: $scope.relatedentity,
                    entityTypeLabel: $scope.entityTypeLabel,
                    relatedentityTypeLabel: $scope.relatedentityTypeLabel,
                    columns: ctlrConfig.columns,
                    relatedEntityIcon: ctlrConfig.relatedEntityIcon,
                    dataServiceResource: ctlrConfig.dataServiceResource,
                    dataServiceGetUnassociatedEntitiesAPI: ctlrConfig.dataServiceGetUnassociatedEntitiesAPI,
                    dataServiceAssociateEntityAPI: ctlrConfig.dataServiceAssociateEntityAPI
                };
                $scope.opts.resolve.item = function() {
                    return angular.copy(subctlrConfig); // pass params to Dialog
                }

                var modalInstance = $modal.open($scope.opts);

                modalInstance.result.then(function(){
                    //on ok button press, refresh list
                    listctlr.getElements();
                },function(){
                    //on cancel button press
                    console.log("Add Modal Closed");
                });
            };


            /*
            * showElementDetails() displays Element details according to selected row
            * overloads base showElementDetails() to deal with non top level entities 
            */
            $scope.showElementDetails = function(elt){

                var parentEntity = $scope.entity,
                    parentEntityId  = $stateParams.id; // = $scope.entityId

                $scope.selectedElement = elt;

                // get entity from element class name.
                var actualentity = elt.clName.toLowerCase();
                if ($scope.secundaryEntity) {
                    $state.go( 'createchildentity', {
                        entity: $scope.relatedentity,  // sometimes we need to use the actualentity => make it configurable
                        id: elt.id, 
                        parentEntity: $scope.entity, 
                        parentEntityId: $stateParams.id
                    });
                }
                else {
                    var actualentity = elt.clName.toLowerCase();
                    console.log('--> showElementDetails(' + actualentity + ':' + elt + ')' + ' => ' + elt.name + ',' + elt.id);
                    var to = 'entity';
                    $state.go(to, {entity: actualentity, id: elt.id});
                }
                var x =1;
            }; 

            /*
            * showCreateModal() invoked when user click on Add 'New' related entity button
            */
            $scope.showCreateModal = function() {

                // new approach. redirect to entity page with extra params
//                $state.go( 'entity', {entity: $scope.relatedentity, id: 'new', parentEntity: $scope.entity, parentEntityId: $stateParams.id});

                $state.go( 'createchildentity', {entity: $scope.relatedentity, id: 'new', parentEntity: $scope.entity, parentEntityId: $stateParams.id});
                return;


                $scope.opts = {
                    backdrop: true,
                    backdropClick: true,
                    dialogFade: false,
                    keyboard: true,
                    templateUrl : ctlrConfig.secundaryEntityCreatePartial,
                    controller : ModalInstanceCtrl,
                    size : 'lg',
                    resolve: {} // empty storage
                };

                var subctlrConfig = {
                    title: 'Add ' + $scope.relatedentityTypeLabel  + '(s) to ' +  $scope.entityTypeLabel,
                    entity: $scope.entity,
                    relatedentity: $scope.relatedentity,
                    entityTypeLabel: $scope.entityTypeLabel,
                    relatedentityTypeLabel: $scope.relatedentityTypeLabel,
                    dataServiceResource: ctlrConfig.dataServiceResource,
                    dataServiceCreateAssociatedEntityAPI: ctlrConfig.dataServiceCreateAssociatedEntityAPI
                };
                $scope.opts.resolve.item = function() {
                    return angular.copy(subctlrConfig); // pass params to Dialog
                }

                var modalInstance = $modal.open($scope.opts);

                modalInstance.result.then(function(){
                    //on ok button press, refresh list
                    listctlr.getElements();  
                },function(){
                    //on cancel button press
                    console.log("Create Modal Closed");
                });
            };
        }          
    ]);

    /* 
     * Controller allowing to display a list of entities that can be associated to a an other 'primary' entity
     *
     * When invoked, this controler expects in the $scope.item object, the following attributes
     *
     * entity: the name of the 'primary' entity (ex 'groupaccount' in add user to group scenario)
     * relatedentity: the name of the 'secundary' entity (ex 'useraccount' in add user to group scenario)
     * entityTypeLabel: user friendly label for the 'primary' entity
     * relatedentityTypeLabel: user friendly label for the 'secundary' entity
     *
     * - dataServiceResource: the data service resource parameter for some APIs that need it like account role mngt  apis
     * that are generic and apply to UserAccounst as well GroupAccounts 
     * !! Leave unspecified for general cases !!
     *
     * - dataServiceGetUnassociatedEntitiesAPI: name of dataservice API to invoke to get the list of unassociated 'secundary' entities 
     * (ex: 'getUnassociatedGroupMembers' n add user to group scenario)
     *
     * - dataServiceAssociateEntityAPI: name of dataservice API to invoke to associate the 'secundary' entity to the primary one
     * (ex: 'addMemberToGroup' n add user to group scenario)
     */

    module.controller(
        'AddRelatedEntityCtrl', ['$scope', '$stateParams', '$state', '$filter', 'authService', 'appContext', 'dataService', 'dialogs', 'alertsManager',
            function($scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs, alertsManager) {

                $scope.alerts = [];

                //overide entity and relatedentity state params

                // init controler from params provided in $scope.item
                var ctlrConfig = $scope.item;
                /*
                $state.entity = ctlrConfig.entity;
                $state.relatedentity = ctlrConfig.relatedentity;
                $scope.entityTypeLabel = ctlrConfig.entityTypeLabel;
                $scope.relatedentityTypeLabel = ctlrConfig.relatedentityTypeLabel;
                */
                $state.entity = ctlrConfig.relatedentity;
                $state.relatedentity = ctlrConfig.entity;
                $scope.entityTypeLabel = ctlrConfig.relatedentityTypeLabel;
                $scope.relatedentityTypeLabel = ctlrConfig.entityTypeLabel;


                var listandcardpagingctlr = new ListAndCardPagingCtlr();
                listandcardpagingctlr.init.call(listandcardpagingctlr, $scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs);
                listandcardpagingctlr.getElements = function(){

                    var queryParams = {filter: $scope.q};
                    if ($scope.orderby) {
                        queryParams.orderby = $scope.orderby;
                        queryParams.orderdir = (!$scope.reverse ? 'ASC' : 'DESC');
                    }

                    var params = [$stateParams.id, queryParams, $scope.currentPage, $scope.pageSize];
                    if (ctlrConfig.dataServiceResource) {
                        params.unshift(ctlrConfig.dataServiceResource)
                    }
                    dataService[ctlrConfig.dataServiceGetUnassociatedEntitiesAPI]              
                    .apply(this, params)
                    .then(function (result) {
                        $scope.elements = result.data;
                        $scope.totalRecords = result.total;

                        // debug code
                        var i = 0;
                        for( i=0; i<$scope.elements.length; i++){
                            var elt = $scope.elements[i];
                            console.log('--> loaded[' + i + ']' + elt.clName + ': ' + elt.id);
                        }
                        // end debug
                        $scope.filterElements($scope.filterText);
                    }, function (error) {
                        dialogs.error("Error", error.data.message, 'lg');
                    });
                };

                listandcardpagingctlr.getElements();

                $scope.associateEntity = function(element) {
                    associateEntity(element);
                };

                $scope.closeAlert = function(index) {
                    $scope.alerts.splice(index, 1);
                };

                function associateEntity(elt){
                    $scope.elt = elt;

                    var params = [$stateParams.id, elt.id];
                    if (ctlrConfig.dataServiceResource) {
                        params.unshift(ctlrConfig.dataServiceResource)
                    }
                    dataService[ctlrConfig.dataServiceAssociateEntityAPI]              
                    .apply(this, params)
                    .then(function (result) {

                        console.log('--> added[' + elt.name + ': ' + elt.id + ']');                           
//                          $scope.elements = _.without($scope.elements, elt);
                        var label = (elt.name !== undefined ? elt.name : 'elt with id: ' + elt.id);
                            alertsManager.addAlert("'" + label + "' successfully added to " +
                                $scope.relatedentityTypeLabel, "success");
                        listandcardpagingctlr.getElements();
                    }, function (error) {

                        $scope.error = error;
                        var label = (elt.name !== undefined ? elt.name : 'elt with id: ' + elt.id);
                        dialogs.error("Error", "Error while adding: '" + label + "' to " +
                             $scope.relatedentityTypeLabel + ": " + error.data.message, 'lg');
                    });
                }

            }
        ]
    );


    /* 
     * Controller allowing to create a new 'sub' entity that will be asscoiated to a parent entity
     *
     * When invoked, this controler expects in the $scope.item object, the following attributes
     *
     * entity: the name of the 'primary' entity (ex 'groupaccount' in add user to group scenario)
     * relatedentity: the name of the 'secundary' entity (ex 'useraccount' in add user to group scenario)
     * entityTypeLabel: user friendly label for the 'primary' entity
     * relatedentityTypeLabel: user friendly label for the 'secundary' entity
     *
     * - dataServiceResource: the data service resource parameter for some APIs that need it like account role mngt  apis
     * that are generic and apply to UserAccounst as well GroupAccounts 
     * !! Leave unspecified for general cases !!
     *
     * - dataServiceGetUnassociatedEntitiesAPI: name of dataservice API to invoke to get the list of unassociated 'secundary' entities 
     * (ex: 'getUnassociatedGroupMembers' n add user to group scenario)
     *
     * - dataServiceAssociateEntityAPI: name of dataservice API to invoke to associate the 'secundary' entity to the primary one
     * (ex: 'addMemberToGroup' n add user to group scenario)
     */
/*
    module.controller(
        'CreateRelatedEntityCtrl',
         ['$scope', '$stateParams', '$state', '$filter', 'authService', 'appContext', 'dataService',
            function($scope, $stateParams, $state, $filter, authService, appContext, dataService) {

                $("#alert").hide();
                $scope.alerts = [];

                console.log( "in CreateRelatedEntityCtrl");

                // $scope. entity=team, id=teamid


                //overide entity and relatedentity state params

                // init controler from params provided in $scope.item
                var ctlrConfig = $scope.item;


            }
        ]
    );
*/
// see controlerhelpers.   must be declared in this file if we want it to have access to some variables
// but create a lot of code duplication
// TODO improve this ...
    var ModalInstanceCtrl = function($scope, $modalInstance, $modal, item) {
        $scope.item = item;
        $scope.ok = function () {
            $modalInstance.close();
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
//

    /* 
     * Controller allowing to display a list of entities for selection of one or several items
     * (NOTE: ONLY ONE item SELECTED SUPPORTED TODAY, but could be easily extended to optionally
     * support single or multiple selections)
     *
     * When invoked, this controler expects in the $scope.item object, the following attributes
     *
     * entity: the name of the 'primary' entity (ex 'team' in Team creation scenario)
     * relatedentity: the name of the 'secundary' entity (ex 'site' in Team creation scenario)
     * entityTypeLabel: user friendly label for the 'primary' entity
     * relatedentityTypeLabel: user friendly label for the 'secundary' entity
     *
     * - dataServiceResource: the data service resource parameter for some APIs that need it like account role mngt  apis
     * that are generic and apply to UserAccounst as well GroupAccounts 
     * !! Leave unspecified for general cases !!
     *
     * - dataServiceGetEntitiesAPI: name of dataservice API to invoke to get the list of  'secundary' entities 
     * (ex: 'getElements' in a Team creation that needs to select a parent Site )
     *
     */

    module.controller(
        'SelectRelatedEntityCtrl', ['$scope', '$stateParams', '$state', '$filter', 'authService', 'appContext', 'dataService', 'dialogs',
            function($scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs) {

                console.log("SelectRelatedEntityCtrl: " + getScopeHierachy($scope));
                console.log("SelectRelatedEntityCtrl state params " + $stateParams.parentEntity + "." + $stateParams.entity);
                console.log("SelectRelatedEntityCtrl scope params " + $scope.entity + "->" + $scope.relatedentity);
                console.log("SelectRelatedEntityCtrl scope order " + $scope.orderby);

                // init controler from params provided in $scope.item
                var ctlrConfig = $scope.item;
                if (ctlrConfig) {
                    // copy controllr config parmas into scope
                    _.extend($scope, ctlrConfig); 
                    // switch entity and related entity params
                    $stateParams.entity = ctlrConfig.relatedentity;
                    $stateParams.relatedentity = ctlrConfig.entity;
                    $stateParams.ctlrConfig = ctlrConfig;
                    $scope.entityTypeLabel = ctlrConfig.relatedentityTypeLabel;
                    $scope.relatedentityTypeLabel = ctlrConfig.entityTypeLabel;
                }


                var listandcardpagingctlr = new ListAndCardPagingCtlr();
                listandcardpagingctlr.init.call(listandcardpagingctlr, $scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs);
                listandcardpagingctlr.getElements = function(){

                    console.log("SelectRelatedEntityCtrl.getElements()");
                    console.log("SelectRelatedEntityCtrl state params " + "." + $stateParams.entity);
                    console.log("SelectRelatedEntityCtrl scope params " + $scope.entity + "->" + $scope.relatedentity);
                    console.log("SelectRelatedEntityCtrl scope order " + $scope.orderby);

                    var queryParams = {filter: $scope.q};
                    if ($scope.orderby) {
                        queryParams.orderby = $scope.orderby;
                        queryParams.orderdir = (!$scope.reverse ? 'ASC' : 'DESC');
                    }

                    var params = [queryParams, $scope.currentPage, $scope.pageSize];
                    if (ctlrConfig.dataServiceResource) {
                        params.unshift(ctlrConfig.dataServiceResource)
                    }
                    dataService[ctlrConfig.dataServiceGetEntitiesAPI]              
                    .apply(this, params)
                    .then(function (result) {
                        $scope.elements = result.data;
                        $scope.totalRecords = result.total;
                        $scope.filterElements($scope.filterText);
                    }, function (error) {
                            dialogs.error("Error", error.data.message, 'lg');
                    });
                };

                listandcardpagingctlr.getElements();

                /* 
                 * default element select action: just set selected entity in parent ctlr 
                 * (actually, 2 levels up, which should be the modal dialog box ctlr)
                */
                $scope.selectEntity = function(elt) {
                    console.log('--> selected[' + elt.name + ': ' + elt.id + ']'); 
                    // put selected item in modal controler
                    $scope.$parent.$parent.selectedEntity = elt;

                    if ($scope.lastSelected) {
                        $scope.lastSelected.selected = '';
                    }
                    this.selected = 'selected';
                    $scope.lastSelected = this;
                };

            }
        ]
    );


}());

