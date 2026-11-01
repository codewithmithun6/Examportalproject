# Exam Portal

An online examination portal with an Angular frontend and a Spring Boot REST API. Learners can register and verify email, take quizzes, see and export results, and review their attempt history. Administrators can manage categories, quizzes, questions, and review all learner results.

## Contents

1. [Project layout](#project-layout)
2. [Requirements](#requirements)
3. [Configure the database and email](#configure-the-database-and-email)
4. [Run locally](#run-locally)
5. [Learner workflow](#learner-workflow)
6. [Administrator workflow](#administrator-workflow)
7. [Question import format](#question-import-format)
8. [Main routes and APIs](#main-routes-and-apis)
9. [Data and scoring](#data-and-scoring)
10. [Build and test](#build-and-test)
11. [Operational and production notes](#operational-and-production-notes)
12. [Database ER diagram](#database-er-diagram)

## Project layout

```text
examfront/   Angular 16 application and UI
examServer/  Spring Boot 2.7 REST API, Spring Security, JPA, and MySQL
```

The frontend calls the backend at `http://localhost:5151` by default. Change the URL in `examfront/src/app/services/helper.ts` when using a different backend host.

## Requirements

- Java 17
- Node.js and npm compatible with Angular 16
- MySQL
- Maven (the backend Maven wrapper is included)
- SMTP credentials for email verification and password recovery

## Configure the database and email

### MySQL

The checked-in local development settings in `examServer/src/main/resources/application.properties` use:

```text
Database: exam
Host: localhost:3306
Username: root
Password: admin
```

Create the `exam` database and configure a suitable local MySQL account before starting the backend. These values are development defaults; do not use them for a deployed environment. Override credentials outside source control for shared or production environments.

JPA is currently configured with `spring.jpa.hibernate.ddl-auto=update`; Hibernate updates the schema when the backend starts. Use managed schema migrations and a deliberate production policy before deployment.

### SMTP

Email is required for registration verification and forgotten-password recovery. For Gmail, use an app password, not the normal account password. In the PowerShell window that will start Spring Boot, set:

```powershell
$env:MAIL_USERNAME = "sender@example.com"
$env:MAIL_APP_PASSWORD = "your-app-password"
```

Optional overrides:

```powershell
$env:MAIL_HOST = "smtp.gmail.com"
$env:MAIL_PORT = "587"
```

Defaults are `smtp.gmail.com` and port `587`. Do not commit credentials or paste them into source files. Restart the backend after changing environment variables.

## Run locally

### 1. Start the backend

Open PowerShell at the repository root:

```powershell
cd examServer
.\mvnw.cmd spring-boot:run
```

The API listens on port `5151` unless `server.port` is changed. The backend needs a running MySQL server and a reachable SMTP server for email flows.

### 2. Start the frontend

Open another terminal at the repository root:

```powershell
cd examfront
npm install
npm start
```

Open the Angular CLI URL, normally `http://localhost:4200`.

### 3. Stop the applications

Use `Ctrl+C` in each terminal. Keep the environment variables set in the backend shell when restarting it.

## Learner workflow

### Register and sign in

1. Open the public home page and select **Create an account** or **Register**.
2. Enter username, password, first and last name, email, and phone.
3. Select **Send Verification Code**.
4. Enter the six-digit code delivered to the supplied email and select **Verify and Register**.
5. Return to Login. Sign in with either the username or registered email address and password.
6. Learners are directed to `/user-dashboard/0`, the all-quizzes view.

Registration creates a `NORMAL` learner account only after email verification. Public direct account creation is disabled. The verification code expires after 10 minutes; pending signup details are held in memory, so a backend restart clears pending verifications.

### Find and take a quiz

1. Choose **All Quizzes** or a category from the learner sidebar.
2. Review the quiz title, subject/category, description, question count, and maximum marks.
3. Select **Start**, read the instructions, then confirm the start prompt.
4. Answer each question using the radio options. The quiz timer is based on the number of questions.
5. Select **Submit Quiz** and confirm, or let the timer reach zero for automatic submission.
6. Review the result: learner name and email, subject, quiz, marks earned, correct answers, attempted and unattempted counts, wrong answers, percentage, and attempt reference.
7. Select **Print / Save as PDF** to open the browser print dialog and choose **Save as PDF**.

Avoid refreshing or navigating away while answering. The application does not currently promise that in-progress answers are saved between page loads.

### Review previous results

Open **Quiz History** from the learner sidebar. Each completed attempt includes subject, quiz, completion time, marks, counts, and percentage. Use **Export** on a row to print or save that saved attempt as a PDF later.

### Profile and password

- **Profile**: view account details, then use **Update** to change first name, last name, email, mobile number, or profile picture. Picture formats are JPG, PNG, GIF, or WEBP, up to 5 MB.
- **Share**: share profile details through the browser share sheet or copy them when browser sharing is unavailable.
- **Settings**: change the signed-in account password by entering the old password, new password, and confirmation.
- **Forgot password**: from Login, request an email code, verify it, and set a new password. This recovery flow works for learner and admin accounts.

## Administrator workflow

Administrator routes are protected by the admin route guard and backend authority checks. After an administrator signs in, the dashboard opens at `/admin`.

### Organize content

1. Use **Categories** to review existing subjects/categories.
2. Use **Add Category** to create a category with a title and description.
3. Use **Add Quiz** to enter a title, description, maximum marks, question count, active status, and category.
4. Use **Quizzes** to review quizzes. The search field automatically searches quiz title, description, and category as text is entered. Clear the query to restore the full list.
5. From a quiz card, use **Questions** to review question content, **update** to edit the quiz, **Attempts** to open all user test results, and **Delete** only when removal is intended.
6. Add questions one at a time using the manual question editor, or import a batch using the upload panel. See [Question import format](#question-import-format).

### Review results

Open **All user test results** in the admin sidebar or select **Attempts** on a quiz card. The admin view lists saved attempts with learner ID, name, username, email, subject, quiz, total and earned marks, total/correct/attempted/unattempted/wrong counts, percentage, completion time, and attempt ID.

## Question import format

The admin **Add Question** screen retains manual entry and also accepts `.xlsx`, `.csv`, and tab-delimited `.txt` files. XLSX reads the first worksheet. CSV/TXT reads the first row as headers. Use these six columns, in this order:

```csv
Question,Option A,Option B,Option C,Option D,Correct Answer
What is 2 + 2?,3,4,5,6,B
```

Rules:

- `Correct Answer` is a letter: `A`, `B`, `C`, or `D`.
- Each question and all four options are required.
- Upload size is limited to 5 MB and each import is limited to 500 questions.
- The backend validates all rows before saving, so a validation error prevents partial imports.
- The upload endpoint is restricted to administrators.

Example files for practice are included in the repository root: `python-programming-questions.csv`, `microservices-questions.csv`, `sports-questions.csv`, and `mongodb-questions.csv`.

## Main routes and APIs

### Frontend routes

| Route | Access | Purpose |
|---|---|---|
| `/` | Public | Home |
| `/signup` | Public | Email-verified learner registration |
| `/login` | Public | Sign in with username or email |
| `/forget-password` | Public | Begin password recovery |
| `/verify-otp` | Public | Verify recovery code |
| `/reset-password` | Public, requires recovery token | Set a replacement password |
| `/admin` | Admin | Admin dashboard |
| `/admin/quizzes` | Admin | Manage and search quizzes |
| `/admin/attempts` | Admin | Review all learner attempts |
| `/user-dashboard/0` | Learner | Browse all active quizzes |
| `/user-dashboard/attempts` | Learner | Review own saved attempts |
| `/start/:qid` | Learner | Take a quiz |

### Backend API groups

| API | Purpose |
|---|---|
| `POST /generate-token` | Authenticate and issue a JWT |
| `GET /current-user` | Return the authenticated account |
| `POST /user/registration/send-otp` | Begin email-verified registration |
| `POST /user/registration/verify-otp` | Verify registration code and create account |
| `POST /user/password-recovery/send-otp` | Send a password recovery code |
| `POST /user/password-recovery/verify-otp` | Verify recovery code and issue temporary reset token |
| `POST /user/password-recovery/reset` | Set a new password |
| `GET /quiz/search?query=...` | Admin search by title, description, or category |
| `POST /question/` | Add one question manually |
| `POST /question/import/{quizId}` | Admin bulk-import questions |
| `POST /question/eval-quiz` | Evaluate a submitted quiz and save its attempt |
| `GET /question/attempts` | Get the authenticated learner's attempt history |
| `GET /question/attempts/all` | Admin-only attempt history |

Authenticated API requests use the JWT bearer token attached by the Angular HTTP interceptor.

## Data and scoring

A successful quiz submission creates a database row in `quiz_attempt`. It snapshots learner identity, quiz title, subject, completion time, total questions/marks, earned marks, correct answers, attempted/unattempted questions, wrong answers, percentage, and a unique attempt ID. Snapshot fields keep historical results readable if a user or quiz is later renamed or removed.

Scoring is performed by the backend using the stored answer key. The frontend displays the result and history; it is not the authority for correct answers. The database schema currently uses JPA `ddl-auto=update`, which creates/updates tables at startup.

## Build and test

### Frontend

```powershell
cd examfront
npm run build
npm test
```

`npm test` launches Karma; a browser may be required by the local environment.

### Backend

```powershell
cd examServer
.\mvnw.cmd test
.\mvnw.cmd package
```

The application-context test requires the configured database. The production frontend build may report existing CommonJS and bundle-size budget warnings.

## Operational and production notes

- Replace the local MySQL credentials, sample administrator seed credentials, JWT signing secret, and SMTP settings before deployment. Never commit real secrets.
- The application currently seeds a development administrator in `ExamServerApplication`; review and replace that behavior with secure provisioning before production use.
- Registration OTP state and password-recovery tokens are stored in server memory. Restarting a single server clears pending flows. A multi-instance deployment needs a shared store such as Redis or a database.
- Use HTTPS in deployment. Restrict CORS to known frontend origins instead of `*`.
- Configure database backups and use migration tooling for production schema changes.
- Uploaded profile pictures are written to the configured `exam.upload-dir` (default `uploads`) and exposed through `/uploads/**`; plan persistent storage and backup for deployed environments.
- The upload importer currently accepts XLSX, CSV, and tab-delimited TXT. PDF question extraction is not implemented.

## Database ER diagram

See [docs/database-erd.md](docs/database-erd.md) for the JPA-derived ER diagram, entity fields, relationships, and notes about quiz-attempt snapshot IDs.
