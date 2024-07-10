import java.util.ArrayList;
import java.util.Arrays;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

//Extra credit: Restart option (press "r"), score tracker, graphics

//to store constants
interface IMinesweeper {
  int ROWS = 16;
  int COLUMNS = 30;
  int MINES = 99;
  int RAND_TEST_SEED = 15;
  int CELL_SIZE = 25;
  
  int SCREEN_WIDTH = ROWS * CELL_SIZE + (CELL_SIZE * 2);
  int SCREEN_HEIGHT = COLUMNS * CELL_SIZE + (CELL_SIZE * 2);
  
  WorldImage WIN_SCREEN = new TextImage("YOU WON!", ROWS * 4, FontStyle.BOLD, Color.GREEN);   
  WorldImage LOSE_SCREEN = new TextImage("YOU LOST :(", ROWS * 4, FontStyle.BOLD, Color.RED);  
  WorldImage BACKGROUND = new RectangleImage(
      SCREEN_HEIGHT * 4, SCREEN_WIDTH * 4, OutlineMode.SOLID, Color.DARK_GRAY);
  WorldImage RESTART_TEXT = new TextImage(
      "Press R to restart", CELL_SIZE - 4, FontStyle.REGULAR, Color.ORANGE);
  
}

//to represent the Minesweeper game
class Minesweeper extends World {
  int rows; 
  int cols;
  int mines;
  ArrayList<Cell> grid; 
  Random randSeed;
  boolean gameLost;

  //Normal constructor
  Minesweeper(int rows, int cols, int mines) {
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.grid = new ArrayList<Cell>();
    this.randSeed = new Random();
    this.gameLost = false;

    //to initialize this grid with cells
    this.initGrid();

    //to place mines randomly on this grid
    this.placeMines();
  }
  
  //Convenience constructor with providing grid
  Minesweeper(int rows, int cols, int mines, ArrayList<Cell> grid) {
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.grid = grid;
    this.randSeed = new Random();
    this.gameLost = false;

    //to initialize this grid with cells
    this.initGrid();

    //to place mines randomly on this grid
    this.placeMines();
  }
    
  //Convenience constructor for testing with grid and random seed
  Minesweeper(int rows, int cols, int mines, ArrayList<Cell> grid, Random randSeed) {
    this.rows = rows;     
    this.cols = cols;      
    this.mines = mines;
    this.grid = grid;
    this.randSeed = randSeed;
    this.gameLost = false;
        
    //to initialize this grid with cells
    this.initGrid();

    //to place mines randomly on this grid
    this.placeMines();
  }
    
  //Convenience constructor for testing initGrid, linkNeighbors, and placeMines
  Minesweeper(int rows, int cols, int mines, ArrayList<Cell> grid, boolean notInit,
      Random randSeed) {
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.grid = grid;
    this.randSeed = randSeed;
    this.gameLost = false;

  }

  //to initialize the grid with cells
  void initGrid() {
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.cols; c++) {
        Cell cell = new Cell();
        this.grid.add(cell);
      }
    }
    
    //to link neighbors for each cell
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.cols; c++) {
        this.linkNeighbors(r, c);
      }
    }
  }

  //to link neighbors for a cell
  void linkNeighbors(int row, int col) {
    Cell cell = this.getCell(row, col);
    for (int dr = -1; dr <= 1; dr++) {
      for (int dc = -1; dc <= 1; dc++) {
        int newRow = row + dr;
        int newCol = col + dc;
        if (!(dr == 0 && dc == 0) && isValidCell(newRow, newCol)) {
          cell.neighbors.add(this.getCell(newRow, newCol));
        }
      }
    }
  }

  //to get the cell at the given row and column from this grid
  Cell getCell(int row, int col) {
    return this.grid.get(row * this.cols + col);
  }

  //to check if given coordinates are valid in this grid
  boolean isValidCell(int row, int col) {
    return row >= 0 && row < this.rows && col >= 0 && col < this.cols;
  }
  
  //to place mines randomly in grid
  void placeMines() {
    
    Random rand = this.randSeed;
    ArrayList<Integer> mineIndices = new ArrayList<>(); //store indices in list
    while (mineIndices.size() < this.mines) {
      int randIndex = rand.nextInt(this.rows * this.cols);
      if (!this.grid.get(randIndex).isMine && !mineIndices.contains(randIndex)) {
        this.grid.get(randIndex).isMine = true;
        mineIndices.add(randIndex);
      }
    }
  }
    
  //to counts the number of mines neighboring a given cell
  int countNeighboringMines(Cell cell) {
    int count = 0;
    for (Cell neighbor : cell.neighbors) {
      if (neighbor.isMine) {
        count++;
      }
    }
    return count;
  }

  //to draw the game 
  public WorldScene makeScene() {
    
    WorldScene scene = new WorldScene(IMinesweeper.SCREEN_HEIGHT, IMinesweeper.SCREEN_WIDTH);
    //image of mines left score
    WorldImage minesLeft = new TextImage("Mines left: " + Integer.toString(
        this.mines - this.countFlaggedMines()), 
        IMinesweeper.CELL_SIZE - 4, FontStyle.REGULAR, Color.ORANGE);
    
    //place background
    scene.placeImageXY(IMinesweeper.BACKGROUND, 0, 0);
    //place mines left score
    scene.placeImageXY(minesLeft, IMinesweeper.SCREEN_HEIGHT / 2,
        IMinesweeper.SCREEN_WIDTH - (IMinesweeper.CELL_SIZE / 2));
    //place restart text
    scene.placeImageXY(IMinesweeper.RESTART_TEXT, IMinesweeper.SCREEN_HEIGHT / 2,
        IMinesweeper.CELL_SIZE / 2);

    //show win screen if game is won
    if (this.countFlaggedMines() == this.mines) {
      scene.placeImageXY(IMinesweeper.WIN_SCREEN, IMinesweeper.SCREEN_HEIGHT / 2,
          IMinesweeper.SCREEN_WIDTH / 2);
      return scene;
    }
    //show lose screen if game is lost
    if (this.gameLost) {
      scene.placeImageXY(IMinesweeper.LOSE_SCREEN, IMinesweeper.SCREEN_HEIGHT / 2,
          IMinesweeper.SCREEN_WIDTH / 2);
      return scene;
    }
    
    //draw cells
    int cellSize = IMinesweeper.CELL_SIZE;
    
    //iterate through all cells and draw them
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.cols; c++) {
        Cell cell = this.grid.get(r * this.cols + c);
        WorldImage cellImage = cell.drawCell(cellSize);
        scene.placeImageXY(cellImage, c * cellSize + (cellSize / 2 + cellSize),
            r * cellSize + (cellSize / 2 + cellSize));
      }
    }   
    
    return scene;
    
  }
  
  //to handle mouse clicks by the user
  public void onMouseClicked(Posn pos, String button) {
    //check if mouse is in bounds of board
    if (pos.x > IMinesweeper.CELL_SIZE && pos.y > IMinesweeper.CELL_SIZE
        && pos.x < IMinesweeper.SCREEN_HEIGHT - IMinesweeper.CELL_SIZE
        && pos.y < IMinesweeper.SCREEN_WIDTH - IMinesweeper.CELL_SIZE) {
      
      int row = pos.y / IMinesweeper.CELL_SIZE;
      int col = pos.x / IMinesweeper.CELL_SIZE;
      
      //reveal cell that was clicked on
      if (button.equals("LeftButton")) {
        Cell clickedCell = getCell(row - 1, col - 1);
        if (!clickedCell.isFlagged) {
          this.revealCell(clickedCell);
        }
      }
      
      //make cell that was clicked on into flag
      if (button.equals("RightButton")) {
        this.toggleFlag(row, col, pos.x, pos.y);
      }
      
    }
  }
  
  //to restart the game if the user presses the "r" key
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      
      //restart game after losing
      if (this.gameLost) {
        this.gameLost = false;
      }
      
      //restart in middle of game
      ArrayList<Cell> newGrid = new ArrayList<Cell>();
      this.grid = newGrid;
      this.initGrid();
      this.placeMines();
    }
  }
  
  //to reveal a cell in game
  void revealCell(Cell cell) {
    
    //reveal non-mine cell and flood neighbors that have no neighboring mines
    if (!cell.isRevealed && cell.countNeighboringMines() == 0 && !cell.isMine) {
      cell.isRevealed = true;      
      
      for (Cell neighbor : cell.neighbors) {
        if (!neighbor.isMine) {
          revealCell(neighbor);                
        }
      }
      
      this.countFlaggedMines();
      
    }
    //reveal non-mine cell but does not flood neighbors that have neigboring mines
    if (!cell.isRevealed && cell.countNeighboringMines() != 0 && !cell.isMine) {
      cell.isRevealed = true;
    }
    //end game is cell is mine
    if (cell.isMine) {
      this.gameLost = true;
    }
  }
  
  //to toggle flag state of a cell on/off
  void toggleFlag(int row, int col, int posX, int posY) {
    if (posX > IMinesweeper.CELL_SIZE && posY > IMinesweeper.CELL_SIZE
        && posX < IMinesweeper.SCREEN_HEIGHT - IMinesweeper.CELL_SIZE
        && posY < IMinesweeper.SCREEN_WIDTH - IMinesweeper.CELL_SIZE) {
      Cell cell = getCell(row - 1, col - 1);
      if (!cell.isRevealed) {
        cell.isFlagged = !cell.isFlagged;
      }
    }
  }
  
  //to determine how many mines are correctly flagged 
  int countFlaggedMines() {
    int allMineFlagged = 0;
        
    for (Cell cell : grid) {
      if (cell.isFlagged && cell.isMine) {
        allMineFlagged++;
      }
    }
    
    return allMineFlagged;
  }
    
}

//to represent a cell in the game grid
class Cell {
  
  boolean isMine; 
  boolean isRevealed;
  boolean isFlagged; 
  ArrayList<Cell> neighbors;

  Cell() {
    this.isMine = false;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }
    
  //Convenience constructor for testing
  Cell(boolean isMine, boolean isRevealed, Boolean isFlagged) {
    this.isMine = isMine;
    this.isRevealed = isRevealed;
    this.isFlagged = isFlagged;
    this.neighbors = new ArrayList<Cell>();
  }
  
  //Convenience constructor for testing with also giving list of neighbors
  Cell(boolean isMine, boolean isRevealed, Boolean isFlagged, ArrayList<Cell> neighbors) {
    this.isMine = isMine;
    this.isRevealed = isRevealed;
    this.isFlagged = isFlagged;
    this.neighbors = neighbors;
  }
    
  //Convenience constructor for testing that automatically reveals cell
  Cell(boolean isRevealed) {
    this.isMine = false;
    this.isRevealed = true;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }
    
    
  //to count mines that neighbor this cell
  int countNeighboringMines() {
    int count = 0;
    for (Cell neighbor : this.neighbors) {
      if (neighbor.isMine) {
        count++;
      }
    }
    return count;
  }

  //to draw this cell
  WorldImage drawCell(int size) {
    
    Color cellColor = Color.LIGHT_GRAY; 
    Color textColor = Color.BLACK; 
    
    if (this.isRevealed) {
      cellColor = Color.WHITE; //reveal white background cell color
      if (this.isMine) {
        textColor = Color.RED; //color for mine cell text
      } else {
        textColor = Color.BLUE; //color for neighboring mine count text
      }
    } else if (this.isFlagged) {
      cellColor = Color.ORANGE; //flagged cell color
    }

    //create black outline for cell
    WorldImage cellOutline = new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK);
    //create rectangle representing the cell
    WorldImage cellImage = new RectangleImage(size, size, OutlineMode.SOLID, cellColor);
    cellImage = new OverlayImage(cellOutline, cellImage);

    //add text (mine count or flag) if the cell is revealed
    if (this.isRevealed) {
      String text = "";
      if (this.isMine) {
        text = "M"; //symbol for mine
      } else {
        text = Integer.toString(this.countNeighboringMines()); //convert count to string
      }
      
      WorldImage textImage = new TextImage(text, size / 2, textColor);
      cellImage = new OverlayImage(textImage, cellImage);
    }

    return cellImage;
    
  }
    
}

// Example test class
class ExamplesMinesweep {
  
  //cell examples
  Cell notRevealNorm;
  Cell notRevealMine;
  Cell notRevealFlag;
  Cell notRevealFlagMine;
  Cell revealNorm;
  Cell revealNormNeighbors;
  Cell revealMine;
  Cell revealFlag;
  Cell revealMineFlag;
  Cell notRevealNormInit;
  
  //array list of cell examples
  ArrayList<Cell> mixOfDiffCells;
  ArrayList<Cell> cellsNine;
  
  //example game setup with normal 30x16
  Minesweeper gameNorm; 
  //example game setup with normal 30x16 for testing
  Minesweeper gameNormTest;
  //example game setup with normal 30x16 for testing
  Minesweeper gameSmaller;
  
  //example games with different number of mines for testing
  Minesweeper gamePlaceNoMines;
  Minesweeper gamePlaceOneMine;
  Minesweeper gamePlaceTwoMines;
  Minesweeper gamePlaceFourMines;
  
  //example game that is not initialized
  Minesweeper gameNotInit;  
  //example game where all cell are revealed
  Minesweeper gameRevealed;
  //example game where all cell are revealed and no random seed provided
  Minesweeper gameRevealedRandom;
  //example game where multiple mines are specifically chosen
  Minesweeper gamePlaceSpecificMines;
  //example game with multiple flagged mines
  Minesweeper gameMultFlaggedMines;
  
  //empty worldscene
  WorldScene empty;
  
  //images of score
  WorldImage zeroMineLeft;
  WorldImage oneMineLeft;
  
  public void reset() {
    
    //cell examples
    notRevealNorm = new Cell(false, false, false);
    notRevealMine = new Cell(true, false, false);
    notRevealFlag = new Cell(false, false, true);
    notRevealFlagMine = new Cell(true, false, true);
    revealNorm = new Cell(false, true, false);
    revealNormNeighbors = new Cell(false, true, false, new ArrayList<Cell>(
        Arrays.asList(new Cell(true, false, false), new Cell(true, false, false))));
    revealMine = new Cell(true, true, false);
    revealFlag = new Cell(false, false, true);
    revealMineFlag = new Cell(true, true, true);
    notRevealNormInit = new Cell(false, false, false,
        new ArrayList<Cell>(Arrays.asList(new Cell())));
        
    //array list of cell examples (used for 30x16 intermediate game board so it is pretty large)
    mixOfDiffCells = new ArrayList<Cell>(Arrays.asList(
        new Cell(false, false, false),  new Cell(true, false, false), new Cell(false, false, true), 
        new Cell(true, false, false), new Cell(true, false, false),new Cell(false, true, false),
        new Cell(false, false, false), new Cell(true, true, false), new Cell(false, false, false),
        new Cell(true, false, false), new Cell(true, false, false), new Cell(false, true, false),
        new Cell(true, true, false), new Cell(false, true, true),  new Cell(true, false, false),
        new Cell(false, false, false), new Cell(true, true, false), new Cell(false, false, false),
        new Cell(true, false, false), new Cell(false, true, false), new Cell(true, false, false),
        new Cell(false, true, false),  new Cell(true, false, false), new Cell(false, false, true), 
        new Cell(true, false, false), new Cell(true, false, false),new Cell(false, true, false),
        new Cell(false, false, false), new Cell(true, true, false), 
        new Cell(true, true, false), 
        new Cell(false, false, false),new Cell(true, false, false), new Cell(true, false, false), 
        new Cell(false, true, false), new Cell(true, true, false), new Cell(false, true, true),  
        new Cell(true, false, false), new Cell(false, false, false), new Cell(true, true, false), 
        new Cell(true, false, false), new Cell(false, true, false), new Cell(true, false, false),
        new Cell(false, true, false),  new Cell(true, false, false), new Cell(false, false, false),
        new Cell(true, false, false), new Cell(true, false, false), 
        new Cell(false, true, false), new Cell(true, true, false), new Cell(false, true, true),
        new Cell(true, false, false),
        new Cell(false, true, false),  new Cell(true, false, false), new Cell(false, false, false),
        new Cell(false, false, false),
        new Cell(true, false, false), new Cell(false, true, false), new Cell(true, false, false),
        new Cell(false, true, false),  new Cell(true, false, false), new Cell(false, false, true), 
        new Cell(true, false, false), new Cell(true, false, true),new Cell(false, true, false),
        new Cell(false, false, false), new Cell(true, true, false),  new Cell(false, true, false),  
        new Cell(true, false, false), new Cell(false, false, true), 
        new Cell(true, false, false), new Cell(true, false, false),new Cell(false, true, false),
        new Cell(false, false, false), new Cell(true, true, false), new Cell(false, false, false),
        new Cell(true, false, false), new Cell(true, false, false), new Cell(false, true, false),
        new Cell(true, true, false), new Cell(false, true, true),  new Cell(true, false, false),
        new Cell(false, false, false), new Cell(true, true, false), new Cell(false, false, false),
        new Cell(true, false, false), new Cell(false, true, false), new Cell(true, false, false),
        new Cell(false, true, false),  new Cell(true, false, false), new Cell(false, false, true), 
        new Cell(true, false, false), new Cell(true, false, false),new Cell(false, true, false),
        new Cell(false, false, false), new Cell(true, true, false)));
    
    cellsNine = new ArrayList<Cell>(Arrays.asList(
        new Cell(false, true, false),  new Cell(true, false, false), new Cell(false, false, true), 
        new Cell(true, false, false), new Cell(true, false, false),new Cell(false, false, false),
        new Cell(false, false, false), new Cell(true, false, false), 
        new Cell(false, false, false)));
    
    //example game setup with normal 30x16
    gameNorm = new Minesweeper(IMinesweeper.ROWS, IMinesweeper.COLUMNS, IMinesweeper.MINES); 
        
    //example game setup with normal 30x16 for testing using given grid of mixed cells
    gameNormTest = new Minesweeper(IMinesweeper.ROWS, IMinesweeper.COLUMNS, 0, this.mixOfDiffCells,
        new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game of smaller 3x3 grid for testing
    gameSmaller = new Minesweeper(3, 3, 0, this.cellsNine, new Random(1));
    
    //example game setup with 3x3 with random mines, and all cells revealed
    gameRevealed = new Minesweeper(3, 3, 2, new ArrayList<Cell>(
        Arrays.asList(new Cell(true), new Cell(true), new Cell(true), new Cell(true),
            new Cell(true), new Cell(true), new Cell(true), new Cell(true), new Cell(true))),
        new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game setup with 3x3 with random mines, 
    //and all cells revealed with random mines (no seed)
    gameRevealedRandom = new Minesweeper(3, 3, 2, new ArrayList<Cell>(
        Arrays.asList(new Cell(true), new Cell(true), new Cell(true), new Cell(true),
            new Cell(true), new Cell(true), new Cell(true), new Cell(true), new Cell(true))));  
    
    gamePlaceSpecificMines = new Minesweeper(2, 1, 1, new ArrayList<Cell>(
        Arrays.asList(new Cell(), new Cell())),true, new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game with no mines
    gamePlaceNoMines = new Minesweeper(2, 2, 0, new ArrayList<Cell>(
        Arrays.asList(new Cell(false, false, false), new Cell(false, false, false), 
            new Cell(false, false, false), new Cell(false, false, false))), 
         true, new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game with one mine
    gamePlaceOneMine = new Minesweeper(1, 1, 1, new ArrayList<Cell>(Arrays.asList(new Cell())),
        true, new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game with two mines
    gamePlaceTwoMines = new Minesweeper(2, 1, 2, new ArrayList<Cell>(
        Arrays.asList(new Cell(), new Cell())), 
        true, new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game with four mine
    gamePlaceFourMines = new Minesweeper(3, 3, 4, new ArrayList<Cell>(
        Arrays.asList(new Cell(), new Cell(), new Cell(), new Cell(), new Cell(), new Cell(),
            new Cell(), new Cell(), new Cell())), 
        true, new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game that grid is not initialized
    gameNotInit = new Minesweeper(2, 1, 0, new ArrayList<Cell>(Arrays.asList(new Cell(), 
        new Cell())), true, new Random(IMinesweeper.RAND_TEST_SEED));
    
    //example game with multiple flagged mines
    gameMultFlaggedMines = new Minesweeper(2, 1, 2, new ArrayList<Cell>(
        Arrays.asList(new Cell(true, false, true), new Cell(true, false, true))), 
        true, new Random(IMinesweeper.RAND_TEST_SEED));
    
    //empty worldScene
    empty = new WorldScene(IMinesweeper.SCREEN_HEIGHT, IMinesweeper.SCREEN_WIDTH);
    
    //images of score
    zeroMineLeft = new TextImage(
        "Mines left: " + Integer.toString(0), 21, FontStyle.REGULAR, Color.ORANGE);
    oneMineLeft = new TextImage(
        "Mines left: " + Integer.toString(1), 21, FontStyle.REGULAR, Color.ORANGE);
    
  }
  
  //to view appearance of game
  void testBigBang(Tester t) {
    this.reset();
     
    
    //test normal intermediate board
    gameNorm.bigBang(IMinesweeper.SCREEN_HEIGHT, IMinesweeper.SCREEN_WIDTH);

    //    //test smaller 9x9 board
    //    gameSmaller.bigBang(
    //        this.gameSmaller.rows * IMinesweeper.CELL_SIZE + (IMinesweeper.CELL_SIZE * 2), 
    //        (this.gameSmaller.cols * IMinesweeper.CELL_SIZE + (IMinesweeper.CELL_SIZE * 2)));
    //      
    //    //test smaller 9x9 board that is all revealed
    //    gameRevealed.bigBang(
    //        this.gameSmaller.rows * IMinesweeper.CELL_SIZE + (IMinesweeper.CELL_SIZE * 2), 
    //        (this.gameSmaller.cols * IMinesweeper.CELL_SIZE + (IMinesweeper.CELL_SIZE * 2)));
    //     
    //    //test smaller 9x9 board that is all reveled and has random mines
    //    gameRevealedRandom.bigBang(
    //        this.gameSmaller.rows * IMinesweeper.CELL_SIZE + (IMinesweeper.CELL_SIZE * 2), 
    //        (this.gameSmaller.cols * IMinesweeper.CELL_SIZE + (IMinesweeper.CELL_SIZE * 2)));
    //    //test game with one mine
    //    gamePlaceOneMine.bigBang(IMinesweeper.SCREEN_HEIGHT, IMinesweeper.SCREEN_WIDTH);     
      
  }
    
  //to test initGrid method
  void testInitGrid(Tester t) {
    this.reset();
      
    //test InitGrid by testing if a cell has neighbors, before method
    //cell should have no neighbors, so size should be 0,
    //after method called neighbors size should be greater than 0
    t.checkExpect(this.gameNotInit.grid.get(0).neighbors.size() > 0, false);
    this.gameNotInit.initGrid();
    t.checkExpect(this.gameNotInit.grid.get(0).neighbors.size() > 0, true);
    //test on other cell in grid
    t.checkExpect(this.gameNotInit.grid.get(1).neighbors.size() > 0, true);
                       
  }
    
  //to test placeMines method
  void testPlaceMines(Tester t) {
    this.reset();
    
    //place no mines
    t.checkExpect(gamePlaceNoMines.grid, 
        new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell(), new Cell())));
    this.gamePlaceNoMines.placeMines();
    t.checkExpect(gamePlaceNoMines.grid, 
        new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell(), new Cell())));
      
    //place one mine
    t.checkExpect(gamePlaceOneMine.grid, new ArrayList<Cell>(Arrays.asList(new Cell())));
    this.gamePlaceOneMine.placeMines();
    t.checkExpect(gamePlaceOneMine.grid, 
        new ArrayList<Cell>(Arrays.asList(new Cell(true, false, false))));
      
    //place multiple mines
    t.checkExpect(gamePlaceTwoMines.grid, 
        new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell())));
    this.gamePlaceTwoMines.placeMines();
    t.checkExpect(gamePlaceTwoMines.grid, new ArrayList<Cell>(
        Arrays.asList(new Cell(true, false, false), new Cell(true, false, false))));
      
  }
    
  //test linkNeighbors method
  void testLinkNeighbors(Tester t) {
    this.reset();
        
    //test neighbor of a corner cell
    t.checkExpect(gamePlaceNoMines.grid.get(0), new Cell());
    gamePlaceNoMines.linkNeighbors(0, 0);
    t.checkExpect(gamePlaceNoMines.grid.get(0),
        new Cell(false, false, false,
            new ArrayList<Cell>(Arrays.asList(new Cell(), 
                new Cell(),new Cell()))));
      
    //test neighbor of a middle cell
    t.checkExpect(gamePlaceFourMines.grid.get(4), new Cell());
    gamePlaceFourMines.linkNeighbors(1, 1);
    t.checkExpect(gamePlaceFourMines.grid.get(4),
        new Cell(false, false, false,
            new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell(),
                new Cell(), new Cell(), new Cell(), new Cell(), new Cell()))));    
      
    this.reset();
      
    //test neighbor of a side cell
    t.checkExpect(gamePlaceFourMines.grid.get(3), new Cell());
    gamePlaceFourMines.linkNeighbors(1, 0);
    t.checkExpect(gamePlaceFourMines.grid.get(3),
        new Cell(false, false, false,
            new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell(),
                new Cell(), new Cell()))));
      
  }
    
  //to test getCell method
  void testGetCell(Tester t) {
    this.reset();
    
    //test first row
    t.checkExpect(this.gameNormTest.getCell(0, 0), this.gameNormTest.grid.get(0));
    t.checkExpect(this.gameNormTest.getCell(0, 1), this.gameNormTest.grid.get(1));
    t.checkExpect(this.gameNormTest.getCell(0, 4), this.gameNormTest.grid.get(4));
    t.checkExpect(this.gameNormTest.getCell(0, 5), this.gameNormTest.grid.get(5)); 
      
    //test different combinations of rows and columns
    t.checkExpect(this.gameSmaller.getCell(0, 0), this.gameSmaller.grid.get(0));
    t.checkExpect(this.gameSmaller.getCell(2, 2), this.gameSmaller.grid.get(8));
    t.checkExpect(this.gameSmaller.getCell(1, 2),  this.gameSmaller.grid.get(5));

  }
    
  //to test isValidCell method
  void testIsValidCell(Tester t) {
    this.reset();
    
    //test inside of grid
    t.checkExpect(this.gameNormTest.isValidCell(0, 0), true);
    t.checkExpect(this.gameNormTest.isValidCell(15, 29), true);
    t.checkExpect(this.gameNormTest.isValidCell(4, 17), true);

    //test outside of grid
    t.checkExpect(this.gameNormTest.isValidCell(-1, 0), false);
    t.checkExpect(this.gameNormTest.isValidCell(-1, 0), false);
    t.checkExpect(this.gameNormTest.isValidCell(16, 29), false);
    t.checkExpect(this.gameNormTest.isValidCell(16, 30), false);

  }
    
  //to test countNeighboringMines method
  void testCountNeighboringMines(Tester t) {
    this.reset();
      
    //test Minesweeper countNeighboringMines
    t.checkExpect(this.gameSmaller.countNeighboringMines(this.gameSmaller.getCell(0, 0)), 3);
    t.checkExpect(this.gameSmaller.countNeighboringMines(this.gameSmaller.getCell(2, 2)), 2);
    t.checkExpect(this.gameSmaller.countNeighboringMines(this.gameSmaller.getCell(1, 2)), 3);

    //test Cell countNeighboringMines
    t.checkExpect(this.gameSmaller.getCell(0, 0).countNeighboringMines(), 3);
    t.checkExpect(this.gameSmaller.getCell(1, 1).countNeighboringMines(), 3);
    t.checkExpect(this.gameSmaller.getCell(2, 2).countNeighboringMines(), 2);

  }
  
  //to test drawCell method
  void testDrawCell(Tester t) {
    this.reset();
    
    //test drawing a not revealed normal cell
    t.checkExpect(this.notRevealNorm.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(25, 25, OutlineMode.SOLID, Color.LIGHT_GRAY)));
      
    //test drawing a not revealed mine cell
    t.checkExpect(this.notRevealMine.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(25, 25, OutlineMode.SOLID, Color.LIGHT_GRAY)));
      
    //test drawing a not revealed flag cell
    t.checkExpect(this.notRevealFlag.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE)));
      
    //test drawing a not revealed flag mine cell
    t.checkExpect(this.notRevealFlag.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE)));
      
    //test drawing a revealed normal cell
    t.checkExpect(this.revealNorm.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new TextImage("0", IMinesweeper.CELL_SIZE / 2, Color.BLUE), 
            new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
                new RectangleImage(25, 25, OutlineMode.SOLID, Color.WHITE))));
      
    //test drawing a revealed normal cell with multiplemine neighbors
    t.checkExpect(this.revealNormNeighbors.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new TextImage("2", IMinesweeper.CELL_SIZE / 2, Color.BLUE), 
            new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
                new RectangleImage(25, 25, OutlineMode.SOLID, Color.WHITE))));
           
    //test drawing a revealed mine cell
    t.checkExpect(this.revealMine.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new TextImage("M", IMinesweeper.CELL_SIZE / 2, Color.RED), 
            new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
                new RectangleImage(25, 25, OutlineMode.SOLID, Color.WHITE))));
      
    //test drawing a revealed flag cell
    t.checkExpect(this.revealFlag.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE)));
      
    //test drawing a revealed mine flag cell
    t.checkExpect(this.revealMineFlag.drawCell(IMinesweeper.CELL_SIZE),
        new OverlayImage(new TextImage("M", IMinesweeper.CELL_SIZE / 2, Color.RED), 
            new OverlayImage(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.BLACK),
                  new RectangleImage(25, 25, OutlineMode.SOLID, Color.WHITE))));    
  }
    
  //to test the makeScene method
  void testMakeScene(Tester t) {
    this.reset();       
    
    //test drawing one cell
   
    empty.placeImageXY(IMinesweeper.BACKGROUND, 0, 0);
    empty.placeImageXY(this.oneMineLeft, IMinesweeper.SCREEN_HEIGHT / 2, 
        IMinesweeper.SCREEN_WIDTH - (IMinesweeper.CELL_SIZE / 2));
    empty.placeImageXY(IMinesweeper.RESTART_TEXT, IMinesweeper.SCREEN_HEIGHT / 2,
        IMinesweeper.CELL_SIZE / 2);
    empty.placeImageXY(this.gamePlaceOneMine.getCell(0, 0).drawCell(IMinesweeper.CELL_SIZE), 
        37, 37);
    
    t.checkExpect(this.gamePlaceOneMine.makeScene(), empty);
      
    this.reset();
      
    empty.placeImageXY(IMinesweeper.BACKGROUND, 0, 0);
    empty.placeImageXY(this.zeroMineLeft, IMinesweeper.SCREEN_HEIGHT / 2, 
        IMinesweeper.SCREEN_WIDTH - (IMinesweeper.CELL_SIZE / 2));
    empty.placeImageXY(IMinesweeper.RESTART_TEXT, IMinesweeper.SCREEN_HEIGHT / 2,
        IMinesweeper.CELL_SIZE / 2);
    empty.placeImageXY(IMinesweeper.WIN_SCREEN, 
        IMinesweeper.SCREEN_HEIGHT / 2, IMinesweeper.SCREEN_WIDTH / 2);
    t.checkExpect(this.gamePlaceNoMines.makeScene(), empty);
           
  }
  
  //to test the onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.reset();   
    
    //test left click on unrevealed cell
    t.checkExpect(this.gameNormTest.grid.get(0).isRevealed, false);
    this.gameNormTest.onMouseClicked(new Posn(30, 30), "LeftButton");
    t.checkExpect(this.gameNormTest.grid.get(0).isRevealed, true);
    
    //test left click on already revealed cell
    t.checkExpect(this.gameNormTest.grid.get(5).isRevealed, true);
    this.gameNormTest.onMouseClicked(new Posn(150, 30), "LeftButton");
    t.checkExpect(this.gameNormTest.grid.get(5).isRevealed, true);
    
    //test left click on already flagged cell
    t.checkExpect(this.gameNormTest.grid.get(2).isFlagged, true);
    this.gameNormTest.onMouseClicked(new Posn(95, 30), "LeftButton");
    t.checkExpect(this.gameNormTest.grid.get(2).isFlagged, true);
    
    //test right click on unrevealed mine cell
    t.checkExpect(this.gameNormTest.grid.get(1).isFlagged, false);
    this.gameNormTest.onMouseClicked(new Posn(55, 30), "RightButton");
    t.checkExpect(this.gameNormTest.grid.get(1).isFlagged, true);
    
    //test right click on non-mine cell
    this.reset();
    t.checkExpect(this.gameNormTest.grid.get(0).isFlagged, false);
    this.gameNormTest.onMouseClicked(new Posn(30, 30), "RightButton");
    t.checkExpect(this.gameNormTest.grid.get(0).isFlagged, true);
    
    //test right click on revealed cell
    t.checkExpect(this.gameNormTest.grid.get(5).isFlagged, false);
    this.gameNormTest.onMouseClicked(new Posn(150, 30), "RightButton");
    t.checkExpect(this.gameNormTest.grid.get(5).isFlagged, false);
    
  }
  
  //to test the onKeyEvent method
  void testOnKeyEvent(Tester t) {
    this.reset();
    
    //test restart on a revealed cell
    t.checkExpect(this.gameNormTest.grid.get(5).isRevealed, true);
    this.gameNormTest.onKeyEvent("r");
    t.checkExpect(this.gameNormTest.grid.get(5).isRevealed, false);   
    
    //test restart on a mine cell
    this.reset();
    t.checkExpect(this.gameNormTest.grid.get(1).isMine, true);
    this.gameNormTest.onKeyEvent("r");
    t.checkExpect(this.gameNormTest.grid.get(1).isMine, false);
    
    //test restart when game is over
    this.reset();
    this.gameNormTest.onMouseClicked(new Posn(55, 30), "LeftButton");
    t.checkExpect(this.gameNormTest.gameLost, true);
    this.gameNormTest.onKeyEvent("r");
    t.checkExpect(this.gameNormTest.gameLost, false);

  }
  
  //to test revealCell method
  void testRevealCell(Tester t) {

    this.reset();
    
    //tests case of no neighboring mines
    t.checkExpect(this.gamePlaceNoMines.getCell(0, 0).isRevealed, false);
    this.gamePlaceNoMines.revealCell(this.gamePlaceNoMines.getCell(0, 0));
    this.gamePlaceOneMine.revealCell(this.gamePlaceNoMines.getCell(0,0));
    t.checkExpect(this.gamePlaceNoMines.getCell(0, 0).isRevealed, true);
    
    //tests case of neighboring mines
    t.checkExpect(gamePlaceSpecificMines.getCell(0, 0).isRevealed, false);
    gamePlaceSpecificMines.revealCell(gamePlaceSpecificMines.getCell(0, 0));
    t.checkExpect(gamePlaceSpecificMines.getCell(0, 0).isRevealed, true);
    
    //tests case of revealing a mine (game over)
    t.checkExpect(this.gameNormTest.gameLost, false);
    gameNormTest.revealCell(gameNormTest.grid.get(1));
    t.checkExpect(this.gameNormTest.gameLost, true);
    
  }
  
  //to test toggleFlag method
  void testToggleFlag(Tester t) {
    this.reset();
    
    //test unrevealed cell
    t.checkExpect(this.gameNormTest.getCell(0, 0).isFlagged, false);
    this.gameNormTest.toggleFlag(1, 1, 30, 30);
    t.checkExpect(this.gameNormTest.getCell(0, 0).isFlagged, true);
    
    //test flagged cell
    t.checkExpect(this.gameNormTest.getCell(2,0).isFlagged, true);
    this.gameNormTest.toggleFlag(2, 1, 100, 30);
    t.checkExpect(this.gameNormTest.getCell(2,0).isFlagged, true);
    
    //test revealed cell
    this.reset();
    t.checkExpect(this.gameNormTest.grid.get(5).isRevealed, true);
    this.gameNormTest.toggleFlag(5, 1, 150, 30);
    t.checkExpect(this.gameNormTest.grid.get(5).isRevealed, true);

  }
  
  //to test the countFlaggedMines method
  void testCountFlaggedMines(Tester t) {
    this.reset();
    
    //test grid with no flagged mine
    t.checkExpect(this.gamePlaceFourMines.countFlaggedMines(), 0);
    
    //test grid with one flag mine
    t.checkExpect(this.gameNormTest.countFlaggedMines(), 1);  
    
    //test grid with multiple flag mines
    t.checkExpect(this.gameMultFlaggedMines.countFlaggedMines(), 2);
    
  }
}
