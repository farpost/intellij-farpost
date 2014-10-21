package com.farpost.intellij.logwatcher;

import com.farpost.intellij.logwatcher.settings.LogWatcherSettings;
import com.google.gson.*;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import com.intellij.util.containers.ConcurrentMultiMap;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

public class LogWatcherProjectComponent extends AbstractProjectComponent {
  @NotNull private final Project myProject;
  @NotNull private final LogWatcherSettings mySettings;
  @NotNull private final Alarm myAlarm;
  @NotNull private final MultiMap<String, LogEntryDescriptor> myData = new ConcurrentMultiMap<String, LogEntryDescriptor>();
  private static final Logger log = Logger.getInstance(LogWatcherProjectComponent.class);

  public LogWatcherProjectComponent(@NotNull Project project, @NotNull LogWatcherSettings settings) {
    super(project);
    myProject = project;
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
      String urlPrefix = mySettings.getUrl();
      String url = urlPrefix + "/search/problem-methods.json";
      try {
        InputStream is = URLUtil.openStream(new URL(url));

        Gson gson = new GsonBuilder().create();
        JsonArray obj = gson.fromJson(new InputStreamReader(is), JsonArray.class);
        for (JsonElement element : obj) {
          JsonObject o = (JsonObject)element;
          int line = o.getAsJsonPrimitive("line").getAsInt();
          String cluster = o.getAsJsonPrimitive("cluster").getAsString();
          String clazz = o.getAsJsonPrimitive("class").getAsString();
          @SuppressWarnings("UnusedDeclaration")
          String file = o.getAsJsonPrimitive("file").getAsString();
          String method = o.getAsJsonPrimitive("method").getAsString();

          myData.putValue(clazz, new LogEntryDescriptor(clazz, method, line, urlPrefix + "/entries/search/" + cluster));
        }
      }
      catch (IOException e) {
        log.error("Unable to read from URL", e, url);
      }

      DaemonCodeAnalyzer.getInstance(myProject).restart();
      scheduleUpdate();
    }
  }
}