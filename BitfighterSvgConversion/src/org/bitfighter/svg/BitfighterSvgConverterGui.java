package org.bitfighter.svg;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


public class BitfighterSvgConverterGui extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private static final int MinPointsPerLevelLine = 1;
	// Max level line args divided by two (each point), minus two (the object and width)
	private static final int MaxPointsPerLevelLine = 62;
	
	private static URI fileUri;
	
	private ActionListener convertListener;
	private ActionListener openFileListener;
	private JPanel mainPanel;
	private JPanel openPanel;
	private JPanel actionPanel;
	private JPanel resultsPanel;
	private JButton convertButton;
	private JButton openButton;
	private JTextArea resultsArea;
	private JScrollPane resultsScrollPane;
    private JFileChooser fileChooser;
	protected JLabel fileNameLabel;
	

	protected JLabel barrierWidthLabel;
	protected JLabel maxBarrierPointsLabel;
	protected JLabel scaleFactorLabel;
	protected JLabel flatnessLabel;
	private JTextField barrierWidthField;
	private JTextField maxBarrierPointsField;
	private JTextField scaleFactorField;
	private JTextField flatnessField;
	private JPanel optionsPanel;
	
	public BitfighterSvgConverterGui() {
		
		convertListener = new ConvertListener();
		openFileListener = new OpenFileListener();
		
		mainPanel = new JPanel();
		
		resultsPanel = new JPanel();
		openPanel = new JPanel();
		actionPanel = new JPanel();
		
		fileChooser = new JFileChooser();
		openButton = new JButton("Open File");
		openButton.setToolTipText("Select an SVG file to convert");
		
		resultsArea = new JTextArea(15, 16);
		resultsScrollPane = new JScrollPane(resultsArea);

		// layout GUI components
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setPreferredSize(new Dimension(600, 400));
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		
		resultsArea.setEditable(false);
		openButton.addActionListener(openFileListener);
		FlowLayout fl_openPanel = new FlowLayout(FlowLayout.LEFT, 5, 5);
		openPanel.setLayout(fl_openPanel);

		// Add GUI components
		openPanel.add(openButton);
		resultsPanel.add(resultsScrollPane);
		
		mainPanel.add(openPanel);
		
		fileNameLabel = new JLabel("Filename: ");
		openPanel.add(fileNameLabel);
		
		optionsPanel = new JPanel();
		mainPanel.add(optionsPanel);
		optionsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("84px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("37px"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.LINE_GAP_ROWSPEC,
				RowSpec.decode("19px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		barrierWidthLabel = new JLabel("Barrier width:");
		barrierWidthLabel.setToolTipText("Self explanitory.  Default: 50");
		optionsPanel.add(barrierWidthLabel, "2, 2, left, center");
		barrierWidthField = new JTextField(3);
		barrierWidthField.setToolTipText("Self explanitory.  Default: 50");
		barrierWidthField.setHorizontalAlignment(SwingConstants.TRAILING);
		barrierWidthField.setText("50");
		optionsPanel.add(barrierWidthField, "10, 2, left, top");
		maxBarrierPointsLabel = new JLabel("Max points per barrier:");
		maxBarrierPointsLabel.setToolTipText("This is the maximum allowed points (from the starting point) for any barrier.  Allowed values:  " + MinPointsPerLevelLine + " - " + MaxPointsPerLevelLine);
		optionsPanel.add(maxBarrierPointsLabel, "2, 4, 7, 1");
		maxBarrierPointsField = new JTextField(2);
		maxBarrierPointsField.setToolTipText("This is the maximum allowed points (from the starting point) for any barrier.  Allowed values:  " + MinPointsPerLevelLine + " - " + MaxPointsPerLevelLine);
		maxBarrierPointsField.setHorizontalAlignment(SwingConstants.TRAILING);
		maxBarrierPointsField.setText("" + MaxPointsPerLevelLine);
		optionsPanel.add(maxBarrierPointsField, "10, 4");
		scaleFactorLabel = new JLabel("Scale factor:");
		scaleFactorLabel.setToolTipText("The factor multiplied against the original SVG coordinates to reduce the size for the Bitfighter editor.  Common values:  0.01 - 0.5");
		optionsPanel.add(scaleFactorLabel, "2, 6");
		scaleFactorField = new JTextField(8);
		scaleFactorField.setToolTipText("The factor multiplied against the original SVG coordinates to reduce the size for the Bitfighter editor.  Common values:  0.01 - 0.5");
		scaleFactorField.setHorizontalAlignment(SwingConstants.TRAILING);
		scaleFactorField.setText("0.1");
		optionsPanel.add(scaleFactorField, "8, 6, 3, 1");
		flatnessLabel = new JLabel("Flatness:");
		flatnessLabel.setToolTipText("The maximum distance that the line segments used to approximate the curved segments are allowed to deviate from any point on the original curve.  Common values:  0.001 - 0.5");
		optionsPanel.add(flatnessLabel, "2, 8");
		flatnessField = new JTextField(8);
		flatnessField.setToolTipText("The maximum distance that the line segments used to approximate the curved segments are allowed to deviate from any point on the original curve.  Common values:  0.001 - 0.5");
		flatnessField.setText("0.1");
		flatnessField.setHorizontalAlignment(SwingConstants.TRAILING);
		optionsPanel.add(flatnessField, "8, 8, 3, 1");
		mainPanel.add(actionPanel);
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		
		convertButton = new JButton("Convert");
		convertButton.setToolTipText("Convert to Bitfighter level code");
		actionPanel.add(convertButton);
		
		// Add action listeners
		convertButton.addActionListener(convertListener);
		mainPanel.add(resultsPanel);
		
		this.add(mainPanel);
	}
	
	private class ConvertListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			
			// Do input checking
			boolean hasError = false;
			String errorString = "";
			
			if(fileUri == null || "".equals(fileUri.toString())) {
				errorString += "Invalid file\n";
				hasError = true;
			} else if(!fileUri.toString().toLowerCase().matches(".*\\.svg$")) {
				errorString += "File must have '.svg' extension\n";
				hasError = true;
			}
			
			int barrierWidth = -1; 
			int maxBarrierPoints = -1;
			double scaleFactor = -1.0d;
			double flatness = -1.0d;
			
			try {
				barrierWidth = Integer.parseInt(barrierWidthField.getText());
			} 
			catch (Exception e) {
				errorString += "Invalid barrier width\n";
				hasError = true;
			}
			
			try {
				maxBarrierPoints = Integer.parseInt(maxBarrierPointsField.getText());
			} 
			catch (Exception e) {
				errorString += "Invalid maximum barrier points\n";
				hasError = true;
			}
			
			try {
				scaleFactor = Double.parseDouble(scaleFactorField.getText());
			} 
			catch (Exception e) {
				errorString += "Invalid scale factor\n";
				hasError = true;
			}
			
			try {
				flatness = Double.parseDouble(flatnessField.getText());
			} 
			catch (Exception e) {
				errorString += "Invalid flatness\n";
				hasError = true;
			}
			
			// Now bounds
			if(barrierWidth < 0 || barrierWidth > 500) {
				errorString += "Barrier with must be within 0 - 500\n";
				hasError = true;
			}
			
			if(maxBarrierPoints < MinPointsPerLevelLine || 
					maxBarrierPoints > MaxPointsPerLevelLine) {
				errorString += "Maximum barrier points must be with in " + MinPointsPerLevelLine +
						" - " + MaxPointsPerLevelLine + "\n";
				hasError = true;
			}
			
			if(scaleFactor < 0 || scaleFactor > 100) {
				errorString += "Scale factor must be within 0 - 100\n";
				hasError = true;
			}
			
			if(flatness < 0 || flatness > 100) {
				errorString += "Flatness factor must be within 0 - 100\n";
				hasError = true;
			}
			
			// Errors!  return error string
			if(hasError) {
				resultsArea.setText(errorString);
			}
			// No errors?  compute!
			else {
				String output = SvgToLevelLine.run(fileUri, barrierWidth,
						maxBarrierPoints, scaleFactor, flatness);

				resultsArea.setText(output);
			}
		}
		
	}
	
	private class OpenFileListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			
	        //Handle open button action.
	        if (event.getSource() == openButton) {
	            int returnVal = fileChooser.showOpenDialog(BitfighterSvgConverterGui.this);

	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fileChooser.getSelectedFile();
	                
	                //This is where a real application would open the file.
	                fileNameLabel.setText("Filename: " + file.getAbsolutePath());
	                
	                fileUri = file.toURI();
	            }
	        }

		}
		
	}
}
