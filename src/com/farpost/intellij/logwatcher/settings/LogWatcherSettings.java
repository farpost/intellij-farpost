package com.farpost.intellij.logwatcher.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "LogWatcher", storages = {@Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/logwatcher.xml")})
public class LogWatcherSettings implements PersistentStateComponent<LogWatcherSettings> {
  private String url = "";

  @NotNull
  public String getUrl() {
    return url;
  }

  public void setUrl(@NotNull String url) {
    this.url = url;
  }

  public static LogWatcherSettings getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, LogWatcherSettings.class);
  }

  @Nullable
  @Override
  public LogWatcherSettings getState() {
    return this;
  }

  @Override
  public void loadState(LogWatcherSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
