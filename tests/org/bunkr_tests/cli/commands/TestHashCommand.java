package org.bunkr_tests.cli.commands;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bouncycastle.crypto.CryptoException;
import org.bunkr.ArchiveBuilder;
import org.bunkr.ArchiveInfoContext;
import org.bunkr.MetadataWriter;
import org.bunkr.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.HashCommand;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.descriptor.Descriptor;
import org.bunkr.exceptions.CLIException;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.FolderInventoryItem;
import org.bunkr.streams.output.MultilayeredOutputStream;
import org.bunkr_tests.XTemporaryFolder;
import org.bunkr_tests.cli.OutputCapture;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-15
 */
public class TestHashCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new HashCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    public File buildArchive() throws IOException, CryptoException
    {
        File archiveFile = folder.newFile();

        ArchiveInfoContext context = ArchiveBuilder
                .createNewEmptyArchive(archiveFile, new Descriptor(null, null), new PasswordProvider());

        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        context.getInventory().getFiles().add(fileOne);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
        {
            bwos.write("The quick brown fox jumps over the lazy dog".getBytes());
        }

        FolderInventoryItem folderOne = new FolderInventoryItem("folder");

        FileInventoryItem fileTwo = new FileInventoryItem("b.txt");
        context.getInventory().getFiles().add(fileTwo);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
        {
            bwos.write(RandomMaker.get(50 * 8));
        }

        folderOne.getFiles().add(fileTwo);
        context.getInventory().getFolders().add(folderOne);

        MetadataWriter.write(context, new PasswordProvider());

        return archiveFile;
    }

    public void checkHash(String algorithm, String expectedHash) throws Exception
    {
        File archive = buildArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(HashCommand.ARG_PATH, "/a.txt");
        args.put(HashCommand.ARG_ALGORITHM, algorithm);

        try(OutputCapture c = new OutputCapture())
        {
            new HashCommand().handle(new Namespace(args));
            assertTrue(c.getContent().trim().endsWith(expectedHash));
        }
    }

    @Test
    public void testMD5() throws Exception
    {
        checkHash("md5", "9e107d9d372bb6826bd81d3542a419d6");
    }

    @Test
    public void testSHA1() throws Exception
    {
        checkHash("sha1", "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12");
    }

    @Test
    public void testSHA224() throws Exception
    {
        checkHash("sha224", "730e109bd7a8a32b1cb9d9a09aa2325d2430587ddbc0c38bad911525");
    }

    @Test
    public void testSHA256() throws Exception
    {
        checkHash("sha256", "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592");
    }


    @Test
    public void testUnknown() throws Exception
    {
        try
        {
            checkHash("wtfmate", "??");
            fail("Should raise failure");
        } catch (CLIException ignored) {}
    }

}
