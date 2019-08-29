package monitors;

import scraper.maletadvj.MaletaDvjScraper;

public class MaletaDvj_Scraper_Monitor extends Monitor {
    
    public static void main(String[] args) {
        setTITLE("MaletaDVJ Scraper");
        doAll(new MaletaDvjScraper());
    }
}
