/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.cli.CLI;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.descriptor.ScryptDescriptor;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.usersec.UserSecurityProvider;

import java.util.Iterator;

/**
 * Created At: 2016-01-21
 */
public class ShowSecurityCommand implements ICLICommand
{
    private static final String ARG_AUDIT = "audit";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("show the security settings of the archive");

        target.addArgument("--audit")
                .dest(ARG_AUDIT)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .type(Boolean.class)
                .help("Scan the archive for files with out of date security settings");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext archive = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        System.out.println(String.format("Archive Security: %s", archive.getDescriptor().getIdentifier()));
        switch (archive.getDescriptor().getIdentifier())
        {
            case PlaintextDescriptor.IDENTIFIER:
            {
                System.out.println("Inventory Encryption: None");
                break;
            }
            case PBKDF2Descriptor.IDENTIFIER:
            {
                PBKDF2Descriptor descriptor = (PBKDF2Descriptor) archive.getDescriptor();
                System.out.println(String.format("PBKDF2 Iterations: %s", descriptor.pbkdf2Iterations));
                System.out.println(String.format("PBKDF2 Salt Length: %s bytes", descriptor.pbkdf2Salt.length));
                System.out.println(String.format("Inventory Encryption: %s", descriptor.encryptionAlgorithm));
                break;
            }
            case ScryptDescriptor.IDENTIFIER:
            {
                ScryptDescriptor descriptor = (ScryptDescriptor) archive.getDescriptor();
                System.out.println(String.format("Scrypt Cost: %s", descriptor.scryptN));
                System.out.println(String.format("Scrypt Parallelization: %s", descriptor.scryptP));
                System.out.println(String.format("Scrypt Block Size: %s", descriptor.scryptR));
                System.out.println(String.format("Scrypt Salt Length: %s bytes", descriptor.scryptSalt.length));
                System.out.println(String.format("Inventory Encryption: %s", descriptor.encryptionAlgorithm));
                break;
            }
        }

        System.out.println(String.format("File Encryption: %s", archive.getInventory().getDefaultEncryption()));

        if (args.getBoolean(ARG_AUDIT))
        {
            scanForOutOfDateEncryption(archive);
        }
    }

    public static void scanForOutOfDateEncryption(ArchiveInfoContext archive)
    {
        System.out.println("Scanning archive for files with out of date encryption settings...");

        int badCount = 0;
        Iterator<FileInventoryItem> fileit = archive.getInventory().getIterator();
        while(fileit.hasNext())
        {
            FileInventoryItem current = fileit.next();
            if (current.getEncryptionAlgorithm() != archive.getInventory().getDefaultEncryption())
            {
                String absolutePath = current.getAbsolutePath();
                Algorithms.Encryption alg = current.getEncryptionAlgorithm();
                System.out.println(String.format("%s has incorrect encryption %s", absolutePath, alg));
                badCount++;
            }
        }
        if (badCount > 0)
        {
            System.out.println(String.format("%d files in the archive have out of date encryption settings. " +
                                                     "Use the 're-encrypt' CLI command to fix these.", badCount));
        }
        else
        {
            System.out.println("All files have up to date encryption settings.");
        }
    }
}
