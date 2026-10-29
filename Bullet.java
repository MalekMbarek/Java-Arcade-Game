//Bullet.java : missile tiré par le joueur ou les ennemis
public class Bullet extends Entity implements Runnable {
    private int maxPositionY; // position verticale maximale de déplacement
    private int speed;        // vitesse de déplacement (négative = vers le haut)
    private volatile boolean paused = false; // le jeu est en pause : le missile ne bouge plus

    public Bullet(int speed, String imgPath, int x, int y, int maxPositionY) {
        super(imgPath, x, y);
        this.speed = speed;
        this.maxPositionY = maxPositionY;
    }

    public int getSpeed() { return speed; }

    public void setMaxPositionY(int maxPositionY) { this.maxPositionY = maxPositionY; }

    public void setPause(boolean paused) { this.paused = paused; }

    // déplace le bullet dans la zone de déplacement (entre 0 et maxPositionY).
    // S'il sort de la zone, il est détruit : son thread s'arrête et la vue retire son JLabel.
    @Override
    public void move(int shiftX, int shiftY) {
        int newY = getY() + shiftY;
        if (newY < 0 || newY > maxPositionY) kill();
        else super.move(shiftX, shiftY);
    }

    // Q7 : le missile se déplace tout seul dans son propre thread (sauf en pause)
    @Override
    public void run() {
        while (isalive()) {
            try {
                Thread.sleep(10);
                if (!paused) move(0, speed);
            } catch (InterruptedException e) {}
        }
    }
}
