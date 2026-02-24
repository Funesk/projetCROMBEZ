package projetCROMBEZ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Panneau de jeu principal - coeur de l'application.
 *
 * Gere :
 *  - La boucle de jeu (delta-time, 60 FPS)
 *  - Les transitions entre etats (GameState)
 *  - Le dispatch des evenements souris vers les ecrans
 *  - Les records (meilleur score, meilleur temps)
 *  - La sauvegarde en fin de partie et a la fermeture
 *  - Le flag hasActiveGame pour le bouton "Reprendre" du menu
 */
public class GamePanel extends JPanel implements Runnable {

    // =========================================================================
    // Parametres de la fenetre
    // =========================================================================

    int originalTileSize = 16;
    int scale            = 2;
    int tileSize         = originalTileSize * scale;
    int screenCol        = 38;
    int screenRow        = 26;
    int screenWidth      = tileSize * screenCol;  // 1216 px
    int screenHeight     = tileSize * screenRow;  // 832 px
    int FPS              = 60;
    int currentFPS       = 0;

    // =========================================================================
    // Police
    // =========================================================================

    /** Police personnalisee BlueWinter (null si font/BlueWinter.ttf introuvable). */
    Font gameFont;

    // =========================================================================
    // Systemes d'entree
    // =========================================================================

    Thread     gameThread;
    KeyHandler keyH = new KeyHandler();

    // =========================================================================
    // Etat du jeu
    // =========================================================================

    public GameState gameState = GameState.MENU;

    /**
     * true si une partie est actuellement en pause et peut etre reprise.
     * Mis a true quand on va au menu depuis la pause.
     * Mis a false quand une nouvelle partie demarre ou quand la partie se termine.
     */
    public boolean hasActiveGame = false;

    // =========================================================================
    // Systemes de jeu
    // =========================================================================

    public Player           player;
    public EnemyManager     enemyManager;
    public List<Projectile> projectiles = new ArrayList<>();

    // =========================================================================
    // Ecrans
    // =========================================================================

    MenuScreen       menuScreen;
    DifficultyScreen difficultyScreen;
    PauseScreen      pauseScreen;
    public OptionsScreen optionsScreen;

    // =========================================================================
    // Score et temps
    // =========================================================================

    /** Score de la partie en cours. */
    int score = 0;

    /** Frames ecoulees depuis le debut de la partie en cours. */
    int survivalTicks = 0;

    // =========================================================================
    // Records (persistes via SaveManager)
    // =========================================================================

    /** Meilleur score toutes parties confondues. */
    public int bestScore = 0;

    /** Meilleur temps de survie en frames toutes parties confondues. */
    public int bestTimeTicks = 0;

    // =========================================================================
    // Constructeur
    // =========================================================================

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.requestFocusInWindow();

        addMouseAdapter();

        // Chargement de la police BlueWinter
        try {
            Font customFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    new File("font/BlueWinter.ttf")).deriveFont(15f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
            this.gameFont = customFont;
        } catch (Exception e) {
            // Secours : Arial partout si la police est introuvable
            System.out.println("[GamePanel] Police BlueWinter introuvable, utilisation d'Arial.");
        }

        // Systemes de jeu
        player       = new Player(this);
        enemyManager = new EnemyManager(this);

        // Ecrans
        menuScreen       = new MenuScreen(this);
        difficultyScreen = new DifficultyScreen(this);
        optionsScreen    = new OptionsScreen(this, GameState.MENU);
        pauseScreen      = new PauseScreen(this);

        // Sauvegarde de secours a la fermeture (croix rouge, Alt+F4, etc.)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SaveManager.save(this)));
    }

    // =========================================================================
    // Reset de partie
    // =========================================================================

    /**
     * Reinitialise les systemes de jeu pour une nouvelle partie.
     * Les stats du joueur et les records ne sont pas remis a zero.
     * hasActiveGame est mis a false car c'est une nouvelle partie.
     */
    public void resetGame() {
        player.reset();
        enemyManager.reset();
        projectiles.clear();
        score            = 0;
        survivalTicks    = 0;
        hasActiveGame    = false;
    }

    // =========================================================================
    // Demarrage
    // =========================================================================

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // =========================================================================
    // Boucle de jeu (delta-time)
    // =========================================================================

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta  = 0;
        long lastTime = System.nanoTime();
        long timer    = 0;
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
                drawCount  = 0;
                timer      = 0;
            }
        }
    }

    // =========================================================================
    // Mise a jour
    // =========================================================================

    public void update() {
        if (gameState == GameState.PLAYING) {
            updateGame();
        }

        // Decremente le timer du feedback "Sauvegarde !" dans PauseScreen
        if (gameState == GameState.PAUSED) {
            pauseScreen.tick();
        }

        // ECHAP : bascule pause / reprise
        if (keyH.escapeJustPressed) {
            keyH.escapeJustPressed = false;
            if (gameState == GameState.PLAYING) {
                pauseScreen.reset();
                gameState = GameState.PAUSED;
            } else if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
            }
        }

        // ENTREE sur ecrans de fin : retour au menu
        if (keyH.enterPressed &&
                (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY)) {
            gameState = GameState.MENU;
        }
    }

    /**
     * Logique de jeu (uniquement quand PLAYING).
     */
    private void updateGame() {
        survivalTicks++;

        projectiles.removeIf(p -> !p.alive);
        for (Projectile p : projectiles) p.update(screenWidth, screenHeight);

        enemyManager.update(player, projectiles);

        // Collisions projectiles joueur -> ennemis
        for (Projectile p : projectiles) {
            if (!p.fromPlayer || !p.alive) continue;
            for (Enemy e : enemyManager.enemies) {
                if (!e.alive) continue;
                if (p.getBounds().intersects(e.getBounds())) {
                    e.takeDamage(p.damage);
                    p.alive = false;
                    if (!e.alive) score += 10;
                    break;
                }
            }
        }

        player.update(keyH, enemyManager.enemies, projectiles);

        if (!player.alive)             endGame(GameState.GAME_OVER);
        if (enemyManager.bossDefeated) endGame(GameState.VICTORY);
    }

    /**
     * Termine la partie, met a jour les records et sauvegarde.
     *
     * @param newState GAME_OVER ou VICTORY
     */
    private void endGame(GameState newState) {
        boolean newRecord = false;
        if (score > bestScore)           { bestScore     = score;         newRecord = true; }
        if (survivalTicks > bestTimeTicks) { bestTimeTicks = survivalTicks; newRecord = true; }

        // La partie est terminee : ne peut plus etre reprise
        hasActiveGame = false;

        SaveManager.save(this);

        if (newRecord) {
            System.out.println("[GamePanel] Nouveau record ! Score=" + bestScore
                    + " Temps=" + (bestTimeTicks / FPS) + "s");
        }

        gameState = newState;
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case MENU:       menuScreen.draw(g2);       break;
            case DIFFICULTY: difficultyScreen.draw(g2); break;
            case OPTIONS:    optionsScreen.draw(g2);    break;
            case PLAYING:    drawGame(g2);              break;
            case PAUSED:
                drawGame(g2);
                pauseScreen.draw(g2);
                break;
            case GAME_OVER:
                drawGame(g2);
                drawEndOverlay(g2, "GAME OVER", new Color(180, 30, 30));
                break;
            case VICTORY:
                drawGame(g2);
                drawEndOverlay(g2, "VICTOIRE !", new Color(50, 180, 80));
                break;
        }

        // FPS (hors menus de navigation)
        if (gameState != GameState.MENU && gameState != GameState.DIFFICULTY) {
            g2.setColor(Color.yellow);
            g2.setFont(gameFont != null ? gameFont.deriveFont(13f) : new Font("Arial", Font.PLAIN, 13));
            g2.drawString("FPS : " + currentFPS, screenWidth - 80, 20);
        }

        g2.dispose();
    }

    /** Dessine la scene de jeu (fond, grille, entites, HUD). */
    private void drawGame(Graphics2D g2) {
        g2.setColor(new Color(20, 20, 35));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(new Color(30, 30, 50));
        for (int i = 0; i < screenWidth;  i += tileSize) g2.drawLine(i, 0, i, screenHeight);
        for (int j = 0; j < screenHeight; j += tileSize) g2.drawLine(0, j, screenWidth, j);

        for (Projectile p : projectiles) p.draw(g2);
        enemyManager.draw(g2);
        player.draw(g2);

        Font scoreFont = gameFont != null ? gameFont.deriveFont(14f) : new Font("Arial", Font.PLAIN, 14);
        g2.setFont(scoreFont);
        g2.setColor(Color.white);
        g2.drawString("Score : " + score,                           10, 45);
        g2.drawString("Temps : " + formatTime(survivalTicks / FPS), 10, 65);

        g2.setColor(new Color(200, 200, 80));
        g2.drawString("Record : " + bestScore,                           10, 85);
        g2.drawString("Meilleur temps : " + formatTime(bestTimeTicks / FPS), 10, 105);

        Font diffFont = gameFont != null ? gameFont.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(diffFont);
        g2.setColor(getDifficultyColor());
        g2.drawString(GameSettings.getInstance().getDifficulty().getLabel(), screenWidth - 75, 40);
    }

    /** Dessine l'overlay de fin de partie. */
    private void drawEndOverlay(Graphics2D g2, String title, Color titleColor) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        Font titleFont = gameFont != null ? gameFont.deriveFont(Font.BOLD, 56f)
                                          : new Font("Arial", Font.BOLD, 56);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();
        int cx = screenWidth / 2 - fm.stringWidth(title) / 2;
        int cy = screenHeight / 2 - 80;

        g2.setColor(titleColor.darker().darker());
        g2.drawString(title, cx + 4, cy + 4);
        g2.setColor(titleColor);
        g2.drawString(title, cx, cy);

        Font statsFont = gameFont != null ? gameFont.deriveFont(20f) : new Font("Arial", Font.PLAIN, 20);
        g2.setFont(statsFont);
        fm = g2.getFontMetrics();
        String stats = "Score : " + score + "   |   Temps : " + formatTime(survivalTicks / FPS);
        g2.setColor(Color.white);
        g2.drawString(stats, screenWidth / 2 - fm.stringWidth(stats) / 2, cy + 55);

        Font recFont = gameFont != null ? gameFont.deriveFont(16f) : new Font("Arial", Font.PLAIN, 16);
        g2.setFont(recFont);
        fm = g2.getFontMetrics();
        String recLine = "Record score : " + bestScore
                + "   |   Record temps : " + formatTime(bestTimeTicks / FPS);
        g2.setColor(new Color(200, 200, 80));
        g2.drawString(recLine, screenWidth / 2 - fm.stringWidth(recLine) / 2, cy + 85);

        boolean newRecord = (score >= bestScore && score > 0)
                         || (survivalTicks >= bestTimeTicks && survivalTicks > 0);
        if (newRecord) {
            Font badgeFont = gameFont != null ? gameFont.deriveFont(Font.BOLD, 18f)
                                             : new Font("Arial", Font.BOLD, 18);
            g2.setFont(badgeFont);
            fm = g2.getFontMetrics();
            String badge = "*** NOUVEAU RECORD ! ***";
            g2.setColor(new Color(255, 220, 50));
            g2.drawString(badge, screenWidth / 2 - fm.stringWidth(badge) / 2, cy + 112);
        }

        Font hintFont = gameFont != null ? gameFont.deriveFont(15f) : new Font("Arial", Font.PLAIN, 15);
        g2.setFont(hintFont);
        fm = g2.getFontMetrics();
        String hint = "Appuie sur ENTREE pour revenir au menu";
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(hint, screenWidth / 2 - fm.stringWidth(hint) / 2, cy + 140);
    }

    // =========================================================================
    // Dispatcher souris central
    // =========================================================================

    private void addMouseAdapter() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                GameState stateNow = gameState;
                Point     p        = e.getPoint();
                switch (stateNow) {
                    case MENU:       menuScreen.handleClick(p);       break;
                    case DIFFICULTY: difficultyScreen.handleClick(p); break;
                    case OPTIONS:    optionsScreen.handleClick(p);    break;
                    case PAUSED:     pauseScreen.handleClick(p);      break;
                    default: break;
                }
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                GameState stateNow = gameState;
                Point     p        = e.getPoint();
                switch (stateNow) {
                    case MENU:       menuScreen.handleHover(p);       break;
                    case DIFFICULTY: difficultyScreen.handleHover(p); break;
                    case OPTIONS:    optionsScreen.handleHover(p);    break;
                    case PAUSED:     pauseScreen.handleHover(p);      break;
                    default: break;
                }
            }
        });
    }

    // =========================================================================
    // Utilitaires
    // =========================================================================

    private String formatTime(int totalSeconds) {
        return (totalSeconds / 60) + "m " + (totalSeconds % 60) + "s";
    }

    private Color getDifficultyColor() {
        switch (GameSettings.getInstance().getDifficulty()) {
            case EASY:  return new Color(80, 200, 80);
            case HARD:  return new Color(220, 60, 60);
            default:    return new Color(220, 180, 0);
        }
    }
}
