# 🆓 Oracle Cloud Free Tier Deployment Guide

Complete step-by-step guide to deploy Orderly on Oracle Cloud's **Always Free** tier.

## 📋 What You Get (Forever Free!)

- **4 ARM-based VMs** (Ampere A1)
- **24GB total RAM** (split across VMs)
- **200GB block storage**
- **10TB outbound data transfer/month**
- **Always on** - No sleeping, no time limits!

---

## 🎯 Step 1: Create Oracle Cloud Account

### 1.1 Sign Up
1. Go to [cloud.oracle.com](https://cloud.oracle.com)
2. Click **"Start for Free"**
3. Fill in your details:
   - Email: `ks517633@gmail.com`
   - Name: `Kuldeep Singh`
   - Country: Select your country
   - Cloud Account Name: `orderly-cloud` (or any name you prefer)

### 1.2 Verify Email
- Check your email and click the verification link
- You'll be redirected to Oracle Cloud Console

### 1.3 Add Payment Method (Required but Won't Charge)
- Oracle requires a credit card for verification
- **You won't be charged** if you stay within free tier limits
- They may place a $1 hold (released immediately)

### 1.4 Wait for Account Activation
- Usually takes 5-15 minutes
- You'll receive an email when ready

---

## 🖥️ Step 2: Create Your First VM Instance

### 2.1 Navigate to Compute
1. Log in to Oracle Cloud Console
2. Click **☰ Menu** (top left)
3. Go to **Compute** → **Instances**

### 2.2 Create Instance
1. Click **"Create Instance"**

### 2.3 Configure Instance Details

#### **Name & Placement**
- **Name**: `orderly-main-server`
- **Placement**: Keep default (or select your preferred region)
- **Image**: Click **"Change Image"**
  - Select **"Canonical Ubuntu"**
  - Version: **22.04** (or latest LTS)
  - Architecture: **ARM64 (Ampere)**

#### **Shape** (This is Important!)
- Click **"Change Shape"**
- Select **"VM.Standard.A1.Flex"** (ARM-based)
- Configure resources:
  - **OCPUs**: `4` (use all free allocation)
  - **Memory**: `24 GB` (use all free allocation)
- Click **"Select Shape"**

#### **Networking**
- **Virtual Cloud Network**: Create new VCN (or use existing)
- **Subnet**: Create new public subnet
- **Public IP**: ✅ **Assign a public IPv4 address** (IMPORTANT!)
- **Security List**: Default (we'll configure later)

#### **Add SSH Keys**
- **SSH Key Source**: Choose one:
  - **Option A**: Generate new key pair (Oracle will give you private key - **SAVE IT!**)
  - **Option B**: Upload your existing SSH public key

**If you don't have SSH keys, generate them locally:**
```bash
# On your Mac
ssh-keygen -t rsa -b 4096 -C "ks517633@gmail.com" -f ~/.ssh/oracle_cloud
# This creates:
# ~/.ssh/oracle_cloud (private key)
# ~/.ssh/oracle_cloud.pub (public key)

# Copy public key content
cat ~/.ssh/oracle_cloud.pub
# Copy the entire output and paste in Oracle Cloud
```

#### **Boot Volume**
- Keep default (50GB is fine, you get 200GB free total)

### 2.4 Create Instance
- Click **"Create"**
- Wait 2-5 minutes for instance to provision

---

## 🔐 Step 3: Configure Security (Firewall Rules)

### 3.1 Open Required Ports
1. In Oracle Cloud Console, go to **Networking** → **Virtual Cloud Networks**
2. Click on your VCN
3. Click **"Security Lists"**
4. Click **"Default Security List"**
5. Click **"Add Ingress Rules"**

**Add these rules one by one:**

| Source Type | Source CIDR | IP Protocol | Destination Port Range | Description |
|------------|-------------|-------------|----------------------|-------------|
| CIDR | 0.0.0.0/0 | TCP | 22 | SSH access |
| CIDR | 0.0.0.0/0 | TCP | 80 | HTTP (Frontend) |
| CIDR | 0.0.0.0/0 | TCP | 443 | HTTPS (if using SSL) |
| CIDR | 0.0.0.0/0 | TCP | 8081 | Order Service |
| CIDR | 0.0.0.0/0 | TCP | 8082 | Inventory Service |
| CIDR | 0.0.0.0/0 | TCP | 8083 | Notification Service |
| CIDR | 0.0.0.0/0 | TCP | 8084 | Recommendation Service |
| CIDR | 0.0.0.0/0 | TCP | 8085 | Auth Service |

**Note**: For production, restrict source CIDR to your IP only!

---

## 🚀 Step 4: Connect to Your VM

### 4.1 Get Your Public IP
1. Go to **Compute** → **Instances**
2. Find your instance
3. Copy the **Public IP address** (e.g., `123.45.67.89`)

### 4.2 SSH into VM
```bash
# If you used Oracle-generated key:
ssh -i /path/to/private-key ubuntu@YOUR_PUBLIC_IP

# If you uploaded your own key:
ssh -i ~/.ssh/oracle_cloud ubuntu@YOUR_PUBLIC_IP

# Example:
ssh -i ~/.ssh/oracle_cloud ubuntu@123.45.67.89
```

**First time connection:**
- Type `yes` when asked about host authenticity
- You should see: `Welcome to Ubuntu 22.04...`

---

## 🛠️ Step 5: Install Docker & Docker Compose

### 5.1 Update System
```bash
sudo apt update && sudo apt upgrade -y
```

### 5.2 Install Docker
```bash
# Install prerequisites
sudo apt install -y ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Verify installation
docker --version
# Should show: Docker version 24.x.x or similar
```

### 5.3 Add User to Docker Group
```bash
sudo usermod -aG docker $USER
# Log out and back in for changes to take effect
exit
```

**Reconnect:**
```bash
ssh -i ~/.ssh/oracle_cloud ubuntu@YOUR_PUBLIC_IP
```

**Verify Docker works without sudo:**
```bash
docker ps
# Should show empty list (no errors)
```

### 5.4 Install Docker Compose (if not included)
```bash
# Docker Compose V2 is included with docker-compose-plugin
# Verify:
docker compose version
# Should show: Docker Compose version v2.x.x
```

---

## 📦 Step 6: Clone Your Repository

### 6.1 Install Git
```bash
sudo apt install -y git
```

### 6.2 Clone Repository
```bash
# Clone your GitHub repo
git clone https://github.com/KuldeepSingh3011/orderly.git
cd orderly

# Verify files
ls -la
```

---

## 🔧 Step 7: Configure Environment Variables

### 7.1 Generate JWT Secret
```bash
# Generate a strong random secret
openssl rand -base64 32
# Copy the output (you'll need it)
```

### 7.2 Create Environment File
```bash
# Create .env file for production
nano .env
```

**Add this content (replace YOUR_JWT_SECRET with the output from above):**
```bash
JWT_SECRET=YOUR_JWT_SECRET_HERE
JWT_EXPIRATION_MS=3600000
SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/orderly
SPRING_DATA_REDIS_HOST=redis
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
SPRING_ELASTICSEARCH_ENABLED=false
```

**Save and exit:**
- Press `Ctrl + X`
- Press `Y` to confirm
- Press `Enter` to save

---

## 🐳 Step 8: Build and Deploy

### 8.1 Build Docker Images
```bash
# Make sure you're in the orderly directory
cd ~/orderly

# Build all services (this will take 10-15 minutes)
docker compose -f docker-compose.prod.yml build

# This builds:
# - All Java microservices
# - Frontend (React + Nginx)
# - Infrastructure (MongoDB, Redis, Kafka, Zookeeper)
```

**Note**: First build takes longer as it downloads base images.

### 8.2 Start All Services
```bash
# Start everything
docker compose -f docker-compose.prod.yml up -d

# Check status
docker compose -f docker-compose.prod.yml ps
```

**Expected output:**
```
NAME                        STATUS          PORTS
orderly-auth-service        Up              8085/tcp
orderly-frontend            Up              0.0.0.0:80->80/tcp
orderly-inventory-service   Up              8082/tcp
orderly-kafka               Up              9092/tcp
orderly-mongodb             Up (healthy)    27017/tcp
orderly-notification-service Up            8083/tcp
orderly-order-service       Up              8081/tcp
orderly-redis               Up (healthy)    6379/tcp
orderly-recommendation-service Up          8084/tcp
orderly-zookeeper           Up              2181/tcp
```

### 8.3 Check Logs
```bash
# View all logs
docker compose -f docker-compose.prod.yml logs -f

# View specific service logs
docker compose -f docker-compose.prod.yml logs -f auth-service
docker compose -f docker-compose.prod.yml logs -f frontend
```

**Wait 1-2 minutes** for all services to start completely.

---

## ✅ Step 9: Verify Deployment

### 9.1 Check Service Health
```bash
# Test from inside the VM
curl http://localhost:8085/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # Inventory Service
curl http://localhost:8081/actuator/health  # Order Service
```

### 9.2 Access Your Application
Open your browser and go to:
```
http://YOUR_PUBLIC_IP
```

**Example:**
```
http://123.45.67.89
```

You should see the Orderly login page! 🎉

---

## 🔒 Step 10: Set Up Domain (Optional but Recommended)

### 10.1 Get a Free Domain
- **Freenom**: [freenom.com](https://freenom.com) - Free `.tk`, `.ml`, `.ga` domains
- **Cloudflare**: Free DNS hosting (even without domain)

### 10.2 Point Domain to Your IP
1. Get your Oracle Cloud public IP
2. In your domain registrar, add **A Record**:
   - **Name**: `@` (or `www`)
   - **Type**: `A`
   - **Value**: `YOUR_PUBLIC_IP`
   - **TTL**: `3600`

### 10.3 Update Nginx Config (if using domain)
```bash
# Edit frontend nginx config
nano ~/orderly/frontend/nginx.conf
```

Update `server_name` to your domain, then rebuild:
```bash
cd ~/orderly
docker compose -f docker-compose.prod.yml up -d --build frontend
```

---

## 🔄 Step 11: Set Up Auto-Restart (Systemd)

### 11.1 Create Systemd Service
```bash
sudo nano /etc/systemd/system/orderly.service
```

**Add this content:**
```ini
[Unit]
Description=Orderly Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ubuntu/orderly
ExecStart=/usr/bin/docker compose -f docker-compose.prod.yml up -d
ExecStop=/usr/bin/docker compose -f docker-compose.prod.yml down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
```

### 11.2 Enable Service
```bash
sudo systemctl daemon-reload
sudo systemctl enable orderly.service
sudo systemctl start orderly.service

# Check status
sudo systemctl status orderly.service
```

**Now your app will auto-start on VM reboot!**

---

## 📊 Step 12: Monitoring & Maintenance

### 12.1 Check Resource Usage
```bash
# CPU and Memory
htop
# (Install with: sudo apt install htop)

# Disk usage
df -h

# Docker stats
docker stats
```

### 12.2 View Logs
```bash
# All services
docker compose -f docker-compose.prod.yml logs --tail=100

# Specific service
docker compose -f docker-compose.prod.yml logs --tail=100 auth-service

# Follow logs in real-time
docker compose -f docker-compose.prod.yml logs -f
```

### 12.3 Restart Services
```bash
# Restart all
docker compose -f docker-compose.prod.yml restart

# Restart specific service
docker compose -f docker-compose.prod.yml restart auth-service
```

### 12.4 Update Application
```bash
cd ~/orderly

# Pull latest changes
git pull

# Rebuild and restart
docker compose -f docker-compose.prod.yml up -d --build

# Or rebuild specific service
docker compose -f docker-compose.prod.yml up -d --build auth-service
```

---

## 🐛 Troubleshooting

### Problem: Can't SSH into VM
**Solution:**
- Check Security List rules (port 22)
- Verify public IP is assigned
- Check instance is running

### Problem: Services won't start
**Solution:**
```bash
# Check logs
docker compose -f docker-compose.prod.yml logs

# Check if ports are available
sudo netstat -tulpn | grep LISTEN

# Restart Docker
sudo systemctl restart docker
```

### Problem: Out of memory
**Solution:**
```bash
# Check memory usage
free -h

# Reduce resources in docker-compose.prod.yml
# Or use fewer services initially
```

### Problem: Can't access frontend
**Solution:**
```bash
# Check if frontend is running
docker ps | grep frontend

# Check frontend logs
docker logs orderly-frontend

# Verify port 80 is open in Security List
```

### Problem: Database connection errors
**Solution:**
```bash
# Check MongoDB is healthy
docker ps | grep mongodb

# Check MongoDB logs
docker logs orderly-mongodb

# Test connection
docker exec -it orderly-mongodb mongosh
```

---

## 💰 Cost Monitoring

### Check Your Usage
1. Go to Oracle Cloud Console
2. **☰ Menu** → **Billing & Cost Management** → **Cost Analysis**
3. Monitor to ensure you stay within free tier

**Free Tier Limits:**
- ✅ 4 ARM VMs (Ampere A1)
- ✅ 24GB RAM total
- ✅ 200GB block storage
- ✅ 10TB outbound data/month
- ❌ Exceeding these = charges

---

## 🎯 Quick Reference Commands

```bash
# Start all services
cd ~/orderly && docker compose -f docker-compose.prod.yml up -d

# Stop all services
docker compose -f docker-compose.prod.yml down

# View logs
docker compose -f docker-compose.prod.yml logs -f

# Restart service
docker compose -f docker-compose.prod.yml restart SERVICE_NAME

# Rebuild and restart
docker compose -f docker-compose.prod.yml up -d --build

# Check status
docker compose -f docker-compose.prod.yml ps

# Access MongoDB shell
docker exec -it orderly-mongodb mongosh

# Access Redis CLI
docker exec -it orderly-redis redis-cli

# View resource usage
docker stats
```

---

## 🎉 Success Checklist

- [ ] Oracle Cloud account created
- [ ] VM instance created (4 OCPU, 24GB RAM)
- [ ] Security List rules configured (ports 22, 80, 8081-8085)
- [ ] SSH connection working
- [ ] Docker installed and working
- [ ] Repository cloned
- [ ] Environment variables configured
- [ ] All services built successfully
- [ ] All services running (docker ps shows all UP)
- [ ] Frontend accessible at http://YOUR_PUBLIC_IP
- [ ] Can login and use application
- [ ] Systemd service configured for auto-start

---

## 📞 Need Help?

If you encounter issues:
1. Check logs: `docker compose -f docker-compose.prod.yml logs`
2. Verify all services are running: `docker ps`
3. Check resource usage: `docker stats`
4. Review Oracle Cloud console for VM status

---

**🎊 Congratulations! Your Orderly application is now live on Oracle Cloud Free Tier!**

Your app URL: `http://YOUR_PUBLIC_IP`
