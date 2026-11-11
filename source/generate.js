#!/usr/bin/env node
'use strict';

// Magic Sorting System, based on Joseph Huckaby's MIT-licensed generator.
// Usage: node source/generate.js [config.json] [output-root]
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');

function validateConfig(config) {
  const resource = /^[a-z0-9_.-]+:[a-z0-9_./-]+$/;
  const groups = new Map();
  const frames = new Set();
  const items = new Set();
  assert(Number.isFinite(config.max_teleport_distance) && config.max_teleport_distance > 0,
    'max_teleport_distance must be positive');
  assert(typeof config.final_fallback === 'string' && config.final_fallback.length &&
    !/[\r\n]/.test(config.final_fallback), 'final_fallback must be a single command');
  assert(Array.isArray(config.effects) && config.effects.every(effect =>
    typeof effect === 'string' && effect.length && !/[\r\n]/.test(effect)),
  'effects must contain single-line commands');
  assert(Array.isArray(config.groups) && config.groups.length, 'groups must not be empty');
  for (const group of config.groups) {
    const id = group.group_name;
    assert(typeof id === 'string' && /^[a-z0-9_-]+$/.test(id), `Invalid group ID: ${id}`);
    assert(!groups.has(id), `Duplicate group: ${id}`);
    assert(resource.test(group.item_frame), `Invalid receiver item: ${group.item_frame}`);
    assert(!frames.has(group.item_frame), `Duplicate receiver item: ${group.item_frame}`);
    assert(Array.isArray(group.items) && group.items.length, `Empty group: ${id}`);
    groups.set(id, group);
    frames.add(group.item_frame);
    for (const item of group.items) {
      assert(resource.test(item), `Invalid item ID: ${item}`);
      assert(!items.has(item), `Duplicate item: ${item}`);
      items.add(item);
    }
  }
  assert(groups.has('misc'), 'A misc group is required for unrecognized items');
  for (const group of config.groups) {
    const visited = new Set([group.group_name]);
    let fallback = group.fallback;
    while (fallback) {
      assert(groups.has(fallback), `Unknown fallback: ${fallback}`);
      assert(!visited.has(fallback), `Fallback cycle: ${group.group_name}`);
      visited.add(fallback);
      fallback = groups.get(fallback).fallback;
    }
  }
  return items;
}

function generate(config, outputRoot = path.resolve(__dirname, '..')) {
  const items = validateConfig(config);
  const functionDir = path.join(outputRoot, 'data/mss/function');
  const tagDir = path.join(outputRoot, 'data/mss/tags/item/group');
  fs.mkdirSync(functionDir, { recursive: true });
  fs.mkdirSync(tagDir, { recursive: true });
  // Remove only generated files, so deleted custom groups cannot linger.
  for (const name of fs.readdirSync(functionDir)) {
    if (/^sort(?:_[a-z0-9_-]+)?\.mcfunction$/.test(name)) {
      fs.unlinkSync(path.join(functionDir, name));
    }
  }
  for (const name of fs.readdirSync(tagDir)) {
    if (name.endsWith('.json')) fs.unlinkSync(path.join(tagDir, name));
  }
  const lines = [
    '# Magic Sorting System -- Minecraft Java Edition 26.3',
    '# Executes as the dropped item, positioned at its controller.',
    '# Return after dispatch so one item can only take one sorting path.',
    ...config.effects.map(effect =>
      `execute unless score #mss_cooldown mss_cooldown matches 1 run ${effect}`),
    'scoreboard players set #mss_cooldown mss_cooldown 1',
    ''
  ];
  for (const group of config.groups) {
    const id = group.group_name;
    const receiver = `type=#mss:item_frames,nbt={Item:{id:"${group.item_frame}"}},distance=..${config.max_teleport_distance}`;
    const fallback = group.fallback ? `function mss:sort_${group.fallback}` : config.final_fallback;
    fs.writeFileSync(path.join(functionDir, `sort_${id}.mcfunction`), [
      `# ${id}: ${group.item_frame}`,
      `execute if entity @e[${receiver},limit=1] run return run teleport @s @e[${receiver},limit=1,sort=random]`,
      `return run ${fallback}`,
      ''
    ].join('\n'));
    fs.writeFileSync(path.join(tagDir, `${id}.json`), JSON.stringify({ values: group.items }, null, 2) + '\n');
    if (id !== 'misc') {
      lines.push(`execute if items entity @s contents #mss:group/${id} run return run function mss:sort_${id}`);
    }
  }
  lines.push('return run function mss:sort_misc', '');
  fs.writeFileSync(path.join(functionDir, 'sort.mcfunction'), lines.join('\n'));
  return { items: items.size, groups: config.groups.length };
}

if (require.main === module) {
  const configPath = process.argv[2] ? path.resolve(process.argv[2]) : path.join(__dirname, 'config.json');
  const outputRoot = process.argv[3] ? path.resolve(process.argv[3]) : path.resolve(__dirname, '..');
  const result = generate(JSON.parse(fs.readFileSync(configPath, 'utf8')), outputRoot);
  console.log(`Generated ${result.items} items in ${result.groups} groups from ${path.basename(configPath)}.`);
}

module.exports = { generate, validateConfig };
