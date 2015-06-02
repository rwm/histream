"use strict";
var pm = new cell('pm');
pm.request = function(method,path,query,body){
	return "<x>PM, method="+method+", path="+path+", query="+query+"</x>";
}

pm.register();

print('PM cell loaded');
