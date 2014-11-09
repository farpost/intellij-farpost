package com.farpost.intellij.logwatcher;

import com.farpost.intellij.logwatcher.client.LogEntryDescriptor;
import com.farpost.intellij.logwatcher.client.LogWatcherClient;
import com.farpost.intellij.logwatcher.settings.LogWatcherSettings;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ConcurrentMultiMap;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LogWatcherProjectComponent extends AbstractProjectComponent {
  @NotNull private final Project myProject;
  @NotNull private final LogWatcherClient myClient;
  @NotNull private final LogWatcherSettings mySettings;
  @NotNull private final Alarm myAlarm;
  @NotNull private final MultiMap<String, LogEntryDescriptor> myData = new ConcurrentMultiMap<String, LogEntryDescriptor>();

  public LogWatcherProjectComponent(@NotNull Project project, @NotNull LogWatcherClient client, @NotNull LogWatcherSettings settings) {
    super(project);
    myProject = project;
    myClient = client;
    mySettings = settings;
    myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project);
  }

  public static LogWatcherProjectComponent getInstance(@NotNull Project project) {
    return project.getComponent(LogWatcherProjectComponent.class);
  }

  public Collection<LogEntryDescriptor> getDescriptorsForClass(String fqn) {
    return myData.get(fqn);
  }

  @Override
  public void initComponent() {
    scheduleUpdate(0);
  }

  @Override
  public void disposeComponent() {
    myAlarm.cancelAllRequests();
    myData.clear();
  }

  public void scheduleUpdate() {
    scheduleUpdate(mySettings.getUpdateRateMillis());
  }

  public void scheduleUpdate(long delayMillis) {
    myAlarm.addRequest(new MyUpdateRequest(), delayMillis);
  }

  private class MyUpdateRequest implements Runnable {
    @Override
    public void run() {
      myData.clear();
      myClient.processLogs(mySettings.getUrl(), new Consumer<LogEntryDescriptor>() {
        @Override
        public void consume(LogEntryDescriptor logEntryDescriptor) {
          myData.putValue(logEntryDescriptor.classFqn, logEntryDescriptor);
        }
      });
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        DaemonCodeAnalyzer.getInstance(myProject).restart();
      }
      scheduleUpdate();
    }
  }
}