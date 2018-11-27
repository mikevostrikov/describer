package com.describer.context;

import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class ContextEvent extends EventObject {
	
	private List<ContextElement> newElements = new LinkedList<ContextElement>();
	
	private List<ContextElement> changedElements = new LinkedList<ContextElement>();
	
	private List<ContextElement> removedElements = new LinkedList<ContextElement>();
	
	private ContextElement convertedElementOld;
	private ContextElement convertedElementNew;
	//private ContextElement convertedElements;
	
	public ContextEvent(Object source) {
		super(source);
	}
	
	protected void addNewElement(ContextElement el) {
		newElements.add(el);		
	}
	
	public List<ContextElement> getNewElements() {
		return newElements;
	}
	
	protected void setNewElements(Collection<ContextElement> newElements) {
		this.newElements.addAll(newElements);		
	}
	
	protected void addRemovedElement(ContextElement el) {
		removedElements.add(el);
	}
	
	public List<ContextElement> getRemovedElements() {
		return removedElements;
	}

	protected void setRemovedElements(Collection<ContextElement> removedElements) {
		this.removedElements.addAll(removedElements);
	}

	protected void addChangedElement(ContextElement el) {
		changedElements.add(el);
	}
	
	public List<ContextElement> getChangedElements() {
		return changedElements;
	}

	protected void setChangedElements(Collection<ContextElement> changedElements) {
		this.changedElements.addAll(changedElements);
	}

	protected void setConvertedElementOld(ContextElement convertedElementOld) {
		this.convertedElementOld = convertedElementOld;
	}

	public ContextElement getConvertedOldElement() {
		return convertedElementOld;
	}

	protected void setConvertedElementNew(ContextElement convertedElementNew) {
		this.convertedElementNew = convertedElementNew;
	}

	public ContextElement getConvertedNewElement() {
		return convertedElementNew;
	}

}
