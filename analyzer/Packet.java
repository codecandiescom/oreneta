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

public class Packet {

	public double ts;
	public long crc;
	public int length;
	public InetAddress src;
	public InetAddress dst;
	public int presentTime; // used in commonFlow & packetSet to calculate loss
	
	////////////////////////////////////////////////////////////////////////////
	Packet () {
	}
	
	////////////////////////////////////////////////////////////////////////////
	public int hashCode () {
		return (int) crc;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public boolean equals ( Object arg ) {
		Packet p = null;

		if ( (arg != null) && (arg instanceof Packet) ) {
			p = (Packet) arg;
			if (p.crc == crc)
				return true;
		}

		return false;
	}

}
