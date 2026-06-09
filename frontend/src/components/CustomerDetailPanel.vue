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
          <div class="panel-avatar" :class="statusClass">
            {{ initials }}
          </div>
          <div>
            <div class="panel-name">{{ customer?.nama || '—' }}</div>
            <div class="panel-custid">{{ customer?.custId }}</div>
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
        <div class="panel-skeleton" v-for="i in 6" :key="i" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="panel-error">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="panel-error-icon">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><circle cx="12" cy="16" r="0.5" fill="currentColor"/>
        </svg>
        <div>Gagal memuat data. Coba lagi.</div>
        <button class="btn btn--primary" @click="loadDetail">Retry</button>
      </div>

      <!-- Content -->
      <div v-else-if="detail" class="panel-body">

        <!-- Status Badge -->
        <div class="panel-status-row">
          <span class="badge" :class="statusBadgeClass">
            <span class="badge-dot" />
            {{ statusLabel }}
          </span>
          <span v-if="detail.isolir" class="badge badge--red">Isolir</span>
          <span class="panel-member-since">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-xs">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
            {{ memberSinceText }}
          </span>
        </div>

        <!-- Section: Info Pribadi -->
        <div class="panel-section">
          <div class="panel-section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            Info Pribadi
          </div>
          <div class="panel-grid">
            <div class="panel-field">
              <div class="panel-field-label">No. Telepon</div>
              <div class="panel-field-value">{{ detail.noTelpon || '—' }}</div>
            </div>
            <div class="panel-field">
              <div class="panel-field-label">Email</div>
              <div class="panel-field-value">{{ detail.email || '—' }}</div>
            </div>
            <div class="panel-field">
              <div class="panel-field-label">Tgl. Registrasi</div>
              <div class="panel-field-value">{{ detail.tanggalRegistrasi || '—' }}</div>
            </div>
            <div class="panel-field">
              <div class="panel-field-label">Tgl. Aktivasi</div>
              <div class="panel-field-value">{{ detail.tanggalAktivasi || '—' }}</div>
            </div>
          </div>
        </div>

        <!-- Section: Paket Internet -->
        <div class="panel-section" v-if="detail.package">
          <div class="panel-section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
              <path d="M5 12.55a11 11 0 0114.08 0"/><path d="M1.42 9a16 16 0 0121.16 0"/><path d="M8.53 16.11a6 6 0 016.95 0"/><circle cx="12" cy="20" r="1"/>
            </svg>
            Paket Internet
          </div>
          <div class="package-card">
            <div class="package-header">
              <div class="package-name">{{ detail.package.name }}</div>
              <div class="package-speed">{{ detail.package.speed ? detail.package.speed + ' Mbps' : '—' }}</div>
            </div>
            <div class="panel-grid">
              <div class="panel-field">
                <div class="panel-field-label">Harga Paket</div>
                <div class="panel-field-value">{{ formatRp(detail.package.price) }}</div>
              </div>
              <div class="panel-field">
                <div class="panel-field-label">Profit/Paket</div>
                <div class="panel-field-value text-green">{{ formatRp(detail.package.profit) }}</div>
              </div>
            </div>
            <div v-if="detail.package.description" class="package-desc">
              {{ detail.package.description }}
            </div>
          </div>
        </div>

        <!-- Section: Status & Monitoring (GPON Fiber Telemetry) -->
        <div class="panel-section">
          <div class="panel-section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            Status & Monitoring
          </div>
          <div class="telemetry-card">
            <div class="telemetry-row">
              <span class="telemetry-label">Router ONT Status</span>
              <span class="telemetry-status" :class="detail.isolir ? 'status--offline' : 'status--online'">
                <span class="status-dot-blink"></span>
                {{ detail.isolir ? 'Offline / Isolir' : 'Online / Connected' }}
              </span>
            </div>
            
            <div class="telemetry-grid">
              <div class="telemetry-field">
                <span class="telemetry-field-lbl">GPON Rx Power</span>
                <span class="telemetry-field-val" :class="detail.isolir ? 'text-warn' : 'text-teal'">{{ detail.isolir ? '—' : '-19.5 dBm' }}</span>
              </div>
              <div class="telemetry-field">
                <span class="telemetry-field-lbl">Ping Latency</span>
                <span class="telemetry-field-val" :class="detail.isolir ? 'text-warn' : 'text-teal'">{{ detail.isolir ? '—' : '12 ms' }}</span>
              </div>
            </div>

            <!-- Bandwidth progress -->
            <div class="bandwidth-wrap">
              <div class="bandwidth-header">
                <span class="bandwidth-title">Kuota Terpakai (Bulan Ini)</span>
                <span class="bandwidth-value">{{ detail.isolir ? '—' : '342.8 GB / Unlimited' }}</span>
              </div>
              <div class="bandwidth-bar-bg">
                <div class="bandwidth-bar-fill" :style="{ width: detail.isolir ? '0%' : '65%' }"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section: Finansial -->
        <div class="panel-section">
          <div class="panel-section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
              <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
            </svg>
            Finansial
          </div>
          <div class="panel-grid panel-grid--3">
            <div class="panel-field panel-field--highlight">
              <div class="panel-field-label">Harga Custom</div>
              <div class="panel-field-value">{{ formatRp(detail.price) }}</div>
            </div>
            <div class="panel-field panel-field--highlight panel-field--green">
              <div class="panel-field-label">Profit</div>
              <div class="panel-field-value text-green">{{ formatRp(detail.profit) }}</div>
            </div>
            <div class="panel-field panel-field--highlight">
              <div class="panel-field-label">Biaya Pasang</div>
              <div class="panel-field-value">{{ formatRp(detail.biayaPasang) }}</div>
            </div>
          </div>
        </div>

        <!-- Section: Alamat -->
        <div class="panel-section" v-if="detail.address">
          <div class="panel-section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
            </svg>
            Alamat
          </div>
          <div class="address-block">
            <div class="address-line" v-if="detail.address.alamat">
              {{ detail.address.alamat }}
              <span v-if="detail.address.rtRw">, RT/RW {{ detail.address.rtRw }}</span>
            </div>
            <div class="address-sub">
              <span v-if="detail.address.kelurahan">{{ detail.address.kelurahan }}</span>
              <span v-if="detail.address.kecamatan"> · {{ detail.address.kecamatan }}</span>
            </div>
            <div class="address-city">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-xs">
                <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
              {{ detail.address.kota || '—' }}
              <span v-if="detail.address.kodePos"> {{ detail.address.kodePos }}</span>
            </div>
          </div>
        </div>

        <!-- Section: Agent -->
        <div class="panel-section" v-if="detail.agent">
          <div class="panel-section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/>
            </svg>
            Agent
          </div>
          <div class="agent-card">
            <div class="agent-avatar">{{ agentInitials }}</div>
            <div class="agent-info">
              <div class="agent-name">{{ detail.agent.nama || '—' }}</div>
              <div class="agent-meta">
                <span v-if="detail.agent.area">{{ detail.agent.area }}</span>
                <span class="badge badge--sm" :class="agentStatusClass">{{ detail.agent.status || '—' }}</span>
              </div>
              <div v-if="detail.agent.noTelpon" class="agent-phone">{{ detail.agent.noTelpon }}</div>
            </div>
          </div>
        </div>

        <!-- Section: Tindakan Cepat -->
        <div class="panel-section actions-section">
          <div class="panel-section-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm">
              <path d="M12 20h9M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </svg>
            Tindakan Cepat
          </div>
          <div class="actions-grid">
            <button class="btn btn--action-status" :class="detail.isolir ? 'btn--activate' : 'btn--isolir'" @click="handleToggleStatus">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="btn-icon">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              {{ detail.isolir ? 'Aktifkan Layanan' : 'Isolir Layanan' }}
            </button>
            
            <button class="btn btn--action-copy" @click="copyCustomerInfo">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="btn-icon">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              </svg>
              Salin Info Pelanggan
            </button>

            <button class="btn btn--action-delete" @click="handleDelete">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="btn-icon">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                <line x1="10" y1="11" x2="10" y2="17"/>
                <line x1="14" y1="11" x2="14" y2="17"/>
              </svg>
              Hapus Pelanggan
            </button>
          </div>
        </div>

      </div><!-- end panel-body -->

    </div><!-- end panel -->
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getCustomerDetail, updateCustomerStatus, deleteCustomer } from '@/services/api'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  visible:  { type: Boolean, default: false },
  customer: { type: Object,  default: null  }
})

const emit = defineEmits(['close', 'refresh'])

const toast   = useToast()
const detail  = ref(null)
const loading = ref(false)
const error   = ref(false)

// Load detail when customer changes
watch(() => props.customer, async (c) => {
  if (!c || !props.visible) return
  await loadDetail()
}, { immediate: true })

watch(() => props.visible, async (v) => {
  if (v && props.customer) await loadDetail()
})

async function loadDetail() {
  if (!props.customer) return
  loading.value = true
  error.value   = false
  detail.value  = null
  try {
    const data = await getCustomerDetail(props.customer.custId)
    detail.value = data
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus() {
  if (!detail.value) return
  try {
    const nextStatus = detail.value.status === 'ACTIVE' ? 'ISOLIR' : 'ACTIVE'
    await updateCustomerStatus(detail.value.id, nextStatus)
    detail.value.status = nextStatus
    detail.value.isolir = nextStatus === 'ISOLIR'
    toast.success(`Status pelanggan diubah menjadi ${nextStatus}`)
    emit('refresh')
  } catch (error) {
    toast.error("Gagal mengubah status: " + error.message)
  }
}

async function handleDelete() {
  if (!detail.value) return
  if (confirm(`Apakah Anda yakin ingin menghapus pelanggan ${detail.value.nama}?`)) {
    try {
      await deleteCustomer(detail.value.id)
      toast.success("Pelanggan berhasil dihapus")
      emit('close')
      emit('refresh')
    } catch (error) {
      toast.error("Gagal menghapus pelanggan: " + error.message)
    }
  }
}

function copyCustomerInfo() {
  if (!detail.value) return
  const addr = detail.value.address
  const addressText = addr ? `${addr.alamat || ''}, RT/RW ${addr.rtRw || ''}, ${addr.kelurahan || ''}, ${addr.kecamatan || ''}, ${addr.kota || ''}` : '—'
  const text = `ID Pelanggan: ${detail.value.custId}
Nama: ${detail.value.nama}
No. Telepon: ${detail.value.noTelpon || '—'}
Email: ${detail.value.email || '—'}
Paket: ${detail.value.package?.name || '—'} (${detail.value.package?.speed || '—'} Mbps)
Alamat: ${addressText}`
  
  navigator.clipboard.writeText(text)
    .then(() => toast.success("Informasi pelanggan berhasil disalin ke clipboard!"))
    .catch(() => toast.error("Gagal menyalin informasi."))
}

const initials = computed(() => {
  const n = props.customer?.nama || ''
  return n.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase() || '?'
})

const agentInitials = computed(() => {
  const n = detail.value?.agent?.nama || ''
  return n.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase() || 'A'
})

const statusClass = computed(() => {
  const s = props.customer?.status
  if (s === 'ACTIVE')  return 'panel-avatar--active'
  if (s === 'ISOLIR')  return 'panel-avatar--isolir'
  return 'panel-avatar--default'
})

const statusBadgeClass = computed(() => {
  const s = props.customer?.status
  if (s === 'ACTIVE')  return 'badge--green'
  if (s === 'ISOLIR')  return 'badge--red'
  return 'badge--gray'
})

const statusLabel = computed(() => {
  const map = { ACTIVE: 'Aktif', ISOLIR: 'Isolir', INACTIVE: 'Tidak Aktif' }
  return map[props.customer?.status] || props.customer?.status || '—'
})

const agentStatusClass = computed(() => {
  const s = detail.value?.agent?.status
  if (s === 'AKTIF')   return 'badge--green'
  if (s === 'NONAKTIF') return 'badge--red'
  return 'badge--gray'
})

const memberSinceText = computed(() => {
  const days = detail.value?.memberSinceDays
  if (!days) return ''
  if (days < 30)   return `${days} hari bergabung`
  if (days < 365)  return `${Math.floor(days/30)} bulan bergabung`
  const y = Math.floor(days/365)
  const m = Math.floor((days % 365) / 30)
  return `${y} thn ${m > 0 ? m + ' bln' : ''} bergabung`
})

function formatRp(val) {
  if (val == null) return '—'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val)
}
</script>

<style scoped>
/* === Transitions === */
.backdrop-enter-active, .backdrop-leave-active { transition: opacity 0.25s ease; }
.backdrop-enter-from, .backdrop-leave-to       { opacity: 0; }

.panel-slide-enter-active, .panel-slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}
.panel-slide-enter-from, .panel-slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* === Backdrop === */
.panel-backdrop {
  position: fixed; inset: 0;
  background: rgba(10, 15, 30, 0.55);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 200;
}

/* === Panel Shell === */
.detail-panel {
  position: fixed;
  top: 0; right: 0; bottom: 0;
  width: min(480px, 100vw);
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
.panel-header-left { display: flex; align-items: center; gap: 14px; }

.panel-avatar {
  width: 46px; height: 46px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 16px; color: #fff;
  flex-shrink: 0;
  font-family: var(--font-display);
}
.panel-avatar--active  { background: linear-gradient(135deg, var(--navy) 0%, var(--navy-mid) 100%); }
.panel-avatar--isolir  { background: linear-gradient(135deg, var(--red-warn) 0%, #C0392B 100%); }
.panel-avatar--default { background: linear-gradient(135deg, var(--text-3) 0%, var(--text-2) 100%); }

.panel-name   { font-size: 16px; font-weight: 700; color: var(--text-1); line-height: 1.3; }
.panel-custid { font-size: 12px; color: var(--text-3); font-family: var(--font-display); margin-top: 2px; }

.panel-close {
  width: 34px; height: 34px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: var(--surface-2);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-3);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.panel-close:hover { background: var(--red-bg); color: var(--red-warn); border-color: var(--red-warn); }
.panel-close svg   { width: 16px; height: 16px; }

/* === Body === */
.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* === Status Row === */
.panel-status-row {
  display: flex; align-items: center; gap: 8px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
}
.panel-member-since {
  margin-left: auto;
  display: flex; align-items: center; gap: 4px;
  font-size: 11px; color: var(--text-3);
}

/* === Section === */
.panel-section {
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}
.panel-section:last-child { border-bottom: none; }

.panel-section-title {
  display: flex; align-items: center; gap: 7px;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.8px;
  color: var(--text-3);
  margin-bottom: 12px;
  font-family: var(--font-display);
}
.icon-sm { width: 14px; height: 14px; flex-shrink: 0; }
.icon-xs { width: 12px; height: 12px; flex-shrink: 0; }

/* === Field Grid === */
.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.panel-grid--3 { grid-template-columns: 1fr 1fr 1fr; }

.panel-field {
  padding: 10px 12px;
  background: var(--surface-2);
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
}
.panel-field--highlight { background: var(--surface-3); }
.panel-field--green     { border-color: var(--green-bg); }

.panel-field-label {
  font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;
  color: var(--text-3); margin-bottom: 4px;
  font-family: var(--font-display);
}
.panel-field-value {
  font-size: 13px; font-weight: 600; color: var(--text-1); word-break: break-all;
}
.text-green { color: var(--green-ok); }

/* === Package Card === */
.package-card {
  background: linear-gradient(135deg, var(--navy) 0%, var(--navy-mid) 100%);
  border-radius: var(--r-md);
  padding: 16px;
  color: #fff;
}
.package-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 14px;
}
.package-name  { font-size: 15px; font-weight: 700; font-family: var(--font-display); }
.package-speed {
  font-size: 12px; font-weight: 600;
  background: rgba(255,255,255,0.15);
  padding: 3px 10px; border-radius: 99px;
  font-family: var(--font-display);
}
.package-card .panel-field {
  background: rgba(255,255,255,0.10);
  border-color: rgba(255,255,255,0.15);
}
.package-card .panel-field-label { color: rgba(255,255,255,0.6); }
.package-card .panel-field-value { color: #fff; }
.package-desc {
  margin-top: 10px; font-size: 12px; color: rgba(255,255,255,0.65);
  font-style: italic; line-height: 1.5;
}

/* === Address === */
.address-block {
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  padding: 14px 16px;
  display: flex; flex-direction: column; gap: 4px;
}
.address-line { font-size: 13px; font-weight: 600; color: var(--text-1); }
.address-sub  { font-size: 12px; color: var(--text-2); }
.address-city {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: var(--text-3); margin-top: 4px;
}

/* === Agent Card === */
.agent-card {
  display: flex; align-items: center; gap: 14px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  padding: 14px 16px;
}
.agent-avatar {
  width: 40px; height: 40px; border-radius: 10px;
  background: linear-gradient(135deg, var(--teal) 0%, var(--cyan) 100%);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 14px; flex-shrink: 0;
}
.agent-name  { font-size: 14px; font-weight: 700; color: var(--text-1); }
.agent-meta  { display: flex; align-items: center; gap: 8px; margin-top: 3px; flex-wrap: wrap; }
.agent-phone { font-size: 12px; color: var(--text-3); margin-top: 3px; }

/* === Badge extras === */
.badge-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: currentColor; flex-shrink: 0;
}
.badge--sm { font-size: 9px; padding: 2px 7px; }

/* === Skeleton === */
.panel-skeleton {
  height: 52px;
  background: linear-gradient(90deg, var(--surface-2) 25%, var(--border) 50%, var(--surface-2) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  border-radius: var(--r-sm);
  margin-bottom: 10px;
}
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* === Error === */
.panel-error {
  flex: 1;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 12px; padding: 40px 24px;
  color: var(--text-3); text-align: center; font-size: 13px;
}
.panel-error-icon { width: 40px; height: 40px; color: var(--red-warn); }

/* === Scrollbar === */
.panel-body::-webkit-scrollbar { width: 4px; }
.panel-body::-webkit-scrollbar-track { background: transparent; }
.panel-body::-webkit-scrollbar-thumb { background: var(--border-2); border-radius: 99px; }

/* === Telemetry Card === */
.telemetry-card {
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.telemetry-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.telemetry-label {
  font-size: 12px;
  color: var(--text-2);
  font-weight: 500;
}
.telemetry-status {
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 99px;
  font-family: var(--font-display);
}
.status--online {
  background: rgba(39, 174, 96, 0.15);
  color: var(--green-ok);
}
.status--offline {
  background: rgba(231, 76, 60, 0.15);
  color: var(--red-warn);
}
.status-dot-blink {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
  display: inline-block;
  animation: blink 1.5s infinite;
}
@keyframes blink {
  0% { opacity: 0.3; }
  50% { opacity: 1; }
  100% { opacity: 0.3; }
}
.telemetry-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.telemetry-field {
  padding: 8px 10px;
  background: var(--surface-3);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.telemetry-field-lbl {
  font-size: 9px;
  color: var(--text-3);
  text-transform: uppercase;
  font-weight: 600;
}
.telemetry-field-val {
  font-size: 13px;
  font-weight: 700;
}
.text-teal { color: var(--teal); }
.text-warn { color: var(--red-warn); }

/* Bandwidth */
.bandwidth-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.bandwidth-header {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
}
.bandwidth-title { color: var(--text-3); }
.bandwidth-value { color: var(--text-1); font-weight: 600; }
.bandwidth-bar-bg {
  height: 6px;
  background: var(--border);
  border-radius: 99px;
  overflow: hidden;
}
.bandwidth-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--teal) 0%, var(--cyan) 100%);
  border-radius: 99px;
  transition: width 0.5s ease;
}

/* Actions Section */
.actions-section {
  padding-bottom: 24px;
}
.actions-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}
.btn--action-status {
  width: 100%;
  font-weight: 700;
  justify-content: center;
  font-family: var(--font-display);
}
.btn--isolir {
  background: rgba(245, 166, 35, 0.1);
  border: 1px solid var(--gold);
  color: var(--gold);
}
.btn--isolir:hover {
  background: var(--gold);
  color: var(--navy);
}
.btn--activate {
  background: rgba(39, 174, 96, 0.1);
  border: 1px solid var(--green-ok);
  color: var(--green-ok);
}
.btn--activate:hover {
  background: var(--green-ok);
  color: #fff;
}
.btn--action-copy {
  width: 100%;
  background: var(--surface-2);
  border: 1px solid var(--border);
  color: var(--text-1);
  font-weight: 600;
  justify-content: center;
  transition: all var(--transition-fast);
}
.btn--action-copy:hover {
  background: var(--surface-3);
  border-color: var(--text-3);
}
.btn--action-delete {
  width: 100%;
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid var(--red-warn);
  color: var(--red-warn);
  font-weight: 700;
  justify-content: center;
  font-family: var(--font-display);
}
.btn--action-delete:hover {
  background: var(--red-warn);
  color: #fff;
}
.btn-icon {
  width: 14px;
  height: 14px;
  stroke-width: 2px;
}
</style>
