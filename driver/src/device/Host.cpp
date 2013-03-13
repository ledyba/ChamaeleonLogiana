/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */
 
#include <usb.h>
#include <string>
#include <stdexcept>
#include <memory>
#include "Host.h"
#include "Device.h"

namespace logiana {

Host& host() {
	static Host host;
	return host;
}

Host::Host() {
	usb_init();
	usb_get_busses();
	usb_set_debug(4);
	this->refresh();
}

void Host::refresh() {
	usb_get_busses();
	if (usb_find_busses() < 0) {
		std::string msg("Oops. failed to find USB buses: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}
	if (usb_find_devices() < 0) {
		std::string msg("Oops. failed to find USB devices: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}
}

std::unique_ptr<Device> Host::find() {
	struct usb_bus* busses;
	busses = usb_get_busses();
	struct usb_bus* bus;
	for (bus = busses; bus; bus = bus->next) {
		struct usb_device* dev;
		for (dev = bus->devices; dev; dev = dev->next) {
			if (dev->descriptor.idVendor == 0x0547 && dev->descriptor.idProduct == 0x2131) {
				return std::unique_ptr<Device>(new Device(dev));
			}
		}
	}
	return std::unique_ptr<Device>();
}

}
