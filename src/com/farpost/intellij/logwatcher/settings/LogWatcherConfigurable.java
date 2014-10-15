package com.farpost.intellij.logwatcher.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LogWatcherConfigurable implements SearchableConfigurable {
  @NotNull private final LogWatcherSettings mySettings;
  private JPanel myPanel;
  private JBTextField myHostField;

  public LogWatcherConfigurable(@NotNull LogWatcherSettings settings) {
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
