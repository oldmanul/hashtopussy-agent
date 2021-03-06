package org.hashes.hashtopussy.agent.common;

public class LoggerFactory {
  private static Logger logger;
  
  public static Logger getLogger() {
    if (logger != null) {
      return logger;
    }
    LogLevel level = (LogLevel) Settings.get(Setting.LOG_LEVEL);
    LoggerType loggerType = ((LoggerType) Settings.get(Setting.LOGGER));
    if (level == null || loggerType == null) {
      logger = new Logger(LoggerType.STDOUT.getPrinter(), LogLevel.DEBUG);
    } else {
      logger = new Logger(loggerType.getPrinter(), level);
    }
    return logger;
  }
}
