<template>
  <header class="topbar">
    <div class="topbar__left">
      <!-- Hamburger (mobile/tablet) -->
      <button
        class="hamburger"
        @click="$emit('toggle-sidebar')"
        aria-label="Toggle menu"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        >
          <line x1="3" y1="6" x2="21" y2="6" />
          <line x1="3" y1="12" x2="21" y2="12" />
          <line x1="3" y1="18" x2="21" y2="18" />
        </svg>
      </button>
      <div class="topbar__search">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.7"
          stroke-linecap="round"
        >
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          v-model="searchVal"
          @input="handleSearch"
          :placeholder="searchPlaceholder"
        />
      </div>
    </div>

    <div class="topbar__right">
      <div class="status-pill premium-badge">
        <span class="status-dot"></span>
        <span class="status-text">LIVE SYNCING</span>
      </div>
      <!-- Dark Mode Toggle -->
      <button class="topbar__darkbtn" @click="toggleDark" :title="isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'">
        <svg v-if="isDark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
          <circle cx="12" cy="12" r="5"/>
          <line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/>
          <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
          <line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/>
          <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
          <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/>
        </svg>
      </button>
      <div class="bell-container">
        <button class="topbar__bell" @click="toggleNotifications">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.7"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 01-3.46 0" />
          </svg>
          <span v-if="unreadCount" class="bell-badge">{{ unreadCount }}</span>
        </button>
        <Transition name="dropdown">
          <div v-if="showNotifications" class="bell-dropdown glass-card">
            <div class="dropdown-header">
              <span>System Notifications</span>
              <button class="dropdown-clear" @click="markNotificationsRead">
                Mark read
              </button>
            </div>
            <div class="dropdown-list">
              <div
                class="dropdown-item"
                :class="item.type"
                v-for="item in notifications"
                :key="item.id"
                @click="goToNotification(item)"
              >
                <span class="dot"></span>
                <div class="item-content">
                  <p class="title">{{ item.title }}</p>
                  <p class="desc">{{ item.desc }}</p>
                </div>
              </div>
              <div class="dropdown-empty" v-if="notifications.length === 0">
                All clear.
              </div>
            </div>
          </div>
        </Transition>
      </div>
      <div class="profile-container">
        <button
          class="topbar__avatar"
          :title="adminName"
          @click="toggleProfile"
        >
          {{ userInitials }}
        </button>
        <Transition name="dropdown">
          <div v-if="showProfile" class="profile-dropdown glass-card">
            <div class="profile-head">
              <div class="profile-avatar">{{ userInitials }}</div>
              <div class="profile-meta">
                <div class="profile-name">{{ adminName }}</div>
                <div class="profile-email">{{ adminEmail }}</div>
              </div>
            </div>
            <div class="profile-list">
              <button @click="router.push('/settings')">
                Account Settings
              </button>
              <button @click="router.push('/data-import')">
                Import Center
              </button>
              <button class="profile-danger" @click="logout">Logout</button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getDashboardOverview, getImportStats } from "@/services/api";

defineEmits(["toggle-sidebar"]);
const route = useRoute();
const router = useRouter();

const searchVal = ref("");
const isDark = ref(localStorage.getItem('vnet-theme') === 'dark');
const userInitials = ref("VA");
const adminName = ref("Super Admin VNet");
const adminEmail = ref("admin@victorynetwork.id");
const showNotifications = ref(false);
const showProfile = ref(false);
const notifications = ref([]);
const notificationsRead = ref(
  localStorage.getItem("vnet_notifications_read") === "1",
);
const unreadCount = computed(() =>
  notificationsRead.value ? 0 : notifications.value.length,
);

const searchPlaceholder = computed(
  () =>
    ({
      "/dashboard": "Search network assets...",
      "/data-import": "Search system logs...",
      "/data-preview": "Search batch data...",
      "/customers": "Search customers...",
      "/agent-performance": "Search analytics...",
      "/address-insights": "Search address insights...",
    })[route.path] || "Search...",
);

const handleSearch = () => {
  window.dispatchEvent(
    new CustomEvent("topbar-search", { detail: searchVal.value }),
  );
};

// Clear search input when navigating to a different page
watch(
  () => route.path,
  () => {
    searchVal.value = "";
  },
);

function toggleDark() {
  isDark.value = !isDark.value;
  document.documentElement.classList.toggle('dark', isDark.value);
  localStorage.setItem('vnet-theme', isDark.value ? 'dark' : 'light');
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value;
  showProfile.value = false;
};

const toggleProfile = () => {
  showProfile.value = !showProfile.value;
  showNotifications.value = false;
};

// ── Click-outside to close dropdowns ─────────────────────────────────────────
function handleDocClick(e) {
  const bellContainer = document.querySelector('.bell-container');
  const profileContainer = document.querySelector('.profile-container');
  if (bellContainer && !bellContainer.contains(e.target)) {
    showNotifications.value = false;
  }
  if (profileContainer && !profileContainer.contains(e.target)) {
    showProfile.value = false;
  }
}

function loadUserProfile() {
  try {
    const rawUser = localStorage.getItem("vnet_user");
    if (rawUser) {
      const user = JSON.parse(rawUser);
      if (user && user.nama) {
        adminName.value = user.nama;
        adminEmail.value = user.email || adminEmail.value;
        const words = user.nama.trim().split(/\s+/);
        if (words.length >= 2) {
          userInitials.value = (words[0][0] + words[1][0]).toUpperCase();
        } else if (words.length === 1 && words[0].length > 0) {
          userInitials.value = words[0].substring(0, 2).toUpperCase();
        }
      }
    }
  } catch (err) {
    console.error("Failed to parse user initials", err);
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleDocClick);
  window.addEventListener('vnet-user-updated', loadUserProfile);
  loadUserProfile();
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocClick);
  window.removeEventListener('vnet-user-updated', loadUserProfile);
});
// ─────────────────────────────────────────────────────────────────────────────

const markNotificationsRead = () => {
  notificationsRead.value = true;
  localStorage.setItem("vnet_notifications_read", "1");
};

const goToNotification = (item) => {
  if (item.to) router.push(item.to);
  showNotifications.value = false;
};

const logout = () => {
  localStorage.removeItem("vnet_token");
  localStorage.removeItem("vnet_user");
  router.push("/login");
};

async function loadNotifications() {
  try {
    const [overview, importStats] = await Promise.all([
      getDashboardOverview(),
      getImportStats(),
    ]);
    const summary = overview.summary || {};
    const items = [];

    if (Number(summary.totalIsolir || 0) > 0) {
      items.push({
        id: "isolir",
        type: "info",
        title: `${summary.totalIsolir} customers need follow-up`,
        desc: `Isolir rate ${summary.isolirRate || 0}% dari total pelanggan.`,
        to: "/customers",
      });
    }
    if (Number(summary.monthlyNewCustomers || 0) > 0) {
      items.push({
        id: "monthly-new",
        type: "success",
        title: `${summary.monthlyNewCustomers} new customers`,
        desc: `Masuk pada periode ${summary.periodMonthLabel || "bulan ini"}.`,
        to: "/dashboard",
      });
    }
    if (Number(importStats.totalErrorRows || 0) > 0) {
      items.push({
        id: "import-errors",
        type: "system",
        title: `${importStats.totalErrorRows} import rows need review`,
        desc:
          importStats.lastActivityDetail || "Cek data preview/import terakhir.",
        to: "/data-import",
      });
    }
    if (items.length === 0) {
      items.push({
        id: "healthy",
        type: "success",
        title: "System healthy",
        desc: "Customer, import, and dashboard data are synced.",
        to: "/dashboard",
      });
    }

    notifications.value = items.slice(0, 4);
    if (items.length) {
      notificationsRead.value = false;
      localStorage.removeItem("vnet_notifications_read");
    }
  } catch (error) {
    notifications.value = [
      {
        id: "offline",
        type: "system",
        title: "API connection pending",
        desc: "Login atau jalankan backend untuk memuat notifikasi live.",
        to: "/login",
      },
    ];
  }
}

onMounted(() => {
  loadNotifications();
});
</script>

<style scoped>
.topbar {
  height: var(--topbar-h);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  gap: 12px;
  flex-shrink: 0;
  position: relative;
  z-index: 50;
}
.topbar__left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.hamburger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--r-sm);
  background: none;
  border: none;
  color: var(--text-3);
  flex-shrink: 0;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.hamburger svg {
  width: 18px;
  height: 18px;
}
.hamburger:hover {
  background: var(--bg);
  color: var(--text-1);
}

.topbar__search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 0 14px;
  flex: 1;
  max-width: 360px;
  height: 38px;
  transition: all var(--transition-fast);
}
.topbar__search:focus-within {
  background: var(--surface);
  border-color: var(--navy);
  box-shadow: 0 0 0 3px rgba(13, 19, 64, 0.07);
}
.topbar__search svg {
  width: 14px;
  height: 14px;
  color: var(--text-3);
  flex-shrink: 0;
}
.topbar__search input {
  background: none;
  border: none;
  outline: none;
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-1);
  width: 100%;
}
.topbar__search input::placeholder {
  color: var(--text-3);
}

.topbar__right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.bell-container,
.profile-container {
  position: relative;
}

/* Dark Mode Toggle Button */
.topbar__darkbtn {
  width: 38px; height: 38px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: var(--surface-2);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-2);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.topbar__darkbtn:hover {
  background: var(--navy-glow);
  color: var(--navy);
  border-color: var(--navy-light);
}
.topbar__darkbtn svg { width: 16px; height: 16px; }

/* Live status pill */
.premium-badge {
  background: var(--green-bg);
  border: 1px solid rgba(16, 185, 129, 0.2);
  padding: 5px 11px;
  border-radius: 99px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all var(--transition-base);
}
.premium-badge .status-text {
  color: var(--green-ok);
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.8px;
  font-family: var(--font-display);
}
.status-dot {
  width: 6px;
  height: 6px;
  background: var(--green-ok);
  border-radius: 50%;
  animation: pulse-live 2s infinite;
  flex-shrink: 0;
}
@keyframes pulse-live {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(16, 185, 129, 0);
  }
}

/* Bell button */
.topbar__bell {
  position: relative;
  background: none;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  transition: all var(--transition-fast);
  cursor: pointer;
}
.topbar__bell:hover {
  background: var(--bg);
  color: var(--text-1);
}
.topbar__bell svg {
  width: 18px;
  height: 18px;
}
.bell-badge {
  position: absolute;
  top: 3px;
  right: 3px;
  min-width: 16px;
  height: 16px;
  background: var(--red-warn);
  color: #fff;
  font-size: 8px;
  font-weight: 700;
  border-radius: 99px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  border: 2px solid var(--surface);
}

/* Notification Dropdown */
.bell-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 300px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-lg);
  padding: 14px;
  z-index: 1000;
}
.dropdown-header {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 800;
  color: var(--text-3);
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 1px solid var(--border);
  padding-bottom: 10px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.dropdown-clear {
  border: 1px solid var(--border);
  background: var(--surface-2);
  color: var(--text-2);
  border-radius: var(--r-xs);
  padding: 3px 8px;
  font-size: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.dropdown-clear:hover {
  background: var(--surface-3);
  color: var(--text-1);
}
.dropdown-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.dropdown-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 9px 10px;
  border-radius: var(--r-sm);
  transition: background var(--transition-fast);
  text-align: left;
  cursor: pointer;
}
.dropdown-item:hover {
  background: var(--surface-2);
}
.dropdown-item .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
}
.dropdown-item.info .dot {
  background: var(--teal-light);
}
.dropdown-item.success .dot {
  background: var(--green-ok);
}
.dropdown-item.system .dot {
  background: var(--gold);
}
.item-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.item-content .title {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0;
}
.item-content .desc {
  font-size: 11px;
  color: var(--text-3);
  margin: 0;
  line-height: 1.4;
}
.dropdown-empty {
  color: var(--text-3);
  font-size: 12px;
  padding: 12px 8px;
  text-align: center;
}

/* Profile Dropdown */
.topbar__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid var(--border);
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(13, 19, 64, 0.15);
}
.topbar__avatar:hover {
  border-color: var(--navy);
  box-shadow: 0 4px 12px rgba(13, 19, 64, 0.25);
  transform: scale(1.04);
}

.profile-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 240px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-xl);
  z-index: 1000;
  overflow: hidden;
}
.profile-head {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(135deg, var(--surface-2) 0%, var(--surface) 100%);
}
.profile-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--navy) 0%, var(--navy-mid) 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(13, 19, 64, 0.2);
}
.profile-meta {
  min-width: 0;
}
.profile-name {
  color: var(--text-1);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: var(--font-display);
}
.profile-email {
  color: var(--text-3);
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}
.profile-list {
  display: flex;
  flex-direction: column;
  padding: 8px;
}
.profile-list button {
  border: none;
  background: transparent;
  color: var(--text-1);
  text-align: left;
  padding: 9px 12px;
  border-radius: var(--r-sm);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: background var(--transition-fast);
}
.profile-list button:hover {
  background: var(--bg);
}
.profile-list .profile-danger {
  color: var(--red-warn);
}
.profile-list .profile-danger:hover {
  background: var(--red-bg);
}

/* Dropdown Transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.22s cubic-bezier(0.4, 0, 0.2, 1);
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.97);
}

@media (max-width: 640px) {
  .status-text {
    display: none;
  }
  .premium-badge {
    padding: 5px 7px;
  }
  .topbar__search {
    max-width: none;
  }
}
</style>
