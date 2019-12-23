package monitors;

import poster.Poster;

public class Poster_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("Poster");
      doAll(new Poster());
   }
}
