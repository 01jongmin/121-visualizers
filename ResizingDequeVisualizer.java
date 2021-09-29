import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.NoSuchElementException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * ResizingDeque Visualizer, CIS 121
 * @author jongmin, 21sp
 */

public class ResizingDequeVisualizer {
	
	private static JFrame mainFrame;
	private static JPanel mainPanel;
	private static GridBagLayout mainLayout;
	
	private final static int MAX_CELL_SIZE = 80;
	private static int MAX_SIZE;
	
	private static ResizingDeque<String> deque;
	
	private static JLabel lastRemovedElement;
	
	public static void main(String[] args) throws Exception {
		Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        MAX_SIZE = (int) (Math.min(screenSize.width, screenSize.height) * 0.75);
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        UIManager.put("Button.disabledText", Color.black);
        
        deque = new ResizingDequeImpl<String>();
        javax.swing.SwingUtilities.invokeLater(ResizingDequeVisualizer::createAndShowGUI);
	}
	
	private static void createAndShowGUI() {
        mainFrame = new JFrame("ResizingDeque Visualizer");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        
        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        
		JLabel elementLabel = new JLabel("Element to Add: ");
		JTextField elementTextField = new JTextField();
		elementTextField.setColumns(5);

        JPanel controlPanel = new JPanel();
        
        JButton addFirstBtn = new JButton("Add First");
        addFirstBtn.addActionListener(e -> {
        	addFirst(elementTextField.getText());
        	elementTextField.setText("");
        });
        
        JButton addLastBtn = new JButton("Add Last");
        addLastBtn.addActionListener(e -> {
        	addLast(elementTextField.getText());
        	elementTextField.setText("");
        });
        
        JButton removeFirstBtn = new JButton("Remove First");
        removeFirstBtn.addActionListener(e -> {
        	removeFirst();
        });
        
        JButton removeLastBtn = new JButton("Remove Last");
        removeLastBtn.addActionListener(e -> {
        	removeLast();
        });
        
        JLabel lastRemovedLabel = new JLabel("Last Removed: ");
        lastRemovedElement = new JLabel();
        
        controlPanel.add(elementLabel);
        controlPanel.add(elementTextField);
        controlPanel.add(addFirstBtn);
        controlPanel.add(addLastBtn);
        controlPanel.add(removeFirstBtn);
        controlPanel.add(removeLastBtn);
        controlPanel.add(lastRemovedLabel);
        controlPanel.add(lastRemovedElement);
        
        mainFrame.add(controlPanel, BorderLayout.NORTH);
        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
        repaint();
    }
	
	private static void addFirst(String e) {
		if (e.isEmpty()) {
			JOptionPane.showMessageDialog(mainFrame, "Element field is Empty");
			return;
		}
		
		deque.addFirst(e);
		repaint();
	}
	
	private static void addLast(String e) {
		if (e.isEmpty()) {
			JOptionPane.showMessageDialog(mainFrame, "Element field is Empty");
			return;
		}
		
		deque.addLast(e);
		repaint();
	}
	
	private static void removeFirst() {
		try {
			lastRemovedElement.setText(deque.pollFirst());
		} catch (NoSuchElementException e) {
			JOptionPane.showMessageDialog(mainFrame, "NoSuchElementException");
		}
		
		repaint();
	}
	
	private static void removeLast() {
		try {
			lastRemovedElement.setText(deque.pollLast());
		} catch (NoSuchElementException e) {
			JOptionPane.showMessageDialog(mainFrame, "NoSuchElementException");
		}
		
		repaint();
	}
	
	
	private static void repaint() {
		mainPanel.removeAll();
        mainLayout = new GridBagLayout();
        mainPanel.setLayout(mainLayout);
        
        Object[] dequeArray = deque.getArray();
        int dequeSize = dequeArray.length;
        
        String start = null;
        String end = null;
        
        if (deque.size() > 0) {
        	start = deque.peekFirst();
            end = deque.peekLast();
        }
        
        final int size = Math.min(MAX_CELL_SIZE, MAX_SIZE / dequeSize);
        
        final Dimension d = new Dimension(size, size);
        
        for (int i = 0; i < dequeSize; i++) {
        	
        	String s = (String) dequeArray[i];
        	
        	final JLabel label = new JLabel(s, SwingConstants.CENTER);
        	
        	
        	if (start != null && end != null) {
        		if (s == start && s == end) {
        			label.setBorder(new LineBorder(Color.yellow));
            	} else if (s == start) {
            		label.setBorder(new LineBorder(Color.green));
            	} else if (s == end) {
            		label.setBorder(new LineBorder(Color.red));
            	} else {
            		label.setBorder(new LineBorder(Color.black));
            	}
        	} else {
        		label.setBorder(new LineBorder(Color.black));
        	}
        	
        	label.setBackground(Color.white);
        	label.setPreferredSize(d);
        	
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 0, 5, 5);
            c.gridx = i;
            c.gridy = 0;
            mainPanel.add(label, c);
        }
        
        mainFrame.pack();
        mainFrame.repaint();
	}
}
