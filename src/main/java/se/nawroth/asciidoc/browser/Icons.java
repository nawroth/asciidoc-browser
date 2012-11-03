/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package se.nawroth.asciidoc.browser;

enum Icons
{
    HOME( "go-home", "actions" ),
    FORWARD( "go-next", "actions" ),
    OPTIONS( "preferences-system", "categories" ),
    BACK( "go-previous", "actions" ),
    APPLICATION( "x-office-document", "mimetypes" ),
    PREVIEW( "internet-web-browser", "apps" );

    private static final String SIZE = "22x22";
    private static final String EXTENSION = "png";
    private static final String BASE_PATH = "/org/freedesktop/tango/";

    private final String path;

    private Icons( final String name, final String section )
    {
        this.path = BASE_PATH + SIZE + "/" + section + "/" + name + "."
                + EXTENSION;
    }

    String path()
    {
        return path;
    }
}
