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

# 3. Verify F-Droid & Fastlane metadata
$PYTHON -c "
import os
assert os.path.exists('metadata/com.example.matrixrain.yml'), 'F-Droid metadata missing'
assert os.path.exists('fastlane/metadata/android/en-US/full_description.txt'), 'Fastlane description missing'
print('✔ F-Droid and Play Store metadata validated successfully.')
"

echo "✔ All Matrix Rain CI verification checks passed!"
