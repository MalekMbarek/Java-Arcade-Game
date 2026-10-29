//Enemies.java : bloc d'ennemis qui se déplacent de manière synchronisée
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Enemies implements Runnable {
    private static final String[] IMAGES = {"enemy1.png", "enemy2.png", "enemy3.png"};
    private static final int CELL_W = 36;   // espace horizontal entre deux lutins
    private static final int CELL_H = 28;   // espace vertical entre deux lignes
    private static final int DROP = 16;     // descente du bloc à chaque bord atteint
    private static final int MAX_SPEED = 6; // le bloc accélère, mais pas indéfiniment

    private List<Entity> sprites = Collections.synchronizedList(new ArrayList<Entity>()); // liste des lutins ennemis
    private volatile int maxPositionX; // position horizontale maximale de déplacement
    private volatile int maxPositionY; // niveau du joueur : les ennemis ne doivent pas l'atteindre
    private volatile int speed = 1;    // vitesse : positive = vers la droite, négative = vers la gauche
    private volatile boolean alive = true; // le thread est actif et il reste des ennemis
    private volatile boolean paused = false;  // le jeu est en pause : le bloc ne bouge plus
    private volatile boolean reached = false; // le bloc a atteint le niveau du joueur

    // Q11 : initialise le nombre et la position des lutins (une image par ligne, lignes identiques)
    public Enemies(int rows, int cols, int startX, int startY, int maxPositionX, int maxPositionY) {
        this.maxPositionX = maxPositionX;
        this.maxPositionY = maxPositionY;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sprites.add(new Entity(IMAGES[r % IMAGES.length], startX + c * CELL_W, startY + r * CELL_H));
            }
        }
    }

    public int getSpeed() { return speed; }
    public List<Entity> getList() { return sprites; }
    public boolean isalive() { return alive; }
    public boolean reachedPlayer() { return reached; }

    public void setMaxPositionX(int maxPositionX) { this.maxPositionX = maxPositionX; }
    public void setMaxPositionY(int maxPositionY) { this.maxPositionY = maxPositionY; }
    public void setPause(boolean paused) { this.paused = paused; }

    // arrête le déplacement et détruit les ennemis
    public void kill() {
        alive = false;
        List<Entity> block;
        synchronized (sprites) {
            block = new ArrayList<Entity>(sprites);
            sprites.clear();
        }
        for (Entity e : block) e.kill();
    }

    // déplace le bloc d'ennemis : tous les lutins bougent ensemble
    public void move() {
        List<Entity> block; // copie, pour ne pas gêner un autre thread qui retirerait un ennemi
        synchronized (sprites) {
            block = new ArrayList<Entity>(sprites);
        }
        if (block.isEmpty()) { // plus d'ennemis : le thread s'arrête
            alive = false;
            return;
        }

        // limites actuelles du bloc
        int left = Integer.MAX_VALUE, right = 0, bottom = 0;
        for (Entity e : block) {
            left = Math.min(left, e.getX());
            right = Math.max(right, e.getX() + e.sizeX());
            bottom = Math.max(bottom, e.getY() + e.sizeY());
        }

        boolean edge = (speed > 0 && right + speed > maxPositionX) || (speed < 0 && left + speed < 0);
        if (edge) {
            // bord atteint : le bloc descend, change de sens et accélère
            for (Entity e : block) e.move(0, DROP);
            int faster = Math.min(Math.abs(speed) + 1, MAX_SPEED);
            speed = (speed > 0) ? -faster : faster;

            // le bloc a atteint le niveau du joueur : c'est perdu, le déplacement s'arrête
            if (bottom + DROP >= maxPositionY) {
                reached = true;
                alive = false;
            }
        } else {
            for (Entity e : block) e.move(speed, 0);
        }
    }

    // Q14 : le déplacement du bloc est exécuté par un thread (sauf en pause)
    @Override
    public void run() {
        while (isalive()) {
            try {
                Thread.sleep(20);
                if (!paused) move();
            } catch (InterruptedException e) {}
        }
    }
}
