/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, psi
 */

#include <cstdio>
#define DLL_EXPORT
#include "dynamic.h"
#include <string>
#include "../device/Host.h"
#include "../device/Device.h"
using namespace logiana;

extern int DYNAPI findLogiana(void** ptr, char* err, int errlen)
{
	try {
		std::unique_ptr<Device> dev = host().find();
		while(!dev){
			std::printf("Device searching...\n");
			sleep(1);
			host().refresh();
			dev = host().find();
		}

		if(dev && !dev->isLogicAnalyzer()) {
			dev->download();
			dev.reset();
			while (!dev) {
				sleep(1);
				host().refresh();
				dev = host().find();
			}
		}
		if(!dev) {
			std::snprintf(err, errlen, "EzUSB device is not found.");
			return -1;
		} else if(!dev->isLogicAnalyzer()) {
			std::snprintf(err, errlen, "Failed to open EzUSB device.");
			return -1;
		} else {
			dev->bootLogicAnalyzer();
			*ptr = dev.release();
			return 0;
		}
	}catch(std::exception& e) {
		std::snprintf(err, errlen, "Failed to open EzUSB device: %s", e.what());
		return -1;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during finding logiana.");
		return -1;
	}
	return -1;
}
extern int DYNAPI nowLogianaMeasuring(void* device, char* err, int errlen)
{
	if( !device ) {
		std::snprintf(err, errlen, "device handler is null.");
		return -1;
	}
	try {
		Device* const dev = reinterpret_cast<Device*>(device);
		return dev->isMeasuring() ? 1 : 0;
	} catch(std::exception& e) {
		std::snprintf(err, errlen, "Failed to check measuring or not now: %s", e.what());
		return -2;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during checking whether measuring or not now");
		return -3;
	}
	return -4;
}
DYNAPI extern int startMeasuring(void* device, char* err, int errlen, char freq, char mesType, char cond, char line )
{
	if( !device ) {
		std::snprintf(err, errlen, "device handler is null.");
		return -1;
	}
	Device* const dev = reinterpret_cast<Device*>(device);
	try {
		Session sess( Frequency::fromCode(freq), MeasureType(mesType), Condition(cond), TriggerLine(line) );
		dev->startMeasuring(sess);
		return 0;
	} catch ( std::exception& e ) {
		std::snprintf(err, errlen, "Failed to start measuring: %s", e.what());
		return -1;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during starting measuring.");
		return -2;
	}
	return -3;
}
DYNAPI extern int endMeasuring(void* device, char* err, int errlen, unsigned int* buffer, int buflen)
{
	if( !device ) {
		std::snprintf(err, errlen, "device handler is null.");
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
		std::snprintf(err, errlen, "Failed to start measuring: %s", e.what());
		return -1;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during starting measuring.");
		return -2;
	}
	return -3;
}

void closeLogiana(void* device)
{
	Device* const dev = reinterpret_cast<Device*>(device);
	delete dev;
}



