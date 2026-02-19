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
 * Responsabilites :
 *  - Contenir la boucle de jeu (pattern game loop a delta-time).
 *  - Gerer les transitions entre les etats via GameState.
 *  - Deleguer la mise a jour et le rendu a chaque sous-systeme.
 *  - Reinitialiser la partie a la demande.
 *
 * --- Architecture evenements souris (IMPORTANT) ---
 * GamePanel possede UN SEUL MouseAdapter pour tous les clics et mouvements.
 * Ce dispatcher central lit gameState au moment ou l'evenement ENTRE dans
 * le handler, puis appelle handleClick() / handleHover() sur le bon ecran.
 *
 * POURQUOI c'est important :
 * Auparavant, chaque ecran enregistrait son propre MouseListener sur ce panel.
 * Quand un ecran (ex: OptionsScreen) changeait gameState dans son listener,
 * le listener suivant (ex: PauseScreen) voyait le NOUVEL etat et reagissait
 * par erreur. Exemple du bug corrige :
 *   1. Clic sur "Retour" dans OptionsScreen (gameState = OPTIONS)
 *   2. OptionsScreen.listener : OPTIONS -> change gameState a PAUSED
 *   3. PauseScreen.listener   : voit PAUSED, le clic coincide avec
 *      "Menu principal" -> change gameState a MENU (bug !)
 *
 * Avec le dispatcher central :
 *   - gameState est lu UNE SEULE FOIS au debut du handler.
 *   - Seul l'ecran correspondant a cet etat recoit l'evenement.
 *   - Les changements de gameState durant le traitement n'affectent pas
 *     le dispatch de ce meme evenement.
 */
public class GamePanel extends JPanel implements Runnable {

    // =========================================================================
    // Parametres de la fenetre
    // =========================================================================

    /** Taille originale d'une tuile avant mise a l'echelle (px). */
    int originalTileSize = 16;

    /** Facteur de mise a l'echelle. */
    int scale = 2;

    /** Taille reelle des tuiles en pixels (32px). */
    int tileSize = originalTileSize * scale;

    /** Nombre de tuiles sur l'axe horizontal. */
    int screenCol = 38;

    /** Nombre de tuiles sur l'axe vertical. */
    int screenRow = 26;

    /** Largeur de l'ecran de jeu en pixels (1216). */
    int screenWidth  = tileSize * screenCol;

    /** Hauteur de l'ecran de jeu en pixels (832). */
    int screenHeight = tileSize * screenRow;

    /** Frequence d'affichage cible (images par seconde). */
    int FPS = 60;

    /** FPS mesure en temps reel. */
    int currentFPS = 0;

    // =========================================================================
    // Police
    // =========================================================================

    /** Police personnalisee (null si "font/Cubic.ttf" est introuvable). */
    Font gameFont;

    // =========================================================================
    // Systemes d'entree
    // =========================================================================

    /** Thread de la boucle de jeu. */
    Thread gameThread;

    /** Gestionnaire des entrees clavier. */
    KeyHandler keyH = new KeyHandler();

    // =========================================================================
    // Etat du jeu
    // =========================================================================

    /** Etat actuel du jeu. */
    public GameState gameState = GameState.MENU;

    // =========================================================================
    // Systemes de jeu
    // =========================================================================

    /** Instance du joueur. */
    public Player player;

    /** Gestionnaire des ennemis et des vagues. */
    public EnemyManager enemyManager;

    /** Liste partagee de tous les projectiles actifs. */
    public List<Projectile> projectiles = new ArrayList<>();

    // =========================================================================
    // Ecrans / menus
    // =========================================================================

    /** Ecran du menu principal. */
    MenuScreen menuScreen;

    /** Ecran de selection de la difficulte. */
    DifficultyScreen difficultyScreen;

    /** Menu pause (overlay sur le jeu). */
    PauseScreen pauseScreen;

    /** Ecran des options. */
    public OptionsScreen optionsScreen;

    // =========================================================================
    // Score et temps
    // =========================================================================

    /** Score accumule (10 points par ennemi tue). */
    int score = 0;

    /** Frames ecoulees depuis le debut de la partie. */
    int survivalTicks = 0;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Initialise le panneau, charge la police et instancie tous les sous-systemes.
     *
     * Un seul MouseAdapter est enregistre sur ce panneau. Il dispatche
     * les evenements souris vers le bon ecran selon gameState.
     * Tous les ecrans (MenuScreen, PauseScreen...) exposent des methodes
     * handleClick() / handleHover() et N'enregistrent PLUS de listeners.
     */
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.requestFocusInWindow();

        // =====================================================================
        // DISPATCHER CENTRAL UNIQUE
        // Un seul listener pour toute la souris.
        // gameState est capture une fois par evenement, ce qui garantit
        // qu'un seul ecran recoit chaque evenement.
        // =====================================================================
        this.addMouseAdapter();

        // Police personnalisee (optionnel)
        try {
            Font customFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    new File("font/BlueWinter.ttf")).deriveFont(15f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
            this.gameFont = customFont;
        } catch (Exception e) {
            // Secours : Arial est utilise partout en fallback
        }

        // Systemes de jeu
        player       = new Player(this);
        enemyManager = new EnemyManager(this);

        // Ecrans (n'enregistrent plus de listeners)
        menuScreen       = new MenuScreen(this);
        difficultyScreen = new DifficultyScreen(this);
        optionsScreen    = new OptionsScreen(this, GameState.MENU);
        pauseScreen      = new PauseScreen(this);
    }

    /**
     * Cree et enregistre le MouseAdapter central unique.
     *
     * mousePressed : restaure le focus clavier (fait avant tout dispatch).
     * mouseReleased : dispatch vers le bon ecran selon gameState.
     * mouseMoved   : dispatch du hover vers le bon ecran.
     *
     * La methode est separee du constructeur pour la lisibilite.
     */
    private void addMouseAdapter() {
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                // Restaure le focus clavier a chaque clic, peu importe l'etat.
                // Indispensable car les menus captent les clics et font perdre
                // le focus AWT au GamePanel.
                requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // IMPORTANT : gameState est lu UNE SEULE FOIS ici.
                // Si un ecran change gameState dans handleClick(),
                // cela n'affecte pas le dispatch de CET evenement.
                GameState stateNow = gameState;
                Point     p        = e.getPoint();

                switch (stateNow) {
                    case MENU:
                        menuScreen.handleClick(p);
                        break;
                    case DIFFICULTY:
                        difficultyScreen.handleClick(p);
                        break;
                    case OPTIONS:
                        optionsScreen.handleClick(p);
                        break;
                    case PAUSED:
                        pauseScreen.handleClick(p);
                        break;
                    case PLAYING:
                    case GAME_OVER:
                    case VICTORY:
                        // Pas de gestion de clic dans ces etats
                        break;
                }
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Meme logique : dispatch selon l'etat courant.
                GameState stateNow = gameState;
                Point     p        = e.getPoint();

                switch (stateNow) {
                    case MENU:
                        menuScreen.handleHover(p);
                        break;
                    case DIFFICULTY:
                        difficultyScreen.handleHover(p);
                        break;
                    case OPTIONS:
                        optionsScreen.handleHover(p);
                        break;
                    case PAUSED:
                        pauseScreen.handleHover(p);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    // =========================================================================
    // Reset de partie
    // =========================================================================

    /**
     * Reinitialise tous les systemes de jeu pour une nouvelle partie.
     * Appele par DifficultyScreen juste avant de passer en PLAYING.
     */
    public void resetGame() {
        player.reset();
        enemyManager.reset();
        projectiles.clear();
        score         = 0;
        survivalTicks = 0;
    }

    // =========================================================================
    // Demarrage du thread
    // =========================================================================

    /**
     * Cree et demarre le thread de la boucle de jeu.
     * Appele une seule fois depuis Main.main().
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // =========================================================================
    // Boucle de jeu
    // =========================================================================

    /**
     * Boucle de jeu principale (pattern delta-time a 60 FPS).
     */
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta   = 0;
        long lastTime  = System.nanoTime();
        long timer     = 0;
        int drawCount  = 0;

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

    /**
     * Dispatche la mise a jour selon l'etat courant.
     */
    public void update() {

        if (gameState == GameState.PLAYING) {
            updateGame();
        }

        // ECHAP : bascule pause / reprise (one-shot, consomme immediatement)
        if (keyH.escapeJustPressed) {
            keyH.escapeJustPressed = false;

            if (gameState == GameState.PLAYING) {
                pauseScreen.reset(); // remet l'ecran d'infos a zero
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

        for (Projectile p : projectiles) {
            p.update(screenWidth, screenHeight);
        }

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

        if (!player.alive)           gameState = GameState.GAME_OVER;
        if (enemyManager.bossDefeated) gameState = GameState.VICTORY;
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
            case MENU:       menuScreen.draw(g2);                                    break;
            case DIFFICULTY: difficultyScreen.draw(g2);                              break;
            case OPTIONS:    optionsScreen.draw(g2);                                 break;
            case PLAYING:    drawGame(g2);                                           break;
            case PAUSED:     drawGame(g2); pauseScreen.draw(g2);                    break;
            case GAME_OVER:  drawGame(g2); drawEndOverlay(g2, "GAME OVER", new Color(180, 30, 30)); break;
            case VICTORY:    drawGame(g2); drawEndOverlay(g2, "VICTOIRE !", new Color(50, 180, 80)); break;
        }

        // Compteur FPS (hors menus de navigation)
        if (gameState != GameState.MENU && gameState != GameState.DIFFICULTY) {
            g2.setColor(Color.yellow);
            g2.setFont(gameFont != null ? gameFont.deriveFont(13f) : new Font("Arial", Font.PLAIN, 13));
            g2.drawString("FPS : " + currentFPS, screenWidth - 80, 20);
        }

        g2.dispose();
    }

    /**
     * Dessine la scene de jeu (fond, grille, projectiles, ennemis, joueur, HUD).
     */
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
        int seconds = survivalTicks / FPS;
        g2.drawString("Score : " + score,              10, 45);
        g2.drawString("Temps : " + formatTime(seconds), 10, 65);

        Font diffFont = gameFont != null ? gameFont.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(diffFont);
        g2.setColor(getDifficultyColor());
        g2.drawString(GameSettings.getInstance().getDifficulty().getLabel(), screenWidth - 75, 40);
    }

    /**
     * Dessine l'overlay de fin de partie (GAME OVER ou VICTOIRE).
     */
    private void drawEndOverlay(Graphics2D g2, String title, Color titleColor) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        Font titleFont = gameFont != null ? gameFont.deriveFont(Font.BOLD, 56f)
                                          : new Font("Arial", Font.BOLD, 56);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();
        int cx = screenWidth  / 2 - fm.stringWidth(title) / 2;
        int cy = screenHeight / 2 - 60;

        g2.setColor(titleColor.darker().darker());
        g2.drawString(title, cx + 4, cy + 4);
        g2.setColor(titleColor);
        g2.drawString(title, cx, cy);

        Font statsFont = gameFont != null ? gameFont.deriveFont(20f) : new Font("Arial", Font.PLAIN, 20);
        g2.setFont(statsFont);
        fm = g2.getFontMetrics();
        String stats = "Score : " + score + "   |   Temps : " + formatTime(survivalTicks / FPS);
        g2.setColor(Color.white);
        g2.drawString(stats, screenWidth / 2 - fm.stringWidth(stats) / 2, cy + 60);

        Font hintFont = gameFont != null ? gameFont.deriveFont(15f) : new Font("Arial", Font.PLAIN, 15);
        g2.setFont(hintFont);
        fm = g2.getFontMetrics();
        String hint = "Appuie sur ENTREE pour revenir au menu";
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(hint, screenWidth / 2 - fm.stringWidth(hint) / 2, cy + 100);
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
