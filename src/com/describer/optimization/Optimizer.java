package com.describer.optimization;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.describer.Main;
import com.describer.context.Context;
import com.describer.context.ContextElement;
import com.describer.context.Entity;
import com.describer.context.Group;
import com.describer.context.Key;
import com.describer.context.Node;
import com.describer.presentation.Presenter;
import com.describer.util.LazySubSets;

public class Optimizer {

	static private JFrame optWindow;

	public void removeSuperkeys(Context ctx) {

		Set<Key> keysToRemove = new HashSet<Key>();

		for (Key key1 : ctx.getKeys()) {
			for (Key key2 : ctx.getKeys()) {
				if (key1 != key2
						&& key1.getEntity() == key2.getEntity()
						&& key1.getKeyElements().containsAll(
								key2.getKeyElements()))
					if (!keysToRemove.contains(key2))
						keysToRemove.add(key1);
			}
		}

		ctx.removeAll(keysToRemove);

	}

	public void removeDuplicateKeys(Context ctx) {

		Set<Key> keysToRemove = new HashSet<Key>();

		Key[] keys = new Key[ctx.getKeys().size()];
		ctx.getKeys().toArray(keys);
		for (int i = 0; i < keys.length - 1; i++) {
			Key key1 = keys[i];
			for (int j = i + 1; j < keys.length; j++) {
				Key key2 = keys[j];
				if (key1.getEntity() == key2.getEntity()
						&& key1.getKeyElements().equals(key2.getKeyElements()))
					keysToRemove.add(key1);
			}
		}

		ctx.removeAll(keysToRemove);

	}

	public Collection<Object> findCycles(Context ctx, Presenter presenter) {
		DirectedGraph<Object, Object> dg = createGraph(ctx);
		CycleDetector<Object, Object> cd = new CycleDetector<Object, Object>(dg);
		Collection<Object> cycleElems = cd.findCycles();
		if (!cycleElems.isEmpty())
			presenter.setSelection(cycleElems);
		return cycleElems;
	}

	private DirectedGraph<Object, Object> createGraph(Context ctx) {
		DirectedGraph<Object, Object> dg = new DefaultDirectedGraph<Object, Object>(
				DefaultEdge.class);
		for (ContextElement el : ctx.getAllElements()) {
			dg.addVertex(el);
		}
		for (ContextElement el : ctx.getAllElements()) {
			for (ContextElement dep : el.getDirectDependants()) {
				dg.addEdge(el, dep);
			}
		}
		return dg;
	}

	public void showOptWindow(JFrame main, Context ctx, Presenter presenter) {
		if (optWindow == null) {
			optWindow = new OptWindow(ctx, presenter);
			optWindow.setSize(600, 400);
			optWindow.setLocationRelativeTo(main);
		}
		optWindow.setVisible(true);
	}

	/**
	 * In building closure uses all nodes, defined in ctx
	 * 
	 * @param nodes
	 * @param fzs
	 * @return
	 */
	static public Set<Node> attrClosure(Set<Node> nodes, Set<Key> fzs) {
		Set<Node> closure = new HashSet<Node>(nodes);
		boolean flag = true;
		while (flag) {
			flag = false;
			for (Key key : fzs) {
				if (!closure.contains(key.getEntity())
						&& closure.containsAll(key.getKeyElements())) {
					closure.add(key.getEntity());
					flag = true;
				}
			}
		}
		return closure;
	}

	static public Set<Key> armRedundantKeys(Set<Key> fzs) {
		Set<Key> G0 = fzs;
		Set<Key> redundant = new HashSet<Key>();
		Set<Key> G1 = new HashSet<Key>(G0);
		for (Key key : fzs) {
			G1.remove(key);
			if (attrClosure(key.getKeyElements(), G1).contains(
					key.getEntity())) {
				redundant.add(key);
			}
			G1.add(key);
		}
		return redundant;
	}
	
	static public Set<Key> redundantKeys(Set<Key> fzs) {
		Set<Key> G0 = fzs;
		Set<Key> redundant = new HashSet<Key>();
		Set<Key> G1 = new HashSet<Key>(G0);
		for (Key key : fzs) {
			G1.remove(key);
			if (attrClosure(allSubNodes(key, G1), G1).contains(key.getEntity())) {
				redundant.add(key);
			}
			G1.add(key);
		}
		return redundant;
	}
	
	

	static private Set<Node> allSubNodes(Key key, Set<Key> fzs) {
		Set<Node> set = new HashSet<Node>();
		//for (Key key : entity.getKeysHas()) {
			//if (fzs.contains(key)) {
		for (Node nd : key.getKeyElements()) {
			subNodes(set, nd, fzs);
		}
			//}
		//}
		return set;
	}

	static private void subNodes(Set<Node> nodesIncluded, Node curNode, Set<Key> fzs) {
		assert curNode != null;
		nodesIncluded.add(curNode);
		Entity curEnt = null;
		if (!curNode.isEntity())
			return;
		else
			curEnt = (Entity) curNode;
		for (Key key : curEnt.getKeysHas()) {
			if (fzs.contains(key)) {
				for (Node nd : key.getKeyElements()) {
					if (!nodesIncluded.contains(nd))
						subNodes(nodesIncluded, nd, fzs);
				}
			}
		}
	}

	static public Set<Set<Node>> minimizeKey(Key key, Set<Key> fzs) {
		Set<Set<Node>> minimalSubKeys = new HashSet<Set<Node>>();
		for (Set<Node> nds : new LazySubSets<Node>(key.getKeyElements())) {
			if (attrClosure(nds, fzs).contains(key.getEntity())) {
				boolean fl = true;
				for (Set<Node> set : minimalSubKeys) {
					if (nds.containsAll(set)) {
						fl = false;
						break;
					}
				}
				if (fl)
					minimalSubKeys.add(nds);
			}
		}
		return minimalSubKeys;
	}
	
	static public Map<Key, Set<Set<Node>>> minimizeKeys(Collection<Key> keys, Set<Key> fzs) {
		Map <Key, Set<Set<Node>>> map = new HashMap<Key, Set<Set<Node>>>();
		for (Key key : keys) {
			Set<Set<Node>> minimals = minimizeKey(key, fzs);
			minimals.remove(key.getKeyElements());
			// Add only such keys, that can be minimized
			if (!minimals.isEmpty())
				map.put(key, minimals);
		}
		return map;
	}

	public Set<Key> minimalCoverage(Context ctx) {
		Set<Key> G0 = ctx.getKeys();

		for (Iterator<Key> it1 = G0.iterator(); it1.hasNext();) {
			Key key = it1.next();
			Set<Key> G1 = new HashSet<Key>(G0);
			G1.remove(key);
			if (attrClosure(key.getKeyElements(), G1).contains(key.getEntity())) {
				it1.remove();
			} else {
				G1.add(key);
				for (Set<Node> nds : new LazySubSets<Node>(key.getKeyElements())) {
					if (attrClosure(nds, G1).contains(key.getEntity())) {
						// TODO do smth with key

					}
				}
			}
		}

		return null;
	}

	@SuppressWarnings("serial")
	class OptWindow extends JFrame implements ActionListener {

		JTextField textField;
		JTextArea textArea;

		Context ctx;
		Presenter presenter;

		OptWindow(Context ctx, Presenter presenter) {

			this.ctx = ctx;
			this.presenter = presenter;

			this.getContentPane().setLayout(new GridBagLayout());

			textField = new JTextField(20);
			textField.addActionListener(this);

			textArea = new JTextArea(5, 20);
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);

			// Add Components to this panel.
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.REMAINDER;

			c.fill = GridBagConstraints.HORIZONTAL;
			add(textField, c);

			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1.0;
			c.weighty = 1.0;
			add(scrollPane, c);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = textField.getText();

			String output = null;

			if (command.matches("clear"))
				textArea.setText("");
			else {
				output = fulfillCommand(command);

				textArea.append(">>" + output + "\n");
				textField.selectAll();

				// Make sure the new text is visible, even if there
				// was a selection in the text area.
				textArea.setCaretPosition(textArea.getDocument().getLength());
			}
		}

		private String showFZ() {
			StringBuilder sb = new StringBuilder("FZs : ");
			for (Key key : ctx.getKeys()) {
				sb.append(key.getKeyElements() + " -> " + key.getEntity()
						+ "; ");
			}
			return sb.toString();
		}

		private String buildAttrClosure(String command) {
			Pattern pt = Pattern.compile("build attr closure\\((.+)\\)");
			Matcher mt = pt.matcher(command);
			mt.find();
			String argument = mt.group(1);
			String[] args = argument.split("\\s*,\\s*");

			pt = Pattern.compile("([erk])(\\d+)");
			mt = pt.matcher("");
			Node nd = null;
			Key key = null;
			Set<Node> nodes = new HashSet<Node>();
			Set<Key> keys = new HashSet<Key>();
			for (String arg : args) {
				mt.reset(arg);
				nd = null;
				key = null;
				if (!mt.find())
					return "Closure : Illegal arguments";
				Integer id = new Integer(mt.group(2));
				if (mt.group(1).equals("e"))
					nd = ctx.getEntityByEId(id);
				else if (mt.group(1).equals("r"))
					nd = ctx.getRequisiteByRId(id);
				else if (mt.group(1).equals("k"))
					key = ctx.getKeyByKId(id);
				if (nd == null && key == null)
					return "Closure : Required elements were not found";
				if (nd != null)
					nodes.add(nd);
				else
					keys.add(key);
			}
			return "Closure(" + nodes.toString() + ") : "
					+ attrClosure(nodes, keys);
		}

		private String getRedundantKeys(String command) {
			Pattern pt = Pattern.compile("redundant keys\\((.+)\\)");
			Matcher mt = pt.matcher(command);
			mt.find();
			String argument = mt.group(1);
			String[] args = argument.split("\\s*,\\s*");

			pt = Pattern.compile("k(\\d+)");
			mt = pt.matcher("");
			Key key = null;
			Set<Key> keys = new HashSet<Key>();
			for (String arg : args) {
				mt.reset(arg);
				key = null;
				if (!mt.find())
					return "Redundant keys : Illegal arguments";
				Integer id = new Integer(mt.group(1));
				key = ctx.getKeyByKId(id);
				if (key == null)
					return "Redundant keys : Required elements were not found";
				keys.add(key);
			}
			return "Redundant() : " + redundantKeys(keys);
		}

		private String getAllSubNodes(String command) {
			Pattern pt = Pattern.compile("subnodes\\((.+)\\)");
			Matcher mt = pt.matcher(command);
			mt.find();
			String argument = mt.group(1);
			String[] args = argument.split("\\s*,\\s*");
			pt = Pattern.compile("([k])(\\d+)");
			mt = pt.matcher("");
			Key key = null;
			Key key1 = null;
			Set<Key> keys = new HashSet<Key>();
			boolean first = true;
			for (String arg : args) {
				mt.reset(arg);
				key = null;
				if (!mt.find())
					return "SubNodes : Illegal arguments";
				Integer id = new Integer(mt.group(2));
				if (mt.group(1).equals("k")) {
					if (first) {
						key1 = ctx.getKeyByKId(id);
						first = false;
					} else {
						key = ctx.getKeyByKId(id);
						if (key == null)
							return "SubNodes : Required elements were not found";
						keys.add(key);
					}
				}
			}
			return "SubNodes : " + allSubNodes(key1, keys);
		}

		private String getMinimalKeys(String command) {
			Pattern pt = Pattern.compile("minimize key\\((.+)\\)");
			Matcher mt = pt.matcher(command);
			mt.find();
			String argument = mt.group(1);
			String[] args = argument.split("\\s*,\\s*");

			pt = Pattern.compile("k(\\d+)");
			mt = pt.matcher("");
			Key key = null;
			Set<Key> keys = new HashSet<Key>();
			Key keyToMinimize = null;
			boolean first = true;
			for (String arg : args) {
				key = null;
				mt.reset(arg);
				if (!mt.find())
					return "Minimal key : Illegal arguments";
				Integer id = new Integer(mt.group(1));
				key = ctx.getKeyByKId(id);
				if (key != null && first) {
					keyToMinimize = key;
					first = false;
				} else if (key != null && !first)
					keys.add(key);
				else
					return "Minimal keys : Required keys were not found";
			}
			return "minimal keys() : " + minimizeKey(keyToMinimize, keys);
		}

		private String fulfillCommand(String command) {
			if (command.equals("findCycles")) {
				return "Cycles detected : " + findCycles(ctx, presenter);
			} else if (command.matches("showAllPaths\\(.+\\)")) {
				return showAllPaths(command);
			} else if (command.equals("showFZ")) {
				return showFZ();
			} else if (command.matches("build attr closure\\(.+\\)")) {
				return buildAttrClosure(command);
			} else if (command.matches("minimize key\\((.+)\\)")) {
				return getMinimalKeys(command);
			} else if (command.matches("redundant keys\\(.+\\)")) {
				return getRedundantKeys(command);
			} else if (command.matches("subnodes\\(.+\\)")) {
				return getAllSubNodes(command);
			} else if (command.matches("all nodes subsets")) {
				StringBuilder sb = new StringBuilder();
				try {
					for (Set<Node> set : new LazySubSets<Node>(ctx.getNodes()))
						sb.append(set.toString()
								+ System.getProperty("line.separator"));
				} catch (Throwable t) {
					sb = new StringBuilder();
					sb.append(t);
				}
				return "" + sb;
			} else if (command.matches("help")) {
				return "Available commands : "
						+ "\nhelp"
						+ "\nshowAllPaths(kid)"
						+ "\nfindCycles"
						+ "\nshowFZ"
						+ "\nbuild attr closure(e[id1]..., r[id3]..., k[id4]...)"
						+ "\nall nodes subsets" + "\nredundant keys(k[id1]...)"
						+ "\nsubnodes(e[id1],k[id2]...)"
						+ "\nminimize key(k[id1], k[id2]...)";
			}
			return "Unknown command";
		}

		private String showAllPaths(String command) {
			Pattern pt = Pattern.compile("showAllPaths\\((.+)\\)");
			Matcher mt = pt.matcher(command);
			mt.find();
			String argument = mt.group(1);
			int kid = Integer.parseInt(argument);
			Key key = ctx.getKeyByKId(kid);
			if (key != null) {
				StringBuilder result = new StringBuilder();/*
				for (AttrPath ap : key.getPaths().keySet()) {
					result.append(ap + " : " + key.getPaths().get(ap) + "\n");
				}*/
				for (Group gr : key.grMgr.getGroups()) {
					result.append(gr + " : " + "inGrs : " + gr.getInGroups() + ": req : " + gr.getReq() + "\n");
				}
				return result.toString();
			}
			return "Key specified was not found";
		}
	}

	public void runWizard(Main main, Context ctx, Presenter presenter) {
		Wizard wizard = new Wizard(ctx, presenter);
		wizard.setModal(true);
		wizard.setSize(600, 400);
		wizard.setLocationRelativeTo(main);
		wizard.setVisible(true);
	}

}
