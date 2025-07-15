import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from "path"

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
      alias: {
        "@hooks": path.resolve(__dirname, "src/hooks"),
        "@contexts": path.resolve(__dirname, "src/contexts"),
        "@components": path.resolve(__dirname, "src/components"),
        "@assets": path.resolve(__dirname, "src/assets"),
        "@utils": path.resolve(__dirname, "src/utils"),
        "@features": path.resolve(__dirname, "src/features"),
        "@queries": path.resolve(__dirname, "src/queries")
      }
  }
})
