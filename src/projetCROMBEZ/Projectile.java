package projetCROMBEZ;

import java.awt.*;

/**
 * Projectile en deplacement rectiligne vers une cible.
 * fromPlayer=true -> tire par le joueur (frappe les ennemis).
 * fromPlayer=false -> tire par un ennemi (frappe le joueur).
 */
public class Projectile {

    public double  x, y, dx, dy;
    public int     damage, size;
    public boolean fromPlayer;
    public boolean alive  = true;
    /** true si ce projectile est un coup critique. */
    public boolean isCrit = false;
    private Color  color;

    /**
     * @param x,y       Position de depart
     * @param targetX,Y Position cible (calcul de direction)
     * @param damage    Degats a l'impact
     * @param fromPlayer true si tir du joueur
     */
    public Projectile(double x, double y, double targetX, double targetY,
                      int damage, boolean fromPlayer) {
        this.x = x; this.y = y;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
        this.size  = fromPlayer ? 8 : 10;
        this.color = fromPlayer ? new Color(255, 220, 0) : new Color(255, 60, 60);

        double speed = fromPlayer ? 10 : 5;
        double dist  = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
        if (dist > 0) { dx = (targetX - x) / dist * speed; dy = (targetY - y) / dist * speed; }
    }

    /** Deplace le projectile et le tue s'il sort de l'ecran. */
    public void update(int sw, int sh) {
        x += dx; y += dy;
        if (x < -20 || x > sw + 20 || y < -20 || y > sh + 20) alive = false;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x - size/2, (int)y - size/2, size, size);
    }

    public void draw(Graphics2D g2) {
        Color displayColor = (isCrit && fromPlayer) ? new Color(238,130,238) : color;
        g2.setColor(new Color(displayColor.getRed(), displayColor.getGreen(), displayColor.getBlue(), 80));
        g2.fillOval((int)x - size, (int)y - size, size*2, size*2);
        g2.setColor(displayColor);
        g2.fillOval((int)x - size/2, (int)y - size/2, size, size);
    }
}
