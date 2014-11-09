package com.farpost.intellij.logwatcher.client;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LogWatcherClient {
  public abstract void processLogs(@NotNull String endpoint, @NotNull Consumer<LogEntryDescriptor> consumer);

  @Nullable
  public abstract String retrieveStacktrace(@NotNull String endpoint, @NotNull String id);

  public static LogWatcherClient getInstance() {
    return ServiceManager.getService(LogWatcherClient.class);
  }
}
