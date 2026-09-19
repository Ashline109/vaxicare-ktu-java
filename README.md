# 💉 VaxiCare — Smart Clinical Vaccination Management System
> **KTU S3 Computer Science & Engineering — Java OOP Mini Project**

A comprehensive, full-stack clinical immunization management platform designed to automate multi-dose vaccine scheduling, monitor cold-chain inventory, triage adverse side-effects (AEFI), and provide an interactive real-time patient portal.

🌐 **Live Web Demo**: [https://arya200631-del.github.io/vaxicare-ktu-java/](https://arya200631-del.github.io/vaxicare-ktu-java/)  
📂 **GitHub Repository**: [https://github.com/arya200631-del/vaxicare-ktu-java](https://github.com/arya200631-del/vaxicare-ktu-java)

---

## 👥 Project Team (Group of 4)
| Sl No. | Name | Register Number / Roll No. | Contribution / Role |
|:---:|:---|:---|:---|
| 1 | *Member 1 (Lead)* | *[Reg No]* | Architecture & Full-Stack Integration |
| 2 | *Member 2* | *[Reg No]* | Core Java OOP & Business Logic |
| 3 | *Member 3* | *[Reg No]* | Web UI/UX & Responsive Front-End |
| 4 | *Member 4* | *[Reg No]* | Database Persistence & Testing |

---

## 🚀 Key Features

- **Object-Oriented Architecture (OOP)**:
  - Polymorphism & Inheritance (`Patient`, `Vaccine`, `ClinicalStaff`)
  - Custom Exception Handling (`InsufficientGapException`)
  - Multi-threaded AEFI triage queue simulation (`AefiTriageService`)
- **Interactive Web Portal**:
  - Hero immunization campaign showcase & quick appointment booking
  - Live interactive city map (Leaflet & OpenStreetMap) with immunization centre pins
  - Client-side & REST API data synchronization (JSON persistence)
  - Emergency 24/7 hotline callouts and post-inoculation guideline repository

---

## 💻 How to Run the Project

### Option A: 1-Click Launch (Web Portal + REST Engine)
1. Double-click **`start-server.bat`** in this project folder.
2. The server starts on `http://localhost:8080` and opens in your default browser.

### Option B: Core Java Console & Viva Verification Suite
1. Open the project in VS Code / IntelliJ IDEA.
2. Run `src/main/java/com/vaxicare/MainApp.java`.
3. Choose **Option 7** to run the complete automated KTU OOP verification suite.

---

## 🛠️ Tech Stack
- **Language**: Java 17 / 21
- **Build Tool**: Apache Maven
- **Frontend**: HTML5, CSS3 (Custom Glassmorphic Theme), JavaScript (ES6+), Leaflet.js
- **Data Persistence**: JSON / XML Clinical Records
