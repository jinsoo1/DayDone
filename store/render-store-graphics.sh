#!/bin/zsh
# 피처 그래픽 렌더 — 헤드리스 크롬으로 feature-graphic-source.html → PNG (ko/ja).
#   ./store/render-store-graphics.sh          # 두 언어
#   ./store/render-store-graphics.sh ja       # 일본어만
# 스크린샷은 store/screenshot-maker.html 을 브라우저로 열어 만든다(캡처 업로드·슬라이더·PNG 내려받기).
set -e
cd "$(dirname "$0")"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
LANGS=(${=1:-ko ja})
mkdir -p out
for L in $LANGS; do
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
    --window-size=1024,500 --screenshot="out/feature-graphic-1024x500-$L.png" \
    "file://$PWD/feature-graphic-source.html?lang=$L" 2>/dev/null
done
ls -la out
