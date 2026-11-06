import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const procs = [];
let shuttingDown = false;

function run(name, cmd, args, color) {
  const p = spawn(cmd, args, { cwd: root, stdio: ['ignore', 'pipe', 'pipe'], env: process.env });
  const tag = `\x1b[${color}m[${name}]\x1b[0m`;
  const pipe = (stream, out) => {
    let buf = '';
    stream.on('data', (chunk) => {
      buf += chunk.toString();
      const lines = buf.split('\n');
      buf = lines.pop() ?? '';
      for (const line of lines) if (line.trim()) out.write(`${tag} ${line}\n`);
    });
  };
  pipe(p.stdout, process.stdout);
  pipe(p.stderr, process.stderr);
  p.on('exit', (code) => {
    if (!shuttingDown) {
      console.log(`${tag} exited with ${code}`);
      shutdown(code ?? 1);
    }
  });
  procs.push(p);
  return p;
}

function shutdown(code = 0) {
  if (shuttingDown) return;
  shuttingDown = true;
  for (const p of procs) {
    try { p.kill('SIGTERM'); } catch {}
  }
  setTimeout(() => process.exit(code), 300);
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));

run('api', process.execPath, ['server/index.js'], '36');
run('web', path.join(root, 'node_modules/.bin/vite'), ['--port', '5173', '--host', '0.0.0.0'], '35');
