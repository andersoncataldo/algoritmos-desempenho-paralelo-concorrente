package br.com.unifor.paralela.model;

/**
 * Armazena o resultado de uma medição individual (uma amostra).
 */
public class BenchmarkResult {
    private final String algoritmo;
    private final boolean paralelo;
    private final int threads;
    private final int tamanhoVetor;
    private final DataDistribution distribuicao;
    private final int amostraIndex;
    private final double tempoMs;

    public BenchmarkResult(String algoritmo, boolean paralelo, int threads, int tamanhoVetor,
                           DataDistribution distribuicao, int amostraIndex, double tempoMs) {
        this.algoritmo = algoritmo;
        this.paralelo = paralelo;
        this.threads = threads;
        this.tamanhoVetor = tamanhoVetor;
        this.distribuicao = distribuicao;
        this.amostraIndex = amostraIndex;
        this.tempoMs = tempoMs;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public boolean isParalelo() {
        return paralelo;
    }

    public int getThreads() {
        return threads;
    }

    public int getTamanhoVetor() {
        return tamanhoVetor;
    }

    public DataDistribution getDistribuicao() {
        return distribuicao;
    }

    public int getAmostraIndex() {
        return amostraIndex;
    }

    public double getTempoMs() {
        return tempoMs;
    }
}
