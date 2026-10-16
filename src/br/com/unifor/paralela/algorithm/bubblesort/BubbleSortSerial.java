package br.com.unifor.paralela.algorithm.bubblesort;

import br.com.unifor.paralela.algorithm.Sorter;

/**
 * Implementação serial do algoritmo Bubble Sort com flag de otimização
 * para interrupção precoce caso o vetor já esteja ordenado.
 */
public class BubbleSortSerial implements Sorter {

    @Override
    public String getName() {
        return "Bubble Sort (Serial)";
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

        int n = array.length;
        boolean trocou;

        for (int i = 0; i < n - 1; i++) {
            trocou = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    trocou = true;
                }
            }
            // Se nenhuma troca ocorreu na passagem, o vetor já está ordenado
            if (!trocou) {
                break;
            }
        }
    }
}
