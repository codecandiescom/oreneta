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

public class Preferences {
	
	// For if the flies (= maybe we need it later)
	Analyzer analyzer;
	
	// IPD values over this one (seconds) are dropped
	public double max_ipd = 2.0;
	
	// How many time (seconds) to wait since the last flow's packet has arrived
	// After this time, remove the flow (if not monitored)
	// Not recommended values below 10 seconds (too much overhead)
	// Not recommended values over 300 seconds (too many memory to keep them all)
	public int flows_holdtime = 30;
	
	// How many time (seconds) we keep a packet waiting for his match
	// After this time, remove the packet (count as packet loss)
	// Not recommneded values below 2 seconds
	public int packet_holdtime = 5;
	
	////////////////////////////////////////////////////////////////////////////
	public Preferences (Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	
}
