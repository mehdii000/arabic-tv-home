"""Build the offscreen review harness (tests/SkyReview, tests/HomePreview) and optionally run it.

    python3 -B preview.py                      # compile only -> build/preview/preview.dex
    python3 -B preview.py --serial SERIAL OUT  # push to /data/local/tmp, render, pull PNGs to OUT, clean up

The harness is never packaged into the launcher APK. Running it installs nothing: it uses
app_process with a temporary dex under /data/local/tmp, which is removed afterwards.
"""
from pathlib import Path
import os
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parent
SDK = Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or Path.home() / 'Android/Sdk')
TOOLS = SDK / 'build-tools' / os.environ.get('ANDROID_BUILD_TOOLS', '36.0.0')
JAR = SDK / 'platforms' / os.environ.get('ANDROID_PLATFORM', 'android-36') / 'android.jar'
OUT = ROOT / 'build/preview'
REMOTE_DEX = '/data/local/tmp/reroute-preview.dex'
REMOTE_DIR = '/data/local/tmp/reroute-preview'


def run(args, **kw):
    return subprocess.run(list(map(str, args)), check=True, **kw)


def compile_dex():
    if OUT.exists():
        shutil.rmtree(OUT)
    (OUT / 'classes').mkdir(parents=True)
    sources = [*ROOT.joinpath('src').rglob('*.java'), ROOT / 'tests/SkyReview.java', ROOT / 'tests/HomePreview.java']
    run(['javac', '-source', '8', '-target', '8', '-bootclasspath', JAR, '-d', OUT / 'classes', *sources])
    run([TOOLS / 'd8', '--lib', JAR, '--min-api', '21', '--output', OUT, *OUT.joinpath('classes').rglob('*.class')])
    dex = OUT / 'preview.dex'
    (OUT / 'classes.dex').rename(dex)
    return dex


def render(serial, dest):
    dex = compile_dex()
    adb = ['adb', '-s', serial]
    dest = Path(dest)
    dest.mkdir(parents=True, exist_ok=True)
    try:
        run([*adb, 'push', dex, REMOTE_DEX], stdout=subprocess.DEVNULL)
        for cls in ('SkyReview', 'HomePreview'):
            run([*adb, 'shell', f'CLASSPATH={REMOTE_DEX} app_process /system/bin local.reroute.youtubehome.{cls} {REMOTE_DIR}'])
        run([*adb, 'pull', REMOTE_DIR + '/.', dest], stdout=subprocess.DEVNULL)
    finally:
        subprocess.run([*adb, 'shell', f'rm -rf {REMOTE_DEX} {REMOTE_DIR}'], check=False)
    print('Preview renders in', dest)


if __name__ == '__main__':
    if len(sys.argv) == 4 and sys.argv[1] == '--serial':
        render(sys.argv[2], sys.argv[3])
    elif len(sys.argv) == 1:
        print('Compiled', compile_dex())
    else:
        sys.exit(__doc__)
