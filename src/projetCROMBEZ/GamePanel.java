package projetCROMBEZ;

import java.awt.*;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Panneau de jeu principal au coeur de l'application.
 *
 * Responsabilités :
 *  - Contenir la boucle de jeu 
 *  - Gérer les transitions entre les états via {@link GameState}.
 *  - Déléguer la mise  à jour et le rendu à  chaque sous-système.
 *  - Réinitialiser la partie  à la demande.
 *
 * états gérés :
 *  MENU        MenuScreen
 *  DIFFICULTY  DifficultyScreen
 *  PLAYING     Player, EnemyManager, Projectile (logique de jeu)
 *  PAUSED      PauseScreen (par-dessus le jeu figÃ©)
 *  OPTIONS    OptionsScreen
 *  GAME_OVER   Overlay game over
 *  VICTORY     Overlay victoire
 */
public class GamePanel extends JPanel implements Runnable {

    // =========================================================================
    // ParamÃ¨tres de la fenêtre
    // =========================================================================

    /** Taille originale d'une tuile avant mise à  l'échelle (px). */
    int originalTileSize = 16;

    /** Facteur de mise à  l'échelle */
    int scale = 2;

    /** Taille réelle des tuiles en pixels (32px). */
    int tileSize = originalTileSize * scale;

    /** Nombre de tuiles sur l'axe horizontal. */
    int screenCol = 38;

    /** Nombre de tuiles sur l'axe vertical. */
    int screenRow = 26;

    /** Largeur de l'écran de jeu en pixels (1216). */
    int screenWidth  = tileSize * screenCol;

    /** Hauteur de l'écran de jeu en pixels (832). */
    int screenHeight = tileSize * screenRow;

    /** Fréquence d'affichage cible (images par seconde). */
    int FPS = 60;

    /** FPS mesuré en temps réel (affiché à l'écran). */
    int currentFPS = 0;

    // =========================================================================
    // Police de caractères
    // =========================================================================

    /** Police personnalisée */
    Font gameFont;


    /** Thread de la boucle de jeu. */
    Thread gameThread;

    /** Gestionnaire des entrées clavier. */
    KeyHandler keyH = new KeyHandler();

    // =========================================================================
    // état du jeu
    // =========================================================================

    /** état actuel du jeu (MENU, PLAYING, PAUSED, etc.). */
    public GameState gameState = GameState.MENU;

    // =========================================================================
    // Systèmes de jeu
    // =========================================================================

    /** Instance du joueur. */
    public Player player;

    /** Gestionnaire des ennemis et des vagues. */
    public EnemyManager enemyManager;

    /** Liste partagée de tous les projectiles actifs. */
    public List<Projectile> projectiles = new ArrayList<>();

    // =========================================================================
    // écrans / menus
    // =========================================================================

    /** écran du menu principal. */
    MenuScreen menuScreen;

    /** écran de sélection de la difficulté. */
    DifficultyScreen difficultyScreen;

    /** Menu pause (overlay sur le jeu). */
    PauseScreen pauseScreen;

    /** écran des options. */
    public OptionsScreen optionsScreen;

    // =========================================================================
    // Score et temps de survie
    // =========================================================================

    /** Score accumulé (10 points par ennemi tué). */
    int score = 0;

    /** Nombre de frames écoulées depuis le début de la partie ( conversion en secondes). */
    int survivalTicks = 0;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Initialise le panneau de jeu, charge la police, instancie tous les sous-systèmes.
     * L'ordre d'instanciation est important : les listeners souris des écrans
     * s'accumulent sur ce panel, donc l'écran actif filtre selon gameState.
     */
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);    // réduit les scintillements
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.requestFocusInWindow();

        // --- Chargement de la police personnalisée ---
        try {
            Font customFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    new File("font/BlueWinter.ttf")).deriveFont(15f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            this.gameFont = customFont;
        } catch (Exception e) {
            
        }

        // --- Instanciation des systèmes (ordre important pour les listeners) ---
        player           = new Player(this);
        enemyManager     = new EnemyManager(this);

        // Les écrans enregistrent leurs propres listeners souris
        menuScreen       = new MenuScreen(this);
        difficultyScreen = new DifficultyScreen(this);
        optionsScreen    = new OptionsScreen(this, GameState.MENU);
        pauseScreen      = new PauseScreen(this);
    }

    // =========================================================================
    // Reset de partie
    // =========================================================================

    /**
     * Réinitialise tous les systèmes de jeu pour démarrer une nouvelle partie.
     * Appelé par DifficultyScreen juste avant de passer en PLAYING.
     */
    public void resetGame() {
        player.reset();
        enemyManager.reset();
        projectiles.clear();
        score = 0;
        survivalTicks = 0;
    }

    // =========================================================================
    // Démarrage du thread
    // =========================================================================

    /**
     * Crée et démarre le thread de la boucle de jeu.
     * Appelé une seule fois depuis Main.main().
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // =========================================================================
    // Boucle de jeu
    // =========================================================================

    /**
     * Boucle de jeu principale (pattern delta-time).
     *
     * Garantit un maximum de {@link #FPS} mises à  jour par seconde, quelle que
     * soit la vitesse de la machine. L'accumulation de delta permet de
     * rattraper les frames en retard sans dépasser le taux cible.
     */
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS; // durée d'un frame en nanosecondes
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
                update();   // logique
                repaint();  // rendu
                delta--;
                drawCount++;
            }

            // Calcul du FPS réel (toutes les secondes)
            if (timer >= 1_000_000_000) {
                currentFPS = drawCount;
                drawCount  = 0;
                timer      = 0;
            }
        }
    }

    // =========================================================================
    // Mise à jour
    // =========================================================================

    /**
     * Dispatche la mise à  jour selon l'état actuel du jeu.
     * Appelé une fois par frame.
     */
    public void update() {
        switch (gameState) {
            case PLAYING:
                updateGame();
                break;

            case PAUSED:
            case MENU:
            case DIFFICULTY:
            case OPTIONS:
            case GAME_OVER:
            case VICTORY:
                // Pas de logique de jeu dans ces états
                // (les menus sont pilotés par les listeners souris)
                break;
        }

        // --- Touche ECHAP : bascule pause / reprise (one-shot) ---
        if (keyH.escapeJustPressed) {
            keyH.escapeJustPressed = false; // consume l'évènement

            if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSED;
            } else if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
            }
        }

        // --- ENTREE sur les écrans de fin : retour au menu ---
        if (keyH.enterPressed &&
                (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY)) {
            gameState = GameState.MENU;
        }
    }

    /**
     * Logique de jeu exécutée uniquement à l'état PLAYING.
     * Met à  jour projectiles, ennemis, collisions et joueur.
     */
    private void updateGame() {
        survivalTicks++;

        // --- Nettoyage des projectiles morts ---
        projectiles.removeIf(p -> !p.alive);

        // --- Déplacement de chaque projectile ---
        for (Projectile p : projectiles) {
            p.update(screenWidth, screenHeight);
        }

        // --- Mise à  jour des ennemis (IA, attaques, spawn) ---
        enemyManager.update(player, projectiles);

        // --- Collisions : projectiles du joueur  ennemis ---
        for (Projectile p : projectiles) {
            if (!p.fromPlayer || !p.alive) continue;

            for (Enemy e : enemyManager.enemies) {
                if (!e.alive) continue;

                if (p.getBounds().intersects(e.getBounds())) {
                    e.takeDamage(p.damage);
                    p.alive = false; // le projectile est consommé

                    if (!e.alive) score += 10; // +10 points par ennemi tué
                    break; // un projectile ne peut toucher qu'un ennemi
                }
            }
        }

        // --- Mise à  jour du joueur (mouvement, attaque auto, collision proj. ennemis) ---
        player.update(keyH, enemyManager.enemies, projectiles);

        // --- Vérification de fin de partie ---
        if (!player.alive) {
            gameState = GameState.GAME_OVER;
        }

        // Le boss vaincu est détecté dans EnemyManager.update()
        if (enemyManager.bossDefeated) {
            gameState = GameState.VICTORY;
        }
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dispatche le rendu selon l'état actuel.
     * Appelé par Swing suite à  repaint().
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case MENU:
                menuScreen.draw(g2);
                break;

            case DIFFICULTY:
                difficultyScreen.draw(g2);
                break;

            case OPTIONS:
                optionsScreen.draw(g2);
                break;

            case PLAYING:
                drawGame(g2);
                break;

            case PAUSED:
                // On dessine d'abord le jeu "figé" en fond, puis l'overlay pause
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

        // --- FPS toujours affiché (sauf en menu) ---
        if (gameState != GameState.MENU && gameState != GameState.DIFFICULTY) {
            g2.setColor(Color.yellow);
            g2.setFont(gameFont != null ? gameFont.deriveFont(13f) : new Font("Arial", Font.PLAIN, 13));
            g2.drawString("FPS : " + currentFPS, screenWidth - 80, 20);
        }

        g2.dispose();
    }

    /**
     * Dessine la scène de jeu (fond, grille, projectiles, ennemis, joueur, score).
     * Partagé entre PLAYING, PAUSED et les overlays de fin.
     *
     * @param g2 Contexte graphique
     */
    private void drawGame(Graphics2D g2) {
        // Fond sombre uni
        g2.setColor(new Color(20, 20, 35));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Grille
        g2.setColor(new Color(30, 30, 50));
        for (int i = 0; i < screenWidth;  i += tileSize) g2.drawLine(i, 0, i, screenHeight);
        for (int j = 0; j < screenHeight; j += tileSize) g2.drawLine(0, j, screenWidth, j);

        // Projectiles (dessinés avant les entités pour qu'ils passent dessous)
        for (Projectile p : projectiles) {
            p.draw(g2);
        }

        // Ennemis et indicateur de vague
        enemyManager.draw(g2);

        // Joueur (toujours au premier plan)
        player.draw(g2);

        // --- Score et temps de survie ---
        Font scoreFont = gameFont != null ? gameFont.deriveFont(14f) : new Font("Arial", Font.PLAIN, 14);
        g2.setFont(scoreFont);
        g2.setColor(Color.white);

        int seconds = survivalTicks / FPS;
        g2.drawString("Score : " + score,              10, 45);
        g2.drawString("Temps : " + formatTime(seconds), 10, 65);

        // --- Indicateur de difficulté ---
        Font diffFont = gameFont != null ? gameFont.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(diffFont);
        g2.setColor(getDifficultyColor());
        g2.drawString(GameSettings.getInstance().getDifficulty().getLabel(),
                      screenWidth - 75, 40);
    }

    /**
     * Dessine un overlay de fin de partie (game over ou victoire).
     * Fond semi-transparent + titre + stats + instruction de retour.
     *
     * @param g2        Contexte graphique
     * @param title     Texte principal à  afficher
     * @param titleColor Couleur du titre
     */
    private void drawEndOverlay(Graphics2D g2, String title, Color titleColor) {
        // Voile noir semi-transparent
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // --- Titre ---
        Font titleFont = gameFont != null ? gameFont.deriveFont(Font.BOLD, 56f)
                                          : new Font("Arial", Font.BOLD, 56);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();

        int cx = screenWidth / 2 - fm.stringWidth(title) / 2;
        int cy = screenHeight / 2 - 60;

        // Ombre portée
        g2.setColor(titleColor.darker().darker());
        g2.drawString(title, cx + 4, cy + 4);
        // Titre principal
        g2.setColor(titleColor);
        g2.drawString(title, cx, cy);

        // --- Stats ---
        Font statsFont = gameFont != null ? gameFont.deriveFont(20f)
                                          : new Font("Arial", Font.PLAIN, 20);
        g2.setFont(statsFont);
        fm = g2.getFontMetrics();

        int seconds = survivalTicks / FPS;
        String stats = "Score : " + score + "   |   Temps : " + formatTime(seconds);
        g2.setColor(Color.white);
        g2.drawString(stats, screenWidth / 2 - fm.stringWidth(stats) / 2, cy + 60);

        // --- Instruction ---
        Font hintFont = gameFont != null ? gameFont.deriveFont(15f)
                                         : new Font("Arial", Font.PLAIN, 15);
        g2.setFont(hintFont);
        fm = g2.getFontMetrics();
        String hint = "Appuie sur ENTRÉE pour revenir au menu";
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(hint, screenWidth / 2 - fm.stringWidth(hint) / 2, cy + 100);
    }

    // =========================================================================
    // Utilitaires
    // =========================================================================

    /**
     * Formate un nombre de secondes en chaine "Xm Ys".
     *
     * @param totalSeconds Durée totale en secondes
     * @return Chaine formatée, ex: "2m 35s"
     */
    private String formatTime(int totalSeconds) {
        return (totalSeconds / 60) + "m " + (totalSeconds % 60) + "s";
    }

    /**
     * Retourne la couleur associée à  la difficulté actuelle pour l'indicateur HUD.
     * Vert = Facile | Jaune = Normal | Rouge = Difficile
     */
    private Color getDifficultyColor() {
        switch (GameSettings.getInstance().getDifficulty()) {
            case EASY:   return new Color(80, 200, 80);
            case HARD:   return new Color(220, 60, 60);
            default:     return new Color(220, 180, 0);
        }
    }
}
