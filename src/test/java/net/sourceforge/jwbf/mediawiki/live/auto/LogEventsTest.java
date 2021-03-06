package net.sourceforge.jwbf.mediawiki.live.auto;

import static net.sourceforge.jwbf.TestHelper.getRandom;
import static net.sourceforge.jwbf.TestHelper.getRandomAlph;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.VersionTestClassVerifier;
import net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version;
import net.sourceforge.jwbf.mediawiki.actions.queries.LogEvents;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import net.sourceforge.jwbf.mediawiki.contentRep.LogItem;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Verifier;
import org.junit.runners.Parameterized.Parameters;

@Slf4j
public class LogEventsTest extends ParamHelper {

  @ClassRule
  public static VersionTestClassVerifier classVerifier = new VersionTestClassVerifier(
      LogEvents.class);

  @Rule
  public Verifier successRegister = classVerifier.getSuccessRegister(this);

  @Parameters(name = "{0}")
  public static Collection<?> stableWikis() {
    return ParamHelper.prepare(Version.valuesStable());
  }

  public LogEventsTest(Version v) {
    super(v, classVerifier);
  }

  private static final int LIMIT = 55;

  @Test
  public final void test() {
    doTest(bot, true, LogEvents.DELETE);
  }

  private static void doPrepare(MediaWikiBot bot) {
    for (int i = 0; i <= LIMIT; i++) {
      String title = getRandomAlph(6);
      Article a = new Article(bot, title);
      a.setText(getRandom(5));
      a.save();
      assertTrue("content shoul be", a.getText().length() > 0);
      a.delete();
    }
  }

  /**
   * @deprecated static is not good
   */
  @Deprecated
  public static void doTest(MediaWikiBot bot, boolean isDemo, String type) {
    LogEvents le = new LogEvents(bot, type);

    int i = 0;
    boolean notEnough = true;
    for (@SuppressWarnings("unused")
    LogItem logItem : le) {
      i++;
      if (i > LIMIT) {
        notEnough = false;
        break;
      }
    }
    if (notEnough && isDemo) {
      doPrepare(bot);
    }

    for (LogItem logItem : le) {
      log.debug(logItem.getTitle() + " ");
      i++;
      if (i > LIMIT) {
        break;
      }
    }
    assertTrue("should be greater then 50 but is " + i, i > LIMIT);
  }
}
