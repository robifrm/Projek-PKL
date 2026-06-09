import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  test: {
    // Gunakan jsdom sebagai DOM environment (simulasi browser)
    environment: 'jsdom',
    // Global test helpers (describe, it, expect) tanpa import manual
    globals: true,
    // Tampilkan reporter yang verbose
    reporter: 'verbose',
    // Coverage configuration
    coverage: {
      provider: 'v8',
      include: ['src/**/*.{js,vue}'],
      exclude: ['src/main.js', 'src/router/**', 'node_modules/**']
    }
  }
})
