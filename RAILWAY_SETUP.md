# 🚂 Railway Deployment Setup Guide

## Current Issue
Backend services are crashing because Railway isn't using the correct Dockerfile configuration.

## Solution: Configure Each Service in Railway

### Step 1: Railway Dashboard Setup

For **EACH** backend service (auth-service, order-service, inventory-service, notification-service, recommendation-service):

1. **Go to Railway Dashboard**
2. **Select the service** (e.g., `orderly-inventory`)
3. **Go to Settings → Docker**
4. **Configure:**
   - **Dockerfile Path:** `Dockerfile.services`
   - **Docker Build Target:** `inventory-service` (use the service name)
   - **Docker Context:** `.` (project root)
5. **Save**

### Step 2: Service Names to Use

| Service | Build Target |
|---------|--------------|
| auth-service | `auth-service` |
| order-service | `order-service` |
| inventory-service | `inventory-service` |
| notification-service | `notification-service` |
| recommendation-service | `recommendation-service` |

### Step 3: Environment Variables

For each service, add these environment variables in Railway:

**Common for all services:**
- `SPRING_DATA_MONGODB_URI`: Your MongoDB connection string
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka connection (if using Railway's Kafka)

**Service-specific:**
- **auth-service:** `JWT_SECRET` (generate a random secret)
- **order-service:** `SPRING_DATA_REDIS_HOST`: Redis host
- **recommendation-service:** `SPRING_DATA_REDIS_HOST`: Redis host

### Step 4: Redeploy

After configuring each service:
1. Go to **Deploy** tab
2. Click **Redeploy**
3. Wait for build to complete
4. Check logs for errors

---

## Alternative: Use Railway CLI

```bash
# Install Railway CLI
npm i -g @railway/cli

# Login
railway login

# Link project
railway link

# For each service, set Dockerfile and target
railway service inventory-service --dockerfile Dockerfile.services --target inventory-service
railway service order-service --dockerfile Dockerfile.services --target order-service
railway service auth-service --dockerfile Dockerfile.services --target auth-service
railway service notification-service --dockerfile Dockerfile.services --target notification-service
railway service recommendation-service --dockerfile Dockerfile.services --target recommendation-service
```

---

## Quick Fix Checklist

- [ ] All services configured with `Dockerfile.services`
- [ ] Correct build target set for each service
- [ ] Environment variables configured
- [ ] All services redeployed
- [ ] Check logs - no JAR file errors
- [ ] Frontend can reach backend services

---

## Troubleshooting

**If services still crash:**
1. Check build logs - verify JAR files are created
2. Check deploy logs - verify JAR file is found
3. Verify Dockerfile.services is being used
4. Verify build target matches service name

**If 502 Bad Gateway:**
1. Verify backend services are running (not crashed)
2. Check Railway service names match nginx config
3. Verify services are in same Railway project (for private networking)

---

**After fixing Railway configuration, push changes and redeploy!**
