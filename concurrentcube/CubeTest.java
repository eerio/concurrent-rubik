package concurrentcube;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


class CubeTest {
    private ExecutorService executorService = Executors.newFixedThreadPool(
        4
    );

    // time of execution will be roughly proportional to this parameter
    // it grows faster than linearly though
    // (25 ~ 9s, 50 ~ 15s, 100 ~ 30s, 200 ~ 80s), depending on machine
    private int stress = 15;

    @Test
    void testConcreteSolved() throws InterruptedException {
        Cube cube = new Cube(0, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        assertEquals("", cube.show());

        cube = new Cube(1, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        assertEquals("012345", cube.show());

        cube = new Cube(2, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        assertEquals("000011112222333344445555", cube.show());
    }

    @Test
    void testConcrete2x2() throws InterruptedException {
        Cube cube = new Cube(2, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        assertEquals("000011112222333344445555", cube.show());

        cube.rotate(2, 0);
        assertEquals("001115152222030344443355", cube.show());

        cube.rotate(1, 0);
        assertEquals("404111550212030345432325", cube.show());

        cube.rotate(0, 1);
        assertEquals("404111120203034345553522", cube.show());
    }

    @Test
    void testConcrete3x3() throws InterruptedException {
        Cube cube = new Cube(3, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        assertEquals("000000000111111111222222222333333333444444444555555555", cube.show());

        cube.rotate(3, 0); // R
        assertEquals("002002002111111111225225225333333333044044044554554554", cube.show());

        cube.rotate(1, 2); // R'
        assertEquals("000000000111111111222222222333333333444444444555555555", cube.show());

        cube.rotate(1, 0); // L
        assertEquals("400400400111111111022022022333333333445445445255255255", cube.show());
        
        cube.rotate(5, 0); // D
        assertEquals("400400400111111445022022111333333022445445333222555555", cube.show());

        cube.rotate(0, 0); // U
        assertEquals("444000000022111445333022111445333022111445333222555555", cube.show());

        cube.rotate(0, 2); // D'
        assertEquals("444000000022111111333022022445333333111445445255255255", cube.show());

        cube.rotate(2, 1); // S
        assertEquals("444112000022151151333022022405303303111445445255334255", cube.show());

        cube.rotate(3, 1); // M'
        assertEquals("434122020022151151353032052405303303101415445245344215", cube.show());

        cube.rotate(1, 1); // M
        assertEquals("444112000022151151333022022405303303111445445255334255", cube.show());

        cube.rotate(4, 1); // S'
        assertEquals("444000000022111111333022022445333333111445445255255255", cube.show());

        cube.rotate(5, 1); // E
        assertEquals("444000000022445111333111022445022333111333445255255255", cube.show());
    }

    @Test
    void testSingmaster() throws InterruptedException {
        Cube cube = new Cube(3, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        assertEquals("000000000111111111222222222333333333444444444555555555", cube.show());

        cube.rotate("R");
        assertEquals("002002002111111111225225225333333333044044044554554554", cube.show());

        cube.rotate("R'");
        assertEquals("000000000111111111222222222333333333444444444555555555", cube.show());

        cube.rotate("L");
        assertEquals("400400400111111111022022022333333333445445445255255255", cube.show());
        
        cube.rotate("D");
        assertEquals("400400400111111445022022111333333022445445333222555555", cube.show());

        cube.rotate("U");
        assertEquals("444000000022111445333022111445333022111445333222555555", cube.show());

        cube.rotate("D'");
        assertEquals("444000000022111111333022022445333333111445445255255255", cube.show());

        cube.rotate("S");
        assertEquals("444112000022151151333022022405303303111445445255334255", cube.show());

        cube.rotate("M'");
        assertEquals("434122020022151151353032052405303303101415445245344215", cube.show());

        cube.rotate("M");
        assertEquals("444112000022151151333022022405303303111445445255334255", cube.show());

        cube.rotate("S'");
        assertEquals("444000000022111111333022022445333333111445445255255255", cube.show());

        cube.rotate("E");
        assertEquals("444000000022445111333111022445022333111333445255255255", cube.show());

    }

    @Test
    void testConcrete4x4() throws InterruptedException {
        Cube cube = new Cube(4, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        assertEquals("000000000000000011111111111111112222222222222222333333333333333344444444444444445555555555555555", cube.show());

        cube.rotate(3, 1);
        assertEquals("002000200020002011111111111111112252225222522252333333333333333340444044404440445545554555455545", cube.show());

        cube.rotate(3, 2);
        assertEquals("022002200220022011111111111111112552255225522552333333333333333340044004400440045445544554455445", cube.show());

        cube.rotate(4, 3);
        assertEquals("022002200220333311101112111211102222555555552222533343334333533340044004400440041111544554455445", cube.show());
    }

    private void trivialHelper(int cubeSize) throws InterruptedException {
        int befRotCnt=0, aftRotCnt=0, befShowCnt=0, aftShowCnt=0;
        var beforeRotationCnt = new Object() { int value = 0; };
        var afterRotationCnt = new Object() { int value = 0; };
        var beforeShowCnt = new Object() { int value = 0; };
        var afterShowCnt = new Object() { int value = 0; };

        Cube cube = new Cube(
            cubeSize,
            (x, y) -> { ++beforeRotationCnt.value; },
            (x, y) -> { ++afterRotationCnt.value; },
            () -> { ++beforeShowCnt.value; },
            () -> { ++afterShowCnt.value; }
        );

        String solved = cube.show();
        befShowCnt++; aftShowCnt++;

        for (int faceno=0; faceno < 6; ++faceno) {
            for (int layer=0; layer < cube.size; ++layer) {
                for (int i=0; i < 4; ++i) {
                    cube.rotate(faceno, layer);
                    befRotCnt++; aftRotCnt++;
                    assertEquals(beforeRotationCnt.value, befRotCnt);
                    assertEquals(afterRotationCnt.value, aftRotCnt);
                    assertEquals(beforeShowCnt.value, befShowCnt);
                    assertEquals(afterShowCnt.value, aftShowCnt);

                    if (i < 3) assertNotEquals(solved, cube.show());
                    else assertEquals(solved, cube.show());

                    befShowCnt++; aftShowCnt++;
                    assertEquals(beforeShowCnt.value, befShowCnt);
                    assertEquals(afterShowCnt.value, aftShowCnt);
                }
            }
        }
    }

    @Test
    void testTrivial() throws InterruptedException {
        int nOp = stress;
        int maxCubeSize = (int)Math.sqrt(stress);

        for (int i=0; i < nOp; ++i) {
            for (int cubeSize=0; cubeSize < maxCubeSize; ++cubeSize) { trivialHelper(cubeSize); }
       }
    }

    private void groupTheoryHelper(int order, String[] sequence) throws InterruptedException {
        int sq = Math.max((int)Math.sqrt(stress), 3);

        for (int size=sq; size < (sq + 1); ++size) {
            Cube cube = new Cube(size, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
            String solved = cube.show();
    
            for (int iter=0; iter < order; ++iter) {
                for (int i=0; i < sequence.length; ++i) {
                    cube.rotate(sequence[i]);
    
                    boolean isLastIter = iter == order - 1 && i == sequence.length - 1;
    
                    if (!isLastIter) assertNotEquals(solved, cube.show());
                    else assertEquals(solved, cube.show());
                }
            }
    
            assertEquals(solved, cube.show());
        }
    }

    @Test
    void testOrder2() throws InterruptedException {
        groupTheoryHelper(2, new String[] {"R", "R"});
    }

    @Test
    void testOrder3() throws InterruptedException {
        groupTheoryHelper(3, new String[] {"R", "U'", "R", "U", "R", "U", "R", "U'", "R'", "U'", "R", "R"});
    }

    @Test
    void testOrder4() throws InterruptedException {
        groupTheoryHelper(4, new String[] {"B"});
    }

    @Test
    void testOrder5() throws InterruptedException {
        groupTheoryHelper(5, new String[] {"R", "U", "R'", "U"});
    }

    @Test
    void testOrder7() throws InterruptedException {
        groupTheoryHelper(7, new String[] {"R", "U'", "F'", "U"});
    }

    @Test
    void testOrder11() throws InterruptedException {
        groupTheoryHelper(11, new String[] {
            "R", "U", "D'", "F", "L", "L", 
            "R", "U", "D'", "F", "L", "L"
        });

        groupTheoryHelper(11, new String[] {
            "R", "L'", "F'", "U",
            "R", "L'", "F'", "U",
            "R", "L'", "F'", "U",
        });
    }

    @Test
    void testOrder63() throws InterruptedException {
        groupTheoryHelper(63, new String[] {"R", "U'"});
    }

    @Test
    void testOrder105() throws InterruptedException {
        groupTheoryHelper(105, new String[] {"R", "U"});
        groupTheoryHelper(105, new String[] {"R'", "U'"});
    }

    @Test
    void testRotate1260() throws InterruptedException {
        groupTheoryHelper(1260, new String[] {"R", "U", "U", "D'", "B", "D'"});
    }

    @Test
    void testRotate1260Manual() throws InterruptedException {
        int size = 3;
        Cube cube = new Cube(size, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});

        StringBuilder builder = new StringBuilder();
        for (int i=0; i < 6; ++i) builder.append(String.valueOf(i).repeat(size * size));
        String solved = builder.toString();
        assertEquals(solved, cube.show());

        for (int i = 0; i < 1260; i++) {
            cube.rotate(3, 0);
            cube.rotate(0, 0);
            cube.rotate(0, 0);
            cube.rotate(5, 0); cube.rotate(5, 0); cube.rotate(5, 0);
            cube.rotate(4, 0);
            cube.rotate(5, 0); cube.rotate(5, 0); cube.rotate(5, 0);
        }
        assertEquals(solved, cube.show());
    }
    
    @Disabled
    @Test
    void testSpeed() throws InterruptedException, ExecutionException {
        int size = 1000;
        int nOp = 100000;

        
        Cube cube = new Cube(size, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (int i=0; i < nOp; ++i) {
            Future<?> future = executorService.submit(
                () -> {
                    try {
                        // int side = (int)(Math.random() * 6);
                        int side = 5;
                        int layer = (int)(Math.random() * size);
                        cube.rotate(side, layer);
                    } catch(InterruptedException e) {
                        System.err.println("interrupted :(((");
                        System.exit(1);
                    };
                    return;
                }
            );
            futures.add(future);
        }
        long start = System.currentTimeMillis();
        for (Future<?> future: futures) future.get();
        long end = System.currentTimeMillis();
        System.out.println("milis for async: " + (end - start));

        
        Cube cube2 = new Cube(size, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
        start = System.currentTimeMillis();
        for (int i=0; i < nOp; ++i) {
            // int side = (int)(Math.random() * 6);
            int side = 5;
            int layer = (int)(Math.random() * size);
            cube2.rotate(side, layer);
        }
        end = System.currentTimeMillis();
        System.out.println("milis for sync: " + (end - start));
    }

    @Test
    void testColorNumerosity() throws InterruptedException, ExecutionException {
        int size = (int)Math.sqrt(stress + 1) * 10;
        int n_op = stress * 100;
        Cube cube = new Cube(size, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});

        List<Future<?>> futures = new ArrayList<Future<?>>();

        for (int i=0; i < n_op; ++i) {
            Future<?> future = executorService.submit(
                () -> {
                    try {
                        Thread.sleep((long)(Math.random() * 10));
                        int side = (int)(Math.random() * 6);
                        int layer = (int)(Math.random() * size);
                        cube.rotate(side, layer);
                    } catch(InterruptedException e) {
                        System.err.println("interrupted :(((");
                        System.exit(1);
                    };
                    return;
                }
            );
            futures.add(future);
        }

        for (Future<?> future: futures) future.get();

        String result = cube.show();
        for (int i=0; i < 6; ++i) {
            final int iFinal = i;
            assertEquals(
                size * size,
                result.chars().filter(c -> c == '0' + iFinal).count()
            );
        }
    }

    @Test
    void testCompareSyncAsync() throws InterruptedException, ExecutionException {
        int queSize = (stress + 1) * 100;
        int cubeSize = (stress + 1) * 5;
        BlockingQueue<Runnable> que = new ArrayBlockingQueue<Runnable>(queSize);

        Cube cubeSync = new Cube(cubeSize, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});

        Cube cubeAsync = new Cube(
            cubeSize,
            (x, y) -> que.add(
                () -> {
                    try {
                        cubeSync.rotate(x, y); 
                    } catch (InterruptedException e) {
                        System.out.println("\nbad ;((\n");
                        System.exit(1);
                    }
                }
            ),
            (x, y) -> {},
            () -> {},
            () -> {}
        );

        List<Future<?>> futures = new ArrayList<Future<?>>();

        for (int i=0; i < queSize; ++i) {
            Future<?> future = executorService.submit(
                () -> {
                    try {
                        Thread.sleep((long)(Math.random() * 10));
                        int side = (int)(Math.random() * 6);
                        int layer = (int)(Math.random() * cubeSize);
                        cubeAsync.rotate(side, layer);
                    } catch(InterruptedException e) {
                        System.err.println("interrupted :(((");
                        System.exit(1);
                    };
                    return;
                }
            );
            futures.add(future);
        }

        for (Future<?> future: futures) future.get();

        assertTrue(que.size() == queSize);

        for (Runnable op: que) {
            op.run();
        }

        assertEquals(cubeSync.show(), cubeAsync.show());
    }

    @Test
    void testConcurrentRotatePositive() throws InterruptedException, ExecutionException {
        int cubeSize = 50;

        CyclicBarrier barrier = new CyclicBarrier(cubeSize);

        Cube cube = new Cube(
            cubeSize,
            (x, y) -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) {}
            },
            (x, y) -> {},
            () -> {},
            () -> {}
        );

        List<Thread> threads = new ArrayList<Thread>();

        for (int layer=0; layer < cubeSize; ++layer) {
            final int layerF = layer;
            Thread thread = new Thread(() -> {
                try {
                    cube.rotate(0, layerF);
                } catch (InterruptedException ignored) {} 
            });

            threads.add(thread);
        }

        for (Thread t: threads) t.start();
    }

    @Test
    void testConcurrentRotateNegative() throws InterruptedException, ExecutionException, BrokenBarrierException {
        int cubeSize = 20;

        CyclicBarrier barrier = new CyclicBarrier(cubeSize + 1);

        Cube cube = new Cube(
            cubeSize,
            (x, y) -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) {}
            },
            (x, y) -> {},
            () -> {},
            () -> {}
        );

        List<Thread> threads = new ArrayList<Thread>();

        for (int layer=0; layer < cubeSize; ++layer) {
            final int layerF = layer;
            Thread thread = new Thread(() -> {
                try {
                    cube.rotate(0, layerF);
                } catch (InterruptedException ignored) {} 
            });
            threads.add(thread);
        }

        int timeout = 2000;
        long start = System.currentTimeMillis();
        for (Thread t: threads) t.start();

        while (true) {
            boolean anyRunning = false;
            for (Thread t: threads) if (t.isAlive()) anyRunning = true;
            if (!anyRunning) break;

            if (System.currentTimeMillis() - start > timeout) {
                barrier.await();
                for (Thread t: threads) t.join();
                return;
            }
        }

        assertTrue(false);
    }

    @Test
    void testConcurrentShowPositive() throws InterruptedException, ExecutionException {
        int cubeSize = 10;
        int nConcurrent = cubeSize + 1; // just something larger than cubeSize

        CyclicBarrier barrier = new CyclicBarrier(nConcurrent);

        Cube cube = new Cube(
            cubeSize,
            (x, y) -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) {}
            },
            (x, y) -> {},
            () -> {},
            () -> {}
        );

        List<Thread> threads = new ArrayList<Thread>();

        for (int i=0; i < nConcurrent; ++i) {
            Thread thread = new Thread(() -> {
                try {
                    cube.show();
                } catch (InterruptedException ignored) {} 
            });
            threads.add(thread);
        }

        for (Thread t: threads) t.start();
        for (Thread t: threads) t.join();
    }

    @Test
    void testInterrupt() throws InterruptedException {
        int cubeSize = 10;

        CyclicBarrier barrier = new CyclicBarrier(cubeSize);

        Cube cube = new Cube(
            cubeSize,
            (x, y) -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) {}
            },
            (x, y) -> {},
            () -> {},
            () -> {}
        );

        List<Thread> threads = new ArrayList<Thread>();
        AtomicInteger cnt = new AtomicInteger(0);

        // one more thread required to trip the barrier
        for (int layer=0; layer < cubeSize - 1; ++layer) {
            final int layerF = layer;
            Thread thread = new Thread(() -> {
                try {
                    cube.rotate(0, layerF);
                } catch (InterruptedException e) {
                    cnt.incrementAndGet();
                } 
            });
            threads.add(thread);
        }

        for (Thread t: threads) { t.start(); t.interrupt(); t.join(); }

        assertEquals(cubeSize - 1, cnt.get());
    }
}
