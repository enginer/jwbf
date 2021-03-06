package net.sourceforge.jwbf.mediawiki.actions.editing;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.jwbf.core.RequestBuilder;
import net.sourceforge.jwbf.core.actions.Post;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.actions.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.jdom.Document;
import org.jdom.Element;

import com.google.common.base.Strings;

/**
 * Action class using the MediaWiki-API's <a href="http://www.mediawiki.org/wiki/API:Edit_-_Move">"action=move"</a>.
 * <p>
 * To allow your bot to move articles in your MediaWiki add the following line to your MediaWiki's LocalSettings.php:<br>
 * 
 * <pre>
 * $wgEnableWriteAPI = true;
 * $wgGroupPermissions['bot']['move'] = true;
 * $wgGroupPermissions['bot']['movefile'] = true;            // optional
 * $wgGroupPermissions['bot']['move-subpages'] = true;       // optional
 * $wgGroupPermissions['bot']['move-rootuserpages'] = true;  // optional
 * </pre>
 * <p>
 * Move an article with
 * 
 * <pre>
 * String oldtitle = ...
 * String newtitle = ...
 * String reason = ...
 * Boolean withsubpages = ...
 * Boolean noredirect = ...
 * 
 * MediaWikiBot bot = ...
 * bot.performAction(new MovePage(bot, oldtitle, newtitle, reason, withsubpages, noredirect));
 * </pre>
 * 
 * @author Christoph Giesel
 */
@Slf4j
public class MovePage extends MWAction {

  private final String oldtitle;
  private final String newtitle;
  private final String reason;
  private final boolean withsubpages;
  private final boolean noredirect;
  private final GetApiToken token;
  private boolean moveToken = true;

  /**
   * Constructs a new <code>MovePage</code> action.
   * 
   * @param bot
   *          the MediaWikiBot
   * @param oldtitle
   *          title to move
   * @param newtitle
   *          new title
   * @param reason
   *          reason why to move
   * @param withsubpages
   *          if <b>TRUE</b> also move the subpages
   * @param noredirect
   *          if <b>TRUE</b> create no redirects
   */
  public MovePage(MediaWikiBot bot, String oldtitle, String newtitle, String reason,
      boolean withsubpages, boolean noredirect) {
    token = new GetApiToken(GetApiToken.Intoken.MOVE, oldtitle, bot.getVersion(), bot.getUserinfo());
    this.oldtitle = oldtitle;
    this.newtitle = newtitle;
    this.reason = reason;
    this.withsubpages = withsubpages;
    this.noredirect = noredirect;

    if (oldtitle == null || oldtitle.length() == 0 || newtitle == null || newtitle.length() == 0) {
      throw new IllegalArgumentException(
          "The arguments 'oldtitle' and 'newtitle' must not be null or empty");
    }

    if (!bot.getUserinfo().getRights().contains("move")) {
      throw new ProcessException("The given user doesn't have the rights to move. "
          + "Add '$wgGroupPermissions['bot']['move'] = true;' "
          + "to your MediaWiki's LocalSettings.php might solve this problem.");
    }

    if (withsubpages && !bot.getUserinfo().getRights().contains("move-subpages")) {
      throw new ProcessException("The given user doesn't have the rights to move subpages. "
          + "Add '$wgGroupPermissions['bot']['move-subpages'] = true;' "
          + "to your MediaWiki's LocalSettings.php might solve this problem.");
    }
  }

  /**
   * @return the delete action
   */
  private HttpAction getSecondRequest() {
    HttpAction msg = null;
    if (token.getToken() == null || token.getToken().length() == 0) {
      throw new IllegalArgumentException("The argument 'token' must not be \""
          + String.valueOf(token.getToken()) + "\"");
    }
    if (log.isTraceEnabled()) {
      log.trace("enter MovePage.generateMoveRequest(String)");
    }

    RequestBuilder requestBuilder = new ApiRequestBuilder() //
        .action("move") //
        .formatXml() //
        .param("from", MediaWiki.encode(oldtitle)) //
        .param("to", MediaWiki.encode(newtitle)) //
        .param("token", MediaWiki.encode(token.getToken())) //
        .param("movetalk", "") // XXX
    ;

    if (withsubpages) {
      requestBuilder //
          .param("movesubpages", "") //
      ;
    }
    if (noredirect) {
      requestBuilder //
          .param("noredirect", "") //
      ;
    }
    if (!Strings.isNullOrEmpty(reason)) {
      requestBuilder //
          .param("reason", MediaWiki.encode(reason)) //
      ;
    }

    String uS = requestBuilder.build();
    if (log.isDebugEnabled()) {
      log.debug("move url: \"" + uS + "\"");
    }
    msg = new Post(uS);

    return msg;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String processReturningText(String s, HttpAction hm) {
    super.processReturningText(s, hm);

    if (moveToken) {
      token.processReturningText(s, hm);
      moveToken = false;
    } else {

      if (log.isTraceEnabled()) {
        log.trace("enter MovePage.processAllReturningText(String)");
      }
      if (log.isDebugEnabled()) {
        log.debug("Got returning text: \"" + s + "\"");
      }
      process(getRootElement(s));
      setHasMoreMessages(false);
    }

    return "";
  }

  /**
   * Processing the XML {@link Document} returned from the MediaWiki API.
   */
  private void process(Element rootElement) {
    Element elem = rootElement.getChild("move");
    if (elem != null) {
      // process reply for delete request
      if (log.isInfoEnabled()) {
        log.info("Moved article '" + elem.getAttributeValue("from") + "' to '"
            + elem.getAttributeValue("to") + "'" + " with reason '"
            + elem.getAttributeValue("reason") + "'");
      }
    } else {
      log.error("Unknow reply. This is not a reply for a delete action.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HttpAction getNextMessage() {
    if (token.hasMoreMessages()) {
      setHasMoreMessages(true);
      return token.getNextMessage();
    }
    return getSecondRequest();
  }
}
