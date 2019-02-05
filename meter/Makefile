####################
# ORENETA Makefile #
# Abel Navarro     #
# December 2002    #
####################

### Compiler definitions ###
CC = gcc

### Main target ###
all: meter

### Targets ###
meter.o: meter.c meter.h
	$(CC) -c meter.c -g

capture.o: capture.c
	$(CC) -c capture.c -g

meter: meter.o capture.o
	$(CC) -o meter meter.o capture.o -lpcap

meter-static: meter.o capture.o
	$(CC) -static -o meter-static meter.o capture.o -lpcap

### Clean ###
clean:
	rm -Rf meter meter-static *.o *~ *.bak core.*

