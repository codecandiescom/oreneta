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

public class Snapshot {
	public FlowSet fs;
	public CommonFlowSet cfs;
	public Analyzer analyzer;
	
	////////////////////////////////////////////////////////////////////////////
	public Snapshot (Analyzer analyzer) {
		this.analyzer = analyzer;
		fs = new FlowSet ();
		cfs = new CommonFlowSet ();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public int getNumSnapshotFlows () {
		return fs.size();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public int getNumSnapshotCommonFlows () {
		return cfs.getNumCommonFlows();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public Flow getSnapshotFlowAt (int pos) {
		return fs.getFlowAt(pos);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public CommonFlow getSnapshotCommonFlowAt (int pos) {
		return cfs.getCommonFlowAt(pos);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void drawSnapshotFlow (Flow f) {
		analyzer.drawFlow(f);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void undrawSnapshotFlow (Flow f) {
		analyzer.undrawFlow(f);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void drawSnapshotCommonFlow (CommonFlow cf) {
		analyzer.drawCommonFlow(cf);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void undrawSnapshotCommonFlow (CommonFlow cf) {
		analyzer.undrawCommonFlow(cf);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void add (Flow f) {
		fs.add(f);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void add (CommonFlow cf) {
		cfs.add(cf);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void delete_flows() {
		fs.removeFlows();	
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void delete_common() {
		cfs.removeCommonFlows();
	}
		
}