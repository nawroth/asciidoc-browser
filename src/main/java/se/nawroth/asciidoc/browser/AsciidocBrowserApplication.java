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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import org.fit.cssbox.swingbox.BrowserPane;

import bsh.util.JConsole;

@SuppressWarnings( "serial" )
public class AsciidocBrowserApplication extends JFrame
{
    private static final int INITIAL_TOOLTIP_DELAY = 500;

    private static final String FILE_URI_SCHEMA = "file://";

    private static final String LINK_LINE_START = "include::";

    private static JDialog settingsDialog;

    private final JButton backButton, forwardButton, refreshButton, homebutton;

    private final JTextField locationTextField;

    private final JEditorPane sourceEditorPane;

    private final List<File> pageList = new ArrayList<File>();

    private File currentFile;
    private final JScrollPane treeScrollPane;
    private final JTree documentTree;
    private final JSplitPane treeSourceSplitPane;

    private final Map<CharSequence, CharSequence> replacements = new HashMap<CharSequence, CharSequence>();

    private final DefaultTreeModel documentModel = new DefaultTreeModel( null );

    private final Map<FileWrapper, Object[]> paths = new HashMap<FileWrapper, Object[]>();

    private TreePath currentSelectionPath;
    private final JTabbedPane documentTabbedPane;
    private final JScrollPane previewScrollPane;

    private final BrowserPane browserPane;

    private final AsciidocExecutor executor;
    private final JSplitPane sourceLogSplitPane;

    private final JConsole console;

    public AsciidocBrowserApplication( final String[] args )
    {
        super( "Asciidoc Browser" );
        setIconImage( Icons.APPLICATION.image() );

        setSize( 1200, 1024 );

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
        backButton.setIcon( Icons.BACK.icon() );
        backButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                actionBack();
            }
        } );
        buttonPanel.setLayout( new MigLayout( "", "[1px][][][][]", "[1px]" ) );

        JButton btnOptionsbutton = new JButton( "" );
        btnOptionsbutton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                settingsDialog.setVisible( true );
            }
        } );
        btnOptionsbutton.setIcon( Icons.OPTIONS.icon() );
        buttonPanel.add( btnOptionsbutton, "flowx,cell 0 0" );
        backButton.setEnabled( false );
        buttonPanel.add( backButton, "cell 0 0,grow" );
        forwardButton = new JButton( "" );
        forwardButton.setIcon( Icons.FORWARD.icon() );
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
        getContentPane().setLayout( new MigLayout( "", "[793.00px,grow]", "[44px][930px]" ) );
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
        locationTextField.setTransferHandler( new TextFieldTransferHandler( locationTextField.getTransferHandler(),
                new Runnable()
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

        refreshButton = new JButton( "" );
        refreshButton.setToolTipText( "Refresh" );
        refreshButton.setEnabled( false );
        refreshButton.setIcon( Icons.REFRESH.icon() );
        refreshButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( final ActionEvent e )
            {
                actionGo();
                refreshPreview();
            }
        } );
        buttonPanel.add( refreshButton, "cell 1 0" );

        homebutton.setIcon( Icons.HOME.icon() );
        buttonPanel.add( homebutton, "cell 2 0" );
        buttonPanel.add( locationTextField, "cell 3 0,grow" );

        treeSourceSplitPane = new JSplitPane();
        treeSourceSplitPane.setResizeWeight( 0.3 );
        getContentPane().add( treeSourceSplitPane, "cell 0 1,grow" );

        treeScrollPane = new JScrollPane();
        treeScrollPane.setMinimumSize( new Dimension( 200, 200 ) );
        treeSourceSplitPane.setLeftComponent( treeScrollPane );

        documentTree = new DocumentTree( documentModel );
        documentTree.setCellRenderer( new TooltipsTreeCellRenderer() );
        ToolTipManager.sharedInstance().registerComponent( documentTree );
        ToolTipManager.sharedInstance().setInitialDelay( INITIAL_TOOLTIP_DELAY );
        ToolTipManager.sharedInstance().setReshowDelay( 0 );
        documentTree.addTreeSelectionListener( new TreeSelectionListener()
        {
            @Override
            public void valueChanged( final TreeSelectionEvent tse )
            {
                TreePath newLeadSelectionPath = tse.getNewLeadSelectionPath();
                if ( newLeadSelectionPath != null && !newLeadSelectionPath.equals( currentSelectionPath ) )
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) newLeadSelectionPath.getLastPathComponent();
                    FileWrapper file = (FileWrapper) node.getUserObject();
                    showFile( file, true );
                    refreshPreview();
                }
            }
        } );
        treeScrollPane.setViewportView( documentTree );

        sourceEditorPane = new JEditorPane();
        sourceEditorPane.setContentType( "text/html" );
        sourceEditorPane.setEditable( false );
        sourceEditorPane.addHyperlinkListener( new HandleHyperlinkUpdate() );
        JScrollPane fileScrollPane = new JScrollPane( sourceEditorPane );
        fileScrollPane.setMinimumSize( new Dimension( 600, 600 ) );

        documentTabbedPane = new JTabbedPane( SwingConstants.BOTTOM );
        documentTabbedPane.addChangeListener( new ChangeListener()
        {
            @Override
            public void stateChanged( final ChangeEvent ce )
            {
                refreshPreview();
            }
        } );
        sourceLogSplitPane = new JSplitPane();
        sourceLogSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );
        treeSourceSplitPane.setRightComponent( sourceLogSplitPane );
        sourceLogSplitPane.setTopComponent( documentTabbedPane );
        documentTabbedPane.add( fileScrollPane );
        documentTabbedPane.setTitleAt( 0, "Source" );

        browserPane = new BrowserPane();

        previewScrollPane = new JScrollPane( browserPane );
        documentTabbedPane.addTab( "Preview", null, previewScrollPane, null );

        console = new JConsole();
        System.setErr( console.getErr() );
        System.setOut( console.getOut() );
        sourceLogSplitPane.setBottomComponent( console );

        executor = new AsciidocExecutor();
    }

    private void refreshPreview()
    {
        Component selectedComponent = documentTabbedPane.getSelectedComponent();
        if ( selectedComponent != previewScrollPane )
        {
            return;
        }
        if ( currentFile != null && currentFile.exists() )
        {
            console.println( "Parsing AsciiDoc from '" + currentFile.getName() + "'." );
            File targetFile = executor.generate( currentFile );
            try
            {
                console.println( "Loading the output from '" + currentFile.getName() + "' into the preview browser." );
                browserPane.setPage( targetFile.toURI().toURL() );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
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
        refreshPreview();
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
        refreshPreview();
    }

    private void actionGo()
    {
        String location = locationTextField.getText().trim();
        if ( location.startsWith( FILE_URI_SCHEMA ) && location.length() > FILE_URI_SCHEMA.length() )
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
        refreshButton.setEnabled( true );
    }

    private void showFile( final File file, final boolean addIt )
    {
        locationTextField.setText( file.getAbsolutePath() );
        refreshReplacements();
        try
        {
            StringBuilder sb = new StringBuilder( 10 * 1024 );
            sb.append( "<html><head><title>" + file.getName()
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

                    sb.append( "<a href=\"" ).append( href ).append( "\">" ).append( line ).append( "</a>" );
                }
                else
                {
                    sb.append( line );
                }
                sb.append( "</pre>" );
            }
            sb.append( "</body></html>" );
            lines.close();

            sourceEditorPane.setText( sb.toString() );
            sourceEditorPane.setCaretPosition( 0 );
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
        FileWrapper.setSmartExclude( this.locationTextField.getText() );
        FileWrapper root = new FileWrapper( this.locationTextField.getText() );
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode( root );
        Object[] rootPath = new Object[] { rootNode };
        paths.put( root, rootPath );
        addFileWrapperChildren( rootNode, rootPath );
        documentModel.setRoot( rootNode );
    }

    private void addFileWrapperChildren( final DefaultMutableTreeNode parentNode, final Object... parentPath )
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

    private Collection<FileWrapper> getChildren( final se.nawroth.asciidoc.browser.FileWrapper parent )
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
                        // System.out.println( href );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return children;
    }

    private void refreshReplacements()
    {
        Properties properties = new Properties();
        try
        {
            properties.load( new StringReader( Settings.getConfiguration() ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        replacements.clear();
        for ( Entry<Object, Object> entry : properties.entrySet() )
        {
            if ( entry.getKey() instanceof String && entry.getValue() instanceof String )
            {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if ( !key.startsWith( "[" ) && !value.trim().isEmpty() )
                {
                    replacements.put( "{" + key + "}", value );
                }
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
            hrefWithReplacements = hrefWithReplacements.replace( entry.getKey(), entry.getValue() );
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
        JOptionPane.showMessageDialog( this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE );
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
                    AsciidocBrowserApplication browser = new AsciidocBrowserApplication( args );
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
