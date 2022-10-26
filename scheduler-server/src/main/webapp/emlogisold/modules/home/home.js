var home = angular.module('emlogis.home', ['ui.bootstrap','ui.router', 'http-auth-interceptor']).config(
		
	function ( $stateProvider, $urlRouterProvider, $locationProvider, $httpProvider) {	
		
		// /manage = management module entry point/url	
		$stateProvider.state( 'home', {
			url: '/home',
			templateUrl: 'modules/home/partials/home.html',
	        data : {
	            title : 'Home'
	        }
		});
	}
);


home.controller('HomeCtrl', function($scope, $compile) {
	console.log('inside home controller');
});


var y = 1;





