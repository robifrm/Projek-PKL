<template>
  <div class="packages page-fade-in">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Packages Management</h1>
        <p class="page-sub">Manage connectivity plans, pricing, speeds, and profitability metrics.</p>
      </div>
      <div class="page-actions" v-if="user.role !== 'AGENT'">
        <button class="btn btn--primary" @click="openAddModal">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span class="btn-text">Add Package</span>
        </button>
      </div>
    </div>

    <div class="charts-row" style="margin-bottom: 20px" v-if="loading">
      <div class="card chart-card">
        <div class="chart-header">
          <div><div class="skeleton skeleton-text--lg" style="width:140px"></div><div class="skeleton skeleton-text--sm" style="margin-top:4px;width:180px"></div></div>
        </div>
        <div class="skeleton" style="height:320px; width:100%; border-radius:12px"></div>
      </div>
      <div class="card chart-card">
        <div class="chart-header">
          <div><div class="skeleton skeleton-text--lg" style="width:140px"></div><div class="skeleton skeleton-text--sm" style="margin-top:4px;width:180px"></div></div>
        </div>
        <div class="skeleton" style="height:320px; width:100%; border-radius:12px"></div>
      </div>
    </div>
    <div class="charts-row" style="margin-bottom: 20px" v-else-if="revPkgData.labels.length > 0">
      <!-- Revenue by Package -->
      <div class="card chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-title">Revenue by Package</div>
            <div class="chart-sub">Total revenue contribution per package</div>
          </div>
        </div>
        <div class="chart-wrap" style="height: 320px; padding: 20px; position: relative;">
          <Bar :data="revenuePackageData" :options="revPkgOptions" />
        </div>
      </div>

      <!-- Package by Status Stacked Bar -->
      <div class="card chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-title">Package by Status</div>
            <div class="chart-sub">Active vs Isolir subscribers per package</div>
          </div>
        </div>
        <div class="chart-wrap" style="height: 320px; padding: 20px; position: relative;">
          <Bar :data="packageStatusData" :options="stackedBarOptions" />
        </div>
      </div>
    </div>

    <!-- Search & Filter Card -->
    <div class="card filter-card">
      <div class="search-input-wrap">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
        </svg>
        <input type="text" v-model="searchQuery" placeholder="Cari paket berdasarkan nama atau deskripsi..." class="search-field" />
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="card section-card">
      <div class="table-wrap">
        <table class="pkg-table">
          <thead>
            <tr>
              <th><div class="skeleton skeleton-text--sm" style="width:100px"></div></th>
              <th><div class="skeleton skeleton-text--sm" style="width:60px"></div></th>
              <th><div class="skeleton skeleton-text--sm" style="width:80px"></div></th>
              <th><div class="skeleton skeleton-text--sm" style="width:80px"></div></th>
              <th><div class="skeleton skeleton-text--sm" style="width:60px"></div></th>
              <th><div class="skeleton skeleton-text--sm" style="width:50px"></div></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="i in 5" :key="i">
              <td colspan="6"><div class="skeleton" style="height:48px; width:100%; border-radius:6px"></div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredPackages.length === 0" class="empty-state card">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="empty-icon">
        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
        <line x1="3.27" y1="6.96" x2="12" y2="12.01" />
        <line x1="12" y1="22.08" x2="12" y2="12" />
      </svg>
      <h3>Belum Ada Paket Internet</h3>
      <p>Tidak ada paket internet yang cocok dengan kata kunci pencarian Anda atau database masih kosong.</p>
      <button class="btn btn--primary" @click="openAddModal" style="margin-top: 14px;" v-if="user.role !== 'AGENT'">Tambah Paket Baru</button>
    </div>

    <!-- Packages Table -->
    <div v-else class="card section-card">
      <div class="table-wrap">
        <table class="pkg-table">
          <thead>
            <tr>
              <th class="th-sort" @click="togglePkgSort('name')">
                Package Name
                <span class="sort-icon">
                  <svg v-if="pkgSortKey === 'name'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline :points="pkgSortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'" /></svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12" /></svg>
                </span>
              </th>
              <th class="th-sort" @click="togglePkgSort('speed')">
                Speed
                <span class="sort-icon">
                  <svg v-if="pkgSortKey === 'speed'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline :points="pkgSortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'" /></svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12" /></svg>
                </span>
              </th>
              <th class="th-sort" @click="togglePkgSort('price')">
                Price (Rp)
                <span class="sort-icon">
                  <svg v-if="pkgSortKey === 'price'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline :points="pkgSortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'" /></svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12" /></svg>
                </span>
              </th>
              <th class="th-sort" @click="togglePkgSort('profit')">
                PPN 11% (Rp)
                <span class="sort-icon">
                  <svg v-if="pkgSortKey === 'profit'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline :points="pkgSortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'" /></svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12" /></svg>
                </span>
              </th>
              <th>Description</th>
              <th v-if="user.role !== 'AGENT'">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in paginatedPackages" :key="p.id" class="table-row-hover">
              <td class="td-name">{{ p.name }}</td>
              <td class="td-speed">
                <span class="badge-speed">{{ p.speed }} Mbps</span>
              </td>
              <td class="td-price">{{ formatPrice(p.price) }}</td>
              <td class="td-profit" style="color: var(--text-2); font-weight: 500;">
                {{ formatPrice(Math.round(p.price * 0.11)) }}
              </td>
              <td class="td-detail">{{ p.description || '-' }}</td>
              <td class="td-actions" v-if="user.role !== 'AGENT'">
                <button class="btn-icon btn-edit" title="Edit" @click="openEditModal(p)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </button>
                <button class="btn-icon btn-delete" title="Hapus" @click="handleDelete(p.id, p.name)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    <line x1="10" y1="11" x2="10" y2="17"></line>
                    <line x1="14" y1="11" x2="14" y2="17"></line>
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Package Table Footer -->
      <div class="table-footer">
        <div class="footer-left">
          <span class="table-count">Showing <strong>{{ (pkgPage-1)*pkgItemsPerPage+1 }}–{{ Math.min(pkgPage*pkgItemsPerPage, filteredPackages.length) }}</strong> of <strong>{{ filteredPackages.length }}</strong> packages</span>
          <select class="rows-select" v-model="pkgItemsPerPage" @change="pkgPage=1">
            <option :value="10">10 / page</option>
            <option :value="25">25 / page</option>
            <option :value="50">50 / page</option>
          </select>
        </div>
        <div class="pagination">
          <button class="page-btn" :disabled="pkgPage === 1" @click="pkgPage--">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="15 18 9 12 15 6" /></svg>
          </button>
          <span class="page-btn page-num page-btn--active" style="cursor:default">{{ pkgPage }}</span>
          <button class="page-btn" :disabled="pkgPage >= pkgTotalPages" @click="pkgPage++">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6" /></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Compact Modal for Add/Edit -->
    <Teleport to="body">
      <div class="compact-modal-backdrop" v-if="showModal" @click.self="closeModal">
        <div class="compact-modal-card">
          <div class="compact-modal-header">
            <h2 class="compact-modal-title">{{ isEdit ? "Edit Paket Internet" : "Tambah Paket" }}</h2>
            <button class="btn-close" @click="closeModal">&times;</button>
          </div>
          <form @submit.prevent="handleSubmit">
            <div class="compact-modal-body">
              <div class="form-group">
                <label class="form-label">Nama Paket</label>
                <input type="text" v-model="form.name" class="form-input" required />
              </div>
              
              <div class="form-row">
                <div class="form-group flex-1">
                  <label class="form-label">Speed (Mbps)</label>
                  <input type="number" step="any" v-model.number="form.speed" class="form-input" required />
                </div>
                <div class="form-group flex-1">
                  <label class="form-label">Harga (Rp)</label>
                  <input type="number" step="any" v-model.number="form.price" @input="form.profit = Math.round(form.price * 0.11)" class="form-input" required />
                </div>
                <div class="form-group flex-1">
                  <label class="form-label">PPN 11% (Rp)</label>
                  <input type="number" :value="form.price ? Math.round(form.price * 0.11) : 0" class="form-input" disabled style="background: var(--bg-hover, rgba(0,0,0,0.03)); cursor: not-allowed;" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Deskripsi</label>
                <input type="text" v-model="form.description" class="form-input" placeholder="Opsional" />
              </div>

              <div v-if="errorMsg" class="form-error">{{ errorMsg }}</div>
            </div>
            <div class="compact-modal-footer">
              <button type="button" class="btn btn--secondary" @click="closeModal">Batal</button>
              <button type="submit" class="btn btn--primary" :disabled="loadingSubmit">Simpan</button>
            </div>
          </form>
        </div>
      </div>
      <!-- Delete Confirm Modal -->
      <div class="compact-modal-backdrop" v-if="showDeleteModal" @click.self="cancelDelete">
        <div class="compact-modal-card delete-modal-card">
          <div class="delete-modal-icon-wrap">
            <div class="delete-modal-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="28" height="28">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" />
                <path d="M10 11v6M14 11v6" />
                <path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2" />
              </svg>
            </div>
          </div>
          <div class="delete-modal-body">
            <div class="delete-modal-title">Hapus Paket Internet</div>
            <div class="delete-modal-desc">
              Anda akan menghapus paket
              <strong>"{{ deleteTarget.name }}"</strong>.
              Tindakan ini permanen dan tidak dapat dibatalkan.
            </div>
          </div>
          <div class="delete-modal-footer">
            <button class="btn btn--secondary" @click="cancelDelete">Batal</button>
            <button class="btn btn--danger" @click="confirmDelete" :disabled="loadingDelete">
              <svg v-if="loadingDelete" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14">
                <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" />
              </svg>
              {{ loadingDelete ? 'Menghapus...' : 'Ya, Hapus' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { Bar } from "vue-chartjs";
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip } from "chart.js";
import { getPackages, savePackage, deletePackage, getDashboardOverview } from "@/services/api";
import { useToast } from "@/composables/useToast";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip);

const toast = useToast();

const user = ref({ role: "" });

const loadUserProfile = () => {
  const stored = localStorage.getItem("vnet_user");
  if (stored) {
    try {
      user.value = JSON.parse(stored);
    } catch (e) {}
  }
};

const packages = ref([]);
const loading = ref(true);
const searchQuery = ref("");
const pkgSortKey = ref('');
const pkgSortDir = ref('asc');
const pkgPage = ref(1);
const pkgItemsPerPage = ref(10);
const revPkgData = ref({ labels: [], data: [], colors: [] });
const pkgStatusRawData = ref({ labels: [], active: [], isolir: [] });

function togglePkgSort(key) {
  if (pkgSortKey.value === key) {
    pkgSortDir.value = pkgSortDir.value === 'asc' ? 'desc' : 'asc';
  } else {
    pkgSortKey.value = key;
    pkgSortDir.value = 'asc';
  }
  pkgPage.value = 1;
}

// Modal & Form state
const showModal = ref(false);
const isEdit = ref(false);
const showDeleteModal = ref(false);
const loadingDelete = ref(false);
const deleteTarget = ref({ id: null, name: '' });
const loadingSubmit = ref(false);
const errorMsg = ref("");

const form = ref({
  id: null,
  name: "",
  speed: null,
  price: null,
  profit: null,
  description: ""
});

const handleTopbarSearch = (e) => { searchQuery.value = e.detail || ''; };

onMounted(() => {
  loadUserProfile();
  fetchData();
  window.addEventListener('topbar-search', handleTopbarSearch);
});

onUnmounted(() => {
  window.removeEventListener('topbar-search', handleTopbarSearch);
});

const fetchData = async () => {
  loading.value = true;
  try {
    const [res, overview] = await Promise.all([
      getPackages(),
      getDashboardOverview().catch(() => ({}))
    ]);
    packages.value = res || [];
    
    if (overview && overview.revenueByPackage) {
      revPkgData.value = {
        labels: overview.revenueByPackage.map(p => p.name.length > 14 ? p.name.slice(0, 12) + "…" : p.name),
        data: overview.revenueByPackage.map(p => Number(p.revenue || 0)),
        colors: overview.revenueByPackage.map(p => p.color)
      };
    }
    if (overview && overview.packages) {
      pkgStatusRawData.value = {
        labels: overview.packages.map(p => p.name.length > 14 ? p.name.slice(0, 12) + "…" : p.name),
        active: overview.packages.map(p => Number(p.active || 0)),
        isolir: overview.packages.map(p => Number(p.isolir || 0))
      };
    }
  } catch (err) {
    console.error("Gagal memuat data:", err);
  } finally {
    loading.value = false;
  }
};

const revenuePackageData = computed(() => ({
  labels: revPkgData.value.labels,
  datasets: [{
    label: "Revenue",
    data: revPkgData.value.data,
    backgroundColor: revPkgData.value.colors,
    borderRadius: 8,
    borderSkipped: false,
  }],
}));

const packageStatusData = computed(() => ({
  labels: pkgStatusRawData.value.labels,
  datasets: [
    {
      label: "Active",
      data: pkgStatusRawData.value.active,
      backgroundColor: "#00B4A6",
      borderRadius: { topLeft: 0, topRight: 0, bottomLeft: 4, bottomRight: 4 },
    },
    {
      label: "Isolir",
      data: pkgStatusRawData.value.isolir,
      backgroundColor: "#F5A623",
      borderRadius: { topLeft: 4, topRight: 4, bottomLeft: 0, bottomRight: 0 },
    }
  ]
}));

const revPkgOptions = {
  responsive: true, maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const v = ctx.parsed.y;
          if (v >= 1_000_000) return `Rp ${(v/1_000_000).toFixed(1)} Jt`;
          if (v >= 1_000) return `Rp ${Math.round(v/1_000)} Rb`;
          return `Rp ${v}`;
        }
      }
    }
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: { font: { family: "Inter", size: 10 }, color: "#9BA3BF" },
      offset: true,
    },
    y: { display: false, beginAtZero: true },
  },
  datasets: {
    bar: {
      maxBarThickness: 56,
      barPercentage: 0.55,
      categoryPercentage: 0.7,
    }
  },
};

const stackedBarOptions = {
  responsive: true, maintainAspectRatio: false,
  plugins: {
    legend: { display: true, position: 'top', labels: { boxWidth: 10, usePointStyle: true, pointStyle: 'circle' } },
  },
  scales: {
    x: {
      stacked: true,
      grid: { display: false },
      border: { display: false },
      ticks: { font: { family: "Inter", size: 10 }, color: "#9BA3BF" },
      offset: true,
    },
    y: { stacked: true, display: false, beginAtZero: true },
  },
  datasets: {
    bar: {
      maxBarThickness: 56,
      barPercentage: 0.55,
      categoryPercentage: 0.7,
    }
  },
};

const filteredPackages = computed(() => {
  let list = packages.value;
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase().trim();
    list = list.filter(p =>
      p.name.toLowerCase().includes(q) ||
      (p.description && p.description.toLowerCase().includes(q))
    );
  }
  // Sort
  if (pkgSortKey.value) {
    list = [...list].sort((a, b) => {
      let av = a[pkgSortKey.value];
      let bv = b[pkgSortKey.value];
      if (['speed','price','profit'].includes(pkgSortKey.value)) { av = Number(av)||0; bv = Number(bv)||0; }
      else if (typeof av === 'string') { av = av.toLowerCase(); bv = (bv||'').toLowerCase(); }
      if (av < bv) return pkgSortDir.value === 'asc' ? -1 : 1;
      if (av > bv) return pkgSortDir.value === 'asc' ? 1 : -1;
      return 0;
    });
  }
  return list;
});

const paginatedPackages = computed(() => {
  const start = (pkgPage.value - 1) * pkgItemsPerPage.value;
  return filteredPackages.value.slice(start, start + pkgItemsPerPage.value);
});

const pkgTotalPages = computed(() => Math.ceil(filteredPackages.value.length / pkgItemsPerPage.value) || 1);

const formatPrice = (value) => {
  return new Intl.NumberFormat("id-ID", { style: "currency", currency: "IDR", maximumFractionDigits: 0 }).format(value);
};

const openAddModal = () => {
  isEdit.value = false;
  errorMsg.value = "";
  form.value = {
    id: null,
    name: "",
    speed: null,
    price: null,
    profit: null,
    description: ""
  };
  showModal.value = true;
};

const openEditModal = (pkg) => {
  isEdit.value = true;
  errorMsg.value = "";
  form.value = {
    id: pkg.id,
    name: pkg.name,
    speed: pkg.speed,
    price: pkg.price,
    profit: Math.round(pkg.price * 0.11),
    description: pkg.description || ""
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  if (!form.value.name || form.value.speed == null || form.value.price == null || form.value.profit == null) {
    errorMsg.value = "Mohon lengkapi semua kolom wajib (*)";
    return;
  }

  loadingSubmit.value = true;
  errorMsg.value = "";
  try {
    await savePackage(form.value);
    closeModal();
    fetchData();
  } catch (err) {
    errorMsg.value = err.message || "Gagal menyimpan paket internet.";
  } finally {
    loadingSubmit.value = false;
  }
};

const handleDelete = (id, name) => {
  deleteTarget.value = { id, name };
  showDeleteModal.value = true;
};

const confirmDelete = async () => {
  loadingDelete.value = true;
  try {
    await deletePackage(deleteTarget.value.id);
    packages.value = packages.value.filter((p) => p.id !== deleteTarget.value.id);
    toast.success(`Paket "${deleteTarget.value.name}" berhasil dihapus`);
    showDeleteModal.value = false;
  } catch (err) {
    toast.error("Gagal menghapus paket: " + (err.message || err));
  } finally {
    loadingDelete.value = false;
  }
};

const cancelDelete = () => {
  showDeleteModal.value = false;
  deleteTarget.value = { id: null, name: '' };
};
</script>

<style scoped>
.packages {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.page-fade-in {
  animation: fadeIn 0.4s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Header */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.page-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.5px;
}
.page-sub {
  font-size: 13px;
  color: var(--text-2);
  margin-top: 4px;
}

/* Actions */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 600;
  border: none;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}
.btn svg {
  width: 16px;
  height: 16px;
  stroke-width: 2.5px;
}
.btn--primary {
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  box-shadow: 0 4px 12px rgba(13, 19, 64, 0.15);
}
.btn--primary:hover {
  background: var(--navy-mid);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 19, 64, 0.22);
}
.btn--secondary {
  background: var(--bg);
  color: var(--text-2);
  border: 1px solid var(--border);
}
.btn--secondary:hover {
  background: var(--border);
  color: var(--text-1);
}
.btn--danger {
  background: #ef4444;
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  gap: 6px;
}
.btn--danger:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.35);
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* Delete Confirm Modal */
.delete-modal-card {
  max-width: 400px;
  text-align: center;
  padding: 0;
}
.delete-modal-icon-wrap {
  padding: 28px 20px 16px;
  display: flex;
  justify-content: center;
}
.delete-modal-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #FEF2F2;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ef4444;
}
.delete-modal-body {
  padding: 0 24px 20px;
}
.delete-modal-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 800;
  color: var(--text-1);
  margin-bottom: 8px;
}
.delete-modal-desc {
  font-size: 13.5px;
  color: var(--text-2);
  line-height: 1.6;
}
.delete-modal-desc strong {
  color: var(--text-1);
}
.delete-modal-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: center;
  gap: 10px;
}
.delete-modal-footer .btn {
  min-width: 110px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.btn-spinner {
  animation: spin 0.8s linear infinite;
}

/* Charts */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.chart-card {
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  overflow: hidden;
}
.chart-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}
.chart-title {
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
}
.chart-sub {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}

/* Filter Card */
.filter-card {
  padding: 14px 18px;
  display: flex;
  align-items: center;
}
.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 9px 14px;
  width: 100%;
  max-width: 460px;
  transition: border-color 0.2s;
}
.search-input-wrap:focus-within {
  border-color: var(--navy);
  box-shadow: 0 0 0 3px rgba(13, 19, 64, 0.06);
}
.search-input-wrap svg {
  width: 16px;
  height: 16px;
  color: var(--text-3);
}
.search-field {
  background: none;
  border: none;
  outline: none;
  font-size: 13px;
  color: var(--text-1);
  width: 100%;
}
.search-field::placeholder {
  color: var(--text-3);
}

/* Section Card */
.section-card {
  padding: 0;
  overflow: hidden;
}

.table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  width: 100%;
}
.pkg-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}
.pkg-table th {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
  text-align: left;
  padding: 14px 18px;
  border-bottom: 2px solid var(--border);
  background: var(--surface-2);
}
.pkg-table td {
  padding: 16px 18px;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
  vertical-align: middle;
  color: var(--text-2);
}
.pkg-table tr:last-child td {
  border-bottom: none;
}
.table-row-hover:hover {
  background-color: var(--surface-2);
}

.td-name {
  font-weight: 700;
  color: var(--text-1);
}
.badge-speed {
  background: var(--navy);
  color: #fff;
  padding: 4px 10px;
  border-radius: var(--r-sm);
  font-size: 11px;
  font-weight: 700;
}
.td-price {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--navy);
}

.td-actions {
  display: flex;
  gap: 8px;
}
.btn-icon {
  background: none;
  border: none;
  color: var(--text-3);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--r-sm);
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.btn-icon svg {
  width: 16px;
  height: 16px;
}
.btn-edit:hover {
  background: rgba(0, 180, 166, 0.08);
  color: var(--teal);
}
.btn-delete:hover {
  background: rgba(231, 76, 60, 0.08);
  color: var(--red-warn);
}

/* States */
.loading-state, .empty-state {
  padding: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 12px;
}
.loading-state p {
  font-size: 13px;
  color: var(--text-2);
}
.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(13, 19, 64, 0.1);
  border-radius: 50%;
  border-top-color: var(--navy);
  animation: spin 1s ease-in-out infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: var(--text-3);
  margin-bottom: 8px;
}
.empty-state h3 {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 18px;
  color: var(--text-1);
}
.empty-state p {
  max-width: 440px;
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.6;
}

/* Compact Modal */
.compact-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  animation: fadeIn 0.15s ease-out;
}
.compact-modal-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  overflow: hidden;
  animation: popIn 0.2s ease-out;
}
@keyframes popIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.compact-modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.compact-modal-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
}
.btn-close {
  background: none;
  border: none;
  font-size: 22px;
  color: var(--text-3);
  cursor: pointer;
  line-height: 1;
}
.compact-modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.compact-modal-footer {
  padding: 14px 20px;
  background: var(--surface-2);
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-row {
  display: flex;
  gap: 12px;
}
.flex-1 {
  flex: 1;
}
.form-label {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-2);
}
.form-input {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 8px 12px;
  font-size: 13px;
  color: var(--text-1);
}
.form-input:focus {
  border-color: var(--navy);
  box-shadow: 0 0 0 2px rgba(13, 19, 64, 0.05);
  outline: none;
}

/* Spinner inside button */
.btn-spinner-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 0.6s linear infinite;
}

/* Responsive Overrides */
@media (max-width: 1024px) {
  .charts-row {
    grid-template-columns: 1fr !important;
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
  .form-row {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
