
var ListAndCardPagingCtlr = function() {   // defining base constructor
    console.log("ListAndCardPagingCtlr base constructor");
    return this;
};

/*
 * Default / Base helper controller for displaying Lists with paging and Card view
 */
ListAndCardPagingCtlr.prototype.init = function($scope, $stateParams, $state, $filter, authService, appContext, dataService, dialogs) {

        console.log("ListAndCardPagingCtlr scope: " + $scope.$id + ' parent: ' +  $scope.$parent.$id);

        if ($scope === undefined) {
        console.log('inside BaseListMgmtCtlr controller NO ARG');
            return;
        }
        console.log('inside BaseListMgmtCtlr controller for: ' + $stateParams.entity + '/' + $stateParams.q);
           
        console.log("ListAndCardPagingCtlr state params " + "." + $stateParams.entity);
        console.log("ListAndCardPagingCtlr scope params " + $scope.entity + "->" + $scope.relatedentity);
        console.log("ListAndCardPagingCtlr scope order " + $scope.orderby);



        // controller init:
        var entity = $stateParams.entity;  
        $scope.entity = entity;
        var entity2resource = appContext.get('entity2resource');             
        $scope.entityTypeLabel = entity2resource.getLabel(entity);
        $scope.resource = entity2resource.getResource(entity);

        var ctlrConfig = $stateParams.ctlrConfig;
        if (ctlrConfig) {
            // copy controllr config parmas into scope
            _.extend($scope, ctlrConfig); 
        }

        // search/query params
        $scope.elements = [];
        $scope.q = $stateParams.q;
        
        // filter params
        $scope.filteredElements = [];
        $scope.filteredCount = 0;
        
        // sort params
        if (ctlrConfig.columns) {
            $scope.orderby = ctlrConfig.columns[0].name;
        }
        $scope.reverse = false;

        //paging params
        $scope.totalRecords = 0;
        $scope.pageSize = 60;
        $scope.currentPage = 1;

        // view type params. show list view by default
        $scope.ViewEnum = {Card: 0, List: 1};
        $scope.listViewEnabled = true;

        // toolbar  filter input
        $scope.filterText = '';
        
        $scope.selectedElement = undefined;

        $scope.this = this;
        // ===========================================================================
        // Scope methods


        // permission check proxy method. permission check is proxied to  authService
        // note: this method is required in all controllers that have controls depending on permissions
        $scope.hasPermission = function( perm) {
            return authService.hasPermission( perm);
        };

        $scope.hasPermissionIn = function(perms) {
            return authService.hasPermissionIn(perms);
        };

        // switch list/card view 
        $scope.changeView = function (view) {
            switch (view) {
                case $scope.ViewEnum.Card:
                    $scope.listViewEnabled = false;
                    break;
                case $scope.ViewEnum.List:
                    $scope.listViewEnabled = true;
                    break;
            }
        };

        // change page and refesh view
        $scope.pageChanged = function () {
            $scope.this.getElements();
        };

        // set sorting order (asc / desc) and refesh view
        $scope.setOrder = function (orderby) {
            if (orderby.name === $scope.orderby) {
                $scope.reverse = !$scope.reverse;
            }
            $scope.orderby = orderby.name;
            $scope.this.getElements();
            var x  = 1;
        };

        // filter rows based on filter input
        $scope.filterElements = function (filterText) {
            if (filterText !== undefined && filterText.length > 0) {
                $scope.filteredElements = $filter('filter')($scope.elements, filterText);
            }
            else {
                $scope.filteredElements = $scope.elements;
            }
            $scope.filteredCount = $scope.filteredElements.length;
            console.log('--> filter(' + filterText + ')' + ' => ' + $scope.filteredCount + '/' +
                $scope.elements.length);
        };

        // displays Element details according to selected row
        $scope.showElementDetails = function( element){
            // FOR NOW, JUST USE THIS METHOD TO SELECT A ROW

            $scope.selectedElement = element;
            // get entity from element class name.
            var actualentity = element.clName.toLowerCase();
            console.log('--> showElementDetails(' + actualentity + ':' + element + ')' + ' => ' + element.name + ',' +
                element.id);
            var to = 'authenticated.entity';
            $state.go(to, {entity: actualentity, id: element.id});
            var x =1;
        };

        $scope.renderDateField = function(value, filter) {
            if (filter === null)
                return value;
            else
                return $filter('date')(value, filter);
        };


        // ===========================================================================
        // overridable Controller methods

        this.getElements = function() {

            console.log("ListAndCardPagingCtlr.getElements()");
            console.log("ListAndCardPagingCtlr.getElements()");
            console.log("ListAndCardPagingCtlr state params " + "." + $stateParams.entity);
            console.log("ListAndCardPagingCtlr scope params " + $scope.entity + "->" + $scope.relatedentity);
            console.log("ListAndCardPagingCtlr scope order " + $scope.orderby);


            var queryParams = {filter: $scope.q};
            if ($scope.orderby) {
                queryParams.orderby = $scope.orderby;
                queryParams.orderdir = (!$scope.reverse ? 'ASC' : 'DESC');
            }

            dataService
            .getElements($scope.resource, queryParams, $scope.currentPage, $scope.pageSize)
            .then(function (result) {
                $scope.elements = result.data;
                $scope.totalRecords = result.total;
                    console.log(result);

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
        

        // ===========================================================================
        // private Controller methods

        createWatches();  


        // watch filter
        function createWatches() {
            //Watch filterText value and pass it and t
            //Doing this instead of adding the filter to ng-repeat allows it to only be run once (rather than twice)
            //while also accessing the filtered count via $scope.filteredCount above
            $scope.$watch("filterText", function(filterText) {
                $scope.filterElements(filterText);
            });
        }

        return this;
};



/*
 * Default / Base helper controller for Displaying the attributes of an entity or creating a new entity
 */
var EditOrCreateEntityCtlr = function($scope, $stateParams, $state, stateManager, $filter, authService, appContext,
                                      dataService, dialogs, alertsManager) {

    console.log("EditOrCreateEntityCtlr scope: " + $scope.$id + ' parent: ' +  $scope.$parent.$id);

//    $("#alert").hide();
    $scope.alerts = [];

    console.log('inside EditOrCreateEntityDetailsCtlr controller for: ' + $stateParams.entity + '/' + $stateParams.id);
    $scope.name = $stateParams.entity + ":" + $stateParams.id;

    var entity = $stateParams.entity; 
    $scope.entity = entity;
    var entity2resource = appContext.get('entity2resource');             
    $scope.entityTypeLabel = entity2resource[entity].label;
    var resource = entity2resource[entity].restResource; 

    var ctlrConfig = $stateParams.ctlrConfig;
    if (ctlrConfig) {
        // copy controllr config parmas into scope
        _.extend($scope, ctlrConfig); 
    }

    var id = $stateParams.id;


    // ===========================================================================
    // Scope methods

    // permission check proxy method. permission check is proxied to  authService
    // note: this method is required in all controllers that have controls depending on permissions
    $scope.hasPermission = function( perm) {
        return authService.hasPermission( perm);
    };

    $scope.hasPermissionIn = function(perms) {
        return authService.hasPermissionIn(perms);
    };

    $scope.getElement = function() {
        getElement();
    };  

    /*
    * placeholder for controllers that need to perform some operations after (re-)loading the data 
    */
//    if ($scope.postGetElement  === undefined) {
        // add it if not already existent to avoid race conditions.
//        $scope.postGetElement = function() {
            // do nothing
//        };    
//    }

    $scope.saveOrCreate = function() {
        if ($scope.mode == 'Edit') {
            // update entity
            updateElement();
        }
        else {
            // create entity, then switch to Edit mode
            if (! $scope.vetoCreate()) {
                createElement();
            }
        }

    };

    /* 
     * vetoCreate() can be ovveriden to for instance check data is valid or prompt for additional data
     * (ex on team creation, a Site must be selected, see teamcontrollers.js) 
     */
    $scope.vetoCreate = function() {
        return false;
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    /*
    * getCreateDto() method to be overriden in some controllers that need
    * to process the data to be sent to the backend on object create  
    */
    $scope.getCreateDto = function() {
        // by default, just return the object itself;
        return $scope.elt;
    };

    /*
    * getUpdateDto() method to be overriden in some controllers that need
    * to process the data to be sent to the backend on object update  
    */
    $scope.getUpdateDto = function() {
        // by default, just return the object itself;
        return $scope.elt;
    };

    //----datepicker methods
    $scope.clear = function () {
        $scope.dt = null;
    };
    $scope.open = function($event,opened) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope[opened] = true;
    };

    $scope.today = function() {
        $scope.dt = new Date();
    };
    $scope.today();
    //---------

    $scope.partials = {
        addInfo: 'modules/browser/partials/entity_info.tpl.html',
        tabs: 'modules/browser/partials/entity_tabs.tpl.html'
    };

    $scope.cancel = function () {
        if($state.current.name == "authenticated.createchildentity"){
            $state.go(stateManager.previousState, stateManager.previousParams);
        }
    };


    // ===========================================================================
    // Controller private methods

    // getElement by id 
    function getElement(){

        if ($scope.secundaryEntity) {
            // api call has form api($parentId, dto)
            var params = [$stateParams.parentEntityId, $scope.elt.id];     
            dataService[ctlrConfig.dataServiceGetAssociatedEntityAPI]
                .apply(this, params)
                .then(function (result) {
                    var elt = result;
                    var label = (elt.name ? elt.name : elt.id) /*+ '(' + elt.id + ')')*/;
                    console.log('--> loaded[' + elt.clName + ': ' + label + ']'); 
                    $scope.elt = elt;  // update view with backend result
                    if ($scope.postGetElement !== undefined) {
                        $scope.postGetElement();    
                    }
                },function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                }); 
        }
        else {
            dataService
            .getElement(resource, $scope.elt.id, {})
            .then(function (result) {
                var elt = result;
                var label = (elt.name ? elt.name : elt.id);
                console.log('--> loaded[' + elt.clName + ': ' + label + ']'); 
                $scope.elt = elt;
                if ($scope.postGetElement !== undefined) {
                    $scope.postGetElement();    
                }
            }, function (error) {
                dialogs.error("Error", error.data.message, 'lg');
            }); 
        }              
    }
    
    // update Element  
    function updateElement(){

        var elt = $scope.getUpdateDto();
        if ($scope.secundaryEntity) {
            // api call has form api($parentId, dto)
            var params = [$stateParams.parentEntityId, $scope.elt.id, elt];     
            dataService[ctlrConfig.dataServiceUpdateAssociatedEntityAPI]
                .apply(this, params)
                .then(function (result) {
                    var elt = result;
                    var label = (elt.name ? elt.name : elt.id);
                    console.log('--> updated[' + label + ']'); 
                    $scope.elt = elt;  // update view with backend result
                    alertsManager.addAlert("'" + label + "' successfully updated.", 'success');
                },function (error) {
                	dialogs.error("Error", "Error while updating: '" + elt.name + "': " + error.data.message, 'lg');
                });
        }
        else {
            dataService
            .updateElement(resource, $scope.elt.id, elt)
            .then(function (result) {
                var elt = result;
                var label = (elt.name ? elt.name : elt.id);
                $scope.elt = elt;  // update view with backend result

                alertsManager.addAlert("'" + label + "' successfully updated.", 'success');

                },function (error) {
            	dialogs.error("Error", "Error while updating: '" + elt.name + "': " + error.data.message, 'lg');
            });
        }
    }

    // create Element  
    function createElement(){

        var elt = $scope.getCreateDto();
        if ($scope.secundaryEntity) {
            // api call has form: api($parentId, dto)
            var params = [$stateParams.parentEntityId, elt];     
            dataService[ctlrConfig.dataServiceCreateAssociatedEntityAPI]
                .apply(this, params)
                .then(function (result) {
                    var elt = result;
                    var label = (elt.name ? elt.name : elt.id);
                    console.log('--> created[' + label + ']'); 
                    $scope.elt = elt;  // update view with backend result
                    $scope.mode = 'Edit';
                    $scope.modeBtn = 'Save';
                    // update state
                    //$stateParams.id = elt.id; // doesn't work

                    alertsManager.addAlert("'" + label + "' successfully created.", 'success');

                    if($state.current.name == "authenticated.createchildentity"){
                        $state.go(stateManager.previousState, stateManager.previousParams);
                    }

                },function (error) {
                	dialogs.error("Error", "Error while creating: '" + $scope.elt.name + "': " + error.data.message, 'lg');
                });
        }
        else {
            // api call has standard form: createElement(dto)
            dataService
            .createElement(resource, elt)
            .then(function (result) {
                var elt = result;
                var label = (elt.name ? elt.name : elt.id);
                console.log('--> created[' + label+ ': ' + elt.id + ']'); 
                $scope.elt = elt;  // update view with backend result
                $scope.mode = 'Edit';
                $scope.modeBtn = 'Save';
//                $stateParams.id = elt.id;
                var st = $state.current;            // reload current state with created object
                alertsManager.addAlert("'" + label + "' successfully created.", 'success');

                if($state.current.name == "authenticated.createchildentity"){
                    $state.go(stateManager.previousState, stateManager.previousParams);
                } else {
                    $state.go(st.name, {id: elt.id});
                }

            }, function (error) {
            	dialogs.error("Error", "Error while creating: '" + $scope.elt.name + "': " + error.data.message, 'lg');
            });    
        }         
    }

    $scope.currentState = $state.current.name;
    $scope.elt = {};
    $scope.mode = 'Edit';
    $scope.modeBtn = 'Save';
    if (id === undefined || id == 'new'  || id === '') {
        id = '';    // clear Id (will be generated by backend)
        $scope.elt.name = 'new ' + $scope.entityTypeLabel;
        $scope.elt.description = 'new ' + $scope.entityTypeLabel;
        $scope.mode = 'Create';
        $scope.modeBtn = 'Create';
        $scope.elt.id = id;

    } 
    else {
        $scope.elt.id = id;
        getElement();

        ctlrConfig = $stateParams.ctlrConfig;
        $state.go('authenticated.entity.relatedentity', {relatedentity: ctlrConfig.defaultRelatedEntity});
    }

};    

var getScopeHierachy = function(scope) {
    var scopelist = '';
    while (scope !== null) {
        scopelist += (scope.$id + ',');
        scope = scope.$parent;
    }
    return scopelist;
};


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




    	
