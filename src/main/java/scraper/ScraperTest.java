package scraper;

import java.text.ParseException;

public class ScraperTest {
    public static void main(String[] args) throws ParseException {
        // String html = FUtils.readFile(new File("Z:\\source.html"));
        // Document document = Jsoup.parse(requireNonNull(html));
        // Element trackContainer = document.select("div[class=widget-content]").first();
        //
        // List<String> dates = trackContainer
        //    .textNodes().stream()
        //    .filter(textNode -> !textNode.isBlank())
        //    .map(textNode -> textNode.text().trim())
        //    .collect(Collectors.toList());
        //
        // String firstDateOnPage = dates.get(0);
        //
        // // dates.forEach(System.out::println);
        //
        // System.out.println(dates.get(1));
        // Calendar cal = Calendar.getInstance();
        // String dateFormat = "MMMM d, yyyy";
        // cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(dates.get(1)));
        // cal.add(Calendar.DAY_OF_MONTH, 1);
        // String ddMM = new SimpleDateFormat("ddMM").format(cal.getTime());
        // System.out.println(ddMM);
        
        String example = "Graham Parker &amp; The Rumour - Don\\\\\\'t Ask Me Questions (Doc Adam Edit).mp3";
        String replaceAll = example.replaceAll("\\\\", "")
           .replaceAll("&amp;", "&");
        System.out.println(example);
        System.out.println(replaceAll);
        
        // String containerHtml = trackContainer.html();
        // int indexOfFirstDate = containerHtml.indexOf(dates.get(1));
        // int indexOfSecondDate = containerHtml.indexOf(dates.get(2));
        //
        // String htmlWithTracks = containerHtml.substring(indexOfFirstDate, indexOfSecondDate);
        //
        // Document parsedInside = Jsoup.parse(htmlWithTracks);
        // Elements trackInfos = parsedInside.select("div[class*=widget-beats-play rpool]");
        // for (Element trackInfo : trackInfos) {
        //     String downloadUrl = "https://beatjunkies.com" +
        //        trackInfo.select("a[href*=download]").first().attr("href");
        //     System.out.println(downloadUrl);
        // }
        
    }
    
}
