package com.farpost.intellij.logwatcher.toolwindow;

import com.farpost.intellij.Icons;
import com.farpost.intellij.logwatcher.client.LogWatcherClient;
import com.farpost.intellij.logwatcher.settings.LogWatcherSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.util.NotNullProducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LogWatcherToolWindow {
  @NotNull private static final Key<String> EXCEPTION_ID_KEY = Key.create("LogWatcherExceptionId");

  @NotNull private final ToolWindow myToolWindow;
  @NotNull private final Project myProject;

  public LogWatcherToolWindow(@NotNull Project project, @NotNull ToolWindowManager toolWindowManager) {
    myProject = project;
    myToolWindow = toolWindowManager.registerToolWindow("LogWatcher", true, ToolWindowAnchor.BOTTOM, myProject, true);
    myToolWindow.setToHideOnEmptyContent(true);
    myToolWindow.setIcon(Icons.ToolWindowLogWatcher);
    myToolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
      @Override
      public void contentAdded(ContentManagerEvent contentManagerEvent) {
        myToolWindow.setAvailable(true, null);
      }

      @Override
      public void contentRemoved(ContentManagerEvent contentManagerEvent) {
        contentManagerEvent.getContent().release();
        myToolWindow.setAvailable(myToolWindow.getContentManager().getContents().length > 0, null);
      }
    });
  }

  public void openException(@NotNull String logUrl, @NotNull String title, @NotNull final String exceptionId) {
    final Content existingContent = findOpenedException(exceptionId);

    final ContentManager contentManager = myToolWindow.getContentManager();
    final Content content;

    if (existingContent != null) {
      content = existingContent;
    }
    else {
      final LogWatcherStacktracePanel logWatcherStacktracePanel = new LogWatcherStacktracePanel(logUrl, myProject);
      logWatcherStacktracePanel.printStacktrace(new NotNullProducer<String>() {
        @NotNull
        @Override
        public String produce() {
          final String endpoint = LogWatcherSettings.getInstance(myProject).getUrl();
          return StringUtil.notNullize(LogWatcherClient.getInstance().retrieveStacktrace(endpoint, exceptionId));
        }
      });

      content = contentManager.getFactory().createContent(logWatcherStacktracePanel.getComponent(), title, true);
      content.putUserData(EXCEPTION_ID_KEY, exceptionId);
      contentManager.addContent(content);
    }

    if (!myToolWindow.isActive()) {
      myToolWindow.activate(new Runnable() {
        @Override
        public void run() {
          if (!contentManager.isDisposed() && contentManager.getIndexOfContent(content) >= 0) {
            contentManager.setSelectedContent(content, true, false);
          }
        }
      });
    }
    else {
      contentManager.setSelectedContent(content);
    }
  }

  @Nullable
  private Content findOpenedException(@NotNull String exceptionId) {
    for (Content content : myToolWindow.getContentManager().getContents()) {
      if (exceptionId.equals(content.getUserData(EXCEPTION_ID_KEY))) {
        return content;
      }
    }
    return null;
  }


  public static LogWatcherToolWindow getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, LogWatcherToolWindow.class);
  }
}
