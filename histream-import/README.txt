Parsing of columns
------------------
Columns are parsed as follows:
1. If a @constant-value attribute is present, that value is used. Jump directly to 5 (@na processing)
2. Otherwise, the actual column value is used
3. Regular expression substitution is performed if present via @regex-replace (may change the value)
4. Map rules are executed if present via map/case and map/otherwise elements. These rules may change the value (and concept code)
5. If the @na attribute is present and equals the calculated value, the value is removed completely
