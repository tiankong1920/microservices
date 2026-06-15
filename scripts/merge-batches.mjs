#!/usr/bin/env node
/**
 * Merge all batch-*.json files in intermediate/ into a single
 * knowledge-graph.json that the dashboard can render.
 *
 * Strategy:
 *  - Read every intermediate/batch-N.json
 *  - Deduplicate nodes/edges by id (incoming precedence) and edge composite key
 *  - Wrap as { nodes, edges, meta }
 *  - Write to dashboard/public/knowledge-graph.json
 */

import { readFileSync, writeFileSync, mkdirSync, readdirSync, existsSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(__dirname, "..");
const intermediateDir = resolve(projectRoot, ".understand-anything/intermediate");
const dashboardPublic = resolve(projectRoot, "node_modules/@understand-anything/dashboard/public");

const batchFiles = readdirSync(intermediateDir)
  .filter((f) => /^batch-\d+\.json$/.test(f))
  .sort((a, b) => {
    const na = parseInt(a.match(/(\d+)/)[1], 10);
    const nb = parseInt(b.match(/(\d+)/)[1], 10);
    return na - nb;
  });

console.log(`[merge-batches] Found ${batchFiles.length} batch files`);

const nodeMap = new Map();
const edgeSet = new Set();
const edges = [];

for (const file of batchFiles) {
  const fp = resolve(intermediateDir, file);
  const json = JSON.parse(readFileSync(fp, "utf-8"));
  for (const n of json.nodes || []) {
    if (n && n.id && !nodeMap.has(n.id)) {
      nodeMap.set(n.id, n);
    }
  }
  for (const e of json.edges || []) {
    const key = `${e.source}|${e.target}|${e.type}`;
    if (!edgeSet.has(key)) {
      edgeSet.add(key);
      edges.push(e);
    }
  }
}

const merged = {
  meta: {
    projectName: "inventory-management-system",
    projectTitle: "库存管理系统",
    description: "基于微服务架构的企业级库存管理平台（部分批次）",
    rootPath: projectRoot,
    generatedAt: new Date().toISOString(),
    partial: true,
    batches: batchFiles.length,
    nodeCount: nodeMap.size,
    edgeCount: edges.length,
  },
  nodes: Array.from(nodeMap.values()),
  edges,
};

if (!existsSync(dashboardPublic)) {
  mkdirSync(dashboardPublic, { recursive: true });
}

const out = resolve(dashboardPublic, "knowledge-graph.json");
writeFileSync(out, JSON.stringify(merged, null, 2));
console.log(`[merge-batches] Wrote ${merged.nodes.length} nodes / ${merged.edges.length} edges -> ${out}`);

// Also drop a copy at project root for inspection
const rootCopy = resolve(projectRoot, ".understand-anything/knowledge-graph.json");
writeFileSync(rootCopy, JSON.stringify(merged, null, 2));
console.log(`[merge-batches] Mirror -> ${rootCopy}`);
