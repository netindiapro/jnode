/**
 * $Id$
 */
package org.jnode.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;

/**
 * <description>
 * 
 * @author epr
 */
public abstract class AbstractAsmConstBuilder {

	private File destFile;
	private URL classesURL;
	private ArrayList classes = new ArrayList();

	/**
	 * Execute this task
	 * 
	 * @throws BuildException
	 */
	public void execute() throws BuildException {
		try {
			doExecute();
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw new BuildException(ex);
		}
	}

	/**
	 * Execute this task
	 * 
	 * @throws BuildException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private void doExecute() throws BuildException, ClassNotFoundException, IllegalAccessException, IOException {

		final VmArchitecture arch = getArchitecture();
		final int slotSize = arch.getReferenceSize();
		VmSystemClassLoader cl = new VmSystemClassLoader(classesURL, arch);
		VmType.initializeForBootImage(cl);

		FileWriter fw = new FileWriter(destFile);
		PrintWriter out = new PrintWriter(fw);
		out.println("; " + destFile.getPath());
		out.println("; THIS file has been generated automatically on " + new Date());
		out.println();

		for (Iterator j = classes.iterator(); j.hasNext();) {
			ClassName cn = (ClassName) j.next();
			out.println("; Constants for " + cn.getClassName());

			if (cn.isStatic()) {
				Class cls = Class.forName(cn.getClassName());
				Field fields[] = cls.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field f = fields[i];
					if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
						Object value = f.get(null);
						if (value instanceof Number) {
							String cname = cls.getName();
							int idx = cname.lastIndexOf('.');
							if (idx > 0) {
								cname = cname.substring(idx + 1);
							}
							String name = cname + "_" + f.getName();
							out.println(name + " equ " + value);
						}
					}
				}
				out.println();
			} else {

				out.println("; VmClass: " + cn.getClassName());
				VmType vmClass = cl.loadClass(cn.getClassName(), true);
				vmClass.link();
				String cname = vmClass.getName().replace('/', '.');
				int idx = cname.lastIndexOf('.');
				if (idx > 0) {
					cname = cname.substring(idx + 1);
				}
				int cnt = vmClass.getNoDeclaredFields();
				for (int i = 0; i < cnt; i++) {
					final VmField f = vmClass.getDeclaredField(i);
					if (!f.isStatic()) {
						final VmInstanceField instF = (VmInstanceField) f;
						String name = cname + "_" + f.getName().toUpperCase() + "_OFFSET";
						out.println(name + " equ " + (instF.getOffset() / slotSize));
					}
				}
				// The size
				if (vmClass instanceof VmNormalClass) {
					final VmNormalClass cls = (VmNormalClass) vmClass;
					out.println(cname + "_SIZE equ " + cls.getObjectSize());
				}
				//
				out.println();
			}
		}

		out.flush();
		fw.flush();
		out.close();
		fw.close();
	}

	public void addClass(ClassName cn) {
		classes.add(cn);
	}

	/**
	 * Returns the destFile.
	 * 
	 * @return File
	 */
	public File getDestFile() {
		return destFile;
	}

	/**
	 * Sets the destFile.
	 * 
	 * @param destFile
	 *            The destFile to set
	 */
	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}

	public static class ClassName {
		private String className;
		private boolean _static = false;

		public ClassName() {
		}

		/**
		 * Returns the className.
		 * 
		 * @return String
		 */
		public String getClassName() {
			return className;
		}

		/**
		 * Sets the className.
		 * 
		 * @param className
		 *            The className to set
		 */
		public void setClassName(String className) {
			this.className = className;
		}
		/**
		 * Returns the _static.
		 * 
		 * @return boolean
		 */
		public boolean isStatic() {
			return _static;
		}

		/**
		 * Sets the _static.
		 * 
		 * @param _static
		 *            The _static to set
		 */
		public void setStatic(boolean _static) {
			this._static = _static;
		}

	}

	/**
	 * Returns the classesURL.
	 * 
	 * @return URL
	 */
	public URL getClassesURL() {
		return classesURL;
	}

	/**
	 * Sets the classesURL.
	 * 
	 * @param classesURL
	 *            The classesURL to set
	 */
	public void setClassesURL(URL classesURL) {
		this.classesURL = classesURL;
	}
	
	protected abstract VmArchitecture getArchitecture();
}
