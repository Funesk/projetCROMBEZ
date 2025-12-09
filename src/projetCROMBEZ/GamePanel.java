package projetCROMBEZ;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.File;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable

{
	// Parametres de la fenetre
	int originalTileSize = 16; 					// taille originale d'une tuile (avant rescaling)
	int scale = 2;			   					// valeur du scaling (pour adapter à la taille de l'écran)
	int tileSize = originalTileSize*scale;      //la taille réelle des tuiles
	int screenCol = 38;							// nombre de tuiles en largeur
	int screenRow = 26;							// nombre de tuiles en hauteur
	int screenWidth = tileSize*screenCol;		//1216 pixels
	int screenHeight = tileSize*screenRow;		//832 pixels
	int FPS = 60;
	int currentFPS = 0;
	
	Font gameFont;
	
	Thread gameThread;							// instantiation du thread
	KeyHandler keyH = new KeyHandler();			// intantiation du keyhandler
	
	//stat de base du personnage
	int playerX = 200;
	int playerY = 200;
	int playerSpeed = 6;
	
	
	public GamePanel()
	{
		this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // pour donner la bonne taille a notre pannel
		this.setBackground(Color.black);									// couleur
		this.setDoubleBuffered(true);									// pas primordial mais peut faire gagner des performances
		this.addKeyListener(keyH);										// permet de capter les touches utilisées
		this.setFocusable(true);										// permet de "focus" sur l'écran de jeu
		this.requestFocusInWindow();									// regle un bug de focus qui faisait que je ne pouvais pas me deplacer
		
		try {
		    Font customFont = Font.createFont(
		            Font.TRUETYPE_FONT, 
		            new File("font/Cubic.ttf")).deriveFont(15f); // Taille 24

		    // Enregistrer la police pour pouvoir l'utiliser partout
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(customFont);

		    // Stocker la police pour l'utiliser plus tard
		    this.gameFont = customFont;

		} catch (Exception e) {
		    e.printStackTrace();
		    System.out.println("Erreur lors du chargement de la police !");
		}
	}		
	
	
	// permet de démarrer le thread
	public void startGameThread()
	{
		gameThread = new Thread(this);
		gameThread.start();
	}

	@Override
	public void run() 							// boucle qui est effectué dès le démarrage du thread 
	{
		// methode pour limiter le nombre de fps
		double drawInterval = 1000000000/FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		long timer = 0;
		int drawCount = 0;
		
		while (gameThread != null)
		{
			currentTime = System.nanoTime();
			
			delta += (currentTime - lastTime) / drawInterval;
			
			timer += currentTime - lastTime;
			
			lastTime = currentTime;
			
			
			if (delta >= 1) 
			{
				update();
				repaint();
				delta--;
				drawCount++;
			}

			if (timer>=1000000000)
			{
				currentFPS = drawCount;
				drawCount = 0;
				timer = 0;
			}
			
		}
		
	}

	
	// pour update ce qui s'affiche à l'écran
	public void update() 
	{
		if (keyH.upPressed == true) {
			playerY -= playerSpeed;
		}
		
		if (keyH.downPressed == true) {
			playerY += playerSpeed;
		}
		
		if (keyH.leftPressed == true) {
			playerX -= playerSpeed;
		}
		
		if (keyH.rightPressed == true) {
			playerX += playerSpeed;
		}
	}
	
	
	// permet de dessiner à l'écran
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		// affichage du carré blanc (premiere etape pour afficher le personnage)
		g2.setColor(Color.white);
		g2.fillRect(playerX, playerY, tileSize, tileSize);
		
		//affichage du nombre de fps en haut a doite
		g2.setColor(Color.yellow);
		g2.setFont(gameFont);
		g2.drawString("FPS : "+ currentFPS,screenWidth-80,20);
		
		g2.dispose();
	}
			
}




