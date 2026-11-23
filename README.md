# ☁️ CloudNotes

A simple, clean **Notes Management Web App** built with **Java 21, Spring Boot 3, Thymeleaf, Bootstrap 5** and **Firebase Firestore**.

Built by **Pablo Chomcobar**.

## Features

- Create, view, edit and delete notes (full CRUD)
- Notes saved in the cloud (Firebase Firestore)
- Search notes by title (case-insensitive)
- Newest notes first
- Form validation (browser + server)
- Success and error messages
- Delete confirmation dialog
- Friendly 404 / error pages
- Mobile responsive white, blue and gray UI
- Ready to deploy on Render (Docker)

---

## 📁 Project structure

```
cloudnotes/
├── pom.xml                         ← Maven dependencies
├── mvnw / mvnw.cmd                 ← Maven wrapper (no need to install Maven)
├── Dockerfile                      ← used by Render
├── render.yaml                     ← optional Render blueprint
├── serviceAccountKey.json          ← YOU add this (Firebase key, never push to GitHub)
└── src/main/
    ├── java/com/pablochomcobar/cloudnotes/
    │   ├── CloudNotesApplication.java     ← app starts here
    │   ├── config/FirebaseConfig.java     ← connects to Firebase
    │   ├── model/Note.java                ← note data (Firestore document)
    │   ├── model/NoteForm.java            ← form data + validation rules
    │   ├── repository/NoteRepository.java ← all Firestore read/write code
    │   ├── service/NoteService.java       ← business logic (search, timestamps)
    │   ├── controller/NoteController.java ← web pages / URLs
    │   └── exception/                     ← custom errors + global handler
    └── resources/
        ├── application.properties
        ├── templates/   (index, add-note, edit-note, error, fragments/)
        └── static/      (css/style.css, js/app.js, favicon.svg)
```

**Flow:** Browser → `NoteController` → `NoteService` → `NoteRepository` → Firestore

| URL | Method | What it does |
|---|---|---|
| `/` | GET | Home page, all notes (add `?search=word` to search) |
| `/notes/new` | GET | Add note page |
| `/notes` | POST | Save new note |
| `/notes/{id}/edit` | GET | Edit page (prefilled) |
| `/notes/{id}` | POST | Update note |
| `/notes/{id}/delete` | POST | Delete note |
| `/actuator/health` | GET | Health check (for Render) |

---

## ✅ Step 1: Install requirements (one time)

1. **Java 21 (JDK)**: download from https://adoptium.net (choose *Temurin 21 LTS*, Windows x64 `.msi`).
   During install, turn on **"Set JAVA_HOME variable"**.
2. Check in a **new** terminal:
   ```
   java -version
   ```
   It should say `21`.
3. You **don't need to install Maven**. The included `mvnw` / `mvnw.cmd` downloads it automatically the first time.
4. VS Code extension (recommended): **Extension Pack for Java** + **Spring Boot Extension Pack**.

---

## 🔥 Step 2: Set up Firebase (one time)

1. Go to https://console.firebase.google.com and click **Add project** → name it `cloudnotes` → create.
2. In the left menu open **Build → Firestore Database** → **Create database**.
   - Pick a location (e.g. `asia-south1` (Mumbai)).
   - Choose **Start in production mode** (fine, because the Admin SDK skips security rules).
3. Click the ⚙️ gear icon → **Project settings** → **Service accounts** tab.
4. Click **Generate new private key** → **Generate key**. A `.json` file downloads.
5. **Rename** that file to exactly:
   ```
   serviceAccountKey.json
   ```
6. Put it in the **project root folder** (same folder as `pom.xml`).

> ⚠️ This file is a password to your database. It is already in `.gitignore`, so it will **not** be pushed to GitHub. Never share it.

You don't need to create the `notes` collection yourself. It is created automatically when you save your first note.

---

## ▶️ Step 3: Run the app on your PC

1. Extract the zip, e.g. to `D:\Projects\cloudnotes`.
2. Make sure `serviceAccountKey.json` is inside that folder.
3. Open the folder in VS Code → open the terminal (``Ctrl + ` ``).
4. Run:

   **Windows (PowerShell / CMD):**
   ```
   .\mvnw.cmd spring-boot:run
   ```

   **Mac / Linux / Git Bash:**
   ```
   ./mvnw spring-boot:run
   ```

5. The first run downloads dependencies (a few minutes). When you see
   `Started CloudNotesApplication`, open:

   👉 **http://localhost:8080**

6. Stop the app with `Ctrl + C`.

**Live HTML editing (optional):** run with the dev profile so template changes show on refresh:
```
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

**Run as a jar (like production):**
```
.\mvnw.cmd clean package
java -jar target\cloudnotes.jar
```

**Run the tests** (no Firebase needed, they use mocks):
```
.\mvnw.cmd test
```

---

## 🐙 Step 4: Push to GitHub

Create an empty repo named `cloudnotes` on GitHub (no README), then in the VS Code terminal:

```
git init
git add .
git commit -m "CloudNotes: Spring Boot + Firestore notes app"
git branch -M main
git remote add origin https://github.com/<your-username>/cloudnotes.git
git push -u origin main
```

Before pushing, check that the key is NOT listed:
```
git status
```
`serviceAccountKey.json` must not appear. (`.gitignore` already blocks it.)

Later updates:
```
git add .
git commit -m "Describe your change"
git push
```

---

## 🚀 Step 5: Deploy on Render

Render has no built-in Java option, so this project uses the included **Dockerfile** (Render builds it for you).

1. Go to https://render.com → sign in with GitHub.
2. **New +** → **Web Service** → select your `cloudnotes` repo.
3. Settings:
   - **Language / Runtime:** `Docker`
   - **Branch:** `main`
   - **Instance type:** Free
4. **Environment Variables** → **Add Environment Variable**:
   - **Key:** `FIREBASE_CREDENTIALS`
   - **Value:** open `serviceAccountKey.json` in VS Code, select all (`Ctrl + A`), copy, and paste the **whole JSON** here.
5. (Optional) **Advanced → Health Check Path:** `/actuator/health`
6. Click **Create Web Service**. The first build takes about 5–10 minutes.
7. Your app will be live at `https://cloudnotes-xxxx.onrender.com` 🎉

> Free Render apps sleep after about 15 minutes without visits. The first visit after that takes around 50 seconds to wake up. That's normal.

**Alternative:** if pasting JSON gives problems, paste a Base64 version instead (the app detects it automatically). In PowerShell:
```
[Convert]::ToBase64String([IO.File]::ReadAllBytes("serviceAccountKey.json")) | Set-Clipboard
```

---

## 🛠️ Troubleshooting

| Problem | Fix |
|---|---|
| `Firebase credentials not found!` | `serviceAccountKey.json` is missing from the root folder, or `FIREBASE_CREDENTIALS` isn't set on Render. |
| `NOT_FOUND: The database (default) does not exist` | You skipped Step 2.2. Create the Firestore Database in the Firebase console. |
| `PERMISSION_DENIED` | Wrong key file, or a key from another project. Generate a new private key. |
| `'java' is not recognized` / wrong version | Install JDK 21 and reopen the terminal. Check `JAVA_HOME`. |
| `Port 8080 already in use` | Stop the other app, or run `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"` |
| `mvnw.cmd` blocked in PowerShell | Use CMD, or run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`. |
| Render build out of memory | Already handled with `-XX:MaxRAMPercentage=75`. Redeploy once more. |

---

## How credentials are loaded

`FirebaseConfig` checks these in order:
1. `FIREBASE_CREDENTIALS` env var (JSON text or Base64): **Render**
2. `FIREBASE_CREDENTIALS_PATH` env var (path to a JSON file)
3. `./serviceAccountKey.json` (project root): **local PC**
4. `src/main/resources/serviceAccountKey.json`

---

Made with ☕ Java & ☁️ Firebase by **Pablo Chomcobar**
