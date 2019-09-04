# Makefile
# Compiling and linking tests
# v.1.0.0

# When adding a new library:
# - On this Makefile
# 	Add its name on LIBNAMES
# - On the root Makefile
# 	Move the library interface to HDIR
# 	Move the library object to LIBDIR

####################################
LIBNAMES = lwct
####################################
CC = gcc
LIBDIR = ../lib/
HDIR = ../include
COMPILEFLAGS = -w -I$(HDIR)
LINKFLAGS = -w $(addprefix -L, $(LIBDIR)) \
		$(addprefix -l, $(LIBNAMES)) \
 		$(addprefix -Wl,-R, $(LIBDIR))
SRC = $(wildcard *.c)
OUT = $(patsubst %.c, %.out, $(SRC))

all: $(OUT)

%.o: %.c
	$(CC) -c $< $(COMPILEFLAGS)

%.out: %.o
	$(CC) -o $@ $< $(LINKFLAGS)

clean:
	rm -f *.out

.PHONY: all clean
