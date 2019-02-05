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

import java.net.*;
import java.io.*;
import java.util.*;

public class Meter {
	
	////////////////////////////////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////////////////////////////////
	public final static int STATE_DISCONNECTED = 0;
	public final static int STATE_WAIT         = 1;
	public final static int STATE_CAPTURE      = 2;
	
	////////////////////////////////////////////////////////////////////////////
	// Attributes
	////////////////////////////////////////////////////////////////////////////
	boolean cont;
	boolean receive;
	int answerSemaphore;
	byte result;            //
	int state;              // State of the meter
	private double lastTime;     // Last packet arrival time
	//public double presentTime;  // Estimated present time of the meter
	public int monitor_length;
	
	Thread thread;
	
	public FlowSet flows;	// Collection of flows

	public int meter_id;
 	InetAddress meter_ip;
	Socket meter_socket;

	PrintWriter meter_out;
	OrenetaDataInputStream meter_in;
	
	public Analyzer analyzer;
	
	MeterStats mStats;
	
	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	Meter (Analyzer analyzer, int meter_id) {
		this.analyzer = analyzer;
		this.meter_id = meter_id;
		thread = new Thread() { public void run() { readLoop(); } };
		answerSemaphore = 0;
		state = STATE_DISCONNECTED;
		lastTime = 0;
		//presentTime = 0;
		monitor_length = Oreneta.MONITOR_LENGTH;
		
		// Initialize FlowSet
		flows = new FlowSet();
	}
		 
	////////////////////////////////////////////////////////////////////////////
	public void connect (String host, int meter_port) throws IOException {
		meter_ip = InetAddress.getByName(host);
		meter_socket = new Socket (meter_ip, meter_port);
		meter_out = new PrintWriter(meter_socket.getOutputStream(), true);
        meter_in = new OrenetaDataInputStream (meter_socket.getInputStream());
        state = STATE_WAIT;
	}

	////////////////////////////////////////////////////////////////////////////
	public void connect (String host) throws IOException {
		this.connect (host, 7777); // default port
	}

	////////////////////////////////////////////////////////////////////////////
	public void disconnect () {
		try {
			meter_in.close();
			meter_out.close();
			meter_socket.close();
			state = STATE_DISCONNECTED;
		} catch (IOException e) {
			analyzer.gui.debug ("Error disconnecting: " + e);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	public synchronized byte start (String device_filter) {
		//gui.debug("Meter.start()");
		meter_out.println("start "+device_filter);
		
		if (!thread.isAlive()) {
			receive = true;
			thread.start();
			//gui.debug ("start thread");
		} else {
			notifyForReceive();	// Unblock the thread
		}
		
		// wait for an answer
		//gui.debug("start: going to wait");
		answerSemaphore = 0;
		waitAnswer();
		//gui.debug("start result: "+result);
		//if (result != 0) thread.stop();
		if (result == 0) {
			state = STATE_CAPTURE;
			lastTime = 0;	// init time counter
		}
		
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	public void stop () {
		receive = false;
		meter_out.println("stop");
		state = STATE_WAIT;
		//thread.stop();
		//flush input
	}
	
	////////////////////////////////////////////////////////////////////////////
	public MeterStats stats () {
		if (state==STATE_CAPTURE) {
			meter_out.println("stats");
			//waitAnswer();
			return mStats;
			//gui.debug ("p_recv: "+mStats.packets_received+" p_drop: " + mStats.packets_dropped);
		} else {
			return null;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void waitAnswer () {
		//gui.debug("waitAnswer");
		try {
			if (answerSemaphore == 0) {
				wait();
			}
			answerSemaphore--;
			//gui.debug("as: "+answerSemaphore);
		} catch (InterruptedException e) {}
	}

	////////////////////////////////////////////////////////////////////////////
	public synchronized void notifyAnswer () {
		answerSemaphore++;
		//gui.debug ("as: "+answerSemaphore);
		notify();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void waitForReceive () {
		try {
			wait();
		} catch (InterruptedException e) {}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void notifyForReceive () {
		receive = true;
		notify();	
	}
		
	////////////////////////////////////////////////////////////////////////////
	private void readLoop() {
		byte proto;			// IP protocol
		byte tproto;		// Transport protocol
		long sec, usec;
		Timestamp ts;
		long crc;
		int length;
		InetAddress src;
		InetAddress dst;
		//int src_port;
		//int dst_port;
		
		// This thread is started once. After that, we must control it
		// with wait() & notify()
		while (true) {
			
			while (!receive) {
				waitForReceive();
			}
					
			try {
				
				// Read common info
				proto = meter_in.readByte();
				
				ts = new Timestamp();
				
				// ui.debug ("PACKET: "+proto+" ["+sec+"."+usec+"] "+crc);
				
				switch(proto) {
				
				////////////////////////////////////////////////////////////////////////////
				// Statistics
				case 10: // statistics
					mStats = new MeterStats();
					mStats.packets_received = meter_in.readUnsignedInt();
					mStats.packets_dropped  = meter_in.readUnsignedInt();
					//gui.debug ("statistics: recv "+mStats.packets_received+
					//	" drop "+mStats.packets_dropped);
					//notifyAnswer();
					break;
					
				////////////////////////////////////////////////////////////////////////////
				// IPv4
				case 4:
					ts.sec   = meter_in.readUnsignedInt();
					ts.usec  = meter_in.readUnsignedInt();
					crc   = meter_in.readUnsignedInt();
				
					length = meter_in.readUnsignedShort();
					src    = meter_in.readInetAddress();
					dst    = meter_in.readInetAddress();
					
					// ui.debug (src+"->"+dst+" ["+length+"]");
					
					// Transport protocol
					tproto = meter_in.readByte();
					switch (tproto) {
					case 6:	// TCP
						IPv4_TCP_packet p1 = new IPv4_TCP_packet();
						p1.ts = ts.toDouble();
						p1.crc = crc;
						p1.length = length;
						p1.src = src;
						p1.dst = dst;
						p1.src_port = meter_in.readUnsignedShort();
						p1.dst_port = meter_in.readUnsignedShort();
						
						//gui.debug (src+":"+p1.src_port+" -> "+dst+":" 
						//  +p1.dst_port+" ["+crc+"]");
						  
						processPacket(p1);
						break;
					case 17: // UDP
						IPv4_UDP_packet p2 = new IPv4_UDP_packet();
						p2.ts = ts.toDouble();
						p2.crc = crc;
						p2.length = length;
						p2.src = src;
						p2.dst = dst;
						p2.src_port = meter_in.readUnsignedShort();
						p2.dst_port = meter_in.readUnsignedShort();
						
						if (analyzer != null) {
							//gui.debug (src+":"+p2.src_port+" -> "+dst+":"
						  	//+p2.dst_port+" ["+crc+"]");
						} else {
							analyzer.gui.debug ("Analyzer is null");
						}
						
						processPacket(p2);
						break;
						
					default:
						IPv4_packet p3 = new IPv4_packet();
						p3.ts = ts.toDouble();
						p3.crc = crc;
						p3.length = length;
						p3.src = src;
						p3.dst = dst;
						
						//gui.debug (src+" -> "+dst+" ["+crc+"]");
						
						// analyzer.processPacket(p3);
						break;
					}
					
					break;	// Once upon a time, I forgot this break
							// and I spent many days looking for the mistake
				
				////////////////////////////////////////////////////////////////////////////
				// IPv6
				case 6:
					ts.sec   = meter_in.readUnsignedInt();
					ts.usec  = meter_in.readUnsignedInt();
					crc   = meter_in.readUnsignedInt();
				
					length = meter_in.readUnsignedShort();
					src    = meter_in.readInet6Address();
					dst    = meter_in.readInet6Address();
					
					tproto = meter_in.readByte();
					switch (tproto) {
						case 6: // TCP
							IPv6_TCP_packet p4 = new IPv6_TCP_packet();
							p4.ts = ts.toDouble();
							p4.crc = crc;
							p4.length = length;
							p4.src = src;
							p4.dst = dst;
							p4.src_port = meter_in.readUnsignedShort();
							p4.dst_port = meter_in.readUnsignedShort();
							
							processPacket(p4);
							break;
						
						case 17: // UDP
							IPv6_UDP_packet p5 = new IPv6_UDP_packet();
							p5.ts = ts.toDouble();
							p5.crc = crc;
							p5.length = length;
							p5.src = src;
							p5.dst = dst;
							p5.src_port = meter_in.readUnsignedShort();
							p5.dst_port = meter_in.readUnsignedShort();
							
							processPacket(p5);
							break;
						
						default:
							IPv6_packet p6 = new IPv6_packet();
							p6.ts = ts.toDouble();
							p6.crc = crc;
							p6.length = length;
							p6.src = src;
							p6.dst = dst;
							
							// do not process packet
							break;
					}
					
					//gui.debug ("IPv6: "+src+" -> "+dst+" ["+length+" bytes]");
					
					break;
					
				////////////////////////////////////////////////////////////////////////////
				// Result from call to meter
				case 127: 
					result = meter_in.readByte();
					//analyzer.gui.debug("Received answer: "+result);
					notifyAnswer();
					break;
					

				////////////////////////////////////////////////////////////////////////////
				// Other protocols (STP, CDP, ARP, ...)
				case 0:
					break;
					
					
				// We should never enter this case (let me see)
				////////////////////////////////////////////////////////////////////////////
				default:
					analyzer.gui.debug("Packet unrecognized: "+proto);
					break;
			
				} // end of switch
			
				// Update time
				/*synchronized (this) {
					if (ts.toDouble()>lastTime)
						lastTime = ts.toDouble();	
				}*/
			
			} catch (IOException e) {
				// The meter has died
				analyzer.gui.debug ("Error reading from dataSocket: " + e );
				receive = false;
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void processPacket (Packet p) {
		Flow f = new Flow(this, p);
		Flow fAux = flows.getFlow(f);
		if ( f != null && fAux == null) {
			// The flow didn't exist
			flows.add(f);
			
			// resolve ip addresses (with threads, because resolving
			// is blocking)
			f.resolve();
			
			//gui.flowDataChanged(meter_id);
			//analyzer.gui.flowInserted(meter_id, flows.size()-1);

			analyzer.checkCommon (f, meter_id);
			f.processPacket (p);
			
		} else if (fAux != null) {
			// flow already exists
			fAux.processPacket(p);
		}
		
		// DEBUG
		if (p.ts - lastTime > 15) {
			analyzer.gui.debug ("Meter.processPacket: time jump");	
		}

		// already in processLoop
		if (p.ts > lastTime) {
			lastTime = p.ts;		
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized Flow getFlow (Flow f) {
		return flows.getFlow(f);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void updateTime () {
		//analyzer.gui.debug("LastTime["+meter_id+"] "+lastTime);
		lastTime++;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void removeOlderThan (int seconds) {
		flows.removeOlderThan(lastTime - seconds);	
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void timeStep () {
		flows.timeStep(lastTime);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized double getLastTime () {
		return lastTime;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// FlowTableModel methods
	////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	// get number of present flows
	public int getNumFlows () {
		return flows.size();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Flow getFlowAt (int pos) {
		return flows.getFlowAt (pos);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void clear () {
		flows.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void save (ObjectOutputStream out) {
		flows.save(out);
	}
}



