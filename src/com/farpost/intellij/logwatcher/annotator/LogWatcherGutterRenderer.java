package com.farpost.intellij.logwatcher.annotator;

import com.farpost.intellij.logwatcher.toolwindow.LogWatcherToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

import static com.farpost.intellij.Icons.LogWatcher;
import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;
import static com.intellij.openapi.ui.popup.JBPopupFactory.getInstance;
import static com.intellij.util.containers.ContainerUtil.getFirstItem;

public class LogWatcherGutterRenderer extends GutterIconRenderer {

  private final ProblemOccurence myProblemOccurence;

  public LogWatcherGutterRenderer(ProblemOccurence problemOccurence) {
    myProblemOccurence = problemOccurence;
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return LogWatcher;
  }

  @Nullable
  @Override
  public AnAction getClickAction() {
    final Collection<String> urls = myProblemOccurence.getUrls();
    if (urls.size() == 1) {
      final String url = getFirstItem(urls);
      assert url != null;
      return new LogWatcherShowStacktraceAction("TODO: add title", url, "TODO: should be part of url");
    }
    return new AnAction() {
      @Override
      public void actionPerformed(AnActionEvent e) {
        DefaultActionGroup g = new DefaultActionGroup();
        for (String url : urls) {
          g.add(new LogWatcherShowStacktraceAction("TODO: add title", url, "TODO: should be part of url"));
        }
        if (e.getInputEvent() instanceof MouseEvent) {
          MouseEvent me = (MouseEvent)e.getInputEvent();
          getInstance().createActionGroupPopup(null, g, e.getDataContext(), NUMBERING, true, null, 10).show(new RelativePoint(me));
        }

      }
    };
  }

  @Override
  public boolean isNavigateAction() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LogWatcherGutterRenderer)) return false;

    LogWatcherGutterRenderer that = (LogWatcherGutterRenderer)o;

    if (!myProblemOccurence.equals(that.myProblemOccurence)) return false;

    return true;
  }

  public ProblemOccurence getProblemOccurence() {
    return myProblemOccurence;
  }

  @Override
  public int hashCode() {
    return myProblemOccurence.hashCode();
  }

  private static class LogWatcherShowStacktraceAction extends AnAction {
    @NotNull private String myTitle;
    @NotNull private String myUrl;
    @NotNull private String myExceptionId;

    public LogWatcherShowStacktraceAction(@NotNull String title, @NotNull String url, @NotNull String exceptionId) {
      myTitle = title;
      myUrl = url;
      myExceptionId = exceptionId;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
      final LogWatcherToolWindow logWatcherToolWindow = LogWatcherToolWindow.getInstance(project);
      if (logWatcherToolWindow != null) {
        logWatcherToolWindow.openException(myUrl, myTitle, myExceptionId);
      }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      final Project project = e.getProject();
      if (project == null) {
        e.getPresentation().setEnabled(false);
      }
      super.update(e);
    }

  }
}
