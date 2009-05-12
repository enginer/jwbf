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
package net.sourceforge.jwbf.actions.mediawiki.util;

import java.util.Collection;
import java.util.Vector;

import net.sourceforge.jwbf.actions.ContentProcessable;
import net.sourceforge.jwbf.actions.mediawiki.MediaWiki;
import net.sourceforge.jwbf.actions.mediawiki.MediaWiki.Version;
import net.sourceforge.jwbf.actions.util.HttpAction;
import net.sourceforge.jwbf.actions.util.ProcessException;

import org.apache.log4j.Logger;

/**
 * @author Thomas Stock
 *
 */
@SupportedBy(MediaWiki.Version.MW1_11)
public abstract class MWAction implements ContentProcessable {

	private Version [] v;
	private Logger log = Logger.getLogger(getClass());
	private boolean hasMore = true;
	
	public boolean hasMoreMessages() {
		final boolean b = hasMore;
		hasMore = false;
//		if(log.isDebugEnabled())
//		log.debug("hasmore = " + b);
		return b;
	}
	
	protected void setHasMoreMessages(boolean b) {
		hasMore = b;
	}


	/**
	 * 
	 * @deprecated use {@link #MWAction(Version)} instead
	 */
	protected MWAction() throws VersionException {

	}
	
	protected MWAction(Version v) throws VersionException {
		checkVersionNewerEquals(v);
		
	}



	/**
	 * @param s
	 *            the returning text
	 * @param hm
	 *            the method object
	 * @return the returning text
	 * @throws ProcessException on processing problems
	 * 
	 */
	public String processReturningText(final String s, final HttpAction hm) throws ProcessException {
		return processAllReturningText(s);
	}

	/**
	 * @param s
	 *            the returning text
	 * @return the returning text
	 * @throws ProcessException never
	 * 
	 */
	public String processAllReturningText(final String s) throws ProcessException {
		return s;
	}
	
	protected final Version [] getVersionArray() {
		
		
		if (v != null)
			return v;
		v = findSupportedVersions(getClass());
		return v;
	}
	/**
	 * 
	 * @param clazz a
	 * @return an
	 */
	private Version [] findSupportedVersions(Class< ? > clazz) {
		if (clazz.getName().contains(MWAction.class.getName())) {
			Version [] v = new MediaWiki.Version[1];
			v[0] = Version.UNKNOWN;
			return v;
		} else if (clazz.isAnnotationPresent(SupportedBy.class)) {
			SupportedBy sb = clazz.getAnnotation(SupportedBy.class);
			System.err.println();
			if (log.isDebugEnabled()) {
				Version [] vtemp = sb.value();
				String sv = "";
				for (int i = 0; i < vtemp.length; i++) {
					sv += vtemp[i].getNumber() + ", ";
				}
				log.debug("found support for: " + sv);
			}
			return sb.value();
		} else {
			return findSupportedVersions(clazz.getSuperclass());
		}
	}
	
	protected void checkVersionNewerEquals(Version v) throws VersionException {
		if (getSupportedVersions().contains(v))
			return;
		for (Version vx : getSupportedVersions()) {
			if (v.greaterEqThen(vx))
				return;
		}
		throw new VersionException("unsupported version: " + v);
	}
	
	public Collection<Version> getSupportedVersions() {
		Collection<Version> v = new Vector<Version>();
		
		Version [] va = getVersionArray();
		for (int i = 0; i < va.length; i++) {
			v.add(va[i]);
		}
		
		return v;
	}
	
	/**
	 * helper method generating a namespace string as required by the MW-api.
	 *
	 * @param namespaces
	 *            namespace as
	 * @return with numbers seperated by |
	 */
	public static String createNsString(int... namespaces) {

		String namespaceString = "";

		if (namespaces != null && namespaces.length != 0) {
			for (int nsNumber : namespaces) {
				namespaceString += nsNumber + "|";
			}
			// remove last '|'
			if (namespaceString.endsWith("|")) {
				namespaceString = namespaceString.substring(0, namespaceString
						.length() - 1);
			}
		}
		return namespaceString;
	}
	
}
