package com.describer.context;

import java.util.HashSet;
import java.util.Set;

public class Entity extends Node {

	private final Set<Node> children = new HashSet<Node>();
	private final Set<Key> keysHas = new HashSet<Key>();
	
	protected Entity() {
		type = Context.TYPE.ENTITY;
	}
	
	protected void addChildPr(Node child){
		children.add(child);		
	}
	
	protected void removeChildPr(Node nd) {
		children.remove(nd);				
	}
	
	protected void addKeyHasPr(Key keyHas) {
		keysHas.add(keyHas);
	}

	protected void removeKeyHasPr(Key key) {
		keysHas.remove(key);		
	}

	public Set<Node> getChildren(){
		return new HashSet<Node>(children);
	}

	public Set<Key> getKeysHas(){
		return new HashSet<Key>(keysHas);
	}

	/*
	private Entity(Context ctx, String name, int id, String semantics, boolean humble) {
		this.setCtxPr(ctx);
		this.type = Context.TYPE.ENTITY;
		// TODO �������� �������� ��������������� Id
		ctx.setIdPr(this, id);
		ctx.setNamePr(this, name);
		ctx.setSemanticsPr(this, semantics);
		if (humble) {
			ctx.addPr(this);
		} else {
			ctx.add(this);
		}
	}
	
	private Entity(Context ctx) {
		this(ctx, ctx.getUniqueName(Context.TYPE.ENTITY,
						ctx.getDefNewName(Context.TYPE.ENTITY))
				, ctx.getFreeId(Context.TYPE.ENTITY), "", false);
	}

	private Entity(Context ctx, String name) throws Exception {
		this.setCtxPr(ctx);
		this.type = Context.TYPE.ENTITY;
		try{
			ctx.setIdPr(this, ctx.getFreeId(type));
			this.setName(name);
			this.setSemantics("");
			ctx.add(this);
		} catch (Exception e) {
			throw e; 
		}
	}
	*/
	/**
	 * @return childrenNotInKeys + keysHas 
	 */
	public Set<ContextElement> getDirectDependants() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		Set<Node> childrenNotInKeys = new HashSet<Node>();
		childrenNotInKeys.addAll(children);
		for (Key key : keysHas) {
			childrenNotInKeys.removeAll(key.getKeyElements());
		}
		set.addAll(childrenNotInKeys);
		set.addAll(keysHas);
		return set;
	}
	
	public Set<ContextElement> getRelatedElements() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		set.addAll(children);
		set.addAll(parents);
		set.addAll(keysIn);
		set.addAll(keysHas);
		set.addAll(mmrelations);
		return set;
	}
	
	public String toString(){
		return "En#" + this.getId();
	}
	
	public boolean isComposite() {
		for (Key key : keysHas)
			if (!key.isEmpty())
				return true;
		return false;
	}

/*
	public boolean addRequisite(Requisite requisite) throws AlreadyHasParentException{
		if (requisite.getParent() != null){
			throw new AlreadyHasParentException(requisite); 			
		}
		requisite.setParent(this);
		return children.add(requisite);	
	}
	
	public boolean addRequisite(Entity requisite) throws AlreadyHasParentException{
		if (requisite.getParent() != null){
			throw new AlreadyHasParentException(requisite); 			
		}
		requisite.setParent(this);
		return children.add(requisite); 						
	}
	
	// ��� �������� ������������ ����� ������ ���� ���������� �����������.	
	public boolean addKey(Key key) throws NotInChildrenException{
		if (!children.containsAll(key.getKeyElements())) {
			throw new NotInChildrenException(this, key);						
		}
		return keys.add(key); 						
	}

	public Set<MM> getMmrelations() {
		return mmrelations;
	}
*/	
}
