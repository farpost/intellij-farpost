package com.farpost.intellij.logwatcher.settings;

import com.farpost.intellij.logwatcher.LogWatcherProjectComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class LogWatcherConfigurable implements SearchableConfigurable {
  @NotNull private final Project myProject;
  @NotNull private final LogWatcherSettings mySettings;
  private JPanel myPanel;
  private JBTextField myHostField;
  private JSpinner myUpdateRateSpinner;

  public LogWatcherConfigurable(@NotNull Project project, @NotNull LogWatcherSettings settings) {
    myProject = project;
    mySettings = settings;
    myUpdateRateSpinner.setModel(new SpinnerNumberModel(1, 1, 60, 1));
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myPanel;
  }

  @Override
  public boolean isModified() {
    return !mySettings.getUrl().equals(myHostField.getText()) ||
           TimeUnit.MILLISECONDS.toMinutes(mySettings.getUpdateRateMillis()) != myUpdateRateSpinner.getValue();
  }

  @Override
  public void apply() throws ConfigurationException {
    mySettings.setUrl(myHostField.getText());
    mySettings.setUpdateRateMillis(TimeUnit.MINUTES.toMillis((Integer)myUpdateRateSpinner.getValue()));

    LogWatcherProjectComponent.getInstance(myProject).scheduleUpdate(0);
  }

  @Override
  public void reset() {
    myHostField.setText(mySettings.getUrl());
    myUpdateRateSpinner.setValue(Math.max(1, TimeUnit.MILLISECONDS.toMinutes(mySettings.getUpdateRateMillis())));
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
