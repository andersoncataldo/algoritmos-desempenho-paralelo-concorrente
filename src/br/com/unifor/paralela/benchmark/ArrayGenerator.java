package br.com.unifor.paralela.benchmark;

import br.com.unifor.paralela.model.DataDistribution;

import java.util.Random;

/**
 * Utilitário para geração de vetores de dados sob diferentes distribuições.
 */
public class ArrayGenerator {

    private static final long SEED_PADRAO = 42L;

    /**
     * Gera um novo vetor de inteiros com base no tamanho e distribuição especificados.
     */
    public static int[] gerar(int tamanho, DataDistribution distribuicao) {
        return gerar(tamanho, distribuicao, SEED_PADRAO);
    }

    /**
     * Gera um vetor com semente específica para repetibilidade dos experimentos.
     */
    public static int[] gerar(int tamanho, DataDistribution distribuicao, long seed) {
        int[] array = new int[tamanho];
        Random random = new Random(seed);

        switch (distribuicao) {
            case ALEATORIO:
                for (int i = 0; i < tamanho; i++) {
                    array[i] = random.nextInt(tamanho * 10);
                }
                break;

            case ORDENADO:
                for (int i = 0; i < tamanho; i++) {
                    array[i] = i + 1;
                }
                break;

            case INVERSO:
                for (int i = 0; i < tamanho; i++) {
                    array[i] = tamanho - i;
                }
                break;

            case QUASE_ORDENADO:
                // Cria ordenado e permuta cerca de 10% dos elementos
                for (int i = 0; i < tamanho; i++) {
                    array[i] = i + 1;
                }
                int trocas = Math.max(1, tamanho / 10);
                for (int k = 0; k < trocas; k++) {
                    int i = random.nextInt(tamanho);
                    int j = random.nextInt(tamanho);
                    int temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
                break;
        }

        return array;
    }
}
