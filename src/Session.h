/*
 * Session.h
 *
 *  Created on: Mar 12, 2013
 *      Author: psi
 */

#pragma once
#include <string>
#include "SessionType.h"

namespace logiana {

class Session {
private:
	Clock const& clock_;
	TriggerType triggerType_;
	TriggerCond triggerCond_;
	TriggerLine triggerLine_;
public:
	Session( Clock const& clock, TriggerType const& type, TriggerCond const& cond, TriggerLine const& line );
	~Session();
public:
	unsigned char clockCode() const;
	unsigned char triggerTypeCode() const;
	unsigned char triggerCondCode() const;
	unsigned char triggerLineCode() const;
};

}
