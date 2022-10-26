var employees = angular.module('emlogis.browser');

employees.controller('BrowserBreadcrumbCtrl', ['$scope', '$state', '$translate', 'applicationContext',
    function($scope, $state, $translate, applicationContext ) {

        $scope.entity =  $state.params.entity;
        console.log("Browser Breadcrumb Ctrl (Entity) : " +  $state.params.entity);

        var module = applicationContext.getModule();

        // Call translate directive function. Put translated text;
        $translate('nav.BROWSER')
            .then(function (translation) {
                module.name =  translation;
            });


        /**
         * state attribute will not update href attribute actually.
         * because this is called behind after ui-sref attribute rendered
         */

        module.icoClass = '';
        module.href = '/browser/site';
        module.disableModuleBreadcrumb = false;
        applicationContext.setModule(module);
    }
]);
