package projetCROMBEZ;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Point d'entree de l'application.
 *
 * =========================================================================
 * ORDRE D'INITIALISATION (important)
 * =========================================================================
 *  1. Creer JFrame + GamePanel
 *  2. Transmettre les references a GameSettings (window + gamePanel)
 *     -> permet a GameSettings.saveIfReady() de fonctionner
 *  3. Appeler SaveManager.load() pour restaurer la sauvegarde
 *     -> charge options, stats joueur, records, hasActiveGame
 *  4. Afficher la fenetre et demarrer la boucle de jeu
 *
 * =========================================================================
 * GARANTIE DE SAUVEGARDE A LA FERMETURE
 * =========================================================================
 * Deux mecanismes independants assurent que la sauvegarde est ecrite
 * meme si l'utilisateur ferme la fenetre par la croix ou Alt+F4 :
 *
 *  1. WindowListener (windowClosing) : appele avant que la fenetre
 *     se ferme, garantit la sauvegarde dans les cas normaux.
 *     Remplace l'action par defaut EXIT_ON_CLOSE pour controler
 *     l'ordre : sauvegarder PUIS quitter.
 *
 *  2. ShutdownHook dans GamePanel : filet de securite supplementaire
 *     pour les fermetures brutales (kill process, fin de session OS).
 *     Moins fiable seul car peut etre interrompu, mais complementaire.
 */
public class Main {

    public static void main(String[] args) {

        // Fenetre principale
        JFrame window = new JFrame();
        window.setTitle("Survivor V1");
        window.setResizable(false);
        window.getContentPane().setBackground(Color.black);
        window.getContentPane().setLayout(new GridBagLayout()); // centre le GamePanel (letterbox)

        // NE PAS utiliser EXIT_ON_CLOSE : on veut controler l'ordre de fermeture
        // (sauvegarder AVANT de quitter)
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Panneau de jeu
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();

        // Transmission des references a GameSettings
        // DOIT etre fait avant SaveManager.load() :
        //   - setGamePanel() permet a saveIfReady() (appele par les setters) de fonctionner
        //   - setWindow() est necessaire pour toggleFullscreen()
        GameSettings.getInstance().setWindow(window);
        GameSettings.getInstance().setGamePanel(gamePanel);

        // Chargement de la sauvegarde
        // DOIT etre appele apres setGamePanel() et apres la creation de gamePanel.player.
        // Restaure : difficulte, showRange, stats joueur, gold, records, hasActiveGame.
        SaveManager.load(gamePanel);

        // WindowListener : garantit la sauvegarde quand l'utilisateur
        // ferme la fenetre (croix rouge, Alt+F4, raccourci clavier OS, etc.)
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("[Main] Fermeture de la fenetre - sauvegarde en cours...");
                SaveManager.save(gamePanel);
                System.out.println("[Main] Sauvegarde terminee. Fermeture.");
                System.exit(0); // declenche aussi le ShutdownHook (second filet)
            }
        });

        // Restauration du plein ecran si c'etait actif lors de la derniere session.
        // DOIT etre fait apres setVisible() : la fenetre doit etre affichee
        // pour que toggleFullscreen() puisse appeler dispose()/setVisible()
        // et que requestFocusInWindow() fonctionne ensuite.
        // GameSettings.isFullscreen() contient la valeur lue par SaveManager.load().
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        if (GameSettings.getInstance().isFullscreen()) {
            // On remet isFullscreen a false car toggleFullscreen() va l'inverser
            GameSettings.getInstance().setFullscreenFlag(false);
            GameSettings.getInstance().toggleFullscreen();
        }

        gamePanel.startGameThread();
    }
}
