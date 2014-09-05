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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

@SuppressWarnings( "serial" )
final class TextFieldTransferHandler extends TransferHandler
{
    private final TransferHandler oldTransferHandler;
    private final Runnable prepareDrop;
    private final Runnable handleDrop;

    TextFieldTransferHandler( final TransferHandler oldTransferHandler,
            final Runnable prepareDrop, final Runnable handleDrop )
    {
        this.oldTransferHandler = oldTransferHandler;
        this.prepareDrop = prepareDrop;
        this.handleDrop = handleDrop;
    }

    @Override
    public void exportAsDrag( final JComponent comp, final InputEvent e,
            final int action )
    {
        oldTransferHandler.exportAsDrag( comp, e, action );
    }

    @Override
    public void exportToClipboard( final JComponent comp, final Clipboard clip,
            final int action ) throws IllegalStateException
    {
        oldTransferHandler.exportToClipboard( comp, clip, action );
    }

    @Override
    public boolean importData( final TransferSupport support )
    {
        if ( prepareDrop != null )
        {
            prepareDrop.run();
        }
        boolean success = oldTransferHandler.importData( support );
        if ( success && handleDrop != null )
        {
            handleDrop.run();
        }
        return success;
    }

    @Override
    public boolean importData( final JComponent comp, final Transferable t )
    {
        return oldTransferHandler.importData( comp, t );
    }

    @Override
    public boolean canImport( final TransferSupport support )
    {
        return oldTransferHandler.canImport( support );
    }

    @Override
    public boolean canImport( final JComponent comp,
            final DataFlavor[] transferFlavors )
    {
        return oldTransferHandler.canImport( comp, transferFlavors );
    }

    @Override
    public int getSourceActions( final JComponent c )
    {
        return oldTransferHandler.getSourceActions( c );
    }

    @Override
    public Icon getVisualRepresentation( final Transferable t )
    {
        return oldTransferHandler.getVisualRepresentation( t );
    }
}
