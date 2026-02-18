package projetCROMBEZ;

import java.awt.*;
import java.util.List;

/**
 * Classe abstraite de base pour tous les ennemis du jeu.
 *
 * Définit les attributs communs (position, vie, dégats, vitesse)
 * et les comportements partagés (déplacement, prise de dégats,
 * barre de vie, flash d'invincibilité).
 *
 * Chaque type d'ennemi hérite de cette classe et implémente
 * sa propre logique via la méthode {@link #update(Player, List)}.
 *
 * Hiérarchie :
 *   Enemy
 *  MeleeEnemy  (triangle rouge   fonce sur le joueur)
 *  RangedEnemy (losange bleu     tire à  distance)
 *  TankEnemy   (hexagone violet  lent et résistant)
 *  BossEnemy   (étoile orange    boss final avec phases)
 */
public abstract class Enemy {

    // -------------------------------------------------------------------------
    // Position
    // -------------------------------------------------------------------------

    /** Position X du centre de l'ennemi (en pixels). */
    public double x;

    /** Position Y du centre de l'ennemi (en pixels). */
    public double y;

    // -------------------------------------------------------------------------
    // Stats de combat
    // -------------------------------------------------------------------------

    /** Points de vie maximum. Modifié lors de l'application de la difficulté. */
    public int maxHp;

    /** Points de vie actuels. */
    public int hp;

    /** Dégats infligés au joueur par attaque. */
    public int damage;

    /** Vitesse de déplacement (pixels par frame). */
    public double speed;

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /** Demi-taille utilisée pour le dessin et la collision. */
    public int size;

    /** Couleur principale de l'ennemi. */
    protected Color color;

    /** Couleur de la barre de vie (personnalisable par sous-classe). */
    protected Color hpBarColor;

    // -------------------------------------------------------------------------
    // à‰tat
    // -------------------------------------------------------------------------

    /** false dà¨s que l'ennemi doit être retiré de la liste. */
    public boolean alive = true;

    /**
     * Compteur d'invincibilité en frames.
     * Pendant ce laps de temps, l'ennemi ne peut pas prendre de nouveaux dégats.
     * Il clignote en blanc pour indiquer visuellement le hit.
     */
    protected int invincibleTimer = 0;

    /** Durée de l'invincibilité post-hit, en frames (â‰ˆ 10/60s). */
    protected static final int INVINCIBLE_DURATION = 10;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Initialise un ennemi avec ses attributs de base.
     *
     * @param x     Position de spawn X
     * @param y     Position de spawn Y
     * @param maxHp Points de vie maximum
     * @param damage Dégats par attaque
     * @param speed  Vitesse de déplacement (px/frame)
     * @param size   Demi-taille pour la hitbox et le dessin
     * @param color  Couleur principale
     */
    public Enemy(double x, double y, int maxHp, int damage, double speed, int size, Color color) {
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.damage = damage;
        this.speed = speed;
        this.size = size;
        this.color = color;
        this.hpBarColor = new Color(220, 50, 50); // rouge par défaut
    }

    // -------------------------------------------------------------------------
    // Méthode abstraite â€“ comportement propre à  chaque ennemi
    // -------------------------------------------------------------------------

    /**
     * Met à  jour l'IA de l'ennemi pour ce frame.
     *
     * @param player      Référence au joueur (pour ciblage/attaque)
     * @param projectiles Liste partagée des projectiles (pour que l'ennemi puisse en ajouter)
     */
    public abstract void update(Player player, List<Projectile> projectiles);

    // -------------------------------------------------------------------------
    // Combat
    // -------------------------------------------------------------------------

    /**
     * Inflige des dégats à  l'ennemi.
     * Ignoré si l'ennemi est encore en phase d'invincibilité post-hit.
     *
     * @param dmg Quantité de dégats à  infliger
     */
    public void takeDamage(int dmg) {
        if (invincibleTimer > 0) return; // invincible : dégats ignorés

        hp -= dmg;
        invincibleTimer = INVINCIBLE_DURATION; // active le flash blanc

        if (hp <= 0) {
            alive = false; // sera retiré par EnemyManager
        }
    }

    // -------------------------------------------------------------------------
    // Collision
    // -------------------------------------------------------------------------

    /**
     * Retourne le rectangle de collision de l'ennemi (carré centré sur sa position).
     */
    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }

    // -------------------------------------------------------------------------
    // Déplacement utilitaire
    // -------------------------------------------------------------------------

    /**
     * Déplace l'ennemi d'un pas en direction d'un point cible.
     * Utilise la normalisation du vecteur direction.
     *
     * @param targetX Coordonnée X de la cible
     * @param targetY Coordonnée Y de la cible
     */
    protected void moveToward(double targetX, double targetY) {
        double dist = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
        if (dist > 0) {
            x += (targetX - x) / dist * speed;
            y += (targetY - y) / dist * speed;
        }
    }

    // -------------------------------------------------------------------------
    // Rendu commun
    // -------------------------------------------------------------------------

    /**
     * Dessine l'ennemi (à surcharger dans les sous-classes pour une forme custom).
     * Version par défaut : carré + barre de vie.
     */
    public void draw(Graphics2D g2) {
        // Flash blanc si invincible, sinon couleur normale
        if (invincibleTimer > 0) {
            invincibleTimer--;
            g2.setColor(Color.white);
        } else {
            g2.setColor(color);
        }
        g2.fillRect((int) x - size / 2, (int) y - size / 2, size, size);

        drawHpBar(g2);
    }

    /**
     * Dessine la barre de vie au-dessus de l'ennemi.
     * La largeur de la barre reflète le ratio HP actuel / HP max.
     */
    protected void drawHpBar(Graphics2D g2) {
        int barWidth  = size;
        int barHeight = 5;
        int barX = (int) x - barWidth / 2;
        int barY = (int) y - size / 2 - 10;

        // Fond gris
        g2.setColor(new Color(60, 60, 60));
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Portion de vie restante
        int currentWidth = (int) (barWidth * ((double) hp / maxHp));
        g2.setColor(hpBarColor);
        g2.fillRect(barX, barY, currentWidth, barHeight);

        // Contour
        g2.setColor(Color.black);
        g2.drawRect(barX, barY, barWidth, barHeight);
    }

    // -------------------------------------------------------------------------
    // Utilitaire de rendu partagé
    // -------------------------------------------------------------------------

    /**
     * Détermine la couleur de dessin pour ce frame.
     * Retourne blanc si l'ennemi est en phase d'invincibilité (flash hit),
     * sinon retourne la couleur normale passée en paramètre.
     * Décrémente aussi le timer d'invincibilité.
     *
     * @param normal Couleur à  utiliser hors flash
     * @return       Couleur effective à  appliquer pour ce frame
     */
    protected Color getDrawColor(Color normal) {
        if (invincibleTimer > 0) {
            invincibleTimer--;
            return Color.white;
        }
        return normal;
    }
}
