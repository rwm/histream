/**
 * Filters to modify HL7 messages before they are processed.
 * TODO: move filter code here (from hl7.hapi) to convert text lab values
 * to numeric values. E.g. remove operator from ">3" or convert "24+" to ">=24", 
 * move percent signs to units ("30%"), etc.
 *  
 */
package histream.hl7.filter;

