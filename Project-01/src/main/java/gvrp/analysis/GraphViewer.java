package gvrp.analysis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import gvrp.Customer;
import gvrp.CustomerSet;
import gvrp.Instance;
import gvrp.Point;
import gvrp.Route;
import gvrp.Solution;

public class GraphViewer extends JDialog {
	
	private static final long serialVersionUID = 3152015704699157209L;
	
	/* Size Parameters */
	public final static int DEF_H = 500;
	public final static int DEF_W = 500;
	public final static int H_INC = 27;
	public final static int W_INC = 0;
	public final static int UPPER_BORDER = 75;
	public final static int LOWER_BORDER = 40;
	public final static int LEFT_BORDER = 40;
	public final static int RIGHT_BORDER = 40;
	public final static float POINT_SIZE = 1.0f;
	public final static int FAST_BUTTON_SPEED = 5;
	
	double xScaleFactor = 1.0, yScaleFactor = 1.0;
	
	JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	
	int currentInstanceIndex = 0;
	ArrayList<String> instanceNames = new ArrayList<>();
	JButton previousButton = new JButton("<"), nextButton = new JButton(">");
	JButton previousFastButton = new JButton("<<"), nextFastButton = new JButton(">>");
	JLabel instanceNameLabel = new JLabel("", SwingConstants.CENTER);
	HashMap<String, Solution> instanceMap = new HashMap<>();
	HashMap<CustomerSet, Color> setColorMap = new HashMap<>();
	HashMap<Route, Color> routeColorMap = new HashMap<>();
	
	HashMap<Customer, Point> customerPositions = new HashMap<>();
	
	public GraphViewer() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Solution viewer");
		setBackground(Color.WHITE);
		setFrameSizeAndPos();
		panel.add(new JLabel("Instance name: "));
		panel.add(previousFastButton);
		panel.add(previousButton);
		panel.add(instanceNameLabel);
		panel.add(nextButton);
		panel.add(nextFastButton);
		panel.setBackground(Color.WHITE);
		add(panel);
		
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentInstanceIndex > 0) {
					currentInstanceIndex--;			
					repaint();
				}
			}
		});
		
		previousFastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentInstanceIndex > 0) {
					currentInstanceIndex = Math.max(0,
							currentInstanceIndex - FAST_BUTTON_SPEED);			
					repaint();
				}
			}
		});
		
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentInstanceIndex < instanceNames.size() - 1) {
					currentInstanceIndex++;					
					repaint();
				}
			}
		});
		
		nextFastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentInstanceIndex < instanceNames.size() - 1) {
					currentInstanceIndex = Math.min(instanceNames.size() - 1,
							currentInstanceIndex + FAST_BUTTON_SPEED);					
					repaint();
				}
			}
		});
		
		instanceNameLabel.setPreferredSize(new Dimension(100, 20));
	}
				
	@Override
	public void paint(Graphics g) {
		super.paintComponents(g);
		previousButton.setEnabled(currentInstanceIndex > 0);
		previousFastButton.setEnabled(currentInstanceIndex > 0);
		nextButton.setEnabled(currentInstanceIndex < instanceNames.size() - 1);
		nextFastButton.setEnabled(currentInstanceIndex < instanceNames.size() - 1);
		String instanceKey = instanceNames.get(currentInstanceIndex);
		if (instanceKey == null) return;
		instanceNameLabel.setText(instanceKey);
		Solution currentSolution = instanceMap.get(instanceKey);
		if (currentSolution == null) return;
		Instance instance = currentSolution.getInstance();
		
		int minX, minY, maxX, maxY;
		minX = minY = Integer.MAX_VALUE;
		maxX = maxY = Integer.MIN_VALUE;
		
		/* Updates maximum and minimum coordinates */
		for (Customer customer : instance.getCustomers()) {
			Point point = customer.getPoint();
			int x = point.getX(), y = point.getY();
			if (x < minX) minX = x;
			if (y < minY) minY = y;
			if (x > maxX) maxX = x;
			if (y > maxY) maxY = y;
		}
		
		xScaleFactor = (double) Math.max(getWidth() - LEFT_BORDER - RIGHT_BORDER, LEFT_BORDER + RIGHT_BORDER) / (double)(maxX - minX);
		yScaleFactor = (double) Math.max(getHeight() - UPPER_BORDER - LOWER_BORDER, UPPER_BORDER + LOWER_BORDER) / (double)(maxY - minY);
		
		/* Defines route colors */
		for (Route route : currentSolution) {
			float h = (float) route.getId() / (float) instance.getFleet();
			routeColorMap.put(route, Color.getHSBColor(h, 1, 0.75f));
		}
		
		/* Defines set colors */
		for (CustomerSet set : instance.getSets()) {
			float h = (float) set.getId() / (float) instance.getNumberOfSets();
			setColorMap.put(set, Color.getHSBColor(h, 1, 1));
		}
		
		int pointSize = (int) (POINT_SIZE * Math.sqrt(getWidth()*getHeight()*0.01/instance.getNumberOfCustomers()));
		Point depotPosition = null;
		
		/* Maps points from Cartesian plane to window coordinates */
		for (Customer customer : instance.getCustomers()) {
			Point point = customer.getPoint();
			int x = LEFT_BORDER + (int) (xScaleFactor * (point.getX() - minX));
			int y = UPPER_BORDER + (int) (yScaleFactor * (point.getY() - minY));
			Point newPosition = new Point(x, y);
			if (customer.getSet() == null) depotPosition = newPosition;
			else customerPositions.put(customer, newPosition);
		}
				
		/* Paints customers */
		customerPositions.forEach((c,p) -> {
			Color setColor = setColorMap.get(c.getSet());
			int x = p.getX() - pointSize/2, y = p.getY() - pointSize/2;
			g.setColor(setColor.darker());
			g.fillOval(x-1, y-1, pointSize+2, pointSize+2);
			g.setColor(setColor);
			g.fillOval(x, y, pointSize, pointSize);
		});
		
		/* Paints routes */
		for (Route route : currentSolution) {
			int size = route.size();
			int [] xPoints = new int[size+2], yPoints = new int[size+2];
			xPoints[0] = xPoints[size+1] = depotPosition.getX();
			yPoints[0] = yPoints[size+1] = depotPosition.getY();
			int i = 0;
			for (Customer customer : route) {
				i++;
				Point customerPos = customerPositions.get(customer); 
				xPoints[i] = customerPos.getX();
				yPoints[i] = customerPos.getY();
			}
			g.setColor(routeColorMap.get(route));
			g.drawPolyline(xPoints, yPoints, xPoints.length);
		}
		
		/* Paints depot */
		g.setColor(Color.BLACK);
		g.fillRect(depotPosition.getX() - pointSize/2, depotPosition.getY() - pointSize/2, pointSize, pointSize);
	}
		
	private void setFrameSizeAndPos() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension d = tk.getScreenSize();
			int x = (d.width - DEF_W - W_INC)/2;
		int y = (d.height - DEF_H - H_INC)/2;
		setBounds(x, y, DEF_W + W_INC, DEF_H + H_INC);
	}
	
	public void addSolution(Solution solution) {
		String instanceName = solution.getInstance().getName();
		for (int i = 0; instanceMap.get(instanceName) != null; i++)
			instanceName = solution.getInstance().getName() + " (" + i + ")" ;
		instanceMap.put(instanceName, solution);
		instanceNames.add(instanceName);
	}
		
}
