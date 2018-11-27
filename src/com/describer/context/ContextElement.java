package com.describer.context;

import java.util.Set;


public abstract class ContextElement {
	protected Context ctx;
	private int id;
	protected Context.TYPE type;
	
	protected void setCtxPr(Context ctx) {
		this.ctx = ctx;
	}

	protected void setIdPr(int id) {
		this.id = id;		
	}

	/*
	public void setId(int id) throws Exception {
		ctx.setId(this, id);
	}
	*/

	public Context getCtx() {
		return ctx;
	}

	protected Context.TYPE getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public abstract Set<ContextElement> getRelatedElements();
	
	public abstract Set<ContextElement> getDirectDependants();
	
	public boolean isNode() {
		return isEntity() || isRequisite();
	}
	
	public boolean isEntity() {
		return type.equals(Context.TYPE.ENTITY);
	}
	
	public boolean isRequisite() {
		return type.equals(Context.TYPE.REQUISITE);
	}
	
	public boolean isMM() {
		return type.equals(Context.TYPE.MM);
	}
	
	public boolean isKey() {
		return type.equals(Context.TYPE.KEY);
	}
	
	public abstract String toString();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ctx == null) ? 0 : ctx.hashCode());
		result = prime * result + id;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ContextElement))
			return false;
		ContextElement other = (ContextElement) obj;
		if (ctx == null) {
			if (other.ctx != null)
				return false;
		} else if (!ctx.equals(other.ctx))
			return false;
		if (id != other.id)
			return false;
		if (type != other.type)
			return false;
		return true;
	}	
	
}
