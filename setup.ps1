# =============================================================
#  HRMS - Automated Build & Deployment Script (No Maven)
#  setup.ps1
#
#  This script:
#    1. Downloads Apache Tomcat 9 (if not present)
#    2. Downloads required JARs from Maven Central
#    3. Compiles all Java source files with javac
#    4. Deploys the webapp to Tomcat's webapps folder
#    5. Starts Tomcat and opens the browser
# =============================================================

$ErrorActionPreference = "Stop"
$ROOT    = Split-Path -Parent $MyInvocation.MyCommand.Path
$JAVAC   = "C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot\bin\javac.exe"

# ---------- Directories ----------
$TOMCAT_DIR = "$ROOT\tomcat"
$LIB_DIR    = "$ROOT\lib"
$SRC_DIR    = "$ROOT\src\main\java"
$WEB_DIR    = "$ROOT\src\main\webapp"
$BUILD_DIR  = "$ROOT\build"
$CLASSES    = "$BUILD_DIR\WEB-INF\classes"
$WARNAME    = "HRMS"

# Temp build dir with NO SPACES in path (fixes javac path issues)
$TMPROOT = "C:\hrms_build"
$TMPSRC  = "$TMPROOT\src"
$TMPLIB  = "$TMPROOT\lib"
$TMPOUT  = "$TMPROOT\out"

# ---------- Download URLs ----------
$TOMCAT_URL  = "https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.98/bin/apache-tomcat-9.0.98.zip"
$SQLITE_URL  = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar"
$SLF4J_URL   = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar"
$SLF4J_SMPL  = "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar"
$GSON_URL    = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
$SERVLET_URL = "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar"

function Write-Step($msg) {
    Write-Host ""
    Write-Host ">>> $msg" -ForegroundColor Cyan
}

function Download-If-Missing($url, $dest) {
    if (!(Test-Path $dest)) {
        Write-Host "    Downloading $(Split-Path $dest -Leaf) ..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri $url -OutFile $dest -UseBasicParsing
        Write-Host "    Done." -ForegroundColor Green
    } else {
        Write-Host "    Already exists: $(Split-Path $dest -Leaf)" -ForegroundColor DarkGray
    }
}

# =============================================================
# STEP 1 - Download required JARs
# =============================================================
Write-Step "STEP 1: Downloading required JARs..."
New-Item -ItemType Directory -Force -Path $LIB_DIR | Out-Null

Download-If-Missing $SQLITE_URL  "$LIB_DIR\sqlite-jdbc.jar"
Download-If-Missing $SLF4J_URL   "$LIB_DIR\slf4j-api.jar"
Download-If-Missing $SLF4J_SMPL  "$LIB_DIR\slf4j-simple.jar"
Download-If-Missing $GSON_URL    "$LIB_DIR\gson.jar"
Download-If-Missing $SERVLET_URL "$LIB_DIR\servlet-api.jar"

# =============================================================
# STEP 2 - Download and extract Tomcat 9
# =============================================================
Write-Step "STEP 2: Setting up Apache Tomcat 9..."

if (!(Test-Path $TOMCAT_DIR)) {
    $zipPath = "$ROOT\tomcat.zip"
    Download-If-Missing $TOMCAT_URL $zipPath
    Write-Host "    Extracting Tomcat..." -ForegroundColor Yellow
    Expand-Archive -Path $zipPath -DestinationPath $ROOT -Force
    $extracted = Get-ChildItem $ROOT -Directory | Where-Object { $_.Name -like "apache-tomcat*" } | Select-Object -First 1
    if ($extracted) { Rename-Item $extracted.FullName $TOMCAT_DIR -Force }
    Remove-Item $zipPath -Force
    Write-Host "    Tomcat ready." -ForegroundColor Green
} else {
    Write-Host "    Tomcat already present." -ForegroundColor DarkGray
}

# =============================================================
# STEP 3 - Prepare build directory (WAR exploded structure)
# =============================================================
Write-Step "STEP 3: Preparing build directory..."

if (Test-Path $BUILD_DIR) { Remove-Item $BUILD_DIR -Recurse -Force }
New-Item -ItemType Directory -Force -Path $CLASSES            | Out-Null
New-Item -ItemType Directory -Force -Path "$BUILD_DIR\WEB-INF\lib" | Out-Null

Write-Host "    Copying webapp resources..." -ForegroundColor Yellow
Copy-Item "$WEB_DIR\*" -Destination $BUILD_DIR -Recurse -Force

# Bundle runtime JARs (servlet-api is provided by Tomcat, exclude it)
Copy-Item "$LIB_DIR\sqlite-jdbc.jar" "$BUILD_DIR\WEB-INF\lib\" -Force
Copy-Item "$LIB_DIR\slf4j-api.jar"   "$BUILD_DIR\WEB-INF\lib\" -Force
Copy-Item "$LIB_DIR\slf4j-simple.jar" "$BUILD_DIR\WEB-INF\lib\" -Force
Copy-Item "$LIB_DIR\gson.jar"        "$BUILD_DIR\WEB-INF\lib\" -Force

Write-Host "    Done." -ForegroundColor Green

# =============================================================
# STEP 4 - Compile Java (using temp dir with no spaces)
# =============================================================
Write-Step "STEP 4: Compiling Java source files..."

# Clean and prepare temp no-space workspace
if (Test-Path $TMPROOT) { Remove-Item $TMPROOT -Recurse -Force }
New-Item -ItemType Directory -Force -Path $TMPSRC | Out-Null
New-Item -ItemType Directory -Force -Path $TMPLIB | Out-Null
New-Item -ItemType Directory -Force -Path $TMPOUT | Out-Null

Write-Host "    Copying sources to C:\hrms_build\src ..." -ForegroundColor Yellow
Copy-Item "$SRC_DIR\*" -Destination $TMPSRC -Recurse -Force
Copy-Item "$LIB_DIR\servlet-api.jar" "$TMPLIB\" -Force
Copy-Item "$LIB_DIR\sqlite-jdbc.jar" "$TMPLIB\" -Force
Copy-Item "$LIB_DIR\slf4j-api.jar"   "$TMPLIB\" -Force
Copy-Item "$LIB_DIR\gson.jar"        "$TMPLIB\" -Force

# Collect .java files from temp dir (no spaces anywhere)
$javaFiles = Get-ChildItem -Path $TMPSRC -Filter "*.java" -Recurse `
             | Select-Object -ExpandProperty FullName
Write-Host "    Found $($javaFiles.Count) Java files." -ForegroundColor Yellow

# Write argfile — all paths under C:\hrms_build, no spaces
$sourceList = "$TMPROOT\sources.txt"
[System.IO.File]::WriteAllLines($sourceList, $javaFiles, [System.Text.Encoding]::ASCII)

$CP = "$TMPLIB\servlet-api.jar;$TMPLIB\sqlite-jdbc.jar;$TMPLIB\slf4j-api.jar;$TMPLIB\gson.jar"

Write-Host "    Running javac..." -ForegroundColor Yellow

# All paths have no spaces, safe to quote normally
$javacCmd = "`"$JAVAC`" -encoding UTF-8 -classpath `"$CP`" -d `"$TMPOUT`" `"@$sourceList`""
$result   = cmd /c $javacCmd 2>&1
$exitCode = $LASTEXITCODE

Remove-Item $sourceList -Force -ErrorAction SilentlyContinue

if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "COMPILATION FAILED:" -ForegroundColor Red
    $result | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    Write-Host ""
    Remove-Item $TMPROOT -Recurse -Force -ErrorAction SilentlyContinue
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "    Compilation successful! Copying .class files..." -ForegroundColor Green
Copy-Item "$TMPOUT\*" -Destination $CLASSES -Recurse -Force
Remove-Item $TMPROOT -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "    Classes deployed." -ForegroundColor Green

# =============================================================
# STEP 5 - Deploy to Tomcat webapps
# =============================================================
Write-Step "STEP 5: Deploying to Tomcat webapps..."

$WEBAPPS = "$TOMCAT_DIR\webapps\$WARNAME"
if (Test-Path $WEBAPPS) { Remove-Item $WEBAPPS -Recurse -Force }
Copy-Item $BUILD_DIR -Destination $WEBAPPS -Recurse -Force
Write-Host "    Deployed to: $WEBAPPS" -ForegroundColor Green

# =============================================================
# STEP 6 - Start Tomcat
# =============================================================
Write-Step "STEP 6: Starting Apache Tomcat on port 8085..."

# Kill any existing Tomcat process on port 8085
$proc = Get-NetTCPConnection -LocalPort 8085 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($proc) {
    Write-Host "    Stopping existing process on port 8085..." -ForegroundColor Yellow
    Stop-Process -Id $proc.OwningProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

$CATALINA_BAT = "$TOMCAT_DIR\bin\catalina.bat"
$javaHome     = "C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot"

Write-Host "    Starting Tomcat..." -ForegroundColor Yellow
$script = "set `"JAVA_HOME=$javaHome`" && set `"JRE_HOME=$javaHome`" && `"$CATALINA_BAT`" run"
Start-Process "cmd.exe" -ArgumentList "/c", $script -WorkingDirectory "$TOMCAT_DIR\bin" -NoNewWindow

Write-Host "    Waiting for Tomcat to initialize (12 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 12

# =============================================================
# STEP 7 - Open browser
# =============================================================
Write-Step "STEP 7: Opening HRMS in browser..."
$url = "http://localhost:8085/HRMS/login.html"
Start-Process $url

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host " HRMS is running at: $url"               -ForegroundColor Green
Write-Host " Admin   : admin@hrms.com / admin123"    -ForegroundColor Green
Write-Host " Employee: alice@hrms.com / alice123"    -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host " Tomcat logs : $TOMCAT_DIR\logs\catalina.out" -ForegroundColor DarkGray
Write-Host " To STOP     : Close the Tomcat console window" -ForegroundColor Yellow
