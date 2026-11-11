#!/usr/bin/env python3
"""Build both installable datapack ZIPs without changing the checked-in standard pack."""
import argparse
import json
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'source'
VARIANTS = {'standard': 'config.json', 'kelp-as-fuel': 'config-kelp-as-fuel.json'}


def build(node, output):
    registry = json.loads((SOURCE / 'items-26.3.json').read_text())
    expected = set(registry['items'])
    format_version = registry['data_pack_version']
    metadata = json.loads((ROOT / 'pack.mcmeta').read_text())
    if metadata['pack']['min_format'] != format_version or metadata['pack']['max_format'] != format_version:
        raise ValueError('pack.mcmeta does not match the Minecraft registry report version')
    output.mkdir(parents=True, exist_ok=True)
    for variant, name in VARIANTS.items():
        config_file = SOURCE / name
        config = json.loads(config_file.read_text())
        if config['minecraft_version'] != registry['minecraft_version']:
            raise ValueError(f'{variant} does not target the Minecraft registry report version')
        items = [item for group in config['groups'] for item in group['items']]
        missing, unknown = expected - set(items), set(items) - expected
        if missing or unknown or len(items) != len(expected):
            raise ValueError(f'{variant} item coverage: missing={sorted(missing)}, unknown={sorted(unknown)}, '
                             f'{len(items)} total entries for {len(expected)} item IDs')
        if any(group['item_frame'] not in expected for group in config['groups']):
            raise ValueError(f'{variant} has an invalid receiver item')
        with tempfile.TemporaryDirectory(prefix='mss-build-') as temp:
            pack = Path(temp)
            shutil.copytree(ROOT / 'data', pack / 'data')
            metadata['pack']['description'] = f'Magic Sorting System — Java Edition 26.3 ({variant})'
            (pack / 'pack.mcmeta').write_text(json.dumps(metadata, indent=2) + '\n')
            subprocess.run([node, str(SOURCE / 'generate.js'), str(config_file), str(pack)], check=True)
            shutil.copyfile(ROOT / 'README.md', pack / 'README.md')
            shutil.copyfile(ROOT / 'LICENSE', pack / 'LICENSE')
            (pack / 'source').mkdir()
            for filename in ('generate.js', 'generate-kelp-as-fuel.js', 'config-kelp-as-fuel.json'):
                shutil.copyfile(SOURCE / filename, pack / 'source' / filename)
            shutil.copyfile(config_file, pack / 'source/config.json')
            archive = output / f'magic-sorting-system-26.3-{variant}.zip'
            with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as zipped:
                for entry in sorted(pack.rglob('*')):
                    if entry.is_file():
                        zipped.write(entry, entry.relative_to(pack).as_posix())
            print(archive)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--node', default='node', help='Path to Node.js')
    parser.add_argument('--output', type=Path, default=ROOT / 'dist', help='Directory for the ZIPs')
    args = parser.parse_args()
    build(args.node, args.output)
