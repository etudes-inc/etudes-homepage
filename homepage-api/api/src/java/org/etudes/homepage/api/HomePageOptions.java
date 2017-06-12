/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-api/api/src/java/org/etudes/homepage/api/HomePageOptions.java $
 * $Id: HomePageOptions.java 9697 2014-12-27 00:32:01Z ggolden $
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

/**
 * HomePageOptions describe the settings for a site's home page.
 */
public interface HomePageOptions
{

	Boolean getAnncFull();

	Boolean getAnnouncementsEnabled();

	/**
	 * @return TRUE if the item has changes not stored, FALSE if not.
	 */
	Boolean getChanged();

	Boolean getChatEnabled();

	String getFormat();

	Long getId();

	Date getModifiedDate();

	String getModifiedUser();

	Integer getNumAnnouncements();

	String getOrder();

	Boolean getScheduleEnabled();

	String getSiteId();

	void setAnncFull(Boolean setting);

	void setAnnouncementsEnabled(Boolean setting);

	void setChatEnabled(Boolean setting);

	void setFormat(String format);

	void setModifiedDate(Date date);

	void setModifiedUser(String userId);

	void setNumAnnouncements(Integer setting);

	void setOrder(String order);

	void setScheduleEnabled(Boolean setting);

	void transferFrom(HomePageOptions other);
}
