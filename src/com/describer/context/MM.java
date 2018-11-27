package com.describer.context;

import java.util.HashSet;
import java.util.Set;

public class MM extends ContextElement {
	
	private final Set<Node> mMElements = new HashSet<Node>();
		
	protected MM(){
		type = Context.TYPE.MM;
	}
	
	/*
	private MM(Context ctx, int id, boolean humble) {
		this.setCtxPr(ctx);
		this.type = Context.TYPE.MM;
		ctx.setIdPr(this, id);
		if (humble) {
			ctx.addPr(this);
		} else {
			ctx.add(this);
		}	
	}
	
	private MM(Context ctx) {
		this(ctx, ctx.getFreeId(Context.TYPE.MM), false);
	}
	*/
	
	protected void addMMElementPr(Node element) {
		mMElements.add(element);
	}
	
	protected void removeMMElementPr(Node element) {
		mMElements.remove(element);
	}
	/*
	private MM(Context ctx, int id, boolean humble) {
		this.setCtxPr(ctx);
		this.type = Context.TYPE.MM;
		ctx.setIdPr(this, id);
		if (humble) {
			ctx.addPr(this);
		} else {
			ctx.add(this);
		}	
	}
	
	private MM(Context ctx) {
		this(ctx, ctx.getFreeId(Context.TYPE.MM), false);
	}
	*/
	
	public Set<Node> getMMElements(){
		return new HashSet<Node>(mMElements);
	}
	
	public boolean isAmongElements(Node emb) {
		return mMElements.contains(emb);
	}
	
	/*
	private MM(Context ctx, int id, boolean humble) {
		this.setCtxPr(ctx);
		this.type = Context.TYPE.MM;
		ctx.setIdPr(this, id);
		if (humble) {
			ctx.addPr(this);
		} else {
			ctx.add(this);
		}	
	}
	
	private MM(Context ctx) {
		this(ctx, ctx.getFreeId(Context.TYPE.MM), false);
	}
	*/
	
	public Set<ContextElement> getDirectDependants() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		set.addAll(mMElements);
		return set;
	}

	/*
	private MM(Context ctx, int id, boolean humble) {
		this.setCtxPr(ctx);
		this.type = Context.TYPE.MM;
		ctx.setIdPr(this, id);
		if (humble) {
			ctx.addPr(this);
		} else {
			ctx.add(this);
		}	
	}
	
	private MM(Context ctx) {
		this(ctx, ctx.getFreeId(Context.TYPE.MM), false);
	}
	*/
	
	public Set<ContextElement> getRelatedElements() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		set.addAll(mMElements);
		return set;
	}

	public String toString(){
		return "M:M#" + this.getId();
	}
	
}
