#!/bin/sh
set -e

PYTHON="${PYTHON:-/usr/bin/python3}"
[ -x "$PYTHON" ] || PYTHON="python3"

echo "==> [CI] Running Matrix Rain F-Droid verification suite..."

# 1. Verify Android manifest XML syntax
$PYTHON -c "
import xml.etree.ElementTree as ET
ET.parse('app/src/main/AndroidManifest.xml')
print('✔ Android XML manifest validated successfully.')
"

# 2. Verify F-Droid metadata and store graphics
$PYTHON -c "
import os
assert os.path.exists('metadata/com.example.matrixrain.yml'), 'F-Droid metadata missing'
assert os.path.exists('fastlane/metadata/android/en-US/full_description.txt'), 'Fastlane description missing'
assert os.path.exists('fastlane/metadata/android/en-US/images/icon.png'), 'Store icon missing'
assert os.path.exists('fastlane/metadata/android/en-US/images/featureGraphic.png'), 'Feature graphic missing'
assert os.path.exists('release.keystore'), 'Release keystore missing'
print('✔ Store graphics, app icons, and release keystore validated successfully.')
"

# 3. Verify clean release build
./gradlew assembleRelease --no-daemon

echo "✔ All Matrix Rain F-Droid CI verification checks passed!"
