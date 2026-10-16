package br.com.unifor.paralela.algorithm.mergesort;

import br.com.unifor.paralela.algorithm.Sorter;
import br.com.unifor.paralela.algorithm.insertionsort.InsertionSortSerial;

/**
 * Implementação serial altamente otimizada do Merge Sort.
 * - Utiliza vetor auxiliar pré-alocado uma única vez.
 * - Chaveamento para Insertion Sort em partições pequenas (< 32 elementos),
 *   eliminando recursões desnecessárias e aproveitando o cache L1 da CPU.
 * - Checagem de atalho (shortcut): se array[meio] <= array[meio + 1], a mescla é ignorada,
 *   resultando em tempo linear O(n) para vetores quase ou já ordenados.
 */
public class MergeSortSerial implements Sorter {

    public static final int INSERTION_THRESHOLD = 32;

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
        // Otimização: para fatias pequenas, Insertion Sort é mais rápido devido à localidade de cache
        if (direita - esquerda < INSERTION_THRESHOLD) {
            InsertionSortSerial.sortRange(array, esquerda, direita + 1);
            return;
        }

        int meio = esquerda + (direita - esquerda) / 2;
        sort(array, aux, esquerda, meio);
        sort(array, aux, meio + 1, direita);

        // Otimização de corte: se o último da esquerda já for <= primeiro da direita, já está ordenado
        if (array[meio] <= array[meio + 1]) {
            return;
        }

        merge(array, aux, esquerda, meio, direita);
    }

    public static void merge(int[] array, int[] aux, int esquerda, int meio, int direita) {
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
    }
}
