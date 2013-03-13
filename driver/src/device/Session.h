/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

#pragma once
#include <string>
#include "SessionType.h"

namespace logiana {

class Session {
private:
	Frequency const& clock_;
	MeasureType triggerType_;
	Condition triggerCond_;
	TriggerLine triggerLine_;
public:
	Session( Frequency const& clock, MeasureType const& type, Condition const& cond, TriggerLine const& line );
	~Session();
public:
	unsigned char clockCode() const;
	unsigned char triggerTypeCode() const;
	unsigned char triggerCondCode() const;
	unsigned char triggerLineCode() const;
};

}
