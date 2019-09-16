# Makefile v.1.2.0
# Compiles project source files

CC = gcc
INCLUDEDIR = ../include

LIBUSERDIRS = $(wildcard ../lib/*)
LIBDIRS = $(foreach dir, $(LIBUSERDIRS), $(wildcard $(dir)/*))
IPATH = $(addsuffix /include, $(LIBDIRS))
COMPILEFLAGS = -w $(addprefix -I, $(IPATH)) -I$(INCLUDEDIR)
SRC = $(wildcard *.c)
OBJ = $(patsubst %.c, %.o, $(SRC))

all: $(OBJ)

%.o: %.c
	$(CC) -c $< $(COMPILEFLAGS)

clean:
	rm -f *.out *.o

.PHONY: all clean
