package org.spatiallibs.jts.util;

public class TreeNode {
	int gid;
	Object obj;
	public TreeNode(int gid, Object obj) {
		this.gid = gid;
		this.obj = obj;
	}

	public int getID() { return this.gid; }
	public Object getObject() { return this.obj ;}
}
