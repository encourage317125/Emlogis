var scheduleBuilder = angular.module('emlogis.schedule_builder');

scheduleBuilder.controller('ScheduleBuilderBreadcrumbCtrl', ['$scope', '$translate', 'applicationContext',
    function($scope, $translate, applicationContext) {

        // Update Module Information

        var module = applicationContext.getModule();

        // Call translate directive function. Put translated text;
        $translate('nav.SCHEDULE_BUILDER')
            .then(function (translation) {
                module.name =  translation;
            });

        module.icoClass = 'glyphicon glyphicon-wrench';
        module.href = '/schedule_builder/create_schedules';
        module.disableModuleBreadcrumb = false;
        applicationContext.setModule(module);

    }
]);
