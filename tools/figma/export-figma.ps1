<#
.SYNOPSIS
    Figma 파일 데이터를 내려받아 개발에 쓸 수 있게 저장합니다.

.DESCRIPTION
    1) 파일 전체 데이터(file.json): 화면 이름, 문구, 색 값이 모두 들어 있습니다. (API 1회)
    2) 화면 목록(frames.csv): file.json에서 만들기 때문에 API를 쓰지 않습니다.
    3) -Images를 붙이면 화면별 PNG도 내려받습니다. (화면 20개마다 API 1회)
    결과는 tools/figma/out 폴더와 tools/figma/figma-export.zip에 저장됩니다.

    Figma 무료(Starter) 플랜 파일은 API 호출 횟수가 한 달에 몇 번으로 제한될 수 있어서,
    이미 받은 file.json이 있으면 다시 받지 않습니다. (-Refresh로 강제)

.EXAMPLE
    $env:FIGMA_TOKEN = "figd_로_시작하는_토큰"
    powershell -ExecutionPolicy Bypass -File .\tools\figma\export-figma.ps1

.EXAMPLE
    # 특정 화면만 PNG로 (ID는 frames.csv의 Id 열)
    powershell -ExecutionPolicy Bypass -File .\tools\figma\export-figma.ps1 -Images -OnlyIds "12:34","12:56"
#>
[CmdletBinding()]
param(
    # Figma 주소에서 /design/ 바로 다음 부분
    [string]$FileKey = "BV806hHQazkQw5243Euuzz",

    # 화면 PNG도 내려받기
    [switch]$Images,

    # PNG로 받을 화면 ID (비우면 전체)
    [string[]]$OnlyIds = @(),

    # PNG 배율 (1 = 원래 크기, 2 = 2배)
    [ValidateRange(0.5, 4)]
    [double]$Scale = 2,

    # 이미 받은 file.json이 있어도 다시 받기
    [switch]$Refresh
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"   # Windows PowerShell에서 다운로드가 매우 느려지는 문제 방지
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

if (-not $env:FIGMA_TOKEN) {
    throw "FIGMA_TOKEN 환경 변수가 없습니다. tools/figma/README.md의 '토큰 만들기'를 먼저 하세요."
}
$headers = @{ "X-Figma-Token" = $env:FIGMA_TOKEN }
$outDir = Join-Path $PSScriptRoot "out"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

function Invoke-Figma {
    param([string]$Uri, [string]$OutFile)
    try {
        if ($OutFile) {
            Invoke-WebRequest -Uri $Uri -Headers $headers -OutFile $OutFile -UseBasicParsing | Out-Null
        } else {
            return Invoke-RestMethod -Uri $Uri -Headers $headers
        }
    } catch {
        $response = $_.Exception.Response
        if ($response) {
            $status = [int]$response.StatusCode
            if ($status -eq 429) {
                throw "Figma API 호출 한도를 넘었습니다 (429). 이미 받은 out 폴더의 내용만 보내 주세요."
            }
            if ($status -eq 403) {
                throw "권한이 없습니다 (403). 토큰이 맞는지, 이 파일을 볼 수 있는 계정의 토큰인지 확인하세요."
            }
            if ($status -eq 404) {
                throw "파일을 찾을 수 없습니다 (404). -FileKey 값을 확인하세요."
            }
        }
        throw
    }
}

# JSON 객체(사전)에서 키가 있을 때만 값을 꺼낸다. Windows PowerShell 5.1과 PowerShell 7 모두에서 동작.
function Get-Field {
    param($Object, [string]$Key)
    if ($Object -is [System.Collections.IDictionary] -and $Object.Contains($Key)) {
        return , $Object[$Key]
    }
    return $null
}

# 1) 파일 전체 JSON: 파싱하지 않고 그대로 저장
$fileJson = Join-Path $outDir "file.json"
if ($Refresh -or -not (Test-Path $fileJson)) {
    Write-Host "1/3 Figma 파일 데이터를 내려받는 중... (API 1회)"
    Invoke-Figma -Uri ("https://api.figma.com/v1/files/{0}" -f $FileKey) -OutFile $fileJson
} else {
    Write-Host "1/3 이미 받은 file.json을 사용합니다. (다시 받으려면 -Refresh)"
}
$sizeMb = [math]::Round((Get-Item $fileJson).Length / 1MB, 1)
Write-Host ("    저장: {0} ({1} MB)" -f $fileJson, $sizeMb)

# 2) 화면(프레임) 목록: 큰 JSON도 읽을 수 있는 방식으로 파싱
Write-Host "2/3 화면 목록을 만드는 중... (API 사용 안 함)"
$raw = [IO.File]::ReadAllText($fileJson, [Text.Encoding]::UTF8)
if ($PSVersionTable.PSVersion.Major -ge 6) {
    $doc = $raw | ConvertFrom-Json -AsHashtable -Depth 1000
} else {
    Add-Type -AssemblyName System.Web.Extensions
    $serializer = New-Object System.Web.Script.Serialization.JavaScriptSerializer
    $serializer.MaxJsonLength = [int]::MaxValue
    $serializer.RecursionLimit = 1000
    $doc = $serializer.DeserializeObject($raw)
}

$frames = New-Object System.Collections.Generic.List[object]
function Add-Frames {
    param($Node, [string]$PageName, [string]$SectionName)
    $children = Get-Field $Node "children"
    if ($null -eq $children) { return }
    foreach ($child in $children) {
        $type = Get-Field $child "type"
        if ($type -eq "SECTION") {
            Add-Frames -Node $child -PageName $PageName -SectionName (Get-Field $child "name")
        } elseif ($type -eq "FRAME" -or $type -eq "COMPONENT" -or $type -eq "COMPONENT_SET") {
            $box = Get-Field $child "absoluteBoundingBox"
            $frames.Add([pscustomobject]@{
                Id      = Get-Field $child "id"
                Page    = $PageName
                Section = $SectionName
                Name    = Get-Field $child "name"
                Width   = if ($box) { [int](Get-Field $box "width") } else { 0 }
                Height  = if ($box) { [int](Get-Field $box "height") } else { 0 }
            })
        }
    }
}
foreach ($page in (Get-Field (Get-Field $doc "document") "children")) {
    Add-Frames -Node $page -PageName (Get-Field $page "name") -SectionName ""
}
$framesCsv = Join-Path $outDir "frames.csv"
$frames | Export-Csv -Path $framesCsv -NoTypeInformation -Encoding UTF8
Write-Host ("    화면 {0}개 → {1}" -f $frames.Count, $framesCsv)

# 3) 화면 PNG (선택)
if ($Images) {
    $targets = @($frames.ToArray())
    if ($OnlyIds.Count -gt 0) {
        $targets = @($targets | Where-Object { $OnlyIds -contains $_.Id })
    }
    $imgDir = Join-Path $outDir "frames"
    New-Item -ItemType Directory -Force -Path $imgDir | Out-Null
    $batchSize = 20
    $calls = [math]::Ceiling($targets.Count / $batchSize)
    Write-Host ("3/3 화면 PNG {0}개를 내려받는 중... (API {1}회)" -f $targets.Count, $calls)
    $scaleText = $Scale.ToString([Globalization.CultureInfo]::InvariantCulture)
    for ($i = 0; $i -lt $targets.Count; $i += $batchSize) {
        $last = [math]::Min($i + $batchSize, $targets.Count) - 1
        $batch = $targets[$i..$last]
        $ids = ($batch | ForEach-Object { $_.Id }) -join ","
        $uri = "https://api.figma.com/v1/images/{0}?ids={1}&format=png&scale={2}" -f $FileKey, [uri]::EscapeDataString($ids), $scaleText
        $result = Invoke-Figma -Uri $uri
        foreach ($frame in $batch) {
            $url = $result.images.($frame.Id)
            if (-not $url) {
                Write-Warning ("이미지를 만들지 못한 화면: {0}" -f $frame.Name)
                continue
            }
            $safeName = $frame.Name -replace '[\\/:*?"<>|]', '_'
            $fileName = "{0}__{1}.png" -f ($frame.Id -replace '[:;]', '-'), $safeName
            Invoke-WebRequest -Uri $url -OutFile (Join-Path $imgDir $fileName) -UseBasicParsing | Out-Null
        }
        Write-Host ("    {0}/{1}" -f ($last + 1), $targets.Count)
    }
} else {
    Write-Host "3/3 화면 PNG는 건너뜁니다. (받으려면 -Images)"
}

# 4) 보내기 쉽게 압축 (토큰은 들어 있지 않음)
$zip = Join-Path $PSScriptRoot "figma-export.zip"
if (Test-Path $zip) { Remove-Item $zip }
Compress-Archive -Path (Join-Path $outDir "*") -DestinationPath $zip
Write-Host ""
Write-Host ("완료: {0}" -f $zip)
Write-Host "이 zip 파일을 대화창에 올려 주세요. 토큰은 들어 있지 않습니다."
