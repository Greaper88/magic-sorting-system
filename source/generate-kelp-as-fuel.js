#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { generate } = require('./generate.js');
const config = JSON.parse(fs.readFileSync(path.join(__dirname, 'config-kelp-as-fuel.json'), 'utf8'));
const result = generate(config, process.argv[2] ? path.resolve(process.argv[2]) : path.resolve(__dirname, '..'));
console.log(`Generated kelp-as-fuel: ${result.items} items in ${result.groups} groups.`);
