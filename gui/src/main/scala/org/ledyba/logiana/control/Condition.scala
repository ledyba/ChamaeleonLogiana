/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */
package org.ledyba.logiana.control

object Condition extends Enumeration {
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
	val PosEdge    = new Value("PosEdge", 0)
	val NegEdge    = new Value("NegEdge", 1)
	val High       = new Value("High",    2)
	val Low        = new Value("Low",     3)
	override lazy val values = super.values.map(x=>x.asInstanceOf[Condition.Value])
}
