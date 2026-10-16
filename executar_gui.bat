@echo off
chcp 65001 > nul
echo ======================================================================
echo  UNIFOR - Computação Concorrente e Paralela
echo  Iniciando Interface Gráfica Java Swing (Benchmark de Algoritmos)
echo ======================================================================

if not exist "bin" mkdir bin

echo Compilando fontes Java...
javac -encoding UTF-8 -d bin src/br/com/unifor/paralela/model/*.java src/br/com/unifor/paralela/algorithm/*.java src/br/com/unifor/paralela/algorithm/bubblesort/*.java src/br/com/unifor/paralela/algorithm/insertionsort/*.java src/br/com/unifor/paralela/algorithm/mergesort/*.java src/br/com/unifor/paralela/algorithm/quicksort/*.java src/br/com/unifor/paralela/benchmark/*.java src/br/com/unifor/paralela/ui/*.java src/br/com/unifor/paralela/*.java

if %errorlevel% neq 0 (
    echo [ERRO] Falha na compilação do projeto.
    pause
    exit /b %errorlevel%
)

echo Iniciando aplicação Swing...
start javaw -cp bin br.com.unifor.paralela.Main
exit /b 0
