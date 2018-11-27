package com.describer.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Group {
	
	private String name;
	
	private Requisite mainReq;
	
	private Key owner;
	
	private List<Group> inGroups = new ArrayList<Group>();
	
	//private List<Requisite> reqs = new ArrayList<Requisite>();
	
	private Requisite req;
	
	public Group(Requisite mainReq, Key owner) {
		this.mainReq = mainReq;		
		this.owner = owner;
	}
	
	public Requisite getMainReq() {
		return mainReq; 
	}
	
	void addInGroup(Group gr) {
		inGroups.add(gr);		
	}
	
	void removeInGroup(Group gr) {
		inGroups.remove(gr);		
	} 
	
	void setReq(Requisite req) {
		//reqs.add(req);
		this.req = req;
	}
	

	public List<Group> getInGroups() {
		return new ArrayList(inGroups);
	}
/*
	public void setInGroups(List<Group> inGroups) {
		this.inGroups = inGroups;
	}
*/
	public Requisite getReq() {
		return req;
	}
/*
	public void setReqs(List<Requisite> reqs) {
		this.reqs = reqs;
	}	
*/
	
	public Set<Group> allPathsNodes() {
		Set<Group> groups = new HashSet<Group>();
		Set<Group> nels = new HashSet<Group>();
		groups.add(this);
		do {
			nels.clear();
			for (Group g : groups) {
				nels.addAll(g.getInGroups());
			}
		} while (groups.addAll(nels));
		return groups;
	}
	
	public boolean resolve(Set<Group> without) {
		if (req != null)
			return true;
		Set<Group> s = new HashSet<Group>();
		s.addAll(without);
		s.add(this);
		for (Group g : inGroups) {
			if(g.res(s))
				return true;
		}
		return false;
	}
	
	private boolean res(Set<Group> visited) {
		if (visited.contains(this))
			return false;
		else
			visited.add(this);
		if (req != null)
			return true;
		for (Group g : inGroups) {
			if (g.res(visited))
				return true;
		}
		return false;
	}
	
	public void setName(String name) {
		this.name = name;		
	}
	
	public String getName() {
		if (name == null)
			return toString();
		else
			return name;
	}
	
	public Key getOwner() {
		return owner;
	}
	
	public String toString() {
		return owner + "-g#" + owner.grMgr.getGroups().indexOf(this);
	}
	
}

