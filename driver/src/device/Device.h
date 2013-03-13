/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

#pragma once
#include <string>
#include <vector>
#include "ControlCode.h"
#include "Session.h"

namespace logiana {

const unsigned int RamSize = (1024*128);

class Device {
	struct usb_device* const dev_;
	struct usb_dev_handle * handle_;
public:
	Device(struct usb_device* dev);
	~Device() noexcept;
public:
	bool isLogicAnalyzer() const;
	void download();
	void bootLogicAnalyzer();
public:
	void startMeasuring(Session const& session);
	bool isMeasuring();
	std::vector<char> endProbe();
private:
	std::string getString(const unsigned char& id, const unsigned short & lang) const;
	void halt();
	void start();
	void bulkWrite(char* buf, std::size_t buflen);
	template <std::size_t N> inline void bulkWrite( char (&buf)[N] ) { bulkWrite(buf, N); };
	void controlWrite(char* buf, std::size_t buflen);
	template <std::size_t N> inline void controlWrite( char (&buf)[N] ) { controlWrite(buf, N); };
	void bulkRead(char* buf, std::size_t buflen);
	template <std::size_t N> inline void bulkRead( char (&buf)[N] ) { bulkRead(buf, N); };
	void setRegister(bool isSecond, unsigned char val);
};

}
