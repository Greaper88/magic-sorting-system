#!/usr/bin/env python3
"""Run the plugin's live checks on a fresh loopback-only Spigot or Folia server."""
import argparse
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import time
import zipfile

HERE = Path(__file__).resolve().parent


def run(args):
    work = Path(tempfile.mkdtemp(prefix='mss-plugin-smoke-'))
    print(f'Test world and log: {work}', flush=True)
    plugins = work / 'plugins'
    config = plugins / 'MagicSortingSystem'
    config.mkdir(parents=True)
    (config / 'config.yml').write_text(f'variant: {args.variant}\ndelivery: {args.delivery}\nmax-distance: 1536\n')
    shutil.copyfile(args.plugin_jar, plugins / 'magic-sorting-system.jar')
    classes = work / 'probe-classes'
    subprocess.run(['javac', '--release', '25', '-cp', str(args.api_jar), '-d', str(classes),
                    str(HERE / 'ServerProbe.java')], check=True)
    with zipfile.ZipFile(plugins / 'mss-server-probe.jar', 'w', zipfile.ZIP_DEFLATED) as jar:
        jar.write(HERE / 'probe.yml', 'plugin.yml')
        for entry in classes.rglob('*.class'):
            jar.write(entry, entry.relative_to(classes).as_posix())
    (work / 'eula.txt').write_text('eula=true\n')
    if args.require_handoff:
        # Small regions in this temporary world keep the fixture's chunks independent.
        (work / 'config').mkdir()
        (work / 'config/paper-global.yml').write_text('threaded-regions:\n  grid-exponent: 2\n  threads: 2\n')
    (work / 'server.properties').write_text('''online-mode=false
server-ip=127.0.0.1
server-port=0
white-list=false
spawn-protection=0
view-distance=2
simulation-distance=2
generate-structures=false
level-type=minecraft:flat
generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}
difficulty=peaceful
max-tick-time=-1
pause-when-empty-seconds=-1
''')
    launch = args.server[1:] if args.server[0] == '--' else args.server[:]
    launch.insert(1, '-Dmss.test.variant=' + args.variant)
    log = work / 'server.log'
    with log.open('w') as output:
        server = subprocess.Popen(launch, cwd=work, stdin=subprocess.PIPE,
                                  stdout=output, stderr=subprocess.STDOUT, text=True)

        def console(text):
            server.stdin.write(text + '\n')
            server.stdin.flush()

        def wait(marker, seconds=60):
            deadline = time.monotonic() + seconds
            while time.monotonic() < deadline:
                text = log.read_text(errors='replace')
                if re.search(r'MSS_PROBE_FAIL|Error occurred while enabling MagicSortingSystem|'
                             r'Could not pass event .* to MagicSortingSystem|'
                             r'(?:Plugin|Task).*MagicSortingSystem.*(?:generated|threw) an exception|'
                             r'\[MagicSortingSystem\] Sorting task failed', text):
                    raise AssertionError(text[-6000:])
                if marker in text:
                    return text
                if server.poll() is not None:
                    break
                time.sleep(0.15)
            raise RuntimeError(f'Missing {marker}\n{text[-6000:]}')

        try:
            wait('For help, type "help"', 120)
            wait('Loaded 1657 item rules')
            if args.delivery == 'frame':
                console('mssprobe legacy-prepare')
                wait('MSS_PROBE_LOCAL_READY')
                time.sleep(1.2)
                console('mssprobe legacy-drop')
                wait('MSS_PROBE_DROPPED')
                time.sleep(1.5)
                console('mssprobe legacy-check')
                wait('MSS_PROBE_PASS')
                print('Passed original frame teleport behavior.', flush=True)
                return
            console('mssprobe prepare')
            wait('MSS_PROBE_LOCAL_READY')
            wait('MSS_PROBE_REMOTE_READY')
            time.sleep(1.6)  # wait for receiver indexing and hopper ticks
            console('mssprobe drop')
            wait('MSS_PROBE_DROPPED')
            time.sleep(4)
            console('mssprobe check')
            wait('MSS_PROBE_LOCAL_PASS')
            wait('MSS_PROBE_REMOTE_PASS')
            time.sleep(2)
            console('mssprobe unlabeled')
            wait('MSS_PROBE_PASS')
            console('mss status')
            text = wait('cross-region deposits.')
            status = next(line for line in text.splitlines() if 'cross-region deposits.' in line)
            if args.require_handoff and not re.search(r', [1-9]\d* cross-region deposits', status):
                raise AssertionError('Folia check did not take the cross-region path: ' + status)
            print(status, flush=True)
            print('Passed receiver routing, components, overflow, hopper output, physical and virtual glass.', flush=True)
        finally:
            if server.poll() is None:
                console('stop')
                try:
                    server.wait(30)
                except subprocess.TimeoutExpired:
                    server.kill()
                    server.wait()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--api-jar', type=lambda value: Path(value).resolve(), required=True)
    parser.add_argument('--plugin-jar', type=lambda value: Path(value).resolve(),
                        default=HERE.parent / 'target/magic-sorting-system-1.0.0.jar')
    parser.add_argument('--variant', choices=['standard', 'kelp-as-fuel'], default='standard')
    parser.add_argument('--delivery', choices=['hopper', 'frame'], default='hopper')
    parser.add_argument('--require-handoff', action='store_true')
    parser.add_argument('server', nargs=argparse.REMAINDER, help='Server launch command after --')
    args = parser.parse_args()
    if not args.server or args.server == ['--']:
        parser.error('Supply the server launch command after --')
    run(args)
