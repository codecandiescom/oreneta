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
import java.util.zip.CRC32; // For hashcodes
import java.awt.*;
import java.util.*;
import java.io.*;

public class Flow implements ChartModel, Serializable, Cloneable {
	public double time;     // Flow creation time
	public double lastTime; // Flow last time access
	
	public InetAddress src;
	public InetAddress dst;
	public int src_port;
	public int dst_port;
	
	public transient boolean monitor;  // monitor this flow?
	int monitor_length;      // size of monitor data (in seconds)
	public transient boolean isCommon;   // is this a common flow?
	public transient boolean isSnapshot; // is a recovered flow?
	public transient CommonFlow cf;      // belongs to this common flow
	Color color;
	
	private transient Meter meter;
	int meter_id;          // Necessary for equal()
	
	//transient Thread resolveThread;
	String srcString;
	String dstString;
	boolean isSrcResolved;
	boolean isDstResolved;
		
	int [] throughput;     // Throughput
	double [] ipd_avg;    // Average IPD
	double [] ipd_max;    // Max IPD
	double [] ipd_min;    // Min IPD
	int [] pps;            // packets per second
	int [] psize_max;      // max packet size per second
	int [] psize_min;      // min packet size per second
	
	long total_bytes;
	long total_packets;
	
	
	////////////////////////////////////////////////////////////////////////////
	public Flow (Meter meter, Packet p) {
		this.meter = meter;
		meter_id = meter.meter_id;
		
		color = new Color (new Random().nextInt());
		
		isCommon = false;
		isSnapshot = false;
		cf = null;
		
		monitor_length = meter.monitor_length;
		throughput = new int [monitor_length];
		ipd_avg = new double [monitor_length];
		ipd_max = new double [monitor_length];
		ipd_min = new double [monitor_length];
		pps = new int [monitor_length];
		psize_max = new int [monitor_length];
		psize_min = new int [monitor_length];
		
		total_bytes = 0;
		total_packets = 0;
		
		if (p instanceof IPv4_TCP_packet) {
			IPv4_TCP_packet p4t = (IPv4_TCP_packet) p;
			set(p4t.src, p4t.dst, p4t.src_port, p4t.dst_port, p4t.ts);
		} else if (p instanceof IPv4_UDP_packet) {
			IPv4_UDP_packet p4u = (IPv4_UDP_packet) p;
			set(p4u.src, p4u.dst, p4u.src_port, p4u.dst_port, p4u.ts);
		} else if (p instanceof IPv6_TCP_packet) {
			IPv6_TCP_packet p6t = (IPv6_TCP_packet) p;
			set(p6t.src, p6t.dst, p6t.src_port, p6t.dst_port, p6t.ts);
		} else if (p instanceof IPv6_UDP_packet) {
			IPv6_UDP_packet p6u = (IPv6_UDP_packet) p;
			set(p6u.src, p6u.dst, p6u.src_port, p6u.dst_port, p6u.ts);
		}
		
		isSrcResolved = false;
		isDstResolved = false;

	}
	
	////////////////////////////////////////////////////////////////////////////
	/*public Flow (InetAddress src, InetAddress dst,
	             int src_port, int dst_port, double time) {
		this.src = src;
		this.dst = dst;
		this.src_port = src_port;
		this.dst_port = dst_port;
		this.time = time;
		this.lastTime = time;
		this.monitor = false;
		this.color = new Color (hashCode());
		//throughput = new int [Oreneta.MONITOR_LENGTH];
		//max_throughput = 0;
		isCommon = false;
		cf = null;
	}*/

	////////////////////////////////////////////////////////////////////////////
	public void set (InetAddress src, InetAddress dst,
	                 int src_port, int dst_port, double time) {
		this.src = src;
		this.dst = dst;
		this.src_port = src_port;
		this.dst_port = dst_port;
		this.time = time;
		this.lastTime = time;
		this.monitor = false;
		this.color = new Color (hashCode());
		isCommon = false;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void reset () {
	//	time = 0;
	//	lastTime = 0;
	//	src = null;
	//	dst = null;
	//	src_port = 0;
	//	dst_port = 0;
	//	
		if (cf != null) cf.reset();
	//	
	//	for (int i=0; i<monitor_length; i++)
	//		throughput[i] = 0;
	}
	
	// Method used in chart to add or remove chart models (=flows)
	////////////////////////////////////////////////////////////////////////////
	public boolean equals ( Object arg ) {
		Flow f = null;
		
		if ( (arg != null) && (arg instanceof Flow) ) {
			f = (Flow) arg;
			if (f.src.equals(src) && f.dst.equals(dst) &&
			    f.src_port == src_port && f.dst_port == dst_port &&
			    f.isSnapshot == isSnapshot /*&& f.meter_id == meter_id*/) {
			    return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	// Chart Model - Necessary to remove correctly drawn flows
	////////////////////////////////////////////////////////////////////////////
	public boolean extraEquals (Object o) {
		Flow f = (Flow) o;
		if (this.equals(f) && f.meter_id == meter_id)
			return true;
		else
			return false;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public int hashCode () {
		CRC32 crc = new CRC32();
		crc.update (src.hashCode());
		crc.update (dst.hashCode());
		crc.update (src_port);
		crc.update (dst_port);
		return (int) crc.getValue();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void processPacket (Packet p) {
		int offset;
		
		// Zero arrays
		if ((int)p.ts != (int)lastTime) {
			for(int i=(int)lastTime+1; i<(int)p.ts 
			    && i<=monitor_length+(int)lastTime; i++) {
			    	offset = i%monitor_length;
				throughput[offset] = 0;
				ipd_avg[offset]   = 0;
				ipd_max[offset] = 0;
				ipd_min[offset] = Double.MAX_VALUE;
				pps[offset] = 0;
				psize_max[offset] = 0;
				psize_min[offset] = 0;
			}	
		}
		
		offset = ((int)p.ts)%monitor_length;
		
		// Update packets per second
		pps[offset]++;
		
		// Update total packets
		total_packets++;
		
		// Update total bytes
		total_bytes += p.length;
		
		// Update throughput and packet size
		if ((int)p.ts != (int)lastTime) {
			throughput[offset] = p.length;
			psize_max[offset] = p.length;
			psize_min[offset] = p.length;
		} else {
			//meter.analyzer.gui.debug("flow.processPacket(), (int)p.ts 
			//= "+p.ts+", module = "+(int)p.ts%monitor_length);
			throughput[offset] += p.length;
			if (p.length > psize_max[offset]) psize_max[offset] = p.length;
			if (p.length < psize_min[offset]) psize_min[offset] = p.length;
		}
		
		// Update IPD
		double ipd = p.ts - lastTime;
		
		if (ipd > meter.analyzer.prefs.max_ipd) { 
			// More than 2.0 seconds since last packet
			// do nothing
		} else {
			ipd_avg[offset] += ipd; 
			
			if (ipd > ipd_max[offset]) ipd_max[offset] = ipd;
			if (ipd_min[offset] > ipd) ipd_min[offset] = ipd;
		}
		
		
		// Update last time access
		lastTime = p.ts;
		
		// Update common flow (if applicable)
		if (isCommon) {
			cf.processPacket(p);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void timeStep (double time) {
		if ((int)lastTime != (int)time) {
			int offset = ((int)time)%monitor_length;
			throughput[offset] = 0;
			ipd_avg[offset]   = 0;
			ipd_max[offset]   = 0;
			ipd_min[offset]   = Double.MAX_VALUE;
			pps[offset] = 0;
			psize_max[offset] = 0;
			psize_min[offset] = 0;
		}		
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void clear () {
		for (int i=0; i<monitor_length; i++) {
			throughput[i] = 0;
			ipd_avg[i] = 0;
			ipd_max[i] = 0;
			ipd_min[i] = Double.MAX_VALUE;
			pps[i] = 0;
			psize_max[i] = 0;
			psize_min[i] = 0;
		}
		
		total_bytes = 0;
		total_packets = 0;
	}
	
	// Methods for resolving IP to names with threads
	////////////////////////////////////////////////////////////////////////////
	/*public void resolveStart () {
		resolveThread = new Thread() { public void run() { resolve(); } }; 
		resolveThread.start();	
	}*/
	
	////////////////////////////////////////////////////////////////////////////
	public void resolve () {
		new Thread () {
			public void run () {
				srcString = src.getCanonicalHostName();
				isSrcResolved = true;
				dstString = dst.getCanonicalHostName();
				isDstResolved = true;
			}
		}.start();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public String getSrc () {
		if (isSrcResolved)
			return srcString;
		else
			return src.toString().substring(1);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public String getDst () {
		if (isDstResolved)
			return dstString;
		else
			return dst.toString().substring(1);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public String getSrcService () {
		return Analyzer.getService(src_port);
	}

	////////////////////////////////////////////////////////////////////////////
	public String getDstService () {
		return Analyzer.getService(dst_port);
	}
	
	// Method called in deserialization process
	////////////////////////////////////////////////////////////////////////////
	private void readObject (ObjectInputStream s)
		throws IOException, ClassNotFoundException {
			
		s.defaultReadObject();
		isSnapshot = true;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Object clone() {
		try {
			Flow f = (Flow) super.clone();
			
			f.throughput = (int []) throughput.clone();
			f.ipd_avg = (double []) ipd_avg.clone();
			f.ipd_max = (double []) ipd_max.clone();
			f.ipd_min = (double []) ipd_min.clone();
			f.pps = (int []) pps.clone();
			f.psize_max = (int []) psize_max.clone();
			f.psize_min = (int []) psize_min.clone();
			
			return f;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Flow reorder () {
		int offset = ((int)meter.getLastTime())%monitor_length;
		int aux = monitor_length - offset - 1;
		
		Flow f = (Flow) this.clone();
		
		for (int i=0; i<monitor_length; i++) {
			if (i >= aux) {
				f.throughput[i] = throughput[i - aux];
				f.pps[i] = pps[i - aux];
				f.psize_max[i] = psize_max[i - aux];
				f.psize_min[i] = psize_min[i - aux];
				f.ipd_avg[i] = ipd_avg[i - aux];
				f.ipd_max[i] = ipd_max[i - aux];
				f.ipd_min[i] = ipd_min[i - aux];
			} else {
				f.throughput[i] = throughput[i + offset + 1];
				f.pps[i] = pps[i + offset + 1];
				f.psize_max[i] = psize_max[i + offset + 1];
				f.psize_min[i] = psize_min[i + offset + 1];
				f.ipd_avg[i] = ipd_avg[i + offset + 1];
				f.ipd_max[i] = ipd_max[i + offset + 1];
				f.ipd_min[i] = ipd_min[i + offset + 1];
			}
		}		
		return f;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Chart Model Methods
	////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	public int getNumHorzDiv () {
		return monitor_length;
	}

	////////////////////////////////////////////////////////////////////////////
	public double getValue (int x, int param, int subparam) {
		int offset = 0;
		int aux = 0;
		double result=0;
		
		// Calculate offset
		if (!isSnapshot) {
			offset = ((int)meter.getLastTime())%monitor_length;
			aux = monitor_length - offset - 1;
		}
			
		switch (param) {
		
		// Throughput
		case 0:
			if (isSnapshot) result = throughput[x] * 8;
			else if (x >= aux) result = throughput[x - aux] * 8; // in bits (bytes*8)
			else result = throughput[x + offset + 1] * 8; // in bits (bytes*8)
			break;
			
		// Packets per second
		case 1:
			if (isSnapshot) result = pps[x];
			else if (x >= aux) result = pps[x - aux];
			else result = pps[x + offset + 1];
			break;
		
		// IPD
		case 2:
			if (isSnapshot) result = ipd_avg[x] / pps[x];
			else if (x >= aux) result = ipd_avg[x - aux] / pps[x - aux];
			else result = ipd_avg[x + offset + 1] / pps[x + offset + 1];
			break;
			
		// Average packet size
		case 3:
			if (isSnapshot && pps[x]>0)
				result = throughput[x] / pps[x];
			else if (x >= aux) {
				if (pps[x-aux] > 0)
					result = throughput[x - aux] / pps[x - aux];
				else
					result = 0;
					
			} else if (pps[x+offset+1] > 0)
				result = throughput[x + offset + 1] / pps[x + offset + 1];
			else
				result = 0;
			break;

		// MaxMin packet size
		case 4:
			switch (subparam) {
			// Average
			case 0:
				if (isSnapshot && pps[x]>0)
					result = throughput[x] / pps[x];
				else if (x >= aux) {
					if (pps[x-aux] > 0)
						result = throughput[x - aux] / pps[x - aux];
					else
						result = 0;
						
				} else if (pps[x+offset+1] > 0)
					result = throughput[x + offset + 1] / pps[x + offset + 1];
				else
					result = 0;
				break;
				
			// Max
			case 1:
				if (isSnapshot) result = psize_max[x];
				else if (x >= aux) result = psize_max[x - aux];
				else result = psize_max[x + offset + 1];
				break;
				
			// Min
			case 2:
				if (isSnapshot) result = psize_min[x];
				else if (x >= aux) result = psize_min[x - aux];
				else result = psize_min[x + offset + 1];
				break;	
			}
			break;
			
		// MaxMin IPD
		case 5:
			switch (subparam) {
			// Average
			case 0:
				if (isSnapshot && pps[x]>0)
					result = ipd_avg[x] / pps[x];
				else if (x >= aux) {
					if (pps[x-aux] > 0)
						result = ipd_avg[x - aux] / pps[x - aux];
					else
						result = 0;
						
				} else if (pps[x+offset+1] > 0)
					result = ipd_avg[x + offset + 1] / pps[x + offset + 1];
				else
					result = 0;
				break;
				
			// Max
			case 1:
				if (isSnapshot) result = ipd_max[x];
				else if (x >= aux) result = ipd_max[x - aux];
				else result = ipd_max[x + offset + 1];
				break;
				
			// Min
			case 2:
				if (isSnapshot) result = ipd_min[x];
				else if (x >= aux) result = ipd_min[x - aux];
				else result = ipd_min[x + offset + 1];
				
				if (result == Double.MAX_VALUE) result = 0;
				break;	
			}
			break;
			
		} // end switch(param)
		
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	public double getMaxValue (int param) {
		double max=0;
		
		switch (param) {
			
		// Throughput
		case 0:
			for (int i=0; i<monitor_length; i++) {
				if (throughput[i]*8>max) {
					max = throughput[i]*8; // in bits (bytes*8)
				}
			}
			break;
			
		// Packets per second
		case 1:
			for (int i=0; i<monitor_length; i++) {
				if (pps[i]>max) {
					max = pps[i];
				}
			}
			break;
		
		// IPD
		case 2:
			for (int i=0; i<monitor_length; i++) {
				if (pps[i]!=0 && ipd_avg[i]/pps[i]>max) {
					max = ipd_avg[i]/pps[i];
				}
			}
			break;
		
		// Average packet size
		case 3:
			for (int i=0; i<monitor_length; i++) {
				if (pps[i]!=0 && throughput[i]/pps[i]>max) {
					max = throughput[i]/pps[i];
				}
			}
			break;
			
		// Max packet size
		case 4:
			for (int i=0; i<monitor_length; i++) {
				if (psize_max[i]>max) {
					max = psize_max[i];
				}
			}
			break;
			
		// Max IPD
		case 5:
			for (int i=0; i<monitor_length; i++) {
				if (ipd_max[i]>max) {
					max = ipd_max[i];
				}
			}
			break;
		}
		
			
		//analyzer.gui.debug("getMaxValue("+param+"): "+max);
		return max;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Color getColor () {
		return color;
	}

}
