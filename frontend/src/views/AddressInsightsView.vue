<template>
  <div class="address-insights">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Address Insights</h1>
        <p class="page-sub">
          Distribusi pelanggan per kecamatan dan kelurahan — analitik wilayah
          layanan VNet.
        </p>
      </div>
    </div>

    <!-- KPI Row -->
    <div class="kpi-row" v-if="loading">
      <div class="card kpi-card" v-for="i in 3" :key="i">
        <div class="kpi-top">
          <div class="skeleton skeleton-text--sm" style="width: 100px"></div>
          <div
            class="skeleton"
            style="width: 32px; height: 32px; border-radius: 8px"
          ></div>
        </div>
        <div
          class="skeleton skeleton-text--lg"
          style="margin-top: 12px; width: 80px"
        ></div>
        <div
          class="skeleton skeleton-text--sm"
          style="margin-top: 8px; width: 120px"
        ></div>
      </div>
    </div>
    <div class="kpi-row" v-else>
      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Total Kecamatan</span>
          <div class="kpi-icon kpi-icon--blue">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
            >
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
              <circle cx="12" cy="10" r="3" />
            </svg>
          </div>
        </div>
        <div class="kpi-value">{{ summary.totalKecamatan }}</div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--blue"
            >{{ summary.totalKelurahan }} Kelurahan / Desa</span
          >
        </div>
      </div>
      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Paket Terpopuler</span>
          <div class="kpi-icon kpi-icon--teal">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
            >
              <path
                d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"
              />
              <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
              <line x1="12" y1="22.08" x2="12" y2="12" />
            </svg>
          </div>
        </div>
        <div class="kpi-value" style="color: var(--teal)">
          {{ mostPopularPackage.cleanName }}
        </div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--teal"
            >{{ mostPopularPackage.count }} Pelanggan ({{
              mostPopularPackage.pct
            }}%)</span
          >
        </div>
      </div>

      <div class="card kpi-card kpi-card--dark">
        <div class="kpi-top">
          <span class="kpi-label" style="color: rgba(255, 255, 255, 0.55)"
            >Kecamatan Terbesar</span
          >
          <span class="badge badge--gold">Dense</span>
        </div>
        <div class="top-area-name">{{ topKecamatan?.name || "—" }}</div>
        <div class="success-rate">
          {{ topKecamatan?.customers || 0 }} pelanggan ·
          {{ topKecamatan?.kelurahanCount || 0 }} kelurahan
        </div>
      </div>
    </div>

    <!-- Tab switcher -->
    <div v-if="loading" class="chart-section" style="margin-top: 24px">
      <div class="card chart-card">
        <div
          class="skeleton skeleton-text--lg"
          style="width: 200px; margin-bottom: 24px"
        ></div>
        <div style="display: flex; gap: 40px; align-items: center">
          <div
            class="skeleton"
            style="width: 280px; height: 280px; border-radius: 50%"
          ></div>
          <div
            style="flex: 1; display: flex; flex-direction: column; gap: 16px"
          >
            <div
              v-for="i in 5"
              :key="i"
              class="skeleton"
              style="height: 32px; width: 100%; border-radius: 6px"
            ></div>
          </div>
        </div>
      </div>
    </div>
    <template v-else-if="areas.length > 0">
      <div class="tab-bar">
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'kecamatan' }"
          @click="activeTab = 'kecamatan'"
        >
          Per Kecamatan
        </button>
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'kelurahan' }"
          @click="activeTab = 'kelurahan'"
        >
          Per Kelurahan / Desa
        </button>
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'table' }"
          @click="activeTab = 'table'"
        >
          Tabel Detail
        </button>
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'paket' }"
          @click="activeTab = 'paket'"
        >
          Paket per Wilayah
        </button>
      </div>

      <!-- KECAMATAN VIEW -->
      <div v-if="activeTab === 'kecamatan'" class="chart-section">
        <div class="card chart-card">
          <div class="section-hdr">
            <div>
              <div class="chart-title">Distribusi Pelanggan per Kecamatan</div>
              <div class="chart-sub">
                Proporsi pelanggan aktif per wilayah kecamatan
              </div>
            </div>
            <span class="badge badge--navy"
              >{{ kecamatanGroups.length }} kecamatan</span
            >
          </div>
          <div class="chart-layout">
            <div class="donut-wrap">
              <canvas ref="kecamatanCanvas" width="280" height="280"></canvas>
              <div class="donut-center">
                <div class="donut-num">{{ summary.totalCustomers }}</div>
                <div class="donut-label">Total</div>
              </div>
            </div>
            <div class="legend-list">
              <div
                class="legend-item"
                v-for="(item, i) in kecamatanGroups"
                :key="item.name"
                @click="selectKecamatan(item)"
                :class="{
                  'legend-item--active': selectedKecamatan?.name === item.name,
                }"
              >
                <div
                  class="legend-dot"
                  :style="{ background: PIE_COLORS[i % PIE_COLORS.length] }"
                ></div>
                <div class="legend-info">
                  <div class="legend-name">{{ item.name }}</div>
                  <div class="legend-meta">
                    {{ item.customers }} pelanggan ·
                    {{ item.kelurahanCount }} kelurahan
                  </div>
                </div>
                <div class="legend-pct">{{ item.pct }}%</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Selected kecamatan detail -->
        <div class="card detail-side" v-if="selectedKecamatan">
          <div class="section-hdr">
            <div>
              <div class="chart-title">{{ selectedKecamatan.name }}</div>
              <div class="chart-sub">Rincian per kelurahan</div>
            </div>
            <span class="badge" :class="riskBadge(selectedKecamatan.risk)">{{
              riskLabel(selectedKecamatan.risk)
            }}</span>
          </div>
          <div class="detail-metrics">
            <div class="detail-metric">
              <span>{{ selectedKecamatan.customers }}</span
              ><small>Pelanggan</small>
            </div>
            <div class="detail-metric">
              <span>{{ selectedKecamatan.active }}</span
              ><small>Aktif</small>
            </div>
            <div class="detail-metric">
              <span style="color: var(--gold)">{{
                selectedKecamatan.isolir
              }}</span
              ><small>Isolir</small>
            </div>
          </div>
          <div class="kelurahan-list">
            <div
              class="kelurahan-item"
              v-for="kel in selectedKecamatan.kelurahan"
              :key="kel.name"
            >
              <div class="kel-info">
                <span class="kel-name">{{ kel.name }}</span>
                <span class="kel-count">{{ kel.customers }} pelanggan</span>
              </div>
              <div class="kel-bar-wrap">
                <div
                  class="kel-bar"
                  :style="{
                    width:
                      Math.max(
                        6,
                        Math.round(
                          (kel.customers / selectedKecamatan.customers) * 100,
                        ),
                      ) + '%',
                    background: kel.color,
                  }"
                ></div>
              </div>
              <span class="kel-pct"
                >{{
                  Math.round(
                    (kel.customers / selectedKecamatan.customers) * 100,
                  )
                }}%</span
              >
            </div>
          </div>
          <div class="agent-block">
            <span class="agent-label">Agent utama</span>
            <span class="agent-name">{{ selectedKecamatan.topAgent }}</span>
          </div>
        </div>
      </div>

      <!-- KELURAHAN VIEW -->
      <div v-if="activeTab === 'kelurahan'" class="chart-section">
        <div class="card chart-card">
          <div class="section-hdr">
            <div>
              <div class="chart-title">
                Distribusi Pelanggan per Kelurahan / Desa
              </div>
              <div class="chart-sub">
                Proporsi pelanggan per kelurahan dari semua wilayah
              </div>
            </div>
            <div class="filter-select">
              <select v-model="kecamatanFilter">
                <option value="">Semua Kecamatan</option>
                <option
                  v-for="k in kecamatanGroups"
                  :key="k.name"
                  :value="k.name"
                >
                  {{ k.name }}
                </option>
              </select>
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </div>
          </div>
          <div class="chart-layout">
            <div class="donut-wrap">
              <canvas ref="kelurahanCanvas" width="280" height="280"></canvas>
              <div class="donut-center">
                <div class="donut-num">
                  {{ filteredKelurahanGroups.length }}
                </div>
                <div class="donut-label">Kelurahan</div>
              </div>
            </div>
            <div class="legend-list legend-list--scroll">
              <div
                class="legend-item"
                v-for="(item, i) in filteredKelurahanGroups"
                :key="item.name"
              >
                <div
                  class="legend-dot"
                  :style="{ background: PIE_COLORS[i % PIE_COLORS.length] }"
                ></div>
                <div class="legend-info">
                  <div class="legend-name">{{ item.name }}</div>
                  <div class="legend-meta">
                    {{ item.kecamatan }} · {{ item.customers }} pelanggan
                  </div>
                </div>
                <div class="legend-pct">{{ item.pct }}%</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TABLE VIEW -->
      <div v-if="activeTab === 'table'" class="card section-card">
        <div class="section-hdr">
          <div>
            <div class="chart-title">Tabel Detail Alamat</div>
            <div class="chart-sub">
              Data operasional untuk perencanaan dan review layanan
            </div>
          </div>
          <div class="search-field">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            <input
              v-model="tableSearch"
              placeholder="Cari alamat, kecamatan, kelurahan..."
            />
          </div>
        </div>
        <div class="table-wrap">
          <table class="address-table">
            <thead>
              <tr>
                <th class="th-sort" @click="toggleAddressSort('street')">
                  Alamat
                  <span class="sort-icon">
                    <svg
                      v-if="tableSortKey === 'street'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                      stroke-linecap="round"
                    >
                      <polyline
                        :points="
                          tableSortDir === 'asc'
                            ? '18 15 12 9 6 15'
                            : '6 9 12 15 18 9'
                        "
                      />
                    </svg>
                    <svg
                      v-else
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      opacity="0.35"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <polyline points="19 12 12 19 5 12" />
                    </svg>
                  </span>
                </th>
                <th class="th-sort" @click="toggleAddressSort('district')">
                  Kecamatan
                  <span class="sort-icon">
                    <svg
                      v-if="tableSortKey === 'district'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                      stroke-linecap="round"
                    >
                      <polyline
                        :points="
                          tableSortDir === 'asc'
                            ? '18 15 12 9 6 15'
                            : '6 9 12 15 18 9'
                        "
                      />
                    </svg>
                    <svg
                      v-else
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      opacity="0.35"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <polyline points="19 12 12 19 5 12" />
                    </svg>
                  </span>
                </th>
                <th class="th-sort" @click="toggleAddressSort('village')">
                  Kelurahan
                  <span class="sort-icon">
                    <svg
                      v-if="tableSortKey === 'village'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                      stroke-linecap="round"
                    >
                      <polyline
                        :points="
                          tableSortDir === 'asc'
                            ? '18 15 12 9 6 15'
                            : '6 9 12 15 18 9'
                        "
                      />
                    </svg>
                    <svg
                      v-else
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      opacity="0.35"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <polyline points="19 12 12 19 5 12" />
                    </svg>
                  </span>
                </th>
                <th class="th-sort" @click="toggleAddressSort('customers')">
                  Pelanggan
                  <span class="sort-icon">
                    <svg
                      v-if="tableSortKey === 'customers'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                      stroke-linecap="round"
                    >
                      <polyline
                        :points="
                          tableSortDir === 'asc'
                            ? '18 15 12 9 6 15'
                            : '6 9 12 15 18 9'
                        "
                      />
                    </svg>
                    <svg
                      v-else
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      opacity="0.35"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <polyline points="19 12 12 19 5 12" />
                    </svg>
                  </span>
                </th>
                <th class="th-sort" @click="toggleAddressSort('active')">
                  Aktif
                  <span class="sort-icon">
                    <svg
                      v-if="tableSortKey === 'active'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                      stroke-linecap="round"
                    >
                      <polyline
                        :points="
                          tableSortDir === 'asc'
                            ? '18 15 12 9 6 15'
                            : '6 9 12 15 18 9'
                        "
                      />
                    </svg>
                    <svg
                      v-else
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      opacity="0.35"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <polyline points="19 12 12 19 5 12" />
                    </svg>
                  </span>
                </th>
                <th class="th-sort" @click="toggleAddressSort('Mbps')">
                  Mbps<span class="sort-icon"
                    ><svg
                      v-if="tableSortKey === 'Mbps'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                      stroke-linecap="round"
                    >
                      <polyline
                        :points="
                          tableSortDir === 'asc'
                            ? '18 15 12 9 6 15'
                            : '6 9 12 15 18 9'
                        "
                      /></svg
                    ><svg
                      v-else
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      opacity="0.35"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <polyline points="19 12 12 19 5 12" /></svg
                  ></span>
                </th>
                <th class="th-sort" @click="toggleAddressSort('isolirRate')">
                  Isolir %<span class="sort-icon"
                    ><svg
                      v-if="tableSortKey === 'isolirRate'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                      stroke-linecap="round"
                    >
                      <polyline
                        :points="
                          tableSortDir === 'asc'
                            ? '18 15 12 9 6 15'
                            : '6 9 12 15 18 9'
                        "
                      /></svg
                    ><svg
                      v-else
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      opacity="0.35"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <polyline points="19 12 12 19 5 12" /></svg
                  ></span>
                </th>
                <th>Paket Dominan</th>
                <th>Agent</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="area in paginatedTableAreas" :key="area.id">
                <td>
                  <div class="td-address">
                    <span>{{ area.street }}</span>
                    <small>{{ area.city }}</small>
                  </div>
                </td>
                <td>{{ area.district }}</td>
                <td>{{ area.village }}</td>
                <td class="td-num">{{ area.customers }}</td>
                <td class="td-num" style="color: var(--green-ok)">
                  {{ area.active }}
                </td>
                <td class="td-num">{{ area.avgMbps }}</td>
                <td>
                  <span
                    class="growth-tag"
                    :class="
                      area.isolirRate > 15
                        ? 'growth-tag--down'
                        : 'growth-tag--up'
                    "
                    >{{ area.isolirRate }}%</span
                  >
                </td>
                <td>
                  <span class="type-tag">{{ area.dominantPackage }}</span>
                </td>
                <td>{{ area.topAgent }}</td>
              </tr>
              <tr v-if="filteredTableAreas.length === 0">
                <td colspan="9" class="td-empty">Tidak ada data yang cocok</td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- Table Footer with Pagination -->
        <div class="table-footer">
          <div class="footer-left">
            <span class="table-count"
              >Showing
              <strong>{{ tableRangeStart }}-{{ tableRangeEnd }}</strong> of
              <strong>{{ filteredTableAreas.length }}</strong> entries</span
            >
            <select
              class="rows-select"
              v-model="tableItemsPerPage"
              @change="tablePage = 1"
            >
              <option :value="10">10 / page</option>
              <option :value="25">25 / page</option>
              <option :value="50">50 / page</option>
            </select>
          </div>
          <div class="pagination">
            <button
              class="page-btn"
              :disabled="tablePage === 1"
              @click="tablePage--"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              >
                <polyline points="15 18 9 12 15 6" />
              </svg>
            </button>
            <button
              v-for="(p, idx) in tableVisiblePages"
              :key="idx"
              class="page-btn page-num"
              :class="{
                'page-btn--active': tablePage === p,
                'page-btn--dots': p === '...',
              }"
              :disabled="p === '...'"
              @click="p !== '...' ? (tablePage = p) : null"
            >
              {{ p }}
            </button>
            <button
              class="page-btn"
              :disabled="tablePage === tableTotalPages"
              @click="tablePage++"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              >
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- PAKET PER WILAYAH VIEW -->
      <div v-if="activeTab === 'paket'" class="chart-section">
        <div class="card chart-card" style="padding: 24px">
          <div class="section-hdr">
            <div>
              <div class="chart-title">Distribusi Paket per Kecamatan</div>
              <div class="chart-sub">
                Paket paling dominan di setiap wilayah kecamatan
              </div>
            </div>
            <span class="badge badge--navy"
              >{{ kecamatanGroups.length }} kecamatan</span
            >
          </div>

          <!-- Package legend -->
          <div class="pkg-legend-row">
            <div
              class="pkg-legend-item"
              v-for="(pkg, i) in uniquePackages"
              :key="pkg"
            >
              <span
                class="pkg-legend-dot"
                :style="{ background: PIE_COLORS[i % PIE_COLORS.length] }"
              ></span>
              <span class="pkg-legend-label">{{ pkg }}</span>
            </div>
          </div>

          <!-- Bars per kecamatan -->
          <div class="pkg-rows">
            <div
              class="pkg-row"
              v-for="(item, i) in kecamatanGroupsWithPackages"
              :key="item.name"
            >
              <div class="pkg-row-label">{{ item.name }}</div>
              <div class="pkg-bar-stacked">
                <div
                  class="pkg-bar-seg"
                  v-for="(pkg, pi) in item.packageBreakdown"
                  :key="pkg.name"
                  :style="{
                    width: pkg.pct + '%',
                    background:
                      PIE_COLORS[
                        uniquePackages.indexOf(pkg.name) % PIE_COLORS.length
                      ],
                  }"
                  :title="
                    pkg.name +
                    ': ' +
                    pkg.count +
                    ' pelanggan (' +
                    pkg.pct +
                    '%)'
                  "
                ></div>
              </div>
              <div class="pkg-row-total">{{ item.customers }} pelanggan</div>
            </div>
          </div>

          <!-- Dominant Package Summary Cards -->
          <div class="pkg-summary">
            <div class="pkg-sum-title">Top Paket Jaringan</div>
            <div class="pkg-sum-grid">
              <div
                class="pkg-sum-card"
                v-for="(pkg, i) in topPackagesSummary"
                :key="pkg.name"
              >
                <div class="pkg-sum-rank">#{{ i + 1 }}</div>
                <div class="pkg-sum-name">{{ pkg.name }}</div>
                <div class="pkg-sum-count">{{ pkg.count }} pelanggan</div>
                <div class="pkg-sum-pct">{{ pkg.pct }}% jaringan</div>
                <div class="pkg-sum-bar">
                  <div
                    class="pkg-sum-bar-fill"
                    :style="{
                      width: pkg.pct + '%',
                      background: PIE_COLORS[i % PIE_COLORS.length],
                    }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty State when no address data -->
    <div v-if="areas.length === 0" class="address-empty-state">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
      >
        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
        <circle cx="12" cy="10" r="3" />
      </svg>
      <h3>Belum Ada Data Alamat</h3>
      <p>
        Data address insights akan otomatis tampil setelah pelanggan berhasil
        diimport melalui Data Import atau didaftarkan secara manual.
      </p>
      <router-link to="/data-import" class="empty-cta-btn">
        Import Data Sekarang
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch, nextTick } from "vue";
import { getAddressInsights } from "@/services/api";

// Helper functions to normalize data presentation
function cleanKecamatan(name) {
  if (!name || name.trim() === "" || name.trim() === "-") return "Lainnya";
  return name
    .trim()
    .toLowerCase()
    .split(/\s+/)
    .map((w) => w.charAt(0).toUpperCase() + w.substring(1))
    .join(" ");
}

function cleanKelurahan(name) {
  if (!name || name.trim() === "" || name.trim() === "-") return "Lainnya";
  return name
    .trim()
    .toLowerCase()
    .split(/\s+/)
    .map((w) => w.charAt(0).toUpperCase() + w.substring(1))
    .join(" ");
}

function normalizePackageName(name) {
  if (!name) return "Lainnya";
  const lower = name.toLowerCase().trim();
  if (
    lower.includes("complimentary") ||
    lower.includes("free") ||
    lower.includes("staff")
  ) {
    return "Complimentary";
  }
  const match = name.match(/(\d+(?:\.\d+)?)\s*(?:mbps|mb|m)/i);
  if (match) {
    const speed = parseFloat(match[1]);
    return `${speed} Mbps`;
  }
  return name
    .trim()
    .split(/\s+/)
    .map((w) => w.charAt(0).toUpperCase() + w.substring(1))
    .join(" ");
}

// ── Chart.js lazy import ───────────────────────────────────────────────────
// vue-chartjs is available via node_modules; we drive Chart.js directly
// to maintain full control over chart lifecycle and avoid memory leaks.
let Chart = null;

const loading = ref(true);
const isDark = ref(document.documentElement.classList.contains("dark"));

const PIE_COLORS = computed(() => {
  return isDark.value
    ? [
        "#38BDF8",
        "#06B6D4",
        "#8B5CF6",
        "#F5A623",
        "#E91E8C",
        "#27AE60",
        "#6B9EF5",
        "#E74C3C",
        "#3498DB",
        "#F39C12",
        "#9B59B6",
        "#1ABC9C",
        "#E67E22",
        "#2ECC71",
        "#E84393",
      ]
    : [
        "#1A2170",
        "#00B4A6",
        "#8B5CF6",
        "#F5A623",
        "#E91E8C",
        "#27AE60",
        "#6B9EF5",
        "#E74C3C",
        "#3498DB",
        "#F39C12",
        "#9B59B6",
        "#1ABC9C",
        "#E67E22",
        "#2ECC71",
        "#E84393",
      ];
});

// ── State ──────────────────────────────────────────────────────────────────
const activeTab = ref("kecamatan");
const areas = ref([]);
const selectedKecamatan = ref({});
const kecamatanFilter = ref("");
const tableSearch = ref("");
const tablePage = ref(1);
const tableItemsPerPage = ref(10);
const tableSortKey = ref("");
const tableSortDir = ref("asc");

function toggleAddressSort(key) {
  if (tableSortKey.value === key) {
    tableSortDir.value = tableSortDir.value === "asc" ? "desc" : "asc";
  } else {
    tableSortKey.value = key;
    tableSortDir.value = "asc";
  }
  tablePage.value = 1;
}

// Chart canvas refs
const kecamatanCanvas = ref(null);
const kelurahanCanvas = ref(null);
let kecamatanChart = null;
let kelurahanChart = null;

// ── Computed: group by kecamatan ───────────────────────────────────────────
const kecamatanGroups = computed(() => {
  const map = new Map();
  for (const area of areas.value) {
    const key = area.district || "Tidak Diketahui";
    if (!map.has(key)) {
      map.set(key, {
        name: key,
        customers: 0,
        active: 0,
        isolir: 0,
        totalBandwidth: 0,
        kelurahan: new Map(),
        topAgentMap: new Map(),
        risk: "healthy",
      });
    }
    const g = map.get(key);
    g.customers += area.customers;
    g.active += area.active;
    g.isolir += area.isolir;
    g.totalBandwidth += area.totalBandwidth;

    // track kelurahan
    const kel = area.village || "Tidak Diketahui";
    if (!g.kelurahan.has(kel))
      g.kelurahan.set(kel, {
        name: kel,
        customers: 0,
        kecamatan: key,
        color: "",
      });
    g.kelurahan.get(kel).customers += area.customers;

    // track top agent
    const agent = area.topAgent || "-";
    g.topAgentMap.set(agent, (g.topAgentMap.get(agent) || 0) + area.customers);
  }

  const total = areas.value.reduce((s, a) => s + a.customers, 0) || 1;
  const result = [];
  let colorIdx = 0;
  for (const [, g] of map) {
    const isolirRate = g.customers
      ? Math.round((g.isolir / g.customers) * 100)
      : 0;
    g.risk = isolirRate >= 20 ? "risk" : isolirRate >= 10 ? "watch" : "healthy";
    g.isolirRate = isolirRate;
    g.avgMbps = g.customers ? Math.round(g.totalBandwidth / g.customers) : 0;
    g.pct = Math.round((g.customers / total) * 100);
    g.kelurahanCount = g.kelurahan.size;
    g.topAgent =
      [...g.topAgentMap.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? "-";
    // convert kelurahan Map to array with colors
    let kelIdx = 0;
    g.kelurahan = [...g.kelurahan.values()].map((k) => ({
      ...k,
      color: PIE_COLORS.value[(colorIdx + kelIdx++) % PIE_COLORS.value.length],
    }));
    result.push(g);
    colorIdx++;
  }
  return result.sort((a, b) => b.customers - a.customers);
});

// ── Computed: kelurahan groups ─────────────────────────────────────────────
// Package breakdown per kecamatan (for Paket per Wilayah tab)
const kecamatanPackageMap = computed(() => {
  const map = new Map();
  for (const area of areas.value) {
    const key = area.district || "Tidak Diketahui";
    if (!map.has(key)) map.set(key, {});
    const pkgMap = map.get(key);
    const pkg = area.dominantPackage || "Lainnya";
    pkgMap[pkg] = (pkgMap[pkg] || 0) + area.customers;
  }
  return map;
});

const uniquePackages = computed(() => {
  const pkgSet = new Set();
  for (const area of areas.value) {
    pkgSet.add(area.dominantPackage || "Lainnya");
  }
  return [...pkgSet].sort();
});

const topPackagesSummary = computed(() => {
  const totalCust = areas.value.reduce((s, a) => s + a.customers, 0) || 1;
  const pkgTotals = {};
  for (const area of areas.value) {
    const pkg = area.dominantPackage || "Lainnya";
    pkgTotals[pkg] = (pkgTotals[pkg] || 0) + area.customers;
  }
  return Object.entries(pkgTotals)
    .map(([name, count]) => ({
      name,
      count,
      pct: Math.round((count / totalCust) * 100),
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 6);
});

// Enhance kecamatanGroups with packageBreakdown
const kecamatanGroupsWithPackages = computed(() => {
  return kecamatanGroups.value.map((g) => {
    const pkgMap = kecamatanPackageMap.value.get(g.name) || {};
    const total = g.customers || 1;
    const packageBreakdown = Object.entries(pkgMap)
      .map(([name, count]) => ({
        name,
        count,
        pct: Math.round((count / total) * 100),
      }))
      .sort((a, b) => b.count - a.count);
    return { ...g, packageBreakdown };
  });
});

const kelurahanGroups = computed(() => {
  const map = new Map();
  for (const area of areas.value) {
    const key = `${area.village}__${area.district}`;
    if (!map.has(key))
      map.set(key, {
        name: area.village || "?",
        kecamatan: area.district || "?",
        customers: 0,
      });
    map.get(key).customers += area.customers;
  }
  const total = areas.value.reduce((s, a) => s + a.customers, 0) || 1;
  return [...map.values()]
    .sort((a, b) => b.customers - a.customers)
    .map((item, i) => ({
      ...item,
      pct: Math.round((item.customers / total) * 100),
      color: PIE_COLORS.value[i % PIE_COLORS.value.length],
    }));
});

const filteredKelurahanGroups = computed(() => {
  if (!kecamatanFilter.value) return kelurahanGroups.value;
  return kelurahanGroups.value.filter(
    (k) => k.kecamatan === kecamatanFilter.value,
  );
});

// ── Summary ────────────────────────────────────────────────────────────────
const summary = computed(() => {
  const totals = areas.value.reduce(
    (acc, a) => {
      acc.c += a.customers;
      acc.bw += a.totalBandwidth;
      acc.i += a.isolir;
      return acc;
    },
    { c: 0, bw: 0, i: 0 },
  );
  return {
    totalCustomers: totals.c,
    totalBandwidth: totals.bw,
    totalIsolir: totals.i,
    avgMbps: totals.c ? Math.round(totals.bw / totals.c) : 0,
    isolirRate: totals.c ? Math.round((totals.i / totals.c) * 100) : 0,
    totalKecamatan: kecamatanGroups.value.length,
    totalKelurahan: kelurahanGroups.value.length,
  };
});

const mostPopularPackage = computed(() => {
  if (topPackagesSummary.value.length === 0) {
    return { name: "—", cleanName: "—", count: 0, pct: 0 };
  }
  const top = topPackagesSummary.value[0];
  let cleanName = top.name;
  if (cleanName.includes(" - ")) {
    cleanName = cleanName.split(" - ")[0];
  }
  return {
    ...top,
    cleanName,
  };
});

const topKecamatan = computed(() => kecamatanGroups.value[0] ?? null);

const filteredTableAreas = computed(() => {
  const q = tableSearch.value.trim().toLowerCase();
  let list = q
    ? areas.value.filter((a) =>
        [a.street, a.village, a.district, a.city, a.topAgent]
          .join(" ")
          .toLowerCase()
          .includes(q),
      )
    : [...areas.value];

  // Sort
  if (tableSortKey.value) {
    list = list.sort((a, b) => {
      let av = a[tableSortKey.value];
      let bv = b[tableSortKey.value];
      const numKeys = ["customers", "active", "Mbps", "isolirRate"];
      if (numKeys.includes(tableSortKey.value)) {
        av = Number(av || 0);
        bv = Number(bv || 0);
      } else if (typeof av === "string") {
        av = av.toLowerCase();
        bv = (bv || "").toLowerCase();
      }
      if (av < bv) return tableSortDir.value === "asc" ? -1 : 1;
      if (av > bv) return tableSortDir.value === "asc" ? 1 : -1;
      return 0;
    });
  }
  return list;
});

const tableTotalPages = computed(
  () =>
    Math.ceil(filteredTableAreas.value.length / tableItemsPerPage.value) || 1,
);

const paginatedTableAreas = computed(() => {
  const start = (tablePage.value - 1) * tableItemsPerPage.value;
  return filteredTableAreas.value.slice(start, start + tableItemsPerPage.value);
});

const tableRangeStart = computed(() =>
  filteredTableAreas.value.length
    ? (tablePage.value - 1) * tableItemsPerPage.value + 1
    : 0,
);
const tableRangeEnd = computed(() =>
  Math.min(
    tablePage.value * tableItemsPerPage.value,
    filteredTableAreas.value.length,
  ),
);

const tableVisiblePages = computed(() => {
  const current = tablePage.value;
  const last = tableTotalPages.value;
  const range = [];
  const rangeWithDots = [];
  let l;
  for (let i = 1; i <= last; i++) {
    if (i === 1 || i === last || (i >= current - 1 && i <= current + 1))
      range.push(i);
  }
  for (let i = 0; i < range.length; i++) {
    if (l) {
      if (range[i] - l === 2) rangeWithDots.push(l + 1);
      else if (range[i] - l !== 1) rangeWithDots.push("...");
    }
    rangeWithDots.push(range[i]);
    l = range[i];
  }
  return rangeWithDots;
});

// ── Chart drawing ──────────────────────────────────────────────────────────
function buildChartData(groups, labelKey = "name") {
  const colors = PIE_COLORS.value;
  return {
    labels: groups.map((g) => g[labelKey]),
    datasets: [
      {
        data: groups.map((g) => g.customers),
        backgroundColor: groups.map((_, i) => colors[i % colors.length]),
        borderWidth: 2,
        borderColor: isDark.value ? "#0D1330" : "#fff",
        hoverOffset: 8,
      },
    ],
  };
}

const chartOptions = {
  responsive: false,
  cutout: "65%",
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) =>
          ` ${ctx.label}: ${ctx.parsed} pelanggan (${Math.round((ctx.parsed / summary.value.totalCustomers) * 100)}%)`,
      },
    },
  },
};

async function initCharts() {
  if (!Chart) {
    const {
      Chart: ChartJS,
      DoughnutController,
      ArcElement,
      Tooltip,
      Legend,
    } = await import("chart.js");
    ChartJS.register(DoughnutController, ArcElement, Tooltip, Legend);
    Chart = ChartJS;
  }

  // Kecamatan donut
  if (kecamatanCanvas.value && kecamatanGroups.value.length) {
    if (kecamatanChart) kecamatanChart.destroy();
    kecamatanChart = new Chart(kecamatanCanvas.value, {
      type: "doughnut",
      data: buildChartData(kecamatanGroups.value),
      options: chartOptions,
    });
  }
}

async function initKelurahanChart() {
  if (!Chart) await initCharts();
  if (kelurahanCanvas.value && filteredKelurahanGroups.value.length) {
    if (kelurahanChart) kelurahanChart.destroy();
    kelurahanChart = new Chart(kelurahanCanvas.value, {
      type: "doughnut",
      data: buildChartData(filteredKelurahanGroups.value),
      options: chartOptions,
    });
  }
}

// Re-draw when data changes
watch(kecamatanGroups, async () => {
  await nextTick();
  if (activeTab.value === "kecamatan") initCharts();
});

watch(
  [activeTab, filteredKelurahanGroups],
  async ([tab]) => {
    await nextTick();
    if (tab === "kecamatan") initCharts();
    if (tab === "kelurahan") initKelurahanChart();
  },
  { immediate: false },
);

watch(kecamatanFilter, async () => {
  await nextTick();
  initKelurahanChart();
});

watch(isDark, () => {
  if (activeTab.value === "kecamatan") initCharts();
  if (activeTab.value === "kelurahan") initKelurahanChart();
});

// ── Actions ────────────────────────────────────────────────────────────────
function selectKecamatan(item) {
  selectedKecamatan.value =
    selectedKecamatan.value?.name === item.name ? {} : item;
}

function riskBadge(risk) {
  if (risk === "healthy") return "badge--green";
  if (risk === "risk") return "badge--red";
  return "badge--gold";
}

function riskLabel(risk) {
  return { healthy: "Sehat", watch: "Pantau", risk: "Berisiko" }[risk] ?? risk;
}

// ── Lifecycle ──────────────────────────────────────────────────────────────
const handleTopbarSearch = (e) => {
  tableSearch.value = e.detail || "";
  if (e.detail) activeTab.value = "table";
  tablePage.value = 1;
};

// Reset page when search changes
watch(tableSearch, () => {
  tablePage.value = 1;
});

const initData = async () => {
  loading.value = true;
  try {
    const data = await getAddressInsights();
    areas.value = data || [];
  } catch (err) {
    console.warn("Failed to load address insights:", err);
  } finally {
    loading.value = false;
  }
};

let observer;

onMounted(async () => {
  window.addEventListener("topbar-search", handleTopbarSearch);

  // Track class changes on <html> for dynamic dark mode charts update
  observer = new MutationObserver(() => {
    isDark.value = document.documentElement.classList.contains("dark");
  });
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["class"],
  });

  try {
    const payload = await getAddressInsights();
    areas.value = payload?.areas || [];
  } catch {
    areas.value = [];
    console.warn("Failed to load address insights");
  } finally {
    loading.value = false;
  }

  selectedKecamatan.value = kecamatanGroups.value[0] || {};
  await nextTick();
  initCharts();
});

onUnmounted(() => {
  window.removeEventListener("topbar-search", handleTopbarSearch);
  if (observer) {
    observer.disconnect();
  }
  kecamatanChart?.destroy();
  kelurahanChart?.destroy();
});
</script>

<style scoped>
.address-insights {
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
}
.page-sub {
  font-size: 13px;
  color: var(--text-2);
  margin-top: 4px;
}
.page-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.btn {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}
.btn svg {
  width: 15px;
  height: 15px;
}
.btn--ghost {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-1);
}
.btn--ghost:hover {
  background: var(--bg);
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.kpi-card {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.kpi-card--dark {
  background: var(--navy);
  border-color: transparent;
}
.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.kpi-label {
  font-size: 10px;
  font-weight: 800;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
}
.kpi-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}
.kpi-icon svg {
  width: 16px;
  height: 16px;
}
.kpi-icon--blue {
  background: #eef2ff;
  color: var(--navy);
}
.kpi-icon--teal {
  background: #e0f8f6;
  color: var(--teal);
}
.kpi-icon--gold {
  background: #fef3e0;
  color: var(--gold);
}
.kpi-value {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 800;
  line-height: 1;
  color: var(--text-1);
}
.kpi-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.badge-inline {
  font-size: 10px;
  font-weight: 800;
  font-family: var(--font-display);
  padding: 2px 7px;
  border-radius: 99px;
  white-space: nowrap;
}
.badge-inline--blue {
  background: #eef2ff;
  color: var(--navy);
}
.badge-inline--teal {
  background: #e0f8f6;
  color: var(--teal);
}
.badge-inline--gold {
  background: #fef3e0;
  color: var(--gold);
}
.top-area-name {
  color: #fff;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 800;
  line-height: 1.25;
}
.success-rate {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  background: rgba(255, 255, 255, 0.08);
  padding: 6px 10px;
  border-radius: var(--r-sm);
}

/* Tabs */
.tab-bar {
  display: flex;
  gap: 6px;
  border-bottom: 1px solid var(--border);
  padding-bottom: 0;
}
.tab-btn {
  padding: 9px 18px;
  font-size: 13px;
  font-weight: 700;
  font-family: var(--font-display);
  border: none;
  background: none;
  color: var(--text-3);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.15s;
  border-radius: var(--r-sm) var(--r-sm) 0 0;
}
.tab-btn:hover {
  color: var(--text-1);
  background: var(--bg);
}
.tab-btn--active {
  color: var(--navy);
  border-bottom-color: var(--navy);
  background: none;
}

/* Charts */
.chart-section {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 14px;
  align-items: start;
}
.chart-card {
  padding: 22px;
}
.detail-side {
  padding: 20px;
}
.section-hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.chart-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 800;
  color: var(--text-1);
}
.chart-sub {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}

.chart-layout {
  display: flex;
  gap: 28px;
  align-items: center;
}
.donut-wrap {
  position: relative;
  flex-shrink: 0;
}
.donut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
}
.donut-num {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 800;
  color: var(--text-1);
  line-height: 1;
}
.donut-label {
  font-size: 10px;
  color: var(--text-3);
  margin-top: 3px;
  text-transform: uppercase;
  font-weight: 700;
}

.legend-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.legend-list--scroll {
  max-height: 260px;
  overflow-y: auto;
  padding-right: 4px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: background 0.12s;
}
.legend-item:hover,
.legend-item--active {
  background: var(--bg);
}
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.legend-info {
  flex: 1;
  min-width: 0;
}
.legend-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}
.legend-meta {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 1px;
}
.legend-pct {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 800;
  color: var(--text-2);
  flex-shrink: 0;
}

/* Detail side */
.detail-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 18px;
}
.detail-metric {
  background: var(--bg);
  border-radius: var(--r-sm);
  padding: 10px;
  text-align: center;
}
.detail-metric span {
  display: block;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 800;
  color: var(--text-1);
}
.detail-metric small {
  display: block;
  font-size: 9px;
  font-weight: 700;
  color: var(--text-3);
  margin-top: 3px;
  text-transform: uppercase;
  font-family: var(--font-display);
}

.kelurahan-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.kelurahan-item {
  display: grid;
  grid-template-columns: 1fr 80px 32px;
  gap: 8px;
  align-items: center;
}
.kel-info .kel-name {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
}
.kel-info .kel-count {
  display: block;
  font-size: 10px;
  color: var(--text-3);
}
.kel-bar-wrap {
  height: 6px;
  background: var(--bg);
  border-radius: 99px;
  overflow: hidden;
  border: 1px solid var(--border);
}
.kel-bar {
  height: 100%;
  border-radius: 99px;
  transition: width 0.3s;
}
.kel-pct {
  font-size: 10px;
  font-weight: 800;
  font-family: var(--font-display);
  color: var(--text-2);
  text-align: right;
}
.agent-block {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border-top: 1px solid var(--border);
  padding-top: 13px;
  margin-top: 15px;
}
.agent-label {
  font-size: 11px;
  color: var(--text-3);
}
.agent-name {
  font-size: 12px;
  font-weight: 700;
  color: var(--navy);
  text-align: right;
}

/* Filter select */
.filter-select {
  position: relative;
  display: flex;
  align-items: center;
}
.filter-select select {
  height: 38px;
  appearance: none;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 0 30px 0 12px;
  font-size: 12px;
  font-family: var(--font-body);
  color: var(--text-1);
  outline: none;
}
.filter-select svg {
  position: absolute;
  right: 9px;
  width: 14px;
  height: 14px;
  color: var(--text-3);
  pointer-events: none;
}

/* Badges */
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
.badge--navy {
  background: #eef2ff;
  color: var(--navy);
}
.badge--green {
  background: #e8f8ef;
  color: var(--green-ok);
}
.badge--gold {
  background: #fef3e0;
  color: var(--gold);
}
.badge--red {
  background: #fdeceb;
  color: var(--red-warn);
}

/* Table */
.section-card {
  padding: 20px;
}
.search-field {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 0 12px;
}
.search-field svg {
  width: 15px;
  height: 15px;
  color: var(--text-3);
  flex-shrink: 0;
}
.search-field input {
  height: 36px;
  border: none;
  outline: none;
  background: none;
  font-size: 12px;
  color: var(--text-1);
  width: 220px;
}
.table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  margin-top: 4px;
  border-radius: 0 0 var(--r-md) var(--r-md);
}
.address-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}

/* Table footer pagination */
.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 4px 4px;
  gap: 12px;
  flex-wrap: wrap;
  border-top: 1px solid var(--border);
  margin-top: 8px;
}
.table-count {
  font-size: 12px;
  color: var(--text-3);
}
.table-count strong {
  color: var(--text-1);
}
.pagination {
  display: flex;
  gap: 4px;
  align-items: center;
}
.page-btn {
  min-width: 30px;
  height: 30px;
  border-radius: 6px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text-2);
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
  padding: 0 6px;
}
.page-btn svg {
  width: 14px;
  height: 14px;
}
.page-btn:hover:not(:disabled) {
  background: var(--bg);
  color: var(--navy);
  border-color: var(--navy);
}
.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.page-btn--active {
  background: var(--navy);
  color: #fff;
  border-color: var(--navy);
}
.page-btn--dots {
  border: none;
  background: none;
  cursor: default;
}
.address-table th {
  font-size: 10px;
  font-weight: 800;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
  text-align: left;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
  background: var(--surface-2);
  white-space: nowrap;
}
.address-table td {
  padding: var(--table-row-padding, 12px 14px);
  border-bottom: 1px solid var(--border);
  font-size: 12px;
  vertical-align: middle;
  white-space: nowrap;
}
.address-table tr:last-child td {
  border-bottom: none;
}
.address-table tr:hover td {
  background: var(--bg);
}
.td-address {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.td-address span {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
  overflow: hidden;
  text-overflow: ellipsis;
}
.td-address small {
  display: block;
  font-size: 10px;
  color: var(--text-3);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.td-num {
  font-family: var(--font-display);
  font-weight: 800;
}
.td-empty {
  text-align: center;
  color: var(--text-3);
  padding: 32px !important;
}
.growth-tag,
.type-tag {
  display: inline-flex;
  align-items: center;
  border-radius: 99px;
  padding: 3px 8px;
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 800;
}
.growth-tag--up {
  background: #e8f8ef;
  color: var(--green-ok);
}
.growth-tag--down {
  background: #fdeceb;
  color: var(--red-warn);
}
.type-tag {
  background: #eef2ff;
  color: var(--navy);
  border-radius: 4px;
}

@media (max-width: 1100px) {
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-section {
    grid-template-columns: 1fr;
  }
  .chart-layout {
    flex-direction: column;
  }
}
@media (max-width: 760px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
}

/* Empty State */
.address-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 64px 32px;
  text-align: center;
  border: 2px dashed var(--border);
  border-radius: var(--r-lg);
  margin: 16px 0;
}
.address-empty-state svg {
  width: 56px;
  height: 56px;
  color: var(--text-3);
}
.address-empty-state h3 {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 800;
  color: var(--text-1);
}
.address-empty-state p {
  font-size: 13px;
  color: var(--text-3);
  max-width: 380px;
  line-height: 1.6;
}
.empty-cta-btn {
  display: inline-flex;
  align-items: center;
  padding: 10px 22px;
  background: var(--navy);
  color: #fff;
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
  font-family: var(--font-display);
  transition: background 0.15s;
}
.empty-cta-btn:hover {
  background: var(--navy-mid, #1a2260);
}

/* ===== Sort Icons ===== */
.th-sort {
  cursor: pointer !important;
  user-select: none;
}
.th-sort:hover {
  background: rgba(13, 19, 64, 0.03) !important;
}
.sort-icon {
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
  vertical-align: middle;
}
.sort-icon svg {
  width: 10px;
  height: 10px;
}

/* ===== Footer Layout ===== */
.footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.table-count {
  font-size: 12px;
  color: var(--text-3);
}
.table-count strong {
  color: var(--text-2);
  font-weight: 600;
}
.rows-select {
  appearance: none;
  -webkit-appearance: none;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 5px 20px 5px 10px;
  font-size: 11px;
  font-weight: 600;
  font-family: var(--font-display);
  color: var(--text-2);
  cursor: pointer;
  outline: none;
}
.rows-select:focus {
  border-color: var(--navy);
}

/* ===== Paket per Wilayah Tab ===== */
.pkg-legend-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin: 16px 0 20px;
}
.pkg-legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pkg-legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.pkg-legend-label {
  font-size: 11px;
  color: var(--text-2);
  font-weight: 500;
}

.pkg-rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.pkg-row {
  display: grid;
  grid-template-columns: 160px 1fr 80px;
  align-items: center;
  gap: 12px;
}
.pkg-row-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
  text-align: right;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pkg-bar-stacked {
  height: 22px;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  background: var(--border);
}
.pkg-bar-seg {
  height: 100%;
  transition: width 0.3s;
}
.pkg-bar-seg:first-child {
  border-radius: 4px 0 0 4px;
}
.pkg-bar-seg:last-child {
  border-radius: 0 4px 4px 0;
}
.pkg-row-total {
  font-size: 11px;
  color: var(--text-3);
  font-weight: 500;
}

.pkg-summary {
  margin-top: 28px;
}
.pkg-sum-title {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  color: var(--text-2);
  text-transform: uppercase;
  letter-spacing: 0;
  margin-bottom: 14px;
}
.pkg-sum-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.pkg-sum-card {
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  background: var(--surface-2);
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.pkg-sum-rank {
  font-size: 10px;
  font-weight: 700;
  color: var(--text-3);
  font-family: var(--font-display);
}
.pkg-sum-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}
.pkg-sum-count {
  font-size: 12px;
  color: var(--text-2);
}
.pkg-sum-pct {
  font-size: 11px;
  color: var(--navy);
  font-weight: 600;
}
.pkg-sum-bar {
  height: 4px;
  background: var(--border);
  border-radius: 99px;
  margin-top: 6px;
  overflow: hidden;
}
.pkg-sum-bar-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 0.4s;
}
</style>
