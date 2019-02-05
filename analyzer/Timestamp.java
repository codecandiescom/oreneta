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

public class Timestamp {
	public long sec;	// seconds
	public long usec;	// microseconds

	////////////////////////////////////////////////////////////////////////////
	Timestamp () {
		sec = 0;
		usec = 0;
	}
	
	////////////////////////////////////////////////////////////////////////////
	Timestamp (long sec, long usec) {
		this.sec = sec;
		this.usec = usec;
	}

	////////////////////////////////////////////////////////////////////////////
	public boolean greaterThan (Timestamp ts) {
		if (sec > ts.sec)
			return true;
		else if ( sec == ts.sec && usec > ts.usec)
			return true;
		else
			return false;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public boolean greaterThan (long sec, long usec) {
		if (this.sec > sec)
			return true;
		else if ( this.sec == sec && this.usec > usec)
			return true;
		else
			return false;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Timestamp less (Timestamp ts) {
		if (usec > ts.usec)
			return new Timestamp (sec - ts.sec, usec - ts.usec);
		else
			return new Timestamp (sec - ts.sec - 1, usec - ts.usec + 1000000);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Timestamp plus (Timestamp ts) {
		if (usec + ts.usec > 1000000)
			return new Timestamp (sec + ts.sec + 1, usec + ts.usec - 1000000);
		else
			return new Timestamp (sec + ts.sec, usec + ts.usec);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void reset () {
		sec = 0;
		usec = 0;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public String toString() {
		String str = new String();
		
		str = Long.toString(sec);
		if (usec > 0) {
			str += ".";

			if (usec < 100000) str += "0";
			if (usec < 10000) str += "0";
			if (usec < 1000) str += "0";
			if (usec < 100) str += "0";
			if (usec < 10) str += "0";
			str += Long.toString(usec);
		}
		return str;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public double toDouble() {
		return sec + ((float)usec / 1000000.0);
	}

}
