package utils;

public class Logger {
    private String loggerName;

    public Logger(String loggerName) {
        this.loggerName = loggerName;
    }

    public void log(Object logMessage) {
        if (logMessage.getClass().equals(Exception.class)) {
            Log.write((Exception) logMessage, loggerName);
        } else {
            Log.write((String) logMessage, loggerName);
        }
    }

}
