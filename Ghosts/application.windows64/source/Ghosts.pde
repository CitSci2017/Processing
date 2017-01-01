//Numerical simulation of SIZ model
//Created Nov_2016 (jangueyra@bard.edu)

//Description:
//    Population is laid out on canvas randomly
//    Each subject moves towards a random direction and bounces off the walls
//    Mouse hovering over subjects interrupts movement
//    Mouse clicking a subject infects it
//    Infected subjects have chance to become zombies
//    Zombies infect nearby subjects

// Simulation parameters
int canvasX = 600; //size of the canvas
int canvasY = 600; //size of the canvas
float canvasC = 255/2; //color of the canvas
int nGhosts = 200; //number of ghosts
float zombifyProb = 0.1; //probability per frame to go from infected to zombie
float infectionProb = 0.2; //probability per frame for zombie to bite nearby ghosts
Table nCounts = new Table(); //table to save data
int nFrame = 0; //time axis (in units of frames)
int nS = 0; //count of susceptible subjects (updates every frame)
int nI = 0; //count of infected subjects (updates every frame)
int nZ = 0; //count of zombie subjects (updates every frame)



ghost[] g = new ghost[nGhosts];
int[] initialX = new int[nGhosts];
int[] initialY = new int[nGhosts];

// SETUP happens once (Processing default)
void setup() {
  size(600, 600, P2D);
  ellipseMode(CENTER);
  rectMode(CENTER);
  background(canvasC);
  frameRate(40);
  //initialize ghosts with random starting position
  for (int i=0; i<nGhosts; i++) {
    initialX[i] = int(random(0, canvasX));
    initialY[i] = int(random(0, canvasY));
    g[i] = new ghost(initialX[i], initialY[i], 35);
  }
  nCounts.addColumn("nFrame");
  nCounts.addColumn("nS");
  nCounts.addColumn("nI");
  nCounts.addColumn("nZ");
  nCounts.addColumn("nT");
}

// DRAW happens every frame (Processing default)
void draw() {
  background(canvasC);
  for (int i=0; i<nGhosts; i++) {
    if (g[i].status == "infected") {
      //infected ghosts can become zombies
      if (zombifyProb > random(0, 1)) {
        g[i].status = "zombie";
      }
    } else if (g[i].status == "zombie") {
      //check if zombie ghosts have adjacent ghosts that can be infected
      for (int other_i=0; other_i<nGhosts; other_i++) {
        if (g[other_i].status!="zombie" && g[other_i].status!="immune" && g[i].overlap(g[other_i]) && infectionProb > random(0, 1)) {
          g[other_i].status="infected";
        }
      }
    } 
  }
  nS = 0;
  nI = 0;
  nZ = 0;
  for (int i=0; i<nGhosts; i++) {
    g[i].display();
    if (g[i].status=="susceptible" || g[i].status=="marked") {
      nS = nS + 1;
    } else if (g[i].status=="infected") {
      nI = nI + 1;
    } else if (g[i].status=="zombie") {
      nZ = nZ + 1;
    }
  }
  TableRow newRow = nCounts.addRow();
  newRow.setInt("nFrame", frameCount);
  newRow.setInt("nS", nS);
  newRow.setInt("nI", nI);
  newRow.setInt("nZ", nZ);
  newRow.setInt("nT", nGhosts);
  
  saveTable(nCounts, "data/nCounts.csv");
}