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
package net.sourceforge.jwbf.contentRep.mw;

import net.sourceforge.jwbf.contentRep.ContentAccessable;

/**
 * This is a simple content helper class that implements the EditContentAccesable
 * interface, plus setter methods.
 * 
 * @author Thomas Stock
 *
 */
public class SimpleArticle implements EditContentAccessable {

	private String label = "";
	private String editSummary = "";
	private String text = "";
	private boolean minorEdit = false;
	
	/**
	 * 
	 *
	 */
	public SimpleArticle() {
//		do nothing 
	}

	/**
	 * 
	 * @param ca a
	 */
	public SimpleArticle(ContentAccessable ca) {
		label = ca.getLabel();
		text = ca.getText();
	}
	/**
	 * @return the
	 */
	public String getEditSummary() {
		return editSummary;
	}
	/**
	 * 
	 * @param s the
	 */
	public void setEditSummary(final String s) {
		this.editSummary = s;
	}

	/**
	 * @return true if it is a minjor edit on the article
	 */
	public boolean isMinorEdit() {
		return minorEdit;
	}
	/**
	 * 
	 * @param minor the
	 */
	public void setMinorEdit(final boolean minor) {
		this.minorEdit = minor;
	}

	/**
	 * @return the label, like "Main Page"
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * 
	 * @param label the label, like "Main Page"
	 */
	public void setLabel(final String label) {
		this.label = label;
	}
	/**
	 * @return the content of the article
	 */
	public String getText() {
		return text;
	}
	/**
	 * 
	 * @param text the content of the article
	 */
	public void setText(final String text) {
		this.text = text;
	}

}
