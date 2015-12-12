package com.bunkr_beta_tests.cli.commands;

import com.bunkr_beta.ArchiveBuilder;
import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.commands.MkdirCommand;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta_tests.XTemporaryFolder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bouncycastle.crypto.CryptoException;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class TestMkdirCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    public Inventory makeSampleInventory()
    {
        Inventory i = new Inventory(new ArrayList<>(), new ArrayList<>());
        FolderInventoryItem d1 = new FolderInventoryItem("d1");
        i.getFolders().add(d1);
        FolderInventoryItem d2 = new FolderInventoryItem("d2");
        i.getFolders().add(d2);
        FolderInventoryItem d3 = new FolderInventoryItem("d2.d3");
        d2.getFolders().add(d3);

        FileInventoryItem f1 = new FileInventoryItem("f1");
        i.getFiles().add(f1);
        FileInventoryItem f2 = new FileInventoryItem("f2");
        i.getFiles().add(f2);
        FileInventoryItem f3 = new FileInventoryItem("d2.f3");
        d2.getFiles().add(f3);

        return i;
    }

    public ArchiveInfoContext buildSampleArchive() throws IOException, CryptoException
    {
        File archivePath = folder.newFile();
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(archivePath, new Descriptor(null, null), new PasswordProvider());

        FolderInventoryItem d1 = new FolderInventoryItem("d1");
        context.getInventory().getFolders().add(d1);
        FolderInventoryItem d2 = new FolderInventoryItem("d2");
        context.getInventory().getFolders().add(d2);
        FolderInventoryItem d3 = new FolderInventoryItem("d2.d3");
        d2.getFolders().add(d3);

        FileInventoryItem f1 = new FileInventoryItem("f1");
        context.getInventory().getFiles().add(f1);
        FileInventoryItem f2 = new FileInventoryItem("f2");
        context.getInventory().getFiles().add(f2);
        FileInventoryItem f3 = new FileInventoryItem("d2.f3");
        d2.getFiles().add(f3);

        MetadataWriter.write(context, new PasswordProvider());

        return context;
    }

    @Test
    public void testBuildParser()
    {
        new MkdirCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testMkdirInRoot() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MkdirCommand.ARG_PATH, "/d4");
        args.put(MkdirCommand.ARG_RECURSIVE, false);
        new MkdirCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());
        InventoryPather.traverse(context.getInventory(), "/d4").isAFolder();
    }

    @Test
    public void testMkdirInExistingDir() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d1/d4", false);
        InventoryPather.traverse(inv, "/d1/d4").isAFolder();
    }

    @Test
    public void testMkdirInExistingDirs() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d2/d2.d3/d4", false);
        InventoryPather.traverse(inv, "/d2/d2.d3/d4").isAFolder();
    }

    @Test
    public void testMkdirRecursiveInRoot() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d4/d5/d6", true);
        InventoryPather.traverse(inv, "/d4/d5/d6").isAFolder();
    }

    @Test
    public void testMkdirRecursiveInADir() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d2/d4/d5", true);
        InventoryPather.traverse(inv, "/d2/d4/d5").isAFolder();
    }

    @Test
    public void testMkdirFailInFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d2.f3/d5", false);
            fail("did not through traversal exception");
        }
        catch (TraversalException ignored) {}
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d2.f3/d5", true);
            fail("did not through traversal exception");
        }
        catch (TraversalException ignored) {}
    }

    @Test
    public void testMkdirFailInMissingFolder() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d4/d5", false);
            fail("did not through traversal exception");
        }
        catch (TraversalException ignored) {}
    }

}