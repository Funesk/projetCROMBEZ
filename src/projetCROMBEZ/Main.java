package projetCROMBEZ;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) 
	{
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Pour bien fermer la fenetre
		window.setResizable(false); 							  // Taille de la fenetre changeable ?
		window.setTitle("Survivor V1");						 // Titre de la fenetre
								   
		
		
		GamePanel gamePanel = new GamePanel();
		
		window.add(gamePanel);
		window.pack();
		window.setLocation(400, 100);				// placer la fenetre au centre de l'ecran
		window.setVisible(true);      				// visualiser la fenetre
		
		gamePanel.startGameThread();
		
		
	}

}
