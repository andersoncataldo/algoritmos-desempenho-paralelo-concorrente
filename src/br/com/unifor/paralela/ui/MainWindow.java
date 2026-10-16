package br.com.unifor.paralela.ui;

import br.com.unifor.paralela.algorithm.Sorter;
import br.com.unifor.paralela.algorithm.bubblesort.BubbleSortParallel;
import br.com.unifor.paralela.algorithm.bubblesort.BubbleSortSerial;
import br.com.unifor.paralela.algorithm.insertionsort.InsertionSortParallel;
import br.com.unifor.paralela.algorithm.insertionsort.InsertionSortSerial;
import br.com.unifor.paralela.algorithm.mergesort.MergeSortParallel;
import br.com.unifor.paralela.algorithm.mergesort.MergeSortSerial;
import br.com.unifor.paralela.algorithm.quicksort.QuickSortParallel;
import br.com.unifor.paralela.algorithm.quicksort.QuickSortSerial;
import br.com.unifor.paralela.benchmark.BenchmarkRunner;
import br.com.unifor.paralela.benchmark.CsvExporter;
import br.com.unifor.paralela.model.BenchmarkResult;
import br.com.unifor.paralela.model.BenchmarkSummary;
import br.com.unifor.paralela.model.DataDistribution;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface gráfica principal em Java Swing nativo.
 * Totalmente reformulada para máxima usabilidade, evitando cortes de tela,
 * com barra de ações fixas sempre visíveis, presets de um clique,
 * suporte a barras de rolagem horizontal/vertical e design moderno.
 */
public class MainWindow extends JFrame {

    private static final DecimalFormat DF = new DecimalFormat("0.000");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Algoritmos Checkboxes
    private JCheckBox chkBubble;
    private JCheckBox chkInsertion;
    private JCheckBox chkMerge;
    private JCheckBox chkQuick;

    // Modos de Execução Checkboxes
    private JCheckBox chkSerial;
    private JCheckBox chkParalelo2;
    private JCheckBox chkParalelo4;
    private JCheckBox chkParalelo8;

    // Distribuições
    private JCheckBox chkDistAleatorio;
    private JCheckBox chkDistQuase;
    private JCheckBox chkDistInverso;
    private JCheckBox chkDistOrdenado;

    // Tamanhos
    private JCheckBox chkTamanho1000;
    private JCheckBox chkTamanho5000;
    private JCheckBox chkTamanho10000;
    private JCheckBox chkTamanho25000;
    private JCheckBox chkTamanho50000;

    // Amostras Spinner
    private JSpinner spnAmostras;

    // Botões Principais de Ação (Sempre visíveis no topo!)
    private JButton btnIniciar;
    private JButton btnCancelar;
    private JButton btnExportarCsv;
    private JLabel lblStatusBadge;

    // Progresso e Logs
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private JTextArea txtLogs;

    // Tabela e Gráficos
    private DefaultTableModel tableModel;
    private JTable tableResultados;
    private ChartPanel chartTempoPanel;
    private ChartPanel chartSpeedupPanel;
    private JComboBox<DataDistribution> cmbDistroGrafico;
    private JTabbedPane tabbedPane;

    // Estado e Benchmark
    private final List<BenchmarkSummary> listaResumos = new ArrayList<>();
    private final List<BenchmarkResult> listaAmostras = new ArrayList<>();
    private BenchmarkRunner runnerAtivo;
    private SwingWorker<Void, String> workerAtivo;

    public MainWindow() {
        setTitle("UNIFOR - Análise de Desempenho de Algoritmos (Serial vs Paralelo/Concorrente)");
        setSize(1240, 820);
        setMinimumSize(new Dimension(940, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        construirInterface();
    }

    private void construirInterface() {
        setLayout(new BorderLayout(0, 0));

        // Cabeçalho institucional superior
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(24, 43, 73));
        pnlHeader.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel lblTitulo = new JLabel("Estudo Comparativo de Algoritmos de Ordenação (Serial vs Paralelo)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        int nucleos = Runtime.getRuntime().availableProcessors();
        JLabel lblSub = new JLabel("Universidade de Fortaleza (UNIFOR) | Hardware Detectado: Intel Core i5 | " + nucleos + " processadores lógicos");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(189, 195, 199));

        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Barra de Ações Superior FIXA (SEMPRE VISÍVEL no topo da tela!)
        JPanel pnlAcoesFixas = criarBarraAcoesFixas();
        
        // Painel central com abas
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("⚡ Execução & Logs", criarPainelExecucao());
        tabbedPane.addTab("📊 Tabela Estatística", criarPainelTabela());
        tabbedPane.addTab("📈 Gráfico de Tempos (ms)", criarPainelGraficoTempo());
        tabbedPane.addTab("🚀 Gráfico de Speedup", criarPainelGraficoSpeedup());
        tabbedPane.addTab("ℹ️ Sobre o Projeto", criarPainelSobre());

        // Painel Central englobando a barra de ações e as abas
        JPanel pnlCentro = new JPanel(new BorderLayout(0, 0));
        pnlCentro.add(pnlAcoesFixas, BorderLayout.NORTH);
        pnlCentro.add(tabbedPane, BorderLayout.CENTER);
        add(pnlCentro, BorderLayout.CENTER);

        // Rodapé de Status e Progresso
        JPanel pnlFooter = new JPanel(new BorderLayout(10, 0));
        pnlFooter.setBorder(new EmptyBorder(6, 16, 6, 16));
        pnlFooter.setBackground(new Color(245, 245, 245));

        lblStatus = new JLabel("Pronto para executar. Clique no botão verde 'INICIAR BENCHMARK' acima.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 22));
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));

        pnlFooter.add(lblStatus, BorderLayout.CENTER);
        pnlFooter.add(progressBar, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    /**
     * Barra fixa com os botões principais de ação.
     * Esta barra NUNCA é cortada por rolagem e fica sempre imediatamente acessível ao usuário.
     */
    private JPanel criarBarraAcoesFixas() {
        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBackground(new Color(238, 242, 246));
        bar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 215, 222), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));

        // Botões de Execução (Esquerda)
        JPanel pnlBotoesEsq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBotoesEsq.setOpaque(false);

        btnIniciar = new JButton("▶  INICIAR BENCHMARK");
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIniciar.setBackground(new Color(39, 174, 96));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFocusPainted(false);
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciar.setPreferredSize(new Dimension(200, 36));
        btnIniciar.addActionListener(e -> iniciarBenchmark());

        btnCancelar = new JButton("⏹  Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelar.setBackground(new Color(231, 76, 60));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setEnabled(false);
        btnCancelar.setPreferredSize(new Dimension(110, 36));
        btnCancelar.addActionListener(e -> cancelarBenchmark());

        btnExportarCsv = new JButton("💾  Exportar CSV");
        btnExportarCsv.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportarCsv.setBackground(new Color(41, 128, 185));
        btnExportarCsv.setForeground(Color.WHITE);
        btnExportarCsv.setFocusPainted(false);
        btnExportarCsv.setEnabled(false);
        btnExportarCsv.setPreferredSize(new Dimension(130, 36));
        btnExportarCsv.addActionListener(e -> exportarParaCsv());

        lblStatusBadge = new JLabel("● PRONTO");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusBadge.setForeground(new Color(39, 174, 96));
        lblStatusBadge.setBorder(new EmptyBorder(0, 10, 0, 0));

        pnlBotoesEsq.add(btnIniciar);
        pnlBotoesEsq.add(btnCancelar);
        pnlBotoesEsq.add(btnExportarCsv);
        pnlBotoesEsq.add(lblStatusBadge);

        // Presets Rápidos (Direita)
        JPanel pnlPresets = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlPresets.setOpaque(false);

        JLabel lblPreset = new JLabel("Presets Rápidos:");
        lblPreset.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnPresetRapido = new JButton("⚡ Rápido (1k-5k)");
        btnPresetRapido.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPresetRapido.addActionListener(e -> aplicarPresetRapido());

        JButton btnPresetPadrao = new JButton("🔬 Padrão (1k-10k)");
        btnPresetPadrao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPresetPadrao.addActionListener(e -> aplicarPresetPadrao());

        JButton btnPresetCompleto = new JButton("🚀 Completo (1k-50k)");
        btnPresetCompleto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPresetCompleto.addActionListener(e -> aplicarPresetCompleto());

        pnlPresets.add(lblPreset);
        pnlPresets.add(btnPresetRapido);
        pnlPresets.add(btnPresetPadrao);
        pnlPresets.add(btnPresetCompleto);

        bar.add(pnlBotoesEsq, BorderLayout.WEST);
        bar.add(pnlPresets, BorderLayout.EAST);

        return bar;
    }

    private JPanel criarPainelExecucao() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Painel Lateral Esquerdo: Configurações com ROLAGEM DUPLA (Vertical e Horizontal)
        JPanel pnlConfigConteudo = new JPanel();
        pnlConfigConteudo.setLayout(new BoxLayout(pnlConfigConteudo, BoxLayout.Y_AXIS));
        pnlConfigConteudo.setBorder(new EmptyBorder(4, 4, 4, 4));

        // 1. Algoritmos
        JPanel pnlAlgoritmos = new JPanel(new GridLayout(4, 1, 2, 2));
        pnlAlgoritmos.setBorder(new TitledBorder("1. Algoritmos a Avaliar"));
        chkBubble = new JCheckBox("Bubble Sort", true);
        chkInsertion = new JCheckBox("Insertion Sort", true);
        chkMerge = new JCheckBox("Merge Sort", true);
        chkQuick = new JCheckBox("Quick Sort", true);
        pnlAlgoritmos.add(chkBubble);
        pnlAlgoritmos.add(chkInsertion);
        pnlAlgoritmos.add(chkMerge);
        pnlAlgoritmos.add(chkQuick);
        pnlConfigConteudo.add(pnlAlgoritmos);

        // 2. Modos de Execução
        JPanel pnlModos = new JPanel(new GridLayout(4, 1, 2, 2));
        pnlModos.setBorder(new TitledBorder("2. Modos de Execução"));
        chkSerial = new JCheckBox("Serial (1 Thread)", true);
        chkParalelo2 = new JCheckBox("Paralelo (2 Threads)", true);
        chkParalelo4 = new JCheckBox("Paralelo (4 Threads)", true);
        chkParalelo8 = new JCheckBox("Paralelo (8 Threads)", true);
        pnlModos.add(chkSerial);
        pnlModos.add(chkParalelo2);
        pnlModos.add(chkParalelo4);
        pnlModos.add(chkParalelo8);
        pnlConfigConteudo.add(pnlModos);

        // 3. Natureza dos Dados
        JPanel pnlDist = new JPanel(new GridLayout(4, 1, 2, 2));
        pnlDist.setBorder(new TitledBorder("3. Natureza dos Dados"));
        chkDistAleatorio = new JCheckBox("Aleatório", true);
        chkDistQuase = new JCheckBox("Quase Ordenado (90%)", true);
        chkDistInverso = new JCheckBox("Inversamente Ordenado", true);
        chkDistOrdenado = new JCheckBox("Já Ordenado", false);
        pnlDist.add(chkDistAleatorio);
        pnlDist.add(chkDistQuase);
        pnlDist.add(chkDistInverso);
        pnlDist.add(chkDistOrdenado);
        pnlConfigConteudo.add(pnlDist);

        // 4. Tamanho dos Vetores
        JPanel pnlTamanhos = new JPanel(new GridLayout(5, 1, 2, 2));
        pnlTamanhos.setBorder(new TitledBorder("4. Tamanhos dos Vetores (N)"));
        chkTamanho1000 = new JCheckBox("1.000 elementos", true);
        chkTamanho5000 = new JCheckBox("5.000 elementos", true);
        chkTamanho10000 = new JCheckBox("10.000 elementos", true);
        chkTamanho25000 = new JCheckBox("25.000 elementos", false);
        chkTamanho50000 = new JCheckBox("50.000 elementos", false);
        pnlTamanhos.add(chkTamanho1000);
        pnlTamanhos.add(chkTamanho5000);
        pnlTamanhos.add(chkTamanho10000);
        pnlTamanhos.add(chkTamanho25000);
        pnlTamanhos.add(chkTamanho50000);
        pnlConfigConteudo.add(pnlTamanhos);

        // 5. Parâmetros extras
        JPanel pnlAmostras = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAmostras.setBorder(new TitledBorder("5. Amostras por Teste (Min. 5)"));
        spnAmostras = new JSpinner(new SpinnerNumberModel(5, 5, 30, 1));
        spnAmostras.setPreferredSize(new Dimension(65, 26));
        pnlAmostras.add(new JLabel("Amostras:"));
        pnlAmostras.add(spnAmostras);
        pnlConfigConteudo.add(pnlAmostras);

        // ScrollPane robusto para o painel de configurações (COM ROLAGEM HORIZONTAL E VERTICAL)
        JScrollPane scrollConfig = new JScrollPane(pnlConfigConteudo);
        scrollConfig.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollConfig.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollConfig.setPreferredSize(new Dimension(310, 480));
        scrollConfig.getVerticalScrollBar().setUnitIncrement(16);
        painel.add(scrollConfig, BorderLayout.WEST);

        // Painel Central: Logs em Tempo Real
        JPanel pnlLogCentral = new JPanel(new BorderLayout(4, 4));
        pnlLogCentral.setBorder(new TitledBorder("Console de Execução & Rastreamento em Tempo Real"));

        // Barra de ferramentas do log
        JPanel pnlToolLog = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        JButton btnLimparLog = new JButton("Limpar Console");
        btnLimparLog.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLimparLog.addActionListener(e -> txtLogs.setText(""));
        pnlToolLog.add(btnLimparLog);
        pnlLogCentral.add(pnlToolLog, BorderLayout.NORTH);

        txtLogs = new JTextArea();
        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLogs.setBackground(new Color(248, 249, 250));
        txtLogs.setMargin(new Insets(8, 10, 8, 10));

        log("Pronto. Selecione os algoritmos desejados e clique no botão verde 'INICIAR BENCHMARK' acima.");

        JScrollPane scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollLogs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollLogs.getVerticalScrollBar().setUnitIncrement(16);

        pnlLogCentral.add(scrollLogs, BorderLayout.CENTER);
        painel.add(pnlLogCentral, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelTabela() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] colunas = {
                "Algoritmo", "Modo", "Threads", "Tamanho (N)", "Distribuição",
                "Amostras", "Tempo Médio (ms)", "Desvio Padrão", "Mínimo (ms)", "Máximo (ms)", "Speedup", "Eficiência"
        };

        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableResultados = new JTable(tableModel);
        tableResultados.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableResultados.setRowHeight(24);
        tableResultados.setAutoCreateRowSorter(true); // Permite ordenação clicando nas colunas!
        tableResultados.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableResultados.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Estilização com zebra striping
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < colunas.length; i++) {
            tableResultados.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollTable = new JScrollPane(tableResultados);
        scrollTable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        painel.add(scrollTable, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelGraficoTempo() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFiltro.add(new JLabel("Filtrar por Natureza dos Dados:"));

        cmbDistroGrafico = new JComboBox<>(DataDistribution.values());
        cmbDistroGrafico.addActionListener(e -> {
            DataDistribution selecionada = (DataDistribution) cmbDistroGrafico.getSelectedItem();
            chartTempoPanel.atualizarDados(listaResumos, selecionada);
            chartSpeedupPanel.atualizarDados(listaResumos, selecionada);
        });
        pnlFiltro.add(cmbDistroGrafico);

        painel.add(pnlFiltro, BorderLayout.NORTH);

        chartTempoPanel = new ChartPanel(ChartPanel.ChartType.TEMPO_EXECUCAO);
        
        JScrollPane scrollChart = new JScrollPane(chartTempoPanel);
        scrollChart.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollChart.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        painel.add(scrollChart, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelGraficoSpeedup() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chartSpeedupPanel = new ChartPanel(ChartPanel.ChartType.SPEEDUP_ESCALABILIDADE);
        
        JScrollPane scrollChart = new JScrollPane(chartSpeedupPanel);
        scrollChart.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollChart.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        painel.add(scrollChart, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelSobre() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText("<html><body style='font-family: Segoe UI, sans-serif; font-size: 13px; color: #333; margin: 15px;'>"
                + "<h2 style='color: #1a365d;'>Universidade de Fortaleza (UNIFOR)</h2>"
                + "<h3 style='color: #2b6cb0;'>Computação Concorrente e Paralela</h3>"
                + "<p><b>Aluno:</b> Anderson Viana Cataldo</p>"
                + "<p><b>Repositório:</b> <a href='https://github.com/andersoncataldo/algoritmos-desempenho-paralelo-concorrente.git'>github.com/andersoncataldo/algoritmos-desempenho-paralelo-concorrente</a></p>"
                + "<hr>"
                + "<h4>Algoritmos e Otimizações de Alto Desempenho:</h4>"
                + "<ul>"
                + "<li><b>Bubble Sort:</b> Serial vs Paralelo via <i>Odd-Even Transposition Sort</i> com sincronização CyclicBarrier.</li>"
                + "<li><b>Insertion Sort:</b> Serial vs Paralelo via <i>Block Partitioning</i> com mescla hierárquica concorrente.</li>"
                + "<li><b>Merge Sort:</b> Serial vs Paralelo via <i>ForkJoinPool</i> com vetor auxiliar único, atalho O(n) e cutoff para Insertion Sort em pequenas partições.</li>"
                + "<li><b>Quick Sort:</b> Serial vs Paralelo com pivô por mediana de três, eliminação de recursão em cauda e cutoff para Insertion Sort (&lt; 32 itens).</li>"
                + "</ul>"
                + "<h4>Métricas e Fórmulas:</h4>"
                + "<ul>"
                + "<li><b>Speedup (S):</b> S = T_serial / T_paralelo</li>"
                + "<li><b>Eficiência (E):</b> E = S / P (onde P é o número de threads)</li>"
                + "<li><b>Amostragem:</b> Mínimo de 5 amostras por teste após warm-up da JVM, com desvio padrão amostral.</li>"
                + "</ul>"
                + "</body></html>");

        JScrollPane scrollSobre = new JScrollPane(editorPane);
        scrollSobre.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        painel.add(scrollSobre, BorderLayout.CENTER);
        return painel;
    }

    private void aplicarPresetRapido() {
        chkBubble.setSelected(true);
        chkInsertion.setSelected(true);
        chkMerge.setSelected(true);
        chkQuick.setSelected(true);

        chkSerial.setSelected(true);
        chkParalelo2.setSelected(true);
        chkParalelo4.setSelected(true);
        chkParalelo8.setSelected(false);

        chkDistAleatorio.setSelected(true);
        chkDistQuase.setSelected(false);
        chkDistInverso.setSelected(true);
        chkDistOrdenado.setSelected(false);

        chkTamanho1000.setSelected(true);
        chkTamanho5000.setSelected(true);
        chkTamanho10000.setSelected(false);
        chkTamanho25000.setSelected(false);
        chkTamanho50000.setSelected(false);

        spnAmostras.setValue(5);
        log("Preset Rápido aplicado: N=1.000 e 5.000 com dados Aleatórios e Inversos.");
    }

    private void aplicarPresetPadrao() {
        chkBubble.setSelected(true);
        chkInsertion.setSelected(true);
        chkMerge.setSelected(true);
        chkQuick.setSelected(true);

        chkSerial.setSelected(true);
        chkParalelo2.setSelected(true);
        chkParalelo4.setSelected(true);
        chkParalelo8.setSelected(true);

        chkDistAleatorio.setSelected(true);
        chkDistQuase.setSelected(true);
        chkDistInverso.setSelected(true);
        chkDistOrdenado.setSelected(false);

        chkTamanho1000.setSelected(true);
        chkTamanho5000.setSelected(true);
        chkTamanho10000.setSelected(true);
        chkTamanho25000.setSelected(false);
        chkTamanho50000.setSelected(false);

        spnAmostras.setValue(5);
        log("Preset Padrão aplicado: N=1.000, 5.000 e 10.000 com threads 1, 2, 4 e 8.");
    }

    private void aplicarPresetCompleto() {
        chkBubble.setSelected(true);
        chkInsertion.setSelected(true);
        chkMerge.setSelected(true);
        chkQuick.setSelected(true);

        chkSerial.setSelected(true);
        chkParalelo2.setSelected(true);
        chkParalelo4.setSelected(true);
        chkParalelo8.setSelected(true);

        chkDistAleatorio.setSelected(true);
        chkDistQuase.setSelected(true);
        chkDistInverso.setSelected(true);
        chkDistOrdenado.setSelected(true);

        chkTamanho1000.setSelected(true);
        chkTamanho5000.setSelected(true);
        chkTamanho10000.setSelected(true);
        chkTamanho25000.setSelected(true);
        chkTamanho50000.setSelected(true);

        spnAmostras.setValue(5);
        log("Preset Completo aplicado: Todos os tamanhos (até 50k) e todas as distribuições.");
    }

    private void iniciarBenchmark() {
        List<Sorter> algoritmos = coletarAlgoritmosSelecionados();
        List<Integer> tamanhos = coletarTamanhosSelecionados();
        List<DataDistribution> distros = coletarDistribuicoesSelecionadas();
        int amostras = (Integer) spnAmostras.getValue();

        if (algoritmos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos um algoritmo e modo de execução.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tamanhos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos um tamanho de vetor.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (distros.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos uma natureza de dados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limpa estado anterior
        listaResumos.clear();
        listaAmostras.clear();
        tableModel.setRowCount(0);
        txtLogs.setText("");

        btnIniciar.setEnabled(false);
        btnCancelar.setEnabled(true);
        btnExportarCsv.setEnabled(false);
        lblStatusBadge.setText("● EXECUTANDO...");
        lblStatusBadge.setForeground(new Color(230, 126, 34));
        progressBar.setValue(0);

        log("Iniciando benchmark com " + algoritmos.size() + " configurações, "
                + tamanhos.size() + " tamanhos e " + distros.size() + " distribuições (" + amostras + " amostras cada)...");

        runnerAtivo = new BenchmarkRunner();

        workerAtivo = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                runnerAtivo.executar(algoritmos, tamanhos, distros, amostras, new BenchmarkRunner.ProgressListener() {
                    @Override
                    public void onProgress(int atual, int total, String mensagem) {
                        int percent = (int) (((double) atual / total) * 100);
                        publish("PROGRESS|" + percent + "|" + mensagem);
                    }

                    @Override
                    public void onSampleFinished(BenchmarkResult res) {
                        listaAmostras.add(res);
                    }

                    @Override
                    public void onSummaryFinished(BenchmarkSummary resumo) {
                        listaResumos.add(resumo);
                        publish("SUMMARY|" + resumo.getAlgoritmo());
                    }
                });
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    if (chunk.startsWith("PROGRESS|")) {
                        String[] partes = chunk.split("\\|", 3);
                        int p = Integer.parseInt(partes[1]);
                        progressBar.setValue(p);
                        lblStatus.setText(partes[2]);
                        log(partes[2]);
                    } else if (chunk.startsWith("SUMMARY|")) {
                        atualizarTabelaECharts();
                    }
                }
            }

            @Override
            protected void done() {
                btnIniciar.setEnabled(true);
                btnCancelar.setEnabled(false);
                btnExportarCsv.setEnabled(!listaResumos.isEmpty());

                if (runnerAtivo.isCancelado()) {
                    lblStatusBadge.setText("● CANCELADO");
                    lblStatusBadge.setForeground(new Color(231, 76, 60));
                    lblStatus.setText("Benchmark cancelado pelo usuário.");
                    log("Benchmark cancelado.");
                } else {
                    lblStatusBadge.setText("● CONCLUÍDO");
                    lblStatusBadge.setForeground(new Color(39, 174, 96));
                    lblStatus.setText("Benchmark finalizado com sucesso!");
                    log("Bateria de testes concluída com sucesso! Navegue pelas abas acima para ver os gráficos.");
                    atualizarTabelaECharts();
                }
            }
        };

        workerAtivo.execute();
    }

    private void cancelarBenchmark() {
        if (runnerAtivo != null) {
            runnerAtivo.cancelar();
        }
        if (workerAtivo != null) {
            workerAtivo.cancel(true);
        }
    }

    private void atualizarTabelaECharts() {
        tableModel.setRowCount(0);
        for (BenchmarkSummary s : listaResumos) {
            tableModel.addRow(new Object[]{
                    s.getAlgoritmo(),
                    s.isParalelo() ? "Paralelo" : "Serial",
                    s.getThreads(),
                    s.getTamanhoVetor(),
                    s.getDistribuicao().getDescricao(),
                    s.getTotalAmostras(),
                    DF.format(s.getTempoMedioMs()),
                    DF.format(s.getDesvioPadraoMs()),
                    DF.format(s.getTempoMinimoMs()),
                    DF.format(s.getTempoMaximoMs()),
                    DF.format(s.getSpeedup()) + "x",
                    DF.format(s.getEficiencia() * 100) + "%"
            });
        }

        DataDistribution distSelecionada = (DataDistribution) cmbDistroGrafico.getSelectedItem();
        chartTempoPanel.atualizarDados(listaResumos, distSelecionada);
        chartSpeedupPanel.atualizarDados(listaResumos, distSelecionada);
    }

    private void exportarParaCsv() {
        if (listaResumos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum resultado disponível para exportação.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File pastaOutput = new File("output");
        try {
            File arqResumo = CsvExporter.exportarResumo(listaResumos, pastaOutput);
            File arqAmostras = CsvExporter.exportarAmostras(listaAmostras, pastaOutput);

            String msg = "Arquivos CSV gerados com sucesso na pasta 'output/':\n- "
                    + arqResumo.getName() + "\n- " + arqAmostras.getName();
            log(msg);
            JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar CSV: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Sorter> coletarAlgoritmosSelecionados() {
        List<Sorter> lista = new ArrayList<>();

        if (chkBubble.isSelected()) {
            if (chkSerial.isSelected()) lista.add(new BubbleSortSerial());
            if (chkParalelo2.isSelected()) lista.add(new BubbleSortParallel(2));
            if (chkParalelo4.isSelected()) lista.add(new BubbleSortParallel(4));
            if (chkParalelo8.isSelected()) lista.add(new BubbleSortParallel(8));
        }

        if (chkInsertion.isSelected()) {
            if (chkSerial.isSelected()) lista.add(new InsertionSortSerial());
            if (chkParalelo2.isSelected()) lista.add(new InsertionSortParallel(2));
            if (chkParalelo4.isSelected()) lista.add(new InsertionSortParallel(4));
            if (chkParalelo8.isSelected()) lista.add(new InsertionSortParallel(8));
        }

        if (chkMerge.isSelected()) {
            if (chkSerial.isSelected()) lista.add(new MergeSortSerial());
            if (chkParalelo2.isSelected()) lista.add(new MergeSortParallel(2));
            if (chkParalelo4.isSelected()) lista.add(new MergeSortParallel(4));
            if (chkParalelo8.isSelected()) lista.add(new MergeSortParallel(8));
        }

        if (chkQuick.isSelected()) {
            if (chkSerial.isSelected()) lista.add(new QuickSortSerial());
            if (chkParalelo2.isSelected()) lista.add(new QuickSortParallel(2));
            if (chkParalelo4.isSelected()) lista.add(new QuickSortParallel(4));
            if (chkParalelo8.isSelected()) lista.add(new QuickSortParallel(8));
        }

        return lista;
    }

    private List<Integer> coletarTamanhosSelecionados() {
        List<Integer> tamanhos = new ArrayList<>();
        if (chkTamanho1000.isSelected()) tamanhos.add(1000);
        if (chkTamanho5000.isSelected()) tamanhos.add(5000);
        if (chkTamanho10000.isSelected()) tamanhos.add(10000);
        if (chkTamanho25000.isSelected()) tamanhos.add(25000);
        if (chkTamanho50000.isSelected()) tamanhos.add(50000);
        return tamanhos;
    }

    private List<DataDistribution> coletarDistribuicoesSelecionadas() {
        List<DataDistribution> distros = new ArrayList<>();
        if (chkDistAleatorio.isSelected()) distros.add(DataDistribution.ALEATORIO);
        if (chkDistQuase.isSelected()) distros.add(DataDistribution.QUASE_ORDENADO);
        if (chkDistInverso.isSelected()) distros.add(DataDistribution.INVERSO);
        if (chkDistOrdenado.isSelected()) distros.add(DataDistribution.ORDENADO);
        return distros;
    }

    private void log(String msg) {
        String hora = LocalTime.now().format(TIME_FMT);
        txtLogs.append("[" + hora + "] " + msg + "\n");
        txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
    }
}
