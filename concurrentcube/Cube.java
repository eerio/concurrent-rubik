package concurrentcube;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;
import java.util.function.BiConsumer;


public class Cube {
    private static final int NFACES = 6;
    // 0 (góra), 1 (lewo), 2 (przód), 3 (prawo), 4 (tył), 5 (dół).
    private int[][] faces;
    public int size;
    private int n;
    private BiConsumer<Integer, Integer> beforeRotation;
    private BiConsumer<Integer, Integer> afterRotation;
    private Runnable beforeShowing;
    private Runnable afterShowing;

    private Semaphore ochrona = new Semaphore(1);
    private Semaphore pierwsi = new Semaphore(0);
    private Semaphore[] reszta;
    private Semaphore koniec = new Semaphore(0);

    public static int getNumSides() { return 6; }
    // 0: noone, 1: show, 2: ZX, 3: YZ, 4: XY
    int kto = 0;
    int iledziala = 0;
    int[] ileczeka = new int[5];
    int ilekonczy = 0;
    int ilegrup = 0;

    Semaphore[][] obrot_warstwy;

    public Cube(
        int size,
        BiConsumer<Integer, Integer> beforeRotation,
        BiConsumer<Integer, Integer> afterRotation,
        Runnable beforeShowing,
        Runnable afterShowing
    ) {
        this.size = size;
        this.n = size;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
        this.beforeShowing = beforeShowing;
        this.afterShowing = afterShowing;
        
        this.faces = new int[NFACES][n * n];

        for (int faceno=0; faceno < NFACES; ++faceno) {
            Arrays.fill(faces[faceno], faceno);
        }

        reszta = new Semaphore[5];
        for (int i=0; i < 5; ++i) reszta[i] = new Semaphore(0);

        obrot_warstwy = new Semaphore[5][n];
        for (int gr=2; gr < 5; ++gr) for (int i=0; i < n; ++i) obrot_warstwy[gr][i] = new Semaphore(1);
    }

    private void rotateFace(int faceno, boolean isClockwise) {
        Utils.rotate(faces[faceno], isClockwise);
    }

    private static PrimitiveIterator.OfInt range(int start, int stop, int step) {
        return (
                step > 0
                ? IntStream.iterate(start, x -> x < stop, x -> x + step)
                : IntStream.iterate(start, x -> x > stop, x -> x + step)
            )
            .sequential()
            .iterator();
    }

    private PrimitiveIterator.OfInt iterKthLastRow(int k) {
        return range((n - 1 - k) * n, (n - 1 - k + 1) * n, 1);
    }

    private PrimitiveIterator.OfInt iterKthRowReversed(int k) {
        return range((k + 1) * n - 1, k * n - 1, -1);
    }

    private PrimitiveIterator.OfInt iterKthColumn(int k) {
        return range(k, n * n, n);
    }

    private PrimitiveIterator.OfInt iterKthLastColumnReversed(int k) {
        return range(n * n - k - 1, -1, -n);
    }

    private void rotateZX(int layer, boolean isClockwise) {
        if (layer == 0) rotateFace(2, isClockwise);
        else if (layer == n - 1) rotateFace(4, !isClockwise);

        Utils.rotateSlices(
            faces,
            new Integer[] {0, 3, 5, 1},
            new PrimitiveIterator.OfInt[] {
                iterKthLastRow(layer),
                iterKthColumn(layer),
                iterKthRowReversed(layer),
                iterKthLastColumnReversed(layer)
            },
            isClockwise
        );
    }

    private void rotateYZ(int layer, boolean isClockwise) {
        if (layer == 0) rotateFace(1, isClockwise);
        else if (layer == n - 1) rotateFace(3, !isClockwise);

        Utils.rotateSlices(
            faces,
            new Integer[] {0, 2, 5, 4},
            new PrimitiveIterator.OfInt[] {
                iterKthColumn(layer),
                iterKthColumn(layer),
                iterKthColumn(layer),
                iterKthLastColumnReversed(layer)
            },
            isClockwise
        );
    }

    private void rotateXY(int layer, boolean isClockwise) {
        // System.out.println("rotate xy: " + layer + " " +isClockwise);
        if (layer == 0) rotateFace(5, isClockwise);
        else if (layer == n - 1) rotateFace(0, !isClockwise);

        Utils.rotateSlices(
            faces,
            new Integer[] {2, 3, 4, 1},
            new PrimitiveIterator.OfInt[] {
                iterKthLastRow(layer),
                iterKthLastRow(layer),
                iterKthLastRow(layer),
                iterKthLastRow(layer),
            },
            isClockwise
        );
    }

    private void initialProtocol(int gr) {
        ochrona.acquireUninterruptibly();
        if (kto == 0) kto = gr;
        else if (kto != gr) {
            ileczeka[gr]++;
            if (ileczeka[gr] == 1) {
                ilegrup++;
                ochrona.release();
                pierwsi.acquireUninterruptibly();
                ilegrup--;
                kto = gr;
            } else {
                ochrona.release();
                reszta[gr].acquireUninterruptibly();
            }
            ileczeka[gr]--;
        }

        iledziala++;
        if (ileczeka[gr] > 0) reszta[gr].release();
        else ochrona.release();
    }

    private void finalProtocol(int gr) {
        ochrona.acquireUninterruptibly();
        iledziala--;
        if (iledziala > 0) {
            ilekonczy++;
            ochrona.release();
            koniec.acquireUninterruptibly();
            ilekonczy--;
        }

        if (ilekonczy > 0) koniec.release();
        else {
            if (ilegrup > 0) pierwsi.release();
            else { kto = 0; ochrona.release(); }
        }
    }

    public void rotate(int side, int layer) throws InterruptedException {
        // local section
        final int orig_layer = layer;
        boolean isClockwise;
        if (side == 0 || side == 3 || side == 4) {
            layer = n - 1 - layer;
            isClockwise = false;
        } else { isClockwise = true; }

        int gr;
        if (side == 2 || side == 4) gr = 2;
        else if (side == 1 || side == 3) gr = 3;
        else gr = 4;

        // initial protocol
        initialProtocol(gr);
        obrot_warstwy[gr][layer].acquireUninterruptibly();

        boolean wasInterrupted = Thread.interrupted();
        if (!wasInterrupted) {
            // critical section
            beforeRotation.accept(side, orig_layer);
            switch (gr) {
                case 0: case 1: break; // suppress warning
                case 2: rotateZX(layer, isClockwise); break;
                case 3: rotateYZ(layer, isClockwise); break;
                case 4: rotateXY(layer, isClockwise); break;
            }
            afterRotation.accept(side, orig_layer);
        }

        // final protocol
        obrot_warstwy[gr][layer].release();
        finalProtocol(gr);

        if (wasInterrupted || Thread.interrupted()) throw new InterruptedException();
    }

    public void rotate(String rot) throws InterruptedException {
        int faceno = 0;
        int layer = 0;

        switch (rot.charAt(0)) {
            case 'F': faceno = 2; break;
            case 'B': faceno = 4; break;
            case 'U': faceno = 0; break;
            case 'D': faceno = 5; break;
            case 'L': faceno = 1; break;
            case 'R': faceno = 3; break;
            case 'M': faceno = 1; layer = 1; break;
            case 'E': faceno = 5; layer = 1; break;
            case 'S': faceno = 2; layer = 1; break;
            default: { System.err.println("baaaad ;("); break; }
        }

        if (rot.length() > 1 && rot.charAt(1) == '\'') {
            // System.out.println("prim detected: " + rot);
            faceno = new int[] {5, 3, 4, 1, 2, 0}[faceno];
            layer = n - 1 - layer;
        }

        rotate(faceno, layer);
    }
    
    public String show() throws InterruptedException {
        int gr = 1;

        initialProtocol(gr);

        boolean wasInterrupted = Thread.interrupted();
        String result = "";
        if (!wasInterrupted) {
            // critical section
            beforeShowing.run();
            StringBuilder builder = new StringBuilder();
            for (int faceno=0; faceno < NFACES; ++faceno) {
                builder.append(java.util.Arrays.toString(faces[faceno]).replaceAll("[\\,\\[\\]\\ ]", ""));
            }
            result = builder.toString();
            afterShowing.run();
        }

        finalProtocol(gr);

        if (wasInterrupted || Thread.interrupted()) {
            throw new InterruptedException();
        }

        return result;
    }
}
