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

package test.bunkr.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.ExportFileCommand;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import test.bunkr.core.XTemporaryFolder;
import test.bunkr.cli.OutputCapture;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

/**
 * Created At: 2015-12-11
 */
public class TestExportFileCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new ExportFileCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    public File buildArchive() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp);

        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        context.getInventory().addFile(fileOne);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
        {
            bwos.write(RandomMaker.get(3333 * 8));
        }

        FolderInventoryItem folderOne = new FolderInventoryItem("folder");

        FileInventoryItem fileTwo = new FileInventoryItem("b.txt");
        context.getInventory().addFile(fileTwo);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
        {
            bwos.write(RandomMaker.get(50 * 8));
        }

        folderOne.addFile(fileTwo);
        context.getInventory().addFolder(folderOne);

        MetadataWriter.write(context, usp);

        return archiveFile;
    }

    @Test
    public void testExportToStdout() throws Exception
    {
        File archive = buildArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/a.txt");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, new File("-"));
        args.put(ExportFileCommand.ARG_IGNORE_INTEGRITY_CHECK, false);

        try(OutputCapture c = new OutputCapture())
        {
            new ExportFileCommand().handle(new Namespace(args));
            assertThat(c.getBytes().length, is(equalTo(3333)));
        }
    }

    @Test
    public void testExportToFile() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/folder/b.txt");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);
        args.put(ExportFileCommand.ARG_IGNORE_INTEGRITY_CHECK, false);
        args.put(ExportFileCommand.ARG_NO_PROGRESS, true);

        new ExportFileCommand().handle(new Namespace(args));
        assertThat(outputFile.length(), is(equalTo(50L)));
    }

    @Test
    public void testExportAFolder() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/folder");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);
        args.put(ExportFileCommand.ARG_IGNORE_INTEGRITY_CHECK, false);
        args.put(ExportFileCommand.ARG_NO_PROGRESS, true);

        try
        {
            new ExportFileCommand().handle(new Namespace(args));
            fail("Should not be able to export /folder");
        }
        catch (CLIException ignored) {}
    }

    @Test
    public void testExportRoot() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);
        args.put(ExportFileCommand.ARG_IGNORE_INTEGRITY_CHECK, false);
        args.put(ExportFileCommand.ARG_NO_PROGRESS, true);

        try
        {
            new ExportFileCommand().handle(new Namespace(args));
            fail("Should not be able to export /");
        }
        catch (CLIException ignored) {}
    }


    @Test
    public void testExportCannotOverwrite() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFile();
        assertTrue(outputFile.exists());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/a.txt");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);
        args.put(ExportFileCommand.ARG_IGNORE_INTEGRITY_CHECK, false);
        args.put(ExportFileCommand.ARG_NO_PROGRESS, true);

        try
        {
            new ExportFileCommand().handle(new Namespace(args));
            fail("Should not be able to overwrite");
        }
        catch (CLIException ignored) {}
    }
}
