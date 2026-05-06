param(
    [Parameter(Mandatory=$true)]
    [string]$Version,

    [Parameter(Mandatory=$true)]
    [string]$Message
)

# Update mod_info.json
$modInfoPath = "mod_info.json"
$modInfo = Get-Content $modInfoPath -Raw | ConvertFrom-Json
$modInfo.version = $Version
$modInfo | ConvertTo-Json | Set-Content $modInfoPath

# Update changelog.txt
$changelogPath = "changelog.txt"
$existingContent = Get-Content $changelogPath -Raw
$Message = $Message -replace '\|', "`n    "
$newEntry = "V$Version`n    $Message`n`n"
$newContent = $newEntry + $existingContent
Set-Content $changelogPath $newContent

Write-Host "Updated version to $Version and added changelog entry: $Message"