/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-webapp/webapp/src/java/org/etudes/homepage/cdp/HomePageCdpHandler.java $
 * $Id: HomePageCdpHandler.java 10497 2015-04-17 20:08:29Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2014, 2015 Etudes, Inc.
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

package org.etudes.homepage.cdp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.basicltiContact.SakaiBLTIUtil;
import org.etudes.cdp.api.CdpHandler;
import org.etudes.cdp.api.CdpStatus;
import org.etudes.cdp.util.CdpResponseHelper;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapMap;
import org.etudes.coursemap.api.CourseMapService;
import org.etudes.homepage.api.HomeContent;
import org.etudes.homepage.api.HomeContentItem;
import org.etudes.homepage.api.HomePageOptions;
import org.etudes.homepage.api.HomePageService;
import org.etudes.homepage.api.HomePageStatus;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.chat.api.ChatMessage;
import org.sakaiproject.chat.api.ChatService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StringUtil;

/**
 */
public class HomePageCdpHandler implements CdpHandler
{
	class CmEvent
	{
		String actionEdit;
		String actionPerform;
		String actionReview;
		List<Reference> attachments;
		String cmType;
		String description;
		Date end;
		String id;
		String location;
		Date start;
		String title;
		String type;
	}

	protected static String[] announcementTool =
	{ "sakai.announcements" };

	protected static String[] calendarTool =
	{ "sakai.schedule" };

	protected static String[] coursemapTool =
	{ "sakai.coursemap" };

	/** the time gap between messages to signify a gap in the conversation (1 hour). */
	protected static long GAP = 60 * 60 * 1000;

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(HomePageCdpHandler.class);

	private static final String PARAM_CHANNEL = "channel";

	public String getPrefix()
	{
		return "homepage";
	}

	public Map<String, Object> handle(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String requestPath,
			String path, String authenticatedUserId) throws ServletException, IOException
	{
		if (requestPath.equals("homePage"))
		{
			return dispatchHomePage(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("setOptions"))
		{
			return dispatchSetOptions(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("events"))
		{
			return dispatchEvents(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("eventsDays"))
		{
			return dispatchEventsDays(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("chat"))
		{
			return dispatchChat(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("saveContent"))
		{
			return dispatchSaveContent(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("deleteContent"))
		{
			return dispatchDeleteContent(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("publishContent"))
		{
			return dispatchPublishContent(req, res, parameters, path, authenticatedUserId);
		}
		else if (requestPath.equals("unpublishContent"))
		{
			return dispatchUnpublishContent(req, res, parameters, path, authenticatedUserId);
		}
		return null;
	}

	protected Map<String, Object> dispatchChat(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchEventsDays - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchEventsDays - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowAccess(userId, siteId))
		{
			M_log.warn("dispatchEventsDays - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		doChat(rv, site);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchDeleteContent(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchDeleteContent - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchDeleteContent - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowUpdate(userId, siteId))
		{
			M_log.warn("dispatchEventsDays - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the content ids parameter
		String contentIds = (String) parameters.get("contentIds");
		if (contentIds == null)
		{
			M_log.warn("dispatchDeleteContent - no contentIds parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		String[] idStrs = StringUtil.split(contentIds, "\t");
		Long[] ids = new Long[idStrs.length];
		int i = 0;
		for (String id : idStrs)
		{
			try
			{
				ids[i++] = Long.parseLong(id);
			}
			catch (NumberFormatException e)
			{
				M_log.warn("dispatchDeleteContent - contentId not Long: " + contentIds);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
				return rv;
			}
		}
		for (Long id : ids)
		{
			try
			{
				homePageService().removeContentItem(userId, siteId, id);
			}
			catch (PermissionException e)
			{
				M_log.warn("dispatchDeleteContent:" + e);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
				return rv;
			}
		}

		// return the new content details
		loadHomeContentItems(rv, site, userId, false);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchEvents(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchEvents - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchEvents - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowAccess(userId, siteId))
		{
			M_log.warn("dispatchEvents - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// year month and day are expressed in the user's local (browser) time zone: year ~ 2013, month ~ (1..12), day ~ (1..31)
		String yearStr = (String) parameters.get("year");
		String monthStr = (String) parameters.get("month");
		String dayStr = (String) parameters.get("day");

		// If missing, use the "current" based on server time, user's time zone
		if ((yearStr == null) && (monthStr == null) && (dayStr == null))
		{
			String[] now = CdpResponseHelper.dateBreakdownInUserZone(System.currentTimeMillis());
			yearStr = now[0];
			monthStr = now[1];
			dayStr = now[2];
		}

		if (yearStr == null)
		{
			M_log.warn("dispatchEvents - no year parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int year = 0;
		try
		{
			year = Integer.valueOf(yearStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchEvents - year not int: " + yearStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		if (monthStr == null)
		{
			M_log.warn("dispatchEvents - no month parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int month = 0;
		try
		{
			month = Integer.valueOf(monthStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchEvents - month not int: " + monthStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		if (dayStr == null)
		{
			M_log.warn("dispatchEvents - no day parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int day = 0;
		try
		{
			day = Integer.valueOf(dayStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchEvents - day not int: " + dayStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		TimeRange dayRange = CdpResponseHelper.dayInUserZone(year, month, day);
		rv.put("eventsDate", CdpResponseHelper.dateDisplayInUserZone(dayRange.firstTime().getTime()));
		rv.put("eventsDateZone", CdpResponseHelper.zoneDisplayInUserZone(dayRange.firstTime().getTime()));

		doEvents(rv, year, month, day, site, userId);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchEventsDays(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchEventsDays - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchEventsDays - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// year and month are expressed in the user's local (browser) time zone: year ~ 2013, month ~ (1..12).
		String yearStr = (String) parameters.get("year");
		String monthStr = (String) parameters.get("month");

		// If missing, use the "current" based on server time, user's time zone
		if ((yearStr == null) && (monthStr == null))
		{
			String[] now = CdpResponseHelper.dateBreakdownInUserZone(System.currentTimeMillis());
			yearStr = now[0];
			monthStr = now[1];
		}

		if (yearStr == null)
		{
			M_log.warn("dispatchEventsDays - no year parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int year = 0;
		try
		{
			year = Integer.valueOf(yearStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchEventsDays - year not int: " + yearStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		if (monthStr == null)
		{
			M_log.warn("dispatchEventsDays - no month parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int month = 0;
		try
		{
			month = Integer.valueOf(monthStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchEventsDays - month not int: " + monthStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowAccess(userId, siteId))
		{
			M_log.warn("dispatchEventsDays - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		doEvenstDays(rv, year, month, site, userId);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchHomePage(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String authenticatedUserId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		if (authenticatedUserId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchHomePage - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchHomePage - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// year month and day are expressed in the user's local (browser) time zone: year ~ 2013, month ~ (1..12), day ~ (1..31)
		String yearStr = (String) parameters.get("year");
		String monthStr = (String) parameters.get("month");
		String dayStr = (String) parameters.get("day");

		// If missing, use the "current" based on server time, user's time zone
		if ((yearStr == null) && (monthStr == null) && (dayStr == null))
		{
			String[] now = CdpResponseHelper.dateBreakdownInUserZone(System.currentTimeMillis());
			yearStr = now[0];
			monthStr = now[1];
			dayStr = now[2];
		}

		if (yearStr == null)
		{
			M_log.warn("dispatchHomePage - no year parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int year = 0;
		try
		{
			year = Integer.valueOf(yearStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchHomePage - year not int: " + yearStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		if (monthStr == null)
		{
			M_log.warn("dispatchHomePage - no month parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int month = 0;
		try
		{
			month = Integer.valueOf(monthStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchHomePage - month not int: " + monthStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		if (dayStr == null)
		{
			M_log.warn("dispatchHomePage - no day parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int day = 0;
		try
		{
			day = Integer.valueOf(dayStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchHomePage - day not int: " + dayStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		// optional - to deliver only the current item, not the full set
		String currentOnlyStr = (String) parameters.get("currentOnly");
		boolean currentOnly = "true".equals(currentOnlyStr);

		// if not authorized
		if (!homePageService().allowAccess(authenticatedUserId, siteId))
		{
			M_log.warn("dispatchHomePage - not authorized - user: " + authenticatedUserId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		loadHomeContent(rv, year, month, day, site, authenticatedUserId, currentOnly);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchPublishContent(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchPublishContent - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchPublishContent - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowUpdate(userId, siteId))
		{
			M_log.warn("dispatchPublishContent - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the content ids parameter
		String contentIds = (String) parameters.get("contentIds");
		if (contentIds == null)
		{
			M_log.warn("dispatchPublishContent - no contentIds parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		String[] idStrs = StringUtil.split(contentIds, "\t");
		Long[] ids = new Long[idStrs.length];
		int i = 0;
		for (String id : idStrs)
		{
			try
			{
				ids[i++] = Long.parseLong(id);
			}
			catch (NumberFormatException e)
			{
				M_log.warn("dispatchPublishContent - contentId not Long: " + contentIds);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
				return rv;
			}
		}
		for (Long id : ids)
		{
			try
			{
				homePageService().publishContentItem(userId, siteId, id);
			}
			catch (PermissionException e)
			{
				M_log.warn("dispatchPublishContent:" + e);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
				return rv;
			}
		}

		// return the new content details
		loadHomeContentItems(rv, site, userId, false);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchSaveContent(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchSaveContent - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchSaveContent - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowUpdate(userId, siteId))
		{
			M_log.warn("dispatchSaveContent - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		String idStr = (String) parameters.get("contentId");
		Long contentId = null;
		if (idStr != null)
		{
			try
			{
				contentId = Long.parseLong(idStr);
			}
			catch (NumberFormatException e)
			{
				M_log.warn("dispatchSaveContent - contentId not long: " + idStr);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
				return rv;
			}
		}

		String source = (String) parameters.get("source");
		if (source == null)
		{
			M_log.warn("dispatchSaveContent - no source parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		String title = (String) parameters.get("title");
		if (title == null)
		{
			M_log.warn("dispatchSaveContent - no title parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		String releaseDateStr = (String) parameters.get("releaseDate");
		if (releaseDateStr == null)
		{
			M_log.warn("dispatchSaveContent - no releaseDate parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Date releaseDate = null;
		if (releaseDateStr.length() > 0)
		{
			try
			{
				releaseDate = CdpResponseHelper.dateFromDateTimeDisplayInUserZone(releaseDateStr);
			}
			catch (IllegalArgumentException e)
			{
			}
		}

		String url = (String) parameters.get("url");
		String style = (String) parameters.get("style");

		String publishedStr = (String) parameters.get("published");
		if (publishedStr == null)
		{
			M_log.warn("dispatchSaveContent - no published parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		Boolean published = publishedStr.equals("1");

		String alt = (String) parameters.get("alt");
		String content = (String) parameters.get("content");

		HomeContentItem item = null;
		if (contentId != null)
		{
			item = homePageService().getContentItem(siteId, contentId);
			if (item == null)
			{
				M_log.warn("dispatchSaveContent - content item not found: " + idStr);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
				return rv;
			}
		}
		else
		{
			try
			{
				item = homePageService().newContentItem(userId, site.getId());
				// start new ones draft
				item.setPublished(Boolean.FALSE);
			}
			catch (PermissionException e)
			{
				M_log.warn("dispatchSaveContent:" + e);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
				return rv;
			}
		}

		item.setSource(source);
		item.setTitle(title);
		item.setReleaseDate(releaseDate);
		item.setStyle(style);

		// set the new content for authored items
		if (source.equals("A"))
		{
			item.setNewContent(content);
		}
		// or the URL for all other items
		else
		{
			item.setUrl(url);
		}

		item.setAltText(alt);

		// if published set from the UI, set published. If not set, leave published alone.
		if (published) item.setPublished(published);

		// unpublish if missing a release date
		if (releaseDate == null)
		{
			item.setPublished(Boolean.FALSE);
		}
		item.setType("?");

		try
		{
			homePageService().saveContentItem(userId, item);
		}
		catch (PermissionException e)
		{
			M_log.warn("dispatchSaveContent:" + e);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// return the new content details
		loadHomeContentItems(rv, site, userId, false);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchSetOptions(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters, String path,
			String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchSetOptions - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchSetOptions - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowUpdate(userId, siteId))
		{
			M_log.warn("dispatchSetOptions - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// year month and day are expressed in the user's local (browser) time zone: year ~ 2013, month ~ (1..12), day ~ (1..31)
		String yearStr = (String) parameters.get("year");
		String monthStr = (String) parameters.get("month");
		String dayStr = (String) parameters.get("day");

		// If missing, use the "current" based on server time, user's time zone
		if ((yearStr == null) && (monthStr == null) && (dayStr == null))
		{
			String[] now = CdpResponseHelper.dateBreakdownInUserZone(System.currentTimeMillis());
			yearStr = now[0];
			monthStr = now[1];
			dayStr = now[2];
		}

		if (yearStr == null)
		{
			M_log.warn("dispatchSetOptions - no year parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int year = 0;
		try
		{
			year = Integer.valueOf(yearStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchSetOptions - year not int: " + yearStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		if (monthStr == null)
		{
			M_log.warn("dispatchSetOptions - no month parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int month = 0;
		try
		{
			month = Integer.valueOf(monthStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchSetOptions - month not int: " + monthStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		if (dayStr == null)
		{
			M_log.warn("dispatchSetOptions - no day parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		int day = 0;
		try
		{
			day = Integer.valueOf(dayStr);
		}
		catch (NumberFormatException e)
		{
			M_log.warn("dispatchSetOptions - day not int: " + dayStr);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		// optional
		String anncCountStr = (String) parameters.get("announcements");
		int anncCount = 5;
		if (anncCountStr != null)
		{
			try
			{
				anncCount = Integer.valueOf(anncCountStr);
			}
			catch (NumberFormatException e)
			{
				M_log.warn("dispatchSetOptions - announcements not int: " + anncCountStr);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
				return rv;
			}
		}

		// Note: fields are optional
		String anncDisplay = (String) parameters.get("anncDisplay");
		String schdDisplay = (String) parameters.get("schdDisplay");
		String chatDisplay = (String) parameters.get("chatDisplay");
		String format = (String) parameters.get("format");
		String anncFull = (String) parameters.get("anncFull");
		String order = (String) parameters.get("order");

		try
		{
			HomePageOptions options = homePageService().getOptions(userId, siteId);
			if (anncDisplay != null)
			{
				options.setAnnouncementsEnabled(anncDisplay.equals("1"));
			}

			if (anncCountStr != null)
			{
				options.setNumAnnouncements(anncCount);
			}

			if (schdDisplay != null)
			{
				options.setScheduleEnabled(schdDisplay.equals("1"));
			}

			if (chatDisplay != null)
			{
				options.setChatEnabled(chatDisplay.equals("1"));
			}
			if (format != null)
			{
				options.setFormat(format);
			}
			if (anncFull != null)
			{
				options.setAnncFull(anncFull.equals("1"));
			}
			if (order != null)
			{
				options.setOrder(order);
			}

			homePageService().saveOptions(userId, options);
		}
		catch (PermissionException e)
		{
			M_log.warn("dispatchSetOptions:" + e);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// return the "homePage" request details
		loadHomeContent(rv, year, month, day, site, userId, false);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	protected Map<String, Object> dispatchUnpublishContent(HttpServletRequest req, HttpServletResponse res, Map<String, Object> parameters,
			String path, String userId) throws ServletException, IOException
	{
		Map<String, Object> rv = new HashMap<String, Object>();

		// if not authenticated
		if (userId == null)
		{
			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the site id parameter
		String siteId = (String) parameters.get("siteId");
		if (siteId == null)
		{
			M_log.warn("dispatchUnpublishContent - no siteId parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}

		Site site = null;
		try
		{
			site = siteService().getSite(siteId);
		}
		catch (IdUnusedException e)
		{
		}

		if (site == null)
		{
			M_log.warn("dispatchUnpublishContent - missing site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// if not authorized
		if (!homePageService().allowUpdate(userId, siteId))
		{
			M_log.warn("dispatchUnpublishContent - not authorized - user: " + userId + " site: " + siteId);

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
			return rv;
		}

		// get the content ids parameter
		String contentIds = (String) parameters.get("contentIds");
		if (contentIds == null)
		{
			M_log.warn("dispatchUnpublishContent - no contentIds parameter");

			// add status parameter
			rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
			return rv;
		}
		String[] idStrs = StringUtil.split(contentIds, "\t");
		Long[] ids = new Long[idStrs.length];
		int i = 0;
		for (String id : idStrs)
		{
			try
			{
				ids[i++] = Long.parseLong(id);
			}
			catch (NumberFormatException e)
			{
				M_log.warn("dispatchUnpublishContent - contentId not Long: " + contentIds);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.badRequest.getId());
				return rv;
			}
		}
		for (Long id : ids)
		{
			try
			{
				homePageService().unpublishContentItem(userId, siteId, id);
			}
			catch (PermissionException e)
			{
				M_log.warn("dispatchUnpublishContent:" + e);

				// add status parameter
				rv.put(CdpStatus.CDP_STATUS, CdpStatus.accessDenied.getId());
				return rv;
			}
		}

		// return the new content details
		loadHomeContentItems(rv, site, userId, false);

		// add status parameter
		rv.put(CdpStatus.CDP_STATUS, CdpStatus.success.getId());

		return rv;
	}

	/**
	 * Lookup a user's display name.
	 * 
	 * @param userId
	 *        The user ID.
	 * @return The user's display name, or "?" if not found.
	 */
	protected String displayName(String userId)
	{
		try
		{
			User u = userDirectoryService().getUser(userId);
			return u.getDisplayName();
		}
		catch (UserNotDefinedException e)
		{
		}
		return "?";
	}

	@SuppressWarnings("unchecked")
	protected void doChat(Map<String, Object> rv, Site site)
	{
		String defaultChannel = null;
		List<Map<String, Object>> chat = new ArrayList<Map<String, Object>>();
		rv.put("chat", chat);

		ToolConfiguration toolConfig = site.getToolForCommonId("sakai.chat");
		if (toolConfig != null)
		{
			defaultChannel = StringUtil.trimToNull(toolConfig.getConfig().getProperty(PARAM_CHANNEL));
			if (defaultChannel == null) defaultChannel = chatService().channelReference(site.getId(), SiteService.MAIN_CONTAINER);
		}
		try
		{
			// recent only, oldest first
			List<ChatMessage> messages = (List<ChatMessage>) chatService().getMessages(defaultChannel, null, 5, true, false, false);

			long last = 0;
			for (ChatMessage m : messages)
			{
				Map<String, Object> msg = new HashMap<String, Object>();
				chat.add(msg);
				msg.put("from", m.getChatHeader().getFrom().getDisplayName());
				msg.put("date", CdpResponseHelper.dateTimeDisplayInUserZone(m.getChatHeader().getDate().getTime()));
				msg.put("message", m.getBody());
				msg.put("timegap", CdpResponseHelper.formatBoolean(m.getChatHeader().getDate().getTime() - last > GAP));

				last = m.getChatHeader().getDate().getTime();
			}
		}
		catch (PermissionException e)
		{

		}
	}

	/**
	 * Process the eventsDays response.
	 * 
	 * @param rv
	 * @param year
	 * @param month
	 * @param sites
	 */
	protected void doEvenstDays(Map<String, Object> rv, int year, int month, Site site, String userId)
	{
		// build up a map to return
		Map<String, Object> map = new HashMap<String, Object>();
		rv.put("eventsDays", map);

		map.put("year", Integer.toString(year));
		map.put("month", CdpResponseHelper.twoDigits(month));

		List<Integer> days = new ArrayList<Integer>();
		map.put("days", days);

		// get events
		List<CalendarEvent> events = getMonthEvents(site, year, month);

		// get the CM events
		List<CmEvent> cmEvents = getMonthCmEvents(site, userId, year, month);

		// check each day
		for (int day = 1; day <= 31; day++)
		{
			// make a time range for this day in the month / year, in the user's zone
			TimeRange dayRange = CdpResponseHelper.dayInUserZone(year, month, day);

			if (dayRange == null) break;

			// do any events overlap this range?
			boolean added = false;
			for (CalendarEvent event : events)
			{
				if (event.getRange().overlaps(dayRange))
				{
					days.add(day);
					added = true;
					break;
				}
			}

			// and check from the cm
			if (!added)
			{
				for (CmEvent event : cmEvents)
				{
					TimeRange range = timeService().newTimeRange(event.start.getTime(), 1000);
					if (range.overlaps(dayRange))
					{
						days.add(day);
						added = true;
						break;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void doEvents(Map<String, Object> rv, int year, int month, int day, Site site, String userId)
	{
		TimeRange dayRange = CdpResponseHelper.dayInUserZone(year, month, day);
		rv.put("eventsDate", CdpResponseHelper.dateDisplayInUserZone(dayRange.firstTime().getTime()));
		rv.put("eventsDateZone", CdpResponseHelper.zoneDisplayInUserZone(dayRange.firstTime().getTime()));

		// the events
		List<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
		rv.put("events", events);

		// get events for this day
		List<CalendarEvent> dayEvents = getDayEvents(site, year, month, day);

		// get the CM events for this day
		List<CmEvent> cmDayEvents = getDayCmEvents(site, userId, year, month, day);

		// add in the calendar events
		for (CalendarEvent e : dayEvents)
		{
			CmEvent event = new CmEvent();
			event.start = new Date(e.getRange().firstTime().getTime());
			event.end = new Date(e.getRange().lastTime().getTime());
			event.type = e.getType();
			event.description = e.getDescriptionFormatted();
			event.location = e.getLocation();
			event.title = e.getDisplayName();
			event.attachments = e.getAttachments();
			cmDayEvents.add(event);
		}

		// sort by time ascending
		Collections.sort(cmDayEvents, new Comparator<CmEvent>()
		{
			public int compare(CmEvent arg0, CmEvent arg1)
			{
				return arg0.start.compareTo(arg1.start);
			}
		});

		for (CmEvent e : cmDayEvents)
		{
			Map<String, Object> event = new HashMap<String, Object>();
			events.add(event);

			if (e.end != null)
			{
				event.put("time", CdpResponseHelper.timeDisplayInUserZone(e.start.getTime(), e.end.getTime()));
			}
			else
			{
				event.put("time", CdpResponseHelper.timeDisplayInUserZone(e.start.getTime()));
			}
			event.put("type", e.type);
			if (e.description != null) event.put("description", e.description);
			if (e.location != null) event.put("location", e.location);
			event.put("title", e.title);

			if (e.cmType != null) event.put("cmType", e.cmType);
			if (e.actionEdit != null) event.put("actionEdit", e.actionEdit);
			if (e.actionPerform != null) event.put("actionPerform", e.actionPerform);
			if (e.actionReview != null) event.put("actionReview", e.actionReview);

			if ((e.attachments != null) && (!e.attachments.isEmpty()))
			{
				List<Map<String, String>> attachments = new ArrayList<Map<String, String>>();
				event.put("attachments", attachments);

				for (Reference a : (List<Reference>) (e.attachments))
				{
					Map<String, String> attachment = new HashMap<String, String>();
					attachments.add(attachment);

					String url = a.getUrl();
					attachment.put("url", url);
					String description = a.getProperties().getPropertyFormatted("DAV:displayname");
					attachment.put("description", description);
				}
			}
		}
	}

	protected List<CmEvent> getDayCmEvents(Site site, String userId, int year, int month, int day)
	{
		boolean mayEdit = courseMapService().allowEditMap(site.getId(), userId);

		TimeRange dayRange = CdpResponseHelper.dayInUserZone(year, month, day);
		List<CmEvent> rv = new ArrayList<CmEvent>();

		// if the site does NOT include the calendar tool, skip it
		if (site.getTools(coursemapTool).isEmpty()) return rv;

		// Note: even if this is for the (mayEdit) instructor, we are using the map, not the getMapEdit(). The map is cached, and for the dashboard request, needed again for the month events
		CourseMapMap map = courseMapService().getMap(site.getId(), userId);

		for (CourseMapItem item : map.getItems())
		{
			Date open = item.getOpen();
			if ((open != null) && (dayRange.contains(timeService().newTime(open.getTime()))))
			{
				CmEvent event = new CmEvent();
				rv.add(event);
				event.id = item.getMapId();
				event.title = "Open Date for " + item.getType().getDisplayString() + ": " + item.getTitle();
				event.type = "CourseMap Date";
				event.start = open;
				event.cmType = item.getType().toString();
				if (mayEdit && (item.getEditLink() != null))
				{
					event.actionEdit = "/" + item.getToolId() + item.getEditLink();
				}
				if ((item.getPerformLink() != null) && ((!item.getBlocked()) || mayEdit))
				{
					event.actionPerform = "/" + item.getToolId() + item.getPerformLink();
				}
				if (item.getReviewLink() != null)
				{
					event.actionReview = "/" + item.getToolId() + item.getReviewLink();
				}
			}
			Date due = item.getDue();
			if ((due != null) && (dayRange.contains(timeService().newTime(due.getTime()))))
			{
				CmEvent event = new CmEvent();
				rv.add(event);
				event.id = item.getMapId();
				event.title = "Due Date for " + item.getType().getDisplayString() + ": " + item.getTitle();
				event.type = "CourseMap Date";
				event.start = due;
				event.cmType = item.getType().toString();
				if (mayEdit && (item.getEditLink() != null))
				{
					event.actionEdit = "/" + item.getToolId() + item.getEditLink();
				}
				if ((item.getPerformLink() != null) && ((!item.getBlocked()) || mayEdit))
				{
					event.actionPerform = "/" + item.getToolId() + item.getPerformLink();
				}
				if (item.getReviewLink() != null)
				{
					event.actionReview = "/" + item.getToolId() + item.getReviewLink();
				}
			}
		}

		return rv;
	}

	/**
	 * Get the user's events for the site for this day in month in this year (in user's time zone terms).
	 * 
	 * @param sites
	 *        The user's sites.
	 * @param year
	 *        The year (~2013)
	 * @param month
	 *        The month (~1..12)
	 * @param day
	 *        The day (~1..31)
	 * @return The events.
	 */
	protected List<CalendarEvent> getDayEvents(Site site, int year, int month, int day)
	{
		TimeRange dayRange = CdpResponseHelper.dayInUserZone(year, month, day);
		List<CalendarEvent> rv = new ArrayList<CalendarEvent>();

		// if the site does NOT include the calendar tool, skip it
		if (site.getTools(calendarTool).isEmpty()) return rv;

		try
		{
			Calendar cal = calendarService().getCalendar(calendarService().calendarReference(site.getId(), SiteService.MAIN_CONTAINER));
			@SuppressWarnings("unchecked")
			List<CalendarEvent> events = cal.getEvents(dayRange, null);

			for (CalendarEvent e : events)
			{
				rv.add(e);
			}
		}
		catch (IdUnusedException e)
		{
			// no calendar for the site
		}
		catch (PermissionException e)
		{
			// not permitted? Evaluators are not - ignore.
		}

		return rv;
	}

	/**
	 * Get the user's events from in this site's CM for this month in this year (in user's time zone terms).
	 * 
	 * @param site
	 *        The site.
	 * @param userId
	 *        The user id.
	 * @param year
	 *        The year (~2013)
	 * @param month
	 *        The month (~1..12)
	 * @return The events.
	 */
	protected List<CmEvent> getMonthCmEvents(Site site, String userId, int year, int month)
	{
		TimeRange monthRange = CdpResponseHelper.monthInUserZone(year, month);
		List<CmEvent> rv = new ArrayList<CmEvent>();

		// if the site does NOT include the calendar tool, skip it
		if (site.getTools(coursemapTool).isEmpty()) return rv;

		CourseMapMap map = courseMapService().getMap(site.getId(), userId);
		for (CourseMapItem item : map.getItems())
		{
			Date open = item.getOpen();
			if ((open != null) && (monthRange.contains(timeService().newTime(open.getTime()))))
			{
				CmEvent event = new CmEvent();
				rv.add(event);
				event.start = open;
			}
			Date due = item.getDue();
			if ((due != null) && (monthRange.contains(timeService().newTime(due.getTime()))))
			{
				CmEvent event = new CmEvent();
				rv.add(event);
				event.start = due;
			}
		}

		return rv;
	}

	/**
	 * Get the user's events from in this site for this month in this year (in user's time zone terms).
	 * 
	 * @param site
	 *        The site.
	 * @param year
	 *        The year (~2013)
	 * @param month
	 *        The month (~1..12)
	 * @return The events.
	 */
	protected List<CalendarEvent> getMonthEvents(Site site, int year, int month)
	{
		TimeRange monthRange = CdpResponseHelper.monthInUserZone(year, month);
		List<CalendarEvent> rv = new ArrayList<CalendarEvent>();

		// if the site does NOT include the calendar tool, skip it
		if (site.getTools(calendarTool).isEmpty()) return rv;

		try
		{
			Calendar cal = calendarService().getCalendar(calendarService().calendarReference(site.getId(), SiteService.MAIN_CONTAINER));
			@SuppressWarnings("unchecked")
			List<CalendarEvent> events = cal.getEvents(monthRange, null);

			rv.addAll(events);
		}
		catch (IdUnusedException e)
		{
			// no calendar for the site
		}
		catch (PermissionException e)
		{
			// not permitted? Evaluators are no - ignore.
		}

		return rv;
	}

	@SuppressWarnings("unchecked")
	protected void loadAnnouncement(AnnouncementMessage msg, Map<String, Object> messageMap)
	{
		messageMap.put("subject", msg.getAnnouncementHeader().getSubject());
		messageMap.put("messageId", msg.getReference());
		long date = msg.getAnnouncementHeader().getDate().getTime();
		try
		{
			date = msg.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE).getTime();
		}
		catch (EntityPropertyTypeException e)
		{
		}
		catch (EntityPropertyNotDefinedException e)
		{
		}
		messageMap.put("date", CdpResponseHelper.dateTimeDisplayInUserZone(date));
		messageMap.put("day", CdpResponseHelper.dateDisplayInUserZone(date));
		messageMap.put("from", msg.getAnnouncementHeader().getFrom().getDisplayName());
		messageMap.put("fromUserId", msg.getAnnouncementHeader().getFrom().getId());

		String body = msg.getBody();

		// Note: removed with ETU-520, as this is most likely not needed, and interferes with the new ZIP file handling
		// body = CdpResponseHelper.accessToCdpDoc(body, false);

		messageMap.put("body", body);

		messageMap.put("unread", CdpResponseHelper.formatBoolean(false));
		messageMap.put("draft", CdpResponseHelper.formatBoolean(msg.getHeader().getDraft()));

		String priorityStr = msg.getProperties().getProperty(AnnouncementService.NOTIFICATION_LEVEL);
		Boolean priority = ((priorityStr == null) ? Boolean.FALSE : Boolean.valueOf(priorityStr.equals("r")));
		messageMap.put("priority", CdpResponseHelper.formatBoolean(priority));

		try
		{
			Time releaseDate = msg.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
			if (releaseDate != null) messageMap.put("releaseDate", CdpResponseHelper.dateTimeDisplayInUserZone(releaseDate.getTime()));
		}
		catch (EntityPropertyNotDefinedException e)
		{
		}
		catch (EntityPropertyTypeException e)
		{
		}

		if (!msg.getAnnouncementHeader().getAttachments().isEmpty())
		{
			List<Map<String, String>> attachments = new ArrayList<Map<String, String>>();
			messageMap.put("attachments", attachments);

			for (Reference a : (List<Reference>) (msg.getAnnouncementHeader().getAttachments()))
			{
				if ((a == null) || (a.getUrl() == null) || (a.getProperties() == null)) continue;

				Map<String, String> attachment = new HashMap<String, String>();
				attachments.add(attachment);

				String url = a.getUrl();
				attachment.put("url", url);
				String description = a.getProperties().getPropertyFormatted("DAV:displayname");
				attachment.put("description", description);
			}
		}
	}

	protected void loadHomeContent(Map<String, Object> rv, int year, int month, int day, Site site, String userId, boolean currentOnly)
	{
		rv.put("siteTitle", site.getTitle());

		boolean enableVT = (SakaiBLTIUtil.showProviderInEditor(site.getId(), "VoiceThread Editor")) ? true : false;
		rv.put("enableVT", enableVT);
		rv.put("serverUrl", ServerConfigurationService.getString("serverUrl"));
		rv.put("siteId", site.getId());

		Preferences prefs = preferencesService().getPreferences(userId);
		ResourceProperties props = prefs.getProperties("sakai:time");
		String val = props.getProperty("timezone");
		if (val == null) val = TimeZone.getDefault().getID();
		rv.put("timezone", val);
		SessionManager.getCurrentSession().setAttribute("ck.siteId", site.getId());

		loadHomeContentItems(rv, site, userId, currentOnly);

		try
		{
			// status
			HomePageStatus status = homePageService().getStatus(userId, site.getId());
			Map<String, String> statusMap = new HashMap<String, String>();
			rv.put("status", statusMap);
			statusMap.put("instructorStatus", CdpResponseHelper.formatBoolean(status.isInstructor()));
			statusMap.put("noVisitCount", CdpResponseHelper.formatInt(status.getNoVisitCount()));
			statusMap.put("postCount", CdpResponseHelper.formatInt((status.getPostCount())));
			statusMap.put("pmCount", CdpResponseHelper.formatInt(status.getPmCount()));
			statusMap.put("reviewCountMneme", CdpResponseHelper.formatInt(status.getReviewCountMneme()));
			statusMap.put("reviewCountJForum", CdpResponseHelper.formatInt(status.getReviewCountJForum()));

			// the tool placement ids for some tools we need to know about (mneme, jforum, activitymeter, chat, announcements)
			Map<String, String> toolsMap = new HashMap<String, String>();
			rv.put("tools", toolsMap);
			ToolConfiguration toolConfig = site.getToolForCommonId("sakai.mneme");
			if (toolConfig != null) toolsMap.put("mneme", toolConfig.getId());
			toolConfig = site.getToolForCommonId("sakai.jforum.tool");
			if (toolConfig != null) toolsMap.put("jforum", toolConfig.getId());
			toolConfig = site.getToolForCommonId("sakai.activitymeter");
			if (toolConfig != null) toolsMap.put("activitymeter", toolConfig.getId());
			toolConfig = site.getToolForCommonId("sakai.chat");
			if (toolConfig != null) toolsMap.put("chat", toolConfig.getId());
			toolConfig = site.getToolForCommonId("sakai.announcements");
			if (toolConfig != null) toolsMap.put("announcement", toolConfig.getId());

			HomePageOptions options = homePageService().getOptions(userId, site.getId());

			Map<String, String> anncMap = new HashMap<String, String>();
			rv.put("announcementsOptions", anncMap);
			anncMap.put("display", CdpResponseHelper.formatBoolean(options.getAnnouncementsEnabled()));
			anncMap.put("count", CdpResponseHelper.formatInt(options.getNumAnnouncements()));
			int numAnnouncements = options.getNumAnnouncements().intValue();

			Map<String, String> scheduleMap = new HashMap<String, String>();
			rv.put("scheduleOptions", scheduleMap);
			scheduleMap.put("display", CdpResponseHelper.formatBoolean(options.getScheduleEnabled()));

			Map<String, String> chatMap = new HashMap<String, String>();
			rv.put("chatOptions", chatMap);
			chatMap.put("display", CdpResponseHelper.formatBoolean(options.getChatEnabled()));

			if (options.getModifiedDate() != null)
			{
				rv.put("optionsModifiedDate", CdpResponseHelper.dateTimeDisplayInUserZone(options.getModifiedDate().getTime()));
				rv.put("optionsModifiedUser", displayName(options.getModifiedUser()));
			}

			rv.put("format", options.getFormat());
			rv.put("anncFull", options.getAnncFull());
			rv.put("order", options.getOrder());
			rv.put("mayConfigure", CdpResponseHelper.formatBoolean(securityService().unlock(userId, "site.upd", site.getReference())));

			List<Map<String, Object>> announcements = new ArrayList<Map<String, Object>>();
			rv.put("announcements", announcements);
			if (!site.getTools(announcementTool).isEmpty())
			{
				String channelId = announcementService().channelReference(site.getId(), SiteService.MAIN_CONTAINER);
				try
				{
					List<AnnouncementMessage> messages = announcementService().getRecentMessages(channelId, numAnnouncements);
					for (AnnouncementMessage msg : messages)
					{
						Map<String, Object> announcement = new HashMap<String, Object>();
						announcements.add(announcement);
						loadAnnouncement(msg, announcement);
					}
				}
				catch (PermissionException e)
				{
					// not permitted to the announcements - Evaluators are not. Ignore
				}
			}

			if (options.getScheduleEnabled())
			{
				TimeRange dayRange = CdpResponseHelper.dayInUserZone(year, month, day);
				rv.put("eventsDate", CdpResponseHelper.dateDisplayInUserZone(dayRange.firstTime().getTime()));
				rv.put("eventsDateZone", CdpResponseHelper.zoneDisplayInUserZone(dayRange.firstTime().getTime()));

				doEvenstDays(rv, year, month, site, userId);
				doEvents(rv, year, month, day, site, userId);
			}

			if (options.getChatEnabled())
			{
				doChat(rv, site);
			}
		}
		catch (PermissionException e)
		{
			M_log.warn("loadHomeContent:" + e);
		}
	}

	protected void loadHomeContentItems(Map<String, Object> rv, Site site, String userId, boolean currentOnly)
	{
		// get the site's home content
		HomeContent content = homePageService().getContent(site.getId());

		List<Map<String, Object>> current = new ArrayList<Map<String, Object>>();
		rv.put("current", current);
		boolean foundCurrent = false;
		for (HomeContentItem c : content.getCurrent())
		{
			Map<String, Object> contentMap = new HashMap<String, Object>();
			current.add(contentMap);
			contentMap.put("title", c.getTitle());
			contentMap.put("contentId", CdpResponseHelper.formatLong(c.getId()));
			contentMap.put("source", c.getSource());
			contentMap.put("type", c.getType());
			contentMap.put("style", c.getStyle());
			contentMap.put("url", c.getUrl());
			if (c.getReleaseDate() != null) contentMap.put("releaseDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getReleaseDate().getTime()));

			// for authored content, unless we want just the current item, also
			// send the content body
			if (("A".equals(c.getSource())) && !currentOnly)
			{
				contentMap.put("content", c.getContent());
			}
			contentMap.put("modifiedDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getModifiedDate().getTime()));
			contentMap.put("modifiedUser", displayName(c.getModifiedUser()));
			if (c.getAltText() != null) contentMap.put("alt", c.getAltText());

			foundCurrent = true;
		}

		// if we want only the current item, we are (almost) done
		if (currentOnly)
		{
			// code to indicate why no item status: 0 - there is a current item, 1- there is a pending item (title and releasedDate) 2- there are no published items, 3- there are no items defined
			if (foundCurrent)
			{
				rv.put("itemShowing", "0");
			}
			else if (!content.getPending().isEmpty())
			{
				HomeContentItem c = content.getPending().get(0);
				rv.put("itemShowing", "1");
				rv.put("itemPendingTitle", c.getTitle());
				rv.put("itemPendingReleaseDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getReleaseDate().getTime()));
			}
			else
			{
				// no current, no pending - are there any items?
				if (content.getItems().isEmpty())
				{
					rv.put("itemShowing", "3");
				}

				else
				{
					// there are items, but no current (which also means no past) or pending, must all be draft.
					rv.put("itemShowing", "2");
				}
			}

			return;
		}

		List<Map<String, Object>> pending = new ArrayList<Map<String, Object>>();
		rv.put("pending", pending);
		for (HomeContentItem c : content.getPending())
		{
			Map<String, Object> contentMap = new HashMap<String, Object>();
			pending.add(contentMap);
			contentMap.put("title", c.getTitle());
			contentMap.put("contentId", CdpResponseHelper.formatLong(c.getId()));
			contentMap.put("source", c.getSource());
			contentMap.put("type", c.getType());
			contentMap.put("style", c.getStyle());
			contentMap.put("url", c.getUrl());
			if (c.getReleaseDate() != null) contentMap.put("releaseDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getReleaseDate().getTime()));
			contentMap.put("content", c.getContent());
			contentMap.put("modifiedDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getModifiedDate().getTime()));
			contentMap.put("modifiedUser", displayName(c.getModifiedUser()));
			if (c.getAltText() != null) contentMap.put("alt", c.getAltText());
		}

		List<Map<String, Object>> drafts = new ArrayList<Map<String, Object>>();
		rv.put("drafts", drafts);
		for (HomeContentItem c : content.getDrafts())
		{
			Map<String, Object> contentMap = new HashMap<String, Object>();
			drafts.add(contentMap);
			contentMap.put("title", c.getTitle());
			contentMap.put("contentId", CdpResponseHelper.formatLong(c.getId()));
			contentMap.put("source", c.getSource());
			contentMap.put("type", c.getType());
			contentMap.put("style", c.getStyle());
			contentMap.put("url", c.getUrl());
			if (c.getReleaseDate() != null) contentMap.put("releaseDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getReleaseDate().getTime()));
			contentMap.put("content", c.getContent());
			contentMap.put("modifiedDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getModifiedDate().getTime()));
			contentMap.put("modifiedUser", displayName(c.getModifiedUser()));
			if (c.getAltText() != null) contentMap.put("alt", c.getAltText());
		}

		List<Map<String, Object>> past = new ArrayList<Map<String, Object>>();
		rv.put("past", past);
		for (HomeContentItem c : content.getPast())
		{
			Map<String, Object> contentMap = new HashMap<String, Object>();
			past.add(contentMap);
			contentMap.put("title", c.getTitle());
			contentMap.put("contentId", CdpResponseHelper.formatLong(c.getId()));
			contentMap.put("source", c.getSource());
			contentMap.put("type", c.getType());
			contentMap.put("style", c.getStyle());
			contentMap.put("url", c.getUrl());
			if (c.getReleaseDate() != null) contentMap.put("releaseDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getReleaseDate().getTime()));
			contentMap.put("content", c.getContent());
			contentMap.put("modifiedDate", CdpResponseHelper.dateTimeDisplayInUserZone(c.getModifiedDate().getTime()));
			contentMap.put("modifiedUser", displayName(c.getModifiedUser()));
			if (c.getAltText() != null) contentMap.put("alt", c.getAltText());
		}
	}

	/**
	 * @return The AnnouncementService, via the component manager.
	 */
	private AnnouncementService announcementService()
	{
		return (AnnouncementService) ComponentManager.get(AnnouncementService.class);
	}

	/**
	 * @return The CalendarService, via the component manager.
	 */
	private CalendarService calendarService()
	{
		return (CalendarService) ComponentManager.get(CalendarService.class);
	}

	/**
	 * @return The ChatService, via the component manager.
	 */
	private ChatService chatService()
	{
		return (ChatService) ComponentManager.get(ChatService.class);
	}

	/**
	 * @return The CourseMapService, via the component manager.
	 */
	private CourseMapService courseMapService()
	{
		return (CourseMapService) ComponentManager.get(CourseMapService.class);
	}

	/**
	 * @return The HomePageService, via the component manager.
	 */
	private HomePageService homePageService()
	{
		return (HomePageService) ComponentManager.get(HomePageService.class);
	}

	/**
	 * @return The AuthenticationManager, via the component manager.
	 */
	private PreferencesService preferencesService()
	{
		return (PreferencesService) ComponentManager.get(PreferencesService.class);
	}

	/**
	 * @return The SecurityService, via the component manager.
	 */
	private SecurityService securityService()
	{
		return (SecurityService) ComponentManager.get(SecurityService.class);
	}

	/**
	 * @return The SiteService, via the component manager.
	 */
	private SiteService siteService()
	{
		return (SiteService) ComponentManager.get(SiteService.class);
	}

	/**
	 * @return The TimeService, via the component manager.
	 */
	private TimeService timeService()
	{
		return (TimeService) ComponentManager.get(TimeService.class);
	}

	/**
	 * @return The UserDirectoryService, via the component manager.
	 */
	private UserDirectoryService userDirectoryService()
	{
		return (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
	}
}
