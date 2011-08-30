/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Davy Landman - Davy.Landman@cwi.nl - CWI
 *******************************************************************************/
package org.rascalmpl.eclipse.library.vis;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.vis.swt.FigureExecutionEnvironment;
import org.rascalmpl.library.vis.util.vector.BoundingBox;

public class FigureLibrary {
	
	IValueFactory values;

	public FigureLibrary(IValueFactory values) {
		this.values = values;
	}
	
	public void renderActual(IString name, IConstructor fig, IEvaluatorContext ctx) {
		FigureViewer.open(name, fig, ctx);
	}

	public void renderSaveActual(final IConstructor cfig, final ISourceLocation loc, final IEvaluatorContext ctx) {
		renderSaveActual(cfig,null,null,loc,ctx);
	}
	
	public void renderSaveActual(final IConstructor cfig, final IInteger width, final IInteger height, final ISourceLocation loc, final IEvaluatorContext ctx) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				
				final Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
				final FigureExecutionEnvironment env = new FigureExecutionEnvironment(shell, cfig, ctx);
				shell.getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						try{
							OutputStream out =  ctx.getResolverRegistry().getOutputStream(loc.getURI(), false);
							env.writeScreenshot(out);
							out.close();
							
						} catch(IOException f){
							System.out.printf("Could not save figure " + f.getMessage() + "\n");
						} finally{
							//shell.close();
						}
					}
				});
				BoundingBox minViewSize = env.getMinViewingSize();
				int w,h;
				w = (int)minViewSize.getX();
				h = (int)minViewSize.getY();
				if(width != null){
					w = Math.max(w,width.intValue());
				}
				if(height != null){
					h = Math.max(h,height.intValue());
				}
				System.out.printf("Drawing in %d %d\n",w,h);
				Rectangle r = shell.computeTrim(0, 0, w + 10 , h+10 );
				shell.setBounds(r);
				env.setSize(w+5,h+5);
				shell.open();
			}
		});
		
	}
	

}
