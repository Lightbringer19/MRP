package monitors;

import mongodb.mongo;

public class MongoMonitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("Mongo");
      doAll(new mongo());
   }
}
