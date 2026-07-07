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

    <!-- Global Logout Confirm Modal -->
    <div class="modal-backdrop" v-if="showLogoutModal" @click.self="showLogoutModal = false" style="z-index: 9999;">
      <div class="logout-modal-card">
        <div class="logout-modal-icon-wrap">
          <div class="logout-modal-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="28" height="28">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </div>
        </div>
        <div class="logout-modal-body">
          <div class="logout-modal-title">Keluar dari Sistem?</div>
          <div class="logout-modal-desc">Sesi Anda akan diakhiri. Anda perlu login kembali untuk mengakses dashboard.</div>
        </div>
        <div class="logout-modal-footer">
          <button class="btn btn--secondary" @click="showLogoutModal = false">Batal</button>
          <button class="btn btn--danger" @click="confirmLogout">Ya, Keluar</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTopbar  from '@/components/layout/AppTopbar.vue'
import ToastContainer from '@/components/common/ToastContainer.vue'
import { logout as apiLogout } from '@/services/api'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)
const desktopCollapsed = ref(false)
const windowWidth = ref(window.innerWidth)

const isTablet = computed(() => windowWidth.value <= 1024)
const isMobile = computed(() => windowWidth.value <= 640)

const isSidebarCollapsed = computed(() => {
  if (isTablet.value) return !sidebarOpen.value
  return desktopCollapsed.value
})

const showLogoutModal = ref(false)

function toggleSidebar() {
  if (isTablet.value) {
    sidebarOpen.value = !sidebarOpen.value
  } else {
    desktopCollapsed.value = !desktopCollapsed.value
  }
}

function handleRequestLogout() {
  showLogoutModal.value = true
}

async function confirmLogout() {
  showLogoutModal.value = false
  try {
    await apiLogout()
  } catch (err) {
    console.error("Gagal membersihkan sesi di server:", err)
  } finally {
    localStorage.removeItem("vnet_token")
    localStorage.removeItem("vnet_user")
    router.push("/login")
  }
}

function onResize() {
  windowWidth.value = window.innerWidth
  if (windowWidth.value > 640) sidebarOpen.value = false
}
onMounted(() => {
  window.addEventListener('resize', onResize)
  window.addEventListener('request-logout', handleRequestLogout)
  // Restore dark mode from localStorage
  if (localStorage.getItem('vnet-theme') === 'dark') {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('request-logout', handleRequestLogout)
})
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
  min-height: 100vh;
  width: 100vw;
  overflow-y: auto;
}

/* Modal CSS */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.modal-card {
  width: 100%;
  background: var(--surface);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-xl);
  display: flex;
  flex-direction: column;
  animation: modalIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes modalIn {
  from { opacity: 0; transform: translateY(15px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.modal-body {
  flex: 1;
  overflow-y: auto;
}
.modal-footer {
  display: flex;
  align-items: center;
}

@media (max-width: 1024px) {
  .app-content { padding: 20px 24px; }
}
@media (max-width: 640px) {
  .app-content { padding: 16px; background-image: none; }
}

/* ── Logout Modal (matches project delete modal style) ── */
.logout-modal-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  overflow: hidden;
  text-align: center;
  animation: popIn 0.2s ease-out;
}
@keyframes popIn {
  from { transform: scale(0.95); opacity: 0; }
  to   { transform: scale(1);    opacity: 1; }
}
.logout-modal-icon-wrap {
  padding: 28px 20px 16px;
  display: flex;
  justify-content: center;
}
.logout-modal-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #FEF2F2;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ef4444;
}
.logout-modal-body {
  padding: 0 24px 20px;
}
.logout-modal-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 800;
  color: var(--text-1);
  margin-bottom: 8px;
}
.logout-modal-desc {
  font-size: 13.5px;
  color: var(--text-2);
  line-height: 1.6;
}
.logout-modal-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: center;
  gap: 10px;
}
.logout-modal-footer .btn {
  min-width: 110px;
}
</style>
