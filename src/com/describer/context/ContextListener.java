package com.describer.context;

import java.util.EventListener;

public interface ContextListener extends EventListener {
	
	void elementsAdded(ContextEvent e);
	
	void elementsChanged(ContextEvent e);
	
	void elementsRemoved(ContextEvent e);
	
	void elementConverted(ContextEvent e);
	
}
