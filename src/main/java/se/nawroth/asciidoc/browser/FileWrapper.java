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

import java.io.File;

@SuppressWarnings( "serial" )
public class FileWrapper extends File
{
    static String excludeStart = "";
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
