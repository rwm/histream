/**
 * API interfaces and classes for fact inference.
 * E.g. deduce a fact from existing observations.
 * <p>
 * Inference can be used in time context or in 
 * encounter context.
 * </p>
 * <p>
 *  Encounter context inference will use all data that
 *  is available for a (complete) encounter therefore
 *  is mainly useful after the patient was discharged.
 *  Typical use cases are the ETL process, where additional
 *  information is generated from a full patient case; or
 *  the data table export where concepts need to be grouped
 *  in a common column.
 * </p>
 * <p>
 *  Time context inference can occur on continuous 
 *  observation in real time.
 * </p>
 * @author R.W.Majeed
 *
 */
package de.sekmi.histream.inference;