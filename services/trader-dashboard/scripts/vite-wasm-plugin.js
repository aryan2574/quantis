/**
 * Vite Plugin for Automatic WebAssembly Compilation
 * 
 * This plugin automatically compiles C++ code to WebAssembly during development
 * and build processes, ensuring the WASM module is always up to date.
 */

import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

function wasmBuildPlugin(options = {}) {
    const pluginOptions = {
        sourceDir: options.sourceDir || 'src/lib/wasm',
        outputDir: options.outputDir || 'public/wasm',
        sourceFile: options.sourceFile || 'analytics.cpp',
        watch: options.watch !== false, // Default to true
        ...options
    };

    const buildScript = path.join(__dirname, 'build-wasm.js');
    let lastBuildTime = 0;

    /**
     * Check if WASM needs to be rebuilt
     */
    function needsRebuild() {
        const sourcePath = path.join(pluginOptions.sourceDir, pluginOptions.sourceFile);
        const outputJsPath = path.join(pluginOptions.outputDir, 'analytics.js');
        const outputWasmPath = path.join(pluginOptions.outputDir, 'analytics.wasm');

        if (!fs.existsSync(sourcePath)) {
            return false; // Source doesn't exist, skip
        }

        if (!fs.existsSync(outputJsPath) || !fs.existsSync(outputWasmPath)) {
            return true; // Output doesn't exist, need to build
        }

        const sourceTime = fs.statSync(sourcePath).mtime.getTime();
        const outputTime = Math.max(
            fs.existsSync(outputJsPath) ? fs.statSync(outputJsPath).mtime.getTime() : 0,
            fs.existsSync(outputWasmPath) ? fs.statSync(outputWasmPath).mtime.getTime() : 0
        );

        return sourceTime > outputTime;
    }

    /**
     * Build WASM module
     */
    function buildWasm() {
        try {
            console.log('🔨 Building WebAssembly module...');
            execSync(`node "${buildScript}" build`, {
                stdio: 'inherit',
                cwd: process.cwd()
            });
            lastBuildTime = Date.now();
            console.log('✅ WebAssembly build completed');
        } catch (error) {
            console.warn('⚠️  WebAssembly build failed:', error.message);
            console.warn('   Continuing without WASM module...');
        }
    }

    // Return the Vite plugin object
    return {
        name: 'wasm-build',
        configResolved(config) {
            // Build WASM on config resolution
            if (needsRebuild()) {
                buildWasm();
            }
        },
        buildStart() {
            // Build WASM at build start
            if (needsRebuild()) {
                buildWasm();
            }
        },
        configureServer(server) {
            if (pluginOptions.watch) {
                // Watch for changes to C++ source files
                const sourcePath = path.join(pluginOptions.sourceDir, pluginOptions.sourceFile);

                if (fs.existsSync(sourcePath)) {
                    server.watcher.add(sourcePath);

                    server.watcher.on('change', (file) => {
                        if (file === sourcePath) {
                            console.log('📝 C++ source file changed, rebuilding WASM...');
                            buildWasm();

                            // Reload the page to pick up new WASM module
                            server.ws.send({
                                type: 'full-reload'
                            });
                        }
                    });
                }
            }
        },
        generateBundle() {
            // Ensure WASM files are included in the build
            const wasmFiles = ['analytics.js', 'analytics.wasm', 'analytics.d.ts'];

            for (const file of wasmFiles) {
                const filePath = path.join(pluginOptions.outputDir, file);
                if (fs.existsSync(filePath)) {
                    this.emitFile({
                        type: 'asset',
                        fileName: `wasm/${file}`,
                        source: fs.readFileSync(filePath)
                    });
                }
            }
        }
    };
}

export default wasmBuildPlugin;
