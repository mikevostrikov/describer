package com.describer.tester;

import javax.swing.ActionMap;
import javax.swing.JComponent;

import org.jgraph.JGraph;

public class KeyStr {

  @SuppressWarnings("rawtypes")
public static void main(String[] args) {
    try {
      Class cls = JGraph.class;
      JComponent comp = (JComponent)cls.newInstance();
      ActionMap actionMap = comp.getActionMap();
      System.out.println("ActionMap: allKeys =");
      Object akeys[] = actionMap.allKeys();
      for ( int i = 0; i < akeys.length; i++) {
          System.out.println(" "+akeys[i]);
      }
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
    System.exit(0);
  }
  
}