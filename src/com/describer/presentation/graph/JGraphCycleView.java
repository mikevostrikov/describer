package com.describer.presentation.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

/**
 * @author Gaudenz Alder
 *
 */
public class JGraphCycleView extends VertexView {

	/**
	 */
	public static transient JGraphEllipseRenderer renderer = new JGraphEllipseRenderer();

	/**
	 */
	public JGraphCycleView() {
		super();
	}

	/**
	 */
	public JGraphCycleView(Object cell) {
		super(cell);
	}

	/**
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}
	
	/**
	 */
	public static class JGraphEllipseRenderer extends VertexRenderer {
			
		/**
		 * Return a slightly larger preferred size than for a rectangle.
		 */
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.width += d.width / 8;
			d.height += d.height / 2;
			return d;
		}
		
		/**
		 */
		public void paint(Graphics g) {		
			int b = borderWidth;
			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();
			boolean tmp = selected;
			if (super.isOpaque()) {
				g.setColor(super.getBackground());
				if (gradientColor != null && !preview) {
					setOpaque(false);
					g2.setPaint(new GradientPaint(0, 0, getBackground(),
							getWidth(), getHeight(), gradientColor, true));
				}
				g.fillOval(b - 1, b - 1, d.width - b, d.height - b);
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
				g.drawOval(b - 1, b - 1, d.width - b, d.height - b);
			}
			if (selected) {
				//g2.setStroke(GraphConstants.SELECTION_STROKE);
				g2.setStroke(new BasicStroke(3));
				g.setColor(highlightColor);
				g.setColor(Color.BLUE);
				g.drawOval(b - 1, b - 1, d.width - b, d.height - b);
			}
		}
	}
}