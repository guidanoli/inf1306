# Project 1 Makefile v.1.0.0
# Downloads dependecies and compiles source code

# When adding a new library:
# Add repo name to LIBNAMES
# Add target with same name to:
#	- clone repo
#	- compile source code
#	- copy .so to lib
#	- copy .h to include

ALLSUBDIRS = $(dir $(wildcard */Makefile))
LIB = lib
INCLUDE = include
LIBNAMES = lwctl
LIBPATHS = $(addprefix $(LIB)/, $(LIBNAMES))

all: dependecies bin

$(LIB):
	mkdir -p $(LIB)

$(INCLUDE):
	mkdir -p $(INCLUDE)

dependecies: $(LIB) $(INCLUDE) $(LIBPATHS)

bin:
	$(MAKE) -C src > /dev/null

$(LIBPATHS): $(LIBNAMES)

lwctl:
	git clone https://github.com/guidanoli/$@.git $(LIB)/$@
	$(MAKE) -C $(LIB)/$@
	cp $(LIB)/$@/lib/liblwct.so $(LIB)
	cp $(LIB)/$@/include/lwct.h $(INCLUDE)

clean:
	rm -f -r -d $(LIB) $(INCLUDE)
	for dir in $(ALLSUBDIRS); do \
	$(MAKE) -C $$dir clean; \
	done > /dev/null

.PHONY: all clean dependecies bin $(LIBNAMES)
