"use strict";
var cells = [];
function cell(id){
	this.id = id;
	this.register = function(){
		cells[id] = this;
	}
}

load('src/main/scripts/i2b2-ws/pm.cell.js');

this.httpRequest = function(method,path,query,body){
	var i = -1;
	if( path != null )i = path.indexOf('/',1);
	
	if( i != -1 ){
		var c = cells[path.substr(1,i-1)];
		if( c )return c.request(method,path.substr(i+1),query,body);
		else return "<error>Unknown cell in path "+path.substr(1,i-1)+"</error>";
	}else return "<error>No cell id specified in path</error>";
}
