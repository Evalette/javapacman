/* Drew Schuster */

import java.awt.*;
import javax.swing.JPanel;
import java.lang.Math;
import java.util.*;
import java.io.*;


/* Both Player and Ghost inherit Mover.  Has generic functions relevant to both*/
class Mover {
    /* Direction ghost is heading */  protected char direction;
    /* Current ghost location */  protected int x;
    protected int y;
    /* FrameCount is used to count animation frames*/ private int frameCount = 0;

    /* State contains the game map */ private boolean[][] state;

    /* gridSize is the size of one square in the game.
       max is the height/width of the game.
       increment is the speed at which the object moves,
       1 increment per move() call */ private int gridSize;
    private int max;
    private int increment;

    /* Generic constructor */
    public Mover() {
        setGridSize(20);
        setIncrement(4);
        setMax(400);
        setState(new boolean[20][20]);
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                getState()[i][j] = false;
            }
        }
    }

    /* Updates the state information */
    public void updateState(boolean[][] state) {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                this.getState()[i][j] = state[i][j];
            }
        }
    }

    /* Determines if a set of coordinates is a valid destination.*/
    public boolean isValidDest(int x, int y) {
    /* The first statements check that the x and y are inbounds.  The last statement checks the map to
       see if it's a valid location */
        return (((x) % 20 == 0) || ((y) % 20) == 0) && 20 <= x && x < 400 && 20 <= y && y < 400 && getState()[x / 20 - 1][y / 20 - 1];
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public boolean[][] getState() {
        return state;
    }

    public void setState(boolean[][] state) {
        this.state = state;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    /* Chooses a new direction randomly for the ghost to move */
    public char newDirection() {
        int random;
        char backwards = 'U';
        int lookX = getX(), lookY = getY();
        Set<Character> set = new HashSet<>();
        switch (getDirection()) {
            case 'L' -> backwards = 'R';
            case 'R' -> backwards = 'L';
            case 'U' -> backwards = 'D';
            case 'D' -> backwards = 'U';
        }

        char newDirection = backwards;
        /* While we still haven't found a valid direction */
        while (newDirection == backwards || !isValidDest(lookX, lookY)) {
            /* If we've tried every location, turn around and break the loop */
            if (set.size() == 3) {
                newDirection = backwards;
                break;
            }

            lookX = getX();
            lookY = getY();

            /* Randomly choose a direction */
            random = (int) (Math.random() * 4) + 1;
            if (random == 1) {
                newDirection = 'L';
                lookX -= getIncrement();
            } else if (random == 2) {
                newDirection = 'R';
                lookX += getGridSize();
            } else if (random == 3) {
                newDirection = 'U';
                lookY -= getIncrement();
            } else if (random == 4) {
                newDirection = 'D';
                lookY += getGridSize();
            }
            if (newDirection != backwards) {
                set.add(newDirection);
            }
        }
        return newDirection;
    }

    public char getDirection() {
        return direction;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

/* This is the pacman object */
class Player extends Mover {
    /* Direction is used in demoMode, currDirection and desiredDirection are used in non demoMode*/ private char direction;
    private char currDirection;
    private char desiredDirection;

    /* Keeps track of pellets eaten to determine end of game */ private int pelletsEaten;

    /* Last location */ private int lastX;
    private int lastY;

    /* Current location */ private int x;
    private int y;

    /* Which pellet the pacman is on top of */ private int pelletX;
    private int pelletY;

    /* teleport is true when travelling through the teleport tunnels*/ private boolean teleport;

    /* Stopped is set when the pacman is not moving or has been killed */ private boolean stopped = false;

    /* Constructor places pacman in initial location and orientation */
    public Player(int x, int y) {

        setTeleport(false);
        setPelletsEaten(0);
        setPelletX(x / getGridSize() - 1);
        setPelletY(y / getGridSize() - 1);
        this.setLastX(x);
        this.setLastY(y);
        this.setX(x);
        this.setY(y);
        setCurrDirection('L');
        setDesiredDirection('L');
    }

    /* This function is used for demoMode.  It is copied from the Ghost class.  See that for comments */
    public boolean isChoiceDest() {
        return getX() % getGridSize() == 0 && getY() % getGridSize() == 0;
    }

    /* This function is used for demoMode.  It is copied from the Ghost class.  See that for comments */
    public void demoMove() {
        setLastX(getX());
        setLastY(getY());
        if (isChoiceDest()) {
            setDirection(newDirection());
        }
        switch (getDirection()) {
            case 'L':
                if (isValidDest(getX() - getIncrement(), getY())) {
                    setX(getX() - getIncrement());
                } else if (getY() == 9 * getGridSize() && getX() < 2 * getGridSize()) {
                    setX(getMax() - getGridSize());
                    setTeleport(true);
                }
                break;
            case 'R':
                if (isValidDest(getX() + getGridSize(), getY())) {
                    setX(getX() + getIncrement());
                } else if (getY() == 9 * getGridSize() && getX() > getMax() - getGridSize() * 2) {
                    setX(getGridSize());
                    setTeleport(true);
                }
                break;
            case 'U':
                if (isValidDest(getX(), getY() - getIncrement())) setY(getY() - getIncrement());
                break;
            case 'D':
                if (isValidDest(getX(), getY() + getGridSize())) setY(getY() + getIncrement());
                break;
        }
        setCurrDirection(getDirection());
        setFrameCount(getFrameCount() + 1);
    }

    /* The move function moves the pacman for one frame in non demo mode */
    public void move() {
        int gridSize = 20;
        setLastX(getX());
        setLastY(getY());

        /* Try to turn in the direction input by the user */
        /*Can only turn if we're in center of a grid*/
        if (getX() % 20 == 0 && getY() % 20 == 0 ||
                /* Or if we're reversing*/
                (getDesiredDirection() == 'L' && getCurrDirection() == 'R') || (getDesiredDirection() == 'R' && getCurrDirection() == 'L') || (getDesiredDirection() == 'U' && getCurrDirection() == 'D') || (getDesiredDirection() == 'D' && getCurrDirection() == 'U')) {
            switch (getDesiredDirection()) {
                case 'L':
                    if (isValidDest(getX() - getIncrement(), getY())) setX(getX() - getIncrement());
                    break;
                case 'R':
                    if (isValidDest(getX() + gridSize, getY())) setX(getX() + getIncrement());
                    break;
                case 'U':
                    if (isValidDest(getX(), getY() - getIncrement())) setY(getY() - getIncrement());
                    break;
                case 'D':
                    if (isValidDest(getX(), getY() + gridSize)) setY(getY() + getIncrement());
                    break;
            }
        }
        /* If we haven't moved, then move in the direction the pacman was headed anyway */
        if (getLastX() == getX() && getLastY() == getY()) {
            switch (getCurrDirection()) {
                case 'L':
                    if (isValidDest(getX() - getIncrement(), getY())) setX(getX() - getIncrement());
                    else if (getY() == 9 * gridSize && getX() < 2 * gridSize) {
                        setX(getMax() - gridSize);
                        setTeleport(true);
                    }
                    break;
                case 'R':
                    if (isValidDest(getX() + gridSize, getY())) setX(getX() + getIncrement());
                    else if (getY() == 9 * gridSize && getX() > getMax() - gridSize * 2) {
                        setX(gridSize);
                        setTeleport(true);
                    }
                    break;
                case 'U':
                    if (isValidDest(getX(), getY() - getIncrement())) setY(getY() - getIncrement());
                    break;
                case 'D':
                    if (isValidDest(getX(), getY() + gridSize)) setY(getY() + getIncrement());
                    break;
            }
        }

        /* If we did change direction, update currDirection to reflect that */
        else {
            setCurrDirection(getDesiredDirection());
        }

        /* If we didn't move at all, set the stopped flag */
        if (getLastX() == getX() && getLastY() == getY()) setStopped(true);

            /* Otherwise, clear the stopped flag and increment the frameCount for animation purposes*/
        else {
            setStopped(false);
            setFrameCount(getFrameCount() + 1);
        }
    }

    /* Update what pellet the pacman is on top of */
    public void updatePellet() {
        if (getX() % getGridSize() == 0 && getY() % getGridSize() == 0) {
            setPelletX(getX() / getGridSize() - 1);
            setPelletY(getY() / getGridSize() - 1);
        }
    }

    public char getDirection() {
        return direction;
    }

    public void setDirection(char direction) {
        this.direction = direction;
    }

    public char getCurrDirection() {
        return currDirection;
    }

    public void setCurrDirection(char currDirection) {
        this.currDirection = currDirection;
    }

    public char getDesiredDirection() {
        return desiredDirection;
    }

    public void setDesiredDirection(char desiredDirection) {
        this.desiredDirection = desiredDirection;
    }

    public int getPelletsEaten() {
        return pelletsEaten;
    }

    public void setPelletsEaten(int pelletsEaten) {
        this.pelletsEaten = pelletsEaten;
    }

    public int getLastX() {
        return lastX;
    }

    public void setLastX(int lastX) {
        this.lastX = lastX;
    }

    public int getLastY() {
        return lastY;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPelletX() {
        return pelletX;
    }

    public void setPelletX(int pelletX) {
        this.pelletX = pelletX;
    }

    public int getPelletY() {
        return pelletY;
    }

    public void setPelletY(int pelletY) {
        this.pelletY = pelletY;
    }

    public boolean isTeleport() {
        return teleport;
    }

    public void setTeleport(boolean teleport) {
        this.teleport = teleport;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}

/* Ghost class controls the ghost. */
class Ghost extends Mover {

    /* Last ghost location*/ private int lastX;
    private int lastY;

    /* The pellet the ghost is on top of */ private int pelletX;
    private int pelletY;

    /* The pellet the ghost was last on top of */ private int lastPelletX;
    private int lastPelletY;

    /*Constructor places ghost and updates states*/
    public Ghost(int x, int y) {
        setDirection('L');
        setPelletX(x / getGridSize() - 1);
        setPelletY(x / getGridSize() - 1);
        setLastPelletX(getPelletX());
        setLastPelletY(getPelletY());
        this.setLastX(x);
        this.setLastY(y);
        this.setX(x);
        this.setY(y);
    }

    /* update pellet status */
    public void updatePellet() {
        int tempX, tempY;
        tempX = getX() / getGridSize() - 1;
        tempY = getY() / getGridSize() - 1;
        if (tempX != getPelletX() || tempY != getPelletY()) {
            setLastPelletX(getPelletX());
            setLastPelletY(getPelletY());
            setPelletX(tempX);
            setPelletY(tempY);
        }

    }

    /* Determines if the location is one where the ghost has to make a decision*/
    public boolean isChoiceDest() {
        return getX() % getGridSize() == 0 && getY() % getGridSize() == 0;
    }

    /* Random move function for ghost */
    public void move() {
        setLastX(getX());
        setLastY(getY());

        /* If we can make a decision, pick a new direction randomly */
        if (isChoiceDest()) {
            setDirection(newDirection());
        }

        /* If that direction is valid, move that way */
        switch (getDirection()) {
            case 'L':
                if (isValidDest(getX() - getIncrement(), getY())) setX(getX() - getIncrement());
                break;
            case 'R':
                if (isValidDest(getX() + getGridSize(), getY())) setX(getX() + getIncrement());
                break;
            case 'U':
                if (isValidDest(getX(), getY() - getIncrement())) setY(getY() - getIncrement());
                break;
            case 'D':
                if (isValidDest(getX(), getY() + getGridSize())) setY(getY() + getIncrement());
                break;
        }
    }

    public void setDirection(char direction) {
        this.direction = direction;
    }

    public int getLastX() {
        return lastX;
    }

    public void setLastX(int lastX) {
        this.lastX = lastX;
    }

    public int getLastY() {
        return lastY;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPelletX() {
        return pelletX;
    }

    public void setPelletX(int pelletX) {
        this.pelletX = pelletX;
    }

    public int getPelletY() {
        return pelletY;
    }

    public void setPelletY(int pelletY) {
        this.pelletY = pelletY;
    }

    public int getLastPelletX() {
        return lastPelletX;
    }

    public void setLastPelletX(int lastPelletX) {
        this.lastPelletX = lastPelletX;
    }

    public int getLastPelletY() {
        return lastPelletY;
    }

    public void setLastPelletY(int lastPelletY) {
        this.lastPelletY = lastPelletY;
    }
}


/*This board class contains the player, ghosts, pellets, and most of the game logic.*/
public class Board extends JPanel {

    /* For NOT JAR file*/ Image pacmanImage = Toolkit.getDefaultToolkit().getImage("img/pacman.jpg");
    Image pacmanUpImage = Toolkit.getDefaultToolkit().getImage("img/pacmanup.jpg");
    Image pacmanDownImage = Toolkit.getDefaultToolkit().getImage("img/pacmandown.jpg");
    Image pacmanLeftImage = Toolkit.getDefaultToolkit().getImage("img/pacmanleft.jpg");
    Image pacmanRightImage = Toolkit.getDefaultToolkit().getImage("img/pacmanright.jpg");
    Image ghost10 = Toolkit.getDefaultToolkit().getImage("img/ghost10.jpg");
    Image ghost20 = Toolkit.getDefaultToolkit().getImage("img/ghost20.jpg");
    Image ghost30 = Toolkit.getDefaultToolkit().getImage("img/ghost30.jpg");
    Image ghost40 = Toolkit.getDefaultToolkit().getImage("img/ghost40.jpg");
    Image ghost11 = Toolkit.getDefaultToolkit().getImage("img/ghost11.jpg");
    Image ghost21 = Toolkit.getDefaultToolkit().getImage("img/ghost21.jpg");
    Image ghost31 = Toolkit.getDefaultToolkit().getImage("img/ghost31.jpg");
    Image ghost41 = Toolkit.getDefaultToolkit().getImage("img/ghost41.jpg");
    Image titleScreenImage = Toolkit.getDefaultToolkit().getImage("img/titleScreen.jpg");
    Image gameOverImage = Toolkit.getDefaultToolkit().getImage("img/gameOver.jpg");
    Image winScreenImage = Toolkit.getDefaultToolkit().getImage("img/winScreen.jpg");

    /* Initialize the player and ghosts */ Player player = new Player(200, 300);
    Ghost ghost1 = new Ghost(180, 180);
    Ghost ghost2 = new Ghost(200, 180);
    Ghost ghost3 = new Ghost(220, 180);
    Ghost ghost4 = new Ghost(220, 180);

    /* Timer is used for playing sound effects and animations */ long timer = System.currentTimeMillis();

    /* Dying is used to count frames in the dying animation.  If it's non-zero,
       pacman is in the process of dying */ int dying = 0;

    /* Score information */ int currScore;
    int highScore;

    /* if the high scores have been cleared, we have to update the top of the screen to reflect that */ boolean clearHighScores = false;

    int numLives = 2;

    /*Contains the game map, passed to player and ghosts */ boolean[][] state;

    /* Contains the state of all pellets*/ boolean[][] pellets;

    /* Game dimensions */ int gridSize;
    int max;

    /* State flags*/ boolean stopped;
    boolean titleScreen;
    boolean winScreen = false;
    boolean overScreen = false;
    boolean demo = false;
    int New;

    /* Used to call sound effects */ GameSounds sounds;

    int lastPelletEatenX = 0;
    int lastPelletEatenY = 0;

    /* This is the font used for the menus */ Font font = new Font("Monospaced", Font.BOLD, 12);

    /* Constructor initializes state flags etc.*/
    public Board() {
        initHighScores();
        sounds = new GameSounds();
        currScore = 0;
        stopped = false;
        max = 400;
        gridSize = 20;
        New = 0;
        titleScreen = true;
    }

    /* Reads the high scores file and saves it */
    public void initHighScores() {
        File file = new File("highScores.txt");
        Scanner sc;
        try {
            sc = new Scanner(file);
            highScore = sc.nextInt();
            sc.close();
        } catch (Exception ignored) {
        }
    }

    /* Writes the new high score to a file and sets flag to update it on screen */
    public void updateScore(int score) {
        PrintWriter out;
        try {
            out = new PrintWriter("highScores.txt");
            out.println(score);
            out.close();
        } catch (Exception ignored) {
        }
        highScore = score;
        clearHighScores = true;
    }

    /* Wipes the high scores file and sets flag to update it on screen */
    public void clearHighScores() {
        PrintWriter out;
        try {
            out = new PrintWriter("highScores.txt");
            out.println("0");
            out.close();
        } catch (Exception ignored) {
        }
        highScore = 0;
        clearHighScores = true;
    }

    /* Reset occurs on a new game*/
    public void reset() {
        numLives = 2;
        state = new boolean[20][20];
        pellets = new boolean[20][20];

        /* Clear state and pellets arrays */
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                state[i][j] = true;
                pellets[i][j] = true;
            }
        }

        /* Handle the weird spots with no pellets*/
        for (int i = 5; i < 14; i++) {
            for (int j = 5; j < 12; j++) {
                pellets[i][j] = false;
            }
        }
        pellets[9][7] = false;
        pellets[8][8] = false;
        pellets[9][8] = false;
        pellets[10][8] = false;

    }


    /* Function is called during drawing of the map.
       Whenever a portion of the map is covered up with a barrier,
       the map and pellets arrays are updated accordingly to note
       that those are invalid locations to travel or put pellets
    */
    public void updateMap(int x, int y, int width, int height) {
        for (int i = x / gridSize; i < x / gridSize + width / gridSize; i++) {
            for (int j = y / gridSize; j < y / gridSize + height / gridSize; j++) {
                state[i - 1][j - 1] = false;
                pellets[i - 1][j - 1] = false;
            }
        }
    }


    /* Draws the appropriate number of lives on the bottom left of the screen.
       Also draws the menu */
    public void drawLives(Graphics g) {
        g.setColor(Color.BLACK);

        /*Clear the bottom bar*/
        g.fillRect(0, max + 5, 600, gridSize);
        g.setColor(Color.YELLOW);
        for (int i = 0; i < numLives; i++) {
            /*Draw each life */
            g.fillOval(gridSize * (i + 1), max + 5, gridSize, gridSize);
        }
        /* Draw the menu items */
        g.setColor(Color.YELLOW);
        g.setFont(font);
        g.drawString("Reset", 100, max + 5 + gridSize);
        g.drawString("Clear High Scores", 180, max + 5 + gridSize);
        g.drawString("Exit", 350, max + 5 + gridSize);
    }


    /*  This function draws the board.  The pacman board is really complicated and can only feasibly be done
        manually.  Whenever I draw a wall, I call updateMap to invalidate those coordinates.  This way the pacman
        and ghosts know that they can't traverse this area */
    public void drawBoard(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 600, 600);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 420, 420);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 20, 600);
        g.fillRect(0, 0, 600, 20);
        g.setColor(Color.WHITE);
        g.drawRect(19, 19, 382, 382);
        g.setColor(Color.BLUE);

        g.fillRect(40, 40, 60, 20);
        updateMap(40, 40, 60, 20);
        g.fillRect(120, 40, 60, 20);
        updateMap(120, 40, 60, 20);
        g.fillRect(200, 20, 20, 40);
        updateMap(200, 20, 20, 40);
        g.fillRect(240, 40, 60, 20);
        updateMap(240, 40, 60, 20);
        g.fillRect(320, 40, 60, 20);
        updateMap(320, 40, 60, 20);
        g.fillRect(40, 80, 60, 20);
        updateMap(40, 80, 60, 20);
        g.fillRect(160, 80, 100, 20);
        updateMap(160, 80, 100, 20);
        g.fillRect(200, 80, 20, 60);
        updateMap(200, 80, 20, 60);
        g.fillRect(320, 80, 60, 20);
        updateMap(320, 80, 60, 20);

        g.fillRect(20, 120, 80, 60);
        updateMap(20, 120, 80, 60);
        g.fillRect(320, 120, 80, 60);
        updateMap(320, 120, 80, 60);
        g.fillRect(20, 200, 80, 60);
        updateMap(20, 200, 80, 60);
        g.fillRect(320, 200, 80, 60);
        updateMap(320, 200, 80, 60);

        g.fillRect(160, 160, 40, 20);
        updateMap(160, 160, 40, 20);
        g.fillRect(220, 160, 40, 20);
        updateMap(220, 160, 40, 20);
        g.fillRect(160, 180, 20, 20);
        updateMap(160, 180, 20, 20);
        g.fillRect(160, 200, 100, 20);
        updateMap(160, 200, 100, 20);
        g.fillRect(240, 180, 20, 20);
        updateMap(240, 180, 20, 20);
        g.setColor(Color.BLUE);


        g.fillRect(120, 120, 60, 20);
        updateMap(120, 120, 60, 20);
        g.fillRect(120, 80, 20, 100);
        updateMap(120, 80, 20, 100);
        g.fillRect(280, 80, 20, 100);
        updateMap(280, 80, 20, 100);
        g.fillRect(240, 120, 60, 20);
        updateMap(240, 120, 60, 20);

        g.fillRect(280, 200, 20, 60);
        updateMap(280, 200, 20, 60);
        g.fillRect(120, 200, 20, 60);
        updateMap(120, 200, 20, 60);
        g.fillRect(160, 240, 100, 20);
        updateMap(160, 240, 100, 20);
        g.fillRect(200, 260, 20, 40);
        updateMap(200, 260, 20, 40);

        g.fillRect(120, 280, 60, 20);
        updateMap(120, 280, 60, 20);
        g.fillRect(240, 280, 60, 20);
        updateMap(240, 280, 60, 20);

        g.fillRect(40, 280, 60, 20);
        updateMap(40, 280, 60, 20);
        g.fillRect(80, 280, 20, 60);
        updateMap(80, 280, 20, 60);
        g.fillRect(320, 280, 60, 20);
        updateMap(320, 280, 60, 20);
        g.fillRect(320, 280, 20, 60);
        updateMap(320, 280, 20, 60);

        g.fillRect(20, 320, 40, 20);
        updateMap(20, 320, 40, 20);
        g.fillRect(360, 320, 40, 20);
        updateMap(360, 320, 40, 20);
        g.fillRect(160, 320, 100, 20);
        updateMap(160, 320, 100, 20);
        g.fillRect(200, 320, 20, 60);
        updateMap(200, 320, 20, 60);

        g.fillRect(40, 360, 140, 20);
        updateMap(40, 360, 140, 20);
        g.fillRect(240, 360, 140, 20);
        updateMap(240, 360, 140, 20);
        g.fillRect(280, 320, 20, 40);
        updateMap(280, 320, 20, 60);
        g.fillRect(120, 320, 20, 60);
        updateMap(120, 320, 20, 60);
        drawLives(g);
    }


    /* Draws the pellets on the screen */
    public void drawPellets(Graphics g) {
        g.setColor(Color.YELLOW);
        for (int i = 1; i < 20; i++) {
            for (int j = 1; j < 20; j++) {
                if (pellets[i - 1][j - 1]) g.fillOval(i * 20 + 8, j * 20 + 8, 4, 4);
            }
        }
    }

    /* Draws one individual pellet.  Used to redraw pellets that ghosts have run over */
    public void fillPellet(int x, int y, Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval(x * 20 + 28, y * 20 + 28, 4, 4);
    }

    /* This is the main function that draws one entire frame of the game */
    public void paint(Graphics g) {
    /* If we're playing the dying animation, don't update the entire screen.
       Just kill the pacman*/
        if (dying > 0) {
            /* Stop any pacman eating sounds */
            sounds.nomNomStop();

            /* Draw the pacman */
            g.drawImage(pacmanImage, player.getX(), player.getY(), Color.BLACK, null);
            g.setColor(Color.BLACK);

            /* Kill the pacman */
            if (dying == 4) g.fillRect(player.getX(), player.getY(), 20, 7);
            else if (dying == 3) g.fillRect(player.getX(), player.getY(), 20, 14);
            else if (dying == 2) g.fillRect(player.getX(), player.getY(), 20, 20);
            else if (dying == 1) {
                g.fillRect(player.getX(), player.getY(), 20, 20);
            }
     
      /* Take .1 seconds on each frame of death, and then take 2 seconds
         for the final frame to allow for the sound effect to end */
            long currTime = System.currentTimeMillis();
            long temp;
            if (dying != 1) temp = 100;
            else temp = 2000;
            /* If it's time to draw a new death frame... */
            if (currTime - timer >= temp) {
                dying--;
                timer = currTime;
                /* If this was the last death frame...*/
                if (dying == 0) {
                    if (numLives == -1) {
                        /* Demo mode has infinite lives, just give it more lives*/
                        if (demo) numLives = 2;
                        else {
                            /* Game over for player.  If relevant, update high score.  Set gameOver flag*/
                            if (currScore > highScore) {
                                updateScore(currScore);
                            }
                            overScreen = true;
                        }
                    }
                }
            }
            return;
        }

        /* If this is the title screen, draw the title screen and return */
        if (titleScreen) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 600, 600);
            g.drawImage(titleScreenImage, 0, 0, Color.BLACK, null);

            /* Stop any pacman eating sounds */
            sounds.nomNomStop();
            New = 1;
            return;
        }

        /* If this is the win screen, draw the win screen and return */
        else if (winScreen) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 600, 600);
            g.drawImage(winScreenImage, 0, 0, Color.BLACK, null);
            New = 1;
            /* Stop any pacman eating sounds */
            sounds.nomNomStop();
            return;
        }

        /* If this is the game over screen, draw the game over screen and return */
        else if (overScreen) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 600, 600);
            g.drawImage(gameOverImage, 0, 0, Color.BLACK, null);
            New = 1;
            /* Stop any pacman eating sounds */
            sounds.nomNomStop();
            return;
        }

        /* If you need to update the high scores, redraw the top menu bar */
        if (clearHighScores) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 600, 18);
            g.setColor(Color.YELLOW);
            g.setFont(font);
            clearHighScores = false;
            if (demo) g.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: " + highScore, 20, 10);
            else g.drawString("Score: " + (currScore) + "\t High Score: " + highScore, 20, 10);
        }

        /* oops, is set to true, when pacman has lost a life. */
        boolean oops = false;

        /* Game initialization */
        if (New == 1) {
            reset();
            player = new Player(200, 300);
            ghost1 = new Ghost(180, 180);
            ghost2 = new Ghost(200, 180);
            ghost3 = new Ghost(220, 180);
            ghost4 = new Ghost(220, 180);
            currScore = 0;
            drawBoard(g);
            drawPellets(g);
            drawLives(g);
            /* Send the game map to player and all ghosts */
            player.updateState(state);
            /* Don't let the player go in the ghost box*/
            player.getState()[9][7] = false;
            ghost1.updateState(state);
            ghost2.updateState(state);
            ghost3.updateState(state);
            ghost4.updateState(state);

            /* Draw the top menu bar*/
            g.setColor(Color.YELLOW);
            g.setFont(font);
            if (demo) g.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: " + highScore, 20, 10);
            else g.drawString("Score: " + (currScore) + "\t High Score: " + highScore, 20, 10);
            New++;
        }
        /* Second frame of new game */
        else if (New == 2) {
            New++;
        }
        /* Third frame of new game */
        else if (New == 3) {
            New++;
            /* Play the newGame sound effect */
            sounds.newGame();
            timer = System.currentTimeMillis();
            return;
        }
        /* Fourth frame of new game */
        else if (New == 4) {
            /* Stay in this state until the sound effect is over */
            long currTime = System.currentTimeMillis();
            if (currTime - timer >= 5000) {
                New = 0;
            } else return;
        }

        /* Drawing optimization */
        g.copyArea(player.getX() - 20, player.getY() - 20, 80, 80, 0, 0);
        g.copyArea(ghost1.getX() - 20, ghost1.getY() - 20, 80, 80, 0, 0);
        g.copyArea(ghost2.getX() - 20, ghost2.getY() - 20, 80, 80, 0, 0);
        g.copyArea(ghost3.getX() - 20, ghost3.getY() - 20, 80, 80, 0, 0);
        g.copyArea(ghost4.getX() - 20, ghost4.getY() - 20, 80, 80, 0, 0);



        /* Detect collisions */
        if (player.getX() == ghost1.getX() && Math.abs(player.getY() - ghost1.getY()) < 10) oops = true;
        else if (player.getX() == ghost2.getX() && Math.abs(player.getY() - ghost2.getY()) < 10) oops = true;
        else if (player.getX() == ghost3.getX() && Math.abs(player.getY() - ghost3.getY()) < 10) oops = true;
        else if (player.getX() == ghost4.getX() && Math.abs(player.getY() - ghost4.getY()) < 10) oops = true;
        else if (player.getY() == ghost1.getY() && Math.abs(player.getX() - ghost1.getX()) < 10) oops = true;
        else if (player.getY() == ghost2.getY() && Math.abs(player.getX() - ghost2.getX()) < 10) oops = true;
        else if (player.getY() == ghost3.getY() && Math.abs(player.getX() - ghost3.getX()) < 10) oops = true;
        else if (player.getY() == ghost4.getY() && Math.abs(player.getX() - ghost4.getX()) < 10) oops = true;

        /* Kill the pacman */
        if (oops && !stopped) {
            /* 4 frames of death*/
            dying = 4;

            /* Play death sound effect */
            sounds.death();
            /* Stop any pacman eating sounds */
            sounds.nomNomStop();

            /*Decrement lives, update screen to reflect that.  And set appropriate flags and timers */
            numLives--;
            stopped = true;
            drawLives(g);
            timer = System.currentTimeMillis();
        }

        /* Delete the players and ghosts */
        g.setColor(Color.BLACK);
        g.fillRect(player.getLastX(), player.getLastY(), 20, 20);
        g.fillRect(ghost1.getLastX(), ghost1.getLastY(), 20, 20);
        g.fillRect(ghost2.getLastX(), ghost2.getLastY(), 20, 20);
        g.fillRect(ghost3.getLastX(), ghost3.getLastY(), 20, 20);
        g.fillRect(ghost4.getLastX(), ghost4.getLastY(), 20, 20);

        /* Eat pellets */
        if (pellets[player.getPelletX()][player.getPelletY()] && New != 2 && New != 3) {
            lastPelletEatenX = player.getPelletX();
            lastPelletEatenY = player.getPelletY();

            /* Play eating sound */
            sounds.nomNom();

            /* Increment pellets eaten value to track for end game */
            player.setPelletsEaten(player.getPelletsEaten() + 1);

            /* Delete the pellet*/
            pellets[player.getPelletX()][player.getPelletY()] = false;

            /* Increment the score */
            currScore += 50;

            /* Update the screen to reflect the new score */
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 600, 20);
            g.setColor(Color.YELLOW);
            g.setFont(font);
            if (demo) g.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: " + highScore, 20, 10);
            else g.drawString("Score: " + (currScore) + "\t High Score: " + highScore, 20, 10);

            /* If this was the last pellet */
            if (player.getPelletsEaten() == 173) {
                /*Demo mode can't get a high score */
                if (!demo) {
                    if (currScore > highScore) {
                        updateScore(currScore);
                    }
                    winScreen = true;
                } else {
                    titleScreen = true;
                }
                return;
            }
        }

        /* If we moved to a location without pellets, stop the sounds */
        else if ((player.getPelletX() != lastPelletEatenX || player.getPelletY() != lastPelletEatenY) || player.isStopped()) {
            /* Stop any pacman eating sounds */
            sounds.nomNomStop();
        }


        /* Replace pellets that have been run over by ghosts */
        if (pellets[ghost1.getLastPelletX()][ghost1.getLastPelletY()])
            fillPellet(ghost1.getLastPelletX(), ghost1.getLastPelletY(), g);
        if (pellets[ghost2.getLastPelletX()][ghost2.getLastPelletY()])
            fillPellet(ghost2.getLastPelletX(), ghost2.getLastPelletY(), g);
        if (pellets[ghost3.getLastPelletX()][ghost3.getLastPelletY()])
            fillPellet(ghost3.getLastPelletX(), ghost3.getLastPelletY(), g);
        if (pellets[ghost4.getLastPelletX()][ghost4.getLastPelletY()])
            fillPellet(ghost4.getLastPelletX(), ghost4.getLastPelletY(), g);


        /*Draw the ghosts */
        if (ghost1.getFrameCount() < 5) {
            /* Draw first frame of ghosts */
            drawGhosts(g, ghost10, ghost20, ghost30, ghost40);
            ghost1.setFrameCount(ghost1.getFrameCount() + 1);
        } else {
            /* Draw second frame of ghosts */
            drawGhosts(g, ghost11, ghost21, ghost31, ghost41);
            if (ghost1.getFrameCount() >= 10) ghost1.setFrameCount(0);
            else ghost1.setFrameCount(ghost1.getFrameCount() + 1);
        }

        /* Draw the pacman */
        if (player.getFrameCount() < 5) {
            /* Draw mouth closed */
            g.drawImage(pacmanImage, player.getX(), player.getY(), Color.BLACK, null);
        } else {
            /* Draw mouth open in appropriate direction */
            if (player.getFrameCount() >= 10) player.setFrameCount(0);

            switch (player.getCurrDirection()) {
                case 'L' -> g.drawImage(pacmanLeftImage, player.getX(), player.getY(), Color.BLACK, null);
                case 'R' -> g.drawImage(pacmanRightImage, player.getX(), player.getY(), Color.BLACK, null);
                case 'U' -> g.drawImage(pacmanUpImage, player.getX(), player.getY(), Color.BLACK, null);
                case 'D' -> g.drawImage(pacmanDownImage, player.getX(), player.getY(), Color.BLACK, null);
            }
        }

        /* Draw the border around the game in case it was overwritten by ghost movement or something */
        g.setColor(Color.WHITE);
        g.drawRect(19, 19, 382, 382);

    }

    private void drawGhosts(Graphics g, Image ghost10, Image ghost20, Image ghost30, Image ghost40) {
        g.drawImage(ghost10, ghost1.getX(), ghost1.getY(), Color.BLACK, null);
        g.drawImage(ghost20, ghost2.getX(), ghost2.getY(), Color.BLACK, null);
        g.drawImage(ghost30, ghost3.getX(), ghost3.getY(), Color.BLACK, null);
        g.drawImage(ghost40, ghost4.getX(), ghost4.getY(), Color.BLACK, null);
    }
}
