var employees = angular.module('emlogis.monitoring');

employees.controller('MonitoringBreadcrumbCtrl', ['$scope', '$translate',  'applicationContext',
    function($scope, $translate,  applicationContext) {


        // Update Module Information
        var module = applicationContext.getModule();

        // Call translate directive function. Put translated text;
        $translate('nav.MONITORING')
            .then(function (translation) {
                module.name =  translation;
            });


        module.href = '/monitoring';
        module.icoClass = '';
        module.disableModuleBreadcrumb = false;
        applicationContext.setModule(module);

    }
]);
