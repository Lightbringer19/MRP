package collector;

public interface NfoExtractionInterface {
   
   default String getFromNfo(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf(":", nfo.indexOf(extract)) + 1,
        nfo.indexOf(extractEnd)).trim();
   }
   
   default String getFromNfoSpace(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf("\n", nfo.indexOf(extract)),
        nfo.indexOf(extractEnd)).trim();
   }
}
