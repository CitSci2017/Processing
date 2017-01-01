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
  int name_i = int(random(4)); // identity
  String name;
  color c;
  float t_xwalk = 0; // for movement
  float t_ywalk = 0; // for movement
  float immuneProb = 0.0;

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

  void display() {
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

  void infected() {
    drawBody(color(0, 0, 255/2, 200));
  }

  void susceptible() {
    drawBody(color(c));
    drawEyes();
  }

  void marked() {
    drawBody(color(0, 0, 255));
    drawEyes();
  }

  void zombie() {
    color zcolor = color(255, 238, 0);
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
  
  void immune(){
    drawBody(color(255/2));
    drawEyes();
  }

  void drawBody(color ghost_color) {
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
      triangle(x-w/2, y+h/3, x-w/5, y+h/3, x-w/2.5, y+w/2);
      triangle(x+w/2, y+h/3, x+w/5, y+h/3, x+w/2.5, y+w/2);
      triangle(x-w*2/8, y+h/3, x, y+h/3, x-w/8, y+w/2);
      triangle(x+w*2/8, y+h/3, x, y+h/3, x+w/8, y+w/2);
    }
  }

  void drawEyes() {
    // eyes
    fill(255);
    ellipse(x+w*25/80, y-w/16, w/4, w/4);
    ellipse(x-w*5/80, y-w/16, w/4, w/4);
    // pupils
    fill(0);
    ellipse(x+w*30/80, y-w/16, w*6/80, w*6/80);
    ellipse(x, y-w/16, w*6/80, w*6/80);
  }

  void checkFrame() {
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

  boolean overlap(ghost otherghost) {
    if (x-w/2 < otherghost.x && otherghost.x < x+w/2 && y-h/2 <otherghost.y && otherghost.y<y+h/2 && this!=otherghost) {
      return true;
    } else {
      return false;
    }
  }

  float calcDist(ghost otherghost) {
    return sqrt(sq(x-otherghost.x)) + (sq(y-otherghost.y));
  }

  float calcAngle(ghost otherghost) {
    return atan((y-otherghost.y)/(x-otherghost.x));
  }

  void move() {
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

  void diffuse() {
    // move towards a random direction
    x += (random(-w, w)/20) + w/10 * cos(d);
    y += (random(-h, h)/20) + h/10 * sin(d);
  }
}