package com.describer.presentation.props;

import java.awt.Component;
import java.util.Collection;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.describer.context.Context;
import com.describer.context.ContextElement;
import com.describer.context.ContextEvent;
import com.describer.context.Entity;
import com.describer.context.Key;
import com.describer.context.MM;
import com.describer.context.Requisite;
import com.describer.presentation.Presenter;
import com.describer.presentation.PresenterListener;
import com.describer.presentation.upd.AbstractUpdatingDelegate;
import com.describer.presentation.upd.Updateable;
import com.describer.presentation.upd.UpdatingDelegate;

public class CtxProperties extends JTable implements PresenterListener
, Updateable
{
	
	private Context ctx;
	private Presenter presenter;
	private Object obj;
	
	// for column size storing 
	int width0 = 80;

	// ctor
	public CtxProperties(CtxTableModel tModel) {
		super(tModel);
		tModel.addTableModelListener(new tableListener());
		tModel.setColumnNames(new String[]{"Property", "Value"});
		getColumnModel().getColumn(1).setResizable(false);
	}
	
	private UpdatingDelegate updDeleg = new AbstractUpdatingDelegate() {
		protected void update() {
			if (presenter.getSelection().size() != 1) {
				setObj(null);
				return;
			}
			Object selected = presenter.getSelection()
											.iterator().next();
			setObj(selected);	
		}
	};
	
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
	}

	public void setObj(Object obj) {
		this.obj = obj;
		rebuild();
	}

	private void rebuild() {
				
		// we need to discard editing first
		if (cellEditor != null) 
			cellEditor.cancelCellEditing();
		CtxTableModel tModel = (CtxTableModel)getModel();
		tModel.setRowCount(0);
		Object[] property = new Object[2];
		if (obj instanceof Entity){
			property[0] = "id";
			property[1] = ((Entity)obj).getId();	
			tModel.addRow(property);
			property[0] = "name";
			property[1] = ((Entity)obj).getName();	
			tModel.addRow(property);
			property[0] = "description";
			property[1] = ((Entity)obj).getSemantics();	
			tModel.addRow(property);
		} else if (obj instanceof Requisite){
			property[0] = "id";
			property[1] = ((Requisite)obj).getId();	
			tModel.addRow(property);
			property[0] = "name";
			property[1] = ((Requisite)obj).getName();	
			tModel.addRow(property);
			property[0] = "description";
			property[1] = ((Requisite)obj).getSemantics();	
			tModel.addRow(property);
			property[0] = "required";
			property[1] = ((Requisite)obj).getRequired();	
			tModel.addRow(property);
		} else if (obj instanceof Key){
			property[0] = "id";
			property[1] = ((Key)obj).getId();	
			tModel.addRow(property);
			/*
			property[0] = "для";
			property[1] = ((Key)obj).getEntity().getName();	
			tModel.addRow(property);
			*/			
		} else if (obj instanceof MM){
			property[0] = "id";
			property[1] = ((MM)obj).getId();	
			tModel.addRow(property);
		}
		
        setUpPropsColumn(getColumnModel().getColumn(1));
               
	}
		
	public void doLayout() {
		if (getTableHeader().getResizingColumn() != null) {
			if (getTableHeader().getResizingColumn().getModelIndex() == 0)
				width0 = getTableHeader().getResizingColumn().getWidth(); 
		} 
		getColumnModel().getColumn(0).setWidth(width0);
		getColumnModel().getColumn(1).setWidth(getSize().width - width0);
		
		//super.doLayout();		
	}

	private void setUpPropsColumn(TableColumn column) {
		
        //Set up the editor for the props cells.
        column.setCellEditor(new CtxPropsCellEditor());

        //Set up the renderer for the props cells.
        column.setCellRenderer(new CtxPropsCellRenderer());
		
	}

	public void updateSelection(Object source, Collection<Object> selection) {
		if (selection.size() != 1) {
			setObj(null);
			return;
		}
		Object selected = selection.iterator().next();
		setObj(selected);		
	}

	@Override
	public void elementsAdded(ContextEvent e) {
		if (e.getNewElements().size() == 1) {
			setObj(e.getNewElements().get(0));
			return;
		}
		setObj(null);
	}

	@Override
	public void elementsChanged(ContextEvent e) {
		if (e.getChangedElements().size() == 1) {
			setObj(e.getChangedElements().get(0));
			return;
		}
		setObj(null);
	}

	@Override
	public void elementsRemoved(ContextEvent e) {
		setObj(null);
	}



	@Override
	public void elementConverted(ContextEvent e) {
		setObj(e.getConvertedNewElement());		
	}

	@Override
	public UpdatingDelegate getUpdDelegate() {
		return updDeleg;
	}

	public class CtxPropsCellRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			return getDefaultRenderer(value.getClass())
				.getTableCellRendererComponent(table, value, isSelected
						, hasFocus, row, column);
		}

	}
	
	public class CtxPropsCellEditor implements TableCellEditor {
		
		TableCellEditor editorDelegate;
		Object prevValue;
		Object newValue;
		
		@Override
		public Object getCellEditorValue() {
			newValue = editorDelegate.getCellEditorValue(); 
			validate(newValue);
			return newValue;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			prevValue = value;
			editorDelegate = getDefaultEditor(value.getClass());
			return editorDelegate.getTableCellEditorComponent(table, value, isSelected, row, column);
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
			editorDelegate.addCellEditorListener(l);			
		}

		@Override
		public void cancelCellEditing() {
			editorDelegate.cancelCellEditing();			
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			return true;
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			editorDelegate.removeCellEditorListener(l);
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return editorDelegate.shouldSelectCell(anEvent);
		}

		@Override
		public boolean stopCellEditing() {
			return editorDelegate.stopCellEditing();
		}
		
		private void validate(Object newValue) {
			if (editorDelegate.getClass().getCanonicalName().equals("javax.swing.JTable.NumberEditor")) {
				Object value = null;
				try {
					value = Integer.parseInt((String) newValue);					
				} catch (Exception e) {
					value = prevValue; 
				} 
				this.newValue = value;				
			}			
		}

	}
	
	class tableListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			if (e.getType() == TableModelEvent.UPDATE) {
				TableModel tmodel = ((TableModel)e.getSource());
				int rowChanged = e.getFirstRow(); 
				if (rowChanged == e.getLastRow() 
						&& rowChanged >=0) {
					Object value = tmodel.getValueAt(rowChanged, 1);
					if (obj instanceof ContextElement) {
						ContextElement el = (ContextElement) obj;
						boolean result = false;
						if (el.isEntity()){
							switch (rowChanged) {
					            case 0: result = ctx.setId(el, (Integer)value); break;
					            case 1: result = ctx.setName(el, (String)value); break;
					            case 2: result = ctx.setSemantics(el, (String)value); break;
							}
						} else if (el.isRequisite()){
							switch (rowChanged) {
								case 0:	result = ctx.setId(el, (Integer) value); break;
								case 1:	result = ctx.setName(el, (String) value); break;
								case 2:	result = ctx.setSemantics(el, (String) value);	break;
								case 3:	result = ctx.setRequired(el, (Boolean) value);	break;
							}							
						} else if (el.isKey()){
							switch (rowChanged) {
								case 0:	result = ctx.setId(el, (Integer) value); break;
							}
						} else if (el.isMM()){
							switch (rowChanged) {
								case 0:	result = ctx.setId(el, (Integer) value); break;
							}
						}
						// If result == false, i.e. nothing really
						// changed, we should to revert any changes
						// we made to the table
						if (!result) setObj(el);
					}
				}								
			}
		}
		
	}
	
}
