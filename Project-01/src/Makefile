# Makefile v.1.0.0
# Compiles project source files

CC = gcc
CFLAGS = -w
SRC = $(wildcard *.c)
OBJ = $(patsubst %.c, %.o, $(SRC))

all: $(OBJ)

%.o: %.c
	$(CC) -c $<

clean:
	rm -f *.out *.o

.PHONY: all clean
