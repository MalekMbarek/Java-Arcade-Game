//Controller.java
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class Controller implements KeyListener, ActionListener {
    private View view;
    private Game game;                              // le jeu en cours (un Game vide au départ)
    private GameFactory factory = new GameFactory(); // Q11 : c'est la fabrique qui crée les jeux

    public Controller(View view) {
        this.view = view;
        this.game = GameFactory.createGame();

        // le controleur ecoute la vue (clavier, bouton, liste) et le timer
        view.addListeners(this);
        Timer t = new Timer(10, this);
        t.start();
    }

    // appelée par le bouton, par la liste déroulante ET par le Timer (qui n'a pas de nom de commande)
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();

        // Q17 : le bouton Play / Pause
        if ("PlayPause".equals(command)) {
            if (game.isPlayable() && !game.isOver()) {
                // partie en cours : Play -> Pause, ou Pause -> Play
                game.togglePlayPause();
                view.setButtonText(game.isPause() ? "Play" : "Pause");
            } else {
                // pas de partie (ou partie terminée) : on lance le jeu choisi dans la liste
                startNewGame();
            }
            return;
        }

        // un autre jeu est choisi dans la liste : on arrête la partie, on attend un clic sur Play
        if ("comboBoxChanged".equals(command)) {
            game.kill();
            view.reset();
            game = GameFactory.createGame();
            view.setButtonText("Play");
            return;
        }

        // sinon c'est le Timer : il synchronise le modèle avec la taille de la fenêtre
        game.setFieldSize(view.getFieldWidth(), view.getFieldHeight());
    }

    // Q11 : l'instanciation du jeu choisi est confiée à la fabrique
    private void startNewGame() {
        game.kill();      // on arrête le jeu précédent
        view.reset();     // on efface son affichage
        game = factory.createGame(getSelectedType(), view);
        view.setButtonText("Pause");
    }

    // convertit le texte de la liste déroulante en TypeGame
    private TypeGame getSelectedType() {
        if (view.getTypeGame().equals("Pong")) return TypeGame.PONG;
        return TypeGame.SPACEINVADERS;
    }

    @Override
    public void keyPressed(KeyEvent kev) {
        switch (kev.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                game.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                game.moveRight();
                break;
            case KeyEvent.VK_SPACE:
                game.launchPlayerBullet();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {}

    @Override
    public void keyTyped(KeyEvent arg0) {}
}
