//GameFactory.java : la fabrique à jeu (patron Factory Method).
// Le contrôleur ne connaît que Game : pour ajouter un jeu, on modifie seulement TypeGame et cette classe.
public class GameFactory {

    public Game createGame(TypeGame type, View view) {
        Game game = new Game(); // jeu vide par défaut
        switch (type) {
            case PONG:
                game = new Pong(view);
                break;
            case SPACEINVADERS:
                game = new SpaceInvaders(view);
                break;
        }
        return game;
    }

    // jeu vide : utilisé au démarrage de l'arcade, avant que le joueur ait appuyé sur Play
    public static Game createGame() { return new Game(); }
}
