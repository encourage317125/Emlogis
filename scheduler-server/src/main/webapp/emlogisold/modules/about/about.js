var about = angular.module('emlogis.about', ['ui.bootstrap','ui.router', 'http-auth-interceptor']).config(
		
	function ( $stateProvider, $urlRouterProvider, $locationProvider, $httpProvider) {	
		
		// /manage = management module entry point/url	
		$stateProvider.state( 'about', {
			url: '/about',
			templateUrl: 'modules/about/partials/about.html',
	        data : {
	            title : 'about'
	        }
		});
	}
);


about.controller('aboutCtrl', function($scope, $compile) {
	console.log('inside about controller');
});





