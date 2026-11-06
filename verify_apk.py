#!/usr/bin/env python3
"""Check the launcher APK's boundary before installing it.

    python3 -B verify_apk.py build/arabic-tv-home.apk [--expect-signer SHA256]

Asserts: package local.reroute.youtubehome, INTERNET as the only permission, one classes.dex,
no native libraries and no bundled assets, a valid APK signature, and (optionally) the
signer certificate you expect, so an update cannot silently switch keys.
"""
import argparse
import os
import re
import subprocess
import zipfile
from pathlib import Path


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('apk', type=Path)
    parser.add_argument('--expect-signer')
    args = parser.parse_args()
    sdk = Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or Path.home() / 'Android/Sdk')
    tools = sdk / 'build-tools' / os.environ.get('ANDROID_BUILD_TOOLS', '36.0.0')
    apk = args.apk
    assert apk.exists(), f'{apk} has not been built'

    permissions = subprocess.check_output([str(tools / 'aapt2'), 'dump', 'permissions', str(apk)], text=True)
    requested = {line.strip() for line in permissions.splitlines() if line.strip().startswith('uses-permission:')}
    assert requested == {"uses-permission: name='android.permission.INTERNET'"}, permissions

    badging = subprocess.check_output([str(tools / 'aapt2'), 'dump', 'badging', str(apk)], text=True)
    assert "name='local.reroute.youtubehome'" in badging, 'unexpected package identity'

    certs = subprocess.check_output([str(tools / 'apksigner'), 'verify', '--print-certs', str(apk)], text=True)
    match = re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)', certs)
    assert match, 'APK signature did not verify'
    if args.expect_signer:
        assert match.group(1) == args.expect_signer.lower(), f'signer {match.group(1)} differs from the expected key'

    with zipfile.ZipFile(apk) as z:
        names = z.namelist()
        assert 'classes.dex' in names and 'classes2.dex' not in names
        assert not any(n.startswith(('lib/', 'assets/')) for n in names), 'native code or bundled assets present'
    print(f'Verified {apk.name}: INTERNET only, no native code or assets, signer {match.group(1)[:12]}…')


if __name__ == '__main__':
    main()
