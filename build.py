#!/usr/bin/env python3
"""Build the Arabic TV Home APK with the Android SDK command-line tools only.

    python3 -B build.py                                   # stock YouTube TV, local key
    python3 -B build.py --youtube-package com.google.android.youtube.tx
    python3 -B build.py --keystore my.jks --ks-pass-file pass.txt --key-alias owner \\
                        --expect-signer <sha256 of your certificate>

No Gradle, no dependencies. The YouTube target package is the only build-time option that
changes code: it is substituted into a staged copy of the sources, never into src/.

If no keystore is given, a new RSA key is created once under private/ (git-ignored). Keep it:
Android only accepts an update to an installed launcher signed with the same key.
"""
import argparse
import os
import re
import secrets
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
DEFAULT_TARGET = 'com.google.android.youtube.tv'
TARGET_LINE = re.compile(r'(private static final String YOUTUBE=")([A-Za-z0-9_.]+)(";)')


def sdk_paths():
    sdk = Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or Path.home() / 'Android/Sdk')
    tools = sdk / 'build-tools' / os.environ.get('ANDROID_BUILD_TOOLS', '36.0.0')
    jar = sdk / 'platforms' / os.environ.get('ANDROID_PLATFORM', 'android-36') / 'android.jar'
    for path in (tools / 'd8', tools / 'aapt2', tools / 'apksigner', tools / 'zipalign', jar):
        if not path.exists():
            raise SystemExit(f'Missing Android SDK component: {path}\nSet ANDROID_HOME, ANDROID_BUILD_TOOLS or ANDROID_PLATFORM.')
    return tools, jar


def run(args, **kw):
    subprocess.run(list(map(str, args)), check=True, **kw)


def stage_sources(build, target):
    staged = build / 'src'
    if staged.exists():
        shutil.rmtree(staged)
    shutil.copytree(ROOT / 'src', staged)
    activity = staged / 'local/reroute/youtubehome/HomeActivity.java'
    text = activity.read_text(encoding='utf-8')
    if len(TARGET_LINE.findall(text)) != 1:
        raise SystemExit('Could not find the single YouTube target constant in HomeActivity.java')
    activity.write_text(TARGET_LINE.sub(lambda m: m.group(1) + target + m.group(3), text), encoding='utf-8')
    return staged


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--youtube-package', default=DEFAULT_TARGET,
                        help=f'package opened by the single action (default {DEFAULT_TARGET})')
    parser.add_argument('--keystore', type=Path, help='existing JKS/PKCS12 keystore')
    parser.add_argument('--ks-pass-file', type=Path, help='file containing the keystore password')
    parser.add_argument('--key-alias', default='owner')
    parser.add_argument('--expect-signer', help='fail unless the signer certificate SHA-256 matches')
    args = parser.parse_args()
    if not re.fullmatch(r'[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z][A-Za-z0-9_]*)+', args.youtube_package):
        raise SystemExit('Invalid package name: ' + args.youtube_package)

    tools, jar = sdk_paths()
    os.umask(0o077)
    build = ROOT / 'build'
    classes, dex = build / 'classes', build / 'dex'
    for directory in (classes, dex):
        if directory.exists():
            shutil.rmtree(directory)
        directory.mkdir(parents=True)

    staged = stage_sources(build, args.youtube_package)
    run(['javac', '-source', '8', '-target', '8', '-bootclasspath', jar, '-d', classes, *sorted(staged.rglob('*.java'))])
    run([tools / 'd8', '--lib', jar, '--min-api', '21', '--output', dex, *sorted(classes.rglob('*.class'))])
    run([tools / 'aapt2', 'link', '-I', jar, '--manifest', ROOT / 'AndroidManifest.xml', '-o', build / 'unsigned.apk'])
    with zipfile.ZipFile(build / 'unsigned.apk', 'a', zipfile.ZIP_DEFLATED) as z:
        z.write(dex / 'classes.dex', 'classes.dex')
    run([tools / 'zipalign', '-f', '4', build / 'unsigned.apk', build / 'aligned.apk'])

    if args.keystore:
        key, password = args.keystore, args.ks_pass_file
        if not password:
            raise SystemExit('--keystore requires --ks-pass-file')
    else:
        private = ROOT / 'private'
        private.mkdir(exist_ok=True)
        key, password = private / 'arabic-tv-home.jks', private / 'key-password'
        if not password.exists():
            password.write_text(secrets.token_urlsafe(32) + '\n')
        if not key.exists():
            run(['keytool', '-genkeypair', '-keystore', key, '-storepass:file', password, '-keypass:file', password,
                 '-alias', args.key_alias, '-keyalg', 'RSA', '-keysize', '3072', '-validity', '10000',
                 '-dname', 'CN=Arabic TV Home local build', '-noprompt'])
            print('Created a new signing key in private/. Back it up; updates need the same key.')
    apk = build / 'arabic-tv-home.apk'
    run([tools / 'apksigner', 'sign', '--ks', key, '--ks-key-alias', args.key_alias,
         '--ks-pass', 'file:' + str(password), '--out', apk, build / 'aligned.apk'])

    verify = ['python3', '-B', ROOT / 'verify_apk.py', apk]
    if args.expect_signer:
        verify += ['--expect-signer', args.expect_signer]
    run(verify)
    print('YouTube target:', args.youtube_package)
    print('Built:', apk)


if __name__ == '__main__':
    main()
