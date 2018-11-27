package com.describer.presentation.props;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class CtxTableModel extends AbstractTableModel {
	
//
// Instance Variables
//

    /**
     * The <code>Vector</code> of <code>Vectors</code> of 
     * <code>Object</code> values.
     */
    protected Vector<Vector<Object>>    data;

    /** The <code>Vector</code> of column identifiers. */
    protected Vector<String>    columnNames;

// ctor
    
    /**
     *  Constructs <code>CtxTableModel</code> 
     *  which is a table of zero columns and zero rows.
     */
    public CtxTableModel() {
        this(0, 0);
    }
    
    private CtxTableModel(int rowCount, int colCount) {
    	columnNames = new Vector<String>();
    	columnNames.setSize(colCount);
    	data = new Vector<Vector<Object>>();
    	data.setSize(rowCount);
    	for (Object obj : data) {
    		obj = new Vector<Object>(colCount);
    	}
    }
    
    public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    public Object getValueAt(int row, int col) {
    	if (data.size() > row && data.get(row).size() > col)
    		return data.get(row).get(col);
    	return null;
    }

    public boolean isCellEditable(int row, int col) {
        if (col < 1) {
            return false;
        } else {
            return true;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        data.get(row).set(col, value);
        fireTableCellUpdated(row, col);
    }

	public void addRow(Object[] property) {
		Vector<Object> newLine = new Vector<Object>();
		newLine.setSize(property.length);
		for (int i = 0; i < property.length; i++) {
			newLine.set(i, property[i]);
		}
		data.add(newLine);
		fireTableRowsInserted(data.size()-1, data.size()-1);
	}

	public void setRowCount(int rowCount) {
		int curCnt = getRowCount();
		if (rowCount <= curCnt) {
			data.setSize(rowCount);
			fireTableRowsDeleted(rowCount-1, curCnt-1);
		} else {
			for (int i = rowCount - curCnt; i > 0; i--) {
				Vector<Object> newLine = new Vector<Object>();
				newLine.setSize(getColumnCount());
				data.add(newLine);
			}
			fireTableRowsInserted(curCnt, rowCount-1);
		}
	}

	public void setColumnNames(String[] names) {
		columnNames.setSize(names.length);
		for (int i = 0; i < names.length; i++) {
			columnNames.set(i, names[i]);
		}
		fireTableStructureChanged();
	}

}
