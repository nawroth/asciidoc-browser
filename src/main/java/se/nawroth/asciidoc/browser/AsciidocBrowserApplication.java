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

import java.awt.BorderLayout;
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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringEscapeUtils;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;

public class AsciidocBrowserApplication extends JFrame implements
        HyperlinkListener
{
    private static final long serialVersionUID = 1L;

    private static final String LINK_LINE_START = "include::";

    private static final JDialog SETTINGS_DIALOG = new SettingsDialog();

    private JButton backButton, forwardButton;

    private JTextField locationTextField;

    private JEditorPane displayEditorPane;

    private List<File> pageList = new ArrayList<File>();

    private File currentFile;

    public AsciidocBrowserApplication()
    {
        super( "Asciidoc Browser" );

        setSize( 800, 1024 );

        addWindowListener( new WindowAdapter()
        {
            public void windowClosing( WindowEvent e )
            {
                actionExit();
            }
        } );

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu( "File" );
        fileMenu.setMnemonic( KeyEvent.VK_F );
        JMenuItem fileExitMenuItem = new JMenuItem( "Exit", KeyEvent.VK_X );
        fileExitMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                actionExit();
            }
        } );

        JMenuItem fileOptionsMenuItem = new JMenuItem( "Options", KeyEvent.VK_O );
        fileOptionsMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                SETTINGS_DIALOG.setVisible( true );
            }
        } );

        fileMenu.add( fileOptionsMenuItem );
        fileMenu.add( fileExitMenuItem );
        menuBar.add( fileMenu );
        setJMenuBar( menuBar );

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new FormLayout(
                new ColumnSpec[] { ColumnSpec.decode( "left:12px" ),
                        FormFactory.RELATED_GAP_COLSPEC,
                        ColumnSpec.decode( "57px" ),
                        ColumnSpec.decode( "9px" ),
                        ColumnSpec.decode( "56px" ),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        ColumnSpec.decode( "max(259dlu;min):grow" ),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        ColumnSpec.decode( "right:56px" ),
                        FormFactory.RELATED_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC, },
                new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
                        RowSpec.decode( "32px" ), } ) );
        backButton = new JButton( "" );
        backButton.setHorizontalAlignment( SwingConstants.LEFT );
        backButton.setIcon(new ImageIcon(AsciidocBrowserApplication.class.getResource("/org/freedesktop/tango/22x22/actions/go-previous.png")));
        backButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                actionBack();
            }
        } );
        backButton.setEnabled( false );
        buttonPanel.add( backButton, "3, 2, left, top" );
        locationTextField = new JTextField( 65 );
        locationTextField.setText( Settings.getHome() );
        locationTextField.addKeyListener( new KeyAdapter()
        {
            public void keyReleased( KeyEvent e )
            {
                if ( e.getKeyCode() == KeyEvent.VK_ENTER )
                {
                    actionGo();
                }
            }
        } );
        forwardButton = new JButton( "" );
        forwardButton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/22x22/actions/go-next.png" ) ) );
        forwardButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                actionForward();
            }
        } );
        forwardButton.setEnabled( false );
        buttonPanel.add( forwardButton, "5, 2, left, top" );
        buttonPanel.add( locationTextField, "7, 2, left, center" );

        displayEditorPane = new JEditorPane();
        displayEditorPane.setContentType( "text/html" );
        displayEditorPane.setEditable( false );
        displayEditorPane.addHyperlinkListener( this );

        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( buttonPanel, BorderLayout.NORTH );
        JButton goButton = new JButton( "" );
        goButton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( "/org/freedesktop/tango/22x22/categories/applications-internet.png" ) ) );
        goButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                actionGo();
            }
        } );
        buttonPanel.add( goButton, "9, 2, left, top" );
        getContentPane().add( new JScrollPane( displayEditorPane ),
                BorderLayout.CENTER );
    }

    private void actionExit()
    {
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

    private void showFile( File file, boolean addIt )
    {
        locationTextField.setText( file.getAbsolutePath() );
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
        try
        {
            StringBuilder sb = new StringBuilder( 10 * 1024 );
            sb.append( "<html><head><title>"
                       + file.getName()
                       + "</title><style>body {font-size: 1em;}pre {margin: 0;}</style></head><body>" );
            LineIterator lines = FileUtils.lineIterator( file, "UTF-8" );
            while ( lines.hasNext() )
            {
                String line = StringEscapeUtils.escapeHtml4( lines.next() );
                sb.append( "<pre>" );
                if ( line.startsWith( LINK_LINE_START ) )
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
                        href = file.getParent() + file.separator + href;
                    }
                    else
                    {
                        href = hrefWithReplacements;
                    }

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

            displayEditorPane.setText( sb.toString() );
            displayEditorPane.setCaretPosition( 0 );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    private void showError( String errorMessage )
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

    public void hyperlinkUpdate( HyperlinkEvent event )
    {
        HyperlinkEvent.EventType eventType = event.getEventType();
        if ( eventType == HyperlinkEvent.EventType.ACTIVATED )
        {
            locationTextField.setText( event.getDescription() );
            actionGo();
        }
    }

    public static void main( String[] args )
    {
        AsciidocBrowserApplication browser = new AsciidocBrowserApplication();
        browser.setVisible( true );
    }
}
