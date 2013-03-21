# coding: utf-8
#
# Logiana
#
# Copyright 2013, PSI
#

PYTHON := python
WAF := ./waf
OPT += 

all: 64 32

64:
	CXX=x86_64-w64-mingw32-g++ CC=x86_64-w64-mingw32-gcc $(PYTHON) $(WAF) -o b64 configure build

32:
	CXX=g++ CC=gcc $(PYTHON) $(WAF) -o b32 configure build

install:
	$(PYTHON) $(WAF) $(OPT) install

uninstall:
	$(PYTHON) $(WAF) $(OPT) uninstall

clean:
	$(PYTHON) $(WAF) $(OPT) clean clean_debug clean_release

distclean:
	$(PYTHON) $(WAF) $(OPT) distclean
	rm -rf b64/
	rm -rf b32/
	rm -f .lock*
	rm -rf .waf-*
	rm -rf waf-*
