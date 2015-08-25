package src;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class FinderFrame {

	private static void createAndDisplayGui(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		JFrame frame = new JFrame("WordFinder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new FlowLayout());
		frame.setPreferredSize(new Dimension(400,150));
		
		JLabel label = new JLabel("Document to highlight:\t");
		frame.add(label);
		
		JButton button = new JButton("Select File");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				JFileChooser fileChooser = new JFileChooser();
				int returnValue = fileChooser.showOpenDialog(null);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					File selectedFile = fileChooser.getSelectedFile();
					WordFinder wf = new WordFinder("TestWords1.txt",selectedFile);
					try{
						wf.highlightMatches();
					}catch(IOException e){
						System.err.println("Program failed with an IOException.\nEnsure your filenames are correct.\n");
						return;
					}
				}
			}
		});
		frame.add(button);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				createAndDisplayGui();
			}
		});
	}

}
