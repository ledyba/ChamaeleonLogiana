/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

package org.ledyba.logiana.control
import org.ledyba.logiana.control._

class Session(_freq : Frequency.Value, _type:MeasureType.Value, _cond:Condition.Value, _line:TriggerLine.Value) {
	val freq = _freq;
	val measureType = _type;
	val cond = _cond;
	val line = _line;
}
