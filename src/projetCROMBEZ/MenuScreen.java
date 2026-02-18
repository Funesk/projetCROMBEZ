package projetCROMBEZ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MenuScreen {

    private GamePanel gp;
    private List<Rectangle> buttons;
    private String[] buttonLabels = {"Jouer", "Options", "Crédits", "Quitter"};
    private int hoveredButton = -1;

    // Dimensions des boutons
    private int btnWidth = 220;
    private int btnHeight = 50;
    private int btnX;
    private int btnGap = 20;

    public MenuScreen(GamePanel gp) {
        this.gp = gp;
        buttons = new ArrayList<>();
        btnX = gp.screenWidth / 2 - btnWidth / 2;

        int startY = gp.screenHeight / 2 - 60;
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons.add(new Rectangle(btnX, startY + i * (btnHeight + btnGap), btnWidth, btnHeight));
        }

        // Listener souris pour hover et click
        gp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredButton = -1;
                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i).contains(e.getPoint())) {
                        hoveredButton = i;
                        break;
                    }
                }
            }
        });

        gp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i).contains(e.getPoint())) {
                        handleButton(i);
                        break;
                    }
                }
            }
        });
    }

    private void handleButton(int index) {
        switch (index) {
            case 0: // Jouer
                gp.gameState = GameState.PLAYING;
                gp.resetGame();
                break;
            case 1: // Options
                // TODO: Ã‰cran options
                break;
            case 2: // CrÃ©dits
                // TODO: Ã‰cran crÃ©dits
                break;
            case 3: // Quitter
                System.exit(0);
                break;
        }
    }

    public void draw(Graphics2D g2) {
        // Fond dégradé
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30),
                0, gp.screenHeight, new Color(30, 10, 60));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Titre du jeu
        g2.setFont(gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 52f) : new Font("Arial", Font.BOLD, 52));
        String title = "SURVIVOR";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = gp.screenWidth / 2 - fm.stringWidth(title) / 2;
        int titleY = gp.screenHeight / 2 - 160;

        // Ombre du titre
        g2.setColor(new Color(150, 0, 0));
        g2.drawString(title, titleX + 3, titleY + 3);
        // Titre principal
        g2.setColor(new Color(220, 50, 50));
        g2.drawString(title, titleX, titleY);

        // Sous-titre
        g2.setFont(gp.gameFont != null ? gp.gameFont.deriveFont(18f) : new Font("Arial", Font.PLAIN, 18));
        String sub = "Rogue-lite";
        fm = g2.getFontMetrics();
        g2.setColor(new Color(200, 150, 50));
        g2.drawString(sub, gp.screenWidth / 2 - fm.stringWidth(sub) / 2, titleY + 35);

        // Boutons
        g2.setFont(gp.gameFont != null ? gp.gameFont.deriveFont(20f) : new Font("Arial", Font.PLAIN, 20));
        fm = g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn = buttons.get(i);
            boolean hovered = (i == hoveredButton);

            // Fond bouton
            if (hovered) {
                g2.setColor(new Color(180, 40, 40));
            } else {
                g2.setColor(new Color(60, 20, 20));
            }
            g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            // Bordure bouton
            g2.setColor(hovered ? new Color(255, 100, 100) : new Color(120, 40, 40));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            // Texte
            g2.setColor(Color.white);
            int textX = btn.x + btn.width / 2 - fm.stringWidth(buttonLabels[i]) / 2;
            int textY = btn.y + btn.height / 2 + fm.getAscent() / 2 - 2;
            g2.drawString(buttonLabels[i], textX, textY);
        }

        // Version
        g2.setFont(gp.gameFont != null ? gp.gameFont.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("v0.1 - Alpha", 10, gp.screenHeight - 10);
    }
}
