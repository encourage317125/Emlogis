var i = 0;

self.addEventListener('message', function(e) {
	var data = e.data;
	var heartbiturl = data;
  	var cnt = 10;
  	console.log('heart bit worker firing, data=' + e.data);
  	console.log('start wait');

    var interval = 60 * 1000;
  	setInterval( function (){
	  		console.log('heart bit worker, loop# ' + cnt);
	  		cnt--;

	  		try{
		  		var xmlHttp = new XMLHttpRequest();
	    		xmlHttp.open( "GET", heartbiturl, false );
	    		xmlHttp.send( null );
	    	}
	    	catch( error){}
  		},
  		interval
  	);

}, false);

/*
self.startTimer = function() {
    timer.start();
    setTimeout(stopTimer,5000);
};

self.stopTimer = function () {
    timer.stop;
};

*/

/*
function timedCount() {
    i = i + 1;
    postMessage(i);
    setTimeout("timedCount()",500);
}

timedCount();
*/
