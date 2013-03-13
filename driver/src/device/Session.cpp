/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

#include "Session.h"

namespace logiana {

Session::Session( Frequency const& clock, MeasureType const& type, Condition const& cond, TriggerLine const& line )
:clock_(clock)
,triggerType_(type)
,triggerCond_(cond)
,triggerLine_(line)
{

}

Session::~Session() {
	// TODO Auto-generated destructor stub
}

unsigned char Session::clockCode() const {
	return this->clock_.code();
}

unsigned char Session::triggerTypeCode() const {
	return static_cast<unsigned int>(this->triggerType_);
}

unsigned char Session::triggerCondCode() const {
	return static_cast<unsigned int>(this->triggerCond_);
}

unsigned char Session::triggerLineCode() const {
	return static_cast<unsigned int>(this->triggerLine_);
}

}
