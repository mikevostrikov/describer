package com.describer.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Node extends ContextElement{
	
	private String name;
	private String semantics;
	protected final Set<Entity> parents = new HashSet<Entity>();
	protected final Set<MM> mmrelations = new HashSet<MM>();
	protected final Set<Key> keysIn = new HashSet<Key>();
	
	protected void addKeyInPr(Key keyIn) {
		keysIn.add(keyIn);		
	}
	
	protected void addKeysInPr(Collection<Key> keysIn) {
		this.keysIn.addAll(keysIn);
	}
	
	/*
	public void setName(String name){
		ctx.setName(this, name);
	}
	*/
	
	protected void removeKeyInPr(Key key) {
		keysIn.remove(key);		
	}

	protected void setNamePr(String name){
		this.name = name.trim();
	}

	/*
	public void setSemantics(String semantics){
		ctx.setSemantics(this, semantics);		
	}
	*/
	
	protected void setSemanticsPr(String semantics){
		this.semantics = semantics.trim();		
	}

	/*
	public void setSemantics(String semantics){
		ctx.setSemantics(this, semantics);		
	}
	*/
	
	protected void addParentPr(Entity parent){
		parents.add(parent);		
	}

	/*
	public void setSemantics(String semantics){
		ctx.setSemantics(this, semantics);		
	}
	*/
	
	protected void removeParentPr(Entity ent) {
		parents.remove(ent);
	}

	protected void addParentsPr(Collection<Entity> parents){
		this.parents.addAll(parents);		
	}

	/*
	public void setSemantics(String semantics){
		ctx.setSemantics(this, semantics);		
	}
	*/
	
	protected void addMMPr(MM mM) {
		mmrelations.add(mM);								
	}
	
	protected void addMMsPr(Collection<MM> mMs) {
		mmrelations.addAll(mMs);								
	}

	protected void removeMMPr(MM mM) {
		mmrelations.remove(mM);		
	}

	public String getName(){
		return name;				
	}
	
	/*
	public void setName(String name){
		ctx.setName(this, name);
	}
	*/
	
	public String getSemantics(){
		return semantics;				
	}
	
	/*
	public void setSemantics(String semantics){
		ctx.setSemantics(this, semantics);		
	}
	*/
	
	public Set<Entity> getParents(){
/*		Set<Entity> prnts = new HashSet<Entity>(parents);
		for (Iterator<Entity> it1 = prnts.iterator(); it1.hasNext();){
			it1.next().setCtx(null);
		}*/
		return new HashSet<Entity>(parents);
	} 
	
	public Set<MM> getMMs(){
		/*		Set<Entity> prnts = new HashSet<Entity>(parents);
				for (Iterator<Entity> it1 = prnts.iterator(); it1.hasNext();){
					it1.next().setCtx(null);
				}*/
		return new HashSet<MM>(mmrelations);
	}
	
	public Set<Key> getKeysIn(){
		return new HashSet<Key>(keysIn);
	}
	
}
