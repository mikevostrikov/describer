package com.describer.context;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class TagsEditor extends JDialog implements ActionListener {
	
	protected static JTable propsT = new JTable();
	
	protected static Context ctx = null;
	
	public TagsEditor(JFrame main, Context ctx) {
		super(main, "Tag Properties' Names Editor", true);
		
		TagsEditor.ctx = ctx;
				
		initTable(ctx.getXmlTagNames());
				
		//Create and initialize the buttons.
		JButton resetButton = new JButton("Reset changes");
        resetButton.setActionCommand("Reset changes");
        resetButton.addActionListener(this);    
        
		JButton loadButton = new JButton("From file");
        loadButton.setActionCommand("From file");
        loadButton.addActionListener(this);    
        
        JButton saveButton = new JButton("Save to file");
        saveButton.setActionCommand("Save to file");
        saveButton.addActionListener(this);
        
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);
        //
        JButton setButton = new JButton("OK");
        setButton.setActionCommand("OK");
        setButton.addActionListener(this);
        
        getRootPane().setDefaultButton(setButton);
		
		//Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(resetButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(loadButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(saveButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);
        
        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(new JScrollPane(propsT), BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
        
        //Initialize values.
        pack();
	}	
	
	private void initTable(Properties props){
		String[] columnNames = { "Свойство", "Имя тега/аттр" };
		TModel tModel = new TModel();
		tModel.setColumnIdentifiers(columnNames);
		tModel.setRowCount(0);
				
		// fill table with values
		for (Object key : props.keySet()){
			Object value = props.get(key);
			Object[] property = new Object[2];
			property[0] = key;
			property[1] = value;
			tModel.addRow(property); 			
		}
		
		propsT.setModel(tModel);
		propsT.setFillsViewportHeight(true);
		propsT.setPreferredScrollableViewportSize(new Dimension(500, 240));
	}
	
    //Handle clicks on the buttons.
    public void actionPerformed(ActionEvent e) {
        if ("Reset changes".equals(e.getActionCommand())) {
        	initTable(ctx.getXmlTagNames());            
        } else if ("From file".equals(e.getActionCommand())) {
        	openFile();        	        	        	
        } else if ("Save to file".equals(e.getActionCommand())) {
        	saveFile();        	
        } else if ("OK".equals(e.getActionCommand())) {
        	ctx.setXmlTagNames(getPropsFromTable());
        	this.dispose();
        } else if ("Cancel".equals(e.getActionCommand())) {
        	this.dispose();        	
        }
    }
    
    private Properties getPropsFromTable() {
    	Properties props = ctx.getXmlTagNames();
    	TModel tModel = (TModel)propsT.getModel();
    	for (int j = tModel.getRowCount() - 1; j >= 0; j--) {
    		props.put(tModel.getValueAt(j, 0), tModel.getValueAt(j, 1));        		
    	}
    	return props;
    }
        
    private void openFile() {
    	JFileChooser c = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "XML - Files (*.xml)";
			}

			public boolean accept(File file) {
				String name = file.getName();
				return name.endsWith(".xml") || file.isDirectory();
			}
		};
		c.setFileFilter(filter);
		c.setCurrentDirectory(new File(System.getProperty("user.dir")));
		int rVal = c.showOpenDialog(this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			try{
				loadXmlPropsNames(new FileInputStream(c.getSelectedFile()));
			} catch (Exception er){
				JOptionPane.showMessageDialog(null, er.getMessage(), er.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
    }
    
    private void saveFile() {
		JFileChooser c = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "XML - Files (*.xml)";
			}
			public boolean accept(File file) {
				String name = file.getName();
				return name.endsWith(".xml") || file.isDirectory();
			}
		};
		c.setFileFilter(filter);
		c.setCurrentDirectory(new File(System.getProperty("user.dir")));
		c.setSelectedFile(new File("xmlNamesProps.xml"));
		int rVal = c.showSaveDialog(this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			try{
				File f = c.getSelectedFile();
		        if (f.exists ()) {
		            int response = JOptionPane.showConfirmDialog (null,
		              "Вы уверены, что хотите заменить существующий файл?",
		              "Подтверждение замены файла",
		              JOptionPane.YES_NO_OPTION,
		              JOptionPane.QUESTION_MESSAGE);
		            if (response == JOptionPane.NO_OPTION) return;
		        }
		        Properties props = getPropsFromTable();
		        props.storeToXML(new FileOutputStream(f), null);
			} catch (Exception er){
				JOptionPane.showMessageDialog(null, er.getMessage(), er.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}    	
	}

	private void loadXmlPropsNames(InputStream is) throws Exception {
		try {
			Properties xmlPropsNames = new Properties();
			xmlPropsNames.loadFromXML(is);
			initTable(xmlPropsNames);
		} catch (Exception e) {
			throw e;
		}
	}

}


@SuppressWarnings("serial")
class TModel extends DefaultTableModel {
    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 1) {
            return false;
        } else {
            return true;
        }
    }

}