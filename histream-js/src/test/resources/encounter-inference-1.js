// concept ids of the facts which can be 
// produced if the conditions are right
this.produces = ["ASDF"];

// concept ids for which facts are read
this.requires = ["X","y","Z"];

// inference function
this.infer = function(facts){
	facts.add("ASDF").value(0);
	// adding concepts not advertised 
	// in this.produces will result in warning. 
	facts.add("OTHER").value(0);
}
