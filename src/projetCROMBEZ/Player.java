package projetCROMBEZ;

import java.awt.*;
import java.util.List;

public class Player {

    public double x, y;
    public int speed;
    public int size;

    // Stats
    public int maxHp = 100;
    public int hp = 100;
    public int damage = 15;
    public int attackRange = 200; // rayon d'attaque auto
    public int attackRate = 60;   // frames entre chaque tir (1/seconde)
    private int attackCooldown = 0;

    // Invincibilité aprés un hit
    private int invincibleTimer = 0;
    private static final int INVINCIBLE_DURATION = 60;

    public boolean alive = true;

    private GamePanel gp;

    public Player(GamePanel gp) {
        this.gp = gp;
        this.x = gp.screenWidth / 2.0;
        this.y = gp.screenHeight / 2.0;
        this.speed = 5;
        this.size = gp.tileSize;
    }

    public void reset() {
        x = gp.screenWidth / 2.0;
        y = gp.screenHeight / 2.0;
        hp = maxHp;
        alive = true;
        invincibleTimer = 0;
        attackCooldown = 0;
    }

    public void update(KeyHandler keyH, List<Enemy> enemies, List<Projectile> projectiles) {
        // Mouvement
        if (keyH.upPressed)    y -= speed;
        if (keyH.downPressed)  y += speed;
        if (keyH.leftPressed)  x -= speed;
        if (keyH.rightPressed) x += speed;

        // Limites écran
        x = Math.max(size / 2.0, Math.min(gp.screenWidth - size / 2.0, x));
        y = Math.max(size / 2.0, Math.min(gp.screenHeight - size / 2.0, y));

        // Attaque automatique
        if (attackCooldown > 0) attackCooldown--;
        if (attackCooldown <= 0) {
            Enemy target = findClosestEnemy(enemies);
            if (target != null) {
                projectiles.add(new Projectile(x, y, target.x, target.y, damage, true));
                attackCooldown = attackRate;
            }
        }

        // Invincibilité
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

    private Enemy findClosestEnemy(List<Enemy> enemies) {
        Enemy closest = null;
        double minDist = attackRange;

        for (Enemy e : enemies) {
            double dist = Math.sqrt(Math.pow(e.x - x, 2) + Math.pow(e.y - y, 2));
            if (dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }
        return closest;
    }

    public void takeDamage(int dmg) {
        if (invincibleTimer > 0) return;
        hp -= dmg;
        invincibleTimer = INVINCIBLE_DURATION;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }

    public void draw(Graphics2D g2) {
        // Cercle de portée (optionnel, peut être retiré)
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillOval((int) x - attackRange, (int) y - attackRange, attackRange * 2, attackRange * 2);

        // Corps du joueur (clignotement si invincible)
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) {
            g2.setColor(new Color(255, 255, 255, 150));
        } else {
            g2.setColor(new Color(80, 200, 120));
        }
        g2.fillRect((int) x - size / 2, (int) y - size / 2, size, size);

        // Bordure
        g2.setColor(new Color(40, 120, 70));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect((int) x - size / 2, (int) y - size / 2, size, size);
        g2.setStroke(new BasicStroke(1));

        drawHUD(g2);
    }

    private void drawHUD(Graphics2D g2) {
        // Barre de vie en haut à  gauche
        int barWidth = 200;
        int barHeight = 18;
        int barX = 10;
        int barY = 10;

        g2.setColor(new Color(60, 60, 60));
        g2.fillRect(barX, barY, barWidth, barHeight);

        int currentWidth = (int) (barWidth * ((double) hp / maxHp));
        Color hpColor = (hp > maxHp * 0.5) ? new Color(50, 200, 80) :
                        (hp > maxHp * 0.25) ? new Color(220, 180, 0) : new Color(220, 50, 50);
        g2.setColor(hpColor);
        g2.fillRect(barX, barY, currentWidth, barHeight);

        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(gp.gameFont != null ? gp.gameFont.deriveFont(13f) : new Font("Arial", Font.PLAIN, 13));
        g2.setColor(Color.white);
        g2.drawString("HP : " + hp + " / " + maxHp, barX + 5, barY + 13);
    }
}
