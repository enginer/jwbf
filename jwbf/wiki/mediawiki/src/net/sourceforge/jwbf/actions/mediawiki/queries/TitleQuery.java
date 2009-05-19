package net.sourceforge.jwbf.actions.mediawiki.queries;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.jwbf.actions.mediawiki.MediaWiki.Version;
import net.sourceforge.jwbf.actions.mediawiki.util.MWAction;
import net.sourceforge.jwbf.actions.mediawiki.util.VersionException;
import net.sourceforge.jwbf.actions.util.ActionException;
import net.sourceforge.jwbf.actions.util.HttpAction;
import net.sourceforge.jwbf.actions.util.ProcessException;
import net.sourceforge.jwbf.bots.MediaWikiBot;

import org.apache.log4j.Logger;

/**
 * Abstract class which is superclass of all titleiterations, represented by the sufix "Titles".
 * 
 * @author Thomas Stock
 *
 */
public abstract class TitleQuery<T> implements Iterable<T>, Iterator<T> {

	protected Iterator<T> titleIterator;
	private Logger log = Logger.getLogger(getClass());
	private InnerAction inner;
	private final MediaWikiBot bot;
	
	/** Information necessary to get the next api page. */
	protected String nextPageInfo = "";
	
	public final String getNextPageInfo() {
		return nextPageInfo;
	}

	protected TitleQuery(MediaWikiBot bot) throws VersionException {
		this.bot = bot;
		inner = getInnerAction(bot.getVersion());
	}

	protected InnerAction getInnerAction(Version v) throws VersionException {
		return new InnerAction(v);
	}

	@SuppressWarnings("unchecked")
	public final Iterator<T> iterator() {
		try {
			return (Iterator<T>) this.clone();
		} catch (CloneNotSupportedException e) {
			log.error("cloning should be supported");
			e.printStackTrace();
			return null;
		}
	}

	public final boolean hasNext() {
		doCollection();
		return titleIterator.hasNext();
	}

	public final T next() {
		doCollection();
		return titleIterator.next();
	}

	public final void remove() {
		titleIterator.remove();
	}
	
	protected abstract HttpAction prepareCollection();

	
	private boolean hasNextPage() {
		return nextPageInfo != null && nextPageInfo.length() > 0;
	}
	
	
	
	private void doCollection() {
		
		
		if (inner.init || (!titleIterator.hasNext() && hasNextPage())) {
			inner.init = false;
			try {
				inner.setHasMoreMessages(true);
				inner.msg = prepareCollection();
				
				bot.performAction(inner);

				

			} catch (ActionException ae) {
				ae.printStackTrace();

			} catch (ProcessException e) {
				e.printStackTrace();

			}
		}
	}
	
	protected abstract Collection<T> parseArticleTitles(String s);
	protected abstract String parseHasMore(final String s);
	
	public class InnerAction extends MWAction {

		private HttpAction msg;
		private boolean init = true;
		
		protected InnerAction(Version v) throws VersionException {
			super(v);
		}
		
		protected void setMessage(HttpAction msg) {
			this.msg = msg;
		}

		public HttpAction getNextMessage() {
			return msg;
		}

		
		/**
		 * Deals with the MediaWiki api's response by parsing the provided text.
		 * 
		 * @param s
		 *            the answer to the most recently generated MediaWiki-request
		 * 
		 * @return empty string
		 */
		public String processAllReturningText(final String s)
				throws ProcessException {
			Collection<T> knownResults = new Vector<T>();

			knownResults.addAll(parseArticleTitles(s));
			nextPageInfo = parseHasMore(s);

			titleIterator = knownResults.iterator();
			return "";
		}
		
	}
}
