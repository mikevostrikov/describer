package com.describer.presentation;

import java.util.Collection;

import com.describer.context.ContextListener;

public interface PresenterListener extends ContextListener {
	
	public void updateSelection(Object source, Collection<Object> selected);

}
