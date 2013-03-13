/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

#pragma once
#include <memory>

namespace logiana {
class Device;
class Host;

Host& host();

class Host {
	friend Host& host();
private:
	Host();
public:
	void refresh();
	std::unique_ptr<Device> find();
};

}
