package org.bunkr.cli.commands;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.CLIException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.inventory.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-11
 *
 * A cli command for moving items.
 * eg:
 * mv /something /another
 *
 */
public class MvCommand implements ICLICommand
{
    public static final String ARG_FROMPATH = "fromPath";
    public static final String ARG_TOPATH = "toPath";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("move a file or folder to a different path");
        target.addArgument("from-path")
                .dest(ARG_FROMPATH)
                .type(String.class)
                .help("the original path of the item to move");
        target.addArgument("to-path")
                .dest(ARG_TOPATH)
                .type(String.class)
                .help("the new path of the item being moved");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        // build some useful path primitives
        String fromPathParent = InventoryPather.dirname(args.getString(ARG_FROMPATH));
        String toPathParent = InventoryPather.dirname(args.getString(ARG_TOPATH));
        String toPathName = InventoryPather.baseName(args.getString(ARG_TOPATH));

        // load up the archive
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        // find the container that has the source item
        IFFTraversalTarget fromParent = InventoryPather.traverse(aic.getInventory(), fromPathParent);
        IFFContainer fromContainer = (IFFContainer) fromParent;

        // find the container that is the destination
        IFFTraversalTarget toParent = InventoryPather.traverse(aic.getInventory(), toPathParent);
        if (toParent.isAFile()) throw new CLIException("Cannot move item to be a child of a file.");
        IFFContainer toContainer = (IFFContainer) toParent;

        // throw some exceptions if there are issues so far
        if (toContainer.hasFile(toPathName)) throw new CLIException("Destination folder already contains a file '%s'", toPathName);
        if (toContainer.hasFolder(toPathName)) throw new CLIException("Destination folder already contains a folder '%s'", toPathName);

        // find the target item that will be moved
        IFFTraversalTarget targetItem = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_FROMPATH));
        if (targetItem.isAFile())
        {
            FileInventoryItem targetFile = (FileInventoryItem) targetItem;
            targetFile.setName(toPathName);
            fromContainer.removeFile(targetFile);
            toContainer.addFile(targetFile);
            MetadataWriter.write(aic, usp);
            System.out.println(String.format("Moved file '%s' to '%s'", args.getString(ARG_FROMPATH), args.getString(ARG_TOPATH)));
        }
        else
        {
            FolderInventoryItem targetFolder = (FolderInventoryItem) targetItem;
            targetFolder.setName(toPathName);
            fromContainer.removeFolder(targetFolder);
            toContainer.addFolder(targetFolder);
            MetadataWriter.write(aic, usp);
            System.out.println(String.format("Moved folder '%s' to '%s'", args.getString(ARG_FROMPATH), args.getString(ARG_TOPATH)));
        }
    }
}
