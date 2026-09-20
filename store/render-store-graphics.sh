#!/bin/zsh
# 스토어 그래픽 렌더 — 헤드리스 크롬으로 HTML → PNG.
#   ./store/render-store-graphics.sh            # 피처 그래픽 ko/ja + 스크린샷 ko/ja 5장씩
#   ./store/render-store-graphics.sh ja         # 일본어만
# 스크린샷 원본은 store/raw/<lang>/01-today.png … 05-privacy.png (없으면 자리 표시로 렌더됨)
set -e
cd "$(dirname "$0")"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
LANGS=(${=1:-ko ja})
mkdir -p out
for L in $LANGS; do
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
    --window-size=1024,500 --screenshot="out/feature-graphic-1024x500-$L.png" \
    "file://$PWD/feature-graphic-source.html?lang=$L" 2>/dev/null
  for N in 1 2 3 4 5; do
    "$CHROME" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
      --window-size=1080,1920 --screenshot="out/screenshot-$L-0$N.png" \
      "file://$PWD/screenshot-maker.html?lang=$L&slot=$N" 2>/dev/null
  done
done
ls -la out
