var profile = angular.module('emlogis.profile');

profile.controller('ProfileBreadcrumbCtrl', function($scope, $translate, $sessionStorage, applicationContext ) {

  console.log('inside profile breadcrumb controller');

  // Update Module Information

  var module = applicationContext.getModule();

  // Call translate directive function. Put translated text;
  $translate('nav.EMPLOYEE_PROFILE')
    .then(function (translation) {
      module.name =  translation;
    });


  module.href = '/profile';
  module.icoClass = '';
  module.disableModuleBreadcrumb = false;
  applicationContext.setModule(module);

});
