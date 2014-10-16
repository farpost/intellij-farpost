package com.farpost.intellij.logwatcher;

public class LogEntryDescriptor {
  public final String classFqn;
  public final String methodName;
  public final int lineNumber;
  public final String logUrl;

  public LogEntryDescriptor(String classFqn, String methodName, int lineNumber, String logUrl) {
    this.classFqn = classFqn;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
    this.logUrl = logUrl;
  }
}
