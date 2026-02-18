package projetCROMBEZ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ecran des options du jeu.
 *
 * Accessible depuis le menu principal ET depuis le menu pause.
 * Retourne à  l'état précédent ({@link #returnState}) quand on clique sur "Retour".
 *
 * Options disponibles :
 *  1. Afficher la portée du joueur (toggle ON/OFF)
 *  2. Plein écran (toggle ON/OFF)
 */
public class OptionsScreen {

    // -------------------------------------------------------------------------
    // Références
    // -------------------------------------------------------------------------

    /** Référence au GamePanel. */
    private GamePanel gp;

    /**
     * etat vers lequel revenir quand l'utilisateur clique sur "Retour".
     * MENU si on vient du menu principal, PAUSED si on vient du menu pause.
     */
    private GameState returnState;

    // -------------------------------------------------------------------------
    // Zones cliquables
    // -------------------------------------------------------------------------

    /** Bouton toggle pour l'affichage de la portée. */
    private Rectangle btnRange;

    /** Bouton toggle pour le plein écran. */
    private Rectangle btnFullscreen;

    /** Bouton pour revenir à  l'état précédent. */
    private Rectangle btnBack;

    /** Index du bouton survolé (-1 = aucun). */
    private int hoveredButton = -1;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée l'écran d'options et enregistre les listeners.
     *
     * @param gp          Référence au GamePanel
     * @param returnState etat vers lequel revenir (MENU ou PAUSED)
     */
    public OptionsScreen(GamePanel gp, GameState returnState) {
        this.gp          = gp;
        this.returnState = returnState;

        int cx     = gp.screenWidth / 2;
        int startY = 300;

        // Boutons toggle (grands, pour être facilement cliquables)
        btnRange      = new Rectangle(cx - 200, startY,       400, 60);
        btnFullscreen = new Rectangle(cx - 200, startY + 90,  400, 60);
        btnBack       = new Rectangle(cx - 80,  startY + 200, 160, 40);

        // Listener souris : survol
        gp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e.getPoint());
            }
        });

        // Listener souris : clic
        gp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gp.gameState != GameState.OPTIONS) return;

                Point p = e.getPoint();
                GameSettings s = GameSettings.getInstance();

                if (btnRange.contains(p)) {
                    // Inverse l'affichage de la portée
                    s.setShowPlayerRange(!s.isShowPlayerRange());
                }
                if (btnFullscreen.contains(p)) {
                    // Bascule plein écran / fenêtré
                    s.toggleFullscreen();
                }
                if (btnBack.contains(p)) {
                    // Retourne à  l'état précédent
                    gp.gameState = returnState;
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Mise à  jour
    // -------------------------------------------------------------------------

    /**
     * Met à  jour la référence de retour (utile si l'écran est réutilisé
     * depuis différents contextes).
     *
     * @param state Nouvel état de retour
     */
    public void setReturnState(GameState state) {
        this.returnState = state;
    }

    /**
     * Met à  jour le bouton survolé en fonction de la position de la souris.
     */
    private void updateHover(Point p) {
        if (gp.gameState != GameState.OPTIONS) return;
        if      (btnRange.contains(p))      hoveredButton = 0;
        else if (btnFullscreen.contains(p)) hoveredButton = 1;
        else if (btnBack.contains(p))       hoveredButton = 2;
        else                                hoveredButton = -1;
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /**
     * Dessine l'écran des options.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // --- Fond ---
        GradientPaint bg = new GradientPaint(0, 0, new Color(10, 10, 30),
                0, gp.screenHeight, new Color(20, 10, 50));
        g2.setPaint(bg);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // --- Titre ---
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 38f)
                                             : new Font("Arial", Font.BOLD, 38);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        String title = "Options";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, 220);

        GameSettings s = GameSettings.getInstance();

        // --- Toggle : portée du joueur ---
        drawToggleButton(g2, btnRange,
                "Afficher la portée d'attaque",
                s.isShowPlayerRange(), 0);

        // --- Toggle : plein écran ---
        drawToggleButton(g2, btnFullscreen,
                "Mode plein écran",
                s.isFullscreen(), 1);

        // --- Bouton Retour ---
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
        String back = "Retour";
        g2.drawString(back, btnBack.x + btnBack.width / 2 - fm.stringWidth(back) / 2,
                      btnBack.y + btnBack.height / 2 + fm.getAscent() / 2 - 2);
    }

    /**
     * Dessine un bouton toggle avec son libellé et son état ON/OFF.
     *
     * @param g2      Contexte graphique
     * @param btn     Rectangle du bouton
     * @param label   Texte descriptif de l'option
     * @param active  true si l'option est activée (ON)
     * @param index   Index du bouton pour la détection de hover
     */
    private void drawToggleButton(Graphics2D g2, Rectangle btn, String label,
                                  boolean active, int index) {
        boolean hovered = (hoveredButton == index);

        // Fond du bouton
        g2.setColor(hovered ? new Color(50, 50, 80) : new Color(30, 30, 55));
        g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 14, 14);

        // Bordure
        g2.setColor(hovered ? new Color(120, 120, 200) : new Color(70, 70, 110));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        // Libellé à  gauche
        Font optFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                           : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(optFont);
        g2.setColor(Color.white);
        g2.drawString(label, btn.x + 20, btn.y + btn.height / 2 + 6);

        // Indicateur ON/OFF   droite
        int toggleW  = 60;
        int toggleH  = 30;
        int toggleX  = btn.x + btn.width - toggleW - 15;
        int toggleY  = btn.y + (btn.height - toggleH) / 2;

        // Fond du toggle
        g2.setColor(active ? new Color(50, 180, 80) : new Color(120, 50, 50));
        g2.fillRoundRect(toggleX, toggleY, toggleW, toggleH, toggleH, toggleH);

        // Pastille blanche
        int knobSize = toggleH - 6;
        int knobX    = active ? toggleX + toggleW - knobSize - 3 : toggleX + 3;
        g2.setColor(Color.white);
        g2.fillOval(knobX, toggleY + 3, knobSize, knobSize);

        // Texte ON/OFF
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
