package projetCROMBEZ;

import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * Represente le personnage controle par le joueur.
 *
 * =========================================================================
 * STATS DE BASE (premier lancement, sans aucun upgrade)
 * =========================================================================
 *   HP max          : 100
 *   Degats          : 15 par projectile
 *   Vitesse         : 5 px/frame (non upgradable)
 *   Portee          : 200 px
 *   Cadence de tir  : 60 frames = 1 tir/sec a 60 FPS
 *   Vol de vie      : 0 %
 *   Chance critique : 0 %
 *   Degats critiques: x1.5 (50 % de bonus si crit)
 *
 * =========================================================================
 * NIVEAUX D'UPGRADE (stockes dans upgradeXxx, 0-5)
 * =========================================================================
 * Les niveaux sont sauvegardes via SaveManager.
 * Les stats effectives sont recalculees depuis les niveaux a chaque
 * appel de applyUpgrades(), notamment apres chaque achat en boutique.
 *
 * Upgrade | Effet par niveau  | Stat cible
 * --------|-------------------|------------
 * Hp      | +25 HP max        | maxHp
 * Damage  | +5 degats         | damage
 * Range   | +30 px portee     | attackRange
 * Speed   | -6 frames cadence | attackRate (min 20)
 * LifeSt. | +5 % vol de vie   | lifeStealPct (0-25)
 * Crit    | +5 % crit chance  | critChancePct (0-25)
 * CritDmg | +25 % crit degats | critMultiplier (1.5->3.0)
 */
public class Player {

    // =========================================================================
    // Position
    // =========================================================================

    public double x, y;

    // =========================================================================
    // Stats de combat effectives (calculees depuis les niveaux d'upgrade)
    // =========================================================================

    /** HP maximum actuels. */
    public int maxHp = 100;

    /** HP actuels. Remis a maxHp au debut de chaque partie. */
    public int hp = 100;

    /** Degats de base par projectile (avant calcul crit). */
    public int damage = 100;

    /** Rayon de portee d'attaque en pixels. */
    public int attackRange = 200;

    /**
     * Intervalle entre deux tirs en frames.
     * 60 frames = 1 tir/sec a 60 FPS. Min 20 (3 tirs/sec).
     */
    public int attackRate = 60;

    /**
     * Pourcentage de vol de vie (0-25).
     * Exemple : 10 signifie que 10 % des degats infliges regenerent des HP.
     */
    public int lifeStealPct = 0;

    /**
     * Probabilite de coup critique en pourcentage (0-25).
     * Un coup critique multiplie les degats par critMultiplier.
     */
    public int critChancePct = 0;

    /**
     * Multiplicateur de degats en cas de coup critique.
     * Valeur de base : 1.5 (= +50 % de degats).
     * Augmente via la boutique.
     */
    public float critMultiplier = 1.5f;

    // =========================================================================
    // Niveaux d'upgrade (persistes via SaveManager)
    // =========================================================================

    public int upgradeHp        = 0;  // 0-5
    public int upgradeDamage    = 0;  // 0-5
    public int upgradeRange     = 0;  // 0-5
    public int upgradeSpeed     = 0;  // 0-5
    public int upgradeLifeSteal = 0;  // 0-5
    public int upgradeCrit      = 0;  // 0-5
    public int upgradeCritDmg   = 0;  // 0-5

    /** Niveau maximum pour chaque upgrade. */
    public static final int MAX_UPGRADE_LEVEL = 5;

    // =========================================================================
    // Ressources
    // =========================================================================

    /**
     * Or cumule entre les parties.
     * Gagne en tuant des ennemis (via GamePanel), depense en boutique.
     */
    public int gold = 0;

    // =========================================================================
    // Rendu et hitbox
    // =========================================================================

    public int size;

    /** Angle de la fleche (pointe vers la derniere cible). */
    private double aimAngle = 0;

    // =========================================================================
    // Etat interne
    // =========================================================================

    public boolean alive = true;

    private int invincibleTimer = 0;
    private static final int INVINCIBLE_DURATION = 60;

    private int attackCooldown = 0;

    private final Random rand = new Random();

    /** Compteur de flash rouge quand un coup critique est inflige. */
    private int critFlashTimer = 0;

    // =========================================================================
    // Reference
    // =========================================================================

    private GamePanel gp;

    // =========================================================================
    // Constructeur
    // =========================================================================

    public Player(GamePanel gp) {
        this.gp   = gp;
        this.size = gp.tileSize;
        applyUpgrades();
        reset();
    }

    // =========================================================================
    // Upgrades
    // =========================================================================

    /**
     * Recalcule toutes les stats effectives a partir des niveaux d'upgrade.
     * Appeler apres chaque achat en boutique et apres SaveManager.load().
     */
    public void applyUpgrades() {
        maxHp          = 100 + upgradeHp        * 25;
        damage         = 15  + upgradeDamage    * 5;
        attackRange    = 200 + upgradeRange     * 30;
        attackRate     = Math.max(20, 60 - upgradeSpeed * 6);
        lifeStealPct   = upgradeLifeSteal * 5;
        critChancePct  = 50 +upgradeCrit      * 5;
        critMultiplier = 1.5f + upgradeCritDmg  * 0.25f;
    }

    // =========================================================================
    // Reset (debut de partie)
    // =========================================================================

    /**
     * Reinitialise la position et l'etat de combat.
     * Les stats et l'or NE sont PAS remis a zero (persistants entre parties).
     */
    public void reset() {
        x               = gp.screenWidth  / 2.0;
        y               = gp.screenHeight / 2.0;
        hp              = maxHp;
        alive           = true;
        invincibleTimer = 0;
        attackCooldown  = 0;
        critFlashTimer  = 0;
        aimAngle        = -Math.PI / 2;
    }

    // =========================================================================
    // Mise a jour
    // =========================================================================

    public void update(KeyHandler keyH, List<Enemy> enemies, List<Projectile> projectiles) {
        // Deplacement
        if (keyH.upPressed)    y -= 5;
        if (keyH.downPressed)  y += 5;
        if (keyH.leftPressed)  x -= 5;
        if (keyH.rightPressed) x += 5;

        x = Math.max(size / 2.0, Math.min(gp.screenWidth  - size / 2.0, x));
        y = Math.max(size / 2.0, Math.min(gp.screenHeight - size / 2.0, y));

        // Tir automatique
        if (attackCooldown > 0) attackCooldown--;

        if (attackCooldown <= 0) {
            Enemy target = findClosestEnemy(enemies);
            if (target != null) {
                aimAngle = Math.atan2(target.y - y, target.x - x);

                // Calcul crit
                boolean isCrit = (critChancePct > 0 && rand.nextInt(100) < critChancePct);
                int shotDamage = isCrit ? (int)(damage * critMultiplier) : damage;

                Projectile p = new Projectile(x, y, target.x, target.y, shotDamage, true);
                p.isCrit = isCrit;
                projectiles.add(p);

                if (isCrit) critFlashTimer = 12; // flash visuel 12 frames
                attackCooldown = attackRate;
            }
        }

        if (invincibleTimer  > 0) invincibleTimer--;
        if (critFlashTimer   > 0) critFlashTimer--;

        // Collisions avec projectiles ennemis
        for (Projectile p : projectiles) {
            if (!p.fromPlayer && p.alive && p.getBounds().intersects(getBounds())) {
                takeDamage(p.damage);
                p.alive = false;
            }
        }

        if (hp <= 0) alive = false;
    }

    private Enemy findClosestEnemy(List<Enemy> enemies) {
        Enemy  closest = null;
        double minDist = attackRange;
        for (Enemy e : enemies) {
            if (!e.alive) continue;
            double d = Math.hypot(e.x - x, e.y - y);
            if (d < minDist) { minDist = d; closest = e; }
        }
        return closest;
    }

    // =========================================================================
    // Combat
    // =========================================================================

    public void takeDamage(int dmg) {
        if (invincibleTimer > 0) return;
        hp = Math.max(0, hp - dmg);
        invincibleTimer = INVINCIBLE_DURATION;
        if (hp <= 0) alive = false;
    }

    /**
     * Soigne le joueur selon le vol de vie.
     * Appele par GamePanel quand un projectile du joueur touche un ennemi.
     *
     * @param dmgDealt degats reellement infliges a l'ennemi
     */
    public void applyLifeSteal(int dmgDealt) {
        if (lifeStealPct <= 0 || !alive) return;
        int heal = Math.max(1, dmgDealt * lifeStealPct / 100);
        hp = Math.min(maxHp, hp + heal);
    }

    // =========================================================================
    // Collision
    // =========================================================================

    public Rectangle getBounds() {
        return new Rectangle((int)x - size/2, (int)y - size/2, size, size);
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    public void draw(Graphics2D g2) {
        // Cercle de portee
        if (GameSettings.getInstance().isShowPlayerRange()) {
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval((int)x - attackRange, (int)y - attackRange, attackRange*2, attackRange*2);
            g2.setColor(new Color(255, 255, 255, 35));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                         10f, new float[]{5f,5f}, 0f));
            g2.drawOval((int)x - attackRange, (int)y - attackRange, attackRange*2, attackRange*2);
            g2.setStroke(new BasicStroke(1f));
        }

        // Fleche (clignote blanc si invincible, jaune si vient de critter)
        boolean blinking = (invincibleTimer > 0 && (invincibleTimer/5) % 2 == 0);
        boolean critting = (critFlashTimer  > 0);
        Color arrowColor = blinking ? new Color(255, 255, 255, 150)
                         : critting ? new Color(255, 220, 50)
                                    : new Color(80, 200, 120);
        g2.setColor(arrowColor);
        Polygon arrow = buildArrow(aimAngle);
        g2.fillPolygon(arrow);
        g2.setColor(new Color(30, 100, 60));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(arrow);
        g2.setStroke(new BasicStroke(1f));

        drawHUD(g2);
    }

    private Polygon buildArrow(double angle) {
        int h = size / 2;
        int[] xl = {  h, -h/2, -h/4, -h/2 };
        int[] yl = {  0, -h/2,  0,    h/2  };
        double cos = Math.cos(angle), sin = Math.sin(angle);
        int[] xp = new int[4], yp = new int[4];
        for (int i = 0; i < 4; i++) {
            xp[i] = (int)(x + xl[i]*cos - yl[i]*sin);
            yp[i] = (int)(y + xl[i]*sin + yl[i]*cos);
        }
        return new Polygon(xp, yp, 4);
    }

    private void drawHUD(Graphics2D g2) {
        int bw = 200, bh = 18, bx = 10, by = 10;

        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(bx, by, bw, bh);

        int    filled = (int)(bw * ((double)hp / maxHp));
        double ratio  = (double)hp / maxHp;
        Color  hpCol  = ratio > 0.5 ? new Color(50,200,80)
                      : ratio > 0.25 ? new Color(220,180,0)
                                     : new Color(220,50,50);
        g2.setColor(hpCol);
        g2.fillRect(bx, by, filled, bh);

        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(bx, by, bw, bh);
        g2.setStroke(new BasicStroke(1f));

        Font f = gp.gameFont != null ? gp.gameFont.deriveFont(13f) : new Font("Arial",Font.PLAIN,13);
        g2.setFont(f);
        g2.setColor(Color.white);
        g2.drawString("HP : " + hp + " / " + maxHp, bx + 5, by + 13);

        // Or en dessous de la barre de vie
        Font gf = gp.gameFont != null ? gp.gameFont.deriveFont(13f) : new Font("Arial",Font.PLAIN,13);
        g2.setFont(gf);
        g2.setColor(new Color(255, 210, 50));
        g2.drawString("Or : " + gold, bx + 5, by + bh + 16);
    }
}
