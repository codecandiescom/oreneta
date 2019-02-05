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

import java.util.*;
import java.io.*;

public class Analyzer {
	
	//FlowSet [] flows;
	//LinkedList flowPool = null;
	public GUI gui = null;
	public double lastTime;
	public CommonFlowSet common_fs;
	public Snapshot snapshot;
	public Preferences prefs;
	
	public Meter [] meter;	// Model

	////////////////////////////////////////////////////////////////////////////
	public Analyzer (GUI gui) {
		this.gui = gui;
  		
  		// Meters init
  		meter = new Meter[Oreneta.NUM_METERS];
  		for (int i=0;i<Oreneta.NUM_METERS;i++) {
  			meter[i] = new Meter (this, i);  // Model
  		}
  		
  		// Common flows init
  		common_fs = new CommonFlowSet(/*this*/);
  		
  		// Snapshot
  		snapshot = new Snapshot(this);
  		
  		// Preferences
  		prefs = new Preferences(this);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void checkCommon (Flow f, int meter_id) {
		Flow fAux = null;
		boolean areAllCommon = true;
		Flow [] flowArray = new Flow[Oreneta.NUM_METERS];
		
		flowArray[meter_id] = f;
		for (int i=0; i<Oreneta.NUM_METERS && areAllCommon; i++) {
			if (i != meter_id) {
				
				// The getFlow method of the FlowSet class checks
				// wether a flow exists or not. Two flows with the
				// same source and destination addresses and ports
				// have the same hashcode.
				fAux = meter[i].getFlow(f);
				if (fAux == null || fAux.isCommon == true) {
					areAllCommon = false;
				} else {
					flowArray[i] = fAux;
				}
			}
		}
		
		if (areAllCommon) {
			// The flow has been recently created, there was no CommonFlow
			// associated with it, so create a new one
			CommonFlow cf = new CommonFlow((Flow [])flowArray.clone(), common_fs, this);
			common_fs.add( cf ); // Add common flow to the common flow set

			// Tell every flow wich commonFlow belongs to
			for (int i=0; i<Oreneta.NUM_METERS; i++) {
				flowArray[i].cf = cf;
				flowArray[i].isCommon = true;
			}
			
			//gui.debug ("Common flow created" +cf.flows[0].src_port+" " +
			//	cf.flows[1].src_port);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void removeOldFlows (int seconds) {
		/*for (int i=0; i<Oreneta.NUM_METERS; i++) {
			flows[i].removeOlderThan (gui.meter[i].lastTime - (double)seconds, this);
			//gui.flowDataChanged(i);
		}*/
		
		for (int i=0; i<Oreneta.NUM_METERS; i++) {
			meter[i].removeOlderThan (seconds);
		}	
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void timeStep () {
		
		// Delete old flows
		removeOldFlows(prefs.flows_holdtime); 
				
		for (int i=0; i<Oreneta.NUM_METERS; i++) {
			//flows[i].timeStep (lastTime);
			meter[i].timeStep ();
		}	
		common_fs.timeStep();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void updateTime () {
		//lastTime++;
		for (int i=0; i<Oreneta.NUM_METERS; i++) {
			meter[i].updateTime();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void drawFlow (Flow f) {
		gui.drawFlow(f);
	}

	////////////////////////////////////////////////////////////////////////////
	public void undrawFlow (Flow f) {
		gui.undrawFlow(f);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void drawCommonFlow (CommonFlow cf) {
		gui.drawCommonFlow(cf);
	}

	////////////////////////////////////////////////////////////////////////////
	public void undrawCommonFlow (CommonFlow cf) {
		gui.undrawCommonFlow(cf);
		cf.check(); // can the common flow be removed?
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void clear () {
		for (int i=0; i<Oreneta.NUM_METERS; i++)
			meter[i].clear();
		
		common_fs.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void load (File file) {
		try {
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			gui.debug ("analyzer.load() - init");
			
			int numFlows = in.readInt();
			
			gui.debug ("analyzer.load() - after read numFlows");
			
			for(int i=0; i<numFlows; i++) {
				snapshot.add((Flow)in.readObject());
			}
			
			gui.debug ("analyzer.load() - after read Flows");
			
			int numCommonFlows = in.readInt();	
			
			gui.debug ("analyzer.load() - after read numCommonFlows");
			
			for(int i=0; i<numCommonFlows; i++) {
				snapshot.add((CommonFlow)in.readObject());
			}
			
			gui.debug ("analyzer.load() - after read CommonFlows");
			
			in.close();
			fileIn.close();
			
		} catch (Exception e) {
			gui.debug("analyzer.load() "+e);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void save (File file) {
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
		
			// Count flows in monitor state
			int count = 0;
			for (int i=0; i<Oreneta.NUM_METERS; i++)
				count += meter[i].flows.numFlowsMonitored();
			out.writeInt(count);
			
			// Write that flows
			for(int i=0; i<Oreneta.NUM_METERS; i++)
				meter[i].save(out);
			
			// Count common flows in monitor state
			out.writeInt(common_fs.numCommonFlowsMonitored());
			
			// Write that common flows
			common_fs.save(out);
			
			out.close();
			fileOut.close();

		} catch (Exception e) {
			gui.debug("analyzer.save() "+e);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void delete_snapshot_flows () {
		snapshot.delete_flows();	
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void delete_snapshot_common () {
		snapshot.delete_common();	
	}
	
	// Utils for Flows & Common Flows
	////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	public static String getService (int port) {
		switch (port) {
			//case 0:	return new String ("");
			case 7:		return new String ("echo");
			case 9:		return new String ("discard");
			case 11:	return new String ("systat");
			case 13:	return new String ("daytime");
			case 17:	return new String ("qotd");
			case 20:	return new String ("ftp-data");
			case 21:	return new String ("ftp");
			case 22:	return new String ("ssh");
			case 23:	return new String ("telnet");
			case 25:	return new String ("smtp");
			case 37:	return new String ("time");
			case 53:	return new String ("domain");
			case 63:	return new String ("whois++");
			case 67:	return new String ("bootps");
			case 68:	return new String ("bootpc");
			case 69:	return new String ("tftp");
			case 70:	return new String ("gopher");
			case 79:	return new String ("finger");
			case 80:	return new String ("http");
			case 88:	return new String ("kerberos");
			case 110:	return new String ("pop3");
			case 111:	return new String ("sunrpc");
			case 115:	return new String ("sftp");
			case 119:	return new String ("nntp");
			case 123:	return new String ("ntp");
			case 137:	return new String ("netbios-ns");
			case 138:	return new String ("netbios-dgm");
			case 139:	return new String ("netbios-ssn");
			case 143:	return new String ("imap");
			case 161:	return new String ("snmp");
			case 162:	return new String ("snmptrap");
			case 179:	return new String ("bgp");
			case 194:	return new String ("irc");
			case 389:	return new String ("ldap");
			case 443:	return new String ("https");
			case 445:	return new String ("microsoft-ds");
			case 514:	return new String ("syslog");
			case 554:	return new String ("rtsp");
			case 993:	return new String ("imaps");
			case 7777:	return new String ("oreneta");
			
			default:	return Integer.toString(port);
		}
	}
	
}
