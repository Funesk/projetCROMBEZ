package projetCROMBEZ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Panneau de jeu principal.
 *
 * Gere la boucle de jeu, les transitions d'etat, le dispatch souris,
 * les records, la sauvegarde, et les nouveaux systemes :
 *  - Gold drop quand un ennemi meurt (via EnemyManager.goldForEnemy)
 *  - Application du vol de vie apres chaque impact de projectile
 *  - Etat SHOP dispatche vers ShopScreen
 */
public class GamePanel extends JPanel implements Runnable {

    // =========================================================================
    // Parametres
    // =========================================================================

    int originalTileSize = 16, scale = 2;
    int tileSize     = originalTileSize * scale;
    int screenCol    = 38, screenRow = 26;
    int screenWidth  = tileSize * screenCol;
    int screenHeight = tileSize * screenRow;
    int FPS = 60, currentFPS = 0;

    Font gameFont;

    // =========================================================================
    // Systemes
    // =========================================================================

    Thread     gameThread;
    KeyHandler keyH = new KeyHandler();

    public GameState gameState   = GameState.MENU;
    public boolean   hasActiveGame = false;

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
    ShopScreen       shopScreen;

    // =========================================================================
    // Temps et records
    // =========================================================================

    int survivalTicks = 0;

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

        // Police BlueWinter
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, new File("font/BlueWinter.ttf")).deriveFont(15f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            this.gameFont = f;
        } catch (Exception e) {
            System.out.println("[GamePanel] Police BlueWinter introuvable, utilisation d'Arial.");
        }

        player       = new Player(this);
        enemyManager = new EnemyManager(this);

        menuScreen       = new MenuScreen(this);
        difficultyScreen = new DifficultyScreen(this);
        optionsScreen    = new OptionsScreen(this, GameState.MENU);
        pauseScreen      = new PauseScreen(this);
        shopScreen       = new ShopScreen(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> SaveManager.save(this)));
    }

    // =========================================================================
    // Reset
    // =========================================================================

    public void resetGame() {
        player.reset();
        enemyManager.reset();
        projectiles.clear();
        survivalTicks = 0; hasActiveGame = false;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // =========================================================================
    // Boucle
    // =========================================================================

    @Override
    public void run() {
        double interval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long last = System.nanoTime(), timer = 0;
        int count = 0;

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - last) / interval;
            timer += now - last;
            last = now;

            if (delta >= 1) { update(); repaint(); delta--; count++; }
            if (timer >= 1_000_000_000) { currentFPS = count; count = 0; timer = 0; }
        }
    }

    // =========================================================================
    // Mise a jour
    // =========================================================================

    public void update() {
        if (gameState == GameState.PLAYING) updateGame();
        if (gameState == GameState.PAUSED)  pauseScreen.tick();

        if (keyH.escapeJustPressed) {
            keyH.escapeJustPressed = false;
            if      (gameState == GameState.PLAYING) { pauseScreen.reset(); gameState = GameState.PAUSED; }
            else if (gameState == GameState.PAUSED)  { gameState = GameState.PLAYING; }
        }

        if (keyH.enterPressed &&
                (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY)) {
            gameState = GameState.MENU;
        }
    }

    private void updateGame() {
        survivalTicks++;
        projectiles.removeIf(p -> !p.alive);
        for (Projectile p : projectiles) p.update(screenWidth, screenHeight);

        enemyManager.update(player, projectiles);

        // Collisions projectiles joueur -> ennemis
        for (Projectile proj : projectiles) {
            if (!proj.fromPlayer || !proj.alive) continue;
            for (Enemy e : enemyManager.enemies) {
                if (!e.alive) continue;
                if (proj.getBounds().intersects(e.getBounds())) {
                    int prevHp = e.hp;
                    e.takeDamage(proj.damage);
                    proj.alive = false;

                    // Vol de vie : soigne selon les degats reellement infliges
                    int dmgDealt = prevHp - Math.max(0, e.hp);
                    player.applyLifeSteal(dmgDealt);

                    // Or si ennemi mort
                    if (!e.alive) {
                        player.gold   += EnemyManager.goldForEnemy(e);
                    }
                    break;
                }
            }
        }

        player.update(keyH, enemyManager.enemies, projectiles);

        if (!player.alive)             endGame(GameState.GAME_OVER);
        if (enemyManager.bossDefeated) endGame(GameState.VICTORY);
    }

    private void endGame(GameState newState) {
        boolean newRecord = false;
        if (survivalTicks > bestTimeTicks) { bestTimeTicks = survivalTicks; newRecord = true; }
        hasActiveGame = false;
        SaveManager.save(this);
        if (newRecord) System.out.println("[GamePanel] Nouveau record de survie !");
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
            case SHOP:       shopScreen.draw(g2);       break;
            case PLAYING:    drawGame(g2);              break;
            case PAUSED:     drawGame(g2); pauseScreen.draw(g2); break;
            case GAME_OVER:  drawGame(g2); drawEndOverlay(g2,"GAME OVER",new Color(180,30,30));  break;
            case VICTORY:    drawGame(g2); drawEndOverlay(g2,"VICTOIRE !",new Color(50,180,80)); break;
            default: break;
        }

        if (gameState != GameState.MENU && gameState != GameState.DIFFICULTY
                && gameState != GameState.SHOP) {
            g2.setColor(Color.yellow);
            g2.setFont(gameFont!=null?gameFont.deriveFont(13f):new Font("Arial",Font.PLAIN,13));
            g2.drawString("FPS : " + currentFPS, screenWidth-80, 20);
        }

        g2.dispose();
    }

    private void drawGame(Graphics2D g2) {
        g2.setColor(new Color(20,20,35));
        g2.fillRect(0,0,screenWidth,screenHeight);
        g2.setColor(new Color(30,30,50));
        for (int i=0;i<screenWidth; i+=tileSize) g2.drawLine(i,0,i,screenHeight);
        for (int j=0;j<screenHeight;j+=tileSize) g2.drawLine(0,j,screenWidth,j);

        for (Projectile p : projectiles) p.draw(g2);
        enemyManager.draw(g2);
        player.draw(g2);

        Font sf = gameFont!=null?gameFont.deriveFont(14f):new Font("Arial",Font.PLAIN,14);
        g2.setFont(sf); g2.setColor(Color.white);

        g2.drawString("Temps : " + formatTime(survivalTicks/FPS), 10, 65);
        g2.setColor(new Color(200,200,80));

        g2.drawString("Meilleur : " + formatTime(bestTimeTicks/FPS), 10, 85);

        Font df = gameFont!=null?gameFont.deriveFont(12f):new Font("Arial",Font.PLAIN,12);
        g2.setFont(df); g2.setColor(getDiffColor());
        g2.drawString(GameSettings.getInstance().getDifficulty().getLabel(), screenWidth-75, 40);
    }

    private void drawEndOverlay(Graphics2D g2, String title, Color tc) {
        g2.setColor(new Color(0,0,0,170));
        g2.fillRect(0,0,screenWidth,screenHeight);

        Font tf = gameFont!=null?gameFont.deriveFont(Font.BOLD,56f):new Font("Arial",Font.BOLD,56);
        g2.setFont(tf); FontMetrics fm=g2.getFontMetrics();
        int cx=screenWidth/2-fm.stringWidth(title)/2, cy=screenHeight/2-80;
        g2.setColor(tc.darker().darker()); g2.drawString(title,cx+4,cy+4);
        g2.setColor(tc); g2.drawString(title,cx,cy);

        Font sf=gameFont!=null?gameFont.deriveFont(20f):new Font("Arial",Font.PLAIN,20);
        g2.setFont(sf); fm=g2.getFontMetrics();
        String stats="Temps de survie : "+formatTime(survivalTicks/FPS);
        g2.setColor(Color.white);
        g2.drawString(stats,screenWidth/2-fm.stringWidth(stats)/2,cy+55);

        Font rf=gameFont!=null?gameFont.deriveFont(16f):new Font("Arial",Font.PLAIN,16);
        g2.setFont(rf); fm=g2.getFontMetrics();
        String rec="Meilleur temps : "+formatTime(bestTimeTicks/FPS);
        g2.setColor(new Color(200,200,80));
        g2.drawString(rec,screenWidth/2-fm.stringWidth(rec)/2,cy+85);

        // Or gagne cette partie
        Font gf=gameFont!=null?gameFont.deriveFont(15f):new Font("Arial",Font.PLAIN,15);
        g2.setFont(gf); fm=g2.getFontMetrics();
        String goldStr="Or total : "+player.gold;
        g2.setColor(new Color(255,210,50));
        g2.drawString(goldStr,screenWidth/2-fm.stringWidth(goldStr)/2,cy+112);

        boolean newRec=(survivalTicks>=bestTimeTicks&&survivalTicks>0);
        if (newRec) {
            Font bf=gameFont!=null?gameFont.deriveFont(Font.BOLD,18f):new Font("Arial",Font.BOLD,18);
            g2.setFont(bf); fm=g2.getFontMetrics();
            String b="*** NOUVEAU RECORD ! ***";
            g2.setColor(new Color(255,220,50));
            g2.drawString(b,screenWidth/2-fm.stringWidth(b)/2,cy+136);
        }

        Font hf=gameFont!=null?gameFont.deriveFont(15f):new Font("Arial",Font.PLAIN,15);
        g2.setFont(hf); fm=g2.getFontMetrics();
        String hint="Appuie sur ENTREE pour revenir au menu";
        g2.setColor(new Color(180,180,180));
        g2.drawString(hint,screenWidth/2-fm.stringWidth(hint)/2,cy+162);
    }

    // =========================================================================
    // Dispatcher souris
    // =========================================================================

    private void addMouseAdapter() {
        this.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { requestFocusInWindow(); }
            @Override public void mouseReleased(MouseEvent e) {
                GameState s = gameState; Point p = e.getPoint();
                switch (s) {
                    case MENU:       menuScreen.handleClick(p);       break;
                    case DIFFICULTY: difficultyScreen.handleClick(p); break;
                    case OPTIONS:    optionsScreen.handleClick(p);    break;
                    case PAUSED:     pauseScreen.handleClick(p);      break;
                    case SHOP:       shopScreen.handleClick(p);       break;
                    default: break;
                }
            }
        });
        this.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                GameState s = gameState; Point p = e.getPoint();
                switch (s) {
                    case MENU:       menuScreen.handleHover(p);       break;
                    case DIFFICULTY: difficultyScreen.handleHover(p); break;
                    case OPTIONS:    optionsScreen.handleHover(p);    break;
                    case PAUSED:     pauseScreen.handleHover(p);      break;
                    case SHOP:       shopScreen.handleHover(p);       break;
                    default: break;
                }
            }
        });
    }

    // =========================================================================
    // Utilitaires
    // =========================================================================

    private String formatTime(int s) { return (s/60)+"m "+(s%60)+"s"; }
    private Color getDiffColor() {
        switch (GameSettings.getInstance().getDifficulty()) {
            case EASY: return new Color(80,200,80);
            case HARD: return new Color(220,60,60);
            default:   return new Color(220,180,0);
        }
    }
}
