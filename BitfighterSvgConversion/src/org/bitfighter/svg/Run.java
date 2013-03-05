package org.bitfighter.svg;
import javax.swing.JFrame;


public class Run {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame ("Bitfighter SVG Converter (alpha)");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new BitfighterSvgConverterGui());
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);
	}

}
