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

public class PacketSet {

	Map map; 	// hash of packets
	
	// Must study this aproach
	// Map m = Collections.synchronizedMap(new LinkedHashMap(...));

	////////////////////////////////////////////////////////////////////////////
	PacketSet () {
		map = new LinkedHashMap();
	}

	////////////////////////////////////////////////////////////////////////////
	public void add (Packet p) {
		map.put(p, p);
	}

	////////////////////////////////////////////////////////////////////////////
	public void remove (Packet p) {
		map.remove(p);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public int removeOlderThan (int time) {
		Collection values = map.values();
		Iterator i = values.iterator();
		Packet p;
		int counter = 0;
		
		while(i.hasNext()) {
			p = (Packet) i.next();
			if (p.presentTime < time) {
				i.remove();
				counter ++;
			}
		}
		
		return counter;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Packet getPacket (Packet p) {
		return (Packet) map.get(p);
	}

}
