package com.describer.presentation.upd;

public interface UpdatingDelegate {
	/**
	 * @return <b>true</b> if events from the object, containing this
	 * delegate, should not be processed by listeners 
	 */
	boolean isBlocked();
	/**
	 * @param delegate - updating delegate of the object which
	 * initiates updating process
	 */
	void update(UpdatingDelegate delegate);
}
