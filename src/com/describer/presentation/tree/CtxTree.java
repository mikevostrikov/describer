package com.describer.presentation.tree;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import com.describer.context.Context;
import com.describer.context.ContextElement;
import com.describer.context.ContextEvent;
import com.describer.context.Entity;
import com.describer.context.GeneralCtxElemsComparator;
import com.describer.context.Key;
import com.describer.context.MM;
import com.describer.context.Requisite;
import com.describer.presentation.Presenter;
import com.describer.presentation.PresenterListener;
import com.describer.presentation.upd.AbstractUpdatingDelegate;
import com.describer.presentation.upd.Updateable;
import com.describer.presentation.upd.UpdatingDelegate;

@SuppressWarnings("serial")
public class CtxTree extends JTree implements TreeExpansionListener,
		TreeWillExpandListener, PresenterListener, Updateable {

	private static String ROOT = "entitiesTree";
	private static String ENTITIES = "entities";
	private static String REQUISITES = "requisites";
	private static String KEYS = "keys";
	private static String MMS = "M:M";

	private DefaultTreeModel model = null;

	private Context ctx;

	private Presenter presenter;

	private UpdatingDelegate updDeleg = new AbstractUpdatingDelegate() {
		protected void update() {
			ArrayList<TreePath> paths = new ArrayList<TreePath>();
			for (Object selected : presenter.getSelection()) {
				computeTreePathSelection(selected, paths);
			}

			TreePath[] tpaths = new TreePath[paths.size()];
			for (int i = 0; i < tpaths.length; i++) {
				tpaths[i] = paths.get(i);
			}

			// now we set the corresponding tree selection
			setSelectionPaths(tpaths);
		}
	};

	private boolean supressSelEvents = false;

	// ctor
	public CtxTree(DefaultMutableTreeNode top) {
		super(top);
		this.setCellRenderer(new CtxTreeRenderer());
		this.addTreeWillExpandListener(this);
		this.addTreeExpansionListener(this);
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
		rebuild();
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	private void rebuild() {
		CtxMutableTreeNode top = new CtxMutableTreeNode(ROOT);
		model = new DefaultTreeModel(top);
		setModel(model);
		// доб. понятия
		CtxMutableTreeNode entitiesNode = new CtxMutableTreeNode(ENTITIES);
		top.add(entitiesNode);
		// доб. рекв.
		CtxMutableTreeNode requisitesNode = new CtxMutableTreeNode(REQUISITES);
		top.add(requisitesNode);
		// доб. кл.
		CtxMutableTreeNode keysNode = new CtxMutableTreeNode(KEYS);
		top.add(keysNode);
		// доб. мм.
		CtxMutableTreeNode mMsNode = new CtxMutableTreeNode(MMS);
		top.add(mMsNode);
		supressSelEvents = true;
		model.reload();
		for (int i = getRowCount() - 1; i >= 0; i--) {
			this.expandRow(i);
		}
		supressSelEvents = false;
	}

	public void setSupressSelEvents(boolean supressSelEvents) {
		this.supressSelEvents = supressSelEvents;
	}

	public boolean getSupressSelEvents() {
		return supressSelEvents;
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		TreePath tP = event.getPath();
		if (tP.getLastPathComponent() instanceof CtxMutableTreeNode) {
			CtxMutableTreeNode node = (CtxMutableTreeNode) tP
					.getLastPathComponent();
			supressSelEvents = true;
			if (!(node.getUserObject() instanceof String)) {
				node.removeAllChildren();
				model.reload(node);
			} else if (node.getUserObject() instanceof String
					&& node.getPath().length == 2) {
				node.removeAllChildren();
				model.reload(node);
			}
			supressSelEvents = false;
		}
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {

	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {

	}

	@Override
	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException {
		TreePath tP = event.getPath();
		if (tP.getLastPathComponent() instanceof CtxMutableTreeNode) {
			buildOneLevel((CtxMutableTreeNode) tP.getLastPathComponent());
		}

	}

	private void buildOneLevel(CtxMutableTreeNode node) {
		Object uO = node.getUserObject();
		if (uO instanceof ContextElement) {
			Set<ContextElement> set = new TreeSet<ContextElement>(
					new GeneralCtxElemsComparator());
			ContextElement el = (ContextElement) uO;
			if (el.isEntity()) {
				Entity curEnt = (Entity) el;

				// доб. рекв.
				if (!curEnt.getChildren().isEmpty()) {
					CtxMutableTreeNode curNodeReqs = new CtxMutableTreeNode(
							"requisites");
					node.add(curNodeReqs);
					set.addAll(curEnt.getChildren());
					for (ContextElement emb : set) {
						curNodeReqs.add(new CtxMutableTreeNode(emb));
					}
					set.clear();
				}

				// доб. родит.
				if (!curEnt.getParents().isEmpty()) {
					CtxMutableTreeNode curNodeParents = new CtxMutableTreeNode(
							"parents");
					node.add(curNodeParents);
					set.addAll(curEnt.getParents());
					for (ContextElement emb : set) {
						curNodeParents.add(new CtxMutableTreeNode(emb));
					}
					set.clear();
				}

				// доб. keyи в которых
				if (!curEnt.getKeysIn().isEmpty()) {
					CtxMutableTreeNode curNodeKeysIn = new CtxMutableTreeNode(
							"in keys");
					node.add(curNodeKeysIn);
					set.addAll(curEnt.getParents());
					for (ContextElement key : set) {
						curNodeKeysIn.add(new CtxMutableTreeNode(key));
					}
					set.clear();
				}

				// доб. keyи
				if (!curEnt.getKeysHas().isEmpty()) {
					CtxMutableTreeNode curNodeKeys = new CtxMutableTreeNode(
							"has keys");
					node.add(curNodeKeys);
					set.addAll(curEnt.getKeysHas());
					for (ContextElement key : set) {
						curNodeKeys.add(new CtxMutableTreeNode(key));
					}
					set.clear();
				}

				// доб. м-м
				if (!curEnt.getMMs().isEmpty()) {
					CtxMutableTreeNode curNodeMMs = new CtxMutableTreeNode(
							"M:M");
					node.add(curNodeMMs);
					set.addAll(curEnt.getMMs());
					for (ContextElement mm : set) {
						curNodeMMs.add(new CtxMutableTreeNode(mm));
					}
					set.clear();
				}
			} else if (el.isRequisite()) {
				Requisite curReq = (Requisite) el;
				// доб. родит.
				if (!curReq.getParents().isEmpty()) {
					CtxMutableTreeNode curNodeParents = new CtxMutableTreeNode(
							"parents");
					node.add(curNodeParents);
					set.addAll(curReq.getParents());
					for (ContextElement emb : set) {
						curNodeParents.add(new CtxMutableTreeNode(emb));
					}
					set.clear();
				}

				// доб. keyи в которых
				if (!curReq.getKeysIn().isEmpty()) {
					CtxMutableTreeNode curNodeKeysIn = new CtxMutableTreeNode(
							"in keys");
					node.add(curNodeKeysIn);
					set.addAll(curReq.getKeysIn());
					for (ContextElement key : set) {
						curNodeKeysIn.add(new CtxMutableTreeNode(key));
					}
					set.clear();
				}
				// доб. м-м
				if (!curReq.getMMs().isEmpty()) {
					CtxMutableTreeNode curNodeMMs = new CtxMutableTreeNode(
							"M:M");
					node.add(curNodeMMs);
					set.addAll(curReq.getMMs());
					for (ContextElement mm : set) {
						curNodeMMs.add(new CtxMutableTreeNode(mm));
					}
					set.clear();
				}
			} else if (el.isKey()) {
				Key curKey = (Key) el;
				// доб пон для
				CtxMutableTreeNode forEnt = new CtxMutableTreeNode(
						"for entity");
				node.add(forEnt);
				forEnt.add(new CtxMutableTreeNode(curKey.getEntity()));
				// доб состав
				if (!curKey.getKeyElements().isEmpty()) {
					CtxMutableTreeNode parts = new CtxMutableTreeNode("composition");
					node.add(parts);
					set.addAll(curKey.getKeyElements());
					for (ContextElement emb : set) {
						parts.add(new CtxMutableTreeNode(emb));
					}
					set.clear();
				}
			} else if (el.isMM()) {
				MM curMM = (MM) el;
				// доб состав
				if (!curMM.getMMElements().isEmpty()) {
					CtxMutableTreeNode parts = new CtxMutableTreeNode("composition");
					node.add(parts);
					set.addAll(curMM.getMMElements());
					for (ContextElement emb : set) {
						parts.add(new CtxMutableTreeNode(emb));
					}
					set.clear();
				}
			}
		} else if (uO instanceof String && node.getPath().length == 2) { // Zero
																			// level
																			// items
																			// are
																			// four
																			// titles
			Set<ContextElement> set = new TreeSet<ContextElement>(
					new GeneralCtxElemsComparator());
			if (((String) uO).equals(ENTITIES)) {
				set.addAll(ctx.getEntities());
			} else if (((String) uO).equals(REQUISITES)) {
				set.addAll(ctx.getRequisites());
			} else if (((String) uO).equals(KEYS)) {
				set.addAll(ctx.getKeys());
			} else if (((String) uO).equals(MMS)) {
				set.addAll(ctx.getMMs());
			}

			for (ContextElement el : set) {
				node.add(new CtxMutableTreeNode(el));
			}

		}
		supressSelEvents = true;
		model.reload(node);
		supressSelEvents = false;
	}

	@Override
	public void elementsAdded(ContextEvent e) {
		rebuild();
	}

	@Override
	public void elementsChanged(ContextEvent e) {
		rebuild();
	}

	@Override
	public void elementsRemoved(ContextEvent e) {
		rebuild();
	}

	public void updateSelection(Object source, Collection<Object> selection) {

		if (source == this) {
			return;
		}

		ArrayList<TreePath> paths = new ArrayList<TreePath>();
		for (Object selected : selection) {
			computeTreePathSelection(selected, paths);
		}

		TreePath[] tpaths = new TreePath[paths.size()];
		for (int i = 0; i < tpaths.length; i++) {
			tpaths[i] = paths.get(i);
		}

		// now we set the corresponding tree selection
		setSupressSelEvents(true);
		setSelectionPaths(tpaths);
		setSupressSelEvents(false);
	}

	/**
	 * Recursively adds to the array list all found paths to all treenodes
	 * containing the selected UserObject.
	 * 
	 * @param paths
	 *            List of paths found
	 * @param obj
	 *            UserObject which we should find paths to
	 */
	public void computeTreePathSelection(Object obj, ArrayList<TreePath> paths) {

		if (obj == null) {
			return;
		}

		// collecting all the tree nodes
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel()
				.getRoot();
		ArrayList<DefaultMutableTreeNode> allTreeNodes = new ArrayList<DefaultMutableTreeNode>();
		retrieveAllTreeNodes(root, allTreeNodes);

		// find all nodes, where our object is
		for (Iterator<DefaultMutableTreeNode> it1 = allTreeNodes.iterator(); it1
				.hasNext();) {
			DefaultMutableTreeNode cur = it1.next();
			if (cur.getUserObject() == obj) {
				paths.add(new TreePath(cur.getPath()));
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public void retrieveAllTreeNodes(DefaultMutableTreeNode node,
			ArrayList<DefaultMutableTreeNode> allTreeNodes) {
		allTreeNodes.add(node);
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) e
						.nextElement();
				retrieveAllTreeNodes(n, allTreeNodes);
			}
		}
	}

	@Override
	public UpdatingDelegate getUpdDelegate() {
		return updDeleg;
	}

	@Override
	public void elementConverted(ContextEvent e) {
		rebuild();
	}

}

@SuppressWarnings("serial")
class CtxTreeRenderer extends DefaultTreeCellRenderer {

	private static String IMAGE_ENT = "/com/describer/resources/images/ent.gif";
	private static String IMAGE_REQ = "/com/describer/resources/images/req.gif";
	private static String IMAGE_KEY = "/com/describer/resources/images/key.gif";
	private static String IMAGE_MM = "/com/describer/resources/images/mm.gif";
	private static String IMAGE_FLDR = "/com/describer/resources/images/folder.gif";

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		if (value instanceof CtxMutableTreeNode) {
			CtxMutableTreeNode node = (CtxMutableTreeNode) value;
			Object userObject = node.getUserObject();
			if (node.isEntityNode()) {
				setText(((Entity) userObject).getName());
				setToolTipText(userObject.toString() + "; "
						+ ((Entity) userObject).getSemantics());
				setIcon(createImageIcon(IMAGE_ENT));
			} else if (node.isRequisiteNode()) {
				setText(((Requisite) userObject).getName());
				setToolTipText(userObject.toString() + "; "
						+ ((Requisite) userObject).getSemantics());
				setIcon(createImageIcon(IMAGE_REQ));
			} else if (node.isKeyNode()) {
				setText(((Key) userObject).toString());
				setToolTipText(userObject.toString() + " для "
						+ ((Key) userObject).getEntity());
				setIcon(createImageIcon(IMAGE_KEY));
			} else if (node.isMMNode()) {
				setToolTipText(userObject.toString());
				setIcon(createImageIcon(IMAGE_MM));
			} else {
				setIcon(createImageIcon(IMAGE_FLDR));
			}
		}
		return this;
	}

	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = CtxTree.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

}
