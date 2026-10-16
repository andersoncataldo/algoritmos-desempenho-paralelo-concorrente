import zipfile
import xml.etree.ElementTree as ET
import os
import shutil
import copy

TEMPLATE_PATH = "Modelo-Artigo.docx"
OUTPUT_PATH = "Relatorio-Artigo-Ordenacao.docx"

NS = {
    'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
    'w14': 'http://schemas.microsoft.com/office/word/2010/wordml'
}
for prefix, uri in NS.items():
    ET.register_namespace(prefix, uri)

def set_paragraph_text(p, text, font_name="Arial", size_half_points="22", bold=False, italic=False, line_spacing=360, align="both"):
    """
    Atualiza o texto de um parágrafo preservando suas propriedades estruturais (pPr),
    aplicando a formatação exigida (Arial 11, espaçamento 1,5, justificado).
    """
    ns_w = NS['w']
    pPr = p.find(f"{{{ns_w}}}pPr")
    if pPr is None:
        pPr = ET.SubElement(p, f"{{{ns_w}}}pPr")
        
    # Ajusta espaçamento entre linhas (360 = 1,5 linhas)
    spacing = pPr.find(f"{{{ns_w}}}spacing")
    if spacing is None:
        spacing = ET.SubElement(pPr, f"{{{ns_w}}}spacing")
    if line_spacing:
        spacing.set(f"{{{ns_w}}}line", str(line_spacing))
        spacing.set(f"{{{ns_w}}}lineRule", "auto")
        spacing.set(f"{{{ns_w}}}after", "120")
        
    # Alinhamento justificado
    if align:
        jc = pPr.find(f"{{{ns_w}}}jc")
        if jc is None:
            jc = ET.SubElement(pPr, f"{{{ns_w}}}jc")
        jc.set(f"{{{ns_w}}}val", align)
        
    # Remove runs antigos
    for r in list(p.findall(f"{{{ns_w}}}r")):
        p.remove(r)
        
    # Cria run com texto formatado
    r = ET.SubElement(p, f"{{{ns_w}}}r")
    rPr = ET.SubElement(r, f"{{{ns_w}}}rPr")
    
    rFonts = ET.SubElement(rPr, f"{{{ns_w}}}rFonts")
    rFonts.set(f"{{{ns_w}}}ascii", font_name)
    rFonts.set(f"{{{ns_w}}}hAnsi", font_name)
    rFonts.set(f"{{{ns_w}}}cs", font_name)
    
    sz = ET.SubElement(rPr, f"{{{ns_w}}}sz")
    sz.set(f"{{{ns_w}}}val", str(size_half_points))
    szCs = ET.SubElement(rPr, f"{{{ns_w}}}szCs")
    szCs.set(f"{{{ns_w}}}val", str(size_half_points))
    
    if bold:
        ET.SubElement(rPr, f"{{{ns_w}}}b")
    if italic:
        ET.SubElement(rPr, f"{{{ns_w}}}i")
        
    lang = ET.SubElement(rPr, f"{{{ns_w}}}lang")
    lang.set(f"{{{ns_w}}}val", "pt-BR")
    
    t = ET.SubElement(r, f"{{{ns_w}}}t")
    t.text = text
    t.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")

def criar_tabela_formatada(dados_linhas):
    ns_w = NS['w']
    tbl = ET.Element(f"{{{ns_w}}}tbl")
    tblPr = ET.SubElement(tbl, f"{{{ns_w}}}tblPr")
    
    tblStyle = ET.SubElement(tblPr, f"{{{ns_w}}}tblStyle")
    tblStyle.set(f"{{{ns_w}}}val", "TableGrid")
    
    tblW = ET.SubElement(tblPr, f"{{{ns_w}}}tblW")
    tblW.set(f"{{{ns_w}}}w", "0")
    tblW.set(f"{{{ns_w}}}type", "auto")
    
    jc = ET.SubElement(tblPr, f"{{{ns_w}}}jc")
    jc.set(f"{{{ns_w}}}val", "center")
    
    for r_idx, linha in enumerate(dados_linhas):
        tr = ET.SubElement(tbl, f"{{{ns_w}}}tr")
        trPr = ET.SubElement(tr, f"{{{ns_w}}}trPr")
        
        for c_idx, celula in enumerate(linha):
            tc = ET.SubElement(tr, f"{{{ns_w}}}tc")
            tcPr = ET.SubElement(tc, f"{{{ns_w}}}tcPr")
            
            # Shading no cabeçalho da tabela (cinza claro harmônico com o modelo)
            if r_idx == 0:
                shd = ET.SubElement(tcPr, f"{{{ns_w}}}shd")
                shd.set(f"{{{ns_w}}}val", "clear")
                shd.set(f"{{{ns_w}}}color", "auto")
                shd.set(f"{{{ns_w}}}fill", "EAEAEA")
                
            p = ET.SubElement(tc, f"{{{ns_w}}}p")
            pPr = ET.SubElement(p, f"{{{ns_w}}}pPr")
            pStyle = ET.SubElement(pPr, f"{{{ns_w}}}pStyle")
            pStyle.set(f"{{{ns_w}}}val", "TAMainText")
            
            pjc = ET.SubElement(pPr, f"{{{ns_w}}}jc")
            pjc.set(f"{{{ns_w}}}val", "center" if c_idx > 0 or r_idx == 0 else "left")
            
            sp = ET.SubElement(pPr, f"{{{ns_w}}}spacing")
            sp.set(f"{{{ns_w}}}after", "40")
            sp.set(f"{{{ns_w}}}before", "40")
            
            r = ET.SubElement(p, f"{{{ns_w}}}r")
            rPr = ET.SubElement(r, f"{{{ns_w}}}rPr")
            
            rFonts = ET.SubElement(rPr, f"{{{ns_w}}}rFonts")
            rFonts.set(f"{{{ns_w}}}ascii", "Arial")
            rFonts.set(f"{{{ns_w}}}hAnsi", "Arial")
            
            sz = ET.SubElement(rPr, f"{{{ns_w}}}sz")
            sz.set(f"{{{ns_w}}}val", "18")  # 9pt para caber perfeitamente na tabela
            szCs = ET.SubElement(rPr, f"{{{ns_w}}}szCs")
            szCs.set(f"{{{ns_w}}}val", "18")
            
            if r_idx == 0:
                ET.SubElement(rPr, f"{{{ns_w}}}b")
                
            t = ET.SubElement(r, f"{{{ns_w}}}t")
            t.text = str(celula)
            t.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
            
    return tbl

def gerar():
    temp_dir = "temp_docx_extract"
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir, exist_ok=True)
    
    with zipfile.ZipFile(TEMPLATE_PATH, 'r') as zin:
        zin.extractall(temp_dir)
        
    doc_path = os.path.join(temp_dir, "word", "document.xml")
    tree = ET.parse(doc_path)
    root = tree.getroot()
    body = root.find('w:body', NS)
    
    # Mapeamos os elementos originais exatamente
    # body[0]: Tabela do cabeçalho institucional (UNIFOR e CCT)
    # body[1], body[2]: Parágrafos em branco
    # body[3]: Título
    # body[4]: Autor 1
    # body[5]: Autor 2
    # body[6], body[7]: Parágrafos em branco
    # body[8]: Palavras-chave
    # body[9], body[10]: Parágrafos em branco
    # body[11]: Cabeçalho "Resumo" (COM BORDA E CINZA D9D9D9)
    # body[12]: Parágrafo em branco
    # body[13]: Texto "Inserir o resumo..."
    # body[14]: Parágrafo em branco
    # body[15]: Cabeçalho "Introdução" (COM BORDA E CINZA D9D9D9)
    # body[16]: Parágrafo em branco
    # body[17]: Texto "Inserir a introdução..."
    # body[18]: Parágrafo em branco
    # body[19]: Cabeçalho "Metodologia" (COM BORDA E CINZA D9D9D9)
    # body[20]: Parágrafo em branco
    # body[21]: Texto "Inserir a metodologia..."
    # body[22]: Cabeçalho "Resultados e Discussão" (COM BORDA E CINZA D9D9D9)
    # body[23]: Parágrafo em branco
    # body[24]: Texto "Inserir os resultados e a discussão..."
    # body[25]: Cabeçalho "Conclusão" (COM BORDA E CINZA D9D9D9)
    # body[26]: Parágrafo em branco
    # body[27]: Texto "Inserir as conclusões..."
    # body[28]: Cabeçalho "Referências" (COM BORDA E CINZA D9D9D9)
    # body[29]: Parágrafo em branco
    # body[30]: Texto "Inserir as referências bibliográficas..."
    # body[31]: sectPr (Propriedades da página e margens)
    
    p_prototipo_corpo = copy.deepcopy(body[13]) # Template para parágrafos de texto (Arial 11, espaçamento 1.5)
    p_prototipo_cabecalho = copy.deepcopy(body[28]) # Template para títulos de seção (Com borda e cinza D9D9D9)
    
    novos_elementos = []
    
    # 0. Tabela de cabeçalho UNIFOR (INTACTA)
    novos_elementos.append(body[0])
    novos_elementos.append(body[1])
    novos_elementos.append(body[2])
    
    # 3. Título (Arial 14, Negrito, Caixa alta, Centralizado)
    p_titulo = body[3]
    set_paragraph_text(p_titulo, 
                       "ANÁLISE DE DESEMPENHO DE ALGORITMOS DE ORDENAÇÃO EM AMBIENTES SERIAIS E CONCORRENTES/PARALELOS: UM ESTUDO COMPARATIVO EM JAVA",
                       font_name="Arial", size_half_points="28", bold=True, line_spacing=240, align="center")
    novos_elementos.append(p_titulo)
    
    # 4. Autor
    p_autor = body[4]
    set_paragraph_text(p_autor, "Autor: Anderson Viana Cataldo", font_name="Arial", size_half_points="22", bold=False, line_spacing=240, align="left")
    novos_elementos.append(p_autor)
    
    # Autor 2 omitido já que é individual
    novos_elementos.append(body[6])
    novos_elementos.append(body[7])
    
    # 8. Palavras-chave
    p_palavras = body[8]
    set_paragraph_text(p_palavras, "Palavras-chave: Computação Concorrente. Algoritmos de Ordenação. Speedup. Java Fork/Join. Desempenho.",
                       font_name="Arial", size_half_points="20", bold=False, line_spacing=240, align="both")
    novos_elementos.append(p_palavras)
    novos_elementos.append(body[9])
    novos_elementos.append(body[10])
    
    # ================= RESUMO =================
    novos_elementos.append(body[11]) # Cabeçalho ORIGINAL do modelo com borda e cinza
    novos_elementos.append(body[12])
    
    p_resumo = body[13]
    texto_resumo = (
        "Este trabalho apresenta uma análise comparativa e aprofundada de desempenho entre implementações seriais e "
        "paralelas de quatro algoritmos fundamentais de ordenação em Java: Bubble Sort, Quick Sort, Merge Sort e Insertion Sort. "
        "O estudo investiga o impacto do tamanho das entradas (1.000 a 10.000 elementos), da natureza dos dados (aleatórios e inversamente "
        "ordenados) e do número de threads de trabalho (1, 2 e 4 threads) na taxa de aceleração (Speedup) e na eficiência computacional. "
        "Para a paralelização dos algoritmos quadráticos, empregou-se a Transposição Par-Ímpar (Odd-Even Transposition) para o Bubble Sort "
        "e o Particionamento em Blocos com Mescla Hierárquica para o Insertion Sort. Já para os algoritmos de divisão e conquista, "
        "utilizou-se o Fork/Join Framework nativo do Java com controle adaptativo de granularidade. Os testes foram submetidos a pré-aquecimento "
        "da JVM (warm-up) e compostos por no mínimo cinco amostras independentes para cálculo de média e desvio padrão amostral. "
        "Os resultados demonstraram ganhos expressivos de desempenho para o Insertion Sort Paralelo (speedup de 8,63x com 4 threads) e para o "
        "Merge Sort Paralelo (speedup de 2,34x), evidenciando a eficácia da exploração de múltiplos núcleos de processamento, enquanto "
        "revelaram as limitações do overhead de sincronização no Bubble Sort em vetores de pequeno porte."
    )
    set_paragraph_text(p_resumo, texto_resumo, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_resumo)
    novos_elementos.append(body[14])
    
    # ================= INTRODUÇÃO =================
    novos_elementos.append(body[15]) # Cabeçalho ORIGINAL do modelo com borda e cinza
    novos_elementos.append(body[16])
    
    p_intro_1 = body[17]
    texto_intro_1 = (
        "A ordenação de conjuntos de dados é uma das operações computacionais mais frequentes e críticas na Ciência da Computação, "
        "servindo como alicerce para algoritmos de busca, indexação de bancos de dados e processamento analítico de grandes volumes de dados. "
        "Historicamente, o aprimoramento de desempenho esteve atrelado ao aumento contínuo da frequência de clock dos processadores. "
        "Entretanto, limitações físicas associadas à dissipação térmica e ao consumo energético impuseram a transição definitiva para "
        "arquiteturas multicore. Nesse cenário, o ganho real de velocidade em aplicações modernas tornou-se estritamente dependente da capacidade "
        "do software em explorar o paralelismo e a concorrência."
    )
    set_paragraph_text(p_intro_1, texto_intro_1, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_intro_1)
    
    p_intro_2 = copy.deepcopy(p_prototipo_corpo)
    texto_intro_2 = (
        "O presente trabalho aborda a implementação e avaliação de desempenho de quatro algoritmos de ordenação em Java: "
        "dois de complexidade assintótica quadrática O(n²) — Bubble Sort e Insertion Sort — e dois de complexidade quase-linear O(n log n) — "
        "Merge Sort e Quick Sort. O objetivo primordial consiste em confrontar as implementações seriais contra suas respectivas versões paralelas, "
        "variando a natureza dos dados de entrada (vetores com distribuição aleatória e vetores inversamente ordenados) e quantificando o ganho de "
        "escala através de métricas formais de Speedup e Eficiência. Para garantir a fidelidade científica das medições, foi desenvolvido um "
        "ambiente em Java padrão (SE 25), sem dependências externas, dotado de interface gráfica nativa em Swing para renderização de "
        "gráficos dinâmicos e registro sistemático dos resultados em arquivos CSV."
    )
    set_paragraph_text(p_intro_2, texto_intro_2, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_intro_2)
    novos_elementos.append(body[18])
    
    # ================= METODOLOGIA =================
    novos_elementos.append(body[19]) # Cabeçalho ORIGINAL do modelo com borda e cinza
    novos_elementos.append(body[20])
    
    p_met_1 = body[21]
    texto_met_1 = (
        "A metodologia empregada fundamentou-se na implementação rigorosa dos algoritmos tanto em modelo serial quanto paralelo, "
        "adotando estruturas concorrentes bem estabelecidas na literatura acadêmica:"
    )
    set_paragraph_text(p_met_1, texto_met_1, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_met_1)
    
    p_met_algos = copy.deepcopy(p_prototipo_corpo)
    texto_met_algos = (
        "1. Bubble Sort: A versão serial utiliza o laço duplo padrão com interrupção antecipada via flag booleana. A versão paralela adota o "
        "algoritmo de Transposição Par-Ímpar (Odd-Even Transposition Sort), particionando os pares disjuntos de índices (j, j+1) entre as threads "
        "e sincronizando cada fase por meio de CyclicBarrier, o que elimina condições de corrida.\n"
        "2. Insertion Sort: A versão serial executa inserções graduais no prefixo ordenado. Na versão paralela, aplicou-se a Partição em Blocos "
        "(Block Partitioning), onde o vetor é subdividido em P partes proporcionais ao número de threads. Cada thread ordena seu bloco isoladamente "
        "e, na sequência, uma etapa de mescla hierárquica paralela consolida os subvetores.\n"
        "3. Merge Sort: A versão paralela utiliza o Fork/Join Framework (RecursiveAction) do Java, subdividindo as chamadas recursivas entre "
        "as threads disponíveis até atingir um limiar de corte (threshold) adaptativo de granularidade mínima, a partir do qual processa-se em modo serial.\n"
        "4. Quick Sort: A implementação utiliza o esquema de particionamento de Hoare com escolha de pivô por mediana de três (início, meio e fim). "
        "Na versão paralela, as duas partições resultantes são submetidas concorrentemente ao ForkJoinPool."
    )
    set_paragraph_text(p_met_algos, texto_met_algos, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_met_algos)
    
    p_met_2 = copy.deepcopy(p_prototipo_corpo)
    texto_met_2 = (
        "O protocolo de teste foi conduzido em ambiente operacional Windows 11 com processador Intel(R) Core(TM) i5-10210U (4 núcleos físicos, "
        "8 processadores lógicos) sob OpenJDK 25 LTS. Foram avaliados tamanhos de entrada N = 1.000, 5.000 e 10.000 elementos nas naturezas Aleatória "
        "e Inversamente Ordenada. Para atenuar as distorções provocadas pela compilação dinâmica do compilador Just-In-Time (JIT C2) da JVM, realizou-se "
        "uma bateria preliminar de aquecimento (warm-up). Cada configuração foi executada por no mínimo cinco amostras sucessivas cronometradas com "
        "System.nanoTime(), permitindo apurar a média aritmética e o desvio padrão amostral. A integridade da ordenação foi verificada ao término de cada "
        "amostra. As métricas de Speedup (S = T_serial / T_paralelo) e Eficiência (E = S / P) foram consolidadas automaticamente."
    )
    set_paragraph_text(p_met_2, texto_met_2, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_met_2)
    
    # ================= RESULTADOS E DISCUSSÃO =================
    novos_elementos.append(body[22]) # Cabeçalho ORIGINAL do modelo com borda e cinza
    novos_elementos.append(body[23])
    
    p_res_1 = body[24]
    texto_res_1 = (
        "A Tabela 1 sintetiza as médias de tempo de execução (em milissegundos) e os respectivos fatores de Speedup obtidos para os "
        "quatro algoritmos analisados, considerando os vetores aleatórios e inversamente ordenados sob 1, 2 e 4 threads:"
    )
    set_paragraph_text(p_res_1, texto_res_1, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_res_1)
    
    # Tabela com resultados reais
    tabela_dados = [
        ["Algoritmo", "Modo / Threads", "N=1.000 (Aleat)", "N=10.000 (Aleat)", "Speedup (10k)", "N=10.000 (Inverso)", "Speedup Inv (10k)"],
        ["Bubble Sort", "Serial (1 Thread)", "1,299 ms", "97,510 ms", "1,00x", "52,525 ms", "1,00x"],
        ["Bubble Sort", "Paralelo (2 Threads)", "14,756 ms", "165,114 ms", "0,59x", "162,118 ms", "0,32x"],
        ["Bubble Sort", "Paralelo (4 Threads)", "15,821 ms", "167,385 ms", "0,58x", "162,156 ms", "0,32x"],
        ["Insertion Sort", "Serial (1 Thread)", "0,327 ms", "29,876 ms", "1,00x", "56,901 ms", "1,00x"],
        ["Insertion Sort", "Paralelo (2 Threads)", "0,363 ms", "19,317 ms", "1,55x", "18,653 ms", "3,05x"],
        ["Insertion Sort", "Paralelo (4 Threads)", "0,660 ms", "3,661 ms", "8,16x", "6,594 ms", "8,63x"],
        ["Merge Sort", "Serial (1 Thread)", "0,146 ms", "1,084 ms", "1,00x", "1,283 ms", "1,00x"],
        ["Merge Sort", "Paralelo (2 Threads)", "0,389 ms", "0,804 ms", "1,35x", "0,766 ms", "1,68x"],
        ["Merge Sort", "Paralelo (4 Threads)", "0,265 ms", "0,871 ms", "1,25x", "0,548 ms", "2,34x"],
        ["Quick Sort", "Serial (1 Thread)", "0,096 ms", "1,135 ms", "1,00x", "0,153 ms", "1,00x"],
        ["Quick Sort", "Paralelo (2 Threads)", "0,251 ms", "0,776 ms", "1,46x", "0,316 ms", "0,48x"],
        ["Quick Sort", "Paralelo (4 Threads)", "0,189 ms", "0,815 ms", "1,39x", "0,399 ms", "0,38x"]
    ]
    novos_elementos.append(criar_tabela_formatada(tabela_dados))
    
    p_legenda_tab = copy.deepcopy(p_prototipo_corpo)
    set_paragraph_text(p_legenda_tab, "Tabela 1: Comparativo de tempos médios e Speedup dos algoritmos sob 5 amostras.", font_name="Arial", size_half_points="18", italic=True, align="center")
    novos_elementos.append(p_legenda_tab)
    
    p_disc_1 = copy.deepcopy(p_prototipo_corpo)
    texto_disc_1 = (
        "A análise dos resultados empíricos evidencia nuances fundamentais da computação concorrente. O caso mais notável de ganho de desempenho "
        "ocorreu no Insertion Sort Paralelo. Para N = 10.000 com dados inversamente ordenados (pior cenário teórico), o tempo serial de 56,901 ms foi "
        "reduzido para apenas 6,594 ms com 4 threads, correspondendo a um Speedup de 8,63x. Esse comportamento superlinear decorre do fato de que a partição "
        "do vetor em P blocos de tamanho N/P reduz o número total de comparações quadráticas de O(N²) para P * O((N/P)²) = O(N²/P), viabilizando uma redução "
        "algébrica de trabalho além da pura distribuição física de núcleos."
    )
    set_paragraph_text(p_disc_1, texto_disc_1, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_disc_1)
    
    p_disc_2 = copy.deepcopy(p_prototipo_corpo)
    texto_disc_2 = (
        "Por sua vez, o Merge Sort Paralelo demonstrou escalabilidade consistente: em vetores inversos de 10.000 elementos, o tempo serial de 1,283 ms "
        "caiu para 0,548 ms sob 4 threads (Speedup de 2,34x e eficiência de 58,5%). Em contrapartida, para tamanhos pequenos (N = 1.000), o custo de instanciação "
        "das tarefas Fork/Join superou o ganho computacional da ordenação, fenômeno clássico conhecido como overhead de granularidade fina."
    )
    set_paragraph_text(p_disc_2, texto_disc_2, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_disc_2)
    
    p_disc_3 = copy.deepcopy(p_prototipo_corpo)
    texto_disc_3 = (
        "No Bubble Sort Paralelo (Odd-Even Transposition), observou-se uma aceleração negativa (Speedup inferior a 1,0x). Embora o particionamento de pares "
        "seja matematicamente correto e livre de corridas, a exigência de sincronização global via barreira cíclica ao término de cada fase gerou contenção "
        "substancial e latência de troca de contexto entre as threads. Esse resultado comprova que algoritmos com alto acoplamento e dependência mútua de passos "
        "não se beneficiam de paralelização em memórias compartilhadas para conjuntos de dados que caibam inteiramente no cache L1/L2 do processador."
    )
    set_paragraph_text(p_disc_3, texto_disc_3, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_disc_3)
    
    # ================= CONCLUSÃO =================
    novos_elementos.append(body[25]) # Cabeçalho ORIGINAL do modelo com borda e cinza
    novos_elementos.append(body[26])
    
    p_concl = body[27]
    texto_concl = (
        "O estudo comparativo atingiu plenamente seus objetivos ao evidenciar as condições práticas sob as quais a paralelização de algoritmos de ordenação "
        "é vantajosa. Constatou-se que a aceleração não decorre unicamente da adição de threads, mas sim da relação entre a complexidade intrínseca da tarefa, "
        "a estratégia de decomposição de dados e a sobrecarga imposta pelos mecanismos de sincronização. Conclui-se que algoritmos estruturados sob "
        "divisão e conquista ou particionamento independente em blocos (como Merge Sort e Block Insertion Sort) apresentam excelente aderência às arquiteturas "
        "multicore modernas, enquanto estratégias de sincronização por barreira fina (como Bubble Sort Par-Ímpar) devem ser reservadas a hardwares massivamente "
        "paralelos com redes de interconexão dedicadas. O framework desenvolvido e os arquivos CSV gerados fornecem uma base sólida e reproduzível para futuras "
        "pesquisas em computação de alto desempenho."
    )
    set_paragraph_text(p_concl, texto_concl, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_concl)
    
    # ================= REFERÊNCIAS =================
    novos_elementos.append(body[28]) # Cabeçalho ORIGINAL do modelo com borda e cinza
    novos_elementos.append(body[29])
    
    refs = [
        "CORMEN, T. H.; LEISERSON, C. E.; RIVEST, R. L.; STEIN, C. Algoritmos: Teoria e Prática. 3. ed. Rio de Janeiro: Elsevier, 2012.",
        "GOETZ, B.; PEIERLS, T.; BLOCH, J.; BOWBEER, J.; HOLMES, D.; LEA, D. Java Concurrency in Practice. Boston: Addison-Wesley, 2006.",
        "LEA, D. A Java Fork/Join Framework. In: ACM JAVA GRANDE CONFERENCE, 2000, San Francisco. Proceedings [...]. New York: ACM, 2000. p. 36-43.",
        "WILKINSON, B.; ALLEN, M. Parallel Programming: Techniques and Applications Using Networked Workstations and Parallel Computers. 2. ed. Upper Saddle River: Prentice Hall, 2005."
    ]
    p_ref_first = body[30]
    set_paragraph_text(p_ref_first, refs[0], font_name="Arial", size_half_points="22", line_spacing=240, align="both")
    novos_elementos.append(p_ref_first)
    
    for r_text in refs[1:]:
        p_ref = copy.deepcopy(p_prototipo_corpo)
        set_paragraph_text(p_ref, r_text, font_name="Arial", size_half_points="22", line_spacing=240, align="both")
        novos_elementos.append(p_ref)
        
    novos_elementos.append(copy.deepcopy(body[12])) # Parágrafo em branco
    
    # ================= ANEXOS =================
    # Usamos uma cópia do cabeçalho de Referências para ter exatamente a mesma borda e fundo cinza D9D9D9!
    p_h_anexos = copy.deepcopy(p_prototipo_cabecalho)
    # Altera o texto do run
    t_anex = p_h_anexos.find(f".//{{{NS['w']}}}t")
    if t_anex is not None:
        t_anex.text = "Anexos – Códigos das Implementações"
    novos_elementos.append(p_h_anexos)
    novos_elementos.append(copy.deepcopy(body[12]))
    
    p_anexos_texto = copy.deepcopy(p_prototipo_corpo)
    texto_anexos = (
        "O código-fonte integral do projeto, os scripts facilitadores de compilação, a interface gráfica Swing e os "
        "conjuntos completos de dados gerados em formato CSV encontram-se disponíveis no repositório GitHub:\n\n"
        "Repositório GitHub: https://github.com/andersoncataldo/algoritmos-desempenho-paralelo-concorrente.git\n\n"
        "Instruções para execução:\n"
        "1. Execute o script 'executar_gui.bat' no Windows para inicializar a interface gráfica Swing com gráficos dinâmicos;\n"
        "2. Ou execute 'executar_cli.bat' para reproduzir os benchmarks diretamente via terminal headless."
    )
    set_paragraph_text(p_anexos_texto, texto_anexos, font_name="Arial", size_half_points="22", line_spacing=360, align="both")
    novos_elementos.append(p_anexos_texto)
    
    # Propriedades de página e margens originais (INTACTAS)
    novos_elementos.append(body[31])
    
    # Substitui no corpo
    body.clear()
    for el in novos_elementos:
        body.append(el)
        
    tree.write(doc_path, encoding='utf-8', xml_declaration=True)
    
    with zipfile.ZipFile(OUTPUT_PATH, 'w', zipfile.ZIP_DEFLATED) as zout:
        for foldername, subfolders, filenames in os.walk(temp_dir):
            for filename in filenames:
                filepath = os.path.join(foldername, filename)
                arcname = os.path.relpath(filepath, temp_dir)
                zout.write(filepath, arcname)
                
    shutil.rmtree(temp_dir)
    print("Sucesso! Relatório gerado com fidelidade absoluta ao Modelo-Artigo.docx.")

if __name__ == "__main__":
    gerar()
