/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-api/api/src/java/org/etudes/homepage/api/HomePageStatus.java $
 * $Id: HomePageStatus.java 7185 2014-01-20 00:23:26Z ggolden $
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

/**
 * HomePageStatus models the status information for the home page.
 */
public interface HomePageStatus
{
	Integer getNoVisitCount();

	Integer getPmCount();

	Integer getPostCount();

	Integer getReviewCountJForum();

	Integer getReviewCountMneme();

	Boolean isInstructor();
}
