angular.module('emlogis').controller('EntitiesTopBreadcrumbCtrl', ['$scope', '$rootScope', '$http', 'crudDataService', 'applicationContext',
    function ($scope, $rootScope, $http, crudDataService, applicationContext) {

        var baseUrl = applicationContext.getBaseRestUrl();
        $scope.search = {txt : ''};

        // Auto Complete KeyField List : mostly it will be name fields
        $scope.getSearchValues = function(val) {

            // Get Filter Info from Global Properties
            var entityFilter = applicationContext.getEntityFilter();

            if ((entityFilter.url === '') || (entityFilter.searchFields === '')) {
                console.log('Filter url or searchFields is(are) empty');
                return null;
            }

            var params = {
                search: val,
                searchfields: entityFilter.searchFields,
                returnedfields: entityFilter.returnedFields,
                orderby: entityFilter.orderBy,
                orderdir: entityFilter.orderDir,
                limit: entityFilter.limit
            };

            return $http.get(baseUrl + entityFilter.url, {params: params})
                .then(function(entities) {
                    return entities.data.map(function(item) {
                        if (!Array.isArray(item)) {
                            item = [item];
                        }
                        var searchItem = {};
                        searchItem.id = item[0];
                        item.splice(0, 1);
                        searchItem.label = item.join(' ');
                        return searchItem;
                    });
                });
        };

        // Search icon click action
        $scope.listKeywords = function() {

            // Save Filter Info as Globalproperties
            var entityFilter = applicationContext.getEntityFilter();
            entityFilter.txt = $scope.search.txt;
            applicationContext.setEntityFilter(entityFilter);

            $rootScope.$broadcast("event:entities-SearchTxtUpdated", null);
        };

        $scope.selectSearchValue = function(item, model, label) {
            $scope.selectEntitySearchValue(item, model, label);
        };
    }
]);

