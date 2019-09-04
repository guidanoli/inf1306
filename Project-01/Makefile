# Project 1 Makefile v.1.0.0
# Downloads dependecies and compiles source code

ALLSUBDIRS = $(dir $(wildcard */Makefile))

all: dependecies bin

lib:
	mkdir lib

dependecies: lwctl

bin:
	$(MAKE) -C src > /dev/null

lwctl: lib
	git clone https://github.com/guidanoli/$@.git lib/$@
	$(MAKE) -C lib/$@

clean:
	rm -f -r -d lib
	for dir in $(ALLSUBDIRS); do \
	$(MAKE) -C $$dir clean; \
	done > /dev/null

.PHONY: all clean dependecies bin lwctl
