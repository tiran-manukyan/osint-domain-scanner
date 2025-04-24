# 🕵️‍♂️ OSINT Domain Scanner

A full-stack web application that allows users to scan domains using the **Amass** OSINT tool and view results through a clean, responsive interface. 
The tool discovers subdomains, IP addresses, and related infrastructure information by running scans inside Docker containers, providing detailed visibility into the publicly exposed surface of a domain.
---

## 🔧 Tech Stack

- **Frontend**: React, TypeScript, Material UI (MUI)
- **Backend**: Kotlin, Spring Boot
- **Database**: PostgreSQL
- **Containerization**: Docker & Docker Compose
- **Tool**: [Amass](https://github.com/owasp-amass/amass) for domain scanning

---

## 🚀 Running without Docker
### Backend
Prepare DB and adjust parameters in *application.yml* for connection
```shell
./backend/gradlew -p ./backend clean build
```
```shell
java -jar ./backend/build/libs/app.jar
```

### Fronted
```shell
(cd frontend && npm install)
```
```shell
(cd frontend && npm run dev)
```

## 🐳 Running with Docker
```shell
docker compose up -d
```

## 🌐 Application URLs

- **Frontend (React App)**: [http://localhost:3000](http://localhost:3000)
- **Backend API (Spring Boot)**: [http://localhost:8080/api](http://localhost:8080/api)

