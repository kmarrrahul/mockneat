package net.andreinc.mockneat.unit.seq;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.IntStream.range;
import static net.andreinc.mockneat.Constants.M;
import static org.junit.Assert.assertTrue;

/**
 * Copyright 2017, Andrei N. Ciobanu

 Permission is hereby granted, free of charge, to any user obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, FREE_TEXT OF OR PARAM CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS PARAM THE SOFTWARE.
 */

public class SeqTest {

    @Test(expected = NullPointerException.class)
    public void testSeqNullList() throws Exception {
        List<String> list = null;
        M.seq(list).list(100).val();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyList() throws Exception {
        M.seq(new ArrayList<String>()).list(100).val();
    }

    @Test
    public void testSeq() throws Exception {
        List<Integer> arr = M.intSeq().max(100).cycle(false).list(100).val();
        List<Integer> seq = M.seq(arr).cycle(false).list(10).val();

        seq.forEach(i -> assertTrue(seq.get(i) == i));
    }

    @Test
    public void testTwoElementsCycle() throws Exception {
        List<Integer> twoElements = new ArrayList<>();

        twoElements.add(1);
        twoElements.add(2);

        List<Integer> seq = M.seq(twoElements).cycle(true).list(100).val();

        range(0, seq.size()).forEach(i -> {
            if (i%2==0)
                assertTrue(seq.get(i) == 1);
            else
                assertTrue(seq.get(i) == 2);
        });
    }

    @Test
    public void testOneElementNoCycle() throws  Exception {
        List<Integer> oneElement = new ArrayList<>();

        oneElement.add(2);

        List<Integer> seq = M.seq(oneElement).list(10).val();

        assertTrue(seq.get(0) == 2);
        assertTrue(seq.get(1) == null);
    }

    @Test
    public void testOneElementNoCycleAfterNonNull() throws Exception {
        List<Integer> oneElement = new ArrayList<>();

        oneElement.add(2);

        List<Integer> seq = M.seq(oneElement).after(4).list(10).val();

        assertTrue(seq.get(0) == 2);
        assertTrue(seq.get(1) == 4);
    }
}
