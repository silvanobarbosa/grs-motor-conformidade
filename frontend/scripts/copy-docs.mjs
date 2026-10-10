// Copia a documentacao da raiz do repo para os assets do frontend, antes do build.
// Fonte unica: ../DOCUMENTACAO.md -> public/DOCUMENTACAO.md (servido em /DOCUMENTACAO.md).
import { copyFileSync, existsSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const origem = resolve(here, '..', '..', 'DOCUMENTACAO.md');
const destino = resolve(here, '..', 'public', 'DOCUMENTACAO.md');

if (!existsSync(origem)) {
  console.warn('[copy-docs] DOCUMENTACAO.md nao encontrado em', origem, '- pulando.');
  process.exit(0);
}
mkdirSync(dirname(destino), { recursive: true });
copyFileSync(origem, destino);
console.log('[copy-docs] DOCUMENTACAO.md copiado para public/.');
