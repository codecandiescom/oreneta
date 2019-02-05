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

// It's also a table model
import java.util.*;
import javax.swing.table.*;
import java.io.*;

public class CommonFlowSet  {
	ArrayList al;
	
	////////////////////////////////////////////////////////////////////////////
	CommonFlowSet () {
		al = new ArrayList();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void add (CommonFlow cf) {
		al.add(cf);
		//fireTableRowsInserted (al.size()-1, al.size()-1);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void remove (CommonFlow cf) {
		int index = al.indexOf(cf);
		if (index >= 0) {
			al.remove(index);
			//fireTableRowsDeleted (index, index);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void timeStep () {
		ListIterator li = al.listIterator();
		CommonFlow cf;
		
		while (li.hasNext()) {
			cf = (CommonFlow) li.next();
			cf.timeStep();
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////
	public synchronized void clear () {
		ListIterator li = al.listIterator();
		CommonFlow cf;
		
		while (li.hasNext()) {
			cf = (CommonFlow) li.next();
			cf.clear();
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////
  	public synchronized void removeCommonFlows() {
  		ListIterator li = al.listIterator();
		CommonFlow cf;
		
		while (li.hasNext()) {
			cf = (CommonFlow) li.next();
			if (!cf.monitor) li.remove();
		}		
  	}
  	
  	////////////////////////////////////////////////////////////////////////////
  	public synchronized int numCommonFlowsMonitored () {
  		ListIterator li = al.listIterator();
		CommonFlow cf;
		int count = 0;
		
		while (li.hasNext()) {
			cf = (CommonFlow) li.next();
			if (cf.monitor) count++;
		}		
		
		return count;
  	}
  	
  	////////////////////////////////////////////////////////////////////////////
  	public synchronized CommonFlow getCommonFlowAt (int pos) {
  		return (CommonFlow) al.get(pos);
  	}
  	
  	////////////////////////////////////////////////////////////////////////////
  	public synchronized int getNumCommonFlows () {
  		return al.size();
  	}
  	
  	////////////////////////////////////////////////////////////////////////////
  	public synchronized void save (ObjectOutputStream out) {
  		ListIterator li = al.listIterator();
		CommonFlow cf;
		
		while (li.hasNext()) {
			cf = (CommonFlow) li.next();
			try {
				if (cf.monitor) out.writeObject(cf);
			} catch (IOException e) {
				System.out.println ("CommonFlowSet.save() "+e);
			}
		}		
  	}


}
