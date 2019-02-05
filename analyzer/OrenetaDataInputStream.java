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
import java.net.*;

public class OrenetaDataInputStream extends DataInputStream {
	
	// Fucking read!!!
	// Let me explain a history about OrenetaDataInputStream. Once upon a time, there
	// was a java programmer (aka Abel), who extended DataInputStream Class to 
	// support reading of complex data structures, as Inet4 and Inet6 address.
	// He used the 'read' method of DataInput Stream, and told him to read 'x' bytes
	// of data, as 'x' was the size of the data he wanted to read.
	// But sometimes, extrange data appeared in the java application. He wondered
	// from where it came, but he didn't discovered until debugged the C server,
	// captured the network packets and finally debugged the OrenetaDataInputStream
	// Class. He spent many days and many hairs, but finally he knowed tha he must use
	// the method 'readFully', that blocks until it read 'x' bytes. He congratulated
	// himself and enjoyed it with cheers of red wine with his friends. (22-07-2003).

	////////////////////////////////////////////////////////////////////////////
	public OrenetaDataInputStream (InputStream in) {
		super(in);
	}

	////////////////////////////////////////////////////////////////////////////
	public long readUnsignedInt () throws IOException {
		byte[] b = new byte[4]; 
		readFully (b, 0, 4);
		
		return ( (long) (((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) |
  		((b[2] & 0xff) << 8) | (b[3] & 0xff)) );
	}
	
	////////////////////////////////////////////////////////////////////////////
	public InetAddress readInetAddress () throws IOException {
		byte[] b = new byte[4];
		InetAddress i;
		
		readFully (b, 0, 4);
		try { 
		    i = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
			System.out.println ("ADDRESS NOT CORRECT");
			i = null;
		}
		
		return (i);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public InetAddress readInet6Address () throws IOException {
		byte[] b = new byte[16];
		InetAddress i;
		
		readFully (b, 0, 16);
		try { 
		    i = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
			System.out.println ("ADDRESS NOT CORRECT");
			i = null;
		}
		
		return (i);
	}
}