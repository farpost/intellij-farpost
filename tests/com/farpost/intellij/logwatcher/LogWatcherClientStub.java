package com.farpost.intellij.logwatcher;

import com.farpost.intellij.logwatcher.client.LogEntryDescriptor;
import com.farpost.intellij.logwatcher.client.LogWatcherClient;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class LogWatcherClientStub extends LogWatcherClient {

  private Collection<LogEntryDescriptor> myLogEntries = ContainerUtil.newSmartList();

  public void addLog(@NotNull LogEntryDescriptor logEntryDescriptor) {
    myLogEntries.add(logEntryDescriptor);
  }

  public void clearLogs() {
    myLogEntries.clear();
  }

  @Override
  public void processLogs(@NotNull String endpoint, @NotNull Consumer<LogEntryDescriptor> consumer) {
    for (LogEntryDescriptor logEntry : ContainerUtil.newArrayList(myLogEntries)) {
      consumer.consume(logEntry);
    }
  }

  @Nullable
  @Override
  public String retrieveStacktrace(@NotNull String endpoint, @NotNull String id) {
    return null;
  }
}
