package projetCROMBEZ;

import java.awt.*;

/**
 * Panneau d'informations detaillees du jeu.
 *
 * Affiche sur deux colonnes :
 *  - Colonne gauche : statistiques du joueur + controles
 *  - Colonne droite : statistiques de chaque ennemi + composition des vagues
 *
 * Utilise par MenuScreen (bouton "Infos") et PauseScreen (bouton "Infos").
 * Centralise le rendu pour eviter la duplication de code.
 *
 * Les valeurs affichees sont les stats DE BASE (avant modificateurs de difficulte).
 * Un message rappelle que la difficulte modifie HP et degats ennemis.
 *
 * Usage :
 *   InfoPanel panel = new InfoPanel(gp);
 *   // Dans draw() de l'ecran parent, si showInfo est true :
 *   panel.draw(g2);
 *   // Dans handleClick() : si showInfo && n'importe quel clic -> showInfo = false
 */
public class InfoPanel {

    // =========================================================================
    // Reference
    // =========================================================================

    /** Reference au GamePanel pour les dimensions et la police. */
    private GamePanel gp;

    // =========================================================================
    // Couleurs des ennemis (coherentes avec les classes ennemies)
    // =========================================================================

    /** Couleur du MeleeEnemy (triangle rouge). */
    private static final Color COLOR_MELEE  = new Color(220, 80,  80);

    /** Couleur du RangedEnemy (losange cyan). */
    private static final Color COLOR_RANGED = new Color(0,   180, 220);

    /** Couleur du TankEnemy (hexagone vert). */
    private static final Color COLOR_TANK   = new Color(80,  200, 100);

    /** Couleur du BossEnemy (etoile orange). */
    private static final Color COLOR_BOSS   = new Color(255, 140, 0);

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree le panneau d'infos.
     *
     * @param gp Reference au GamePanel
     */
    public InfoPanel(GamePanel gp) {
        this.gp = gp;
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine le panneau d'informations complet par-dessus l'ecran courant.
     * Le fond semi-transparent laisse deviner l'ecran derriere.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Overlay sombre
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Panneau principal centre
        int panelW = 1070;
        int panelH = 580;
        int panelX = gp.screenWidth  / 2 - panelW / 2;
        int panelY = gp.screenHeight / 2 - panelH / 2;

        // Fond du panneau
        g2.setColor(new Color(12, 12, 32, 250));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        // Bordure
        g2.setColor(new Color(90, 90, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        // Titre
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 26f)
                                             : new Font("Arial", Font.BOLD, 26);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String title = "Informations & Statistiques";
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, panelY + 38);

        // Ligne separatrice sous le titre
        g2.setColor(new Color(70, 70, 120));
        g2.drawLine(panelX + 20, panelY + 42, panelX + panelW - 20, panelY + 42);

        // Deux colonnes
        int colLeftX  = panelX + 30;
        int colRightX = panelX + panelW / 2 + 10;
        int contentY  = panelY + 62;

        drawLeftColumn(g2,  colLeftX,  contentY);
        drawRightColumn(g2, colRightX, contentY);

        // Instruction de fermeture en bas
        Font hintFont = gp.gameFont != null ? gp.gameFont.deriveFont(13f)
                                            : new Font("Arial", Font.PLAIN, 13);
        g2.setFont(hintFont);
        g2.setColor(new Color(140, 140, 160));
        fm = g2.getFontMetrics();
        String hint = "Cliquer n'importe ou pour fermer";
        g2.drawString(hint, gp.screenWidth / 2 - fm.stringWidth(hint) / 2,
                      panelY + panelH - 12);
    }

    // =========================================================================
    // Colonne gauche : joueur + controles
    // =========================================================================

    /**
     * Dessine la colonne gauche : statistiques du joueur et controles clavier.
     *
     * @param g2 Contexte graphique
     * @param x  Coordonnee X de depart
     * @param y  Coordonnee Y de depart
     */
    private void drawLeftColumn(Graphics2D g2, int x, int y) {
        int lineH = 22; // hauteur d'une ligne

        // --- Section joueur ---
        drawSectionTitle(g2, "Joueur", new Color(80, 200, 120), x, y);
        y += 28;

        // Petite fleche decorative (forme du joueur)
        drawPlayerShape(g2, x + 8, y + 8 * lineH - 4);

        // Stats du joueur 
        Player p = gp.player;
        String[][] playerStats = {
            { "Points de vie",    p.maxHp + " / " + p.hp + " actuels" },
            { "Degats / tir",     String.valueOf(p.damage)             },
            { "Portee d'attaque", p.attackRange + " px"                },
            { "Cadence de tir",   (60 / p.attackRate) + " tir/sec"     },
        };

        for (String[] row : playerStats) {
            drawStatRow(g2, x + 20, y, row[0], row[1], new Color(180, 240, 180));
            y += lineH;
        }

        y += 14; // espace

        // --- Section controles ---
        drawSectionTitle(g2, "Controles", new Color(180, 180, 255), x, y);
        y += 28;

        String[][] controls = {
            { "Z / Fleche haut",    "Monter"     },
            { "S / Fleche bas",     "Descendre"  },
            { "Q / Fleche gauche",  "Gauche"     },
            { "D / Fleche droite",  "Droite"     },
            { "ECHAP",              "Pause"      },
            { "ENTREE",             "Confirmer"  },
        };

        for (String[] row : controls) {
            drawStatRow(g2, x + 20, y, row[0], row[1], new Color(200, 200, 255));
            y += lineH;
        }
    }

    // =========================================================================
    // Colonne droite : ennemis + vagues
    // =========================================================================

    /**
     * Dessine la colonne droite : statistiques de chaque ennemi et composition des vagues.
     *
     * @param g2 Contexte graphique
     * @param x  Coordonnee X de depart
     * @param y  Coordonnee Y de depart
     */
    private void drawRightColumn(Graphics2D g2, int x, int y) {
        int lineH = 22;

        // --- Section ennemis ---
        drawSectionTitle(g2, "Ennemis (stats de base)", new Color(220, 120, 80), x, y);
        y += 28;

        // En-tete du tableau
        drawTableHeader(g2, x, y);
        y += lineH + 2;

        // Ligne de separation
        g2.setColor(new Color(60, 60, 90));
        g2.drawLine(x, y - 15, x + 390, y - 15);

        // Donnees de chaque ennemi
        // Colonnes : Nom | Forme | HP | Degats | Vitesse | Atk/s
        drawEnemyRow(g2, x, y,
                "Melee",   "Triangle",  "40",   "10",  "2.0", "1/s",    COLOR_MELEE);
        y += lineH;

        drawEnemyRow(g2, x, y,
                "Distance", "Losange",  "25",    "8",  "1.5", "0.5/s",  COLOR_RANGED);
        y += lineH;

        drawEnemyRow(g2, x, y,
                "Tank",    "Hexagone", "200",   "20",  "0.8", "0.7/s",  COLOR_TANK);
        y += lineH;

        drawEnemyRow(g2, x, y,
                "Boss",    "Etoile",  "1000",   "25",  "1.2", "1/s*",   COLOR_BOSS);
        y += lineH + 2;

        // Note boss
        Font noteFont = gp.gameFont != null ? gp.gameFont.deriveFont(11f)
                                            : new Font("Arial", Font.PLAIN, 11);
        g2.setFont(noteFont);
        g2.setColor(new Color(160, 160, 160));
        g2.drawString("* Le boss tire aussi des projectiles radiaux (x4 ou x8 en phase 2)", x, y);
        y += 18;

        // Note difficulte
        g2.setColor(new Color(255, 200, 80));
        g2.drawString("La difficulte modifie les HP et degats des ennemis", x, y);
        y += 35;
        
        
        // Ligne de separation
        g2.setColor(new Color(60, 60, 90));
        g2.drawLine(x, y - 25, x + 390, y - 25);
        
        // --- Section vagues ---
        drawSectionTitle(g2, "Composition des vagues (Normal)", new Color(200, 180, 80), x, y);
        y += 28;

        // Donnees des vagues
        // computeMaxEnemies(n) = 8 + n*3, x1.0 en Normal
        String[][] waves = {
            { "Vague 1", "11",  "100% Melee"                         },
            { "Vague 2", "14",  "70% Melee  +  30% Distance"         },
            { "Vague 3", "17",  "70% Melee  +  30% Distance"         },
            { "Vague 4", "20",  "45% Melee  +  30% Distance  +  25% Tank" },
            { "Vague 5", "23",  "45% Melee + 30% Distance + 25% Tank + BOSS" },
        };

        for (String[] w : waves) {
            drawWaveRow(g2, x, y, w[0], w[1], w[2]);
            y += lineH;
        }

        y += 6;
        g2.setFont(noteFont);
        g2.setColor(new Color(160, 160, 160));
        g2.drawString("En Facile : x0.6 ennemis  |  En Difficile : x1.5 ennemis", x, y);
    }

    // =========================================================================
    // Helpers de rendu
    // =========================================================================

    /**
     * Dessine un titre de section colore avec une petite barre decorative.
     */
    private void drawSectionTitle(Graphics2D g2, String text, Color color, int x, int y) {
        // Barre coloree a gauche
        g2.setColor(color);
        g2.fillRect(x, y - 12, 3, 16);

        Font sectionFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 16f)
                                               : new Font("Arial", Font.BOLD, 16);
        g2.setFont(sectionFont);
        g2.setColor(color);
        g2.drawString(text, x + 8, y);
    }

    /**
     * Dessine une ligne cle -> valeur alignee.
     *
     * @param keyColor Couleur de la valeur (la cle est toujours grise)
     */
    private void drawStatRow(Graphics2D g2, int x, int y, String key, String value, Color keyColor) {
        Font statFont = gp.gameFont != null ? gp.gameFont.deriveFont(14f)
                                            : new Font("Arial", Font.PLAIN, 14);
        g2.setFont(statFont);

        // Cle (grise)
        g2.setColor(new Color(160, 160, 180));
        g2.drawString(key, x, y);

        // Valeur (coloree), alignee a droite d'une colonne fixe
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(keyColor);
        g2.drawString(value, x + 190, y);
    }

    /**
     * Dessine l'en-tete du tableau des ennemis.
     */
    private void drawTableHeader(Graphics2D g2, int x, int y) {
        Font headerFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 13f)
                                              : new Font("Arial", Font.BOLD, 13);
        g2.setFont(headerFont);
        g2.setColor(new Color(180, 180, 200));

        g2.drawString("Nom",     x,       y);
        g2.drawString("Forme",   x + 80,  y);
        g2.drawString("HP",      x + 165, y);
        g2.drawString("Degats",  x + 210, y);
        g2.drawString("Vitesse", x + 275, y);
        g2.drawString("Atk/s",   x + 345, y);
    }

    /**
     * Dessine une ligne du tableau ennemi avec le nom colore.
     */
    private void drawEnemyRow(Graphics2D g2, int x, int y,
                               String name, String shape, String hp,
                               String dmg, String speed, String atkRate,
                               Color nameColor) {
        Font rowFont = gp.gameFont != null ? gp.gameFont.deriveFont(14f)
                                           : new Font("Arial", Font.PLAIN, 14);
        g2.setFont(rowFont);

        // Puce coloree + nom
        g2.setColor(nameColor);
        g2.fillOval(x, y - 9, 8, 8);
        g2.drawString(name,   x + 12,  y);

        // Reste de la ligne en blanc
        g2.setColor(new Color(210, 210, 210));
        g2.drawString(shape,  x + 80,  y);
        g2.drawString(hp,     x + 165, y);
        g2.drawString(dmg,    x + 210, y);
        g2.drawString(speed,  x + 275, y);
        g2.drawString(atkRate,x + 345, y);
    }

    /**
     * Dessine une ligne de composition de vague.
     */
    private void drawWaveRow(Graphics2D g2, int x, int y,
                              String waveName, String count, String composition) {
        Font rowFont = gp.gameFont != null ? gp.gameFont.deriveFont(14f)
                                           : new Font("Arial", Font.PLAIN, 14);
        g2.setFont(rowFont);

        // Nom de la vague (jaune)
        g2.setColor(new Color(255, 210, 80));
        g2.drawString(waveName, x, y);

        // Nombre d'ennemis (blanc)
        g2.setColor(Color.white);
        g2.drawString(count + " ennemis", x + 70, y);

        // Composition (grise)
        g2.setColor(new Color(180, 180, 200));
        g2.drawString(composition, x + 165, y);
    }

    /**
     * Dessine une petite fleche representant le joueur a cote de ses stats.
     * Taille reduite pour tenir dans la colonne.
     *
     * @param g2 Contexte graphique
     * @param cx Centre X de la fleche
     * @param cy Centre Y de la fleche
     */
    private void drawPlayerShape(Graphics2D g2, int cx, int cy) {
        // Fleche pointant vers le haut (angle = -PI/2)
        double angle = -Math.PI / 2;
        int h = 10;
        int[] xLocal = {  h,  -h/2, -h/4, -h/2 };
        int[] yLocal = {  0,  -h/2,  0,    h/2  };

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int[] xp = new int[4];
        int[] yp = new int[4];
        for (int i = 0; i < 4; i++) {
            xp[i] = cx + (int)(xLocal[i] * cos - yLocal[i] * sin);
            yp[i] = cy + (int)(xLocal[i] * sin + yLocal[i] * cos);
        }
        Polygon arrow = new Polygon(xp, yp, 4);
        g2.setColor(new Color(80, 200, 120));
        g2.fillPolygon(arrow);
        g2.setColor(new Color(30, 100, 60));
        g2.drawPolygon(arrow);
    }
}
