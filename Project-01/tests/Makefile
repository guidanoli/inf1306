# Makefile
# Compiling and linking tests
# v.1.3.1

####################################
LIBNAMES = lwct
####################################
CC = gcc
LIBUSERDIRS = $(wildcard ../lib/*)
LIBDIRS = $(foreach dir, $(LIBUSERDIRS), $(wildcard $(dir)/*))
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

utils.out: $(SRCPATH)/utils.o $(SRCPATH)/parser.o t.utils.o
	$(CC) -o $@ $^ $(LINKFLAGS) -lm

%.out: $(SRCPATH)/%.o t.%.o
	$(CC) -o $@ $^ $(LINKFLAGS)

clean:
	rm -f *.out *.o

.PHONY: all clean
