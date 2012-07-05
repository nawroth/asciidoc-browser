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
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.JTextField;
import javax.swing.JEditorPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SettingsDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTextField homeLocationTextField;
    private JEditorPane replacementsEditorPane;

    /**
     * Launch the application.
     */
    public static void main( String[] args )
    {
        try
        {
            SettingsDialog dialog = new SettingsDialog();
            dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
            dialog.setVisible( true );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public SettingsDialog()
    {
        setBounds( 100, 100, 700, 300 );
        getContentPane().setLayout( new BorderLayout() );
        contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        getContentPane().add( contentPanel, BorderLayout.CENTER );
        contentPanel.setLayout( new FormLayout( new ColumnSpec[] {
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode( "default:grow" ), }, new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode( "default:grow" ), } ) );
        {
            JLabel lblHomeLocation = new JLabel( "Home location:" );
            contentPanel.add( lblHomeLocation, "2, 2" );
        }
        {
            homeLocationTextField = new JTextField();
            contentPanel.add( homeLocationTextField, "2, 4, fill, default" );
            homeLocationTextField.setColumns( 10 );
        }
        {
            JLabel lblReplacements = new JLabel( "Include Replacements:" );
            lblReplacements.setToolTipText( "Replace inside include statements only." );
            contentPanel.add( lblReplacements, "2, 6" );
        }
        {
            replacementsEditorPane = new JEditorPane();
            contentPanel.add( replacementsEditorPane, "2, 10, fill, fill" );
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
            getContentPane().add( buttonPane, BorderLayout.SOUTH );
            {
                JButton okButton = new JButton( "OK" );
                okButton.addActionListener( new ActionListener()
                {
                    public void actionPerformed( ActionEvent e )
                    {
                        actionOk();
                    }
                } );
                okButton.setActionCommand( "OK" );
                buttonPane.add( okButton );
                getRootPane().setDefaultButton( okButton );
            }
            {
                JButton cancelButton = new JButton( "Cancel" );
                cancelButton.addActionListener( new ActionListener()
                {
                    public void actionPerformed( ActionEvent e )
                    {
                        actionCancel();
                    }
                } );
                cancelButton.setActionCommand( "Cancel" );
                buttonPane.add( cancelButton );
            }
        }
        loadSettings();
    }

    private void loadSettings()
    {
        homeLocationTextField.setText( Settings.getHome() );
        replacementsEditorPane.setText( Settings.getReplacements() );
    }

    private void actionOk()
    {
        Settings.setHome( homeLocationTextField.getText() );
        Settings.setReplacements( replacementsEditorPane.getText() );
        setVisible( false );
    }

    private void actionCancel()
    {
        setVisible( false );
        loadSettings();
    }
}
