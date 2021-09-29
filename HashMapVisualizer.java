import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

/**
 * HashMap Visualizer, CIS 121
 * @author jongmin, 21sp
 */
public class HashMapVisualizer {
	
	enum mapOp {
		CONTAINS_KEY,
		CONTAINS_VALUE,
		GET,
		PUT,
		REMOVE,
		CLEAR
	}
	
	enum state {
		HASH,
		TRIE
	}
	
	private static JFrame mainFrame;
	private static JPanel mainPanel;
	private static JPanel graphViewPanel;
	private static GridBagLayout mainLayout;
	
	private final static int MAX_CELL_SIZE = 80;
	private static int MAX_SIZE;
	
	private static HashMap<String, String> hashMap;
	private static MultiGraph graphView;
	
	 protected static String styleSheet = 
				"node {" + 
						"fill-color: #d3d3d3;" + 
						"size: 23px;" + 
						"fill-mode: dyn-plain;" + 
						"stroke-color: black;" + 
						"stroke-width: 1px;" + 
						"text-size: 21px;" + 
					"}" + 
				"edge {" + 
						"text-size: 25px;" + 
						"text-background-mode: plain;"+ 
						"text-background-color: white;"+ 
					"}"
	            + "node.source { fill-color: green; }"
	            + "node.target { fill-color: red; }"
	            + "node.sourceTarget { fill-color: yellow; }"
	            + "edge.marked {fill-color: red; size: 4px; }";
	
	public static void main(String[] args) throws Exception {
		Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        MAX_SIZE = (int) (Math.min(screenSize.width, screenSize.height) * 0.75);
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        UIManager.put("Button.disabledText", Color.black);
        
		graphView = new MultiGraph("Graph");
		Viewer viewer = new Viewer(graphView, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.disableAutoLayout();
		graphViewPanel = viewer.addDefaultView(false);
        
        javax.swing.SwingUtilities.invokeLater(HashMapVisualizer::createAndShowGUI);
	}
	
	private static void createAndShowGUI() {
        mainFrame = new JFrame("HashMap Visualizer");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        
        mainPanel = new JPanel();
        mainFrame.setSize(MAX_SIZE, MAX_SIZE);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
		JLabel keyLabel = new JLabel("Key: ");
		JTextField keyTextField = new JTextField();
		keyTextField.setColumns(5);
		
		JLabel valueLabel = new JLabel("Value: ");
		JTextField valueTextField = new JTextField();
		valueTextField.setColumns(5);

        JPanel controlPanel = new JPanel();
        
        JButton putBtn = new JButton("Put");
        putBtn.addActionListener(e -> {
        	hashOperation(mapOp.PUT, keyTextField.getText(), valueTextField.getText());
        });
        
        JButton containsKeyBtn = new JButton("Contains Key");
        containsKeyBtn.addActionListener(e -> {
        	hashOperation(mapOp.CONTAINS_KEY, keyTextField.getText(), valueTextField.getText());
        });
        
        JButton containsValueBtn = new JButton("Contains Value");
        containsValueBtn.addActionListener(e -> {
        	hashOperation(mapOp.CONTAINS_VALUE, keyTextField.getText(), valueTextField.getText());
        });
        
        JButton getBtn = new JButton("Get");
        getBtn.addActionListener(e -> {
        	hashOperation(mapOp.GET, keyTextField.getText(), valueTextField.getText());
        });
        
        JButton removeBtn = new JButton("Remove");
        removeBtn.addActionListener(e -> {
        	hashOperation(mapOp.REMOVE, keyTextField.getText(), valueTextField.getText());
        });
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
        	hashOperation(mapOp.CLEAR, keyTextField.getText(), valueTextField.getText());
        });
        
        JButton newMapBtn = new JButton("New Map");
        newMapBtn.addActionListener(e -> initMap());
        
        controlPanel.add(keyLabel);
        controlPanel.add(keyTextField);
        controlPanel.add(valueLabel);
        controlPanel.add(valueTextField);
        
        controlPanel.add(putBtn);
        controlPanel.add(containsKeyBtn);
        controlPanel.add(containsValueBtn);
        controlPanel.add(getBtn);
        controlPanel.add(removeBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(newMapBtn);
        
        
        mainFrame.add(controlPanel, BorderLayout.NORTH);
        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setResizable(true);
        mainFrame.pack();
        mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
        
        newMapBtn.doClick();
    }
	
	private static void initMap() {
		JDialog dialog = new JDialog(mainFrame, "New Hash Table", true);
		
		JPanel panel = new JPanel();
		
		panel.add(new JLabel("Capacity: "));
		JTextField capacityTextField = new JTextField();
		capacityTextField.setColumns(5);
		panel.add(capacityTextField);
		
		panel.add(new JLabel("Load Factor: "));
		JTextField loadFactorField = new JTextField();
		loadFactorField.setColumns(5);
		panel.add(loadFactorField);
		
		JButton mapBtn = new JButton("Create");
		mapBtn.addActionListener(e -> {
			
			int capacity;
			float loadFactor;
			
			try {
				capacity = Integer.valueOf(capacityTextField.getText());
				loadFactor = Float.valueOf(loadFactorField.getText());
			} catch (Exception _e) {
				JOptionPane.showMessageDialog(dialog, "Non integer/decimal value inputted in textfields");
				return;
			}
			
			hashMap = new HashMap<>(capacity, loadFactor);
			repaintHash();
			dialog.dispose();
		});
		
		JPanel mapBtnPanel = new JPanel();
		mapBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		mapBtnPanel.add(mapBtn);
		
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(panel, BorderLayout.NORTH);
		mainPanel.add(mapBtnPanel, BorderLayout.SOUTH);
		
		dialog.setContentPane(mainPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
	}
	
	private static void repaintHash() {
		mainFrame.remove(graphViewPanel);
		mainPanel.removeAll();
		
        mainLayout = new GridBagLayout();
        mainPanel.setLayout(mainLayout);
        
        HashMap.Entry<String, String>[] table = hashMap.getTable();
        int tableSize = table.length;
        
        int maxCol = 1;
        
        for (HashMap.Entry<String, String> e : table) {
        	
        	HashMap.Entry<String, String> eCopy = e;
        	
        	int count = 1;
        	
        	while (eCopy != null) {
        		count++;
        		eCopy = eCopy.next;
        	}
        	
        	maxCol = Math.max(maxCol, count);
        }
        
        final int size = Math.min(MAX_CELL_SIZE, Math.min(MAX_SIZE / maxCol, MAX_SIZE / tableSize));
        
        final Dimension d = new Dimension(size, size);
        
        for (int i = 0; i < tableSize; i++) {
        	HashMap.Entry<String, String> e = table[i];
        	
        	int xAxis = 1;
        	
        	GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 0, 5, 5);
            c.gridx = 0;
            c.gridy = i;
            
            JLabel bucketLabel = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            
            bucketLabel.setOpaque(true);
            bucketLabel.setBackground(Color.black);
            
            bucketLabel.setForeground(Color.white);
            
            bucketLabel.setPreferredSize(d);
            
            mainPanel.add(bucketLabel, c);
            
            while (e != null) {
            	GridBagConstraints c1 = new GridBagConstraints();
                c1.insets = new Insets(0, 0, 5, 5);
                c1.gridx = xAxis;
                c1.gridy = i;
                
                KeyValuePanel keyValuePanel = new KeyValuePanel(e.getKey(), e.getValue(), size);
                
                mainPanel.add(keyValuePanel, c1);
                
                xAxis++;
                e = e.next;
            }
        }
        
        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.repaint();
	}
	
	public static void hashOperation(mapOp op, String key, String value) {
		
		if (key.equals("null")) {
			key = null;
		}
		
		if (value.equals("null")) {
			value = null;
		}
		
		switch (op) {
		case PUT:
			hashMap.put(key, value);
			repaintHash();
			break;
		case REMOVE:
			hashMap.remove(key);
			repaintHash();
			break;
		case CLEAR:
			hashMap.clear();
			repaintHash();
			break;
		case CONTAINS_KEY:
			boolean containsKey = hashMap.containsKey(key);
			JOptionPane.showMessageDialog(mainFrame, key + (containsKey ? " exists " : " missing ") + "in the keys of the map");
			break;
		case CONTAINS_VALUE:
			boolean containsValue = hashMap.containsValue(value);
			JOptionPane.showMessageDialog(mainFrame, value + (containsValue ? " exists " : " missing ") + "in the values of the map");
			break;
		case GET:
			String v = hashMap.get(key);
			repaintHash();
			JOptionPane.showMessageDialog(mainFrame, "Value for key " + key + " is " + v);
			break;
		}
	}

}

class KeyValuePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	JPanel p;
	JLabel keyLabel;
	JLabel valueLabel;
	
	public KeyValuePanel(String key, String value, int size) {
		
		if (key == null) {
			key = "null";
		}
		
		if (value == null) {
			value = "null";
		}
		
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		keyLabel = new JLabel(key, SwingConstants.CENTER);
		valueLabel = new JLabel(value, SwingConstants.CENTER);
		
		keyLabel.setBorder(new LineBorder(Color.black));
		valueLabel.setBorder(new LineBorder(Color.black));
		
		Dimension keyD = new Dimension((size *  2)/3, size);
		Dimension valueD = new Dimension(size, size);
		
		keyLabel.setPreferredSize(keyD);
		valueLabel.setPreferredSize(valueD);
		
		this.add(keyLabel);
		this.add(valueLabel);
	}
	
	public void setKeyValueText(String key, String value) {
		keyLabel.setText(key);
		valueLabel.setText(value);
	}
	
}



