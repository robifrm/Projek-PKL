<template>
  <div class="data-preview">

    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header__left">
        <h1 class="page-title">Data Preview</h1>
        <div class="page-meta">
          <span class="batch-tag">Batch #8291-TX</span>
          <span class="batch-phase">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" style="width:13px;height:13px">
              <circle cx="12" cy="12" r="3"/><path d="M19.07 4.93l-1.41 1.41M4.93 4.93l1.41 1.41M19.07 19.07l-1.41-1.41M4.93 19.07l1.41-1.41M21 12h-2M5 12H3M12 5V3M12 21v-2"/>
            </svg>
            Fiber Node Migration Phase 2
          </span>
        </div>
      </div>
      <div class="page-actions">
        <button class="btn btn--primary" :disabled="isCommitting || !sourceRows.length" @click="commitImport">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
          {{ isCommitting ? 'Committing...' : 'Commit Import' }}
        </button>
      </div>
    </div>

    <div class="commit-banner commit-banner--success" v-if="commitResult">
      <strong>Import committed.</strong>
      {{ commitResult.success }} success, {{ commitResult.failed }} failed from {{ commitResult.totalData }} rows.
    </div>
    <div class="commit-banner commit-banner--error" v-if="commitError">
      <strong>Commit failed.</strong>
      {{ commitError }}
    </div>

    <!-- KPI Cards -->
    <div class="kpi-row">
      <div class="card kpi-card" v-for="kpi in kpis" :key="kpi.label">
        <div class="kpi-top">
          <div class="kpi-icon" :style="{ background: kpi.iconBg, color: kpi.iconColor }">
            <component :is="kpi.icon" />
          </div>
          <span class="kpi-delta" :class="kpi.deltaClass">{{ kpi.delta }}</span>
        </div>
        <div class="kpi-value" :style="{ color: kpi.valueColor || 'var(--text-1)' }">{{ kpi.value }}</div>
        <div class="kpi-label">{{ kpi.label }}</div>
      </div>
    </div>

    <!-- Records Table -->
    <div class="card table-card">
      <!-- Tabs + Filters -->
      <div class="table-toolbar">
        <div class="table-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-btn"
            :class="{ 'tab-btn--active': activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <component :is="tab.icon" />
            {{ tab.label }}
          </button>
        </div>
        <div class="toolbar-actions">
          <div class="sort-dropdown">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" class="sort-icon-ui"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>
            <select v-model="sortOption" @change="applySort" class="sort-select">
              <option value="">Default Sort</option>
              <option value="regDate_desc">Reg. Date (Newest)</option>
              <option value="regDate_asc">Reg. Date (Oldest)</option>
              <option value="actDate_desc">Act. Date (Newest)</option>
              <option value="actDate_asc">Act. Date (Oldest)</option>
            </select>
          </div>
          <button class="btn-filter">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg>
            Filters
          </button>
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <table class="records-table">
          <thead>
            <tr>
              <th>Cust ID</th>
              <th>Customer Info</th>
              <th @click="toggleSort('regDate')" class="sortable-th" :class="{ 'is-sorted': sortColumn === 'regDate' }">
                <div class="th-content">
                  Reg. Date
                  <div class="sort-arrows">
                    <svg class="sort-arrow" :class="{ active: sortColumn === 'regDate' && !sortDesc }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                    <svg class="sort-arrow" :class="{ active: sortColumn === 'regDate' && sortDesc }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
                  </div>
                </div>
              </th>
              <th @click="toggleSort('actDate')" class="sortable-th" :class="{ 'is-sorted': sortColumn === 'actDate' }">
                <div class="th-content">
                  Act. Date
                  <div class="sort-arrows">
                    <svg class="sort-arrow" :class="{ active: sortColumn === 'actDate' && !sortDesc }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                    <svg class="sort-arrow" :class="{ active: sortColumn === 'actDate' && sortDesc }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
                  </div>
                </div>
              </th>
              <th>Package</th>
              <th>Status</th>
              <th>Agent</th>
              <th>Action</th>
              <th>Log</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in paginatedRows" :key="row.id" class="record-row">
              <td><span class="cust-id">{{ row.custId }}</span></td>
              <td>
                <div class="cust-name">{{ row.name }}</div>
                <div class="cust-phone">{{ row.phone || '-' }}</div>
                <div class="cust-phone" v-if="row.email" style="opacity: 0.7; font-size: 0.85em;">{{ row.email }}</div>
              </td>
              <td><span class="cust-phone">{{ row.regDate || '-' }}</span></td>
              <td><span class="cust-phone">{{ row.actDate || '-' }}</span></td>
              <td class="pkg-cell">{{ row.package }}</td>
              <td>
                <span class="badge" :class="row.status === 'ACTIVE' ? 'badge--green' : 'badge--gold'">
                  {{ row.status }}
                </span>
              </td>
              <td class="agent-cell">{{ row.agent }}</td>
              <td>
                <span class="action-tag" :class="row.action === 'NEW_RECORD' ? 'action-tag--new' : 'action-tag--update'">
                  {{ row.action }}
                </span>
              </td>
              <td>
                <span class="log-tag" :class="row.logOk ? 'log-tag--ok' : 'log-tag--err'">
                  {{ row.log }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="table-footer">
        <span class="table-count">Displaying <strong>{{ rangeStart }}-{{ rangeEnd }}</strong> of <strong>{{ filteredRows.length }}</strong> entries</span>
        <div class="pagination">
          <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
          </button>
          <button
            v-for="(p, index) in visiblePages" :key="index"
            class="page-btn page-num"
            :class="{ 'page-btn--active': currentPage === p, 'page-btn--dots': p === '...' }"
            :disabled="p === '...'"
            @click="p !== '...' && (currentPage = p)"
          >{{ p }}</button>
          <button class="page-btn" :disabled="currentPage === pageCount" @click="currentPage++">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Bottom Row -->
    <div class="bottom-row">

      <!-- Import Summary by Agent -->
      <div class="card summary-card">
        <div class="summary-header">
          <div class="summary-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <div class="summary-title">Import Summary by Agent</div>
        </div>
        <table class="summary-table">
          <thead>
            <tr>
              <th>Agent</th>
              <th>Records</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in agentSummary" :key="s.agent">
              <td class="s-agent">{{ s.agent }}</td>
              <td class="s-records">{{ s.records }}</td>
              <td><span class="ready-tag">{{ s.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Node Capacity -->
      <div class="card capacity-card">
        <div class="cap-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="8" rx="2"/><rect x="2" y="14" width="20" height="8" rx="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg>
        </div>
        <div class="cap-title">Node Capacity Status</div>
        <p class="cap-desc">
          TX-Central node is at <strong class="cap-warn">82% capacity</strong>, projected post-import: <strong class="cap-warn">89%</strong>.
        </p>
        <button class="cap-link">View Capacity Analysis</button>
      </div>

      <!-- Migration Wizard -->
      <div class="card wizard-card">
        <div class="wizard-tag">Premium Workflow</div>
        <div class="wizard-title">Migration Wizard</div>
        <p class="wizard-desc">Streamline complex multi-node customer shifts with assisted validation.</p>
        <button class="wizard-btn">
          Start Wizard Workflow
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>
        </button>
      </div>

    </div>

    <!-- Validation Engine -->
    <div class="validation-engine card">
      <div class="ve-dot"></div>
      <div>
        <div class="ve-label">Validation Engine</div>
        <div class="ve-status">System Online</div>
      </div>
      <div class="ve-bar-wrap">
        <div class="ve-bar"><div class="ve-bar__fill"></div></div>
        <div class="ve-sub">Real-time processing active</div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, defineComponent, h, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { confirmImport } from '@/services/api'

const router = useRouter()

const activeTab  = ref('all')
const currentPage = ref(1)
const pageSize = 10
const sourceRows = ref([])
const fileName = ref('manual-confirm')
const isCommitting = ref(false)
const commitResult = ref(null)
const commitError = ref('')

// Icon components
const makeIcon = (paths) =>
  defineComponent({ render: () =>
    h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.7', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', style: 'width:14px;height:14px' },
      paths.map(d => h('path', { d }))
    )
  })

const IconDB     = makeIcon(['M12 2C6.48 2 2 4.24 2 7s4.48 5 10 5 10-2.24 10-5-4.48-5-10-5z','M2 7v5c0 2.76 4.48 5 10 5s10-2.24 10-5V7','M2 12v5c0 2.76 4.48 5 10 5s10-2.24 10-5v-5'])
const IconShield = makeIcon(['M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'])
const IconWarn   = makeIcon(['M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z','M12 9v4','M12 17h.01'])
const IconPlus   = makeIcon(['M12 5v14','M5 12h14'])
const IconSync   = makeIcon(['M23 4v6h-6','M1 20v-6h6','M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15'])

const IconList   = defineComponent({ render: () =>
  h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.7', 'stroke-linecap': 'round', style: 'width:13px;height:13px' }, [
    h('line', { x1: '8', y1: '6',  x2: '21', y2: '6'  }),
    h('line', { x1: '8', y1: '12', x2: '21', y2: '12' }),
    h('line', { x1: '8', y1: '18', x2: '21', y2: '18' }),
    h('line', { x1: '3', y1: '6',  x2: '3.01', y2: '6'  }),
    h('line', { x1: '3', y1: '12', x2: '3.01', y2: '12' }),
    h('line', { x1: '3', y1: '18', x2: '3.01', y2: '18' }),
  ])
})
const IconCheck = defineComponent({ render: () =>
  h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.7', 'stroke-linecap': 'round', style: 'width:13px;height:13px' }, [
    h('polyline', { points: '20 6 9 17 4 12' })
  ])
})
const IconAlert = defineComponent({ render: () =>
  h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.7', 'stroke-linecap': 'round', style: 'width:13px;height:13px' }, [
    h('path', { d: 'M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z' }),
    h('line', { x1: '12', y1: '9', x2: '12', y2: '13' }),
    h('line', { x1: '12', y1: '17', x2: '12.01', y2: '17' }),
  ])
})

const kpis = computed(() => {
  const total = allRows.value.length
  const active = allRows.value.filter(row => row.status === 'ACTIVE').length
  const isolir = allRows.value.filter(row => row.status === 'ISOLIR').length
  const inserts = allRows.value.filter(row => row.action === 'NEW_RECORD').length
  const updates = allRows.value.filter(row => row.action === 'UPDATE_ROW').length
  const activeRate = total ? Math.round((active / total) * 100) : 0

  return [
    { label: 'Total Records',  value: total,  delta: `${filteredRows.value.length} shown`, deltaClass: 'delta--up',   iconBg: '#EEF2FF', iconColor: 'var(--navy)',      icon: IconDB     },
    { label: 'Active Records', value: active, delta: `${activeRate}%`, deltaClass: 'delta--teal', iconBg: '#E0F8F6', iconColor: 'var(--teal)',      icon: IconShield, valueColor: 'var(--teal)' },
    { label: 'Isolir Records', value: isolir, delta: `${total ? Math.round((isolir / total) * 100) : 0}%`, deltaClass: 'delta--red',  iconBg: '#FEF3E0', iconColor: 'var(--gold)',      icon: IconWarn,   valueColor: 'var(--gold)' },
    { label: 'New Records',    value: inserts, delta: '',      deltaClass: '',            iconBg: '#EEF2FF', iconColor: 'var(--navy)',      icon: IconPlus   },
    { label: 'Updates',        value: updates, delta: '',      deltaClass: '',            iconBg: '#F4F6FB', iconColor: 'var(--text-3)',    icon: IconSync   },
  ]
})

const tabs = [
  { key: 'all',    label: 'All Records', icon: IconList  },
  { key: 'valid',  label: 'Valid',       icon: IconCheck },
  { key: 'errors', label: 'Errors',      icon: IconAlert },
]


const sortColumn = ref('')
const sortDesc = ref(false)
const sortOption = ref('')

function applySort() {
  if (sortOption.value) {
    const [col, desc] = sortOption.value.split('_')
    sortColumn.value = col
    sortDesc.value = desc === 'desc'
  } else {
    sortColumn.value = ''
    sortDesc.value = false
  }
}

function toggleSort(col) {
  if (sortColumn.value === col) {
    if (sortDesc.value) {
      sortOption.value = ''
    } else {
      sortOption.value = col + '_desc'
    }
  } else {
    sortOption.value = col + '_asc'
  }
  applySort()
}

// allRows starts empty - populated from sessionStorage (uploaded file preview)
const allRows = ref([])

const filteredRows = computed(() => {
  let rows = allRows.value
  if (activeTab.value === 'valid')  rows = allRows.value.filter(r => r.logOk)
  if (activeTab.value === 'errors') rows = allRows.value.filter(r => !r.logOk)
  
  if (sortColumn.value) {
    rows = [...rows].sort((a, b) => {
      let dateA = Date.parse(a[sortColumn.value])
      let dateB = Date.parse(b[sortColumn.value])
      if (isNaN(dateA)) dateA = 0
      if (isNaN(dateB)) dateB = 0
      return sortDesc.value ? dateB - dateA : dateA - dateB
    })
  }
  
  return rows
})

const pageCount = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize)))
const visiblePages = computed(() => {
  const current = currentPage.value
  const last = pageCount.value
  const delta = 2
  const left = current - delta
  const right = current + delta + 1
  const range = []
  const rangeWithDots = []
  let l

  for (let i = 1; i <= last; i++) {
    if (i === 1 || i === last || i >= left && i < right) {
      range.push(i)
    }
  }

  for (let i of range) {
    if (l) {
      if (i - l === 2) {
        rangeWithDots.push(l + 1)
      } else if (i - l !== 1) {
        rangeWithDots.push('...')
      }
    }
    rangeWithDots.push(i)
    l = i
  }

  return rangeWithDots
})

const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredRows.value.slice(start, start + pageSize)
})
const rangeStart = computed(() => filteredRows.value.length ? ((currentPage.value - 1) * pageSize) + 1 : 0)
const rangeEnd = computed(() => Math.min(currentPage.value * pageSize, filteredRows.value.length))

const agentSummary = computed(() => {
  const grouped = new Map()
  allRows.value.forEach(row => {
    const name = row.agent || '-'
    grouped.set(name, (grouped.get(name) || 0) + 1)
  })
  return [...grouped.entries()]
    .map(([agent, records]) => ({ agent, records, status: 'Ready' }))
    .sort((a, b) => b.records - a.records)
    .slice(0, 5)
})

function normalizeKey(value) {
  return String(value || '').toLowerCase().replace(/[^a-z0-9]/g, '')
}

function rowValue(row, keys) {
  const normalizedKeys = keys.map(normalizeKey)
  for (const [key, value] of Object.entries(row)) {
    const cleanKey = normalizeKey(key)
    if (normalizedKeys.includes(cleanKey) && value !== undefined && value !== null && String(value).trim()) {
      return String(value).trim()
    }
  }
  return ''
}

function normalizeStatus(value) {
  return String(value || '').toUpperCase().includes('ISOLIR') ? 'ISOLIR' : 'ACTIVE'
}

function mapPreviewRow(row, index) {
  const actionType = row.actionType === 'INSERT'
    ? 'NEW_RECORD'
    : row.actionType === 'UPDATE'
      ? 'UPDATE_ROW'
      : 'SKIP'

  return {
    id: index + 1,
    custId: rowValue(row, ['Cust ID', 'Customer ID', 'custId']),
    name: rowValue(row, ['Nama', 'Nama (Customer Name)', 'Customer Name', 'Name']),
    phone: rowValue(row, ['No Telpon', 'No Telepon', 'Telepon', 'Phone', 'Nomor HP', 'Nomor Telepon', 'Nomor Telpon']),
    email: rowValue(row, ['Email', 'email']),
    regDate: rowValue(row, ['Tanggal Registrasi', 'Tgl Registrasi', 'Registration Date', 'Tanggal Register']),
    actDate: rowValue(row, ['Tanggal Aktivasi', 'Tgl Aktivasi', 'Tanggal Aktif', 'Activation Date']),
    package: rowValue(row, ['Package', 'Paket', 'Paket Internet', 'Service']),
    status: normalizeStatus(rowValue(row, ['Status', 'Customer Status'])),
    agent: rowValue(row, ['Agen', 'Agent', 'Nama Agen']),
    action: actionType,
    log: row.errorMessage || 'Schema valid',
    logOk: row.validationStatus !== 'ERROR',
  }
}



async function commitImport() {
  isCommitting.value = true
  commitError.value = ''
  commitResult.value = null

  try {
    commitResult.value = await confirmImport({ fileName: fileName.value, rows: sourceRows.value })
    sessionStorage.removeItem('vnetImportPreview')
  } catch (error) {
    commitError.value = error.message || 'Gagal commit import.'
  } finally {
    isCommitting.value = false
  }
}

onMounted(() => {
  const cached = sessionStorage.getItem('vnetImportPreview')
  if (!cached) {
    // No preview data - redirect back to import so user uploads a file first
    router.replace('/data-import')
    return
  }

  try {
    const payload = JSON.parse(cached)
    sourceRows.value = payload.rows || []
    fileName.value = payload.fileName || 'manual-confirm'
    allRows.value = sourceRows.value.map(mapPreviewRow)
  } catch (error) {
    console.warn('Failed to load import preview', error)
    router.replace('/data-import')
  }
})

watch([activeTab, filteredRows], () => {
  currentPage.value = Math.min(currentPage.value, pageCount.value)
  if (currentPage.value < 1) currentPage.value = 1
})
</script>

<style scoped>
.data-preview { display: flex; flex-direction: column; gap: 20px; }

/* Header */
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-title  { font-family: var(--font-display); font-size: 28px; font-weight: 800; color: var(--text-1); letter-spacing: 0; }
.page-meta   { display: flex; align-items: center; gap: 10px; margin-top: 8px; }

.batch-tag {
  background: var(--navy); color: #fff;
  font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 0; padding: 4px 10px; border-radius: 6px;
}
.batch-phase {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: var(--text-2);
}

.page-actions { display: flex; gap: 10px; }
.btn {
  display: flex; align-items: center; gap: 7px;
  padding: 9px 18px; border-radius: var(--r-sm);
  font-size: 13px; font-weight: 500; border: none; cursor: pointer;
  transition: all .15s;
}
.btn svg { width: 15px; height: 15px; }
.btn--ghost { background: var(--surface); border: 1px solid var(--border); color: var(--text-1); }
.btn--ghost:hover { background: var(--bg); }
.btn--primary { background: var(--navy); color: #fff; font-family: var(--font-display); font-weight: 700; letter-spacing: 0; }
.btn--primary:hover { background: var(--navy-mid); }
.btn:disabled { opacity: .55; cursor: not-allowed; }

.commit-banner {
  padding: 12px 16px;
  border-radius: var(--r-sm);
  font-size: 13px;
  line-height: 1.5;
}
.commit-banner--success {
  background: #E8F8EF;
  border: 1px solid #A3E4BC;
  color: #1E8449;
}
.commit-banner--error {
  background: #FDECEB;
  border: 1px solid #F5B7B1;
  color: #B03A2E;
}

/* KPI Row */
.kpi-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; }
.kpi-card { padding: 18px; display: flex; flex-direction: column; gap: 8px; }
.kpi-top  { display: flex; align-items: center; justify-content: space-between; }
.kpi-icon { width: 32px; height: 32px; border-radius: var(--r-sm); display: flex; align-items: center; justify-content: center; }
.delta--up   { font-size: 11px; font-weight: 700; color: var(--green-ok); background: #E8F8EF; padding: 2px 7px; border-radius: 99px; font-family: var(--font-display); }
.delta--teal { font-size: 11px; font-weight: 700; color: var(--teal);     background: #E0F8F6; padding: 2px 7px; border-radius: 99px; font-family: var(--font-display); }
.delta--red  { font-size: 11px; font-weight: 700; color: var(--red-warn); background: #FDECEB; padding: 2px 7px; border-radius: 99px; font-family: var(--font-display); }
.kpi-value { font-family: var(--font-display); font-size: 30px; font-weight: 800; letter-spacing: 0; line-height: 1; }
.kpi-label { font-size: 10px; font-weight: 600; letter-spacing: 0; text-transform: uppercase; color: var(--text-3); font-family: var(--font-display); }

/* Table Card */
.table-card { padding: 0; overflow: hidden; }
.table-toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid var(--border-color); }
.toolbar-actions { display: flex; align-items: center; gap: 12px; }

.sort-dropdown {
  position: relative;
  display: flex; align-items: center;
  background: var(--bg-2);
  border: 1px solid var(--border-color);
  border-radius: var(--r-sm);
  padding: 0 10px;
  height: 34px;
}
.sort-icon-ui { width: 14px; height: 14px; color: var(--text-2); margin-right: 6px; }
.sort-select {
  background: transparent; border: none; font-size: 12px; font-weight: 500;
  color: var(--text-1); outline: none; cursor: pointer;
  padding-right: 5px;
}
.sort-select:focus { color: var(--navy); }

.table-tabs { display: flex; gap: 4px; }
.tab-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; border-radius: var(--r-sm);
  border: 1px solid transparent;
  background: none; font-size: 12px; font-weight: 500;
  color: var(--text-2); cursor: pointer; font-family: var(--font-body);
  transition: all .15s;
}
.tab-btn:hover { background: var(--bg); }
.tab-btn--active {
  background: var(--surface);
  border-color: var(--border);
  color: var(--text-1);
  font-weight: 600;
  box-shadow: var(--shadow-sm);
}
.btn-filter {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; border-radius: var(--r-sm);
  border: 1px solid var(--border); background: none;
  font-size: 12px; font-weight: 500; color: var(--text-2);
  cursor: pointer; font-family: var(--font-body); transition: all .15s;
}
.btn-filter svg { width: 14px; height: 14px; }
.btn-filter:hover { background: var(--bg); }

/* Table */
.records-table { width: 100%; border-collapse: collapse; }
.records-table th { padding: 12px 20px; text-align: left; font-size: 11px; font-weight: 700; color: var(--text-3); text-transform: uppercase; letter-spacing: .5px; border-bottom: 1px solid var(--border-color); white-space: nowrap; }
.sortable-th { cursor: pointer; user-select: none; transition: color 0.2s, background 0.2s; }
.sortable-th:hover { color: var(--navy); background: rgba(0,0,0,0.01); }
.sortable-th.is-sorted { color: var(--navy); }
.th-content { display: flex; align-items: center; gap: 6px; }
.sort-arrows { display: flex; flex-direction: column; gap: 1px; opacity: 0.3; transition: opacity 0.2s; }
.sortable-th:hover .sort-arrows { opacity: 0.6; }
.sortable-th.is-sorted .sort-arrows { opacity: 1; }
.sort-arrow { width: 9px; height: 9px; color: currentColor; opacity: 0.3; }
.sort-arrow.active { opacity: 1; }
.record-row:hover { background: #f9f9f9; }
.records-table td { padding: 14px 20px; border-bottom: 1px solid var(--border); font-size: 13px; vertical-align: middle; }
.record-row:last-child td { border-bottom: none; }
.record-row:hover td { background: var(--bg); }

.cust-id { font-family: var(--font-display); font-weight: 700; font-size: 12px; color: var(--navy); }
.cust-name { font-weight: 600; color: var(--text-1); font-size: 13px; }
.cust-phone { font-size: 11px; color: var(--text-3); margin-top: 2px; }
.pkg-cell { color: var(--text-2); font-size: 12px; }
.agent-cell { color: var(--text-2); font-size: 12px; font-weight: 500; }

.action-tag {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 0; padding: 3px 8px; border-radius: 4px;
}
.action-tag--new    { background: #EEF2FF; color: var(--navy); }
.action-tag--update { background: #FEF3E0; color: var(--gold); }

.log-tag { font-size: 11px; font-weight: 500; }
.log-tag--ok  { color: var(--teal); }
.log-tag--err { color: var(--red-warn); }

/* Pagination */
.table-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-top: 1px solid var(--border);
}
.table-count { font-size: 12px; color: var(--text-3); }
.table-count strong { color: var(--text-1); }
.pagination  { display: flex; gap: 4px; align-items: center; }
.page-btn {
  width: 32px; height: 32px; border-radius: var(--r-sm);
  border: 1px solid var(--border); background: none;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 13px; font-weight: 600; color: var(--text-2);
  font-family: var(--font-display); transition: all .15s;
}
.page-btn svg { width: 14px; height: 14px; }
.page-btn:hover:not(:disabled) { background: var(--bg); }
.page-btn:disabled { opacity: .3; cursor: default; }
.page-btn--active { background: var(--navy); color: #fff; border-color: var(--navy); }

/* Bottom Row */
.bottom-row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; }

/* Summary Card */
.summary-card { padding: 20px; }
.summary-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.summary-icon {
  width: 32px; height: 32px; border-radius: var(--r-sm);
  background: var(--bg); color: var(--text-2);
  display: flex; align-items: center; justify-content: center;
}
.summary-icon svg { width: 16px; height: 16px; }
.summary-title { font-family: var(--font-display); font-size: 13px; font-weight: 700; color: var(--text-1); text-transform: uppercase; letter-spacing: 0; }
.summary-table { width: 100%; border-collapse: collapse; }
.summary-table th {
  font-size: 9px; font-weight: 700; letter-spacing: 0; text-transform: uppercase;
  color: var(--text-3); font-family: var(--font-display); text-align: left;
  padding: 0 0 8px; border-bottom: 1px solid var(--border);
}
.summary-table td { padding: 10px 0; border-bottom: 1px solid var(--border); font-size: 13px; }
.summary-table tr:last-child td { border-bottom: none; }
.s-agent { font-weight: 500; color: var(--text-1); }
.s-records { color: var(--text-2); }
.ready-tag { color: var(--green-ok); font-size: 12px; font-weight: 700; font-family: var(--font-display); }

/* Capacity Card */
.capacity-card { padding: 20px; display: flex; flex-direction: column; gap: 10px; }
.cap-icon {
  width: 36px; height: 36px; border-radius: var(--r-sm);
  background: var(--bg); color: var(--text-2);
  display: flex; align-items: center; justify-content: center;
}
.cap-icon svg { width: 18px; height: 18px; }
.cap-title { font-family: var(--font-display); font-size: 14px; font-weight: 700; color: var(--text-1); }
.cap-desc  { font-size: 12px; color: var(--text-2); line-height: 1.6; }
.cap-warn  { color: var(--gold); }
.cap-link  { background: none; border: none; color: var(--gold); font-size: 12px; font-weight: 700; font-family: var(--font-display); cursor: pointer; text-align: left; padding: 0; }

/* Wizard Card */
.wizard-card {
  background: var(--navy); border-color: transparent;
  padding: 22px; display: flex; flex-direction: column; gap: 10px;
}
.wizard-tag  { font-size: 9px; font-weight: 700; letter-spacing: 0; text-transform: uppercase; color: rgba(255,255,255,.5); font-family: var(--font-display); }
.wizard-title{ font-family: var(--font-display); font-size: 20px; font-weight: 800; color: #fff; }
.wizard-desc { font-size: 12px; color: rgba(255,255,255,.55); line-height: 1.6; }
.wizard-btn {
  display: flex; align-items: center; gap: 8px;
  background: var(--gold); color: var(--navy);
  font-family: var(--font-display); font-size: 12px; font-weight: 800;
  letter-spacing: 0; text-transform: uppercase;
  padding: 10px 16px; border-radius: var(--r-sm); border: none;
  cursor: pointer; margin-top: 4px; transition: background .15s;
}
.wizard-btn:hover { background: var(--gold-light); }
.wizard-btn svg { width: 15px; height: 15px; }

/* Validation Engine */
.validation-engine {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 20px; position: fixed;
  bottom: 20px; left: calc(var(--sidebar-w) + 20px);
  width: 220px; box-shadow: var(--shadow-md);
  z-index: 100;
}
.ve-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--green-ok);
  box-shadow: 0 0 0 3px rgba(39,174,96,.2);
  flex-shrink: 0;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%,100% { box-shadow: 0 0 0 3px rgba(39,174,96,.2); }
  50%      { box-shadow: 0 0 0 6px rgba(39,174,96,.06); }
}
.ve-label  { font-size: 9px; font-weight: 700; letter-spacing: 0; text-transform: uppercase; color: var(--text-3); font-family: var(--font-display); }
.ve-status { font-family: var(--font-display); font-size: 13px; font-weight: 700; color: var(--green-ok); }
.ve-bar-wrap { flex: 1; }
.ve-bar { height: 4px; background: var(--bg); border-radius: 99px; overflow: hidden; }
.ve-bar__fill { height: 100%; width: 65%; background: var(--green-ok); border-radius: 99px; animation: progress 2s ease-in-out infinite; }
@keyframes progress { 0% { width: 40%; } 50% { width: 80%; } 100% { width: 40%; } }
.ve-sub { font-size: 9px; color: var(--text-3); margin-top: 4px; }

.table-wrap {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

/* Responsive Overrides */
@media (max-width: 1024px) {
  .kpi-row {
    grid-template-columns: 1fr 1fr;
  }
  .bottom-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .toolbar-actions {
    width: 100%;
    justify-content: space-between;
  }
  .table-tabs {
    width: 100%;
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 4px;
  }
  .validation-engine {
    display: none !important;
  }
}
@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .page-actions {
    width: 100%;
  }
  .page-actions .btn {
    width: 100%;
    justify-content: center;
  }
  .kpi-row {
    grid-template-columns: 1fr;
  }
  .table-footer {
    flex-direction: column;
    gap: 12px;
    align-items: center;
    text-align: center;
  }
}
</style>
