package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ScraperTest {
    public static void main(String[] args) throws ParseException {
        String html = FUtils.readFile(new File("Z:\\source.html"));
        Element trackContainer = Jsoup.parse(requireNonNull(html))
           .select("div[class=widget-content]").first();
        
        List<String> dates = trackContainer
           .textNodes().stream()
           .filter(textNode -> !textNode.isBlank())
           .map(textNode -> textNode.text().trim())
           .collect(Collectors.toList());
        
        String containerHtml = trackContainer.html();
        int indexOfFirstDate = containerHtml.indexOf(dates.get(1));
        int indexOfSecondDate = containerHtml.indexOf(dates.get(2));
        String htmlWithTracks = containerHtml.substring(indexOfFirstDate, indexOfSecondDate);
        
        System.out.println(htmlWithTracks);
        
        Jsoup.parse(htmlWithTracks)
           .select("div[class*=widget-beats-play rpool]").stream()
           .map(trackInfo -> "https://beatjunkies.com" +
              trackInfo.select("a[href*=download]").first().attr("href"))
           .peek(System.out::println)
           .collect(Collectors.toList());
    }
    
}
