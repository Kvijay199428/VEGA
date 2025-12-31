# Vega Trader's Frontend - Deployment Guide

## 🚀 Quick Deployment

### Method 1: Using the Build Script (Recommended)

1. **Run the build script**
   ```bash
   chmod +x build-frontend.sh
   ./build-frontend.sh
   ```

2. **Start the application**
   ```bash
   cd vega-traders-frontend
   npm start
   ```

### Method 2: Manual Setup

1. **Navigate to frontend directory**
   ```bash
   cd vega-traders-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start development server**
   ```bash
   npm start
   ```

4. **Build for production**
   ```bash
   npm run build
   ```

## 🐳 Docker Deployment

### Development with Docker
```bash
docker-compose up
```

### Production Docker Deployment
```bash
# Build the image
docker build -t vega-traders-frontend .

# Run the container
docker run -p 28020:28020 vega-traders-frontend
```

## ☁️ Cloud Deployment

### Vercel (Recommended)
1. Install Vercel CLI:
   ```bash
   npm i -g vercel
   ```

2. Deploy:
   ```bash
   cd vega-traders-frontend
   vercel --prod
   ```

### Netlify
1. Build the project:
   ```bash
   npm run build
   ```

2. Deploy to Netlify:
   ```bash
   npm install -g netlify-cli
   netlify deploy --prod --dir=build
   ```

### AWS Amplify
1. Connect your repository to AWS Amplify
2. Configure build settings:
   ```yaml
   version: 1
   frontend:
     phases:
       preBuild:
         commands:
           - npm ci
       build:
         commands:
           - npm run build
     artifacts:
       baseDirectory: build
       files:
         - '**/*'
     cache:
       paths:
         - node_modules/**/*
   ```

### GitHub Pages
1. Install gh-pages:
   ```bash
   npm install --save-dev gh-pages
   ```

2. Add to package.json:
   ```json
   {
     "homepage": "https://yourusername.github.io/vega-traders-frontend",
     "scripts": {
       "predeploy": "npm run build",
       "deploy": "gh-pages -d build"
     }
   }
   ```

3. Deploy:
   ```bash
   npm run deploy
   ```

## 🔧 Environment Configuration

### Production Environment Variables
Create a `.env.production` file:
```env
REACT_APP_API_URL=https://api.yourdomain.com/api/v1
REACT_APP_WS_URL=wss://api.yourdomain.com
REACT_APP_ENVIRONMENT=production
```

### Environment-Specific Builds
```bash
# Development
npm start

# Staging
REACT_APP_ENVIRONMENT=staging npm run build

# Production
REACT_APP_ENVIRONMENT=production npm run build
```

## 🔒 Security Considerations

### HTTPS Configuration
1. **Vercel/Netlify**: Automatic HTTPS
2. **AWS**: Use CloudFront or ALB
3. **Custom Server**: Configure SSL certificates

### Environment Variables
- Never commit `.env` files
- Use environment-specific variables
- Validate all environment variables

### Content Security Policy
Add to public/index.html:
```html
<meta http-equiv="Content-Security-Policy" content="default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';">
```

## 📊 Performance Optimization

### Bundle Analysis
```bash
npm install --save-dev webpack-bundle-analyzer
npm run build
npx webpack-bundle-analyzer build/static/js/*.js
```

### Code Splitting
The application uses React.lazy for code splitting:
```typescript
const Dashboard = lazy(() => import('./pages/Dashboard'));
```

### Image Optimization
- Use WebP format when possible
- Implement lazy loading
- Use responsive images

## 🚀 CI/CD Pipeline

### GitHub Actions Example
```yaml
name: Deploy Frontend

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '16'
        cache: 'npm'
    
    - name: Install dependencies
      run: npm ci
    
    - name: Run tests
      run: npm test -- --coverage --watchAll=false
    
    - name: Build
      run: npm run build
    
    - name: Deploy to Vercel
      uses: amondnet/vercel-action@v25
      with:
        vercel-token: ${{ secrets.VERCEL_TOKEN }}
        vercel-org-id: ${{ secrets.ORG_ID }}
        vercel-project-id: ${{ secrets.PROJECT_ID }}
        working-directory: ./vega-traders-frontend
```

## 📱 Mobile Optimization

### PWA Configuration
1. Add service worker:
   ```bash
   npm install -g workbox-cli
   workbox generateSW workbox-config.js
   ```

2. Update manifest.json with PWA settings

### Performance Metrics
- Lighthouse score: 90+
- First Contentful Paint: < 1.5s
- Time to Interactive: < 3.5s

## 🔍 Monitoring

### Error Tracking
```bash
npm install @sentry/react @sentry/tracing
```

### Analytics
```bash
npm install react-ga4
```

### Performance Monitoring
```bash
npm install web-vitals
```

## 🚨 Troubleshooting

### Common Issues

1. **Build fails with memory error**
   ```bash
   export NODE_OPTIONS="--max-old-space-size=4096"
   npm run build
   ```

2. **WebSocket connection fails**
   - Check environment variables
   - Verify backend WebSocket server
   - Check firewall settings

3. **API requests fail**
   - Verify API URL configuration
   - Check CORS settings
   - Verify authentication tokens

### Debug Mode
Enable debug logging:
```bash
REACT_APP_DEBUG=true npm start
```

## 🔄 Updates & Maintenance

### Dependency Updates
```bash
npm update
npm audit fix
```

### Security Patches
```bash
npm audit
npm audit fix
```

## 📋 Deployment Checklist

- [ ] Environment variables configured
- [ ] SSL certificate installed
- [ ] Build completed successfully
- [ ] Tests passing
- [ ] Performance metrics acceptable
- [ ] Security headers configured
- [ ] Monitoring enabled
- [ ] Error tracking configured
- [ ] Analytics integrated
- [ ] Backup strategy in place

## 🎯 Success Metrics

- **Performance**: Page load time < 3 seconds
- **Uptime**: 99.9% availability
- **Security**: No critical vulnerabilities
- **User Experience**: Lighthouse score > 90

---

**Ready to deploy? Follow the quick start guide above! 🚀**