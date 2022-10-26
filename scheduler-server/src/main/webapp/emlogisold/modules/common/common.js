(function () {

    console.log('in commonservices.');

    angular
    .module('emlogis.commonservices', [])

    .factory('appContext', function () {
        var ctx = {
            entity2resource: {}
        };
        return {
            get: function (id, defaultEntry) {
            	if (defaultEntry !== undefined) {
    	            return ctx[id] || (ctx[id] = defaultEntry);
    	        }
    	        return ctx[id];
            },
            set: function( id, entry) {
            	ctx[id] = entry;
            }
        };
    })

    .factory('deepCopy', function () {
        return {
        };
    })

    .factory('alertsManager', function ($timeout) {
            return {
                alerts: [],
                addAlert: function(message, type) {

                    this.alerts.push({type: type, msg: message});
                    var that = this;
                    $timeout(function(){
                        that.closeAlert(that.alerts.length -1);
                    }, 3000);
                },
                closeAlert: function(index) {
                    this.alerts.splice(index, 1);
                }
            }
        })

    .factory('stateManager', function ($rootScope) {
            return {
                previousState: {},
                previousParams: {},
                currentState: {},
                currentParams: {},
                onStateChangeSuccess: function() {
                    var that = this;
                    $rootScope.$on('$stateChangeSuccess', function(ev, to, toParams, from, fromParams) {
                        that.previousState = from.name;
                        that.previousParams = fromParams;
                        that.currentState = to.name;
                        that.currentParams = toParams;
                        console.log('===>Previous state:'+that.previousState);
                        console.log('===>Current state:'+that.currentState);
                    });
                }
            }
        })

}());