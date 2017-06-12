/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-webapp/webapp/src/java/org/etudes/homepage/cdp/HomePageServiceImpl.java $
 * $Id: HomePageServiceImpl.java 9697 2014-12-27 00:32:01Z ggolden $
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

package org.etudes.homepage.cdp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.api.app.jforum.JForumCategoryService;
import org.etudes.api.app.jforum.JForumGradeService;
import org.etudes.api.app.jforum.JForumPrivateMessageService;
import org.etudes.api.app.jforum.PrivateMessage;
import org.etudes.homepage.api.HomeContent;
import org.etudes.homepage.api.HomeContentItem;
import org.etudes.homepage.api.HomePageOptions;
import org.etudes.homepage.api.HomePageService;
import org.etudes.homepage.api.HomePageStatus;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.etudes.siteimport.api.SiteImportService;
import org.etudes.siteimport.api.SiteImporter;
import org.etudes.siteresources.api.HarvestReference;
import org.etudes.siteresources.api.SitePlacement;
import org.etudes.siteresources.api.SiteResourceOwner;
import org.etudes.siteresources.api.SiteResourcesService;
import org.etudes.siteresources.api.ToolReference;
import org.etudes.util.Different;
import org.etudes.util.HtmlHelper;
import org.etudes.util.XrefHelper;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.DateManagerService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.StringUtil;

/**
 * HomepageServiceImpl ...
 */
public class HomePageServiceImpl implements HomePageService, SiteImporter, SiteResourceOwner
{
	protected class HomeContentImpl implements HomeContent
	{
		protected List<HomeContentItem> current = new ArrayList<HomeContentItem>();
		protected List<HomeContentItem> drafts = new ArrayList<HomeContentItem>();
		protected List<HomeContentItem> past = new ArrayList<HomeContentItem>();
		protected List<HomeContentItem> pending = new ArrayList<HomeContentItem>();

		public List<HomeContentItem> getCurrent()
		{
			return this.current;
		}

		public List<HomeContentItem> getDrafts()
		{
			return this.drafts;
		}

		public List<HomeContentItem> getItems()
		{
			List<HomeContentItem> allItems = new ArrayList<HomeContentItem>();
			allItems.addAll(this.current);
			allItems.addAll(this.drafts);
			allItems.addAll(this.past);
			allItems.addAll(this.pending);
			return allItems;
		}

		public List<HomeContentItem> getPast()
		{
			return this.past;
		}

		public List<HomeContentItem> getPending()
		{
			return this.pending;
		}
	}

	protected class HomeContentItemImpl implements HomeContentItem
	{
		protected String alt = null;
		protected boolean changed = false;
		protected Long id = null;
		protected Date modifiedDate = null;
		protected String modifiedUser = null;
		protected String newContent = null;
		protected String origSource = null;
		protected String origUrl = null;
		protected Boolean published = null;
		protected Date releaseDate = null;
		protected String siteId = null;
		protected String source = null;
		protected String style = null;
		protected String title = null;
		protected String type = null;
		protected String url = null;

		public HomeContentItemImpl(Long contentId, String siteId)
		{
			this.id = contentId;
			this.siteId = siteId;
			this.changed = true;
		}

		public String getAltText()
		{
			return this.alt;
		}

		public Boolean getChanged()
		{
			return Boolean.valueOf(this.changed);
		}

		public String getContent()
		{
			String rv = "";

			if ("A".equals(this.source) && (this.url != null))
			{
				// find the site resource site placement based on the name in our url
				ToolReference ref = siteResourcesService().parseToolReferenceUrl(this.url);

				if ((ref != null) && (ref.getId() != null))
				{
					// get the site placement
					SitePlacement placement = siteResourcesService().getSitePlacement(ref.getSiteId(), ref.getName());
					if (placement != null)
					{
						rv = placement.getSiteResource().getString();
					}
					else
					{
						// this should not happen
						M_log.warn("HomeContentItemImpl.getContent() - null placement from url for A");
					}
				}
				else
				{
					// this should not happen
					M_log.warn("HomeContentItemImpl.getContent() - null or null id ref from url for A");
				}
			}

			return rv;
		}

		public Long getId()
		{
			return this.id;
		}

		public Date getModifiedDate()
		{
			return this.modifiedDate;
		}

		public String getModifiedUser()
		{
			return this.modifiedUser;
		}

		public String getNewContent()
		{
			return this.newContent;
		}

		public Boolean getPublished()
		{
			return this.published;
		}

		public Date getReleaseDate()
		{
			return this.releaseDate;
		}

		public String getSiteId()
		{
			return this.siteId;
		}

		public String getSource()
		{
			return this.source;
		}

		public String getStyle()
		{
			return this.style;
		}

		public String getTitle()
		{
			return this.title;
		}

		public String getType()
		{
			return this.type;
		}

		public String getUrl()
		{
			return this.url;
		}

		public void setAltText(String alt)
		{
			if (Different.different(alt, this.alt))
			{
				this.changed = true;
				this.alt = alt;
			}
		}

		public void setId(Long id)
		{
			if (Different.different(id, this.id))
			{
				this.changed = true;
				this.id = id;
			}
		}

		public void setModifiedDate(Date date)
		{
			if (Different.different(date, this.modifiedDate))
			{
				this.changed = true;
				this.modifiedDate = date;
			}
		}

		public void setModifiedUser(String userId)
		{
			if (Different.different(userId, this.modifiedUser))
			{
				this.changed = true;
				this.modifiedUser = userId;
			}
		}

		public void setNewContent(String content)
		{
			// no new content set yet, so compare against the actual content
			if (this.newContent == null)
			{
				if (Different.different(content, this.getContent()))
				{
					this.changed = true;
					this.newContent = content;
				}
			}
			// else compare to new content
			else
			{
				if (Different.different(content, this.newContent))
				{
					this.changed = true;
					this.newContent = content;
				}
			}
		}

		public void setPublished(Boolean published)
		{
			if (Different.different(published, this.published))
			{
				this.changed = true;
				this.published = published;
			}
		}

		public void setReleaseDate(Date releaseDate)
		{
			if (Different.different(releaseDate, this.releaseDate))
			{
				this.changed = true;
				this.releaseDate = releaseDate;
			}
		}

		public void setSource(String source)
		{
			if (Different.different(source, this.source))
			{
				this.changed = true;
				this.source = source;
			}
		}

		public void setStyle(String style)
		{
			if (Different.different(style, this.style))
			{
				this.changed = true;
				this.style = style;
			}
		}

		public void setTitle(String title)
		{
			if (Different.different(title, this.title))
			{
				this.changed = true;
				this.title = title;
			}
		}

		public void setType(String type)
		{
			if (Different.different(type, this.type))
			{
				this.changed = true;
				this.type = type;
			}
		}

		public void setUrl(String url)
		{
			if (Different.different(url, this.url))
			{
				this.changed = true;
				this.url = url;
			}
		}

		public void transferUnpublishedFrom(HomeContentItem other)
		{
			setAltText(other.getAltText());
			setPublished(Boolean.FALSE);
			setReleaseDate(other.getReleaseDate());
			setSource(other.getSource());
			setStyle(other.getStyle());
			setTitle(other.getTitle());
			setType(other.getType());
			if (this.source.equals("A"))
			{
				setNewContent(other.getContent());
			}
			else
			{
				setUrl(other.getUrl());
			}
		}

		protected void clearChanged()
		{
			this.changed = false;
		}

		protected void initSource(String source)
		{
			this.source = source;
			this.origSource = source;
		}

		protected void initUrl(String url)
		{
			this.url = url;
			this.origUrl = url;
		}
	}

	protected class HomePageOptionsImpl implements HomePageOptions
	{
		protected Boolean anncEnabled = Boolean.TRUE;
		protected Boolean anncFull = null;
		protected boolean changed = false;
		protected Boolean chatEnabled = Boolean.FALSE;
		protected String format = "1";
		protected Long id = null;
		protected Date modifiedDate = null;
		protected String modifiedUser = null;
		protected Integer numAnnouncements = Integer.valueOf(5);
		protected String order = "ASC";
		protected Boolean scheduleEnabled = Boolean.TRUE;
		protected String siteId = null;

		public Boolean getAnncFull()
		{
			// for old values, this was tied to format=1
			if (this.anncFull == null)
			{
				return "1".equals(this.format);
			}

			return this.anncFull;
		}

		public Boolean getAnnouncementsEnabled()
		{
			return this.anncEnabled;
		}

		public Boolean getChanged()
		{
			return Boolean.valueOf(this.changed);
		}

		public Boolean getChatEnabled()
		{
			return this.chatEnabled;
		}

		public String getFormat()
		{
			return this.format;
		}

		public Long getId()
		{
			return this.id;
		}

		public Date getModifiedDate()
		{
			return this.modifiedDate;
		}

		public String getModifiedUser()
		{
			return this.modifiedUser;
		}

		public Integer getNumAnnouncements()
		{
			return this.numAnnouncements;
		}

		public String getOrder()
		{
			return this.order;
		}

		public Boolean getScheduleEnabled()
		{
			return this.scheduleEnabled;
		}

		public String getSiteId()
		{
			return this.siteId;
		}

		public void setAnncFull(Boolean setting)
		{
			if (Different.different(setting, this.anncFull))
			{
				this.changed = true;
				this.anncFull = setting;
			}
		}

		public void setAnnouncementsEnabled(Boolean setting)
		{
			if (Different.different(setting, this.anncEnabled))
			{
				this.changed = true;
				this.anncEnabled = setting;
			}
		}

		public void setChatEnabled(Boolean setting)
		{
			if (Different.different(setting, this.chatEnabled))
			{
				this.changed = true;
				this.chatEnabled = setting;
			}
		}

		public void setFormat(String format)
		{
			if (Different.different(format, this.format))
			{
				this.changed = true;
				this.format = format;
			}
		}

		public void setModifiedDate(Date date)
		{
			if (Different.different(date, this.modifiedDate))
			{
				this.changed = true;
				this.modifiedDate = date;
			}
		}

		public void setModifiedUser(String userId)
		{
			if (Different.different(userId, this.modifiedUser))
			{
				this.changed = true;
				this.modifiedUser = userId;
			}
		}

		public void setNumAnnouncements(Integer setting)
		{
			if (setting == null) return;
			if (Different.different(setting, this.numAnnouncements))
			{
				this.changed = true;
				this.numAnnouncements = setting;
			}
		}

		public void setOrder(String order)
		{
			if (Different.different(order, this.order))
			{
				this.changed = true;
				this.order = order;
			}
		}

		public void setScheduleEnabled(Boolean setting)
		{
			if (Different.different(setting, this.scheduleEnabled))
			{
				this.changed = true;
				this.scheduleEnabled = setting;
			}
		}

		public void setSiteId(String id)
		{
			if (Different.different(id, this.siteId))
			{
				this.changed = true;
				this.siteId = id;
			}
		}

		public void transferFrom(HomePageOptions other)
		{
			setAnnouncementsEnabled(other.getAnnouncementsEnabled());
			setChatEnabled(other.getChatEnabled());
			setFormat(other.getFormat());
			setOrder(other.getOrder());
			setScheduleEnabled(other.getScheduleEnabled());
			setModifiedUser(other.getModifiedUser());
			setModifiedDate(other.getModifiedDate());
			setNumAnnouncements(other.getNumAnnouncements());
		}

		protected void clearChanged()
		{
			this.changed = false;
		}

		protected void setId(Long id)
		{
			if (Different.different(id, this.id))
			{
				this.changed = true;
				this.id = id;
			}
		}
	}

	protected class HomePageStatusImpl implements HomePageStatus
	{
		protected boolean isInstructor;
		protected int noVisitCount = 0;
		protected int pmCount = 0;
		protected int postCount = 0;
		protected int reviewCountJForum = 0;
		protected int reviewCountMneme = 0;

		public HomePageStatusImpl(boolean isInstructor, int noVisitCount, int pmCount, int postCount, int reviewCountMneme, int reviewCountJForum)
		{
			this.isInstructor = isInstructor;
			this.noVisitCount = noVisitCount;
			this.pmCount = pmCount;
			this.postCount = postCount;
			this.reviewCountMneme = reviewCountMneme;
			this.reviewCountJForum = reviewCountJForum;
		}

		public Integer getNoVisitCount()
		{
			return Integer.valueOf(this.noVisitCount);
		}

		public Integer getPmCount()
		{
			return Integer.valueOf(this.pmCount);
		}

		public Integer getPostCount()
		{
			return Integer.valueOf(this.postCount);
		}

		public Integer getReviewCountJForum()
		{
			return Integer.valueOf(this.reviewCountJForum);
		}

		public Integer getReviewCountMneme()
		{
			return Integer.valueOf(this.reviewCountMneme);
		}

		public Boolean isInstructor()
		{
			return Boolean.valueOf(this.isInstructor);
		}
	}

	/** Our log. */
	private static Log M_log = LogFactory.getLog(HomePageServiceImpl.class);

	protected boolean autoDdl = false;

	/**
	 * Construct
	 */
	public HomePageServiceImpl()
	{
		final HomePageServiceImpl service = this;

		ComponentManager.whenAvailable(ServerConfigurationService.class, new Runnable()
		{
			public void run()
			{
				ComponentManager.whenAvailable(SqlService.class, new Runnable()
				{
					public void run()
					{
						ComponentManager.whenAvailable(SiteImportService.class, new Runnable()
						{
							public void run()
							{
								ComponentManager.whenAvailable(SiteResourcesService.class, new Runnable()
								{
									public void run()
									{
										String str = serverConfigurationService().getString("auto.ddl");
										boolean auto = autoDdl;
										if (str != null) auto = Boolean.valueOf(str).booleanValue();

										// if we are auto-creating our schema, check and create
										if (auto)
										{
											M_log.warn("running ddl");
											sqlService().ddl(this.getClass().getClassLoader(), "homecontent");
										}

										// register for site import
										siteImportService().registerImporter(service);

										// register for site resource ownership
										siteResourcesService().registerOwner(service);
									}
								});
							}
						});
					}
				});
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowAccess(String userId, String siteId)
	{
		// security check - must have site.visit
		return securityService().unlock(userId, "site.visit", siteService().siteReference(siteId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowUpdate(String userId, String siteId)
	{
		// security check - must have site.upd
		return securityService().unlock(userId, "site.upd", siteService().siteReference(siteId));
	}

	/**
	 * {@inheritDoc}
	 */
	public void applyBaseDateTx(String siteId, int days)
	{
		// TODO: this really needs security!

		HomeContent content = getContent(siteId);
		for (HomeContentItem item : content.getItems())
		{
			if (item.getReleaseDate() != null)
			{
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(item.getReleaseDate());
				gc.add(Calendar.DATE, days);
				item.setReleaseDate(gc.getTime());

				doSave(item);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void convertFromSiteInfo(String userId, String siteId, boolean asImport)
	{
		try
		{
			Site site = siteService().getSite(siteId);
			if ((site.getInfoUrl() != null) || (site.getDescription() != null))
			{
				HomeContent content = getContent(site.getId());
				if (content.getItems().isEmpty())
				{
					List<HomeContentItem> newItems = contentItemsFromSiteInfo(site, site.getId(), asImport);
					for (HomeContentItem item : newItems)
					{
						try
						{
							saveContentItem(userId, item);
						}
						catch (PermissionException e)
						{
							M_log.warn("convertFromSiteInfo - saving site: " + site.getId() + " : " + e);
						}
					}

					// clear the site
					site.setDescription(null);
					site.setShortDescription(null);
					site.setInfoUrl(null);

					// save the site
					try
					{
						this.siteService().save(site);
					}
					catch (IdUnusedException e)
					{
						M_log.warn("convertFromSiteInfo - saving site: " + site.getId() + " : " + e);
					}
					catch (PermissionException e)
					{
						M_log.warn("convertFromSiteInfo - saving site: " + site.getId() + " : " + e);
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			M_log.warn("convertFromSiteInfo - missing site: " + siteId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public HomeContent getContent(final String siteId)
	{
		HomeContentImpl rv = new HomeContentImpl();
		final List<HomeContentItem> items = new ArrayList<HomeContentItem>();

		String sql = "SELECT ID, PUBLISHED, RELEASE_DATE, SOURCE, TITLE, MIME_TYPE, URL, SITE_ID, STYLE, MODIFIED_DATE, MODIFIED_USER, ALT FROM HOME_CONTENT WHERE SITE_ID = ? ORDER BY PUBLISHED DESC, RELEASE_DATE ASC, TITLE ASC";
		Object[] fields = new Object[1];
		fields[0] = siteId;

		sqlService().dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					HomeContentItemImpl item = new HomeContentItemImpl(sqlService().readLong(result, 1), sqlService().readString(result, 8));
					items.add(item);
					item.setId(sqlService().readLong(result, 1));
					item.setPublished(sqlService().readBoolean(result, 2));
					item.setReleaseDate(sqlService().readDate(result, 3));
					item.initSource(sqlService().readString(result, 4));
					item.setTitle(sqlService().readString(result, 5));
					item.setType(sqlService().readString(result, 6));
					item.initUrl(sqlService().readString(result, 7));
					item.setStyle(sqlService().readString(result, 9));
					item.setModifiedDate(sqlService().readDate(result, 10));
					item.setModifiedUser(sqlService().readString(result, 11));
					item.setAltText(sqlService().readString(result, 12));
					item.clearChanged();

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getContent: " + e);
					return null;
				}
			}
		});

		// find the published item with a release date set to now or before, that is at the end of the list, or followed by one with a release date > now
		long now = System.currentTimeMillis();
		HomeContentItem current = null;
		for (int i = 0; i < items.size(); i++)
		{
			HomeContentItem item = items.get(i);
			if (item.getPublished())
			{
				if (item.getReleaseDate().getTime() <= now)
				{
					if ((i == items.size() - 1) || (!items.get(i + 1).getPublished()) || (items.get(i + 1).getReleaseDate().getTime() > now))
					{
						current = item;
						break;
					}
				}
			}
		}

		// sort into the separate lists
		if (current != null)
		{
			rv.getCurrent().add(current);
		}

		for (HomeContentItem item : items)
		{
			// we already handled the current
			if (item == current) continue;

			if (item.getPublished())
			{
				// prev is past or pending
				if (item.getReleaseDate().getTime() <= now)
				{
					rv.getPast().add(item);
				}
				else
				{
					rv.getPending().add(item);
				}
			}
			else
			{
				rv.getDrafts().add(item);
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public HomeContentItem getContentItem(String siteId, final Long contentId)
	{
		final List<HomeContentItem> items = new ArrayList<HomeContentItem>();

		String sql = "SELECT ID, PUBLISHED, RELEASE_DATE, SOURCE, TITLE, MIME_TYPE, URL, SITE_ID, STYLE, MODIFIED_DATE, MODIFIED_USER, ALT FROM HOME_CONTENT WHERE ID = ?";
		Object[] fields = new Object[1];
		fields[0] = contentId;

		sqlService().dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					HomeContentItemImpl item = new HomeContentItemImpl(sqlService().readLong(result, 1), sqlService().readString(result, 8));
					items.add(item);
					item.setPublished(sqlService().readBoolean(result, 2));
					item.setReleaseDate(sqlService().readDate(result, 3));
					item.initSource(sqlService().readString(result, 4));
					item.setTitle(sqlService().readString(result, 5));
					item.setType(sqlService().readString(result, 6));
					item.initUrl(sqlService().readString(result, 7));
					item.setStyle(sqlService().readString(result, 9));
					item.setModifiedDate(sqlService().readDate(result, 10));
					item.setModifiedUser(sqlService().readString(result, 11));
					item.setAltText(sqlService().readString(result, 12));
					item.clearChanged();

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getContentItem: " + e);
					return null;
				}
			}
		});

		if (items.size() == 0) return null;
		return items.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getMaxStartDate(String siteId)
	{
		Date rv = null;

		HomeContent content = getContent(siteId);
		for (HomeContentItem item : content.getItems())
		{
			if (item.getReleaseDate() != null)
			{
				if ((rv == null) || (item.getReleaseDate().after(rv)))
				{
					rv = item.getReleaseDate();
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getMinStartDate(String siteId)
	{
		Date rv = null;

		HomeContent content = getContent(siteId);
		for (HomeContentItem item : content.getItems())
		{
			if (item.getReleaseDate() != null)
			{
				if ((rv == null) || (item.getReleaseDate().before(rv)))
				{
					rv = item.getReleaseDate();
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public HomePageOptions getOptions(String userId, String siteId) throws PermissionException
	{
		if (!allowAccess(userId, siteId)) throw new PermissionException(userId, "access", siteId);

		final HomePageOptionsImpl rv = new HomePageOptionsImpl();
		rv.setSiteId(siteId);

		String sql = "SELECT ID, SITE_ID, ANNC_DISPLAY, SCHD_DISPLAY, CHAT_DISPLAY, FORMAT, DISPLAY_ORDER, MODIFIED_DATE, MODIFIED_USER, ANNC_COUNT, ANNC_FULL FROM HOME_OPTIONS WHERE SITE_ID = ?";
		Object[] fields = new Object[1];
		fields[0] = siteId;

		sqlService().dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					rv.setId(sqlService().readLong(result, 1));
					rv.setSiteId(sqlService().readString(result, 2));
					rv.setAnnouncementsEnabled(sqlService().readBoolean(result, 3));
					rv.setScheduleEnabled(sqlService().readBoolean(result, 4));
					rv.setChatEnabled(sqlService().readBoolean(result, 5));
					rv.setFormat(sqlService().readString(result, 6));
					rv.setOrder(sqlService().readString(result, 7));
					rv.setModifiedDate(sqlService().readDate(result, 8));
					rv.setModifiedUser(sqlService().readString(result, 9));
					rv.setNumAnnouncements(sqlService().readInteger(result, 10));
					rv.setAnncFull(sqlService().readBoolean(result, 11));

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getOptions: " + e);
					return null;
				}
			}
		});

		rv.clearChanged();
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public HomePageStatus getStatus(String userId, String siteId) throws PermissionException
	{
		boolean instructor = allowUpdate(userId, siteId);

		// alert users (only for instructors)
		int notVisitedCount = 0;
		if (instructor)
		{
			notVisitedCount = getSiteStudentNotVisitedInPeriod(siteId, 7);
		}

		// unread posts
		int unreadPosts = jForumCategoryService().getUserUnreadTopicsCount(siteId, userId);

		// unread messages
		int unreadCount = 0;
		List<PrivateMessage> msgs = jForumPrivateMessageService().inbox(siteId, userId);
		for (PrivateMessage m : msgs)
		{
			if (m.getType() == PrivateMessage.TYPE_NEW) unreadCount++;
		}

		int reviewCountMneme = 0;
		List<Submission> submissions = submissionService().getUserContextSubmissions(siteId, null, null);
		for (Submission s : submissions)
		{
			if (s.getEvaluationNotReviewed()) reviewCountMneme++;
		}

		int reviewCountJForum = jForumGradeService().getUserNotReviewedGradeEvaluationsCount(siteId, userId);

		HomePageStatus rv = new HomePageStatusImpl(instructor, notVisitedCount, unreadCount, unreadPosts, reviewCountMneme, reviewCountJForum);
		return rv;
	}

	public String getTool()
	{
		return "home";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getToolId()
	{
		return "e3.homepage";
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Boolean hasOptions(String userId, String siteId) throws PermissionException
	{
		if (!allowAccess(userId, siteId)) throw new PermissionException(userId, "access", siteId);

		String sql = "SELECT COUNT(ID) FROM HOME_OPTIONS WHERE SITE_ID = ?";
		Object[] fields = new Object[1];
		fields[0] = siteId;

		List<Boolean> rv = (List<Boolean>) sqlService().dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					Long count = sqlService().readLong(result, 1);

					return Boolean.valueOf(count != 0);
				}
				catch (SQLException e)
				{
					M_log.warn("hasOptions: " + e);
					return null;
				}
			}
		});

		if (rv.isEmpty()) return Boolean.FALSE;
		return rv.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void importFromSite(String userId, String fromSite, String toSite)
	{
		try
		{
			// get all the items from the fromSite
			HomeContent from = getContent(fromSite);
			for (HomeContentItem item : from.getItems())
			{
				HomeContentItem newItem = newContentItem(userId, toSite);
				newItem.transferUnpublishedFrom(item);
				saveContentItemIfNotDuplicate(userId, newItem);
			}

			// special handling for sites with the old home setup
			if (from.getItems().isEmpty())
			{
				try
				{
					Site fromSiteSite = siteService().getSite(fromSite);
					List<HomeContentItem> items = contentItemsFromSiteInfo(fromSiteSite, toSite, true);
					for (HomeContentItem item : items)
					{
						// save if we don't already have this
						saveContentItemIfNotDuplicate(userId, item);
					}
				}
				catch (IdUnusedException e)
				{
				}
			}

			// if the destination does not have home options set, and the sources does
			if (hasOptions(userId, fromSite))
			{
				if (!hasOptions(userId, toSite))
				{
					// get the options from the fromSite
					HomePageOptions fromOptions = getOptions(userId, fromSite);

					// to the toSite
					HomePageOptions options = getOptions(userId, toSite);
					options.transferFrom(fromOptions);
					saveOptions(userId, options);
				}
			}
		}
		catch (PermissionException e)
		{
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void informDeletedResource(ToolReference reference)
	{
		// M_log.warn("deleted resource: " + reference.getSiteId() + " " + reference.getTool() + " " + reference.getToolId() + " " + reference.getName());

		// ignore changes to our own content item bodies
		if (reference.getName().startsWith(".home/")) return;

		// get the item
		HomeContentItem item = getContentItem(reference.getSiteId(), reference.getToolId());
		if (item == null)
		{
			M_log.warn("informDeletedResource: missing home content item: " + reference.getToolId() + " in site: " + reference.getSiteId());
			return;
		}

		// re-compute the references
		computeReferences(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public void informNewResource(ToolReference reference, SitePlacement placement)
	{
		// M_log.warn("new resource: " + reference.getSiteId() + " " + reference.getTool() + " " + reference.getToolId() + " " + reference.getName()
		// + " placement: " + placement.getSiteId() + " " + placement.getName());

		// ignore changes to our own content item bodies
		if (reference.getName().startsWith(".home/")) return;

		// get the item
		HomeContentItem item = getContentItem(reference.getSiteId(), reference.getToolId());
		if (item == null)
		{
			M_log.warn("informNewResource: missing home content item: " + reference.getToolId() + " in site: " + reference.getSiteId());
			return;
		}

		// re-compute the references
		computeReferences(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public void informRenamedResource(ToolReference reference, SitePlacement placement)
	{
		// M_log.warn("renamed resource: " + reference.getSiteId() + " " + reference.getTool() + " " + reference.getToolId() + " " + reference.getName()
		// + " placement: " + placement.getSiteId() + " " + placement.getName());

		// ignore changes to our own content item bodies
		if (reference.getName().startsWith(".home/")) return;

		// get the item
		HomeContentItem item = getContentItem(reference.getSiteId(), reference.getToolId());
		if (item == null)
		{
			M_log.warn("informRenamedResource: missing home content item: " + reference.getToolId() + " in site: " + reference.getSiteId());
			return;
		}

		// TODO: propagate the new name into the content body

		// re-compute the references
		computeReferences(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public HomeContentItem newContentItem(String userId, String siteId) throws PermissionException
	{
		if (!allowUpdate(userId, siteId)) throw new PermissionException(userId, "update", siteId);

		return new HomeContentItemImpl(null, siteId);
	}

	/**
	 * {@inheritDoc}
	 */
	public void publishContentItem(final String userId, String siteId, final Long itemId) throws PermissionException
	{
		if (!allowUpdate(userId, siteId)) throw new PermissionException(userId, "update", siteId);

		sqlService().transact(new Runnable()
		{
			public void run()
			{
				publishContentItemTx(itemId, userId, new Date());
			}
		}, "publishContentItem: " + itemId);
	}

	/**
	 * {@inheritDoc}
	 */
	public void purge(String userId, final String siteId) throws PermissionException
	{
		if (!allowUpdate(userId, siteId)) throw new PermissionException(userId, "update", siteId);

		// delete options
		sqlService().transact(new Runnable()
		{
			public void run()
			{
				deleteOptionsTx(siteId);
			}
		}, "deleteOptionsTx: " + siteId);

		// fully delete each content item, including the related site resources
		List<Long> ids = readContentItemIds(siteId);
		for (Long itemId : ids)
		{
			removeContentItem(userId, siteId, itemId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeContentItem(String userId, String siteId, final Long itemId) throws PermissionException
	{
		if (!allowUpdate(userId, siteId)) throw new PermissionException(userId, "update", siteId);

		// deal with any site resources used by the item
		HomeContentItem item = getContentItem(siteId, itemId);
		if (item != null)
		{
			// for "A", delete the placement that we own
			if ("A".equals(item.getSource()))
			{
				// find the site resource site placement based on the name in our url
				ToolReference ref = siteResourcesService().parseToolReferenceUrl(item.getUrl());

				if ((ref != null) && (ref.getId() != null))
				{
					// get the site placement
					SitePlacement placement = siteResourcesService().getSitePlacement(ref.getSiteId(), ref.getName());
					if (placement != null)
					{
						// delete it - it will delete the resource when there are no further placements (i.e. now)
						siteResourcesService().removeSitePlacement(placement);
					}
					else
					{
						// this should not happen
						M_log.warn("removeContentItem - null placement from url for A");
					}
				}
				else
				{
					// this should not happen
					M_log.warn("removeContentItem - null or null id ref from url for A");
				}
			}

			// clear all tool references from this item
			siteResourcesService().clearToolReferences(item.getSiteId(), getTool(), item.getId());

			sqlService().transact(new Runnable()
			{
				public void run()
				{
					deleteContentItemTx(itemId);
				}
			}, "removeContentItem: " + itemId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveContentItem(String userId, final HomeContentItem item) throws PermissionException
	{
		if (!allowUpdate(userId, item.getSiteId())) throw new PermissionException(userId, "update", item.getSiteId());

		// if not changed, ignore
		if (!item.getChanged()) return;

		item.setModifiedDate(new Date());
		item.setModifiedUser(userId);

		doSave(item);
	}

	public Boolean saveContentItemIfNotDuplicate(String userId, final HomeContentItem item) throws PermissionException
	{
		if (!allowUpdate(userId, item.getSiteId())) throw new PermissionException(userId, "update", item.getSiteId());

		// if not changed, ignore
		if (!item.getChanged()) return false;

		item.setModifiedDate(new Date());
		item.setModifiedUser(userId);

		// to compare to all existing items in the site... before we save the new item (which then shows up in the site!)
		HomeContent toContent = getContent(item.getSiteId());

		// add if new - item needs an id for the prep
		boolean justAdded = false;
		if (item.getId() == null)
		{
			justAdded = true;
			sqlService().transact(new Runnable()
			{
				public void run()
				{
					addContentItemTx(item);
				}
			}, "saveContentItemIfNotDuplicate: " + item.getSiteId());
		}

		Set<ToolReference> refsToRemove = new HashSet<ToolReference>();
		Set<ToolReference> refsToAdd = new HashSet<ToolReference>();
		Set<HarvestReference> harvest = new HashSet<HarvestReference>();
		prepSaveContentItem(item, refsToRemove, refsToAdd, harvest);

		// compare to all existing items in the site
		for (HomeContentItem i : toContent.getItems())
		{
			// compare item to i - if they are the same, we reject item's save
			// source "A" item has content in newContent...
			// ignore release date and published
			if (Different.different(i.getTitle(), item.getTitle())) continue;
			if (Different.different(i.getSource(), item.getSource())) continue;
			if (Different.different(i.getStyle(), item.getStyle())) continue;
			if (Different.different(i.getType(), item.getType())) continue;
			if ((item.getSource().equals("W") || (item.getSource().equals("Y"))) && Different.different(i.getUrl(), item.getUrl())) continue;

			if (item.getSource().equals("A"))
			{
				// process the bodies so site resource references would look the same (the item id is part of the URL) - ignore >uses<
				Set<ToolReference> refs = new HashSet<ToolReference>();
				String iContent = siteResourcesService().toolLocalizeEmbeddedToolReferences(i.getContent(), item.getSiteId(), getTool(), 0L, refs);
				String itemContent = siteResourcesService().toolLocalizeEmbeddedToolReferences(item.getNewContent(), item.getSiteId(), getTool(), 0L,
						refs);

				if (Different.different(iContent, itemContent)) continue;
			}

			else if (item.getSource().equals("F"))
			{
				Set<ToolReference> uses = new HashSet<ToolReference>();
				String iUrl = siteResourcesService().toolLocalizeEmbeddedToolReferences(i.getUrl(), item.getSiteId(), getTool(), 0L, uses);
				String itemUrl = siteResourcesService().toolLocalizeEmbeddedToolReferences(item.getUrl(), item.getSiteId(), getTool(), 0L, uses);

				if (Different.different(iUrl, itemUrl)) continue;
			}

			// there's no difference!
			if (justAdded)
			{
				// we added the item, now we need to delete it
				sqlService().transact(new Runnable()
				{
					public void run()
					{
						deleteContentItemTx(item.getId());
					}
				}, "saveContentItemIfNotDuplicate: " + item.getId());
			}

			return Boolean.FALSE;
		}

		// continue with the save
		completeSaveContentItem(item, refsToRemove, refsToAdd, harvest);

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOptions(String userId, final HomePageOptions options) throws PermissionException
	{
		if (!allowUpdate(userId, options.getSiteId())) throw new PermissionException(userId, "update", options.getSiteId());

		// if not changed, ignore
		if (!options.getChanged()) return;

		options.setModifiedDate(new Date());
		options.setModifiedUser(userId);

		if (options.getId() == null)
		{
			sqlService().transact(new Runnable()
			{
				public void run()
				{
					addOptionsTx(options);
				}
			}, "saveOptions: " + options.getSiteId());
		}
		else
		{
			sqlService().transact(new Runnable()
			{
				public void run()
				{
					updateOptionsTx(options);
				}
			}, "saveOptions: " + options.getId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void unpublishContentItem(final String userId, String siteId, final Long itemId) throws PermissionException
	{
		if (!allowUpdate(userId, siteId)) throw new PermissionException(userId, "update", siteId);

		sqlService().transact(new Runnable()
		{
			public void run()
			{
				unpublishContentItemTx(itemId, userId, new Date());
			}
		}, "unpublishContentItem: " + itemId);
	}

	protected void addContentItemTx(HomeContentItem content)
	{
		String sql = "INSERT INTO HOME_CONTENT (SITE_ID, PUBLISHED, RELEASE_DATE, SOURCE, TITLE, MIME_TYPE, URL, STYLE, MODIFIED_DATE, MODIFIED_USER, ALT) values (?,?,?,?,?,?,?,?,?,?,?)";

		Object[] fields = new Object[11];
		fields[0] = content.getSiteId();
		fields[1] = sqlService().valueForBoolean(content.getPublished());
		fields[2] = sqlService().valueForDate(content.getReleaseDate());
		fields[3] = content.getSource();
		fields[4] = content.getTitle();
		fields[5] = content.getType();
		fields[6] = content.getUrl();
		fields[7] = content.getStyle();
		fields[8] = sqlService().valueForDate(content.getModifiedDate());
		fields[9] = content.getModifiedUser();
		fields[10] = content.getAltText();

		Long id = sqlService().dbInsert(null, sql, fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("addContentItemTx: dbInsert failed");
		}

		((HomeContentItemImpl) content).setId(id);
	}

	protected void addOptionsTx(HomePageOptions options)
	{
		String sql = "INSERT INTO HOME_OPTIONS (SITE_ID, ANNC_DISPLAY, SCHD_DISPLAY, CHAT_DISPLAY, FORMAT, DISPLAY_ORDER, MODIFIED_DATE, MODIFIED_USER, ANNC_COUNT, ANNC_FULL) values (?,?,?,?,?,?,?,?,?,?)";

		Object[] fields = new Object[10];
		fields[0] = options.getSiteId();
		fields[1] = sqlService().valueForBoolean(options.getAnnouncementsEnabled());
		fields[2] = sqlService().valueForBoolean(options.getScheduleEnabled());
		fields[3] = sqlService().valueForBoolean(options.getChatEnabled());
		fields[4] = options.getFormat();
		fields[5] = options.getOrder();
		fields[6] = sqlService().valueForDate(options.getModifiedDate());
		fields[7] = options.getModifiedUser();
		fields[8] = options.getNumAnnouncements();
		fields[9] = sqlService().valueForBoolean(options.getAnncFull());

		Long id = sqlService().dbInsert(null, sql, fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("addOptionsTx: dbInsert failed");
		}

		((HomePageOptionsImpl) options).setId(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	protected boolean checkSecurity(String userId, String function, String context)
	{
		// check for super user
		if (securityService().isSuperUser(userId)) return true;

		// check for the user / function / context-as-site-authz
		// use the site ref for the security service (used to cache the security calls in the security service)
		String siteRef = siteService().siteReference(context);

		// form the azGroups for a context-as-implemented-by-site
		Collection azGroups = new Vector(2);
		azGroups.add(siteRef);
		azGroups.add("!site.helper");

		boolean rv = securityService().unlock(userId, function, siteRef, azGroups);
		return rv;
	}

	/**
	 * Complete a save.
	 * 
	 * @param item
	 *        The item to save.
	 * @param oldReferences
	 *        ToolReferences to remove.
	 * @param newReferences
	 *        ToolReferences to add.
	 */
	protected void completeSaveContentItem(final HomeContentItem item, Set<ToolReference> refsToRemove, Set<ToolReference> refsToAdd,
			Set<HarvestReference> harvest)
	{
		// if we are replacing our owned site resource, or switching from source "A", we need to delete our old site placement (and the site resource) found in our origUrl

		// if we used to be source "A" and are no longer, or are updating our content, remove the old content by removing the site placement of our site resource.
		if (("A".equals(((HomeContentItemImpl) item).origSource)) && ((!"A".equals(item.getSource())) || (item.getNewContent() != null)))
		{
			// find the site resource site placement based on the name in our url
			ToolReference ref = siteResourcesService().parseToolReferenceUrl(((HomeContentItemImpl) item).origUrl);

			if ((ref != null) && (ref.getId() != null))
			{
				// get the site placement
				SitePlacement placement = siteResourcesService().getSitePlacement(ref.getSiteId(), ref.getName());
				if (placement != null)
				{
					// delete it - it will delete the resource when there are no further placements (i.e. now)
					siteResourcesService().removeSitePlacement(placement);
				}
				else
				{
					// this should not happen
					M_log.warn("completeSaveContentItem - null placement from url for orig A");
				}
			}
			else
			{
				// this should not happen
				M_log.warn("completeSaveContentItem - null or null id ref from url for orig A");
			}
		}

		// update our owned site resource
		if ("A".equals(item.getSource()) && (item.getNewContent() != null))
		{
			try
			{
				// create the site resource, a site placement (unlisted), and a reference
				byte[] bytes = item.getNewContent().getBytes("UTF-8");
				InputStream contents = new ByteArrayInputStream(bytes);
				// unlisted, in a private folder, named with my id to avoid name conflicts in my private folder
				String name = ".home/home_" + item.getId().toString() + ".html";
				siteResourcesService().addSiteResource("text/html", bytes.length, contents, item.getSiteId(), name);
				ToolReference r = siteResourcesService().addToolReference(item.getSiteId(), getTool(), item.getId(), name);

				// record the tool reference access URL
				item.setUrl(r.getAccessUrl());
			}
			catch (UnsupportedEncodingException e)
			{
			}
		}

		// harvest any CHS references
		// if any are unable to be harvested, then the content will have a broken link, the same results as if it had been deleted from site resources.
		siteResourcesService().harvestChsResources(harvest, item.getSiteId(), getTool(), item.getId(), "Home/");

		// collect indirect resource references
		Set<ToolReference> indirectRefs = siteResourcesService().collectIndirectReferences(refsToAdd, item.getSiteId(), getTool(), item.getId(),
				"Home/");
		refsToAdd.addAll(indirectRefs);

		// record the change in tool references
		siteResourcesService().updateToolReferences(refsToRemove, refsToAdd);

		sqlService().transact(new Runnable()
		{
			public void run()
			{
				updateContentItemTx(item);
			}
		}, "completeSaveContentItem: " + item.getId());
	}

	/**
	 * Recompute and update the item's tool references.
	 * 
	 * @param item
	 *        The item.
	 */
	protected void computeReferences(HomeContentItem item)
	{
		if ("F".equals(item.getSource()) || "A".equals(item.getSource()))
		{
			Set<ToolReference> oldReferences = new HashSet<ToolReference>();
			Set<ToolReference> newReferences = new HashSet<ToolReference>();

			// our current references
			oldReferences.addAll(siteResourcesService().getToolReferences(item.getSiteId(), getTool(), item.getId()));

			if ("F".equals(item.getSource()))
			{
				siteResourcesService().toolLocalizeEmbeddedToolReferences(item.getUrl(), item.getSiteId(), getTool(), item.getId(), newReferences);
			}

			else if ("A".equals(item.getSource()))
			{
				siteResourcesService()
						.toolLocalizeEmbeddedToolReferences(item.getContent(), item.getSiteId(), getTool(), item.getId(), newReferences);

				// and our item body reference
				ToolReference ref = siteResourcesService().parseToolReferenceUrl(item.getUrl());
				newReferences.add(ref);
			}

			// the indirect ones
			Set<ToolReference> indirectRefs = siteResourcesService().collectIndirectReferences(newReferences, item.getSiteId(), getTool(),
					item.getId(), "Home/");
			newReferences.addAll(indirectRefs);

			// record the change in tool references
			siteResourcesService().updateToolReferences(oldReferences, newReferences);
		}
	}

	/**
	 * If the site has info or description, create a content item from each. The URL has precedence, so if that exists, make that first and published. Otherwise publish the description item.
	 * 
	 * @param site
	 *        The site to read the info from.
	 * @param forSiteId
	 *        The site id to create items in.
	 * @return The home content item(s) created from the site info: may be empty.
	 */
	protected List<HomeContentItem> contentItemsFromSiteInfo(Site site, String forSiteId, boolean asImport)
	{
		List<HomeContentItem> rv = new ArrayList<HomeContentItem>();

		if (site.getInfoUrl() != null)
		{
			boolean abort = false;
			String url = XrefHelper.shortenFullUrl(site.getInfoUrl());

			// for CHS site reference, create a file and get it harvested
			// otherwise, create a web content item
			HomeContentItem newItem = new HomeContentItemImpl(null, forSiteId);
			if (!asImport)
			{
				// use the base date of the site
				Date baseDate = dateManagerService().getMinStartDate(site.getId());
				if (baseDate == null) baseDate = new Date();

				newItem.setReleaseDate(baseDate);
				newItem.setPublished(Boolean.TRUE);
			}
			else
			{
				newItem.setReleaseDate(null);
				newItem.setPublished(Boolean.FALSE);
			}

			if (harvestableChsUrl(url))
			{
				// setup a security advisor
				securityService().pushAdvisor(new SecurityAdvisor()
				{
					public SecurityAdvice isAllowed(String userId, String function, String reference)
					{
						if (reference.startsWith("/content/private/meleteDocs"))
						{
							String parts[] = StringUtil.split(reference, "/");
							if (parts.length >= 5)
							{
								String siteId = parts[4];
								if (checkSecurity(userId, "melete.author", siteId)) return SecurityAdvice.ALLOWED;
								if (checkSecurity(userId, "melete.student", siteId)) return SecurityAdvice.ALLOWED;
								return SecurityAdvice.NOT_ALLOWED;
							}
						}
						else if (reference.startsWith("/content/private/mneme/"))
						{
							String parts[] = StringUtil.split(reference, "/");
							if (parts.length >= 5)
							{
								String siteId = parts[4];
								if (checkSecurity(userId, "mneme.manage", siteId)) return SecurityAdvice.ALLOWED;
								if (checkSecurity(userId, "mneme.grade", siteId)) return SecurityAdvice.ALLOWED;
								return SecurityAdvice.NOT_ALLOWED;
							}
						}
						return SecurityAdvice.PASS;
					}
				});

				try
				{
					String resourceId = url;
					resourceId = decodeUrl(resourceId);
					if (resourceId.startsWith("/access/content/group/"))
					{
						resourceId = resourceId.substring("/access/content".length());
					}
					else if (resourceId.startsWith("/access/meleteDocs/content/private/"))
					{
						resourceId = resourceId.substring("/access/meleteDocs/content".length());
					}
					else if (resourceId.startsWith("/access/mneme/content/private/"))
					{
						resourceId = resourceId.substring("/access/mneme/content".length());
					}

					ContentResource resource = contentHostingService().getResource(resourceId);
					String type = resource.getContentType();

					// for a text/url, make a "W" with that URL
					if ("text/url".equalsIgnoreCase(type))
					{
						byte[] body = resource.getContent();
						String bodyStr = new String(body, "UTF-8");
						newItem.setSource("W");
						newItem.setStyle("600");
						url = bodyStr;
					}

					// for a text/html, make a "A" with that body, harvest refs
					else if ("text/html".equalsIgnoreCase(type))
					{
						byte[] body = resource.getContent();
						String bodyStr = new String(body, "UTF-8");

						// drop from full document to fragment
						bodyStr = siteResourcesService().convertToFragment(bodyStr);

						// expand relative refs to CHS refs
						String base = url;
						int end = base.lastIndexOf("/");
						if (end != -1)
						{
							base = base.substring(0, end + 1);
						}
						else
						{
							base = "";
						}
						bodyStr = siteResourcesService().expandRelRefsInChs(bodyStr, base);

						newItem.setSource("A");
						newItem.setNewContent(bodyStr);
						url = null;
					}

					// otherwise we will harvest a file
					else
					{
						newItem.setSource("F");
					}
				}
				catch (PermissionException e)
				{
					M_log.warn("contentItemsFromSiteInfo: " + url + " : " + e);
					abort = true;
				}
				catch (IdUnusedException e)
				{
					M_log.warn("contentItemsFromSiteInfo: " + url + " : " + e);
					abort = true;
				}
				catch (TypeException e)
				{
					M_log.warn("contentItemsFromSiteInfo: " + url + " : " + e);
					abort = true;
				}
				catch (ServerOverloadException e)
				{
					M_log.warn("contentItemsFromSiteInfo: " + url + " : " + e);
					abort = true;
				}
				catch (UnsupportedEncodingException e)
				{
					M_log.warn("contentItemsFromSiteInfo: " + url + " : " + e);
					abort = true;
				}
				finally
				{
					securityService().popAdvisor();
				}
			}
			else
			{
				newItem.setSource("W");
				newItem.setStyle("600");
			}

			if (!abort)
			{
				newItem.setTitle("Welcome");
				newItem.setType("?");
				newItem.setUrl(url);
				newItem.setModifiedDate(new Date());
				newItem.setModifiedUser("");

				rv.add(newItem);
			}
		}

		// if there's a description
		if (site.getDescription() != null)
		{
			// create an authored item
			HomeContentItem newItem = new HomeContentItemImpl(null, forSiteId);

			// publish this one only if we don't already have one from the infoUrl
			if (rv.isEmpty() && !asImport)
			{
				// use the base date of the site
				Date baseDate = dateManagerService().getMinStartDate(site.getId());
				if (baseDate == null) baseDate = new Date();

				newItem.setReleaseDate(baseDate);
				newItem.setPublished(Boolean.TRUE);
			}
			else
			{
				newItem.setReleaseDate(null);
				newItem.setPublished(Boolean.FALSE);
			}

			newItem.setSource("A");
			newItem.setTitle("Welcome");
			newItem.setType("text/html");
			String body = site.getDescription();
			body = StringEscapeUtils.escapeHtml4(body);
			body = body.replaceAll("(\r\n|\n)", "<br />");
			body = "<p>" + body + "</p>";
			newItem.setNewContent(body);
			newItem.setModifiedDate(new Date());
			newItem.setModifiedUser("");

			rv.add(newItem);
		}

		return rv;
	}

	/**
	 * Decode the URL as a browser would.
	 * 
	 * @param url
	 *        The URL.
	 * @return the decoded URL.
	 */
	protected String decodeUrl(String url)
	{
		try
		{
			// these the browser will convert when it's making the URL to send
			String processed = url.replaceAll("&amp;", "&");
			processed = processed.replaceAll("&lt;", "<");
			processed = processed.replaceAll("&gt;", ">");
			processed = processed.replaceAll("&quot;", "\"");

			// if a browser sees a plus, it sends a plus (URLDecoder will change it to a space)
			processed = processed.replaceAll("\\+", "%2b");

			// and the rest of the works, including %20 and + handling
			String decoded = URLDecoder.decode(processed, "UTF-8");

			return decoded;
		}
		catch (UnsupportedEncodingException e)
		{
			M_log.warn("decodeUrl: " + e);
		}
		catch (IllegalArgumentException e)
		{
			M_log.warn("decodeUrl: " + e);
		}

		return url;
	}

	protected void deleteContentItemTx(Long itemId)
	{
		String sql = "DELETE FROM HOME_CONTENT WHERE ID = ?";
		Object[] fields = new Object[1];
		fields[0] = itemId;
		if (!sqlService().dbWrite(sql, fields))
		{
			throw new RuntimeException("deleteContentItemTx: db write failed: " + itemId);
		}
	}

	protected void deleteOptionsTx(String siteId)
	{
		String sql = "DELETE FROM HOME_OPTIONS WHERE SITE_ID = ?";
		Object[] fields = new Object[1];
		fields[0] = siteId;
		if (!sqlService().dbWrite(sql, fields))
		{
			throw new RuntimeException("deleteOptionsTx: db write failed: " + siteId);
		}
	}

	/**
	 * Called just before we shutdown.
	 */
	protected void destroy()
	{
		// unregister for site import
		siteImportService().unregisterImporter(this);

		// unregister for site resource ownership
		siteResourcesService().unregisterOwner(this);
	}

	/**
	 * Process the save.
	 */
	protected void doSave(final HomeContentItem item)
	{
		// add if new - item needs an id for the prep
		if (item.getId() == null)
		{
			sqlService().transact(new Runnable()
			{
				public void run()
				{
					addContentItemTx(item);
				}
			}, "saveContentItem: " + item.getSiteId());
		}

		Set<ToolReference> refsToRemove = new HashSet<ToolReference>();
		Set<ToolReference> refsToAdd = new HashSet<ToolReference>();
		Set<HarvestReference> harvest = new HashSet<HarvestReference>();
		prepSaveContentItem(item, refsToRemove, refsToAdd, harvest);

		completeSaveContentItem(item, refsToRemove, refsToAdd, harvest);
	}

	/**
	 * Count how many active students in the site have not visited in the last period (days).
	 * 
	 * @param siteId
	 *        The site id.
	 * @param period
	 *        The period in days.
	 * @return The # not visited.
	 */
	protected int getSiteStudentNotVisitedInPeriod(String siteId, int period)
	{
		// Note: This will only work with "Student" role users - not "access" or any other role names.

		// compute the cutoff date for "recent" (based on 7 day period)
		Calendar cutoff = Calendar.getInstance();
		cutoff.add(Calendar.DATE, -1 * period);
		final Date cutoffDate = cutoff.getTime();

		// get a last visit date for all the active "Student" role users in the site
		// Note: joining to the sakai_user table eliminates any grants to user ids which have been since deleted but left in the grants table
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT G.USER_ID, A.LAST_VISIT FROM SAKAI_REALM_RL_GR G ");
		sql.append("LEFT OUTER JOIN AM_SITE_VISIT A ON G.USER_ID=A.USER_ID AND A.CONTEXT=? ");
		sql.append("JOIN SAKAI_REALM R ON G.REALM_KEY=R.REALM_KEY ");
		sql.append("JOIN SAKAI_REALM_ROLE O ON G.ROLE_KEY=O.ROLE_KEY ");
		sql.append("JOIN SAKAI_USER U ON G.USER_ID=U.USER_ID ");
		sql.append("WHERE R.REALM_ID=? AND G.ACTIVE=1 AND O.ROLE_NAME='Student'");

		Object[] fields = new Object[2];
		fields[0] = siteId;
		fields[1] = "/site/" + siteId;

		@SuppressWarnings("rawtypes")
		List results = sqlService().dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					String userId = sqlService().readString(result, 1);
					Date lastVisit = sqlService().readDate(result, 2);

					// if this user has never visited, or visited before the cutoff, add it to the return
					if ((lastVisit == null) || (lastVisit.before(cutoffDate)))
					{
						return userId;
					}
					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getSiteStudentNotVisitedInPeriod: " + e);
					return null;
				}
			}
		});

		// count the users
		return results.size();
	}

	/**
	 * Check if the URL (long relative) points at a CHS resource that we want to harvest.
	 * 
	 * @param url
	 *        the (long relative) URL.
	 * @return true if we want to harvest, false if not.
	 */
	protected boolean harvestableChsUrl(String url)
	{
		return (url.startsWith("/access/content/group/")) || (url.startsWith("/access/meleteDocs/content/private/"))
				|| (url.startsWith("/access/mneme/content/private/"));
	}

	/**
	 * {@inheritDoc}
	 */
	protected void prepSaveContentItem(HomeContentItem item, Set<ToolReference> oldReferences, Set<ToolReference> newReferences,
			Collection<HarvestReference> harvest)
	{
		// if we are "W" and changing what we are referencing
		if ("W".equals(item.getSource()) && Different.different(item.getUrl(), ((HomeContentItemImpl) item).origUrl))
		{
			String newUrl = XrefHelper.shortenFullUrl(item.getUrl());
			item.setUrl(newUrl);

			// convert a type "W" pointing at a harvest-able CHS resource to a type "F"
			if (harvestableChsUrl(newUrl))
			{
				item.setSource("F");
			}
		}

		// if we are no longer source "F"
		if ((!"F".equals(item.getSource())) && ("F".equals(((HomeContentItemImpl) item).origSource)))
		{
			// clear our current references (one, to the site resource we were using)
			oldReferences.addAll(siteResourcesService().getToolReferences(item.getSiteId(), getTool(), item.getId()));
		}

		// if we are no longer source "A"
		if ((!"A".equals(item.getSource())) && ("A".equals(((HomeContentItemImpl) item).origSource)))
		{
			// clear current references
			oldReferences.addAll(siteResourcesService().getToolReferences(item.getSiteId(), getTool(), item.getId()));

			// we will also need to delete our site placement (in origUrl)
		}

		// if authored with new content
		if ("A".equals(item.getSource()) && (item.getNewContent() != null))
		{
			// clear our current tool references references
			oldReferences.addAll(siteResourcesService().getToolReferences(item.getSiteId(), getTool(), item.getId()));

			// we will also need to delete our site placement (in origUrl)

			// clean and replace any embedded site resource references in our content with a tool-localized URL, and record tool references for all the embedded site resources
			Set<ToolReference> refs = new HashSet<ToolReference>();
			Set<HarvestReference> h = new HashSet<HarvestReference>();

			String content = item.getNewContent();
			content = HtmlHelper.clean(content);
			content = siteResourcesService().toolLocalizeEmbeddedChsReferences(content, item.getSiteId(), getTool(), item.getId(), "Home/", refs, h);
			content = siteResourcesService().toolLocalizeEmbeddedToolReferences(content, item.getSiteId(), getTool(), item.getId(), refs);
			item.setNewContent(content);

			// we need these new tool references
			newReferences.addAll(refs);
			harvest.addAll(h);
		}

		// if we are source "F" and changing what we are referencing
		if ("F".equals(item.getSource()) && Different.different(item.getUrl(), ((HomeContentItemImpl) item).origUrl))
		{
			// clear our current tool references
			oldReferences.addAll(siteResourcesService().getToolReferences(item.getSiteId(), getTool(), item.getId()));

			// url may be a site placement reference URL - convert to a tool reference URL
			Set<ToolReference> refs = new HashSet<ToolReference>();
			Set<HarvestReference> h = new HashSet<HarvestReference>();
			String newUrl = XrefHelper.shortenFullUrl(item.getUrl());
			newUrl = siteResourcesService().toolLocalizeEmbeddedChsReferences(newUrl, item.getSiteId(), getTool(), item.getId(), "Home/", refs, h);
			newUrl = siteResourcesService().toolLocalizeEmbeddedToolReferences(newUrl, item.getSiteId(), getTool(), item.getId(), refs);
			item.setUrl(newUrl);

			// we need these new tool references (really just the one to our selected site resource)
			newReferences.addAll(refs);
			harvest.addAll(h);
		}
	}

	protected void publishContentItemTx(Long itemId, String userId, Date modifiedDate)
	{
		String sql = "UPDATE HOME_CONTENT SET PUBLISHED = '1', MODIFIED_DATE = ?, MODIFIED_USER = ? WHERE ID = ? AND RELEASE_DATE IS NOT NULL AND PUBLISHED = '0'";
		Object[] fields = new Object[3];
		fields[0] = sqlService().valueForDate(modifiedDate);
		fields[1] = userId;
		fields[2] = itemId;
		if (!sqlService().dbWrite(sql, fields))
		{
			throw new RuntimeException("deleteContentItemTx: db write failed: " + itemId);
		}
	}

	protected List<Long> readContentItemIds(String siteId)
	{
		String sql = "SELECT ID FROM HOME_CONTENT WHERE SITE_ID = ?";
		Object[] fields = new Object[1];
		fields[0] = siteId;

		final List<Long> rv = new ArrayList<Long>();
		sqlService().dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					rv.add(sqlService().readLong(result, 1));

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("readContentItemIds: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	protected void unpublishContentItemTx(Long itemId, String userId, Date modifiedDate)
	{
		String sql = "UPDATE HOME_CONTENT SET PUBLISHED='0', MODIFIED_DATE = ?, MODIFIED_USER = ? WHERE ID = ? AND PUBLISHED = '1'";
		Object[] fields = new Object[3];
		fields[0] = sqlService().valueForDate(modifiedDate);
		fields[1] = userId;
		fields[2] = itemId;
		if (!sqlService().dbWrite(sql, fields))
		{
			throw new RuntimeException("unpublishContentItemTx: db write failed: " + itemId);
		}
	}

	protected void updateContentItemTx(HomeContentItem content)
	{
		String sql = "UPDATE HOME_CONTENT SET PUBLISHED = ?, RELEASE_DATE = ?, SOURCE = ?, TITLE = ?, MIME_TYPE = ?, URL = ?, STYLE = ?, MODIFIED_DATE = ?, MODIFIED_USER = ?, ALT=? WHERE ID = ?";

		Object[] fields = new Object[11];
		fields[0] = sqlService().valueForBoolean(content.getPublished());
		fields[1] = sqlService().valueForDate(content.getReleaseDate());
		fields[2] = content.getSource();
		fields[3] = content.getTitle();
		fields[4] = content.getType();
		fields[5] = content.getUrl();
		fields[6] = content.getStyle();
		fields[7] = sqlService().valueForDate(content.getModifiedDate());
		fields[8] = content.getModifiedUser();
		fields[9] = content.getAltText();
		fields[10] = content.getId();

		if (!sqlService().dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("updateContentItemTx: db write failed");
		}
	}

	protected void updateOptionsTx(HomePageOptions options)
	{
		String sql = "UPDATE HOME_OPTIONS SET ANNC_DISPLAY = ?, SCHD_DISPLAY = ?, CHAT_DISPLAY = ?, FORMAT = ?, DISPLAY_ORDER = ?, MODIFIED_DATE = ?, MODIFIED_USER = ?, ANNC_COUNT = ?, ANNC_FULL = ? WHERE ID = ?";

		Object[] fields = new Object[10];
		fields[0] = sqlService().valueForBoolean(options.getAnnouncementsEnabled());
		fields[1] = sqlService().valueForBoolean(options.getScheduleEnabled());
		fields[2] = sqlService().valueForBoolean(options.getChatEnabled());
		fields[3] = options.getFormat();
		fields[4] = options.getOrder();
		fields[5] = sqlService().valueForDate(options.getModifiedDate());
		fields[6] = options.getModifiedUser();
		fields[7] = options.getNumAnnouncements();
		fields[8] = sqlService().valueForBoolean(options.getAnncFull());
		fields[9] = options.getId();

		if (!sqlService().dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("updateOptionsTx: db write failed");
		}
	}

	/**
	 * @return The ContentHostingService, via the component manager.
	 */
	private ContentHostingService contentHostingService()
	{
		return (ContentHostingService) ComponentManager.get(ContentHostingService.class);
	}

	/**
	 * @return The DateManagerService, via the component manager.
	 */
	private DateManagerService dateManagerService()
	{
		return (DateManagerService) ComponentManager.get(DateManagerService.class);
	}

	/**
	 * @return The JForumCategoryService, via the component manager.
	 */
	private JForumCategoryService jForumCategoryService()
	{
		return (JForumCategoryService) ComponentManager.get(JForumCategoryService.class);
	}

	/**
	 * @return The JForumGradeService, via the component manager.
	 */
	private JForumGradeService jForumGradeService()
	{
		return (JForumGradeService) ComponentManager.get(JForumGradeService.class);
	}

	/**
	 * @return The JForumPrivateMessageService, via the component manager.
	 */
	private JForumPrivateMessageService jForumPrivateMessageService()
	{
		return (JForumPrivateMessageService) ComponentManager.get(JForumPrivateMessageService.class);
	}

	/**
	 * @return The SecurityService, via the component manager.
	 */
	private SecurityService securityService()
	{
		return (SecurityService) ComponentManager.get(SecurityService.class);
	}

	/**
	 * @return The ServerConfigurationService, via the component manager.
	 */
	private ServerConfigurationService serverConfigurationService()
	{
		return (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
	}

	/**
	 * @return The SiteImportService, via the component manager.
	 */
	private SiteImportService siteImportService()
	{
		return (SiteImportService) ComponentManager.get(SiteImportService.class);
	}

	/**
	 * @return The SiteResourcesService, via the component manager.
	 */
	private SiteResourcesService siteResourcesService()
	{
		return (SiteResourcesService) ComponentManager.get(SiteResourcesService.class);
	}

	/**
	 * @return The SiteService, via the component manager.
	 */
	private SiteService siteService()
	{
		return (SiteService) ComponentManager.get(SiteService.class);
	}

	/**
	 * @return The SqlService, via the component manager.
	 */
	private SqlService sqlService()
	{
		return (SqlService) ComponentManager.get(SqlService.class);
	}

	/**
	 * @return The SubmissionService, via the component manager.
	 */
	private SubmissionService submissionService()
	{
		return (SubmissionService) ComponentManager.get(SubmissionService.class);
	}
}
