package com.describer.presentation.graph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.TreeNode;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.PortView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.describer.context.Context;
import com.describer.context.ContextElement;
import com.describer.context.ContextEvent;
import com.describer.context.Entity;
import com.describer.context.Key;
import com.describer.context.MM;
import com.describer.context.Node;
import com.describer.context.Requisite;
import com.describer.presentation.KeyPathsMgr;
import com.describer.presentation.Presenter;
import com.describer.presentation.PresenterListener;
import com.describer.presentation.upd.AbstractUpdatingDelegate;
import com.describer.presentation.upd.Updateable;
import com.describer.presentation.upd.UpdatingDelegate;
import com.describer.util.Pair;
import com.describer.util.Utils;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

@SuppressWarnings("serial")
public class CtxGraph extends JGraph implements java.awt.print.Printable,
		PresenterListener, Updateable {
	private Context ctx;

	private Presenter presenter;

	// stores vertexes and UOs
	private HashMap<Object, DefaultGraphCell> uObjVertTable = new HashMap<Object, DefaultGraphCell>();
	// stores all cells (vertexes and edges)
	private List<DefaultGraphCell> cells = new LinkedList<DefaultGraphCell>();

	private boolean supressSelEvents = false;

	// used for placing new vertexes
	private Point2D activePoint = null;

	private UpdatingDelegate updDeleg = new AbstractUpdatingDelegate() {
		protected void update() {
			List<DefaultGraphCell> selectedCells = new ArrayList<DefaultGraphCell>();

			/*
			 * // remain currently selected edges Object[] curSelected =
			 * getSelectionModel().getSelectionCells(); for (int i = 0; i <
			 * curSelected.length; i++) { if (curSelected[i] instanceof
			 * DefaultEdge) selectedCells.add((DefaultGraphCell)
			 * curSelected[i]); }
			 */

			Collection<?> selection = presenter.getSelection();

			for (Object uO : selection) {
				if (uO instanceof Pair<?,?>) {
					selectedCells.add(getEdgeBySNTObjs(((Pair<?,?>) uO).getFirst(), ((Pair<?,?>) uO).getSecond()));
				} else {
					selectedCells.add(getCellByObject(uO));
				}
			}

			// highlight edges among selected elements as well
			/*for (Object uO : selection) {
				if (uO instanceof ContextElement) {
					ContextElement el = (ContextElement) uO;
					for (ContextElement dep : el.getDirectDependants()) {
						if (selection.contains(dep))
							selectedCells.add(getEdgeBySNTObjs(el, dep));
					}
				} else {
					assert false : selection;
				}
			}
			*/

			// now we set the corresponding tree selection
			getSelectionModel().setSelectionCells(selectedCells.toArray());
		}
	};

	// ctor
	public CtxGraph(GraphModel model, GraphLayoutCache view) {
		super(model, view);
		initActions();
		setMarqueeHandler(new CtxMarqueeHandler(this));
		setHighlightColor(Color.BLUE);
	}

	public void initActions() {

		/*
		 * delete = new DeleteAction();
		 * this.getInputMap().put(KeyStroke.getKeyStroke("DELETE"),
		 * "delete-action"); this.getActionMap().put("delete-action", delete);
		 * delete.addActionListener(this);
		 */
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	/*
	 * public void rebuild() {
	 * 
	 * GraphModel model = new DefaultGraphModel();
	 * 
	 * model.addUndoableEditListener(new CtxGraphUndoableEditListener());
	 * 
	 * model.addGraphModelListener(new CtxGraphModelListener());
	 * 
	 * setModel(model);
	 * 
	 * GraphLayoutCache view = new GraphLayoutCache(model, new
	 * CtxCellViewFactory());
	 * 
	 * setGraphLayoutCache(view);
	 * 
	 * // Control-drag should clone selection setCloneable(true);
	 * 
	 * // Enable edit without final RETURN keystroke
	 * setInvokesStopCellEditing(true);
	 * 
	 * // When over a cell, jump to its default port (we only have one, anyway)
	 * setJumpToDefaultPort(true);
	 * 
	 * cells = new ArrayList<DefaultGraphCell>();
	 * 
	 * // show reqs Set<Requisite> reqs = ctx.getRequisites(); for (Requisite
	 * req : reqs){ DefaultGraphCell cell = createVertex(req); cells.add(cell);
	 * vertUObjTable.put(cell, cell.getUserObject()); }
	 * 
	 * // show ents Set<Entity> ents = ctx.getEntities(); for (Entity ent :
	 * ents){ DefaultGraphCell cell = createVertex(ent); cells.add(cell);
	 * vertUObjTable.put(cell, cell.getUserObject()); }
	 * 
	 * Set<Key> keys = ctx.getKeys(); // show keys for (Key key : keys) {
	 * DefaultGraphCell cell = createVertex(key); cells.add(cell);
	 * vertUObjTable.put(cell, cell.getUserObject()); }
	 * 
	 * // show ents-keys edges for (Entity ent : ents){ for (Key key :
	 * ent.getKeysHas()){ cells.add(createEdge(ent, key)); } }
	 * 
	 * // show keys-embs edges for (Entity ent : ents){ for (Key key :
	 * ent.getKeysIn()){ cells.add(createEdge(key, ent)); } } for (Requisite req
	 * : reqs){ for (Key key : req.getKeysIn()){ cells.add(createEdge(key,
	 * req)); } }
	 * 
	 * // show reqs-ents edges (non-keys) for (Entity ent : ents){ Set<Node>
	 * allKeyElements = new HashSet<Node>(); for (Key key : ent.getKeysHas()) {
	 * allKeyElements.addAll(key.getKeyElements()); } for (Node emb :
	 * ent.getChildren()){ if (!allKeyElements.contains(emb)) {
	 * cells.add(createEdge(ent, emb)); } } }
	 * 
	 * Set<MM> mMs = ctx.getMMs(); // show MMs for (MM mM : mMs){
	 * DefaultGraphCell cell = createVertex(mM); cells.add(cell);
	 * vertUObjTable.put(cell, cell.getUserObject()); }
	 * 
	 * // show emb-MM edges for (MM mM : mMs) { for (Node emb :
	 * mM.getMMElements()){ cells.add(createEdge(emb, mM)); } }
	 * 
	 * System.out.println();
	 * 
	 * // // Insert the cells via the cache, so they get selected //
	 * getGraphLayoutCache().insert(cells.toArray());
	 * 
	 * }
	 */

	@SuppressWarnings("unchecked")
	public void doAutoLayout() {
		// Aligning cells
		JGraphFacade facade = new JGraphFacade(this); // Pass the facade the
		// JGraph instance
		JGraphLayout layout = new JGraphHierarchicalLayout(); // Create an
		// instance of the
		// appropriate
		// layout

		layout.run(facade); // Run the layout on the facade. Note that layouts
		// do not implement the Runnable interface, to avoid
		// confusion
		Map<DefaultGraphCell, AttributeMap> nested = facade.createNestedMap(
				true, true); // Obtain a map of the resulting attribute changes
		// from the facade
		getGraphLayoutCache().edit(nested); // Apply the results to the actual
		// graph
	}

	private void createVertex(ContextElement el) {
		DefaultGraphCell cell = null;

		if (el.isEntity()) {
			cell = createVertex((Entity) el);
		} else if (el.isRequisite()) {
			cell = createVertex((Requisite) el);
		} else if (el.isKey()) {
			cell = createVertex((Key) el);
		} else if (el.isMM()) {
			cell = createVertex((MM) el);
		}

		// Insert the cells via the cache, so they get selected
		if (cell != null) {
			uObjVertTable.put(cell.getUserObject(), cell);
			cells.add(cell);
			if (activePoint != null)
				GraphConstants.setBounds(cell.getAttributes(),
						new Rectangle2D.Double(activePoint.getX(), activePoint
								.getY(), 0, 0));
			activePoint = null;
			getGraphLayoutCache().insert(cell);
		}

	}

	private DefaultGraphCell createVertex(final Key key) {

		// Create vertex with the given name
		DefaultGraphCell cell = new DefaultGraphCell(key) {
			@Override
			public String toString() {
				return "" + key;
			}
		};

		// set the view class (indirection for the renderer and the editor)
		CtxCellViewFactory.setViewClass(cell.getAttributes(),
				JGraphCycleView.class.getCanonicalName());

		Color bg = new Color(39, 149, 211);

		// Set autosize
		GraphConstants.setAutoSize(cell.getAttributes(), true);
		GraphConstants.setSizeable(cell.getAttributes(), false);

		// Set fill color
		if (bg != null) {
			GraphConstants.setGradientColor(cell.getAttributes(), bg);
			GraphConstants.setOpaque(cell.getAttributes(), true);
		}

		// Set raised border
		/*
		 * if (false) GraphConstants.setBorder(cell.getAttributes(),
		 * BorderFactory .createRaisedBevelBorder()); else // Set black border
		 * GraphConstants.setBorderColor(cell.getAttributes(), Color.black);
		 */

		// Add a Floating Port
		cell.addPort();

		// Set not editable
		GraphConstants.setEditable(cell.getAttributes(), false);

		return cell;
	}

	private DefaultGraphCell createVertex(Node emb) {

		// Create vertex with the given name
		DefaultGraphCell cell = new DefaultGraphCell(emb) {
			@Override
			public String toString() {
				if (this.getUserObject() instanceof Node)
					return ((Node) this.getUserObject()).getName();
				return getUserObject().toString();
			}
		};

		// set the view class (indirection for the renderer and the editor)
		CtxCellViewFactory.setViewClass(cell.getAttributes(),
				JGraphRoundRectView.class.getCanonicalName());

		Color bg = null;

		if (emb instanceof Node) {
			if (emb instanceof Entity) {
				bg = new Color(255, 243, 53);
			} else if (emb instanceof Requisite) {
				bg = new Color(0, 255, 0);
			}
		}

		// Set autosize
		GraphConstants.setAutoSize(cell.getAttributes(), true);

		// Set inset
		GraphConstants.setInset(cell.getAttributes(), 2);

		// Set fill color
		if (bg != null) {
			GraphConstants.setGradientColor(cell.getAttributes(), bg);
			GraphConstants.setOpaque(cell.getAttributes(), true);
		}

		// Set black border
		GraphConstants.setBorderColor(cell.getAttributes(), Color.black);

		// Add a Floating Port
		cell.addPort();

		// Set not editable
		GraphConstants.setEditable(cell.getAttributes(), true);

		return cell;
	}

	private DefaultGraphCell createVertex(MM mM) {

		// Name is set to JLabel calling jgraph.convertValueToString
		// function with CellView argument. Then the function
		// gets corresponding GraphCell and calls it's toString
		// so we should override it to display what we want.
		DefaultGraphCell cell = new DefaultGraphCell(mM) {
			@Override
			public String toString() {
				return "M:M";
			}
		};

		// set the view class (indirection for the renderer and the editor)
		CtxCellViewFactory.setViewClass(cell.getAttributes(),
				JGraphEllipseView.class.getCanonicalName());

		Color bg = new Color(214, 168, 206);

		// Set autosize
		GraphConstants.setAutoSize(cell.getAttributes(), true);

		// Set fill color
		if (bg != null) {
			GraphConstants.setGradientColor(cell.getAttributes(), bg);
			GraphConstants.setOpaque(cell.getAttributes(), true);
		}

		// Set raised border
		if (true)
			GraphConstants.setBorder(cell.getAttributes(), BorderFactory
					.createRaisedBevelBorder());
		else
			// Set black border
			GraphConstants.setBorderColor(cell.getAttributes(), Color.black);

		// Set not editable
		GraphConstants.setEditable(cell.getAttributes(), false);

		// Add a Floating Port
		cell.addPort();

		return cell;
	}

	/**
	 * Encapsulates logic of connections creation and presentation
	 * 
	 * @param source
	 *            source port
	 * @param destination
	 *            destination port
	 */
	private void connect(DefaultGraphCell source, DefaultGraphCell destination) {
		DefaultGraphCell src = (DefaultGraphCell) source.getParent();
		DefaultGraphCell dest = (DefaultGraphCell) destination.getParent();
		if (src != null && dest != null) {
			Object srcUO = src.getUserObject();
			Object destUO = dest.getUserObject();
			if (srcUO != null && destUO != null) {
				if (srcUO instanceof ContextElement
						&& destUO instanceof ContextElement) {
					if (!ctx.connect((ContextElement) srcUO,
							(ContextElement) destUO)) {
						updateUI();
					}
					;
				}
				/*
				 * DefaultGraphCell cell = null; // rotate src and dest when it
				 * isn't important if (destUO instanceof Key && srcUO instanceof
				 * Node || destUO instanceof Node && srcUO instanceof MM ||
				 * destUO instanceof Entity && srcUO instanceof Requisite) {
				 * Object c = destUO; destUO = srcUO; srcUO = c; } if (srcUO
				 * instanceof Key && destUO instanceof Node &&
				 * ((Key)srcUO).getEntity() != destUO) { try {
				 * ((Key)srcUO).getEntity().addChild((Node)destUO);
				 * ((Key)srcUO).addKeyElement((Node)destUO); if
				 * (getEdgeBySNTObjs(((Key)srcUO).getEntity(),
				 * (ContextElement)destUO) != null) { Object[] cellToRemove =
				 * new Object[1]; cellToRemove[0] =
				 * getEdgeBySNTObjs(((Key)srcUO).getEntity(),
				 * (ContextElement)destUO); cells.remove(cellToRemove[0]);
				 * getGraphLayoutCache().remove(cellToRemove); } if
				 * (getEdgeBySNTObjs((ContextElement)srcUO,
				 * (ContextElement)destUO) == null) { cell =
				 * createEdge((Key)srcUO, (Node)destUO); } } catch (Exception e)
				 * { e.printStackTrace(); } } else if (srcUO instanceof Entity
				 * && destUO instanceof Node) { try { if
				 * (!((Entity)srcUO).isAmongChildren((Node)destUO)){
				 * ((Entity)srcUO).addChild((Node)destUO); cell =
				 * createEdge((Entity)srcUO, (Node)destUO); } } catch (Exception
				 * e) { e.printStackTrace(); } } else if (srcUO instanceof Node
				 * && destUO instanceof MM) { try { if
				 * (!((MM)destUO).isAmongElements((Node)srcUO)) {
				 * ((MM)destUO).addMMElement((Node)srcUO); cell =
				 * createEdge((Node)srcUO, ((MM)destUO)); } } catch (Exception
				 * e) { e.printStackTrace(); } } if (cell != null) {
				 * cells.add(cell); getGraphLayoutCache().insert(cell); }
				 * getGraphLayoutCache().update();
				 */
			}
		}
	}

	private DefaultEdge insertEdge(ContextElement el1, ContextElement el2) {
		DefaultEdge edge = null;
		if (el1.isEntity() && el2.isKey()) {
			// Entity -> Key
			edge = createKeyEntityEdge(el1, el2);
		} else if (el1.isKey() && (el2.isEntity() || el2.isRequisite())) {
			// Key -> KeyEl
			edge = createKeyNodeEdge(el1, el2);
		} else if (el1.isEntity() && (el2.isEntity() || el2.isRequisite())) {
			// Entity -> Child
			edge = createEntityNodeEdge(el1, el2);
		} else if (el1.isMM() && (el2.isEntity() || el2.isRequisite())) {
			// 
			edge = createNodeMMEdge(el1, el2);
		} else {
			return null;
		}
		GraphConstants.setEditable(edge.getAttributes(), false);
		GraphConstants.setDisconnectable(edge.getAttributes(), false);
		GraphConstants.setBendable(edge.getAttributes(), true);
		GraphConstants.setConnectable(edge.getAttributes(), false);
		// Insert the cells via the cache, so they get selected
		getGraphLayoutCache().insert(edge);
		this.updateUI();
		cells.add(edge);
		return edge;
	}

	private DefaultEdge createNodeMMEdge(ContextElement el1, ContextElement el2) {
		// Create Edge
		DefaultEdge edge = new DefaultEdge();
		// Fetch the ports from the new vertices, and connect them with the edge
		edge.setSource(getCellByObject(el1).getChildAt(0));
		edge.setTarget(getCellByObject(el2).getChildAt(0));
		// Set Arrow Style for edge
		// GraphConstants.setLineEnd(edge.getAttributes(),
		// GraphConstants.ARROW_NONE);
		float[] dashPattern = { 10, 15 };
		GraphConstants.setDashPattern(edge.getAttributes(), dashPattern);
		GraphConstants.setEndFill(edge.getAttributes(), true);
		GraphConstants.setLineStyle(edge.getAttributes(),
				GraphConstants.STYLE_SPLINE);

		return edge;
	}

	private DefaultEdge createEntityNodeEdge(ContextElement el1,
			ContextElement el2) {
		// Create Edge
		DefaultEdge edge = new DefaultEdge();
		// Fetch the ports from the new vertices, and connect them with the edge
		edge.setSource(getCellByObject(el1).getChildAt(0));
		edge.setTarget(getCellByObject(el2).getChildAt(0));
		// Set Arrow Style for edge
		GraphConstants.setLineEnd(edge.getAttributes(),
				GraphConstants.ARROW_CLASSIC);
		GraphConstants.setEndFill(edge.getAttributes(), true);
		GraphConstants.setLineStyle(edge.getAttributes(),
				GraphConstants.STYLE_SPLINE);

		return edge;
	}

	private DefaultEdge createKeyNodeEdge(ContextElement el1, ContextElement el2) {
		// Create Edge
		DefaultEdge edge = new DefaultEdge();
		// Fetch the ports from the new vertices, and connect them with the edge
		edge.setSource(getCellByObject(el1).getChildAt(0));
		edge.setTarget(getCellByObject(el2).getChildAt(0));
		// Set Arrow Style for edge
		GraphConstants.setLineEnd(edge.getAttributes(),
				GraphConstants.ARROW_CLASSIC);
		GraphConstants.setEndFill(edge.getAttributes(), true);
		GraphConstants.setLineStyle(edge.getAttributes(),
				GraphConstants.STYLE_SPLINE);

		return edge;
	}

	private DefaultEdge createKeyEntityEdge(ContextElement el1,
			ContextElement el2) {
		// Create Edge
		DefaultEdge edge = new DefaultEdge();
		// Fetch the ports from the new vertices, and connect them with the edge
		edge.setSource(getCellByObject(el1).getChildAt(0));
		edge.setTarget(getCellByObject(el2).getChildAt(0));
		// Set Arrow Style for edge
		// GraphConstants.setLineEnd(edge.getAttributes(),
		// GraphConstants.ARROW_CLASSIC);
		GraphConstants.setEndFill(edge.getAttributes(), true);
		GraphConstants.setLineStyle(edge.getAttributes(),
				GraphConstants.STYLE_SPLINE);

		return edge;
	}

	private DefaultGraphCell getCellByObject(Object obj) {
		/*
		 * for (Iterator<DefaultGraphCell> it1 = cells.iterator();
		 * it1.hasNext();){ DefaultGraphCell cell = it1.next();
		 * if(cell.getUserObject() == obj){ return cell; } }
		 */
		return uObjVertTable.get(obj);
	}

	protected void remove(DefaultGraphCell cell) {
		Object uO = cell.getUserObject();
		if (uO != null) {// if we have uO we have a deal with a Vertex
			try {
				ctx.remove((ContextElement) uO);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (cell instanceof DefaultEdge) {
			ctx.disconnect((ContextElement) getSourceUO(cell),
					(ContextElement) getTargetUO(cell));
		}
	}

	private Object getSourceUO(Object edge) {
		return ((DefaultGraphCell) (((DefaultPort) (((DefaultEdge) edge)
				.getSource())).getParent())).getUserObject();
	}

	private Object getTargetUO(Object edge) {
		return ((DefaultGraphCell) (((DefaultPort) (((DefaultEdge) edge)
				.getTarget())).getParent())).getUserObject();
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
	}

	public void setSupressSelEvents(boolean supressSelEvents) {
		this.supressSelEvents = supressSelEvents;
	}

	public boolean getSupressSelEvents() {
		return supressSelEvents;
	}

	public void saveCoords(OutputStream os)
			throws ParserConfigurationException, TransformerException,
			IOException {

		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder p = f.newDocumentBuilder();
		Document doc = p.newDocument();

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		StreamResult result = new StreamResult(os);
		DOMSource source = new DOMSource(doc);

		Element coordsEl = doc.createElement("coords");
		doc.appendChild(coordsEl);

		// ������� ����, ����� ������� ���������
		// ����� ��������� �������� � �����

		for (DefaultGraphCell cell : cells) {
			Object uO = cell.getUserObject();
			if (uO instanceof ContextElement) {
				Rectangle2D rect = GraphConstants.getBounds(cell
						.getAttributes());
				Element el = doc.createElement("vertex");
				coordsEl.appendChild(el);
				if (uO instanceof Entity) {
					el.setAttribute("type", "entity");
				} else if (uO instanceof Requisite) {
					el.setAttribute("type", "requisite");
				} else if (uO instanceof Key) {
					el.setAttribute("type", "key");
				} else if (uO instanceof MM) {
					el.setAttribute("type", "MM");
				}
				el.setAttribute("id", Integer.toString(((ContextElement) uO)
						.getId()));
				el.setAttribute("x", Double.toString(rect.getX()));
				el.setAttribute("y", Double.toString(rect.getY()));
				el.setAttribute("w", Double.toString(rect.getWidth()));
				el.setAttribute("h", Double.toString(rect.getHeight()));
			}
		}

		for (DefaultGraphCell cell : cells) {
			if (cell instanceof DefaultEdge) {
				DefaultEdge edge = (DefaultEdge) cell;
				Element el = doc.createElement("edge");
				coordsEl.appendChild(el);
				Object uO1 = ((DefaultGraphCell) ((DefaultPort) (edge
						.getSource())).getParent()).getUserObject();
				Object uO2 = ((DefaultGraphCell) ((DefaultPort) (edge
						.getTarget())).getParent()).getUserObject();
				if (uO1 instanceof ContextElement) {
					el.setAttribute("id1", Integer
							.toString(((ContextElement) uO1).getId()));
					if (uO1 instanceof Entity) {
						el.setAttribute("type1", "entity");
					} else if (uO1 instanceof Key) {
						el.setAttribute("type1", "key");
					} else if (uO1 instanceof MM) {
						el.setAttribute("type1", "MM");
					}
				}
				if (uO2 instanceof ContextElement) {
					el.setAttribute("id2", Integer
							.toString(((ContextElement) uO2).getId()));
					if (uO2 instanceof Entity) {
						el.setAttribute("type2", "entity");
					} else if (uO2 instanceof Requisite) {
						el.setAttribute("type2", "requisite");
					} else if (uO2 instanceof Key) {
						el.setAttribute("type2", "key");
					} else if (uO2 instanceof MM) {
						el.setAttribute("type2", "MM");
					}
				}
				List pts = GraphConstants.getPoints(cell.getAttributes());
				if (pts == null)
					continue;
				for (Object pt : pts) {
					Element ptNode = doc.createElement("point");
					el.appendChild(ptNode);
					ptNode.setAttribute("x", Double.toString(((Point2D) pt)
							.getX()));
					ptNode.setAttribute("y", Double.toString(((Point2D) pt)
							.getY()));
				}
			}
		}

		// transform source into result will do save
		// transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
		// "coordsdescr.dtd");
		transformer.transform(source, result);
		os.flush();
	}

	/**
	 * Loads coords from input stream and applies them to the graph
	 * 
	 * @param is
	 *            InputStream
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void loadCoords(InputStream is) throws ParserConfigurationException,
			SAXException, IOException {

		// parse an XML document into a DOM tree
		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = parser.parse(is);

		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// load a WXS schema, represented by a Schema instance
		java.net.URL xsdURL = Context.class
				.getResource("/com/describer/resources/coordsdescr.xsd");
		Source schemaFile = new StreamSource(xsdURL.openStream());
		Schema schema = factory.newSchema(schemaFile);

		// create a Validator instance, which can be used to validate an
		// instance document
		Validator validator = schema.newValidator();

		// validate the DOM tree
		try {
			validator.validate(new DOMSource(doc));
		} catch (SAXException e) {
			// instance document is invalid!
			throw e;
		}

		// ��������� ������
		NodeList vertexes = doc.getElementsByTagName("vertex");
		for (int i = 0; i < vertexes.getLength(); i++) {
			Element item = (Element) vertexes.item(i);
			// ������� �������������
			int id = Integer.parseInt(item.getAttribute("id"));
			// ������� ���
			String type = (item.getAttribute("type"));
			// ������� x
			double x = Double.parseDouble(item.getAttribute("x"));
			// ������� y
			double y = Double.parseDouble(item.getAttribute("y"));
			// ������� w
			double w = Double.parseDouble(item.getAttribute("w"));
			// ������� h
			double h = Double.parseDouble(item.getAttribute("h"));
			Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
			ContextElement el = null;
			if (type.equals("entity")) {
				el = ctx.getEntityByEId(id);
			} else if (type.equals("requisite")) {
				el = ctx.getRequisiteByRId(id);
			} else if (type.equals("key")) {
				el = ctx.getKeyByKId(id);
			} else if (type.equals("MM")) {
				el = ctx.getMMByMId(id);
			}
			// System.out.println(el + " wants to be positioned");
			if (el != null) {
				DefaultGraphCell cell = (DefaultGraphCell) getCellByObject(el);
				// System.out.println(cell + " wants to be positioned");
				if (cell != null) {
					GraphConstants.setBounds(cell.getAttributes(), rect);
				}
			}
		}

		// ��������� ������
		NodeList edges = doc.getElementsByTagName("edge");
		for (int i = 0; i < edges.getLength(); i++) {
			Element edge = (Element) edges.item(i);
			// ������� �������������1
			int id1 = Integer.parseInt(edge.getAttribute("id1"));
			// ������� ���1
			String type1 = edge.getAttribute("type1");
			// ������� �������������2
			int id2 = Integer.parseInt(edge.getAttribute("id2"));
			// ������� ���2
			String type2 = edge.getAttribute("type2");

			List<Point2D> ptList = new ArrayList<Point2D>();
			NodeList pointsList = edge.getElementsByTagName("point");
			for (int j = 0; j < pointsList.getLength(); j++) {
				double x = Double.parseDouble(((Element) pointsList.item(j))
						.getAttribute("x"));
				double y = Double.parseDouble(((Element) pointsList.item(j))
						.getAttribute("y"));
				Point2D pt = new Point2D.Double(x, y);
				ptList.add(pt);
			}

			ContextElement uO1 = null;
			ContextElement uO2 = null;
			if (type1.equals("entity")) {
				uO1 = ctx.getEntityByEId(id1);
			} else if (type1.equals("requisite")) {
				uO1 = ctx.getRequisiteByRId(id1);
			} else if (type1.equals("key")) {
				uO1 = ctx.getKeyByKId(id1);
			} else if (type1.equals("MM")) {
				uO1 = ctx.getMMByMId(id1);
			}
			if (type2.equals("entity")) {
				uO2 = ctx.getEntityByEId(id2);
			} else if (type2.equals("requisite")) {
				uO2 = ctx.getRequisiteByRId(id2);
			} else if (type2.equals("key")) {
				uO2 = ctx.getKeyByKId(id2);
			} else if (type2.equals("MM")) {
				uO2 = ctx.getMMByMId(id2);
			}

			DefaultEdge edgeCell = getEdgeBySNTObjs(uO1, uO2);
			if (edgeCell != null && !ptList.isEmpty()) {
				GraphConstants.setPoints(edgeCell.getAttributes(), ptList);
			}

		}

		getGraphLayoutCache().reload();
		updateUI();

		System.out.println("Coords loaded");

	}

	// Presumably all cells have one port
	private DefaultEdge getEdgeBySNTObjs(Object uO1, Object uO2) {
		DefaultGraphCell cell1 = getCellByObject(uO1);
		DefaultGraphCell cell2 = getCellByObject(uO2);
		if (cell1 == null || cell2 == null) {
			// can't find cells with given UOs
			return null;
		}
		Object portObj1 = cell1.getChildren().get(0);
		Object portObj2 = cell2.getChildren().get(0);
		if (portObj1 instanceof DefaultPort) {
			Set edges = ((DefaultPort) portObj1).getEdges();
			for (Object edgeObj : edges) {
				DefaultEdge edge = (DefaultEdge) edgeObj;
				if (edge.getSource() == portObj1
						&& edge.getTarget() == portObj2) {
					return edge;
				}
			}
		}
		return null;
	}

	public void export(File file) throws IOException {
		String ext = Utils.getFileExtension(file);
		Color bg = this.getBackground(); // Use this to make the background
		// transparent
		BufferedImage img = this.getImage(bg, 10);
		if (ext.equals(".jpg")) {
			// It's so complex beacuse we need high quality JPEG
			Iterator writers = ImageIO.getImageWritersBySuffix("jpeg");
			if (!writers.hasNext())
				throw new IllegalStateException("No writers for jpeg?!");
			ImageWriter writer = (ImageWriter) writers.next();
			ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
			imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			List thumbNails = null;
			IIOImage iioImage = new IIOImage(img, thumbNails,
					(IIOMetadata) null);
			imageWrite(writer, imageWriteParam, iioImage, file, 1.0f);
		} else if (ext.equals(".png")) {
			ImageIO.write(img, "png", file);
		}
	}

	public static void imageWrite(ImageWriter writer,
			ImageWriteParam imageWriteParam, IIOImage iioImage, File file,
			float compressionQuality) throws IOException {
		ImageOutputStream out = ImageIO.createImageOutputStream(file);
		imageWriteParam.setCompressionQuality(compressionQuality);
		writer.setOutput(out);
		writer.write((IIOMetadata) null, iioImage, imageWriteParam);
		out.flush();
		out.close();
	}

	@Override
	public int print(Graphics g, PageFormat printFormat, int page)
			throws PrinterException {

		float pageScale = 1;

		Dimension pSize = this.getPreferredSize(); // graph is a JGraph

		double imageableWidth = printFormat.getImageableWidth();
		double imageableHeight = printFormat.getImageableHeight();
		double indentX = printFormat.getImageableX();
		double indentY = printFormat.getImageableY();

		int w = (int) (imageableWidth * pageScale);
		int h = (int) (imageableHeight * pageScale);
		int cols = (int) Math.max(Math.ceil((double) (pSize.width - 5)
				/ (double) w), 1);
		int rows = (int) Math.max(Math.ceil((double) (pSize.height - 5)
				/ (double) h), 1);
		if (page < cols * rows) {
			// Configures graph for printing
			RepaintManager currentManager = RepaintManager.currentManager(this);
			currentManager.setDoubleBufferingEnabled(false);
			double oldScale = this.getScale();
			this.setScale(1 / pageScale);
			int dx = (int) ((int) (page % cols) * imageableWidth);
			int dy = (int) ((int) (page / cols) * imageableHeight);
			// System.out.println(dx + "||" + dy);
			g.translate((int) indentX, (int) indentY);
			g.translate(-dx, -dy);
			g.setClip(dx, dy, (int) imageableWidth, (int) imageableHeight);
			// Prints the graph on the graphics.
			this.paint(g);
			// Restores graph
			// g.translate(dx, dy);
			this.setScale(oldScale);
			currentManager.setDoubleBufferingEnabled(true);
			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}

	/*
	 * class CtxGraphModelListener implements GraphModelListener {
	 * 
	 * @Override public void graphChanged(GraphModelEvent event) {
	 * //System.out.println("----------"); if
	 * (event.getChange().getPreviousAttributes() != null) for (Object obj :
	 * event.getChange().getPreviousAttributes().keySet()) { if (obj != null &&
	 * obj instanceof DefaultGraphCell) { // Cell's UO changes, when user enters
	 * new name; // so, we need to manually store cells-UOs // relationship and
	 * restore original connections // after every change. we store it in
	 * vertUObjTable Object curUO = ((DefaultGraphCell) obj).getUserObject();
	 * Object assocUO = vertUObjTable.get(obj); if (curUO != null && curUO
	 * instanceof String) { if (assocUO != null && assocUO instanceof Node) {
	 * ((Node) assocUO).setName((String) curUO); ((DefaultGraphCell)
	 * obj).setUserObject(assocUO); // System.out.println("Prev = " + prevValue
	 * + // "| Cur = " + curValue); } } } } }
	 * 
	 * }
	 */

	//
	// Custom MarqueeHandler
	// MarqueeHandler that Connects Vertices and Displays PopupMenus
	class CtxMarqueeHandler extends BasicMarqueeHandler {

		private CtxGraph graph = null;

		// Holds the Start and the Current Point
		protected Point2D start, current;

		// Holds the First and the Current Port
		protected PortView port, firstPort;

		/**
		 * Component that is used for highlighting cells if the graph does not
		 * allow XOR painting.
		 */
		protected JComponent highlight = new JPanel();

		public CtxMarqueeHandler(CtxGraph graph) {
			this.graph = graph;
			// Configures the panel for highlighting ports
			// highlight = createHighlight();
		}

		//
		// /**
		// * Creates the component that is used for highlighting cells if
		// * the graph does not allow XOR painting.
		// */
		// protected JComponent createHighlight() {
		// JPanel panel = new JPanel();
		// panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		// panel.setVisible(false);
		// panel.setOpaque(false);
		//
		// return panel;
		// }
		//		
		// Override to Gain Control (for PopupMenu and ConnectMode)
		public boolean isForceMarqueeEvent(MouseEvent e) {
			if (e.isShiftDown())
				return false;
			// If Right Mouse Button we want to Display the PopupMenu
			if (SwingUtilities.isRightMouseButton(e)) {
				// Return Immediately
				return true;
			}
			// Find and Remember Port
			port = getSourcePortAt(e.getPoint());
			// If Port Found and in ConnectMode (=Ports Visible)
			if (port != null)// && graph.isPortsVisible())
				return true;
			// Else Call Superclass
			return super.isForceMarqueeEvent(e);
		}

		// Display PopupMenu or Remember Start Location and First Port
		public void mousePressed(final MouseEvent e) {
			// If Right Mouse Button
			if (SwingUtilities.isRightMouseButton(e)) {
				// Find Cell in Model Coordinates
				Object cell = graph.getFirstCellForLocation(e.getX(), e.getY());
				// if (cell == null){
				// Create PopupMenu for the Cell
				JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
				// Display PopupMenu if it is not empty
				if (menu.getComponentCount() != 0) {
					menu.show(graph, e.getX(), e.getY());
					// Store active point
					activePoint = new Point2D.Double(e.getX(), e.getY());
				}
				// }
			} else if (port != null) {// && graph.isPortsVisible()) {
				// Remember Start Location
				start = graph.toScreen(port.getLocation());
				// Remember First Port
				firstPort = port;
			} else {
				// Call Superclass
				super.mousePressed(e);
			}

		}

		// Find Port under Mouse and Repaint Connector
		public void mouseDragged(MouseEvent e) {
			// If remembered Start Point is Valid
			if (start != null) {
				// Fetch Graphics from Graph
				Graphics g = graph.getGraphics();
				// Reset Remembered Port
				PortView newPort = getTargetPortAt(e.getPoint());
				// Do not flicker (repaint only on real changes)
				if (newPort == null || newPort != port) {
					// Xor-Paint the old Connector (Hide old Connector)
					paintConnector(Color.black, graph.getBackground(), g);
					// If Port was found then Point to Port Location
					port = newPort;
					if (port != null)
						current = graph.toScreen(port.getLocation());
					// Else If no Port was found then Point to Mouse Location
					else
						current = graph.snap(e.getPoint());
					// Xor-Paint the new Connector
					paintConnector(graph.getBackground(), Color.black, g);
				}
			}
			// Call Superclass
			super.mouseDragged(e);
		}

		public PortView getSourcePortAt(Point2D point) {
			// Disable jumping
			graph.setJumpToDefaultPort(false);
			PortView result;
			try {
				// Find a Port View in Model Coordinates and Remember
				result = graph.getPortViewAt(point.getX(), point.getY());
			} finally {
				graph.setJumpToDefaultPort(true);
			}
			return result;
		}

		// Find a Cell at point and Return its first Port as a PortView
		protected PortView getTargetPortAt(Point2D point) {
			// Find a Port View in Model Coordinates and Remember
			return graph.getPortViewAt(point.getX(), point.getY());
		}

		// Connect the First Port and the Current Port in the Graph or Repaint
		public void mouseReleased(MouseEvent e) {
			// If Valid Event, Current and First Port
			if (e != null && port != null && firstPort != null
					&& firstPort != port) {
				// Then Establish Connection
				connect((DefaultGraphCell) firstPort.getCell(),
						(DefaultGraphCell) port.getCell());
				e.consume();
				// Else Repaint the Graph
			} else
				graph.repaint();
			// Reset Global Vars
			firstPort = port = null;
			start = current = null;
			// Restore default cursor
			graph.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			// Call Superclass
			super.mouseReleased(e);
		}

		// Show Special Cursor if Over Port
		public void mouseMoved(MouseEvent e) {
			// Check Mode and Find Port
			if (e != null && getSourcePortAt(e.getPoint()) != null) {
				// Set Cusor on Graph (Automatically Reset)
				graph.setCursor(new Cursor(Cursor.HAND_CURSOR));
				// Consume Event
				// Note: This is to signal the BasicGraphUI's
				// MouseHandle to stop further event processing.
				e.consume();
			} else
				// Call Superclass
				super.mouseMoved(e);
		}

		// Use Xor-Mode on Graphics to Paint Connector
		protected void paintConnector(Color fg, Color bg, Graphics g) {
			if (graph.isXorEnabled()) {
				// Set Foreground
				g.setColor(fg);
				// Set Xor-Mode Color
				g.setXORMode(bg);
				// Highlight the Current Port
				paintPort(graph.getGraphics());

				drawConnectorLine(g);
			} else {
				Rectangle dirty = new Rectangle((int) start.getX(), (int) start
						.getY(), 1, 1);

				if (current != null) {
					dirty.add(current);
				}

				dirty.grow(1, 1);

				graph.repaint(dirty);
				// highlight(graph, port);
			}
		}

		// Overrides parent method to paint connector if
		// XOR painting is disabled in the graph
		public void paint(JGraph graph, Graphics g) {
			super.paint(graph, g);

			if (!graph.isXorEnabled()) {
				g.setColor(Color.black);
				drawConnectorLine(g);
			}
		}

		protected void drawConnectorLine(Graphics g) {
			if (firstPort != null && start != null && current != null) {
				// Then Draw A Line From Start to Current Point
				g.drawLine((int) start.getX(), (int) start.getY(),
						(int) current.getX(), (int) current.getY());
			}
		}

		// Use the Preview Flag to Draw a Highlighted Port
		protected void paintPort(Graphics g) {
			// If Current Port is Valid
			if (port != null) {
				// If Not Floating Port...
				boolean o = (GraphConstants.getOffset(port.getAllAttributes()) != null);
				// ...Then use Parent's Bounds
				Rectangle2D r = (o) ? port.getBounds() : port.getParentView()
						.getBounds();
				// Scale from Model to Screen
				r = graph.toScreen((Rectangle2D) r.clone());
				// Add Space For the Highlight Border
				r.setFrame(r.getX() - 3, r.getY() - 3, r.getWidth() + 6, r
						.getHeight() + 6);
				// Paint Port in Preview (=Highlight) Mode
				graph.getUI().paintCell(g, port, r, true);
			}
		}

		//
		// /**
		// * Highlights the given cell view or removes the highlight if
		// * no cell view is specified.
		// *
		// * @param graph
		// * @param cellView
		// */
		// protected void highlight(JGraph graph, CellView cellView)
		// {
		// if (cellView != null)
		// {
		// highlight.setBounds(getHighlightBounds(graph, cellView));
		//
		// if (highlight.getParent() == null)
		// {
		// graph.add(highlight);
		// highlight.setVisible(true);
		// }
		// }
		// else
		// {
		// if (highlight.getParent() != null)
		// {
		// highlight.setVisible(false);
		// highlight.getParent().remove(highlight);
		// }
		// }
		// }
		//
		// /**
		// * Returns the bounds to be used to highlight the given cell view.
		// *
		// * @param graph
		// * @param cellView
		// * @return
		// */
		// protected Rectangle getHighlightBounds(JGraph graph, CellView
		// cellView)
		// {
		// boolean offset =
		// (GraphConstants.getOffset(cellView.getAllAttributes()) != null);
		// Rectangle2D r = (offset) ? cellView.getBounds() : cellView
		// .getParentView().getBounds();
		// r = graph.toScreen((Rectangle2D) r.clone());
		// int s = 3;
		//
		// return new Rectangle((int) (r.getX() - s), (int) (r.getY() - s),
		// (int) (r.getWidth() + 2 * s), (int) (r.getHeight() + 2 * s));
		// }
		//		
		//
		// PopupMenu
		//
		public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
			JPopupMenu menu = new JPopupMenu();
			if (cell != null
					&& (((DefaultGraphCell) cell).getUserObject() instanceof Key)) {
				// groups manager
				menu.add(new AbstractAction("Manage paths") {
					public void actionPerformed(ActionEvent e) {
						KeyPathsMgr kpm = new KeyPathsMgr((Key)((DefaultGraphCell) cell).getUserObject(), presenter);
						kpm.setLocationRelativeTo(presenter);
						kpm.setModal(true);
						kpm.setVisible(true);						
					}
				});
			}
			if (cell != null
					&& (((DefaultGraphCell) cell).getUserObject() instanceof Node)) {
				// Edit
				menu.add(new AbstractAction("Edit") {
					public void actionPerformed(ActionEvent e) {
						graph.startEditingAtCell(cell);
					}
				});
				// conversion
				if (((Node) ((DefaultGraphCell) cell).getUserObject())
						.getDirectDependants().isEmpty())
					menu.add(new AbstractAction("Transform") {
						public void actionPerformed(ActionEvent ev) {
							ctx.convertNode((Node) ((DefaultGraphCell) cell)
									.getUserObject());
						}
					});
			}
			if (cell != null
					&& (((DefaultGraphCell) cell).getUserObject() instanceof Entity)) {
				// Insert key
				menu.add(new AbstractAction("Add key") {
					public void actionPerformed(ActionEvent ev) {
						try {
							ctx.createKey((Entity) (((DefaultGraphCell) cell)
									.getUserObject()));
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
			}
			if (cell == null) {
				// Insert req
				menu.add(new AbstractAction("Add requisite") {
					public void actionPerformed(ActionEvent ev) {
						try {
							ctx.createRequisite();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				// Insert ent
				menu.add(new AbstractAction("Add entity") {
					public void actionPerformed(ActionEvent ev) {
						ctx.createEntity();
					}
				});
				// Insert mM
				menu.add(new AbstractAction("Add M:M marker") {
					public void actionPerformed(ActionEvent ev) {
						try {
							ctx.createMM();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			// Remove
			boolean show_remove = true;
			if (cell != null) {
				if (cell instanceof DefaultEdge) {
					if (getSourceUO(cell) instanceof Entity
							&& getTargetUO(cell) instanceof Key)
						show_remove = false;
				}
				if (show_remove) {
					menu.addSeparator();
					menu.add(new AbstractAction("Delete") {
						public void actionPerformed(ActionEvent e) {
							remove((DefaultGraphCell) cell);
						}
					});
				}
			}
			return menu;
		}

	} // End of Editor.MyMarqueeHandler

	@Override
	public void elementsAdded(ContextEvent e) {
		for (ContextElement el : e.getNewElements()) {
			createVertex(el);
		}
		for (ContextElement el : e.getNewElements()) {
			updateElementsAndEdges(el.getRelatedElements());
		}
		if (e.getNewElements().size() == 1) {
			startEditingAtCell(getCellByObject(e.getNewElements().get(0)));
		}
	}

	/*
	 * private void createEdges(ContextElement el) {
	 * 
	 * if (el.getType().equals(Context.TYPE.ENTITY)) { Entity elem = (Entity)el;
	 * for (Node nd : elem.getChildren()) { if (!elem.isAmongChildren(nd))
	 * insertEdge(elem, nd); } for (Key key : elem.getKeysHas()) {
	 * insertEdge(elem, key); } } else if
	 * (el.getType().equals(Context.TYPE.REQUISITE)) { // Nothing } else if
	 * (el.getType().equals(Context.TYPE.KEY)) { Key elem = (Key)el; for (Node
	 * nd : elem.getKeyElements()) { insertEdge(elem, nd); } } else if
	 * (el.getType().equals(Context.TYPE.MM)) { MM elem = (MM)el; for (Node nd :
	 * elem.getMMElements()) { insertEdge(nd, elem); } }
	 * 
	 * }
	 */
	@Override
	public void elementsChanged(ContextEvent e) {
		updateElementsAndEdges(e.getChangedElements());
	}

	private void updateElementsAndEdges(Collection<ContextElement> col) {
		List<DefaultGraphCell> cellsChanged = new ArrayList<DefaultGraphCell>();
		Map<DefaultGraphCell, AttributeMap> nested = new Hashtable<DefaultGraphCell, AttributeMap>();
		Set<DefaultEdge> edgesToRemove = new HashSet<DefaultEdge>();
		for (ContextElement el : col) {
			DefaultGraphCell cell = getCellByObject(el);
			if (cell == null)
				continue;
			// get current edges
			Iterator currentEdgesIt = ((DefaultPort) cell.getChildAt(0))
					.edges();
			for (; currentEdgesIt.hasNext();) {
				DefaultEdge curEdge = (DefaultEdge) currentEdgesIt.next();
				if (cell == ((DefaultPort) curEdge.getSource()).getParent())
					edgesToRemove.add(curEdge);
			}
			// create missed edges
			for (ContextElement dep : el.getDirectDependants()) {
				if (getCellByObject(dep) != null) {
					DefaultEdge edge = getEdgeBySNTObjs(el, dep);
					if (edge == null) {
						edge = insertEdge(el, dep);
					}
					edgesToRemove.remove(edge);
				}
			}
			// compute edges to remove
			cells.removeAll(edgesToRemove);
			nested.put(cell, cell.getAttributes());
			cellsChanged.add(cell);
		}
		getGraphLayoutCache().remove(edgesToRemove.toArray());
		getGraphLayoutCache().edit(nested);
	}

	@Override
	public void elementsRemoved(ContextEvent e) {
		List<DefaultGraphCell> vertexesToRemove = new ArrayList<DefaultGraphCell>();
		List<DefaultEdge> edgesToRemove = new ArrayList<DefaultEdge>();
		List<DefaultPort> portsToRemove = new ArrayList<DefaultPort>();

		// Compute vertexes to remove
		for (ContextElement el : e.getRemovedElements()) {
			DefaultGraphCell cell = uObjVertTable.get(el);
			if (cell != null) {
				uObjVertTable.remove(el);
				cells.remove(cell);
				vertexesToRemove.add(cell);
			}
		}
		// Add edges & ports to remove
		for (DefaultGraphCell cell : vertexesToRemove) {
			TreeNode port = cell.getChildAt(0);
			if (port != null) {
				portsToRemove.add((DefaultPort) port);
				Object o;
				for (Iterator it1 = ((DefaultPort) port).edges(); it1.hasNext();) {
					o = it1.next();
					if (o instanceof DefaultEdge) {
						cells.remove(o);
						edgesToRemove.add((DefaultEdge) o);
					}
				}
			}
		}
		vertexesToRemove.addAll(edgesToRemove);
		vertexesToRemove.addAll(portsToRemove);
		getGraphLayoutCache().remove(vertexesToRemove.toArray());
	}

	public void updateSelection(Object source, Collection<Object> selected) {

		if (source == this) {
			return;
		}

		List<DefaultGraphCell> selectedCells = new ArrayList<DefaultGraphCell>();
		// Add currently selected edges
		Object[] curSelected = getSelectionModel().getSelectionCells();
		for (int i = 0; i < curSelected.length; i++) {
			if (curSelected[i] instanceof DefaultEdge)
				selectedCells.add((DefaultGraphCell) curSelected[i]);
		}

		for (Object uO : selected) {
			selectedCells.add(getCellByObject(uO));
		}
		// now we set the corresponding tree selection
		setSupressSelEvents(true);
		getSelectionModel().setSelectionCells(selectedCells.toArray());
		setSupressSelEvents(false);
	}

	@Override
	public UpdatingDelegate getUpdDelegate() {
		return updDeleg;
	}

	public void removeSelectedEdges() {
		Object[] curSelected = getSelectionModel().getSelectionCells();
		for (Object cell : curSelected) {
			if (cell instanceof DefaultEdge)
				remove((DefaultEdge) cell);
		}
	}

	@Override
	public void elementConverted(ContextEvent e) {
		DefaultGraphCell cell = getCellByObject(e.getConvertedOldElement());
		if (cell == null)
			return;

		Object emb = e.getConvertedNewElement();

		cell.setUserObject(emb);

		// redecorate our cell
		Color bg = null;
		if (emb instanceof Node) {
			if (emb instanceof Entity) {
				bg = new Color(255, 243, 53);
			} else if (emb instanceof Requisite) {
				bg = new Color(0, 255, 0);
			}
		}
		GraphConstants.setGradientColor(cell.getAttributes(), bg);

		// update view
		getGraphLayoutCache().editCell(cell, cell.getAttributes());
		updateUI();

		// maintain our mapping table
		uObjVertTable.remove(e.getConvertedOldElement());
		uObjVertTable.put(e.getConvertedNewElement(), cell);

		// select the cell
		setSelectionCell(cell);
	}

}

class CtxGraphUndoableEditListener implements UndoableEditListener {

	@Override
	public void undoableEditHappened(UndoableEditEvent event) {

	}
}