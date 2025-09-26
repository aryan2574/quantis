#!/usr/bin/env node

/**
 * Emscripten Setup Script for Quantis Trading Dashboard
 * 
 * This script automatically installs and configures Emscripten SDK
 * for WebAssembly compilation on Windows, macOS, and Linux.
 */

import { execSync, spawn } from 'child_process';
import fs from 'fs';
import path from 'path';
import os from 'os';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

class EmscriptenSetup {
    constructor() {
        this.isWindows = process.platform === 'win32';
        this.isMacOS = process.platform === 'darwin';
        this.isLinux = process.platform === 'linux';
        this.homeDir = os.homedir();
        this.emsdkDir = path.join(this.homeDir, 'emsdk');
    }

    /**
     * Check if Emscripten is already installed
     */
    isEmscriptenInstalled() {
        try {
            const emccPath = this.isWindows ? 'emcc.bat' : 'emcc';
            execSync(`${emccPath} --version`, { stdio: 'ignore' });
            return true;
        } catch (error) {
            return false;
        }
    }

    /**
     * Check if emsdk directory exists
     */
    isEmsdkInstalled() {
        return fs.existsSync(this.emsdkDir);
    }

    /**
     * Install Git if not available (Windows)
     */
    async installGit() {
        if (this.isWindows) {
            try {
                execSync('git --version', { stdio: 'ignore' });
                console.log('✅ Git is already installed');
                return true;
            } catch (error) {
                console.log('❌ Git is not installed');
                console.log('Please install Git from: https://git-scm.com/download/win');
                console.log('Then run this script again.');
                return false;
            }
        }
        return true;
    }

    /**
     * Clone emsdk repository
     */
    async cloneEmsdk() {
        console.log('📥 Cloning Emscripten SDK...');

        if (this.isEmsdkInstalled()) {
            console.log('✅ Emscripten SDK directory already exists');
            return true;
        }

        try {
            const command = 'git';
            const args = ['clone', 'https://github.com/emscripten-core/emsdk.git', this.emsdkDir];

            await this.runCommand(command, args, { cwd: this.homeDir });
            console.log('✅ Emscripten SDK cloned successfully');
            return true;
        } catch (error) {
            console.error('❌ Failed to clone Emscripten SDK:', error.message);
            return false;
        }
    }

    /**
     * Install Emscripten SDK
     */
    async installEmscripten() {
        console.log('🔧 Installing Emscripten SDK...');

        try {
            // Install latest version
            await this.runCommand('./emsdk', ['install', 'latest'], { cwd: this.emsdkDir });

            // Activate latest version
            await this.runCommand('./emsdk', ['activate', 'latest'], { cwd: this.emsdkDir });

            console.log('✅ Emscripten SDK installed successfully');
            return true;
        } catch (error) {
            console.error('❌ Failed to install Emscripten SDK:', error.message);
            return false;
        }
    }

    /**
     * Setup environment variables
     */
    setupEnvironment() {
        console.log('🌍 Setting up environment variables...');

        const emsdkEnvScript = path.join(this.emsdkDir, this.isWindows ? 'emsdk_env.bat' : 'emsdk_env.sh');

        if (!fs.existsSync(emsdkEnvScript)) {
            console.error('❌ Emscripten environment script not found');
            return false;
        }

        // Create a setup script for the project
        const setupScript = this.isWindows ?
            `@echo off
call "${emsdkEnvScript}"
echo Emscripten environment activated
` :
            `#!/bin/bash
source "${emsdkEnvScript}"
echo "Emscripten environment activated"
`;

        const projectSetupScript = path.join(__dirname, '..', 'setup-emscripten-env.' + (this.isWindows ? 'bat' : 'sh'));
        fs.writeFileSync(projectSetupScript, setupScript);

        if (!this.isWindows) {
            fs.chmodSync(projectSetupScript, '755');
        }

        console.log(`✅ Environment setup script created: ${projectSetupScript}`);
        return true;
    }

    /**
     * Test Emscripten installation
     */
    async testInstallation() {
        console.log('🧪 Testing Emscripten installation...');

        try {
            // Source the environment and test emcc
            const testCommand = this.isWindows ?
                `call "${path.join(this.emsdkDir, 'emsdk_env.bat')}" && emcc --version` :
                `source "${path.join(this.emsdkDir, 'emsdk_env.sh')}" && emcc --version`;

            const output = execSync(testCommand, {
                encoding: 'utf8',
                shell: true,
                cwd: this.emsdkDir
            });

            console.log('✅ Emscripten test successful:');
            console.log(output.trim());
            return true;
        } catch (error) {
            console.error('❌ Emscripten test failed:', error.message);
            return false;
        }
    }

    /**
     * Run a command with promise
     */
    runCommand(command, args, options = {}) {
        return new Promise((resolve, reject) => {
            const process = spawn(command, args, {
                stdio: 'inherit',
                shell: this.isWindows,
                ...options
            });

            process.on('close', (code) => {
                if (code === 0) {
                    resolve();
                } else {
                    reject(new Error(`Command failed with code ${code}`));
                }
            });

            process.on('error', (error) => {
                reject(error);
            });
        });
    }

    /**
     * Main setup process
     */
    async setup() {
        console.log('🚀 Setting up Emscripten for WebAssembly compilation...');
        console.log(`   Platform: ${process.platform}`);
        console.log(`   Home Directory: ${this.homeDir}`);
        console.log(`   Emscripten SDK: ${this.emsdkDir}`);

        // Check if already installed
        if (this.isEmscriptenInstalled()) {
            console.log('✅ Emscripten is already installed and available');
            return true;
        }

        // Install Git if needed
        if (!(await this.installGit())) {
            return false;
        }

        // Clone emsdk
        if (!(await this.cloneEmsdk())) {
            return false;
        }

        // Install Emscripten
        if (!(await this.installEmscripten())) {
            return false;
        }

        // Setup environment
        if (!this.setupEnvironment()) {
            return false;
        }

        // Test installation
        if (!(await this.testInstallation())) {
            return false;
        }

        console.log('🎉 Emscripten setup completed successfully!');
        console.log('');
        console.log('Next steps:');
        console.log('1. Restart your terminal/command prompt');
        console.log('2. Run: npm run build:wasm:check');
        console.log('3. Run: npm run dev');
        console.log('');
        console.log('Note: You may need to run the environment setup script before building:');
        console.log(`   ${this.isWindows ? 'setup-emscripten-env.bat' : './setup-emscripten-env.sh'}`);

        return true;
    }
}

// CLI interface
if (import.meta.url === `file://${process.argv[1]}`) {
    const setup = new EmscriptenSetup();
    setup.setup().catch((error) => {
        console.error('❌ Setup failed:', error.message);
        process.exit(1);
    });
}

export default EmscriptenSetup;
