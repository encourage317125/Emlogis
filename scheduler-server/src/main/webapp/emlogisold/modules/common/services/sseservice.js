

(function () {
    console.log('in sseService.');

    angular.module('emlogis.commonservices')
    .factory('sseService', ['$http', function($http) {
        var factory = {};
        var idx = 0;
        var consumers = {};
        console.log('creating sseService...');


        factory.startSSElistener = function (baseurl, tokenId) {

            var url = baseurl + 'sse?tokenId=' + tokenId;
            console.log('sseService  startSSElistener: ' + url);

            if(typeof(EventSource) !== "undefined") {
                var source = new EventSource(url + '&action=subscribe');
                source.onmessage = function(event) {
                    console.log('got Event ! :' + event.data);
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

        console.log('sseService created.');
        return factory;

    }]);

    var y =1;

}());

