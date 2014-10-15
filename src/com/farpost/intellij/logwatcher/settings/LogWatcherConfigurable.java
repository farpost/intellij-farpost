package com.farpost.intellij.logwatcher.settings;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

public class LogWatcherConfigurable implements SearchableConfigurable {
  @NotNull private final Project myProject;
  @NotNull private final LogWatcherSettings mySettings;
  private JPanel myPanel;
  private JBTextField myHostField;

  public LogWatcherConfigurable(@NotNull Project project, @NotNull LogWatcherSettings settings) {
    myProject = project;
    mySettings = settings;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myPanel;
  }

  @Override
  public boolean isModified() {
    return !mySettings.getUrl().equals(myHostField.getText());
  }

  @Override
  public void apply() throws ConfigurationException {
    mySettings.setUrl(myHostField.getText());
    
    DaemonCodeAnalyzerImpl codeAnalyzer = (DaemonCodeAnalyzerImpl)DaemonCodeAnalyzer.getInstance(myProject);
    codeAnalyzer.restart();
  }

  @Override
  public void reset() {
    myHostField.setText(mySettings.getUrl());
  }

  @Override
  public void disposeUIResources() {

  }

  @Nls
  @Override
  public String getDisplayName() {
    return "LogWatcher";
  }

  @NotNull
  @Override
  public String getId() {
    return "settings.logwatcher";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

}
