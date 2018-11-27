package com.describer.presentation.graph;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.jgraph.JGraph;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphCellEditor;
import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

import com.describer.context.ContextElement;

public class JGraphRoundRectView extends VertexView {
	
	/**
	 * Holds the static editor for views of this kind.
	 */
	public static CtxTextEditor editor = new CtxTextEditor();
	
	public static transient ActivityRenderer renderer = new ActivityRenderer();

	public JGraphRoundRectView() {
		super();
	}

	public JGraphRoundRectView(Object cell) {
		super(cell);
	}

	/**
	 * Returns the intersection of the bounding rectangle and the straight line
	 * between the source and the specified point p. The specified point is
	 * expected not to intersect the bounds.
	 */
	// todo public Point2D getPerimeterPoint(Point source, Point p) {
	/**
	 * getArcSize calculates an appropriate arc for the corners of the rectangle
	 * for boundary size cases of width and height
	 */
	public static int getArcSize(int width, int height) {
		int arcSize;

		// The arc width of a activity rectangle is 1/5th of the larger
		// of the two of the dimensions passed in, but at most 1/2
		// of the smaller of the two. 1/5 because it looks nice and 1/2
		// so the arc can complete in the given dimension

		if (width <= height) {
			arcSize = height / 5;
			if (arcSize > (width / 2)) {
				arcSize = width / 2;
			}
		} else {
			arcSize = width / 5;
			if (arcSize > (height / 2)) {
				arcSize = height / 2;
			}
		}

		return arcSize;
	}

	public CellViewRenderer getRenderer() {
		return renderer;
	}
	
	/**
	 * Returns a cell editor for the view.
	 * 
	 * @return the cell editor for this view
	 */
	public GraphCellEditor getEditor() {
		return editor;
	}

	public static class ActivityRenderer extends VertexRenderer {
		
		/**
		 * Return a slightly larger preferred size than for a rectangle.
		 */
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.width += d.height / 5;
			return d;
		}

		public void paint(Graphics g) {
			int b = borderWidth;
			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();
			boolean tmp = selected;
			int roundRectArc = JGraphRoundRectView.getArcSize(d.width - b,
					d.height - b);
			if (super.isOpaque()) {
				g.setColor(super.getBackground());
				if (gradientColor != null && !preview) {
					setOpaque(false);
					g2.setPaint(new GradientPaint(0, 0, getBackground(),
							getWidth(), getHeight(), gradientColor, true));
				}
				g.fillRoundRect(b / 2, b / 2, d.width - (int) (b * 1.5),
						d.height - (int) (b * 1.5), roundRectArc, roundRectArc);
			}
			try {
				setBorder(null);
				setOpaque(false);
				selected = false;
				super.paint(g);
			} finally {
				selected = tmp;
			}
			if (bordercolor != null) {
				g.setColor(bordercolor);
				g2.setStroke(new BasicStroke(b));
				g.drawRoundRect(b / 2, b / 2, d.width - (int) (b * 1.5),
						d.height - (int) (b * 1.5), roundRectArc, roundRectArc);
			}
			if (selected) {
				//g2.setStroke(GraphConstants.SELECTION_STROKE);
				g2.setStroke(new BasicStroke(3));
				g.setColor(highlightColor);
				g.drawRoundRect(b / 2, b / 2, d.width - (int) (b * 1.5),
						d.height - (int) (b * 1.5), roundRectArc, roundRectArc);
			}
		}
	}

}

class CtxTextEditor extends DefaultGraphCellEditor {
	
	/**
	 * This is invoked if a TreeCellEditor is not supplied in the constructor.
	 * It returns a TextField editor.
	 */
	protected GraphCellEditor createGraphCellEditor() {
		Border aBorder = UIManager.getBorder("Tree.editorBorder");
		RealEditor editor =
			new RealEditor(new DefaultTextField(aBorder)) {
			public boolean shouldSelectCell(EventObject event) {
				boolean retValue = super.shouldSelectCell(event);
				getComponent().requestFocus();
				return retValue;
			}
		};

		// One click to edit.
		editor.setClickCountToStart(1);
		return editor;
	}
	
}

class RealEditor extends DefaultCellEditor implements GraphCellEditor {

	//
	// Constructors
	//
	public RealEditor(final JTextField textField) {
		super(textField);
		// Undo some superConstructor's actions
		textField.removeActionListener(delegate);
		// Set our own delegate
		delegate = new RealEditorDelegate();
		textField.addActionListener(delegate);
	}
	
    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     * @see EditorDelegate#stopCellEditing
     */
    public boolean stopCellEditing() {
    	return delegate.stopCellEditing();
    }

	//
	// GraphCellEditor Interface
	//
	public Component getGraphCellEditorComponent(JGraph graph, Object value,
			boolean isSelected) {
		if (value instanceof DefaultGraphCell) {
			// Use real userobject if it's of supposed type
			delegate.setValue(((DefaultGraphCell)value).getUserObject());
		} else {
			// else use traditional strategy
			String stringValue = graph.convertValueToString(value);
			delegate.setValue(stringValue);
		}
		((JTextField) editorComponent).setText(graph.convertValueToString(value));
		((JTextField) editorComponent).selectAll();
		return editorComponent;
	}
	
	// This is our textField's listener
	// when editing is stopped, we update our objects
	class RealEditorDelegate extends DefaultCellEditor.EditorDelegate {
		
	       /**
	        * Stops editing and
	        * returns true to indicate that editing has stopped.
	        * This method calls <code>fireEditingStopped</code>.
	        *
	        * @return  true 
	        */
	        public boolean stopCellEditing() { 
	        	// update ContextElement
	            if (value instanceof ContextElement) {
	            	((ContextElement)value).getCtx().setName((ContextElement)value
	            			, ((JTextField) editorComponent).getText());	            		            	
	            }
		    fireEditingStopped(); 
		    return true;
		}
		
	}
	
} // End of class RealEditor