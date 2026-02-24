package projetCROMBEZ;

import java.awt.*;
import java.util.List;

/**
 * Represente le personnage controle par le joueur.
 *
 * ===============================================
 *  STATS DE BASE (valeurs par defaut au premier lancement)
 *  HP max     : 100
 *  Degats     : 15   (par projectile)
 *  Vitesse    : 5    px/frame  (fixe, non upgradable pour l'instant)
 *  Portee     : 200px (rayon de detection de cible)
 *  Cadence    : 60 frames (1 tir/seconde)
 * ===============================================
 *
 * Toutes les stats sont publiques et modifiables : elles seront
 * chargees depuis SaveManager au demarrage et pourront etre
 * augmentees via la boutique future.
 *
 * Forme : Fleche verte pointant vers le dernier ennemi cible.
 */
public class Player {

    // =========================================================================
    // Position
    // =========================================================================

    /** Position X du centre du joueur (en pixels). */
    public double x;

    /** Position Y du centre du joueur (en pixels). */
    public double y;

    // =========================================================================
    // Stats de combat
    // Ces valeurs sont chargees depuis la sauvegarde au demarrage.
    // Elles peuvent etre modifiees par la boutique future.
    // Les valeurs ci-dessous sont les DEFAUTS si aucune sauvegarde n'existe.
    // =========================================================================

    /** Points de vie maximum. Augmentable via la boutique. */
    public int maxHp = 100;

    /** Points de vie actuels. Remis a maxHp au debut de chaque partie. */
    public int hp = 100;

    /** Degats infliges par chaque projectile. Augmentable via la boutique. */
    public int damage = 15;

    /** Rayon de portee d'attaque en pixels. Augmentable via la boutique. */
    public int attackRange = 200;

    /**
     * Or du joueur, cumule entre les parties.
     * Gagne en tuant des ennemis, depense dans la boutique future.
     * Persiste entre les sessions via SaveManager.
     */
    public int gold = 0;

    /**
     * Intervalle entre deux tirs en frames (moins = plus rapide).
     * 60 frames = 1 tir/sec a 60 FPS.
     * Diminuable via la boutique.
     */
    public int attackRate = 60;

    // =========================================================================
    // Rendu et hitbox
    // =========================================================================

    /** Taille du joueur en pixels (= tileSize du GamePanel = 32px). */
    public int size;

    /** Angle de rotation de la fleche (pointe vers le dernier ennemi cible). */
    private double aimAngle = 0;

    // =========================================================================
    // Etat
    // =========================================================================

    /** false des que le joueur est mort. */
    public boolean alive = true;

    /**
     * Compteur d'invincibilite en frames apres reception de degats.
     * Pendant ce temps, le joueur clignote et ne peut pas prendre de degats.
     */
    private int invincibleTimer = 0;

    /** Duree de l'invincibilite post-hit : 60 frames = 1 seconde a 60 FPS. */
    private static final int INVINCIBLE_DURATION = 60;

    /** Compteur de cooldown avant le prochain tir automatique. */
    private int attackCooldown = 0;

    // =========================================================================
    // Reference
    // =========================================================================

    /** Reference au GamePanel pour les dimensions d'ecran et la police. */
    private GamePanel gp;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree le joueur avec les stats par defaut.
     * Les vraies valeurs seront ecrasees par SaveManager.load() juste apres.
     *
     * @param gp Reference au GamePanel
     */
    public Player(GamePanel gp) {
        this.gp   = gp;
        this.size = gp.tileSize; // 32px
        reset();
    }

    // =========================================================================
    // Reset (debut de partie)
    // =========================================================================

    /**
     * Reinitialise la position et l'etat de combat pour une nouvelle partie.
     * Les stats (maxHp, damage, etc.) NE sont PAS remises a zero :
     * elles doivent persister d'une partie a l'autre (upgrades boutique).
     *
     * Appele par GamePanel.resetGame() au demarrage de chaque partie.
     */
    public void reset() {
        x               = gp.screenWidth  / 2.0;
        y               = gp.screenHeight / 2.0;
        hp              = maxHp; // repart avec la vie pleine
        alive           = true;
        invincibleTimer = 0;
        attackCooldown  = 0;
        aimAngle        = -Math.PI / 2; // pointe vers le haut par defaut
    }

    // =========================================================================
    // Mise a jour
    // =========================================================================

    /**
     * Met a jour le joueur pour un frame :
     *  1. Deplacement selon les touches (ZQSD / fleches)
     *  2. Clamp dans les bords de l'ecran
     *  3. Attaque automatique sur l'ennemi le plus proche dans la portee
     *  4. Decrementation de l'invincibilite post-hit
     *  5. Verification des collisions avec les projectiles ennemis
     *
     * @param keyH        Etat du clavier
     * @param enemies     Liste des ennemis (pour le ciblage)
     * @param projectiles Liste des projectiles (pour les collisions)
     */
    public void update(KeyHandler keyH, List<Enemy> enemies, List<Projectile> projectiles) {
        // Deplacement
        if (keyH.upPressed)    y -= 5;
        if (keyH.downPressed)  y += 5;
        if (keyH.leftPressed)  x -= 5;
        if (keyH.rightPressed) x += 5;

        // Clamp dans les bords
        x = Math.max(size / 2.0, Math.min(gp.screenWidth  - size / 2.0, x));
        y = Math.max(size / 2.0, Math.min(gp.screenHeight - size / 2.0, y));

        // Attaque automatique sur le plus proche dans la portee
        if (attackCooldown > 0) attackCooldown--;

        if (attackCooldown <= 0) {
            Enemy target = findClosestEnemy(enemies);
            if (target != null) {
                aimAngle = Math.atan2(target.y - y, target.x - x);
                projectiles.add(new Projectile(x, y, target.x, target.y, damage, true));
                attackCooldown = attackRate;
            }
        }

        // Decremente l'invincibilite
        if (invincibleTimer > 0) invincibleTimer--;

        // Collisions avec projectiles ennemis
        for (Projectile p : projectiles) {
            if (!p.fromPlayer && p.alive && p.getBounds().intersects(getBounds())) {
                takeDamage(p.damage);
                p.alive = false;
            }
        }

        if (hp <= 0) alive = false;
    }

    /**
     * Cherche l'ennemi vivant le plus proche dans le rayon attackRange.
     *
     * @param enemies Liste des ennemis actifs
     * @return        L'ennemi le plus proche, ou null si aucun dans la portee
     */
    private Enemy findClosestEnemy(List<Enemy> enemies) {
        Enemy  closest = null;
        double minDist = attackRange;

        for (Enemy e : enemies) {
            if (!e.alive) continue;
            double dist = Math.sqrt(Math.pow(e.x - x, 2) + Math.pow(e.y - y, 2));
            if (dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }
        return closest;
    }

    // =========================================================================
    // Combat
    // =========================================================================

    /**
     * Inflige des degats au joueur.
     * Ignore si l'invincibilite post-hit est active.
     *
     * @param dmg Quantite de degats
     */
    public void takeDamage(int dmg) {
        if (invincibleTimer > 0) return;
        hp = Math.max(0, hp - dmg);
        invincibleTimer = INVINCIBLE_DURATION;
        if (hp <= 0) alive = false;
    }

    // =========================================================================
    // Collision
    // =========================================================================

    /** Retourne le rectangle de collision (carre centre sur la position). */
    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine le joueur :
     *  - Cercle de portee (si active dans les options)
     *  - Fleche verte orientee vers la cible
     *  - HUD barre de vie
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Cercle de portee (optionnel)
        if (GameSettings.getInstance().isShowPlayerRange()) {
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval((int)x - attackRange, (int)y - attackRange,
                        attackRange * 2,      attackRange * 2);
            g2.setColor(new Color(255, 255, 255, 35));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                         10f, new float[]{5f, 5f}, 0f));
            g2.drawOval((int)x - attackRange, (int)y - attackRange,
                        attackRange * 2,      attackRange * 2);
            g2.setStroke(new BasicStroke(1f));
        }

        // Fleche du joueur (clignotement si invincible)
        boolean isBlinking = (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0);
        g2.setColor(isBlinking ? new Color(255, 255, 255, 150) : new Color(80, 200, 120));

        Polygon arrow = buildArrow(aimAngle);
        g2.fillPolygon(arrow);

        g2.setColor(new Color(30, 100, 60));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(arrow);
        g2.setStroke(new BasicStroke(1f));

        drawHUD(g2);
    }

    /**
     * Construit le polygone en forme de fleche oriente selon l'angle de visee.
     * Compose d'une pointe avant et de deux ailettes arriere.
     *
     * @param angle Angle en radians
     * @return      Polygone pret a l'affichage
     */
    private Polygon buildArrow(double angle) {
        int h = size / 2;
        int[] xLocal = {  h,  -h/2, -h/4, -h/2 };
        int[] yLocal = {  0,  -h/2,  0,    h/2  };

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int[] xp = new int[4];
        int[] yp = new int[4];
        for (int i = 0; i < 4; i++) {
            xp[i] = (int)(x + xLocal[i] * cos - yLocal[i] * sin);
            yp[i] = (int)(y + xLocal[i] * sin + yLocal[i] * cos);
        }
        return new Polygon(xp, yp, 4);
    }

    /**
     * Dessine la barre de vie HUD en haut a gauche.
     * Couleur : vert > 50% | jaune 25-50% | rouge < 25%
     *
     * @param g2 Contexte graphique
     */
    private void drawHUD(Graphics2D g2) {
        int barWidth  = 200;
        int barHeight = 18;
        int barX = 10;
        int barY = 10;

        // Fond
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Vie restante
        int    currentWidth = (int)(barWidth * ((double) hp / maxHp));
        double ratio        = (double) hp / maxHp;
        Color  hpColor      = (ratio > 0.50) ? new Color(50, 200, 80)
                            : (ratio > 0.25) ? new Color(220, 180, 0)
                                             : new Color(220, 50, 50);
        g2.setColor(hpColor);
        g2.fillRect(barX, barY, currentWidth, barHeight);

        // Contour
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.setStroke(new BasicStroke(1f));

        // Texte
        Font hudFont = gp.gameFont != null ? gp.gameFont.deriveFont(13f)
                                           : new Font("Arial", Font.PLAIN, 13);
        g2.setFont(hudFont);
        g2.setColor(Color.white);
        g2.drawString("HP : " + hp + " / " + maxHp, barX + 5, barY + 13);
    }
}
