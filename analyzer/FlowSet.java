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

import java.io.*;
import java.util.*;

// All the methods in this class are synchronized, so we can
// have concurrent access. But this is not very efficient, you
// may want to try the readers/writers paradigm. A sample can
// be found at:
// http://www.mcs.drexel.edu/~shartley/ConcProgJava/Monitors/rwmo.java

public class FlowSet {

	ArrayList al = null; // As a FIFO
	HashMap map = null;
	
	////////////////////////////////////////////////////////////////////////////
	public FlowSet () {
		al = new ArrayList(); // capacity 10
		map = new HashMap();  // capacity 16, load factor .75 
	}

	////////////////////////////////////////////////////////////////////////////
	// May have duplicated values. You'd better check before add (me no).
	public synchronized void add (Flow f) {
		if (!map.containsKey(f)) {
			al.add(f);
			map.put(f, f); // the flow has its own key (crc32)
		}
	}

	// It would be interesting (for performance purpouses) 
	// to have a check&add method, don't you think so?
	
	////////////////////////////////////////////////////////////////////////////
	// number of elements stored in data structure
	public synchronized int size () {
		return al.size();
	}
	
	////////////////////////////////////////////////////////////////////////////
	// do a flow exist in our data structure?
	public synchronized boolean existFlow (Flow f) {
		return map.containsKey (f);
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Returns a flow with the specified key (flow) in flowset
	// If it not exists, return null
	public synchronized Flow getFlow (Flow f) {
		return (Flow) map.get(f);	
	}
	
	////////////////////////////////////////////////////////////////////////////
	// get flow at given position
	public synchronized Flow getFlowAt ( int pos ) {
		if (pos<0 || pos>=al.size()) {
			return null;
		} else {
			return (Flow) al.get(pos);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void removeOlderThan (double time) {
		ListIterator li = al.listIterator();
		Flow f;
		
		while (li.hasNext()) {
			f = (Flow) li.next();
			if (f.lastTime < time && !f.monitor) {
				li.remove();
				map.remove(f);
				f.reset(); // remove common flow associated to this flow
				//analyzer.addToPool(f);
			}	
		}
	}

	////////////////////////////////////////////////////////////////////////////
	public synchronized void removeFlows () {
		ListIterator li = al.listIterator();
		Flow f;
		
		while (li.hasNext()) {
			f = (Flow) li.next();
			if (!f.monitor) {
				li.remove();
				map.remove(f);
				//f.reset(); // remove common flow associated to this flow
				//analyzer.addToPool(f);
			}	
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void timeStep (double time) {
		ListIterator li = al.listIterator();
		Flow f;
		
		while (li.hasNext()) {
			f = (Flow) li.next();
			f.timeStep(time);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void clear () {
		ListIterator li = al.listIterator();
		Flow f;
		
		while (li.hasNext()) {
			f = (Flow) li.next();
			f.clear();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized int numFlowsMonitored () {
		ListIterator li = al.listIterator();
		Flow f;
		int count = 0;
		
		while (li.hasNext()) {
			f = (Flow) li.next();
			if (f.monitor) {
				count++;
			}	
		}
		
		return count;
	}	
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void save (ObjectOutputStream out) {
		ListIterator li = al.listIterator();
		Flow f;
		
		while (li.hasNext()) {
			f = (Flow) li.next();
			if (f.monitor) {
				try {
					out.writeObject(f.reorder());
				} catch (IOException e) {
					System.out.println("FlowSet.save() "+e);
				}
			}	
		}
	}	
	
}
