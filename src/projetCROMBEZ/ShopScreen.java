package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecran de la boutique d'ameliorations.
 *
 * Accessible uniquement depuis le menu principal.
 * Permet d'acheter des ameliorations permanentes avec l'or gagne en jeu.
 * Les achats persistent entre les sessions via SaveManager.
 *
 * =========================================================================
 * UPGRADES DISPONIBLES (5 niveaux chacune)
 * =========================================================================
 *
 *  # | Nom              | Effet / niveau | Cout par niveau
 * ---|------------------|----------------|------------------------------
 *  1 | Vie max          | +25 HP         | 30 / 60 / 100 / 150 / 220
 *  2 | Degats           | +5 dgts        | 40 / 80 / 130 / 190 / 270
 *  3 | Portee           | +30 px         | 30 / 60 / 100 / 150 / 220
 *  4 | Vitesse d'atk    | +10% cadence   | 40 / 80 / 120 / 180 / 260
 *  5 | Vol de vie       | +5 % heal      | 50 / 100/ 160 / 230 / 320
 *  6 | Chance critique  | +5 % crit      | 60 / 120/ 190 / 270 / 370
 *  7 | Degats critiques | +25% mult.     | 70 / 140/ 220 / 310 / 420
 *
 * --- Architecture listener ---
 * Aucun MouseListener ici. GamePanel dispatche via handleClick/handleHover
 * uniquement quand gameState == SHOP.
 */
public class ShopScreen {

    // =========================================================================
    // Reference
    // =========================================================================

    private GamePanel gp;

    // =========================================================================
    // Definition des upgrades
    // =========================================================================

    /** Noms affiches dans la boutique. */
    private static final String[] NAMES = {
        "Vie maximum",
        "Degats d'attaque",
        "Portee d'attaque",
        "Vitesse d'attaque",
        "Vol de vie",
        "Chance critique",
        "Degats critiques"
    };

    /** Description de l'effet au niveau suivant. */
    private static final String[] EFFECTS = {
        "+25 HP maximum",
        "+5 degats / tir",
        "+30 px de portee",
        "+10 % cadence",
        "+5 % de vol de vie",
        "+5 % chance de crit",
        "+25 % mult. crit"
    };

    /** Valeur actuelle formatee. */
    private String[] getCurrentValues() {
        Player p = gp.player;
        return new String[]{
            p.maxHp       + " HP",
            p.damage      + " dgts",
            p.attackRange + " px",
            String.format("%.1f tirs/s", 60.0 / p.attackRate),
            p.lifeStealPct + " %",
            p.critChancePct + " %",
            String.format("x%.2f", p.critMultiplier)
        };
    }

    /** Couts par niveau (index 0 = niveau 1, index 4 = niveau 5). */
    private static final int[][] COSTS = {
        { 30,  60, 100, 150, 220 }, // HP
        { 40,  80, 130, 190, 270 }, // Damage
        { 30,  60, 100, 150, 220 }, // Range
        { 40,  80, 120, 180, 260 }, // Speed
        { 50, 100, 160, 230, 320 }, // LifeSteal
        { 60, 120, 190, 270, 370 }, // Crit
        { 70, 140, 220, 310, 420 }  // CritDmg
    };

    /** Couleurs associees a chaque upgrade. */
    private static final Color[] COLORS = {
        new Color(80,  200, 120),  // Vie
        new Color(220,  80,  80),  // Degats
        new Color(80,  180, 220),  // Portee
        new Color(180, 120, 220),  // Vitesse
        new Color(220, 100, 160),  // Vol de vie
        new Color(255, 200,  50),  // Crit
        new Color(255, 140,   0)   // CritDmg
    };

    // =========================================================================
    // Layout
    // =========================================================================

    private static final int PANEL_W   = 820;
    private static final int ROW_H     = 68;
    private static final int ROW_GAP   = 8;
    private static final int MAX_LEVEL = Player.MAX_UPGRADE_LEVEL;

    /** Rectangle du bouton "Retour". */
    private Rectangle btnBack;

    /** Rectangles des boutons "Acheter" (un par upgrade). */
    private List<Rectangle> buyButtons = new ArrayList<>();

    /** Index survole (-1 = aucun). */
    private int hoveredRow  = -1;
    private boolean hoveredBack = false;

    // =========================================================================
    // Constructeur
    // =========================================================================

    public ShopScreen(GamePanel gp) {
        this.gp = gp;
        buildLayout();
    }

    /** Calcule les positions de tous les boutons selon la taille de l'ecran. */
    private void buildLayout() {
        buyButtons.clear();

        int panelX = gp.screenWidth  / 2 - PANEL_W / 2;
        int startY = 110; // premiere ligne

        for (int i = 0; i < NAMES.length; i++) {
            int rowY = startY + i * (ROW_H + ROW_GAP);
            // Bouton Acheter a droite de chaque ligne
            buyButtons.add(new Rectangle(
                panelX + PANEL_W - 130,
                rowY + (ROW_H - 36) / 2,
                120, 36
            ));
        }

        // Bouton retour en bas
        btnBack = new Rectangle(
            gp.screenWidth / 2 - 90,
            startY + NAMES.length * (ROW_H + ROW_GAP) + 10,
            180, 42
        );
    }

    // =========================================================================
    // Evenements
    // =========================================================================

    public void handleClick(Point p) {
        // Bouton retour
        if (btnBack.contains(p)) {
            gp.gameState = GameState.MENU;
            return;
        }
        // Boutons acheter
        for (int i = 0; i < buyButtons.size(); i++) {
            if (buyButtons.get(i).contains(p)) {
                tryBuy(i);
                return;
            }
        }
    }

    public void handleHover(Point p) {
        hoveredRow  = -1;
        hoveredBack = btnBack.contains(p);
        for (int i = 0; i < buyButtons.size(); i++) {
            if (buyButtons.get(i).contains(p)) { hoveredRow = i; return; }
        }
    }

    // =========================================================================
    // Logique d'achat
    // =========================================================================

    /**
     * Tente d'acheter le prochain niveau de l'upgrade i.
     * Verifie que le niveau max n'est pas atteint et que l'or est suffisant.
     */
    private void tryBuy(int i) {
        Player player  = gp.player;
        int    curLevel = getLevel(player, i);

        if (curLevel >= MAX_LEVEL) return; // deja au max
        int cost = COSTS[i][curLevel];
        if (player.gold < cost)    return; // pas assez d'or

        // Deduction de l'or
        player.gold -= cost;

        // Increment du niveau
        setLevel(player, i, curLevel + 1);

        // Recalcul des stats depuis les niveaux
        player.applyUpgrades();

        // Sauvegarde immediate
        SaveManager.save(gp);
    }

    /** Retourne le niveau actuel de l'upgrade i. */
    private int getLevel(Player p, int i) {
        switch (i) {
            case 0: return p.upgradeHp;
            case 1: return p.upgradeDamage;
            case 2: return p.upgradeRange;
            case 3: return p.upgradeSpeed;
            case 4: return p.upgradeLifeSteal;
            case 5: return p.upgradeCrit;
            case 6: return p.upgradeCritDmg;
            default: return 0;
        }
    }

    /** Modifie le niveau de l'upgrade i. */
    private void setLevel(Player p, int i, int level) {
        switch (i) {
            case 0: p.upgradeHp        = level; break;
            case 1: p.upgradeDamage    = level; break;
            case 2: p.upgradeRange     = level; break;
            case 3: p.upgradeSpeed     = level; break;
            case 4: p.upgradeLifeSteal = level; break;
            case 5: p.upgradeCrit      = level; break;
            case 6: p.upgradeCritDmg   = level; break;
        }
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    public void draw(Graphics2D g2) {
        // Fond
        GradientPaint bg = new GradientPaint(0, 0, new Color(10,10,30),
                                              0, gp.screenHeight, new Color(20,5,40));
        g2.setPaint(bg);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // Titre
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 34f)
                                             : new Font("Arial", Font.BOLD, 34);
        g2.setFont(titleFont);
        g2.setColor(new Color(255, 210, 50));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Boutique";
        g2.drawString(title, gp.screenWidth/2 - fm.stringWidth(title)/2, 70);

        // Or disponible
        Font goldFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                            : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(goldFont);
        fm = g2.getFontMetrics();
        String goldStr = "Or disponible : " + gp.player.gold;
        g2.setColor(new Color(255, 210, 50));
        g2.drawString(goldStr, gp.screenWidth/2 - fm.stringWidth(goldStr)/2, 92);

        // Lignes d'upgrade
        int panelX = gp.screenWidth/2 - PANEL_W/2;
        int startY = 110;
        String[] currentValues = getCurrentValues();

        for (int i = 0; i < NAMES.length; i++) {
            int rowY    = startY + i * (ROW_H + ROW_GAP);
            int level   = getLevel(gp.player, i);
            boolean max = (level >= MAX_LEVEL);
            boolean sel = (hoveredRow == i);

            drawUpgradeRow(g2, panelX, rowY, i, level, max, sel, currentValues[i]);
        }

        // Bouton retour
        g2.setColor(hoveredBack ? new Color(80,80,110) : new Color(40,40,65));
        g2.fillRoundRect(btnBack.x, btnBack.y, btnBack.width, btnBack.height, 12, 12);
        g2.setColor(new Color(140, 140, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(btnBack.x, btnBack.y, btnBack.width, btnBack.height, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        Font backFont = gp.gameFont != null ? gp.gameFont.deriveFont(16f)
                                            : new Font("Arial", Font.PLAIN, 16);
        g2.setFont(backFont);
        g2.setColor(Color.white);
        fm = g2.getFontMetrics();
        String back = "<- Retour";
        g2.drawString(back, btnBack.x + btnBack.width/2 - fm.stringWidth(back)/2,
                      btnBack.y + btnBack.height/2 + fm.getAscent()/2 - 2);
    }

    /**
     * Dessine une ligne complete de l'upgrade : fond, couleur, icone, nom,
     * valeur actuelle, jauge de niveau, et bouton Acheter.
     */
    private void drawUpgradeRow(Graphics2D g2, int x, int y, int index,
                                 int level, boolean max, boolean hovered,
                                 String currentValue) {
        Color accent = COLORS[index];

        // Fond de la ligne
        Color rowBg = hovered ? new Color(30, 30, 55) : new Color(18, 18, 38);
        g2.setColor(rowBg);
        g2.fillRoundRect(x, y, PANEL_W, ROW_H, 12, 12);

        // Bordure gauche coloree
        g2.setColor(accent);
        g2.fillRoundRect(x, y, 5, ROW_H, 4, 4);

        // Bordure exterieure fine
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(accent.getRed()/3, accent.getGreen()/3, accent.getBlue()/3));
        g2.drawRoundRect(x, y, PANEL_W, ROW_H, 12, 12);

        // Nom de l'upgrade
        Font nameFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 16f)
                                            : new Font("Arial", Font.BOLD, 16);
        g2.setFont(nameFont);
        g2.setColor(accent);
        g2.drawString(NAMES[index], x + 16, y + 22);

        // Effet du prochain niveau (ou "MAX" si max)
        Font effectFont = gp.gameFont != null ? gp.gameFont.deriveFont(13f)
                                              : new Font("Arial", Font.PLAIN, 13);
        g2.setFont(effectFont);
        if (max) {
            g2.setColor(new Color(255, 210, 50));
            g2.drawString("NIVEAU MAX", x + 16, y + 42);
        } else {
            g2.setColor(new Color(170, 170, 200));
            g2.drawString("Prochain : " + EFFECTS[index], x + 16, y + 42);
        }

        // Valeur actuelle
        Font valFont = gp.gameFont != null ? gp.gameFont.deriveFont(15f)
                                           : new Font("Arial", Font.PLAIN, 15);
        g2.setFont(valFont);
        g2.setColor(Color.white);
        g2.drawString(currentValue, x + 220, y + ROW_H/2 + 6);

        // Jauge de niveaux (5 carres)
        int gaugX = x + 340;
        int gaugY = y + ROW_H/2 - 7;
        int sqW = 22, sqH = 14, sqGap = 4;
        for (int k = 0; k < MAX_LEVEL; k++) {
            boolean filled = (k < level);
            g2.setColor(filled ? accent : new Color(40, 40, 60));
            g2.fillRoundRect(gaugX + k*(sqW + sqGap), gaugY, sqW, sqH, 4, 4);
            g2.setColor(new Color(60, 60, 80));
            g2.drawRoundRect(gaugX + k*(sqW + sqGap), gaugY, sqW, sqH, 4, 4);
        }

        // Niveau textuel
        Font lvlFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                           : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(lvlFont);
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(level + "/" + MAX_LEVEL, gaugX + MAX_LEVEL*(sqW+sqGap) + 6, gaugY + 11);

        // Bouton Acheter
        Rectangle buyBtn = buyButtons.get(index);
        if (max) {
            // Niveau max : bouton grise
            g2.setColor(new Color(30, 30, 45));
            g2.fillRoundRect(buyBtn.x, buyBtn.y, buyBtn.width, buyBtn.height, 10, 10);
            g2.setColor(new Color(70, 70, 90));
            g2.drawRoundRect(buyBtn.x, buyBtn.y, buyBtn.width, buyBtn.height, 10, 10);
            g2.setFont(effectFont);
            g2.setColor(new Color(100, 100, 120));
            FontMetrics fm = g2.getFontMetrics();
            String lbl = "MAX";
            g2.drawString(lbl, buyBtn.x + buyBtn.width/2 - fm.stringWidth(lbl)/2,
                          buyBtn.y + buyBtn.height/2 + fm.getAscent()/2 - 2);
        } else {
            int cost = COSTS[index][level];
            boolean canAfford = (gp.player.gold >= cost);

            Color btnBg  = hovered && canAfford ? new Color(180, 150, 20)
                         : canAfford            ? new Color(120, 100, 10)
                                               : new Color(50, 30, 30);
            Color btnBrd = canAfford ? new Color(255, 210, 50) : new Color(100, 60, 60);
            Color btnTxt = canAfford ? Color.white : new Color(120, 80, 80);

            g2.setColor(btnBg);
            g2.fillRoundRect(buyBtn.x, buyBtn.y, buyBtn.width, buyBtn.height, 10, 10);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(btnBrd);
            g2.drawRoundRect(buyBtn.x, buyBtn.y, buyBtn.width, buyBtn.height, 10, 10);
            g2.setStroke(new BasicStroke(1f));

            Font btnFont = gp.gameFont != null ? gp.gameFont.deriveFont(14f)
                                               : new Font("Arial", Font.PLAIN, 14);
            g2.setFont(btnFont);
            g2.setColor(btnTxt);
            FontMetrics fm = g2.getFontMetrics();
            String lbl = cost + " or";
            g2.drawString(lbl, buyBtn.x + buyBtn.width/2 - fm.stringWidth(lbl)/2,
                          buyBtn.y + buyBtn.height/2 + fm.getAscent()/2 - 2);
        }
    }
}
