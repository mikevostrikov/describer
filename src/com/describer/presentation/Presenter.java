package com.describer.presentation;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.xml.sax.SAXException;

import com.describer.CtxAbstractAction;
import com.describer.context.Context;
import com.describer.context.ContextElement;
import com.describer.context.ContextEvent;
import com.describer.context.ContextListener;
import com.describer.presentation.graph.CtxCellViewFactory;
import com.describer.presentation.graph.CtxGraph;
import com.describer.presentation.props.CtxProperties;
import com.describer.presentation.props.CtxTableModel;
import com.describer.presentation.tree.CtxTree;
import com.describer.presentation.upd.Updateable;
import com.describer.presentation.upd.UpdatingDelegate;

@SuppressWarnings("serial")
public class Presenter extends JPanel implements ContextListener,
		GraphSelectionListener, TreeSelectionListener, ActionListener {

	private Context ctx;
	private CtxGraph graph;
	private CtxTree tree;
	private CtxProperties props;

	private CtxAbstractAction deleteAction;

	// selection === set of selected user objects
	// used for selection synchronization
	private Set<Object> selection = new HashSet<Object>();

	public Presenter() {

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		graph = graphInit();
		tree = treeInit();
		props = propsInit();

		initActions();

		JScrollPane sGraph = new JScrollPane(graph);
		JScrollPane sTree = new JScrollPane(tree);
		JScrollPane sProps = new JScrollPane(props);

		// sGraph.setPreferredSize(new Dimension(200,0));
		sProps.setPreferredSize(new Dimension(200, 80));
		sProps.setMinimumSize(new Dimension(200, 80));
		// sProps.setMaximumSize(new Dimension(200, 80));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		sTree.setPreferredSize(new Dimension(200, screenSize.height
				- sProps.getSize().height - 200));
		sTree.setMinimumSize(new Dimension(200, 450));

		JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sTree,
				sProps);
		JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pane1,
				sGraph);
		this.add(pane2);
		// container.add( new JScrollPane(pane) );
	}

	private void initActions() {
		class DeleteAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "delete-command";
			private static final String NAME = "Exit";
			private static final String SMALL_ICON = "_16x16/Exit.png";
			private static final String LARGE_ICON = "_24x24/Exit.png";
			private static final String SHORT_DESCRIPTION = "Exit application";
			private static final String LONG_DESCRIPTION = "Exit application";
			private static final int MNEMONIC_KEY = 'O';

			/**
			 * ctor
			 */
			public DeleteAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		deleteAction = new DeleteAction();
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("DELETE"), "delete-action");
		getActionMap().put("delete-action", deleteAction);
		deleteAction.addActionListener(this);

	}

	private CtxGraph graphInit() {
		GraphModel model = new DefaultGraphModel();
		GraphLayoutCache view = new GraphLayoutCache(model,
				new CtxCellViewFactory());
		CtxGraph jgraph = new CtxGraph(model, view);
		jgraph.addGraphSelectionListener(this);
		jgraph.setGridVisible(true);
		jgraph.setPresenter(this);
		jgraph.setInvokesStopCellEditing(true);
		// adjust selection view
		GraphConstants.SELECTION_STROKE = new BasicStroke(5,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{0.5f, 0.5f}, 0.0f);
		return jgraph;
	}

	private CtxTree treeInit() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("entitiesTree");
		CtxTree tree = new CtxTree(top);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(this);
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setPresenter(this);
		return tree;
	}

	private CtxProperties propsInit() {
		CtxTableModel tModel = new CtxTableModel();
		CtxProperties props = new CtxProperties(tModel);
		props.setFillsViewportHeight(true);
		props.setPreferredScrollableViewportSize(new Dimension(500, 70));
		props.setPresenter(this);
		return props;
	}

	public void setGraph(CtxGraph graph) {
		this.graph = graph;
	}

	public CtxGraph getGraph() {
		return graph;
	}

	public void setTree(CtxTree tree) {
		this.tree = tree;
	}

	public CtxTree getTree() {
		return tree;
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
		graph.setCtx(ctx);
		tree.setCtx(ctx);
		props.setCtx(ctx);
	}

	public void setProps(CtxProperties props) {
		this.props = props;
	}

	public CtxProperties getProps() {
		return props;
	}

	public Set<Object> getSelection() {
		return selection;
	}

	/**
	 * Populate selCells with graph cells, containing selected user object
	 * 
	 * @param selected
	 * @param selCells
	 */
	@SuppressWarnings("unused")
	private void computeGraphCellSelection(Object selected,
			ArrayList<DefaultGraphCell> selCells) {
		// Get all graph cells
		ArrayList<DefaultGraphCell> allGraphCells = new ArrayList<DefaultGraphCell>();
		Object[] roots = graph.getRoots();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i] instanceof DefaultGraphCell) {
				getGraphCellsUnderRoot((DefaultGraphCell) roots[i],
						allGraphCells);
			}
		}

		// Find containing selected user object among them
		for (DefaultGraphCell cell : allGraphCells) {
			if (cell.getUserObject() == selected) {
				selCells.add(cell);
			}
		}

	}

	private void getGraphCellsUnderRoot(DefaultGraphCell root,
			ArrayList<DefaultGraphCell> allGraphCells) {

		allGraphCells.add(root);

		for (Object child : root.getChildren()) {
			if (child instanceof DefaultGraphCell) {
				getGraphCellsUnderRoot((DefaultGraphCell) child, allGraphCells);
			}
		}

	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		valueChanged((EventObject) e);
	}

	@Override
	public void valueChanged(GraphSelectionEvent e) {
		valueChanged((EventObject) e);
	}

	// Encapsulates updater's behavior
	public void valueChanged(EventObject ev) {
		Object source = ev.getSource();
		if (source == null)
			return;
		if (!(source instanceof Updateable))
			return;
		UpdatingDelegate deleg = ((Updateable) source).getUpdDelegate();
		if (deleg == null)
			return;
		if (deleg.isBlocked())
			return;
		if (ev instanceof GraphSelectionEvent) {
			process((GraphSelectionEvent) ev);
		} else if (ev instanceof TreeSelectionEvent) {
			process((TreeSelectionEvent) ev);
		}
		fireUpdate(deleg);
	}

	private void fireUpdate(UpdatingDelegate deleg) {
		graph.getUpdDelegate().update(deleg);
		tree.getUpdDelegate().update(deleg);
		props.getUpdDelegate().update(deleg);
	}

	// Updates selected objects array
	private void process(GraphSelectionEvent e) {

		selection.clear();

		Object[] graphSelection = graph.getSelectionModel().getSelectionCells();

		for (int i = 0; i < graphSelection.length; i++) {
			Object selected = graphSelection[i];
			if (!graph.getModel().isPort(selected)) {
				if (selected instanceof DefaultGraphCell) {
					Object uO = ((DefaultGraphCell) selected).getUserObject();
					if (uO instanceof ContextElement)
						selection.add((ContextElement) uO);
				}
			}
		}

	}

	private void process(TreeSelectionEvent e) {

		TreePath[] treeSelection = tree.getSelectionModel().getSelectionPaths();

		if (treeSelection == null) {
			return;
		}

		selection.clear();

		for (int i = 0; i < treeSelection.length; i++) {
			if (treeSelection[i].getLastPathComponent() instanceof DefaultMutableTreeNode) {
				Object uO = ((DefaultMutableTreeNode) treeSelection[i]
						.getLastPathComponent()).getUserObject();
				// Selection must contain only ContextElements
				if (uO instanceof ContextElement)
					selection.add((ContextElement) uO);
				assert uO instanceof ContextElement || uO instanceof String : uO;
			}
		}

	}

	public void setSelection(Collection<?> uOs) {
		selection.clear();
		for (Object uO : uOs) {
			//if (uO instanceof ContextElement)
				selection.add(/*(ContextElement)*/ uO);
		}
		fireUpdate(null);
	}
	
	public void select(Object o) {
		setSelection(java.util.Collections.singleton(o));				
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
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
				.getRoot();
		ArrayList<DefaultMutableTreeNode> allTreeNodes = new ArrayList<DefaultMutableTreeNode>();
		getAllTreeNodes(root, allTreeNodes);

		// find all nodes, where our object is
		for (Iterator<DefaultMutableTreeNode> it1 = allTreeNodes.iterator(); it1
				.hasNext();) {
			DefaultMutableTreeNode cur = it1.next();
			if (cur.getUserObject() == obj) {
				paths.add(new TreePath(cur.getPath()));
			}
		}

	}

	@SuppressWarnings("unchecked")
	public void getAllTreeNodes(DefaultMutableTreeNode node,
			ArrayList<DefaultMutableTreeNode> allTreeNodes) {

		allTreeNodes.add(node);

		if (node.getChildCount() >= 0) {
			for (Enumeration<DefaultMutableTreeNode> e = node.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode n = e.nextElement();
				getAllTreeNodes(n, allTreeNodes);
			}
		}
	}

	public void loadCoords(InputStream is) throws ParserConfigurationException,
			SAXException, IOException {
		graph.loadCoords(is);
	}

	public void saveCoords(OutputStream os)
			throws ParserConfigurationException, TransformerException,
			IOException {
		graph.saveCoords(os);
	}

	public void exportGraph(File file) throws IOException {
		graph.export(file);
	}

	public void showPreview(JFrame main) {
		PrintPreview prv = new PrintPreview(main, graph, "Print Preview");
		prv.setModal(true);
		prv.setVisible(true);

	}

	public void autoLayout() {
		graph.doAutoLayout();
	}

	@Override
	public void elementsAdded(ContextEvent e) {
		graph.elementsAdded(e);
		tree.elementsAdded(e);
		props.elementsAdded(e);
	}

	@Override
	public void elementsChanged(ContextEvent e) {
		graph.elementsChanged(e);
		tree.elementsChanged(e);
		props.elementsChanged(e);
	}

	@Override
	public void elementsRemoved(ContextEvent e) {
		graph.elementsRemoved(e);
		tree.elementsRemoved(e);
		props.elementsRemoved(e);
	}

	@Override
	public void elementConverted(ContextEvent e) {
		graph.elementConverted(e);
		tree.elementConverted(e);
		props.elementConverted(e);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (deleteAction.isMyEvent(ev)) {
			Set<ContextElement> selection_ = new HashSet<ContextElement>();
			for (Object o : selection) {
				if (o instanceof ContextElement)
					selection_.add((ContextElement) o);
			}			
			/*
			 * for (Object obj : selection_) { if (obj instanceof
			 * ContextElement) { ctx.remove((ContextElement) obj); } }
			 */
			ctx.removeAll(selection_);
			graph.removeSelectedEdges();
		}
	}

}
