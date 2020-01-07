package collector;

public interface NfoExtractionInterface {
   
   default String getFromNfo(String nfo, String extract, String extractEnd) {
      int beginIndex = nfo.indexOf(":", nfo.indexOf(extract)) + 1;
      int endIndex = nfo.indexOf(extractEnd, beginIndex);
      return nfo.substring(beginIndex, endIndex).trim();
   }
   
   default String getFromNfoSpace(String nfo, String extract, String extractEnd) {
      int beginIndex = nfo.indexOf("\n", nfo.indexOf(extract));
      int endIndex = nfo.indexOf(extractEnd, beginIndex);
      return nfo.substring(beginIndex, endIndex).trim();
   }
   
   default String getFromNfoDot(String nfo, String extract, String extractEnd) {
      int beginIndex = nfo.indexOf(" ", nfo.indexOf(".", nfo.indexOf(extract)) + 1);
      int endIndex = nfo.indexOf(extractEnd, beginIndex);
      return nfo.substring(beginIndex, endIndex).trim();
   }
   
}
