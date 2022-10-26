var rules = angular.module('emlogis.rules');

rules.controller('RulesBreadcrumbCtrl', ['$scope', '$translate', 'applicationContext',
    function($scope, $translate, applicationContext) {
        //console.log('inside Rules breadcrumbs controller');

        // Update Module Information
        var module = applicationContext.getModule();

        // Call translate directive function. Put translated text;
        $translate('nav.RULES')
            .then(function (translation) {
                module.name =  translation;
            });


        module.href = '/rules/general';
        module.icoClass = 'fa fa-cogs';
        module.disableModuleBreadcrumb = false;
        applicationContext.setModule(module);

    }
]);