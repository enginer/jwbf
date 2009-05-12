/*
 * Copyright 2007 Tobias Knerr.
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
 * Tobias Knerr
 * 
 */
package net.sourceforge.jwbf.actions.mediawiki.queries;

import static net.sourceforge.jwbf.actions.mediawiki.MediaWiki.Version.MW1_11;
import static net.sourceforge.jwbf.actions.mediawiki.MediaWiki.Version.MW1_12;
import static net.sourceforge.jwbf.actions.mediawiki.MediaWiki.Version.MW1_13;
import static net.sourceforge.jwbf.actions.mediawiki.MediaWiki.Version.MW1_14;

import java.util.Collection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jwbf.actions.Get;
import net.sourceforge.jwbf.actions.mediawiki.MediaWiki;
import net.sourceforge.jwbf.actions.mediawiki.util.SupportedBy;
import net.sourceforge.jwbf.actions.mediawiki.util.VersionException;
import net.sourceforge.jwbf.actions.util.ActionException;
import net.sourceforge.jwbf.actions.util.HttpAction;
import net.sourceforge.jwbf.actions.util.ProcessException;
import net.sourceforge.jwbf.bots.MediaWikiBot;

import org.apache.log4j.Logger;

/**
 * action class using the MediaWiki-api's "list=imagelinks"
 * 
 * @author Tobias Knerr
 * @author Thomas Stock
 * @since MediaWiki 1.9.0
 * @deprecated use {@link ImageUsageTitles}
 */
@SupportedBy({MW1_11, MW1_12, MW1_13, MW1_14 })
public class ImagelinkTitles extends TitleQuery {

	/** constant value for the illimit-parameter. **/
	private static final int LIMIT = 50;
	
	/**
	 * Collection that will contain the result
	 * (titles of articles using the image) 
	 * after performing the action has finished.
	 */
	private Collection<String> titleCollection = new Vector<String>();


	/**
	 * information necessary to get the next api page.
	 */
	private String nextPageInfo = null;
		
	private Get msg;

	private boolean hasMoreResults;

	private boolean init = true;

	private final MediaWikiBot bot;
	
	private final Logger log = Logger.getLogger(getClass());
	
	private final String imageName;
	private final int [] namespaces;
		
	/**
	 * The public constructor. It will have an MediaWiki-request generated,
	 * which is then added to msgs. When it is answered,
	 * the method processAllReturningText will be called
	 * (from outside this class).
	 * For the parameters, see {@link ImagelinkTitles#generateRequest(String, String, String)}
	 * @throws VersionException if not supported 
	 */
	public ImagelinkTitles(MediaWikiBot bot, String imageName, int... namespaces) throws VersionException {
		super(bot.getVersion());
		this.bot = bot;
		this.imageName = imageName;
		this.namespaces = namespaces;
		
	}
	
	/**
	 * 
	 * @param bot a
	 * @param nextPageInfo a
	 * @throws VersionException if not supported 
	 */
	public ImagelinkTitles(MediaWikiBot bot, String nextPageInfo) throws VersionException {
		this(bot, nextPageInfo, MediaWiki.NS_ALL);
		
	}
	
	/**
	 * generates the next MediaWiki-request (GetMethod) and adds it to msgs.
	 *
	 * @param imageName     the title of the image,
	 *                      may only be null if ilcontinue is not null
	 * @param namespace     the namespace(s) that will be searched for links,
	 *                      as a string of numbers separated by '|';
	 *                      if null, this parameter is omitted
	 * @param ilcontinue    the value for the ilcontinue parameter,
	 *                      null for the generation of the initial request
	 * @return a
	 */
	private Get generateRequest(String imageName, String namespace,
		String ilcontinue) {

		String uS = "";

		if (ilcontinue == null) {

			uS = "/api.php?action=query&list=imageusage"
					+ "&iutitle="
					+ MediaWiki.encode(imageName)
					+ ((namespace != null && namespace.length() != 0) ? ("&iunamespace=" + MediaWiki.encode(namespace))
							: "") + "&iulimit=" + LIMIT + "&format=xml";

		} else {

			uS = "/api.php?action=query&list=imageusage" + "&iucontinue="
					+ MediaWiki.encode(ilcontinue) + "&iulimit=" + LIMIT
					+ "&format=xml";

		}


		return new Get(uS);

	}
	
	/**
	 * deals with the MediaWiki api's response by parsing the provided text.
	 *
	 * @param s   the answer to the most recently generated MediaWiki-request
	 *
	 * @return empty string
	 */
	public String processAllReturningText(final String s) throws ProcessException {
		titleCollection.clear();
		log.debug(s);
		parseArticleTitles(s);
		parseHasMore(s);
		titleIterator = titleCollection.iterator();
		return "";
	}

	/**
	 * gets the information about a follow-up page from a provided api response.
	 * If there is one, a new request is added to msgs by calling generateRequest.
	 *	
	 * @param s   text for parsing
	 */
	protected void parseHasMore(final String s) {
			
		System.out.println(s);
		
		// get the ilcontinue-value
		
		Pattern p = Pattern.compile(
			"<query-continue>.*?"
			+ "<imageusage *iucontinue=\"([^\"]*)\" */>"
			+ ".*?</query-continue>",
			Pattern.DOTALL | Pattern.MULTILINE);
			
		Matcher m = p.matcher(s);

		if (m.find()) {			
			nextPageInfo = m.group(1);
			hasMoreResults = true;
		} else {
			hasMoreResults = false;
		}

	}

	/**
	 * picks the article name from a MediaWiki api response.
	 *	
	 * @param s   text for parsing
	 */
	public void parseArticleTitles(String s) {
		
		System.out.println(s);

		// get the backlink titles and add them all to the titleCollection
			
		Pattern p = Pattern.compile(
			"<iu pageid=\".*?\" ns=\".*?\" title=\"(.*?)\" />");
			
		Matcher m = p.matcher(s);
		
		while (m.find()) {
			titleCollection.add(m.group(1));
		}
		
	}
	
	
//	/**
//	 * @return   necessary information for the next action
//	 *           or null if no next api page exists
//	 *           @deprecated please never use this
//	 */
//	public ImagelinkTitles getNextAction() {
//		if( nextPageInfo == null ){ return null; }
//		else{
//			return new ImagelinkTitles(bot,nextPageInfo);
//		}
//	}

	public HttpAction getNextMessage() {
		return msg;
	}

	protected void prepareCollection() {

		if (init  || (!titleIterator.hasNext() && hasMoreResults)) {
			if (init) {
				msg = generateRequest(imageName, createNsString(namespaces), null);
			} else {
				
				msg = generateRequest(null, null, nextPageInfo);
			}
			init = false;
			try {

				bot.performAction(this);
				setHasMoreMessages(true);
				if (log.isDebugEnabled())
					log.debug("preparing success");
			} catch (ActionException e) {
				e.printStackTrace();
				setHasMoreMessages(false);
			} catch (ProcessException e) {
				e.printStackTrace();
				setHasMoreMessages(false);
			}

		}
	}
	
	


	@Override
	protected Object clone() throws CloneNotSupportedException {
		try {
			return new ImagelinkTitles(bot, imageName, namespaces);
		} catch (VersionException e) {
			throw new CloneNotSupportedException(e.getLocalizedMessage());
		}
	}
	
	

}
