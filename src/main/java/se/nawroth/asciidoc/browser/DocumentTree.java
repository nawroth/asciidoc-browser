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

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

@SuppressWarnings( "serial" )
public class DocumentTree extends JTree
{
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
