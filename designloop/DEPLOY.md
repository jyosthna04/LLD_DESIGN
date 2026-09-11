# Deploying DesignLoop to Google Cloud

This deploys your app to **Cloud Run** (runs the Spring Boot app in a
container, scales to zero when idle, cheap for a small project) with
**Cloud SQL** (managed MySQL) as the database.

## Prerequisites

1. A Google Cloud account with billing enabled (new accounts get free
   trial credit).
2. [Install the gcloud CLI](https://cloud.google.com/sdk/docs/install)
   on your machine.
3. Docker installed (Cloud Run can also build for you without Docker
   installed locally, using Cloud Build - see Step 4 alternative).

## Step 1 — Create a Google Cloud project

```bash
gcloud auth login
gcloud projects create designloop-app --name="DesignLoop"
gcloud config set project designloop-app
gcloud services enable run.googleapis.com sqladmin.googleapis.com \
    artifactregistry.googleapis.com cloudbuild.googleapis.com
```

(If you already have a project, skip creating one and just
`gcloud config set project YOUR_PROJECT_ID`.)

## Step 2 — Create a Cloud SQL (MySQL) instance

```bash
gcloud sql instances create designloop-db \
    --database-version=MYSQL_8_0 \
    --tier=db-f1-micro \
    --region=us-central1 \
    --root-password=CHOOSE_A_STRONG_PASSWORD

gcloud sql databases create designloop --instance=designloop-db
```

`db-f1-micro` is the cheapest tier - fine for a practice project.
Note your instance connection name for later:

```bash
gcloud sql instances describe designloop-db --format="value(connectionName)"
# looks like: designloop-app:us-central1:designloop-db
```

## Step 3 — Store secrets in Secret Manager

Don't put your DB password or Groq API key directly in deploy
commands where they'd sit in shell history. Store them properly:

```bash
gcloud services enable secretmanager.googleapis.com

echo -n "CHOOSE_A_STRONG_PASSWORD" | gcloud secrets create db-password --data-file=-
echo -n "gsk_your_real_groq_key" | gcloud secrets create groq-api-key --data-file=-
```

## Step 4 — Build and deploy to Cloud Run

From inside your `designloop` project folder (where the Dockerfile
and pom.xml live):

```bash
gcloud run deploy designloop \
    --source . \
    --region=us-central1 \
    --allow-unauthenticated \
    --add-cloudsql-instances=designloop-app:us-central1:designloop-db \
    --set-env-vars="SPRING_DATASOURCE_URL=jdbc:mysql:///designloop?cloudSqlInstance=designloop-app:us-central1:designloop-db&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false" \
    --set-env-vars="SPRING_DATASOURCE_USERNAME=root" \
    --set-secrets="SPRING_DATASOURCE_PASSWORD=db-password:latest" \
    --set-secrets="GROQ_API_KEY=groq-api-key:latest"
```

`--source .` tells Cloud Run to build your Dockerfile using Cloud
Build automatically - you don't need Docker installed locally for
this to work.

Note: the JDBC URL above uses the **Cloud SQL Socket Factory**, which
needs one extra dependency in `pom.xml` (see Step 5) rather than a
plain TCP connection - this is Google's recommended, more secure way
for Cloud Run to reach Cloud SQL without exposing a public IP.

## Step 5 — Add the Cloud SQL connector dependency

Add this to your `pom.xml`, inside `<dependencies>`:

```xml
<dependency>
    <groupId>com.google.cloud.sql</groupId>
    <artifactId>mysql-socket-factory-connector-j-8</artifactId>
    <version>1.19.1</version>
</dependency>
```

(Check [Maven Central](https://mvnrepository.com/artifact/com.google.cloud.sql/mysql-socket-factory-connector-j-8)
for the latest version number before deploying.)

## Step 6 — Verify

```bash
gcloud run services describe designloop --region=us-central1 --format="value(status.url)"
```

Open the printed URL in your browser - that's your live app.

## Notes / gotchas

- **Cost**: Cloud Run's free tier covers light traffic; `db-f1-micro`
  Cloud SQL costs roughly $8-10/month running 24/7 (it doesn't scale
  to zero like Cloud Run does). To avoid ongoing cost after you're
  done testing, delete the instance: `gcloud sql instances delete designloop-db`.
- **Redeploying after code changes**: re-run the same `gcloud run
  deploy` command from Step 4 - it rebuilds and replaces the running
  version.
- **Logs**: `gcloud run services logs read designloop --region=us-central1`
  if something isn't starting correctly.
- **First request may be slow**: Cloud Run "cold starts" containers
  after idle periods - a startup delay of a few seconds is normal on
  a free-tier project.
