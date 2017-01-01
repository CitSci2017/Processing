import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Ghosts extends PApplet {

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
float zombifyProb = 0.1f; //probability per frame to go from infected to zombie
float infectionProb = 0.2f; //probability per frame for zombie to bite nearby ghosts
Table nCounts = new Table(); //table to save data
int nFrame = 0; //time axis (in units of frames)
int nS = 0; //count of susceptible subjects (updates every frame)
int nI = 0; //count of infected subjects (updates every frame)
int nZ = 0; //count of zombie subjects (updates every frame)



ghost[] g = new ghost[nGhosts];
int[] initialX = new int[nGhosts];
int[] initialY = new int[nGhosts];

// SETUP happens once (Processing default)
public void setup() {
  
  ellipseMode(CENTER);
  rectMode(CENTER);
  background(canvasC);
  frameRate(40);
  //initialize ghosts with random starting position
  for (int i=0; i<nGhosts; i++) {
    initialX[i] = PApplet.parseInt(random(0, canvasX));
    initialY[i] = PApplet.parseInt(random(0, canvasY));
    g[i] = new ghost(initialX[i], initialY[i], 35);
  }
  nCounts.addColumn("nFrame");
  nCounts.addColumn("nS");
  nCounts.addColumn("nI");
  nCounts.addColumn("nZ");
  nCounts.addColumn("nT");
}

// DRAW happens every frame (Processing default)
public void draw() {
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
// Ghost class: small animation updates depending on frameCount
// By default starts as susceptible. Marked when mouse hovers over it.
// Infected if clicked, then chance of zombifying. Zombies can infect
// nearby ghosts.

class ghost {
  float x, y, w, h; //x and y, width and heigth
  float d; //moving angle
  int sprite = 0; //animation statis
  boolean mouseOver = false; //detection of mouse hovering over
  String status = "susceptible"; // current status
  int name_i = PApplet.parseInt(random(4)); // identity
  String name;
  int c;
  float t_xwalk = 0; // for movement
  float t_ywalk = 0; // for movement
  float immuneProb = 0.0f;

  ghost (float xpos, float ypos, float sze) {
    x = xpos;
    y = ypos;
    w = sze;
    h = w * 7 /8;
    d = random(0, TWO_PI);
    if (random(0,1)<immuneProb) {
      status = "immune";
    }
    if (name_i == 0) {
      name = "blinky";
      c = color(255, 0, 0);
    } else if (name_i == 1) {
      name = "pinky";
      c = color(255, 184, 222);
    } else if (name_i == 2) {
      name = "inky";
      c = color(0, 255, 255);
    } else if (name_i == 3) {
      name = "clyde";
      c = color(255, 184, 71);
    } else {
      c = color(120, 120, 120);
    }
  }

  public void display() {
    checkFrame();
    if (status=="infected") {
      if (mouseOver) {
        if (mousePressed) {
          status="marked";
        }
      }
      move();
      infected();
    } else if (status=="zombie") {
      move();
      zombie();
    } else if (status=="immune") {
      move();
      immune();
    } else {
      if (mouseOver) {
        marked();
        status="marked";
        if (mousePressed) {
          status="infected";
        }
      } else {
        move();
        susceptible();
      }
    }
  }

  public void infected() {
    drawBody(color(0, 0, 255/2, 200));
  }

  public void susceptible() {
    drawBody(color(c));
    drawEyes();
  }

  public void marked() {
    drawBody(color(0, 0, 255));
    drawEyes();
  }

  public void zombie() {
    int zcolor = color(255, 238, 0);
    // body
    noStroke();
    fill(zcolor);
    arc(x, y, w, w, PI/6, TWO_PI-PI/6, PIE);
    // update animation
    if (sprite==0) {
      fill(zcolor);
    } else if (sprite==1) {
      fill(zcolor);
      arc(x, y, w, w, PI/24, TWO_PI -PI/24, PIE);
    }
  }
  
  public void immune(){
    drawBody(color(255/2));
    drawEyes();
  }

  public void drawBody(int ghost_color) {
    // body
    noStroke();
    fill(ghost_color);
    arc(x, y, w, h, -PI, 0, OPEN);
    rect(x, y+h/6-1, w, 1+h*3/8);
    // skirt
    if (sprite==0) {
      triangle(x-w/2, y, x-w/2, y+w/2, x, y);
      triangle(x+w/2, y, x+w/2, y+w/2, x, y);
      triangle(x-w/4, y+h/4, x, y+w/2, x, y);
      triangle(x+w/4, y+h/4, x, y+w/2, x, y);
    } else if (sprite==1) {
      triangle(x-w/2, y+h/3, x-w/5, y+h/3, x-w/2.5f, y+w/2);
      triangle(x+w/2, y+h/3, x+w/5, y+h/3, x+w/2.5f, y+w/2);
      triangle(x-w*2/8, y+h/3, x, y+h/3, x-w/8, y+w/2);
      triangle(x+w*2/8, y+h/3, x, y+h/3, x+w/8, y+w/2);
    }
  }

  public void drawEyes() {
    // eyes
    fill(255);
    ellipse(x+w*25/80, y-w/16, w/4, w/4);
    ellipse(x-w*5/80, y-w/16, w/4, w/4);
    // pupils
    fill(0);
    ellipse(x+w*30/80, y-w/16, w*6/80, w*6/80);
    ellipse(x, y-w/16, w*6/80, w*6/80);
  }

  public void checkFrame() {
    // update animation state every 10 frames
    if (frameCount % 10 == 1) {
      if (sprite == 0) {
        sprite = 1;
      } else if (sprite == 1) {
        sprite = 0;
      }
    }
    // detect if mouse is hovering over ghost
    if (mouseX > x - w/2 && mouseX < x + w/2 && mouseY > y - h/2 && mouseY < y + h/2) {
      mouseOver = true;
    } else {
      mouseOver = false;
    }
  }

  public boolean overlap(ghost otherghost) {
    if (x-w/2 < otherghost.x && otherghost.x < x+w/2 && y-h/2 <otherghost.y && otherghost.y<y+h/2 && this!=otherghost) {
      return true;
    } else {
      return false;
    }
  }

  public float calcDist(ghost otherghost) {
    return sqrt(sq(x-otherghost.x)) + (sq(y-otherghost.y));
  }

  public float calcAngle(ghost otherghost) {
    return atan((y-otherghost.y)/(x-otherghost.x));
  }

  public void move() {
      diffuse();
    // check if ghost is floating away from canvas and bounce back in
    if (x-w/2 < 0) {
      x=w/2;
      d = random (-HALF_PI, HALF_PI);
    } else if (x+w/2 > width) {
      x = width - w/2;
      d = random (HALF_PI, PI*3/4);
    } 
    if (y-h/2 < 0) {
      y = h/2;
      d = random (0, PI);
    } else if (y+h/2 > height) {
      y = height - h/2;
      d = random (PI, TWO_PI);
    }
  }

  public void diffuse() {
    // move towards a random direction
    x += (random(-w, w)/20) + w/10 * cos(d);
    y += (random(-h, h)/20) + h/10 * sin(d);
  }
}
  public void settings() {  size(600, 600, P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "Ghosts" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
