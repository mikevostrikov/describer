package com.describer.context;

import java.util.Comparator;

/**
 * @author Mike V
 * Compares context elements. Entities < Requisites < Keys < MMs.
 * Entities and Requisites are sorted accordingly with their names.
 * Other elements of the same type are sorted accordingly with their
 * identifiers. 
 */
public class GeneralCtxElemsComparator implements Comparator<ContextElement> {

    @Override
    public int compare(ContextElement o1, ContextElement o2) {
	if (o1.isEntity() && o2.isEntity() 
		|| o1.isRequisite() && o2.isRequisite()) {
	    return ((Node) o1).getName().toLowerCase()
	    	.compareTo(((Node) o2).getName().toLowerCase());
	} else if (o1.isKey() && o2.isKey() 
		|| o1.isMM() && o2.isMM()) {
	    return o1.getId() - o2.getId();	    	    
	} else if (o1.isEntity()) {
	    return -1;
	} else if (o2.isEntity()) {
	    return 1;
	} else if (o1.isRequisite()) {
	    return -1;
	} else if (o2.isRequisite()) {
	    return 1;
	} else if (o1.isKey()) {    
	    return -1;
	} else if (o2.isKey()) {    
	    return 1;
	} else {
	    assert false;
	    return 0;
	}
    }

}
