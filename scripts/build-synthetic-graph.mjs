#!/usr/bin/env node
/**
 * Build a heuristic knowledge-graph.json from scan-result.json without
 * requiring the Understand-Anything LLM agent pipeline.
 *
 * Output: .understand-anything/knowledge-graph-synthetic.json
 *
 * This produces a graph useful for visualization when LLM-driven analysis
 * is unavailable. Each file becomes a node; parent/child relations become
 * contains edges; co-location by directory creates depends_on edges.
 */

import { readFileSync, writeFileSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(__dirname, "..");

const scan = JSON.parse(
  readFileSync(resolve(projectRoot, ".understand-anything/intermediate/scan-result.json"), "utf-8"),
);

const LANG_TYPE = {
  java: "class",
  kotlin: "class",
  typescript: "module",
  javascript: "module",
  python: "module",
  yaml: "config",
  json: "config",
  sql: "schema",
  groovy: "test",
  dockerfile: "container",
  shell: "script",
  powershell: "script",
  markdown: "doc",
};

const nodes = [];
const edges = [];
const idMap = new Map();

let i = 0;
for (const f of scan.files) {
  const id = `synthetic:${f.path}`;
  if (idMap.has(id)) continue;
  idMap.set(id, true);
  const type = LANG_TYPE[f.language] || "file";
  const parts = f.path.split("/");
  const name = parts[parts.length - 1];
  nodes.push({
    id,
    type,
    language: f.language,
    fileCategory: f.fileCategory,
    title: name,
    summary: `[heuristic] ${name} (${f.sizeLines} lines, ${f.language})`,
    tags: [f.language, f.fileCategory],
    synthetic: true,
  });

  // contains edge to parent directory
  if (parts.length > 1) {
    const parent = `synthetic:dir:${parts.slice(0, -1).join("/")}`;
    if (!idMap.has(parent)) {
      idMap.set(parent, true);
      nodes.push({ id: parent, type: "directory", title: parts.slice(0, -1).join("/"), synthetic: true });
    }
    edges.push({ source: parent, target: id, type: "contains" });
  }

  // depends_on: same-directory files
  // (computed below in second pass for performance)
  i++;
}

// depends_on: files sharing a directory
const dirGroups = new Map();
for (const f of scan.files) {
  const dir = f.path.includes("/") ? f.path.split("/").slice(0, -1).join("/") : ".";
  if (!dirGroups.has(dir)) dirGroups.set(dir, []);
  dirGroups.get(dir).push(`synthetic:${f.path}`);
}

for (const [dir, ids] of dirGroups) {
  if (ids.length > 8) continue; // skip huge directories
  for (let a = 0; a < ids.length; a++) {
    for (let b = a + 1; b < ids.length; b++) {
      edges.push({ source: ids[a], target: ids[b], type: "depends_on", synthetic: true });
    }
  }
}

const out = {
  meta: {
    projectName: scan.projectName,
    projectTitle: scan.projectTitle,
    description: scan.description,
    rootPath: scan.rootPath,
    generatedAt: new Date().toISOString(),
    synthetic: true,
    strategy: "heuristic from scan-result.json (no LLM)",
    nodeCount: nodes.length,
    edgeCount: edges.length,
  },
  nodes,
  edges,
};

writeFileSync(
  resolve(projectRoot, ".understand-anything/knowledge-graph-synthetic.json"),
  JSON.stringify(out, null, 2),
);
console.log(`[synthetic-graph] Wrote ${nodes.length} nodes / ${edges.length} edges`);
