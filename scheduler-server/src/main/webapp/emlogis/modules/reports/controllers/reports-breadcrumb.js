var employees = angular.module('emlogis.reports');

employees.controller('ReportsBreadcrumbCtrl', ['$scope', '$translate', 'applicationContext',
    function($scope, $translate, applicationContext) {

        // Update Module Information
        var module = applicationContext.getModule();

        // Call translate directive function. Put translated text;
        $translate('nav.REPORTS')
            .then(function (translation) {
                module.name =  translation;
            });


        module.href = '/reports';
        module.icoClass = '';
        module.disableModuleBreadcrumb = false;
        applicationContext.setModule(module);

    }
]);
