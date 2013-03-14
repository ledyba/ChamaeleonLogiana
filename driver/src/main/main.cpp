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
			std::fprintf(stderr, "EzUSB device is not found.\n");
			return -1;
		} else if(!dev->isLogicAnalyzer()) {
			std::fprintf(stderr, "Failed to open EzUSB device.\n");
			return -1;
		} else {
			std::printf("load device: %p\n", dev.get());
		}
		dev->bootLogicAnalyzer();

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
