package br.com.unifor.paralela.ui;

import br.com.unifor.paralela.model.BenchmarkSummary;
import br.com.unifor.paralela.model.DataDistribution;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Componente gráfico nativo em Java Swing (Graphics2D) para visualização de métricas de desempenho.
 * Renderiza gráficos de barras comparativas e curvas de speedup sem necessidade de bibliotecas externas.
 */
public class ChartPanel extends JPanel {

    public enum ChartType {
        TEMPO_EXECUCAO,
        SPEEDUP_ESCALABILIDADE
    }

    private final ChartType chartType;
    private List<BenchmarkSummary> dados = new ArrayList<>();
    private DataDistribution filtroDistribuicao = DataDistribution.ALEATORIO;
    private static final DecimalFormat DF = new DecimalFormat("#,##0.00");

    // Paleta de cores moderna e contrastante para visualização científica
    private static final Color[] PALETA_CORES = {
            new Color(41, 128, 185),  // Azul oceano
            new Color(39, 174, 96),   // Verde esmeralda
            new Color(230, 126, 34),  // Laranja
            new Color(142, 68, 173),  // Roxo ametista
            new Color(231, 76, 60),   // Vermelho
            new Color(22, 160, 133),  // Turquesa
            new Color(243, 156, 18),  // Âmbar
            new Color(52, 73, 94)     // Azul escuro
    };

    public ChartPanel(ChartType chartType) {
        this.chartType = chartType;
        setBackground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setToolTipText(""); // Ativa suporte a tooltips interativos
    }

    @Override
    public Dimension getPreferredSize() {
        int w = Math.max(780, dados.size() * 35);
        return new Dimension(w, 480);
    }

    public void atualizarDados(List<BenchmarkSummary> novosDados, DataDistribution distribuicao) {
        this.dados = new ArrayList<>(novosDados);
        this.filtroDistribuicao = distribuicao;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Ativa suavização (Anti-aliasing) de alta qualidade
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int largura = getWidth();
        int altura = getHeight();

        // Filtra dados pela distribuição selecionada
        List<BenchmarkSummary> dadosFiltrados = new ArrayList<>();
        for (BenchmarkSummary s : dados) {
            if (filtroDistribuicao == null || s.getDistribuicao() == filtroDistribuicao) {
                dadosFiltrados.add(s);
            }
        }

        if (dadosFiltrados.isEmpty()) {
            desenharMensagemVazia(g2, largura, altura);
            g2.dispose();
            return;
        }

        if (chartType == ChartType.TEMPO_EXECUCAO) {
            desenharGraficoTempo(g2, dadosFiltrados, largura, altura);
        } else {
            desenharGraficoSpeedup(g2, dadosFiltrados, largura, altura);
        }

        g2.dispose();
    }

    private void desenharMensagemVazia(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(120, 120, 120));
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        String msg = "Nenhum dado disponível para exibição. Execute o benchmark para gerar o gráfico.";
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(msg)) / 2;
        int y = h / 2;
        g2.drawString(msg, x, y);
    }

    private void desenharGraficoTempo(Graphics2D g2, List<BenchmarkSummary> lista, int w, int h) {
        int margemEsquerda = 80;
        int margemDireita = 40;
        int margemSuperior = 60;
        int margemInferior = 100;

        int areaW = w - margemEsquerda - margemDireita;
        int areaH = h - margemSuperior - margemInferior;

        // Título do Gráfico
        g2.setColor(new Color(33, 33, 33));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        String titulo = "Tempo de Execução (ms) - Distribuição: " +
                (filtroDistribuicao != null ? filtroDistribuicao.getDescricao() : "Geral");
        g2.drawString(titulo, margemEsquerda, 30);

        // Identifica os algoritmos únicos e tamanhos únicos
        Set<Integer> tamanhosSet = new TreeSet<>();
        Set<String> algoritmosSet = new LinkedHashSet<>();
        double valorMaximo = 0.0;

        for (BenchmarkSummary s : lista) {
            tamanhosSet.add(s.getTamanhoVetor());
            algoritmosSet.add(s.getAlgoritmo());
            if (s.getTempoMedioMs() > valorMaximo) {
                valorMaximo = s.getTempoMedioMs();
            }
        }

        if (valorMaximo <= 0) valorMaximo = 1.0;
        valorMaximo *= 1.15; // Margem no topo

        List<Integer> tamanhos = new ArrayList<>(tamanhosSet);
        List<String> algoritmos = new ArrayList<>(algoritmosSet);

        // Grade e eixos
        desenharGradeEEixos(g2, margemEsquerda, margemSuperior, areaW, areaH, valorMaximo, "Tempo (ms)");

        // Desenho das barras agrupadas por tamanho
        int numGrupos = tamanhos.size();
        if (numGrupos == 0) return;

        double larguraGrupo = (double) areaW / numGrupos;
        int numBarrasPorGrupo = algoritmos.size();
        double larguraBarra = Math.min(36, (larguraGrupo * 0.75) / Math.max(1, numBarrasPorGrupo));

        for (int g = 0; g < numGrupos; g++) {
            int tamanho = tamanhos.get(g);
            double centroGrupo = margemEsquerda + (g + 0.5) * larguraGrupo;
            double inicioBarras = centroGrupo - (numBarrasPorGrupo * larguraBarra) / 2.0;

            // Rótulo do grupo no eixo X
            g2.setColor(new Color(60, 60, 60));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String rotuloX = "N=" + tamanho;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(rotuloX, (int) (centroGrupo - fm.stringWidth(rotuloX) / 2.0), margemSuperior + areaH + 20);

            for (int b = 0; b < numBarrasPorGrupo; b++) {
                String algo = algoritmos.get(b);
                double valor = 0.0;
                for (BenchmarkSummary s : lista) {
                    if (s.getTamanhoVetor() == tamanho && s.getAlgoritmo().equals(algo)) {
                        valor = s.getTempoMedioMs();
                        break;
                    }
                }

                double xBarra = inicioBarras + b * larguraBarra;
                double alturaBarra = (valor / valorMaximo) * areaH;
                double yBarra = margemSuperior + areaH - alturaBarra;

                Color cor = PALETA_CORES[b % PALETA_CORES.length];
                g2.setColor(cor);
                g2.fill(new Rectangle2D.Double(xBarra + 1, yBarra, larguraBarra - 2, alturaBarra));

                // Borda da barra
                g2.setColor(cor.darker());
                g2.draw(new Rectangle2D.Double(xBarra + 1, yBarra, larguraBarra - 2, alturaBarra));

                // Rótulo de valor sobre a barra se houver espaço
                if (alturaBarra > 18 && larguraBarra >= 24) {
                    g2.setColor(new Color(30, 30, 30));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String valStr = DF.format(valor);
                    FontMetrics fmb = g2.getFontMetrics();
                    g2.drawString(valStr, (int) (xBarra + (larguraBarra - fmb.stringWidth(valStr)) / 2), (int) (yBarra - 3));
                }
            }
        }

        // Desenha a Legenda na parte inferior
        desenharLegenda(g2, algoritmos, margemEsquerda, margemSuperior + areaH + 45, areaW);
    }

    private void desenharGraficoSpeedup(Graphics2D g2, List<BenchmarkSummary> lista, int w, int h) {
        int margemEsquerda = 70;
        int margemDireita = 40;
        int margemSuperior = 60;
        int margemInferior = 100;

        int areaW = w - margemEsquerda - margemDireita;
        int areaH = h - margemSuperior - margemInferior;

        // Título do Gráfico
        g2.setColor(new Color(33, 33, 33));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        String titulo = "Speedup vs Número de Threads (Speedup = T_serial / T_paralelo)";
        g2.drawString(titulo, margemEsquerda, 30);

        // Filtra apenas registros paralelos
        List<BenchmarkSummary> paralelos = new ArrayList<>();
        for (BenchmarkSummary s : lista) {
            if (s.isParalelo()) {
                paralelos.add(s);
            }
        }

        if (paralelos.isEmpty()) {
            desenharMensagemVazia(g2, w, h);
            return;
        }

        // Identifica maior speedup e threads
        double maxSpeedup = 1.0;
        Set<Integer> threadsSet = new TreeSet<>();
        Set<String> series = new LinkedHashSet<>();

        for (BenchmarkSummary s : paralelos) {
            threadsSet.add(s.getThreads());
            series.add(extrairNomeBase(s.getAlgoritmo()) + " (N=" + s.getTamanhoVetor() + ")");
            if (s.getSpeedup() > maxSpeedup) {
                maxSpeedup = s.getSpeedup();
            }
        }

        int maxThreads = 8;
        for (int t : threadsSet) {
            if (t > maxThreads) maxThreads = t;
        }

        double yMax = Math.max(maxThreads, maxSpeedup * 1.2);
        desenharGradeEEixos(g2, margemEsquerda, margemSuperior, areaW, areaH, yMax, "Speedup (S)");

        // Desenha linha de Speedup Linear Ideal (S = P)
        g2.setColor(new Color(180, 180, 180));
        Stroke originalStroke = g2.getStroke();
        float[] dash = {6.0f, 4.0f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

        double xP1 = margemEsquerda;
        double yP1 = margemSuperior + areaH - (1.0 / yMax) * areaH;
        double xPmax = margemEsquerda + areaW;
        double yPmax = margemSuperior + areaH - ((double) maxThreads / yMax) * areaH;
        g2.draw(new Line2D.Double(xP1, yP1, xPmax, yPmax));

        g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("Speedup Ideal (Linear: S = P)", (int) xPmax - 170, (int) yPmax - 8);
        g2.setStroke(originalStroke);

        // Desenha as curvas de speedup para cada série
        int corIdx = 0;
        List<String> seriesLista = new ArrayList<>(series);

        for (String serie : seriesLista) {
            Color cor = PALETA_CORES[corIdx % PALETA_CORES.length];
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(2.2f));

            Point anterior = null;

            // Inicia em 1 thread com Speedup = 1.0
            int x1 = margemEsquerda;
            int y1 = (int) (margemSuperior + areaH - (1.0 / yMax) * areaH);
            anterior = new Point(x1, y1);
            g2.fillOval(x1 - 4, y1 - 4, 8, 8);

            for (int t : threadsSet) {
                // Localiza o resumo correspondente
                for (BenchmarkSummary s : paralelos) {
                    String sNome = extrairNomeBase(s.getAlgoritmo()) + " (N=" + s.getTamanhoVetor() + ")";
                    if (sNome.equals(serie) && s.getThreads() == t) {
                        double sp = s.getSpeedup();
                        int xPt = margemEsquerda + (int) (((double) (t - 1) / (maxThreads - 1)) * areaW);
                        int yPt = (int) (margemSuperior + areaH - (sp / yMax) * areaH);

                        g2.drawLine(anterior.x, anterior.y, xPt, yPt);
                        g2.fillOval(xPt - 4, yPt - 4, 8, 8);

                        // Rótulo do ponto
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        g2.drawString(DF.format(sp) + "x", xPt - 12, yPt - 7);

                        anterior = new Point(xPt, yPt);
                        break;
                    }
                }
            }

            corIdx++;
        }

        g2.setStroke(originalStroke);
        desenharLegenda(g2, seriesLista, margemEsquerda, margemSuperior + areaH + 45, areaW);
    }

    private void desenharGradeEEixos(Graphics2D g2, int mx, int my, int w, int h, double maxVal, String rotuloY) {
        g2.setColor(new Color(235, 235, 235));
        int linhasGrade = 5;

        for (int i = 0; i <= linhasGrade; i++) {
            int y = my + h - (int) ((double) i / linhasGrade * h);
            g2.drawLine(mx, y, mx + w, y);

            double valorLinha = (double) i / linhasGrade * maxVal;
            g2.setColor(new Color(110, 110, 110));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String strVal = DF.format(valorLinha);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(strVal, mx - fm.stringWidth(strVal) - 8, y + 4);
            g2.setColor(new Color(235, 235, 235));
        }

        // Eixo Y e Eixo X
        g2.setColor(new Color(120, 120, 120));
        g2.drawLine(mx, my, mx, my + h);
        g2.drawLine(mx, my + h, mx + w, my + h);

        // Rótulo Eixo Y
        g2.setColor(new Color(50, 50, 50));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.drawString(rotuloY, mx - 65, my - 12);
    }

    private void desenharLegenda(Graphics2D g2, List<String> itens, int x, int y, int larguraMax) {
        int posX = x;
        int posY = y;
        int boxSize = 12;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < itens.size(); i++) {
            String item = itens.get(i);
            int larguraItem = boxSize + 6 + fm.stringWidth(item) + 16;

            if (posX + larguraItem > x + larguraMax) {
                posX = x;
                posY += 20;
            }

            g2.setColor(PALETA_CORES[i % PALETA_CORES.length]);
            g2.fillRect(posX, posY, boxSize, boxSize);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(posX, posY, boxSize, boxSize);

            g2.setColor(new Color(40, 40, 40));
            g2.drawString(item, posX + boxSize + 6, posY + 10);

            posX += larguraItem;
        }
    }

    private static String extrairNomeBase(String nomeCompleto) {
        if (nomeCompleto.startsWith("Bubble")) return "Bubble";
        if (nomeCompleto.startsWith("Insertion")) return "Insertion";
        if (nomeCompleto.startsWith("Merge")) return "Merge";
        if (nomeCompleto.startsWith("Quick")) return "Quick";
        return nomeCompleto;
    }
}
