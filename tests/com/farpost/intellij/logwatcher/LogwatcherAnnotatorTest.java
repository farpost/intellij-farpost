package com.farpost.intellij.logwatcher;

import com.farpost.intellij.logwatcher.annotator.LogWatcherGutterRenderer;
import com.farpost.intellij.logwatcher.annotator.ProblemOccurence;
import com.farpost.intellij.logwatcher.client.LogEntryDescriptor;
import com.farpost.intellij.logwatcher.client.LogWatcherClient;
import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;

import java.util.Collection;

import static com.intellij.util.containers.ContainerUtil.newIdentityTroveSet;

public class LogwatcherAnnotatorTest extends LightPlatformCodeInsightFixtureTestCase {

  @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
  public LogwatcherAnnotatorTest() {
    System.setProperty("idea.load.plugins.id", "com.farpost.intellij.tests");
  }

  public void testAnnotateLine() {
    getClient().addLog(new LogEntryDescriptor("foo.Foo", "main", 7, "Url"));
    doIconsTest(Pair.create(7, ContainerUtil.newArrayList("Url")));
  }

  public void testMergeUrls() {
    getClient().addLog(new LogEntryDescriptor("foo.Foo", "main", 7, "Url1"));
    getClient().addLog(new LogEntryDescriptor("foo.Foo", "main", 7, "Url2"));

    doIconsTest(Pair.create(7, ContainerUtil.newArrayList("Url1", "Url2")));
  }

  @Override
  public void tearDown() throws Exception {
    getClient().clearLogs();
    super.tearDown();
  }

  /**
   * @param expectedData pairs of line number and urls
   */
  private void doIconsTest(Pair<Integer, ? extends Collection<String>>... expectedData) {
    final String fileName = getTestName(false) + ".java";

    Collection<Pair<Integer, ? extends Collection<String>>> actualData = ContainerUtil.newArrayList();
    for (GutterMark gutter : newIdentityTroveSet(myFixture.findAllGutters(fileName))) {
      if (gutter instanceof LogWatcherGutterRenderer) {
        final ProblemOccurence problemOccurence = ((LogWatcherGutterRenderer)gutter).getProblemOccurence();
        final int lineNumber = myFixture.getDocument(myFixture.getFile()).getLineNumber(problemOccurence.getTextRange().getStartOffset());
        actualData.add(Pair.create(lineNumber + 1, problemOccurence.getUrls()));
      }
    }

    assertSameElements(actualData, expectedData);
  }

  private static LogWatcherClientStub getClient() {
    return assertInstanceOf(LogWatcherClient.getInstance(), LogWatcherClientStub.class);
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  @Override
  protected String getTestDataPath() {
    return "testData/annotator/";
  }
}
