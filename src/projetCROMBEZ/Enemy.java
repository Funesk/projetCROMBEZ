package projetCROMBEZ;

import java.awt.*;
import java.util.List;

public abstract class Enemy {

    public double x, y;
    public int maxHp;
    public int hp;
    public int damage;
    public double speed;
    public int size;
    public boolean alive = true;
    protected Color color;
    protected Color hpBarColor;

    // Invincibilité brève après un hit (flash)
    protected int invincibleTimer = 0;
    protected static final int INVINCIBLE_DURATION = 10;

    public Enemy(double x, double y, int maxHp, int damage, double speed, int size, Color color) {
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.damage = damage;
        this.speed = speed;
        this.size = size;
        this.color = color;
        this.hpBarColor = new Color(220, 50, 50);
    }

    public abstract void update(Player player, List<Projectile> projectiles);

    public void takeDamage(int dmg) {
        if (invincibleTimer > 0) return;
        hp -= dmg;
        invincibleTimer = INVINCIBLE_DURATION;
        if (hp <= 0) {
            alive = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }

    // Déplace l'ennemi vers le joueur
    protected void moveToward(double targetX, double targetY) {
        double dist = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
        if (dist > 0) {
            x += (targetX - x) / dist * speed;
            y += (targetY - y) / dist * speed;
        }
    }

    public void draw(Graphics2D g2) {
        if (invincibleTimer > 0) {
            invincibleTimer--;
            g2.setColor(Color.white);
        } else {
            g2.setColor(color);
        }
        g2.fillRect((int) x - size / 2, (int) y - size / 2, size, size);

        drawHpBar(g2);
    }

    protected void drawHpBar(Graphics2D g2) {
        int barWidth = size;
        int barHeight = 5;
        int barX = (int) x - barWidth / 2;
        int barY = (int) y - size / 2 - 10;

        // Fond gris
        g2.setColor(new Color(60, 60, 60));
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Vie restante
        int currentWidth = (int) (barWidth * ((double) hp / maxHp));
        g2.setColor(hpBarColor);
        g2.fillRect(barX, barY, currentWidth, barHeight);

        // Bordure
        g2.setColor(Color.black);
        g2.drawRect(barX, barY, barWidth, barHeight);
    }
}
