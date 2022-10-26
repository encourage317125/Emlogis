
(function () {
	angular
	.module('emlogis.accountmgmt', ['ui.bootstrap','ui.router', 'http-auth-interceptor', 'emlogis.commonservices'])
	.config(
			
		function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider) {	

		}
	)
	.run(function(appContext) { 

		var entity2resource = appContext.get('entity2resource', {});
		_.defaults(entity2resource, {
	    	useraccount: {restResource: "useraccounts", label: "User Account"},
	        groupaccount: {restResource: "groupaccounts", label: "User Group"},
	        role: {restResource: "roles", label: "Role"}
	    });
  	});
}());






