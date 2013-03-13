/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

#include "SessionType.h"
#include <cstring>
#include <cstdio>
#include <stdexcept>

namespace logiana {

Frequency const& Frequency::fromCode( char const& id ) {
	if(id < static_cast<int>(sizeof ClockTable)) {
		return *ClockTable[static_cast<int>(id)];
	}else{
		char buff[256];
		std::snprintf(buff, sizeof buff, "Unknown frequency type: %d\n", id);
		throw std::runtime_error(buff);
	}
}

const Frequency Frequency::_100MHz("100MHz", 10.0, 0);
const Frequency Frequency::_50MHz("50MHz", 20.0, 1);
const Frequency Frequency::_25MHz("25MHz", 40, 2);
const Frequency Frequency::_16_6MHz("16.6MHz", 60, 3);
const Frequency Frequency::_12_5MHz("12.5MHz", 80.0, 4);
const Frequency Frequency::_10MHz("10MHz", 100.0, 5);
const Frequency Frequency::_8_3MHz("8.3MHz", 120.0, 6);
const Frequency Frequency::_5MHz("5MHz", 200.0, 7);
const Frequency Frequency::_3_84MHz("3.84MHz", 260.0, 8);
const Frequency Frequency::_2MHz("2MHz", 500.0, 9);
const Frequency Frequency::_1MHz("1MHz", 1000.0, 10);
const Frequency Frequency::_500KHz("50KHz", 2000.0, 11);
const Frequency Frequency::_100KHz("50KHz", 10000.0, 12);
const Frequency Frequency::_10KHz("10KHz", 100000.0, 13);
const Frequency Frequency::_1KHz("1KHz", 1000000.0, 14);
const Frequency Frequency::_EXT("EXT", 1, 15);
const Frequency* Frequency::ClockTable[16] = {
		&_100MHz,
		&_50MHz,
		&_25MHz,
		&_16_6MHz,
		&_12_5MHz,
		&_10MHz,
		&_8_3MHz,
		&_5MHz,
		&_3_84MHz,
		&_2MHz,
		&_1MHz,
		&_500KHz,
		&_100KHz,
		&_10KHz,
		&_1KHz,
		&_EXT,
};

}
