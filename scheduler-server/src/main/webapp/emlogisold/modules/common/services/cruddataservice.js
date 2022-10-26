

//====================================================================
// Service in charge of doing base CRUD operations against REST resources
//
// TODO: make it generic and reusable so that we can have specialized dataServices that just extend that one
// TODO: add methods for getting one object, updating and creating objects

(function () {
    console.log('in crudDataService.');

    angular.module('emlogis.commonservices')
    .factory('crudDataService', ['$http', '$q', function($http, $q) {

        console.log('creating crudDataService...');

        var factory = {},
//            baseUrl = './rest/';
            baseUrl = '../emlogis/rest/';

        /* metamodel holds some information for each class of the model (defined in dataService)
         * each class defines the list of attributes that need a mapping via a 'ttype' value.
         * ttype values are:
         *  tdate (maps a long into a Date object)
         *  time (maps a int or long into a Date object, with only Time relevant attributes)
         *
         * metamodel also contains 2 methods to convert an entity attributes from their Dto type to a 'UI' type
         * and vice versa.
         */
        var metamodel = {
            // mapping methods. fromDto() convert a Dto element to a 'UI' object
            // toDto() convert a 'UI' object to a Dto element
            fromDto: function(elt){
                return this.map(elt,'fromDto');
            },

            toDto: function(elt){
                return this.map(elt,'toDto');
            },

            // attribute type converter methods
            // eacch attribute type must come with 2 converters: one to convert from the dto attribute type to the UI type
            // and one for the inverse transformation (UI type to dto) 
            fromDtoMappers:{
                tdate: function(dateAsLong) {
                    return new Date(dateAsLong);
                },
                ttime: function(timeAsSecs) {
                    var date = new Date(timeAsSecs);
                    var correctdate = new Date(date.getTime() + date.getTimezoneOffset()*60000);
                    return correctdate;
                },
                tjson: function(jsonstring) {
                    if (jsonstring && jsonstring.length > 0) { 
                        return angular.fromJson(jsonstring);
                    }
                    return undefined;
                }
            },
            // converter methods: Javascript to Dto 
            toDtoMappers:{
                tdate: function(dateAsDate) {
                    return dateAsDate.getTime();
                },
                ttime: function(timeAsDate) {
                    return timeAsDate.getTime();
                }
            },        

            map: function(elt, direction){
                var clModel = this[elt.clName];
                if (!clModel) {
                    // return directly the element if no mapping configured
                    return elt;
                }
                var result = {};
                if (direction == 'fromDto' && clModel.fromDto) {
                    // we have custom fromDto mapping function  
                    result = clModel.fromDto(elt);
                }
                else if (direction == 'toDto' && clModel.toDto) {
                    // we have custom toDto mapping function  
                    result = clModel.toDto(elt);
                }
                // use attribute based mapping (can be in addition to custom mapping)
                   for (var k in elt) {
                    if (clModel[k]) {
                        var mapper;
                        if (direction == 'fromDto') {
                            mapper = this.fromDtoMappers[clModel[k]];
                        }
                        else {
                            mapper = this.toDtoMappers[clModel[k]];
                        }
                        var mappedvalue = mapper(elt[k]);
                        result[k] = mappedvalue;
                    }
                    else {
                        result[k] = elt[k];
                    }
                }
                if (direction != 'fromDto') {
                    delete result.clName;  // remove clName from Dto as backend would reject request
                } 
                return result;
            }
        };



        //====================================================================
        // public methods

        /*
        * get the list of elements with paging for a specified REST resource
        *
        * expected  queryParams format is { 
        *    filter: <filter expression>, 
        *    orderby: <attribute name>,
        *    orderdir: <'ASC' | 'DESC'>
        * }
        * paging is optional (leave it undefined or specify pageIndex < 0) to skip paging
        * if pecified, paging starts at 0
        * returns the object  {                
        *   data: <array of records>,
        *   total: <total nb of records for that resource> 
        * }
        */
        factory.getElements = function (resource, queryParams, pageIndex, pageSize) {

            var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
            var url = baseUrl + resource;
            console.log('dataService --> querying( ' + url + ' q=' + params.filter
                + " orderby: " + params.orderby + '(' + params.orderdir 
                + ') from: ' + params.offset + '[' + params.limit + ']'
            );

            return $http.get(url, {params: params}).then(function (response) {
                return factory.toResultSet(response);
            });
        };

        /*
        * get one element with optionally  parameters specified in a urlParams object;
        * returns the received object
        */
        factory.getElement = function (resource, resourceId, urlParams) {

            var params = urlParams || {};
            var url = baseUrl + resource + '/' + resourceId;
            console.log('dataService --> getting( ' + url + ' params=' + params);

            return $http.get(url, {params: params}).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * upadte one element with optionally parameters specified in a urlParams object;
        *
        * NOTE: urlParams NOT IMPLEMENTED YET
        * 
        * returns the updated object
        */
        factory.updateElement = function (resource, resourceId, resourceElt, urlParams) {

            var url = baseUrl + resource + '/' + resourceId;
            //console.log('--> updating( ' + url + ' params=' + urlParams);
            console.log('--> updating( ' + url + ') with:' + resourceElt);

            var dto = metamodel.toDto(resourceElt);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };


        /*
        * Create one element with optionally parameters specified in a urlParams object;
        *
        * NOTE: urlParams NOT IMPLEMENTED YET
        * 
        * returns the updated object
        */
        factory.createElement = function (resource, resourceElt, urlParams) {

            var url = baseUrl + resource + '/';
            //console.log('--> creating( ' + url + ' params=' + urlParams);
            console.log('dataService --> creating( ' + url + ') with:' + resourceElt);

            var dto = metamodel.toDto(resourceElt);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * delete one element with optionally parameters specified in a urlParams object;
        *
        * NOTE: urlParams NOT IMPLEMENTED YET
        * 
        * returns the updated object
        */
        factory.deleteElement = function (resource, resourceId, urlParams) {

            var params = urlParams || {};
            var url = baseUrl + resource + '/' + resourceId;
            //console.log('--> deleting( ' + url + ' params=' + urlParams);
            console.log('dataService --> deleting( ' + url + ')');

            return $http.delete(url, {params: params}).then(function (response) {
                return factory.toObjectResult(response);
            });
        };


        factory.toResultSet = function (response) {
            
            // we expect a result that is a result set ie {result:[records], total:count}
            // however we accomodate some APIs that directly return a list of records 
            var result;
            if (Array.isArray(response.data)) {
                result = {         
                    data: response.data,
                    total: response.data.length
                };
            }
            else {
                result = {
                    data: response.data.result,
                    total: response.data.total
                };
            }
            console.log('dataService --> ' + response.config.method + ':' + response.config.url + ' -> [' + result.data.length + '] records out of: ' + result.total);
            result.data.forEach(function(elt, idx, array){
                array[idx] = metamodel.fromDto(elt);
            });
            return result;
        };


        factory.toObjectResult = function (response) {
            
            var s = console.log('dataService --> ' + response.config.method + ':' + response.config.url + ' -> ');
            // we expect a result that is an object
            if (response.data) {
                console.log( s + '[' + response.data + ']');

                return metamodel.fromDto(response.data);
//                return response.data;
            }
            else {
                console.log( s + '[EMPTY RESULT]');
                return null;
            }            
        };
                

        factory.prepareQueryParams = function (queryParams, pageIndex, pageSize) {

            queryParams = queryParams || {};
            var params = _.pick(queryParams, 'filter', 'orderby', 'orderdir');
            if (pageIndex !== undefined && pageIndex >= 0) {
                // set offset & limit params if required (pageIndex >= 0)
                pageSize = (pageSize ? pageSize : 20);
                params.offset = (pageIndex-1) * pageSize;
                params.limit = pageSize;
            }
            return params;
        };

        //====================================================================
        // MetaModel configuration

        _.defaults(metamodel, {

            fromDto: function(elt){
                var clModel = this[elt.clName];
                if (clModel) {
                    var result = {};
                       for (var k in elt) {
                        if (clModel[k]) {
                            var mapper = this.fromDtoMappers[clModel[k]];
                            var mappedvalue = mapper(elt[k]);
                            result[k] = mappedvalue;
                        }
                        else {
                            result[k] = elt[k];
                        }
                    }
                    return result;
                }
                else {
                    return elt;
                }
            },

            toDto: function(elt){
                var clModel = this[elt.clName];
                if (clModel) {
                    var result = {};
                       for (var k in elt) {
                        if (clModel[k]) {
                            var mapper = this.toDtoMappers[clModel[k]];
                            var mappedvalue = mapper(elt[k]);
                            result[k] = mappedvalue;
                        }
                        else {
                            result[k] = elt[k];
                        }
                    }
                    return result;
                }
                else {
                    return elt;
                }
            }
        });

        factory.metamodel = metamodel;

        //====================================================================
        // private methods

        // addUrlParams() 


        // 
        console.log('crudDataService created.');
        return factory;

    }]);

}());

