<template>
  <aside
    class="sidebar"
    :class="{
      'sidebar--collapsed': collapsed,
      'sidebar--mobile-open': mobileOpen,
    }"
  >
    <!-- Logo -->
    <div class="sidebar__logo">
      <RouterLink
        to="/dashboard"
        class="sidebar__logo-link"
        aria-label="Go to dashboard"
      >
        <img
          class="logo-full"
          :src="logoSrc"
          alt="VNet"
          @error="logoSrc = '/brand/vnet-logo.png'"
        />
        <img
          class="logo-icon"
          :src="iconSrc"
          alt="VNet"
          @error="iconSrc = '/brand/vnet-icon.svg'"
        />
      </RouterLink>
    </div>

    <!-- Nav -->
    <nav class="sidebar__nav">
      <RouterLink
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        class="nav-item"
        :class="{ 'nav-item--active': $route.path === item.to }"
        :data-tooltip="item.label"
      >
        <component :is="item.icon" class="nav-icon" />
        <span class="nav-label">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <!-- Bottom -->
    <div class="sidebar__bottom">
      <RouterLink to="/settings" class="nav-item" data-tooltip="Settings">
        <IconSettings class="nav-icon" /><span class="nav-label">Settings</span>
      </RouterLink>
      <RouterLink to="/support" class="nav-item" data-tooltip="Support">
        <IconSupport class="nav-icon" /><span class="nav-label">Support</span>
      </RouterLink>
      <a href="#" @click.prevent="logout" class="nav-item" data-tooltip="Logout" style="color: #ef4444;">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" class="nav-icon" style="color: #ef4444;">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
          <polyline points="16 17 21 12 16 7"></polyline>
          <line x1="21" y1="12" x2="9" y2="12"></line>
        </svg>
        <span class="nav-label">Logout</span>
      </a>
      <div class="user-chip">
        <div class="user-avatar">{{ userInitials }}</div>
        <div class="user-info nav-label">
          <div class="user-name">{{ user.nama }}</div>
          <div class="user-role">@{{ user.username }}</div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { defineComponent, h, ref, computed, onMounted, onBeforeUnmount } from "vue";
defineProps({ collapsed: Boolean, mobileOpen: Boolean });

const user = ref({
  nama: 'Admin User',
  username: 'admin',
  email: 'admin@victorynetwork.id'
});

function loadUserProfile() {
  const stored = localStorage.getItem("vnet_user");
  if (stored) {
    try {
      user.value = JSON.parse(stored);
    } catch (e) {
      // ignore
    }
  }
}

onMounted(() => {
  loadUserProfile();
  window.addEventListener('vnet-user-updated', loadUserProfile);
});

onBeforeUnmount(() => {
  window.removeEventListener('vnet-user-updated', loadUserProfile);
});

const userInitials = computed(() => {
  if (!user.value || !user.value.nama) return 'A';
  return user.value.nama.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase();
});

function logout() {
  if (confirm("Apakah Anda yakin ingin keluar dari sistem?")) {
    localStorage.removeItem("vnet_token");
    localStorage.removeItem("vnet_user");
    window.location.href = "/login";
  }
}

const logoSrc = ref("/brand/vnet-logo.png");
const iconSrc = ref("/brand/vnet-icon.png");

const mkI = (paths) =>
  defineComponent({
    render: () =>
      h(
        "svg",
        {
          viewBox: "0 0 24 24",
          fill: "none",
          stroke: "currentColor",
          "stroke-width": "1.7",
          "stroke-linecap": "round",
          "stroke-linejoin": "round",
        },
        paths.map((d) => h("path", { d })),
      ),
  });

const IconDashboard = mkI([
  "M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z",
  "M9 22V12h6v10",
]);
const IconDataImport = mkI([
  "M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4",
  "M17 8l-5-5-5 5",
  "M12 3v12",
]);
const IconDataPreview = mkI([
  "M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z",
  "M12 9a3 3 0 100 6 3 3 0 000-6z",
]);
const IconCustomers = mkI([
  "M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2",
  "M9 11a4 4 0 100-8 4 4 0 000 8z",
  "M23 21v-2a4 4 0 00-3-3.87",
  "M16 3.13a4 4 0 010 7.75",
]);
const IconAgent = mkI(["M18 20V10", "M12 20V4", "M6 20v-6"]);
const IconAddress = mkI([
  "M21 10c0 7-9 12-9 12S3 17 3 10a9 9 0 1118 0z",
  "M12 13a3 3 0 100-6 3 3 0 000 6z",
]);
const IconPackages = mkI([
  "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z",
  "M3.27 6.96L12 12.01l8.73-5.05",
  "M12 22.08V12"
]);
const IconSettings = mkI([
  "M12 15a3 3 0 100-6 3 3 0 000 6z",
  "M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z",
]);
const IconSupport = mkI([
  "M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z",
  "M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3",
  "M12 17h.01",
]);

const navItems = [
  { to: "/dashboard", label: "Dashboard", icon: IconDashboard },
  { to: "/packages", label: "Packages", icon: IconPackages },
  { to: "/data-import", label: "Data Import", icon: IconDataImport },
  { to: "/data-preview", label: "Data Preview", icon: IconDataPreview },
  { to: "/customers", label: "Customers", icon: IconCustomers },
  { to: "/agent-performance", label: "Agent Performance", icon: IconAgent },
  { to: "/address-insights", label: "Address Insights", icon: IconAddress },
];
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-w);
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative;
  z-index: 100;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1), transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border-right: 1px solid var(--sidebar-border);
}

/* Logo */
.sidebar__logo {
  padding: 18px 14px 20px;
  border-bottom: 1px solid var(--sidebar-border);
  display: flex;
  align-items: center;
  min-height: 72px;
}
.sidebar__logo-link {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 0;
}
.logo-full {
  width: 150px; height: 48px;
  display: block; flex-shrink: 0;
  object-fit: contain; object-position: left center;
}
.logo-icon {
  width: 34px; height: 34px;
  display: none; flex-shrink: 0;
  object-fit: contain; object-position: center;
}

/* Collapsed state */
.sidebar--collapsed { width: 60px; overflow: visible; }
.sidebar--collapsed .sidebar__logo { padding: 18px 13px 20px; }
.sidebar--collapsed .sidebar__logo-link { justify-content: center; }
.sidebar--collapsed .logo-full { display: none; }
.sidebar--collapsed .logo-icon { display: block; width: 30px; height: 30px; }
.sidebar--collapsed .nav-label { display: none; }
.sidebar--collapsed .nav-item { justify-content: center; padding: 10px; position: relative; }
.sidebar--collapsed .user-info { display: none; }
.sidebar--collapsed .user-chip { justify-content: center; padding: 8px; }

/* Premium Tooltip for collapsed sidebar */
.sidebar--collapsed .nav-item::after {
  content: attr(data-tooltip);
  position: absolute;
  left: calc(100% + 12px);
  top: 50%;
  transform: translateY(-50%) translateX(6px);
  background: var(--sidebar-bg);
  border: 1px solid var(--sidebar-border);
  box-shadow: var(--shadow-lg);
  padding: 7px 14px;
  border-radius: var(--r-sm);
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font-display);
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  z-index: 1000;
  pointer-events: none;
  color: var(--text-1);
}
.sidebar--collapsed .nav-item::before {
  content: '';
  position: absolute;
  left: calc(100% + 7px);
  top: 50%;
  transform: translateY(-50%) translateX(4px);
  border: 5px solid transparent;
  border-right-color: var(--navy);
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s;
  z-index: 1000;
  pointer-events: none;
}
.sidebar--collapsed .nav-item:hover::after,
.sidebar--collapsed .nav-item:hover::before {
  opacity: 1;
  visibility: visible;
  transform: translateY(-50%) translateX(0);
}

/* Mobile */
@media (max-width: 640px) {
  .sidebar {
    position: fixed; top: 0; left: 0; bottom: 0;
    width: 220px !important;
    transform: translateX(-100%);
    box-shadow: var(--shadow-xl);
  }
  .sidebar--mobile-open { transform: translateX(0); }
  .logo-full { display: block !important; }
  .logo-icon { display: none !important; }
  .nav-label { display: block !important; }
  .nav-item { justify-content: flex-start !important; padding: 9px 14px !important; }
  .user-info { display: block !important; }
  .user-chip { justify-content: flex-start !important; padding: 10px 14px !important; }
}

/* Nav */
.sidebar__nav {
  flex: 1;
  padding: 10px 8px;
  display: flex;
  flex-direction: column;
  gap: 1px;
  overflow-y: auto;
  overflow-x: hidden;
}
.sidebar__nav::-webkit-scrollbar { width: 0; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--r-sm);
  color: var(--sidebar-text);
  font-size: 13px;
  font-weight: 500;
  font-family: var(--font-body);
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}
.nav-item:hover {
  background: var(--sidebar-item-hover);
  color: var(--sidebar-text-active);
}
.nav-item--active {
  background: var(--sidebar-item-active);
  color: var(--sidebar-text-active);
  font-weight: 600;
}
/* Active indicator bar */
.nav-item--active::before {
  content: '';
  position: absolute;
  left: 0; top: 15%; bottom: 15%;
  width: 3px;
  background: linear-gradient(180deg, var(--teal), var(--navy));
  border-radius: 0 3px 3px 0;
  box-shadow: 0 0 8px var(--teal);
}

.nav-icon {
  width: 16px; height: 16px; flex-shrink: 0;
  transition: color 0.18s;
}

/* Bottom */
.sidebar__bottom {
  padding: 8px 8px 16px;
  border-top: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin-top: 8px;
  background: var(--sidebar-item-hover);
  border: 1px solid var(--sidebar-border);
  border-radius: var(--r-sm);
  transition: background 0.15s;
}
.user-chip:hover { background: var(--sidebar-item-active); }
.user-avatar {
  width: 30px; height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--gold) 0%, #F59E0B 100%);
  color: var(--navy);
  font-family: var(--font-display);
  font-weight: 800; font-size: 11px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
}
.user-name { font-size: 11px; font-weight: 600; color: var(--text-1); line-height: 1.3; }
.user-role { font-size: 9px; color: var(--text-3); margin-top: 1px; }
</style>
