/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package se.nawroth.asciidoc.browser;

import java.util.prefs.Preferences;

public class Settings
{
    private static final String HOME_LOCATION_KEY = "home";
    private static final String ASCIIDOC_CONFIG_KEY = "asciidocConfig";
    private static final String MAX_FILEPATH_LENGTH_KEY = "maxFilepathLength";
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
        return prefs.getInt( MAX_FILEPATH_LENGTH_KEY, Settings.MAX_FILEPATH_LENGTH );
    }
}
