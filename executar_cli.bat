@echo off
rem ======================================================================
rem UNIFOR - Computacao Concorrente e Paralela
rem Execucao em Linha de Comando (Modo CLI)
rem ======================================================================

cd /d "%~dp0"

if not exist "bin" mkdir bin

echo Compilando fontes Java...
javac -encoding UTF-8 -d bin src\br\com\unifor\paralela\*.java src\br\com\unifor\paralela\model\*.java src\br\com\unifor\paralela\algorithm\*.java src\br\com\unifor\paralela\algorithm\bubblesort\*.java src\br\com\unifor\paralela\algorithm\insertionsort\*.java src\br\com\unifor\paralela\algorithm\mergesort\*.java src\br\com\unifor\paralela\algorithm\quicksort\*.java src\br\com\unifor\paralela\benchmark\*.java src\br\com\unifor\paralela\ui\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha na compilacao do projeto. Verifique se o JDK esta instalado.
    pause
    exit /b %ERRORLEVEL%
)

echo Executando benchmark headless via terminal...
java -cp bin br.com.unifor.paralela.Main --cli
pause
exit /b 0
