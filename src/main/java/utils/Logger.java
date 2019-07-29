package utils;

public class Logger {
    private String loggerName;
    
    public Logger(String loggerName) {
        this.loggerName = loggerName;
    }
    
    public void log(Object logMessage) {
        if (logMessage.getClass().equals(String.class)) {
            Log.write((String) logMessage, loggerName);
        } else {
            Log.write((Exception) logMessage, loggerName);
        }
    }
    
}
