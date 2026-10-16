package br.com.unifor.paralela.model;

import java.util.List;

/**
 * Resumo estatístico agregado de uma configuração de teste (após pelo menos 5 amostras).
 * Inclui média, desvio padrão, mínimo, máximo, speedup e eficiência.
 */
public class BenchmarkSummary {
    private final String algoritmo;
    private final boolean paralelo;
    private final int threads;
    private final int tamanhoVetor;
    private final DataDistribution distribuicao;
    private final int totalAmostras;
    private final double tempoMedioMs;
    private final double desvioPadraoMs;
    private final double tempoMinimoMs;
    private final double tempoMaximoMs;
    private double speedup;
    private double eficiencia;

    public BenchmarkSummary(String algoritmo, boolean paralelo, int threads, int tamanhoVetor,
                            DataDistribution distribuicao, List<Double> temposAmostras) {
        this.algoritmo = algoritmo;
        this.paralelo = paralelo;
        this.threads = threads;
        this.tamanhoVetor = tamanhoVetor;
        this.distribuicao = distribuicao;
        this.totalAmostras = temposAmostras.size();

        if (temposAmostras.isEmpty()) {
            this.tempoMedioMs = 0.0;
            this.desvioPadraoMs = 0.0;
            this.tempoMinimoMs = 0.0;
            this.tempoMaximoMs = 0.0;
        } else {
            double soma = 0.0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            for (double t : temposAmostras) {
                soma += t;
                if (t < min) min = t;
                if (t > max) max = t;
            }

            this.tempoMedioMs = soma / temposAmostras.size();
            this.tempoMinimoMs = min;
            this.tempoMaximoMs = max;

            double somaQuadrados = 0.0;
            for (double t : temposAmostras) {
                double diff = t - this.tempoMedioMs;
                somaQuadrados += diff * diff;
            }
            this.desvioPadraoMs = Math.sqrt(somaQuadrados / temposAmostras.size());
        }

        this.speedup = 1.0;
        this.eficiencia = 1.0;
    }

    public void calcularSpeedupEEficiencia(double tempoSerialMedioMs) {
        if (tempoSerialMedioMs > 0.0 && this.tempoMedioMs > 0.0) {
            this.speedup = tempoSerialMedioMs / this.tempoMedioMs;
            int numThreads = Math.max(1, this.threads);
            this.eficiencia = this.speedup / numThreads;
        } else {
            this.speedup = 1.0;
            this.eficiencia = 1.0;
        }
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

    public int getTotalAmostras() {
        return totalAmostras;
    }

    public double getTempoMedioMs() {
        return tempoMedioMs;
    }

    public double getDesvioPadraoMs() {
        return desvioPadraoMs;
    }

    public double getTempoMinimoMs() {
        return tempoMinimoMs;
    }

    public double getTempoMaximoMs() {
        return tempoMaximoMs;
    }

    public double getSpeedup() {
        return speedup;
    }

    public double getEficiencia() {
        return eficiencia;
    }
}
