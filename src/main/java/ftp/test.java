package ftp;

import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;

public class test {
   
   private static final FTPClient FTP_CLIENT = new FTPClient();
   
   public static void main(String[] args) throws IOException {
      String SERVER = "localhost";
      int PORT = 21;
      String USERNAME = "admin";
      String PASSWORD = "admin";
      FTP_CLIENT.connect(SERVER, PORT);
      Log();
      FTP_CLIENT.login(USERNAME, PASSWORD);
      Log();
      FTP_CLIENT.enterLocalPassiveMode();
      FTP_CLIENT.setFileType(FTP.BINARY_FILE_TYPE);
      FTPFile[] a = FTP_CLIENT.listFiles("");
      for (FTPFile ftpFile : a) {
         downloadFile("", "D:\\", ftpFile);
      }
   }
   
   private static void downloadFile(String releaseRemotePath, String releaseLocalPath,
                                    FTPFile releaseFile) throws IOException {
      try (OutputStream output = new FileOutputStream(
        releaseLocalPath + "/" + releaseFile.getName())) {
         AtomicBoolean val = new AtomicBoolean(false);
         Thread thread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
               for (int i = 0; i < 5; i++) {
                  sleep(1_000);
                  if (i == 4) {
                     FTP_CLIENT.abort();
                  }
                  if (val.get()) {
                     break;
                  }
               }
            }
         });
         thread.start();
         FTP_CLIENT.retrieveFile(releaseRemotePath
           + releaseFile.getName(), output);
         val.set(true);
         thread.join();
         Log();
      } catch (FileNotFoundException | InterruptedException ignored) {
      }
   }
   
   protected static void Log() {
      System.out.print(FTP_CLIENT.getReplyString());
   }
}
