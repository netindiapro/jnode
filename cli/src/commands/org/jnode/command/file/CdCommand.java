/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.command.file;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * The CdCommand class changes the current directory as given by the "user.dir" property.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 * @author crawley@jnode.org
 */
public class CdCommand extends AbstractCommand {

    private static final String help_dir = "the directory to change to";
    private static final String help_super = "Change the current directory";
    private static final String err_home = "user.home is not set";
    private static final String err_dir_invalid = "%s is not a valid directory%n";
    
    private final FileArgument argDir = new FileArgument(
            "directory", Argument.OPTIONAL | Argument.EXISTING, help_dir);

    public CdCommand() {
        super(help_super);
        registerArguments(argDir);
    }

    public static void main(String[] args) throws Exception {
        new CdCommand().execute(args);
    }

    public void execute() 
        throws IOException {
        File dir = argDir.getValue();
        PrintWriter err = getError().getPrintWriter();
        if (dir == null) {
            // If no directory argument was given, change to the "user.home" directory.
            String home = System.getProperty("user.home");
            if (home == null || home.isEmpty()) {
                err.println(err_home);
                exit(1);
            }
            dir = new File(home);
        }
        if (dir.exists() && dir.isDirectory()) {
            System.setProperty("user.dir", dir.getAbsoluteFile().getCanonicalPath());
        } else {
            err.format(err_dir_invalid, dir);
            exit(1);
        }
    }

}