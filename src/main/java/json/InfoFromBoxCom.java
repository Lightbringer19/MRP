package json;

public class InfoFromBoxCom {
   private String releaseName;
   private String downloadLinkBoxCom;
   private String localPathToFolder;
   
   public InfoFromBoxCom() {
   }
   
   public InfoFromBoxCom(String releaseName, String downloadLinkBoxCom, String localPathToFolder) {
      super();
      this.releaseName = releaseName;
      this.downloadLinkBoxCom = downloadLinkBoxCom;
      this.localPathToFolder = localPathToFolder;
   }
   
   public String getDownloadLinkBoxCom() {
      return downloadLinkBoxCom;
   }
   
   public void setDownloadLinkBoxCom(String downloadLinkBoxCom) {
      this.downloadLinkBoxCom = downloadLinkBoxCom;
   }
   
   public String getLocalPathToFolder() {
      return localPathToFolder;
   }
   
   public void setLocalPathToFolder(String localPathToFolder) {
      this.localPathToFolder = localPathToFolder;
   }
   
   public String getReleaseName() {
      return releaseName;
   }
   
   public void setReleaseName(String nameForRelease) {
      releaseName = nameForRelease;
   }
   
   @Override
   public String toString() {
      return "InfoForCollector [releaseName=" + releaseName + ", downloadLinkBoxCom=" + downloadLinkBoxCom + ", localPathToFolder=" + localPathToFolder + "]";
   }
}
