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

/* Capture.c */
/* Capture loop */

/* Libpcap includes */
#include <stdio.h>
#include <pcap.h>
#include <net/ethernet.h>
#include <sys/poll.h>

/* Pthread include */
/* #include <pthread.h> */

/* Oreneta include */
#include "meter.h"
#include "capture.h"

/* Global variables            */
/* Shared with child processes */
extern int state;		/* State of the server */
extern int newsockfd;	/* socket to analizer  */

unsigned long crc;   /* crc-32 identificator   */
unsigned long ncrc;  /* crc-32 network order   */

int datalink;		/* Layer 2 technology */
int layer2offset;	/* Layer 2 offset (i.e. ethernet header = 14) */

/****************************************************************************/
void capture_packet (pcap_t *handle) {

	/* Variables */
	unsigned char *packet;	/* Packet pointer */
	struct pcap_pkthdr header; /* Header of libpcap for packet */
	char message[MESSAGE_SIZE];		/* Message to send */
	int message_length=0;		/* Length of message to send */
  	
	packet = (unsigned char *) pcap_next (handle, &header);
	if (packet != NULL) {
		//printf("packet not null. cool!\n");
		/* process_packet */
		process_packet (&header, packet, message, &message_length);
	
		/* send_message */
		if (message_length > 0)
			send_message (message, &message_length);
			
	} else {
		/* if you do a 'service network restart' in RedHat distributions */
		/* and you are capturing packets, you can enter this case        */
		
		//printf ("capture_packet: hey dude, packet is null\n");
	}	
}

/****************************************************************************/
pcap_t * begin_session (char *dev, char *str_filter, char *err) {
	
  /* Variables */
  //char *dev;				/* Pointer to capture device    */
  char errbuf[PCAP_ERRBUF_SIZE];	/* Error string         */
  pcap_t *handle;			/* Pcap session handler	        */
  int status;				/* for return values            */
  char message[MESSAGE_SIZE];		/* Message to send      */
  int message_length;		/* Length of message to send    */
  //int datalink;				/* Layer 2 technology		*/
  
  /* Debug variables */
  //char str_filter[] = "not port 22 and not port 7777";
  //char str_filter[] = "port 123";
  struct bpf_program filter;
  bpf_u_int32 mask;
  bpf_u_int32 net;
	
  /* Automatically get the capture device */
  
  /* Be careful - must free resources? (not here) */
  //dev = pcap_lookupdev (errbuf);
  //if (dev != NULL) {
  //   printf ("Device: %s\n", dev);
  //} else {
  //   printf ("ERROR (pcap_lookupdev): %s\n", errbuf);
  //   exit(1);
  //}
  
  bzero(message, MESSAGE_SIZE);

  /* Begin capture session */
  /* Parameters:
     par0: device pointer
     par1: bytes to read (BUFSIZ defined in pcap.h)
     par2: 0 - normal mode
           1 - promiscuous mode
     par3: time to wait for packets in miliseconds before read times out
     par4: error string
  */

  //printf ("Trying to open capture in \"%s\"\n",dev);  
  
  handle = pcap_open_live (dev, BUFSIZ, 1, 2500, errbuf);
  if (handle == NULL) {
  	printf ("ERROR (pcap_open_live): %s\n", errbuf);
	printf ("Must be root?\n");
	
	message[0] = (char)127;   /* type of message: notification to analyzer */
	message[1] = 1;		      /* message: error */
	*err = 1;
	message_length = 2;
	send_message (message, &message_length);
	
	return handle;
  }
  
  //printf ("Pcap session openend\n");
  
  //printf ("Applying filter: \"%s\"\n", str_filter);
  
  /* Debug - Filter SSH & Oreneta */
  if (pcap_lookupnet (dev, &net, &mask, errbuf)<0) {
  	message[0] = (char)127;
  	message[1] = 2;
  	*err = 2;
  	message_length = 2;
  	send_message (message, &message_length);
  	return handle;
  }
  
  if (pcap_compile (handle, &filter, str_filter, 0 ,net)<0) {
  	message[0] = (char)127;
  	message[1] = 3;
  	*err = 3;
  	message_length = 2;
  	send_message (message, &message_length);
  	return handle;
  }
  
  if (pcap_setfilter (handle, &filter)<0) {
  	message[0] = (char)127;
  	message[1] = 4;
  	*err = 4;
  	message_length = 2;
  	send_message (message, &message_length);
  	return handle;
  }
  pcap_freecode (&filter);
 
 //printf ("Pcap filters applied\n");

//free(dev);
/* Must free dev? */

  /* Determine link type for the device */
  /* Not very useful (at present)       */
  datalink = pcap_datalink(handle);
  switch (datalink) {
  	case DLT_NULL:
	  layer2offset = 0;
  	  //printf ("DATA Link: Loopback\n");
	  break;
			
	case DLT_EN10MB:
	  layer2offset = ETH_H;
	  //printf ("DATA Link: Ethernet\n");
	  break;
			
	case DLT_LINUX_SLL:
	  layer2offset = COOK_H;
	  //printf ("Data Link: Linux cooked sockets\n");
	  break;

	default:
	  layer2offset = 0;
	  //printf ("Data link undetermined: %d\n", datalink);
	  break;
  }
  
  message[0] = (char)127;
  message[1] = 0;
  *err = 0;
  message_length = 2;
  send_message (message, &message_length);
  //printf("Sending start result\n");
  return handle;
}

/****************************************************************************/
void close_session (pcap_t *handle) {
  
  /* Close capture session */
  pcap_close (handle);
}

/****************************************************************************/
void process_packet (struct pcap_pkthdr *header, unsigned char *packet, \
                     char *m, int *ml) {
    
    /* Are static variables more efficient in a so popular function? */
	/* Let's try                                                     */
	                     	
	/* Variables */
	static long sec;                   /* timestamp.seconds      */
	static long usec;                  /* timestamp.microseconds */
	static char proto;                 /* ethernet protocol      */
	static unsigned int ipproto;        /* ip protocol     */
	static int payload_crc;		       /* length of payload crc  */
	static unsigned char next_header;  /* IPv6 next header       */
	static char cont;                  /* continue ip6 headers?  */
	static unsigned int ext_size;      /* extended headers size  */
	int i;
	
	/* Pointers to different parts of the packet */
	ethernet_header *eth;
	cooked_header *cook;
	ipv4_header *ipv4;
	ipv6_header *ipv6;
	ipv6_ext_header *ipv6_ext;
	udp_header *udp;
	tcp_header *tcp;
	icmp6_header *icmp6;
	char *payload;
	
	/* init */
	*ml = 0;                  /* message length        */
	bzero (m, MESSAGE_SIZE);  /* message               */
	
	/* crc 32 */
	crc = 0xffffffff;
	ncrc = htonl (crc);
	payload_crc = 0;		  /* length of payload crc */
	
	/* convert timestamp to network format */
	sec = htonl(header->ts.tv_sec);
	usec = htonl(header->ts.tv_usec);

	/* Position pointers in packet */
	if (datalink == DLT_EN10MB) {
		eth = (ethernet_header *) packet;
		ipproto = ntohs(eth->proto);
		
	} else if (datalink == DLT_LINUX_SLL) {
		/*
		//printf ("packet[0] = %d[%x]\n", packet[0], packet[0]);
		if (packet[layer2offset] >> 4 == 4) ipproto = IP4;
		if (packet[layer2offset] >> 4 == 6) ipproto = IP6;
		*/
		cook = (cooked_header *) packet;
		ipproto = ntohs(cook->proto);
	}

/*
printf ("Packet: ");
	for (i=0;i<30;i++) {
		printf ("%02x ", packet[i]);
	}
printf("\n");
*/
	
	switch (ipproto) {
		
		/*** IPV4 ***/
		case IP4:
		    ipv4 = (ipv4_header *) (packet + layer2offset);
			proto = 4;
	
			/* This info is independent of the protocol */
			m_append (m, ml, (void *) &proto, 1);
			m_append (m, ml, (void *) &sec, sizeof(sec));
	            m_append (m, ml, (void *) &usec, sizeof(usec));
	        
	        
			switch (ipv4->proto) {
				case IPPROTO_TCP:
					//printf("IPv4 TCP packet\n");
					tcp = (tcp_header *) (packet + layer2offset + IP4_H);
					payload = (char *) (packet + layer2offset + IP4_H + TCP_H);
					
					/* Calculate CRC-32 */
					crc = crc_append ((unsigned char *) &ipv4->length, 2);
					crc = crc_append ((unsigned char *) &ipv4->id, 2);
					crc = crc_append ((unsigned char *) &ipv4->proto, 1);
					crc = crc_append ((unsigned char *) &ipv4->src, 4);
					crc = crc_append ((unsigned char *) &ipv4->dst, 4);
					
					/* append crc of the first 40 bytes from payload */
					payload_crc = header->len - layer2offset - IP4_H - TCP_H;
					if (payload_crc > 40) payload_crc = 40;
					crc = crc_append ((unsigned char *) payload, payload_crc);
					ncrc = htonl (crc); /* only necessary for debug */
					
					/* Construct message */					
					m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
			        m_append (m, ml, (void *) &ipv4->length, 2);
			        m_append (m, ml, (void *) &ipv4->src, 4);
			        m_append (m, ml, (void *) &ipv4->dst, 4);
			        m_append (m, ml, (void *) &ipv4->proto, 1);
					m_append (m, ml, (void *) &tcp->src_port, 2);
					m_append (m, ml, (void *) &tcp->dst_port, 2);
					break;
					
				case IPPROTO_UDP:
					//printf ("IPv4 UDP packet\n");
					udp = (udp_header *) (packet + layer2offset + IP4_H);
					payload = (char *) (packet + layer2offset + IP4_H + UDP_H);
					
					/* Calculate CRC-32 */
					crc = crc_append ((unsigned char *) &ipv4->length, 2);
					crc = crc_append ((unsigned char *) &ipv4->id, 2);
					crc = crc_append ((unsigned char *) &ipv4->proto, 1);
					crc = crc_append ((unsigned char *) &ipv4->src, 4);
					crc = crc_append ((unsigned char *) &ipv4->dst, 4);
					
					/* append crc of the first 40 bytes from payload */
					payload_crc = header->len - layer2offset - IP4_H - UDP_H;
					if (payload_crc > 40) payload_crc = 40;
					crc = crc_append ((unsigned char *) payload, payload_crc);
					ncrc = htonl (crc); /* only necessary for debug */
					
					/* Construct message */
					m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
			        m_append (m, ml, (void *) &ipv4->length, 2);
			        m_append (m, ml, (void *) &ipv4->src, 4);
			        m_append (m, ml, (void *) &ipv4->dst, 4);
			        m_append (m, ml, (void *) &ipv4->proto, 1);
			        m_append (m, ml, (void *) &udp->src_port, 2);
					m_append (m, ml, (void *) &udp->dst_port, 2);
					break;
				
				default:
					//printf ("IPv4 default packet\n");
					payload = (char *) (packet + layer2offset + IP4_H);
					
					crc = crc_append ((unsigned char *) &ipv4->length, 2);
					crc = crc_append ((unsigned char *) &ipv4->id, 2);
					crc = crc_append ((unsigned char *) &ipv4->proto, 1);
					crc = crc_append ((unsigned char *) &ipv4->src, 4);
					crc = crc_append ((unsigned char *) &ipv4->dst, 4);
					
					/* append crc of the first 40 bytes from payload */
					payload_crc = header->len - layer2offset - IP4_H;
					if (payload_crc > 40) payload_crc = 40;
					crc = crc_append ((unsigned char *) payload, payload_crc);
					ncrc = htonl (crc); /* only necessary for debug */
					
					m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
			        m_append (m, ml, (void *) &ipv4->length, 2);
			        m_append (m, ml, (void *) &ipv4->src, 4);
			        m_append (m, ml, (void *) &ipv4->dst, 4);
			        m_append (m, ml, (void *) &ipv4->proto, 1);
					break;
			}

			break;
		
		/*** IPv6 ***/
		case IP6:
			//printf("IPv6 packet\n");
			proto = 6;
			ipv6 = (ipv6_header *) (packet + layer2offset);
			
			m_append (m, ml, (void *) &proto, 1);
			m_append (m, ml, (void *) &sec, sizeof(sec));
	            m_append (m, ml, (void *) &usec, sizeof(usec));
	        
	        ext_size = 0;
	        next_header = ipv6->next_h;
	        payload = (char *) (packet + IP6_H + layer2offset);
	        cont = 1;
	        while (cont) {
	        	
	        	switch (next_header) {
	        	
	        	case IPPROTO_HOP2HOP:
	        	case IPPROTO_DSTOPTS:
	        	case IPPROTO_ROUTING:
	        	case IPPROTO_AH:
	        	case IPPROTO_ESP:
	        		ipv6_ext = (ipv6_ext_header *) payload;
	        		next_header = ipv6_ext->next_header;
	        		payload += 8 + ipv6_ext->header_length;
	        		ext_size += 8 + ipv6_ext->header_length;
	        		break;
	        		
	        	case IPPROTO_FRAGMENT:
	        		ipv6_ext = (ipv6_ext_header *) payload;
	        		next_header = ipv6_ext->next_header;
	        		payload += 8;
	        		ext_size += 8;
	        		break;
	        		
	        	case IPPROTO_TCP:
	        		tcp = (tcp_header *) payload;
					payload += (tcp->h_length)*4;
					ext_size += (tcp->h_length)*4;
					
					/* Calculate CRC-32 */
					crc = crc_append ((unsigned char *) &ipv6->length, 2);
					crc = crc_append ((unsigned char *) &ipv6->src, 16);
					crc = crc_append ((unsigned char *) &ipv6->dst, 16);
					crc = crc_append ((unsigned char *) &tcp->seq, 4);
					crc = crc_append ((unsigned char *) &tcp->tcp_checksum, 2);
					
					/* append crc of the first 40 bytes from payload */
					payload_crc = ipv6->length - ext_size;
					if (payload_crc > 40) payload_crc = 40;
					crc = crc_append ((unsigned char *) payload, payload_crc);
					ncrc = htonl (crc); /* only necessary for debug */
					
					/* Construct message */
					m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
					i = ntohs(ipv6->length) + 40;
					i = htons(i);
					m_append (m, ml, (void *) &i, 2); //header 40 bytes not included, we must add them.
					m_append (m, ml, (void *) &ipv6->src, 16);
					m_append (m, ml, (void *) &ipv6->dst, 16);
					m_append (m, ml, (void *) &next_header, 1);
					m_append (m, ml, (void *) &tcp->src_port, 2);
					m_append (m, ml, (void *) &tcp->dst_port, 2);
					
	        		cont = 0;
	        		break;
	        		
	        	case IPPROTO_UDP:
	        		udp = (udp_header *) payload;
					payload += UDP_H;
					ext_size += UDP_H;
					
					/* Calculate CRC-32 */
					crc = crc_append ((unsigned char *) &ipv6->length, 2);
					crc = crc_append ((unsigned char *) &ipv6->src, 16);
					crc = crc_append ((unsigned char *) &ipv6->dst, 16);
					crc = crc_append ((unsigned char *) &udp->udp_checksum, 2);
					
					/* append crc of the first 40 bytes from payload */
					payload_crc = ipv6->length - ext_size;
					if (payload_crc > 40) payload_crc = 40;
					crc = crc_append ((unsigned char *) payload, payload_crc);
					ncrc = htonl (crc); /* only necessary for debug */
					
					/* Construct message */
					m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
					i = ntohs(ipv6->length) + 40;
					i = htons(i);
			        m_append (m, ml, (void *) &i, 2); //add header 40 bytes
			        m_append (m, ml, (void *) &ipv6->src, 16);
			        m_append (m, ml, (void *) &ipv6->dst, 16);
			        m_append (m, ml, (void *) &next_header, 1);
			        m_append (m, ml, (void *) &udp->src_port, 2);
					m_append (m, ml, (void *) &udp->dst_port, 2);
					
	        		cont = 0;
	        		break;
	        		
	        	case IPPROTO_ICMP6:
	   				/* Maybe could be included in the default case for ipv6 */
	        		icmp6 = (icmp6_header *) payload;
	        		payload += ICMP6_H;
	        		ext_size += ICMP6_H;
	        		
	        		/* Calculate CRC-32 */
					crc = crc_append ((unsigned char *) &ipv6->length, 2);
					crc = crc_append ((unsigned char *) &ipv6->src, 16);
					crc = crc_append ((unsigned char *) &ipv6->dst, 16);
					crc = crc_append ((unsigned char *) &icmp6->type, 1);
					crc = crc_append ((unsigned char *) &icmp6->checksum, 2);
					
					/* append crc of the first 40 bytes from payload */
					payload_crc = ipv6->length - ext_size;
					if (payload_crc > 40) payload_crc = 40;
					crc = crc_append ((unsigned char *) payload, payload_crc);
					ncrc = htonl (crc); /* only necessary for debug */
					
					/* Construct message */
					m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
			        m_append (m, ml, (void *) (&ipv6->length)+40, 2); // add header 40 bytes
			        m_append (m, ml, (void *) &ipv6->src, 16);
			        m_append (m, ml, (void *) &ipv6->dst, 16);
			        m_append (m, ml, (void *) &next_header, 1);
	        		
	        		cont = 0;
	        		break;
	        		
	        	default:
	        		/* Calculate CRC-32 */
					crc = crc_append ((unsigned char *) &ipv6->length, 2);
					crc = crc_append ((unsigned char *) &ipv6->src, 16);
					crc = crc_append ((unsigned char *) &ipv6->dst, 16);
										
					/* append crc of the first 40 bytes from payload */
					payload_crc = ipv6->length - ext_size;
					if (payload_crc > 40) payload_crc = 40;
					crc = crc_append ((unsigned char *) payload, payload_crc);
					ncrc = htonl (crc); /* only necessary for debug */
					
					/* Construct message */
					m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
					i = ntohs(ipv6->length) + 40;
					i = htons(i);
			        m_append (m, ml, (void *) &i, 2); // add header 40 bytes
			        m_append (m, ml, (void *) &ipv6->src, 16);
			        m_append (m, ml, (void *) &ipv6->dst, 16);
			        m_append (m, ml, (void *) &next_header, 1);
	        		cont = 0;
	        		break;
	        	}
	        }
	        
	        break;
		
		/*** ARP and others ***/
		default:
			/* if we found a packet we doesn't recognize, we forget about it */
			//printf ("ARP & others\n");
			//proto = 0;
			//m_append (m, ml, (void *) &proto, sizeof(proto));
			//m_append (m, ml, (void *) &sec, sizeof(sec));
	        //m_append (m, ml, (void *) &usec, sizeof(usec));
	        //m_append (m, ml, (void *) &ncrc, sizeof(ncrc));
			break;
	}
	
	/* Print capture information */
	/* With high bandwith, printing each packet is very very very inefficient */
	/*printf ("%d.%06d len %d crc-32 %x\n", \
		header->ts.tv_sec, header->ts.tv_usec, \
		header->len, crc);*/
}

/****************************************************************************/
void send_stats (pcap_t *handle) {
	int ml=0;               /* message length */
	char m[MESSAGE_SIZE];   /* message        */
	char proto = 10;        /* statistics     */
	struct pcap_stat stats;
	unsigned int ps_recv, ps_drop;
	
	pcap_stats (handle, &stats);
	
	ps_recv = htonl(stats.ps_recv);
	ps_drop = htonl(stats.ps_drop);
	
	m_append (m, &ml, (void *) &proto, sizeof(proto));
	m_append (m, &ml, (void *) &ps_recv, sizeof(ps_recv));
	m_append (m, &ml, (void *) &ps_drop, sizeof(ps_drop));
	
	//printf ("statistics %d/%d %d/%d\n",stats.ps_recv,ps_recv,stats.ps_drop,ps_drop);
	
	send_message (m, &ml);
}

/****************************************************************************/
void send_message (char *message, int *message_length) {
	int i;
	
	write (newsockfd, message, *message_length);
	//if (message[0] == 127) printf("message sent (%d)\n",message[1]);
}

/****************************************************************************/
void m_append (char *message, int *message_length, void *data, int data_size) {
  int ml;

  ml = *message_length;
  memcpy (message+ml, data, data_size);
  *message_length += data_size;
  
}

/****************************************************************************/
unsigned long crc_append (unsigned char *buf, int len)
{
  int i;
  
  for (i = 0; i < len; i++) {
    crc = UPDC32 (buf[i], crc);
  }
  return (crc);
}
