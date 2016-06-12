Algorithm
---------

1. Read all concept references. This can be actual concept codes 
   or wildcard concept codes.

2. The facts will be processed grouped by encounter. E.g. as 
   a list of all concepts for the given encounter.
   
   If the patient or visit is different from the previous one
   create new row in visit or encounter. Process all columns
   which are NOT bound to any fact (not for fact tables which
   are always bound to a fact).

3. For each fact:
3.1	Find all matching columns.
3.2	

or

3. For each column:
3.1	Find all matching facts