param(
    [switch]$Check
)

$ErrorActionPreference = "Stop"
$scriptRoot = $PSScriptRoot
$formatterVersion = "1.28.0"
$formatterPath = Join-Path $env:USERPROFILE ".m2\repository\com\google\googlejavaformat\google-java-format\$formatterVersion\google-java-format-$formatterVersion-all-deps.jar"

if (-not (Test-Path -LiteralPath $formatterPath)) {
    & (Join-Path $scriptRoot "mvnw.cmd") dependency:get "-Dartifact=com.google.googlejavaformat:google-java-format:$formatterVersion`:jar:all-deps"
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

$javaFiles = Get-ChildItem (Join-Path $scriptRoot "src/main/java"), (Join-Path $scriptRoot "src/test/java") -Recurse -Filter "*.java" |
    Select-Object -ExpandProperty FullName

if ($Check) {
    & java -jar $formatterPath --dry-run --set-exit-if-changed $javaFiles
} else {
    & java -jar $formatterPath --replace $javaFiles
}

exit $LASTEXITCODE
