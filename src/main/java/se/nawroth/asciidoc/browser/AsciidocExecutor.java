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
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;

public class AsciidocExecutor
{
    private final static Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    AsciidocExecutor()
    {
    }

    File generate( final File documentPath )
    {
        try
        {
            return Document.getDocument( documentPath ).render();
        }
        catch ( IOException e1 )
        {
            e1.printStackTrace();
        }
        return null;
    }

    private static class Document
    {
        private final static Map<File, Document> documents = new HashMap<File, Document>();
        private final File documentPath;
        private final File targetDir;
        private final File targetFile;
        private final Attributes attributes = new Attributes();
        private final Options options = new Options();
        private Long lastModified = null;

        private Document( final File documentPath ) throws IOException
        {

            this.documentPath = documentPath;
            this.targetDir = Files.createTempDirectory( "adoc-browser" ).toFile();
            targetDir.deleteOnExit();
            targetFile = new File( targetDir, "index.html" );
            attributes.setBackend( "html5" );
            options.setAttributes( attributes );
            options.setInPlace( false );
            options.setToFile( targetFile.getAbsolutePath() );
            options.setSafe( SafeMode.UNSAFE );
        }

        private File render()
        {
            long documentTimestamp = documentPath.lastModified();
            // String configuration = Settings.getConfiguration();
            if ( targetFile.length() > 0 && lastModified != null && lastModified.longValue() == documentTimestamp )
            {
                return targetFile;
            }
            lastModified = documentTimestamp;

            asciidoctor.renderFile( documentPath, options );

            return targetFile;
        }

        static Document getDocument( final File documentPath ) throws IOException
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
