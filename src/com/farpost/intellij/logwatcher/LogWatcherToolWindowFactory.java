package com.farpost.intellij.logwatcher;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LogWatcherToolWindowFactory implements ToolWindowFactory, DumbAware {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    final ContentManager contentManager = toolWindow.getContentManager();
    final JPanel panel = new JPanel();
    final Content content = contentManager.getFactory().createContent(panel, null, false);
    contentManager.addContent(content);
  }
}
