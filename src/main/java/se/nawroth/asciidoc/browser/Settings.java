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

import java.util.prefs.Preferences;

public class Settings
{
    private static final String HOME_LOCATION_KEY = "home";
    private static final String ASCIIDOC_CONFIG_KEY = "asciidocConfig";
    private static final String MAX_FILEPATH_LENGTH_KEY = "maxFilepathLength";
    private static final String USE_JYTHON_KEY = "jython";
    static final int MAX_FILEPATH_LENGTH = 50;
    private static final Preferences prefs = Preferences.userNodeForPackage( Settings.class );

    static void setHome( final String location )
    {
        prefs.put( HOME_LOCATION_KEY, location );
    }

    static String getHome()
    {
        return prefs.get( HOME_LOCATION_KEY, "" );
    }

    static void setConfiguration( final String configuration )
    {
        prefs.put( ASCIIDOC_CONFIG_KEY, configuration );
    }

    static String getConfiguration()
    {
        return prefs.get( ASCIIDOC_CONFIG_KEY, "" );
    }

    static void setMaxFilepathLength( final int length )
    {
        prefs.putInt( MAX_FILEPATH_LENGTH_KEY, length );
        FileWrapper.maxStringLength = length;
    }

    static int getMaxFilepathLength()
    {
        return prefs.getInt( MAX_FILEPATH_LENGTH_KEY,
                Settings.MAX_FILEPATH_LENGTH );
    }

    static boolean getJython()
    {
        return prefs.getBoolean( USE_JYTHON_KEY, false );
    }

    static void setJython( final boolean jythonEnabled )
    {
        prefs.putBoolean( USE_JYTHON_KEY, jythonEnabled );
    }
}
