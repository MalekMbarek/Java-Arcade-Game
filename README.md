# Java Arcade: Pong & Space Invaders

A small arcade written in **Java Swing** with two games, **Pong** and **Space Invaders**, selectable from a drop-down list. It is a practice project for object-oriented design patterns (Factory Method, Observer, MVC) and multithreading in Java.

<!-- TODO: add screenshots/GIFs, e.g. docs/pong.png and docs/space-invaders.png -->
<!-- ![Space Invaders](docs/space-invaders.png) -->

## Features

- Arcade window with a game selector and a **Play / Pause** button
- Live **score** and **lives** display, plus a Game Over / "BRAVO !" end screen with a **Rejouer** (replay) button
- The playing field follows the window size when it is resized

### Pong
- The ball bounces off the walls and the paddle
- You have **3 lives**: if the ball passes the paddle, you lose one and the ball restarts from the top
- Each paddle rebound scores 1 point, and **5 rebounds win the game**

### Space Invaders
- A block of **3 rows x 6 enemies** moves sideways, drops down at each edge and **speeds up** (up to a cap)
- You shoot missiles upward, and enemies shoot back at random, **more often as the block gets closer**
- **3 lives**, and **10 points** per enemy destroyed
- **Win** by destroying every enemy. **Lose** if you run out of lives or the block reaches your level

## Controls

| Action | Input |
|---|---|
| Move left / right | `←` / `→` |
| Fire (Space Invaders) | `Space` |
| Start / Pause / Resume | **Play / Pause** button |
| Choose the game | Drop-down list (stops the current game, then press Play) |
| Play again after a game ends | **Rejouer** button |

## Run

Requires a **JDK 8 or newer**.

```bash
javac *.java
java Main
```

> Run these in the folder that contains the `.java` files and the `.png` images. The sprites are loaded with `getClass().getResource(...)`, so they must stay **next to the compiled classes** (same folder / classpath location). If you move them into a separate folder, loading will fail with a `NullPointerException`.

## Project structure

| File | Role |
|---|---|
| `Main.java` | Entry point: creates the `View` and the `Controller` |
| `Controller.java` | Keyboard, button and drop-down handling; a 10 ms `Timer` keeps the model in sync with the window size |
| `View.java` | Swing window. Observes every entity and draws it as a `JLabel`. Also shows score, lives and end screen |
| `Game.java` | Base class: score, lives, pause, outcome (`NONE` / `WIN` / `LOST`), field size. An empty `Game` is used before any game starts |
| `GameFactory.java`, `TypeGame.java` | Factory Method: builds the selected game from the `TypeGame` enum |
| `Pong.java` | Pong rules and game thread |
| `SpaceInvaders.java` | Space Invaders rules, enemy fire, collisions and game thread |
| `Enemies.java` | The enemy block: synchronized movement in its own thread |
| `Bullet.java` | A missile that moves itself in its own thread |
| `Sprite.java` | Player ship, moved with the keyboard |
| `Entity.java` | Drawable, observable object with position, image, collision test |
| `Observable.java`, `Observer.java` | Observer pattern between entities and the view |
| `*.png` | Sprites: `player`, `enemy1-3`, `shoot1-2`, `ball`, `paddle` |

## Design notes

- **MVC:** `Controller` handles input, `Game` classes hold the logic and `View` handles display. The controller only knows `Game`, so a new game needs only a new `TypeGame` value and a case in `GameFactory`.
- **Factory Method:** `GameFactory.createGame(type, view)` creates `Pong` or `SpaceInvaders`.
- **Observer:** entities notify the view whenever they move or are destroyed, and the view updates or removes the matching `JLabel`.
- **Concurrency:** each game, the enemy block and every missile run in their own thread. Shared state uses `volatile` fields, `CopyOnWriteArrayList` and synchronized blocks. All Swing updates go through `SwingUtilities.invokeLater`.
- **Inheritance:** `Pong` and `SpaceInvaders` extend `Game` and override `moveLeft`, `moveRight`, `launchPlayerBullet`, `togglePlayPause`, `kill` and `setFieldSize`.

## Context

<!-- TODO: e.g. "Lab project for <course>, <school>, <year>" -->

## Author

TODO: your name

## Credits

TODO: credit the source of the sprites if they are not your own.
