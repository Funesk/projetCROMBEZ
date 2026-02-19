package projetCROMBEZ;

import java.awt.Color;
import java.awt.GridBagLayout;
import javax.swing.JFrame;

/**
 * Point d'entree de l'application.
 *
 * Cree la fenetre principale, y ajoute le GamePanel et demarre la boucle de jeu.
 *
 *
 * --- Transmission des references ---
 * La JFrame et le GamePanel sont passes a GameSettings pour permettre
 * le toggle plein ecran et le retablissement du focus clavier.
 */
public class Main {

    public static void main(String[] args) {

        // --- Creation de la fenetre ---
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Survivor V1");

        // Fond noir visible dans les bandes du letterboxing en plein ecran
        window.getContentPane().setBackground(Color.black);

        // GridBagLayout : centre le GamePanel dans la fenetre meme quand
        // celle-ci est plus grande que le panneau (mode plein ecran)
        window.getContentPane().setLayout(new GridBagLayout());

        // --- Creation du GamePanel ---
        GamePanel gamePanel = new GamePanel();

        // GridBagConstraints par defaut = centrage automatique
        window.add(gamePanel);
        window.pack();

        // --- Transmission des references a GameSettings ---
        // Indispensable pour le toggle plein ecran et la restauration du focus.
        GameSettings.getInstance().setWindow(window);
        GameSettings.getInstance().setGamePanel(gamePanel);

        // --- Positionnement et affichage ---
        window.setLocationRelativeTo(null); // centre sur l'ecran
        window.setVisible(true);

        // --- Demarrage de la boucle de jeu ---
        gamePanel.startGameThread();
    }
}
