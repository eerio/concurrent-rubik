package concurrentcube;

import java.util.Arrays;
import java.util.Collections;
import java.util.PrimitiveIterator;

public class Utils {
    private static int isqrt(int n) {
        return (int) Math.sqrt(n);
    }

    private static int getByYX(int[] face, int y, int x) {
        int n = isqrt(face.length);
        return face[n * y + x];
    }

    private static void setByYX(int[] face, int y, int x, int e) {
        int n = isqrt(face.length);
        face[n * y + x] = e;
    }

    private static void transpose(int[] face) {
        int n = isqrt(face.length);
        for (int y=0; y < n; ++y) {
            for (int x=y + 1; x < n; ++x) {
                int temp = getByYX(face, y, x);
                setByYX(face, y, x, getByYX(face, x, y));
                setByYX(face, x, y, temp);
            }
        }
    }

    private static void reverseColumns(int[] face) {
        // flip symetrically around x = size / 2
        int n = isqrt(face.length);

        for (int y=0; y < n; ++y) {
            for (int x=0; x < n / 2; ++x) {
                int temp = getByYX(face, y, x);
                setByYX(face, y, x, getByYX(face, y, n - 1 - x));
                setByYX(face, y, n - 1 - x, temp);
            }
        }
    }

    private static void reverseRows(int[] face) {
        // flip symetrically around y = size / 2
        int n = isqrt(face.length);

        for (int x=0; x < n; ++x) {
            for (int y=0; y < n / 2; ++y) {
                int temp = getByYX(face, y, x);
                setByYX(face, y, x, getByYX(face, n - 1 - y, x));
                setByYX(face, n - 1 - y, x, temp);
            }
        }
    }

    public static void rotate(int[] face, boolean isClockwise) {
        transpose(face);
        if (isClockwise) reverseColumns(face);
        else reverseRows(face);
    }

    public static void rotateSlices(
        int[][] faces, // faces[6][n * n]
        Integer[] facenos,
        PrimitiveIterator.OfInt[] indgetters,
        boolean isClockwise
    ) {
        if (isClockwise) {
            Collections.reverse(Arrays.asList(facenos));
            Collections.reverse(Arrays.asList(indgetters));
        }

        while (indgetters[0].hasNext()) {
            int[] old_face = faces[facenos[0]];
            int old_ind = indgetters[0].next();
            int first_val = old_face[old_ind];

            int[] last_face = null;
            int last_ind = 0;

            for (int j=0; j < facenos.length - 1; ++j) {
                int[] new_face = faces[facenos[j + 1]];
                int new_ind = indgetters[j + 1].next();
                old_face[old_ind] = new_face[new_ind];
                
                old_face = new_face;
                old_ind = new_ind;

                last_face = new_face;
                last_ind = new_ind;
            }

            last_face[last_ind] = first_val;
        }  
    }
}