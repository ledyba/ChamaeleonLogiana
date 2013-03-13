//============================================================================
// Name        : logiana.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include "Host.h"
#include "Device.h"
#include <unistd.h>
#include <iostream>
#include <cstdio>

#ifdef __WIN32__

#include <windows.h>
#define sleep(n) Sleep(1000 * n)

#endif

using namespace logiana;

int main() {
	Host host;

	std::unique_ptr<Device> dev = host.find();
	if(dev && !dev->isLogicAnalyzer()) {
		dev->download();
		sleep(2);
		dev.reset();
		host.refresh();
		dev = host.find();
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
	dev->initAsLogicAnalyzer();
	std::printf("Starting...");
	std::fflush(stdout);
	dev->startProbe(Session(Clock::_100MHz, TriggerType::_Center, TriggerCond::_NegEdge, TriggerLine::_Probe00));
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
}
