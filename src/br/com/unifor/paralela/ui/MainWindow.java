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
 * Janela principal da aplicação (Java Swing nativo).
 * Gerencia a configuração dos experimentos, execução assíncrona via SwingWorker,
 * visualização tabular, gráficos dinâmicos e exportação de relatórios CSV.
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

    // Botões
    private JButton btnIniciar;
    private JButton btnCancelar;
    private JButton btnExportarCsv;

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

    // Estado e Benchmark
    private final List<BenchmarkSummary> listaResumos = new ArrayList<>();
    private final List<BenchmarkResult> listaAmostras = new ArrayList<>();
    private BenchmarkRunner runnerAtivo;
    private SwingWorker<Void, String> workerAtivo;

    public MainWindow() {
        setTitle("UNIFOR - Análise de Desempenho de Algoritmos (Serial vs Paralelo/Concorrente)");
        setSize(1200, 780);
        setMinimumSize(new Dimension(980, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        construirInterface();
    }

    private void construirInterface() {
        setLayout(new BorderLayout(8, 8));

        // Cabeçalho da aplicação
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(24, 43, 73));
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitulo = new JLabel("Estudo Comparativo de Algoritmos de Ordenação (Serial vs Paralelo)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setForeground(Color.WHITE);

        int nucleos = Runtime.getRuntime().availableProcessors();
        JLabel lblSub = new JLabel("Universidade de Fortaleza | Ambiente detectado: " + nucleos + " processadores lógicos");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(189, 195, 199));

        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Painel com Abas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabbedPane.addTab("Execução & Logs", criarPainelExecucao());
        tabbedPane.addTab("Tabela Estatística", criarPainelTabela());
        tabbedPane.addTab("Gráfico de Tempos (ms)", criarPainelGraficoTempo());
        tabbedPane.addTab("Gráfico de Speedup", criarPainelGraficoSpeedup());
        tabbedPane.addTab("Sobre o Projeto", criarPainelSobre());

        add(tabbedPane, BorderLayout.CENTER);

        // Rodapé com Barra de Progresso
        JPanel pnlFooter = new JPanel(new BorderLayout(8, 8));
        pnlFooter.setBorder(new EmptyBorder(6, 12, 8, 12));

        lblStatus = new JLabel("Pronto para executar experimentos.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(280, 20));

        pnlFooter.add(lblStatus, BorderLayout.CENTER);
        pnlFooter.add(progressBar, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel criarPainelExecucao() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Painel Lateral Esquerdo: Configurações
        JPanel pnlConfig = new JPanel();
        pnlConfig.setLayout(new BoxLayout(pnlConfig, BoxLayout.Y_AXIS));
        pnlConfig.setPreferredSize(new Dimension(320, 500));

        // 1. Algoritmos
        JPanel pnlAlgoritmos = new JPanel(new GridLayout(4, 1, 3, 3));
        pnlAlgoritmos.setBorder(new TitledBorder("1. Algoritmos a Avaliar"));
        chkBubble = new JCheckBox("Bubble Sort", true);
        chkInsertion = new JCheckBox("Insertion Sort", true);
        chkMerge = new JCheckBox("Merge Sort", true);
        chkQuick = new JCheckBox("Quick Sort", true);
        pnlAlgoritmos.add(chkBubble);
        pnlAlgoritmos.add(chkInsertion);
        pnlAlgoritmos.add(chkMerge);
        pnlAlgoritmos.add(chkQuick);
        pnlConfig.add(pnlAlgoritmos);

        // 2. Modos de Execução
        JPanel pnlModos = new JPanel(new GridLayout(4, 1, 3, 3));
        pnlModos.setBorder(new TitledBorder("2. Modos de Execução"));
        chkSerial = new JCheckBox("Serial (1 Thread)", true);
        chkParalelo2 = new JCheckBox("Paralelo (2 Threads)", true);
        chkParalelo4 = new JCheckBox("Paralelo (4 Threads)", true);
        chkParalelo8 = new JCheckBox("Paralelo (8 Threads)", true);
        pnlModos.add(chkSerial);
        pnlModos.add(chkParalelo2);
        pnlModos.add(chkParalelo4);
        pnlModos.add(chkParalelo8);
        pnlConfig.add(pnlModos);

        // 3. Natureza dos Dados
        JPanel pnlDist = new JPanel(new GridLayout(4, 1, 3, 3));
        pnlDist.setBorder(new TitledBorder("3. Natureza dos Dados"));
        chkDistAleatorio = new JCheckBox("Aleatório", true);
        chkDistQuase = new JCheckBox("Quase Ordenado (90%)", true);
        chkDistInverso = new JCheckBox("Inversamente Ordenado", true);
        chkDistOrdenado = new JCheckBox("Já Ordenado", false);
        pnlDist.add(chkDistAleatorio);
        pnlDist.add(chkDistQuase);
        pnlDist.add(chkDistInverso);
        pnlDist.add(chkDistOrdenado);
        pnlConfig.add(pnlDist);

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
        pnlConfig.add(pnlTamanhos);

        // 5. Parâmetros extras
        JPanel pnlAmostras = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAmostras.setBorder(new TitledBorder("5. Amostras por Teste (Min. 5)"));
        spnAmostras = new JSpinner(new SpinnerNumberModel(5, 5, 30, 1));
        pnlAmostras.add(new JLabel("Amostras:"));
        pnlAmostras.add(spnAmostras);
        pnlConfig.add(pnlAmostras);

        // Botões de Ação
        JPanel pnlBotoes = new JPanel(new GridLayout(3, 1, 6, 6));
        pnlBotoes.setBorder(new EmptyBorder(8, 4, 4, 4));

        btnIniciar = new JButton("Iniciar Benchmark");
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIniciar.setBackground(new Color(39, 174, 96));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.addActionListener(e -> iniciarBenchmark());

        btnCancelar = new JButton("Cancelar Execução");
        btnCancelar.setEnabled(false);
        btnCancelar.addActionListener(e -> cancelarBenchmark());

        btnExportarCsv = new JButton("Exportar CSV");
        btnExportarCsv.setEnabled(false);
        btnExportarCsv.addActionListener(e -> exportarParaCsv());

        pnlBotoes.add(btnIniciar);
        pnlBotoes.add(btnCancelar);
        pnlBotoes.add(btnExportarCsv);
        pnlConfig.add(pnlBotoes);

        JScrollPane scrollConfig = new JScrollPane(pnlConfig);
        scrollConfig.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        painel.add(scrollConfig, BorderLayout.WEST);

        // Painel Central: Logs em Tempo Real
        JPanel pnlLogCentral = new JPanel(new BorderLayout(4, 4));
        pnlLogCentral.setBorder(new TitledBorder("Console de Execução & Rastreamento em Tempo Real"));

        txtLogs = new JTextArea();
        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLogs.setBackground(new Color(245, 247, 250));
        txtLogs.setMargin(new Insets(8, 8, 8, 8));

        log("Aplicação iniciada com sucesso. Selecione os parâmetros desejados e clique em 'Iniciar Benchmark'.");

        JScrollPane scrollLogs = new JScrollPane(txtLogs);
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
        tableResultados.setRowHeight(22);
        tableResultados.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Alinhamento central para colunas numéricas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < colunas.length; i++) {
            tableResultados.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollTable = new JScrollPane(tableResultados);
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
        painel.add(chartTempoPanel, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelGraficoSpeedup() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chartSpeedupPanel = new ChartPanel(ChartPanel.ChartType.SPEEDUP_ESCALABILIDADE);
        painel.add(chartSpeedupPanel, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelSobre() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText("<html><body style='font-family: Segoe UI, sans-serif; font-size: 13px; color: #333;'>"
                + "<h2 style='color: #1a365d;'>Universidade de Fortaleza (UNIFOR)</h2>"
                + "<h3 style='color: #2b6cb0;'>Computação Concorrente e Paralela</h3>"
                + "<p><b>Projeto:</b> Análise de Desempenho de Algoritmos de Ordenação em Ambientes Seriais e Paralelos</p>"
                + "<hr>"
                + "<h4>Algoritmos Implementados:</h4>"
                + "<ul>"
                + "<li><b>Bubble Sort:</b> Serial vs Paralelo via <i>Odd-Even Transposition Sort</i> com sincronização CyclicBarrier.</li>"
                + "<li><b>Insertion Sort:</b> Serial vs Paralelo via <i>Block Partitioning</i> com mescla hierárquica concorrente.</li>"
                + "<li><b>Merge Sort:</b> Serial vs Paralelo via <i>ForkJoinPool</i> (RecursiveAction) com limiar adaptativo.</li>"
                + "<li><b>Quick Sort:</b> Serial vs Paralelo via <i>ForkJoinPool</i> com pivô mediana de três e particionamento de Hoare.</li>"
                + "</ul>"
                + "<h4>Métricas e Fórmulas:</h4>"
                + "<ul>"
                + "<li><b>Speedup (S):</b> S = T_serial / T_paralelo</li>"
                + "<li><b>Eficiência (E):</b> E = S / P (onde P é o número de threads)</li>"
                + "<li><b>Amostragem:</b> Mínimo de 5 amostras por teste após warm-up da JVM, com desvio padrão amostral.</li>"
                + "</ul>"
                + "</body></html>");

        painel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        return painel;
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
        progressBar.setValue(0);

        log("Iniciando bateria de testes com " + algoritmos.size() + " configurações de algoritmos, "
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
                    lblStatus.setText("Benchmark cancelado.");
                    log("Benchmark interrompido pelo usuário.");
                } else {
                    lblStatus.setText("Benchmark finalizado com sucesso!");
                    log("Bateria de experimentos concluída! Os dados estão prontos para visualização e exportação.");
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
