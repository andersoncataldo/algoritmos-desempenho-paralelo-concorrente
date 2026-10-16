package br.com.unifor.paralela.model;

/**
 * Representa as diferentes naturezas/distribuições dos dados de entrada
 * para avaliar o comportamento dos algoritmos sob diferentes cenários.
 */
public enum DataDistribution {
    ALEATORIO("Aleatório"),
    QUASE_ORDENADO("Quase Ordenado (90% ordenado)"),
    INVERSO("Inversamente Ordenado (Pior caso)"),
    ORDENADO("Já Ordenado (Melhor caso)");

    private final String descricao;

    DataDistribution(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
