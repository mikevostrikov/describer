package com.describer.presentation.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import com.describer.context.Entity;
import com.describer.context.Key;
import com.describer.context.MM;
import com.describer.context.Requisite;

@SuppressWarnings("serial")
public class CtxMutableTreeNode extends DefaultMutableTreeNode{

	public CtxMutableTreeNode(Object obj) {
		super(obj);
	}
	
	public boolean isLeaf() {
		return false;		
	}

	public boolean isEntityNode() {
		return this.getUserObject() instanceof Entity;		
	}

	public boolean isRequisiteNode() {
		return this.getUserObject() instanceof Requisite;		
	}

	public boolean isKeyNode() {
		return this.getUserObject() instanceof Key;		
	}
	
	public boolean isMMNode() {
		return this.getUserObject() instanceof MM;		
	}

}
