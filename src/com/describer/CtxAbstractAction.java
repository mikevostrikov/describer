package com.describer;
/*
 * @(#)JLFAbstractAction.java	1.6 00/06/12
 *
 * Copyright 2000 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import javax.swing.event.EventListenerList;

/**
 * Abstract Action for the JLF. Defines some useful methods.
 *
 * @version 1.6 06/12/00
 * @author  Mark Davidson
 */
@SuppressWarnings("serial")
public abstract class CtxAbstractAction extends AbstractAction {

    // The listener to action events (usually the main UI)
    private EventListenerList listeners;

    // Image directory URL
    public static final String ICONS_DIR = "/com/describer/resources/images/icons/";
   
    /**
     * The key used for storing a large icon for the action,
     * used for toolbar buttons.
     * <p>
     * Note: Eventually this key belongs in the javax.swing.Action interface.
     */
    public static final String LARGE_ICON = "LargeIcon";

    //
    // These next public methods may belong in the AbstractAction class.
    //
   
    /** 
     * Gets the value from the key Action.ACTION_COMMAND_KEY
     */
    public String getActionCommand()  {
        return (String)getValue(Action.ACTION_COMMAND_KEY);
    }
    
    /** 
     * Gets the value from the key Action.SHORT_DESCRIPTION
     */
    public String getShortDescription()  {
        return (String)getValue(Action.SHORT_DESCRIPTION);
    }
    
    /** 
     * Gets the value from the key Action.LONG_DESCRIPTION
     */
    public String getLongDescription()  {
        return (String)getValue(Action.LONG_DESCRIPTION);
    }
    
    /* Should finish the implementation and add get/set methods for all the 
     * javax.swing.Action keys:
        
        Action.NAME
        Action.SMALL_ICON
        ActionConstants.LARGE_ICON
        Action.MNEMONIC_KEY
     */
    

    // ActionListener registration and invocation.

    /** 
     * Forwards the ActionEvent to the registered listener.
     */
    public void actionPerformed(ActionEvent evt)  {
        if (listeners != null) {
            Object[] listenerList = listeners.getListenerList();
            // Recreate the ActionEvent and stuff the value of the ACTION_COMMAND_KEY
            ActionEvent e = new ActionEvent(evt.getSource(), evt.getID(), 
                                            (String)getValue(Action.ACTION_COMMAND_KEY));
            for (int i = 0; i <= listenerList.length-2; i+=2) {
                ((ActionListener)listenerList[i+1]).actionPerformed(e);
            }
        }
    }
    
    public void addActionListener(ActionListener l)  {
        if (listeners == null) {
            listeners = new EventListenerList();
	}
        listeners.add(ActionListener.class, l);
    }
    
    public void removeActionListener(ActionListener l)  {
	if (listeners == null) {
	    return;
	}
        listeners.remove(ActionListener.class, l);
    }
    
    /** 
     * Returns the Icon associated with the name from the resources.
     * The resouce should be in the path.
     * @param name Name of the icon file i.e., help16.gif
     * @return the name of the image or null if the icon is not found.
     */
    public ImageIcon getIcon(String name)  {
        String imagePath = ICONS_DIR + name;
        URL url = this.getClass().getResource(imagePath);
        if (url != null)  {
            return new ImageIcon(url);
        }
        return null;
    }
    
    /**
     * Checks if given event contains command of this action
     * @param e
     * @return
     */
    public boolean isMyEvent(ActionEvent e) {
    	return e.getActionCommand().equals(getValue(Action.ACTION_COMMAND_KEY));
    }

}
