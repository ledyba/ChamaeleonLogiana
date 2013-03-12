/*
 * Clock.h
 *
 *  Created on: Mar 12, 2013
 *      Author: psi
 */

#pragma once

namespace logiana {

class Clock;

class Clock {
private:
	const char* const name_;
	float const nsec_;
	unsigned char const code_;
	constexpr Clock( const char* const& name, float const& nsec, unsigned char code )
	:name_(name)
	,nsec_(nsec)
	,code_(code)
	{
	}
public:
	const char* const& name() const { return this->name_; }
	float const& nsec() const { return this->nsec_; }
	unsigned char const& code() const { return this->code_; }
	bool operator==(const Clock& other) const { return code_ == other.code_; }
	static Clock const& fromCode( char const& id );
public:
	static const Clock _100MHz;
	static const Clock _50MHz;
	static const Clock _25MHz;
	static const Clock _16_6MHz;
	static const Clock _12_5MHz;
	static const Clock _10MHz;
	static const Clock _8_3MHz;
	static const Clock _5MHz;
	static const Clock _3_84MHz;
	static const Clock _2MHz;
	static const Clock _1MHz;
	static const Clock _500KHz;
	static const Clock _100KHz;
	static const Clock _10KHz;
	static const Clock _1KHz;
	static const Clock _EXT;
	static const Clock ClockTable[16];
};

enum class TriggerType : unsigned char {
	_TOP=0,_Center=1,_Last=2
};

enum class TriggerCond :unsigned char {
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
