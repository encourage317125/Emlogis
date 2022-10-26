angular.module('emlogis.home', ['ui.bootstrap', 'ui.router', 'http-auth-interceptor'])
  .config( function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider) {
    $stateProvider.state('authenticated.home', {
      url: '/home',
      views: {
        "content@authenticated": {
          templateUrl: "modules/home/partials/home.html",
          controller: "HomeCtrl as home"
        },
        "breadcrumb@authenticated": {
          template: '',
          controller: 'HomeBreadcrumbCtrl'
        }
      }
    });
  })

  .controller('HomeBreadcrumbCtrl', ['$scope', '$filter', 'applicationContext',
    function($scope, $filter, applicationContext) {

      // Update Module Information
      var module = applicationContext.getModule();

      module.name = $filter('translate')('nav.HOME');
      module.icoClass = '';
      module.href = '/home';
      module.disableModuleBreadcrumb = true;
      applicationContext.setModule(module);

    }
  ])
;


