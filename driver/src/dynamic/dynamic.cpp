/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, psi
 */

#include <cstdio>
#define DLL_EXPORT
#include "dynamic.h"
#include "../device/Host.h"
#include "../device/Device.h"
using namespace logiana;

extern void* DYNAPI findLogiana()
{
	try {
		std::unique_ptr<Device> dev = host().find();
		if(dev && !dev->isLogicAnalyzer()) {
			dev->download();
			sleep(2);
			dev.reset();
			host().refresh();
			dev = host().find();
		}
		if(!dev) {
			std::fprintf(stderr, "EzUSB device is not found.\n");
			return nullptr;
		} else if(!dev->isLogicAnalyzer()) {
			std::fprintf(stderr, "Failed to open EzUSB device.\n");
			return nullptr;
		} else {
			std::printf("load device: %p\n", dev.get());
		}
		dev->bootLogicAnalyzer();
		return dev.release();
	}catch(std::exception& e) {
		std::string const msg("Failed to open EzUSB device: ");
		std::fprintf(stderr, (msg+e.what()+"\n").c_str());
	} catch(...) {
		std::fprintf(stderr, "Caught unknown exception during finding logiana.\n");
	}
	return nullptr;
}
int nowLogianaMeasuring(void* device)
{
	if( !device ) {
		std::fprintf(stderr, "device handler is null.\n");
		return -1;
	}
	try {
		Device* const dev = reinterpret_cast<Device*>(device);
		return dev->isMeasuring() ? 0 : 1;
	} catch(std::exception& e) {
		std::string msg("Failed to check measuring or not now: ");
		msg += e.what();
		msg += "\n";
		std::fprintf(stderr, msg.c_str());
		return -2;
	} catch(...) {
		std::fprintf(stderr, "Caught unknown exception during checking whether measuring or not now\n");
		return -3;
	}
	return -4;
}
int startMeasuring(void* device, char freq, char mesType, char cond, char line )
{
	if( !device ) {
		std::fprintf(stderr, "device handler is null.\n");
		return -1;
	}
	Device* const dev = reinterpret_cast<Device*>(device);
	try {
		Session sess( Frequency::fromCode(freq), MeasureType(mesType), Condition(cond), TriggerLine(line) );
		dev->startMeasuring(sess);
		return 0;
	} catch ( std::exception& e ) {
		std::string msg("Failed to start measuring: ");
		msg += e.what();
		msg += "\n";
		std::fprintf(stderr, msg.c_str());
		return -1;
	} catch(...) {
		std::fprintf(stderr, "Caught unknown exception during starting measuring\n");
		return -2;
	}
	return -3;
}
int endMeasuring(void* device, unsigned int* buffer, int buflen)
{
	if( !device ) {
		std::fprintf(stderr, "device handler is null.\n");
		return -1;
	}
	Device* const dev = reinterpret_cast<Device*>(device);
	try {
		if( dev->isMeasuring() ) {
			return 0;
		}
		std::vector<char> dat(dev->endProbe());
		int bufi = 0;
		for( std::size_t i = 0; i<dat.size(); i+=4, ++bufi ) {
			if( buflen <= bufi ) {
				break;
			}
			buffer[bufi] =
					(dat[i+0] << 24) |
					(dat[i+1] << 16) |
					(dat[i+2] <<  8) |
					(dat[i+3] <<  0);
		}
		return bufi;
	} catch ( std::exception& e ) {
		std::string msg("Failed to start measuring: ");
		msg += e.what();
		msg += "\n";
		std::fprintf(stderr, msg.c_str());
		return -1;
	} catch(...) {
		std::fprintf(stderr, "Caught unknown exception during starting measuring\n");
		return -2;
	}
	return -3;
}

void closeLogiana(void* device)
{
	Device* const dev = reinterpret_cast<Device*>(device);
	delete dev;
}



