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
import java.awt.*;
import java.util.*;
import java.io.*;

public class CommonFlow implements ChartModel, Serializable {

	public transient Flow [] flows;
	transient PacketSet ps;
	transient CommonFlowSet cfs;
	transient Analyzer analyzer;
	int monitor_length;
	transient public boolean monitor;
	transient boolean canBeRemoved;
	double time;
	double lastTime;
	int presentTime;
	Color color;
	
	String srcString;
	String dstString;
	boolean isSrcResolved;
	boolean isDstResolved;
	
	public InetAddress src;
	public InetAddress dst;
	public int src_port;
	public int dst_port;
	
	// Statistics
	public double owd_global_max;
	public double owd_global_min;
	double lastOwd;       // last owd (for IPDV)
	double [] owd_avg;    // Average OWD (in a second)
	double [] owd_max;    // Max OWD (in a second)
	double [] owd_min;    // Min OWD (in a second)
	double [] ipdv_avg;   // Average IPDV (in a second)
	double [] ipdv_max;   // Max IPDV (in a second)
	double [] ipdv_min;   // Min IPDV (in a second)
	int [] owd_count;     // packets per second (also used as ipdv_count)
	int [] ploss;         // Packet loss
	
	////////////////////////////////////////////////////////////////////////////
	/*CommonFlow () {
		flows = new Flow[Oreneta.NUM_METERS];
		ps = new PacketSet();
		cfs = null;
		owd_max = 0;
		owd_min = Double.MAX_VALUE;
		
		monitor_length = Oreneta.MONITOR_LENGTH;
		owd_avg = new double [monitor_length];
		owd_count = new int [monitor_length];
		
		monitor = false;
		canBeRemoved = false;
		lastTime = 0;
	}*/
	
	////////////////////////////////////////////////////////////////////////////
	CommonFlow (Flow [] flows, CommonFlowSet cfs, Analyzer analyzer) {
		this.flows = flows;
		ps = new PacketSet();
		this.cfs = cfs;
		this.analyzer = analyzer;
		owd_global_max = 0;
		owd_global_min = Double.MAX_VALUE;
		lastOwd = Double.MAX_VALUE;
		
		monitor_length = Oreneta.MONITOR_LENGTH;
		owd_avg = new double [monitor_length];
		owd_max = new double [monitor_length];
		owd_min = new double [monitor_length];
		ipdv_avg = new double [monitor_length];
		ipdv_max = new double [monitor_length];
		ipdv_min = new double [monitor_length];
		for(int i=0;i<monitor_length;i++) {
			owd_min[i] = Double.MAX_VALUE;
			ipdv_max[i] = Double.MIN_VALUE;
			ipdv_min[i] = Double.MAX_VALUE;
		}
		owd_count = new int [monitor_length];
		ploss = new int [monitor_length];
		
		monitor = false;
		canBeRemoved = false;
		time = 0;
		lastTime = 0;
		presentTime = 0;
		
		src = flows[0].src;
		dst = flows[0].dst;
		src_port = flows[0].src_port;
		dst_port = flows[0].dst_port;
		color = flows[0].color;
		time = flows[0].lastTime;
		
		isSrcResolved = false;
		isDstResolved = false;
		resolve();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void reset () {
		// Tell all the flows not to reference anymore
		// this common flow
		for (int i=0; i<Oreneta.NUM_METERS; i++) {
			flows[i].cf = null;
			flows[i].isCommon = false;
		}
		
		if (!monitor)
			cfs.remove(this);
		else
			canBeRemoved = true;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void check () {
		// Method called when this common flow is undrawed
		// check if we can remove it (and remove it)
		if (canBeRemoved)
			cfs.remove(this);
	}

	////////////////////////////////////////////////////////////////////////////
	public synchronized void processPacket (Packet p) {
		Packet pAux;
		double ts;
		double ipdv;
		int offset;

		//System.out.println ("COMMON FLOW: processPacket");
		
		pAux = ps.getPacket (p);
		if (pAux == null) {
			if (presentTime == 0) presentTime = (int) lastTime;
			
			// the packet didn't exist
			p.presentTime = presentTime;
			ps.add(p);
		} else {
			ts = owd (p, pAux); // calculate owd, also update lasttime
			offset = (int)lastTime%monitor_length;
						
			if (ts > owd_global_max) owd_global_max = ts;
			if (owd_global_min > ts) owd_global_min = ts;
			
			if(ts > owd_max[offset]) owd_max[offset] = ts;
			if(owd_min[offset] > ts) owd_min[offset] = ts;
			
			owd_avg[offset] += ts; 
			owd_count[offset]++;
			
			// Calculate IPDV
			if (lastOwd != Double.MAX_VALUE) {
				ipdv = lastOwd - ts; // easy, don't you think so?
				//analyzer.gui.debug("ipdv: "+ipdv);
				
				ipdv_avg[offset] += ipdv;
				if (ipdv_max[offset] < ipdv) ipdv_max[offset] = ipdv;
				if (ipdv_min[offset] > ipdv) ipdv_min[offset] = ipdv;
			}	
			
			// Update last owd
			lastOwd = ts;
				
			
			// Remove packet first from packet set, we don't need it anymore
			// the second packet is never stored. The hash key of the second
			// packet (p) will remove the first packet on packet set.
			ps.remove(p);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// OWD - One Way Delay
	private double owd (Packet p1, Packet p2) {
		if (p1.ts > p2.ts) {
			if (p1.ts > lastTime) {
				lastTime = p1.ts;
				if ((int)lastTime > presentTime)
					presentTime = (int)lastTime;
			}
			return p1.ts - p2.ts;
			
		} else {
			if (p2.ts > lastTime) {
				lastTime = p2.ts;
				if ((int)lastTime > presentTime)
					presentTime = (int)lastTime;
			}
			return p2.ts - p1.ts;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void timeStep () {
		presentTime++;
		
		int offset = presentTime%monitor_length;	
		owd_avg[offset] = 0;
		owd_max[offset] = 0;
		owd_min[offset] = Double.MAX_VALUE;
		ipdv_avg[offset] = 0;
		ipdv_max[offset] = Double.MIN_VALUE;
		ipdv_min[offset] = Double.MAX_VALUE;
		owd_count[offset] = 0;
		
		// Packets older than 5 seconds
		ploss[offset] = ps.removeOlderThan (presentTime - analyzer.prefs.packet_holdtime);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void clear () {
		owd_global_max = 0;
		owd_global_min = 0;
		lastOwd = Double.MAX_VALUE;
		for (int i=0; i<monitor_length;  i++) {
			owd_avg[i] = 0;
			owd_max[i] = 0;
			owd_min[i] = Double.MAX_VALUE;
			ipdv_avg[i] = 0;
			ipdv_max[i] = Double.MIN_VALUE;
			ipdv_min[i] = Double.MAX_VALUE;
			owd_count[i] = 0;
			ploss[i] = 0;
		}
	}	
	
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
	
	////////////////////////////////////////////////////////////////////////////
	// Chart Model Methods
	////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	public int getNumHorzDiv () {
		return monitor_length;
	}

	////////////////////////////////////////////////////////////////////////////
	public double getValue (int x, int param, int subparam) {
		int offset;
		int aux;
		double result=0;
		
		// Calculate offset of circular vector
		offset = presentTime%monitor_length;
		aux = monitor_length - offset - 1;
		
		switch (param) {
		
		// Average OWD
		case 0:
			if (x >= aux)
				result = owd_avg[x - aux] / owd_count[x - aux];
			else
				result = owd_avg[x + offset + 1] / owd_count[x + offset + 1];
			break;
		
		// MaxMin OWD
		case 1:
			offset = presentTime%monitor_length;
			aux = monitor_length - offset - 1;
			
			switch (subparam) {
			case 0: // average
				if (x >= aux)
					result = owd_avg[x - aux] / owd_count[x - aux];
				else
					result = owd_avg[x + offset + 1] / owd_count[x + offset + 1];
				break;
			
			case 1: // max
				if (x >= aux)
					result = owd_max[x - aux];
				else
					result = owd_max[x + offset + 1];
				break;
				
			case 2: // min
				if (x >= aux)
					result = owd_min[x - aux];
				else
					result = owd_min[x + offset + 1];
				break;
			}
			break;
			
		// Packet loss
		case 2:
			if (x >= aux)
				result = ploss[x - aux];
			else
				result = ploss[x + offset + 1];
			break;	
		
		// IPDV
		case 3:		
			if (x >= aux)
				result = ipdv_avg[x - aux] / owd_count[x - aux];
			else
				result = ipdv_avg[x + offset + 1] / owd_count[x + offset + 1];
			break;
		
		
		// MaxMin IPDV
		case 4:
			switch (subparam) {
			case 0: // average
				if (x >= aux)
					result = ipdv_avg[x - aux] / owd_count[x - aux];
				else
					result = ipdv_avg[x + offset + 1] / owd_count[x + offset + 1];
				break;
			
			case 1: // max
				if (x >= aux)
					result = ipdv_max[x - aux];
				else
					result = ipdv_max[x + offset + 1];
				break;
				
			case 2: // min
				if (x >= aux)
					result = ipdv_min[x - aux];
				else
					result = ipdv_min[x + offset + 1];
				break;
			}
			break;
		}
		
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	public double getMaxValue (int param) {
		double max=0;
		double tmp;
		
		switch (param) {
		
		// Average OWD
		case 0:	
			for (int i=0; i<monitor_length; i++) {
				if (owd_avg[i]/owd_count[i]>max) {
					max = owd_avg[i]/owd_count[i];
				}
			}
			break;
			
		// Max OWD
		case 1:
			for (int i=0; i<monitor_length; i++) {
				if (owd_max[i] > max) max = owd_max[i];
				//if (owd_avg[i]/owd_count[i]>max) {
				//	max = owd_avg[i]/owd_count[i];
				//}
			}
			break;
			
		// Packet loss
		case 2:
			for (int i=0; i<monitor_length; i++) {
				if (ploss[i]>max) {
					max = ploss[i];
				}
			}
			break;
		
		// IPDV
		case 3:
			for (int i=0; i<monitor_length; i++) {
				tmp = ipdv_avg[i]/owd_count[i];
				if (tmp < 0) tmp = -tmp;
				if (tmp > max) max = tmp;
			}
			break;
			
		// MaxMin IPDV
		case 4:
			for (int i=0; i<monitor_length; i++) {
				tmp = ipdv_max[i];
				if (tmp < 0) tmp = -tmp;
				if (tmp > max && tmp != Double.MAX_VALUE) max = tmp;
				
				tmp = ipdv_min[i];
				if (tmp < 0) tmp = -tmp;
				if (tmp > max && tmp != Double.MAX_VALUE) max = tmp;
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
	
	////////////////////////////////////////////////////////////////////////////
	public boolean extraEquals (Object o) {
		return equals(o);
	}


}





