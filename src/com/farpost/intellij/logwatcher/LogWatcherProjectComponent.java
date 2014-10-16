package com.farpost.intellij.logwatcher;

import com.farpost.intellij.logwatcher.settings.LogWatcherSettings;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import com.intellij.util.containers.ConcurrentMultiMap;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.text.DateFormatUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;

public class LogWatcherProjectComponent extends AbstractProjectComponent {
  @NotNull private final Project myProject;
  @NotNull private final LogWatcherSettings mySettings;
  @NotNull private final Alarm myAlarm;
  @NotNull private final MultiMap<String, LogEntryDescriptor> myData = new ConcurrentMultiMap<String, LogEntryDescriptor>();

  public LogWatcherProjectComponent(@NotNull Project project, @NotNull LogWatcherSettings settings) {
    super(project);
    myProject = project;
    mySettings = settings;
    myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project);
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

  private void scheduleUpdate() {
    scheduleUpdate(mySettings.getUpdateRateMillis());
  }

  private void scheduleUpdate(long delayMillis) {
    myAlarm.addRequest(new MyUpdateRequest(), delayMillis);
  }

  private class MyUpdateRequest implements Runnable {
    @Override
    public void run() {
      // todo update from real logwatcher
      final String cl = "com.company.Main";
      final String m = "main";
      final int lineNumber = 7;
      myData.clear();
      myData.putValue(cl, new LogEntryDescriptor(cl, m, lineNumber, "/entries/search/0f346881b0b1752a000b459252242265?updatedTime=" +
                                                                    DateFormatUtil.formatTime(new Date())));
      myData.putValue(cl, new LogEntryDescriptor(cl, m, lineNumber, "/entries/search/608f0fea4b024d08a4fa3ea4dec59d39?updatedTime=" +
                                                                    DateFormatUtil.formatTime(new Date())));

      DaemonCodeAnalyzer.getInstance(myProject).restart();
      scheduleUpdate();
    }
  }
}

