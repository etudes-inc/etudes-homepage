/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-api/api/src/java/org/etudes/homepage/api/HomePageService.java $
 * $Id: HomePageService.java 8246 2014-06-12 23:26:37Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2014 Etudes, Inc.
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

import org.sakaiproject.exception.PermissionException;

/**
 * HomepageService ...
 */
public interface HomePageService
{
	/**
	 * Check permission for this user to access home page information for this site.
	 * 
	 * @param userId
	 *        The user id.
	 * @param siteId
	 *        The site id.
	 * @return true if the user is permitted, false if not.
	 */
	boolean allowAccess(String userId, String siteId);

	/**
	 * Check permission for this user to update home page information for this site.
	 * 
	 * @param userId
	 *        The user id.
	 * @param siteId
	 *        The site id.
	 * @return true if the user is permitted, false if not.
	 */
	boolean allowUpdate(String userId, String siteId);

	/**
	 * Apply a base date shift - moving all home item publication dates by the given number of days. (For the DateManagerService in SiteManage)
	 * 
	 * @param siteId
	 *        The site id.
	 * @param days
	 *        The days to shift.
	 */
	void applyBaseDateTx(String siteId, int days);

	/**
	 * If the site has info url or description, and there are no home items defined, "import" the url and / or description from the site, creating home item(s), and clear this information from the site.
	 * 
	 * @param userId
	 *        The user id.
	 * @param siteId
	 *        The site id.
	 * @param unpublished
	 *        If true, treat the items as should be from a site / archives import.
	 */
	void convertFromSiteInfo(String userId, String siteId, boolean asImport);

	/**
	 * Access the defined content items for this site.
	 * 
	 * @param siteId
	 *        The site id.
	 * @return The HomeContent defined for the site.
	 */
	HomeContent getContent(String siteId);

	/**
	 * Access the defined content item.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param siteId
	 *        the content's site id.
	 * @param contentId
	 *        The content id.
	 * @return The HomeContent with this id, or null if not found.
	 */
	HomeContentItem getContentItem(String siteId, Long contentId);

	/**
	 * Find the maximum publication date of all defined items in the site. (For the DateManagerService in SiteManage)
	 * 
	 * @param siteId
	 *        The site id.
	 * @return The maximum publication date of all defined items, or null if there are none.
	 */
	Date getMaxStartDate(String siteId);

	/**
	 * Find the minimum publication date of all defined items in the site. (For the DateManagerService in SiteManage)
	 * 
	 * @param siteId
	 *        The site id.
	 * @return The minimum publication date of all defined items, or null if there are none.
	 */
	Date getMinStartDate(String siteId);

	/**
	 * Access the homepage options for the site.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param siteId
	 *        The site id.
	 * @return The site's options, or a default set if none are set.
	 */
	HomePageOptions getOptions(String userId, String siteId) throws PermissionException;

	/**
	 * Get the status information for this site (for this user).
	 * 
	 * @param userId
	 *        The user id.
	 * @param siteId
	 *        The site id.
	 * @return The HomePageStatus object.
	 * @throws PermissionException
	 *         if the user does not have access to the site.
	 */
	HomePageStatus getStatus(String userId, String siteId) throws PermissionException;

	/**
	 * Check if this site has options set.
	 * 
	 * @param userId
	 *        The user making the request.
	 * @param siteId
	 *        The site id.
	 * @return TRUE if the site has options set, FALSE if not.
	 * @throws PermissionException
	 *         If the user does not have access to the site.
	 */
	Boolean hasOptions(String userId, String siteId) throws PermissionException;

	/**
	 * Create a new HomeContentItem, to be filled in and later saved with a call to saveContentItem().
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param siteId
	 *        The site id.
	 * @return The new HomeContentItem.
	 */
	HomeContentItem newContentItem(String userId, String siteId) throws PermissionException;

	/**
	 * Publish this item, if not published already, and if it has a release date.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param siteId
	 *        the content's site id.
	 * @param contentId
	 *        The item's id.
	 */
	void publishContentItem(String userId, String siteId, Long contentId) throws PermissionException;

	/**
	 * Remove all homepage data for a site.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param siteId
	 *        The site id.
	 */
	void purge(String userId, String siteId) throws PermissionException;

	/**
	 * Remove this content item.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param siteId
	 *        The content's site id.
	 * @param itemId
	 *        The item id.
	 */
	void removeContentItem(String userId, String siteid, Long itemId) throws PermissionException;

	/**
	 * Save changes to this item.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param item
	 *        The HomeContentItem.
	 */
	void saveContentItem(String userId, HomeContentItem item) throws PermissionException;

	/**
	 * Save changes to this item, unless the results would be a duplicate to an existing item (based on title, release date, type, source, and source details)
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param item
	 *        The HomeContentItem.
	 * @returns TRUE if the item was saved, FALSE if it was not.
	 */
	Boolean saveContentItemIfNotDuplicate(String userId, HomeContentItem item) throws PermissionException;

	/**
	 * Save the home page options for this site.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param options
	 *        The options for the site.
	 */
	void saveOptions(String userId, HomePageOptions options) throws PermissionException;

	/**
	 * Unpublish this item, if published already.
	 * 
	 * @param userId
	 *        The user id making the request.
	 * @param siteId
	 *        the content's site id.
	 * @param contentId
	 *        The item's id.
	 */
	void unpublishContentItem(String userId, String siteId, Long contentId) throws PermissionException;
}
