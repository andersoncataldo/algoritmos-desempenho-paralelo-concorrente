package br.com.unifor.paralela.algorithm.quicksort;

import br.com.unifor.paralela.algorithm.Sorter;
import br.com.unifor.paralela.algorithm.insertionsort.InsertionSortSerial;

/**
 * Implementação serial do Quick Sort altamente otimizada:
 * - Pivô por mediana de três (início, meio e fim) para evitar pior caso O(n²).
 * - Chaveamento para Insertion Sort em partições menores que 32 elementos (mesma técnica do DualPivotQuicksort do Java).
 * - Eliminação de recursão em cauda (tail-call elimination) para reduzir a pilha de execução.
 */
public class QuickSortSerial implements Sorter {

    public static final int INSERTION_THRESHOLD = 32;

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
        while (esquerda < direita) {
            // Se o subvetor for pequeno, usa Insertion Sort
            if (direita - esquerda < INSERTION_THRESHOLD) {
                InsertionSortSerial.sortRange(array, esquerda, direita + 1);
                return;
            }

            int indicePivo = particionar(array, esquerda, direita);

            // Otimização de cauda: executa recursão na menor partição primeiro e itera na maior
            if (indicePivo - esquerda < direita - indicePivo) {
                quickSort(array, esquerda, indicePivo);
                esquerda = indicePivo + 1;
            } else {
                quickSort(array, indicePivo + 1, direita);
                direita = indicePivo;
            }
        }
    }

    /**
     * Particionamento de Hoare com escolha de pivô por mediana de três.
     */
    public static int particionar(int[] array, int esquerda, int direita) {
        int meio = esquerda + (direita - esquerda) / 2;

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
