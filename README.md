# FinalProjectLedZeppelin

## 📦 Project overview

Учебный full-stack проект:

-   **Backend:** Spring Boot 4, Java 21\
-   **Frontend:** React + Vite\
-   **Database:** PostgreSQL\
-   **Monitoring:** Prometheus, Grafana, Loki, Promtail\
-   **CI/CD:** GitHub Actions + Docker Compose

Проект полностью контейнеризирован и может быть запущен на любой машине
с Docker.

------------------------------------------------------------------------

## 🚀 Run locally (Docker)

### Requirements

-   Docker
-   Docker Compose

### Start application

В корне проекта:

``` bash
docker compose -f docker-compose.ci.yml up -d
```

При первом запуске будут автоматически собраны: - backend - frontend -
все сервисы мониторинга

### Stop application

``` bash
docker compose -f docker-compose.ci.yml down -v
```

------------------------------------------------------------------------

## 🌍 Available services

После запуска:

-   Frontend: http://localhost\
-   Backend health: http://localhost:8080/actuator/health\
-   Grafana: http://localhost:3000
    -   default login/password: `admin / admin`

------------------------------------------------------------------------

## 🔐 Environment variables

Часть конфигурации (SMTP для Grafana alerts) **не хранится в
репозитории**.

Перед запуском необходимо создать файл:

    monitoring/password.env

### Example `monitoring/password.env`

``` env
GF_SMTP_USER=example@gmail.com
GF_SMTP_PASSWORD=abcdefghijklmnop
GF_SMTP_FROM_ADDRESS=example@gmail.com
GF_SMTP_FROM_NAME=Grafana
ALERT_EMAIL_TO=example@gmail.com
```

⚠️\
- Файл `monitoring/password.env` добавлен в `.gitignore` - В CI/CD этот
файл создаётся автоматически из GitHub Secrets - Для локального запуска
можно использовать любые тестовые значения, если email-алерты не нужны

------------------------------------------------------------------------

## 📊 Logs & Monitoring

-   **Prometheus** собирает метрики backend'а
-   **Loki + Promtail** собирают логи приложения
-   В Grafana логи доступны через **Explore → Loki**

Пример запроса:

    {job="spring"}

или

    {app="FinalProjectLedZeppelin"}

------------------------------------------------------------------------

## 🔄 CI/CD

При каждом `push` в основную ветку (`master` / `main`) автоматически
запускается GitHub Actions pipeline:

-   сборка Docker-образов
-   разворачивание полного стека через Docker Compose
-   health-checks сервисов
-   автоматическое завершение окружения
