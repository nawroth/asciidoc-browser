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

import org.apache.commons.io.FileUtils;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class AsciidocExecutor
{
    private static final String ASCIIDOC_DIR = "/home/anders/git/doctools/src/bin/asciidoc";
    private static final File asciidoc = new File( new File( ASCIIDOC_DIR ),
            "asciidoc.py" );
    private static final PySystemState pySys = new PySystemState();
    private static final PyList argv = pySys.argv;

    private File targetFile;

    public static void main( final String[] args )
    {
        transform( "/home/anders/git/manual/src/main/resources/community/translating.asciidoc" );
        transform( "/home/anders/git/manual/src/main/resources/community/contributors.asciidoc" );
    }

    AsciidocExecutor()
    {
        try
        {
            targetFile = File.createTempFile( "asciidoc-browser.", ".xhtml" );
            System.out.println( targetFile.getAbsolutePath() );
        }
        catch ( IOException ioe )
        {
            ioe.printStackTrace();
        }
    }

    void generate( final String document )
    {
        argv.clear();
        argv.append( new PyString( "" ) );
        argv.append( new PyString( "--backend" ) );
        argv.append( new PyString( "xhtml11" ) );
        argv.append( new PyString( "--out-file" ) );
        argv.append( new PyString( targetFile.getAbsolutePath() ) );
        argv.append( new PyString( document ) );
        PythonInterpreter python = new PythonInterpreter( null, pySys );
        python.set( "__file__", asciidoc.getAbsolutePath() );
        python.execfile( asciidoc.getAbsolutePath() );
    }

    File getTargetFile()
    {
        return targetFile;
    }

    String getResult()
    {
        String result = null;
        try
        {
            result = FileUtils.readFileToString( targetFile, "UTF-8" );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return result;
    }

    static void transform( final String document )
    {
        argv.clear();
        argv.append( new PyString( "" ) );
        argv.append( new PyString( document ) );
        PythonInterpreter python = new PythonInterpreter( null, pySys );
        python.set( "__file__", asciidoc.getAbsolutePath() );
        python.execfile( asciidoc.getAbsolutePath() );
    }
}
