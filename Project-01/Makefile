# Project 1 Makefile v.2.0.0
# Downloads dependecies and compiles source code

# When adding a new library:
# Add repo name to LIBNAMES
# Add target with same name to:
#	- clone repo
#	- compile source code
# Assumes that every repo contains:
# 	- lib for .so
# 	- include for .h
#	(if not, make sure it does!)

ALLSUBDIRS = $(dir $(wildcard */Makefile))
LIB = lib
TESTS = tests
SRC = src
LIBNAMES = lwctl
LIBPATHS = $(addprefix $(LIB)/, $(LIBNAMES))

all: dependecies bin tests

$(LIB):
	mkdir -p $(LIB)

dependecies: $(LIB) $(LIBPATHS)

bin:
	$(MAKE) -C $(SRC) > /dev/null

tests:
	$(MAKE) -C $(TESTS) > /dev/null

$(LIBPATHS): $(LIBNAMES)

lwctl:
	git clone https://github.com/guidanoli/$@.git $(LIB)/$@
	$(MAKE) -C $(LIB)/$@

clean:
	rm -f -r -d $(LIB)
	for dir in $(ALLSUBDIRS); do \
	$(MAKE) -C $$dir clean; \
	done > /dev/null

.PHONY: all clean dependecies bin tests $(LIBNAMES)
