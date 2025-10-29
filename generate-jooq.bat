@echo off
REM Generate JOOQ code from database

echo ====================================
echo JOOQ Code Generation
echo ====================================
echo.

echo Building classpath...
cd roya-plugins\database

for /f "tokens=*" %%i in ('gradlew :roya-plugins:database:dependencies --configuration runtimeClasspath --quiet') do (
    echo !cp! %%i
    set cp=!cp!;%%i
)

echo.
echo Generating JOOQ classes...
java -cp "%cp%" org.jooq.codegen.GenerationTool src\main\resources\jooq-config.xml

echo.
echo ✓ JOOQ code generation complete!
echo Generated files in: build\generated\jooq

cd ..\..

