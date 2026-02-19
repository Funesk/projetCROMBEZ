package projetCROMBEZ;

import java.awt.*;

/**
 * Ecran des options du jeu.
 *
 * Accessible depuis le menu principal ET depuis le menu pause.
 * Retourne a l'etat precedent (returnState) quand on clique sur "Retour".
 *
 * Options disponibles :
 *  1. Afficher la portee du joueur (toggle ON/OFF)
 *  2. Plein ecran (toggle ON/OFF)
 *
 * --- Architecture listener ---
 * Aucun MouseListener n'est enregistre ici. GamePanel appelle
 * handleClick() et handleHover() UNIQUEMENT quand gameState == OPTIONS.
 * Cela evite le bug de double-dispatch : quand on retourne de OPTIONS
 * vers PAUSED, PauseScreen ne recoit pas ce meme clic.
 */
public class OptionsScreen {

    // =========================================================================
    // References
    // =========================================================================

    /** Reference au GamePanel. */
    private GamePanel gp;

    /**
     * Etat vers lequel revenir quand l'utilisateur clique sur "Retour".
     * MENU si on vient du menu principal.
     * PAUSED si on vient du menu pause.
     */
    private GameState returnState;

    // =========================================================================
    // Boutons
    // =========================================================================

    /** Bouton toggle pour l'affichage de la portee. */
    private Rectangle btnRange;

    /** Bouton toggle pour le plein ecran. */
    private Rectangle btnFullscreen;

    /** Bouton pour revenir a l'etat precedent. */
    private Rectangle btnBack;

    /** Index du bouton survole (-1 = aucun). */
    private int hoveredButton = -1;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree l'ecran des options et calcule les positions des boutons.
     * Aucun MouseListener n'est enregistre ici.
     *
     * @param gp          Reference au GamePanel
     * @param returnState Etat vers lequel revenir (MENU ou PAUSED)
     */
    public OptionsScreen(GamePanel gp, GameState returnState) {
        this.gp          = gp;
        this.returnState = returnState;

        int cx     = gp.screenWidth / 2;
        int startY = 300;

        btnRange      = new Rectangle(cx - 200, startY,       400, 60);
        btnFullscreen = new Rectangle(cx - 200, startY + 90,  400, 60);
        btnBack       = new Rectangle(cx - 80,  startY + 200, 160, 40);
    }

    // =========================================================================
    // Setter
    // =========================================================================

    /**
     * Met a jour l'etat de retour avant d'afficher l'ecran.
     * Appele par MenuScreen ou PauseScreen selon le contexte.
     *
     * @param state MENU si venant du menu, PAUSED si venant de la pause
     */
    public void setReturnState(GameState state) {
        this.returnState = state;
    }

    // =========================================================================
    // Gestion des evenements (appeles par GamePanel)
    // =========================================================================

    /**
     * Traite un clic souris sur cet ecran.
     * Appele par GamePanel UNIQUEMENT quand gameState == OPTIONS.
     * Garantit qu'aucun autre ecran ne recoit ce meme clic.
     *
     * @param p Position du clic en coordonnees ecran
     */
    public void handleClick(Point p) {
        GameSettings s = GameSettings.getInstance();

        if (btnRange.contains(p)) {
            // Inverse l'affichage de la portee du joueur
            s.setShowPlayerRange(!s.isShowPlayerRange());
            return;
        }
        if (btnFullscreen.contains(p)) {
            // Bascule plein ecran / fenetre
            // Note : toggleFullscreen() appelle requestFocusInWindow() via invokeLater
            s.toggleFullscreen();
            return;
        }
        if (btnBack.contains(p)) {
            // Retourne a l'etat precedent (MENU ou PAUSED)
            // Aucun autre ecran ne recevra ce clic car GamePanel dispatch
            // selon l'etat courant au moment ou le clic entre dans handleClick.
            gp.gameState = returnState;
        }
    }

    /**
     * Met a jour le bouton survole.
     * Appele par GamePanel UNIQUEMENT quand gameState == OPTIONS.
     *
     * @param p Position de la souris en coordonnees ecran
     */
    public void handleHover(Point p) {
        if      (btnRange.contains(p))      hoveredButton = 0;
        else if (btnFullscreen.contains(p)) hoveredButton = 1;
        else if (btnBack.contains(p))       hoveredButton = 2;
        else                                hoveredButton = -1;
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine l'ecran des options.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Fond degrade
        GradientPaint bg = new GradientPaint(0, 0, new Color(10, 10, 30),
                0, gp.screenHeight, new Color(20, 10, 50));
        g2.setPaint(bg);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // Titre
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 38f)
                                             : new Font("Arial", Font.BOLD, 38);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String title = "Options";
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, 220);

        GameSettings s = GameSettings.getInstance();

        // Toggle : portee du joueur
        drawToggleButton(g2, btnRange,
                "Afficher la portee d'attaque", s.isShowPlayerRange(), 0);

        // Toggle : plein ecran
        drawToggleButton(g2, btnFullscreen,
                "Mode plein ecran", s.isFullscreen(), 1);

        // Bouton Retour
        boolean backHover = (hoveredButton == 2);
        g2.setColor(backHover ? new Color(80, 80, 100) : new Color(40, 40, 60));
        g2.fillRoundRect(btnBack.x, btnBack.y, btnBack.width, btnBack.height, 10, 10);
        g2.setColor(new Color(150, 150, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(btnBack.x, btnBack.y, btnBack.width, btnBack.height, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        Font backFont = gp.gameFont != null ? gp.gameFont.deriveFont(16f)
                                            : new Font("Arial", Font.PLAIN, 16);
        g2.setFont(backFont);
        g2.setColor(Color.white);
        fm = g2.getFontMetrics();
        String back = "<- Retour";
        g2.drawString(back, btnBack.x + btnBack.width / 2 - fm.stringWidth(back) / 2,
                      btnBack.y + btnBack.height / 2 + fm.getAscent() / 2 - 2);
    }

    /**
     * Dessine un bouton toggle avec son libelle et son etat ON/OFF.
     *
     * @param g2     Contexte graphique
     * @param btn    Rectangle du bouton
     * @param label  Texte descriptif de l'option
     * @param active true si l'option est activee (ON)
     * @param index  Index du bouton pour la detection de hover
     */
    private void drawToggleButton(Graphics2D g2, Rectangle btn, String label,
                                  boolean active, int index) {
        boolean hovered = (hoveredButton == index);

        // Fond
        g2.setColor(hovered ? new Color(50, 50, 80) : new Color(30, 30, 55));
        g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 14, 14);

        // Bordure
        g2.setColor(hovered ? new Color(120, 120, 200) : new Color(70, 70, 110));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        // Libelle
        Font optFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                           : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(optFont);
        g2.setColor(Color.white);
        g2.drawString(label, btn.x + 20, btn.y + btn.height / 2 + 6);

        // Indicateur ON/OFF a droite
        int toggleW = 60;
        int toggleH = 30;
        int toggleX = btn.x + btn.width - toggleW - 15;
        int toggleY = btn.y + (btn.height - toggleH) / 2;

        // Fond du toggle (vert si ON, rouge si OFF)
        g2.setColor(active ? new Color(50, 180, 80) : new Color(120, 50, 50));
        g2.fillRoundRect(toggleX, toggleY, toggleW, toggleH, toggleH, toggleH);

        // Pastille blanche (a droite si ON, a gauche si OFF)
        int knobSize = toggleH - 6;
        int knobX    = active ? toggleX + toggleW - knobSize - 3 : toggleX + 3;
        g2.setColor(Color.white);
        g2.fillOval(knobX, toggleY + 3, knobSize, knobSize);

        // Texte ON / OFF
        Font toggleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 11f)
                                              : new Font("Arial", Font.BOLD, 11);
        g2.setFont(toggleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String state = active ? "ON" : "OFF";
        g2.drawString(state, toggleX + toggleW / 2 - fm.stringWidth(state) / 2,
                      toggleY + toggleH / 2 + fm.getAscent() / 2 - 2);
    }
}
