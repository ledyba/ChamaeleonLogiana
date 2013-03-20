/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */
package org.ledyba.logiana.control

object Frequency extends Enumeration {
	class Value(_name:String, _nanosec:Float, _code:Byte) extends Val(nextId, _name) {
		nextId = nextId + 1;
		val name = _name;
		val code = _code;
		val nanosec = _nanosec;
		lazy val freq = 1000000000.0f/this.nanosec;
	}
	
	val _100MHz  = new Value("100MHz",    10.0f,  0);
	val _50MHz   = new Value("50MHz",     20.0f,  1);
	val _25MHz   = new Value("25MHz",     40.0f,  2);
	val _16_6MHz = new Value("16.6MHz",   60.0f,  3);
	val _12_5MHz = new Value("12.5MHz",   80.0f,  4);
	val _10MHz   = new Value("10MHz",    100.0f,  5);
	val _8_3MHz  = new Value("8.3MHz",   120.0f,  6);
	val _5MHz    = new Value("5MHz",     200.0f,  7);
	val _3_84MHz = new Value("3.84MHz",  260.0f,  8);
	val _2MHz    = new Value("2MHz",     500.0f,  9);
	val _1MHz    = new Value("1MHz",    1000.0f, 10);
	val _500KHz  = new Value("50KHz",   2000.0f, 11);
	val _100KHz  = new Value("50KHz",  10000.0f, 12);
	val _10KHz   = new Value("10KHz", 100000.0f, 13);
	val _1KHz    = new Value("1KHz", 1000000.0f, 14);
	val _EXT     = new Value("EXT", 1, 15);
	override lazy val values = super.values.map(x=>x.asInstanceOf[Frequency.Value])
}