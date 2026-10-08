#!/bin/sh
set -e

PYTHON="${PYTHON:-/usr/bin/python3}"
[ -x "$PYTHON" ] || PYTHON="python3"

echo "==> [CI] Running Matrix Rain verification suite..."

# 1. Verify Android manifests and Watch Face Format XML syntax
$PYTHON -c "
import xml.etree.ElementTree as ET
ET.parse('app/src/main/AndroidManifest.xml')
ET.parse('wear/src/main/AndroidManifest.xml')
ET.parse('wear/src/main/res/raw/watchface.xml')
print('✔ Android & Wear OS XML manifests validated successfully.')
"

# 2. Verify Landing page files
$PYTHON -c "
import os
assert os.path.exists('landing/index.html'), 'landing/index.html missing'
assert os.path.exists('landing/styles.css'), 'landing/styles.css missing'
assert os.path.exists('landing/app.js'), 'landing/app.js missing'
assert os.path.exists('landing/privacy.html'), 'landing/privacy.html missing'
print('✔ matrixrain.app landing assets validated successfully.')
"

# 3. Verify F-Droid & Fastlane metadata and store graphics
$PYTHON -c "
import os
assert os.path.exists('metadata/com.example.matrixrain.yml'), 'F-Droid metadata missing'
assert os.path.exists('fastlane/metadata/android/en-US/full_description.txt'), 'Fastlane description missing'
assert os.path.exists('fastlane/metadata/android/en-US/images/icon.png'), 'Store 512x512 icon missing'
assert os.path.exists('fastlane/metadata/android/en-US/images/featureGraphic.png'), 'Feature graphic missing'
assert os.path.exists('landing/assets/og-preview.png'), 'OpenGraph image missing'
assert os.path.exists('release.keystore'), 'Release keystore missing'
print('✔ Store graphics, app icons, and release keystore validated successfully.')
"

echo "✔ All Matrix Rain CI verification checks passed!"
