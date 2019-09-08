# Project 1 Makefile v.3.0.1
# Downloads dependecies and compiles source code

# When adding a new library:
# Add repo name to LIBNAMES as USERNAME/REPONAME
# Add a target of same name preceded by $(LIB)/ to download and compile it
# Assumes that every repo contains:
# 	- folder './lib' for .so
# 	- folder './include' for .h
#	(if not, make sure it does!)

ALLSUBDIRS = $(dir $(wildcard */Makefile))
LIB = lib
TESTS = tests
SRC = src
LIBNAMES = guidanoli/lwctl
LIBPATHS = $(addprefix $(LIB)/, $(LIBNAMES))

gitclone = git clone $(patsubst $(LIB)%,https://github.com%,$(1)).git $(1)

all: dependecies bin tests

$(LIB):
	mkdir -p $(LIB)

dependecies: $(LIB) $(LIBPATHS)

bin:
	$(MAKE) -C $(SRC) > /dev/null

tests:
	$(MAKE) -C $(TESTS) > /dev/null

# Dependecies targets
# ----------- -------

$(LIB)/guidanoli/lwctl:
	$(call gitclone, $@)
	$(MAKE) -C $@

# -------------------

cleanlib:
	rm -f -r -d $(LIB)

clean:
	for dir in $(ALLSUBDIRS); do \
	$(MAKE) -C $$dir clean; \
	done > /dev/null

.PHONY: all clean cleanlib dependecies bin tests
