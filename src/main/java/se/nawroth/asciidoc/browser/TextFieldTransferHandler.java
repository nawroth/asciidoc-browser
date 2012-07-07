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
