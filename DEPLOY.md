# Deployment (free tier)

Architecture: **Vercel** (React) → **Render** (Spring Boot, Docker) → **Aiven** (MySQL).

Deploy in this order — each step produces a value the next one needs.

## 1. Database — Aiven MySQL

You can reuse your existing Aiven MySQL service. The booking app needs its **own
database** (it has a `users` table that would clash with other apps), so:

1. In the Aiven console → your MySQL service → **Databases** → create a database
   named `booking`.
2. Note your connection details: host, port, user (`avnadmin`), password.

Your JDBC URL will be:
`jdbc:mysql://<host>:<port>/booking?sslMode=REQUIRED`

## 2. Backend — Render (Docker)

1. render.com → **New → Web Service** → connect the `booking-api` repo.
2. Render auto-detects the **Dockerfile**. Instance type: **Free**.
3. Environment variables:

   | Key | Value |
   |-----|-------|
   | `DB_URL` | `jdbc:mysql://<host>:<port>/booking?sslMode=REQUIRED` |
   | `DB_DRIVER` | `com.mysql.cj.jdbc.Driver` |
   | `DB_USERNAME` | `avnadmin` |
   | `DB_PASSWORD` | (your Aiven password) |
   | `JWT_SECRET` | a long random string (40+ chars) |
   | `CORS_ORIGINS` | (set after step 3 — the Vercel URL) |

   (Don't set `PORT` — Render injects it; the app reads it automatically.)
4. Deploy. Note the service URL, e.g. `https://booking-api.onrender.com`.
   Check `https://<render-url>/api/health` returns `{"status":"ok"}`.

## 3. Frontend — Vercel

1. vercel.com → **Add New → Project** → import `booking-web`. Auto-detects Vite.
2. Environment variable:

   | Key | Value |
   |-----|-------|
   | `VITE_API_URL` | `https://<your-render-url>` |

   (No path — just the base URL.)
3. Deploy. Note the Vercel URL.

## 4. Close the loop

On Render, set `CORS_ORIGINS` to your Vercel URL (e.g.
`https://booking-web.vercel.app`) and redeploy the backend.

Open the Vercel URL → register → add availability → book. Done.

## Notes

- Render's free service **sleeps after ~15 min idle**; first request takes
  ~30–50s to wake. The Docker build also makes the first deploy take a few minutes.
- `ddl-auto=update` auto-creates the tables in the `booking` database on first boot.
