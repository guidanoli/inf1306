# Makefile
# Compiling and linking tests
# v.1.2.0

# When adding a new library:
# - On this Makefile
# 	Add its name on LIBNAMES

####################################
LIBNAMES = lwct
####################################
CC = gcc
LIBDIRS = $(wildcard ../lib/*)
IPATH = $(addsuffix /include, $(LIBDIRS))
LPATH = $(addsuffix /lib, $(LIBDIRS))
SRCPATH = ../src
COMPILEFLAGS = -w $(addprefix -I, $(IPATH)) -I$(SRCPATH)
RTLIBFLAG = -Wl,-R
LINKFLAGS = -w $(addprefix -L, $(LPATH)) \
		$(addprefix -l, $(LIBNAMES)) \
 		$(addprefix $(RTLIBFLAG), $(LPATH))
SRC = $(wildcard *.c)
OUT = $(patsubst t.%.c, %.out, $(SRC))

all: $(OUT)

%.o: %.c
	$(CC) -c $< $(COMPILEFLAGS)

%.out: $(SRCPATH)/%.o t.%.o
	$(CC) -o $@ $^ $(LINKFLAGS)

clean:
	rm -f *.out *.o

.PHONY: all clean
