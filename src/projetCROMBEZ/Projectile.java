package projetCROMBEZ;

import java.awt.*;

public class Projectile {

    public double x, y;
    public double dx, dy;
    public int damage;
    public int size;
    public boolean fromPlayer; // true = joueur, false = ennemi
    public boolean alive = true;
    private Color color;

    public Projectile(double x, double y, double targetX, double targetY, int damage, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
        this.size = fromPlayer ? 8 : 10;
        this.color = fromPlayer ? new Color(255, 220, 0) : new Color(255, 60, 60);

        double speed = fromPlayer ? 10 : 5;
        double dist = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
        if (dist > 0) {
            dx = (targetX - x) / dist * speed;
            dy = (targetY - y) / dist * speed;
        }
    }

    public void update(int screenWidth, int screenHeight) {
        x += dx;
        y += dy;
        // Hors écran = mort
        if (x < -20 || x > screenWidth + 20 || y < -20 || y > screenHeight + 20) {
            alive = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }

    public void draw(Graphics2D g2) {
        // Halo
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2.fillOval((int) x - size, (int) y - size, size * 2, size * 2);
        // Corps
        g2.setColor(color);
        g2.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
    }
}
