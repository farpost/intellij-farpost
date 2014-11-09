package com.farpost.intellij.logwatcher.toolwindow;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.CloseActiveTabAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.unscramble.AnalyzeStacktraceUtil;
import com.intellij.util.NotNullProducer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;

public class LogWatcherStacktracePanel {
  @NotNull private final Project myProject;
  @NotNull private final ConsoleView myConsoleView;

  private JPanel myPanel;

  public LogWatcherStacktracePanel(@NotNull String url, @NotNull Project project) {
    myProject = project;
    myConsoleView = createConsoleView();

    JPanel toolbarPanel = createToolbarPanel(url);
    myPanel = new JBLoadingPanel(new BorderLayout(), myProject);
    myPanel.add(toolbarPanel, BorderLayout.WEST);
    myPanel.add(myConsoleView.getComponent(), BorderLayout.CENTER);
  }

  public void printStacktrace(@NotNull final NotNullProducer<String> stacktraceProducer) {
    ((JBLoadingPanel)myPanel).startLoading();
    Executors.newSingleThreadExecutor().submit(new Runnable() {
      @Override
      public void run() {
        AnalyzeStacktraceUtil.printStacktrace(myConsoleView, stacktraceProducer.produce());
        ((JBLoadingPanel)myPanel).stopLoading();
      }
    });
  }

  public JComponent getComponent() {
    return myPanel;
  }

  private static JPanel createToolbarPanel(@NotNull String url) {
    final DefaultActionGroup toolbarActions = new DefaultActionGroup();
    toolbarActions.add(new GotoUrlAction(url));

    final AnAction closeContentAction = new CloseActiveTabAction();
    closeContentAction.getTemplatePresentation().setIcon(AllIcons.Actions.Cancel);
    closeContentAction.getTemplatePresentation().setText("Close tab");
    toolbarActions.add(closeContentAction);

    JPanel toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false).getComponent());
    return toolbarPanel;
  }

  @NotNull
  private ConsoleView createConsoleView() {
    final TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(myProject);
    builder.filters(Extensions.getExtensions(AnalyzeStacktraceUtil.EP_NAME, myProject));
    final ConsoleView consoleView = builder.getConsole();
    consoleView.allowHeavyFilters();
    return consoleView;
  }

  private void createUIComponents() {
  }

  private static class GotoUrlAction extends AnAction implements DumbAware {
    private final String url;

    public GotoUrlAction(String url) {
      this.url = url;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      BrowserUtil.browse(url);
    }

    @Override
    public void update(AnActionEvent e) {
      e.getPresentation().setText(url);
      e.getPresentation().setIcon(AllIcons.General.Web);
    }
  }
}
