/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */
package org.ledyba.logiana.control

object TriggerLine extends Enumeration {
	class Value(_name:String,  _code:Byte) extends Val(nextId, _name) {
		val name = _name;
		val code = _code;
	}
	def fromCode(code:Byte):Value = {
		for(v <- values) {
			if(v.asInstanceOf[Value].code == code) {
				return v.asInstanceOf[Value];
			}
		}
		return null;
	}
	val Probe00 = new Value("Probe00",  0)
	val Probe01 = new Value("Probe01",  1)
	val Probe02 = new Value("Probe02",  2)
	val Probe03 = new Value("Probe03",  3)
	val Probe04 = new Value("Probe04",  4)
	val Probe05 = new Value("Probe05",  5)
	val Probe06 = new Value("Probe06",  6)
	val Probe07 = new Value("Probe07",  7)
	val Probe08 = new Value("Probe08",  8)
	val Probe09 = new Value("Probe09",  9)
	val Probe10 = new Value("Probe10", 10)
	val Probe11 = new Value("Probe11", 11)
	val Probe12 = new Value("Probe12", 12)
	val Probe13 = new Value("Probe13", 13)
	val Probe14 = new Value("Probe14", 14)
	val HDL     = new Value("HDL",     15)
	override lazy val values = super.values.map(x=>x.asInstanceOf[TriggerLine.Value])
}
