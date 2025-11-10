import { cpSync, existsSync, mkdirSync, rmSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const projectRoot = resolve(__dirname, '..', '..');
const sourceDir = resolve(projectRoot, 'build', 'docs', 'javadoc');
const targetDir = resolve(__dirname, '..', 'public', 'javadoc');

if (!existsSync(sourceDir)) {
  console.error(
    `No aggregated Javadoc found at ${sourceDir}. Run "./gradlew aggregateJavadoc" first.`
  );
  process.exit(1);
}

if (existsSync(targetDir)) {
  rmSync(targetDir, { recursive: true, force: true });
}
mkdirSync(targetDir, { recursive: true });
cpSync(sourceDir, targetDir, { recursive: true });

console.info(`Copied aggregated Javadoc from ${sourceDir} to ${targetDir}`);


