/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-api/api/src/java/org/etudes/homepage/api/HomeContent.java $
 * $Id: HomeContent.java 6388 2013-11-24 03:14:47Z ggolden $
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

import java.util.List;

/**
 * HomeContent ...
 */
public interface HomeContent
{
	/**
	 * Access the current item.
	 * 
	 * @return A List containing the current HomeContentItem.
	 */
	List<HomeContentItem> getCurrent();

	/**
	 * Access the draft (unpublished) items.
	 * 
	 * @return A List containing the unpublished HomeContentItem.
	 */
	List<HomeContentItem> getDrafts();

	/**
	 * Access all the items.
	 * 
	 * @return A List containing all the items.
	 */
	List<HomeContentItem> getItems();

	/**
	 * Access the past released items.
	 * 
	 * @return A List containing the published and past-released HomeContentItems.
	 */
	List<HomeContentItem> getPast();

	/**
	 * Access the pending items.
	 * 
	 * @return A List containing the published HomeContentItem that are to-be-released.
	 */
	List<HomeContentItem> getPending();
}
