

var duration = function(start, end) {
    var d;
    if (start.getTime() <= 0 || end.getTime() <=0) {
        return "";
    }
    var d = end.getTime() - start.getTime();
    if (d < 1000) {
        return "" + d + " ms";
    }
    else {
        return "" + d / 1000 + " sec";
    }
};