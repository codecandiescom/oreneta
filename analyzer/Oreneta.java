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

public class Oreneta {
	
	// Debug or not debug?
	// Change and recompile to see/hide debug info
	public final static boolean DEBUG = true;
	
	// Number of meters to use
	public final static int NUM_METERS = 2;
	
	// Amount in seconds of monitoring activity
	public final static int MONITOR_LENGTH = 300;

	// Size of flow pool
	public final static int POOL_SIZE = 50;

	////////////////////////////////////////////////////////////////////////////
	public static void main (String args[] ) {
	
		// UI textUI = new UI ();
		// textUI.start();
		
		GUI gui = new GUI ();
		gui.start();

	}
}
