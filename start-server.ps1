# VaxiCare Lightweight Backend Server
# Provides full REST API persistence (database/users.json, database/appointments.json)
# Serves the web frontend at http://localhost:8080

$baseDir = $PSScriptRoot
$webDir = Join-Path $baseDir "src\main\webapp"
$dbUsersFile = Join-Path $baseDir "database\users.json"
$dbApptsFile = Join-Path $baseDir "database\appointments.json"
$dbAefiFile = Join-Path $baseDir "database\aefi_reports.json"

# Ensure database files exist
if (-not (Test-Path $dbUsersFile)) {
    @'
[
  { "username": "arya_prakash", "pass": "pass123", "fullName": "Arya Prakash", "role": "PATIENT", "phone": "9876543210" },
  { "username": "admin", "pass": "admin123", "fullName": "Dr. Radhakrishnan (Nodal Officer)", "role": "ADMIN", "phone": "9876543200" }
]
'@ | Set-Content -Path $dbUsersFile -Encoding UTF8
}
if (-not (Test-Path $dbApptsFile)) {
    "[]" | Set-Content -Path $dbApptsFile -Encoding UTF8
}
if (-not (Test-Path $dbAefiFile)) {
    "[]" | Set-Content -Path $dbAefiFile -Encoding UTF8
}

$port = 8080
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:$port/")
$listener.Prefixes.Add("http://127.0.0.1:$port/")

try {
    $listener.Start()
} catch {
    Write-Host "Port $port in use or access denied. Trying port 8000..." -ForegroundColor Yellow
    $port = 8000
    $listener = New-Object System.Net.HttpListener
    $listener.Prefixes.Add("http://localhost:$port/")
    $listener.Prefixes.Add("http://127.0.0.1:$port/")
    $listener.Start()
}

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "     VAXICARE BACKEND SERVER & DATABASE ENGINE ONLINE" -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host " Server URL     : http://localhost:$port/" -ForegroundColor White
Write-Host " Web Root       : $webDir" -ForegroundColor White
Write-Host " Users Database : $dbUsersFile" -ForegroundColor White
Write-Host " Appts Database : $dbApptsFile" -ForegroundColor White
Write-Host " Press Ctrl+C in this console window to stop the server." -ForegroundColor Yellow
Write-Host "================================================================================" -ForegroundColor Cyan

# Open default browser
Start-Process "http://localhost:$port/"

while ($listener.IsListening) {
    try {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response

        # Add CORS Headers
        $response.Headers.Add("Access-Control-Allow-Origin", "*")
        $response.Headers.Add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        $response.Headers.Add("Access-Control-Allow-Headers", "Content-Type")

        if ($request.HttpMethod -eq "OPTIONS") {
            $response.StatusCode = 200
            $response.Close()
            continue
        }

        $rawUrl = $request.RawUrl
        $path = $request.Url.AbsolutePath
        $method = $request.HttpMethod

        # Helper to read request body
        $bodyText = ""
        if ($request.HasEntityBody) {
            $reader = New-Object System.IO.StreamReader($request.InputStream, $request.ContentEncoding)
            $bodyText = $reader.ReadToEnd()
            $reader.Close()
        }

        # JSON response helper
        $sendJson = {
            param($data, $code = 200)
            $jsonString = $data | ConvertTo-Json -Depth 5 -Compress
            $buffer = [System.Text.Encoding]::UTF8.GetBytes($jsonString)
            $response.StatusCode = $code
            $response.ContentType = "application/json; charset=utf-8"
            $response.ContentLength64 = $buffer.Length
            $response.OutputStream.Write($buffer, 0, $buffer.Length)
            $response.Close()
        }

        # Clean JSON list helpers (guarantees flat JSON arrays without PS wrappers)
        function Read-JsonList($filePath) {
            if (-not (Test-Path $filePath)) { return @() }
            $raw = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
            if ([string]::IsNullOrWhiteSpace($raw) -or $raw.Trim() -eq "[]") { return @() }
            return @($raw | ConvertFrom-Json)
        }

        function Save-JsonList($filePath, $list) {
            $json = @($list) | ConvertTo-Json -Depth 6
            [System.IO.File]::WriteAllText($filePath, $json, [System.Text.Encoding]::UTF8)
        }

        # ---------------------------------------------------------------------
        # API ENDPOINTS
        # ---------------------------------------------------------------------
        if ($path -eq "/api/status" -and $method -eq "GET") {
            $users = Read-JsonList $dbUsersFile
            $appts = Read-JsonList $dbApptsFile
            &$sendJson @{
                status = "ONLINE"
                database = "ACTIVE"
                userCount = ($users.Count)
                appointmentCount = ($appts.Count)
                timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
            }
            continue
        }

        # API: Login
        if ($path -eq "/api/login" -and $method -eq "POST") {
            $loginReq = $bodyText | ConvertFrom-Json
            $u = ($loginReq.username).ToString().Trim()
            $p = ($loginReq.password).ToString().Trim()

            $users = Read-JsonList $dbUsersFile
            $matchedUser = $null
            foreach ($user in $users) {
                if ($user.username.ToString().ToLower() -eq $u.ToLower() -and $user.pass.ToString() -eq $p) {
                    $matchedUser = $user
                    break
                }
            }

            if ($matchedUser) {
                Write-Host "[LOGIN SUCCESS] User: $u ($($matchedUser.role))" -ForegroundColor Green
                &$sendJson @{
                    success = $true
                    message = "Login successful"
                    user = @{
                        username = $matchedUser.username
                        fullName = $matchedUser.fullName
                        role = $matchedUser.role
                        phone = $matchedUser.phone
                    }
                }
            } else {
                Write-Host "[LOGIN FAILED] Invalid credentials for: $u" -ForegroundColor Red
                &$sendJson @{
                    success = $false
                    message = "Invalid username or password. Default test accounts: arya_prakash / pass123 or admin / admin123"
                } 401
            }
            continue
        }

        # API: Register
        if ($path -eq "/api/register" -and $method -eq "POST") {
            $regReq = $bodyText | ConvertFrom-Json
            $username = ($regReq.username).ToString().Trim()
            $password = ($regReq.password).ToString().Trim()
            $fullName = ($regReq.fullName).ToString().Trim()
            $phone = ($regReq.phone).ToString().Trim()

            $users = Read-JsonList $dbUsersFile
            $exists = $false
            foreach ($user in $users) {
                if ($user.username.ToString().ToLower() -eq $username.ToLower()) {
                    $exists = $true
                    break
                }
            }

            if ($exists) {
                Write-Host "[REGISTER REJECTED] Username '$username' already exists" -ForegroundColor Yellow
                &$sendJson @{
                    success = $false
                    message = "Username '$username' is already registered. Please choose another username."
                } 400
            } else {
                $newUser = [PSCustomObject]@{
                    username = $username
                    pass = $password
                    fullName = $fullName
                    role = "PATIENT"
                    phone = $phone
                }
                $users = @($users) + $newUser
                Save-JsonList $dbUsersFile $users

                Write-Host "[REGISTER SUCCESS] New Patient Saved to DB: $username ($fullName)" -ForegroundColor Green
                &$sendJson @{
                    success = $true
                    message = "Patient account created successfully and saved to backend database!"
                    user = @{
                        username = $username
                        fullName = $fullName
                        role = "PATIENT"
                        phone = $phone
                    }
                }
            }
            continue
        }

        # API: Book Appointment
        if ($path -eq "/api/book" -and $method -eq "POST") {
            $bReq = $bodyText | ConvertFrom-Json
            $appts = Read-JsonList $dbApptsFile

            $rand = Get-Random -Minimum 1000 -Maximum 9999
            $newBooking = [PSCustomObject]@{
                bookingId = "BK-$rand"
                patientName = $bReq.patientName
                contact = $bReq.contact
                vaccine = $bReq.vaccine
                centre = $bReq.centre
                date = (Get-Date).ToString("yyyy-MM-dd")
                status = "CONFIRMED"
                created_at = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
            }

            $appts = @($appts) + $newBooking
            Save-JsonList $dbApptsFile $appts

            Write-Host "[BOOKING SAVED] Slot: $($newBooking.bookingId) for $($newBooking.patientName) at $($newBooking.centre)" -ForegroundColor Cyan
            &$sendJson @{
                success = $true
                message = "Appointment booked and recorded in backend database!"
                booking = $newBooking
            }
            continue
        }

        # API: Get Appointments
        if ($path -eq "/api/appointments" -and $method -eq "GET") {
            $appts = Read-JsonList $dbApptsFile
            &$sendJson @($appts)
            continue
        }

        # API: AEFI Report
        if ($path -eq "/api/aefi" -and $method -eq "POST") {
            $aefiReq = $bodyText | ConvertFrom-Json
            $reports = Read-JsonList $dbAefiFile

            $rand = Get-Random -Minimum 1000 -Maximum 9999
            $newReport = [PSCustomObject]@{
                reportId = "AEFI-$rand"
                patientName = $aefiReq.patientName
                phone = $aefiReq.phone
                severity = $aefiReq.severity
                timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
                status = "PENDING_TRIAGE"
            }

            $reports = @($reports) + $newReport
            Save-JsonList $dbAefiFile $reports

            Write-Host "[AEFI REPORT SAVED] Report ID: $($newReport.reportId) (Severity: $($newReport.severity))" -ForegroundColor Red
            &$sendJson @{
                success = $true
                message = "AEFI report saved to clinical surveillance database!"
                report = $newReport
            }
            continue
        }

        # ---------------------------------------------------------------------
        # STATIC FILE SERVING
        # ---------------------------------------------------------------------
        if ($path -eq "/" -or $path -eq "") {
            $filePath = Join-Path $webDir "index.html"
        } else {
            $cleanRelative = $path.TrimStart("/").Replace("/", "\")
            $filePath = Join-Path $webDir $cleanRelative
        }

        if (Test-Path $filePath -PathType Leaf) {
            $bytes = [System.IO.File]::ReadAllBytes($filePath)
            $ext = [System.IO.Path]::GetExtension($filePath).ToLower()
            $contentType = "application/octet-stream"

            switch ($ext) {
                ".html" { $contentType = "text/html; charset=utf-8" }
                ".htm"  { $contentType = "text/html; charset=utf-8" }
                ".css"  { $contentType = "text/css; charset=utf-8" }
                ".js"   { $contentType = "application/javascript; charset=utf-8" }
                ".json" { $contentType = "application/json; charset=utf-8" }
                ".png"  { $contentType = "image/png" }
                ".jpg"  { $contentType = "image/jpeg" }
                ".svg"  { $contentType = "image/svg+xml" }
            }

            $response.StatusCode = 200
            $response.ContentType = $contentType
            $response.ContentLength64 = $bytes.Length
            $response.OutputStream.Write($bytes, 0, $bytes.Length)
            $response.Close()
        } else {
            $response.StatusCode = 404
            $errBytes = [System.Text.Encoding]::UTF8.GetBytes("404 Not Found: $path")
            $response.ContentLength64 = $errBytes.Length
            $response.OutputStream.Write($errBytes, 0, $errBytes.Length)
            $response.Close()
        }
    } catch {
        Write-Host "Request handling error: $_" -ForegroundColor Red
    }
}
