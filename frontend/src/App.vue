<template>
  <div v-if="route.meta.hideLayout" class="auth-wrapper">
    <RouterView />
  </div>
  <div v-else class="app-shell" :class="{ 'sidebar-open': sidebarOpen }">
    <!-- Mobile overlay -->
    <div class="sidebar-overlay" @click="sidebarOpen = false" v-if="sidebarOpen"></div>

    <AppSidebar :collapsed="isSidebarCollapsed" :mobile-open="sidebarOpen" />

    <div class="app-main">
      <AppTopbar @toggle-sidebar="toggleSidebar" />
      <main class="app-content">
        <RouterView />
      </main>
    </div>
    <ToastContainer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTopbar  from '@/components/layout/AppTopbar.vue'
import ToastContainer from '@/components/common/ToastContainer.vue'

const route = useRoute()
const sidebarOpen = ref(false)
const desktopCollapsed = ref(false)
const windowWidth = ref(window.innerWidth)

const isTablet = computed(() => windowWidth.value <= 1024)
const isMobile = computed(() => windowWidth.value <= 640)

const isSidebarCollapsed = computed(() => {
  if (isTablet.value) return !sidebarOpen.value
  return desktopCollapsed.value
})

function toggleSidebar() {
  if (isTablet.value) {
    sidebarOpen.value = !sidebarOpen.value
  } else {
    desktopCollapsed.value = !desktopCollapsed.value
  }
}

function onResize() {
  windowWidth.value = window.innerWidth
  if (windowWidth.value > 640) sidebarOpen.value = false
}
onMounted(() => {
  window.addEventListener('resize', onResize)
  // Restore dark mode from localStorage
  if (localStorage.getItem('vnet-theme') === 'dark') {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})
onUnmounted(() => window.removeEventListener('resize', onResize))
</script>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background: var(--bg);
}
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}
.app-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 28px 32px;
  /* Subtle dot grid background */
  background-color: var(--bg);
  background-image: radial-gradient(var(--border) 1px, transparent 1px);
  background-size: 24px 24px;
}
.sidebar-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 99;
  animation: overlayIn 0.2s ease;
}
@keyframes overlayIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.auth-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

@media (max-width: 1024px) {
  .app-content { padding: 20px 24px; }
}
@media (max-width: 640px) {
  .app-content { padding: 16px; background-image: none; }
}
</style>
