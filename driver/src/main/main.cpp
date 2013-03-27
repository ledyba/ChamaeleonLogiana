/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */
 
#include <iostream>
#include <cstdio>
#include "../device/Host.h"
#include "../device/Device.h"
#include "../device/Util.h"

using namespace logiana;

int main() {
	std::unique_ptr<Device> dev = host().find();
	try {
		std::printf("Device searching...\n");
		{
			int findCnt = 0;
			while(!dev){
				if( findCnt > 10 ){
					std::fprintf(stderr, "Device not found.");
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
			sleep(3);
			std::printf("Device re searching...\n");
			host().refresh();
			dev = host().find();
			{
				int findCnt = 0;
				while (!dev) {
					std::printf("wait...\n");
					if( findCnt > 10 ){
						std::fprintf(stderr, "Failed to download firmware. Device not found.");
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
			std::fprintf(stderr, "EzUSB device is not found.");
			return -1;
		} else if(!dev->isLogicAnalyzer()) {
			std::fprintf(stderr, "Failed to open EzUSB device.");
			return -1;
		} else {
			dev->bootLogicAnalyzer();
			std::printf("load device: %p\n", dev.get());
		}
	}catch(std::exception& e) {
		std::fprintf(stderr, "Failed to open EzUSB device: %s", e.what());
		return -1;
	} catch(...) {
		std::fprintf(stderr, "Caught unknown exception during finding logiana.");
		return -1;
	}
	try {
		std::printf("Starting...");
		std::fflush(stdout);
		dev->startMeasuring(Session(Frequency::_100MHz, MeasureType::_TOP, Condition::_PosEdge, TriggerLine::_Probe00));
		std::printf(" ok\n");
		std::fflush(stdout);
		while( dev->isMeasuring() ) {
			std::printf("Measuring now.\n");
		}
		std::vector<char> dat(dev->endProbe());
		for( std::size_t i=0;i<dat.size();i+=4 ){
			unsigned int d =
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+0])) <<  0) |
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+1])) <<  8) |
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+2])) << 16) |
					(static_cast<unsigned int>(static_cast<unsigned char>(dat[i+3])) << 24);
			std::printf("%08x\n",d);
			std::fflush(stdout);
		}
		return 0;
	} catch( std::exception& e ) {
		std::fprintf(stderr, "Error caught: %s\n", e.what());
	} catch (...) {
		std::fprintf(stderr, "Unknown error caught.\n");
	}
	return -1;

}
