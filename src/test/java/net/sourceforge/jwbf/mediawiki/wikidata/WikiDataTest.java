package net.sourceforge.jwbf.mediawiki.wikidata;

import static org.junit.Assert.assertEquals;
import net.sourceforge.jwbf.TestHelper;
import net.sourceforge.jwbf.core.actions.Get;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.junit.Test;

public class WikiDataTest {

  @Test
  public void test() {
    // GIVEN
    // TODO do not work with a live system
    String wikidataApiLiveUrl = "https://www.wikidata.org/w/api.php";
    TestHelper.assumeReachable(wikidataApiLiveUrl); // will skip the test if not
    MediaWikiBot bot = new MediaWikiBot(wikidataApiLiveUrl);

    // WHEN
    GetClaims getClaims = new GetClaims(bot, "Q4115189");
    bot.performAction(getClaims);

    // THEN
    // TODO better assertions
    assertEquals("{\"claims\":", getClaims.getResult().substring(0, 10));
  }

  // TODO extract to file
  // TODO what is a claim?
  private static class GetClaims extends MWAction {

    private final Get buildGet;
    private String result = "";

    public GetClaims(MediaWikiBot bot, String entity) {
      buildGet = new ApiRequestBuilder() //
          .action("wbgetclaims") //
          .formatJson() //
          .param("entity", entity) //
          .buildGet();

    }

    @Override
    public String processAllReturningText(String text) {
      result = text;
      return "doNotCallThis";
    }

    // TODO this have to be a complex type, not a string
    public String getResult() {
      return result;
    }

    @Override
    public HttpAction getNextMessage() {
      return buildGet;
    }

  }

}
