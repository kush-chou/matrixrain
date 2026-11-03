#!/bin/sh
set -e

PYTHON="${PYTHON:-/usr/bin/python3}"
[ -x "$PYTHON" ] || PYTHON="python3"

echo "==> [CI] Running Matrix Rain verification suite..."

# 1. Verify Android manifest XML syntax
$PYTHON -c "
import xml.etree.ElementTree as ET
ET.parse('app/src/main/AndroidManifest.xml')
print('✔ Android XML manifest validated successfully.')
"

# 2. Verify Web app & landing page files
$PYTHON -c "
import os
assert os.path.exists('web/index.html'), 'web/index.html missing'
assert os.path.exists('web/styles.css'), 'web/styles.css missing'
assert os.path.exists('web/app.js'), 'web/app.js missing'
assert os.path.exists('web/privacy.html'), 'web/privacy.html missing'
print('✔ matrixrain.app web assets validated successfully.')
"

# 3. Verify F-Droid & Fastlane metadata and store graphics
$PYTHON -c "
import os
assert os.path.exists('metadata/app.matrixrain.yml'), 'F-Droid metadata missing'
assert os.path.exists('fastlane/metadata/android/en-US/full_description.txt'), 'Fastlane description missing'
assert os.path.exists('fastlane/metadata/android/en-US/images/icon.png'), 'Store 512x512 icon missing'
assert os.path.exists('fastlane/metadata/android/en-US/images/featureGraphic.png'), 'Feature graphic missing'
assert os.path.exists('web/assets/og-preview.png'), 'OpenGraph image missing'
assert os.path.exists('release.keystore'), 'Release keystore missing'
print('✔ Store graphics, app icons, and release keystore validated successfully.')
"

echo "✔ All Matrix Rain CI verification checks passed!"
