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

package test.bunkr.core;

import org.bunkr.core.BlockAllocationManager;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.Inventory;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

/**
 * Created At: 2015-12-01
 */
public class TestBlockAllocationManager
{
    private Inventory fakeInventory()
    {

        ArrayList<FileInventoryItem> files = new ArrayList<>();
        FileInventoryItem file = new FileInventoryItem("something");
        file.setBlocks(new FragmentedRange(10, 8));
        files.add(file);
        return new Inventory(files, new ArrayList<>(), Encryption.NONE);
    }

    @Test
    public void testDefault()
    {
        BlockAllocationManager bam = new BlockAllocationManager(fakeInventory(), new FragmentedRange());

        assertThat(bam.getNextAllocatableBlockId(), is(equalTo(0)));
        assertThat(bam.allocateNextBlock(), is(equalTo(0)));
        assertTrue(bam.getCurrentAllocation().equals(new FragmentedRange(0, 1)));
        assertThat(bam.getTotalBlocks(), is(equalTo(18)));
        assertThat(bam.getNextAllocatableBlockId(), is(equalTo(1)));
        assertThat(bam.allocateBlock(1), is(equalTo(1)));
        assertTrue(bam.getCurrentAllocation().equals(new FragmentedRange(0, 2)));
    }

    @Test
    public void testOutOfRangeAllocations()
    {
        BlockAllocationManager bam = new BlockAllocationManager(fakeInventory(), new FragmentedRange());

        try
        {
            bam.allocateBlock(-1);
            fail("Can allocate negative block");
        }
        catch(Exception ignored) {}

        bam = new BlockAllocationManager(new Inventory(
                new ArrayList<>(), new ArrayList<>(), Encryption.NONE
        ), new FragmentedRange());

        try
        {
            bam.allocateBlock(10000);
            fail("Can allocate out of range block");
        }
        catch(Exception ignored) {}
    }

    @Test
    public void testUnallocatedBlocks()
    {
        BlockAllocationManager bam = new BlockAllocationManager(fakeInventory(), new FragmentedRange());

        for (int i = 0; i <= 9; i++)
        {
            assertThat(bam.allocateNextBlock(), is(equalTo(i)));
        }


        for (int i = 18; i <= 21; i++)
        {
            assertThat(bam.allocateNextBlock(), is(equalTo(i)));
        }

        assertThat(bam.getTotalBlocks(), is(equalTo(22)));
        FragmentedRange r = new FragmentedRange();
        r.add(0, 10);
        r.add(18, 4);
        assertTrue(bam.getCurrentAllocation().equals(r));

    }
}
