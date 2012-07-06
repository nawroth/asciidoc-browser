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

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

public class DocumentTree extends JTree
{
    private static final long serialVersionUID = 1L;

    public DocumentTree( final TreeModel newModel )
    {
        super( newModel );
        initLooks();
    }

    public DocumentTree()
    {
        super();
        initLooks();
    }

    private void initLooks()
    {
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION );
        setBorder( UIManager.getBorder( "Tree.editorBorder" ) );
    }

    public DocumentTree( final TreeNode root )
    {
        super( root );
        initLooks();
    }
}