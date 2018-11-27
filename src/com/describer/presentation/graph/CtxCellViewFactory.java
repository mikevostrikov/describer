package com.describer.presentation.graph;

import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.jgraph.graph.CellHandle;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.VertexView;

/**
 * A default view factory for a JGraph. This simple factory associate a given
 * cell class to a cell view. This is a javabean, just parameter it correctly in
 * order it meets your requirements (else subclass it or subclass
 * DefaultCellViewFactory). You can also recover the gpConfiguration of that
 * javabean via an XML file via XMLEncoder/XMLDecoder.
 * 
 * @author rvalyi, license of this file: LGPL as stated by the Free Software
 *         Foundation
 */
@SuppressWarnings("serial")
public class CtxCellViewFactory extends DefaultCellViewFactory {
	
	public static final String VIEW_CLASS_KEY = "viewClassKey";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final void setViewClass(Map map, String viewClass) {
		map.put(VIEW_CLASS_KEY, viewClass);
	}

	protected VertexView createVertexView(Object v) {
		try {
			DefaultGraphCell cell = (DefaultGraphCell) v;
			String viewClass = (String) cell.getAttributes().get(VIEW_CLASS_KEY);

			VertexView view = (VertexView) Thread.currentThread()
					.getContextClassLoader().loadClass(viewClass).newInstance();
			view.setCell(v);
			return view;
		} catch (Exception ex) {
		}
		return super.createVertexView(v);
	}
	
	/**
	 * Constructs an EdgeView view for the specified object.
	 */
	protected EdgeView createEdgeView(Object cell) {
		// Return Custom EdgeView
		return new EdgeView(cell) {
			/**
			 * Returns a cell handle for the view.
			 */
			public CellHandle getHandle(GraphContext context) {
				return new CtxEdgeHandle(this, context);
			}
		};
	}
	
//
// Custom Edge Handle
//

	// Defines a EdgeHandle that uses the Shift-Button (Instead of the Right
	// Mouse Button, which is Default) to add/remove point to/from an edge.
	public static class CtxEdgeHandle extends EdgeView.EdgeHandle {

		/**
		 * @param edge
		 * @param ctx
		 */
		public CtxEdgeHandle(EdgeView edge, GraphContext ctx) {
			super(edge, ctx);
		}

		// Override Superclass Method
		public boolean isAddPointEvent(MouseEvent event) {
			// Points are Added using Left-Click
			return event.isPopupTrigger()
				|| (SwingUtilities.isLeftMouseButton(event)
						&& event.getClickCount() == 2);
		}

		// Override Superclass Method
		public boolean isRemovePointEvent(MouseEvent event) {
			// Points are Removed using Right-Click
			return event.isPopupTrigger()
				|| (SwingUtilities.isLeftMouseButton(event)
						&& event.getClickCount() == 2);
		}

	}
	
}