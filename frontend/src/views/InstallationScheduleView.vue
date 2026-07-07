<template>
  <div class="schedule-dashboard">
    <!-- View 1: Schedule List -->
    <div v-if="!selectedRegForBa">
      <!-- Header -->
      <div class="page-header">
        <div class="page-header-left">
          <h1 class="page-title">Jadwal Pemasangan</h1>
          <div class="page-meta">
            Status: <strong>{{ currentTabLabel }}</strong>
          </div>
        </div>
      </div>

      <!-- Toolbar / Filters -->
      <div class="card toolbar-card">
        <div class="toolbar-left">
          <div class="status-tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              @click="activeTab = tab.value"
              class="tab-btn"
              :class="[
                `tab-btn--${tab.colorClass}`,
                { 'tab-btn--active': activeTab === tab.value },
              ]"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>

        <!-- Search & Refresh -->
        <div class="toolbar-right">
          <div class="search-box">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Cari nama customer"
              class="form-input search-input"
              @keyup.enter="fetchList"
            />
            <button @click="fetchList" class="btn btn--primary btn-cari">
              Cari
            </button>
          </div>
          <button
            @click="fetchList"
            class="btn btn--secondary btn-refresh"
            :disabled="loading"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              class="refresh-icon"
              :class="{ spin: loading }"
            >
              <polyline points="23 4 23 10 17 10"></polyline>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
            </svg>
            refresh
          </button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="card table-card">
        <div class="table-header-info">
          <span
            >Total: <strong>{{ filteredRegistrations.length }}</strong></span
          >
        </div>

        <div v-if="loading && registrations.length === 0" class="loading-state">
          <div class="spinner"></div>
          <p>Memuat data jadwal pemasangan...</p>
        </div>

        <div
          v-else-if="paginatedRegistrations.length === 0"
          class="empty-state"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            class="empty-icon"
          >
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <p>Tidak ada jadwal pemasangan dalam status ini.</p>
        </div>

        <div v-else class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 60px">No</th>
                <th>Customer ID</th>
                <th>Nama</th>
                <th>Jadwal</th>
                <th style="width: 100px; text-align: center">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(reg, index) in paginatedRegistrations" :key="reg.id">
                <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                <td class="text-secondary font-mono">
                  {{ reg.custId || "-" }}
                </td>
                <td>
                  <div class="customer-info-cell">
                    <span class="customer-name"
                      >{{ reg.firstName }} {{ reg.lastName }}</span
                    >
                    <span class="customer-subtext">{{ reg.alamatDetail }}</span>
                  </div>
                </td>
                <td class="font-mono text-secondary">
                  {{ formatDateTime(reg.tanggalJadwal) }}
                </td>
                <td style="text-align: center">
                  <button
                    @click="openBaForm(reg)"
                    class="btn btn--outline-primary btn-sm btn-detail"
                  >
                    Detail
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Footer -->
        <div class="table-footer">
          <div class="pagination-info">
            Page {{ currentPage }}/{{ totalPages }} · Menampilkan
            {{ startIndex + 1 }}-{{ endIndex }} dari
            {{ filteredRegistrations.length }}
          </div>
          <div class="pagination-buttons">
            <button
              @click="prevPage"
              class="btn btn-pag"
              :disabled="currentPage === 1"
            >
              &lt; Prev
            </button>
            <button
              @click="nextPage"
              class="btn btn-pag"
              :disabled="currentPage === totalPages"
            >
              Next &gt;
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- View 3: Official BA View (Technician side) -->
    <template v-else-if="currentSubView === 'OFFICIAL_BA'">
      <div class="official-ba-container">
        <!-- Action bar (hidden when printing) -->
        <div class="official-ba-actions no-print">
          <button class="btn btn-back" @click="currentSubView = 'FORM'">
            &larr; Kembali
          </button>
          <button
            v-if="
              baForm &&
              (!baForm.tandaTanganCustomer ||
                !baForm.tandaTanganTeknisi ||
                customerSigEditing ||
                technicianSigEditing)
            "
            class="btn btn-print btn-save-signatures"
            @click="saveOfficialSignatures"
            :disabled="savingSigs"
          >
            {{ savingSigs ? "Menyimpan..." : "Simpan Tanda Tangan" }}
          </button>
          <button class="btn btn-print" @click="window.print()">Print</button>
        </div>

        <!-- Printable document -->
        <div v-if="selectedRegForBa" class="official-ba-document">
          <div class="obad-header">
            <div class="obad-logo-section">
              <!-- SVG Gelo logo matching reference -->
              <svg class="gelo-logo-svg" viewBox="0 0 240 80" fill="none">
                <path
                  d="M40 50 C20 50, 10 35, 20 20 C30 5, 50 10, 50 25 C50 35, 45 42, 38 45"
                  stroke="#00e5ff"
                  stroke-width="8"
                  stroke-linecap="round"
                />
                <path
                  d="M42 22 L26 22 C20 22, 20 30, 26 30 L42 30"
                  stroke="#00b0ff"
                  stroke-width="6"
                  stroke-linecap="round"
                />
                <text
                  x="75"
                  y="45"
                  fill="#00e5ff"
                  font-family="'Outfit', sans-serif"
                  font-size="34"
                  font-weight="900"
                >
                  GELO
                </text>
                <text
                  x="75"
                  y="62"
                  fill="#ffffff"
                  font-family="'Outfit', sans-serif"
                  font-size="13"
                  font-weight="600"
                  letter-spacing="1"
                >
                  By VNet
                </text>
              </svg>
            </div>
            <div class="obad-title-section">
              <h1 class="obad-title">GELO</h1>
              <h2 class="obad-subtitle">BA AKTIVASI</h2>
            </div>
          </div>

          <div class="obad-divider"></div>

          <div class="obad-date-row">
            Tanggal:
            <strong>{{
              formatDateTime(
                selectedRegForBa.tanggalJadwal || selectedRegForBa.waktu,
              ).substring(0, 10)
            }}</strong>
          </div>

          <!-- Customer metadata table -->
          <table class="obad-meta-table">
            <tbody>
              <tr>
                <td class="obadm-label">Nama</td>
                <td class="obadm-value">
                  <strong
                    >{{ selectedRegForBa.firstName }}
                    {{ selectedRegForBa.lastName }}</strong
                  >
                </td>
              </tr>
              <tr>
                <td class="obadm-label">Email</td>
                <td class="obadm-value">{{ selectedRegForBa.email }}</td>
              </tr>
              <tr>
                <td class="obadm-label">Nomor HP</td>
                <td class="obadm-value">
                  {{ selectedRegForBa.nomorSelulerUtama }}
                </td>
              </tr>
              <tr>
                <td class="obadm-label">Nomor Cadangan</td>
                <td class="obadm-value">
                  {{ selectedRegForBa.nomorCadangan || "-" }}
                </td>
              </tr>
              <tr>
                <td class="obadm-label">Paket Internet</td>
                <td class="obadm-value">
                  {{ selectedRegForBa.pkg ? selectedRegForBa.pkg.name : "-" }}
                </td>
              </tr>
              <tr>
                <td class="obadm-label">Harga</td>
                <td class="obadm-value">
                  {{
                    formatRp(
                      selectedRegForBa.harga ||
                        (selectedRegForBa.pkg ? selectedRegForBa.pkg.price : 0),
                    )
                  }}
                </td>
              </tr>
              <tr>
                <td class="obadm-label">Alamat</td>
                <td class="obadm-value">
                  {{ selectedRegForBa.alamatDetail }} <br />
                  RT {{ selectedRegForBa.rt || "-" }} / RW
                  {{ selectedRegForBa.rw || "-" }} &bull;
                  {{ selectedRegForBa.kelurahan }},
                  {{ selectedRegForBa.kecamatan }}, {{ selectedRegForBa.kota }}
                </td>
              </tr>
              <tr>
                <td class="obadm-label">CID</td>
                <td class="obadm-value">
                  <strong>{{ selectedRegForBa.custId }}</strong>
                </td>
              </tr>
            </tbody>
          </table>

          <div class="obad-notes-block" style="margin-top: 20px">
            <div class="notes-block-title">Catatan Tambahan</div>
            <div class="notes-block-content">
              {{ selectedRegForBa.noteRequest || "-" }}
            </div>
          </div>

          <!-- Perangkat Terpasang -->
          <div
            class="obad-section-title"
            style="
              margin-top: 30px;
              font-weight: 700;
              font-size: 14px;
              text-transform: uppercase;
              color: #334155;
            "
          >
            Perangkat Terpasang
          </div>
          <div class="obad-devices-table-wrapper" style="margin-top: 10px">
            <table class="obad-devices-table">
              <thead>
                <tr>
                  <th style="width: 50px">No</th>
                  <th>Equipment</th>
                  <th style="width: 80px; text-align: center">Qty</th>
                  <th>Serial Number</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="baForm.qtyOnt > 0">
                  <td>1</td>
                  <td>Modem / ONT / ONU</td>
                  <td style="text-align: center">{{ baForm.qtyOnt }}</td>
                  <td>{{ baForm.snOnt || "—" }}</td>
                </tr>
                <tr v-if="baForm.qtyRouter > 0">
                  <td>2</td>
                  <td>Wi-Fi Router</td>
                  <td style="text-align: center">{{ baForm.qtyRouter }}</td>
                  <td>{{ baForm.snRouter || "—" }}</td>
                </tr>
                <tr v-if="baForm.qtyKabel > 0">
                  <td>3</td>
                  <td>Kabel Fiber Optic Drop Core (Meter)</td>
                  <td style="text-align: center">{{ baForm.qtyKabel }}</td>
                  <td>{{ baForm.snKabel || "—" }}</td>
                </tr>
                <tr v-if="baForm.qtyRoset > 0">
                  <td>4</td>
                  <td>Roset Optik</td>
                  <td style="text-align: center">{{ baForm.qtyRoset }}</td>
                  <td>{{ baForm.snRoset || "—" }}</td>
                </tr>
                <tr v-if="baForm.qtyAksesoris > 0">
                  <td>5</td>
                  <td>Fast Connector / Aksesoris</td>
                  <td style="text-align: center">{{ baForm.qtyAksesoris }}</td>
                  <td>{{ baForm.snAksesoris || "—" }}</td>
                </tr>
                <tr
                  v-if="
                    !(
                      baForm.qtyOnt > 0 ||
                      baForm.qtyRouter > 0 ||
                      baForm.qtyKabel > 0 ||
                      baForm.qtyRoset > 0 ||
                      baForm.qtyAksesoris > 0
                    )
                  "
                >
                  <td colspan="4" style="text-align: center; color: #94a3b8">
                    -
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Tanda Tangan Digital -->
          <div class="obad-signatures-row" style="margin-top: 30px">
            <!-- Customer signature -->
            <div class="obad-signature-box">
              <h4 class="sig-title">Customer</h4>
              <div class="sig-name">
                Nama: {{ selectedRegForBa.firstName }}
                {{ selectedRegForBa.lastName }}
              </div>

              <div class="sig-canvas-wrapper">
                <div
                  v-if="baForm.tandaTanganCustomer && !customerSigEditing"
                  class="sig-image-display"
                >
                  <img
                    :src="baForm.tandaTanganCustomer"
                    class="signature-img"
                    alt="Customer Signature"
                  />
                  <button
                    type="button"
                    class="btn-sig-clear no-print"
                    @click="editCustomerSig"
                  >
                    Ulangi
                  </button>
                </div>
                <div v-else class="sig-canvas-draw">
                  <canvas
                    ref="customerSigCanvas"
                    class="sig-canvas-element"
                    width="350"
                    height="150"
                    @mousedown="startSigDraw($event, 'customer')"
                    @mousemove="drawSig($event, 'customer')"
                    @mouseup="stopSigDraw('customer')"
                    @mouseleave="stopSigDraw('customer')"
                    @touchstart="startSigDrawTouch($event, 'customer')"
                    @touchmove="drawSigTouch($event, 'customer')"
                    @touchend="stopSigDraw('customer')"
                  ></canvas>
                  <div class="sig-canvas-actions no-print">
                    <button
                      type="button"
                      class="btn-canvas-clear"
                      @click="clearSigCanvas('customer')"
                    >
                      Bersihkan
                    </button>
                    <button
                      v-if="baForm.tandaTanganCustomer"
                      type="button"
                      class="btn-canvas-cancel"
                      @click="cancelCustomerSigEdit"
                    >
                      Batal
                    </button>
                  </div>
                </div>
              </div>

              <div class="sig-timestamp">
                {{
                  baForm.tandaTanganCustomer
                    ? "Ditandatangani: " +
                      formatDateTime(
                        selectedRegForBa.tanggalJadwal ||
                          selectedRegForBa.waktu,
                      )
                    : "Belum ditandatangani"
                }}
              </div>
            </div>

            <!-- Technician signature -->
            <div class="obad-signature-box">
              <h4 class="sig-title">Teknisi</h4>
              <div class="sig-name">
                Nama: {{ baForm.namaTeknisi || "Teknisi" }}
              </div>

              <div class="sig-canvas-wrapper">
                <div
                  v-if="baForm.tandaTanganTeknisi && !technicianSigEditing"
                  class="sig-image-display"
                >
                  <img
                    :src="baForm.tandaTanganTeknisi"
                    class="signature-img"
                    alt="Technician Signature"
                  />
                  <button
                    type="button"
                    class="btn-sig-clear no-print"
                    @click="editTechnicianSig"
                  >
                    Ulangi
                  </button>
                </div>
                <div v-else class="sig-canvas-draw">
                  <canvas
                    ref="technicianSigCanvas"
                    class="sig-canvas-element"
                    width="350"
                    height="150"
                    @mousedown="startSigDraw($event, 'technician')"
                    @mousemove="drawSig($event, 'technician')"
                    @mouseup="stopSigDraw('technician')"
                    @mouseleave="stopSigDraw('technician')"
                    @touchstart="startSigDrawTouch($event, 'technician')"
                    @touchmove="drawSigTouch($event, 'technician')"
                    @touchend="stopSigDraw('technician')"
                  ></canvas>
                  <div class="sig-canvas-actions no-print">
                    <button
                      type="button"
                      class="btn-canvas-clear"
                      @click="clearSigCanvas('technician')"
                    >
                      Bersihkan
                    </button>
                    <button
                      v-if="baForm.tandaTanganTeknisi"
                      type="button"
                      class="btn-canvas-cancel"
                      @click="cancelTechnicianSigEdit"
                    >
                      Batal
                    </button>
                  </div>
                </div>
              </div>

              <div class="sig-timestamp">
                {{
                  baForm.tandaTanganTeknisi
                    ? "Ditandatangani: " +
                      formatDateTime(
                        selectedRegForBa.tanggalJadwal ||
                          selectedRegForBa.waktu,
                      )
                    : "Belum ditandatangani"
                }}
              </div>
            </div>
          </div>

          <!-- Parameter IKR -->
          <div
            class="obad-section-title"
            style="
              margin-top: 30px;
              font-weight: 700;
              font-size: 14px;
              text-transform: uppercase;
              color: #334155;
            "
          >
            Parameter IKR
          </div>
          <table
            class="obad-meta-table"
            style="margin-top: 10px; width: 100%; border-collapse: collapse"
          >
            <tbody>
              <tr>
                <td
                  class="obadm-label"
                  style="
                    width: 150px;
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Bandwidth
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.bandwidth || "-" }}
                </td>
                <td
                  class="obadm-label"
                  style="
                    width: 150px;
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Kode ODP
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.kodeOdp || "-" }}
                </td>
              </tr>
              <tr>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Panjang Kabel
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.panjangKabel || "0" }} Meter
                </td>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Port ODP / Terpakai
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.portOdp || "-" }} /
                  {{ baForm.portOdpTerpakai || "-" }}
                </td>
              </tr>
              <tr>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Koordinat Rumah
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.koordinatRumah || "-" }}
                </td>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  POP / OLT
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.pop || "-" }} / {{ baForm.olt || "-" }}
                </td>
              </tr>
              <tr>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Rosset
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.rosset || "-" }}
                </td>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Pigtail / Patchcore
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.pigtail || "0" }} / {{ baForm.patchcore || "0" }}
                </td>
              </tr>
              <tr>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Splicing
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.splicing || "0" }}
                </td>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Redaman Output
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.redamanOutputKabel || "-" }}
                </td>
              </tr>
              <tr>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Bracket / S-Clamp
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.jumlahBracket || "0" }} /
                  {{ baForm.jumlahSClamp || "0" }}
                </td>
                <td
                  class="obadm-label"
                  style="
                    font-weight: 600;
                    padding: 6px 8px;
                    border: 1px solid #cbd5e1;
                    background: #f8fafc;
                  "
                >
                  Status Aktivasi
                </td>
                <td
                  class="obadm-value"
                  style="padding: 6px 8px; border: 1px solid #cbd5e1"
                >
                  {{ baForm.statusAktivasi || "-" }}
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Dokumentasi Foto -->
          <div
            class="obad-section-title"
            style="
              margin-top: 30px;
              font-weight: 700;
              font-size: 14px;
              text-transform: uppercase;
              color: #334155;
            "
          >
            Dokumentasi Foto
          </div>
          <div
            class="photo-display-grid"
            style="
              display: grid;
              grid-template-columns: repeat(4, 1fr);
              gap: 10px;
              margin-top: 10px;
              margin-bottom: 20px;
            "
          >
            <div
              v-for="(photo, pIdx) in docPhotos"
              :key="pIdx"
              class="photo-display-card"
              style="
                border: 1px solid #cbd5e1;
                border-radius: 4px;
                padding: 6px;
                text-align: center;
                background: white;
              "
            >
              <div
                style="
                  font-size: 11px;
                  font-weight: 600;
                  color: #475569;
                  margin-bottom: 5px;
                  text-overflow: ellipsis;
                  overflow: hidden;
                  white-space: nowrap;
                "
              >
                {{ photo.label }}
              </div>
              <div
                style="
                  width: 100%;
                  height: 100px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  background: #f1f5f9;
                  overflow: hidden;
                  border-radius: 2px;
                "
              >
                <img
                  v-if="baForm[photo.key]"
                  :src="baForm[photo.key]"
                  style="width: 100%; height: 100%; object-fit: cover"
                  alt="Foto"
                />
                <span v-else style="font-size: 11px; color: #94a3b8"
                  >Tidak ada foto</span
                >
              </div>
            </div>
          </div>

          <!-- Footer Company details -->
          <div class="obad-footer-info" style="margin-top: 40px">
            <div class="obadf-company">PT. Victory Network Indonesia</div>
            <div class="obadf-address">
              Graha Matapel, Jl. Arjuna Utara No. 46 - Jakarta Barat 11510
            </div>
            <div class="obadf-links">
              Customer Care: http://client.gelo.co.id &nbsp;·&nbsp;
              https://gelo.co.id
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- View 2: BA Aktivasi (Teknisi) Form View -->
    <div v-else class="ba-container animate-fade-in">
      <!-- Title & Details Header -->
      <div class="ba-header-row">
        <div>
          <h1 class="ba-title">BA Aktivasi (Teknisi)</h1>
          <div class="ba-meta">
            Registrasi #{{ selectedRegForBa.id }} &bull; Status:
            <span class="status-indicator">{{
              getStatusLabel(selectedRegForBa.status)
            }}</span>
          </div>
          <div class="ba-cust-info">
            Customer ID: <strong>{{ selectedRegForBa.custId }}</strong> &bull;
            Jadwal:
            <strong>{{
              formatDateTime(selectedRegForBa.tanggalJadwal)
            }}</strong>
          </div>
        </div>
      </div>

      <!-- Action: Kembali -->
      <button @click="closeBaForm" class="btn btn-back">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          class="back-arrow"
        >
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        Kembali
      </button>

      <!-- Customer Data (Dropdown) -->
      <div class="card customer-dropdown-card">
        <div
          class="dropdown-header"
          @click="showCustomerData = !showCustomerData"
        >
          <span class="dropdown-title">Data Customer</span>
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
            class="chevron-icon"
            :class="{ open: showCustomerData }"
          >
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </div>

        <Transition name="expand">
          <div v-if="showCustomerData" class="dropdown-content">
            <div class="dropdown-grid">
              <div class="info-group">
                <div class="info-label">Nama Lengkap</div>
                <div class="info-value">
                  {{ selectedRegForBa.firstName }}
                  {{ selectedRegForBa.lastName }}
                </div>
              </div>
              <div class="info-group">
                <div class="info-label">Kontak</div>
                <div class="info-value">
                  {{ selectedRegForBa.nomorSelulerUtama }}
                  {{
                    selectedRegForBa.nomorCadangan
                      ? "/ " + selectedRegForBa.nomorCadangan
                      : ""
                  }}
                  ({{ selectedRegForBa.email }})
                </div>
              </div>
              <div class="info-group">
                <div class="info-label">Alamat Lengkap</div>
                <div class="info-value">
                  {{ selectedRegForBa.alamatDetail }} <br />
                  RT {{ selectedRegForBa.rt || "-" }} / RW
                  {{ selectedRegForBa.rw || "-" }} &bull;
                  {{ selectedRegForBa.kelurahan }},
                  {{ selectedRegForBa.kecamatan }}, {{ selectedRegForBa.kota }}
                </div>
              </div>
              <div class="info-group" v-if="selectedRegForBa.pkg">
                <div class="info-label">Paket Internet Pilihan</div>
                <div class="info-value">
                  <strong>{{ selectedRegForBa.pkg.name }}</strong> ({{
                    selectedRegForBa.pkg.speed
                  }}
                  Mbps) - {{ formatRp(selectedRegForBa.pkg.price) }} / Bulan
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <!-- Equipment Activation Card -->
      <div class="card form-card">
        <div class="form-card-title">Isi Perangkat</div>

        <!-- Notes for Technician -->
        <div class="notes-block">
          <label class="notes-label">Catatan untuk Teknisi</label>
          <textarea
            v-model="baForm.catatanTeknisi"
            class="form-input notes-textarea"
            placeholder="-"
            :disabled="!isEditing"
          ></textarea>
        </div>

        <!-- Predefined 5 rows table -->
        <div class="table-responsive">
          <table class="equipment-table">
            <thead>
              <tr>
                <th style="width: 60px">No</th>
                <th>Equipment</th>
                <th style="width: 100px">Qty</th>
                <th style="width: 250px">Serial Number</th>
                <th style="width: 200px; text-align: center">
                  Foto Pemasangan
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, idx) in baItems" :key="idx">
                <td>{{ idx + 1 }}</td>
                <td class="eq-name">{{ item.name }}</td>
                <td>
                  <input
                    v-model.number="baForm[item.qtyKey]"
                    type="number"
                    class="form-input qty-input"
                    min="0"
                    :disabled="!isEditing"
                  />
                </td>
                <td>
                  <input
                    v-model="baForm[item.snKey]"
                    type="text"
                    class="form-input sn-input"
                    placeholder="SN123..."
                    :disabled="!isEditing || item.noSn"
                  />
                </td>
                <td style="text-align: center">
                  <!-- Upload trigger -->
                  <div class="upload-cell">
                    <input
                      :id="'file-' + idx"
                      type="file"
                      accept="image/*"
                      class="hidden-file-input"
                      @change="handleFileUpload($event, item.fotoKey)"
                      :disabled="!isEditing"
                    />

                    <!-- Previews / Upload state -->
                    <div
                      v-if="baForm[item.fotoKey]"
                      class="photo-preview-wrapper"
                    >
                      <img
                        :src="baForm[item.fotoKey]"
                        class="photo-preview-img"
                        alt="Foto Perangkat"
                      />
                      <button
                        v-if="isEditing"
                        @click="clearPhoto(item.fotoKey)"
                        type="button"
                        class="btn-clear-photo"
                        title="Hapus Foto"
                      >
                        &times;
                      </button>
                    </div>

                    <button
                      v-else
                      type="button"
                      class="btn-upload-trigger"
                      @click="triggerUpload(idx)"
                      :disabled="!isEditing"
                    >
                      + Tambah gambar
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Parameter IKR Section -->
        <div
          class="form-card-title"
          style="
            margin-top: 30px;
            border-top: 1px solid #e2e8f0;
            padding-top: 25px;
          "
        >
          Parameter IKR
        </div>
        <div
          class="ikr-params-grid"
          style="
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
          "
        >
          <div class="form-group">
            <label class="form-label">Bandwidth</label>
            <input
              v-model="baForm.bandwidth"
              type="text"
              class="form-input"
              placeholder="Contoh: Gelo 200 Mbps"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Panjang Kabel (Meter)</label>
            <input
              v-model.number="baForm.panjangKabel"
              type="number"
              class="form-input"
              placeholder="150"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Koordinat Rumah</label>
            <input
              v-model="baForm.koordinatRumah"
              type="text"
              class="form-input"
              placeholder="Contoh: -6.914, 106.926"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Kode ODP</label>
            <input
              v-model="baForm.kodeOdp"
              type="text"
              class="form-input"
              placeholder="Contoh: GRV-A261"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Port ODP</label>
            <input
              v-model.number="baForm.portOdp"
              type="number"
              class="form-input"
              placeholder="16"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Port ODP Terpakai</label>
            <input
              v-model="baForm.portOdpTerpakai"
              type="text"
              class="form-input"
              placeholder="Contoh: 10/16"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">POP</label>
            <input
              v-model="baForm.pop"
              type="text"
              class="form-input"
              placeholder="Contoh: GRV"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">OLT</label>
            <input
              v-model="baForm.olt"
              type="text"
              class="form-input"
              placeholder="Contoh: HW / ZTE"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Rosset</label>
            <select
              v-model="baForm.rosset"
              class="form-input"
              :disabled="!isEditing"
            >
              <option value="Ya">Ya</option>
              <option value="Tidak">Tidak</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Pigtail</label>
            <input
              v-model.number="baForm.pigtail"
              type="number"
              class="form-input"
              placeholder="2"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Patchcore</label>
            <input
              v-model.number="baForm.patchcore"
              type="number"
              class="form-input"
              placeholder="0"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Splicing</label>
            <input
              v-model.number="baForm.splicing"
              type="number"
              class="form-input"
              placeholder="2"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Redaman Output Kabel</label>
            <input
              v-model="baForm.redamanOutputKabel"
              type="text"
              class="form-input"
              placeholder="Contoh: 18.08"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Jumlah Bracket/Spiral</label>
            <input
              v-model.number="baForm.jumlahBracket"
              type="number"
              class="form-input"
              placeholder="0"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Jumlah S-Clamp</label>
            <input
              v-model.number="baForm.jumlahSClamp"
              type="number"
              class="form-input"
              placeholder="0"
              :disabled="!isEditing"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Status Aktivasi</label>
            <select
              v-model="baForm.statusAktivasi"
              class="form-input"
              :disabled="!isEditing"
            >
              <option value="Done">Done</option>
              <option value="Active">Active</option>
              <option value="Pending">Pending</option>
            </select>
          </div>
        </div>

        <!-- Dokumentasi Foto Section -->
        <div
          class="form-card-title"
          style="
            margin-top: 30px;
            border-top: 1px solid #e2e8f0;
            padding-top: 25px;
          "
        >
          Dokumentasi Foto
        </div>
        <div
          class="photo-upload-grid"
          style="
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
          "
        >
          <div
            v-for="(photo, pIdx) in docPhotos"
            :key="pIdx"
            class="photo-upload-card"
            style="
              background: #f8fafc;
              border: 1px solid #e2e8f0;
              padding: 15px;
              border-radius: 8px;
              text-align: center;
            "
          >
            <label
              class="form-label"
              style="font-weight: 600; margin-bottom: 10px; display: block"
              >{{ photo.label }}</label
            >
            <div
              class="photo-upload-box"
              style="
                position: relative;
                width: 100%;
                height: 120px;
                border: 2px dashed #cbd5e1;
                border-radius: 6px;
                display: flex;
                align-items: center;
                justify-content: center;
                background: white;
                overflow: hidden;
              "
            >
              <input
                :id="'photo-file-' + photo.key"
                type="file"
                accept="image/*"
                class="hidden-file-input"
                style="display: none"
                @change="handleFileUpload($event, photo.key)"
                :disabled="!isEditing"
              />

              <div
                v-if="baForm[photo.key]"
                class="photo-preview-wrapper"
                style="width: 100%; height: 100%; position: relative"
              >
                <img
                  :src="baForm[photo.key]"
                  style="width: 100%; height: 100%; object-fit: cover"
                  alt="Pratinjau"
                />
                <button
                  v-if="isEditing"
                  @click="clearPhoto(photo.key)"
                  type="button"
                  class="btn-clear-photo"
                  style="
                    position: absolute;
                    top: 5px;
                    right: 5px;
                    background: rgba(239, 68, 68, 0.9);
                    color: white;
                    border: none;
                    width: 22px;
                    height: 22px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 14px;
                    cursor: pointer;
                    font-weight: bold;
                    line-height: 1;
                  "
                >
                  &times;
                </button>
              </div>

              <button
                v-else
                type="button"
                class="btn-upload-trigger"
                style="
                  background: transparent;
                  border: none;
                  color: #64748b;
                  font-size: 13px;
                  font-weight: 500;
                  cursor: pointer;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  gap: 5px;
                "
                @click="triggerPhotoUpload(photo.key)"
                :disabled="!isEditing"
              >
                <span style="font-size: 20px; font-weight: bold">+</span>
                <span>Unggah Foto</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Technician Name & Submit -->
        <div class="form-footer">
          <div class="tech-name-block">
            <label class="form-label">Nama Teknisi</label>
            <input
              v-model="baForm.namaTeknisi"
              type="text"
              class="form-input tech-input"
              placeholder="nama..."
              :disabled="!isEditing"
            />
          </div>

          <div class="action-buttons-row">
            <button
              v-if="isEditing"
              @click="submitBa"
              class="btn btn-save"
              :disabled="statusLoading"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                class="save-icon"
              >
                <path
                  d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"
                ></path>
                <polyline points="17 21 17 13 7 13 7 21"></polyline>
                <polyline points="7 3 7 8 15 8"></polyline>
              </svg>
              Simpan
            </button>
            <template v-else-if="selectedRegForBa.status === 'SELESAI'">
              <button
                type="button"
                @click="isEditing = true"
                class="btn btn-edit-ba"
                style="
                  background: #f59e0b;
                  color: white;
                  border: none;
                  padding: 10px 20px;
                  border-radius: 6px;
                  font-weight: 600;
                  cursor: pointer;
                  margin-right: 10px;
                "
              >
                Edit
              </button>
              <button
                type="button"
                @click="openOfficialBa"
                class="btn btn-official-ba"
                style="
                  background: #3b82f6;
                  color: white;
                  border: none;
                  padding: 10px 20px;
                  border-radius: 6px;
                  font-weight: 600;
                  cursor: pointer;
                "
              >
                BA Aktivasi Resmi
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import {
  getRegistrations,
  getBaAktivasi,
  saveBaAktivasi,
} from "@/services/api";
import { useToast } from "@/composables/useToast";

const toast = useToast();

const registrations = ref([]);
const loading = ref(false);
const statusLoading = ref(false);
const searchQuery = ref("");
const activeTab = ref("DIJADWALKAN");
const selectedRegForBa = ref(null);
const showCustomerData = ref(false);

const isEditing = ref(false);
const currentSubView = ref("FORM"); // "FORM", "OFFICIAL_BA"

// Pagination states
const currentPage = ref(1);
const pageSize = ref(10);

const tabs = [
  { value: "DIJADWALKAN", label: "Dijadwalkan", colorClass: "teal" },
  { value: "SELESAI", label: "Selesai", colorClass: "green" },
];

const currentTabLabel = computed(() => {
  const current = tabs.find((t) => t.value === activeTab.value);
  return current ? current.label : "Dijadwalkan";
});

const filteredRegistrations = computed(() => {
  return registrations.value.filter((r) => r.status === activeTab.value);
});

// Pagination Calculations
const totalPages = computed(() => {
  return Math.ceil(filteredRegistrations.value.length / pageSize.value) || 1;
});

const startIndex = computed(() => {
  return (currentPage.value - 1) * pageSize.value;
});

const endIndex = computed(() => {
  const calculatedEnd = currentPage.value * pageSize.value;
  return calculatedEnd > filteredRegistrations.value.length
    ? filteredRegistrations.value.length
    : calculatedEnd;
});

const paginatedRegistrations = computed(() => {
  return filteredRegistrations.value.slice(startIndex.value, endIndex.value);
});

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
}
// 5 Fixed Equipment rows metadata
const baItems = [
  {
    name: "Modem / ONT / ONU",
    qtyKey: "qtyOnt",
    snKey: "snOnt",
    fotoKey: "fotoOnt",
    defaultQty: 1,
  },
  {
    name: "Wi-Fi Router",
    qtyKey: "qtyRouter",
    snKey: "snRouter",
    fotoKey: "fotoRouter",
    defaultQty: 1,
  },
  {
    name: "Kabel Fiber Optic Drop Core (Meter)",
    qtyKey: "qtyKabel",
    snKey: "snKabel",
    fotoKey: "fotoKabel",
    defaultQty: 150,
    noSn: true,
  },
  {
    name: "Roset Optik",
    qtyKey: "qtyRoset",
    snKey: "snRoset",
    fotoKey: "fotoRoset",
    defaultQty: 1,
    noSn: true,
  },
  {
    name: "Fast Connector / Aksesoris",
    qtyKey: "qtyAksesoris",
    snKey: "snAksesoris",
    fotoKey: "fotoAksesoris",
    defaultQty: 2,
    noSn: true,
  },
];

const docPhotos = [
  { label: "Rumah Customer", key: "fotoRumah" },
  { label: "ODP Depan", key: "fotoOdpDepan" },
  { label: "Redaman ODP", key: "fotoRedamanOdp" },
  { label: "Dalem ODP", key: "fotoDalemOdp" },
  { label: "Redaman Kabel", key: "fotoRedamanKabel" },
  { label: "Depan ONT", key: "fotoDepanOnt" },
  { label: "Belakang ONT", key: "fotoBelakangOnt" },
  { label: "Speed Test", key: "fotoSpeedTest" },
];

function triggerPhotoUpload(key) {
  const input = document.getElementById("photo-file-" + key);
  if (input) {
    input.click();
  }
}

// BA Form state
const baForm = ref({
  namaTeknisi: "",
  catatanTeknisi: "",
  qtyOnt: 1,
  snOnt: "",
  fotoOnt: "",
  qtyRouter: 1,
  snRouter: "",
  fotoRouter: "",
  qtyKabel: 150,
  snKabel: "—",
  fotoKabel: "",
  qtyRoset: 1,
  snRoset: "—",
  fotoRoset: "",
  qtyAksesoris: 2,
  snAksesoris: "—",
  fotoAksesoris: "",
});

async function fetchList() {
  loading.value = true;
  try {
    const params = {
      search: searchQuery.value,
    };
    const res = await getRegistrations(params);
    registrations.value = res;
    currentPage.value = 1;
  } catch (e) {
    toast.error("Gagal mengambil data jadwal: " + e.message);
  } finally {
    loading.value = false;
  }
}

async function openBaForm(reg) {
  selectedRegForBa.value = reg;
  showCustomerData.value = false;
  currentSubView.value = "FORM";
  isEditing.value = reg.status !== "SELESAI";

  // Reset form to defaults
  baForm.value = {
    namaTeknisi: "",
    catatanTeknisi: "",
    qtyOnt: 1,
    snOnt: "",
    fotoOnt: "",
    qtyRouter: 1,
    snRouter: "",
    fotoRouter: "",
    qtyKabel: 150,
    snKabel: "—",
    fotoKabel: "",
    qtyRoset: 1,
    snRoset: "—",
    fotoRoset: "",
    qtyAksesoris: 2,
    snAksesoris: "—",
    fotoAksesoris: "",
    tandaTanganCustomer: "",
    tandaTanganTeknisi: "",
    // New fields
    bandwidth: reg.pkg ? reg.pkg.name + " (" + reg.pkg.speed + " Mbps)" : "",
    panjangKabel: 150,
    koordinatRumah: reg.koordinat || "",
    kodeOdp: "",
    portOdp: 16,
    portOdpTerpakai: "",
    pop: "",
    olt: "",
    rosset: "Ya",
    pigtail: 2,
    patchcore: 0,
    splicing: 2,
    redamanOutputKabel: "",
    jumlahBracket: 0,
    jumlahSClamp: 0,
    statusAktivasi: "Done",
    fotoRumah: "",
    fotoOdpDepan: "",
    fotoRedamanOdp: "",
    fotoDalemOdp: "",
    fotoRedamanKabel: "",
    fotoDepanOnt: "",
    fotoBelakangOnt: "",
    fotoSpeedTest: "",
  };

  // If already completed or has saved data, fetch from backend
  try {
    const savedBa = await getBaAktivasi(reg.id);
    if (savedBa) {
      baForm.value = {
        namaTeknisi: savedBa.namaTeknisi || "",
        catatanTeknisi: savedBa.catatanTeknisi || "",
        qtyOnt: savedBa.qtyOnt !== null ? savedBa.qtyOnt : 1,
        snOnt: savedBa.snOnt || "",
        fotoOnt: savedBa.fotoOnt || "",
        qtyRouter: savedBa.qtyRouter !== null ? savedBa.qtyRouter : 1,
        snRouter: savedBa.snRouter || "",
        fotoRouter: savedBa.fotoRouter || "",
        qtyKabel: savedBa.qtyKabel !== null ? savedBa.qtyKabel : 150,
        snKabel: savedBa.snKabel || "—",
        fotoKabel: savedBa.fotoKabel || "",
        qtyRoset: savedBa.qtyRoset !== null ? savedBa.qtyRoset : 1,
        snRoset: savedBa.snRoset || "—",
        fotoRoset: savedBa.fotoRoset || "",
        qtyAksesoris: savedBa.qtyAksesoris !== null ? savedBa.qtyAksesoris : 2,
        snAksesoris: savedBa.snAksesoris || "—",
        fotoAksesoris: savedBa.fotoAksesoris || "",
        tandaTanganCustomer: savedBa.tandaTanganCustomer || "",
        tandaTanganTeknisi: savedBa.tandaTanganTeknisi || "",
        // New fields
        bandwidth:
          savedBa.bandwidth ||
          (reg.pkg ? reg.pkg.name + " (" + reg.pkg.speed + " Mbps)" : ""),
        panjangKabel:
          savedBa.panjangKabel !== null
            ? savedBa.panjangKabel
            : savedBa.qtyKabel || 150,
        koordinatRumah: savedBa.koordinatRumah || reg.koordinat || "",
        kodeOdp: savedBa.kodeOdp || "",
        portOdp: savedBa.portOdp !== null ? savedBa.portOdp : 16,
        portOdpTerpakai: savedBa.portOdpTerpakai || "",
        pop: savedBa.pop || "",
        olt: savedBa.olt || "",
        rosset: savedBa.rosset || (savedBa.qtyRoset > 0 ? "Ya" : "Tidak"),
        pigtail: savedBa.pigtail !== null ? savedBa.pigtail : 2,
        patchcore: savedBa.patchcore !== null ? savedBa.patchcore : 0,
        splicing: savedBa.splicing !== null ? savedBa.splicing : 2,
        redamanOutputKabel: savedBa.redamanOutputKabel || "",
        jumlahBracket:
          savedBa.jumlahBracket !== null ? savedBa.jumlahBracket : 0,
        jumlahSClamp: savedBa.jumlahSClamp !== null ? savedBa.jumlahSClamp : 0,
        statusAktivasi: savedBa.statusAktivasi || "Done",
        fotoRumah: savedBa.fotoRumah || "",
        fotoOdpDepan: savedBa.fotoOdpDepan || "",
        fotoRedamanOdp: savedBa.fotoRedamanOdp || "",
        fotoDalemOdp: savedBa.fotoDalemOdp || "",
        fotoRedamanKabel: savedBa.fotoRedamanKabel || "",
        fotoDepanOnt: savedBa.fotoDepanOnt || "",
        fotoBelakangOnt: savedBa.fotoBelakangOnt || "",
        fotoSpeedTest: savedBa.fotoSpeedTest || "",
      };
    }
  } catch (e) {
    console.log("No saved BA found or failed loading, using defaults.");
  }
}

function closeBaForm() {
  selectedRegForBa.value = null;
  fetchList();
}

function triggerUpload(idx) {
  const input = document.getElementById("file-" + idx);
  if (input) {
    input.click();
  }
}

function handleFileUpload(event, fotoKey) {
  const file = event.target.files[0];
  if (!file) return;

  // Limit file size to 15MB to allow high-quality pictures
  if (file.size > 15 * 1024 * 1024) {
    toast.error("Ukuran file terlalu besar! Maksimal 15MB.");
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    baForm.value[fotoKey] = e.target.result;
    toast.success("Foto berhasil dilampirkan.");
  };
  reader.readAsDataURL(file);
}

function clearPhoto(fotoKey) {
  baForm.value[fotoKey] = "";
}

async function submitBa() {
  if (!baForm.value.namaTeknisi || !baForm.value.namaTeknisi.trim()) {
    toast.error("Nama Teknisi wajib diisi!");
    return;
  }

  statusLoading.value = true;
  try {
    const isAlreadyCompleted = selectedRegForBa.value.status === "SELESAI";
    const saved = await saveBaAktivasi(selectedRegForBa.value.id, baForm.value);
    toast.success("Dokumen BA Aktivasi berhasil disimpan!");

    // Refresh list in background
    getRegistrations({ search: searchQuery.value }).then((res) => {
      registrations.value = res;
      const updated = res.find((r) => r.id === selectedRegForBa.value.id);
      if (updated) {
        selectedRegForBa.value = updated;
      }
    });

    isEditing.value = false;

    if (!isAlreadyCompleted) {
      closeBaForm();
      activeTab.value = "SELESAI";
    } else {
      await openBaForm(selectedRegForBa.value);
    }
  } catch (e) {
    toast.error("Gagal memproses aktivasi: " + e.message);
  } finally {
    statusLoading.value = false;
  }
}

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return "—";
  try {
    const date = new Date(dateTimeStr);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");
    const hh = String(date.getHours()).padStart(2, "0");
    const min = String(date.getMinutes()).padStart(2, "0");
    const sec = String(date.getSeconds()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd} ${hh}:${min}:${sec}`;
  } catch (e) {
    return dateTimeStr;
  }
}

function getStatusLabel(status) {
  switch (status) {
    case "DIJADWALKAN":
      return "dijadwalkan";
    case "SELESAI":
      return "selesai";
    default:
      return status;
  }
}

function formatRp(value) {
  if (!value) return "Rp 0";
  return "Rp " + Math.round(value).toLocaleString("id-ID");
}

// Interactive Digital Signature Logic (Technician side)
const customerSigCanvas = ref(null);
const technicianSigCanvas = ref(null);
const customerSigEditing = ref(false);
const technicianSigEditing = ref(false);
const savingSigs = ref(false);

let isSigDrawing = { customer: false, technician: false };
let lastSigX = { customer: 0, technician: 0 };
let lastSigY = { customer: 0, technician: 0 };
let hasDrawn = { customer: false, technician: false };

function startSigDraw(e, type) {
  isSigDrawing[type] = true;
  const canvas =
    type === "customer" ? customerSigCanvas.value : technicianSigCanvas.value;
  if (!canvas) return;
  const rect = canvas.getBoundingClientRect();
  lastSigX[type] = (e.clientX - rect.left) * (canvas.width / rect.width);
  lastSigY[type] = (e.clientY - rect.top) * (canvas.height / rect.height);
}

function drawSig(e, type) {
  if (!isSigDrawing[type]) return;
  const canvas =
    type === "customer" ? customerSigCanvas.value : technicianSigCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  const rect = canvas.getBoundingClientRect();
  const x = (e.clientX - rect.left) * (canvas.width / rect.width);
  const y = (e.clientY - rect.top) * (canvas.height / rect.height);

  ctx.beginPath();
  ctx.moveTo(lastSigX[type], lastSigY[type]);
  ctx.lineTo(x, y);
  ctx.strokeStyle = "#0d1340";
  ctx.lineWidth = 3;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.stroke();

  lastSigX[type] = x;
  lastSigY[type] = y;
  hasDrawn[type] = true;
}

function stopSigDraw(type) {
  isSigDrawing[type] = false;
}

function startSigDrawTouch(e, type) {
  e.preventDefault();
  isSigDrawing[type] = true;
  const canvas =
    type === "customer" ? customerSigCanvas.value : technicianSigCanvas.value;
  if (!canvas) return;
  const rect = canvas.getBoundingClientRect();
  const touch = e.touches[0];
  lastSigX[type] = (touch.clientX - rect.left) * (canvas.width / rect.width);
  lastSigY[type] = (touch.clientY - rect.top) * (canvas.height / rect.height);
}

function drawSigTouch(e, type) {
  e.preventDefault();
  if (!isSigDrawing[type]) return;
  const canvas =
    type === "customer" ? customerSigCanvas.value : technicianSigCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  const rect = canvas.getBoundingClientRect();
  const touch = e.touches[0];
  const x = (touch.clientX - rect.left) * (canvas.width / rect.width);
  const y = (touch.clientY - rect.top) * (canvas.height / rect.height);

  ctx.beginPath();
  ctx.moveTo(lastSigX[type], lastSigY[type]);
  ctx.lineTo(x, y);
  ctx.strokeStyle = "#0d1340";
  ctx.lineWidth = 3;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.stroke();

  lastSigX[type] = x;
  lastSigY[type] = y;
  hasDrawn[type] = true;
}

function clearSigCanvas(type) {
  const canvas =
    type === "customer" ? customerSigCanvas.value : technicianSigCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  hasDrawn[type] = false;
}

function editCustomerSig() {
  customerSigEditing.value = true;
  hasDrawn.customer = false;
}

function cancelCustomerSigEdit() {
  customerSigEditing.value = false;
}

function editTechnicianSig() {
  technicianSigEditing.value = true;
  hasDrawn.technician = false;
}

function cancelTechnicianSigEdit() {
  technicianSigEditing.value = false;
}

async function saveOfficialSignatures() {
  savingSigs.value = true;
  try {
    const payload = { ...baForm.value };

    if (customerSigEditing.value || !baForm.value.tandaTanganCustomer) {
      if (customerSigCanvas.value && hasDrawn.customer) {
        payload.tandaTanganCustomer =
          customerSigCanvas.value.toDataURL("image/png");
      }
    }

    if (technicianSigEditing.value || !baForm.value.tandaTanganTeknisi) {
      if (technicianSigCanvas.value && hasDrawn.technician) {
        payload.tandaTanganTeknisi =
          technicianSigCanvas.value.toDataURL("image/png");
      }
    }

    const updated = await saveBaAktivasi(selectedRegForBa.value.id, payload);
    baForm.value = updated;

    customerSigEditing.value = false;
    technicianSigEditing.value = false;
    toast.success("Tanda tangan digital berhasil disimpan!");
  } catch (e) {
    toast.error("Gagal menyimpan tanda tangan: " + e.message);
  } finally {
    savingSigs.value = false;
  }
}

function openOfficialBa() {
  currentSubView.value = "OFFICIAL_BA";
  customerSigEditing.value = false;
  technicianSigEditing.value = false;
}

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.schedule-dashboard {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header & Meta */
.page-meta {
  font-size: 13px;
  color: var(--text-3);
  margin-top: 4px;
}

.breadcrumbs {
  font-size: 12px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 8px;
}

.breadcrumbs .arrow {
  color: var(--border);
}

.breadcrumbs .active {
  color: var(--navy);
  font-weight: 600;
}

/* Cards */
.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.02);
  margin-bottom: 20px;
}

/* Toolbar Style */
.toolbar-card {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* Status Tabs */
.status-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--surface-2);
  border: 1px solid var(--border);
  color: var(--text-2);
}

.tab-btn--teal.tab-btn--active {
  background: #0d9488;
  border-color: #0f766e;
  color: white;
}

.tab-btn--green.tab-btn--active {
  background: #10b981;
  border-color: #059669;
  color: white;
}

.tab-btn:hover:not(.tab-btn--active) {
  background: var(--border);
  color: var(--navy);
}

/* Search bar */
.search-box {
  display: flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  overflow: hidden;
  height: 38px;
}

.search-input {
  border: none !important;
  border-radius: 0 !important;
  outline: none !important;
  box-shadow: none !important;
  width: 200px;
  font-size: 12px;
}

.btn-cari {
  border-radius: 0 !important;
  height: 100% !important;
  padding: 0 16px !important;
  font-size: 12px !important;
}

.btn-refresh {
  height: 38px !important;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px !important;
  padding: 0 16px !important;
}

.refresh-icon {
  width: 14px;
  height: 14px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Tables */
.table-card {
  padding: 0;
  overflow: hidden;
}

.table-header-info {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border);
  font-size: 12px;
  color: var(--text-3);
  background: var(--surface-2);
  display: flex;
  justify-content: flex-end;
}

.table-responsive {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  background: var(--surface-2);
  padding: 14px 20px;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border);
}

.data-table td {
  padding: 14px 20px;
  font-size: 13px;
  color: var(--navy);
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.customer-info-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.customer-name {
  font-weight: 700;
  color: var(--navy);
}

.customer-subtext {
  font-size: 11px;
  color: var(--text-3);
}

.btn-detail {
  padding: 5px 12px !important;
  font-size: 11px !important;
  font-weight: 700 !important;
}

/* Table states */
.loading-state,
.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: var(--text-3);
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  margin: 0 auto 12px;
  animation: spin 0.8s linear infinite;
}

.empty-icon {
  width: 40px;
  height: 40px;
  color: var(--border);
  margin-bottom: 12px;
}

.table-footer {
  padding: 14px 20px;
  background: var(--surface-2);
  border-top: 1px solid var(--border);
  font-size: 12px;
  color: var(--text-3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-buttons {
  display: flex;
  gap: 6px;
}

.btn-pag {
  padding: 6px 12px !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-2);
}

.btn-pag:hover:not(:disabled) {
  background: var(--border);
  color: var(--navy);
}

.btn-pag:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* BA Activation View Styles */
.ba-container {
  display: flex;
  flex-direction: column;
}

.ba-header-row {
  margin-bottom: 16px;
}

.ba-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--navy);
}

.ba-meta {
  font-size: 13px;
  color: var(--text-3);
  margin-top: 4px;
}

.status-indicator {
  font-weight: 700;
  color: #0d9488;
  text-transform: capitalize;
}

.ba-cust-info {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 6px;
}

.btn-back {
  align-self: flex-start;
  background: var(--surface-2);
  border: 1px solid var(--border);
  color: var(--navy);
  padding: 6px 12px !important;
  font-size: 11px !important;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;
  border-radius: 6px;
}

.btn-back:hover {
  background: var(--border);
}

.back-arrow {
  width: 12px;
  height: 12px;
}

/* Collapsible Customer Dropdown */
.customer-dropdown-card {
  margin-bottom: 20px;
  overflow: hidden;
}

.dropdown-header {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  background: var(--surface-2);
  user-select: none;
}

.dropdown-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--navy);
}

.hint-text {
  font-size: 12px;
  color: var(--text-3);
  font-weight: normal;
  margin-left: 4px;
}

.chevron-icon {
  width: 16px;
  height: 16px;
  color: var(--text-3);
  transition: transform 0.25s ease;
}

.chevron-icon.open {
  transform: rotate(90deg);
}

.dropdown-content {
  padding: 20px;
  border-top: 1px solid var(--border);
  background: var(--surface);
}

.dropdown-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

@media (max-width: 768px) {
  .dropdown-grid {
    grid-template-columns: 1fr;
  }
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 10px;
  text-transform: uppercase;
  color: var(--text-3);
  letter-spacing: 0.5px;
  font-weight: 700;
}

.info-value {
  font-size: 13px;
  color: var(--navy);
  line-height: 1.4;
}

/* Equipment activation form card */
.form-card {
  padding: 24px;
}

.form-card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--navy);
  border-bottom: 1.5px solid var(--border);
  padding-bottom: 8px;
  margin-bottom: 20px;
}

/* Catatan untuk teknisi */
.notes-block {
  margin-bottom: 20px;
}

.notes-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: var(--navy);
  margin-bottom: 6px;
}

.notes-textarea {
  min-height: 50px;
  width: 100%;
  resize: vertical;
  background: var(--surface-2);
  border: 1px solid var(--border);
  padding: 10px 14px;
  font-size: 13px;
}

/* Equipment Table */
.equipment-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  margin-bottom: 24px;
}

.equipment-table th {
  background: var(--surface-2);
  padding: 12px 16px;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-3);
  text-transform: uppercase;
  border-bottom: 1px solid var(--border);
}

.equipment-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
  font-size: 13px;
  color: var(--navy);
}

.eq-name {
  font-weight: 600;
}

.qty-input {
  width: 80px;
  text-align: center;
}

.sn-input {
  width: 100%;
  font-family: monospace;
}

/* Upload cells styles */
.upload-cell {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hidden-file-input {
  display: none;
}

.btn-upload-trigger {
  background: none;
  border: 1.5px dashed var(--border);
  border-radius: 6px;
  color: var(--primary);
  padding: 6px 14px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-upload-trigger:hover {
  border-color: var(--primary);
  background: rgba(37, 99, 235, 0.02);
}

.photo-preview-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--border);
  background: var(--surface-2);
}

.photo-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-clear-photo {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  font-size: 11px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.btn-clear-photo:hover {
  background: rgba(239, 68, 68, 0.9);
}

/* Footer elements */
.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-top: 1.5px solid var(--border);
  padding-top: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.tech-name-block {
  width: 300px;
  max-width: 100%;
}

.tech-input {
  width: 100%;
}

.btn-save {
  background: #10b981;
  border-color: #059669;
  color: white;
  padding: 10px 24px !important;
  font-size: 13px !important;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover:not(:disabled) {
  background: #059669;
}

.save-icon {
  width: 16px;
  height: 16px;
}

.completion-banner {
  font-size: 13px;
  font-weight: 700;
  color: #059669;
  background: rgba(16, 185, 129, 0.05);
  border: 1px solid rgba(16, 185, 129, 0.15);
  padding: 8px 16px;
  border-radius: 6px;
  display: inline-block;
}

/* Transitions & Animations */
.expand-enter-active,
.expand-leave-active {
  transition:
    max-height 0.25s ease-out,
    opacity 0.25s ease-out;
  max-height: 300px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.animate-fade-in {
  animation: fadeIn 0.2s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Official BA View & Signature Styles */
.official-ba-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.official-ba-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.btn-print {
  background: #3b82f6 !important;
  color: white !important;
  padding: 8px 20px !important;
  font-weight: 700 !important;
  border-radius: 8px !important;
  border: none !important;
  cursor: pointer;
}

.official-ba-document {
  background: white;
  color: #1e293b;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 40px;
  border: 1px solid #e2e8f0;
}

.obad-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gelo-logo-svg {
  width: 180px;
  height: auto;
}

.obad-title-section {
  text-align: right;
}

.obad-title {
  font-size: 28px;
  font-weight: 900;
  letter-spacing: 2px;
  color: #0ea5e9;
  margin: 0;
}

.obad-subtitle {
  font-size: 14px;
  font-weight: 700;
  color: #475569;
  margin: 2px 0 0 0;
}

.obad-divider {
  height: 3px;
  background: #0ea5e9;
  margin: 20px 0;
  border-radius: 99px;
}

.obad-date-row {
  text-align: right;
  font-size: 13px;
  color: #475569;
  margin-bottom: 15px;
}

.obad-meta-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.obad-meta-table td {
  padding: 10px 14px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
}

.obadm-label {
  width: 200px;
  background: #f8fafc;
  font-weight: 700;
  color: #334155;
}

.obadm-value {
  color: #0f172a;
}

.obad-notes-block {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 15px;
}

.notes-block-title {
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  margin: 0 0 8px 0;
  text-transform: uppercase;
}

.notes-block-content {
  font-size: 13px;
  color: #1e293b;
  line-height: 1.5;
}

.obad-devices-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.obad-devices-table th {
  background: #f1f5f9;
  color: #334155;
  font-weight: 700;
  text-align: left;
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  font-size: 12px;
  text-transform: uppercase;
}

.obad-devices-table td {
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
  color: #1e293b;
}

.obad-signatures-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.obad-signature-box {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 15px;
  background: #f8fafc;
}

.sig-title {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px 0;
  text-transform: uppercase;
}

.sig-name {
  font-size: 12px;
  color: #475569;
  margin-bottom: 8px;
}

.sig-canvas-wrapper {
  background: white;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  min-height: 150px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.sig-image-display {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  position: relative;
}

.signature-img {
  max-width: 100%;
  max-height: 120px;
  object-fit: contain;
}

.btn-sig-clear {
  position: absolute;
  top: 5px;
  right: 5px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
  padding: 2px 8px;
  font-size: 11px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-sig-clear:hover {
  background: rgba(239, 68, 68, 0.2);
}

.sig-canvas-draw {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
}

.sig-canvas-element {
  background: #ffffff;
  border: 1px dashed #cbd5e1;
  border-radius: 4px;
  cursor: crosshair;
  touch-action: none;
  max-width: 100%;
}

.sig-canvas-actions {
  display: flex;
  gap: 8px;
  margin-top: 6px;
  width: 100%;
  justify-content: center;
}

.btn-canvas-clear,
.btn-canvas-cancel {
  border: 1px solid #cbd5e1;
  background: #ffffff;
  padding: 3px 10px;
  font-size: 11px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-canvas-clear {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.2);
  background: rgba(239, 68, 68, 0.04);
}
.btn-canvas-clear:hover {
  background: rgba(239, 68, 68, 0.1);
}

.btn-canvas-cancel {
  color: #475569;
}
.btn-canvas-cancel:hover {
  background: #f1f5f9;
}

.btn-save-signatures {
  background: #10b981 !important;
  color: white !important;
}
.btn-save-signatures:hover {
  background: #059669 !important;
}

.sig-timestamp {
  font-size: 11px;
  color: #64748b;
  margin-top: 8px;
  text-align: center;
}

.obad-footer-info {
  border-top: 1px solid #e2e8f0;
  padding-top: 15px;
  text-align: center;
}

.obadf-company {
  font-weight: 700;
  font-size: 12px;
  color: #0f172a;
}

.obadf-address {
  font-size: 11px;
  color: #475569;
  margin-top: 2px;
}

.obadf-links {
  font-size: 11px;
  color: #0ea5e9;
  margin-top: 2px;
}

/* Print specific styles */
@media print {
  body * {
    visibility: hidden;
  }
  .official-ba-document,
  .official-ba-document * {
    visibility: visible;
  }
  .official-ba-document {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    border: none;
    box-shadow: none;
    padding: 0;
    margin: 0;
  }
  .no-print {
    display: none !important;
  }
}
</style>
