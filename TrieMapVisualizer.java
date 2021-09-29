import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;

/**
 * TrieMap Visualizer, CIS 121
 * @author jongmin, 21sp
 */
public class TrieMapVisualizer {
	
	enum mapOp {
		CONTAINS_KEY,
		CONTAINS_VALUE,
		GET,
		PUT,
		REMOVE,
		CLEAR
	}
	
	private static MultiGraph graphView;
	private static SpriteManager graphSpriteManger;
	private static int WIDTH_SIZE;
	private static int HEIGHT_SIZE;
	private static JFrame mainFrame;
    private static TrieMap<String> map;
    private static int ySegment = 0;  
    private static Set<String> keySet = new HashSet<>();
    
    private static JTextField keyTextField;
    private static JTextField valueTextField;
    
    protected static String styleSheet = 
			"node {" + 
					"fill-color: #d3d3d3;" + 
					"size: 22px;" + 
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
            + "node.marked { text-color: red; }"
            + "node.error { fill-mode: none; }"
			+ "sprite {" + 
				"fill-color: #d3d3d3;" + 
				"shape: box;" +
				"size: 23px;" + 
				"text-size: 21px;" + 
			"}"
            + "edge.marked {fill-color: red; size: 4px; }";
	
	
	public static void main(String[] args) throws Exception {
		Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		WIDTH_SIZE = (int) (screenSize.width * 0.9);
		HEIGHT_SIZE = (int) (screenSize.height * 0.9);
		
		System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        UIManager.put("Button.disabledText", Color.black);
        map = new TrieMap<>();
        javax.swing.SwingUtilities.invokeLater(TrieMapVisualizer::createAndShowGUI);
	}
	
	private static void createAndShowGUI() {
		mainFrame = new JFrame("TrieMap Visualizer");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setSize(WIDTH_SIZE, HEIGHT_SIZE);
        
		JLabel keyLabel = new JLabel("Key: ");
		keyTextField = new JTextField();
		keyTextField.setColumns(5);
		
		JLabel valueLabel = new JLabel("Value: ");
		valueTextField = new JTextField();
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
        
        graphView = new MultiGraph("Graph");
        graphSpriteManger = new SpriteManager(graphView);
		Viewer viewer = new Viewer(graphView, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.disableAutoLayout();
		JPanel view = viewer.addDefaultView(false);
		
		repaint();
        
        mainFrame.add(controlPanel, BorderLayout.NORTH);
		mainFrame.add(view, BorderLayout.CENTER);
		mainFrame.setLocationByPlatform(true);
		mainFrame.setVisible(true);
	}
	
	private static void recursiveDraw(int startX, int endX, int level, char c, TrieMap.Node<String> root, Node parentNode) {
		String id = UUID.randomUUID().toString();
		Node n = graphView.addNode(id);
		
		n.setAttribute("xy", (startX + endX)/2, -level * ySegment);
		
		if (c == ' ') {
			n.setAttribute("ui.label", "root");
		} else {
			n.setAttribute("ui.label", c);
		}
		
		if (root.hasValue()) {
			Sprite s = graphSpriteManger.addSprite(UUID.randomUUID().toString());
			s.attachToNode(id);
			s.setPosition(Units.PX, 24, 0, 0);
			s.setAttribute("ui.label", root.getValue());
		}
		
		if (parentNode != null) {
			graphView.addEdge(UUID.randomUUID().toString(), parentNode, n);
		}
		
		int numOfChild = 0;
		
		if (root.getChildren() != null) {
			n.setAttribute("ui.class", "marked");
		}
		
		if (root.hasChildren()) {
			for (TrieMap.Node<String> child: root.getChildren()) {
				if (child != null) {
					numOfChild++;
				}
			}
			
			int xSegment = (endX - startX)/numOfChild;
			int counter = 0;
			
			for (int i = 0; i < 26; i++) {
				char targetC = (char) ('a' + i);
				TrieMap.Node<String> targetN = root.getChild(targetC);
				
				if (targetN != null) {
					recursiveDraw(startX + counter * xSegment, startX + (counter + 1) * xSegment, level + 1, targetC, targetN, n);
					counter++;
				}
			}
		}
	}
	
	private static void repaint() {
		graphView.clear();
		graphView.addAttribute("ui.stylesheet", styleSheet);
		
		int maxKeyLength = 0;
		
		for (CharSequence k: keySet) {
			maxKeyLength = Math.max(k.length(), maxKeyLength);
		}
		
		ySegment = HEIGHT_SIZE/(maxKeyLength + 1);

		TrieMap.Node<String> root = map.getRoot();
		
		recursiveDraw(0, WIDTH_SIZE, 0, ' ', root, null);
	}
	
	private static void hashOperation(mapOp op, String key, String value) {
		try {
			keyTextField.setText("");
			valueTextField.setText("");
			
			switch (op) {
			case PUT:
				map.put(key, value);
				keySet.add(key);
				repaint();
				break;
			case REMOVE:
				map.remove(key);
				keySet.remove(key);
				repaint();
				break;
			case CLEAR:
				map.clear();
				keySet.clear();
				repaint();
				break;
			case CONTAINS_KEY:
				boolean containsKey = map.containsKey(key);
				JOptionPane.showMessageDialog(mainFrame, key + (containsKey ? " exists " : " missing ") + "in the keys of the map");
				break;
			case CONTAINS_VALUE:
				boolean containsValue = map.containsValue(value);
				JOptionPane.showMessageDialog(mainFrame, value + (containsValue ? " exists " : " missing ") + "in the values of the map");
				break;
			case GET:
				String v = map.get(key);
				repaint();
				JOptionPane.showMessageDialog(mainFrame, "Value for key " + key + " is " + v);
				break;
			}
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(mainFrame, "Characters for key must be in the range [a..z]");
		}
		
	}
	
	
}
