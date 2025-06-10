# --------------------------------------------------------------
# 🚀 DEVELOPMENT DOCKERFILE
# --------------------------------------------------------------
FROM node:18-alpine

WORKDIR /app

# Copy dependency files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Create a non-root user
RUN addgroup -g 1001 -S nodejs && \
    adduser -S vueuser -u 1001 -G nodejs && \
    chown -R vueuser:nodejs /app

USER vueuser

EXPOSE 5173

# Development mode with hot reload
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]