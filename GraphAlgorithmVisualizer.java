import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.view.Viewer;

/**
 * Graph Algorithm Visualizer, CIS 121
 * @author jongmin, 21sp
 */

final class GraphAlgorithmVisualizer {
	
	private enum Type {
		SHORTEST,
		WIDEST
	}

	public static final int SCREEN_WIDTH = 1250;
    public static final int SCREEN_HEIGHT = 860;

	private static JFrame mainFrame;
	
	private static Graph graph;
	
	private static Map<String, Integer> nodeToIdx;
	private static Map<Integer, String> idxToNode;
	
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
	
	private static MultiGraph graphView;

	public static void main(String[] args) throws Exception {		
		System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		UIManager.put("Button.disabledText", Color.black);
		
		initUI();
	}
	
	private static void initUI() {
		mainFrame = new JFrame("Graph Algorithm Visualizer");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		mainFrame.setLayout(new BorderLayout());
		mainFrame.setResizable(true);

		graphView = new MultiGraph("Graph");
		graphView.addAttribute("ui.stylesheet", styleSheet);
		
		Viewer viewer = new Viewer(graphView, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.enableAutoLayout();
		JPanel view = viewer.addDefaultView(false);
		
		JLabel srcTextFieldLabel = new JLabel("Source Node Id");
		JTextField srcTextField = new JTextField();
		srcTextField.setColumns(5);
		
		JLabel tgtTextFieldLabel = new JLabel("Target Node Id");
		JTextField tgtTextField = new JTextField();
		tgtTextField.setColumns(5);
		
		// Set up 3 buttons
		JButton graphBtn = new JButton("New Graph");
		graphBtn.addActionListener(e -> newGraph());

		JButton shortestPathBtn = new JButton("Find Shortest Path");
		shortestPathBtn.addActionListener(e -> graphAlgorithm(Type.SHORTEST, srcTextField.getText(), tgtTextField.getText()));
		
		JButton widestPathBtn = new JButton("Find Widest Path");
		widestPathBtn.addActionListener(e -> graphAlgorithm(Type.WIDEST, srcTextField.getText(), tgtTextField.getText()));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(graphBtn);
		buttonPanel.add(shortestPathBtn);
		buttonPanel.add(widestPathBtn);
		
		JPanel nodeIdPanel = new JPanel();
		nodeIdPanel.add(srcTextFieldLabel);
		nodeIdPanel.add(srcTextField);
		nodeIdPanel.add(tgtTextFieldLabel);
		nodeIdPanel.add(tgtTextField);

		JPanel controllerPanel = new JPanel();
		controllerPanel.setLayout(new BoxLayout(controllerPanel, BoxLayout.Y_AXIS));
		controllerPanel.add(buttonPanel);
		controllerPanel.add(nodeIdPanel);

		mainFrame.add(controllerPanel, BorderLayout.NORTH);
		mainFrame.add(view, BorderLayout.CENTER);
		mainFrame.setLocationByPlatform(true);
		mainFrame.setVisible(true);
		
		graphBtn.doClick();
	}
	
	private static void graphAlgorithm(Type type, String src, String tgt) {
		if (!nodeToIdx.containsKey(src) || !nodeToIdx.containsKey(tgt)) {
			JOptionPane.showMessageDialog(mainFrame, "Source or Target Id not found in Graph");
			return;
		}
		
		// Reset node + edge UI
		for (Node n : graphView.getNodeSet()) {
			n.removeAttribute("ui.class");
		}
		for (Edge e : graphView.getEdgeSet()) {
			e.setAttribute("ui.class", "");
		}
		
		int srcIntId = nodeToIdx.get(src);
		int tgtIntId = nodeToIdx.get(tgt);
		
		Node srcNode = graphView.getNode(String.valueOf(srcIntId));
		Node tgtNode = graphView.getNode(String.valueOf(tgtIntId));
		
		if (src.equals(tgt)) {
			srcNode.setAttribute("ui.class", "sourceTarget");
		} else {
			srcNode.setAttribute("ui.class", "source");
			tgtNode.setAttribute("ui.class", "target");
		}
		
		Object[] path = null;
		
		switch (type) {
		case WIDEST:
			path = WidestPath.getWidestPath(graph, srcIntId, tgtIntId).toArray(); 
			break;
		case SHORTEST:
			path = Dijkstra.getShortestPath(graph, srcIntId, tgtIntId).toArray();
			break;
		}
		
		Set<Integer> pathNodes = new HashSet<>();
		
		for (Object o : path) {
			int i = (int) o;
			
			if (pathNodes.contains(i)) {
				JOptionPane.showMessageDialog(mainFrame, "Duplicate nodes found in path");
				return;
			} else {
				pathNodes.add(i);
			}
			
			if (!idxToNode.containsKey(i)) {
				JOptionPane.showMessageDialog(mainFrame, "Missing node found in path");
				return;
			}
		}
		
		//check missing edge
		
		if (path.length == 0) {
			JOptionPane.showMessageDialog(mainFrame, "No Path Found");
		} else if (path.length >= 2) {
			for (int i = 1; i < path.length; i++) {
				String uString = String.valueOf((int) path[i - 1]);
				String vString = String.valueOf((int) path[i]);
				
				Edge e = graphView.getEdge(uString + "." + vString);
				
				if (e == null) {
					JOptionPane.showMessageDialog(mainFrame, "Missing Edge");
					return;
				}
				
				e.setAttribute("ui.class", "marked");
			}
		}
	}
	
	private static void newGraph() {
		JDialog dialog = new JDialog(mainFrame, "New Graph", true);
		
		JLabel hint = new JLabel("Type in the edges of your graph here and their weights");
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setPreferredSize(new Dimension(SCREEN_WIDTH/4, SCREEN_HEIGHT/2));

		JButton imp = new JButton("Import");
		imp.addActionListener(e -> {
			final String err = importGraph(editorPane.getText());
			if (err == null) {
				dialog.dispose();
				displayGraph();
			}
			else
				JOptionPane.showMessageDialog(dialog, err);
		});
		
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(hint, BorderLayout.NORTH);
		panel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
		panel.add(imp, BorderLayout.SOUTH);

		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
	}
	
	private static String importGraph(String graphString) {
		nodeToIdx = new HashMap<>();
		idxToNode = new HashMap<>();
		
		graphString = graphString.trim();
		
		String[] lines = graphString.split("\n");
		int counter = 0;
		
		for (int i = 0; i < lines.length; i++) {
			String[] edgeRep = lines[i].split(" ");
			if (edgeRep.length != 3) {
				return "Format error on line " + i + " of your graph. Check the format is '{id1} {id2} {weight}'";
			} else {
				String u = edgeRep[0];
				String v = edgeRep[1];
				
				try {
					Integer.valueOf(edgeRep[2]);
				} catch (Exception e) {
					return "Weight value on line " + i + " is not an integer value";
				}
				
				if (!nodeToIdx.containsKey(u)) {
					nodeToIdx.put(u, counter);
					idxToNode.put(counter, u);
					counter++;
				}
				
				if (!nodeToIdx.containsKey(v)) {
					nodeToIdx.put(v, counter);
					idxToNode.put(counter, v);
					counter++;
				}
			}
		}
		
		graph = new Graph(counter);
		
		for (String line : lines) {
			String[] edgeRep = line.split(" ");
			
			int uIdx = nodeToIdx.get(edgeRep[0]);
			int vIdx = nodeToIdx.get(edgeRep[1]);
			
			int weight = Integer.valueOf(edgeRep[2]);
			
			graph.addEdge(uIdx, vIdx, weight);
		}

		return null;
	}
	
	private static void displayGraph() {
		graphView.clear();
		graphView.addAttribute("ui.stylesheet", styleSheet);
		
		int size = graph.getSize();
		
		for (int i = 0; i < size; i++) {
			String nodeId = idxToNode.get(i);
			Node n = graphView.addNode(String.valueOf(i));
			n.addAttribute("ui.label", nodeId);
		}
		
		for (int u = 0; u < size; u++) {
			for (int v = 0; v < size; v++) {
				if (graph.hasEdge(u, v)) {
					String uString = String.valueOf(u);
					String vString = String.valueOf(v);
					Edge e = graphView.addEdge(uString + "." + vString, uString, vString, true);
					e.setAttribute("ui.label", graph.getWeight(u, v));
					e.setAttribute("text-background-mode", "plain");
				}
			}
		}
	}
}
