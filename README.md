# PUB-SUB-ALERT — Deployment Guide

## Deploy to Railway (5 minutes)

### Step 1 — Push to GitHub
1. Create a new repo on github.com (call it `pub-sub-alert`)
2. Open PowerShell in this folder and run:
```
git init
git add .
git commit -m "initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/pub-sub-alert.git
git push -u origin main
```

### Step 2 — Deploy on Railway
1. Go to https://railway.app and sign in with GitHub
2. Click **New Project → Deploy from GitHub repo**
3. Select your `pub-sub-alert` repo
4. Railway auto-detects the Dockerfile and builds it
5. Click **Settings → Networking → Generate Domain**
6. Your app is live at `https://pub-sub-alert-xxxx.up.railway.app`

### Step 3 — Use it
- Open the Railway URL in your browser → dashboard loads
- Open multiple tabs → each tab is a separate subscriber
- One tab as Publisher → type alerts → all other tabs receive instantly

## Run Locally
```
# Option A: Docker
docker build -t pubsub .
docker run -p 8080:8080 pubsub
# Open: http://localhost:8080

# Option B: Maven
mvn package
java -jar target/pub-sub-alert-1.0.jar
# Open: http://localhost:8080
```
