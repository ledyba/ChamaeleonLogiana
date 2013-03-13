/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

package org.ledyba.logiana.control
import org.ledyba.logiana.control._

class Session(_freq : Frequency.Value, _type:MeasureType.Value, _cond:Condition.Value, _line:TriggerLine.Value) {
	private val freq = _freq;
	private val measureType = _type;
	private val cond = _cond;
	private val line = _line;
	lazy val freqCode = this.freq.code;
	lazy val measureTypeCode = this.measureType.code;
	lazy val condCode = this.cond.code;
	lazy val lineCode = this.line.code;
}
