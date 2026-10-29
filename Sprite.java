//Sprite.java : lutin déplaçable à l'aide du clavier
public class Sprite extends Entity {
    private int maxPositionX; // position horizontale maximale de déplacement

    public Sprite(String imgPath, int x, int y, int maxPositionX) {
        super(imgPath, x, y);
        this.maxPositionX = maxPositionX;
    }

    public void setMaxPositionX(int maxPositionX) { this.maxPositionX = maxPositionX; }

    // déplace à droite le sprite (sans dépasser maxPositionX)
    public void moveRight() {
        int shift = Math.min(10, maxPositionX - getX());
        if (shift > 0) move(shift, 0);
    }

    // déplace à gauche le sprite (sans dépasser le bord gauche)
    public void moveLeft() {
        int shift = Math.min(10, getX());
        if (shift > 0) move(-shift, 0);
    }
}
