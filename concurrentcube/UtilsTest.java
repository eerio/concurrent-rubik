package concurrentcube;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import concurrentcube.Utils;

public class UtilsTest {
    @Test
    void testRotate() {
        int[] face = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Utils.rotate(face, true);
        assertArrayEquals(new int[] {7, 4, 1, 8, 5, 2, 9, 6, 3}, face);
        Utils.rotate(face, false);
        assertArrayEquals(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, face);
    }

    @Test
    void testRotateSlices() {
        int[][] faces = {
            new int[] { 0,  1,  2,  3,  4,  5,  6,  7,  8,  9},
            new int[] {10, 11, 12, 13, 14, 15, 16, 17, 18, 19},
            new int[] {20, 21, 22, 23, 24, 25, 26, 27, 28, 29}
        };

        int[][] orig_faces = new int[3][];
        for (int i=0; i < 3; ++i) orig_faces[i] = faces[i].clone();

        Integer[] facenos = new Integer[] {1, 0, 2};

        PrimitiveIterator.OfInt[] indgetters = new PrimitiveIterator.OfInt[] {
            IntStream.of(new int[] {0, 1, 2, 3}).iterator(),
            IntStream.of(new int[] {9, 8, 7, 6}).iterator(),
            IntStream.of(new int[] {0, 1, 2, 3}).iterator()
        };
        
        boolean isClockwise = true;
        Utils.rotateSlices(faces, facenos, indgetters, isClockwise);

        int[][] expected = {
            new int[] {0, 1, 2, 3, 4, 5, 13, 12, 11, 10},
            new int[] {20, 21, 22, 23, 14, 15, 16, 17, 18, 19},
            new int[] {9, 8, 7, 6, 24, 25, 26, 27, 28, 29}
        };

        // for (int i=0; i < faces.length; ++i) {
        //     for (int j=0; j < faces[0].length; ++j) System.out.print(orig_faces[i][j] + " ");
        //     System.out.println();
        // }
        
        assertTrue(Arrays.deepEquals(faces, expected));
        assertFalse(Arrays.deepEquals(faces, orig_faces));

    }
}
