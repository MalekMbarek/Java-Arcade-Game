//View.java : la vue observe les entités (Observer)
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Font;
import java.util.Hashtable;

public class View implements Observer {
    JFrame frame = new JFrame("Arcade");
    JPanel panel = new JPanel();
    JLabel scoreLabel = new JLabel("Score : 0"); // score en cours, en haut à droite
    JLabel livesLabel = new JLabel("Vies : " + Game.START_LIVES); // vies restantes

    // Q4 : bouton Play / Pause et sélecteur de jeu
    JButton button = new JButton("Play");
    String[] strcomboBox = {"Pong", "SpaceInvaders"};
    JComboBox<String> comboBox = new JComboBox<String>(strcomboBox);

    // à chaque entité son JLabel
    Hashtable<Entity, JLabel> table = new Hashtable<Entity, JLabel>();

    public View() {
        panel.setLayout(null);

        // Q4 : on ne prend pas le focus, pour que le clavier reste sur la fenêtre de jeu
        button.setActionCommand("PlayPause"); // le texte change (Play / Pause), pas la commande
        button.setFocusable(false);
        button.setBounds(130, 0, 70, 20);
        panel.add(button);

        comboBox.setFocusable(false);
        comboBox.setBounds(0, 0, 130, 20);
        panel.add(comboBox);

        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 14f));
        scoreLabel.setBounds(300, 2, 100, 20);
        panel.add(scoreLabel);

        livesLabel.setFont(livesLabel.getFont().deriveFont(Font.BOLD, 14f));
        livesLabel.setBounds(215, 2, 80, 20);
        panel.add(livesLabel);

        frame.add(panel);
        frame.pack();
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    // appelee par le controleur : la frame (vue) est ecoutee par le controleur
    public void addListeners(Controller controller) {
        frame.addKeyListener(controller);
        button.addActionListener(controller);    // Q5 : le contrôleur écoute le bouton
        comboBox.addActionListener(controller);  // et la liste déroulante
    }

    // Q6 : jeu choisi dans la liste déroulante
    public String getTypeGame() {
        return (String) comboBox.getSelectedItem();
    }

    // Q17 : le texte du bouton suit l'état du jeu ("Play" ou "Pause")
    public void setButtonText(final String text) {
        SwingUtilities.invokeLater(() -> button.setText(text));
    }

    // taille de la zone de jeu (change quand on redimensionne la fenêtre)
    public int getFieldWidth() { return panel.getWidth(); }
    public int getFieldHeight() { return panel.getHeight(); }

    // appelée par une entité qui a changé (parfois depuis le thread d'un missile),
    // donc on exécute la mise à jour de l'affichage dans le thread Swing
    @Override
    public void update(Observable obs) {
        final Entity entity = (Entity) obs;
        SwingUtilities.invokeLater(() -> refresh(entity));
    }

    private void refresh(Entity entity) {
        JLabel jlabel = table.get(entity);

        if (!entity.isalive()) {
            // entité détruite : on retire son JLabel
            if (jlabel != null) {
                panel.remove(jlabel);
                table.remove(entity);
            }
        } else if (jlabel == null) {
            // nouvelle entité : on crée son JLabel
            jlabel = new JLabel(entity.getimage());
            table.put(entity, jlabel);
            panel.add(jlabel);
            jlabel.setBounds(entity.getX(), entity.getY(), entity.sizeX(), entity.sizeY());
        } else {
            // entité connue : on met à jour sa position
            jlabel.setBounds(entity.getX(), entity.getY(), entity.sizeX(), entity.sizeY());
        }
        panel.repaint();
    }

    // met à jour le score affiché pendant la partie
    public void showScore(final int score) {
        SwingUtilities.invokeLater(() -> scoreLabel.setText("Score : " + score));
    }

    // Partie VI : affiche les vies restantes du joueur
    public void showLives(final int lives) {
        SwingUtilities.invokeLater(() -> livesLabel.setText("Vies : " + lives));
    }

    // Q18 : états Win et Lost : message, score final et bouton pour rejouer.
    // Le bouton principal repasse sur « Play » : la partie est finie.
    public void showEndScreen(final String title, final int score, final Runnable onRetry) {
        SwingUtilities.invokeLater(() -> {
            int w = panel.getWidth();
            int h = panel.getHeight();

            button.setText("Play");

            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 32f));
            titleLabel.setBounds(0, h / 2 - 70, w, 50);
            panel.add(titleLabel);

            JLabel finalScore = new JLabel("Score : " + score, SwingConstants.CENTER);
            finalScore.setFont(finalScore.getFont().deriveFont(Font.PLAIN, 20f));
            finalScore.setBounds(0, h / 2 - 15, w, 30);
            panel.add(finalScore);

            JButton retry = new JButton("Rejouer");
            retry.setFocusable(false); // le clavier reste sur la fenêtre de jeu
            retry.setBounds(w / 2 - 60, h / 2 + 30, 120, 34);
            retry.addActionListener(e -> onRetry.run());
            panel.add(retry);

            panel.repaint();
        });
    }

    // remet l'affichage à zéro pour une nouvelle partie (on garde le sélecteur, le bouton et le score)
    public void reset() {
        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            table.clear();
            scoreLabel.setText("Score : 0");
            livesLabel.setText("Vies : " + Game.START_LIVES);
            panel.add(button);
            panel.add(comboBox);
            panel.add(scoreLabel);
            panel.add(livesLabel);
            panel.repaint();
            frame.requestFocus();
        });
    }
}
