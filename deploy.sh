#!/bin/bash

# Orderly Deployment Script for Oracle Cloud
# Run this script on your Oracle Cloud VM after SSH connection

set -e  # Exit on error

echo "🚀 Starting Orderly Deployment..."
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Update system
echo -e "${BLUE}📦 Step 1: Updating system packages...${NC}"
sudo apt update && sudo apt upgrade -y

# Step 2: Install prerequisites
echo -e "${BLUE}📦 Step 2: Installing prerequisites...${NC}"
sudo apt install -y ca-certificates curl gnupg lsb-release git

# Step 3: Install Docker
echo -e "${BLUE}🐳 Step 3: Installing Docker...${NC}"
if ! command -v docker &> /dev/null; then
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
    
    # Add user to docker group
    sudo usermod -aG docker $USER
    
    echo -e "${GREEN}✅ Docker installed successfully!${NC}"
    echo -e "${YELLOW}⚠️  You need to log out and back in for Docker group changes to take effect.${NC}"
    echo -e "${YELLOW}   Or run: newgrp docker${NC}"
else
    echo -e "${GREEN}✅ Docker already installed${NC}"
fi

# Step 4: Verify Docker installation
echo -e "${BLUE}🔍 Step 4: Verifying Docker installation...${NC}"
docker --version
docker compose version

# Step 5: Clone repository
echo -e "${BLUE}📥 Step 5: Cloning repository...${NC}"
if [ ! -d "orderly" ]; then
    git clone https://github.com/KuldeepSingh3011/orderly.git
    cd orderly
else
    echo -e "${YELLOW}⚠️  Repository already exists. Updating...${NC}"
    cd orderly
    git pull
fi

# Step 6: Generate JWT Secret
echo -e "${BLUE}🔐 Step 6: Generating JWT secret...${NC}"
JWT_SECRET=$(openssl rand -base64 32)
echo -e "${GREEN}✅ JWT Secret generated: ${JWT_SECRET}${NC}"

# Step 7: Create .env file
echo -e "${BLUE}⚙️  Step 7: Creating environment configuration...${NC}"
cat > .env << EOF
JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION_MS=3600000
SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/orderly
SPRING_DATA_REDIS_HOST=redis
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
SPRING_ELASTICSEARCH_ENABLED=false
EOF

echo -e "${GREEN}✅ Environment file created${NC}"

# Step 8: Build Docker images
echo -e "${BLUE}🔨 Step 8: Building Docker images (this will take 10-15 minutes)...${NC}"
docker compose -f docker-compose.prod.yml build

# Step 9: Start all services
echo -e "${BLUE}🚀 Step 9: Starting all services...${NC}"
docker compose -f docker-compose.prod.yml up -d

# Step 10: Wait for services to start
echo -e "${BLUE}⏳ Step 10: Waiting for services to start (30 seconds)...${NC}"
sleep 30

# Step 11: Check service status
echo -e "${BLUE}📊 Step 11: Checking service status...${NC}"
docker compose -f docker-compose.prod.yml ps

# Step 12: Show logs
echo -e "${BLUE}📋 Step 12: Recent logs...${NC}"
docker compose -f docker-compose.prod.yml logs --tail=20

echo ""
echo -e "${GREEN}🎉 Deployment complete!${NC}"
echo ""
echo -e "${YELLOW}📝 Next steps:${NC}"
echo -e "   1. Get your VM's public IP from Oracle Cloud Console"
echo -e "   2. Access your application at: http://YOUR_PUBLIC_IP"
echo -e "   3. Check logs: docker compose -f docker-compose.prod.yml logs -f"
echo -e "   4. Check status: docker compose -f docker-compose.prod.yml ps"
echo ""
