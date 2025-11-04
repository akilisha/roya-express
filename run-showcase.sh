#!/bin/bash

# AI Showcase Runner Script
# Usage: ./run-showcase.sh [your-openai-api-key]

API_KEY=${1:-${OPENAI_API_KEY}}

if [ -z "$API_KEY" ]; then
    echo "❌ Error: OpenAI API key required"
    echo ""
    echo "Usage:"
    echo "  ./run-showcase.sh YOUR_API_KEY"
    echo "  OR"
    echo "  export OPENAI_API_KEY=your-key"
    echo "  ./run-showcase.sh"
    echo ""
    exit 1
fi

echo "🚀 Starting AI Showcase..."
echo "📝 API Key: ${API_KEY:0:8}..."
echo ""

./gradlew :roya-examples:run \
  -Dai.openai.apiKey="$API_KEY" \
  -Dexec.mainClass="com.akilisha.oss.roya.examples.AIShowcase" \
  --args="AIShowcase" \
  --no-daemon

