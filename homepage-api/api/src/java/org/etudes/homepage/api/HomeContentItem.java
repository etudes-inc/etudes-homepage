/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-api/api/src/java/org/etudes/homepage/api/HomeContentItem.java $
 * $Id: HomeContentItem.java 6636 2013-12-16 21:26:20Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2013 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.homepage.api;

import java.util.Date;

/**
 * HomeContentItem ...
 */
public interface HomeContentItem
{
	/**
	 * Used as alt text for any item that supports that.
	 * 
	 * @return The alt text.
	 */
	String getAltText();

	/**
	 * @return TRUE if the item has changes not stored, FALSE if not.
	 */
	Boolean getChanged();

	/**
	 * Only for source "A" authored items, access the actual content body
	 * 
	 * @return the content item's body, or an empty string ("") if not a source "A".
	 */
	String getContent();

	/**
	 * @return the tool item id.
	 */
	Long getId();

	/**
	 * @return the date last modified.
	 */
	Date getModifiedDate();

	/**
	 * @return the user id who last modified the item.
	 */
	String getModifiedUser();

	/**
	 * @return the updated content string.
	 */
	String getNewContent();

	/**
	 * @return the published status.
	 */
	Boolean getPublished();

	/**
	 * @return the release date.
	 */
	Date getReleaseDate();

	/**
	 * @return the site id.
	 */
	String getSiteId();

	/**
	 * Supported sources are: "A" - authored, "F" - file from site resources, "W" - web resource, "Y" - youtube
	 * 
	 * @return the source type.
	 */
	String getSource();

	/**
	 * Display style notes: Used for source "W" (web resources) as a number of pixels of height of the display (no "px", just the number), and for source "Y" (youtube) as the ratio numbers ("16:9", "4:3", "1:1") for the size of the display.
	 * 
	 * @return the display style notes.
	 */
	String getStyle();

	/**
	 * @return the display title.
	 */
	String getTitle();

	/**
	 * @return the mime type of the resource, or "?" if not known.
	 */
	String getType();

	/**
	 * Used for source "W" as the web resource URL. Used for source "F" and "A" as the site resource tool reference URL. Used for "Y" as the youtube video id.
	 * 
	 * @return The item url.
	 */
	String getUrl();

	/**
	 * Set the alt text for the item.
	 * 
	 * @param alt
	 *        The new alt text.
	 */
	void setAltText(String alt);

	/**
	 * Set the modified date.
	 * 
	 * @param date
	 *        The modified date.
	 */
	void setModifiedDate(Date date);

	/**
	 * Set the modified-by user id.
	 * 
	 * @param userId
	 *        The user id making the change.
	 */
	void setModifiedUser(String userId);

	/**
	 * Set a new content string.
	 * 
	 * @param content
	 *        The new content (html) string.
	 */
	void setNewContent(String content);

	/**
	 * Set the published flag.
	 * 
	 * @param published
	 *        The published flag.
	 */
	void setPublished(Boolean published);

	/**
	 * Set a new release date
	 * 
	 * @param releaseDate
	 *        The new release date.
	 */
	void setReleaseDate(Date releaseDate);

	/**
	 * Set a new source for the item.
	 * 
	 * @param source
	 *        The new source: "A" - authored, "F" - file from site resources, "W" - web resource, "Y" - youtube
	 */
	void setSource(String source);

	/**
	 * Set a new display style: Used for source "W" (web resources) as a number of pixels of height of the display (no "px", just the number), and for source "Y" (youtube) as the ratio numbers ("16:9", "4:3", "1:1") for the size of the display.
	 * 
	 * @param style
	 *        The new display style
	 */
	void setStyle(String style);

	/**
	 * Set the display title for the item.
	 * 
	 * @param title
	 *        The new title.
	 */
	void setTitle(String title);

	/**
	 * Set the mime type for the item.
	 * 
	 * @param type
	 *        The item's mime type, or "?" if not known.
	 */
	void setType(String type);

	/**
	 * Set the item's URL. Used for source "W" as the web resource URL. Used for source "F" and "A" as the site resource tool reference URL. Used for "Y" as the youtube video id.
	 * 
	 * @param url
	 *        The items' new URL.
	 */
	void setUrl(String url);

	/**
	 * Transfer settings from the other item to this one, but leave this one unpublished.
	 * 
	 * @param other
	 *        The other item to copy from.
	 */
	void transferUnpublishedFrom(HomeContentItem other);
}
