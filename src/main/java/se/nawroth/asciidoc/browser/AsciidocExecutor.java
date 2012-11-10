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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class AsciidocExecutor
{
    private static File asciidoc;
    private static final PySystemState pySys = new PySystemState();
    private static final PyList argv = pySys.argv;
    private static final List<String> DEFAULT_ARGUMENTS = new ArrayList<String>()
    {
        {
            add( "--backend" );
            add( "xhtml11" );
        }
    };

    private static PythonInterpreter python;

    AsciidocExecutor( final String asciidocDir )
    {
        asciidoc = new File( new File( asciidocDir ), "asciidoc.py" );
        python = new PythonInterpreter( null, pySys );
    }

    File generate( final String documentPath )
    {
        try
        {
            return Document.getDocument( documentPath )
                    .render();
        }
        catch ( IOException e1 )
        {
            e1.printStackTrace();
        }
        return null;
    }

    private static class Document
    {
        private final static Map<String, Document> documents = new HashMap<String, Document>();
        private final String documentPath;
        private final File targetFile;
        private Long lastModified = null;
        private String asciidocConfig = null;

        private Document( final String documentPath ) throws IOException
        {
            this.documentPath = documentPath;
            this.targetFile = File.createTempFile( "asciidoc-browser.",
                    ".xhtml" );
        }

        private File render()
        {
            long documentTimestamp = new File( documentPath ).lastModified();
            String configuration = Settings.getConfiguration();
            if ( targetFile.length() > 0
                 && lastModified.longValue() == documentTimestamp
                 && configuration == asciidocConfig )
            {
                return targetFile;
            }
            lastModified = documentTimestamp;
            asciidocConfig = configuration;
            File configFile = null;
            try
            {
                if ( configuration != null && !configuration.isEmpty() )
                {
                    try
                    {
                        configFile = File.createTempFile( "asciidoc", ".conf" );
                        FileUtils.write( configFile, configuration, "UTF-8" );
                    }
                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }
                }
                List<String> command = new ArrayList<String>( DEFAULT_ARGUMENTS );
                if ( configFile != null )
                {
                    command.add( "--conf-file" );
                    command.add( configFile.getAbsolutePath() );
                }
                command.add( "--out-file" );
                command.add( targetFile.getAbsolutePath() );
                command.add( documentPath );
                if ( Settings.getJython() )
                {
                    renderUsingJython( command );
                }
                else
                {
                    renderUsingNativePython( command );
                }
            }
            finally
            {
                if ( configFile != null && configFile.exists() )
                {
                    configFile.delete();
                }
            }
            return targetFile;
        }

        private void renderUsingNativePython( final List<String> arguments )
        {
            List<String> command = new ArrayList<String>();
            command.add( asciidoc.getAbsolutePath() );
            command.addAll( arguments );
            ProcessBuilder processBuilder = new ProcessBuilder( command );
            try
            {
                Process process = processBuilder.start();
                process.waitFor();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        private void renderUsingJython( final List<String> arguments )
        {
            argv.clear();
            argv.append( new PyString( "" ) );
            for ( String argument : arguments )
            {
                argv.append( new PyString( argument ) );
            }
            python.set( "__file__", asciidoc.getAbsolutePath() );
            python.execfile( asciidoc.getAbsolutePath() );
        }

        static Document getDocument( final String documentPath )
                throws IOException
        {
            Document document = documents.get( documentPath );
            if ( document == null )
            {
                document = new Document( documentPath );
                documents.put( documentPath, document );
            }
            return document;
        }
    }
}
