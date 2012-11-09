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

import java.io.File;

@SuppressWarnings( "serial" )
public class FileWrapper extends File
{
    static String excludeStart = Settings.getExcludeStart();
    static int maxStringLength = Settings.getMaxFilepathLength();

    public FileWrapper( final File parent, final String child )
    {
        super( parent, child );
    }

    public FileWrapper( final String parent, final String child )
    {
        super( parent, child );
    }

    public FileWrapper( final String pathname )
    {
        super( pathname );
    }

    static void setSmartExclude( final String path )
    {
        int separatorPos = path.lastIndexOf( File.separatorChar );
        if ( separatorPos > 10 )
        {
            excludeStart = path.substring( 0, separatorPos + 1 );
        }
    }

    @Override
    public String toString()
    {
        String string = super.toString();
        if ( !excludeStart.isEmpty() && string.startsWith( excludeStart ) )
        {
            string = string.substring( excludeStart.length() );
        }
        int length = string.length();
        if ( length > maxStringLength )
        {
            string = "..." + string.substring( length - maxStringLength );
        }
        return string;
    }
}
