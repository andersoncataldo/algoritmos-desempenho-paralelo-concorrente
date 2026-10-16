package br.com.unifor.paralela.algorithm;

/**
 * Interface comum para todos os algoritmos de ordenação (seriais e paralelos).
 */
public interface Sorter {

    /**
     * Retorna o nome amigável do algoritmo.
     */
    String getName();

    /**
     * Indica se o algoritmo utiliza execução paralela/concorrente.
     */
    boolean isParallel();

    /**
     * Retorna a quantidade de threads configurada para a execução.
     * Retorna 1 se for serial.
     */
    int getThreads();

    /**
     * Ordena o vetor fornecido no local (in-place) ou aplicando a mescla necessária.
     * 
     * @param array vetor de inteiros a ser ordenado
     */
    void sort(int[] array);
}
