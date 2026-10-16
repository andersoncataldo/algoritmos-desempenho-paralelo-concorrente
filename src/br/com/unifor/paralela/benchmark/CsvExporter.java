package br.com.unifor.paralela.benchmark;

import br.com.unifor.paralela.model.BenchmarkResult;
import br.com.unifor.paralela.model.BenchmarkSummary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Utilitário para exportação dos resultados de benchmark em arquivos CSV.
 */
public class CsvExporter {

    private static final DecimalFormat DF = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));

    /**
     * Exporta o resumo consolidado estatístico dos testes para um arquivo CSV.
     */
    public static File exportarResumo(List<BenchmarkSummary> resumos, File diretorioDestino) throws IOException {
        if (!diretorioDestino.exists()) {
            diretorioDestino.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File arquivo = new File(diretorioDestino, "benchmark_consolidados_" + timestamp + ".csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivo))) {
            // Cabeçalho CSV
            writer.println("Algoritmo,Paralelo,Threads,Tamanho,Distribuicao,TotalAmostras,TempoMedioMs,DesvioPadraoMs,TempoMinimoMs,TempoMaximoMs,Speedup,Eficiencia");

            for (BenchmarkSummary s : resumos) {
                writer.printf("%s,%b,%d,%d,%s,%d,%s,%s,%s,%s,%s,%s%n",
                        escaparCsv(s.getAlgoritmo()),
                        s.isParalelo(),
                        s.getThreads(),
                        s.getTamanhoVetor(),
                        s.getDistribuicao().name(),
                        s.getTotalAmostras(),
                        DF.format(s.getTempoMedioMs()),
                        DF.format(s.getDesvioPadraoMs()),
                        DF.format(s.getTempoMinimoMs()),
                        DF.format(s.getTempoMaximoMs()),
                        DF.format(s.getSpeedup()),
                        DF.format(s.getEficiencia())
                );
            }
        }

        return arquivo;
    }

    /**
     * Exporta todas as amostras individuais para rastreabilidade e geração de gráficos de dispersão.
     */
    public static File exportarAmostras(List<BenchmarkResult> amostras, File diretorioDestino) throws IOException {
        if (!diretorioDestino.exists()) {
            diretorioDestino.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File arquivo = new File(diretorioDestino, "benchmark_amostras_detalhadas_" + timestamp + ".csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivo))) {
            writer.println("Algoritmo,Paralelo,Threads,Tamanho,Distribuicao,Amostra,TempoMs");

            for (BenchmarkResult r : amostras) {
                writer.printf("%s,%b,%d,%d,%s,%d,%s%n",
                        escaparCsv(r.getAlgoritmo()),
                        r.isParalelo(),
                        r.getThreads(),
                        r.getTamanhoVetor(),
                        r.getDistribuicao().name(),
                        r.getAmostraIndex(),
                        DF.format(r.getTempoMs())
                );
            }
        }

        return arquivo;
    }

    private static String escaparCsv(String valor) {
        if (valor.contains(",") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
