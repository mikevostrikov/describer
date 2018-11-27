package com.describer.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

public class Key extends ContextElement{
	
	private Entity entity;
	
	private Set<Node> keyElements = new HashSet<Node>();
	
	public GroupManager grMgr = new GroupManager();
	
	private boolean grMgrEnabled = true; 

	// Map contains all attributes provided to this key by children keys as keySet
	// and according groups of equal attributes.
	// Map<AttrPath, List<AttrPath>> paths = new HashMap<AttrPath, List<AttrPath>>();
	
	protected Key(){
		type = Context.TYPE.KEY;
	}
	
	/*
	 * This ctor doesn't register this key in any contexts
	 * @param ent
	 */
	protected Key(Entity ent){
		this();
		setEntityPr(ent);
	}
	
	protected boolean addKeyElementPr(Node requisite) {
		if (grMgrEnabled)
			grMgr.processNewKeyEl(requisite);
		return keyElements.add(requisite);
	}
	/*
	private void processPathAdding(Node requisite) {
		if (requisite.isRequisite() //|| requisite.isEntity() && !((Entity) requisite).isComposite()
				) {
			List<Key> interm = new ArrayList<Key>();
			AttrPath p = new AttrPath(null, interm, requisite);
			addPathDefault(p);
		} else if (requisite.isEntity()) {
			for (Key key : ((Entity) requisite).getKeysHas()) {
				Map<AttrPath, List<AttrPath>> pathsToProcess = key.getPaths();
				Set<AttrPath> notToProcess = new HashSet<AttrPath>();
				for (AttrPath ap : pathsToProcess.keySet()) {
					if (!notToProcess.contains(ap)) {
						AttrPath a = new AttrPath(key, ap.getIntermediates(), ap.getAttribute());
						addPathDefault(a);						
					}
					if (pathsToProcess.get(ap) != null 
							&& !pathsToProcess.get(ap).isEmpty()) {
						notToProcess.addAll(pathsToProcess.get(ap));						
					}
				}
			}
		}
	}
	
	private void addPathDefault(AttrPath path) {
		boolean changed = true;
		for (AttrPath p : paths.keySet()) {
			if (p.equalsAttr(path)
					&& !p.equals(path)) {
				List<AttrPath> s = null;
				if (paths.get(p) == null) {
					s = new ArrayList<AttrPath>();
					s.add(p);
					s.add(path);
					paths.put(p, s);
				} else {
					s = paths.get(p);
					s.add(path);
				}
				paths.put(path, s);
				changed = false;
				break;
			}
			if (p.equals(path)) {
				changed = false;
				break;
			}
		}
		if (changed) {
			// Cascading
			paths.put(path, null);
			for (Key key : entity.keysIn) {
				AttrPath ap = new AttrPath(this, path.getIntermediates(), path.getAttribute());
				key.addPathDefault(ap);
			}
		}
	}
	
	public Map<AttrPath, List<AttrPath>> getPaths() {
		return new HashMap<AttrPath, List<AttrPath>>(paths);		
	}
*/
	protected boolean removeKeyElementPr(Node req) {
		return removeKeyElementsPr(java.util.Collections.singleton(req));
	}
	/*
	private void processPathRemoving(Node requisite) {
		if (requisite.isRequisite()) {
			List<Key> interm = new ArrayList<Key>();
			AttrPath p = new AttrPath(null, interm, requisite);
			removePathDefault(p);
		} else if (requisite.isEntity()) {
			for (AttrPath ap : getPaths().keySet()) {
				if (((Entity) requisite).getKeysHas().contains(ap.getSource()))
					removePathDefault(ap);
			}
		}
	}
	
	private boolean ping(Node nd, Set<Key> visited) {
		if (visited.contains(this))
			return false;
		else
			visited.add(this);
		if (keyElements.contains(nd)) {
			System.out.println(visited);
			System.out.println(this);
			return true;
		}
		for (Node n : keyElements)
			if (n instanceof Entity) {
				Entity e = (Entity) n;
				for (Key k : e.getKeysHas()) {
					if (k.ping(nd, visited) == true)
						return true;
				}					
			}
		return false;
	}
	
	private boolean resolve(AttrPath path, Set<Key> visited) {
		if (visited.contains(this) || !path.getSource().equals(this))
			return false;
		else
			visited.add(this);
		return false;
	}
	*/
	/*
	// Remove the path from this key 
	private void removePathDefault(AttrPath path) {
		boolean cascadeRemoval = true;
		boolean leafSourceDeleted = false;
		{
			//if (path.getSource() == null) { // it means that real (leaf) requisite was removed
				leafSourceDeleted = true;
				Set<Key> v = new HashSet<Key>();
				v.add(this);
				o:for (Node n : keyElements) 
					if (n instanceof Entity) {
						for (Key k : ((Entity) n).getKeysHas()) {
							if (!k.equals(path.getSource()) && k.ping(path.getAttribute(), v)) {
								leafSourceDeleted = false;
								break o;
							}																
						}						
					}			
			//}
		}
		AttrPath newFirstGrMember = null;
		
		// Stop cascading
		{
			boolean containsRelevantPaths = false;
			if (paths.containsKey(path))
				containsRelevantPaths = true;
			if (leafSourceDeleted) {
				for (AttrPath ap : paths.keySet()) {
					if (ap.getAttribute().equals(path.getAttribute()))
						containsRelevantPaths = true;
				}
			}
			if (!containsRelevantPaths)
				return;
		}
		
		List<AttrPath> pairedWith = paths.get(path);
		paths.remove(path);
		if (leafSourceDeleted) {
			removeByLeafSource(path.getAttribute());
		}
		if (pairedWith != null) {
			if (pairedWith.get(0).equals(path)) {
				// if we should update paths in keys higher
				newFirstGrMember = pairedWith.get(1); 
			}			
			pairedWith.remove(path);
			if (pairedWith.size() == 1) {
				AttrPath pair = pairedWith.iterator().next();
				// Set list to null, if there is no pair
				paths.put(pair, null);
			}
			// If it's my attribute or firstGrMember changed cascade anyway
			if (newFirstGrMember != null || leafSourceDeleted)
				cascadeRemoval = true;
			else
				cascadeRemoval = false;
		}
		// Cascading
		if (cascadeRemoval) {
			for (Key key : entity.keysIn) {
				AttrPath ap;
				//if (!leafSourceDeleted)
					ap = new AttrPath(this, path.getIntermediates(), path.getAttribute());
				//else
					//ap = new AttrPath(null, path.getIntermediates(), path.getAttribute());				
				key.removePathDefault(ap);
			}
		}
		// firstMemberChanged
		if (newFirstGrMember != null && !leafSourceDeleted) {
			for (Key key : entity.keysIn) {
				AttrPath ap = new AttrPath(this
						, newFirstGrMember.getIntermediates(), newFirstGrMember.getAttribute());
				key.addPathDefault(ap);
			}
		}
	}
	
	private void removeByLeafSource(Node source) {
		Set<AttrPath> pathsToRemove = new HashSet<AttrPath>();
		for (AttrPath ap : paths.keySet()) {
			if (ap.getAttribute().equals(source)) {
				pathsToRemove.add(ap);
			}
		}
		for (AttrPath ap : pathsToRemove) {
			paths.remove(ap);
		}
	}
	
	private void detachPath(AttrPath path) {
		if (paths.get(path) == null)
			return;
		List<AttrPath> pairedAttrs = paths.get(path);
	}
*/
	protected boolean removeKeyElementsPr(Collection<Node> req) {
		if (grMgrEnabled)
			for (Node nd : req) {
				grMgr.processKeyElRemoving(nd);
			}
		return keyElements.removeAll(req);				
	}
	
	protected void setGrMgrEnabled(boolean enabled) {
		grMgrEnabled = enabled;
	}
		
	protected void setEntityPr(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}
	
	public Set<Node> getKeyElements(){
		return new HashSet<Node>(keyElements);		
	}
		
	/**
	 * @return All key elements
	 */
	public Set<ContextElement> getDirectDependants() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		set.addAll(keyElements);
		return set;
	}
	
	public Set<ContextElement> getRelatedElements() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		set.add(entity);
		set.addAll(keyElements);
		return set;
	}
	
	public String toString(){
		return "Key#" + this.getId();// + " for " + getEntity();
	}
		
	public boolean isEmpty() {
		return keyElements.isEmpty();
	}
	
	public class GroupManager {
		
		private List<Group> groups = new ArrayList<Group>();
		
		private Map<Requisite, Group> defGroups = new HashMap<Requisite, Group>();
		
		/**
		 * Adds group without cascading
		 * @return index of added group
		 */
		protected int addGroupPr(Group g, boolean isDef) {
			if (isDef)
				defGroups.put(g.getMainReq(), g);
			groups.add(g);
			return groups.indexOf(g);
		}
				
		public void deleteGroup(Group g) {
			if (!defGroups.containsValue(g)) {	// if it's not a default group
				Group def = defGroups.get(g.getMainReq());
				for (Group gr : g.getInGroups()) {
					def.addInGroup(gr);
				}
				clean(g);				
			} else {	// it is default group
				// find candidate group for being default
				Group newDef = null;
				for (Group gr : groups) {
					if (gr.getMainReq().equals(g.getMainReq()) && gr != g) {
						newDef = gr;
						break;
					}
				}
				// Change defGroup and then delete old
				if (newDef != null) {
					defGroups.put(g.getMainReq(), newDef);
					deleteGroup(g);				
				}				
			}
		}
		
		public boolean separateInGroup(Group inGroup, Group from) {
			if (!groups.contains(from))
				return false;
			if (!from.getInGroups().contains(inGroup))
				return false;
			if (from.getInGroups().size() + ((from.getReq() != null) ? 1 : 0) == 1)
				return false;
			{
				/*Set<Group> without = new HashSet<Group>();
				without.add(from);
				if (!inGroup.resolve(without))
					return false;*/
			}
			from.removeInGroup(inGroup);
			checkAndClean(from);
			Group ngr = new Group(from.getMainReq(), Key.this);
			groups.add(ngr);
			ngr.addInGroup(inGroup);
			if (!defGroups.containsKey(ngr.getMainReq()))
				defGroups.put(ngr.getMainReq(), ngr);
			// cascade
			for (Key k : entity.keysIn) {
				k.grMgr.addNewGroupDefault(ngr);
			}
			return true;
		}
		
		public boolean moveInGroup(Group inGroup, Group from, Group to) {
			if (!groups.contains(from))
				return false;
			if (!groups.contains(to))
				return false;
			if (!from.getInGroups().contains(inGroup))
				return false;
			if (!from.getMainReq().equals(to.getMainReq()))
				return false;
			if (from.equals(to))
				return false;
			from.removeInGroup(inGroup);
			checkAndClean(from);
			to.addInGroup(inGroup);
			return true;
		}

		public void processNewKeyEl(Node nd) {
			if (nd.isRequisite()) {
				Requisite r = (Requisite) nd;
				Group defGr = defGroups.get(r);
				if (defGr != null) {
					defGr.setReq(r);
				} else {
					Group ngr = new Group(r, Key.this);
					ngr.setReq(r);
					groups.add(ngr);
					defGroups.put(r, ngr);
					for (Key k : Key.this.entity.keysIn) {
						k.grMgr.addNewGroupDefault(ngr);
					}
				}
			} else if (nd.isEntity()) {
				Entity e = (Entity) nd;
				for (Key k : e.getKeysHas()) {
					for (Group gr : k.grMgr.groups) {
						addNewGroupDefault(gr);
					}
				}
			}
		}
		
		public void processKeyElRemoving(Node nd) {
			if (nd.isRequisite()) {
				removeReq((Requisite) nd);
			} else if (nd.isEntity()) {
				Entity e = (Entity) nd;
				for (Key k : e.getKeysHas()) {
					for (Group gr : k.grMgr.groups) {
						removeInGroup(gr);
					}
				}
			}
		}
		
		// Use this method to cascade new groups creation events
		public void addNewGroupDefault(Group gr) {
			/*Set<Group> without = new HashSet<Group>();
			without.addAll(groups);
			if (!gr.resolve(without))
				return;*/
			// If there is a default group for gr's requisite
			// then add gr there
			Group defGr = defGroups.get(gr.getMainReq()); 
			if (defGr != null) {
				defGr.addInGroup(gr);
			} else {			// else create new group
				Group ngr = new Group(gr.getMainReq(), Key.this);
				groups.add(ngr);
				defGroups.put(gr.getMainReq(), ngr);
				ngr.addInGroup(gr);
				// cascade
				for (Key k : entity.keysIn) {
					k.grMgr.addNewGroupDefault(ngr);
				}
			}
		}
		
		public void removeReq(Requisite r) {
			Group groupToChange = null;
			for (Group g : groups) {
				if (g.getReq() != null && g.getReq().equals(r)) {
					groupToChange = g;
					break;
				}
			}
			if (groupToChange == null)
				return;	
			groupToChange.setReq(null);
			checkAndClean(groupToChange);
		}
		
		public void removeInGroup(Group gr) {
			Group groupToChange = null;
			for (Group g : groups) {
				if (g.getInGroups().contains(gr)) {
					groupToChange = g;
					break;
				}
			}
			if (groupToChange == null)
				return;
			groupToChange.removeInGroup(gr);
			checkAndClean(groupToChange);
		}
		
		public Group groupByInGroup(Group in) {
			for (Group g : groups) {
				for (Group gr : g.getInGroups()) {
					if (in.equals(gr))
						return g;					
				}
			}
			return null;
		}
		
		private void checkAndClean(Group gr) {
			if (!gr.resolve(new HashSet<Group>())) {
				clean(gr);
			}
		}		
				
		private void clean(Group gr) {
			groups.remove(gr);
			// defGroups reconstruction
			if (defGroups.containsValue(gr)) {
				// remove old defGroup
				defGroups.remove(gr.getMainReq());
				for (Group g : groups) {
					// set new defGroup for the requisite
					if (g.getMainReq().equals(gr.getMainReq())) {
						defGroups.put(gr.getMainReq(), g);
						break;
					}
				}
			}
			// cascade removing
			for (Key k : entity.keysIn) {
				k.grMgr.removeInGroup(gr);
			}
		}

		public List<Group> getGroups() {
			return groups;
		}
		
		public int getGroupNum(Group g) {
			return groups.indexOf(g);
		}

		public boolean isDefault(Group g) {
			return defGroups.containsValue(g);
		}
		
	}
	
}
