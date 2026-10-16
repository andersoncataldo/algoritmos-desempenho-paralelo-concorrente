package br.com.unifor.paralela.algorithm.insertionsort;

import br.com.unifor.paralela.algorithm.Sorter;

/**
 * Implementação serial clássica do algoritmo Insertion Sort.
 * Constrói o vetor ordenado um elemento por vez, inserindo cada novo
 * elemento na sua posição correta relativa aos elementos já examinados.
 */
public class InsertionSortSerial implements Sorter {

    @Override
    public String getName() {
        return "Insertion Sort (Serial)";
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
        sortRange(array, 0, array.length);
    }

    /**
     * Ordena a faixa [inicio, fim) do vetor usando Insertion Sort.
     * Útil também para subvetores no algoritmo paralelo e híbrido.
     */
    public static void sortRange(int[] array, int inicio, int fim) {
        for (int i = inicio + 1; i < fim; i++) {
            int chave = array[i];
            int j = i - 1;

            while (j >= inicio && array[j] > chave) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = chave;
        }
    }
}
