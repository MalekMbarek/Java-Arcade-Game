//Pong.java : le jeu Pong dans l'arcade.
// La balle rebondit sur les bords et sur le paddle. Si elle dépasse le paddle, le joueur a perdu.
// Après 5 rebonds sur le paddle, le joueur a gagné.
public class Pong extends Game implements Runnable {
    private volatile Entity ball;
    private volatile Entity paddle;
    int speedX, speedY;

    private volatile boolean finished = false; // la partie est terminée (gagnée ou perdue)
    private volatile boolean lost = false;

    static final int PADDLE_MARGIN = 70; // distance entre le bas du terrain et le paddle
    static final int TOP = 25;           // la balle rebondit sous le sélecteur et le bouton
    static final int WIN_REBOUNDS = 5;   // rebonds sur le paddle pour gagner

    public Pong(View view) {
        super(view);
        startGame();
    }

    @Override
    public boolean isPlayable() { return true; }

    // crée la balle et le paddle, puis lance le thread : sert au démarrage et pour rejouer
    private void startGame() {
        score = 0;
        lives = START_LIVES;
        pause = false;
        outcome = Outcome.NONE;
        finished = false;
        lost = false;

        paddle = new Entity("paddle.png", 0, 0);
        paddle.setX((width - paddle.sizeX()) / 2);
        paddle.setY(height - PADDLE_MARGIN);

        ball = new Entity("ball.png", 0, TOP + 5);
        speedX = 1;
        speedY = 1;

        // la vue observe la balle et le paddle, puis on la prévient pour qu'elle les affiche
        paddle.add(view);
        ball.add(view);
        paddle.notifyObserver();
        ball.notifyObserver();

        new Thread(this).start();
    }

    // bouton « Rejouer »
    public void restart() {
        ball.kill();
        paddle.kill();
        view.reset();
        view.setButtonText("Pause");
        startGame();
    }

    // arrêt du jeu demandé par le contrôleur (autre jeu choisi, nouveau clic sur Play)
    @Override
    public void kill() {
        super.kill();
        ball.kill();
        paddle.kill();
    }

    // Q16 et Q17 : en pause, la balle ne bouge plus (voir run)
    @Override
    public void togglePlayPause() {
        if (isOver()) return;
        super.togglePlayPause();
    }

    public Entity getBall() { return ball; }
    public Entity getPaddle() { return paddle; }

    @Override
    public void moveRight() {
        if (finished || isPause()) return;
        paddle.setX(Math.min(paddle.getX() + 10, width - paddle.sizeX()));
    }

    @Override
    public void moveLeft() {
        if (finished || isPause()) return;
        paddle.setX(Math.max(paddle.getX() - 10, 0));
    }

    // le terrain suit la taille de la fenêtre : le paddle reste en bas
    @Override
    public void setFieldSize(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (w == width && h == height) return;
        super.setFieldSize(w, h);
        paddle.setY(height - PADDLE_MARGIN);
        paddle.setX(Math.max(0, Math.min(paddle.getX(), width - paddle.sizeX())));
    }

    private void moveBall() {
        if (ball.getX() > width - ball.sizeX()) speedX = -1;
        if (ball.getX() < 0) speedX = 1;
        if (ball.getY() < TOP) speedY = 1;

        // on ne compte le rebond que lorsque la balle descend vers le paddle
        if (ball.collides(paddle) && speedY > 0) {
            speedY = -1;
            score++;
            view.showScore(score);
        }

        ball.move(speedX, speedY);
    }

    // thread du jeu : la balle avance toutes les 10 ms (sauf en pause) jusqu'à la fin de la partie
    @Override
    public void run() {
        while (isAlive() && !finished) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
            if (isPause()) continue;
            moveBall();

            if (ball.getY() > paddle.getY()) {   // la balle a dépassé le paddle : une vie en moins
                lives--;
                view.showLives(lives);
                if (lives <= 0) {                // plus de vie : Lost
                    lost = true;
                    finished = true;
                } else {                         // sinon la balle repart du haut
                    ball.setX(0);
                    ball.setY(TOP + 5);
                    speedX = 1;
                    speedY = 1;
                }
            } else if (score >= WIN_REBOUNDS) {  // 5 rebonds : Win
                finished = true;
            }
        }

        // jeu arrêté par le contrôleur : pas d'écran de fin
        if (!isAlive()) return;

        outcome = lost ? Outcome.LOST : Outcome.WIN;
        view.showEndScreen(lost ? "GAME OVER" : "BRAVO !", score, this::restart);
    }
}
