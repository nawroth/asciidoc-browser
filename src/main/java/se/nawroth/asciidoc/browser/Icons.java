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

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;

enum Icons
{
    HOME( "go-home", "actions" ),
    FORWARD( "go-next", "actions" ),
    OPTIONS( "preferences-system", "categories" ),
    BACK( "go-previous", "actions" ),
    APPLICATION( "x-office-document", "mimetypes" ),
    PREVIEW( "internet-web-browser", "apps" ),
    REFRESH( "view-refresh", "actions" );

    private static final String SIZE = "22x22";
    private static final String EXTENSION = "png";
    private static final String BASE_PATH = "/org/freedesktop/tango/";

    private final String path;

    private Icons( final String name, final String section )
    {
        this.path = BASE_PATH + SIZE + "/" + section + "/" + name + "."
                + EXTENSION;
    }

    private URL getResource()
    {
        return Icons.class.getResource( path );
    }

    ImageIcon icon()
    {
        return new ImageIcon( getResource() );
    }

    Image image()
    {
        return Toolkit.getDefaultToolkit()
                .getImage( getResource() );
    }
}
