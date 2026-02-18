package projetCROMBEZ;

import javax.swing.JFrame;

/**
 * Point d'entrée de l'application.
 *
 * Crée la fenetre principale (JFrame), y ajoute le GamePanel,
 * puis démarre la boucle de jeu.
 *
 * La référence a la JFrame est transmise a {@link GameSettings} pour
 * permettre le basculement plein écran depuis les options.
 */
public class Main {

    public static void main(String[] args) {

        // --- Création de la fenetre ---
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // fermeture propre
        window.setResizable(false);                            // taille fixe
        window.setTitle("Survivor V1");

        // --- Création et ajout du panneau de jeu ---
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // adapte la taille de la fenétre au panneau

        // --- Transmission de la JFrame a  GameSettings ---
        // Nécessaire pour le toggle plein écran dans les options.
        GameSettings.getInstance().setWindow(window);

        // --- Positionnement et affichage ---
        window.setLocationRelativeTo(null); // centré sur l'ecran
        window.setVisible(true);

        // --- Démarrage de la boucle de jeu ---
        gamePanel.startGameThread();
    }
}
