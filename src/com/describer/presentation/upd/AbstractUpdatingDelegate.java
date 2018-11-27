package com.describer.presentation.upd;

public abstract class AbstractUpdatingDelegate implements UpdatingDelegate {
	
	protected boolean blocked;
	
	@Override
	public boolean isBlocked() {
		return blocked;
	}
	
	@Override
	public void update(UpdatingDelegate delegate) {
		if (delegate == this) return;
		blocked = true;
		update();
		blocked = false;
	}
	
	abstract protected void update();
	
}
