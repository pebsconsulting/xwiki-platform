/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.internal.macro;

import java.io.StringWriter;
import java.util.List;

import org.jmock.Mock;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.internal.macro.include.IncludeMacro;
import org.xwiki.rendering.internal.transformation.MacroTransformation;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.include.IncludeMacroParameters;
import org.xwiki.rendering.macro.include.IncludeMacroParameters.Context;
import org.xwiki.rendering.parser.Syntax;
import org.xwiki.rendering.parser.SyntaxType;
import org.xwiki.rendering.scaffolding.AbstractXWikiRenderingTestCase;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.velocity.VelocityManager;

/**
 * Unit tests for {@link IncludeMacro}.
 * 
 * @version $Id$
 * @since 1.5M2
 */
public class IncludeMacroTest extends AbstractXWikiRenderingTestCase
{
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();
        
        // Use a mock implementation for the Velocity Manager since we don't want to add a (cyclic)
        // dependency on XWiki Core (where DefaultVelocityManager is currently located).
        getComponentManager().registerComponent(MockVelocityManager.getComponentDescriptor());
    }

    public void testIncludeMacroWithNewContext() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [velocity] [] [$myvar]\n"
            + "beginParagraph\n"
            + "onWord [hello]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [velocity] [] [$myvar]\n"
            + "endDocument";

        // Since it's not in the same context, we verify that a Velocity variable set in the including page is not
        // seen in the included page.
        VelocityManager velocityManager = getComponentManager().lookup(VelocityManager.class);
        StringWriter writer = new StringWriter();
        velocityManager.getVelocityEngine().evaluate(velocityManager.getVelocityContext(), writer, "template",
            "#set ($myvar = 'hello')");

        IncludeMacro macro = (IncludeMacro) getComponentManager().lookup(Macro.class, "include");
        Mock mockDocumentAccessBridge = mock(DocumentAccessBridge.class);
        mockDocumentAccessBridge.expects(once()).method("isDocumentViewable").will(returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentContent").will(
            returnValue("{{velocity}}$myvar{{/velocity}}"));
        mockDocumentAccessBridge.expects(once()).method("getDocumentSyntaxId").will(
            returnValue(new Syntax(SyntaxType.XWIKI, "2.0").toIdString()));
        macro.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());
        mockDocumentAccessBridge.stubs().method("pushDocumentInContext");
        mockDocumentAccessBridge.stubs().method("popDocumentFromContext");

        IncludeMacroParameters parameters = new IncludeMacroParameters();
        parameters.setDocument("wiki:Space.Page");
        parameters.setContext(Context.NEW);

        // Create a Macro transformation context with the Macro transformation object defined so that the include
        // macro can transform included page which is using a new context.
        MacroTransformation macroTransformation = 
            (MacroTransformation) getComponentManager().lookup(Transformation.class, "macro");
        MacroTransformationContext context = new MacroTransformationContext();
        context.setMacroTransformation(macroTransformation);
        
        List<Block> blocks = macro.execute(parameters, null, context);

        assertBlocks(expected, blocks);
    }
    
    public void testIncludeMacroWithCurrentContext() throws Exception
    {
        String expected = "beginDocument\n"
            + "onMacroStandalone [someMacro] []\n"
            + "endDocument";

        IncludeMacro macro = (IncludeMacro) getComponentManager().lookup(Macro.class, "include");
        Mock mockDocumentAccessBridge = mock(DocumentAccessBridge.class);
        mockDocumentAccessBridge.expects(once()).method("isDocumentViewable").will(returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentContent").will(returnValue("{{someMacro/}}"));
        mockDocumentAccessBridge.expects(once()).method("getDocumentSyntaxId").will(
            returnValue(new Syntax(SyntaxType.XWIKI, "2.0").toIdString()));
        macro.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        IncludeMacroParameters parameters = new IncludeMacroParameters();
        parameters.setDocument("wiki:Space.Page");
        parameters.setContext(Context.CURRENT);

        List<Block> blocks = macro.execute(parameters, null, new MacroTransformationContext());

        assertBlocks(expected, blocks);
    }
    
    public void testIncludeMacroWithNoDocumentSpecified() throws Exception
    {
        IncludeMacro macro = (IncludeMacro) getComponentManager().lookup(Macro.class, "include");
        IncludeMacroParameters parameters = new IncludeMacroParameters();

        try {
            macro.execute(parameters, null, new MacroTransformationContext());
            fail("An exception should have been thrown");
        } catch (MacroExecutionException expected) {
            assertEquals("You must specify a 'document' parameter pointing to the document to include.",
                expected.getMessage());
        }
    }
}
