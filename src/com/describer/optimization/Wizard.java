package com.describer.optimization;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.describer.context.Context;
import com.describer.context.Key;
import com.describer.context.Node;
import com.describer.presentation.Presenter;
import com.describer.util.MultiLineLabel;

@SuppressWarnings("serial")
class Wizard extends JDialog {
	
	List<Step> steps = new ArrayList<Step>();
	int stepNum = -1;
	MultiLineLabel header;
	JButton back, next, finish;
	JPanel stepPanel = new JPanel(new GridLayout());
	Context ctx; 
	Presenter presenter;		

	Wizard(Context ctx, Presenter presenter) {
		this.ctx = ctx;
		this.presenter = presenter;
		initSteps();
		initButtons();
		header = new MultiLineLabel("Header is not defined");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createRigidArea(new Dimension(0,10)));
		mainPanel.add(buildHeader());
		mainPanel.add(Box.createRigidArea(new Dimension(0,5)));
		mainPanel.add(stepPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,5)));
		mainPanel.add(buildButtonsPanel());
		mainPanel.add(Box.createRigidArea(new Dimension(0,10)));
		next();
		getContentPane().add(mainPanel);
	}
	
	JPanel buildHeader() {
		JPanel h = new JPanel();
		h.setLayout(new BoxLayout(h, BoxLayout.X_AXIS));
		h.add(Box.createRigidArea(new Dimension(25, 0)));
		h.add(header);
		h.add(Box.createHorizontalGlue());		
		return h;
	}
	
	void initButtons() {
		ButtonListener bl = new ButtonListener();
		back = new JButton("Back");
		back.setActionCommand("Back");
		back.addActionListener(bl);
		next = new JButton("Next");
		next.setActionCommand("Next");
		next.addActionListener(bl);
		finish = new JButton("Finish");
		finish.setActionCommand("Finish");
		finish.addActionListener(bl);
	}
	
	class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Back"))
				back();				
			else if (e.getActionCommand().equals("Next"))
				next();
			else if (e.getActionCommand().equals("Finish"))
				finish();
		}		
	}	
	
	void next() {
		if (stepNum < steps.size() - 1) {
			stepNum++;
		}
		steps.get(stepNum).prepareData();
		switchStepView();
	}
	
	void back() {
		if (stepNum > 0) {
			stepNum--;
		}
		steps.get(stepNum).prepareData();
		switchStepView();
	}
	
	void finish() {
		dispose();
	}
	
	/**
	 * Updates view accordingly to the current stepNum 
	 */
	void switchStepView() {
		if (stepNum == steps.size() - 1) {
			next.setEnabled(false);
		} else {
			next.setEnabled(true);
		}
		if (stepNum == 0) {
			back.setEnabled(false);
		} else {
			back.setEnabled(true);
		}
		header.setText(steps.get(stepNum).getHeaderText());
		setTitle(steps.get(stepNum).getTitle());
		stepPanel.removeAll();
		stepPanel.add(steps.get(stepNum).getPanel());
		stepPanel.revalidate();
		stepPanel.repaint();
	}
	
	JPanel buildButtonsPanel() {
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		buttons.add(back);
		buttons.add(Box.createRigidArea(new Dimension(5, 0)));
		buttons.add(next);
		buttons.add(Box.createRigidArea(new Dimension(15, 0)));
		buttons.add(finish);
		buttons.add(Box.createRigidArea(new Dimension(25, 0)));
		return buttons;
	}
	
	void initSteps() {
		steps.add(new StepOne());
		steps.add(new StepTwo());
		steps.add(new StepThree());
	}
	
	interface Step {
		String getHeaderText();
		String getTitle();
		JPanel getPanel();
		void prepareData();
	}
	
	abstract class AbstractStep extends JPanel implements Step {
		@Override
		public String getTitle() {
			return "Title is not defined.";
		}
		@Override
		public String getHeaderText() {
			return "Header is not defined.";
		}
		@Override
		public JPanel getPanel() {
			return this;
		}
	}
	
	class StepOne extends AbstractStep implements ActionListener, ListSelectionListener, ListDataListener {
		
		JList redundFzs = createList();
		JList fzsToDel = createList();
						
		StepOne() {
			JPanel pane = this;
			
			JButton button;
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c;
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(5,15,5,10);  //padding
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridheight = 3;
			JScrollPane jp = new JScrollPane(redundFzs);
			jp.setPreferredSize(new Dimension(100,100));
			pane.add(jp, c);

			button = new JButton(">>");
			button.setActionCommand("toDelList");
			button.addActionListener(this);
			c = new GridBagConstraints();
			c.anchor = GridBagConstraints.CENTER;
			c.weighty = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			pane.add(button, c);

			button = new JButton("<<");
			button.setActionCommand("fromDelList");
			button.addActionListener(this);
			c = new GridBagConstraints();
			c.anchor = GridBagConstraints.CENTER;
			c.weighty = 0.5;
			c.gridx = 1;
			c.gridy = 1;
			pane.add(button, c);

			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 0;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(5,10,5,15);  //padding
			c.weightx = 0.5;
			c.weighty = 10;
			c.gridheight = 2;
			jp = new JScrollPane(fzsToDel);
			jp.setPreferredSize(new Dimension(100,100));
			pane.add(jp, c);
			
			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 2;
			c.weightx = 0.5;
			c.weighty = 0;
			c.insets = new Insets(5,10,5,15);  //padding
			c.anchor = GridBagConstraints.LINE_END;
			button = new JButton("Delete");
			button.setActionCommand("delete");
			button.addActionListener(this);
			pane.add(button, c);
		}
		
		JList createList() {
			JList list = new JList();
			list.setPrototypeCellValue("AAAAAA");
			list.setModel(new DefaultListModel());
			list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);
			list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			list.getModel().addListDataListener(this);
			return list;
		}
		
		@Override
		public String getTitle() {
			return "Step one. Deleting Armstrong-deducible keys.";
		}
		
		@Override
		public String getHeaderText() {
			return "Keys that can be derived from other keys using Armstrong" +
					" axioms are presented on the left list. " +
					"\nMove some to the right list to eliminate them.";
		}

		@Override
		public void prepareData() {
			((DefaultListModel) redundFzs.getModel()).removeAllElements();
			((DefaultListModel) fzsToDel.getModel()).removeAllElements();
			for (Key key : Optimizer.armRedundantKeys(ctx.getKeys())) {
				((DefaultListModel) redundFzs.getModel()).addElement(key);				
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				if (e.getSource() instanceof JList) {
					JList list = (JList) e.getSource(); 
					if (list.getSelectedIndex() != -1) {
						Set<Object> set = new HashSet<Object>();
						Key key = (Key) list.getSelectedValue();
						set.add(key);
						set.add(key.getEntity());
						set.addAll(key.getKeyElements());
						presenter.setSelection(set);
					} else {
						//No selection
					}
				}
		    }
		}
		
		void updateRedundFzs() {
			Set<Key> consideredKeys = new HashSet<Key>();
			Set<Key> unconsideredKeys = new HashSet<Key>();
			for (Object o : java.util.Collections.list(((DefaultListModel) fzsToDel.getModel()).elements())) {
				if (o instanceof Key) {
					unconsideredKeys.add((Key) o);					
				}
			}
			consideredKeys = ctx.getKeys();
			consideredKeys.removeAll(unconsideredKeys);
			((DefaultListModel) redundFzs.getModel()).clear();
			for (Key key : Optimizer.armRedundantKeys(consideredKeys)) {
				((DefaultListModel) redundFzs.getModel()).addElement(key);				
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("toDelList")) {
				if (redundFzs.getSelectedIndex() != -1) {
					((DefaultListModel) fzsToDel.getModel()).addElement(redundFzs.getSelectedValue());
					((DefaultListModel) redundFzs.getModel()).removeElement(redundFzs.getSelectedValue());
					updateRedundFzs();
				}
			} else if (e.getActionCommand().equals("fromDelList")) {
				if (fzsToDel.getSelectedIndex() != -1) {
					((DefaultListModel) redundFzs.getModel()).addElement(fzsToDel.getSelectedValue());
					((DefaultListModel) fzsToDel.getModel()).removeElement(fzsToDel.getSelectedValue());
					updateRedundFzs();
				}
			}  else if (e.getActionCommand().equals("delete")) {
				Set<Key> toDelEls = new HashSet<Key>();
				for (int i = ((DefaultListModel) fzsToDel.getModel()).size() - 1; i >= 0; i--) {
					toDelEls.add((Key) ((DefaultListModel) fzsToDel.getModel()).getElementAt(i));
				}
				ctx.removeAll(toDelEls);
				((DefaultListModel) fzsToDel.getModel()).removeAllElements();
			}
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			listChanged(e);
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			listChanged(e);			
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			listChanged(e);
		}
		
		public void listChanged(ListDataEvent e) {
			if (e.getSource() == fzsToDel.getModel()) {
				fzsToDel.setVisibleRowCount(-1);
			} else if (e.getSource() == redundFzs.getModel()) {
				redundFzs.setVisibleRowCount(-1);
			}
		}
		
	}
	
	class StepTwo extends AbstractStep implements ActionListener, ListSelectionListener, ListDataListener {
		
		JList minimizableKeys = createMultColList();
		JList possibleReplacements = createOneColList();
		Map<Key, Set<Set<Node>>> keysNMins;
						
		StepTwo() {
			JPanel pane = this;
			
			JButton button;
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c;
			
			JLabel label = new JLabel("Minimizable keys:");
			label.setPreferredSize(new Dimension(130, 15));
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.insets = new Insets(5,15,0,10);  //padding
			c.weighty = 0;
			pane.add(label, c);
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(5,15,5,10);  //padding
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridheight = 2;
			JScrollPane jp = new JScrollPane(minimizableKeys);
			jp.setPreferredSize(new Dimension(100,100));
			pane.add(jp, c);

			label = new JLabel("Min. versions of the key:");
			label.setPreferredSize(new Dimension(130, 15));
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.weighty = 0;
			c.insets = new Insets(5,10,0,15);  //padding
			pane.add(label, c);

			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(5,10,5,15);  //padding
			c.weightx = 0.5;
			c.weighty = .5;
			jp = new JScrollPane(possibleReplacements);
			jp.setPreferredSize(new Dimension(100,100));
			pane.add(jp, c);
			
			button = new JButton("Replace key");
			button.setActionCommand("replace");
			button.addActionListener(this);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.weightx = 0.5;
			c.weighty = 0;
			c.insets = new Insets(5,10,5,15);  //padding
			c.anchor = GridBagConstraints.LINE_END;
			pane.add(button, c);
			
		}
		
		JList createMultColList() {
			JList list = new JList();
			list.setPrototypeCellValue("AAAAAA");
			list.setModel(new DefaultListModel());
			list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);
			list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			list.getModel().addListDataListener(this);
			return list;
		}
		
		JList createOneColList() {
			JList list = new JList();
			list.setModel(new DefaultListModel());
			list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);
			return list;
		}
		
		@Override
		public String getTitle() {
			return "Step two. Shortening keys.";
		}
		
		@Override
		public String getHeaderText() {
			return "Step two. Keys that can be shortened are presented on the left list." +
					"\nChoose a key, a possible replacement for it, and press \"Replace\"";
		}

		@Override
		public void prepareData() {
			((DefaultListModel) minimizableKeys.getModel()).removeAllElements();
			((DefaultListModel) possibleReplacements.getModel()).removeAllElements();
			keysNMins = Optimizer.minimizeKeys(ctx.getKeys(), ctx.getKeys());
			for (Key key : keysNMins.keySet()) {
				((DefaultListModel) minimizableKeys.getModel()).addElement(key);				
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("replace")) {
				Key key = (Key) minimizableKeys.getSelectedValue();
				Collection<Node> repl = (Collection<Node>) possibleReplacements.getSelectedValue();
				if (key != null && repl != null) {
					Set<Node> extraEls = key.getKeyElements();
					extraEls.removeAll(repl);
					ctx.removeKeyElements(key, extraEls);
					((DefaultListModel) minimizableKeys.getModel()).removeElement(key);
					((DefaultListModel) possibleReplacements.getModel()).removeAllElements();
				}
			}
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			listChanged(e);
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			listChanged(e);			
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			listChanged(e);
		}
		
		public void listChanged(ListDataEvent e) {
			if (e.getSource() == minimizableKeys.getModel()) {
				minimizableKeys.setVisibleRowCount(-1);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false
					&& e.getSource() == minimizableKeys 
					&& ((JList) e.getSource()).getSelectedIndex() != -1) {
				Key key = (Key) ((JList) e.getSource()).getSelectedValue();
				Set<Object> set = new HashSet<Object>();
				set.add(key);
				set.add(key.getEntity());
				set.addAll(key.getKeyElements());
				presenter.setSelection(set);
				setReplacements(keysNMins.get(key));
			} else if (e.getValueIsAdjusting() == false
					&& e.getSource() == possibleReplacements 
					&& ((JList) e.getSource()).getSelectedIndex() != -1) {
				Key key = (Key) minimizableKeys.getSelectedValue();
				Set<Object> set = new HashSet<Object>();
				set.add(key);
				set.add(key.getEntity());
				set.addAll((Collection<Node>) possibleReplacements.getSelectedValue());
				presenter.setSelection(set);
			}
		}
		
		private void setReplacements(Set<Set<Node>> repl) {
			((DefaultListModel) possibleReplacements.getModel()).removeAllElements();
			for (Set<Node> set : repl) {
				((DefaultListModel) possibleReplacements.getModel()).addElement(set);
			}
		} 
		
	}
	
	class StepThree extends StepOne {
		
		@Override
		public String getHeaderText() {
			return "Keys that forms redundant computation paths are presented on the left list" +
					"\nMove some to the right list to get rid of them.";
		}
		
		@Override
		public String getTitle() {
			return "Step three. Eliminating redundant computation paths.";
		}

		@Override
		public void prepareData() {
			((DefaultListModel) redundFzs.getModel()).removeAllElements();
			((DefaultListModel) fzsToDel.getModel()).removeAllElements();
			for (Key key : Optimizer.redundantKeys(ctx.getKeys())) {
				((DefaultListModel) redundFzs.getModel()).addElement(key);				
			}
		}
		
		@Override
		void updateRedundFzs() {
			Set<Key> consideredKeys = new HashSet<Key>();
			Set<Key> unconsideredKeys = new HashSet<Key>();
			for (Object o : java.util.Collections.list(((DefaultListModel) fzsToDel.getModel()).elements())) {
				if (o instanceof Key) {
					unconsideredKeys.add((Key) o);					
				}
			}
			consideredKeys = ctx.getKeys();
			consideredKeys.removeAll(unconsideredKeys);
			((DefaultListModel) redundFzs.getModel()).clear();
			for (Key key : Optimizer.redundantKeys(consideredKeys)) {
				((DefaultListModel) redundFzs.getModel()).addElement(key);				
			}
		}
		
	}		
	
}
