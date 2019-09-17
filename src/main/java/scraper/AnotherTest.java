package scraper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class AnotherTest {
   public static void main(String[] args) throws UnsupportedEncodingException {
      String content = "attachment; filename=\"ShawnMendes_%26_CamilaCabello_x_SilkCity_DuaLipa_x_DavidGuetta_%26_R3HAB_Senorita_x_Stay_DaveDefenderMashupIntro_Cln_CLUBDJVIDEOS_HD.mp4\"; filename*=UTF-8''ShawnMendes_%26_CamilaCabello_x_SilkCity_DuaLipa_x_DavidGuetta_%26_R3HAB_Senorita_x_Stay_DaveDefenderMashupIntro_Cln_CLUBDJVIDEOS_HD.mp4";
      int index = content.indexOf("\"");
      String filename = content
        .substring(index + 1, content.indexOf("\"", index + 1));
      
      String decode = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8.name());
      System.out.println(decode);
   }
}
