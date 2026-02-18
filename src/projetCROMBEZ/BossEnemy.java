package projetCROMBEZ;

import java.awt.*;
import java.util.List;

public class BossEnemy extends Enemy {

    private int attackCooldown = 0;
    private int shootCooldown = 0;
    private int phase = 1; // Phase 1 > 50% HP, Phase 2 <= 50%
    private int chargeTimer = 0;
    private boolean charging = false;
    private double chargeDX, chargeDY;
    private static final int CHARGE_DURATION = 25;

    public BossEnemy(double spawnX, double spawnY) {
        super(spawnX, spawnY, 1000, 25, 1.2, 70, new Color(180, 30, 30));
        this.hpBarColor = new Color(255, 80, 0);
    }

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        // Changement de phase à 50% HP
        if (hp <= maxHp / 2) phase = 2;

        if (charging) {
            x += chargeDX;
            y += chargeDY;
            chargeTimer--;
            if (chargeTimer <= 0) charging = false;

            if (getBounds().intersects(player.getBounds())) {
                player.takeDamage(damage * 2);
            }
        } else {
            moveToward(player.x, player.y);
        }

        // Attaque mêlée
        if (attackCooldown > 0) attackCooldown--;
        if (getBounds().intersects(player.getBounds()) && attackCooldown <= 0) {
            player.takeDamage(damage);
            attackCooldown = 60;
        }

        // Tir de projectiles
        if (shootCooldown > 0) shootCooldown--;
        if (shootCooldown <= 0) {
            int numShots = (phase == 1) ? 4 : 8;
            shootRadial(projectiles, numShots);
            shootCooldown = (phase == 1) ? 120 : 80;
        }

        // Charge (Phase 2 uniquement)
        if (phase == 2 && !charging && attackCooldown <= 0) {
            triggerCharge(player);
        }
    }

    private void shootRadial(List<Projectile> projectiles, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI / count * i;
            double tx = x + Math.cos(angle) * 200;
            double ty = y + Math.sin(angle) * 200;
            projectiles.add(new Projectile(x, y, tx, ty, 12, false));
        }
    }

    private void triggerCharge(Player player) {
        double dist = Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));
        if (dist > 0) {
            chargeDX = (player.x - x) / dist * 8;
            chargeDY = (player.y - y) / dist * 8;
        }
        charging = true;
        chargeTimer = CHARGE_DURATION;
        attackCooldown = 180;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Aura du boss
        Color aura = (phase == 1) ? new Color(200, 50, 0, 60) : new Color(255, 0, 0, 80);
        g2.setColor(aura);
        g2.fillOval((int) x - size / 2 - 15, (int) y - size / 2 - 15, size + 30, size + 30);

        // Corps
        if (invincibleTimer > 0) {
            invincibleTimer--;
            g2.setColor(Color.white);
        } else {
            g2.setColor(charging ? new Color(255, 100, 0) : color);
        }
        g2.fillRect((int) x - size / 2, (int) y - size / 2, size, size);

        // Bordure
        g2.setColor(new Color(100, 0, 0));
        g2.setStroke(new BasicStroke(3));
        g2.drawRect((int) x - size / 2, (int) y - size / 2, size, size);
        g2.setStroke(new BasicStroke(1));

        // Label BOSS
        g2.setColor(Color.red);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        String label = "BOSS" + (phase == 2 ? " !!!" : "");
        g2.drawString(label, (int) x - fm.stringWidth(label) / 2, (int) y - size / 2 - 14);

        drawHpBar(g2);
    }

    @Override
    protected void drawHpBar(Graphics2D g2) {
        // Barre de vie grande en bas de l'écran pour le boss
        int barWidth = 400;
        int barHeight = 20;
        int barX = 408; // centré à 1216/2
        int barY = 790;

        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(barX, barY, barWidth, barHeight);

        int currentWidth = (int) (barWidth * ((double) hp / maxHp));
        GradientPaint gp2 = new GradientPaint(barX, barY, new Color(255, 80, 0),
                barX + barWidth, barY, new Color(200, 0, 0));
        g2.setPaint(gp2);
        g2.fillRect(barX, barY, currentWidth, barHeight);

        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.white);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "BOSS  " + hp + " / " + maxHp;
        g2.drawString(txt, barX + barWidth / 2 - fm.stringWidth(txt) / 2, barY + 14);
    }
}
