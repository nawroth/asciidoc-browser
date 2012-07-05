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

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringEscapeUtils;

public class AsciidocBrowserApplication extends JFrame implements
        HyperlinkListener
{
    private static final long serialVersionUID = 1L;

    private static final String LINK_LINE_START = "include::";

    private static JDialog settingsDialog;

    private final JButton backButton, forwardButton;

    private final JTextField locationTextField;

    private final JEditorPane fileEditorPane;

    private final List<File> pageList = new ArrayList<File>();

    private File currentFile;
    private final JButton btnHomebutton;
    private final JScrollPane treeScrollPane;
    private final JTree documentTree;
    private final JSplitPane splitPane;

    public AsciidocBrowserApplication()
    {
        super( "Asciidoc Browser" );
        setIconImage( Toolkit.getDefaultToolkit()
                .getImage(
                        AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/16x16/mimetypes/x-office-document.png" ) ) );

        setSize( 800, 1024 );

        addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( final WindowEvent e )
            {
                actionExit();
            }
        } );

        JPanel buttonPanel = new JPanel();
        backButton = new JButton( "" );
        backButton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/22x22/actions/go-previous.png" ) ) );
        backButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                actionBack();
            }
        } );
        buttonPanel.setLayout( new MigLayout( "", "[1px][][][]", "[1px]" ) );

        JButton btnOptionsbutton = new JButton( "" );
        btnOptionsbutton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                settingsDialog.setVisible( true );
            }
        } );
        btnOptionsbutton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/22x22/categories/preferences-system.png" ) ) );
        buttonPanel.add( btnOptionsbutton, "flowx,cell 0 0" );
        backButton.setEnabled( false );
        buttonPanel.add( backButton, "cell 0 0,grow" );
        forwardButton = new JButton( "" );
        forwardButton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/22x22/actions/go-next.png" ) ) );
        forwardButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                actionForward();
            }
        } );
        forwardButton.setEnabled( false );
        buttonPanel.add( forwardButton, "cell 0 0,grow" );
        getContentPane().setLayout(
                new MigLayout( "", "[793.00px,grow]", "[44px][930px]" ) );
        getContentPane().add( buttonPanel, "cell 0 0,growx,aligny top" );
        JButton goButton = new JButton( "" );
        goButton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/22x22/categories/applications-internet.png" ) ) );
        goButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                actionGo();
            }
        } );
        locationTextField = new JTextField( 65 );
        locationTextField.setText( Settings.getHome() );
        locationTextField.addKeyListener( new KeyAdapter()
        {
            @Override
            public void keyReleased( final KeyEvent e )
            {
                if ( e.getKeyCode() == KeyEvent.VK_ENTER )
                {
                    actionGo();
                }
            }
        } );

        btnHomebutton = new JButton( "" );
        btnHomebutton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                showFile( Settings.getHome(), true );
            }
        } );
        btnHomebutton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/22x22/actions/go-home.png" ) ) );
        buttonPanel.add( btnHomebutton, "cell 1 0" );
        buttonPanel.add( locationTextField, "cell 2 0,grow" );
        buttonPanel.add( goButton, "flowx,cell 3 0,alignx right,growy" );

        splitPane = new JSplitPane();
        splitPane.setResizeWeight( 0.15 );
        getContentPane().add( splitPane, "cell 0 1,grow" );

        treeScrollPane = new JScrollPane();
        splitPane.setLeftComponent( treeScrollPane );

        documentTree = new DocumentTree();
        treeScrollPane.setViewportView( documentTree );

        fileEditorPane = new JEditorPane();
        fileEditorPane.setContentType( "text/html" );
        fileEditorPane.setEditable( false );
        fileEditorPane.addHyperlinkListener( this );
        JScrollPane fileScrollPane = new JScrollPane( fileEditorPane );
        splitPane.setRightComponent( fileScrollPane );
    }

    private void actionExit()
    {
        settingsDialog.dispose();
        System.exit( 0 );
    }

    private void actionBack()
    {
        int pageIndex = pageList.indexOf( currentFile );
        try
        {
            showFile( pageList.get( pageIndex - 1 ), false );
        }
        catch ( Exception e )
        {
        }
    }

    private void actionForward()
    {
        int pageIndex = pageList.indexOf( currentFile );
        try
        {
            showFile( pageList.get( pageIndex + 1 ), false );
        }
        catch ( Exception e )
        {
        }
    }

    private void actionGo()
    {
        File file = FileUtils.getFile( locationTextField.getText() );
        if ( !file.exists() )
        {
            showError( "FIle doesn't exist: " + file );
            return;
        }
        if ( file.isDirectory() )
        {
            showError( "This is a directory, cannot open it: " + file );
            return;
        }
        if ( !file.canRead() )
        {
            showError( "Cannot read the file: " + file );
        }
        showFile( file, true );
    }

    private void showFile( final String file, final boolean addIt )
    {
        showFile( FileUtils.getFile( file ), addIt );
    }

    private void showFile( final File file, final boolean addIt )
    {
        locationTextField.setText( file.getAbsolutePath() );
        Map<CharSequence, CharSequence> replacements = getReplacements();
        try
        {
            StringBuilder sb = new StringBuilder( 10 * 1024 );
            sb.append( "<html><head><title>"
                       + file.getName()
                       + "</title><style>body {font-size: 1em;}pre {margin: 0;}</style></head><body>" );
            String parent = file.getParent();
            LineIterator lines = FileUtils.lineIterator( file, "UTF-8" );
            while ( lines.hasNext() )
            {
                String line = StringEscapeUtils.escapeHtml4( lines.next() );
                sb.append( "<pre>" );
                if ( line.startsWith( LINK_LINE_START ) )
                {
                    String href = getFileLocation( replacements, parent, line );

                    sb.append( "<a href=\"" )
                            .append( href )
                            .append( "\">" )
                            .append( line )
                            .append( "</a>" );
                }
                else
                {
                    sb.append( line );
                }
                sb.append( "</pre>" );
            }
            sb.append( "</body></html>" );
            lines.close();

            fileEditorPane.setText( sb.toString() );
            fileEditorPane.setCaretPosition( 0 );
            if ( addIt )
            {
                int listSize = pageList.size();
                if ( listSize > 0 )
                {
                    int pageIndex = pageList.indexOf( currentFile );
                    if ( pageIndex < listSize - 1 )
                    {
                        for ( int i = listSize - 1; i > pageIndex; i-- )
                        {
                            pageList.remove( i );
                        }
                    }
                }
                pageList.add( file );
            }
            currentFile = file;
            updateButtons();
        }
        catch ( IOException e )
        {
            showError( "Error in file handling: " + e );
        }
    }

    private Map<CharSequence, CharSequence> getReplacements()
    {
        Map<CharSequence, CharSequence> replacements = new HashMap<CharSequence, CharSequence>();
        for ( String replacementLine : Settings.getReplacements()
                .split( "\n" ) )
        {
            int commaPos = replacementLine.indexOf( ',' );
            if ( commaPos > 0 )
            {
                replacements.put( replacementLine.substring( 0, commaPos ),
                        replacementLine.substring( commaPos + 1 ) );
            }
        }
        return replacements;
    }

    private String getFileLocation(
            final Map<CharSequence, CharSequence> replacements,
            final String parent, final String line )
    {
        int pos = line.indexOf( "[" );
        String href = line.substring( LINK_LINE_START.length(), pos );
        String hrefWithReplacements = href;
        for ( Entry<CharSequence, CharSequence> entry : replacements.entrySet() )
        {
            hrefWithReplacements = hrefWithReplacements.replace(
                    entry.getKey(), entry.getValue() );
        }
        if ( hrefWithReplacements.equals( href ) )
        {
            href = parent + File.separator + href;
        }
        else
        {
            href = hrefWithReplacements;
        }
        return href;
    }

    private void showError( final String errorMessage )
    {
        JOptionPane.showMessageDialog( this, errorMessage, "Error",
                JOptionPane.ERROR_MESSAGE );
    }

    private void updateButtons()
    {
        if ( pageList.size() < 2 )
        {
            backButton.setEnabled( false );
            forwardButton.setEnabled( false );
        }
        else
        {
            int pageIndex = pageList.indexOf( currentFile );
            backButton.setEnabled( pageIndex > 0 );
            forwardButton.setEnabled( pageIndex < ( pageList.size() - 1 ) );
        }
    }

    @Override
    public void hyperlinkUpdate( final HyperlinkEvent event )
    {
        HyperlinkEvent.EventType eventType = event.getEventType();
        if ( eventType == HyperlinkEvent.EventType.ACTIVATED )
        {
            locationTextField.setText( event.getDescription() );
            actionGo();
        }
    }

    public static void main( final String[] args )
    {
        EventQueue.invokeLater( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JFrame.setDefaultLookAndFeelDecorated( true );
                    JDialog.setDefaultLookAndFeelDecorated( true );
                    System.setProperty( "sun.awt.noerasebackground", "true" );
                    UIManager.setLookAndFeel( new AsciidocBrowserSubstanceSkin() );
                    AsciidocBrowserApplication browser = new AsciidocBrowserApplication();
                    browser.setVisible( true );
                    settingsDialog = new SettingsDialog();
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        } );
    }
}
