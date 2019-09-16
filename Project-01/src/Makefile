# Makefile v.1.0.1
# Compiles project source files

CC = gcc
INCLUDEDIR = ../include
CFLAGS = -w -I$(INCLUDEDIR)
SRC = $(wildcard *.c)
OBJ = $(patsubst %.c, %.o, $(SRC))

all: $(OBJ)

%.o: %.c
	$(CC) -c $< $(CFLAGS)

clean:
	rm -f *.out *.o

.PHONY: all clean
