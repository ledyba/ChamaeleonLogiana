/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

#include <usb.h>
#include <cstring>
#include <stdexcept>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include "Firmware.h"
#include "Device.h"
#include "Util.h"

namespace logiana {

Device::Device(struct usb_device* dev)
:dev_(dev)
,handle_(nullptr)
{
	this->handle_ = usb_open(dev_);
	if (!this->handle_) {
		std::string msg("Failed to open device: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}
	if( usb_set_configuration(this->handle_, this->dev_->config->bConfigurationValue) < 0 ) {
		std::string msg("Failed to set configuration: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}
	if( usb_claim_interface(this->handle_, this->dev_->config->interface->altsetting->bInterfaceNumber) < 0 ) {
		std::string msg("Failed to claim interface: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}
}

Device::~Device() noexcept
{
	try {
		usb_release_interface(this->handle_, this->dev_->config->interface->altsetting->bInterfaceNumber);
		if (usb_close(this->handle_)) {
			std::fprintf(stderr, "Failed to close device: %s\n", usb_strerror());
			std::fflush(stderr);
		}
		this->handle_ = nullptr;
	}catch(...){}
}

std::string Device::getString(const unsigned char& id, const unsigned short & lang) const {
	char buff[64];
	std::memset(buff, 0, sizeof buff);
	const int num = usb_control_msg(this->handle_, 0x80, 0x06, ((0x3 << 8) | id), lang, buff, sizeof buff, 5000);
	if (num < 0) {
		std::string msg("Failed to read string: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}
	std::string ret;
	for (std::size_t n = 2; n < sizeof buff; n += 2) {
		if (buff[n] == '\0') {
			break;
		}
		ret.push_back(buff[n]);
	}
	return ret;
}

bool Device::isLogicAnalyzer() const {
	return getString(1, 27) == "LGFW" && getString(2, 27) == "V100";
}

void Device::bootLogicAnalyzer()
{
#if IS_LINUX
	usb_reset(this->handle_);
	usb_resetep(this->handle_, GPFW_CPIPE);
	usb_resetep(this->handle_, GPFW_RPIPE);
	usb_resetep(this->handle_, GPFW_WPIPE);
#endif
	char buf[2];
	buf[0]=GPFW_DIR | 0x07;
	buf[1]=GPFW_SET | 0x00;
	this->controlWrite(buf);
}

void Device::halt() {
	char data = 1; //1 means "halt"
	std::printf("halt: ");
	std::fflush(stdout);
	const int ret = usb_control_msg(this->handle_, 0x40, 0xA0, 0x7f92, 0, &data, 1, 5000);
	if (ret != 1) {
		std::string msg("Failed to halt: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	} else {
		std::printf("ok!\n");
		std::fflush(stdout);
	}
}

void Device::start() {
	char data = 0; // 0 means "run"
	std::printf("start up: ");
	std::fflush(stdout);
	const int ret = usb_control_msg(this->handle_, 0x40, 0xA0, 0x7f92, 0, &data, 1, 5000);
	if (ret != 1) {
#if IS_LINUX
		std::string msg("Failed to start: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
#endif
	}
	std::printf("ok!\n");
	std::fflush(stdout);
}

void Device::download() {
	this->halt();
	std::printf("sending: ");
	for (int off = 0; off < FirmwareSize; off += 0x100) {
		std::vector<unsigned char> data(&Firmware[off], &Firmware[off + 0x100]);
		const std::size_t ret = usb_control_msg(this->handle_, 0x40, 0xA0, off,
				0, reinterpret_cast<char*>(data.data()), data.size(), 5000);
		if (ret != data.size()) {
			std::string msg("Failed to upload firmware: ");
			msg += usb_strerror();
			throw std::runtime_error(msg);
		} else {
			std::printf(".");
		}
	}
	std::printf("done!\n");
	this->halt();
	std::printf("comparing: ");
	for (int off = 0; off < FirmwareSize; off += 0x100) {
		std::vector<unsigned char> data;
		data.resize(0x100);
		const std::size_t ret = usb_control_msg(this->handle_, 0xC0, 0xA0, off, 0, reinterpret_cast<char*>(data.data()), data.size(), 5000);
		if (ret != data.size()) {
			std::string msg("Failed to download firmware: ");
			msg += usb_strerror();
			throw std::runtime_error(msg);
		}
		for( std::size_t i = 0; i<data.size(); ++i ) {
			if (data[i] != Firmware[off + i]) {
				char buff[1024];
				std::snprintf(buff, sizeof buff,
						"Firmware is not the same at: $%04x, 0x%02x != %02x",
						static_cast<unsigned short>((off + i) & 0xffff), data[i],
						Firmware[off + i]);
				throw std::runtime_error(buff);
			}
		}
		std::printf(".");
	}
	std::printf("done!\n");
	this->start();
}

void Device::bulkWrite(char* buf, std::size_t buflen) {
	const int ret = usb_bulk_write(this->handle_, GPFW_WPIPE, buf, buflen, 5000);
	if (ret < 0) {
		std::string msg("Failed to write control: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}else if( static_cast<std::size_t>(ret) != buflen ) {
		char buf[1024];
		snprintf(buf, sizeof buf, "[ControlWrite] size not match:  req: %d != actual: %d", static_cast<int>(buflen), ret);
		throw std::runtime_error(buf);
	}
}

void Device::controlWrite(char* buf, std::size_t buflen) {
	const int ret = usb_bulk_write(this->handle_, GPFW_CPIPE, buf, buflen, 5000);
	if (ret < 0) {
		std::string msg("Failed to write control: ");
		msg += usb_strerror();
		throw std::runtime_error(msg);
	}else if( static_cast<std::size_t>(ret) != buflen ) {
		char buf[1024];
		snprintf(buf, sizeof buf, "[ControlWrite] size not match:  req: %d != actual: %d", static_cast<int>(buflen), ret);
		throw std::runtime_error(buf);
	}
}

void Device::bulkRead(char* buf, std::size_t buflen) {
	std::size_t left = buflen;
	while( left > 0 ) {
		const int ret = usb_bulk_read(this->handle_, GPFW_RPIPE, &buf[buflen-left], left, 5000);
		if (ret < 0) {
			std::string msg("Failed to write bulk: ");
			msg += usb_strerror();
			throw std::runtime_error(msg);
		}
		left -= ret;
	}
}

void Device::setRegister(bool isSecond, unsigned char val)
{
	char buf[2];
	buf[0] = ( isSecond ? GPFW_WRITE | 0x02 : GPFW_WRITE );
	buf[1] = ( val );
	this->controlWrite(buf);
}

void Device::startMeasuring(Session const& session)
{
	this->setRegister(false, ((session.clockCode() <<4) | (session.triggerTypeCode())));
	this->setRegister(true,  ((session.triggerCondCode() <<6) | (session.triggerLineCode() << 2)));
	{
		char buf[1];
		buf[0] = ( GPFW_SET | 0x04 );
		this->controlWrite(buf);
	}
}

bool Device::isMeasuring()
{
	char buf[1];
	buf[0] = ( GPFW_GET | 0x04 );
	this->controlWrite(buf);
	this->bulkRead(buf);
	return (buf[0] & 0x08) == 0;
}

std::vector<char> Device::endProbe()
{
	{
		char cbuf[4];
		cbuf[0]=GPFW_BREAD|0x06;
		cbuf[1]=(RamSize*4) & 0xff;
		cbuf[2]=((RamSize*4)/64) & 0xff;
		cbuf[3]=(((RamSize*4)/64)>>8) & 0xff;
		this->controlWrite(cbuf);
	}
	std::vector<char> ramData;
	ramData.resize( RamSize*4 );
	this->bulkRead(ramData.data(), ramData.size());
	{
		char cbuf[1];
		cbuf[0] = LGFW_GETBAD;
		this->controlWrite(cbuf);
		unsigned short bad;
		this->bulkRead(reinterpret_cast<char*>(&bad), 2);
		std::memset(ramData.data(), 0, bad*8);
	}
	{
		char cbuf[1];
		cbuf[0] = GPFW_SET | 0x0;
		this->controlWrite(cbuf);
	}
	return std::move(ramData);
}

}
