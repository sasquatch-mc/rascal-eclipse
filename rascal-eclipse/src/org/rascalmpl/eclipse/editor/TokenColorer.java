/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.eclipse.library.vis.swt.SWTFontsAndColors;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.ITokenColorer;

public class TokenColorer implements ITokenColorer {
	private Boolean firstUse = true;

	private final Map<String,TextAttribute> map = new HashMap<String,TextAttribute>();

	public TokenColorer() {
		super();
		
		map.put(TreeAdapter.NORMAL, new TextAttribute(null, null, SWT.NONE));
		
		map.put(TreeAdapter.NONTERMINAL_LABEL, new TextAttribute(new Color(Display.getDefault(), 187, 181, 41), null, SWT.ITALIC));
		map.put(TreeAdapter.META_KEYWORD, new TextAttribute(new Color(Display.getDefault(), 204, 120, 50), null, SWT.BOLD));
		map.put(TreeAdapter.META_VARIABLE, new TextAttribute(new Color(Display.getDefault(), 152,118,170), null, SWT.ITALIC));
		map.put(TreeAdapter.META_AMBIGUITY,  new TextAttribute(new Color(Display.getDefault(), 186, 29, 29), null, SWT.BOLD));
		map.put(TreeAdapter.META_SKIPPED,  new TextAttribute(new Color(Display.getDefault(), 172, 172, 172), new Color(Display.getDefault(), 104, 58, 74), SWT.ITALIC)); //82, 141, 115
		map.put(TreeAdapter.TODO,new TextAttribute(new Color(Display.getDefault(), 98, 151, 85), null, SWT.BOLD));
		map.put(TreeAdapter.COMMENT,new TextAttribute(new Color(Display.getDefault(), 255, 198, 109), null, SWT.ITALIC));
		map.put(TreeAdapter.CONSTANT,new TextAttribute(new Color(Display.getDefault(), 98, 151, 85), null, SWT.NONE));
		map.put(TreeAdapter.VARIABLE,new TextAttribute(new Color(Display.getDefault(), 152,118,170), null, SWT.NONE));
		map.put(TreeAdapter.IDENTIFIER,new TextAttribute(new Color(Display.getDefault(), 187,202,219), null, SWT.NONE));
		map.put(TreeAdapter.QUOTE,new TextAttribute(new Color(Display.getDefault(), 255, 69, 0), new Color(Display.getDefault(), 32,178,170), SWT.NONE));
		map.put(TreeAdapter.TYPE,new TextAttribute(new Color(Display.getDefault(), 187,202,219), null, SWT.NONE));
		map.put(TreeAdapter.RESULT, new TextAttribute(new Color(Display.getDefault(), 0x74,0x8B,0x00), new Color(Display.getDefault(), 0xEC, 0xEC, 0xEC), SWT.ITALIC));
		map.put(TreeAdapter.STDOUT, new TextAttribute(new Color(Display.getDefault(), 0xB3,0xB3,0xB3), null, SWT.ITALIC));
		map.put(TreeAdapter.STDERR, new TextAttribute(new Color(Display.getDefault(), 0xAF,0x00,0x00), null, SWT.NONE));
		
	} 

	public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
		return seed;
	}

	public TextAttribute getColoring(IParseController controller, Object token) {
		if (firstUse) {
			firstUse = false;
			ISet contribs = TermLanguageRegistry.getInstance().getContributions(controller.getLanguage());
			if (contribs != null) {
				// check if there might be a category contribution?
				for (IValue contrib : contribs) {
					IConstructor node = (IConstructor) contrib;
					if (node.getName().equals("categories")) {
						extendDefaultCategories(node);
					}
				}
			}
		}
		return map.get(((Token) token).getCategory());
	}

	private void extendDefaultCategories(IConstructor categories) {
		IMap styleMap = (IMap)categories.get("styleMap");
		for (IValue category: styleMap) {
			String categoryName = ((IString)category).getValue();
			TextAttribute textStyle = translate((ISet)styleMap.get(category));
			map.put(categoryName, textStyle);
		}
	}

	private TextAttribute translate(ISet fontProperties) {
		int style = SWT.NONE;
		Color foreground = null;  
		Color background = null;  
		for (IValue fs : fontProperties) {
			String fsName = ((IConstructor)fs).getName();
			if (fsName.equals("bold")) {
				style |= SWT.BOLD;
			}
			else if (fsName.equals("italic")) {
				style |= SWT.ITALIC;
			}
			else if (fsName.equals("foregroundColor")) {
				int color = ((IInteger) ((IConstructor)fs).get("color")).intValue();
				foreground = SWTFontsAndColors.getRgbColor(Display.getCurrent(), color);
			}
			else if (fsName.equals("backgroundColor")) {
				int color = ((IInteger) ((IConstructor)fs).get("color")).intValue();
				background = SWTFontsAndColors.getRgbColor(Display.getCurrent(), color);
			}
			else {
				throw RuntimeExceptionFactory.illegalArgument(fs, null, null, "Font property " + fsName + " is not supported by IMP syntax highlighting.");
			}
		}
		return new TextAttribute(foreground, background, style);
	}
}
