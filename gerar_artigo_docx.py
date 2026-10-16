import zipfile
import xml.etree.ElementTree as ET
import os
import shutil

TEMPLATE_PATH = "Modelo-Artigo.docx"
OUTPUT_PATH = "Relatorio-Artigo-Ordenacao.docx"

NS = {
    'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
    'w14': 'http://schemas.microsoft.com/office/word/2010/wordml'
}
ET.register_namespace('w', NS['w'])
ET.register_namespace('w14', NS['w14'])

def criar_paragrafo_texto(texto, estilo="TAMainText", negrito=False, italico=False, alinhamento="both", espacamento=360):
    p = ET.Element(f"{{{NS['w']}}}p")
    pPr = ET.SubElement(p, f"{{{NS['w']}}}pPr")
    
    if estilo:
        pStyle = ET.SubElement(pPr, f"{{{NS['w']}}}pStyle")
        pStyle.set(f"{{{NS['w']}}}val", estilo)
        
    if alinhamento:
        jc = ET.SubElement(pPr, f"{{{NS['w']}}}jc")
        jc.set(f"{{{NS['w']}}}val", alinhamento)
        
    if espacamento:
        sp = ET.SubElement(pPr, f"{{{NS['w']}}}spacing")
        sp.set(f"{{{NS['w']}}}line", str(espacamento))
        sp.set(f"{{{NS['w']}}}lineRule", "auto")
        sp.set(f"{{{NS['w']}}}after", "120")
        
    rPr_p = ET.SubElement(pPr, f"{{{NS['w']}}}rPr")
    rFonts_p = ET.SubElement(rPr_p, f"{{{NS['w']}}}rFonts")
    rFonts_p.set(f"{{{NS['w']}}}ascii", "Arial")
    rFonts_p.set(f"{{{NS['w']}}}hAnsi", "Arial")
    rFonts_p.set(f"{{{NS['w']}}}cs", "Arial")
    
    r = ET.SubElement(p, f"{{{NS['w']}}}r")
    rPr = ET.SubElement(r, f"{{{NS['w']}}}rPr")
    rFonts = ET.SubElement(rPr, f"{{{NS['w']}}}rFonts")
    rFonts.set(f"{{{NS['w']}}}ascii", "Arial")
    rFonts.set(f"{{{NS['w']}}}hAnsi", "Arial")
    rFonts.set(f"{{{NS['w']}}}cs", "Arial")
    
    sz = ET.SubElement(rPr, f"{{{NS['w']}}}sz")
    sz.set(f"{{{NS['w']}}}val", "22")  # 11pt
    szCs = ET.SubElement(rPr, f"{{{NS['w']}}}szCs")
    szCs.set(f"{{{NS['w']}}}val", "22")
    
    if negrito:
        ET.SubElement(rPr, f"{{{NS['w']}}}b")
    if italico:
        ET.SubElement(rPr, f"{{{NS['w']}}}i")
        
    lang = ET.SubElement(rPr, f"{{{NS['w']}}}lang")
    lang.set(f"{{{NS['w']}}}val", "pt-BR")
    
    t = ET.SubElement(r, f"{{{NS['w']}}}t")
    t.text = texto
    t.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
    return p

def criar_tabela(dados_linhas):
    tbl = ET.Element(f"{{{NS['w']}}}tbl")
    tblPr = ET.SubElement(tbl, f"{{{NS['w']}}}tblPr")
    tblStyle = ET.SubElement(tblPr, f"{{{NS['w']}}}tblStyle")
    tblStyle.set(f"{{{NS['w']}}}val", "TableGrid")
    tblW = ET.SubElement(tblPr, f"{{{NS['w']}}}tblW")
    tblW.set(f"{{{NS['w']}}}w", "0")
    tblW.set(f"{{{NS['w']}}}type", "auto")
    jc = ET.SubElement(tblPr, f"{{{NS['w']}}}jc")
    jc.set(f"{{{NS['w']}}}val", "center")
    
    for r_idx, linha in enumerate(dados_linhas):
        tr = ET.SubElement(tbl, f"{{{NS['w']}}}tr")
        for celula in linha:
            tc = ET.SubElement(tr, f"{{{NS['w']}}}tc")
            tcPr = ET.SubElement(tc, f"{{{NS['w']}}}tcPr")
            
            p = ET.SubElement(tc, f"{{{NS['w']}}}p")
            pPr = ET.SubElement(p, f"{{{NS['w']}}}pPr")
            pStyle = ET.SubElement(pPr, f"{{{NS['w']}}}pStyle")
            pStyle.set(f"{{{NS['w']}}}val", "TAMainText")
            pjc = ET.SubElement(pPr, f"{{{NS['w']}}}jc")
            pjc.set(f"{{{NS['w']}}}val", "center" if r_idx > 0 else "center")
            
            r = ET.SubElement(p, f"{{{NS['w']}}}r")
            rPr = ET.SubElement(r, f"{{{NS['w']}}}rPr")
            rFonts = ET.SubElement(rPr, f"{{{NS['w']}}}rFonts")
            rFonts.set(f"{{{NS['w']}}}ascii", "Arial")
            rFonts.set(f"{{{NS['w']}}}hAnsi", "Arial")
            
            sz = ET.SubElement(rPr, f"{{{NS['w']}}}sz")
            sz.set(f"{{{NS['w']}}}val", "18" if r_idx > 0 else "18")  # 9pt
            
            if r_idx == 0:
                ET.SubElement(rPr, f"{{{NS['w']}}}b")
                
            t = ET.SubElement(r, f"{{{NS['w']}}}t")
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
    
    # Monta os novos filhos do corpo preservando cabeçalho, logo, seções e rodapés
    novos_filhos = []
    
    # 0. Tabela de cabeçalho UNIFOR (logo e dados da instituição)
    novos_filhos.append(body[0])
    novos_filhos.append(body[1]) # p vazio
    
    # Título (Arial 14, Negrito, Caixa alta, Centralizado)
    p_titulo = ET.Element(f"{{{NS['w']}}}p")
    pPr_t = ET.SubElement(p_titulo, f"{{{NS['w']}}}pPr")
    st_t = ET.SubElement(pPr_t, f"{{{NS['w']}}}pStyle")
    st_t.set(f"{{{NS['w']}}}val", "TAMainText")
    jc_t = ET.SubElement(pPr_t, f"{{{NS['w']}}}jc")
    jc_t.set(f"{{{NS['w']}}}val", "center")
    r_t = ET.SubElement(p_titulo, f"{{{NS['w']}}}r")
    rPr_t = ET.SubElement(r_t, f"{{{NS['w']}}}rPr")
    rf_t = ET.SubElement(rPr_t, f"{{{NS['w']}}}rFonts")
    rf_t.set(f"{{{NS['w']}}}ascii", "Arial")
    rf_t.set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_t, f"{{{NS['w']}}}b")
    sz_t = ET.SubElement(rPr_t, f"{{{NS['w']}}}sz")
    sz_t.set(f"{{{NS['w']}}}val", "28") # 14pt
    t_t = ET.SubElement(r_t, f"{{{NS['w']}}}t")
    t_t.text = "ANÁLISE DE DESEMPENHO DE ALGORITMOS DE ORDENAÇÃO EM AMBIENTES SERIAIS E CONCORRENTES/PARALELOS: UM ESTUDO COMPARATIVO EM JAVA"
    novos_filhos.append(p_titulo)
    
    # Autores
    novos_filhos.append(criar_paragrafo_texto("Autor 1: Anderson", alinhamento="left"))
    novos_filhos.append(criar_paragrafo_texto("Curso: Ciência da Computação - Universidade de Fortaleza (UNIFOR)", alinhamento="left", italico=True))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # Palavras-chave
    novos_filhos.append(criar_paragrafo_texto("Palavras-chave: Computação Concorrente. Algoritmos de Ordenação. Speedup. Java Fork/Join. Desempenho.", estilo="NormalWeb", alinhamento="both"))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # RESUMO
    p_h_resumo = ET.Element(f"{{{NS['w']}}}p")
    pPr_hr = ET.SubElement(p_h_resumo, f"{{{NS['w']}}}pPr")
    ET.SubElement(pPr_hr, f"{{{NS['w']}}}pStyle").set(f"{{{NS['w']}}}val", "Absbox")
    r_hr = ET.SubElement(p_h_resumo, f"{{{NS['w']}}}r")
    rPr_hr = ET.SubElement(r_hr, f"{{{NS['w']}}}rPr")
    ET.SubElement(rPr_hr, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}ascii", "Arial")
    ET.SubElement(rPr_hr, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_hr, f"{{{NS['w']}}}b")
    ET.SubElement(r_hr, f"{{{NS['w']}}}t").text = "Resumo"
    novos_filhos.append(p_h_resumo)
    
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
    novos_filhos.append(criar_paragrafo_texto(texto_resumo))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # INTRODUÇÃO
    p_h_intro = ET.Element(f"{{{NS['w']}}}p")
    pPr_hi = ET.SubElement(p_h_intro, f"{{{NS['w']}}}pPr")
    ET.SubElement(pPr_hi, f"{{{NS['w']}}}pStyle").set(f"{{{NS['w']}}}val", "Absbox")
    r_hi = ET.SubElement(p_h_intro, f"{{{NS['w']}}}r")
    rPr_hi = ET.SubElement(r_hi, f"{{{NS['w']}}}rPr")
    ET.SubElement(rPr_hi, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}ascii", "Arial")
    ET.SubElement(rPr_hi, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_hi, f"{{{NS['w']}}}b")
    ET.SubElement(r_hi, f"{{{NS['w']}}}t").text = "Introdução"
    novos_filhos.append(p_h_intro)
    
    texto_intro_1 = (
        "A ordenação de conjuntos de dados é uma das operações computacionais mais frequentes e críticas na Ciência da Computação, "
        "servindo como alicerce para algoritmos de busca, indexação de bancos de dados e processamento analítico de grandes volumes de dados. "
        "Historicamente, o aprimoramento de desempenho esteve atrelado ao aumento contínuo da frequência de clock dos processadores. "
        "Entretanto, limitações físicas associadas à dissipação térmica e ao consumo energético impuseram a transição definitiva para "
        "arquiteturas multicore. Nesse cenário, o ganho real de velocidade em aplicações modernas tornou-se estritamente dependente da capacidade "
        "do software em explorar o paralelismo e a concorrência."
    )
    texto_intro_2 = (
        "O presente trabalho aborda a implementação e avaliação de desempenho de quatro algoritmos de ordenação em Java: "
        "dois de complexidade assintótica quadrática O(n²) — Bubble Sort e Insertion Sort — e dois de complexidade quase-linear O(n log n) — "
        "Merge Sort e Quick Sort. O objetivo primordial consiste em confrontar as implementações seriais contra suas respectivas versões paralelas, "
        "variando a natureza dos dados de entrada (vetores com distribuição aleatória e vetores inversamente ordenados) e quantificando o ganho de "
        "escala através de métricas formais de Speedup e Eficiência. Para garantir a fidelidade científica das medições, foi desenvolvido um "
        "ambiente em Java padrão (SE 25), sem dependências externas, dotado de interface gráfica nativa em Swing para renderização de "
        "gráficos dinâmicos e registro sistemático dos resultados em arquivos CSV."
    )
    novos_filhos.append(criar_paragrafo_texto(texto_intro_1))
    novos_filhos.append(criar_paragrafo_texto(texto_intro_2))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # METODOLOGIA
    p_h_met = ET.Element(f"{{{NS['w']}}}p")
    pPr_hm = ET.SubElement(p_h_met, f"{{{NS['w']}}}pPr")
    ET.SubElement(pPr_hm, f"{{{NS['w']}}}pStyle").set(f"{{{NS['w']}}}val", "Absbox")
    r_hm = ET.SubElement(p_h_met, f"{{{NS['w']}}}r")
    rPr_hm = ET.SubElement(r_hm, f"{{{NS['w']}}}rPr")
    ET.SubElement(rPr_hm, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}ascii", "Arial")
    ET.SubElement(rPr_hm, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_hm, f"{{{NS['w']}}}b")
    ET.SubElement(r_hm, f"{{{NS['w']}}}t").text = "Metodologia"
    novos_filhos.append(p_h_met)
    
    texto_met_1 = (
        "A metodologia empregada fundamentou-se na implementação rigorosa dos algoritmos tanto em modelo serial quanto paralelo, "
        "adotando estruturas concorrentes bem estabelecidas na literatura acadêmica:"
    )
    novos_filhos.append(criar_paragrafo_texto(texto_met_1))
    
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
    novos_filhos.append(criar_paragrafo_texto(texto_met_algos))
    
    texto_met_2 = (
        "O protocolo de teste foi conduzido em ambiente operacional Windows 11 com processador Intel(R) Core(TM) i5-10210U (4 núcleos físicos, "
        "8 processadores lógicos) sob OpenJDK 25 LTS. Foram avaliados tamanhos de entrada N = 1.000, 5.000 e 10.000 elementos nas naturezas Aleatória "
        "e Inversamente Ordenada. Para atenuar as distorções provocadas pela compilação dinâmica do compilador Just-In-Time (JIT C2) da JVM, realizou-se "
        "uma bateria preliminar de aquecimento (warm-up). Cada configuração foi executada por no mínimo cinco amostras sucessivas cronometradas com "
        "System.nanoTime(), permitindo apurar a média aritmética e o desvio padrão amostral. A integridade da ordenação foi verificada ao término de cada "
        "amostra. As métricas de Speedup (S = T_serial / T_paralelo) e Eficiência (E = S / P) foram consolidadas automaticamente."
    )
    novos_filhos.append(criar_paragrafo_texto(texto_met_2))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # RESULTADOS E DISCUSSÃO
    p_h_res = ET.Element(f"{{{NS['w']}}}p")
    pPr_hrs = ET.SubElement(p_h_res, f"{{{NS['w']}}}pPr")
    ET.SubElement(pPr_hrs, f"{{{NS['w']}}}pStyle").set(f"{{{NS['w']}}}val", "Absbox")
    r_hrs = ET.SubElement(p_h_res, f"{{{NS['w']}}}r")
    rPr_hrs = ET.SubElement(r_hrs, f"{{{NS['w']}}}rPr")
    ET.SubElement(rPr_hrs, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}ascii", "Arial")
    ET.SubElement(rPr_hrs, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_hrs, f"{{{NS['w']}}}b")
    ET.SubElement(r_hrs, f"{{{NS['w']}}}t").text = "Resultados e Discussão"
    novos_filhos.append(p_h_res)
    
    texto_res_1 = (
        "A Tabela 1 sintetiza as médias de tempo de execução (em milissegundos) e os respectivos fatores de Speedup obtidos para os "
        "quatro algoritmos analisados, considerando os vetores aleatórios e inversamente ordenados sob 1, 2 e 4 threads:"
    )
    novos_filhos.append(criar_paragrafo_texto(texto_res_1))
    
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
    novos_filhos.append(criar_tabela(tabela_dados))
    novos_filhos.append(criar_paragrafo_texto("Tabela 1: Comparativo de tempos médios e Speedup dos algoritmos sob 5 amostras.", estilo="TAMainText", italico=True, alinhamento="center"))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    texto_disc_1 = (
        "A análise dos resultados empíricos evidencia nuances fundamentais da computação concorrente. O caso mais notável de ganho de desempenho "
        "ocorreu no Insertion Sort Paralelo. Para N = 10.000 com dados inversamente ordenados (pior cenário teórico), o tempo serial de 56,901 ms foi "
        "reduzido para apenas 6,594 ms com 4 threads, correspondendo a um Speedup de 8,63x. Esse comportamento superlinear decorre do fato de que a partição "
        "do vetor em P blocos de tamanho N/P reduz o número total de comparações quadráticas de O(N²) para P * O((N/P)²) = O(N²/P), viabilizando uma redução "
        "algébrica de trabalho além da pura distribuição física de núcleos."
    )
    texto_disc_2 = (
        "Por sua vez, o Merge Sort Paralelo demonstrou escalabilidade consistente: em vetores inversos de 10.000 elementos, o tempo serial de 1,283 ms "
        "caiu para 0,548 ms sob 4 threads (Speedup de 2,34x e eficiência de 58,5%). Em contrapartida, para tamanhos pequenos (N = 1.000), o custo de instanciação "
        "das tarefas Fork/Join superou o ganho computacional da ordenação, fenômeno clássico conhecido como overhead de granularidade fina."
    )
    texto_disc_3 = (
        "No Bubble Sort Paralelo (Odd-Even Transposition), observou-se uma aceleração negativa (Speedup inferior a 1,0x). Embora o particionamento de pares "
        "seja matematicamente correto e livre de corridas, a exigência de sincronização global via barreira cíclica ao término de cada fase gerou contenção "
        "substancial e latência de troca de contexto entre as threads. Esse resultado comprova que algoritmos com alto acoplamento e dependência mútua de passos "
        "não se beneficiam de paralelização em memórias compartilhadas para conjuntos de dados que caibam inteiramente no cache L1/L2 do processador."
    )
    novos_filhos.append(criar_paragrafo_texto(texto_disc_1))
    novos_filhos.append(criar_paragrafo_texto(texto_disc_2))
    novos_filhos.append(criar_paragrafo_texto(texto_disc_3))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # CONCLUSÃO
    p_h_concl = ET.Element(f"{{{NS['w']}}}p")
    pPr_hc = ET.SubElement(p_h_concl, f"{{{NS['w']}}}pPr")
    ET.SubElement(pPr_hc, f"{{{NS['w']}}}pStyle").set(f"{{{NS['w']}}}val", "Absbox")
    r_hc = ET.SubElement(p_h_concl, f"{{{NS['w']}}}r")
    rPr_hc = ET.SubElement(r_hc, f"{{{NS['w']}}}rPr")
    ET.SubElement(rPr_hc, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}ascii", "Arial")
    ET.SubElement(rPr_hc, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_hc, f"{{{NS['w']}}}b")
    ET.SubElement(r_hc, f"{{{NS['w']}}}t").text = "Conclusão"
    novos_filhos.append(p_h_concl)
    
    texto_concl = (
        "O estudo comparativo atingiu plenamente seus objetivos ao evidenciar as condições práticas sob as quais a paralelização de algoritmos de ordenação "
        "é vantajosa. Constatou-se que a aceleração não decorre unicamente da adição de threads, mas sim da relação entre a complexidade intrínseca da tarefa, "
        "a estratégia de decomposição de dados e a sobrecarga imposta pelos mecanismos de sincronização. Conclui-se que algoritmos estruturados sob "
        "divisão e conquista ou particionamento independente em blocos (como Merge Sort e Block Insertion Sort) apresentam excelente aderência às arquiteturas "
        "multicore modernas, enquanto estratégias de sincronização por barreira fina (como Bubble Sort Par-Ímpar) devem ser reservadas a hardwares massivamente "
        "paralelos com redes de interconexão dedicadas. O framework desenvolvido e os arquivos CSV gerados fornecem uma base sólida e reproduzível para futuras "
        "pesquisas em computação de alto desempenho."
    )
    novos_filhos.append(criar_paragrafo_texto(texto_concl))
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # REFERÊNCIAS
    p_h_ref = ET.Element(f"{{{NS['w']}}}p")
    pPr_hrf = ET.SubElement(p_h_ref, f"{{{NS['w']}}}pPr")
    ET.SubElement(pPr_hrf, f"{{{NS['w']}}}pStyle").set(f"{{{NS['w']}}}val", "Absbox")
    r_hrf = ET.SubElement(p_h_ref, f"{{{NS['w']}}}r")
    rPr_hrf = ET.SubElement(r_hrf, f"{{{NS['w']}}}rPr")
    ET.SubElement(rPr_hrf, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}ascii", "Arial")
    ET.SubElement(rPr_hrf, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_hrf, f"{{{NS['w']}}}b")
    ET.SubElement(r_hrf, f"{{{NS['w']}}}t").text = "Referências"
    novos_filhos.append(p_h_ref)
    
    refs = [
        "CORMEN, T. H.; LEISERSON, C. E.; RIVEST, R. L.; STEIN, C. Algoritmos: Teoria e Prática. 3. ed. Rio de Janeiro: Elsevier, 2012.",
        "GOETZ, B.; PEIERLS, T.; BLOCH, J.; BOWBEER, J.; HOLMES, D.; LEA, D. Java Concurrency in Practice. Boston: Addison-Wesley, 2006.",
        "LEA, D. A Java Fork/Join Framework. In: ACM JAVA GRANDE CONFERENCE, 2000, San Francisco. Proceedings [...]. New York: ACM, 2000. p. 36-43.",
        "WILKINSON, B.; ALLEN, M. Parallel Programming: Techniques and Applications Using Networked Workstations and Parallel Computers. 2. ed. Upper Saddle River: Prentice Hall, 2005."
    ]
    for r in refs:
        novos_filhos.append(criar_paragrafo_texto(r, espacamento=240))
        
    novos_filhos.append(criar_paragrafo_texto("", espacamento=180))
    
    # ANEXOS
    p_h_anex = ET.Element(f"{{{NS['w']}}}p")
    pPr_ha = ET.SubElement(p_h_anex, f"{{{NS['w']}}}pPr")
    ET.SubElement(pPr_ha, f"{{{NS['w']}}}pStyle").set(f"{{{NS['w']}}}val", "Absbox")
    r_ha = ET.SubElement(p_h_anex, f"{{{NS['w']}}}r")
    rPr_ha = ET.SubElement(r_ha, f"{{{NS['w']}}}rPr")
    ET.SubElement(rPr_ha, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}ascii", "Arial")
    ET.SubElement(rPr_ha, f"{{{NS['w']}}}rFonts").set(f"{{{NS['w']}}}hAnsi", "Arial")
    ET.SubElement(rPr_ha, f"{{{NS['w']}}}b")
    ET.SubElement(r_ha, f"{{{NS['w']}}}t").text = "Anexos – Códigos das Implementações"
    novos_filhos.append(p_h_anex)
    
    texto_anexos = (
        "O código-fonte integral do projeto, os scripts facilitadores de compilação e os conjuntos completos de dados "
        "gerados em formato CSV encontram-se disponíveis no repositório GitHub:\n"
        "Repositório GitHub: [Inserir o link do seu GitHub aqui]\n"
        "Instruções para execução: Execute o script 'executar_gui.bat' no Windows para inicializar a interface gráfica Swing, "
        "ou execute 'executar_cli.bat' para reproduzir as medições diretamente via terminal."
    )
    novos_filhos.append(criar_paragrafo_texto(texto_anexos))
    
    # Adiciona a seção final original (margens, tamanho de página)
    novos_filhos.append(body[-1])
    
    # Substitui os filhos do body
    body.clear()
    for f in novos_filhos:
        body.append(f)
        
    tree.write(doc_path, encoding='utf-8', xml_declaration=True)
    
    # Empacota novamente como docx
    with zipfile.ZipFile(OUTPUT_PATH, 'w', zipfile.ZIP_DEFLATED) as zout:
        for foldername, subfolders, filenames in os.walk(temp_dir):
            for filename in filenames:
                filepath = os.path.join(foldername, filename)
                arcname = os.path.relpath(filepath, temp_dir)
                zout.write(filepath, arcname)
                
    shutil.rmtree(temp_dir)
    print(f"Relatório gerado com sucesso em: {OUTPUT_PATH}")

if __name__ == "__main__":
    gerar()
