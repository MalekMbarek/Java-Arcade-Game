//SpaceInvaders.java (ancienne classe Pong)
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

// Q10 : SpaceInvaders est un Game (héritage). La vue, les dimensions, le score,
// la pause et le résultat de la partie viennent de Game.
public class SpaceInvaders extends Game implements Runnable {
    volatile Sprite sprite;           // le joueur (volatile : remplacé quand on rejoue)
    private volatile Enemies enemies; // Q12 : le bloc d'ennemis (idem)

    // Q15 : tous les missiles (joueur et ennemis) dans une même liste.
    // CopyOnWriteArrayList : plusieurs threads (clavier, jeu, missiles) y accèdent en même temps
    private List<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();

    // score : points gagnés pour chaque ennemi détruit
    static final int POINTS_PER_ENEMY = 10;

    // Q16 : probabilité de tir d'un ennemi à chaque lancement (elle augmente quand le bloc approche)
    static final double SHOOT_PROBABILITY = 0.008;

    static final int SPRITE_MARGIN = 50; // distance entre le bas du terrain et le lutin

    public SpaceInvaders(View view) {
        super(view);
        startGame();
    }

    @Override
    public boolean isPlayable() { return true; }

    // crée le joueur et le bloc d'ennemis, puis lance les threads : sert au démarrage et pour rejouer
    private void startGame() {
        score = 0;
        lives = START_LIVES;
        pause = false;
        outcome = Outcome.NONE;

        // le joueur : un lutin centré en bas de l'écran au démarrage
        sprite = new Sprite("player.png", 0, 0, width);
        sprite.setMaxPositionX(width - sprite.sizeX());
        sprite.setX((width - sprite.sizeX()) / 2);
        sprite.setY(height - SPRITE_MARGIN);

        // la vue observe le lutin, puis on la prévient pour qu'elle l'affiche
        sprite.add(view);
        sprite.notifyObserver();

        // Q13 : un bloc de 3 lignes de 6 ennemis, qui ne doit pas descendre plus bas que le joueur
        enemies = new Enemies(3, 6, 20, 30, width, sprite.getY());
        for (Entity enemy : enemies.getList()) {
            enemy.add(view);
            enemy.notifyObserver();
        }

        // Q14 : le bloc d'ennemis se déplace dans son propre thread
        new Thread(enemies).start();

        // Q17 : le jeu a aussi son thread (tirs ennemis, collisions)
        new Thread(this).start();
    }

    // bouton « Rejouer » : on arrête ce qui reste de la partie précédente et on en démarre une nouvelle
    public void restart() {
        for (Bullet b : bullets) b.kill();
        bullets.clear();
        enemies.kill();
        sprite.kill();
        view.reset();
        view.setButtonText("Pause");
        startGame();
    }

    // arrêt du jeu demandé par le contrôleur (autre jeu choisi, nouveau clic sur Play) :
    // on arrête les threads et on détruit tous les éléments, la vue retire leurs JLabel
    @Override
    public void kill() {
        super.kill();
        for (Bullet b : bullets) b.kill();
        bullets.clear();
        enemies.kill();
        sprite.kill();
    }

    // Q16 et Q17 : pause / reprise. Le bloc d'ennemis et les missiles s'arrêtent aussi.
    @Override
    public void togglePlayPause() {
        if (isOver()) return;
        super.togglePlayPause();
        enemies.setPause(pause);
        for (Bullet b : bullets) b.setPause(pause);
    }

    public Sprite getSprite() { return sprite; }
    public Enemies getEnemies() { return enemies; }

    @Override
    public void moveRight() { if (!isPause() && sprite.isalive()) sprite.moveRight(); }

    @Override
    public void moveLeft() { if (!isPause() && sprite.isalive()) sprite.moveLeft(); }

    // appelée par le contrôleur quand la taille de la fenêtre change
    @Override
    public void setFieldSize(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (w == width && h == height) return;
        super.setFieldSize(w, h);
        sprite.setMaxPositionX(width - sprite.sizeX());
        sprite.setX(Math.max(0, Math.min(sprite.getX(), width - sprite.sizeX())));
        sprite.setY(height - SPRITE_MARGIN);
        enemies.setMaxPositionX(width);
        enemies.setMaxPositionY(sprite.getY());
    }

    // Q8 : le joueur tire un missile vers le haut depuis le milieu du canon
    // (pas de tir en pause ni si la partie est terminée)
    @Override
    public void launchPlayerBullet() {
        if (isPause() || !sprite.isalive() || !enemies.isalive()) return;
        launchBullet(sprite.getX() + sprite.sizeX() / 2, sprite.getY(), -1);
    }

    // Q16 : chaque ennemi tire (ou non) au hasard. Plus le bloc est proche du joueur,
    // plus la probabilité de tir est élevée.
    public void launchEnemyBullet() {
        List<Entity> block;
        synchronized (enemies.getList()) {
            block = new ArrayList<Entity>(enemies.getList());
        }
        if (block.isEmpty()) return;

        int bottom = 0;
        for (Entity e : block) bottom = Math.max(bottom, e.getY() + e.sizeY());
        double progress = Math.min(1.0, (double) bottom / Math.max(1, sprite.getY()));
        double probability = SHOOT_PROBABILITY * (1 + 3 * progress);

        for (Entity enemy : block) {
            if (Math.random() < probability) {
                // le tir part du dessous de l'ennemi, vers le bas (vitesse positive)
                launchBullet(enemy.getX() + enemy.sizeX() / 2, enemy.getY() + enemy.sizeY(), 1);
            }
        }
    }

    // crée un missile, lui donne son thread, le range dans la liste et ajoute la vue comme observateur.
    // Vitesse négative : missile du joueur (vers le haut). Vitesse positive : missile ennemi (vers le bas).
    private void launchBullet(final int x, final int y, final int speed) {
        Bullet bullet = new Bullet(speed, speed < 0 ? "shoot1.png" : "shoot2.png", x, y, height);
        bullet.setX(x - bullet.sizeX() / 2); // centre le missile sur x
        bullet.setPause(pause);
        bullets.add(bullet);
        bullet.add(view);
        final Thread thread = new Thread(bullet);
        thread.start();
    }

    // Q18 : collisions entre les missiles, les ennemis et le joueur.
    public void collisions() {
        for (Bullet bullet : bullets) {
            // Q20 : un missile hors de l'écran (ou déjà détruit) est détruit et retiré de la liste
            if (!bullet.isalive() || bullet.getY() <= 0 || bullet.getY() >= height) {
                bullet.kill();
                bullets.remove(bullet);
                continue;
            }

            // collision d'un missile ennemi (vitesse positive) avec le joueur
            if (sprite.isalive() && bullet.getSpeed() > 0 && bullet.collides(sprite)) {
                lives--;                    // une vie de moins ; à 0 le joueur est détruit
                view.showLives(lives);
                if (lives <= 0) sprite.kill();
                bullet.kill();
                bullets.remove(bullet);
                return;
            }

            // collisions d'un missile du joueur (vitesse négative) avec les ennemis.
            // Le parcours des ennemis est refait pour chaque missile (nouvel itérateur)
            // et protégé, car le thread des ennemis lit aussi cette liste.
            if (bullet.getSpeed() < 0) {
                synchronized (enemies.getList()) {
                    final ListIterator<Entity> itEnemy = enemies.getList().listIterator();
                    while (itEnemy.hasNext()) {
                        Entity enemy = itEnemy.next();
                        if (bullet.collides(enemy)) {
                            enemy.kill();
                            bullet.kill();
                            bullets.remove(bullet);
                            itEnemy.remove();
                            score += POINTS_PER_ENEMY;
                            view.showScore(score);
                            return;
                        }
                    }
                }
            }
        }
    }

    // Q17 et Q19 : thread du jeu. Il lance périodiquement les tirs ennemis
    // et teste les collisions en continu (sauf en pause). Il s'arrête quand la partie
    // est finie (Win ou Lost) ou quand le contrôleur arrête le jeu.
    @Override
    public void run() {
        int tick = 0;
        while (isAlive() && enemies.isalive() && sprite.isalive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
            if (isPause()) continue;
            tick++;
            if (tick % 50 == 0) launchEnemyBullet(); // toutes les 500 ms
            collisions();                            // toutes les 10 ms
        }

        // jeu arrêté par le contrôleur : pas d'écran de fin
        if (!isAlive()) return;

        // Q18 : état Lost si le joueur est touché ou si les ennemis ont atteint son niveau, sinon Win
        boolean reached = enemies.reachedPlayer();
        if (!sprite.isalive() || reached) {
            if (!reached) enemies.kill(); // joueur touché : on arrête et on retire les ennemis
            outcome = Outcome.LOST;
            view.showEndScreen("GAME OVER", score, this::restart);
        } else {
            outcome = Outcome.WIN;
            view.showEndScreen("BRAVO !", score, this::restart);
        }
    }
}
