import java.awt.Image;
import java.awt.image.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FaceUI extends JFrame{
	JButton button;
	JLabel label;
	
	public FaceUI(){
		super("FaceFinder");
		button = new JButton("Browse");
		button.setBounds(300,400,100,40);
		label = new JLabel();
		label.setBounds(10,10,670,350);
		add(button);
		add(label);
		
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JFileChooser file = new JFileChooser();
				file.setCurrentDirectory(new File(System.getProperty("user.home")));;
				//filter the files
				FileNameExtensionFilter filter = new FileNameExtensionFilter("*.images", "jpg", "gif", "png");
				file.addChoosableFileFilter(filter);;
				int result = file.showSaveDialog(null);
				//if user clicks on save in Jfilechooser
				if(result == JFileChooser.APPROVE_OPTION){
					File selectedFile = file.getSelectedFile();
					//file path of the image, use this path for the original image in FaceFinder
					String path = selectedFile.getAbsolutePath();
					//after running the FaceFinder program and getting the new image with rectangles on the face
					//set the new image file path to path, so it can be displayed in the window
					label.setIcon(ResizeImage(path));
				}
				
				else if(result == JFileChooser.CANCEL_OPTION){
					System.out.println("No File Selected");
				}
			}
		});
		
		setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setSize(700,500);
		setVisible(true);
	}
	
	//resize image icon with the same size as jlabel
	public ImageIcon ResizeImage(String ImagePath){
		ImageIcon MyImage = new ImageIcon(ImagePath);
		Image img = MyImage.getImage();
		Image newImg = img.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
		ImageIcon image = new ImageIcon(newImg);
		return image;
	}
	
	public static void main(String[] args){
		new FaceUI();
	}
	
}