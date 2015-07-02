
Uniqueness of notation
----------------------
According to SKOS, a single skos:Concept may contain 0, 1 or more notations.
Each notation (literal+datatype) uniquely identifies a Concept within a ConceptScheme.

TODO: write validator script, which checks that 
1. Every concept with a notation has specified at least one ConceptScheme
2. Notations are unique (within a ConceptScheme)

Processing ontologies with Jena
-------------------------------

* Parse enumerated data types: http://stackoverflow.com/questions/15018784/use-jena-to-parse-enumerated-datatype-generated-by-protege
