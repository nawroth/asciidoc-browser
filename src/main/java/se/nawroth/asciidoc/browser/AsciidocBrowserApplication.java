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
import java.util.Arrays;
import java.util.Collection;
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
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings( "serial" )
public class AsciidocBrowserApplication extends JFrame
{
    private static final String HOME_ICON = "/org/freedesktop/tango/22x22/actions/go-home.png";

    private static final String FORWARD_ICON = "/org/freedesktop/tango/22x22/actions/go-next.png";

    private static final String OPTIONS_ICON = "/org/freedesktop/tango/22x22/categories/preferences-system.png";

    private static final String BACK_ICON = "/org/freedesktop/tango/22x22/actions/go-previous.png";

    private static final String APPLICATION_ICON = "/org/freedesktop/tango/16x16/mimetypes/x-office-document.png";

    private static final int INITIAL_TOOLTIP_DELAY = 500;

    private static final String FILE_URI_SCHEMA = "file://";

    private static final String LINK_LINE_START = "include::";

    private static JDialog settingsDialog;

    private final JButton backButton, forwardButton;

    private final JTextField locationTextField;

    private final JEditorPane fileEditorPane;

    private final List<File> pageList = new ArrayList<File>();

    private File currentFile;
    private final JButton homebutton;
    private final JScrollPane treeScrollPane;
    private final JTree documentTree;
    private final JSplitPane splitPane;

    private final Map<CharSequence, CharSequence> replacements = new HashMap<CharSequence, CharSequence>();

    private final DefaultTreeModel documentModel = new DefaultTreeModel( null );

    private final Map<FileWrapper, Object[]> paths = new HashMap<FileWrapper, Object[]>();

    private TreePath currentSelectionPath;

    public AsciidocBrowserApplication()
    {
        super( "Asciidoc Browser" );
        setIconImage( Toolkit.getDefaultToolkit()
                .getImage(
                        AsciidocBrowserApplication.class.getResource( APPLICATION_ICON ) ) );

        setSize( 1024, 800 );

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
                AsciidocBrowserApplication.class.getResource( BACK_ICON ) ) );
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
                AsciidocBrowserApplication.class.getResource( OPTIONS_ICON ) ) );
        buttonPanel.add( btnOptionsbutton, "flowx,cell 0 0" );
        backButton.setEnabled( false );
        buttonPanel.add( backButton, "cell 0 0,grow" );
        forwardButton = new JButton( "" );
        forwardButton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( FORWARD_ICON ) ) );
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
        locationTextField = new JTextField( 65 );
        locationTextField.setText( "" );
        locationTextField.addKeyListener( new KeyAdapter()
        {
            @Override
            public void keyReleased( final KeyEvent e )
            {
                int keyCode = e.getKeyCode();
                if ( keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_TAB )
                {
                    actionGo();
                    refreshDocumentTree();
                }
            }
        } );
        locationTextField.setTransferHandler( new TextFieldTransferHandler(
                locationTextField.getTransferHandler(), new Runnable()
                {
                    @Override
                    public void run()
                    {
                        locationTextField.setText( "" );
                    }
                }, new Runnable()
                {

                    @Override
                    public void run()
                    {
                        actionGo();
                        refreshDocumentTree();
                    }
                } ) );

        homebutton = new JButton( "" );
        homebutton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                locationTextField.setText( Settings.getHome() );
                actionGo();
                refreshDocumentTree();
            }
        } );
        homebutton.setIcon( new ImageIcon(
                AsciidocBrowserApplication.class.getResource( HOME_ICON ) ) );
        buttonPanel.add( homebutton, "cell 1 0" );
        buttonPanel.add( locationTextField, "cell 2 0,grow" );

        splitPane = new JSplitPane();
        splitPane.setResizeWeight( 0.3 );
        getContentPane().add( splitPane, "cell 0 1,grow" );

        treeScrollPane = new JScrollPane();
        splitPane.setLeftComponent( treeScrollPane );

        documentTree = new DocumentTree( documentModel );
        documentTree.setCellRenderer( new TooltipsTreeCellRenderer() );
        ToolTipManager.sharedInstance()
                .registerComponent( documentTree );
        ToolTipManager.sharedInstance()
                .setInitialDelay( INITIAL_TOOLTIP_DELAY );
        ToolTipManager.sharedInstance()
                .setReshowDelay( 0 );
        documentTree.addTreeSelectionListener( new TreeSelectionListener()
        {
            @Override
            public void valueChanged( final TreeSelectionEvent tse )
            {
                TreePath newLeadSelectionPath = tse.getNewLeadSelectionPath();
                if ( newLeadSelectionPath != null
                     && !newLeadSelectionPath.equals( currentSelectionPath ) )
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) newLeadSelectionPath.getLastPathComponent();
                    FileWrapper file = (FileWrapper) node.getUserObject();
                    showFile( file, true );
                }
            }
        } );
        treeScrollPane.setViewportView( documentTree );

        fileEditorPane = new JEditorPane();
        fileEditorPane.setContentType( "text/html" );
        fileEditorPane.setEditable( false );
        fileEditorPane.addHyperlinkListener( new HandleHyperlinkUpdate() );
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
        int pageToShow = pageIndex - 1;
        if ( pageToShow >= 0 )
        {
            showFile( pageList.get( pageToShow ), false );
        }
    }

    private void actionForward()
    {
        int pageIndex = pageList.indexOf( currentFile );
        if ( pageIndex != -1 )
        {
            int pageToShow = pageIndex + 1;
            if ( pageList.size() > pageToShow )
            {
                showFile( pageList.get( pageToShow ), false );
            }
        }
    }

    private void actionGo()
    {
        String location = locationTextField.getText()
                .trim();
        if ( location.startsWith( FILE_URI_SCHEMA )
             && location.length() > FILE_URI_SCHEMA.length() )
        {
            location = location.substring( FILE_URI_SCHEMA.length() );
            locationTextField.setText( location );
        }
        File file = FileUtils.getFile( location );
        if ( !file.exists() )
        {
            showError( "File doesn't exist: " + file.getAbsolutePath() );
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

    private void showFile( final File file, final boolean addIt )
    {
        locationTextField.setText( file.getAbsolutePath() );
        refreshReplacements();
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
                    String href = getFileLocation( parent, line );

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
            if ( paths.containsKey( file ) )
            {
                currentSelectionPath = new TreePath( paths.get( file ) );
                documentTree.setSelectionPath( currentSelectionPath );
            }
        }
        catch ( IOException e )
        {
            showError( "Error in file handling: " + e );
        }
    }

    private void refreshDocumentTree()
    {
        paths.clear();
        FileWrapper root = new FileWrapper( this.locationTextField.getText() );
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode( root );
        Object[] rootPath = new Object[] { rootNode };
        paths.put( root, rootPath );
        addFileWrapperChildren( rootNode, rootPath );
        documentModel.setRoot( rootNode );
    }

    private void addFileWrapperChildren(
            final DefaultMutableTreeNode parentNode, final Object... parentPath )
    {
        FileWrapper parent = (FileWrapper) parentNode.getUserObject();
        for ( FileWrapper child : getChildren( parent ) )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( child );
            parentNode.add( node );
            Object[] path = Arrays.copyOf( parentPath, parentPath.length + 1 );
            path[parentPath.length] = node;
            paths.put( child, path );
            addFileWrapperChildren( node, path );
        }
    }

    private Collection<FileWrapper> getChildren(
            final se.nawroth.asciidoc.browser.FileWrapper parent )
    {
        List<FileWrapper> children = new ArrayList<FileWrapper>();
        String directory = parent.getParent();
        LineIterator lines;
        try
        {
            lines = FileUtils.lineIterator( parent, "UTF-8" );
            while ( lines.hasNext() )
            {
                String line = lines.next();
                if ( line.startsWith( LINK_LINE_START ) )
                {
                    String href = getFileLocation( directory, line );
                    FileWrapper fileWrapper = new FileWrapper( href );
                    if ( fileWrapper.exists() )
                    {
                        children.add( fileWrapper );
                    }
                    else
                    {
                        System.out.println( href );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return children;
    }

    private void refreshReplacements()
    {
        replacements.clear();
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
    }

    private String getFileLocation( final String parent, final String line )
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

    private class HandleHyperlinkUpdate implements HyperlinkListener
    {
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
