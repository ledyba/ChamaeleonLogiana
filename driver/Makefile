# coding: utf-8
#
# Logiana
#
# Copyright 2013, PSI
#

PYTHON := python
WAF := ./waf
OPT += 

64:
	CXX=x86_64-w64-mingw32-g++ CC=x86_64-w64-mingw32-gcc $(PYTHON) $(WAF) configure build

all:
	$(PYTHON) $(WAF) $(OPT) build

install:
	$(PYTHON) $(WAF) $(OPT) install

uninstall:
	$(PYTHON) $(WAF) $(OPT) uninstall

clean:
	$(PYTHON) $(WAF) $(OPT) clean clean_debug clean_release

distclean:
	$(PYTHON) $(WAF) $(OPT) distclean
	rm -rf build/
	rm -f .lock*
	rm -rf .waf-*
	rm -rf waf-*
