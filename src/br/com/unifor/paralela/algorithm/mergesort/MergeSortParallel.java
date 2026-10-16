package br.com.unifor.paralela.algorithm.mergesort;

import br.com.unifor.paralela.algorithm.Sorter;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Implementação paralela do Merge Sort utilizando o framework Fork/Join do Java.
 * A árvore de recursão é dividida em subtarefas executadas em paralelo pelo ForkJoinPool.
 * Quando o subvetor atinge um limiar mínimo, a execução comuta para a versão serial
 * para evitar sobrecarga excessiva de agendamento de tarefas pequenas.
 */
public class MergeSortParallel implements Sorter {

    private final int numThreads;

    public MergeSortParallel(int numThreads) {
        this.numThreads = Math.max(1, numThreads);
    }

    @Override
    public String getName() {
        return "Merge Sort Paralelo (" + numThreads + " threads)";
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
            new MergeSortSerial().sort(array);
            return;
        }

        int[] aux = new int[array.length];
        int threshold = Math.max(1024, array.length / (numThreads * 4));

        ForkJoinPool pool = new ForkJoinPool(numThreads);
        try {
            pool.invoke(new MergeSortTask(array, aux, 0, array.length - 1, threshold));
        } finally {
            pool.shutdown();
        }
    }

    private static class MergeSortTask extends RecursiveAction {
        private final int[] array;
        private final int[] aux;
        private final int esquerda;
        private final int direita;
        private final int threshold;

        public MergeSortTask(int[] array, int[] aux, int esquerda, int direita, int threshold) {
            this.array = array;
            this.aux = aux;
            this.esquerda = esquerda;
            this.direita = direita;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (direita - esquerda < threshold) {
                MergeSortSerial.sort(array, aux, esquerda, direita);
                return;
            }

            int meio = esquerda + (direita - esquerda) / 2;
            MergeSortTask tarefaEsquerda = new MergeSortTask(array, aux, esquerda, meio, threshold);
            MergeSortTask tarefaDireita = new MergeSortTask(array, aux, meio + 1, direita, threshold);

            invokeAll(tarefaEsquerda, tarefaDireita);
            MergeSortSerial.merge(array, aux, esquerda, meio, direita);
        }
    }
}
