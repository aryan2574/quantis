#!/usr/bin/env node

/**
 * Automated WebAssembly Build Script for Quantis Analytics
 * 
 * This script automatically compiles the C++ analytics code to WebAssembly
 * and integrates it into the Vite build process.
 */

import { execSync, spawn } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Configuration
const CONFIG = {
    sourceDir: path.join(__dirname, '../src/lib/wasm'),
    outputDir: path.join(__dirname, '../public/wasm'),
    sourceFile: 'analytics.cpp',
    outputJs: 'analytics.js',
    outputWasm: 'analytics.wasm',
    outputDts: 'analytics.d.ts'
};

// Emscripten compiler flags
const EMCC_FLAGS = [
    '-std=c++17',
    '-O3',
    '-s', 'WASM=1',
    '-s', 'EXPORTED_RUNTIME_METHODS=["ccall","cwrap"]',
    '-s', 'ALLOW_MEMORY_GROWTH=1',
    '-s', 'MODULARIZE=1',
    '-s', 'EXPORT_NAME=AnalyticsModule',
    '-s', 'EXPORT_ES6=1',
    '--bind',
    '-s', 'NO_EXIT_RUNTIME=1'
];

class WasmBuilder {
    constructor() {
        this.isWindows = process.platform === 'win32';
        this.emccPath = this.findEmccPath();
    }

    /**
     * Find Emscripten compiler path
     */
    findEmccPath() {
        try {
            // Try to find emcc in PATH
            const emccPath = this.isWindows ? 'emcc.bat' : 'emcc';
            execSync(`${emccPath} --version`, { stdio: 'ignore' });
            return emccPath;
        } catch (error) {
            // Try common Emscripten installation paths
            const commonPaths = [
                path.join(process.env.HOME || process.env.USERPROFILE, 'emsdk', 'upstream', 'emscripten', 'emcc'),
                path.join(process.env.HOME || process.env.USERPROFILE, 'emsdk', 'upstream', 'emscripten', 'emcc.bat'),
                '/usr/local/bin/emcc',
                '/opt/emsdk/upstream/emscripten/emcc'
            ];

            for (const emccPath of commonPaths) {
                if (fs.existsSync(emccPath)) {
                    return emccPath;
                }
            }

            throw new Error(
                'Emscripten not found. Please install Emscripten SDK:\n' +
                '1. git clone https://github.com/emscripten-core/emsdk.git\n' +
                '2. cd emsdk && ./emsdk install latest && ./emsdk activate latest\n' +
                '3. source ./emsdk_env.sh (or emsdk_env.bat on Windows)'
            );
        }
    }

    /**
     * Check if Emscripten is available
     */
    checkEmscripten() {
        try {
            const version = execSync(`${this.emccPath} --version`, { encoding: 'utf8' });
            console.log('✅ Emscripten found:', version.trim());
            return true;
        } catch (error) {
            console.error('❌ Emscripten not found:', error.message);
            return false;
        }
    }

    /**
     * Create output directory if it doesn't exist
     */
    ensureOutputDir() {
        if (!fs.existsSync(CONFIG.outputDir)) {
            fs.mkdirSync(CONFIG.outputDir, { recursive: true });
            console.log(`📁 Created output directory: ${CONFIG.outputDir}`);
        }
    }

    /**
     * Check if source file exists
     */
    checkSourceFile() {
        const sourcePath = path.join(CONFIG.sourceDir, CONFIG.sourceFile);
        if (!fs.existsSync(sourcePath)) {
            throw new Error(`Source file not found: ${sourcePath}`);
        }
        console.log(`📄 Source file found: ${sourcePath}`);
    }

    /**
     * Check if rebuild is needed
     */
    needsRebuild() {
        const sourcePath = path.join(CONFIG.sourceDir, CONFIG.sourceFile);
        const outputJsPath = path.join(CONFIG.outputDir, CONFIG.outputJs);
        const outputWasmPath = path.join(CONFIG.outputDir, CONFIG.outputWasm);

        if (!fs.existsSync(outputJsPath) || !fs.existsSync(outputWasmPath)) {
            return true;
        }

        const sourceTime = fs.statSync(sourcePath).mtime;
        const outputTime = Math.max(
            fs.statSync(outputJsPath).mtime,
            fs.statSync(outputWasmPath).mtime
        );

        return sourceTime > outputTime;
    }

    /**
     * Compile C++ to WebAssembly
     */
    async compileWasm() {
        const sourcePath = path.join(CONFIG.sourceDir, CONFIG.sourceFile);
        const outputJsPath = path.join(CONFIG.outputDir, CONFIG.outputJs);

        console.log('🔨 Compiling WebAssembly module...');
        console.log(`   Source: ${sourcePath}`);
        console.log(`   Output: ${outputJsPath}`);

        const command = this.emccPath;
        const args = [
            ...EMCC_FLAGS,
            sourcePath,
            '-o', outputJsPath
        ];

        return new Promise((resolve, reject) => {
            const process = spawn(command, args, {
                stdio: 'inherit',
                shell: this.isWindows
            });

            process.on('close', (code) => {
                if (code === 0) {
                    console.log('✅ WebAssembly compilation successful');
                    resolve();
                } else {
                    reject(new Error(`Compilation failed with code ${code}`));
                }
            });

            process.on('error', (error) => {
                reject(new Error(`Failed to start compilation: ${error.message}`));
            });
        });
    }

    /**
     * Generate TypeScript declaration file
     */
    generateTypeDeclarations() {
        const dtsPath = path.join(CONFIG.outputDir, CONFIG.outputDts);

        const dtsContent = `// Auto-generated TypeScript declarations for WebAssembly analytics module
declare module "/wasm/analytics.js" {
  interface AnalyticsModule {
    calculatePortfolioMetrics(
      returns: number[],
      portfolioValues: number[],
      riskFreeRate?: number
    ): {
      sharpeRatio: number;
      maxDrawdown: number;
      volatility: number;
      var: number;
      winRate: number;
      profitFactor: number;
      riskScore: number;
    };

    predictPrice(
      historicalPrices: number[],
      period?: number
    ): {
      predictedPrice: number;
      confidence: number;
      trend: "bullish" | "bearish" | "neutral";
    };

    assessRisk(
      returns: number[],
      portfolioValue: number,
      leverage?: number
    ): {
      riskScore: number;
      riskLevel: "low" | "medium" | "high" | "critical";
      recommendations: string[];
    };
  }

  const AnalyticsModule: () => Promise<AnalyticsModule>;
  export default AnalyticsModule;
}
`;

        fs.writeFileSync(dtsPath, dtsContent);
        console.log(`📝 Generated TypeScript declarations: ${dtsPath}`);
    }

    /**
     * Main build process
     */
    async build() {
        try {
            console.log('🚀 Starting WebAssembly build process...');

            // Check prerequisites
            if (!this.checkEmscripten()) {
                throw new Error('Emscripten not available');
            }

            this.checkSourceFile();
            this.ensureOutputDir();

            // Check if rebuild is needed
            if (!this.needsRebuild()) {
                console.log('⏭️  WebAssembly module is up to date, skipping compilation');
                return;
            }

            // Compile WebAssembly
            await this.compileWasm();

            // Generate TypeScript declarations
            this.generateTypeDeclarations();

            console.log('🎉 WebAssembly build completed successfully!');
            console.log(`   JavaScript: ${path.join(CONFIG.outputDir, CONFIG.outputJs)}`);
            console.log(`   WebAssembly: ${path.join(CONFIG.outputDir, CONFIG.outputWasm)}`);
            console.log(`   TypeScript: ${path.join(CONFIG.outputDir, CONFIG.outputDts)}`);

        } catch (error) {
            console.error('❌ WebAssembly build failed:', error.message);
            process.exit(1);
        }
    }

    /**
     * Clean build artifacts
     */
    clean() {
        console.log('🧹 Cleaning WebAssembly build artifacts...');

        const filesToRemove = [
            path.join(CONFIG.outputDir, CONFIG.outputJs),
            path.join(CONFIG.outputDir, CONFIG.outputWasm),
            path.join(CONFIG.outputDir, CONFIG.outputDts)
        ];

        for (const file of filesToRemove) {
            if (fs.existsSync(file)) {
                fs.unlinkSync(file);
                console.log(`   Removed: ${file}`);
            }
        }

        console.log('✅ Clean completed');
    }
}

// CLI interface
if (import.meta.url === `file://${process.argv[1]}`) {
    const builder = new WasmBuilder();
    const command = process.argv[2];

    switch (command) {
        case 'build':
            builder.build();
            break;
        case 'clean':
            builder.clean();
            break;
        case 'check':
            builder.checkEmscripten();
            break;
        default:
            console.log('Usage: node build-wasm.js [build|clean|check]');
            console.log('  build  - Compile WebAssembly module (default)');
            console.log('  clean  - Remove build artifacts');
            console.log('  check  - Check if Emscripten is available');
            process.exit(1);
    }
}

export default WasmBuilder;
