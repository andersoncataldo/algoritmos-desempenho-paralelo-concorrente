package br.com.unifor.paralela.algorithm.quicksort;

import br.com.unifor.paralela.algorithm.Sorter;

/**
 * Implementação serial do Quick Sort.
 * Utiliza a estratégia de pivô por mediana de três (início, meio e fim)
 * e particionamento de Hoare para máxima eficiência e proteção contra
 * degeneração em vetores já ordenados ou inversos.
 */
public class QuickSortSerial implements Sorter {

    @Override
    public String getName() {
        return "Quick Sort (Serial)";
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
        quickSort(array, 0, array.length - 1);
    }

    public static void quickSort(int[] array, int esquerda, int direita) {
        if (esquerda < direita) {
            int indicePivo = particionar(array, esquerda, direita);
            quickSort(array, esquerda, indicePivo);
            quickSort(array, indicePivo + 1, direita);
        }
    }

    /**
     * Particionamento baseado no esquema de Hoare com pivô mediana de três.
     */
    public static int particionar(int[] array, int esquerda, int direita) {
        int meio = esquerda + (direita - esquerda) / 2;

        // Mediana de três: ordena esquerda, meio e direita para escolher o pivô
        if (array[esquerda] > array[meio]) {
            trocar(array, esquerda, meio);
        }
        if (array[esquerda] > array[direita]) {
            trocar(array, esquerda, direita);
        }
        if (array[meio] > array[direita]) {
            trocar(array, meio, direita);
        }

        int pivo = array[meio];
        int i = esquerda - 1;
        int j = direita + 1;

        while (true) {
            do {
                i++;
            } while (array[i] < pivo);

            do {
                j--;
            } while (array[j] > pivo);

            if (i >= j) {
                return j;
            }

            trocar(array, i, j);
        }
    }

    private static void trocar(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
