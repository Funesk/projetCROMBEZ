package projetCROMBEZ;

import java.awt.*;
import java.util.List;

/**
 * Représente le personnage contrôlé par le joueur.
 *
 *  STATS DE BASE
 *  HP max     : 100
 *  Dégats     : 15   (par projectile)
 *  Vitesse    : 5    px/frame
 *  Portée     : 200px (rayon de détection de cible)
 *  Cadence    : 60 frames (1 tir/seconde)
 *
 * Fonctionnalités :
 *  - Déplacement via KeyHandler (ZQSD / flèches).
 *  - Attaque automatique sur l'ennemi le plus proche dans la portée.
 *  - Invincibilité brève après avoir reçu des dégats (clignotement).
 *  - Affiche son HUD (barre de vie) directement.
 *
 * Forme : Flèche pointant vers le dernier ennemi ciblé  indique visuellement
 *         la direction de l'attaque.
 */
public class Player {

    // -------------------------------------------------------------------------
    // Position
    // -------------------------------------------------------------------------

    /** Position X du centre du joueur (en pixels). */
    public double x;

    /** Position Y du centre du joueur (en pixels). */
    public double y;

    // -------------------------------------------------------------------------
    // Stats de combat (modifiables pour future boutique)
    // -------------------------------------------------------------------------

    /** Points de vie maximum. */
    public int maxHp = 100;

    /** Points de vie actuels. */
    public int hp = 100;

    /** Dégats infligés par chaque projectile. */
    public int damage = 100;

    /** Rayon de portée d'attaque en pixels. */
    public int attackRange = 300;

    /** Intervalle entre deux tirs (en frames). */
    public int attackRate = 20;

    // -------------------------------------------------------------------------
    // Rendu et hitbox
    // -------------------------------------------------------------------------

    /** Taille du joueur (correspond à  un tile de l'écran). */
    public int size;

    /** Angle de rotation de la flèche (pointe vers le dernier ennemi ciblé). */
    private double aimAngle = 0;

    // -------------------------------------------------------------------------
    // à‰tat
    // -------------------------------------------------------------------------

    /** false dès que le joueur est mort. */
    public boolean alive = true;

    /** Compteur d'invincibilité en frames (après réception de dégats). */
    private int invincibleTimer = 0;

    /** Durée de l'invincibilité post-hit (frames). 60 frames = 1 seconde. */
    private static final int INVINCIBLE_DURATION = 60;

    /** Compteur de cooldown avant prochain tir. */
    private int attackCooldown = 0;

    // -------------------------------------------------------------------------
    // Référence
    // -------------------------------------------------------------------------

    /** Référence au GamePanel (pour dimensions d'écran et police). */
    private GamePanel gp;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée le joueur et le positionne au centre de l'écran.
     *
     * @param gp Référence au GamePanel
     */
    public Player(GamePanel gp) {
        this.gp   = gp;
        this.size = gp.tileSize; // = 32px (16 à— scale 2)
        reset();
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    /**
     * Réinitialise le joueur au centre de l'écran avec la vie pleine.
     * Appelé par GamePanel.resetGame() au démarrage d'une partie.
     */
    public void reset() {
        x = gp.screenWidth  / 2.0;
        y = gp.screenHeight / 2.0;
        hp = maxHp;
        alive = true;
        invincibleTimer = 0;
        attackCooldown  = 0;
        aimAngle = -Math.PI / 2; // pointe vers le haut par défaut
    }

    // -------------------------------------------------------------------------
    // Mise à  jour
    // -------------------------------------------------------------------------

    /**
     * Met à  jour le joueur pour un frame :
     *  - Déplacement selon les touches
     *  - Clamp dans les bords de l'écran
     *  - Attaque automatique sur l'ennemi le plus proche
     *  - Vérification des collisions avec les projectiles ennemis
     *
     * @param keyH        à‰tat du clavier
     * @param enemies     Liste des ennemis (pour ciblage)
     * @param projectiles Liste partagée des projectiles
     */
    public void update(KeyHandler keyH, List<Enemy> enemies, List<Projectile> projectiles) {
        // --- Déplacement ---
        if (keyH.upPressed)    y -= 5;
        if (keyH.downPressed)  y += 5;
        if (keyH.leftPressed)  x -= 5;
        if (keyH.rightPressed) x += 5;

        // Empêche de sortir de l'écran
        x = Math.max(size / 2.0, Math.min(gp.screenWidth  - size / 2.0, x));
        y = Math.max(size / 2.0, Math.min(gp.screenHeight - size / 2.0, y));

        // --- Attaque automatique ---
        if (attackCooldown > 0) attackCooldown--;

        if (attackCooldown <= 0) {
            Enemy target = findClosestEnemy(enemies);
            if (target != null) {
                // Met à  jour l'angle visuel vers la cible
                aimAngle = Math.atan2(target.y - y, target.x - x);
                // Crée le projectile
                projectiles.add(new Projectile(x, y, target.x, target.y, damage, true));
                attackCooldown = attackRate;
            }
        }

        // --- Invincibilité post-hit ---
        if (invincibleTimer > 0) invincibleTimer--;

        // --- Collision avec projectiles ennemis ---
        for (Projectile p : projectiles) {
            if (!p.fromPlayer && p.alive && p.getBounds().intersects(getBounds())) {
                takeDamage(p.damage);
                p.alive = false; // consomme le projectile
            }
        }

        // Vérification de mort
        if (hp <= 0) alive = false;
    }

    /**
     * Cherche l'ennemi le plus proche dans le rayon {@link #attackRange}.
     *
     * @param enemies Liste des ennemis actuels
     * @return L'ennemi le plus proche, ou null si aucun dans la portée
     */
    private Enemy findClosestEnemy(List<Enemy> enemies) {
        Enemy closest = null;
        double minDist = attackRange;

        for (Enemy e : enemies) {
            if (!e.alive) continue;
            double dist = Math.sqrt(Math.pow(e.x - x, 2) + Math.pow(e.y - y, 2));
            if (dist < minDist) {
                minDist  = dist;
                closest  = e;
            }
        }
        return closest;
    }

    // -------------------------------------------------------------------------
    // Combat
    // -------------------------------------------------------------------------

    /**
     * Inflige des dégats au joueur.
     * Ignoré pendant la fenêtre d'invincibilité post-hit.
     *
     * @param dmg Dégats à  infliger
     */
    public void takeDamage(int dmg) {
        if (invincibleTimer > 0) return;
        hp -= dmg;
        hp = Math.max(0, hp); // ne descend pas en dessous de 0
        invincibleTimer = INVINCIBLE_DURATION;
        if (hp <= 0) alive = false;
    }

    // -------------------------------------------------------------------------
    // Collision
    // -------------------------------------------------------------------------

    /**
     * Retourne le rectangle de collision du joueur (carré centré sur sa position).
     */
    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /**
     * Dessine le joueur :
     *  - Cercle de portée (optionnel, toggle dans les options)
     *  - Flèche pointant vers la cible
     *  - HUD barre de vie
     */
    public void draw(Graphics2D g2) {
        // --- Cercle de portée (si activé dans les options) ---
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

        // --- Corps : flèche pointant vers aimAngle ---
        boolean isInvincible = (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0);

        if (isInvincible) {
            // Clignotement : semi-transparent pendant les frames paires
            g2.setColor(new Color(255, 255, 255, 150));
        } else {
            g2.setColor(new Color(80, 200, 120)); // vert joueur
        }

        Polygon arrow = buildArrow(aimAngle);
        g2.fillPolygon(arrow);

        // Contour sombre
        g2.setColor(new Color(30, 100, 60));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(arrow);
        g2.setStroke(new BasicStroke(1f));

        // HUD
        drawHUD(g2);
    }

    /**
     * Construit le polygone "flèche" du joueur, orienté selon l'angle de visée.
     *
     * La flèche est une pointe + deux ailettes arrière.
     *
     * @param angle Angle de visée en radians
     * @return Polygone prêt à  l'affichage
     */
    private Polygon buildArrow(double angle) {
        int h = size / 2; // demi-taille

        // Points dans l'espace local (non rotatés) :
        //  Pointe avant, aile gauche arrière, creux arrière, aile droite arrière
        int[] xLocal = {  h,   -h/2,   -h/4,   -h/2 };
        int[] yLocal = {  0,   -h/2,    0,       h/2 };

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
     * Dessine la barre de vie en haut à  gauche de l'écran (HUD).
     * La couleur change avec le niveau de vie restant :
     *  > 50% â†’ vert | 25-50% â†’ jaune | < 25% â†’ rouge
     */
    private void drawHUD(Graphics2D g2) {
        int barWidth  = 200;
        int barHeight = 18;
        int barX = 10;
        int barY = 10;

        // Fond sombre
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Portion de vie
        int currentWidth = (int)(barWidth * ((double) hp / maxHp));
        double ratio = (double) hp / maxHp;
        Color hpColor = (ratio > 0.5) ? new Color(50, 200, 80)
                      : (ratio > 0.25) ? new Color(220, 180, 0)
                                       : new Color(220, 50, 50);
        g2.setColor(hpColor);
        g2.fillRect(barX, barY, currentWidth, barHeight);

        // Contour
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.setStroke(new BasicStroke(1f));

        // Texte HP
        Font hudFont = gp.gameFont != null ? gp.gameFont.deriveFont(13f)
                                           : new Font("Arial", Font.PLAIN, 13);
        g2.setFont(hudFont);
        g2.setColor(Color.white);
        g2.drawString("HP : " + hp + " / " + maxHp, barX + 5, barY + 13);
    }
}
