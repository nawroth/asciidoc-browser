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
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class SettingsDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTextField homeLocationTextField;
    private JEditorPane replacementsEditorPane;
    private JTextField excludeAtStartTextField;
    private JSpinner pathLengthSpinner;

    /**
     * Create the dialog.
     */
    public SettingsDialog()
    {
        setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        setIconImage( Toolkit.getDefaultToolkit()
                .getImage(
                        SettingsDialog.class.getResource( "/org/freedesktop/tango/16x16/categories/preferences-system.png" ) ) );
        setBounds( 100, 100, 698, 416 );
        getContentPane().setLayout(
                new MigLayout( "", "[698px]", "[236px,grow][35px]" ) );
        contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        getContentPane().add( contentPanel, "cell 0 0,grow" );
        contentPanel.setLayout( new MigLayout( "", "[106px,grow,fill]",
                "[21px][][][][][][grow,fill]" ) );
        {
            JLabel lblHomeLocation = new JLabel( "Home Location:" );
            contentPanel.add( lblHomeLocation,
                    "cell 0 0,alignx left,aligny center" );
        }
        {
            homeLocationTextField = new JTextField();
            contentPanel.add( homeLocationTextField,
                    "cell 0 1,alignx left,aligny center" );
            homeLocationTextField.setColumns( 10 );
        }
        {
            JLabel lblExcludeAtStart = new JLabel(
                    "Exclude at start of location:" );
            contentPanel.add( lblExcludeAtStart, "cell 0 2" );
        }
        {
            excludeAtStartTextField = new JTextField();
            contentPanel.add( excludeAtStartTextField, "flowx,cell 0 3" );
            excludeAtStartTextField.setColumns( 10 );
        }
        {
            JLabel lblMaxFilepathLength = new JLabel(
                    "Max filepath length to show:" );
            contentPanel.add( lblMaxFilepathLength,
                    "flowx,cell 0 4,alignx left" );
        }
        {
            JLabel lblReplacements = new JLabel( "Include Path Substitutions" );
            lblReplacements.setToolTipText( "Substitute inside include statements only." );
            contentPanel.add( lblReplacements,
                    "cell 0 5,alignx left,aligny center" );
        }
        {
            replacementsEditorPane = new JEditorPane();
            contentPanel.add( replacementsEditorPane, "cell 0 6,grow" );
        }
        {
            pathLengthSpinner = new JSpinner();
            pathLengthSpinner.setValue( Settings.getMaxFilepathLength() );
            contentPanel.add( pathLengthSpinner, "cell 0 4,alignx left" );
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
        replacementsEditorPane.setText( Settings.getReplacements() );
        excludeAtStartTextField.setText( Settings.getExcludeStart() );
        pathLengthSpinner.setValue( Settings.getMaxFilepathLength() );
    }

    private void actionOk()
    {
        setVisible( false );
        Settings.setHome( homeLocationTextField.getText() );
        Settings.setReplacements( replacementsEditorPane.getText() );
        Settings.setExcludeStart( excludeAtStartTextField.getText() );
        Settings.setMaxFilepathLength( (Integer) pathLengthSpinner.getValue() );
    }

    private void actionCancel()
    {
        setVisible( false );
        loadSettings();
    }
}
