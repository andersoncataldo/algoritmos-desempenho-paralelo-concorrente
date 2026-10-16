package br.com.unifor.paralela.algorithm.bubblesort;

import br.com.unifor.paralela.algorithm.Sorter;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementação paralela do Bubble Sort utilizando o algoritmo de
 * Transposição Par-Ímpar (Odd-Even Transposition Sort).
 * 
 * Em cada fase (par ou ímpar), os pares independentes (j, j+1) são distribuídos
 * uniformemente entre as threads de trabalho. A barreira cíclica (CyclicBarrier)
 * garante a sincronização exata ao final de cada fase.
 */
public class BubbleSortParallel implements Sorter {

    private final int numThreads;

    public BubbleSortParallel(int numThreads) {
        this.numThreads = Math.max(1, numThreads);
    }

    @Override
    public String getName() {
        return "Bubble Sort Paralelo (" + numThreads + " threads)";
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public int getThreads() {
        return numThreads;
    }

    @Override
    public void sort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }

        if (numThreads == 1) {
            new BubbleSortSerial().sort(array);
            return;
        }

        final int n = array.length;
        int totalThreads = Math.min(numThreads, n / 2);
        if (totalThreads < 1) {
            totalThreads = 1;
        }

        final AtomicBoolean houveTrocaGlobal = new AtomicBoolean(false);
        final AtomicBoolean ordenado = new AtomicBoolean(false);

        // Barreira cíclica para sincronizar as threads ao final de cada fase
        final CyclicBarrier barreira = new CyclicBarrier(totalThreads);
        Thread[] threads = new Thread[totalThreads];

        final int numParesPares = n / 2;
        final int numParesImpares = (n - 1) / 2;

        for (int t = 0; t < totalThreads; t++) {
            final int threadId = t;
            final int tCount = totalThreads;

            threads[t] = new Thread(() -> {
                // Intervalo de pares pares tratados por esta thread
                int blocoPar = (numParesPares + tCount - 1) / tCount;
                int kParInicio = threadId * blocoPar;
                int kParFim = Math.min(kParInicio + blocoPar, numParesPares);

                // Intervalo de pares ímpares tratados por esta thread
                int blocoImpar = (numParesImpares + tCount - 1) / tCount;
                int kImparInicio = threadId * blocoImpar;
                int kImparFim = Math.min(kImparInicio + blocoImpar, numParesImpares);

                try {
                    for (int fase = 0; fase < n; fase++) {
                        boolean faseImpar = (fase % 2 != 0);
                        boolean trocouLocal = false;

                        if (!faseImpar) {
                            // Fase Par: pares j = 2*k
                            for (int k = kParInicio; k < kParFim; k++) {
                                int j = 2 * k;
                                if (array[j] > array[j + 1]) {
                                    int temp = array[j];
                                    array[j] = array[j + 1];
                                    array[j + 1] = temp;
                                    trocouLocal = true;
                                }
                            }
                        } else {
                            // Fase Ímpar: pares j = 2*k + 1
                            for (int k = kImparInicio; k < kImparFim; k++) {
                                int j = 2 * k + 1;
                                if (array[j] > array[j + 1]) {
                                    int temp = array[j];
                                    array[j] = array[j + 1];
                                    array[j + 1] = temp;
                                    trocouLocal = true;
                                }
                            }
                        }

                        if (trocouLocal) {
                            houveTrocaGlobal.set(true);
                        }

                        // Sincroniza todas as threads ao fim da fase
                        barreira.await();

                        // Ao final de um ciclo completo (fase ímpar concluída)
                        if (faseImpar) {
                            if (threadId == 0) {
                                if (!houveTrocaGlobal.get()) {
                                    ordenado.set(true);
                                }
                                houveTrocaGlobal.set(false);
                            }
                            barreira.await();

                            if (ordenado.get()) {
                                break;
                            }
                        }
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            }, "BubbleSortParallel-Worker-" + t);

            threads[t].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
