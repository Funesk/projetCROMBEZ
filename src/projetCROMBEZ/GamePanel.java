package projetCROMBEZ;

import java.awt.*;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    // Paramètres fenêtre
    int originalTileSize = 16;
    int scale = 2;
    int tileSize = originalTileSize * scale;
    int screenCol = 38;
    int screenRow = 26;
    int screenWidth = tileSize * screenCol;   // 1216px
    int screenHeight = tileSize * screenRow;  // 832px
    int FPS = 60;
    int currentFPS = 0;

    Font gameFont;

    Thread gameThread;
    KeyHandler keyH = new KeyHandler();

    // Etat du jeu
    public GameState gameState = GameState.MENU;

    // Systèmes de jeu
    public Player player;
    public EnemyManager enemyManager;
    public List<Projectile> projectiles = new ArrayList<>();

    // Menus
    MenuScreen menuScreen;

    // Score / temps
    int score = 0;
    int survivalTicks = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.requestFocusInWindow();

        try {
            Font customFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    new File("font/BlueWinter.ttf")).deriveFont(15f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            this.gameFont = customFont;
        } catch (Exception e) {
            // Police de secours si non trouvée
        }

        // Init des systèmes (avant menuScreen pour que les listeners existent)
        player = new Player(this);
        enemyManager = new EnemyManager(this);
        menuScreen = new MenuScreen(this);
    }

    public void resetGame() {
        player.reset();
        enemyManager.reset();
        projectiles.clear();
        score = 0;
        survivalTicks = 0;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (timer >= 1_000_000_000) {
                currentFPS = drawCount;
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        if (gameState == GameState.PLAYING) {
            survivalTicks++;

            // Update projectiles
            projectiles.removeIf(p -> !p.alive);
            for (Projectile p : projectiles) {
                p.update(screenWidth, screenHeight);
            }

            // Update ennemis
            enemyManager.update(player, projectiles);

            // Collision projectile joueur â†’ ennemi
            for (Projectile p : projectiles) {
                if (p.fromPlayer && p.alive) {
                    for (Enemy e : enemyManager.enemies) {
                        if (e.alive && p.getBounds().intersects(e.getBounds())) {
                            e.takeDamage(p.damage);
                            p.alive = false;
                            if (!e.alive) score += 10;
                            break;
                        }
                    }
                }
            }

            // Update joueur
            player.update(keyH, enemyManager.enemies, projectiles);

            // Fin de partie
            if (!player.alive) {
                gameState = GameState.GAME_OVER;
            }
            if (enemyManager.bossDefeated) {
                gameState = GameState.VICTORY;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case MENU:
                menuScreen.draw(g2);
                break;

            case PLAYING:
                drawGame(g2);
                break;

            case GAME_OVER:
                drawGame(g2);
                drawOverlay(g2, "GAME OVER", new Color(180, 30, 30));
                break;

            case VICTORY:
                drawGame(g2);
                drawOverlay(g2, "VICTOIRE !", new Color(50, 180, 80));
                break;
        }

        // FPS toujours visible
        g2.setColor(Color.yellow);
        g2.setFont(gameFont != null ? gameFont.deriveFont(13f) : new Font("Arial", Font.PLAIN, 13));
        g2.drawString("FPS : " + currentFPS, screenWidth - 80, 20);

        g2.dispose();
    }

    private void drawGame(Graphics2D g2) {
        // Fond
        g2.setColor(new Color(20, 20, 35));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Grille de fond (esthÃ©tique)
        g2.setColor(new Color(30, 30, 50));
        for (int i = 0; i < screenWidth; i += tileSize) {
            g2.drawLine(i, 0, i, screenHeight);
        }
        for (int j = 0; j < screenHeight; j += tileSize) {
            g2.drawLine(0, j, screenWidth, j);
        }

        // Projectiles
        for (Projectile p : projectiles) {
            p.draw(g2);
        }

        // Ennemis
        enemyManager.draw(g2);

        // Joueur
        player.draw(g2);

        // Score & temps
        int seconds = survivalTicks / FPS;
        g2.setFont(gameFont != null ? gameFont.deriveFont(14f) : new Font("Arial", Font.PLAIN, 14));
        g2.setColor(Color.white);
        g2.drawString("Score : " + score, 10, 45);
        g2.drawString("Temps : " + (seconds / 60) + "m " + (seconds % 60) + "s", 10, 65);
    }

    private void drawOverlay(Graphics2D g2, String title, Color titleColor) {
        // Fond semi-transparent
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Titre
        g2.setFont(gameFont != null ? gameFont.deriveFont(Font.BOLD, 56f) : new Font("Arial", Font.BOLD, 56));
        FontMetrics fm = g2.getFontMetrics();
        int cx = screenWidth / 2 - fm.stringWidth(title) / 2;
        int cy = screenHeight / 2 - 60;
        g2.setColor(titleColor.darker());
        g2.drawString(title, cx + 3, cy + 3);
        g2.setColor(titleColor);
        g2.drawString(title, cx, cy);

        // Infos
        g2.setFont(gameFont != null ? gameFont.deriveFont(20f) : new Font("Arial", Font.PLAIN, 20));
        fm = g2.getFontMetrics();
        int seconds = survivalTicks / FPS;
        String info = "Score : " + score + "   |   Temps : " + (seconds / 60) + "m " + (seconds % 60) + "s";
        g2.setColor(Color.white);
        g2.drawString(info, screenWidth / 2 - fm.stringWidth(info) / 2, cy + 60);

        // Instruction
        g2.setFont(gameFont != null ? gameFont.deriveFont(16f) : new Font("Arial", Font.PLAIN, 16));
        fm = g2.getFontMetrics();
        String hint = "Appuie sur ENTRÉE pour revenir au menu";
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(hint, screenWidth / 2 - fm.stringWidth(hint) / 2, cy + 100);

        // Retour menu via ENTRÉE
        if (keyH.enterPressed) {
            gameState = GameState.MENU;
        }
    }
}
