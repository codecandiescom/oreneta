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

/* ORENETA                        */
/* Meter - Sniffing server engine */

/* Abel Navarro                   */
/* December 2002 - September 2003 */


/* Libpcap includes */
#include <stdio.h>
#include <pcap.h>
#include <net/ethernet.h>

/* Socket includes */
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/poll.h>

/* Pthread includes */
/* #include <pthread.h> */
#include <string.h>

/* Signal includes */
#include <signal.h>

/* Oreneta includes */
#include "meter.h"

/****************************************************************************/
/* Global variables                                */
int state;			   /* State of the server      */
int sockfd;			   /* Listen socket descriptor */
int newsockfd;		   /* socket to analizer       */
pcap_t *pcap_handle;   /* Handle for pcap session  */
int cont;			   /* Continue in main loop?   */
struct pollfd ufds[2]; /* 0=data, 1=pcap           */

/****************************************************************************/
//void broken_socket (int signal) {
//      /* This signal can be removed if we do these actions */
//      /* after a read error in main loop */
//      printf ("Received SIGPIPE signal (%d)\n", signal);
//      if (state == STATE_CAPTURE) {
//              printf ("SIGPIPE: closing pcap session\n");
//              close_session (pcap_handle);
//              state = STATE_WAIT;
//      }
//}

/****************************************************************************/
void interrupt(int signal)
{
    //printf("Received SIGINT signal (%d)\n", signal);
    //printf("Somebody typed Control+C\n");
	printf("exiting via user request...\n");

    close(sockfd);
    if (state == STATE_CAPTURE) {
		printf("SIGINT: closing pcap session\n");
		close_session(pcap_handle);
		close(newsockfd);
    }

    exit(1);
}

/****************************************************************************/
void check_input (int argc, char **argv, int *portno) {
	int aux_port;
	
	if (argc > 2) {
		printf ("***   Oreneta Meter   ***\n");
		printf ("*** Abel Navarro 2003 ***\n");
		printf ("USAGE: %s\n",argv[0]);
		printf ("       %s <listen_port> (default 7777)\n",argv[0]);
		exit(0);	
	}

	/* No parameters */
	if (argc == 1) return;

	/* One parameter */
	if (argc == 2) {
		aux_port = strtol(argv[1], (char **)NULL, 10);
		if (aux_port < 1 || aux_port > 65535) {
			printf ("Error: incorrect port number\n");
			exit (1);
		} else {
			*portno = aux_port;
			return;
		}
	} 
}

/****************************************************************************/
int main(int argc, char **argv)
{

    /* Socket variables */
    int portno = LISTEN_PORT;         /* TCP port                         */
    int clilen;         /* Client socket struct length      */
    char buffer[256];   /* Socket buffer                    */
    //struct sockaddr_in6 serv_addr;	/* Server socket struct */
    struct sockaddr_in serv_addr;	/* Server socket struct */
    //struct sockaddr_in6 cli_addr;	/* Client socket struct */
    struct sockaddr_in cli_addr;	/* Client socket struct */
    //struct in6_addr anyaddr;		/* For INADDR_ANY		*/

    /* Other variables */
    int n;			/* index                                                */
    int tmp;

    int status;

    /* Check input parameters */
    check_input (argc, argv, &portno);

    /* Initialize socket communitcation */
    //sockfd = socket(AF_INET6, SOCK_STREAM, 0);
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
		printf("ERROR (socket): error opening listen socket\n");
		exit(1);
    }

    //printf("After socket()\n");

    /* Program signal for broken sockets */
    // signal (SIGPIPE, &broken_socket);

    /* Program signal to handle Control-C */
    signal(SIGINT, &interrupt);

    /* setsockopt? */
    

    /* Initialize server socket struct */
    //anyaddr = IN6ADDR_ANY_INIT;
    bzero((char *) &serv_addr, sizeof(serv_addr));
    //serv_addr.sin6_family = AF_INET;	/* Internet Protocol            */
    //serv_addr.sin6_addr = in6addr_any;	/* Listen on all interfaces     */
    //serv_addr.sin6_port = htons(portno);	/* Listen port                  */
    serv_addr.sin_family = AF_INET;	/* Internet Protocol            */
    serv_addr.sin_addr.s_addr = htonl (INADDR_ANY);	/* Listen on all interfaces     */
    serv_addr.sin_port = htons(portno);	/* Listen port                  */

    /* Bind server socket */
    //printf("Before bind()\n");

    if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
		perror("ERROR on binding");
		exit(1);
    }

    //printf("After bind()\n");

    /* Begin listen the server socket */
    listen(sockfd, 5);

    /* Accept connections */
    clilen = sizeof(cli_addr);

    /* Infinite loop */
    while (TRUE) {
		newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		if (newsockfd < 0) {
		    printf("ERROR (accept): error accepting new connection\n");
		    exit(1);
		}
		//printf("Connected to: %s:%hu\n", inet_ntoa(cli_addr.sin6_addr),
		       //ntohs(cli_addr.sin6_port));

		/* Prepare the newly created socket to be polled */
		ufds[0].fd = newsockfd;
		ufds[0].events = POLLIN;

		bzero(buffer, 256);


		/*** MAIN LOOP ***/
		cont = TRUE;
		state = STATE_WAIT;
		while (cont) {

			//printf ("\n");
			cont = TRUE;

			/* Poll data socket or (data & pcap) descriptors */
			if (state == STATE_WAIT) {
				status = poll (ufds, 1, -1);  /* data */
			} else {
				status = poll (ufds, 2, -1);  /* data & pcap */
			}

			if (status < 0) {
				printf ("ERROR (poll): %d\n", status);
				exit(1);

			} else if (status == 0) {
				/* there is nothing to read */
				//printf ("there is nothing to read\n");
				if (state == STATE_WAIT) {
					sleep(1);	// <-- must change this ! ! ! ! ! !
				}

			} else {

				/* Wait state */
				if (state == STATE_WAIT) {
					if (ufds[0].revents & POLLIN) {
						process_command (newsockfd);
					}

				/* Capture state */
				} else {
					if (ufds[1].revents & POLLIN) {
						capture_packet (pcap_handle);
					}

					if (ufds[0].revents & POLLIN) {
						process_command (newsockfd);
					} 
				}
			}	        
		}       
    }           
                
} 

/****************************************************************************/
void process_command (int fd) {
	/* Variables */
	int n, i, j;
	char buffer[256];
	char dev[6];
	char filter[256];
	char err;
	
	
	bzero (buffer, 256);
	n = read(newsockfd, buffer, 255);
	if (n < 0) {
		//printf("ERROR %d (read): error reading from socket\n", n);
		//if (state == STATE_CAPTURE) {
		//    state = STATE_WAIT;
		//    close(newsockfd);	/* if it's not closed yet */
		//    sleep(1);	/* give time to thread to exit normally */
		//    close_session(pcap_handle);
		//}
		//cont = FALSE;
		if (state == STATE_WAIT) {
			sleep(1);
		}
		
	} else if (n == 0) {
		
		/* This means that the analyzer disconnected */
		//printf("read zero bytes command\n");
		cont = FALSE;
	
	} else if (n > 0) {
	
		if (strncmp(buffer, "start", 5) == 0) {
		    // Start command
		    // Begin sending capture
		    
		    //printf("start capture\n");
		    //printf ("start: \"%s\"\n",buffer);
		    //for(n=0;n<50;n++) printf("%02x ",buffer[n]);
		    //printf("\n");
		    
		    if (state == STATE_WAIT) {
		    	
		    	/* get device and filter strings */
		    	bzero (dev, 6);
		    	bzero (filter, 256);
		    	
		    	for(i=0;buffer[6+i]!=0x20;i++)
		    		dev[i] = buffer[6+i];
		    		
		    	for(j=0;buffer[7+i+j]!=0x0d && buffer[7+i+j]!=0x0a;j++)
		    		filter[j] = buffer[7+i+j];
				
				pcap_handle = begin_session(dev, filter, &err);
				if (pcap_handle != NULL && err == 0) {
					//printf("Sending capture...\n");
					state = STATE_CAPTURE;
					
					ufds[1].fd = *((int *)pcap_handle);	/* SUPER-ULTRA-MEGA-DIRTY! :) */
					ufds[1].events = POLLIN;
				} else if (pcap_handle != NULL) {
					close_session(pcap_handle);
				}
					
		    } else {
				//printf ("Not in 'wait' state. Cannot send capture.\n");
		    }
	
		} else if (strncmp(buffer, "quit", 4) == 0) {
	
		    /* The server should never die (until ctrl+c) */
	
		} else if (strncmp(buffer, "stop", 4) == 0) {
		    /* Stop command   */
		    /* Stop capturing */
		    if (state == STATE_CAPTURE) {
				//printf("Stop capture.\n");
				state = STATE_WAIT;						
				close_session(pcap_handle);
				//cont=FALSE;
				//set_block(newsockfd);
				
		    } else {
				//printf ("Cannot stop capture if it's not started.\n");
		    }
	
		} else if (strncmp(buffer, "stats", 5) == 0) {
		    /* Send capture statistics */
	
		    if (state == STATE_CAPTURE) {
				//printf("Send capture statistics\n");
				send_stats(pcap_handle);
		    } else {
				//printf ("Cannot send stats if capture not started\n");
		    }
	
		} else if (strncmp(buffer, "filter", 6) == 0) {
	
	
		} else {
		    /* Unknown command */
		    //printf("Command not recognized\n");
		}
	}
}


