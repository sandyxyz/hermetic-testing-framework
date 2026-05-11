# Local Setup

This project needs Java, Maven, Docker Desktop, and WSL 2 on Windows.

## Current Project Maven

A local Maven copy is installed under `.tools/` and exposed through:

```powershell
.\mvn.cmd -version
```

Use this project-local command instead of global `mvn`:

```powershell
.\mvn.cmd test
```

## Docker Desktop

Testcontainers needs Docker. On Windows, install Docker Desktop with the WSL 2 backend.

1. Open PowerShell as Administrator.
2. Run:

   ```powershell
   wsl --install
   ```

3. Restart Windows if prompted.
4. Install Docker Desktop from the official Docker installer.
5. Start Docker Desktop and wait until it says the engine is running.
6. Verify:

   ```powershell
   docker --version
   docker run hello-world
   ```

7. Run this project's hermetic test suite:

   ```powershell
   .\mvn.cmd test
   ```

## Run Services With Docker Compose

After Docker Desktop is running:

```powershell
.\mvn.cmd -DskipTests package
docker compose up --build
```

Order Service:

```http
http://localhost:8081/orders
```

Payment Service:

```http
http://localhost:8082/payments
```
