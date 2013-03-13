/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

#pragma once

namespace logiana {

class Frequency;

class Frequency {
private:
	const char* const name_;
	float const nsec_;
	unsigned char const code_;
	constexpr Frequency( const char* const& name, float const& nsec, unsigned char code )
	:name_(name)
	,nsec_(nsec)
	,code_(code)
	{
	}
public:
	const char* const& name() const { return this->name_; }
	float const& nsec() const { return this->nsec_; }
	unsigned char const& code() const { return this->code_; }
	bool operator==(const Frequency& other) const { return code_ == other.code_; }
	static Frequency const& fromCode( char const& id );
public:
	static const Frequency _100MHz;
	static const Frequency _50MHz;
	static const Frequency _25MHz;
	static const Frequency _16_6MHz;
	static const Frequency _12_5MHz;
	static const Frequency _10MHz;
	static const Frequency _8_3MHz;
	static const Frequency _5MHz;
	static const Frequency _3_84MHz;
	static const Frequency _2MHz;
	static const Frequency _1MHz;
	static const Frequency _500KHz;
	static const Frequency _100KHz;
	static const Frequency _10KHz;
	static const Frequency _1KHz;
	static const Frequency _EXT;
	static const Frequency* ClockTable[16];
};

enum class MeasureType : unsigned char {
	_TOP=0,_Center=1,_Last=2
};

enum class Condition :unsigned char {
	_PosEdge=0, _NegEdge=1, _High=2, _Low=3
};

enum class TriggerLine : unsigned char {
			_Probe00=0,
			_Probe01=1,
			_Probe02=2,
			_Probe03=3,
			_Probe04=4,
			_Probe05=5,
			_Probe06=6,
			_Probe07=7,
			_Probe08=8,
			_Probe09=9,
			_Probe10=10,
			_Probe11=11,
			_Probe12=12,
			_Probe13=13,
			_Probe14=14,
			_HDL=15,
};



}
