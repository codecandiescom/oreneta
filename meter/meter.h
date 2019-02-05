/*

	This file is part of ORENETA.

    ORENETA is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    ORENETA is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ORENETA; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

/* meter.h */

#ifndef METERH
#define METERH

/* Definitions for server capture engine */

#define LISTEN_PORT	7777

/* States of the server engine */
#define STATE_WAIT	  1
#define STATE_CAPTURE	2

#define MESSAGE_SIZE  100

/* Another definitions */
#define TRUE  1
#define FALSE 0

/* Function definition */
void capture_loop (pcap_t *handle);
pcap_t * begin_session ();
void close_session (pcap_t *handle);
void process_packet (struct pcap_pkthdr *header, unsigned char *packet, \
                     char *message, int *message_length);
void send_message (char *message, int *message_length);
void m_append (char *message, int *message_length, void *data, int data_size);
unsigned long crc_append (unsigned char *buf, int len);
void process_command (int fd);

#endif
