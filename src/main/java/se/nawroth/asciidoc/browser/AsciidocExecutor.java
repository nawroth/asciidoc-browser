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

import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class AsciidocExecutor
{
    private static File asciidoc;
    private static final PySystemState pySys = new PySystemState();
    private static final PyList argv = pySys.argv;
    private static final List<String> arguments = new ArrayList<String>()
    {
        {
            add( "--backend" );
            add( "xhtml11" );
            add( "--out-file" );
        }
    };

    private static PythonInterpreter python;

    private File targetFile;

    AsciidocExecutor( final String asciidocDir )
    {
        asciidoc = new File( new File( asciidocDir ), "asciidoc.py" );
        try
        {
            targetFile = File.createTempFile( "asciidoc-browser.", ".xhtml" );
            System.out.println( targetFile.getAbsolutePath() );
        }
        catch ( IOException ioe )
        {
            ioe.printStackTrace();
        }
        python = new PythonInterpreter( null, pySys );
    }

    File generate( final String documentPath )
    {
        try
        {
            Document document = Document.getDocument( documentPath );
            return document.render();
        }
        catch ( IOException e1 )
        {
            e1.printStackTrace();
        }
        return targetFile;
    }

    private static class Document
    {
        private final static Map<String, Document> documents = new HashMap<String, Document>();
        private final String documentPath;
        private final File targetFile;
        private Long lastModified = null;

        private Document( final String documentPath ) throws IOException
        {
            this.documentPath = documentPath;
            this.targetFile = File.createTempFile( "asciidoc-browser.",
                    ".xhtml" );
        }

        private File render()
        {
            long documentTimestamp = new File( documentPath ).lastModified();
            if ( targetFile.length() > 0 && lastModified == documentTimestamp )
            {
                return targetFile;
            }
            lastModified = documentTimestamp;
            if ( true )
            {
                renderUsingNativePython();
            }
            else
            {
                renderUsingJython();
            }
            return targetFile;
        }

        private void renderUsingNativePython()
        {
            List<String> command = new ArrayList<String>();
            command.add( asciidoc.getAbsolutePath() );
            command.addAll( arguments );
            command.add( targetFile.getAbsolutePath() );
            command.add( documentPath );
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

        private void renderUsingJython()
        {
            argv.clear();
            argv.append( new PyString( "" ) );
            for (String argument : arguments)
            {
                argv.append( new PyString( argument) );
            }
            argv.append( new PyString( targetFile.getAbsolutePath() ) );
            argv.append( new PyString( documentPath ) );
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
            }
            return document;
        }
    }
}
