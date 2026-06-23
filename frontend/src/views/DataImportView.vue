<template>
  <div class="data-import">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header__text">
        <h1 class="page-title">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.7"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="title-icon"
          >
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
          Data Import
        </h1>
        <p class="page-sub">
          Provision new customer batches to the core VNet database with
          real-time validation and schema matching.
        </p>
      </div>
    </div>

    <!-- Main Content -->
    <div class="import-layout">
      <!-- Left: Upload + File List -->
      <div class="import-main">
        <!-- Drop Zone -->
        <div
          class="card drop-zone"
          :class="{
            'drop-zone--active': isDragging,
            'drop-zone--has-file': uploadedFile,
          }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
        >
          <div class="drop-zone__inner" v-if="!uploadedFile">
            <div class="drop-icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="16 16 12 12 8 16" />
                <line x1="12" y1="12" x2="12" y2="21" />
                <path d="M20.39 18.39A5 5 0 0018 9h-1.26A8 8 0 103 16.3" />
              </svg>
            </div>
            <h2 class="drop-title">Drop your customer manifest</h2>
            <p class="drop-sub">
              Supported formats: <strong>.xlsx only</strong>. Max file size
              50MB. Up to 1,200+ rows supported.
            </p>
            <label class="btn-browse">
              <input
                type="file"
                accept=".xlsx"
                @change="handleFileInput"
                style="display: none"
              />
              Browse Files
            </label>
          </div>

          <!-- Security Tags -->
          <div class="security-tags">
            <div class="sec-tag">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
              Encrypted Transfer (AES-256)
            </div>
            <div class="sec-tag">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <circle cx="12" cy="12" r="3" />
                <path
                  d="M19.07 4.93l-1.41 1.41M4.93 4.93l1.41 1.41M19.07 19.07l-1.41-1.41M4.93 19.07l1.41-1.41M21 12h-2M5 12H3M12 5V3M12 21v-2"
                />
              </svg>
              Direct Database Sync
            </div>
          </div>

          <!-- File Item -->
          <div class="file-item" v-if="uploadedFile">
            <div class="file-item__icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <line x1="3" y1="9" x2="21" y2="9" />
                <line x1="3" y1="15" x2="21" y2="15" />
                <line x1="9" y1="3" x2="9" y2="21" />
              </svg>
            </div>
            <div class="file-item__info">
              <div class="file-item__name">{{ uploadedFile.name }}</div>
              <div class="file-item__meta">
                {{ uploadedFile.size }} - {{ uploadedFile.rows }} Rows detected
              </div>
            </div>
            <span class="badge badge--teal file-item__badge"
              >PRE-VALIDATED</span
            >
            <button class="file-item__del" @click="removeFile">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" />
                <path d="M10 11v6M14 11v6" />
                <path d="M9 6V4h6v2" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Process Button -->
        <button
          class="btn-process"
          :class="{
            'btn-process--ready': uploadedFile,
            'btn-process--loading': isProcessing,
          }"
          :disabled="!uploadedFile || isProcessing"
          @click="processData"
        >
          <svg
            v-if="!isProcessing"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <polyline points="23 4 23 10 17 10" />
            <polyline points="1 20 1 14 7 14" />
            <path
              d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"
            />
          </svg>
          <svg
            v-else
            class="spin"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          >
            <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" opacity=".3" />
            <path d="M12 3a9 9 0 019 9" />
          </svg>
          {{ isProcessing ? "Synchronizing..." : "Process & Synchronize Data" }}
        </button>

        <div class="success-banner success-banner--error" v-if="importError">
          <div class="success-banner__icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </div>
          <div>
            <div class="success-banner__title">Import Failed</div>
            <div class="success-banner__sub">{{ importError }}</div>
          </div>
        </div>

        <!-- Success Banner -->
        <div class="success-banner" v-if="syncDone">
          <div class="success-banner__icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </div>
          <div>
            <div class="success-banner__title">Preview Ready!</div>
            <div class="success-banner__sub">
              {{ uploadedFile?.rows || 0 }} records validated and ready to
              review.
            </div>
          </div>
        </div>

        <!-- Stats Row -->
        <div class="stats-row">
          <div class="stat-card card">
            <div class="stat-label">Last Activity</div>
            <div class="stat-value">{{ importStats.lastActivityLabel }}</div>
            <div class="stat-sub stat-sub--green">
              {{ importStats.lastActivityDetail }}
            </div>
            <div class="stat-ts">
              Timestamp: {{ importStats.lastTimestamp }}
            </div>
          </div>
          <div class="stat-card card">
            <div class="stat-label">System Queue</div>
            <div class="stat-value">{{ importStats.queueStatus }}</div>
            <div class="stat-sub">{{ importStats.queueDetail }}</div>
            <div class="stat-ts">{{ importStats.queueMeta }}</div>
          </div>
          <div class="stat-card card">
            <div class="stat-label">Storage Quota</div>
            <div class="stat-value">
              {{ importStats.storageUsedLabel }}
              <span class="stat-unit"
                >/ {{ importStats.storageQuotaLabel }} Units</span
              >
            </div>
            <div class="quota-bar">
              <div
                class="quota-bar__fill"
                :style="{ width: importStats.storagePct + '%' }"
              ></div>
            </div>
            <div class="stat-ts">
              {{ importStats.storagePct }}% capacity utilized
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Schema  -->
      <div class="import-sidebar">
        <!-- Required Schema -->
        <div class="card schema-card">
          <div class="schema-header">
            <div class="schema-header__icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="2" y="3" width="20" height="14" rx="2" />
                <line x1="8" y1="21" x2="16" y2="21" />
                <line x1="12" y1="17" x2="12" y2="21" />
              </svg>
            </div>
            <div>
              <div class="schema-title">Required Schema</div>
              <div class="schema-sub">Strict enforcement active</div>
            </div>
          </div>

          <div class="schema-fields">
            <div
              class="schema-field"
              v-for="field in schemaFields"
              :key="field.name"
            >
              <div class="schema-field__check">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <div>
                <div class="schema-field__name">{{ field.name }}</div>
                <div class="schema-field__desc">{{ field.desc }}</div>
              </div>
            </div>
          </div>

          <button
            class="btn-template"
            @click="downloadTemplate"
            :disabled="loadingTemplate"
          >
            <svg
              v-if="loadingTemplate"
              class="btn-spinner"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              width="14"
              height="14"
            >
              <circle
                cx="12"
                cy="12"
                r="10"
                stroke-dasharray="40"
                stroke-dashoffset="10"
              />
            </svg>
            <svg
              v-else
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="16" />
              <line x1="8" y1="12" x2="16" y2="12" />
            </svg>
            {{ loadingTemplate ? "Downloading..." : "Download Template" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  getImportStats,
  previewImport,
  downloadImportTemplate,
} from "@/services/api";

const router = useRouter();
const isDragging = ref(false);
const uploadedRawFile = ref(null);
const uploadedFile = ref(null);
const isProcessing = ref(false);
const syncDone = ref(false);
const importError = ref("");
const loadingTemplate = ref(false);
const importStats = ref({
  lastActivityLabel: "Belum ada import",
  lastActivityDetail: "Upload Excel untuk mulai sinkronisasi data",
  lastTimestamp: "-",
  queueStatus: "Idle",
  queueDetail: "Tidak ada import yang tertunda",
  queueMeta: "ALL IMPORTS SETTLED",
  storageUsedLabel: "0",
  storageQuotaLabel: "50k",
  storagePct: 0,
});

const schemaFields = [
  {
    name: "Nama (Customer Name)",
    desc: "Full name for billing. String format required.",
  },
  {
    name: "Cust ID",
    desc: "Unique identifier (VN-XXXXX format, e.g. VN23481).",
  },
  { name: "Package", desc: "Internet package name (e.g. '20 Mbps (Bageur)')." },
  { name: "Status", desc: "Customer status. Values: Active or Isolir." },
  { name: "Agen", desc: "Agent/reseller name who acquired the customer." },
  {
    name: "Tanggal Registrasi",
    desc: "Registration date. Date format required.",
  },
];

function handleDrop(e) {
  isDragging.value = false;
  const file = e.dataTransfer.files[0];
  if (file) loadFile(file);
}

function handleFileInput(e) {
  const file = e.target.files[0];
  if (file) loadFile(file);
}

function loadFile(file) {
  syncDone.value = false;
  importError.value = "";
  if (!file.name.toLowerCase().endsWith(".xlsx")) {
    importError.value = "File harus format .xlsx.";
    return;
  }
  if (file.size > 50 * 1024 * 1024) {
    importError.value = "Ukuran file maksimal 50MB.";
    return;
  }

  uploadedRawFile.value = file;
  const sizeKB = (file.size / 1024).toFixed(0);
  const sizeMB =
    file.size > 1024 * 1024
      ? (file.size / 1024 / 1024).toFixed(1) + " MB"
      : sizeKB + " KB";
  uploadedFile.value = {
    name: file.name,
    size: sizeMB,
    rows: "Ready",
  };
}

function removeFile() {
  uploadedRawFile.value = null;
  uploadedFile.value = null;
  syncDone.value = false;
  importError.value = "";
}

async function processData() {
  isProcessing.value = true;
  syncDone.value = false;
  importError.value = "";

  try {
    const rows = await previewImport(uploadedRawFile.value);
    sessionStorage.setItem(
      "vnetImportPreview",
      JSON.stringify({
        fileName: uploadedFile.value.name,
        rows,
        createdAt: new Date().toISOString(),
      }),
    );
    uploadedFile.value.rows = rows.length;
    syncDone.value = true;
    await router.push("/data-preview");
  } catch (error) {
    importError.value = error.message || "Gagal memproses file import.";
  } finally {
    isProcessing.value = false;
  }
}

async function downloadTemplate() {
  if (loadingTemplate.value) return;
  loadingTemplate.value = true;
  importError.value = "";
  try {
    const blob = await downloadImportTemplate();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "template_import_pelanggan.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    importError.value = error.message || "Gagal mengunduh template.";
  } finally {
    loadingTemplate.value = false;
  }
}

async function loadImportStats() {
  try {
    importStats.value = {
      ...importStats.value,
      ...(await getImportStats()),
    };
  } catch (error) {
    console.warn("Failed to load import stats", error);
  }
}

onMounted(loadImportStats);
</script>

<style scoped>
.data-import {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

/* Header */
.page-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.page-header__text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.page-breadcrumb {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
  margin-bottom: 4px;
}
.breadcrumb-icon {
  width: 12px;
  height: 12px;
  opacity: 0.6;
}
.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: 0;
  line-height: 1.1;
}
.title-icon {
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  color: var(--navy);
  opacity: 0.75;
}
.page-sub {
  font-size: 13px;
  color: var(--text-2);
  margin-top: 6px;
  max-width: 520px;
  line-height: 1.6;
}

/* Layout */
.import-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  align-items: start;
}
.import-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.import-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Drop Zone */
.drop-zone {
  padding: 0;
  border: 2px dashed var(--border);
  border-radius: var(--r-lg);
  transition:
    border-color 0.2s,
    background 0.2s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 0;
}
.drop-zone--active {
  border-color: var(--navy);
  background: #eef2ff;
}
.drop-zone--has-file {
  border-style: solid;
  border-color: var(--border);
}

.drop-zone__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 48px 32px 32px;
  gap: 14px;
}

.drop-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--r-md);
  background: #eef2ff;
  color: var(--navy);
  display: flex;
  align-items: center;
  justify-content: center;
}
.drop-icon svg {
  width: 30px;
  height: 30px;
}

.drop-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: 0;
}
.drop-sub {
  font-size: 13px;
  color: var(--text-2);
  max-width: 380px;
  line-height: 1.6;
}
.drop-sub strong {
  color: var(--text-1);
}

.btn-browse {
  display: inline-flex;
  align-items: center;
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0;
  padding: 11px 28px;
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: background 0.15s;
  margin-top: 4px;
}
.btn-browse:hover {
  background: var(--navy-mid);
}

/* Security tags */
.security-tags {
  display: flex;
  gap: 0;
  border-top: 1px solid var(--border);
}
.sec-tag {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
  padding: 12px;
  border-right: 1px solid var(--border);
}
.sec-tag:last-child {
  border-right: none;
}
.sec-tag svg {
  width: 13px;
  height: 13px;
}

/* File Item */
.file-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  border-top: 1px solid var(--border);
  background: var(--surface-2);
}
.file-item__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--r-sm);
  background: #e0f8f6;
  color: var(--teal);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.file-item__icon svg {
  width: 20px;
  height: 20px;
}
.file-item__info {
  flex: 1;
}
.file-item__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
}
.file-item__meta {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}
.file-item__badge {
  margin-left: auto;
}
.file-item__del {
  background: none;
  border: none;
  color: var(--text-3);
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--r-sm);
  transition: all 0.15s;
}
.file-item__del:hover {
  background: #fdeceb;
  color: var(--red-warn);
}
.file-item__del svg {
  width: 16px;
  height: 16px;
}

/* Process Button */
.btn-process {
  width: 100%;
  padding: 18px;
  border-radius: var(--r-md);
  border: none;
  background: var(--border);
  color: var(--text-3);
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: not-allowed;
  transition: all 0.2s;
}
.btn-process svg {
  width: 20px;
  height: 20px;
}
.btn-process--ready {
  background: var(--navy);
  color: #fff;
  cursor: pointer;
}
.btn-process--ready:hover {
  background: var(--navy-mid);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}
.btn-process--loading {
  background: var(--navy-mid);
  color: rgba(255, 255, 255, 0.7);
  cursor: wait;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
.spin {
  animation: spin 0.8s linear infinite;
}

/* Success Banner */
.success-banner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: #e8f8ef;
  border: 1px solid #a3e4bc;
  border-radius: var(--r-md);
  animation: slideIn 0.3s ease;
}
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
.success-banner__icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--green-ok);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.success-banner__icon svg {
  width: 18px;
  height: 18px;
}
.success-banner__title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 14px;
  color: var(--green-ok);
}
.success-banner__sub {
  font-size: 12px;
  color: #3d9970;
  margin-top: 2px;
}
.success-banner--error {
  background: #fdeceb;
  border-color: #f5b7b1;
}
.success-banner--error .success-banner__icon {
  background: var(--red-warn);
}
.success-banner--error .success-banner__title {
  color: var(--red-warn);
}
.success-banner--error .success-banner__sub {
  color: #b03a2e;
}

/* Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 14px;
}
.stat-card {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat-label {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
}
.stat-value {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: 0;
}
.stat-unit {
  font-size: 13px;
  color: var(--text-3);
  font-weight: 400;
}
.stat-sub {
  font-size: 11px;
  color: var(--text-2);
}
.stat-sub--green {
  color: var(--green-ok);
}
.stat-ts {
  font-size: 9px;
  color: var(--text-3);
  letter-spacing: 0;
  text-transform: uppercase;
  font-family: var(--font-display);
}

.quota-bar {
  height: 5px;
  background: var(--bg);
  border-radius: 99px;
  overflow: hidden;
  margin: 2px 0;
}
.quota-bar__fill {
  height: 100%;
  background: var(--gold);
  border-radius: 99px;
}

/* Schema Card */
.schema-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.schema-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.schema-header__icon {
  width: 34px;
  height: 34px;
  border-radius: var(--r-sm);
  background: #fef3e0;
  color: var(--gold);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.schema-header__icon svg {
  width: 16px;
  height: 16px;
}
.schema-title {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
  letter-spacing: 0;
  text-transform: uppercase;
}
.schema-sub {
  font-size: 10px;
  color: var(--text-3);
  margin-top: 1px;
}

.schema-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.schema-field {
  display: flex;
  gap: 10px;
}
.schema-field__check {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #e8f8ef;
  color: var(--green-ok);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}
.schema-field__check svg {
  width: 10px;
  height: 10px;
}
.schema-field__name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
}
.schema-field__desc {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
  line-height: 1.5;
}

.btn-template {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: none;
  color: var(--text-2);
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font-display);
  letter-spacing: 0;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-template:hover {
  background: var(--bg);
  color: var(--text-1);
}
.btn-template svg {
  width: 14px;
  height: 14px;
}

/* Responsive Overrides */
@media (max-width: 1024px) {
  .import-layout {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 480px) {
  .drop-zone__inner {
    padding: 32px 16px 24px;
  }
  .drop-title {
    font-size: 18px;
  }
  .security-tags {
    flex-direction: column;
  }
  .sec-tag {
    border-right: none;
    border-bottom: 1px solid var(--border);
  }
  .sec-tag:last-child {
    border-bottom: none;
  }
  .btn-process {
    padding: 14px;
    font-size: 13px;
  }
}

/* Spinner inside button */
.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: var(--text-2);
  animation: spin 0.6s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
