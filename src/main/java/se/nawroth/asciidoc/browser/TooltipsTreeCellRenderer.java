package se.nawroth.asciidoc.browser;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

class TooltipsTreeCellRenderer extends DefaultTreeCellRenderer
{
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTreeCellRendererComponent( final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus )
    {
        setToolTipText( "foobar" + row );
        return super.getTreeCellRendererComponent( tree, value, sel, expanded,
                leaf, row, hasFocus );
    }
}
