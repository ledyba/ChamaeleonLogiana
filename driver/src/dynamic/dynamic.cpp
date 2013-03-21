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
		std::printf("Device searching...\n");
		{
			int findCnt = 0;
			while(!dev){
				if( findCnt > 10 ){
					std::snprintf(err, errlen, "Device not found.");
					std::fputs(err, stderr);
					return -1;
				}
				++findCnt;
				std::printf("wait...\n");
				sleep(1);
				host().refresh();
				dev = host().find();
			}
		}

		if(dev && !dev->isLogicAnalyzer()) {
			dev->download();
			dev.reset();
			std::printf("Device re searching...\n");
			host().refresh();
			dev = host().find();
			{
				int findCnt = 0;
				while (!dev) {
					std::printf("wait...\n");
					if( findCnt > 10 ){
						std::snprintf(err, errlen, "Failed to download firmware. Device not found.");
						std::fputs(err, stderr);
						return -1;
					}
					++findCnt;
					sleep(1);
					host().refresh();
					dev = host().find();
				}
			}
		}
		if(!dev) {
			std::snprintf(err, errlen, "EzUSB device is not found.");
			std::fputs(err, stderr);
			return -1;
		} else if(!dev->isLogicAnalyzer()) {
			std::snprintf(err, errlen, "Failed to open EzUSB device.");
			std::fputs(err, stderr);
			return -1;
		} else {
			dev->bootLogicAnalyzer();
			*ptr = dev.release();
			std::printf("load device: %p\n", *ptr);
			return 0;
		}
	}catch(std::exception& e) {
		std::snprintf(err, errlen, "Failed to open EzUSB device: %s", e.what());
		std::fputs(err, stderr);
		return -1;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during finding logiana.");
		std::fputs(err, stderr);
		return -1;
	}
	return -1;
}
extern int DYNAPI nowLogianaMeasuring(void* device, char* err, int errlen)
{
	if( !device ) {
		std::snprintf(err, errlen, "device handler is null.");
		std::fputs(err, stderr);
		return -1;
	}
	try {
		Device* const dev = reinterpret_cast<Device*>(device);
		const int ret = dev->isMeasuring() ? 1 : 0;
		return ret;
	} catch(std::exception& e) {
		std::snprintf(err, errlen, "Failed to check measuring or not now: \"%s\"", e.what());
		std::fputs(err, stderr);
		return -2;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during checking whether measuring or not now");
		std::fputs(err, stderr);
		return -3;
	}
	return -4;
}
DYNAPI extern int startMeasuring(void* device, char* err, int errlen, char freq, char mesType, char cond, char line )
{
	if( !device ) {
		std::snprintf(err, errlen, "device handler is null.");
		std::fputs(err, stderr);
		return -1;
	}
	Device* const dev = reinterpret_cast<Device*>(device);
	try {
		Session sess( Frequency::fromCode(freq), MeasureType(mesType), Condition(cond), TriggerLine(line) );
		dev->startMeasuring(sess);
		return 0;
	} catch ( std::exception& e ) {
		std::snprintf(err, errlen, "Failed to start measuring: %s", e.what());
		std::fputs(err, stderr);
		return -1;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during starting measuring.");
		std::fputs(err, stderr);
		return -2;
	}
	return -3;
}
DYNAPI extern int endMeasuring(void* device, char* err, int errlen, unsigned int* buffer, int buflen)
{
	if( !device ) {
		std::snprintf(err, errlen, "device handler is null.");
		std::fputs(err, stderr);
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
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+0])) <<  0) |
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+1])) <<  8) |
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+2])) << 16) |
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+3])) << 24);
		}
		return bufi;
	} catch ( std::exception& e ) {
		std::snprintf(err, errlen, "Failed to end measuring: %s", e.what());
		std::fputs(err, stderr);
		return -1;
	} catch(...) {
		std::snprintf(err, errlen, "Caught unknown exception during ending measuring.");
		std::fputs(err, stderr);
		return -2;
	}
	return -3;
}

void closeLogiana(void* device)
{
	Device* const dev = reinterpret_cast<Device*>(device);
	delete dev;
}



