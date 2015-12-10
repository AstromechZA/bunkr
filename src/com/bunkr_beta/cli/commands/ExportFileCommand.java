package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.IFFTraversalTarget;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-08
 */
public class ExportFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_DESTINATION_FILE = "destination";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("read or export a file from the archive");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("source path in the archive");
        target.addArgument("destination")
                .dest(ARG_DESTINATION_FILE)
                .type(Arguments.fileType().acceptSystemIn())
                .help("file to export to or - for stdout");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
            IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));
            if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString(ARG_PATH));

            FileInventoryItem targetFile = (FileInventoryItem) target;

            OutputStream contentOutputStream;
            File inputFile = args.get(ARG_DESTINATION_FILE);
            if (inputFile.getPath().equals("-"))
                contentOutputStream = System.out;
            else
                contentOutputStream = new FileOutputStream(inputFile);

            try
            {
                try (MultilayeredInputStream ms = new MultilayeredInputStream(aic, targetFile))
                {
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = ms.read(buffer)) != -1)
                    {
                        contentOutputStream.write(buffer, 0, n);
                    }
                }
            }
            finally
            {
                contentOutputStream.close();
            }
        }
        catch (IllegalPathException | TraversalException e)
        {
            throw new CLIException(e);
        }
        catch (CryptoException e)
        {
            throw new CLIException("Decryption failed: %s", e.getMessage());
        }
    }
}