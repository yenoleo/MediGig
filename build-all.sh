#!/usr/bin/env bash
# MediGig ZA — one-shot build + preview.
#   ./build-all.sh            build and serve preview on :8787
#   ./build-all.sh dev        start dev servers (Vite :5173 + API :8787)
#   ./build-all.sh build      build only
#   ./build-all.sh preview    build then serve the production bundle
set -euo pipefail
cd "$(dirname "$0")"

MODE="${1:-preview}"

if [ ! -d node_modules ]; then
  echo "• installing dependencies"
  npm install --no-audit --no-fund
fi

case "$MODE" in
  dev)
    echo "• MediGig dev → http://localhost:5173  (API :8787)"
    exec npm run dev
    ;;
  build)
    echo "• building storefront"
    npm run build
    ;;
  preview|start)
    echo "• building storefront"
    npm run build
    echo "• serving MediGig ZA → http://localhost:8787"
    NODE_ENV=production exec node server/index.js
    ;;
  *)
    echo "usage: $0 [dev|build|preview]" >&2
    exit 2
    ;;
esac
