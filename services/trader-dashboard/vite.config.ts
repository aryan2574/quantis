import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
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
  },
});
