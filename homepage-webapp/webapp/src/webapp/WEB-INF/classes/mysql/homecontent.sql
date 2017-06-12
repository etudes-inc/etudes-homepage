--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/homepage/trunk/homepage-webapp/webapp/src/webapp/WEB-INF/classes/mysql/homecontent.sql $
-- $Id: homecontent.sql 9697 2014-12-27 00:32:01Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2013, 2014 Etudes, Inc.
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
--*********************************************************************************/

-----------------------------------------------------------------------------
-- Homecontent DDL
-----------------------------------------------------------------------------

CREATE TABLE HOME_OPTIONS
(
	ID				BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    SITE_ID			VARCHAR (99) NOT NULL,
    ANNC_DISPLAY	CHAR (1) NOT NULL CHECK (ANNC_DISPLAY IN ('0','1')),
    SCHD_DISPLAY	CHAR (1) NOT NULL CHECK (SCHD_DISPLAY IN ('0','1')),
    CHAT_DISPLAY	CHAR (1) NOT NULL CHECK (CHAT_DISPLAY IN ('0','1')),
    FORMAT			CHAR (1) NOT NULL CHECK (FORMAT IN ('0','1')),
    DISPLAY_ORDER	VARCHAR (12) NOT NULL,
    MODIFIED_DATE	BIGINT NOT NULL,
    MODIFIED_USER	VARCHAR (99) NOT NULL,
    ANNC_COUNT		INTEGER NULL,
    ANNC_FULL		CHAR (1),
    PRIMARY KEY	(ID),
    UNIQUE KEY HOME_OPTIONS_IDX_SITE (SITE_ID)
);

CREATE TABLE HOME_CONTENT
(
	ID				BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    SITE_ID			VARCHAR (99) NOT NULL,    
    PUBLISHED		CHAR (1) NOT NULL CHECK (PUBLISHED IN ('0','1')),
	RELEASE_DATE	BIGINT NULL,
	SOURCE			CHAR (1) NOT NULL CHECK (SOURCE IN ('-','W','A','F','Y')),
	TITLE			VARCHAR (255) NOT NULL,
	MIME_TYPE		VARCHAR (255) NOT NULL,
	URL				VARCHAR (2048) NULL,
	STYLE			VARCHAR(12) NULL,
    MODIFIED_DATE	BIGINT NOT NULL,
    MODIFIED_USER	VARCHAR (99) NOT NULL,
	ALT				VARCHAR (2048) NULL,
    PRIMARY KEY	(ID),
    KEY HOME_CONTENT_IDX_SITE (SITE_ID)
);
