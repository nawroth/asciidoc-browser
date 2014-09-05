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
    private JCheckBox jythonCheckbox;

    /**
     * Create the dialog.
     */
    public SettingsDialog()
    {
        setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        setIconImage( Toolkit.getDefaultToolkit().getImage( SettingsDialog.class.getResource( OPTIONS_ICON ) ) );
        setBounds( 100, 100, 698, 416 );
        getContentPane().setLayout( new MigLayout( "", "[698px]", "[236px,grow][35px]" ) );
        contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        getContentPane().add( contentPanel, "cell 0 0,grow" );
        contentPanel.setLayout( new MigLayout( "", "[106px,grow,fill]", "[21px][][][][][grow,fill]" ) );
        {
            JLabel lblHomeLocation = new JLabel( "Home Location:" );
            contentPanel.add( lblHomeLocation, "cell 0 0,alignx left,aligny center" );
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
            contentPanel.add( homeLocationTextField, "cell 0 1,alignx left,aligny center" );
            homeLocationTextField.setColumns( 10 );
        }
        {
            jythonCheckbox = new JCheckBox( "Use Jython (experimental)" );
            contentPanel.add( jythonCheckbox, "cell 0 2" );
        }
        {
            JLabel lblMaxFilepathLength = new JLabel( "Max filepath length to show:" );
            contentPanel.add( lblMaxFilepathLength, "flowx,cell 0 3,alignx left" );
        }
        {
            JLabel lblConfigration = new JLabel( "AsciiDoc Configuration" );
            contentPanel.add( lblConfigration, "cell 0 4,alignx left,aligny center" );
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
            JPanel buttonPane = new JPanel();
            getContentPane().add( buttonPane, "cell 0 1,alignx right,aligny bottom" );
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
                buttonPane.setLayout( new MigLayout( "", "[54px][81px]", "[25px]" ) );
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
