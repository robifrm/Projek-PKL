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
                style="opacity: 0.7; cursor: not-allowed; background: var(--bg)"
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
        </div>



        <!-- Security -->
        <div
          v-show="activeSection === 'security'"
          class="settings-section card"
        >
          <div class="section-title">Security & Access</div>
          <div class="section-sub">
            Manage passwords and active sessions.
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
              <button
                class="btn btn--primary"
                style="width: fit-content"
                @click="changePassword"
              >
                Update Password
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
                <button
                  v-if="!s.current"
                  class="btn-sm btn-sm--danger"
                  @click="revokeSession(s)"
                >
                  Revoke
                </button>
              </div>
            </div>
          </div>
        </div>



        <!-- User Management (SUPER_ADMIN only) -->
        <div v-show="activeSection === 'users'" class="settings-section card">
          <div class="section-title">User Management</div>
          <div class="section-sub">
            Kelola data user dan agent yang memiliki akses ke dashboard backend.
          </div>

          <!-- Topbar action: Add User -->
          <div
            class="user-management-actions"
            style="
              margin-bottom: 20px;
              display: flex;
              justify-content: flex-end;
            "
          >
            <button class="btn btn--primary" @click="openAddUserModal">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                width="16"
                height="16"
                style="margin-right: 6px"
              >
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="8.5" cy="7" r="4" />
                <line x1="20" y1="8" x2="20" y2="14" />
                <line x1="23" y1="11" x2="17" y2="11" />
              </svg>
              Tambah User Baru
            </button>
          </div>

          <!-- User Table -->
          <div
            class="users-table-wrapper"
            style="overflow-x: auto; width: 100%"
          >
            <table
              class="users-table"
              style="width: 100%; border-collapse: collapse; min-width: 600px"
            >
              <thead>
                <tr
                  style="
                    border-bottom: 1px solid var(--border);
                    text-align: left;
                  "
                >
                  <th
                    style="
                      padding: 12px 8px;
                      font-weight: 600;
                      font-size: 13px;
                      color: var(--text-2);
                    "
                  >
                    NAMA
                  </th>
                  <th
                    style="
                      padding: 12px 8px;
                      font-weight: 600;
                      font-size: 13px;
                      color: var(--text-2);
                    "
                  >
                    USERNAME
                  </th>
                  <th
                    style="
                      padding: 12px 8px;
                      font-weight: 600;
                      font-size: 13px;
                      color: var(--text-2);
                    "
                  >
                    ROLE
                  </th>
                  <th
                    style="
                      padding: 12px 8px;
                      font-weight: 600;
                      font-size: 13px;
                      color: var(--text-2);
                    "
                  >
                    ASSOCIATED AGENT
                  </th>
                  <th
                    style="
                      padding: 12px 8px;
                      font-weight: 600;
                      font-size: 13px;
                      color: var(--text-2);
                      text-align: right;
                    "
                  >
                    AKSI
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="u in usersList"
                  :key="u.id"
                  style="border-bottom: 1px solid rgba(0, 0, 0, 0.05)"
                >
                  <td
                    style="
                      padding: 12px 8px;
                      font-size: 13px;
                      font-weight: 500;
                      color: var(--text-1);
                    "
                  >
                    {{ u.name }}
                  </td>
                  <td
                    style="
                      padding: 12px 8px;
                      font-size: 13px;
                      color: var(--text-3);
                    "
                  >
                    @{{ u.username }}
                  </td>
                  <td style="padding: 12px 8px; font-size: 13px">
                    <span
                      :class="getRoleClass(u.role)"
                      style="
                        padding: 3px 8px;
                        border-radius: 4px;
                        font-size: 11px;
                        font-weight: 600;
                      "
                    >
                      {{ u.role }}
                    </span>
                  </td>
                  <td
                    style="
                      padding: 12px 8px;
                      font-size: 13px;
                      color: var(--text-2);
                    "
                  >
                    {{ u.agent ? u.agent.nama : "-" }}
                  </td>
                  <td
                    style="
                      padding: 12px 8px;
                      font-size: 13px;
                      text-align: right;
                    "
                  >
                    <button
                      class="btn-delete-user"
                      @click="requestDeleteUser(u)"
                      :disabled="u.username === profile.username"
                      style="
                        background: none;
                        border: none;
                        color: #ef4444;
                        cursor: pointer;
                        padding: 4px 8px;
                        font-size: 12px;
                        font-weight: 500;
                      "
                    >
                      Hapus
                    </button>
                  </td>
                </tr>
                <tr v-if="loadError">
                  <td
                    colspan="5"
                    style="
                      text-align: center;
                      padding: 20px;
                      color: #ef4444;
                      font-size: 13px;
                      font-weight: 500;
                    "
                  >
                    {{ loadError }}
                  </td>
                </tr>
                <tr v-else-if="usersList.length === 0">
                  <td
                    colspan="5"
                    style="
                      text-align: center;
                      padding: 20px;
                      color: var(--text-3);
                      font-size: 13px;
                    "
                  >
                    Tidak ada data user.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Save Toast -->
    <div class="save-toast" :class="{ 'save-toast--show': saved }">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
        stroke-linecap="round"
      >
        <polyline points="20 6 9 17 4 12" />
      </svg>
      Settings saved successfully!
    </div>


  </div>

  <!-- Revoke Session Confirm Modal -->
  <Teleport to="body">
    <div
      class="revoke-modal-backdrop"
      v-if="showRevokeModal"
      @click.self="cancelRevoke"
    >
      <div class="revoke-modal-card">
        <div class="revoke-modal-icon-wrap">
          <div class="revoke-modal-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              width="26"
              height="26"
            >
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0110 0v4" />
            </svg>
          </div>
        </div>
        <div class="revoke-modal-body">
          <div class="revoke-modal-title">Keluarkan Sesi</div>
          <div class="revoke-modal-desc">
            Anda akan mengeluarkan sesi aktif di
            <strong>{{ revokeTarget?.device }}</strong
            >. Perangkat tersebut akan logout otomatis.
          </div>
        </div>
        <div class="revoke-modal-footer">
          <button class="btn-sm btn-sm--ghost" @click="cancelRevoke">
            Batal
          </button>
          <button
            class="btn-sm btn-sm--danger"
            @click="confirmRevoke"
            :disabled="loadingRevoke"
          >
            {{ loadingRevoke ? "Memproses..." : "Ya, Keluarkan" }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- Add User Modal -->
  <Teleport to="body">
    <div
      class="user-modal-backdrop"
      v-if="showAddUserModal"
      @click.self="closeAddUserModal"
    >
      <div class="user-modal-card">
        <div class="user-modal-header">
          <div class="user-modal-title">Tambah User Baru</div>
          <button class="user-modal-close" @click="closeAddUserModal">
            &times;
          </button>
        </div>
        <form @submit.prevent="submitAddUser" class="user-modal-form">
          <div class="form-group" style="margin-bottom: 12px; text-align: left">
            <label
              class="form-label"
              style="
                font-weight: 500;
                font-size: 13px;
                display: block;
                margin-bottom: 5px;
              "
              >Nama Lengkap</label
            >
            <input
              type="text"
              v-model="newUserForm.name"
              class="form-input"
              placeholder="Masukkan nama lengkap"
              required
              style="
                width: 100%;
                padding: 8px;
                border: 1px solid var(--border);
                border-radius: 6px;
                background: transparent;
                color: var(--text-1);
              "
            />
          </div>
          <div class="form-group" style="margin-bottom: 12px; text-align: left">
            <label
              class="form-label"
              style="
                font-weight: 500;
                font-size: 13px;
                display: block;
                margin-bottom: 5px;
              "
              >Username</label
            >
            <input
              type="text"
              v-model="newUserForm.username"
              class="form-input"
              placeholder="Masukkan username"
              required
              style="
                width: 100%;
                padding: 8px;
                border: 1px solid var(--border);
                border-radius: 6px;
                background: transparent;
                color: var(--text-1);
              "
            />
          </div>
          <div class="form-group" style="margin-bottom: 12px; text-align: left">
            <label
              class="form-label"
              style="
                font-weight: 500;
                font-size: 13px;
                display: block;
                margin-bottom: 5px;
              "
              >Password</label
            >
            <input
              type="password"
              v-model="newUserForm.password"
              class="form-input"
              placeholder="Minimal 6 karakter"
              required
              minlength="6"
              style="
                width: 100%;
                padding: 8px;
                border: 1px solid var(--border);
                border-radius: 6px;
                background: transparent;
                color: var(--text-1);
              "
            />
          </div>
          <div class="form-group" style="margin-bottom: 12px; text-align: left">
            <label
              class="form-label"
              style="
                font-weight: 500;
                font-size: 13px;
                display: block;
                margin-bottom: 5px;
              "
              >Role</label
            >
            <select
              v-model="newUserForm.role"
              class="form-input"
              required
              style="
                width: 100%;
                padding: 8px;
                border: 1px solid var(--border);
                border-radius: 6px;
                background: var(--card-bg);
                color: var(--text-1);
              "
            >
              <option value="SUPER_ADMIN">SUPER_ADMIN</option>
              <option value="STAFF">STAFF</option>
              <option value="AGENT">AGENT</option>
              <option value="TECHNICIAN">TECHNICIAN</option>
            </select>
          </div>
          <div
            class="form-group"
            style="margin-bottom: 12px; text-align: left"
            v-if="newUserForm.role === 'AGENT'"
          >
            <label
              class="form-label"
              style="
                font-weight: 500;
                font-size: 13px;
                display: block;
                margin-bottom: 5px;
              "
              >Pilih Agen</label
            >
            <select
              v-model="newUserForm.agentId"
              class="form-input"
              required
              style="
                width: 100%;
                padding: 8px;
                border: 1px solid var(--border);
                border-radius: 6px;
                background: var(--card-bg);
                color: var(--text-1);
              "
            >
              <option value="" disabled>
                -- {{ loadError ? "Gagal memuat agen" : "Pilih Agen" }} --
              </option>
              <option v-for="ag in agentsList" :key="ag.id" :value="ag.id">
                {{ ag.nama }}
              </option>
            </select>
          </div>

          <div
            class="user-modal-error"
            v-if="loadError"
            style="
              color: #ef4444;
              margin-top: 10px;
              font-size: 13px;
              text-align: left;
            "
          >
            {{ loadError }}
          </div>

          <div
            class="user-modal-error"
            v-if="addUserError"
            style="
              color: #ef4444;
              margin-top: 10px;
              font-size: 13px;
              text-align: left;
            "
          >
            {{ addUserError }}
          </div>

          <div
            class="user-modal-footer"
            style="
              margin-top: 18px;
              display: flex;
              justify-content: flex-end;
              gap: 8px;
            "
          >
            <button
              type="button"
              class="btn btn--secondary"
              @click="closeAddUserModal"
              style="
                background: none;
                border: 1px solid var(--border);
                color: var(--text-2);
              "
            >
              Batal
            </button>
            <button
              type="submit"
              class="btn btn--primary"
              :disabled="submittingUser"
            >
              {{ submittingUser ? "Menyimpan..." : "Simpan User" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>

  <!-- Delete User Confirm Modal -->
  <Teleport to="body">
    <div
      class="revoke-modal-backdrop"
      v-if="showDeleteUserModal"
      @click.self="cancelDeleteUser"
    >
      <div class="revoke-modal-card">
        <div
          class="revoke-modal-icon-wrap"
          style="
            background: rgba(239, 68, 68, 0.1);
            color: #ef4444;
            padding: 28px 20px 14px;
            display: flex;
            justify-content: center;
          "
        >
          <div
            class="revoke-modal-icon"
            style="
              width: 60px;
              height: 60px;
              border-radius: 50%;
              background: rgba(239, 68, 68, 0.1);
              display: flex;
              align-items: center;
              justify-content: center;
              color: #ef4444;
            "
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              width="26"
              height="26"
            >
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="8.5" cy="7" r="4" />
              <line x1="18" y1="8" x2="23" y2="13" />
              <line x1="23" y1="8" x2="18" y2="13" />
            </svg>
          </div>
        </div>
        <div class="revoke-modal-body" style="padding: 0 24px 20px">
          <div
            class="revoke-modal-title"
            style="
              font-family: var(--font-display);
              font-size: 16px;
              font-weight: 800;
              color: var(--text-1);
              margin-bottom: 7px;
            "
          >
            Hapus User
          </div>
          <div
            class="revoke-modal-desc"
            style="font-size: 13px; color: var(--text-2); line-height: 1.6"
          >
            Anda akan menghapus user
            <strong>{{ deleteUserTarget?.name }}</strong> (@{{
              deleteUserTarget?.username
            }}). Aksi ini tidak dapat dibatalkan.
          </div>
        </div>
        <div
          class="revoke-modal-footer"
          style="
            padding: 14px 20px;
            border-top: 1px solid var(--border);
            display: flex;
            justify-content: center;
            gap: 10px;
          "
        >
          <button
            class="btn-sm btn-sm--ghost"
            @click="cancelDeleteUser"
            style="
              background: transparent;
              border: 1px solid var(--border);
              color: var(--text-2);
              cursor: pointer;
              padding: 6px 12px;
              border-radius: var(--r-sm);
              font-size: 13px;
            "
          >
            Batal
          </button>
          <button
            class="btn-sm btn-sm--danger"
            @click="confirmDeleteUser"
            :disabled="deletingUser"
            style="
              background: #ef4444;
              border: 1px solid #ef4444;
              color: #fff;
              cursor: pointer;
              padding: 6px 12px;
              border-radius: var(--r-sm);
              font-size: 13px;
              font-weight: 600;
            "
          >
            {{ deletingUser ? "Menghapus..." : "Ya, Hapus" }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, defineComponent, h, onMounted, watch } from "vue";
import {
  updateProfile,
  changePassword as apiChangePassword,
  getSystemConfig,
  saveSystemConfig,
  getActiveSessions,
  revokeSession as apiRevokeSession,
  getUsers,
  createUser,
  deleteUser,
  getAgentsList,
} from "@/services/api";

const activeSection = ref("profile");
const saved = ref(false);

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



const sessions = ref([]);
const showRevokeModal = ref(false);
const revokeTarget = ref(null);
const loadingRevoke = ref(false);

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
const IconShield = mkI("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z");

const IconUsers = mkI([
  "M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2",
  "M9 11a4 4 0 100-8 4 4 0 000 8z",
  "M23 21v-2a4 4 0 00-3-3.87",
  "M16 3.13a4 4 0 010 7.75",
]);

const sections = [
  { key: "profile", label: "Profile", sub: "Personal info", icon: IconProfile },
  {
    key: "users",
    label: "Users",
    sub: "User & agent accounts",
    icon: IconUsers,
  },
  {
    key: "system",
    label: "System",
    sub: "DB & engine config",
    icon: IconSystem,
  },
  {
    key: "security",
    label: "Security",
    sub: "Password & sessions",
    icon: IconShield,
  },
];

const filteredSections = computed(() => {
  return sections.filter((s) => {
    if (s.key === "system" || s.key === "users") {
      return profile.value.role === "SUPER_ADMIN";
    }
    return true;
  });
});





onMounted(async () => {
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
  if (
    profile.value.role !== "SUPER_ADMIN" &&
    activeSection.value === "system"
  ) {
    activeSection.value = "profile";
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
        maintenanceMode:
          config.maintenanceMode === true || config.maintenanceMode === "true",
      };
    }
  } catch (err) {
    console.error(
      "Gagal memuat konfigurasi sistem dari API, fallback ke localStorage",
      err,
    );
    const storedSystem = localStorage.getItem("vnet_system_config");
    if (storedSystem) {
      try {
        systemConfig.value = {
          ...systemConfig.value,
          ...JSON.parse(storedSystem),
        };
      } catch (e) {}
    }
  }

  await loadSessions();
  if (activeSection.value === "users") {
    loadUsersAndAgents();
  }
});



async function changePassword() {
  if (
    !passwordForm.value.currentPassword ||
    !passwordForm.value.newPassword ||
    !passwordForm.value.confirmPassword
  ) {
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
    sessions.value = sessions.value.filter(
      (s) => s.id !== revokeTarget.value.id,
    );
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
  if (activeSection.value === "profile") {
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

  if (activeSection.value === "system") {
    try {
      await saveSystemConfig(systemConfig.value);
    } catch (err) {
      alert(err.message || "Gagal memperbarui konfigurasi sistem di database");
      return;
    }
  }

  localStorage.setItem(
    "vnet_system_config",
    JSON.stringify(systemConfig.value),
  );

  saved.value = true;
  setTimeout(() => {
    saved.value = false;
  }, 2500);
}

const usersList = ref([]);
const agentsList = ref([]);
const showAddUserModal = ref(false);
const showDeleteUserModal = ref(false);
const deleteUserTarget = ref(null);
const submittingUser = ref(false);
const deletingUser = ref(false);
const addUserError = ref("");
const loadError = ref("");
const newUserForm = ref({
  name: "",
  username: "",
  password: "",
  role: "AGENT",
  agentId: "",
});

const loadUsersAndAgents = async () => {
  loadError.value = "";
  try {
    const uData = await getUsers();
    usersList.value = uData || [];
  } catch (err) {
    console.error("Failed to load users", err);
    loadError.value = "Gagal memuat user: " + (err.message || err);
  }

  try {
    const aData = await getAgentsList();
    agentsList.value = aData || [];
  } catch (err) {
    console.error("Failed to load agents", err);
    loadError.value =
      (loadError.value ? loadError.value + " | " : "") +
      "Gagal memuat agen: " +
      (err.message || err);
  }
};

watch(activeSection, (newSection) => {
  if (newSection === "users") {
    loadUsersAndAgents();
  }
});

const getRoleClass = (role) => {
  if (role === "SUPER_ADMIN") return "role-badge-admin";
  if (role === "STAFF") return "role-badge-staff";
  if (role === "TECHNICIAN") return "role-badge-technician";
  return "role-badge-agent";
};

const openAddUserModal = () => {
  newUserForm.value = {
    name: "",
    username: "",
    password: "",
    role: "AGENT",
    agentId: "",
  };
  addUserError.value = "";
  showAddUserModal.value = true;
};

const closeAddUserModal = () => {
  showAddUserModal.value = false;
};

const submitAddUser = async () => {
  if (
    !newUserForm.value.name.trim() ||
    !newUserForm.value.username.trim() ||
    !newUserForm.value.password.trim()
  ) {
    addUserError.value = "Semua field wajib diisi";
    return;
  }
  if (newUserForm.value.password.length < 6) {
    addUserError.value = "Password minimal 6 karakter";
    return;
  }
  if (newUserForm.value.role === "AGENT" && !newUserForm.value.agentId) {
    addUserError.value = "Pilih Agen wajib diisi jika role adalah AGENT";
    return;
  }

  submittingUser.value = true;
  addUserError.value = "";
  try {
    const payload = {
      name: newUserForm.value.name.trim(),
      username: newUserForm.value.username.trim(),
      password: newUserForm.value.password,
      role: newUserForm.value.role,
    };
    if (newUserForm.value.role === "AGENT") {
      payload.agentId = newUserForm.value.agentId;
    }

    await createUser(payload);
    showAddUserModal.value = false;
    await loadUsersAndAgents();
  } catch (err) {
    addUserError.value = err?.message || err || "Gagal membuat user";
  } finally {
    submittingUser.value = false;
  }
};

const requestDeleteUser = (user) => {
  deleteUserTarget.value = user;
  showDeleteUserModal.value = true;
};

const cancelDeleteUser = () => {
  showDeleteUserModal.value = false;
  deleteUserTarget.value = null;
};

const confirmDeleteUser = async () => {
  if (!deleteUserTarget.value) return;
  deletingUser.value = true;
  try {
    await deleteUser(deleteUserTarget.value.id);
    showDeleteUserModal.value = false;
    await loadUsersAndAgents();
  } catch (err) {
    alert(err?.message || err || "Gagal menghapus user");
  } finally {
    deletingUser.value = false;
    deleteUserTarget.value = null;
  }
};
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
  background: rgba(0, 0, 0, 0.45);
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
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
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
  background: #fff7ed;
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
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
@keyframes popIn {
  from {
    transform: scale(0.94);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
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
  .form-grid {
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



/* Improve active nav with accent */
.snav-item--active {
  background: linear-gradient(
    90deg,
    rgba(13, 19, 64, 0.06) 0%,
    transparent 100%
  );
  border-left: 3px solid var(--navy);
  padding-left: 9px;
}

/* Badges */
.role-badge-admin {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}
.role-badge-staff {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}
.role-badge-agent {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}
.role-badge-technician {
  background: rgba(139, 92, 246, 0.1);
  color: #8b5cf6;
}

/* User table styles */
.users-table th,
.users-table td {
  border-bottom: 1px solid var(--border);
}
.users-table tr:hover {
  background: rgba(0, 0, 0, 0.01);
}
.dark .users-table tr:hover {
  background: rgba(255, 255, 255, 0.02);
}

/* User Modal styling */
.user-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(3px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  animation: fadeIn 0.15s ease-out;
}
.user-modal-card {
  width: 100%;
  max-width: 440px;
  background: var(--card-bg, #fff);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  border: 1px solid var(--border);
  overflow: hidden;
  animation: popIn 0.2s ease-out;
  display: flex;
  flex-direction: column;
}
.user-modal-header {
  padding: 18px 24px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.user-modal-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
}
.user-modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--text-3);
  cursor: pointer;
  line-height: 1;
}
.user-modal-form {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.user-modal-footer {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
