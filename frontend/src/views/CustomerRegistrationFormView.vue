<template>
  <div class="registration-page-wrapper">
    <div class="registration-container">
      <!-- Top Header (Outside the form card) -->
      <div v-if="!success" class="reg-header">
        <div class="reg-header-left">
          <h1 class="reg-title">Registrasi Pelanggan</h1>
          <p class="reg-subtitle">
            Lengkapi data berikut untuk proses pendaftaran layanan internet.
          </p>
        </div>
        <div class="reg-badge">
          <span class="badge-dot"></span>
          Customer Registration
        </div>
      </div>

      <!-- Main Content Card with Blue Top Border -->
      <div class="main-form-card" :class="{ 'main-form-card--success': success }">
        <!-- Success View (Updated matching screenshot 3) -->
        <div v-if="success" class="new-success-container animated fadeIn">
          <!-- Top Header Header block -->
          <div class="new-success-header">
            <div class="new-success-logo-wrap">
              <!-- SVG Gelo logo matching VNet branding -->
              <svg class="new-success-logo-svg" viewBox="0 0 240 80" fill="none">
                <path d="M40 50 C20 50, 10 35, 20 20 C30 5, 50 10, 50 25 C50 35, 45 42, 38 45" stroke="#0ea5e9" stroke-width="8" stroke-linecap="round"/>
                <path d="M42 22 L26 22 C20 22, 20 30, 26 30 L42 30" stroke="#0284c7" stroke-width="6" stroke-linecap="round"/>
                <text x="75" y="45" fill="#0ea5e9" font-family="'Outfit', sans-serif" font-size="34" font-weight="900">GELO</text>
                <text x="75" y="62" fill="#475569" font-family="'Outfit', sans-serif" font-size="13" font-weight="600" letter-spacing="1">By VNet</text>
              </svg>
            </div>
            <div class="new-success-header-text">
              <h2 class="nsh-title">Registrasi Berhasil Terkirim</h2>
              <p class="nsh-subtitle">Terima kasih telah melakukan registrasi. Silakan menunggu informasi dari tim kami.</p>
            </div>
          </div>

          <!-- Main Inner Card Box -->
          <div class="new-success-card">
            <div class="nsc-status-row">
              <div class="nsc-check-badge">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" class="nsc-check-icon">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </div>
              <div class="nsc-status-text">
                <h3 class="nsct-title">Pengajuan kamu sudah kami terima <span class="checkmark-emoji">✅</span></h3>
                <p class="nsct-subtitle">Tim kami akan memverifikasi data dan akan segera menghubungi anda melalui nomor HP / email yang kamu isi.</p>
              </div>
            </div>

            <!-- Status Indicator Panel -->
            <div class="nsc-status-panel">
              <div class="nscp-label">Status</div>
              <div class="nscp-value">Menunggu verifikasi tim</div>
            </div>

            <!-- Action Button -->
            <div class="nsc-actions">
              <button class="btn btn-back-home" @click="resetForm">
                Kembali ke Halaman Utama
              </button>
            </div>

            <!-- Footer Note -->
            <div class="nsc-footer-note">
              Estimasi follow up <strong>1 &times; 24 jam</strong> (tergantung jam operasional &amp; antrian). Pastikan nomor HP kamu aktif, ya.
            </div>
          </div>
        </div>

        <!-- Form View -->
        <form v-else @submit.prevent="submitForm">
          <!-- Step 1: Identitas & Kontak -->
          <div class="step-card">
            <div class="step-header">
              <div class="step-number-circle">1</div>
              <div>
                <h2 class="step-title">Identitas & Kontak</h2>
                <p class="step-subtitle-text">Silahkan isi data dengan benar</p>
              </div>
            </div>

            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label"
                  >First Name <span class="required">*</span></label
                >
                <input
                  id="firstName"
                  v-model="form.firstName"
                  type="text"
                  class="form-input"
                  placeholder="Nama Depan"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label"
                  >Last Name <span class="required">*</span></label
                >
                <input
                  id="lastName"
                  v-model="form.lastName"
                  type="text"
                  class="form-input"
                  placeholder="Nama Belakang"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label"
                >Email <span class="required">*</span></label
              >
              <input
                id="email"
                v-model="form.email"
                type="email"
                class="form-input"
                placeholder="Masukkan alamat email"
                required
              />
            </div>

            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label"
                  >Jenis Identitas <span class="required">*</span></label
                >
                <div class="select-wrapper">
                  <select
                    id="jenisIdentitas"
                    v-model="form.jenisIdentitas"
                    class="form-input select-input"
                    required
                  >
                    <option value="" disabled selected>- pilih -</option>
                    <option value="KTP">KTP</option>
                    <option value="Passport">Passport</option>
                    <option value="Tax_id">Tax_id (NPWP)</option>
                    <option value="Kitas">Kitas</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label"
                  >Nomor Identitas <span class="required">*</span></label
                >
                <input
                  id="nomorIdentitas"
                  v-model="form.nomorIdentitas"
                  @input="onIdentitasInput"
                  type="text"
                  class="form-input"
                  placeholder="Masukkan nomor identitas"
                  required
                />
              </div>
            </div>

            <!-- Dynamic Identity Document Upload -->
            <transition name="upload-slide">
              <div v-if="form.jenisIdentitas" class="upload-section">
                <label class="form-label">
                  Upload {{ identityUploadLabel }}
                  <span class="required">*</span>
                  <span class="upload-hint">(JPG/PNG, maks. 3MB)</span>
                </label>
                <div
                  class="upload-dropzone"
                  :class="{
                    'upload-dropzone--dragging': isDragging,
                    'upload-dropzone--has-file': uploadedFile,
                  }"
                  @dragover.prevent="isDragging = true"
                  @dragleave.prevent="isDragging = false"
                  @drop.prevent="onFileDrop"
                  @click="triggerFileInput"
                >
                  <input
                    ref="fileInputRef"
                    type="file"
                    accept="image/jpeg,image/png"
                    class="upload-file-input"
                    @change="onFileChange"
                  />

                  <!-- No file state -->
                  <div v-if="!uploadedFile" class="upload-empty-state">
                    <div class="upload-icon-wrap">
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="1.5"
                        class="upload-icon"
                      >
                        <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
                        <polyline points="17 8 12 3 7 8" />
                        <line x1="12" y1="3" x2="12" y2="15" />
                      </svg>
                    </div>
                    <p class="upload-title">Drag &amp; drop file di sini</p>
                    <p class="upload-sub">
                      atau
                      <span class="upload-browse-link">klik untuk browse</span>
                    </p>
                    <p class="upload-types">JPG, PNG &bull; Maks. 3MB</p>
                  </div>

                  <!-- File preview state -->
                  <div v-else class="upload-preview-state" @click.stop>
                    <div class="upload-preview-icon-wrap">
                      <svg
                        v-if="uploadedFileIsImage"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="1.5"
                        class="upload-prev-icon uploaded"
                      >
                        <rect
                          x="3"
                          y="3"
                          width="18"
                          height="18"
                          rx="2"
                          ry="2"
                        />
                        <circle cx="8.5" cy="8.5" r="1.5" />
                        <polyline points="21 15 16 10 5 21" />
                      </svg>
                      <svg
                        v-else
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="1.5"
                        class="upload-prev-icon uploaded"
                      >
                        <path
                          d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"
                        />
                        <polyline points="14 2 14 8 20 8" />
                      </svg>
                    </div>
                    <div class="upload-preview-info">
                      <p class="upload-filename">{{ uploadedFile.name }}</p>
                      <p class="upload-filesize">
                        {{ formatFileSize(uploadedFile.size) }}
                      </p>
                    </div>
                    <button
                      type="button"
                      class="upload-remove-btn"
                      @click.stop="removeFile"
                    >
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <line x1="18" y1="6" x2="6" y2="18" />
                        <line x1="6" y1="6" x2="18" y2="18" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </transition>

            <!-- Mandatory notice matching reference image exactly -->
            <div class="form-grid-2" style="margin-top: 14px">
              <div class="form-group">
                <label class="form-label">Nomor Seluler Utama</label>
                <div class="phone-input-group">
                  <span class="phone-prefix">+62</span>
                  <input
                    id="nomorSelulerUtama"
                    v-model="form.nomorSelulerUtama"
                    @input="onPhoneInput($event, 'nomorSelulerUtama')"
                    type="tel"
                    class="form-input phone-field"
                    placeholder="contoh: 81234567890"
                    required
                  />
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">Nomor Cadangan (Opsional)</label>
                <div class="phone-input-group">
                  <span class="phone-prefix">+62</span>
                  <input
                    id="nomorCadangan"
                    v-model="form.nomorCadangan"
                    @input="onPhoneInput($event, 'nomorCadangan')"
                    type="tel"
                    class="form-input phone-field"
                    placeholder="contoh: 83164747713"
                  />
                </div>
              </div>
            </div>
            
            <div class="form-group" style="margin-top: 14px">
              <label class="form-label">Afiliator (Opsional)</label>
              <input
                id="afiliator"
                v-model="form.afiliator"
                type="text"
                class="form-input"
                placeholder="Masukkan nama afiliator (jika ada)"
              />
            </div>
          </div>

          <!-- Step 2: Alamat (Wajib) -->
          <div class="step-card">
            <div class="step-header">
              <div class="step-number-circle">2</div>
              <div>
                <h2 class="step-title">Alamat (Wajib)</h2>
                <p class="step-subtitle-text">
                  Masukkan alamat pemasangan layanan internet secara lengkap
                </p>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label"
                >Detail Alamat (Jalan/No/Patokan)
                <span class="required">*</span></label
              >
              <textarea
                id="alamatDetail"
                v-model="form.alamatDetail"
                class="form-input textarea-input"
                rows="3"
                placeholder="Masukkan detail alamat lengkap"
                required
              ></textarea>
            </div>

            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label">RT</label>
                <input
                  id="rt"
                  v-model="form.rt"
                  @input="onRtInput"
                  type="text"
                  class="form-input"
                  placeholder="001"
                />
              </div>
              <div class="form-group">
                <label class="form-label">RW</label>
                <input
                  id="rw"
                  v-model="form.rw"
                  @input="onRwInput"
                  type="text"
                  class="form-input"
                  placeholder="002"
                />
              </div>
            </div>

            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label"
                  >Kelurahan/Desa <span class="required">*</span></label
                >
                <input
                  id="kelurahan"
                  v-model="form.kelurahan"
                  type="text"
                  class="form-input"
                  placeholder="Masukkan kelurahan/desa"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label"
                  >Kecamatan <span class="required">*</span></label
                >
                <input
                  id="kecamatan"
                  v-model="form.kecamatan"
                  type="text"
                  class="form-input"
                  placeholder="Masukkan kecamatan"
                  required
                />
              </div>
            </div>

            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label"
                  >Kota/Kabupaten <span class="required">*</span></label
                >
                <input
                  id="kota"
                  v-model="form.kota"
                  type="text"
                  class="form-input"
                  placeholder="Masukkan kota/kabupaten"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label"
                  >Provinsi <span class="required">*</span></label
                >
                <input
                  id="provinsi"
                  v-model="form.provinsi"
                  type="text"
                  class="form-input"
                  placeholder="Masukkan provinsi"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label"
                >Kode Pos <span class="required">*</span></label
              >
              <input
                id="kodePos"
                v-model="form.kodePos"
                type="text"
                class="form-input"
                placeholder="Masukkan kode pos"
                required
              />
            </div>

            <div class="form-group" style="margin-top: 14px">
              <label class="form-label">Titik Koordinat <span class="required">*</span></label>
              <input
                id="koordinat"
                v-model="form.koordinat"
                type="text"
                class="form-input"
                placeholder="contoh: -6.914744, 106.926574"
                required
              />
            </div>
          </div>

          <!-- Step 3: Paket Internet -->
          <div class="step-card">
            <div class="step-header">
              <div class="step-number-circle">3</div>
              <div>
                <h2 class="step-title">Paket Internet</h2>
                <p class="step-subtitle-text">
                  Pilihan paket internet yang dipasang
                </p>
              </div>
            </div>

            <!-- Dynamic Selector if no Package ID in URL query -->
            <div
              v-if="!preSelectedPackage"
              class="form-group"
              style="margin-bottom: 20px"
            >
              <label class="form-label"
                >Pilih Paket Internet <span class="required">*</span></label
              >
              <div class="select-wrapper">
                <select
                  v-model="selectedPackageId"
                  class="form-input select-input"
                  required
                >
                  <option value="" disabled selected>- pilih paket -</option>
                  <option v-for="p in packages" :key="p.id" :value="p.id">
                    {{ p.name }} - {{ p.speed ? p.speed + " Mbps" : "" }} ({{
                      formatRp(p.price)
                    }}/Bulan)
                  </option>
                </select>
              </div>
            </div>

            <!-- High-Fidelity Premium Pricing Card matching the reference image exactly -->
            <div v-if="activePackage" class="pricing-card-wrap">
              <div class="pricing-card-inner">
                <div class="pricing-package-title">
                  Paket {{ activePackage.name }}
                </div>

                <div class="pricing-details-row">
                  <!-- Speed Badge -->
                  <div class="pricing-speed-badge">
                    <div class="speed-badge-val">
                      {{ Math.round(activePackage.speed || 0) }}
                    </div>
                    <div class="speed-badge-unit">Mbps</div>
                  </div>
                  <!-- Price value -->
                  <div class="pricing-val-block">
                    <span class="price-curr">Rp</span>
                    <span class="price-number">{{
                      formatPriceCompact(activePackage.price)
                    }}</span>
                    <span class="price-sub">/Bulan</span>
                  </div>
                </div>

                <!-- Features list with circle checkmarks -->
                <ul class="pricing-features">
                  <li>
                    <span class="check-circle">
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2.5"
                        class="check-icon-svg"
                      >
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                    </span>
                    Unlimited Tanpa FUP
                  </li>
                  <li>
                    <span class="check-circle">
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2.5"
                        class="check-icon-svg"
                      >
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                    </span>
                    Gratis Instalasi
                  </li>
                  <li>
                    <span class="check-circle">
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2.5"
                        class="check-icon-svg"
                      >
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                    </span>
                    Support Prioritas 24/7
                  </li>
                </ul>
              </div>
            </div>

            <div v-else-if="!packagesLoading" class="package-card-empty">
              Silakan pilih paket internet terlebih dahulu.
            </div>
          </div>

          <!-- Bottom Action Buttons inside the main form card container -->
          <div class="action-buttons-group">
            <button type="button" @click="resetForm" class="btn-cancel">
              Batal
            </button>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? "Mengirim..." : "Kirim Registrasi" }}
            </button>
          </div>

          <div class="bottom-disclaimer">
            Pastikan data yang Anda input sudah benar sebelum mengirim.
          </div>
        </form>
      </div>
      
      <!-- Confirmation Modal -->
      <div v-if="showConfirmModal" class="confirm-modal-overlay">
        <div class="confirm-modal-box animated zoomIn">
          <div class="confirm-modal-header">
            <h3 class="cmh-title">Konfirmasi Data</h3>
            <button type="button" class="confirm-modal-close" @click="showConfirmModal = false">&times;</button>
          </div>
          
          <div class="confirm-modal-body">
            <!-- Left Side: Checklist SVG Illustration -->
            <div class="cmb-left">
              <svg class="checklist-illustration-svg" viewBox="0 0 200 180" fill="none">
                <!-- Document card -->
                <rect x="50" y="30" width="100" height="120" rx="8" fill="#f8fafc" stroke="#e2e8f0" stroke-width="4" />
                <!-- Lines on paper -->
                <line x1="70" y1="55" x2="130" y2="55" stroke="#cbd5e1" stroke-width="4" stroke-linecap="round" />
                <line x1="70" y1="75" x2="115" y2="75" stroke="#cbd5e1" stroke-width="4" stroke-linecap="round" />
                <line x1="70" y1="95" x2="120" y2="95" stroke="#cbd5e1" stroke-width="4" stroke-linecap="round" />
                <line x1="70" y1="115" x2="100" y2="115" stroke="#cbd5e1" stroke-width="4" stroke-linecap="round" />
                
                <!-- Circular Green Checkmark badge -->
                <circle cx="140" cy="110" r="28" fill="#dcfce7" stroke="#22c55e" stroke-width="4" />
                <path d="M128 108 L137 117 L152 98" stroke="#22c55e" stroke-width="5" stroke-linecap="round" stroke-linejoin="round" />
                
                <!-- Magnifying glass overlay -->
                <circle cx="55" cy="105" r="20" fill="#f1f5f9" stroke="#64748b" stroke-width="4" />
                <line x1="69" y1="119" x2="85" y2="135" stroke="#64748b" stroke-width="5" stroke-linecap="round" />
                <path d="M47 98 A 12 12 0 0 1 65 102" stroke="#94a3b8" stroke-width="3" stroke-linecap="round" />
              </svg>
            </div>
            
            <!-- Right Side: Text description -->
            <div class="cmb-right">
              <h4 class="cmbr-title">Apakah kamu yakin data sudah benar?</h4>
              <p class="cmbr-subtitle">Kamu masih bisa kembali untuk cek lagi sebelum data dikirim.</p>
            </div>
          </div>
          
          <div class="confirm-modal-footer">
            <button type="button" class="btn btn-modal-cancel" @click="showConfirmModal = false">Cek lagi</button>
            <button type="button" class="btn btn-modal-confirm" @click="performSubmit" :disabled="loading">
              {{ loading ? 'Mengirim...' : 'Yakin, kirim' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import {
  getPackagesPublic,
  getPackagePublicDetail,
  submitPublicRegistration,
} from "@/services/api";
import { useToast } from "@/composables/useToast";

const route = useRoute();
const toast = useToast();

const success = ref(false);
const loading = ref(false);
const packagesLoading = ref(false);
const showConfirmModal = ref(false);
const pendingPayload = ref(null);

const packages = ref([]);
const preSelectedPackage = ref(null);
const selectedPackageId = ref("");
const form = ref({
  firstName: "",
  lastName: "",
  email: "",
  jenisIdentitas: "",
  nomorIdentitas: "",
  nomorSelulerUtama: "",
  nomorCadangan: "",
  alamatDetail: "",
  rt: "",
  rw: "",
  kelurahan: "",
  kecamatan: "",
  kota: "Sukabumi",
  provinsi: "Jawa Barat",
  kodePos: "",
  koordinat: "",
  afiliator: "",
});

function onRtInput(e) {
  form.value.rt = e.target.value.replace(/\D/g, "").slice(0, 3);
}

function onRwInput(e) {
  form.value.rw = e.target.value.replace(/\D/g, "").slice(0, 3);
}

function onPhoneInput(e, field) {
  form.value[field] = e.target.value.replace(/\D/g, "").slice(0, 13);
}

function onIdentitasInput(e) {
  const val = e.target.value;
  if (form.value.jenisIdentitas === "KTP") {
    form.value.nomorIdentitas = val.replace(/\D/g, "").slice(0, 16);
  } else {
    form.value.nomorIdentitas = val.slice(0, 20);
  }
}

watch(
  () => form.value.jenisIdentitas,
  (newVal) => {
    // Reset KTP number format
    if (newVal === "KTP") {
      form.value.nomorIdentitas = form.value.nomorIdentitas
        .replace(/\D/g, "")
        .slice(0, 16);
    }
    // Clear upload whenever identity type changes
    removeFile();
  },
);

// ---- File upload state (declared after `form`) ----
const fileInputRef = ref(null);
const uploadedFile = ref(null);
const isDragging = ref(false);

const uploadedFileIsImage = computed(() => {
  if (!uploadedFile.value) return false;
  return uploadedFile.value.type.startsWith("image/");
});

const identityUploadLabel = computed(() => {
  const labels = {
    KTP: "Foto KTP",
    Passport: "Foto Passport",
    Tax_id: "Foto NPWP",
    Kitas: "Foto Kitas",
  };
  return labels[form.value.jenisIdentitas] || "Dokumen Identitas";
});

function triggerFileInput() {
  fileInputRef.value?.click();
}

function onFileChange(e) {
  const file = e.target.files?.[0];
  if (file) handleFile(file);
}

function onFileDrop(e) {
  isDragging.value = false;
  const file = e.dataTransfer?.files?.[0];
  if (file) handleFile(file);
}

function handleFile(file) {
  const allowedTypes = ["image/jpeg", "image/png"];
  if (!allowedTypes.includes(file.type)) {
    toast.error("Format file tidak didukung. Gunakan JPG atau PNG.");
    return;
  }
  if (file.size > 3 * 1024 * 1024) {
    toast.error(
      "Ukuran file melebihi 3MB. Silakan pilih file yang lebih kecil.",
    );
    return;
  }
  uploadedFile.value = file;
}

function removeFile() {
  uploadedFile.value = null;
  if (fileInputRef.value) fileInputRef.value.value = "";
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
  return (bytes / (1024 * 1024)).toFixed(1) + " MB";
}

const activePackage = computed(() => {
  if (preSelectedPackage.value) {
    return preSelectedPackage.value;
  }
  return (
    packages.value.find((p) => p.id === parseInt(selectedPackageId.value)) ||
    null
  );
});

function formatPriceCompact(price) {
  if (!price) return "0";
  if (price >= 1000) {
    return `${Math.round(price / 1000)}rb`;
  }
  return price.toString();
}

function formatRp(value) {
  if (!value) return "Rp 0";
  return "Rp " + Math.round(value).toLocaleString("id-ID");
}

function resetForm() {
  form.value = {
    firstName: "",
    lastName: "",
    email: "",
    jenisIdentitas: "",
    nomorIdentitas: "",
    nomorSelulerUtama: "",
    nomorCadangan: "",
    alamatDetail: "",
    rt: "",
    rw: "",
    kelurahan: "",
    kecamatan: "",
    kota: "Sukabumi",
    provinsi: "Jawa Barat",
    kodePos: "",
    koordinat: "",
    afiliator: "",
  };
  success.value = false;
  if (!preSelectedPackage.value) {
    selectedPackageId.value = "";
  }
}

async function loadInitialData() {
  const packageIdParam = route.query.packageId;

  if (packageIdParam) {
    packagesLoading.value = true;
    try {
      const res = await getPackagePublicDetail(packageIdParam);
      preSelectedPackage.value = res;
    } catch (e) {
      toast.error(
        "Gagal memuat detail paket dari link. Menampilkan daftar paket.",
      );
      preSelectedPackage.value = null;
      await fetchAllPackages();
    } finally {
      packagesLoading.value = false;
    }
  } else {
    await fetchAllPackages();
  }
}

async function fetchAllPackages() {
  packagesLoading.value = true;
  try {
    const res = await getPackagesPublic();
    packages.value = res;
  } catch (e) {
    toast.error("Gagal memuat pilihan paket internet.");
  } finally {
    packagesLoading.value = false;
  }
}

function focusAndScrollTo(id) {
  const el = document.getElementById(id);
  if (el) {
    el.scrollIntoView({ behavior: "smooth", block: "center" });
    el.focus();
  }
}

async function submitForm() {
  // 1. Native HTML5 validation check
  const formEl = document.querySelector("form");
  if (formEl && !formEl.checkValidity()) {
    formEl.reportValidity();
    const firstInvalid = formEl.querySelector(":invalid");
    if (firstInvalid) {
      firstInvalid.scrollIntoView({ behavior: "smooth", block: "center" });
      firstInvalid.focus();
    }
    toast.error("Silakan lengkapi kolom wajib diisi (*)");
    return;
  }

  // 2. KTP Validation: exactly 16 digits
  if (form.value.jenisIdentitas === "KTP") {
    const ktpNum = form.value.nomorIdentitas.replace(/\D/g, "");
    if (ktpNum.length !== 16) {
      toast.error("Nomor Identitas KTP harus tepat 16 digit angka!");
      focusAndScrollTo("nomorIdentitas");
      return;
    }
  }

  // 3. Phone number validation: 10 to 13 digits (excluding +62 prefix)
  const phone1Raw = form.value.nomorSelulerUtama.replace(/\D/g, "");
  if (phone1Raw.length < 10 || phone1Raw.length > 13) {
    toast.error(
      "Nomor Seluler Utama harus terdiri dari 10 sampai 13 digit angka!",
    );
    focusAndScrollTo("nomorSelulerUtama");
    return;
  }

  const phone2Raw = form.value.nomorCadangan.replace(/\D/g, "");
  if (phone2Raw && (phone2Raw.length < 10 || phone2Raw.length > 13)) {
    toast.error("Nomor Cadangan harus terdiri dari 10 sampai 13 digit angka!");
    focusAndScrollTo("nomorCadangan");
    return;
  }

  const targetPkg = activePackage.value;
  if (!targetPkg) {
    toast.error("Silakan pilih paket internet terlebih dahulu!");
    return;
  }

  // Format phone numbers
  let phone1 = form.value.nomorSelulerUtama.trim();
  if (phone1.startsWith("0")) phone1 = phone1.substring(1);
  if (!phone1.startsWith("+62") && !phone1.startsWith("62"))
    phone1 = "+62" + phone1;
  else if (phone1.startsWith("62")) phone1 = "+" + phone1;

  let phone2 = form.value.nomorCadangan.trim();
  if (phone2) {
    if (phone2.startsWith("0")) phone2 = phone2.substring(1);
    if (!phone2.startsWith("+62") && !phone2.startsWith("62"))
      phone2 = "+62" + phone2;
    else if (phone2.startsWith("62")) phone2 = "+" + phone2;
  }

  // 4. Identity document upload check
  if (!uploadedFile.value) {
    toast.error(`Silakan upload ${identityUploadLabel.value} terlebih dahulu!`);
    return;
  }

  const payload = {
    ...form.value,
    nomorSelulerUtama: phone1,
    nomorCadangan: phone2 || null,
    pkg: {
      id: targetPkg.id,
    },
  };

  pendingPayload.value = payload;
  showConfirmModal.value = true;
}

async function performSubmit() {
  if (!pendingPayload.value) return;

  loading.value = true;
  try {
    await submitPublicRegistration(pendingPayload.value, uploadedFile.value || null);
    success.value = true;
    showConfirmModal.value = false;
    toast.success("Pendaftaran berhasil dikirim!");
  } catch (e) {
    toast.error("Gagal mengirim pendaftaran: " + e.message);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadInitialData();
});
</script>

<style scoped>
/* Page Layout Wrapper */
.registration-page-wrapper {
  background-color: #f8fafc;
  min-height: 100vh;
  width: 100%;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  font-family:
    "Outfit",
    "Inter",
    system-ui,
    -apple-system,
    sans-serif;
}

.registration-container {
  width: 100%;
  max-width: 820px;
  display: flex;
  flex-direction: column;
}

/* Outer Header styling */
.reg-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 0 4px;
}

.reg-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.reg-subtitle {
  font-size: 12px;
  color: #64748b;
  margin: 0;
}

.reg-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 99px;
  font-size: 11px;
  font-weight: 600;
  color: #2563eb;
}

.badge-dot {
  width: 6px;
  height: 6px;
  background: #2563eb;
  border-radius: 50%;
}

/* Card Container with top primary color bar */
.main-form-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-top: 4px solid #2563eb;
  border-radius: 12px;
  padding: 32px;
  box-shadow:
    0 10px 25px -5px rgba(0, 0, 0, 0.04),
    0 8px 10px -6px rgba(0, 0, 0, 0.04);
}

/* Steps Block Layout */
.step-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  background: #ffffff;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.01);
}

.step-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 24px;
}

.step-number-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 700;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 2px 0;
}

.step-subtitle-text {
  font-size: 11px;
  color: #64748b;
  margin: 0;
}

/* Inputs & Labels */
.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.required {
  color: #ef4444;
}

.form-input {
  width: 100%;
  height: 42px;
  padding: 0 14px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 13px;
  color: #0f172a;
  transition: all 0.2s ease-in-out;
}

.form-input::placeholder {
  color: #94a3b8;
}

.form-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.select-wrapper {
  position: relative;
  width: 100%;
}

.select-input {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%23475569' stroke-width='2.5'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M19.5 8.25l-7.5 7.5-7.5-7.5'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  background-size: 12px;
  padding-right: 36px;
}

.textarea-input {
  height: auto;
  padding: 12px 14px;
  resize: vertical;
}

/* ---- Upload Section ---- */
.upload-section {
  margin-top: 16px;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 11px;
  font-weight: 400;
  color: #94a3b8;
  margin-left: 6px;
}

.upload-dropzone {
  position: relative;
  border: 2px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
  padding: 28px 20px;
  text-align: center;
  cursor: pointer;
  transition:
    border-color 0.2s,
    background 0.2s,
    box-shadow 0.2s;
}
.upload-dropzone:hover {
  border-color: #2563eb;
  background: #eff6ff;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.07);
}
.upload-dropzone--dragging {
  border-color: #2563eb;
  background: #dbeafe;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
}
.upload-dropzone--has-file {
  border-style: solid;
  border-color: #22c55e;
  background: #f0fdf4;
  cursor: default;
}
.upload-dropzone--has-file:hover {
  border-color: #16a34a;
  background: #dcfce7;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.07);
}

.upload-file-input {
  display: none;
}

/* Empty state */
.upload-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.upload-icon-wrap {
  width: 48px;
  height: 48px;
  background: #e0e7ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
  transition: transform 0.2s;
}
.upload-dropzone:hover .upload-icon-wrap {
  transform: translateY(-2px);
}

.upload-icon {
  width: 24px;
  height: 24px;
  color: #2563eb;
}

.upload-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0;
}

.upload-sub {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.upload-browse-link {
  color: #2563eb;
  font-weight: 600;
  text-decoration: underline;
}

.upload-types {
  font-size: 11px;
  color: #94a3b8;
  margin: 0;
}

/* Preview state */
.upload-preview-state {
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
}

.upload-preview-icon-wrap {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  background: #dcfce7;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-prev-icon {
  width: 22px;
  height: 22px;
}
.upload-prev-icon.uploaded {
  color: #16a34a;
}

.upload-preview-info {
  flex: 1;
  min-width: 0;
}

.upload-filename {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 2px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.upload-filesize {
  font-size: 11px;
  color: #64748b;
  margin: 0;
}

.upload-remove-btn {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  border: none;
  background: #fee2e2;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    background 0.15s,
    transform 0.15s;
}
.upload-remove-btn:hover {
  background: #fca5a5;
  transform: scale(1.08);
}
.upload-remove-btn svg {
  width: 14px;
  height: 14px;
  stroke: #dc2626;
}

/* Transition animation */
.upload-slide-enter-active,
.upload-slide-leave-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease,
    max-height 0.3s ease;
  overflow: hidden;
  max-height: 300px;
}
.upload-slide-enter-from,
.upload-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  max-height: 0;
}

/* Mandantory Notice */
.mandatory-notice {
  font-size: 11px;
  font-weight: 700;
  color: #ef4444;
  margin: -6px 0 10px 0;
}

/* Phone Number Groups */
.phone-input-group {
  display: flex;
  align-items: center;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  height: 42px;
  transition: all 0.2s ease;
}

.phone-input-group:focus-within {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.phone-prefix {
  height: 100%;
  padding: 0 14px;
  background: #f8fafc;
  border-right: 1px solid #cbd5e1;
  font-size: 12px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
}

.phone-field {
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  flex: 1;
  height: 100%;
}

/* Section 3: High-Fidelity Pricing Card */
.pricing-card-wrap {
  border: 1.5px solid #bfdbfe;
  background: #f0f7ff;
  border-radius: 16px;
  padding: 32px;
  max-width: 520px;
  margin: 16px auto;
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.05);
}

.pricing-package-title {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 16px;
}

.pricing-details-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.pricing-speed-badge {
  background: #2563eb;
  border-radius: 12px;
  width: 90px;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.25);
}

.speed-badge-val {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.speed-badge-unit {
  font-size: 11px;
  font-weight: 600;
  opacity: 0.95;
  margin-top: 2px;
}

.pricing-val-block {
  display: flex;
  align-items: baseline;
  color: #2563eb;
}

.price-curr {
  font-size: 14px;
  font-weight: 700;
  margin-right: 2px;
}

.price-number {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -1px;
  line-height: 1;
}

.price-sub {
  font-size: 14px;
  font-weight: 700;
  color: #2563eb;
  margin-left: 2px;
}

.pricing-features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.pricing-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  font-weight: 700;
  color: #1e293b;
}

.check-circle {
  width: 18px;
  height: 18px;
  background: #dbeafe;
  color: #2563eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.check-icon-svg {
  width: 10px;
  height: 10px;
}

.package-card-empty {
  text-align: center;
  padding: 24px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  font-size: 12px;
  color: #64748b;
}

/* Action buttons at the bottom */
.action-buttons-group {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  padding: 0 4px;
}

.btn-cancel {
  flex: 1;
  height: 46px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}

.btn-submit {
  flex: 1;
  height: 46px;
  background: #2563eb;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
}

.btn-submit:hover {
  background: #1d4ed8;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.2);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.bottom-disclaimer {
  text-align: center;
  font-size: 11px;
  color: #94a3b8;
  margin-top: 14px;
}

/* Success State styling - New Premium Layout (Screenshot 3) */
.main-form-card--success {
  border-top: none !important;
  background: transparent !important;
  box-shadow: none !important;
  padding: 0 !important;
}

.new-success-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.new-success-header {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.new-success-logo-wrap {
  flex-shrink: 0;
  border-right: 1px solid #e2e8f0;
  padding-right: 20px;
  height: 60px;
  display: flex;
  align-items: center;
}

.new-success-logo-svg {
  width: 140px;
  height: auto;
}

.new-success-header-text {
  flex: 1;
}

.nsh-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 2px 0;
}

.nsh-subtitle {
  font-size: 12px;
  color: #475569;
  margin: 0;
}

.new-success-card {
  background: #f0f7ff;
  border: 1px solid #bfdbfe;
  border-radius: 16px;
  padding: 35px 40px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.nsc-status-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.nsc-check-badge {
  background: #dcfce7;
  border: 1.5px solid #86efac;
  color: #16a34a;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nsc-check-icon {
  width: 22px;
  height: 22px;
}

.nsct-title {
  font-size: 18px;
  font-weight: 800;
  color: #1e3a8a;
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.nsct-subtitle {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  margin: 0;
}

.nsc-status-panel {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 16px 20px;
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nscp-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #64748b;
}

.nscp-value {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.nsc-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.btn-back-home {
  background: #2563eb !important;
  color: white !important;
  font-size: 13px !important;
  font-weight: 700 !important;
  padding: 12px 28px !important;
  border-radius: 8px !important;
  border: none !important;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
  transition: all 0.2s ease;
}

.btn-back-home:hover {
  background: #1d4ed8 !important;
  transform: translateY(-1px);
}

.nsc-footer-note {
  text-align: center;
  font-size: 11px;
  color: #64748b;
  margin-top: 20px;
  line-height: 1.5;
}

/* Confirmation Modal styling (Screenshot 2) */
.confirm-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.confirm-modal-box {
  background: white;
  width: 100%;
  max-width: 600px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.15);
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.confirm-modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
}

.cmh-title {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.confirm-modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #94a3b8;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.confirm-modal-close:hover {
  color: #64748b;
}

.confirm-modal-body {
  padding: 24px;
  display: flex;
  gap: 24px;
  align-items: center;
}

.cmb-left {
  flex-shrink: 0;
  width: 160px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checklist-illustration-svg {
  width: 100%;
  height: 100%;
}

.cmb-right {
  flex: 1;
}

.cmbr-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.cmbr-subtitle {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  margin: 0;
}

.confirm-modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #f8fafc;
}

.btn-modal-cancel {
  background: white !important;
  color: #475569 !important;
  border: 1px solid #cbd5e1 !important;
  font-size: 13px !important;
  font-weight: 700 !important;
  padding: 10px 20px !important;
  border-radius: 8px !important;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-modal-cancel:hover {
  background: #f8fafc !important;
  color: #0f172a !important;
}

.btn-modal-confirm {
  background: #2563eb !important;
  color: white !important;
  border: none !important;
  font-size: 13px !important;
  font-weight: 700 !important;
  padding: 10px 24px !important;
  border-radius: 8px !important;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.15);
  transition: all 0.15s ease;
}

.btn-modal-confirm:hover {
  background: #1d4ed8 !important;
}

.btn-modal-confirm:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .form-grid-2 {
    grid-template-columns: 1fr;
    gap: 0;
  }
  .main-form-card {
    padding: 20px;
  }
  .new-success-header {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  .new-success-logo-wrap {
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
    padding-right: 0;
    padding-bottom: 12px;
    width: 100%;
    justify-content: center;
  }
  .new-success-card {
    padding: 20px;
  }
  .nsc-status-row {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  .confirm-modal-body {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  .confirm-modal-footer {
    justify-content: stretch;
  }
  .confirm-modal-footer .btn {
    flex: 1;
  }
}
</style>

