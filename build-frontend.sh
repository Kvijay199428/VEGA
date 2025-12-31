#!/bin/bash

# Vega Trader's Frontend Build Script
# This script sets up and builds the complete React frontend application

echo "🚀 Building Vega Trader's Frontend Application"
echo "=============================================="

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16+ first."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm first."
    exit 1
fi

echo "✅ Node.js and npm found"
echo "📦 Node version: $(node --version)"
echo "📦 npm version: $(npm --version)"

# Navigate to frontend directory
cd vega-traders-frontend

echo ""
echo "📥 Installing dependencies..."
echo "================================"

# Install dependencies
npm install

if [ $? -ne 0 ]; then
    echo "❌ Failed to install dependencies"
    exit 1
fi

echo "✅ Dependencies installed successfully"

# Create additional directories if they don't exist
mkdir -p src/components/{Dashboard,Market,Trading,Portfolio,Strategies,Indicators,Setup}
mkdir -p src/pages/{Dashboard,Market,Trading,Portfolio,Strategies,Indicators,Settings,Setup,Auth}
mkdir -p src/hooks
mkdir -p src/types
mkdir -p src/utils

echo ""
echo "🔧 Setting up Tailwind CSS..."
echo "==============================="

# Initialize Tailwind CSS
npx tailwindcss init -p

echo "✅ Tailwind CSS configured"

echo ""
echo "🎨 Building application..."
echo "=========================="

# Build the application
npm run build

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build completed successfully"

echo ""
echo "📋 Build Summary"
echo "================"
echo "✅ Project structure created"
echo "✅ Dependencies installed"
echo "✅ Tailwind CSS configured"
echo "✅ Application built"
echo ""
echo "📂 Build output: build/"
echo "🚀 Ready for deployment!"
echo ""
echo "To run the application in development mode:"
echo "cd vega-traders-frontend && npm start"
echo ""
echo "To serve the built application:"
echo "cd vega-traders-frontend && npm install -g serve && serve -s build"