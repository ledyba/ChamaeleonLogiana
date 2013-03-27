/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */
package org.ledyba.logiana.control

object MeasureType extends Enumeration {
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
	val Top    = new Value("Top",    0)
	val Center = new Value("Center", 1)
	val Last   = new Value("Last",   2)
	override lazy val values = super.values.map(x=>x.asInstanceOf[MeasureType.Value])
}