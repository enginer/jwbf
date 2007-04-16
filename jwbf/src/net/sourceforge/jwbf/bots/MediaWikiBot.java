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
 * Philipp Kohl 
 */
package net.sourceforge.jwbf.bots;

import java.net.URL;
import java.util.AbstractCollection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.NamingException;

import net.sourceforge.jwbf.actions.http.ActionException;
import net.sourceforge.jwbf.actions.http.mw.GetAllPages;
import net.sourceforge.jwbf.actions.http.mw.GetCategoryElements;
import net.sourceforge.jwbf.actions.http.mw.GetCategoryPictures;
import net.sourceforge.jwbf.actions.http.mw.GetEnvironmentVars;
import net.sourceforge.jwbf.actions.http.mw.GetPageContent;
import net.sourceforge.jwbf.actions.http.mw.GetWhatlinkshereElements;
import net.sourceforge.jwbf.actions.http.mw.PostDelete;
import net.sourceforge.jwbf.actions.http.mw.PostLogin;
import net.sourceforge.jwbf.actions.http.mw.PostModifyContent;
import net.sourceforge.jwbf.contentRep.ContentAccessable;
import net.sourceforge.jwbf.contentRep.mw.EditContentAccessable;
import net.sourceforge.jwbf.contentRep.mw.SimpleArticle;

/**
 * 
 * This class helps you to interact with each mediawiki.
 * 
 * How to use:
 * 
 * <pre>
 * MediaWikiBot b = new MediaWikiBot(&quot;http://yourwiki.org&quot;);
 * b.login(&quot;Username&quot;, &quot;Password&quot;);
 * System.out.writeln(b.readContentOf(&quot;Main Page&quot;).getText());
 * </pre>
 * 
 * @author Thomas Stock
 * 
 */
public class MediaWikiBot extends HttpBot {

	private boolean loggedIn = false;

	/**
	 * @param u
	 *            wikihosturl like "http://www.mediawiki.org/wiki/index.php"
	 */
	public MediaWikiBot(final URL u) {
		super();
		setConnection(u);

	}

	/**
	 * Performs a Login.
	 * 
	 * @param username
	 *            the username
	 * @param passwd
	 *            the password
	 * @throws ActionException
	 *             on problems
	 */
	public final void login(final String username, final String passwd)
			throws ActionException {
		performAction(new PostLogin(username, passwd));
		loggedIn = true;

	}

	/**
	 * 
	 * @param name
	 *            of article in a mediawiki like "Main Page"
	 * @return a content representation of requested article, never null
	 * @throws ActionException
	 *             on problems or if conent null
	 */
	public final ContentAccessable readContentOf(final String name)
			throws ActionException {
		SimpleArticle a = null;

		a = new SimpleArticle(performAction(new GetPageContent(name)), name);

		return a;
	}

	/**
	 * 
	 * @param title
	 *            of category in a mediawiki like "Category:Small Things"
	 * @return with all article names in the requestet category
	 * @throws ActionException
	 *             on problems
	 */
	public final AbstractCollection<String> readCategory(final String title)
			throws ActionException {

		return readCategory(title, GetCategoryElements.ARTICLE);
	}

	/**
	 * TODO Feature not works realy.
	 * @param title
	 *            of category in a mediawiki like "Category:Small Things"
	 * 
	 * @param type
	 *            of category elements, GetCategoryElements.MEDIA |
	 *            GetCategoryElements.ARTICLE
	 * @return with all article names in the requestet category
	 * @throws ActionException
	 *             on problems
	 */
	public final AbstractCollection<String> readCategory(final String title,
			final byte type) throws ActionException {
		Vector<String> av = new Vector<String>();

		GetCategoryElements cel = new GetCategoryElements(title, av, type);
		performAction(cel);
		try {
			while (cel.hasMore()) {
				cel = new GetCategoryElements(title, cel.next().toString(), av,
						type);
				performAction(cel);
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}

		if (av.isEmpty()) {
			throw new ActionException("Category: \"" + title + "\" is empty");
		}

		return av;
	}
	/**
	 * 
	 * @param title
	 *            of category in a mediawiki like "Category:Small Things"
	 * @return with all article names in the requestet category
	 * @throws ActionException
	 *             on problems
	 */
	public final Iterator<String> readCategoryPics(final String title)
			throws ActionException {
		Vector<String> av = new Vector<String>();

		GetCategoryPictures cel = new GetCategoryPictures(title, av);
		performAction(cel);
		try {
			while (cel.hasMore()) {
				cel = new GetCategoryPictures(title, cel.next().toString(), av);
				performAction(cel);
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}

		if (av.isEmpty()) {
			throw new ActionException("Category: \"" + title + "\" is empty");
		}

		return av.iterator();
	}

	/**
	 * TODO infinite loop in GetWhatlinkshereElements.
	 * 
	 * @param title
	 *            of page in a mediawiki like "Main Page"
	 * @return with all article names what links to the page
	 * @throws ActionException
	 *             on problems
	 */
	public final AbstractCollection<String> readWhatLinksHere(final String title)
			throws ActionException {
		Vector<String> av = new Vector<String>();

		GetWhatlinkshereElements cel = new GetWhatlinkshereElements(title, av);
		performAction(cel);
		try {
			while (cel.hasMore()) {
				cel = new GetWhatlinkshereElements(title,
						cel.next().toString(), av);
				performAction(cel);
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}

		if (av.isEmpty()) {
			throw new ActionException("\"" + title + "\" is empty");
		}

		return av;
	}

	/**
	 * 
	 * @param a
	 *            write the article (if already exists) in the mediawiki
	 * @throws ActionException
	 *             on problems
	 */
	public final void writeContent(final EditContentAccessable a)
			throws ActionException {

		if (!loggedIn) {
			throw new ActionException("Please login first");
		}
		Hashtable<String, String> tab = new Hashtable<String, String>();
		performAction(new GetEnvironmentVars(a.getLabel(), tab));
		performAction(new PostModifyContent(a, tab));

	}

	/**
	 * Writes an iteration of ContentAccessables to the mediawiki.
	 * 
	 * @param cav
	 *            a
	 * @throws ActionException
	 *             on problems
	 */
	public final void writeMultContent(final Iterator<EditContentAccessable> cav)
			throws ActionException {
		while (cav.hasNext()) {
			writeContent(cav.next());

		}
	}

	/**
	 * 
	 * @param label
	 *            like "Tamplate:FooBar" or "Main Page"
	 * @throws ActionException
	 *             on problems
	 */
	public final void deleteArticle(final String label) throws ActionException {

		Hashtable<String, String> tab = new Hashtable<String, String>();
		performAction(new GetEnvironmentVars(label, tab));

		performAction(new PostDelete(label, tab));

	}
	/**
	 * Use ONLY in wikis with less articles.
	 * @return Articles from the Specialpage: AllPages
	 * @throws ActionException on problems
	 */
	public AbstractCollection<String> readAllPages() throws ActionException {
		Vector<String> av = new Vector<String>();

		GetAllPages cel = new GetAllPages(av);
		performAction(cel);
		try {
			while (cel.hasMore()) {
				cel = new GetAllPages(cel.next().toString(), av);
				performAction(cel);
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}

		if (av.isEmpty()) {
			throw new ActionException("Allpages is empty");
		}

		return av;
	}

}
