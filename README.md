<div align="center">

# 📅 VVS Calendar

**Agendador de reuniões em Spring Boot com feed iCal e descoberta de eventos**

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apachemaven&logoColor=white)
![H2](https://img.shields.io/badge/BD-H2-1F6FEB)
![CI](https://github.com/pedrorosa3/VVS_Calendar/actions/workflows/maven.yml/badge.svg)

👤 **Autor:** Pedro Rosa &nbsp;·&nbsp; 🗓️ **Data:** junho de 2026 &nbsp;·&nbsp; 🎓 Verificação e Validação de Software

</div>

---

## 📖 Sobre o projeto

O **VVS Calendar** é uma aplicação web para marcar reuniões. Permite:

- 🔐 **Criar conta e iniciar sessão**
- 🤝 **Propor reuniões** e convidar outros utilizadores
- ✅ **Aceitar ou recusar convites**
- 📆 **Subscrever um feed iCal pessoal** (Apple Calendar, Google Calendar, Outlook)
- 🎟️ **Descobrir eventos reais** no Ticketmaster, SeatGeek e na Agenda Cultural de Lisboa e adicioná-los ao calendário

Este repositório acompanha o trabalho de **Verificação e Validação de Software**: testes unitários, de integração, de repositório (DbSetup), de API (REST Assured), com *mocks* HTTP (WireMock) e *end-to-end* (Selenium + HtmlUnit), cobertura com **JaCoCo** e uma pipeline de **CI no GitHub Actions**.

---

## 🧰 Requisitos

| Ferramenta | Versão |
| --- | --- |
| ☕ Java (JDK) | 17 ou superior |
| 📦 Maven | 3.9 ou superior |

A aplicação usa uma base de dados **H2 embebida**, guardada em `./data/`, por isso não é preciso instalar mais nada.

---

## 🚀 Como executar

### 1. Clonar o repositório

```bash
git clone https://github.com/pedrorosa3/VVS_Calendar.git
cd VVS_Calendar
```

### 2. Arrancar a aplicação

```bash
mvn spring-boot:run
```

### 3. Abrir no browser

Abrir 👉 **<http://localhost:8080>**, criar conta em `/register` e iniciar sessão.

### Alternativa: gerar e correr o JAR

```bash
mvn clean package -DskipTests
java -jar target/meetings-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Testes e cobertura

Correr toda a bateria de testes:

```bash
mvn clean test
```

O relatório de cobertura do **JaCoCo** é gerado automaticamente em:

```
target/site/jacoco/index.html
```

Correr só uma classe de testes:

```bash
mvn test -Dtest=MeetingServiceTest
```

> 🤖 A pipeline [`.github/workflows/maven.yml`](.github/workflows/maven.yml) executa `mvn clean test` com o JDK 17 em cada *push* e em cada *pull request* para a `main`.

---

## 🗺️ Rotas principais

| Caminho | Descrição |
| --- | --- |
| `/register`, `/login` | Criar conta e iniciar sessão |
| `/calendar` | As tuas reuniões e convites pendentes |
| `/meetings/new` | Propor uma nova reunião |
| `/discover` | Pesquisar eventos (Ticketmaster / SeatGeek / Agenda Cultural) |
| `/ical/{token}.ics` | Feed iCal pessoal (subscrevível via `webcal://`) |
| `/h2-console` | Consola da base de dados H2 |

O URL do feed iCal aparece no fundo da página `/calendar`. Basta colá-lo no teu cliente de calendário como **calendário subscrito** para receberes todas as reuniões, só em modo de leitura.

---

## 🔑 Configurar a descoberta de eventos (opcional)

Sem chaves, o `/discover` continua a funcionar com a **Agenda Cultural de Lisboa**, que não precisa de credenciais. Para ativar os outros fornecedores (ambos **gratuitos**):

<details>
<summary><b>🎫 Ticketmaster (Discovery API)</b></summary>

1. Criar conta em <https://developer.ticketmaster.com/>.
2. Ir a **My Account → My Apps**.
3. Copiar a **Consumer Key** (o *Consumer Secret* não é necessário).

</details>

<details>
<summary><b>🏟️ SeatGeek (Platform API)</b></summary>

1. Criar conta em <https://platform.seatgeek.com/>.
2. Criar uma app (pode usar `http://localhost:8080` como App URL).
3. Copiar o **Client ID**.

</details>

Passar as chaves como **variáveis de ambiente**, para não ficarem no git:

**Bash / Linux / macOS**

```bash
TICKETMASTER_API_KEY=a_tua_chave \
SEATGEEK_CLIENT_ID=o_teu_client_id \
mvn spring-boot:run
```

**PowerShell (Windows)**

```powershell
$env:TICKETMASTER_API_KEY = "a_tua_chave"
$env:SEATGEEK_CLIENT_ID = "o_teu_client_id"
mvn spring-boot:run
```

| Variável | Descrição | Por omissão |
| --- | --- | --- |
| `TICKETMASTER_API_KEY` | Chave da Ticketmaster | *(vazio, fornecedor desligado)* |
| `SEATGEEK_CLIENT_ID` | Client ID do SeatGeek | *(vazio, fornecedor desligado)* |
| `TICKETMASTER_COUNTRY` | Código ISO do país (vazio = global) | `PT` |

---

## 🗂️ Estrutura do projeto

```
src/main/java/com/example/meetings/
├── config/        Spring Security
├── controller/    Endpoints HTTP
├── discover/      Fornecedores de eventos + DiscoveryService
├── model/         Entidades JPA (User, Meeting, MeetingParticipant)
├── repository/    Spring Data JPA
└── service/       Lógica de negócio + geração do iCal
src/main/resources/
├── application.properties
├── static/css/    Folha de estilos
└── templates/     Vistas Thymeleaf
src/test/java/com/example/meetings/
├── controller/    Testes de controladores e API
├── discover/      Testes dos fornecedores (WireMock)
├── e2e/           Testes end-to-end (Selenium + HtmlUnit)
├── model/         Testes de entidades
├── repository/    Testes de repositório (DbSetup)
└── service/       Testes de serviços
```

---

## ⚙️ Como funcionam as reuniões

- 📌 Ao **propor** uma reunião, o horário fica logo bloqueado no calendário do organizador (que aceita automaticamente o próprio convite) e são criados convites `PENDING` para os convidados. A reunião fica **provisória** (*tentative*).
- ✅ Quando **todos os convidados aceitam**, a reunião passa a **confirmada**.
- ❌ Um convite **recusado** desaparece do calendário de quem recusou, mas mantém-se no do organizador.
- 🎟️ Os eventos copiados de `/discover` entram como reuniões **confirmadas** com um único participante e aparecem logo no feed iCal.

---

<div align="center">

Feito por **Pedro Rosa** · junho de 2026

</div>
