package com.describer.presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.describer.context.Group;
import com.describer.context.Key;
import com.describer.context.Requisite;
import com.describer.util.Pair;

public class KeyPathsMgr extends JDialog {

	TreeDragSource ds;

	TreeDropTarget dt;

	AutoScrollingJTree tree;

	Key key;

	public KeyPathsMgr(Key key, Presenter presenter) {
		
		this.key = key;

		setSize(300, 200);
		setTitle("Paths manager");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// If you want autoscrolling, use this line:
		tree = new AutoScrollingJTree(key, presenter, this);
		// Otherwise, use this line:
		// tree = new JTree();
		getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
		pack();
		// If we only support move operations...
		ds = new TreeDragSource(tree, DnDConstants.ACTION_MOVE);
		// ds = new TreeDragSource(tree, DnDConstants.ACTION_COPY_OR_MOVE);
		dt = new TreeDropTarget(tree);
	}
}

class AutoScrollingJTree extends JTree implements Autoscroll, ActionListener,
		TreeSelectionListener, KeyListener {

	private DefaultTreeModel model;

	Presenter presenter;
	
	JDialog parent;

	private Key key;

	private int margin = 12;

	// Used to transfer node between ds and dt
	DefaultMutableTreeNode oldNode;

	// Used to store node between popup creation and choosing an action
	DefaultMutableTreeNode node;

	public AutoScrollingJTree(Key key, Presenter presenter, JDialog parent) {
		this.parent = parent;
		this.presenter = presenter;
		this.key = key;
		model = new DefaultTreeModel(null);
		this.setModel(model);
		createNodes();
		// Add listener to the text area so the popup menu can come up.
		MouseListener popupListener = new PopupListener();
		addMouseListener(popupListener);
		addTreeSelectionListener(this);
		addKeyListener(this);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		if (!(node.getUserObject() instanceof UOWrapper<?>))
			return;
		Object uO = ((UOWrapper<?>) node.getUserObject()).getUO();
		if (uO instanceof Key) {
			Set<Object> s = new HashSet<Object>();
			s.add(uO);
			s.add(((Key) uO).getEntity());
			s.add(new Pair<Object, Object>(((Key) uO).getEntity(), uO));
			for (Group g : key.grMgr.getGroups()) {
				s.add(g.getMainReq());
			}
			presenter.setSelection(s);
		} else if (uO instanceof Requisite) {
			Set<Object> s = new HashSet<Object>();
			s.add(uO);
			s.add(key);
			s.add(new Pair<Object, Object>(key, uO));
			// s.add(key.getEntity());
			// s.add(new Pair<Object, Object>(key.getEntity(), key));
			presenter.setSelection(s);
		} else if (uO instanceof Group) {
			Group g = (Group) uO;
			Set<Group> s = g.allPathsNodes();
			Set<Object> res = new HashSet<Object>(); 
			res.add(key);
			// res.add(key.getEntity());
			// res.add(new Pair<Object, Object>(key.getEntity(), key));
			res.add(new Pair<Object, Object>(key, g.getOwner().getEntity()));
			for (Group gr : s) {

				res.add(gr.getOwner());
				res.add(gr.getOwner().getEntity());
				res.add(new Pair<Object, Object>(gr.getOwner().getEntity(), gr.getOwner()));

				for (Group d : gr.getInGroups()) {
					if (s.contains(d))
						res.add(new Pair<Object, Object>(gr.getOwner(), d.getOwner().getEntity()));
				}
				if (gr.getReq() != null) {
					res.add(gr.getReq());
					res.add(new Pair<Object, Object>(gr.getOwner(), gr.getReq()));
				}
				/*
				Group gro = null;
				if ((gro = key.grMgr.groupByInGroup(gr)) != null) {
					res.add(new Pair<Object, Object>(key, gr.getOwner().getEntity()));
				}*/
			}
			presenter.setSelection(res);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("separate")) {
			if (!(node.getUserObject() instanceof UOWrapper<?>))
				return;
			if (!(((DefaultMutableTreeNode) node.getParent()).getUserObject() instanceof UOWrapper<?>))
				return;
			if (((UOWrapper<?>) node.getUserObject()).getUO() instanceof Requisite)
				return;
			Group inGroup = ((UOWrapper<Group>) node.getUserObject()).getUO();
			Group from = ((UOWrapper<Group>) ((DefaultMutableTreeNode) node
					.getParent()).getUserObject()).getUO();
			if (key.grMgr.separateInGroup(inGroup, from))
				rebuild();
		} else if (e.getActionCommand().equals("delete")) {
			if (!(node.getUserObject() instanceof UOWrapper<?>))
				return;
			if (!(((DefaultMutableTreeNode) node.getParent()).getUserObject() instanceof UOWrapper<?>))
				return;
			if (!(((UOWrapper<?>) ((DefaultMutableTreeNode) node.getParent())
					.getUserObject()).getUO() instanceof Key))
				return;
			Group gr = ((UOWrapper<Group>) node.getUserObject()).getUO();
			key.grMgr.deleteGroup(gr);
			rebuild();
		} else if (e.getActionCommand().equals("rename")) {
			if (!(node.getUserObject() instanceof UOWrapper<?>))
				return;
			if (node.getParent() == null)
				return;
			if (!(((DefaultMutableTreeNode) node.getParent()).getUserObject() instanceof UOWrapper<?>))
				return;
			if (!(((UOWrapper<?>) ((DefaultMutableTreeNode) node.getParent())
					.getUserObject()).getUO() instanceof Key))
				return;
			Group gr = ((UOWrapper<Group>) node.getUserObject()).getUO();
			NewNameWindow name = new NewNameWindow(gr);
			name.setLocationRelativeTo(presenter);
			name.setModal(true);
			name.setVisible(true);
			name.dispose();
			rebuild();
		}
	}

	class PopupListener extends MouseAdapter {

		public JPopupMenu createPopupMenu() {
			JMenuItem menuItem = null;
			// Create the popup menu.
			JPopupMenu popup = null;
			if (node.getPath().length == 3) { // InGroup item
				popup = new JPopupMenu();
				menuItem = new JMenuItem("Separate");
				menuItem.setActionCommand("separate");
				menuItem.addActionListener(AutoScrollingJTree.this);
				popup.add(menuItem);
			} else if (node.getPath().length == 2) { // Group item
				popup = new JPopupMenu();
				menuItem = new JMenuItem("Rename");
				menuItem.setActionCommand("rename");
				menuItem.addActionListener(AutoScrollingJTree.this);
				popup.add(menuItem);
				menuItem = new JMenuItem("Delete");
				menuItem.setActionCommand("delete");
				menuItem.addActionListener(AutoScrollingJTree.this);
				popup.add(menuItem);
			} 
			return popup;
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {

			TreePath path = getPathForLocation(e.getX(), e.getY());
			if ((path == null) || (path.getPathCount() <= 1)) {
				// We can't move the root node or an empty selection
				return;
			}

			node = (DefaultMutableTreeNode) path.getLastPathComponent();

			if (e.isPopupTrigger()) {
				JPopupMenu popup = createPopupMenu();
				if (popup != null) { // null if item hasn't menu
					popup.show(e.getComponent(), e.getX(), e.getY());
					AutoScrollingJTree.this.setSelectionPath(path);
				}
			}
		}
	}

	public void expandAll() {
		int row = 0;
		while (row < this.getRowCount()) {
			this.expandRow(row);
			row++;
		}
	}

	private void createNodes() {
		rebuild();
	}

	public void rebuild() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				new UOWrapper<Key>(key, key.getEntity().getName() + " - "
						+ key.toString()));
		model.setRoot(top);
		for (Group g : key.grMgr.getGroups()) {
			DefaultMutableTreeNode n = new DefaultMutableTreeNode(
					new UOWrapper<Group>(g, g.getName() + " - "
							+ g.getMainReq().getName()));
			for (Group gr : g.getInGroups()) {
				DefaultMutableTreeNode in = new DefaultMutableTreeNode(
						new UOWrapper<Group>(gr, gr.getOwner().getEntity()
								.getName()
								+ " - " + gr.getName()));
				model.insertNodeInto(in, n, n.getChildCount());
			}
			if (g.getReq() != null) {
				DefaultMutableTreeNode in = new DefaultMutableTreeNode(
						new UOWrapper<Requisite>(g.getReq(), g.getReq()
								.getName()));
				model.insertNodeInto(in, n, n.getChildCount());
			}
			model.insertNodeInto(n, top, top.getChildCount());
		}
		expandAll();
	}

	public void autoscroll(Point p) {
		int realrow = getRowForLocation(p.x, p.y);
		Rectangle outer = getBounds();
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
				: realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		scrollRowToVisible(realrow);
	}

	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x
				+ margin, outer.height - inner.height - inner.y + outer.y
				+ margin, outer.width - inner.width - inner.x + outer.x
				+ margin);
	}

	// Use this method if you want to see the boundaries of the
	// autoscroll active region

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		/*
		 * g.setColor(Color.red); g.drawRect(-outer.x + 12, -outer.y + 12,
		 * inner.width - 24, inner.height - 24);
		 */
	}

	@SuppressWarnings("unchecked")
	public boolean canBeMoved(DefaultMutableTreeNode child,
			DefaultMutableTreeNode parent) {
		if (!(child.getUserObject() instanceof UOWrapper<?>)
				|| child.getPath().length != 3)
			return false;
		if (!(parent.getUserObject() instanceof UOWrapper<?>)
				|| parent.getPath().length != 2)
			return false;
		if (!(((DefaultMutableTreeNode) child.getParent()).getUserObject() instanceof UOWrapper<?>))
			return false;
		if (((UOWrapper<?>) child.getUserObject()).getUO() instanceof Requisite)
			return false;
		Group ch = ((UOWrapper<Group>) child.getUserObject()).getUO();
		Group par = ((UOWrapper<Group>) parent.getUserObject()).getUO();
		Group oldPar = ((UOWrapper<Group>) ((DefaultMutableTreeNode) child
				.getParent()).getUserObject()).getUO();
		// key's groups can't be moved
		if (key.grMgr.getGroups().contains(ch))
			return false;
		// key's groups must conatain parent
		if (!key.grMgr.getGroups().contains(par))
			return false;
		key.grMgr.moveInGroup(ch, oldPar, par);
		return true;
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE : 
			parent.dispose(); break;
		case KeyEvent.VK_ENTER : 
			node = (DefaultMutableTreeNode) getSelectionPath().getLastPathComponent();
			actionPerformed(new ActionEvent(this, 0, "rename"));
		}
			
	}

	@Override
	public void keyReleased(KeyEvent e) {}

}

// TreeDragSource.java
// A drag source wrapper for a JTree. This class can be used to make
// a rearrangeable DnD tree with the TransferableTreeNode class as the
// transfer data type.

class TreeDragSource implements DragSourceListener, DragGestureListener {

	DragSource source;

	DragGestureRecognizer recognizer;

	TransferableTreeNode transferable;

	AutoScrollingJTree sourceTree;

	public TreeDragSource(AutoScrollingJTree tree, int actions) {
		sourceTree = tree;
		source = new DragSource();
		recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
				actions, this);
	}

	/*
	 * Drag Gesture Handler
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = sourceTree.getSelectionPath();
		if ((path == null) || (path.getPathCount() <= 1)) {
			// We can't move the root node or an empty selection
			return;
		}

		sourceTree.oldNode = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		transferable = new TransferableTreeNode(path);
		source.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);

		// If you support dropping the node anywhere, you should probably
		// start with a valid move cursor:
		// source.startDrag(dge, DragSource.DefaultMoveDrop, transferable,
		// this);
	}

	/*
	 * Drag Event Handlers
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		/*
		 * to support move or copy, we have to check which occurred:
		 */
		/*
		 * System.out.println("Drop Action: " + dsde.getDropAction()); if
		 * (dsde.getDropSuccess() && (dsde.getDropAction() ==
		 * DnDConstants.ACTION_MOVE)) { ((DefaultTreeModel)
		 * sourceTree.getModel()).removeNodeFromParent(oldNode); }
		 */

		/*
		 * to support move only... if (dsde.getDropSuccess()) {
		 * ((DefaultTreeModel
		 * )sourceTree.getModel()).removeNodeFromParent(oldNode); }
		 */
	}
}

// TreeDropTarget.java
// A quick DropTarget that's looking for drops from draggable JTrees.
//

class TreeDropTarget implements DropTargetListener {

	DropTarget target;

	AutoScrollingJTree targetTree;

	public TreeDropTarget(AutoScrollingJTree tree) {
		targetTree = tree;
		target = new DropTarget(targetTree, this);
	}

	/*
	 * Drop Event Handlers
	 */
	private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
		Point p = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath path = tree.getClosestPathForLocation(p.x, p.y);
		return (TreeNode) path.getLastPathComponent();
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		TreeNode node = getNodeForEvent(dtde);
		/*
		 * if (node.isLeaf()) { dtde.rejectDrag(); } else
		 */{
			// start by supporting move operations
			// dtde.acceptDrag(DnDConstants.ACTION_MOVE);
			dtde.acceptDrag(dtde.getDropAction());
		}
	}

	public void dragOver(DropTargetDragEvent dtde) {
		TreeNode node = getNodeForEvent(dtde);
		/*
		 * if (node.isLeaf()) { dtde.rejectDrag(); } else
		 */{
			// start by supporting move operations
			// dtde.acceptDrag(DnDConstants.ACTION_MOVE);
			dtde.acceptDrag(dtde.getDropAction());
		}
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {
		Point pt = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		AutoScrollingJTree tree = (AutoScrollingJTree) dtc.getComponent();
		TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentpath
				.getLastPathComponent();

		if (parent.getPath().length == 3)
			parent = (DefaultMutableTreeNode) parent.getParent();

		if (!tree.canBeMoved(tree.oldNode, parent)) {
			dtde.rejectDrop();
			return;
		}

		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (tr.isDataFlavorSupported(flavors[i])) {
					dtde.acceptDrop(dtde.getDropAction());
					tree.rebuild();
					/*
					 * DefaultTreeModel model = (DefaultTreeModel)
					 * tree.getModel();
					 * model.removeNodeFromParent(tree.oldNode);
					 * model.insertNodeInto(tree.oldNode, parent, 0);
					 */
					dtde.dropComplete(true);
					return;
				}
			}
			dtde.rejectDrop();
		} catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}
}

// TransferableTreeNode.java
// A Transferable TreePath to be used with Drag & Drop applications.
//

class TransferableTreeNode implements Transferable {

	public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class,
			"Tree Path");

	DataFlavor flavors[] = { TREE_PATH_FLAVOR };

	TreePath path;

	public TransferableTreeNode(TreePath tp) {
		path = tp;
	}

	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.getRepresentationClass() == TreePath.class);
	}

	public synchronized Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavor)) {
			return (Object) path;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}

class UOWrapper<T> {

	private T o;

	private String name;

	public UOWrapper(T o, String name) {
		this.o = o;
		this.name = name;
	}

	public T getUO() {
		return o;
	}

	public String toString() {
		return name;
	}

}

class NewNameWindow extends JDialog implements ActionListener, KeyListener {

	private JTextField name;
	private JLabel label;
	private JButton button;
	private Group group;

	public NewNameWindow(Group gr) {
		group = gr;
		initComponents();
	}

	private void initComponents() {
		name = new JTextField();
		label = new JLabel();
		button = new JButton("OK");
		button.addActionListener(this);
		button.setActionCommand("OK");

		setTitle("New name");

		label.setText("Enter new name:");
		name.setText(group.getName());
		name.setPreferredSize(new Dimension(200,25));
		name.addActionListener(this);
		name.setActionCommand("OK");
		
		JPanel pane = new JPanel();
		
		button.addKeyListener(this);
		name.addKeyListener(this);
		
		pane.add(label);
		pane.add(name);
		pane.add(button);
		
		getContentPane().add(pane);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("OK")) {
			group.setName(name.getText());
			dispose();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			dispose();
	}

	@Override
	public void keyReleased(KeyEvent e) {}

}