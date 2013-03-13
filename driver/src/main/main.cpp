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
		if(dev && !dev->isLogicAnalyzer()) {
			dev->download();
			dev.reset();
			sleep(2);
			host().refresh();
			dev = host().find();
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
		dev->startMeasuring(Session(Frequency::_100MHz, MeasureType::_Center, Condition::_NegEdge, TriggerLine::_Probe00));
		std::printf(" ok\n");
		std::fflush(stdout);
		while( dev->isMeasuring() ) {
			std::printf("Measuring now.\n");
		}
		std::vector<char> dat(dev->endProbe());
		for( char d: dat ) {
			std::cout << std::hex << ((unsigned int)(unsigned char)d) << std::endl;
		}
		return 0;
	} catch( std::exception& e ) {
		std::fprintf(stderr, "Error caught: %s\n", e.what());
	} catch (...) {
		std::fprintf(stderr, "Unknown error caught.\n");
	}
	return -1;

}
