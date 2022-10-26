(function() {
  	'use strict';
  	angular.module('login',['http-auth-interceptor'])
  
  	.controller('LoginController', function ($scope, $http, authService) {

  		// default values for testing .. MUST be removed in production !
  		$scope.tenantId = 'jcso';
  		$scope.login = 'admin';
  		$scope.password = 'admin';

	    $scope.submit = function() {
	      	$http.post('../emlogis/rest/sessions', {tenantId:$scope.tenantId, login:$scope.login, password:$scope.password})
	      	.success(function(data) {

	      		// set HTTP header field for subsequent calls

	      		//var headers = $http.defaults.headers.common;

	      		//$http.defaults.headers.common.EmLogisToken = data.token;
	      		console.log('--> login successfull()');
	      		// reset login info
	      		$scope.tenantId = '';
  				$scope.login = '';
  				$scope.password = '';
	        	authService.loginConfirmed( data);
	      	})
	      	.error(function() {
	      		// TODO show login error
	      		console.log('--> login FAILED()');

	      		// TODO should we clear data ?
		  		$scope.password = '';
	      		authService.loginCancelled();
	     	});
	    };
  	});
})();
