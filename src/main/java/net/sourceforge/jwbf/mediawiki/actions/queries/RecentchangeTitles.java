/*
 * Copyright 2007 Thomas Stock.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributors:
 *
 */
package net.sourceforge.jwbf.mediawiki.actions.queries;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.jwbf.core.RequestBuilder;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.actions.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.jdom.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Gets a list of pages recently changed, ordered by modification timestamp. Parameters: rcfrom (paging timestamp), rcto
 * (flt), rcnamespace (flt), rcminor (flt), rcusertype (dflt=not|bot), rcdirection (dflt=older), rclimit (dflt=10,
 * max=500/5000) F api.php ? action=query & list=recentchanges - List last 10 changes
 * 
 * @author Thomas Stock
 */
@Slf4j
public class RecentchangeTitles extends TitleQuery<String> {

  /** value for the bllimit-parameter. **/
  private static final int limit = 10;

  private int find = 1;

  private final MediaWikiBot bot;

  private final int[] namespaces;

  /**
   * Collection that will contain the result (titles of articles linking to the target) after performing the action has
   * finished.
   */
  private final Collection<String> titleCollection = Lists.newArrayList();
  private final boolean uniqChanges;

  private class RecentInnerAction extends InnerAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String processAllReturningText(final String s) {

      titleCollection.clear();
      parseArticleTitles(s);

      if (log.isDebugEnabled()) {
        log.debug("found: " + titleCollection);
      }
      if (uniqChanges) {
        Set<String> set = Sets.newHashSet();
        set.addAll(titleCollection);
        titleCollection.clear();
        titleCollection.addAll(set);
      }
      titleIterator = titleCollection.iterator();

      return "";
    }
  }

  /**
   * generates the next MediaWiki-request (GetMethod) and adds it to msgs.
   * 
   * @param namespace
   *          the namespace(s) that will be searched for links, as a string of numbers separated by '|'; if null, this
   *          parameter is omitted
   * @param rcstart
   *          timestamp
   */
  private HttpAction generateRequest(int[] namespace, String rcstart) {

    RequestBuilder requestBuilder = new ApiRequestBuilder() //
        .action("query") //
        .formatXml() //
        .param("list", "recentchanges") //
        .param("rclimit", limit + "") //
    ;
    if (namespace != null) {
      requestBuilder.param("rcnamespace", MediaWiki.encode(MWAction.createNsString(namespace)));
    }
    if (rcstart.length() > 0) {
      requestBuilder.param("rcstart", rcstart);
    }

    return requestBuilder.buildGet();

  }

  private HttpAction generateRequest(int[] namespace) {

    return generateRequest(namespace, "");

  }

  /**
   *
   */
  public RecentchangeTitles(MediaWikiBot bot, int... ns) {
    this(bot, false, ns);

  }

  /**
   *
   */
  public RecentchangeTitles(MediaWikiBot bot, boolean uniqChanges, int... ns) {
    super(bot);
    namespaces = ns;
    this.bot = bot;
    this.uniqChanges = uniqChanges;

  }

  /**
   *
   */
  public RecentchangeTitles(MediaWikiBot bot) {
    this(bot, MediaWiki.NS_ALL);

  }

  /**
   * picks the article name from a MediaWiki api response.
   * 
   * @param s
   *          text for parsing
   */
  @Override
  protected Collection<String> parseArticleTitles(String s) {
    Element root = getRootElement(s);
    findContent(root);
    return titleCollection;

  }

  @SuppressWarnings("unchecked")
  private void findContent(final Element root) {

    Iterator<Element> el = root.getChildren().iterator();
    while (el.hasNext()) {
      Element element = el.next();
      if (element.getQualifiedName().equalsIgnoreCase("rc")) {
        if (find < limit) {
          titleCollection.add(MediaWiki.decode(element.getAttributeValue("title")));
        }

        nextPageInfo = element.getAttribute("timestamp").getValue();
        find++;
      } else {
        findContent(element);
      }

    }
  }

  @Override
  protected HttpAction prepareCollection() {
    find = 1;
    if (getNextPageInfo().length() <= 0) {
      return generateRequest(namespaces);
    } else {
      return generateRequest(namespaces, getNextPageInfo());
    }

  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new RecentchangeTitles(bot, uniqChanges, namespaces);
  }

  @Override
  protected String parseHasMore(String s) {
    return "";
  }

  @Override
  protected InnerAction getInnerAction() {
    return new RecentInnerAction();
  }

}
