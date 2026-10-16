package br.com.unifor.paralela.algorithm.mergesort;

import br.com.unifor.paralela.algorithm.Sorter;

/**
 * Implementação serial do Merge Sort (divisão e conquista).
 * Utiliza vetor auxiliar pré-alocado uma única vez para evitar sobrecarga de Garbage Collection.
 */
public class MergeSortSerial implements Sorter {

    @Override
    public String getName() {
        return "Merge Sort (Serial)";
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public int getThreads() {
        return 1;
    }

    @Override
    public void sort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        int[] aux = new int[array.length];
        sort(array, aux, 0, array.length - 1);
    }

    public static void sort(int[] array, int[] aux, int esquerda, int direita) {
        if (esquerda >= direita) {
            return;
        }

        int meio = esquerda + (direita - esquerda) / 2;
        sort(array, aux, esquerda, meio);
        sort(array, aux, meio + 1, direita);
        merge(array, aux, esquerda, meio, direita);
    }

    public static void merge(int[] array, int[] aux, int esquerda, int meio, int direita) {
        // Copia os elementos para o vetor auxiliar
        System.arraycopy(array, esquerda, aux, esquerda, direita - esquerda + 1);

        int i = esquerda;
        int j = meio + 1;
        int k = esquerda;

        while (i <= meio && j <= direita) {
            if (aux[i] <= aux[j]) {
                array[k++] = aux[i++];
            } else {
                array[k++] = aux[j++];
            }
        }

        while (i <= meio) {
            array[k++] = aux[i++];
        }

        // Os elementos remanescentes à direita já estão na posição correta
    }
}
