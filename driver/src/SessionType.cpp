/*
 * Clock.cpp
 *
 *  Created on: Mar 12, 2013
 *      Author: psi
 */

#include "SessionType.h"

namespace logiana {

const Clock Clock::_100MHz("100MHz", 10.0, 0);
const Clock Clock::_50MHz("50MHz", 20.0, 1);
const Clock Clock::_25MHz("25MHz", 40, 2);
const Clock Clock::_16_6MHz("16.6MHz", 60, 3);
const Clock Clock::_12_5MHz("12.5MHz", 80.0, 4);
const Clock Clock::_10MHz("10MHz", 100.0, 5);
const Clock Clock::_8_3MHz("8.3MHz", 120.0, 6);
const Clock Clock::_5MHz("5MHz", 200.0, 7);
const Clock Clock::_3_84MHz("3.84MHz", 260.0, 8);
const Clock Clock::_2MHz("2MHz", 500.0, 9);
const Clock Clock::_1MHz("1MHz", 1000.0, 10);
const Clock Clock::_500KHz("50KHz", 2000.0, 11);
const Clock Clock::_100KHz("50KHz", 10000.0, 12);
const Clock Clock::_10KHz("10KHz", 100000.0, 13);
const Clock Clock::_1KHz("1KHz", 1000000.0, 14);
const Clock Clock::_EXT("EXT", 1, 15);
const Clock Clock::ClockTable[16] = {
		_100MHz,
		_50MHz,
		_25MHz,
		_16_6MHz,
		_12_5MHz,
		_10MHz,
		_8_3MHz,
		_5MHz,
		_3_84MHz,
		_2MHz,
		_1MHz,
		_500KHz,
		_100KHz,
		_10KHz,
		_1KHz,
		_EXT,
};

}
