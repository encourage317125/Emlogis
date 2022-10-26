var app = angular
    .module('emlogis', ['ui.bootstrap', 'ui.router', 'http-auth-interceptor', 'login', 'ncy-angular-breadcrumb',
        'emlogis.home', 'emlogis.about', 'emlogis.commonservices', 'emlogis.accountmgmt', 'emlogis.browser'])
    .config(function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider) {

        $stateProvider
            // Ignored
            .state('index', {
                url: "/emlogis.html",
                abstract: true
            });
    })
    .run(function (appContext) {

        // initialize entity2resource map
        // entity2resource is a map between an entity name, and its
		// corresponding REST resource and user friendly label
        // for instance, the entity2resource 'useraccount' attribute would have
		// value {restResource: "useraccounts", label: "User Account"}
        var entity2resource = appContext.get('entity2resource', {});
        _.defaults(entity2resource, {
            getResource: function (entity) {
                return this[entity].restResource;
            },
            getLabel: function (entity) {
                return this[entity].label;
            }
        });
    });

/**
 * This directive will find itself inside HTML as a class, and will remove that
 * class, so CSS will remove loading image and show app content. It is also
 * responsible for showing/hiding login form.
 */
app.directive('securedApplication', function () {
    return {
        restrict: 'C',
        link: function (scope, elem, attrs) {
            // once Angular is started, remove class:
        	        	
            elem.removeClass('waiting-for-angular');

            var login = elem.find('#login-holder');
            var main = elem.find('#content-holder');

            scope.$on('event:auth-loginRequired', function () {
                login.slideDown('slow', function () {
                    main.hide();
                });
            });
            scope.$on('event:auth-loginConfirmed', function () {
                main.show();
                login.slideUp();
            });

            main.hide();
            
        }
    }
});

// directive for validation emails in forms
var emailPattern = /^$|^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
app.directive('cemail', function () {
    return {
        require: 'ngModel',
        link: function (scope, elm, attrs, ctrl) {
            ctrl.$parsers.unshift(function (viewValue) {
                if (emailPattern.test(viewValue)) {
                    // it is valid
                    ctrl.$setValidity('cemail', true);
                } else {
                    // it is invalid
                    ctrl.$setValidity('cemail', false);
                }
                return viewValue;
            });
        }
    };
});

app.controller('HeaderCtrl', ['$scope', '$location', '$stateParams', '$state', '$http', 'authService', 'sseService',
    function ($scope, $location, $stateParams, $state, $http, authService, sseService) {
        $scope.location = $location;
        $scope.searchEntity = "Employee";
        $scope.searchEntityLabel = "Search";
        $scope.searchQuery = "";

        $scope.taskLabel = 'Tasks';
        $scope.accountMgmtTaskLabel = 'Account Mgmt';

        // register a consumer for SSE that will display the event in header
        // (registration can happen safely even before getting events is
		// started.)
        $scope.eventCnt = 0;
        $scope.eventData = 'no event';
        sseService.registerConsumer({
            selector: function () {
                return true;					// for now, subscribe to all
												// events
            },
            callback: function (key, serverEvent) {
                $scope.$apply(function () {     // use $scope.$apply to refresh
												// the view
                    $scope.eventData = serverEvent;
                    $scope.eventCnt++;
                });
            },
            scope: $scope,
            params: []
        });

        $scope.setSearchEntity = function (entity, label) {
            $scope.searchEntity = entity;
            $scope.searchEntityLabel = label;
        };

        $scope.fireSearch = function () {
            var x = 1;
            var currentstate = $state.current;
            var to = 'search.query';
            $state.go(to, {entity: $scope.searchEntity, q: $scope.searchQuery});
        };

        $scope.hasPermission = function (perm) {
            return authService.hasPermission(perm);
        };

        $scope.hasPermissionIn = function (perms) {
            return authService.hasPermissionIn(perms);
        };

        $scope.getUserName = function () {
            var impersonatingUser = authService.getImpersonatingUserName();
            if (impersonatingUser != null) {
                impersonatingUser = '(' + impersonatingUser + ')';
            }
            else {
                impersonatingUser = '';
            }
            return    (authService.getUserName() != null ? 'Hi ' + impersonatingUser + authService.getUserName() + ' !' : 'not signed');
        };

        $scope.logout = function () {
            var token = authService.getSessionInfo().token;
            if (token != null) {
                $http.delete('../emlogis/rest/sessions')
                    .success(function (data) {
                        console.log('--> logout successfull() token:' + data.token);
                        authService.logout();
                    })
                    .error(function () {
                        console.log('--> logout FAILED()');
                        authService.logout();
                    });
            }
        };
    }
]);


app.controller('AppCtrl', ['$scope', '$location', '$state', 'authService', 'sseService', 'stateManager',
    function ($scope, $location, $state, authService, sseService, stateManager) {

        $scope.heartbitWorker = null;

        $scope.$broadcast('event:auth-loginRequired', null);

        // register a listener on login success to activate SSE
        $scope.$on('event:auth-loginConfirmed', function () {

            // we just logged in, subscribe to SSE events and send a heartbit
			// periodically
            // get session token and build base url for SSE subscribtion &
			// heartbit
            var tokenId = authService.getToken();
            var baseurl = "../emlogis/";

            // subscribe to SSE events
            sseService.startSSElistener(baseurl, tokenId)
            // start a webworker to send a heartbit to server every min
            //$scope.startHeartBit(baseurl, tokenId);
        });

        $scope.startHeartBit = function (baseurl, tokenId) {

            if (typeof(Worker) !== "undefined") {
                // Web Workers supported
                if ($scope.heartbitWorker != null) {
                    // Heartbit work lready active, so let's stop it first
                    $scope.heartbitWorker.terminate();
                    $scope.heartbitWorker = null;
                }
                $scope.heartbitWorker = new Worker("./heartbit.js");
                var url = baseurl + 'sseheartbit?tokenId=' + tokenId;
                $scope.heartbitWorker.postMessage(url);
            }
        };

        stateManager.onStateChangeSuccess();
    }
]);

app.controller('AlertsCtrl', ['$scope', 'alertsManager', '$timeout', function ($scope, alertsManager, $timeout) {
    $scope.alerts = alertsManager.alerts;
    $scope.closeAlert = function (index) {
        alertsManager.closeAlert(index);
    }
}]);


// TODO TEMPORARY, will go into its own module

app.controller('AdministrationCtrl', function ($scope, $compile, $stateParams) {
    console.log('inside Administration controller');
});


// TODO TEMPORARY, must go in a separate module

/*
 * pubsub Service - based on
 * https://github.com/phiggins42/bloody-jquery-plugins/blob/master/pubsub.js
 */
// TODO avoid accumulating subscribtion of same function and overwrite them
// instead.
// may be less clean but is less strict regarding the sequence of
// subscribe/unsubscribes

// accept callback as
// function()
// [function, args]
app.factory('pubsub', function () {
    var cache = {};
    return {
        publish: function (topic, args) {
            var z = cache[topic];
            cache[topic] && angular.forEach(cache[topic], function (callback, idx) {
                if (typeof callback == 'function') {
                    callback.apply(null, args || []);
                }
                else {
                    // assume callback is an array with first arg = function
                    var f = callback[0];
                    var params = callback.slice(1, callback.length).concat(args || []);
                    f.apply(null, params);
                    var x = 1;
                }
            });
        },

        subscribe: function (topic, callback) {
            if (!cache[topic]) {
                cache[topic] = [];
            }
            cache[topic].push(callback);
            return [topic, callback];
        },

        unsubscribe: function (handle) {
            var t = handle[0];
            cache[t] && d.each(cache[t], function (idx) {
                if (this == handle[1]) {
                    cache[t].splice(idx, 1);
                }
            });
        },

        clearCache: function () {
            cache = {};
        }
    }
});

var x = 1;


angular.module("template/timepicker/timepicker.html", []).run(["$templateCache", function ($templateCache) {
    $templateCache.put("template/timepicker/timepicker.html",
            "<table>\n" +
            "	<tbody>\n" +
            "		<tr class=\"text-center\">\n" +
            "			<td><a ng-click=\"incrementHours()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-up\"></span></a></td>\n" +
            "			<td>&nbsp;</td>\n" +
            "			<td><a ng-click=\"incrementMinutes()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-up\"></span></a></td>\n" +
            "			<td ng-show=\"showMeridian\"></td>\n" +
            "		</tr>\n" +
            "		<tr>\n" +
            "			<td style=\"width:50px;\" class=\"form-group\">\n" +
            "				<input type=\"text\" ng-model=\"hours\" ng-change=\"updateHours()\" class=\"form-control text-center\" ng-mousewheel=\"incrementHours()\" ng-readonly=\"readonlyInput\" >\n" +
            "			</td>\n" +
            "			<td>:</td>\n" +
            "			<td style=\"width:50px;\" class=\"form-group\">\n" +
            "				<input type=\"text\" ng-model=\"minutes\" ng-change=\"updateMinutes()\" class=\"form-control text-center\" ng-readonly=\"readonlyInput\" >\n" +
            "			</td>\n" +
            "			<td ng-show=\"showMeridian\"><button type=\"button\" class=\"btn btn-default text-center\" ng-click=\"toggleMeridian()\">{{meridian}}</button></td>\n" +
            "		</tr>\n" +
            "		<tr class=\"text-center\">\n" +
            "			<td><a ng-click=\"decrementHours()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-down\"></span></a></td>\n" +
            "			<td>&nbsp;</td>\n" +
            "			<td><a ng-click=\"decrementMinutes()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-down\"></span></a></td>\n" +
            "			<td ng-show=\"showMeridian\"></td>\n" +
            "		</tr>\n" +
            "	</tbody>\n" +
            "</table>\n" +
            "");
}]);




