/*
 * Host.h
 *
 *  Created on: Mar 12, 2013
 *      Author: psi
 */

#pragma once
#include <memory>

namespace logiana {
class Device;

class Host {
public:
	Host();
	void refresh();
	std::unique_ptr<Device> find();
};

}
