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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings( "serial" )
public class SettingsDialog extends JDialog
{
    private static final String OPTIONS_ICON = "/org/freedesktop/tango/16x16/categories/preferences-system.png";
    private final JPanel contentPanel = new JPanel();
    private JTextField homeLocationTextField;
    private JEditorPane configEditorPane;
    private JSpinner pathLengthSpinner;

    /**
     * Create the dialog.
     */
    public SettingsDialog()
    {
        setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        setIconImage( Toolkit.getDefaultToolkit()
                .getImage(
                        SettingsDialog.class.getResource( OPTIONS_ICON ) ) );
        setBounds( 100, 100, 698, 416 );
        getContentPane().setLayout(
                new MigLayout( "", "[698px]", "[236px,grow][35px]" ) );
        contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        getContentPane().add( contentPanel, "cell 0 0,grow" );
        contentPanel.setLayout( new MigLayout( "", "[106px,grow,fill]",
                "[21px][][][][][grow,fill]" ) );
        {
            JLabel lblHomeLocation = new JLabel( "Home Location:" );
            contentPanel.add( lblHomeLocation,
                    "cell 0 0,alignx left,aligny center" );
        }
        {
            homeLocationTextField = new JTextField();
            homeLocationTextField.setTransferHandler( new TextFieldTransferHandler(
                    homeLocationTextField.getTransferHandler(), new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            homeLocationTextField.setText( "" );
                        }
                    }, null ) );
            contentPanel.add( homeLocationTextField,
                    "cell 0 1,alignx left,aligny center" );
            homeLocationTextField.setColumns( 10 );
        }
        {
            JLabel lblJython = new JLabel( "jython" );
            contentPanel.add( lblJython, "cell 0 2" );
        }
        {
            JLabel lblMaxFilepathLength = new JLabel(
                    "Max filepath length to show:" );
            contentPanel.add( lblMaxFilepathLength,
                    "flowx,cell 0 3,alignx left" );
        }
        {
            JLabel lblConfigration = new JLabel( "AsciiDoc Configuration" );
            contentPanel.add( lblConfigration,
                    "cell 0 4,alignx left,aligny center" );
        }
        {
            configEditorPane = new JEditorPane();
            contentPanel.add( configEditorPane, "cell 0 5,grow" );
        }
        {
            pathLengthSpinner = new JSpinner();
            pathLengthSpinner.setValue( Settings.getMaxFilepathLength() );
            contentPanel.add( pathLengthSpinner, "cell 0 3,alignx left" );
        }
        {
            JCheckBox jythonCheckbox = new JCheckBox(
                    "Use Jython (experimental):" );
            contentPanel.add( jythonCheckbox, "cell 0 3" );
        }
        {
            JPanel buttonPane = new JPanel();
            getContentPane().add( buttonPane,
                    "cell 0 1,alignx right,aligny bottom" );
            {
                JButton okButton = new JButton( "OK" );
                okButton.addActionListener( new ActionListener()
                {
                    @Override
                    public void actionPerformed( final ActionEvent e )
                    {
                        actionOk();
                    }
                } );
                buttonPane.setLayout( new MigLayout( "", "[54px][81px]",
                        "[25px]" ) );
                okButton.setActionCommand( "OK" );
                buttonPane.add( okButton, "cell 0 0,alignx right,aligny top" );
                getRootPane().setDefaultButton( okButton );
            }
            {
                JButton cancelButton = new JButton( "Cancel" );
                cancelButton.addActionListener( new ActionListener()
                {
                    @Override
                    public void actionPerformed( final ActionEvent e )
                    {
                        actionCancel();
                    }
                } );
                cancelButton.setActionCommand( "Cancel" );
                buttonPane.add( cancelButton, "cell 1 0,alignx left,aligny top" );
            }
        }
        loadSettings();
    }

    private void loadSettings()
    {
        homeLocationTextField.setText( Settings.getHome() );
        configEditorPane.setText( Settings.getConfiguration() );
        pathLengthSpinner.setValue( Settings.getMaxFilepathLength() );
    }

    private void actionOk()
    {
        setVisible( false );
        Settings.setHome( homeLocationTextField.getText() );
        Settings.setConfiguration( configEditorPane.getText() );
        Settings.setMaxFilepathLength( (Integer) pathLengthSpinner.getValue() );
    }

    private void actionCancel()
    {
        setVisible( false );
        loadSettings();
    }
}
