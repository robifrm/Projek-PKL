<template>
  <div class="settings">
    <div class="page-header">
      <div>
        <h1 class="page-title">Settings</h1>
        <p class="page-sub">
          Manage your account, system preferences, and VNet configuration.
        </p>
      </div>
      <button class="btn btn--primary" @click="saveAll">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        >
          <path
            d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z"
          />
          <polyline points="17 21 17 13 7 13 7 21" />
          <polyline points="7 3 7 8 15 8" />
        </svg>
        Save Changes
      </button>
    </div>

    <div class="settings-layout">
      <!-- Sidebar Nav -->
      <div class="settings-nav card">
        <button
          v-for="section in filteredSections"
          :key="section.key"
          class="snav-item"
          :class="{ 'snav-item--active': activeSection === section.key }"
          @click="activeSection = section.key"
        >
          <component :is="section.icon" class="snav-icon" />
          <div class="snav-text">
            <span class="snav-label">{{ section.label }}</span>
            <span class="snav-sub">{{ section.sub }}</span>
          </div>
        </button>
      </div>

      <!-- Content -->
      <div class="settings-content">
        <!-- Profile -->
        <div v-show="activeSection === 'profile'" class="settings-section card">
          <div class="section-title">Profile Information</div>
          <div class="section-sub">
            Perbarui data profil Anda langsung ke database.
          </div>

          <div class="avatar-row">
            <div class="avatar-big">{{ avatarInitials }}</div>
            <div>
              <div class="avatar-name">{{ profile.name }}</div>
              <div class="avatar-role">
                {{ profile.role }}
              </div>
            </div>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">Full Name</label>
              <input
                class="form-input"
                v-model="profile.name"
                placeholder="Full Name"
              />
            </div>
            <div class="form-group">
              <label class="form-label">Username</label>
              <input
                class="form-input"
                v-model="profile.username"
                placeholder="Username"
              />
            </div>
            <div class="form-group" style="grid-column: 1/-1">
              <label class="form-label">Role</label>
              <input
                class="form-input"
                v-model="profile.role"
                disabled
                style="opacity: 0.7; cursor: not-allowed; background: var(--bg);"
              />
            </div>
          </div>
        </div>

        <!-- System -->
        <div v-show="activeSection === 'system'" class="settings-section card">
          <div class="section-title">System Configuration</div>
          <div class="section-sub">
            Database connections, sync intervals, and core engine parameters.
          </div>

          <div class="config-list">
            <div class="config-row">
              <div class="config-info">
                <div class="config-label">Database Endpoint</div>
                <div class="config-desc">
                  Primary database connection string
                </div>
              </div>
              <input
                class="form-input config-input"
                v-model="systemConfig.dbEndpoint"
              />
            </div>
            <div class="config-row">
              <div class="config-info">
                <div class="config-label">Sync Interval</div>
                <div class="config-desc">Data sync frequency in minutes</div>
              </div>
              <div class="input-unit">
                <input
                  class="form-input config-input"
                  v-model.number="systemConfig.syncInterval"
                  type="number"
                  min="1"
                />
                <span class="unit-label">min</span>
              </div>
            </div>
            <div class="config-row">
              <div class="config-info">
                <div class="config-label">Max Import Batch</div>
                <div class="config-desc">Maximum rows per import session</div>
              </div>
              <div class="input-unit">
                <input
                  class="form-input config-input"
                  v-model.number="systemConfig.maxImportBatch"
                  type="number"
                />
                <span class="unit-label">rows</span>
              </div>
            </div>
            <div class="config-row">
              <div class="config-info">
                <div class="config-label">Auto-Backup</div>
                <div class="config-desc">Enable scheduled database backups</div>
              </div>
              <label class="toggle">
                <input type="checkbox" v-model="systemConfig.autoBackup" />
                <span class="toggle-track"></span>
              </label>
            </div>
            <div class="config-row">
              <div class="config-info">
                <div class="config-label">Maintenance Mode</div>
                <div class="config-desc">
                  Disable imports during maintenance
                </div>
              </div>
              <label class="toggle">
                <input type="checkbox" v-model="systemConfig.maintenanceMode" />
                <span class="toggle-track"></span>
              </label>
            </div>
          </div>

          <!-- Engine Status -->
          <div class="engine-status">
            <div class="es-dot"></div>
            <div class="es-info">
              <div class="es-label">Sync Engine</div>
              <div class="es-val">Operational - Last sync 2 min ago</div>
            </div>
            <span class="badge badge--green">99.8% Uptime</span>
          </div>
        </div>

        <!-- Notifications -->
        <div
          v-show="activeSection === 'notifications'"
          class="settings-section card"
        >
          <div class="section-title">Notification Preferences</div>
          <div class="section-sub">
            Choose when and how you receive alerts from the system.
          </div>

          <div class="notif-list">
            <div class="notif-row" v-for="n in notifications" :key="n.key">
              <div
                class="notif-icon"
                :style="{ background: n.iconBg, color: n.iconColor }"
              >
                <component :is="n.icon" />
              </div>
              <div class="notif-info">
                <div class="notif-label">{{ n.label }}</div>
                <div class="notif-desc">{{ n.desc }}</div>
              </div>
              <label class="toggle">
                <input type="checkbox" v-model="n.enabled" />
                <span class="toggle-track"></span>
              </label>
            </div>
          </div>

          <div class="section-title" style="margin-top: 24px">
            Notification Channels
          </div>
          <div class="channel-grid">
            <div class="channel-card" v-for="c in channels" :key="c.label">
              <div class="channel-icon"><component :is="c.icon" /></div>
              <div class="channel-label">{{ c.label }}</div>
              <label class="toggle"
                ><input type="checkbox" v-model="c.enabled" /><span
                  class="toggle-track"
                ></span
              ></label>
            </div>
          </div>
        </div>

        <!-- Security -->
        <div
          v-show="activeSection === 'security'"
          class="settings-section card"
        >
          <div class="section-title">Security & Access</div>
          <div class="section-sub">
            Manage passwords, two-factor authentication, and active sessions.
          </div>

          <div class="sec-block">
            <div class="sec-block-title">Change Password</div>
            <div class="form-grid" style="grid-template-columns: 1fr">
              <div class="form-group">
                <label class="form-label">Current Password</label>
                <input
                  class="form-input"
                  type="password"
                  v-model="passwordForm.currentPassword"
                  placeholder="********"
                />
              </div>
              <div class="form-group">
                <label class="form-label">New Password</label>
                <input
                  class="form-input"
                  type="password"
                  v-model="passwordForm.newPassword"
                  placeholder="********"
                />
              </div>
              <div class="form-group">
                <label class="form-label">Confirm New Password</label>
                <input
                  class="form-input"
                  type="password"
                  v-model="passwordForm.confirmPassword"
                  placeholder="********"
                />
              </div>
              <button class="btn btn--primary" style="width: fit-content" @click="changePassword">
                Update Password
              </button>
            </div>
          </div>

          <div class="sec-block">
            <div class="sec-block-title">Two-Factor Authentication</div>
            <div class="tfa-row">
              <div>
                <div class="tfa-label">Authenticator App</div>
                <div class="tfa-desc">
                  {{ is2FAEnabled ? 'Two-factor authentication is active.' : 'Use Google Authenticator or similar to generate codes.' }}
                </div>
              </div>
              <button 
                class="btn-sm" 
                :class="is2FAEnabled ? 'btn-sm--danger' : 'btn-sm--primary'"
                @click="toggle2FA"
              >
                {{ is2FAEnabled ? 'Disable 2FA' : 'Enable 2FA' }}
              </button>
            </div>
          </div>

          <div class="sec-block">
            <div class="sec-block-title">Active Sessions</div>
            <div class="session-list">
              <div v-if="sessions.length === 0" class="session-empty">
                Tidak ada sesi aktif yang ditemukan.
              </div>
              <div class="session-row" v-for="s in sessions" :key="s.id">
                <div
                  class="session-dot"
                  :class="s.current ? 'session-dot--active' : ''"
                ></div>
                <div class="session-info">
                  <div class="session-device">
                    {{ s.device }}
                    <span v-if="s.current" class="session-current"
                      >Current</span
                    >
                  </div>
                  <div class="session-meta">{{ s.ip }} - {{ s.time }}</div>
                </div>
                <button v-if="!s.current" class="btn-sm btn-sm--danger" @click="revokeSession(s)">
                  Revoke
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Display -->
        <div v-show="activeSection === 'display'" class="settings-section card">
          <div class="section-title">Display & Appearance</div>
          <div class="section-sub">
            Customize the look and feel of your dashboard.
          </div>

          <div class="display-grid">
            <div class="form-group">
              <label class="form-label">Language</label>
              <div class="select-wrap">
                <select class="form-input" v-model="displayPreferences.language">
                  <option>Bahasa Indonesia</option>
                  <option>English</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Timezone</label>
              <div class="select-wrap">
                <select class="form-input" v-model="displayPreferences.timezone">
                  <option>Asia/Jakarta (WIB, UTC+7)</option>
                  <option>Asia/Singapore</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Date Format</label>
              <div class="select-wrap">
                <select class="form-input" v-model="displayPreferences.dateFormat">
                  <option>DD/MM/YYYY</option>
                  <option>MM/DD/YYYY</option>
                  <option>YYYY-MM-DD</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Currency Display</label>
              <div class="select-wrap">
                <select class="form-input" v-model="displayPreferences.currency">
                  <option>Rupiah (Rp)</option>
                  <option>USD ($)</option>
                </select>
              </div>
            </div>
          </div>

          <div class="theme-section">
            <div class="form-label" style="margin-bottom: 12px">Table Row Density</div>
            <div class="density-row">
              <button 
                class="density-btn" 
                :class="{ 'density-btn--active': displayPreferences.rowDensity === 'comfortable' }"
                @click="displayPreferences.rowDensity = 'comfortable'"
              >Comfortable</button>
              <button 
                class="density-btn" 
                :class="{ 'density-btn--active': displayPreferences.rowDensity === 'compact' }"
                @click="displayPreferences.rowDensity = 'compact'"
              >Compact</button>
              <button 
                class="density-btn" 
                :class="{ 'density-btn--active': displayPreferences.rowDensity === 'spacious' }"
                @click="displayPreferences.rowDensity = 'spacious'"
              >Spacious</button>
            </div>
          </div>

          <div class="theme-section">
            <div class="form-label" style="margin-bottom: 10px">Appearance Mode</div>
            <div class="dark-mode-card">
              <div class="dark-mode-info">
                <div class="dark-mode-icon" :class="isDark ? 'dark-mode-icon--dark' : 'dark-mode-icon--light'">
                  <svg v-if="isDark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"><path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/></svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/></svg>
                </div>
                <div>
                  <div class="dark-mode-label">{{ isDark ? 'Dark Mode' : 'Light Mode' }}</div>
                  <div class="dark-mode-desc">{{ isDark ? 'Dark color scheme is active' : 'Light color scheme is active' }}</div>
                </div>
              </div>
              <label class="toggle">
                <input type="checkbox" :checked="isDark" @change="toggleDark" />
                <span class="toggle-track"></span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Save Toast -->
    <div class="save-toast" :class="{ 'save-toast--show': saved }">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="20 6 9 17 4 12" /></svg>
      Settings saved successfully!
    </div>

    <!-- 2FA Modal -->
    <div class="modal-overlay" v-if="show2FAModal" @click.self="show2FAModal = false">
      <div class="modal-card card">
        <div class="modal-header">
          <div class="modal-title">Enable Two-Factor Authentication</div>
          <button class="modal-close" @click="show2FAModal = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="modal-desc">
            Amankan akun Anda dengan mengaktifkan otentikasi dua faktor (2FA). Silakan ikuti langkah-langkah di bawah ini:
          </p>
          <div class="tfa-steps">
            <div class="tfa-step">
              <span class="step-num">1</span>
              <span>Pindai kode QR di bawah ini menggunakan Google Authenticator atau aplikasi sejenis:</span>
            </div>
            <div class="qr-container">
              <div class="simulated-qr">
                <svg viewBox="0 0 100 100" class="qr-svg">
                  <rect x="0" y="0" width="30" height="30" fill="currentColor"/>
                  <rect x="5" y="5" width="20" height="20" fill="white"/>
                  <rect x="10" y="10" width="10" height="10" fill="currentColor"/>
                  <rect x="70" y="0" width="30" height="30" fill="currentColor"/>
                  <rect x="75" y="5" width="20" height="20" fill="white"/>
                  <rect x="80" y="10" width="10" height="10" fill="currentColor"/>
                  <rect x="0" y="70" width="30" height="30" fill="currentColor"/>
                  <rect x="5" y="75" width="20" height="20" fill="white"/>
                  <rect x="10" y="80" width="10" height="10" fill="currentColor"/>
                  <rect x="35" y="5" width="10" height="10" fill="currentColor"/>
                  <rect x="50" y="15" width="5" height="15" fill="currentColor"/>
                  <rect x="40" y="25" width="15" height="10" fill="currentColor"/>
                  <rect x="10" y="45" width="15" height="5" fill="currentColor"/>
                  <rect x="25" y="35" width="10" height="20" fill="currentColor"/>
                  <rect x="35" y="70" width="15" height="15" fill="currentColor"/>
                  <rect x="55" y="75" width="10" height="10" fill="currentColor"/>
                  <rect x="70" y="45" width="15" height="15" fill="currentColor"/>
                  <rect x="85" y="35" width="10" height="20" fill="currentColor"/>
                  <rect x="75" y="80" width="15" height="15" fill="currentColor"/>
                </svg>
              </div>
              <div class="qr-secret">Secret Key: <code>VNET AUTH 2026 KEY</code></div>
            </div>
            <div class="tfa-step">
              <span class="step-num">2</span>
              <span>Masukkan 6 digit kode yang muncul di aplikasi Anda:</span>
            </div>
            <div class="tfa-input-row">
              <input 
                type="text" 
                class="form-input code-input" 
                placeholder="000000" 
                maxLength="6" 
                v-model="tfaCode" 
              />
              <button class="btn btn--primary" @click="confirm2FA">Verify & Activate</button>
            </div>
            <div class="tfa-error" v-if="tfaError">{{ tfaError }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Revoke Session Confirm Modal -->
  <Teleport to="body">
    <div class="revoke-modal-backdrop" v-if="showRevokeModal" @click.self="cancelRevoke">
      <div class="revoke-modal-card">
        <div class="revoke-modal-icon-wrap">
          <div class="revoke-modal-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="26" height="26">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0110 0v4"/>
            </svg>
          </div>
        </div>
        <div class="revoke-modal-body">
          <div class="revoke-modal-title">Keluarkan Sesi</div>
          <div class="revoke-modal-desc">
            Anda akan mengeluarkan sesi aktif di
            <strong>{{ revokeTarget?.device }}</strong>.
            Perangkat tersebut akan logout otomatis.
          </div>
        </div>
        <div class="revoke-modal-footer">
          <button class="btn-sm btn-sm--ghost" @click="cancelRevoke">Batal</button>
          <button class="btn-sm btn-sm--danger" @click="confirmRevoke" :disabled="loadingRevoke">
            {{ loadingRevoke ? 'Memproses...' : 'Ya, Keluarkan' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, defineComponent, h, onMounted, watch } from "vue";
import { updateProfile, changePassword as apiChangePassword, getSystemConfig, saveSystemConfig, getActiveSessions, revokeSession as apiRevokeSession } from "@/services/api";

const activeSection = ref("profile");
const saved = ref(false);
const isDark = ref(false);

const profile = ref({
  name: "",
  username: "",
  role: "",
});

const systemConfig = ref({
  dbEndpoint: "mysql://vnet-db.internal:3306/vnet_prod",
  syncInterval: 15,
  maxImportBatch: 1200,
  autoBackup: true,
  maintenanceMode: false,
});

const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const is2FAEnabled = ref(false);
const show2FAModal = ref(false);
const tfaCode = ref("");
const tfaError = ref("");

const sessions = ref([]);
const showRevokeModal = ref(false);
const revokeTarget = ref(null);
const loadingRevoke = ref(false);

const displayPreferences = ref({
  language: "Bahasa Indonesia",
  timezone: "Asia/Jakarta (WIB, UTC+7)",
  dateFormat: "DD/MM/YYYY",
  currency: "Rupiah (Rp)",
  rowDensity: "comfortable",
});

const fileInput = ref(null);

const avatarInitials = computed(() => {
  if (!profile.value.name) return "A";
  const words = profile.value.name.trim().split(/\s+/);
  if (words.length >= 2) {
    return (words[0][0] + words[1][0]).toUpperCase();
  } else if (words.length === 1 && words[0].length > 0) {
    return words[0].substring(0, 2).toUpperCase();
  }
  return "A";
});

const mkI = (d) =>
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
          style: "width:16px;height:16px",
        },
        (Array.isArray(d) ? d : [d]).map((p) => h("path", { d: p })),
      ),
  });

const IconProfile = mkI([
  "M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2",
  "M12 11a4 4 0 100-8 4 4 0 000 8z",
]);
const IconSystem = defineComponent({
  render: () =>
    h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: "currentColor",
        "stroke-width": "1.7",
        "stroke-linecap": "round",
        style: "width:16px;height:16px",
      },
      [
        h("rect", { x: "2", y: "3", width: "20", height: "14", rx: "2" }),
        h("line", { x1: "8", y1: "21", x2: "16", y2: "21" }),
        h("line", { x1: "12", y1: "17", x2: "12", y2: "21" }),
      ],
    ),
});
const IconBell = mkI(
  "M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0",
);
const IconShield = mkI("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z");
const IconDisplay = defineComponent({
  render: () =>
    h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: "currentColor",
        "stroke-width": "1.7",
        "stroke-linecap": "round",
        style: "width:16px;height:16px",
      },
      [
        h("circle", { cx: "12", cy: "12", r: "3" }),
        h("path", {
          d: "M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z",
        }),
      ],
    ),
});

const IconMail = mkI([
  "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z",
  "M22 6l-10 7L2 6",
]);
const IconPhone = mkI(
  "M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81a19.79 19.79 0 01-3.07-8.63A2 2 0 012 .18h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L6.09 7.91a16 16 0 006 6l1.19-1.19a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 14.92z",
);
const IconSlack = mkI([
  "M14.5 10c-.83 0-1.5-.67-1.5-1.5v-5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5v5c0 .83-.67 1.5-1.5 1.5z",
  "M20.5 10H19V8.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z",
]);

const sections = [
  { key: "profile", label: "Profile", sub: "Personal info", icon: IconProfile },
  {
    key: "system",
    label: "System",
    sub: "DB & engine config",
    icon: IconSystem,
  },
  {
    key: "notifications",
    label: "Notifications",
    sub: "Alerts & channels",
    icon: IconBell,
  },
  {
    key: "security",
    label: "Security",
    sub: "Password & sessions",
    icon: IconShield,
  },
  {
    key: "display",
    label: "Display",
    sub: "Language & appearance",
    icon: IconDisplay,
  },
];

const filteredSections = computed(() => {
  return sections.filter(s => {
    if (s.key === "system") {
      return profile.value.role === "SUPER_ADMIN";
    }
    return true;
  });
});

const notifications = ref([
  {
    key: "new_customer",
    label: "New Customer Registered",
    desc: "Alert when a new customer is added to the database.",
    enabled: true,
    icon: mkI([
      "M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2",
      "M12 11a4 4 0 100-8 4 4 0 000 8z",
    ]),
    iconBg: "#EEF2FF",
    iconColor: "var(--navy)",
  },
  {
    key: "isolir",
    label: "Isolir Rate Warning",
    desc: "Alert when isolir rate exceeds 20% threshold.",
    enabled: true,
    icon: mkI([
      "M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z",
      "M12 9v4",
      "M12 17h.01",
    ]),
    iconBg: "#FEF3E0",
    iconColor: "var(--gold)",
  },
  {
    key: "import_done",
    label: "Import Completed",
    desc: "Notify when a data import finishes successfully.",
    enabled: true,
    icon: mkI([
      "M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4",
      "M17 8l-5-5-5 5",
      "M12 3v12",
    ]),
    iconBg: "#E0F8F6",
    iconColor: "var(--teal)",
  },
  {
    key: "sync_fail",
    label: "Sync Failure Alert",
    desc: "Alert when database sync fails or times out.",
    enabled: true,
    icon: mkI(["M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"]),
    iconBg: "#FDECEB",
    iconColor: "var(--red-warn)",
  },
  {
    key: "weekly_report",
    label: "Weekly Report",
    desc: "Receive automated weekly performance digest.",
    enabled: false,
    icon: mkI([
      "M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z",
      "M14 2v6h6",
      "M16 13H8",
      "M16 17H8",
      "M10 9H8",
    ]),
    iconBg: "#F4F6FB",
    iconColor: "var(--text-3)",
  },
]);

const channels = ref([
  { label: "Email", enabled: true, icon: IconMail },
  { label: "WhatsApp", enabled: false, icon: IconPhone },
  { label: "Slack", enabled: false, icon: IconSlack },
]);

onMounted(async () => {
  isDark.value = localStorage.getItem('vnet-theme') === 'dark';
  if (isDark.value) document.documentElement.classList.add('dark');

  const stored = localStorage.getItem("vnet_user");
  if (stored) {
    try {
      const u = JSON.parse(stored);
      profile.value.name = u.nama || u.name || "";
      profile.value.username = u.username || "";
      profile.value.role = u.role || "";
    } catch (e) {
      console.error("Failed to parse stored user", e);
    }
  }

  // Enforce activeSection check for non-SUPER_ADMIN
  if (profile.value.role !== 'SUPER_ADMIN' && activeSection.value === 'system') {
    activeSection.value = 'profile';
  }

  // Load system config from backend, fallback to localStorage
  try {
    const config = await getSystemConfig();
    if (config) {
      systemConfig.value = {
        dbEndpoint: config.dbEndpoint || "",
        syncInterval: config.syncInterval || 15,
        maxImportBatch: config.maxImportBatch || 1200,
        autoBackup: config.autoBackup === true || config.autoBackup === "true",
        maintenanceMode: config.maintenanceMode === true || config.maintenanceMode === "true",
      };
    }
  } catch (err) {
    console.error("Gagal memuat konfigurasi sistem dari API, fallback ke localStorage", err);
    const storedSystem = localStorage.getItem("vnet_system_config");
    if (storedSystem) {
      try {
        systemConfig.value = { ...systemConfig.value, ...JSON.parse(storedSystem) };
      } catch (e) {}
    }
  }

  const storedNotifs = localStorage.getItem("vnet_notifications");
  if (storedNotifs) {
    try {
      const parsed = JSON.parse(storedNotifs);
      notifications.value.forEach(n => {
        const matching = parsed.find(p => p.key === n.key);
        if (matching) n.enabled = matching.enabled;
      });
    } catch (e) {}
  }

  const storedChannels = localStorage.getItem("vnet_notif_channels");
  if (storedChannels) {
    try {
      const parsed = JSON.parse(storedChannels);
      channels.value.forEach(c => {
        const matching = parsed.find(p => p.label === c.label);
        if (matching) c.enabled = matching.enabled;
      });
    } catch (e) {}
  }

  is2FAEnabled.value = localStorage.getItem("vnet_2fa_enabled") === "true";

  const storedDisplay = localStorage.getItem("vnet_display_preferences");
  if (storedDisplay) {
    try {
      displayPreferences.value = { ...displayPreferences.value, ...JSON.parse(storedDisplay) };
    } catch (e) {}
  }

  await loadSessions();
});

watch(() => displayPreferences.value.rowDensity, (newDensity) => {
  let paddingVal = "14px 20px";
  if (newDensity === "compact") paddingVal = "8px 14px";
  if (newDensity === "spacious") paddingVal = "20px 24px";
  document.documentElement.style.setProperty("--table-row-padding", paddingVal);
}, { immediate: true });

function toggleDark() {
  isDark.value = !isDark.value;
  document.documentElement.classList.toggle('dark', isDark.value);
  localStorage.setItem('vnet-theme', isDark.value ? 'dark' : 'light');
}

async function changePassword() {
  if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
    alert("Silakan isi semua kolom password.");
    return;
  }
  if (passwordForm.value.newPassword.length < 6) {
    alert("Password baru minimal 6 karakter.");
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert("Konfirmasi password baru tidak sesuai.");
    return;
  }
  try {
    const res = await apiChangePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    });
    alert(res.message || "Password berhasil diperbarui!");
    passwordForm.value.currentPassword = "";
    passwordForm.value.newPassword = "";
    passwordForm.value.confirmPassword = "";
  } catch (err) {
    alert(err.message || "Gagal memperbarui password");
  }
}

function toggle2FA() {
  if (is2FAEnabled.value) {
    is2FAEnabled.value = false;
    localStorage.setItem("vnet_2fa_enabled", "false");
    alert("Two-Factor Authentication (2FA) dinonaktifkan.");
  } else {
    tfaCode.value = "";
    tfaError.value = "";
    show2FAModal.value = true;
  }
}

function confirm2FA() {
  if (tfaCode.value.trim().length !== 6 || isNaN(Number(tfaCode.value))) {
    tfaError.value = "Kode harus berupa 6 digit angka.";
    return;
  }
  is2FAEnabled.value = true;
  localStorage.setItem("vnet_2fa_enabled", "true");
  show2FAModal.value = false;
  alert("Two-Factor Authentication (2FA) berhasil diaktifkan!");
}

async function loadSessions() {
  try {
    const res = await getActiveSessions();
    if (res) {
      sessions.value = res;
    }
  } catch (err) {
    console.error("Gagal memuat sesi aktif:", err);
  }
}

function revokeSession(session) {
  revokeTarget.value = session;
  showRevokeModal.value = true;
}

async function confirmRevoke() {
  if (!revokeTarget.value) return;
  loadingRevoke.value = true;
  try {
    await apiRevokeSession(revokeTarget.value.id);
    sessions.value = sessions.value.filter(s => s.id !== revokeTarget.value.id);
    showRevokeModal.value = false;
  } catch (err) {
    console.error("Gagal mengeluarkan sesi:", err.message);
  } finally {
    loadingRevoke.value = false;
    revokeTarget.value = null;
  }
}

function cancelRevoke() {
  showRevokeModal.value = false;
  revokeTarget.value = null;
}

async function saveAll() {
  if (activeSection.value === 'profile') {
    try {
      const res = await updateProfile({
        name: profile.value.name,
        username: profile.value.username,
      });
      if (res.token) {
        localStorage.setItem("vnet_token", res.token);
      }
      const u = {
        id: res.user.id,
        username: res.user.username,
        nama: res.user.name,
        role: res.user.role,
      };
      localStorage.setItem("vnet_user", JSON.stringify(u));
      window.dispatchEvent(new CustomEvent("vnet-user-updated", { detail: u }));
      await loadSessions();
    } catch (err) {
      alert(err.message || "Gagal memperbarui profil");
      return;
    }
  }

  if (activeSection.value === 'system') {
    try {
      await saveSystemConfig(systemConfig.value);
    } catch (err) {
      alert(err.message || "Gagal memperbarui konfigurasi sistem di database");
      return;
    }
  }

  localStorage.setItem("vnet_system_config", JSON.stringify(systemConfig.value));
  localStorage.setItem("vnet_notifications", JSON.stringify(notifications.value));
  localStorage.setItem("vnet_notif_channels", JSON.stringify(channels.value));
  localStorage.setItem("vnet_display_preferences", JSON.stringify(displayPreferences.value));

  saved.value = true;
  setTimeout(() => {
    saved.value = false;
  }, 2500);
}
</script>

<style scoped>
.settings {
  display: flex;
  flex-direction: column;
  gap: 18px;
  max-width: 1400px;
}
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.page-title {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: 0;
}
.page-sub {
  font-size: 13px;
  color: var(--text-2);
  margin-top: 4px;
}
.btn {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 500;
  border: none;
  transition: all 0.15s;
  cursor: pointer;
}
.btn svg {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}
.btn--primary {
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 700;
}
.btn--primary:hover {
  background: var(--navy-mid);
}

.settings-layout {
  display: grid;
  grid-template-columns: 230px 1fr;
  gap: 16px;
  align-items: start;
}

.settings-nav {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  position: sticky;
  top: 0;
}
.snav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--r-sm);
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s;
  width: 100%;
}
.snav-item:hover {
  background: var(--bg);
}
.snav-item--active {
  background: var(--bg);
}
.snav-item--active .snav-label {
  color: var(--navy);
  font-weight: 700;
}
.snav-icon {
  width: 16px;
  height: 16px;
  color: var(--text-3);
  flex-shrink: 0;
}
.snav-item--active .snav-icon {
  color: var(--navy);
}
.snav-text {
  display: flex;
  flex-direction: column;
}
.snav-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-1);
}
.snav-sub {
  font-size: 10px;
  color: var(--text-3);
  margin-top: 1px;
}

.settings-content {
}
.settings-section {
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.section-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: 0;
}
.section-sub {
  font-size: 13px;
  color: var(--text-2);
  margin-top: -12px;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.avatar-big {
  width: 60px;
  height: 60px;
  border-radius: var(--r-md);
  background: var(--gold);
  color: var(--navy);
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.avatar-name {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 800;
  color: var(--text-1);
}
.avatar-role {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 2px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
}
.form-input {
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  font-size: 13px;
  font-family: var(--font-body);
  color: var(--text-1);
  background: var(--surface);
  outline: none;
  transition: border-color 0.15s;
}
.form-input:focus {
  border-color: var(--navy);
}

.btn-sm {
  padding: 7px 14px;
  border-radius: var(--r-sm);
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font-display);
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-sm--outline {
  background: none;
  border: 1px solid var(--border);
  color: var(--text-1);
}
.btn-sm--outline:hover {
  background: var(--bg);
}
.btn-sm--primary {
  background: var(--navy);
  color: #fff;
}
.btn-sm--primary:hover {
  background: var(--navy-mid);
}
.btn-sm--danger {
  background: #fdeceb;
  color: var(--red-warn);
  border: none;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
}
.config-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
}
.config-row:last-child {
  border-bottom: none;
}
.config-info {
  flex: 1;
}
.config-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}
.config-desc {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}
.config-input {
  width: 260px;
  flex-shrink: 0;
}
.input-unit {
  display: flex;
  align-items: center;
  gap: 8px;
}
.unit-label {
  font-size: 12px;
  color: var(--text-3);
  font-family: var(--font-display);
  font-weight: 600;
}

.toggle {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 22px;
  flex-shrink: 0;
  cursor: pointer;
}
.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
  position: absolute;
}
.toggle-track {
  position: absolute;
  inset: 0;
  background: var(--border);
  border-radius: 99px;
  transition: background 0.2s;
}
.toggle-track::after {
  content: "";
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  top: 3px;
  left: 3px;
  transition: transform 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}
.toggle input:checked + .toggle-track {
  background: var(--navy);
}
.toggle input:checked + .toggle-track::after {
  transform: translateX(18px);
}

.engine-status {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: var(--bg);
  border-radius: var(--r-md);
}
.es-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--green-ok);
  box-shadow: 0 0 0 3px rgba(39, 174, 96, 0.2);
  flex-shrink: 0;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%,
  100% {
    box-shadow: 0 0 0 3px rgba(39, 174, 96, 0.2);
  }
  50% {
    box-shadow: 0 0 0 6px rgba(39, 174, 96, 0.06);
  }
}
.es-info {
  flex: 1;
}
.es-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0;
  color: var(--text-3);
  font-family: var(--font-display);
}
.es-val {
  font-size: 13px;
  font-weight: 600;
  color: var(--green-ok);
  margin-top: 1px;
}

.notif-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
}
.notif-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
}
.notif-row:last-child {
  border-bottom: none;
}
.notif-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.notif-info {
  flex: 1;
}
.notif-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}
.notif-desc {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.channel-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
}
.channel-icon {
  width: 32px;
  height: 32px;
  background: var(--bg);
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-2);
}
.channel-label {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}

.sec-block {
  padding: 20px;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.sec-block-title {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
  text-transform: uppercase;
  letter-spacing: 0;
}
.tfa-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.tfa-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}
.tfa-desc {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}
.session-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.session-empty {
  color: var(--text-3);
  font-size: 13px;
  text-align: center;
  padding: 16px 0;
  font-style: italic;
}
.session-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.session-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--border);
  flex-shrink: 0;
}
.session-dot--active {
  background: var(--green-ok);
}
.session-info {
  flex: 1;
}
.session-device {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  display: flex;
  align-items: center;
  gap: 8px;
}
.session-meta {
  font-size: 11px;
  color: var(--text-3);
}
.session-current {
  font-size: 9px;
  font-weight: 700;
  font-family: var(--font-display);
  background: #e8f8ef;
  color: var(--green-ok);
  padding: 2px 7px;
  border-radius: 99px;
}

/* Revoke Session Modal */
.revoke-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  backdrop-filter: blur(3px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  animation: fadeIn 0.15s ease-out;
}
.revoke-modal-card {
  width: 100%;
  max-width: 380px;
  background: var(--card-bg, #fff);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.18);
  overflow: hidden;
  text-align: center;
  animation: popIn 0.2s ease-out;
}
.revoke-modal-icon-wrap {
  padding: 28px 20px 14px;
  display: flex;
  justify-content: center;
}
.revoke-modal-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #FFF7ED;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f97316;
}
.revoke-modal-body {
  padding: 0 24px 20px;
}
.revoke-modal-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
  margin-bottom: 7px;
}
.revoke-modal-desc {
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.6;
}
.revoke-modal-desc strong {
  color: var(--text-1);
}
.revoke-modal-footer {
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: center;
  gap: 10px;
}
.btn-sm--ghost {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-2);
  padding: 7px 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-sm--ghost:hover {
  background: var(--border);
  color: var(--text-1);
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes popIn {
  from { transform: scale(0.94); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.display-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.select-wrap {
  position: relative;
}
.select-wrap select {
  width: 100%;
}
.theme-section {
}
.density-row {
  display: flex;
  gap: 8px;
}
.density-btn {
  padding: 9px 20px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: none;
  font-size: 13px;
  font-weight: 500;
  font-family: var(--font-body);
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.15s;
}
.density-btn--active {
  background: var(--navy);
  color: #fff;
  border-color: var(--navy);
}

.save-toast {
  position: fixed;
  bottom: 28px;
  right: 28px;
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  padding: 12px 20px;
  border-radius: var(--r-md);
  display: flex;
  align-items: center;
  gap: 8px;
  transform: translateY(80px);
  opacity: 0;
  transition: all 0.3s;
  z-index: 999;
  box-shadow: var(--shadow-lg);
}
.save-toast svg {
  width: 16px;
  height: 16px;
}
.save-toast--show {
  transform: translateY(0);
  opacity: 1;
}

@media (max-width: 900px) {
  .settings-layout {
    grid-template-columns: 1fr;
  }
  .settings-nav {
    flex-direction: row;
    flex-wrap: wrap;
    position: static;
  }
  .form-grid,
  .display-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 640px) {
  .channel-grid {
    grid-template-columns: 1fr;
  }
  .config-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .config-input {
    width: 100%;
  }
}

/* Dark Mode Card */
.dark-mode-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 18px;
  border: 1px solid var(--border); border-radius: var(--r-md);
  background: var(--surface-2);
  gap: 12px;
}
.dark-mode-info { display: flex; align-items: center; gap: 12px; }
.dark-mode-icon {
  width: 38px; height: 38px; border-radius: var(--r-sm);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.dark-mode-icon svg { width: 18px; height: 18px; }
.dark-mode-icon--light { background: #FEF3E0; color: var(--gold); }
.dark-mode-icon--dark { background: #EEF2FF; color: var(--navy); }
.dark-mode-label { font-size: 13px; font-weight: 600; color: var(--text-1); }
.dark-mode-desc { font-size: 11px; color: var(--text-3); margin-top: 2px; }

/* Improve active nav with accent */
.snav-item--active { background: linear-gradient(90deg, rgba(13,19,64,0.06) 0%, transparent 100%); border-left: 3px solid var(--navy); padding-left: 9px; }

/* 2FA Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  animation: fadeIn 0.2s ease;
}
.modal-card {
  width: 480px;
  max-width: 90%;
  padding: 24px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-xl);
  animation: scaleUp 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.modal-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
}
.modal-close {
  background: none;
  border: none;
  color: var(--text-3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.15s;
}
.modal-close:hover {
  background: var(--bg);
  color: var(--text-1);
}
.modal-close svg {
  width: 18px;
  height: 18px;
}
.modal-desc {
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.5;
  margin-bottom: 20px;
}
.tfa-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.tfa-step {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: var(--text-1);
  line-height: 1.4;
}
.step-num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: var(--navy);
  color: #fff;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 1px;
}
.qr-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  align-self: center;
}
.simulated-qr {
  width: 140px;
  height: 140px;
  background: #fff;
  padding: 8px;
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000;
}
.qr-svg {
  width: 100%;
  height: 100%;
}
.qr-secret {
  font-size: 11px;
  color: var(--text-3);
}
.qr-secret code {
  font-family: monospace;
  font-weight: 700;
  color: var(--text-1);
  background: var(--surface-3);
  padding: 2px 6px;
  border-radius: 4px;
}
.tfa-input-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.code-input {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 4px;
  width: 120px;
  flex-shrink: 0;
}
.tfa-error {
  font-size: 12px;
  color: var(--red-warn);
  font-weight: 600;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes scaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
