package com.describer.context;

import java.util.HashSet;
import java.util.Set;

public class Requisite extends Node {
	
	private Boolean required;
	
	protected Requisite() {
		type = Context.TYPE.REQUISITE;
	}
	protected void setRequiredPr(boolean r) {
		this.required = r;
	}
	
	public boolean getRequired() {
		return required;
	}
	/*
	private Requisite(Context ctx, String name, int id, String semantics, boolean required, boolean humble) {
		this.setCtxPr(ctx);
		this.type = Context.TYPE.REQUISITE;
		ctx.setIdPr(this, id);
		ctx.setNamePr(this, name);
		ctx.setSemanticsPr(this, semantics);
		ctx.setRequiredPr(this, required);
		if (humble) {
			ctx.addPr(this);
		} else {
			ctx.add(this);
		}
	}
	
	private Requisite(Context ctx) {
		this(ctx, ctx.getUniqueName(Context.TYPE.REQUISITE
						, ctx.getDefNewName(Context.TYPE.REQUISITE))
				, ctx.getFreeId(Context.TYPE.REQUISITE)
				, ""
				, false
				, false
				);
	}
		

	protected Requisite(Context ctx, String name, boolean humble) throws Exception {
		this.setCtx(ctx);
		this.type = Context.TYPE.REQUISITE;
		try{
			ctx.setIdPr(this, ctx.getFreeId(type));
			this.setName(name);
			this.setSemantics("");
			this.setRequired(false);
			if (humble) {
				ctx.humbleAdd(this);
			} else {
				ctx.add(this);
			}
		} catch (Exception e) {
			throw e; 
		}
	}
	*/
	public Set<ContextElement> getDirectDependants() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		return set;
	}
	
	public Set<ContextElement> getRelatedElements() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		set.addAll(parents);
		set.addAll(keysIn);
		set.addAll(mmrelations);
		return set;
	}
	
	public String toString(){
		return "Req#" + this.getId();
	}
	
	/*
	public void setRequired(boolean r) {
		ctx.setRequired(this, r);		
	}
	*/
	
}
