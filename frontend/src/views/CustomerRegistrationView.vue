<template>
  <div class="registration-dashboard">
    <!-- ===================== LIST VIEW ===================== -->
    <template v-if="!selectedReg">
      <!-- Header -->
      <div class="page-header">
        <div class="page-header-left">
          <h1 class="page-title">Customer Registration</h1>
          <div class="page-meta">
            Total: <strong>{{ filteredRegistrations.length }}</strong> | Status:
            <strong>{{ currentTabLabel }}</strong>
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
            <button
              v-if="activeTab === 'SELESAI'"
              @click="exportToExcel"
              class="btn btn--success btn-export"
              style="margin-left: 8px; background: #22c55e; color: white;"
            >
              Export Excel
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

      <!-- Table -->
      <div class="card table-card">
        <div v-if="loading && registrations.length === 0" class="loading-state">
          <div class="spinner"></div>
          <p>Memuat data registrasi...</p>
        </div>

        <div v-else-if="filteredRegistrations.length === 0" class="empty-state">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            class="empty-icon"
          >
            <path
              d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
            ></path>
            <polyline points="14 2 14 8 20 8"></polyline>
          </svg>
          <p>Tidak ada data registrasi customer dalam status ini.</p>
        </div>

        <div v-else class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 60px">No</th>
                <th>Waktu</th>
                <th>Customer</th>
                <th>Kontak</th>
                <th>Internet Speed</th>
                <th>Status</th>
                <th style="width: 100px; text-align: center">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(reg, index) in filteredRegistrations" :key="reg.id">
                <td>{{ index + 1 }}</td>
                <td class="text-secondary">{{ formatDateTime(reg.waktu) }}</td>
                <td>
                  <div class="customer-info-cell">
                    <span class="customer-name"
                      >{{ reg.firstName }} {{ reg.lastName }}</span
                    >
                    <span class="customer-subtext">{{ reg.email }}</span>
                    <span class="customer-subtext"
                      >{{ reg.jenisIdentitas }}: {{ reg.nomorIdentitas }}</span
                    >
                  </div>
                </td>
                <td>
                  <div class="contact-info-cell">
                    <span class="contact-main">{{
                      reg.nomorSelulerUtama
                    }}</span>
                    <span class="contact-backup text-secondary">{{
                      reg.nomorCadangan || "-"
                    }}</span>
                  </div>
                </td>
                <td>
                  <span class="badge badge--teal-light">
                    {{ reg.pkg?.name || "Paket Custom" }}
                    {{ reg.pkg?.speed ? reg.pkg.speed + " Mbps" : "" }}
                  </span>
                </td>
                <td>
                  <span class="badge" :class="getStatusBadgeClass(reg.status)">
                    {{ getStatusLabel(reg.status) }}
                  </span>
                </td>
                <td style="text-align: center">
                  <button
                    @click="viewDetail(reg)"
                    class="btn btn--outline-primary btn-sm"
                  >
                    Detail
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          Total <strong>{{ filteredRegistrations.length }}</strong> {{ footerStatusText }}.
        </div>
      </div>
    </template>

    <!-- ===================== DETAIL VIEW (full-page) ===================== -->
    <template v-else>
      <template v-if="currentSubView === 'DETAIL'">
        <!-- Detail Header -->
        <div class="detail-view-header">
          <div class="page-header-left">
            <div class="detail-page-title-row">
              <div>
                <h1 class="page-title">
                  Detail Registrasi #{{ selectedReg.id }}
                </h1>
                <div class="page-meta detail-meta-legacy">
                  ID Registrasi: #{{ selectedReg.id }} &nbsp;·&nbsp;
                  {{ formatDateTime(selectedReg.waktu) }}
                </div>
                <div class="page-meta detail-meta-live">
                  <span>
                    Status:
                    <span class="badge" :class="getStatusBadgeClass(selectedReg.status)">
                      {{ getStatusLabel(selectedReg.status) }}
                    </span>
                  </span>
                  <span class="meta-separator">|</span>
                  <span>Created: {{ formatDateTime(selectedReg.waktu) }}</span>
                </div>
                <button class="btn btn--secondary btn-back detail-inline-back" @click="closeDetail">
                  &larr; Kembali
                </button>
                </div>
              </div>
            </div>
        </div>

        <!-- Detail Body: 2-column grid -->
        <div class="detail-page-body">
          <!-- LEFT: Data Customer dark card -->
          <div class="detail-card detail-card--customer">
            <div class="detail-card-header">
              <div class="detail-card-title">Data Customer</div>
              <div class="detail-card-subtitle">
                Ringkasan identitas &amp; paket yang dipilih.
              </div>
            </div>

            <!-- Badges row -->
            <div class="detail-badges-row">
              <span class="detail-speed-badge" v-if="selectedReg.pkg">
                Speed: {{ getPackageLabel(selectedReg) }}
              </span>
              <a
                v-if="selectedReg.nomorSelulerUtama"
                :href="
                  'https://wa.me/' +
                  selectedReg.nomorSelulerUtama.replace(/[^0-9]/g, '')
                "
                target="_blank"
                class="wa-btn"
              >
                <svg viewBox="0 0 24 24" fill="currentColor" class="wa-icon">
                  <path
                    d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"
                  />
                </svg>
                WhatsApp
              </a>
            </div>

            <!-- Data rows table -->
            <div class="detail-data-table">
              <div class="ddt-row">
                <div class="ddt-label">Nama</div>
                <div class="ddt-value">
                  {{ getFullName(selectedReg) }}
                </div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">Email</div>
                <div class="ddt-value">{{ selectedReg.email }}</div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">Nomor HP</div>
                <div class="ddt-value">{{ selectedReg.nomorSelulerUtama }}</div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">Nomor Cadangan</div>
                <div class="ddt-value">
                  {{ selectedReg.nomorCadangan || "-" }}
                </div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">Internet Speed</div>
                <div class="ddt-value">
                  <span class="detail-speed-badge" v-if="selectedReg.pkg">
                    {{ getPackageLabel(selectedReg) }}
                  </span>
                  <span v-else>-</span>
                </div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">ID Type</div>
                <div class="ddt-value">{{ selectedReg.jenisIdentitas }}</div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">ID Number</div>
                <div class="ddt-value">{{ selectedReg.nomorIdentitas }}</div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">Titik Koordinat</div>
                <div class="ddt-value">{{ selectedReg.koordinat || "-" }}</div>
              </div>
              <div class="ddt-row">
                <div class="ddt-label">Afiliator</div>
                <div class="ddt-value">{{ selectedReg.afiliator || "-" }}</div>
              </div>
              <div class="ddt-row" v-if="selectedReg.identityPhotoUrl">
                <div class="ddt-label">
                  {{ selectedReg.jenisIdentitas }} Photo
                </div>
                <div class="ddt-value ddt-photo-row">
                  <button
                    type="button"
                    @click="openFile(selectedReg.identityPhotoUrl)"
                    class="btn-lihat-file"
                    >Lihat File</button
                  >
                  <div v-if="isPdf(selectedReg.identityPhotoUrl)" class="ddt-pdf-container">
                    <svg viewBox="0 0 24 24" class="ddt-pdf-icon" fill="currentColor">
                      <path d="M20 2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-8.5 7.5c0 .83-.67 1.5-1.5 1.5H9v2H7.5V7H10c.83 0 1.5.67 1.5 1.5v1zm5 2c0 .83-.67 1.5-1.5 1.5h-2.5V7H15c.83 0 1.5.67 1.5 1.5v3zm4-3.5H19v1h1.5V10H19v2h-1.5V7h3v1.5zM6 6H4v14c0 1.1.9 2 2 2h14v-2H6V6z"/>
                    </svg>
                    <span class="ddt-pdf-text">PDF Document</span>
                  </div>
                  <img
                    v-else
                    :src="selectedReg.identityPhotoUrl"
                    class="ddt-photo-thumb"
                    alt="ID Photo"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- RIGHT column -->
          <div class="detail-right-col">
            <!-- Update Status Action -->
            <div class="detail-card detail-card--action">
              <!-- Special Action buttons for DIPROSES, DIJADWALKAN, SELESAI, DIBATALKAN and Default -->
              <template v-if="selectedReg.status === 'DIPROSES' || selectedReg.status === 'DIJADWALKAN'">
                <button class="btn-action-full btn-action--disabled" disabled>
                  Status Terupdate
                </button>
                <button
                  @click="openSetBaAktivasi"
                  class="btn-action-full btn-action--blue"
                >
                  Set BA Aktivasi
                </button>
                <button
                  @click="changeStatus(selectedReg.id, 'DIBATALKAN')"
                  class="btn-action-full btn-action--red"
                >
                  Batalkan
                </button>
              </template>

              <template v-else-if="selectedReg.status === 'SELESAI'">
                <button class="btn-action-full btn-action--disabled" disabled>
                  Status Terupdate
                </button>
                <button
                  @click="openSetBaAktivasi"
                  class="btn-action-full btn-action--blue"
                >
                  Set BA Aktivasi
                </button>
                <button
                  @click="viewOfficialBa"
                  class="btn-action-full btn-action--navy"
                >
                  BA Aktivasi Resmi
                </button>
              </template>

              <template v-else>
                <!-- Status is MASUK -->
                <button
                  @click="changeStatus(selectedReg.id, 'DIPROSES')"
                  class="btn-action-full btn-action--blue"
                  :disabled="statusLoading"
                >
                  Update Status ke Diproses
                </button>
                <button
                  @click="openEditRegModal"
                  class="btn-action-full btn-action--orange"
                  style="margin-top: 8px; margin-bottom: 8px;"
                >
                  Edit Data Customer
                </button>
                <button
                  @click="changeStatus(selectedReg.id, 'DIBATALKAN')"
                  class="btn-action-full btn-action--red"
                  :disabled="statusLoading"
                >
                  Batalkan Registrasi
                </button>
              </template>
            </div>

            <!-- Address Card -->
            <div class="detail-card detail-card--address">
              <div class="detail-card-header">
                <div class="detail-card-title">Detail Alamat</div>
                <div class="detail-card-subtitle">
                  Detail alamat ditampilkan lengkap untuk kebutuhan survey/instalasi.
                </div>
              </div>
              <div class="addr-row">
                <div class="addr-label">Alamat</div>
                <div class="addr-value">{{ selectedReg.alamatDetail || "-" }}</div>
              </div>
              <div class="addr-row">
                <div class="addr-label">RT / RW</div>
                <div class="addr-value">
                  {{ selectedReg.rt || "-" }} / {{ selectedReg.rw || "-" }}
                </div>
              </div>
              <div class="addr-row">
                <div class="addr-label">Kelurahan / Desa</div>
                <div class="addr-value">{{ selectedReg.kelurahan || "-" }}</div>
              </div>
              <div class="addr-row">
                <div class="addr-label">Kecamatan</div>
                <div class="addr-value">{{ selectedReg.kecamatan || "-" }}</div>
              </div>
              <div class="addr-row">
                <div class="addr-label">Kota / Kabupaten</div>
                <div class="addr-value">{{ selectedReg.kota || "-" }}</div>
              </div>
              <div class="addr-row">
                <div class="addr-label">Provinsi</div>
                <div class="addr-value">{{ selectedReg.provinsi || "-" }}</div>
              </div>
              <div class="addr-row">
                <div class="addr-label">Kode Pos</div>
                <div class="addr-value">{{ selectedReg.kodePos || "-" }}</div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- ===================== SET BA AKTIVASI VIEW ===================== -->
      <template v-else-if="currentSubView === 'SET_BA'">
        <div class="ba-page-header">
          <div class="page-header-left">
            <h1 class="page-title">Set BA Aktivasi</h1>
            <div class="page-meta ba-page-meta">
              Registrasi #{{ selectedReg.id }} | Status:
              <strong class="status-lowercase">{{ getStatusLabel(selectedReg.status) }}</strong>
            </div>
            <button class="btn btn-back ba-inline-back" @click="goBackToDetail">
              &larr; Kembali
            </button>
          </div>
        </div>

        <div class="detail-page-body ba-setup-layout">
          <!-- LEFT COLUMN: Pengaturan BA Aktivasi (Light Card to match standard style) -->
          <div class="detail-card ba-setup-card">
            <div class="detail-card-header">
              <div class="detail-card-title">Pengaturan BA Aktivasi</div>
              <div class="detail-card-subtitle">
                <span v-if="showBaEditButton">Data BA sudah tersimpan dan dikunci. Klik <strong>Edit</strong> untuk mengubah.</span>
                <span v-else>Lengkapi pengaturan aktivasi sebelum diteruskan ke jadwal pemasangan.</span>
              </div>
            </div>

            <!-- Customer Summary info -->
            <div class="ba-customer-summary">
              <h4 class="section-title">Data Customer</h4>
              <div class="ba-summary-table">
                <div class="bas-row"><div class="bas-label">Nama</div><div class="bas-value">{{ getFullName(selectedReg) }}</div></div>
                <div class="bas-row"><div class="bas-label">Email</div><div class="bas-value">{{ selectedReg.email }}</div></div>
                <div class="bas-row"><div class="bas-label">Nomor HP</div><div class="bas-value">{{ selectedReg.nomorSelulerUtama }}</div></div>
                <div class="bas-row"><div class="bas-label">Nomor Cadangan</div><div class="bas-value">{{ selectedReg.nomorCadangan || "-" }}</div></div>
                <div class="bas-row"><div class="bas-label">Paket Internet</div><div class="bas-value">{{ getPackageLabel(selectedReg) }}</div></div>
                <div class="bas-row"><div class="bas-label">Jenis ID</div><div class="bas-value">{{ selectedReg.jenisIdentitas }}</div></div>
                <div class="bas-row"><div class="bas-label">Nomor ID</div><div class="bas-value">{{ selectedReg.nomorIdentitas }}</div></div>
                <div class="bas-row"><div class="bas-label">Alamat Lengkap</div><div class="bas-value">{{ getFullAddress(selectedReg) }}</div></div>
              </div>
            </div>

            <!-- Inputs -->
            <div class="ba-setup-inputs">
              <div class="ba-input-group">
                <label class="ba-label">Customer ID (unique) <span class="required">*</span></label>
                <input type="text" v-model="baSetupForm.custId" class="ba-input" :disabled="!isEditingBa" placeholder="cth: VN30621" />
                <span class="ba-input-note">Wajib diisi &amp; harus unik.</span>
              </div>

              <div class="ba-input-group">
                <label class="ba-label">Jadwal Pemasangan <span class="required">*</span></label>
                
                <!-- Custom Indonesian Date-Time Picker -->
                <div ref="datepickerContainer" class="custom-datetime-container">
                  <div 
                    class="custom-datepicker-trigger" 
                    @click="isEditingBa && (showDatePicker = !showDatePicker)" 
                    :class="{ 'disabled-trigger': !isEditingBa, 'active-trigger': showDatePicker }"
                  >
                    <span class="trigger-value">
                      {{ formatIndonesianDateTime(baSetupForm.tanggalJadwal) || 'Pilih Tanggal & Jam Pemasangan' }}
                    </span>
                    <svg class="calendar-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="16" y1="2" x2="16" y2="6"></line>
                      <line x1="8" y1="2" x2="8" y2="6"></line>
                      <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                  </div>
                  
                  <div v-if="showDatePicker && isEditingBa" class="custom-datepicker-popover">
                    <div class="picker-panel-body">
                      <!-- Calendar -->
                      <div class="calendar-section">
                        <div class="calendar-header">
                          <button type="button" class="btn-picker-nav" @click="prevMonth">&larr;</button>
                          <span class="calendar-title">{{ indonesianMonths[pickerMonth] }} {{ pickerYear }}</span>
                          <button type="button" class="btn-picker-nav" @click="nextMonth">&rarr;</button>
                        </div>
                        <div class="calendar-weekdays">
                          <span v-for="day in indonesianDays" :key="day" class="weekday-cell">{{ day }}</span>
                        </div>
                        <div class="calendar-days-grid">
                          <button
                            v-for="cell in calendarDays"
                            :key="cell.dateString"
                            type="button"
                            class="day-cell"
                            :class="{
                              'day-current-month': cell.isCurrentMonth,
                              'day-other-month': !cell.isCurrentMonth,
                              'day-selected': selectedDateStr === cell.dateString,
                              'day-today': isTodayDate(cell.dateString)
                            }"
                            @click="selectDate(cell.dateString)"
                          >
                            {{ cell.day }}
                          </button>
                        </div>
                        <div class="calendar-footer-buttons">
                          <button type="button" class="btn-picker-clear" @click="clearDate">Bersihkan</button>
                          <button type="button" class="btn-picker-today" @click="setToday">Hari Ini</button>
                        </div>
                      </div>
                      
                      <!-- Time Selection -->
                      <div class="time-section">
                        <div class="time-header">
                          <span>Jam Pemasangan</span>
                        </div>
                        <div class="time-columns">
                          <div class="time-col-wrapper">
                            <div class="col-title">Jam</div>
                            <div class="time-col scrollable-col">
                              <button
                                v-for="h in hourOptions"
                                :key="h"
                                type="button"
                                class="time-item"
                                :class="{ 'time-item-selected': selectedHour === h }"
                                @click="selectHour(h)"
                              >
                                {{ h }}
                              </button>
                            </div>
                          </div>
                          
                          <div class="time-col-wrapper">
                            <div class="col-title">Menit</div>
                            <div class="time-col scrollable-col">
                              <button
                                v-for="m in minuteOptions"
                                :key="m"
                                type="button"
                                class="time-item"
                                :class="{ 'time-item-selected': selectedMinute === m }"
                                @click="selectMinute(m)"
                              >
                                {{ m }}
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="picker-panel-footer">
                      <div class="selected-summary">
                        Terpilih: <strong>{{ formatIndonesianDateTime(baSetupForm.tanggalJadwal) || '-' }}</strong>
                      </div>
                      <button type="button" class="btn-picker-done" @click="showDatePicker = false">Selesai</button>
                    </div>
                  </div>
                </div>
                
                <span class="ba-input-note">Wajib diisi tanggal &amp; jam pemasangan.</span>
              </div>

              <div class="ba-input-group">
                <label class="ba-label">Harga (Rp) <span class="required">*</span></label>
                <input
                  type="text"
                  inputmode="numeric"
                  v-model="baSetupForm.harga"
                  class="ba-input"
                  :disabled="!isEditingBa"
                  placeholder="cth: Rp 350.000"
                  @input="normalizeHargaInput"
                />
                <span class="ba-input-note">Wajib diisi. Ketik angka saja, nanti auto format.</span>
              </div>

              <div class="ba-input-group">
                <label class="ba-label">Note Request Customer <span class="optional">(opsional)</span></label>
                <textarea v-model="baSetupForm.noteRequest" class="ba-textarea" :disabled="!isEditingBa" placeholder="Speedboost, extra modem, dll..."></textarea>
                <span class="ba-input-note">Opsional, untuk ditampilkan di BA Aktivasi Resmi.</span>
              </div>

              <div class="ba-input-group">
                <label class="ba-label">Note untuk Teknisi (Admin) <span class="optional">(opsional)</span></label>
                <textarea v-model="baSetupForm.noteTeknisi" class="ba-textarea" :disabled="!isEditingBa" placeholder="posisi modem di ruang keluarga, router tambahan untuk lantai atas, speedboost, dll..."></textarea>
                <span class="ba-input-note">Opsional, untuk instruksi tambahan.</span>
              </div>
            </div>

            <!-- Action Button -->
            <div class="ba-setup-actions" style="margin-top: 25px">
              <button v-if="showBaEditButton" @click="isEditingBa = true" class="btn btn-edit-ba">
                Edit
              </button>
              <button v-else @click="saveBaSetup" class="btn btn-save-ba" :disabled="statusLoading">
                {{ statusLoading ? 'Menyimpan...' : baSetupButtonLabel }}
              </button>
            </div>
          </div>

          <!-- RIGHT COLUMN: Teknisi & Perangkat -->
          <div class="detail-card ba-tech-card">
            <div class="detail-card-header">
              <div class="detail-card-title">Teknisi &amp; Perangkat</div>
            </div>

            <div class="ba-tech-info">
              <div class="tech-row">
                <div class="tech-label">Nama Teknisi</div>
                <div class="tech-value">{{ baData ? baData.namaTeknisi : '—' }}</div>
              </div>
            </div>

            <div class="ba-devices-table-wrapper">
              <table class="ba-devices-table">
                <thead>
                  <tr>
                    <th style="width: 50px">No</th>
                    <th>Equipment</th>
                    <th style="width: 70px; text-align: center">Qty</th>
                    <th>Serial Number</th>
                    <th>Foto</th>
                  </tr>
                </thead>
                <tbody>
                  <!-- Row 1: ONT -->
                  <tr v-if="baData && baData.qtyOnt > 0">
                    <td>1</td>
                    <td>Modem / ONT / ONU</td>
                    <td style="text-align: center">{{ baData.qtyOnt }}</td>
                    <td>{{ baData.snOnt || '—' }}</td>
                    <td style="text-align: center">
                      <img v-if="baData.fotoOnt" :src="baData.fotoOnt" class="ba-device-thumb" @click="openFile(baData.fotoOnt)" style="cursor: pointer" />
                      <span v-else>—</span>
                    </td>
                  </tr>
                  <!-- Row 2: Router -->
                  <tr v-if="baData && baData.qtyRouter > 0">
                    <td>2</td>
                    <td>Wi-Fi Router</td>
                    <td style="text-align: center">{{ baData.qtyRouter }}</td>
                    <td>{{ baData.snRouter || '—' }}</td>
                    <td style="text-align: center">
                      <img v-if="baData.fotoRouter" :src="baData.fotoRouter" class="ba-device-thumb" @click="openFile(baData.fotoRouter)" style="cursor: pointer" />
                      <span v-else>—</span>
                    </td>
                  </tr>
                  <!-- Row 3: Kabel -->
                  <tr v-if="baData && baData.qtyKabel > 0">
                    <td>3</td>
                    <td>Kabel Fiber Optic Drop Core (Meter)</td>
                    <td style="text-align: center">{{ baData.qtyKabel }}</td>
                    <td>{{ baData.snKabel || '—' }}</td>
                    <td style="text-align: center">
                      <img v-if="baData.fotoKabel" :src="baData.fotoKabel" class="ba-device-thumb" @click="openFile(baData.fotoKabel)" style="cursor: pointer" />
                      <span v-else>—</span>
                    </td>
                  </tr>
                  <!-- Row 4: Roset -->
                  <tr v-if="baData && baData.qtyRoset > 0">
                    <td>4</td>
                    <td>Roset Optik</td>
                    <td style="text-align: center">{{ baData.qtyRoset }}</td>
                    <td>{{ baData.snRoset || '—' }}</td>
                    <td style="text-align: center">
                      <img v-if="baData.fotoRoset" :src="baData.fotoRoset" class="ba-device-thumb" @click="openFile(baData.fotoRoset)" style="cursor: pointer" />
                      <span v-else>—</span>
                    </td>
                  </tr>
                  <!-- Row 5: Aksesoris -->
                  <tr v-if="baData && baData.qtyAksesoris > 0">
                    <td>5</td>
                    <td>Fast Connector / Aksesoris</td>
                    <td style="text-align: center">{{ baData.qtyAksesoris }}</td>
                    <td>{{ baData.snAksesoris || '—' }}</td>
                    <td style="text-align: center">
                      <img v-if="baData.fotoAksesoris" :src="baData.fotoAksesoris" class="ba-device-thumb" @click="openFile(baData.fotoAksesoris)" style="cursor: pointer" />
                      <span v-else>—</span>
                    </td>
                  </tr>
                  <!-- Fallback empty state -->
                  <tr v-if="baDeviceRows.length === 0">
                    <td colspan="5" class="ba-table-empty">-</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </template>

      <!-- ===================== OFFICIAL BA VIEW ===================== -->
      <template v-else-if="currentSubView === 'OFFICIAL_BA'">
        <div class="official-ba-container">
          <!-- Action bar (hidden when printing) -->
          <div class="official-ba-actions no-print">
            <button class="btn btn-back" @click="goBackToDetail">
              &larr; Kembali
            </button>
            <button 
              v-if="baData && (!baData.tandaTanganCustomer || !baData.tandaTanganTeknisi || customerSigEditing || technicianSigEditing)" 
              class="btn btn-print btn-save-signatures" 
              @click="saveOfficialSignatures" 
              :disabled="savingSigs"
            >
              {{ savingSigs ? 'Menyimpan...' : 'Simpan Tanda Tangan' }}
            </button>
            <button class="btn btn-print" @click="window.print()">
              Print
            </button>
          </div>

          <!-- Printable document -->
          <div class="official-ba-document">
            <div class="obad-header">
              <div class="obad-logo-section">
                <!-- SVG Gelo logo matching reference -->
                <svg class="gelo-logo-svg" viewBox="0 0 240 80" fill="none">
                  <path d="M40 50 C20 50, 10 35, 20 20 C30 5, 50 10, 50 25 C50 35, 45 42, 38 45" stroke="#00e5ff" stroke-width="8" stroke-linecap="round"/>
                  <path d="M42 22 L26 22 C20 22, 20 30, 26 30 L42 30" stroke="#00b0ff" stroke-width="6" stroke-linecap="round"/>
                  <text x="75" y="45" fill="#00e5ff" font-family="'Outfit', sans-serif" font-size="34" font-weight="900">GELO</text>
                  <text x="75" y="62" fill="#ffffff" font-family="'Outfit', sans-serif" font-size="13" font-weight="600" letter-spacing="1">By VNet</text>
                </svg>
              </div>
              <div class="obad-title-section">
                <h1 class="obad-title">GELO</h1>
                <h2 class="obad-subtitle">BA AKTIVASI</h2>
              </div>
            </div>

            <div class="obad-divider"></div>

            <div class="obad-date-row">
              Tanggal: <strong>{{ formatDateTime(selectedReg.tanggalJadwal || selectedReg.waktu).substring(0, 10) }}</strong>
            </div>

            <!-- Customer metadata table -->
            <table class="obad-meta-table">
              <tbody>
                <tr>
                  <td class="obadm-label">Nama</td>
                  <td class="obadm-value"><strong>{{ selectedReg.firstName }} {{ selectedReg.lastName }}</strong></td>
                </tr>
                <tr>
                  <td class="obadm-label">Email</td>
                  <td class="obadm-value">{{ selectedReg.email }}</td>
                </tr>
                <tr>
                  <td class="obadm-label">Nomor HP</td>
                  <td class="obadm-value">{{ selectedReg.nomorSelulerUtama }}</td>
                </tr>
                <tr>
                  <td class="obadm-label">Nomor Cadangan</td>
                  <td class="obadm-value">{{ selectedReg.nomorCadangan || "-" }}</td>
                </tr>
                <tr>
                  <td class="obadm-label">Paket Internet</td>
                  <td class="obadm-value"><strong>{{ selectedReg.pkg ? selectedReg.pkg.name : "—" }}</strong></td>
                </tr>
                <tr>
                  <td class="obadm-label">Harga</td>
                  <td class="obadm-value"><strong>Rp {{ formatRp(selectedReg.harga || (selectedReg.pkg ? selectedReg.pkg.price : 0)) }}</strong></td>
                </tr>
                <tr>
                  <td class="obadm-label">Alamat</td>
                  <td class="obadm-value">{{ selectedReg.alamatDetail }}, RT {{ selectedReg.rt }} / RW {{ selectedReg.rw }}, {{ selectedReg.kelurahan }}, {{ selectedReg.kecamatan }}, {{ selectedReg.kota }}, {{ selectedReg.provinsi }}</td>
                </tr>
                <tr>
                  <td class="obadm-label">CID</td>
                  <td class="obadm-value"><strong>{{ selectedReg.custId }}</strong></td>
                </tr>
              </tbody>
            </table>

            <!-- Catatan Tambahan block -->
            <div class="obad-notes-block" style="margin-top: 20px">
              <h3 class="notes-block-title">Catatan Tambahan</h3>
              <div class="notes-block-content">{{ selectedReg.noteRequest || "-" }}</div>
            </div>

            <!-- Perangkat Terpasang table -->
            <div class="obad-devices-section" style="margin-top: 25px">
              <h3 class="section-title">Perangkat Terpasang</h3>
              <table class="obad-devices-table">
                <thead>
                  <tr>
                    <th style="width: 50px">No</th>
                    <th>Equipment</th>
                    <th style="width: 70px; text-align: center">Qty</th>
                    <th>Serial Number</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="baData && baData.qtyOnt > 0">
                    <td style="text-align: center"><strong>1</strong></td>
                    <td>ONT ZTE F670L</td>
                    <td style="text-align: center"><strong>{{ baData.qtyOnt }}</strong></td>
                    <td>{{ baData.snOnt || '—' }}</td>
                  </tr>
                  <tr v-if="baData && baData.qtyRouter > 0">
                    <td style="text-align: center"><strong>2</strong></td>
                    <td>Wi-Fi Router</td>
                    <td style="text-align: center"><strong>{{ baData.qtyRouter }}</strong></td>
                    <td>{{ baData.snRouter || '—' }}</td>
                  </tr>
                  <tr v-if="baData && baData.qtyKabel > 0">
                    <td style="text-align: center"><strong>3</strong></td>
                    <td>Kabel Fiber Optic Drop Core (Meter)</td>
                    <td style="text-align: center"><strong>{{ baData.qtyKabel }}</strong></td>
                    <td>{{ baData.snKabel || '—' }}</td>
                  </tr>
                  <tr v-if="baData && baData.qtyRoset > 0">
                    <td style="text-align: center"><strong>4</strong></td>
                    <td>Roset Optik</td>
                    <td style="text-align: center"><strong>{{ baData.qtyRoset }}</strong></td>
                    <td>{{ baData.snRoset || '—' }}</td>
                  </tr>
                  <tr v-if="baData && baData.qtyAksesoris > 0">
                    <td style="text-align: center"><strong>5</strong></td>
                    <td>Fast Connector / Aksesoris</td>
                    <td style="text-align: center"><strong>{{ baData.qtyAksesoris }}</strong></td>
                    <td>{{ baData.snAksesoris || '—' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Tanda Tangan Digital -->
            <div class="obad-signatures-row" style="margin-top: 30px">
              <!-- Customer signature -->
              <div class="obad-signature-box">
                <h4 class="sig-title">Customer</h4>
                <div class="sig-name">Nama: {{ selectedReg.firstName }} {{ selectedReg.lastName }}</div>
                
                <div class="sig-canvas-wrapper">
                  <!-- Show Image if already saved AND not in edit/clear mode -->
                  <div v-if="baData.tandaTanganCustomer && !customerSigEditing" class="sig-image-display">
                    <img :src="baData.tandaTanganCustomer" class="signature-img" alt="Customer Signature" />
                    <button type="button" class="btn-sig-clear no-print" @click="editCustomerSig">Ulangi</button>
                  </div>
                  <!-- Show Canvas for drawing otherwise -->
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
                      <button type="button" class="btn-canvas-clear" @click="clearSigCanvas('customer')">Bersihkan</button>
                      <button v-if="baData.tandaTanganCustomer" type="button" class="btn-canvas-cancel" @click="cancelCustomerSigEdit">Batal</button>
                    </div>
                  </div>
                </div>
                
                <div class="sig-timestamp">
                  {{ baData.tandaTanganCustomer ? 'Ditandatangani: ' + formatDateTime(selectedReg.tanggalJadwal || selectedReg.waktu) : 'Belum ditandatangani' }}
                </div>
              </div>

              <!-- Technician signature -->
              <div class="obad-signature-box">
                <h4 class="sig-title">Teknisi</h4>
                <div class="sig-name">Nama: {{ baData ? baData.namaTeknisi : 'Teknisi' }}</div>
                
                <div class="sig-canvas-wrapper">
                  <!-- Show Image if already saved AND not in edit/clear mode -->
                  <div v-if="baData.tandaTanganTeknisi && !technicianSigEditing" class="sig-image-display">
                    <img :src="baData.tandaTanganTeknisi" class="signature-img" alt="Technician Signature" />
                    <button type="button" class="btn-sig-clear no-print" @click="editTechnicianSig">Ulangi</button>
                  </div>
                  <!-- Show Canvas for drawing otherwise -->
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
                      <button type="button" class="btn-canvas-clear" @click="clearSigCanvas('technician')">Bersihkan</button>
                      <button v-if="baData.tandaTanganTeknisi" type="button" class="btn-canvas-cancel" @click="cancelTechnicianSigEdit">Batal</button>
                    </div>
                  </div>
                </div>
                
                <div class="sig-timestamp">
                  {{ baData.tandaTanganTeknisi ? 'Ditandatangani: ' + formatDateTime(selectedReg.tanggalJadwal || selectedReg.waktu) : 'Belum ditandatangani' }}
                </div>
              </div>
            </div>

            <!-- Parameter IKR -->
            <div class="obad-section-title" style="margin-top: 30px; font-weight: 700; font-size: 14px; text-transform: uppercase; color: #334155;">Parameter IKR</div>
            <table class="obad-meta-table" style="margin-top: 10px; width: 100%; border-collapse: collapse;">
              <tbody>
                <tr>
                  <td class="obadm-label" style="width: 150px; font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Bandwidth</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.bandwidth || '-' }}</td>
                  <td class="obadm-label" style="width: 150px; font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Kode ODP</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.kodeOdp || '-' }}</td>
                </tr>
                <tr>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Panjang Kabel</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.panjangKabel || '0' }} Meter</td>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Port ODP / Terpakai</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.portOdp || '-' }} / {{ baData.portOdpTerpakai || '-' }}</td>
                </tr>
                <tr>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Koordinat Rumah</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.koordinatRumah || '-' }}</td>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">POP / OLT</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.pop || '-' }} / {{ baData.olt || '-' }}</td>
                </tr>
                <tr>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Rosset</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.rosset || '-' }}</td>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Pigtail / Patchcore</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.pigtail || '0' }} / {{ baData.patchcore || '0' }}</td>
                </tr>
                <tr>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Splicing</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.splicing || '0' }}</td>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Redaman Output</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.redamanOutputKabel || '-' }}</td>
                </tr>
                <tr>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Bracket / S-Clamp</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.jumlahBracket || '0' }} / {{ baData.jumlahSClamp || '0' }}</td>
                  <td class="obadm-label" style="font-weight: 600; padding: 6px 8px; border: 1px solid #cbd5e1; background: #f8fafc;">Status Aktivasi</td>
                  <td class="obadm-value" style="padding: 6px 8px; border: 1px solid #cbd5e1;">{{ baData.statusAktivasi || '-' }}</td>
                </tr>
              </tbody>
            </table>

            <!-- Dokumentasi Foto -->
            <div class="obad-section-title" style="margin-top: 30px; font-weight: 700; font-size: 14px; text-transform: uppercase; color: #334155;">Dokumentasi Foto</div>
            <div class="photo-display-grid" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 10px; margin-bottom: 20px;">
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">Rumah Customer</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoRumah" :src="baData.fotoRumah" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">ODP Depan</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoOdpDepan" :src="baData.fotoOdpDepan" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">Redaman ODP</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoRedamanOdp" :src="baData.fotoRedamanOdp" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">Dalem ODP</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoDalemOdp" :src="baData.fotoDalemOdp" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">Redaman Kabel</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoRedamanKabel" :src="baData.fotoRedamanKabel" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">Depan ONT</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoDepanOnt" :src="baData.fotoDepanOnt" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">Belakang ONT</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoBelakangOnt" :src="baData.fotoBelakangOnt" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
              <div class="photo-display-card" style="border: 1px solid #cbd5e1; border-radius: 4px; padding: 6px; text-align: center; background: white;">
                <div style="font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 5px;">Speed Test</div>
                <div style="width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; overflow: hidden; border-radius: 2px;">
                  <img v-if="baData.fotoSpeedTest" :src="baData.fotoSpeedTest" style="width: 100%; height: 100%; object-fit: cover;" alt="Foto" />
                  <span v-else style="font-size: 11px; color: #94a3b8;">Tidak ada foto</span>
                </div>
              </div>
            </div>

            <!-- Footer Company details -->
            <div class="obad-footer-info" style="margin-top: 40px">
              <div class="obadf-company">PT. Victory Network Indonesia</div>
              <div class="obadf-address">Graha Matapel, Jl. Arjuna Utara No. 46 - Jakarta Barat 11510</div>
              <div class="obadf-links">Customer Care: http://client.gelo.co.id &nbsp;·&nbsp; https://gelo.co.id</div>
            </div>
          </div>
        </div>
      </template>
    </template>

    <!-- Edit Registration Modal -->
    <div v-if="showEditRegModal" class="edit-modal-overlay">
      <div class="edit-modal-box">
        <div class="edit-modal-header">
          <h3 class="emh-title">Edit Data Customer</h3>
          <button type="button" class="edit-modal-close" @click="showEditRegModal = false">&times;</button>
        </div>
        <div class="edit-modal-body">
          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">First Name</label>
              <input type="text" v-model="editRegForm.firstName" class="modal-input" />
            </div>
            <div class="modal-form-group">
              <label class="modal-label">Last Name</label>
              <input type="text" v-model="editRegForm.lastName" class="modal-input" />
            </div>
          </div>
          
          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">Email</label>
              <input type="email" v-model="editRegForm.email" class="modal-input" />
            </div>
            <div class="modal-form-group">
              <label class="modal-label">Paket Internet</label>
              <select v-model="editRegForm.pkg.id" class="modal-input">
                <option value="" disabled>- pilih paket -</option>
                <option v-for="p in packagesList" :key="p.id" :value="p.id">
                  {{ p.name }} - {{ p.speed ? p.speed + ' Mbps' : '' }} ({{ formatRp(p.price) }})
                </option>
              </select>
            </div>
          </div>
          
          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">Nomor Seluler Utama</label>
              <input type="text" v-model="editRegForm.nomorSelulerUtama" class="modal-input" />
            </div>
            <div class="modal-form-group">
              <label class="modal-label">Nomor Cadangan</label>
              <input type="text" v-model="editRegForm.nomorCadangan" class="modal-input" />
            </div>
          </div>

          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">Jenis Identitas</label>
              <select v-model="editRegForm.jenisIdentitas" class="modal-input">
                <option value="KTP">KTP</option>
                <option value="Passport">Passport</option>
                <option value="Tax_id">NPWP</option>
                <option value="Kitas">Kitas</option>
              </select>
            </div>
            <div class="modal-form-group">
              <label class="modal-label">Nomor Identitas</label>
              <input type="text" v-model="editRegForm.nomorIdentitas" class="modal-input" />
            </div>
          </div>

          <div class="modal-form-group">
            <label class="modal-label">Detail Alamat</label>
            <textarea v-model="editRegForm.alamatDetail" class="modal-input modal-textarea" rows="3"></textarea>
          </div>

          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">RT</label>
              <input type="text" v-model="editRegForm.rt" class="modal-input" />
            </div>
            <div class="modal-form-group">
              <label class="modal-label">RW</label>
              <input type="text" v-model="editRegForm.rw" class="modal-input" />
            </div>
          </div>

          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">Kelurahan/Desa</label>
              <input type="text" v-model="editRegForm.kelurahan" class="modal-input" />
            </div>
            <div class="modal-form-group">
              <label class="modal-label">Kecamatan</label>
              <input type="text" v-model="editRegForm.kecamatan" class="modal-input" />
            </div>
          </div>

          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">Kota/Kabupaten</label>
              <input type="text" v-model="editRegForm.kota" class="modal-input" />
            </div>
            <div class="modal-form-group">
              <label class="modal-label">Provinsi</label>
              <input type="text" v-model="editRegForm.provinsi" class="modal-input" />
            </div>
          </div>

          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">Kode Pos</label>
              <input type="text" v-model="editRegForm.kodePos" class="modal-input" />
            </div>
            <div class="modal-form-group">
              <!-- Spacer -->
            </div>
          </div>

          <div class="modal-form-grid-2">
            <div class="modal-form-group">
              <label class="modal-label">Titik Koordinat</label>
              <input type="text" v-model="editRegForm.koordinat" class="modal-input" placeholder="contoh: -6.914744, 106.926574" />
            </div>
            <div class="modal-form-group">
              <label class="modal-label">Afiliator</label>
              <input type="text" v-model="editRegForm.afiliator" class="modal-input" placeholder="Nama afiliator" />
            </div>
          </div>
        </div>
        <div class="edit-modal-footer">
          <button type="button" class="btn btn--secondary" @click="showEditRegModal = false">Batal</button>
          <button type="button" class="btn btn--primary" @click="saveRegEdit" :disabled="statusLoading">
            {{ statusLoading ? 'Menyimpan...' : 'Simpan' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getRegistrations,
  updateRegistrationStatus,
  deleteRegistration,
  getBaAktivasi,
  updateBaSetup,
  getPackages,
  updateRegistration,
} from "@/services/api";
import { useToast } from "@/composables/useToast";


const toast = useToast();
const route = useRoute();
const router = useRouter();

const registrations = ref([]);
const loading = ref(false);
const statusLoading = ref(false);
const searchQuery = ref("");
const activeTab = ref("MASUK");
const selectedReg = ref(null);
const showDatePicker = ref(false);
const jadwalDateTime = ref("");

const currentSubView = ref("DETAIL"); // "DETAIL", "SET_BA", "OFFICIAL_BA"
const baData = ref(null);
const isEditingBa = ref(false);

const showEditRegModal = ref(false);
const packagesList = ref([]);
const editRegForm = ref({
  id: null,
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
  kota: "",
  provinsi: "",
  kodePos: "",
  pkg: { id: "" },
  koordinat: "",
  afiliator: ""
});
const baSetupForm = ref({
  custId: "",
  tanggalJadwal: "",
  harga: 0,
  noteRequest: "",
  noteTeknisi: "",
});

// Custom Indonesian Date-Time Picker State & Logic
const datepickerContainer = ref(null);
const pickerYear = ref(new Date().getFullYear());
const pickerMonth = ref(new Date().getMonth()); // 0-11
const selectedDateStr = ref(""); // YYYY-MM-DD
const selectedTimeStr = ref("09:00"); // HH:mm

const indonesianMonths = [
  "Januari", "Februari", "Maret", "April", "Mei", "Juni",
  "Juli", "Agustus", "September", "Oktober", "November", "Desember"
];
const indonesianDays = ["Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"];

const hourOptions = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));
const minuteOptions = Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0'));

const selectedHour = computed(() => {
  if (!selectedTimeStr.value) return "09";
  return selectedTimeStr.value.split(":")[0] || "09";
});

const selectedMinute = computed(() => {
  if (!selectedTimeStr.value) return "00";
  return selectedTimeStr.value.split(":")[1] || "00";
});

function getDaysInMonth(year, month) {
  return new Date(year, month + 1, 0).getDate();
}

function getStartDayOfMonth(year, month) {
  return new Date(year, month, 1).getDay();
}

const calendarDays = computed(() => {
  const year = pickerYear.value;
  const month = pickerMonth.value;
  
  const daysInMonth = getDaysInMonth(year, month);
  const startDay = getStartDayOfMonth(year, month);
  
  const cells = [];
  
  // Previous month padding
  const prevMonth = month === 0 ? 11 : month - 1;
  const prevYear = month === 0 ? year - 1 : year;
  const daysInPrevMonth = getDaysInMonth(prevYear, prevMonth);
  for (let i = startDay - 1; i >= 0; i--) {
    const day = daysInPrevMonth - i;
    cells.push({
      day,
      month: prevMonth,
      year: prevYear,
      isCurrentMonth: false,
      dateString: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    });
  }
  
  // Current month days
  for (let day = 1; day <= daysInMonth; day++) {
    cells.push({
      day,
      month,
      year,
      isCurrentMonth: true,
      dateString: `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    });
  }
  
  // Next month padding to fill a 6-row grid (42 cells)
  const remaining = 42 - cells.length;
  const nextMonth = month === 11 ? 0 : month + 1;
  const nextYear = month === 11 ? year + 1 : year;
  for (let day = 1; day <= remaining; day++) {
    cells.push({
      day,
      month: nextMonth,
      year: nextYear,
      isCurrentMonth: false,
      dateString: `${nextYear}-${String(nextMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    });
  }
  
  return cells;
});

// Watch for changes in baSetupForm.tanggalJadwal to keep picker state in sync
watch(
  () => baSetupForm.value.tanggalJadwal,
  (newVal) => {
    if (newVal) {
      const parts = newVal.split("T");
      selectedDateStr.value = parts[0];
      if (parts[1]) {
        selectedTimeStr.value = parts[1].substring(0, 5); // HH:mm
      } else {
        selectedTimeStr.value = "09:00";
      }
      
      const dateParts = parts[0].split("-");
      if (dateParts.length === 3) {
        pickerYear.value = parseInt(dateParts[0]);
        pickerMonth.value = parseInt(dateParts[1]) - 1;
      }
    } else {
      selectedDateStr.value = "";
      selectedTimeStr.value = "09:00";
    }
  },
  { immediate: true }
);

function selectDate(dateString) {
  selectedDateStr.value = dateString;
  updateTanggalJadwal();
}

function selectHour(h) {
  selectedTimeStr.value = `${h}:${selectedMinute.value}`;
  updateTanggalJadwal();
}

function selectMinute(m) {
  selectedTimeStr.value = `${selectedHour.value}:${m}`;
  updateTanggalJadwal();
}

// Interactive Digital Signature Logic
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
  const canvas = type === 'customer' ? customerSigCanvas.value : technicianSigCanvas.value;
  if (!canvas) return;
  const rect = canvas.getBoundingClientRect();
  lastSigX[type] = (e.clientX - rect.left) * (canvas.width / rect.width);
  lastSigY[type] = (e.clientY - rect.top) * (canvas.height / rect.height);
}

function drawSig(e, type) {
  if (!isSigDrawing[type]) return;
  const canvas = type === 'customer' ? customerSigCanvas.value : technicianSigCanvas.value;
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
  const canvas = type === 'customer' ? customerSigCanvas.value : technicianSigCanvas.value;
  if (!canvas) return;
  const rect = canvas.getBoundingClientRect();
  const touch = e.touches[0];
  lastSigX[type] = (touch.clientX - rect.left) * (canvas.width / rect.width);
  lastSigY[type] = (touch.clientY - rect.top) * (canvas.height / rect.height);
}

function drawSigTouch(e, type) {
  e.preventDefault();
  if (!isSigDrawing[type]) return;
  const canvas = type === 'customer' ? customerSigCanvas.value : technicianSigCanvas.value;
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
  const canvas = type === 'customer' ? customerSigCanvas.value : technicianSigCanvas.value;
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
    const payload = { ...baData.value };
    
    if (customerSigEditing.value || !baData.value.tandaTanganCustomer) {
      if (customerSigCanvas.value && hasDrawn.customer) {
        payload.tandaTanganCustomer = customerSigCanvas.value.toDataURL("image/png");
      }
    }
    
    if (technicianSigEditing.value || !baData.value.tandaTanganTeknisi) {
      if (technicianSigCanvas.value && hasDrawn.technician) {
        payload.tandaTanganTeknisi = technicianSigCanvas.value.toDataURL("image/png");
      }
    }
    
    const updated = await saveBaAktivasi(selectedReg.value.id, payload);
    baData.value = updated;
    
    customerSigEditing.value = false;
    technicianSigEditing.value = false;
    toast.success("Tanda tangan digital berhasil disimpan!");
  } catch (e) {
    toast.error("Gagal menyimpan tanda tangan: " + e.message);
  } finally {
    savingSigs.value = false;
  }
}

function updateTanggalJadwal() {
  if (selectedDateStr.value) {
    baSetupForm.value.tanggalJadwal = `${selectedDateStr.value}T${selectedTimeStr.value}`;
  } else {
    baSetupForm.value.tanggalJadwal = "";
  }
}

// Scroll to selected element in scrollable column helper
function scrollToSelectedTimeItem(colSelector, selectedClass) {
  setTimeout(() => {
    const col = datepickerContainer.value?.querySelector(colSelector);
    const selected = col?.querySelector(selectedClass);
    if (col && selected) {
      col.scrollTop = selected.offsetTop - col.offsetTop - (col.clientHeight / 2) + (selected.clientHeight / 2);
    }
  }, 50);
}

function prevMonth() {
  if (pickerMonth.value === 0) {
    pickerMonth.value = 11;
    pickerYear.value -= 1;
  } else {
    pickerMonth.value -= 1;
  }
}

function nextMonth() {
  if (pickerMonth.value === 11) {
    pickerMonth.value = 0;
    pickerYear.value += 1;
  } else {
    pickerMonth.value += 1;
  }
}

function setToday() {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  
  selectedDateStr.value = `${y}-${m}-${d}`;
  selectedTimeStr.value = `${hh}:${mm}`;
  updateTanggalJadwal();
  
  pickerYear.value = y;
  pickerMonth.value = now.getMonth();
}

function clearDate() {
  selectedDateStr.value = "";
  selectedTimeStr.value = "09:00";
  baSetupForm.value.tanggalJadwal = "";
}

function isTodayDate(dateString) {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return dateString === `${y}-${m}-${d}`;
}

function formatIndonesianDateTime(value) {
  if (!value) return "";
  try {
    const parts = value.split("T");
    const dateParts = parts[0].split("-");
    if (dateParts.length !== 3) return value;
    
    const y = dateParts[0];
    const mIndex = parseInt(dateParts[1]) - 1;
    const d = parseInt(dateParts[2]);
    const monthName = indonesianMonths[mIndex] || dateParts[1];
    
    let timeStr = "";
    if (parts[1]) {
      timeStr = " pukul " + parts[1].substring(0, 5);
    }
    
    return `${d} ${monthName} ${y}${timeStr}`;
  } catch {
    return value;
  }
}

function handleClickOutside(event) {
  if (datepickerContainer.value && !datepickerContainer.value.contains(event.target)) {
    showDatePicker.value = false;
  }
}

// Synchronize selectedReg with URL query parameter ?id=...
watch(
  [() => route.query.id, registrations],
  ([newId, list]) => {
    if (newId && list.length > 0) {
      const reg = list.find((r) => r.id.toString() === newId.toString());
      if (reg) {
        selectedReg.value = reg;
        showDatePicker.value = false;
        jadwalDateTime.value = reg.tanggalJadwal
          ? reg.tanggalJadwal.substring(0, 16)
          : "";
      } else {
        selectedReg.value = null;
        currentSubView.value = "DETAIL";
      }
    } else if (!newId) {
      selectedReg.value = null;
      currentSubView.value = "DETAIL";
    }
  },
  { immediate: true }
);


const tabs = [
  { value: "MASUK", label: "Masuk", colorClass: "yellow" },
  { value: "DIPROSES", label: "Diproses", colorClass: "blue" },
  { value: "DIJADWALKAN", label: "Dijadwalkan", colorClass: "teal" },
  { value: "SELESAI", label: "Selesai", colorClass: "green" },
  { value: "DIBATALKAN", label: "Dibatalkan", colorClass: "red" },
];

const currentTabLabel = computed(() => {
  const current = tabs.find((t) => t.value === activeTab.value);
  return current ? current.label : "Masuk";
});

const filteredRegistrations = computed(() => {
  // Client-side status filtering based on activeTab
  return registrations.value.filter((r) => r.status === activeTab.value);
});

const footerStatusText = computed(() => {
  switch (activeTab.value) {
    case "MASUK":
      return "masuk";
    case "DIPROSES":
      return "diproses";
    case "DIJADWALKAN":
      return "dijadwalkan";
    case "SELESAI":
      return "selesai";
    case "DIBATALKAN":
      return "dibatalkan";
    default:
      return "registrasi";
  }
});

function getTabCount(statusValue) {
  return registrations.value.filter((r) => r.status === statusValue).length;
}

async function fetchList() {
  loading.value = true;
  try {
    const params = {
      search: searchQuery.value,
    };
    const res = await getRegistrations(params);
    registrations.value = res;
  } catch (e) {
    toast.error("Gagal mengambil data registrasi: " + e.message);
  } finally {
    loading.value = false;
  }
}

function viewDetail(reg) {
  selectedReg.value = reg;
  currentSubView.value = "DETAIL";
  showDatePicker.value = false;
  jadwalDateTime.value = reg.tanggalJadwal
    ? reg.tanggalJadwal.substring(0, 16)
    : "";
  router.push({ query: { id: reg.id } });
}

function closeDetail() {
  selectedReg.value = null;
  currentSubView.value = "DETAIL";
  showDatePicker.value = false;
  jadwalDateTime.value = "";
  router.push({ query: {} });
}


function isPdf(url) {
  if (!url) return false;
  return url.startsWith("data:application/pdf") || url.toLowerCase().endsWith(".pdf");
}

function openFile(dataUrl) {
  if (!dataUrl) return;
  if (dataUrl.startsWith("http://") || dataUrl.startsWith("https://")) {
    window.open(dataUrl, "_blank");
    return;
  }
  try {
    const parts = dataUrl.split(",");
    const mime = parts[0].match(/:(.*?);/)[1];
    const bstr = atob(parts[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    const blob = new Blob([u8arr], { type: mime });
    const blobUrl = URL.createObjectURL(blob);
    window.open(blobUrl, "_blank");
  } catch (e) {
    const newTab = window.open();
    newTab.document.write(`<iframe src="${dataUrl}" frameborder="0" style="border:0; top:0px; left:0px; bottom:0px; right:0px; width:100%; height:100%;" allowfullscreen></iframe>`);
  }
}

function getFullName(reg) {
  if (!reg) return "-";
  return [reg.firstName, reg.lastName].filter(Boolean).join(" ") || "-";
}

function getPackageLabel(reg) {
  if (!reg?.pkg) return "-";
  return [reg.pkg.name, reg.pkg.speed ? `${reg.pkg.speed} Mbps` : ""]
    .filter(Boolean)
    .join(" ");
}

function getFullAddress(reg) {
  if (!reg) return "-";
  return [
    reg.alamatDetail,
    `RT ${reg.rt || "-"} / RW ${reg.rw || "-"}`,
    reg.kelurahan,
    reg.kecamatan,
    reg.kota,
    reg.provinsi,
    reg.kodePos,
  ]
    .filter(Boolean)
    .join(", ");
}

function getBaPriceNumber() {
  const raw = String(baSetupForm.value.harga || "");
  const digits = raw.replace(/\D/g, "");
  return digits ? Number(digits) : 0;
}

function toDateTimeLocal(value) {
  if (!value) return "";
  return value.replace(" ", "T").substring(0, 16);
}

function normalizeHargaInput() {
  const amount = getBaPriceNumber();
  baSetupForm.value.harga = amount ? formatRp(amount) : "";
}

const baDeviceRows = computed(() => {
  if (!baData.value) return [];
  const rows = [
    baData.value.qtyOnt,
    baData.value.qtyRouter,
    baData.value.qtyKabel,
    baData.value.qtyRoset,
    baData.value.qtyAksesoris,
  ];
  return rows.filter((qty) => Number(qty) > 0);
});

const showBaEditButton = computed(() => {
  return selectedReg.value?.status === "SELESAI" && !isEditingBa.value;
});

const baSetupButtonLabel = computed(() => {
  return selectedReg.value?.status === "DIPROSES" ? "Simpan & Lanjut" : "Simpan";
});

function openSetBaAktivasi() {
  currentSubView.value = "SET_BA";
  isEditingBa.value = selectedReg.value.status !== "SELESAI";
  
  // Fill the form state
  baSetupForm.value = {
    custId: selectedReg.value.custId || "",
    tanggalJadwal: toDateTimeLocal(selectedReg.value.tanggalJadwal),
    harga: formatRp(selectedReg.value.harga || (selectedReg.value.pkg ? selectedReg.value.pkg.price : 0)),
    noteRequest: selectedReg.value.noteRequest || "",
    noteTeknisi: selectedReg.value.noteTeknisi || "",
  };

  // Fetch BA Aktivasi details if they exist
  getBaAktivasi(selectedReg.value.id)
    .then((res) => {
      baData.value = res;
    })
    .catch(() => {
      baData.value = null;
    });
}

async function viewOfficialBa() {
  try {
    const ba = await getBaAktivasi(selectedReg.value.id);
    if (ba && ba.id) {
      baData.value = ba;
      currentSubView.value = "OFFICIAL_BA";
    } else {
      toast.error("Berita Acara (BA) Aktivasi belum diisi untuk registrasi ini.");
    }
  } catch (e) {
    toast.error("Gagal memeriksa BA Aktivasi: " + e.message);
  }
}

function goBackToDetail() {
  currentSubView.value = "DETAIL";
}

async function saveBaSetup() {
  if (!baSetupForm.value.custId || !baSetupForm.value.custId.trim()) {
    toast.error("Customer ID wajib diisi!");
    return;
  }
  if (!baSetupForm.value.tanggalJadwal || !baSetupForm.value.tanggalJadwal.trim()) {
    toast.error("Jadwal Pemasangan wajib diisi!");
    return;
  }
  const hargaNumber = getBaPriceNumber();
  if (hargaNumber <= 0) {
    toast.error("Harga wajib diisi dan lebih besar dari 0!");
    return;
  }

  statusLoading.value = true;
  try {
    const shouldContinueToSchedule = selectedReg.value.status === "DIPROSES";
    const payload = {
      custId: baSetupForm.value.custId,
      tanggalJadwal: baSetupForm.value.tanggalJadwal,
      harga: hargaNumber,
      noteRequest: baSetupForm.value.noteRequest,
      noteTeknisi: baSetupForm.value.noteTeknisi,
    };
    let updated = await updateBaSetup(selectedReg.value.id, payload);

    if (shouldContinueToSchedule) {
      updated = await updateRegistrationStatus(
        selectedReg.value.id,
        "DIJADWALKAN",
        baSetupForm.value.tanggalJadwal,
      );
    }
    
    // Update local state
    selectedReg.value = updated;

    // Refresh list in background
    getRegistrations({ search: searchQuery.value }).then((res) => {
      registrations.value = res;
    });

    if (shouldContinueToSchedule) {
      toast.success("Pengaturan BA Aktivasi disimpan dan diteruskan ke jadwal pemasangan.");
      currentSubView.value = "DETAIL";
      activeTab.value = "DIJADWALKAN";
    } else {
      toast.success("Pengaturan BA Aktivasi berhasil disimpan!");
    }
    isEditingBa.value = selectedReg.value.status !== "SELESAI";
  } catch (e) {
    toast.error("Gagal menyimpan pengaturan BA: " + e.message);
  } finally {
    statusLoading.value = false;
  }
}

async function exportToExcel() {
  try {
    toast.info("Sedang mengunduh laporan Excel...");
    const token = localStorage.getItem("vnet_token");
    const response = await fetch("/api/registrations/export", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    if (!response.ok) throw new Error("Gagal mengunduh file Excel.");
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const formattedDate = `${yyyy}-${mm}-${dd}`;
    
    a.download = `registrasi_selesai_${formattedDate}.xlsx`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    toast.success("Excel berhasil diunduh!");
  } catch (e) {
    toast.error("Error saat export: " + e.message);
  }
}

async function fetchPackages() {
  try {
    const res = await getPackages();
    packagesList.value = res;
  } catch (e) {
    console.error("Gagal memuat paket: " + e.message);
  }
}

function openEditRegModal() {
  if (!selectedReg.value) return;
  const reg = selectedReg.value;
  editRegForm.value = {
    id: reg.id,
    firstName: reg.firstName || "",
    lastName: reg.lastName || "",
    email: reg.email || "",
    jenisIdentitas: reg.jenisIdentitas || "",
    nomorIdentitas: reg.nomorIdentitas || "",
    nomorSelulerUtama: reg.nomorSelulerUtama || "",
    nomorCadangan: reg.nomorCadangan || "",
    alamatDetail: reg.alamatDetail || "",
    rt: reg.rt || "",
    rw: reg.rw || "",
    kelurahan: reg.kelurahan || "",
    kecamatan: reg.kecamatan || "",
    kota: reg.kota || "",
    provinsi: reg.provinsi || "",
    kodePos: reg.kodePos || "",
    pkg: reg.pkg ? { id: reg.pkg.id } : { id: "" },
    koordinat: reg.koordinat || "",
    afiliator: reg.afiliator || ""
  };
  showEditRegModal.value = true;
}

async function saveRegEdit() {
  if (!editRegForm.value.firstName || !editRegForm.value.lastName || !editRegForm.value.email) {
    toast.error("Nama dan Email wajib diisi!");
    return;
  }
  
  statusLoading.value = true;
  try {
    const updated = await updateRegistration(editRegForm.value.id, editRegForm.value);
    
    // Update local state
    selectedReg.value = updated;
    
    // Also update in registrations list
    const idx = registrations.value.findIndex(r => r.id === updated.id);
    if (idx !== -1) {
      registrations.value[idx] = updated;
    }
    
    showEditRegModal.value = false;
    toast.success("Data customer berhasil diperbarui!");
  } catch (e) {
    toast.error("Gagal memperbarui data: " + e.message);
  } finally {
    statusLoading.value = false;
  }
}




async function changeStatus(id, newStatus) {
  statusLoading.value = true;
  try {
    const updated = await updateRegistrationStatus(id, newStatus);
    toast.success(`Status berhasil diubah ke ${getStatusLabel(newStatus)}`);

    // Update local list
    const index = registrations.value.findIndex((r) => r.id === id);
    if (index !== -1) {
      registrations.value[index] = updated;
    }

    // Update detail overlay if currently open
    if (selectedReg.value && selectedReg.value.id === id) {
      selectedReg.value = updated;
    }
    closeDetail();

    // Switch to target status tab to view updated item
    activeTab.value = newStatus;
  } catch (e) {
    toast.error("Gagal mengubah status: " + e.message);
  } finally {
    statusLoading.value = false;
  }
}

async function saveJadwalAndChangeStatus(id) {
  if (!jadwalDateTime.value) {
    toast.error("Silakan tentukan tanggal & waktu pemasangan!");
    return;
  }
  statusLoading.value = true;
  try {
    const updated = await updateRegistrationStatus(
      id,
      "DIJADWALKAN",
      jadwalDateTime.value,
    );
    toast.success("Jadwal pemasangan berhasil disimpan.");

    const index = registrations.value.findIndex((r) => r.id === id);
    if (index !== -1) {
      registrations.value[index] = updated;
    }

    if (selectedReg.value && selectedReg.value.id === id) {
      selectedReg.value = updated;
    }

    showDatePicker.value = false;
    jadwalDateTime.value = "";
    closeDetail();

    activeTab.value = "DIJADWALKAN";
  } catch (e) {
    toast.error("Gagal menyimpan jadwal: " + e.message);
  } finally {
    statusLoading.value = false;
  }
}

async function confirmDelete(id) {
  if (confirm("Apakah Anda yakin ingin menghapus data registrasi ini?")) {
    statusLoading.value = true;
    try {
      await deleteRegistration(id);
      toast.success("Registrasi berhasil dihapus.");
      registrations.value = registrations.value.filter((r) => r.id !== id);
      closeDetail();
    } catch (e) {
      toast.error("Gagal menghapus data: " + e.message);
    } finally {
      statusLoading.value = false;
    }
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
    case "MASUK":
      return "Masuk";
    case "DIPROSES":
      return "Diproses";
    case "DIJADWALKAN":
      return "Dijadwalkan";
    case "SELESAI":
      return "Selesai";
    case "DIBATALKAN":
      return "Dibatalkan";
    default:
      return status;
  }
}

function getStatusBadgeClass(status) {
  switch (status) {
    case "MASUK":
      return "badge--yellow";
    case "DIPROSES":
      return "badge--blue";
    case "DIJADWALKAN":
      return "badge--teal";
    case "SELESAI":
      return "badge--green";
    case "DIBATALKAN":
      return "badge--red";
    default:
      return "";
  }
}

function getInitials(reg) {
  if (!reg) return "";
  const f = reg.firstName ? reg.firstName[0] : "";
  const l = reg.lastName ? reg.lastName[0] : "";
  return (f + l).toUpperCase() || "C";
}

function formatRp(value) {
  if (!value) return "Rp 0";
  return "Rp " + Math.round(value).toLocaleString("id-ID");
}

onMounted(() => {
  fetchList();
  fetchPackages();
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<style scoped>
.registration-dashboard {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header & Meta */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--navy);
  margin-bottom: 4px;
}

.page-meta {
  font-size: 13px;
  color: var(--text-3);
}

.detail-view-header,
.ba-page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 24px;
}

.detail-view-header .detail-page-title-row {
  display: block;
}

.detail-meta-legacy {
  display: none;
}

.detail-meta-live {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-inline-back,
.ba-inline-back {
  margin-top: 16px;
}

.detail-breadcrumbs,
.ba-breadcrumbs--top {
  justify-content: flex-end;
  flex-wrap: wrap;
}

.ba-breadcrumbs--top {
  margin-bottom: 28px;
}

.breadcrumb-link {
  border: 0;
  background: transparent;
  color: var(--navy);
  cursor: pointer;
  font: inherit;
  padding: 0;
}

.breadcrumb-link:hover {
  text-decoration: underline;
}

.status-lowercase {
  text-transform: lowercase;
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

/* Status Tabs (Image 2 style) */
.status-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
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

/* Color tabs aligned with Image 2 */
.tab-btn--yellow.tab-btn--active {
  background: #f59e0b;
  border-color: #d97706;
  color: white;
}

.tab-btn--blue.tab-btn--active {
  background: #2563eb;
  border-color: #1d4ed8;
  color: white;
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

.tab-btn--red.tab-btn--active {
  background: #ef4444;
  border-color: #dc2626;
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

.data-table tr:hover td {
  background: rgba(0, 0, 0, 0.005);
}

.customer-info-cell,
.contact-info-cell {
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

.contact-main {
  font-weight: 600;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 99px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.badge--yellow {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}
.badge--blue {
  background: rgba(37, 99, 235, 0.1);
  color: #1d4ed8;
}
.badge--teal {
  background: rgba(13, 148, 136, 0.1);
  color: #0f766e;
}
.badge--green {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}
.badge--red {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}
.badge--teal-light {
  background: rgba(13, 148, 136, 0.05);
  color: #0d9488;
  border: 1px solid rgba(13, 148, 136, 0.15);
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
  font-size: 11px;
  color: var(--text-3);
}

/* ============================
   DETAIL PANEL — new design
   ============================ */
.panel-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(3px);
  z-index: 1000;
}

.detail-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: min(96vw, 900px);
  height: 100vh;
  background: var(--surface);
  box-shadow: -8px 0 40px rgba(0, 0, 0, 0.18);
  z-index: 1001;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Header */
.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--surface-2);
  flex-shrink: 0;
}
.panel-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.panel-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}
.panel-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--navy);
}
.panel-custid {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}
.panel-close {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--text-3);
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}
.panel-close:hover {
  color: var(--navy);
}

/* Meta bar (status + back btn) */
.panel-meta-bar {
  padding: 10px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  vertical-align: middle;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.data-table tr:hover td {
  background: rgba(0, 0, 0, 0.005);
}

.customer-info-cell,
.contact-info-cell {
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

.contact-main {
  font-weight: 600;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 99px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.badge--yellow {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}
.badge--blue {
  background: rgba(37, 99, 235, 0.1);
  color: #1d4ed8;
}
.badge--teal {
  background: rgba(13, 148, 136, 0.1);
  color: #0f766e;
}
.badge--green {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}
.badge--red {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}
.badge--teal-light {
  background: rgba(13, 148, 136, 0.05);
  color: #0d9488;
  border: 1px solid rgba(13, 148, 136, 0.15);
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
  font-size: 11px;
  color: var(--text-3);
}
/* BA Setup Breadcrumbs */
.ba-breadcrumbs {
  font-size: 13px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 6px;
}
.bc-sep {
  color: var(--border);
}
.breadcrumb-item.active {
  color: var(--primary);
  font-weight: 600;
}

/* Set BA Form Layout */
.ba-setup-layout {
  margin-top: 15px;
}

.ba-setup-card {
  padding: 24px;
}

.ba-customer-summary {
  margin-top: 20px;
  border-bottom: 1px solid var(--border);
  padding-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--primary);
  margin-bottom: 12px;
}

.ba-summary-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bas-row {
  display: grid;
  grid-template-columns: 180px 1fr;
  font-size: 13px;
  line-height: 1.5;
}

.bas-label {
  color: var(--text-3);
  font-weight: 500;
}

.bas-value {
  color: var(--text-1);
}

.ba-setup-inputs {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.ba-input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ba-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-2);
}

.ba-label .required {
  color: var(--red);
}

.ba-label .optional {
  color: var(--text-3);
  font-weight: 400;
}

.ba-input {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-1);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.15s;
}

.ba-input:focus {
  border-color: var(--primary);
}

.ba-input:disabled {
  background: var(--surface-2);
  color: var(--text-3);
  cursor: not-allowed;
}

.ba-textarea {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-1);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  min-height: 80px;
  resize: vertical;
  outline: none;
  transition: border-color 0.15s;
}

.ba-textarea:focus {
  border-color: var(--primary);
}

.ba-textarea:disabled {
  background: var(--surface-2);
  color: var(--text-3);
  cursor: not-allowed;
}

.ba-input-note {
  font-size: 11px;
  color: var(--text-3);
}

.btn-edit-ba {
  background: var(--primary) !important;
  color: white !important;
  padding: 10px 24px !important;
  font-weight: 700 !important;
  border-radius: 8px !important;
  border: none !important;
}

.btn-save-ba {
  background: var(--green-ok) !important;
  color: white !important;
  width: 100%;
  padding: 12px 24px !important;
  font-weight: 700 !important;
  border-radius: 8px !important;
  border: none !important;
}

/* Right Column: Tech Card */
.ba-tech-card {
  padding: 24px;
}

.ba-tech-info {
  border-bottom: 1px solid var(--border);
  padding-bottom: 15px;
}

.tech-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tech-label {
  font-size: 13px;
  color: var(--text-3);
  font-weight: 500;
}

.tech-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--navy);
}

/* Devices Table */
.ba-devices-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.ba-devices-table th {
  background: var(--surface-2);
  color: var(--text-2);
  font-weight: 600;
  text-align: left;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
}

.ba-devices-table td {
  padding: 12px;
  border-bottom: 1px solid var(--border);
  color: var(--text-1);
}

.ba-device-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--border);
}

.ba-table-empty {
  text-align: center;
  color: var(--text-3);
  padding: 30px !important;
}

/* Official BA View */
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

.btn-canvas-clear, .btn-canvas-cancel {
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
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
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
  .official-ba-document, .official-ba-document * {
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

/* ========= Full-page Detail Layout ========= */
.detail-page-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
  margin-top: 0;
}

.detail-page-title-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.detail-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-back {
  font-size: 12px !important;
  font-weight: 700 !important;
  padding: 7px 16px !important;
  border-radius: 8px !important;
  background: var(--surface-2) !important;
  border: 1px solid var(--border) !important;
  color: var(--navy) !important;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-back:hover { background: var(--border) !important; }

.btn-action--outline {
  background: none;
  border: 1px solid var(--border);
  color: var(--text-2);
}

@media (max-width: 768px) {
  .detail-page-body { grid-template-columns: 1fr; }
  .detail-panel { width: 100vw; }
}

/* Generic detail card */
.detail-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
  background: var(--surface);
}

/* Customer detail left card */
.detail-card--customer {
  background: var(--surface);
  border: 1px solid var(--border);
}
.detail-card .detail-card-header {
  padding: 16px 18px 10px;
  border-bottom: 1px solid var(--border);
}
.detail-card .detail-card-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-1);
}
.detail-card .detail-card-subtitle {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}

/* Badges row inside customer card */
.detail-badges-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 18px;
  border-bottom: 1px solid var(--border);
}
.detail-speed-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 6px;
  background: #2563eb;
  color: white;
  font-size: 11px;
  font-weight: 700;
}

/* WhatsApp button */
.wa-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 6px;
  background: #22c55e;
  color: white;
  font-size: 11px;
  font-weight: 700;
  text-decoration: none;
  transition: background 0.15s;
}
.wa-btn:hover {
  background: #16a34a;
}
.wa-icon {
  width: 13px;
  height: 13px;
}

/* Data rows table inside customer card */
.detail-data-table {
  padding: 0;
}
.ddt-row {
  display: grid;
  grid-template-columns: 130px 1fr;
  border-bottom: 1px solid var(--border);
}
.ddt-row:last-child {
  border-bottom: none;
}
.ddt-label {
  padding: 10px 18px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-3);
  background: var(--surface-2);
  border-right: 1px solid var(--border);
}
.ddt-value {
  padding: 10px 18px;
  font-size: 13px;
  color: var(--text-1);
  word-break: break-all;
}
.ddt-muted {
  color: var(--text-3);
  font-style: italic;
}

/* Photo row */
.ddt-photo-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.btn-lihat-file {
  display: inline-block;
  padding: 4px 12px;
  border: 1px solid var(--navy-mid);
  color: var(--navy-mid);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  text-decoration: none;
  background: none;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-lihat-file:hover {
  background: var(--navy-glow);
}
.ddt-photo-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--border);
}

.ddt-pdf-container {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  padding: 4px 8px;
  border-radius: 6px;
  color: #f87171;
  font-size: 11px;
  font-weight: 600;
}
.ddt-pdf-icon {
  width: 15px;
  height: 15px;
}

/* Right column */
.detail-right-col {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* Action card */
.detail-card--action {
  background: var(--surface-2);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.btn-action-full {
  width: 100%;
  padding: 11px 16px;
  border-radius: 8px;
  border: none;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition:
    opacity 0.15s,
    transform 0.12s;
  text-align: center;
}
.btn-action-full:hover:not(:disabled) {
  opacity: 0.88;
  transform: translateY(-1px);
}
.btn-action-full:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-action--blue {
  background: #3b5bdb;
  color: white;
}
.btn-action--outline-blue {
  background: transparent;
  border: 1px solid #3b5bdb;
  color: #3b5bdb;
}
.btn-action--teal {
  background: #0d9488;
  color: white;
}
.btn-action--green {
  background: #10b981;
  color: white;
}
.btn-action--red {
  background: #ef4444;
  color: white;
}
.btn-action--outline-red {
  background: none;
  border: 1px solid #ef4444;
  color: #ef4444;
}
.btn-action--disabled {
  background: #334155 !important;
  color: #94a3b8 !important;
  cursor: not-allowed !important;
  opacity: 1 !important;
}
.btn-action--navy {
  background: #0d1330;
  color: #38bdf8;
  border: 1px solid rgba(56, 189, 248, 0.2);
}

/* Address Card */
.detail-card--address {
  background: var(--surface);
  border: 1px solid var(--border);
}
.addr-row {
  display: grid;
  grid-template-columns: 130px 1fr;
  border-bottom: 1px solid var(--border);
}
.addr-row:last-child {
  border-bottom: none;
}
.addr-label {
  padding: 9px 14px;
  font-size: 12px;
  color: var(--text-3);
  font-weight: 600;
  background: var(--surface-2);
  border-right: 1px solid var(--border);
}
.addr-value {
  padding: 9px 14px;
  font-size: 12px;
  color: var(--text-1);
  word-break: break-word;
}

/* Custom Indonesian Date-Time Picker Styles */
.custom-datetime-container {
  position: relative;
  width: 100%;
  font-family: 'Inter', sans-serif;
}

.custom-datepicker-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: var(--surface-2, #f8fafc);
  border: 1px solid var(--border, #cbd5e1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  color: var(--text-1, #0f172a);
}

.custom-datepicker-trigger:hover:not(.disabled-trigger) {
  border-color: var(--primary, #3b82f6);
  background: var(--surface, #ffffff);
}

.custom-datepicker-trigger.active-trigger {
  border-color: var(--primary, #3b82f6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
  background: var(--surface, #ffffff);
}

.custom-datepicker-trigger.disabled-trigger {
  cursor: not-allowed;
  opacity: 0.65;
  background: var(--surface-3, #f1f5f9);
}

.trigger-value {
  flex-grow: 1;
}

.calendar-icon {
  width: 18px;
  height: 18px;
  color: var(--text-3, #64748b);
  margin-left: 8px;
}

.custom-datepicker-popover {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 6px;
  z-index: 10000;
  background: var(--surface, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  padding: 16px;
  width: 530px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  animation: fadeInPopover 0.15s ease-out;
}

@keyframes fadeInPopover {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.picker-panel-body {
  display: flex;
  gap: 20px;
  height: 290px;
}

/* Calendar Section */
.calendar-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.calendar-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-1, #0f172a);
}

.btn-picker-nav {
  background: transparent;
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 6px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-2, #334155);
  font-size: 12px;
  transition: all 0.2s;
}

.btn-picker-nav:hover {
  background: var(--surface-2, #f8fafc);
  border-color: var(--text-3, #94a3b8);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 6px;
}

.weekday-cell {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-3, #94a3b8);
  padding: 4px 0;
}

.calendar-days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  flex-grow: 1;
}

.day-cell {
  background: transparent;
  border: none;
  border-radius: 6px;
  height: 30px;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  color: var(--text-1, #0f172a);
}

.day-cell:hover:not(.day-selected) {
  background: var(--surface-2, #f1f5f9);
}

.day-current-month {
  font-weight: 500;
}

.day-other-month {
  color: var(--text-4, #cbd5e1);
}

.day-selected {
  background: var(--primary, #3b82f6) !important;
  color: #ffffff !important;
  font-weight: 600;
}

.day-today {
  border: 1px solid var(--primary, #3b82f6);
  color: var(--primary, #3b82f6);
  font-weight: 600;
}

.calendar-footer-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border, #f1f5f9);
}

.btn-picker-clear, .btn-picker-today {
  background: transparent;
  border: none;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.15s;
}

.btn-picker-clear {
  color: var(--danger, #ef4444);
}

.btn-picker-clear:hover {
  background: rgba(239, 68, 68, 0.08);
}

.btn-picker-today {
  color: var(--primary, #3b82f6);
}

.btn-picker-today:hover {
  background: rgba(59, 130, 246, 0.08);
}

/* Time Section */
.time-section {
  width: 180px;
  border-left: 1px solid var(--border, #e2e8f0);
  padding-left: 16px;
  display: flex;
  flex-direction: column;
}

.time-header {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-1, #0f172a);
  margin-bottom: 12px;
  height: 28px;
  display: flex;
  align-items: center;
}

.time-columns {
  display: flex;
  gap: 12px;
  flex-grow: 1;
  height: calc(100% - 40px);
}

.time-col-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.col-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-3, #94a3b8);
  text-align: center;
  margin-bottom: 6px;
}

.time-col {
  flex-grow: 1;
  overflow-y: auto;
  border: 1px solid var(--border, #f1f5f9);
  border-radius: 6px;
  padding: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  scrollbar-width: thin;
  scrollbar-color: var(--border) transparent;
}

.time-col::-webkit-scrollbar {
  width: 4px;
}

.time-col::-webkit-scrollbar-thumb {
  background-color: var(--border, #cbd5e1);
  border-radius: 2px;
}

.time-item {
  background: transparent;
  border: none;
  border-radius: 4px;
  padding: 6px 0;
  font-size: 13px;
  font-weight: 500;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
  color: var(--text-2, #475569);
}

.time-item:hover:not(.time-item-selected) {
  background: var(--surface-2, #f1f5f9);
  color: var(--text-1, #0f172a);
}

.time-item-selected {
  background: var(--primary, #3b82f6) !important;
  color: #ffffff !important;
  font-weight: 600;
}

/* Footer Section */
.picker-panel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border, #e2e8f0);
}

.selected-summary {
  font-size: 12px;
  color: var(--text-2, #475569);
}

.btn-picker-done {
  background: var(--primary, #3b82f6);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-picker-done:hover {
  background: var(--primary-dark, #2563eb);
}

/* Modal edit styles */
.edit-modal-overlay {
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
.edit-modal-box {
  background: white;
  width: 100%;
  max-width: 700px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.15);
  overflow: hidden;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}
.edit-modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
}
.emh-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}
.edit-modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #94a3b8;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}
.edit-modal-close:hover {
  color: #64748b;
}
.edit-modal-body {
  padding: 24px;
  overflow-y: auto;
}
.edit-modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #f8fafc;
}
.modal-form-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.modal-form-group {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}
.modal-form-group:last-child {
  margin-bottom: 0;
}
.modal-label {
  font-size: 12px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 6px;
}
.modal-input {
  padding: 10px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 13px;
  transition: border-color 0.15s;
}
.modal-input:focus {
  outline: none;
  border-color: #3b82f6;
}
.modal-textarea {
  min-height: 80px;
  resize: vertical;
}
.btn-action--orange {
  background: #f97316;
  color: white;
}
</style>
