var employees = angular.module('emlogis.browser');

employees.controller('BrowserEntityBreadcrumbCtrl', ['$scope', '$state', 'appContext',
    function($scope,$state, appContext ) {

        $scope.entity =  $state.params.entity;
        console.log("Browser Entity Breadcrumb Ctrl (Entity) : " +  $state.params.entity);

        $scope.relatedentity =  $state.params.relatedentity;
    }
]);
