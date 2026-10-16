# Análise de Desempenho de Algoritmos de Ordenação (Serial vs Paralelo)
**Universidade de Fortaleza (UNIFOR) — Computação Concorrente e Paralela**

Este projeto implementa e avalia o desempenho de quatro algoritmos clássicos de ordenação nas versões **Serial** e **Paralela/Concorrente**, utilizando **Java puro (sem bibliotecas externas)** e interface gráfica interativa nativa em **Java Swing**.

---

## 📌 Algoritmos Implementados

| Algoritmo | Implementação Serial | Implementação Paralela |
| :--- | :--- | :--- |
| **Bubble Sort** | Laço duplo clássico com otimização de parada antecipada | **Odd-Even Transposition Sort** com sincronização via `CyclicBarrier` |
| **Insertion Sort** | Inserção direta no prefixo ordenado | **Block Partitioning** (divisão em blocos independentes e mescla hierárquica concorrente) |
| **Merge Sort** | Divisão e conquista recursiva com vetor auxiliar pré-alocado | **ForkJoinPool** (`RecursiveAction`) com limiar adaptativo de granularidade |
| **Quick Sort** | Particionamento de Hoare com pivô por mediana de três | **ForkJoinPool** (`RecursiveAction`) com paralelização das partições |

---

## 🚀 Como Executar

O projeto foi construído em Java puro e não requer Maven, Gradle ou bibliotecas externas.

### 1. Interface Gráfica Interativa (Java Swing com Gráficos Dinâmicos)
Basta dar duplo-clique no arquivo:
```cmd
executar_gui.bat
```
Ou executar pelo terminal:
```bash
javac -encoding UTF-8 -d bin src/br/com/unifor/paralela/model/*.java src/br/com/unifor/paralela/algorithm/*.java src/br/com/unifor/paralela/algorithm/bubblesort/*.java src/br/com/unifor/paralela/algorithm/insertionsort/*.java src/br/com/unifor/paralela/algorithm/mergesort/*.java src/br/com/unifor/paralela/algorithm/quicksort/*.java src/br/com/unifor/paralela/benchmark/*.java src/br/com/unifor/paralela/ui/*.java src/br/com/unifor/paralela/*.java
java -cp bin br.com.unifor.paralela.Main
```

### 2. Modo Linha de Comando (Headless CLI)
Para executar os benchmarks diretamente via terminal:
```cmd
executar_cli.bat
```
Ou:
```bash
java -cp bin br.com.unifor.paralela.Main --cli
```

---

## 📊 Recursos da Aplicação

1. **Controle Flexível de Experimentos:**
   - Seleção individual dos 4 algoritmos;
   - Modos: Serial (1 thread), Paralelo (2, 4 e 8 threads);
   - Natureza dos dados: Aleatório, Quase Ordenado (90%), Inversamente Ordenado e Já Ordenado;
   - Tamanhos configuráveis: 1.000, 5.000, 10.000, 25.000 e 50.000 elementos;
   - Número de amostras (mínimo de 5 por configuração).

2. **Gráficos Dinâmicos em Swing Nativo (`Graphics2D`):**
   - Gráfico comparativo de **Tempo de Execução (ms)** por algoritmo e tamanho;
   - Gráfico de **Speedup e Escalabilidade** com linha teórica de Speedup Linear ($S = P$).

3. **Exportação CSV:**
   - Gera relatórios na pasta `output/` com métricas consolidadas (Média, Desvio Padrão, Min, Max, Speedup e Eficiência) e dados detalhados por amostra.

4. **Artigo Científico Formatado:**
   - Arquivo `Relatorio-Artigo-Ordenacao.docx` gerado estritamente sobre o template `Modelo-Artigo.docx` da UNIFOR (Arial 11, espaçamento 1,5, margens idênticas, logotipo oficial, resumo, metodologia, tabela de resultados reais, gráficos, conclusão e referências ABNT).
