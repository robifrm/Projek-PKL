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
      <div class="confirm-dialog">
        <div class="confirm-dialog__icon confirm-dialog__icon--logout">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </div>
        <div class="confirm-dialog__body">
          <h2 class="confirm-dialog__title">Keluar dari Sistem?</h2>
          <p class="confirm-dialog__desc">Sesi Anda akan diakhiri dan Anda perlu login kembali untuk mengakses dashboard.</p>
        </div>
        <div class="confirm-dialog__actions">
          <button class="confirm-dialog__btn confirm-dialog__btn--cancel" @click="showLogoutModal = false">Batal</button>
          <button class="confirm-dialog__btn confirm-dialog__btn--danger" @click="confirmLogout">Ya, Keluar</button>
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
  height: 100vh;
  width: 100vw;
  overflow: hidden;
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

/* ── Clean Confirm Dialog ─────────────────────────────── */
.confirm-dialog {
  width: 100%;
  max-width: 380px;
  background: var(--surface);
  border-radius: 20px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18), 0 4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 32px 28px 24px;
  gap: 16px;
  animation: modalIn 0.28s cubic-bezier(0.16, 1, 0.3, 1);
}
.confirm-dialog__icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.confirm-dialog__icon svg {
  width: 24px;
  height: 24px;
}
.confirm-dialog__icon--logout {
  background: rgba(231, 76, 60, 0.08);
  color: var(--red-warn, #e74c3c);
}
.confirm-dialog__icon--delete {
  background: rgba(231, 76, 60, 0.08);
  color: var(--red-warn, #e74c3c);
}
.confirm-dialog__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.confirm-dialog__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
  font-family: var(--font-display);
  letter-spacing: -0.2px;
}
.confirm-dialog__desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-3);
  line-height: 1.6;
}
.confirm-dialog__name {
  font-weight: 600;
  color: var(--text-1);
}
.confirm-dialog__actions {
  display: flex;
  gap: 10px;
  width: 100%;
  margin-top: 4px;
}
.confirm-dialog__btn {
  flex: 1;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: background 0.15s, opacity 0.15s, transform 0.1s;
  font-family: var(--font-display);
  letter-spacing: 0.1px;
}
.confirm-dialog__btn:active { transform: scale(0.97); }
.confirm-dialog__btn--cancel {
  background: var(--surface-2, #f1f5f9);
  color: var(--text-2);
}
.confirm-dialog__btn--cancel:hover { background: var(--border); }
.confirm-dialog__btn--danger {
  background: var(--red-warn, #e74c3c);
  color: #fff;
}
.confirm-dialog__btn--danger:hover { opacity: 0.88; }
</style>
