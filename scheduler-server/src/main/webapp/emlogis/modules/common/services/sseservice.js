

(function () {
    //console.log('in sseService.');

    angular.module('emlogis.commonservices')
    .factory('sseService', ['$http', '$rootScope', '$sessionStorage', 'applicationContext',
            function($http, $rootScope, $sessionStorage, applicationContext) {
        var factory = {};
        var idx = 0;
        var consumers = {};
        var totalConnectionRefused = 0;
        /**
         * if we have more than 30 times of connection refused, then go to login page
         * @type {number}
         */
        var limitConnectionRefused = 30;
        var callingLogout = false;
        var source = null;

        //console.log('creating sseService...');


        factory.startSSElistener = function (baseurl, tokenId) {

            var url = baseurl + 'sse?tokenId=' + tokenId;
            //console.log('sseService  startSSElistener: ' + url);
            callingLogout = false;

            if(typeof(EventSource) !== "undefined") {

                // Store eventsource in rootScope as well
                $rootScope.sseService = source = new EventSource(url + '&action=subscribe');


                source.addEventListener('open', function (e) {
                    //console.log("sseevent open");
                }, false);

                source.addEventListener('error', function (e) {
                    //console.log("SSE event error occured ,event source state :" + this.readyState);

                    // Connectin Refused Response
                    if (this.readyState === 0){
                        totalConnectionRefused++;
                        // If it happens for a specific period of time, move to first page
                        if (totalConnectionRefused > limitConnectionRefused && callingLogout === false){
                            //console.log("Keep having connection refused response, log out");
                            //alert("Connection is lost.");

                            var notificationMsg = applicationContext.getNotificationMsg();
                            notificationMsg.param.lostConnection = true;
                            notificationMsg.type = 'danger';
                            notificationMsg.visible = true;
                            notificationMsg.content = 'Connection is lost.';
                            applicationContext.setNotificationMsg(notificationMsg);

                            //var param = {lostConnection : true};
                            callingLogout = true;
                            source.close();
                            //$rootScope.logout(param);

                        }
                    }
                }, false);

                source.onmessage = function(event) {
                    //console.log('got Event ! :' + event.data);

                    // clear connection refused = 0
                    totalConnectionRefused = 0;

                    // go through list of selectors and invoke consumers
                    for (var consumerId in consumers) { 
                        var consumer = consumers[consumerId];
                        try{
                            var serverEvent = JSON.parse(event.data);
                            if (consumer.selector(serverEvent.key)) {
                                // process event
                                var params = consumer.params || [];
                                params.unshift(serverEvent.key, serverEvent.data);
                                if (consumer.scope) {
                                      consumer.callback.apply(consumer.scope, params);
                                }
                                else {
                                    consumer.callback.apply(params); 
                                }
                            }
                        }
                        catch(error){
                            console.error('error while processing SSE consumer id:' + consumerId + " error: " + error);
                        }
                    }
                //  $scope.$apply(function () {     // use $scope.$apply to refresh the view
                //      $scope.event = event.data;
                //      $scope.eventCnt++;
                //    });
                };
            }
            else {
                console.error('SSE is not supported on this browser. unable to display real time information.');
            }

        };

        //====================================================================
        // public methods

        /*
        * public registerConsumer() register an SSE consumer
        * a consumer is defined by an object with following attributes:
        * - selector a function that must return true or false. it is invoked with the event 'key' as parameter
        * - callback a function invoked if selector() returns true. invoked with event.key and event.data, as parameters
        * - an optional id. if unspecified, an id is generated autmatically and returned in object + as return value.
        * followed by optional parameters sepcified in consumer.params attributes
        * - scope (optional) scope for the callback ftn
        * - params (optional) and array of params passed to the callback, right after the event data 
        * returns a registration id if success / null otherwise
        */ 
        factory.registerConsumer = function (consumer) {
            if (consumer && consumer.selector !== undefined && consumer.callback !== undefined) {
                idx++;
                // as id, either use consumer specified id, or generate one
                var consumerId = consumer.id || 'c' + idx;
                consumers[consumerId] = consumer;
                consumer.id = consumerId;
                return consumerId;
            }
            else {
                return null;
            }
        };

        factory.unregisterConsumer = function (consumerId) {
            consumers.delete(consumerId);
        };


        //====================================================================
        // private methods

        //console.log('sseService created.');
        return factory;

    }]);

    var y =1;

}());

