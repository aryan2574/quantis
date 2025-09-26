import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import wasmBuildPlugin from "./scripts/vite-wasm-plugin.js";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    wasmBuildPlugin({
      sourceDir: "src/lib/wasm",
      outputDir: "public/wasm",
      sourceFile: "analytics.cpp",
      watch: true,
    }),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 3000,
    proxy: {
      "/graphql": {
        target: "http://localhost:8085",
        changeOrigin: true,
      },
      "/ws": {
        target: "ws://localhost:8081",
        ws: true,
      },
    },
  },
  // WebAssembly support
  optimizeDeps: {
    exclude: ["/wasm/analytics.js"],
  },
  build: {
    rollupOptions: {
      external: ["/wasm/analytics.js"],
    },
    // Ensure WASM files are copied to dist
    copyPublicDir: true,
  },
  // Enable WASM support
  define: {
    global: "globalThis",
  },
  // Handle WASM files properly
  assetsInclude: ["**/*.wasm"],
});
