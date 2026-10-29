//Game.java : ce qui est commun à tous les jeux de l'arcade (Q8, Q13, Q16, Q18).
// Un Game « vide » ne fait rien : on s'en sert tant qu'aucun jeu n'a été lancé.
public class Game {
    volatile boolean alive = true;

    // éléments communs à Pong et SpaceInvaders (Q13)
    protected View view;                // la vue, observatrice des entités du jeu
    protected int width = 400;          // dimensions du terrain (suivent la fenêtre)
    protected int height = 370;
    protected volatile int score = 0;   // score de la partie en cours

    // Partie VI : vies du joueur
    public static final int START_LIVES = 3;
    protected volatile int lives = START_LIVES;

    // Q16 : jeu en pause
    protected volatile boolean pause = false;

    // Q18 : résultat de la partie. NONE = en cours (états Play ou Pause), puis WIN ou LOST.
    public enum Outcome { NONE, WIN, LOST }
    protected volatile Outcome outcome = Outcome.NONE;

    public Game() {}

    public Game(View view) { this.view = view; }

    public boolean isAlive() { return alive; }
    public int getScore() { return score; }
    public int getLives() { return lives; }

    public boolean isPause() { return pause; }
    public Outcome getOutcome() { return outcome; }
    public boolean isOver() { return outcome != Outcome.NONE; }

    // vrai pour un vrai jeu (Pong, SpaceInvaders), faux pour le jeu vide du démarrage
    public boolean isPlayable() { return false; }

    // Q16 : Play devient Pause, et inversement (les jeux complètent cette méthode)
    public void togglePlayPause() { pause = !pause; }

    // arrête le jeu (les jeux redéfinissent cette méthode pour détruire leurs éléments)
    public void kill() { alive = false; }

    // méthodes appelées dans le contrôleur : par défaut elles ne font rien
    public void moveRight() {}
    public void moveLeft() {}
    public void launchPlayerBullet() {}

    // le terrain prend la taille de la fenêtre (les jeux complètent cette méthode)
    public void setFieldSize(int w, int h) {
        if (w <= 0 || h <= 0) return;
        width = w;
        height = h;
    }
}
