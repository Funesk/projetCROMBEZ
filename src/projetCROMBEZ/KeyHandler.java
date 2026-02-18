package projetCROMBEZ;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Gestion des entrées clavier.
 *
 * écoute les événements de pression / relachement de touches et
 * met a jour des booléens consultables par les autres classes.
 *
 * Touches supportÃ©es :
 *  - Z / Flèche haut     déplacement vers le haut
 *  - S / Flèche bas      déplacement vers le bas
 *  - Q / Flèche gauche   déplacement vers la gauche
 *  - D / Flèche droite   déplacement vers la droite
 *  - ECHAP               ouvre/ferme le menu pause
 *  - ENTREE              confirmation (retour au menu après game over/victoire)
 */
public class KeyHandler implements KeyListener {

    // -------------------------------------------------------------------------
    // état des touches (true = enfoncée, false = relachée)
    // -------------------------------------------------------------------------

    /** Touche de déplacement haut (Z ou flèche haut). */
    public boolean upPressed;

    /** Touche de déplacement bas (S ou flèche bas). */
    public boolean downPressed;

    /** Touche de déplacement gauche (Q ou flèche gauche). */
    public boolean leftPressed;

    /** Touche de déplacement droite (D ou flèche droite). */
    public boolean rightPressed;

    /** Touche ENTREE confirmation/retour au menu. */
    public boolean enterPressed;

    /**
     * Touche ECHAP  mise en pause / reprise.
     * Gérée en "one-shot" : on écoute le keyPressed pour éviter les répétitions.
     */
    public boolean escapeJustPressed;

    // -------------------------------------------------------------------------
    // Implémentation KeyListener
    // -------------------------------------------------------------------------

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    /** Appelé dès qu'une touche est enfoncée. */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Déplacementsé ZQSD et flèches directionnelles
        if (code == KeyEvent.VK_Z     || code == KeyEvent.VK_UP)    upPressed    = true;
        if (code == KeyEvent.VK_S     || code == KeyEvent.VK_DOWN)  downPressed  = true;
        if (code == KeyEvent.VK_Q     || code == KeyEvent.VK_LEFT)  leftPressed  = true;
        if (code == KeyEvent.VK_D     || code == KeyEvent.VK_RIGHT) rightPressed = true;

        // Confirmation
        if (code == KeyEvent.VK_ENTER)  enterPressed = true;

        // Pause (one-shot : sera remis à  false après lecture dans GamePanel)
        if (code == KeyEvent.VK_ESCAPE) escapeJustPressed = true;
    }

    /** Appelé dès qu'une touche est relachée. */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_Z     || code == KeyEvent.VK_UP)    upPressed    = false;
        if (code == KeyEvent.VK_S     || code == KeyEvent.VK_DOWN)  downPressed  = false;
        if (code == KeyEvent.VK_Q     || code == KeyEvent.VK_LEFT)  leftPressed  = false;
        if (code == KeyEvent.VK_D     || code == KeyEvent.VK_RIGHT) rightPressed = false;

        if (code == KeyEvent.VK_ENTER)  enterPressed = false;
        // escapeJustPressed est remis à  false manuellement par GamePanel après traitement
    }
}
