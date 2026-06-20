<template>
  <!-- Overlay backdrop -->
  <Transition name="backdrop">
    <div v-if="visible" class="panel-backdrop" @click="$emit('close')" />
  </Transition>

  <!-- Slide-over panel from right -->
  <Transition name="panel-slide">
    <div v-if="visible" class="detail-panel" role="dialog" aria-modal="true">

      <!-- Panel Header -->
      <div class="panel-header">
        <div class="panel-header-left">
          <div
            class="panel-avatar"
            :style="{ background: agent?.color || 'var(--navy)' }"
          >
            {{ initials }}
          </div>
          <div>
            <div class="panel-name">{{ agent?.name || '—' }}</div>
            <div class="panel-custid">{{ agent?.type }}</div>
          </div>
        </div>
        <button class="panel-close" @click="$emit('close')" aria-label="Close">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="panel-body">
        <div class="panel-skeleton" v-for="i in 5" :key="i" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="panel-error">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="panel-error-icon">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><circle cx="12" cy="16" r="0.5" fill="currentColor"/>
        </svg>
        <div style="font-weight: 600;">Gagal memuat data customer.</div>
        <div v-if="errorMessage" style="font-size: 12px; color: var(--text-3); margin-top: 4px; word-break: break-all;">{{ errorMessage }}</div>
        <button class="btn btn--primary" @click="loadCustomers" style="margin-top: 8px;">Retry</button>
      </div>

      <!-- Content -->
      <div v-else class="panel-body">
        
        <!-- Metrics Row -->
        <div class="metrics-grid">
          <div class="metric-mini-card">
            <div class="metric-mini-val">{{ agent?.customers }}</div>
            <div class="metric-mini-lbl">Customers</div>
          </div>
          <div class="metric-mini-card">
            <div class="metric-mini-val text-green">{{ agent?.active }}</div>
            <div class="metric-mini-lbl">Active</div>
          </div>
          <div class="metric-mini-card">
            <div class="metric-mini-val" :class="{ 'text-gold': agent?.isolirRate > 15 }">
              {{ agent?.isolirRate }}%
            </div>
            <div class="metric-mini-lbl">Isolir Rate</div>
          </div>
          <div class="metric-mini-card">
            <div class="metric-mini-val text-navy">{{ agent?.commission }}</div>
            <div class="metric-mini-lbl">
              {{ agent?.isCompany ? 'Revenue Sharing' : 'Commission' }}
            </div>
          </div>
        </div>

        <!-- Section: Customers List -->
        <div class="panel-section" style="border-bottom: none; flex: 1; display: flex; flex-direction: column;">
          <div class="panel-section-title-row">
            <div class="panel-section-title" style="margin-bottom: 0;">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
                <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/>
              </svg>
              Assigned Customers ({{ filteredCustomers.length }})
            </div>
            
            <!-- Inline Search -->
            <div class="panel-search-field" v-if="customers.length > 0">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="icon-xs">
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <input
                type="text"
                v-model="searchQuery"
                placeholder="Cari customer..."
                class="panel-search-input"
              />
            </div>
          </div>

          <!-- Customers Scroller -->
          <div class="customers-scroller" style="flex: 1; margin-top: 14px;">
            <div v-if="customers.length === 0" class="panel-empty-state">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
              </svg>
              <p>Agen ini belum memiliki customer yang terdaftar.</p>
            </div>
            
            <div v-else-if="filteredCustomers.length === 0" class="panel-empty-state">
              <p>Tidak ada customer yang cocok dengan pencarian.</p>
            </div>

            <div v-else class="customer-items-list">
              <div v-for="c in filteredCustomers" :key="c.id" class="cust-item-card">
                <div class="cust-item-header">
                  <div class="cust-item-avatar">
                    {{ customerInitials(c.nama) }}
                  </div>
                  <div class="cust-item-identity">
                    <div class="cust-item-name" :title="c.nama">{{ c.nama }}</div>
                    <div class="cust-item-id">{{ c.custId }}</div>
                  </div>
                  <span
                    class="badge badge--sm"
                    :class="c.status === 'ACTIVE' ? 'badge--green' : 'badge--red'"
                  >
                    {{ c.status === 'ACTIVE' ? 'Active' : 'Isolir' }}
                  </span>
                </div>
                
                <div class="cust-item-detail-row">
                  <div class="cust-item-pkg-info">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-xs text-navy">
                      <path d="M5 12.55a11 11 0 0114.08 0"/><path d="M1.42 9a16 16 0 0121.16 0"/><circle cx="12" cy="20" r="1"/>
                    </svg>
                    <span class="cust-item-pkg-name">{{ c.pkg?.name || '—' }}</span>
                    <small class="cust-item-pkg-speed">({{ c.pkg?.speed }} Mbps)</small>
                  </div>
                  <div class="cust-item-price-val">
                    {{ formatRp(c.price || c.pkg?.price) }}
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>

      </div>

    </div>
  </Transition>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { getAgentCustomers } from "@/services/api";

const props = defineProps({
  visible: Boolean,
  agent: Object, // The performance row agent object
  period: String, // The selected period, e.g. "2025-12"
});

const emit = defineEmits(["close"]);

const loading = ref(false);
const error = ref(false);
const errorMessage = ref("");
const customers = ref([]);
const searchQuery = ref("");

watch(
  [() => props.visible, () => props.period, () => props.agent?.id],
  async ([newVisible]) => {
    if (newVisible && props.agent?.id) {
      await loadCustomers();
    } else if (!newVisible) {
      searchQuery.value = "";
    }
  }
);

async function loadCustomers() {
  loading.value = true;
  error.value = false;
  errorMessage.value = "";
  try {
    const data = await getAgentCustomers(props.agent.id, props.period);
    customers.value = data || [];
  } catch (err) {
    console.error("Gagal mengambil customer agent:", err);
    error.value = true;
    errorMessage.value = err.message || "Terjadi kesalahan koneksi.";
  } finally {
    loading.value = false;
  }
}

const initials = computed(() => {
  const n = props.agent?.name || "";
  return n
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0])
    .join("")
    .toUpperCase() || "A";
});

function customerInitials(name) {
  const n = name || "";
  return n
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0])
    .join("")
    .toUpperCase() || "?";
}

const filteredCustomers = computed(() => {
  if (!searchQuery.value.trim()) return customers.value;
  const q = searchQuery.value.toLowerCase().trim();
  return customers.value.filter(
    (c) =>
      c.nama?.toLowerCase().includes(q) ||
      c.custId?.toLowerCase().includes(q) ||
      c.pkg?.name?.toLowerCase().includes(q)
  );
});

function formatRp(val) {
  if (val == null) return "—";
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(val);
}
</script>

<style scoped>
/* === Transitions === */
.backdrop-enter-active,
.backdrop-leave-active {
  transition: opacity 0.25s ease;
}
.backdrop-enter-from,
.backdrop-leave-to {
  opacity: 0;
}

.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}
.panel-slide-enter-from,
.panel-slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* === Backdrop === */
.panel-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(10, 15, 30, 0.55);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 200;
}

/* === Panel Shell === */
.detail-panel {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: min(520px, 100vw);
  background: var(--surface);
  border-left: 1px solid var(--border);
  box-shadow: var(--shadow-xl);
  z-index: 201;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* === Header === */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
  background: var(--surface);
}
.panel-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.panel-avatar {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  color: #fff;
  flex-shrink: 0;
  font-family: var(--font-display);
}

.panel-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
  line-height: 1.3;
}
.panel-custid {
  font-size: 12px;
  color: var(--text-3);
  font-family: var(--font-display);
  margin-top: 2px;
}

.panel-close {
  width: 34px;
  height: 34px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: var(--surface-2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.panel-close:hover {
  background: var(--red-bg);
  color: var(--red-warn);
  border-color: var(--red-warn);
}
.panel-close svg {
  width: 16px;
  height: 16px;
}

/* === Body === */
.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* === Section === */
.panel-section {
  padding: 16px 0;
  border-top: 1px solid var(--border);
}

.panel-section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.panel-section-title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--text-3);
  font-family: var(--font-display);
}
.icon-sm {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}
.icon-xs {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}

/* === Search input === */
.panel-search-field {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 0 10px;
}
.panel-search-field svg {
  color: var(--text-3);
}
.panel-search-input {
  height: 32px;
  border: none;
  outline: none;
  background: none;
  font-size: 12px;
  color: var(--text-1);
  width: 160px;
}

/* === Mini metrics grid === */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.metric-mini-card {
  padding: 14px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  text-align: center;
}
.metric-mini-val {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 800;
  color: var(--text-1);
  line-height: 1.2;
}
.metric-mini-lbl {
  font-size: 9px;
  font-weight: 700;
  color: var(--text-3);
  text-transform: uppercase;
  margin-top: 4px;
  font-family: var(--font-display);
}

/* === Scroller and list === */
.customers-scroller {
  overflow-y: auto;
}
.customer-items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cust-item-card {
  padding: 14px;
  border: 1px solid var(--border);
  background: var(--surface-2);
  border-radius: var(--r-md);
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: all var(--transition-fast);
}
.cust-item-card:hover {
  border-color: var(--navy);
  background: var(--bg);
}

.cust-item-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cust-item-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--navy-mid);
  color: #fff;
  font-weight: 700;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
}
.cust-item-identity {
  flex: 1;
  min-width: 0;
}
.cust-item-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cust-item-id {
  font-size: 11px;
  color: var(--text-3);
  font-family: var(--font-display);
  margin-top: 1px;
}

.cust-item-detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px dashed var(--border);
}
.cust-item-pkg-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-2);
}
.cust-item-pkg-name {
  font-weight: 600;
}
.cust-item-pkg-speed {
  color: var(--text-3);
}
.cust-item-price-val {
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 800;
  color: var(--text-1);
}

/* === Empty/Skeleton === */
.panel-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-3);
  gap: 12px;
}
.panel-empty-state svg {
  width: 40px;
  height: 40px;
}
.panel-empty-state p {
  font-size: 12px;
}

.panel-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  gap: 14px;
  text-align: center;
  color: var(--red-warn);
}
.panel-error-icon {
  width: 48px;
  height: 48px;
}

.panel-skeleton {
  height: 90px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  position: relative;
  overflow: hidden;
  margin-bottom: 12px;
}
.panel-skeleton::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  transform: translateX(-100%);
  animation: loading-shimmer 1.5s infinite;
}

@keyframes loading-shimmer {
  100% {
    transform: translateX(100%);
  }
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 99px;
  font-size: 10px;
  font-weight: 800;
  font-family: var(--font-display);
  white-space: nowrap;
}
.badge--sm {
  padding: 2px 7px;
  font-size: 9px;
}
.badge--green {
  background: #e8f8ef;
  color: var(--green-ok);
}
.badge--red {
  background: #fdeceb;
  color: var(--red-warn);
}
</style>
