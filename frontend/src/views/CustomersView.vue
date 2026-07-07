<template>
  <div class="customers">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Customer Management</h1>
        <p class="page-sub">
          Manage global enterprise connectivity and subscriber accounts.
        </p>
      </div>
      <div class="page-actions">
        <button class="btn btn--ghost" @click="showExportModal = true">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="7 10 12 15 17 10" />
            <line x1="12" y1="15" x2="12" y2="3" />
          </svg>
          Export
        </button>
        <button class="btn btn--primary" @click="showRegisterModal = true">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
            <circle cx="8.5" cy="7" r="4" />
            <line x1="20" y1="8" x2="20" y2="14" />
            <line x1="23" y1="11" x2="17" y2="11" />
          </svg>
          Register Customer
        </button>
      </div>
    </div>

    <!-- KPI Row -->
    <div class="kpi-row">
      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Total Subscribers</span>
          <div class="kpi-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 00-3-3.87" />
              <path d="M16 3.13a4 4 0 010 7.75" />
            </svg>
          </div>
        </div>
        <div class="kpi-value">{{ kpiData.total }}</div>
        <div class="kpi-meta">
          <span
            class="badge-inline"
            :class="
              kpiData.growthSign >= 0
                ? 'badge-inline--green'
                : 'badge-inline--red'
            "
            >{{ kpiData.growthText }}</span
          >
          <span class="kpi-sub">vs last month</span>
        </div>
      </div>

      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Active</span>
          <div class="kpi-icon kpi-icon--teal">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <circle cx="12" cy="12" r="2" />
              <path d="M16.24 7.76a6 6 0 010 8.49" />
              <path d="M19.07 4.93a10 10 0 010 14.14" />
              <path d="M7.76 16.24a6 6 0 010-8.49" />
              <path d="M4.93 19.07a10 10 0 010-14.14" />
            </svg>
          </div>
        </div>
        <div class="kpi-value" style="color: var(--teal)">
          {{ kpiData.active }}
        </div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--teal">{{
            kpiData.saturation
          }}</span>
          <span class="kpi-sub">Customer Active</span>
        </div>
      </div>

      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">New This Month</span>
          <div class="kpi-icon kpi-icon--green">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
          </div>
        </div>
        <div class="kpi-value" style="color: var(--green-ok)">
          {{ kpiData.newThisMonth }}
        </div>
        <div class="kpi-meta"></div>
      </div>
    </div>

    <!-- ===== ROW: Growth Line + Status Donut ===== -->
    <div
      class="charts-row"
      style="
        display: grid;
        grid-template-columns: 2fr 1fr;
        gap: 20px;
        margin-bottom: 20px;
        width: 100%;
      "
    >
      <!-- Cumulative Growth -->
      <div
        class="card chart-card"
        style="padding: 20px"
        v-if="cumulativeGrowthData.labels.length > 0"
      >
        <div
          class="chart-header"
          style="
            margin-bottom: 16px;
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
          "
        >
          <div>
            <div
              class="chart-title"
              style="
                font-family: var(--font-display);
                font-size: 14px;
                font-weight: 700;
              "
            >
              Subscriber Growth
            </div>
            <div
              class="chart-sub"
              style="font-size: 11px; color: var(--text-3)"
            >
              Cumulative total subscribers over time
            </div>
          </div>
          <div class="time-filters">
            <button
              class="time-filter-btn"
              :class="{ active: growthFilter === '6M' }"
              @click="growthFilter = '6M'"
            >
              6M
            </button>
            <button
              class="time-filter-btn"
              :class="{ active: growthFilter === '1Y' }"
              @click="growthFilter = '1Y'"
            >
              1Y
            </button>
            <button
              class="time-filter-btn"
              :class="{ active: growthFilter === 'All' }"
              @click="growthFilter = 'All'"
            >
              All
            </button>
          </div>
        </div>
        <div class="chart-wrap" style="height: 200px">
          <Line :data="filteredCumulativeChartData" :options="lineOptions" />
        </div>
      </div>

      <!-- Status Donut -->
      <div class="card chart-card chart-card--sm" style="padding: 20px">
        <div class="chart-header" style="margin-bottom: 16px">
          <div>
            <div
              class="chart-title"
              style="
                font-family: var(--font-display);
                font-size: 14px;
                font-weight: 700;
              "
            >
              Subscriber Status
            </div>
            <div
              class="chart-sub"
              style="font-size: 11px; color: var(--text-3)"
            >
              Active vs Isolir breakdown
            </div>
          </div>
        </div>
        <div
          class="donut-wrap"
          style="
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 16px;
            height: 200px;
            justify-content: center;
          "
        >
          <svg
            class="donut-svg"
            viewBox="0 0 120 120"
            style="width: 130px; height: 130px"
          >
            <g transform="rotate(-90 60 60)">
              <circle
                class="donut-bg"
                cx="60"
                cy="60"
                r="48"
                style="fill: none; stroke: var(--border); stroke-width: 14"
              />
              <circle
                v-if="statusDist.activePct > 0"
                class="donut-active"
                cx="60"
                cy="60"
                r="48"
                :stroke-dasharray="`${statusDist.activePct * 3.016} ${301.6}`"
                stroke-dashoffset="0"
                :style="{
                  fill: 'none',
                  stroke: 'var(--teal)',
                  strokeWidth: 14,
                  strokeLinecap: statusDist.activePct >= 100 ? 'butt' : 'round',
                }"
              />
              <circle
                v-if="statusDist.isolirPct > 0"
                class="donut-isolir"
                cx="60"
                cy="60"
                r="48"
                :stroke-dasharray="`${statusDist.isolirPct * 3.016} ${301.6}`"
                :stroke-dashoffset="`${-(statusDist.activePct * 3.016)}`"
                :style="{
                  fill: 'none',
                  stroke: 'var(--gold)',
                  strokeWidth: 14,
                  strokeLinecap: statusDist.isolirPct >= 100 ? 'butt' : 'round',
                }"
              />
            </g>
            <text
              x="60"
              y="54"
              style="
                font-family: var(--font-display);
                font-size: 22px;
                font-weight: 800;
                fill: var(--navy);
              "
              text-anchor="middle"
              dominant-baseline="middle"
            >
              {{ statusDist.activePct }}%
            </text>
            <text
              x="60"
              y="70"
              style="
                font-family: var(--font-display);
                font-size: 9px;
                font-weight: 600;
                fill: var(--text-3);
                text-transform: uppercase;
                letter-spacing: 1px;
              "
              text-anchor="middle"
              dominant-baseline="middle"
            >
              Active
            </text>
          </svg>
          <div class="donut-legend" style="display: flex; gap: 16px">
            <div
              class="donut-legend-item"
              style="display: flex; align-items: center; gap: 6px"
            >
              <span
                class="donut-dot"
                style="
                  width: 8px;
                  height: 8px;
                  border-radius: 50%;
                  flex-shrink: 0;
                  background: var(--teal);
                "
              ></span>
              <span
                class="donut-legend-label"
                style="font-size: 11px; color: var(--text-3)"
                >Active</span
              >
              <span
                class="donut-legend-val"
                style="
                  font-size: 12px;
                  font-weight: 700;
                  color: var(--text-1);
                  font-family: var(--font-display);
                "
                >{{ statusDist.active }}</span
              >
            </div>
            <div
              class="donut-legend-item"
              style="display: flex; align-items: center; gap: 6px"
            >
              <span
                class="donut-dot"
                style="
                  width: 8px;
                  height: 8px;
                  border-radius: 50%;
                  flex-shrink: 0;
                  background: var(--gold);
                "
              ></span>
              <span
                class="donut-legend-label"
                style="font-size: 11px; color: var(--text-3)"
                >Isolir</span
              >
              <span
                class="donut-legend-val"
                style="
                  font-size: 12px;
                  font-weight: 700;
                  color: var(--text-1);
                  font-family: var(--font-display);
                "
                >{{ statusDist.isolir }}</span
              >
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card table-card">
      <!-- Filters -->
      <div class="table-toolbar">
        <div class="filters-left">
          <div class="search-input">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              class="search-icon"
            >
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search by name or ID..."
              class="form-input-sm"
            />
          </div>

          <div class="filter-select">
            <select v-model="statusFilter">
              <option value="">Status: All</option>
              <option value="ACTIVE">Active</option>
              <option value="ISOLIR">Isolir</option>
              <option value="NEW">New</option>
            </select>
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>
          <div class="filter-select">
            <select v-model="serviceFilter">
              <option value="">Service: All</option>
              <option v-for="p in packagesList" :key="p.id" :value="p.name">
                {{ p.name }}
              </option>
            </select>
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>
        </div>
        <div class="filters-right">
          <button class="btn-filter" @click="resetFilters">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
              :class="{ 'rotate-icon': isResetting }"
            >
              <line x1="4" y1="21" x2="4" y2="14" />
              <line x1="4" y1="10" x2="4" y2="3" />
              <line x1="12" y1="21" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12" y2="3" />
              <line x1="20" y1="21" x2="20" y2="16" />
              <line x1="20" y1="12" x2="20" y2="3" />
              <line x1="1" y1="14" x2="7" y2="14" />
              <line x1="9" y1="8" x2="15" y2="8" />
              <line x1="17" y1="16" x2="23" y2="16" />
            </svg>
            Reset
          </button>
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <table class="cust-table">
          <thead>
            <tr>
              <th class="th-sort" @click="toggleSort('id')">
                Customer ID
                <span class="sort-icon">
                  <svg
                    v-if="sortKey === 'id'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        sortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'
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
                    <polyline points="18 15 12 9 6 15" />
                    <polyline points="6 9 12 15 18 9" style="display: none" />
                  </svg>
                </span>
              </th>
              <th class="th-sort" @click="toggleSort('org')">
                Organization
                <span class="sort-icon">
                  <svg
                    v-if="sortKey === 'org'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        sortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'
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
              <th>Service Details</th>
              <th class="th-sort" @click="toggleSort('status')">
                Operational Status
                <span class="sort-icon">
                  <svg
                    v-if="sortKey === 'status'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        sortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'
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
              <th class="th-sort" @click="toggleSort('agent')">
                Assigned Agent
                <span class="sort-icon">
                  <svg
                    v-if="sortKey === 'agent'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        sortDir === 'asc' ? '18 15 12 9 6 15' : '6 9 12 15 18 9'
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
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredCustomers.length === 0">
              <td colspan="6" class="td-empty">
                <div class="empty-state">
                  <div class="empty-glow"></div>
                  <div class="empty-icon-wrap">
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                      class="pulsing-svg"
                    >
                      <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
                      <circle cx="9" cy="7" r="4" />
                      <path d="M23 21v-2a4 4 0 00-3-3.87" />
                    </svg>
                  </div>
                  <h3>Belum Ada Pelanggan</h3>
                  <p>
                    Database pelanggan saat ini kosong. Daftarkan pelanggan baru
                    secara manual atau impor dari Excel.
                  </p>
                  <button
                    class="btn btn--primary"
                    style="margin: 12px auto 0 auto"
                    @click="showRegisterModal = true"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                    Daftar Pelanggan Pertama
                  </button>
                </div>
              </td>
            </tr>
            <tr
              v-else
              v-for="c in paginatedCustomers"
              :key="c.id"
              class="cust-row"
            >
              <td>
                <div
                  class="cust-id-badge"
                  :style="{ background: c.color + '18', color: c.color }"
                >
                  #{{ c.id }}
                </div>
              </td>
              <td>
                <div class="org-cell">
                  <div class="org-avatar" :style="{ background: c.color }">
                    {{ c.initials }}
                  </div>
                  <div>
                    <div class="org-name">{{ c.org }}</div>
                    <div class="org-loc">
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="1.7"
                        stroke-linecap="round"
                        style="width: 10px; height: 10px"
                      >
                        <path
                          d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"
                        />
                        <circle cx="12" cy="10" r="3" />
                      </svg>
                      {{ c.location }}
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <div class="svc-cell">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.7"
                    stroke-linecap="round"
                    style="width: 13px; height: 13px; color: var(--text-3)"
                  >
                    <path
                      d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"
                    />
                    <polyline points="22,6 12,13 2,6" />
                  </svg>
                  <div>
                    <div class="svc-name">{{ c.service }}</div>
                    <div class="svc-renewal">Registered: {{ c.regDate }}</div>
                    <div class="svc-renewal">
                      Activated: {{ c.activationDate }}
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <span
                  class="badge"
                  :class="
                    c.status === 'ACTIVE' ? 'badge--green' : 'badge--gold'
                  "
                >
                  {{ c.status }}
                </span>
              </td>
              <td>
                <div class="agent-cell">
                  <div class="agent-avatar-sm">{{ c.agentInitial }}</div>
                  <span class="agent-name-sm">{{ c.agent }}</span>
                </div>
              </td>
              <td class="action-cell">
                <button
                  class="action-btn"
                  @click="activeMenu = activeMenu === c.id ? null : c.id"
                >
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                  >
                    <circle cx="12" cy="12" r="1" />
                    <circle cx="19" cy="12" r="1" />
                    <circle cx="5" cy="12" r="1" />
                  </svg>
                </button>
                <div v-if="activeMenu === c.id" class="action-dropdown">
                  <button
                    @click="
                      openDetail(c);
                      activeMenu = null;
                    "
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      style="
                        width: 13px;
                        height: 13px;
                        vertical-align: middle;
                        margin-right: 4px;
                      "
                    >
                      <circle cx="12" cy="12" r="10" />
                      <line x1="12" y1="8" x2="12" y2="12" />
                      <line x1="12" y1="16" x2="12.01" y2="16" />
                    </svg>
                    View Details
                  </button>
                  <button @click="toggleStatus(c)">
                    {{ c.status === "ACTIVE" ? "Isolir" : "Activate" }}
                  </button>
                  <button class="danger" @click="removeCustomer(c)">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="table-footer">
        <div class="footer-left">
          <span class="table-count"
            >Showing <strong>{{ rangeStart }}-{{ rangeEnd }}</strong> of
            <strong>{{ sortedFilteredCustomers.length }}</strong> entries</span
          >
          <select class="rows-select" v-model="itemsPerPage">
            <option :value="10">10 / page</option>
            <option :value="25">25 / page</option>
            <option :value="50">50 / page</option>
          </select>
        </div>
        <div class="pagination">
          <button class="page-btn" :disabled="page === 1" @click="page--">
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
            v-for="(p, index) in visiblePages"
            :key="index"
            class="page-btn page-num"
            :class="{
              'page-btn--active': page === p,
              'page-btn--dots': p === '...',
            }"
            :disabled="p === '...'"
            @click="p !== '...' ? (page = p) : null"
          >
            {{ p }}
          </button>
          <button
            class="page-btn"
            :disabled="page === totalPages"
            @click="page++"
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

    <!-- Bottom Row -->
    <div class="bottom-row">
      <!-- Capacity Insight -->
      <div class="card insight-card">
        <span class="insight-tag">Capacity Insight</span>
        <h3 class="insight-title">
          {{ capacityInsight.title }}
        </h3>
        <p class="insight-desc">
          {{ capacityInsight.description }}
        </p>
        <button class="btn-analyze" @click="focusHealthQueue">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.7"
            stroke-linecap="round"
          >
            <rect x="2" y="3" width="20" height="14" rx="2" />
            <line x1="8" y1="21" x2="16" y2="21" />
            <line x1="12" y1="17" x2="12" y2="21" />
          </svg>
          {{ capacityInsight.action }}
        </button>
      </div>

      <!-- Queue Priority -->
      <div class="card queue-card">
        <div class="queue-header">
          <span class="queue-title">Queue Priority</span>
          <span
            class="badge"
            :class="queue.length ? 'badge--red' : 'badge--green'"
            >+ {{ queue.length }} Active</span
          >
        </div>
        <div class="queue-list">
          <div class="queue-item" v-for="q in queue" :key="q.id">
            <div class="queue-bar" :style="{ background: q.color }"></div>
            <div class="queue-info">
              <span class="queue-name">{{ q.name }}</span>
              <span class="queue-time">{{ q.time }}</span>
            </div>
            <span class="queue-id">#{{ q.id }}</span>
          </div>
        </div>
        <button class="all-tickets-btn" @click="goToTickets">
          All Tickets
        </button>
      </div>
    </div>
    <!-- Custom Confirm Modal -->
    <div
      v-if="showConfirmModal"
      class="modal-overlay"
      @click.self="showConfirmModal = false"
      style="z-index: 1000"
    >
      <div class="modal-card modal-card--confirm">
        <div class="modal-header">
          <div class="confirm-title-wrapper">
            <div class="confirm-icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"
                />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
            </div>
            <h2>{{ confirmTitle }}</h2>
          </div>
          <button @click="showConfirmModal = false" class="close-btn">
            &times;
          </button>
        </div>
        <div class="modal-body">
          <p class="confirm-text">{{ confirmMessage }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn--ghost" @click="showConfirmModal = false">
            Batal
          </button>
          <button class="btn btn--danger" @click="executeConfirm">
            Ya, Hapus
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Register (Simple Placeholder) -->
    <div
      v-if="showRegisterModal"
      class="modal-overlay"
      @click.self="showRegisterModal = false"
    >
      <div class="modal-card">
        <div class="modal-header">
          <h2>Register New Customer</h2>
          <button @click="showRegisterModal = false" class="close-btn">
            &times;
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Customer ID (Cust ID)</label>
            <input
              v-model="newCustomer.custId"
              type="text"
              class="form-input"
              placeholder="VN00000 or (Leave blank to auto-generate)"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Customer Name</label>
            <input
              v-model="newCustomer.name"
              type="text"
              class="form-input"
              placeholder="Full Name or Organization Name"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Internet Package</label>
            <div class="filter-select" style="width: 100%">
              <select v-model="newCustomer.packageId" class="form-input">
                <option v-for="p in packagesList" :key="p.id" :value="p.id">
                  {{ p.name }} - {{ p.speed }} Mbps (Rp
                  {{ new Intl.NumberFormat("id-ID").format(p.price) }})
                </option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Address</label>
            <textarea
              v-model="newCustomer.address"
              class="form-input"
              rows="2"
              placeholder="Full Address"
            ></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">Agent Name</label>
            <input
              v-model="newCustomer.agent"
              type="text"
              class="form-input"
              placeholder="Assigned Agent (optional)"
            />
          </div>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">Registration Date</label>
              <input
                v-model="newCustomer.registrationDate"
                type="date"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label class="form-label">Activation Date</label>
              <input
                v-model="newCustomer.activationDate"
                type="date"
                class="form-input"
              />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn--ghost" @click="showRegisterModal = false">
            Cancel
          </button>
          <button class="btn btn--primary" @click="submitRegistration">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            >
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
            Register
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Export -->
    <div
      v-if="showExportModal"
      class="modal-overlay"
      @click.self="showExportModal = false"
    >
      <div class="modal-card" style="max-width: 500px;">
        <div class="modal-header">
          <h2>Export Subscribers Data</h2>
          <button @click="showExportModal = false" class="close-btn">
            &times;
          </button>
        </div>
        <div class="modal-body">
          <p class="modal-desc" style="font-size: 13px; color: var(--text-3); margin-bottom: 16px;">
            Export subscribers list to Microsoft Excel format with selected period filter.
          </p>
          
          <div class="form-group">
            <label class="form-label">Timeframe Period</label>
            <div class="filter-select" style="width: 100%;">
              <select v-model="exportFilterType" class="form-input">
                <option value="all">Semua Waktu (All Time)</option>
                <option value="weekly">Mingguan (Weekly)</option>
                <option value="month">Bulanan (Monthly)</option>
                <option value="quarter">Per Kuartal (Quarterly)</option>
                <option value="year">Tahunan (Yearly)</option>
              </select>
            </div>
          </div>

          <!-- Weekly Filter Context -->
          <div v-if="exportFilterType === 'weekly'" class="form-grid" style="grid-template-columns: repeat(3, minmax(0, 1fr));">
            <div class="form-group">
              <label class="form-label">Tahun</label>
              <div class="filter-select" style="width: 100%;">
                <select v-model="exportYear" class="form-input">
                  <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Bulan</label>
              <div class="filter-select" style="width: 100%;">
                <select v-model="exportMonth" class="form-input">
                  <option :value="1">Januari</option>
                  <option :value="2">Februari</option>
                  <option :value="3">Maret</option>
                  <option :value="4">April</option>
                  <option :value="5">Mei</option>
                  <option :value="6">Juni</option>
                  <option :value="7">Juli</option>
                  <option :value="8">Agustus</option>
                  <option :value="9">September</option>
                  <option :value="10">Oktober</option>
                  <option :value="11">November</option>
                  <option :value="12">Desember</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Minggu Ke</label>
              <div class="filter-select" style="width: 100%;">
                <select v-model="exportWeek" class="form-input">
                  <option :value="1">Minggu 1</option>
                  <option :value="2">Minggu 2</option>
                  <option :value="3">Minggu 3</option>
                  <option :value="4">Minggu 4</option>
                  <option :value="5">Minggu 5</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Month Filter Context -->
          <div v-if="exportFilterType === 'month'" class="form-grid">
            <div class="form-group">
              <label class="form-label">Tahun</label>
              <div class="filter-select" style="width: 100%;">
                <select v-model="exportYear" class="form-input">
                  <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Bulan</label>
              <div class="filter-select" style="width: 100%;">
                <select v-model="exportMonth" class="form-input">
                  <option :value="1">Januari</option>
                  <option :value="2">Februari</option>
                  <option :value="3">Maret</option>
                  <option :value="4">April</option>
                  <option :value="5">Mei</option>
                  <option :value="6">Juni</option>
                  <option :value="7">Juli</option>
                  <option :value="8">Agustus</option>
                  <option :value="9">September</option>
                  <option :value="10">Oktober</option>
                  <option :value="11">November</option>
                  <option :value="12">Desember</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Quarter Filter Context -->
          <div v-if="exportFilterType === 'quarter'" class="form-grid">
            <div class="form-group">
              <label class="form-label">Tahun</label>
              <div class="filter-select" style="width: 100%;">
                <select v-model="exportYear" class="form-input">
                  <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Kuartal</label>
              <div class="filter-select" style="width: 100%;">
                <select v-model="exportQuarter" class="form-input">
                  <option :value="1">Q1 (Jan - Mar)</option>
                  <option :value="2">Q2 (Apr - Jun)</option>
                  <option :value="3">Q3 (Jul - Sep)</option>
                  <option :value="4">Q4 (Oct - Dec)</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Year Filter Context -->
          <div v-if="exportFilterType === 'year'" class="form-group">
            <label class="form-label">Tahun</label>
            <div class="filter-select" style="width: 100%;">
              <select v-model="exportYear" class="form-input">
                <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
              </select>
            </div>
          </div>

          <!-- Columns Preview -->
          <div class="export-preview-card" style="margin-top: 16px; padding: 12px; border: 1px solid var(--border); border-radius: 8px; background: rgba(0,0,0,0.02);">
            <div style="font-size: 11px; font-weight: 700; color: var(--text-3); text-transform: uppercase; margin-bottom: 6px; letter-spacing: 0.5px;">
              Export Columns (16 Fields)
            </div>
            <div style="display: flex; flex-wrap: wrap; gap: 4px; font-size: 11px;">
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">No</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Tanggal Registrasi</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Agen</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Nama</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Alamat</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">RT / RW</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Kecamatan</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Kelurahan/Desa</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Kota/Kab</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Kode Pos</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Nomor Telpon</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Email</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Package</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Status</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Tanggal Aktivasi</span>
              <span class="badge badge--teal" style="font-size: 10px; padding: 2px 6px;">Cust Id</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn--ghost" @click="showExportModal = false" :disabled="exportLoading">
            Cancel
          </button>
          <button class="btn btn--primary" @click="handleExportDownload" :disabled="exportLoading" style="min-width: 140px; display: flex; align-items: center; justify-content: center; gap: 6px;">
            <svg v-if="!exportLoading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width: 15px; height: 15px;">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
            <svg v-else class="spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="width: 15px; height: 15px;">
              <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" opacity=".3" />
              <path d="M12 3a9 9 0 019 9" />
            </svg>
            {{ exportLoading ? "Downloading..." : "Download Excel" }}
          </button>
        </div>
      </div>
    </div>

    <!-- Customer Detail Panel (new rich panel component) -->
    <Teleport to="body">
      <CustomerDetailPanel
        :visible="showDetailPanel"
        :customer="selectedCustomer"
        @close="showDetailPanel = false"
      />
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { Line } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
} from "chart.js";
import {
  getCustomers,
  getPackages,
  createCustomer,
  updateCustomerStatus,
  deleteCustomer,
  getDashboardOverview,
  downloadCustomersExport,
} from "@/services/api";
import CustomerDetailPanel from "@/components/CustomerDetailPanel.vue";
import { useToast } from "@/composables/useToast";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
);

const toast = useToast();
const router = useRouter();
const isResetting = ref(false);

const page = ref(1);
const itemsPerPage = ref(10);
const sortKey = ref("");
const sortDir = ref("asc");
const statusFilter = ref("");
const serviceFilter = ref("");
const searchQuery = ref("");
const activeMenu = ref(null);

function toggleSort(key) {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === "asc" ? "desc" : "asc";
  } else {
    sortKey.value = key;
    sortDir.value = "asc";
  }
  page.value = 1;
}
const showRegisterModal = ref(false);
const showExportModal = ref(false);
const exportLoading = ref(false);
const exportFilterType = ref("all");
const exportYear = ref(new Date().getFullYear());
const exportMonth = ref(new Date().getMonth() + 1);
const exportQuarter = ref(Math.floor(new Date().getMonth() / 3) + 1);
const exportWeek = ref(1);

const availableYears = computed(() => {
  const currentYear = new Date().getFullYear();
  const years = [];
  for (let y = currentYear; y >= currentYear - 5; y--) {
    years.push(y);
  }
  return years;
});

const handleExportDownload = async () => {
  exportLoading.value = true;
  try {
    const params = {
      filterType: exportFilterType.value,
      year: exportYear.value,
      month: exportMonth.value,
      quarter: exportQuarter.value,
      week: exportWeek.value,
    };
    const blob = await downloadCustomersExport(params);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    
    let filename = "subscribers_export";
    if (exportFilterType.value === "weekly") {
      filename += `_${exportYear.value}_${String(exportMonth.value).padStart(2, "0")}_W${exportWeek.value}`;
    } else if (exportFilterType.value === "month") {
      filename += `_${exportYear.value}_${String(exportMonth.value).padStart(2, "0")}`;
    } else if (exportFilterType.value === "quarter") {
      filename += `_${exportYear.value}_Q${exportQuarter.value}`;
    } else if (exportFilterType.value === "year") {
      filename += `_${exportYear.value}`;
    } else {
      filename += "_all";
    }
    
    const currentDate = new Date().toISOString().split('T')[0];
    filename += `_${currentDate}.xlsx`;
    
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    
    toast.success("Data customer berhasil diexport");
    showExportModal.value = false;
  } catch (error) {
    toast.error("Gagal melakukan export: " + error.message);
  } finally {
    exportLoading.value = false;
  }
};
const showConfirmModal = ref(false);
const confirmTitle = ref("");
const confirmMessage = ref("");
const onConfirmAction = ref(null);

function triggerConfirm(title, message, action) {
  confirmTitle.value = title;
  confirmMessage.value = message;
  onConfirmAction.value = action;
  showConfirmModal.value = true;
}

function executeConfirm() {
  if (onConfirmAction.value) {
    onConfirmAction.value();
  }
  showConfirmModal.value = false;
}
const selectedCustomer = ref(null);
const showDetailPanel = ref(false);

function openDetail(c) {
  selectedCustomer.value = c;
  showDetailPanel.value = true;
}
const packagesList = ref([]);
const cumulativeGrowthData = ref({ labels: [], data: [] });
const statusDist = ref({ active: 0, isolir: 0, activePct: 0, isolirPct: 0 });
const growthFilter = ref("1Y");

const loading = ref(false);
const overviewData = ref(null);

const newCustomer = ref({
  custId: "",
  name: "",
  packageId: "",
  address: "",
  agent: "",
  registrationDate: new Date().toISOString().slice(0, 10),
  activationDate: new Date().toISOString().slice(0, 10),
});

async function submitRegistration() {
  if (!newCustomer.value.name) return;

  try {
    const payload = {
      custId: newCustomer.value.custId.trim() || null,
      nama: newCustomer.value.name,
      status: "ACTIVE",
      pkg: {
        id: parseInt(newCustomer.value.packageId),
      },
      agent: {
        nama: newCustomer.value.agent || "Direct",
      },
      address: {
        alamat: newCustomer.value.address || "Direct / Unspecified",
      },
      tanggalRegistrasi: newCustomer.value.registrationDate || null,
      tanggalAktivasi: newCustomer.value.activationDate || null,
    };

    await createCustomer(payload);

    // Refresh customer list and overview
    await fetchCustomers();

    showRegisterModal.value = false;
    toast.success("Pelanggan berhasil ditambahkan");
    newCustomer.value = {
      custId: "",
      name: "",
      packageId: packagesList.value[0]?.id || "",
      address: "",
      agent: "",
      registrationDate: new Date().toISOString().slice(0, 10),
      activationDate: new Date().toISOString().slice(0, 10),
    };
  } catch (error) {
    toast.error("Gagal mendaftarkan pelanggan: " + error.message);
  }
}

async function toggleStatus(c) {
  try {
    const nextStatus = c.status === "ACTIVE" ? "ISOLIR" : "ACTIVE";
    await updateCustomerStatus(c.dbId, nextStatus);
    await fetchCustomers();
    activeMenu.value = null;
    toast.success(`Status pelanggan diubah menjadi ${nextStatus}`);
  } catch (error) {
    toast.error("Gagal mengubah status pelanggan: " + error.message);
  }
}

async function removeCustomer(c) {
  triggerConfirm(
    "Hapus Pelanggan",
    `Apakah Anda yakin ingin menghapus pelanggan ${c.org}? Tindakan ini tidak dapat dibatalkan.`,
    async () => {
      try {
        await deleteCustomer(c.dbId);
        await fetchCustomers();
        activeMenu.value = null;
        toast.success("Pelanggan berhasil dihapus");
      } catch (error) {
        toast.error("Gagal menghapus pelanggan: " + error.message);
      }
    },
  );
}

const rowColors = [
  "#4A90D9",
  "#27AE60",
  "#F5A623",
  "#8B5CF6",
  "#E74C3C",
  "#00B4A6",
];
const customers = ref([]);

const anchorDate = computed(() => {
  let anchor = new Date(0);
  customers.value.forEach((c) => {
    const d = parseDateValue(c.registrationRaw || c.activationRaw);
    if (d && d > anchor) {
      anchor = d;
    }
  });
  if (anchor.getTime() === 0) anchor = new Date();
  return anchor;
});

const kpiData = computed(() => {
  const totalCount = customers.value.length;
  const activeCount = customers.value.filter(
    (c) => c.status === "ACTIVE",
  ).length;

  let newThisMonth = 0;
  let growth = 0;
  let growthText = "0%";
  let growthSign = 1;

  const overview = overviewData.value;
  if (overview && overview.summary) {
    newThisMonth = overview.summary.monthlyNewCustomers || 0;
    growth = overview.summary.monthlyCustomerGrowthPct || 0;
    growthSign = growth >= 0 ? 1 : -1;
    growthText = (growth > 0 ? "+" : "") + growth + "%";
  } else {
    // Fallback if no overview
    newThisMonth = customers.value.filter((c) =>
      isSameMonth(c.registrationRaw, anchorDate.value),
    ).length;

    const prevTotal = totalCount - newThisMonth;
    if (prevTotal > 0) {
      growth = (newThisMonth / prevTotal) * 100;
    } else if (newThisMonth > 0) {
      growth = 100;
    }
    growthSign = growth >= 0 ? 1 : -1;
    growthText = (growth >= 0 ? "+" : "") + growth.toFixed(1) + "%";
  }

  const activationReadyCount = customers.value.filter(
    (c) => !c.activationRaw && c.status !== "ISOLIR",
  ).length;

  const saturation =
    totalCount > 0 ? Math.round((activeCount / totalCount) * 100) : 0;

  return {
    total: new Intl.NumberFormat("id-ID").format(totalCount),
    growthText,
    growthSign,
    active: new Intl.NumberFormat("id-ID").format(activeCount),
    saturation: saturation + "%",
    newThisMonth: new Intl.NumberFormat("id-ID").format(newThisMonth),
    activationReady: totalCount > 0 ? activationReadyCount : 0,
  };
});

const filteredCustomers = computed(() =>
  customers.value.filter((c) => {
    const matchesStatus =
      !statusFilter.value ||
      (statusFilter.value === "NEW"
        ? isSameMonth(c.registrationRaw, anchorDate.value)
        : c.status === statusFilter.value);
    const matchesService =
      !serviceFilter.value || c.packageName === serviceFilter.value;
    const matchesSearch =
      !searchQuery.value ||
      c.org.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      String(c.id).toLowerCase().includes(searchQuery.value.toLowerCase());

    return matchesStatus && matchesService && matchesSearch;
  }),
);

const sortedFilteredCustomers = computed(() => {
  const arr = [...filteredCustomers.value];
  if (!sortKey.value) return arr;
  return arr.sort((a, b) => {
    let av = a[sortKey.value];
    let bv = b[sortKey.value];
    // numeric id sort
    if (sortKey.value === "id") {
      av = Number(av);
      bv = Number(bv);
    }
    if (typeof av === "string") av = av.toLowerCase();
    if (typeof bv === "string") bv = bv.toLowerCase();
    if (av < bv) return sortDir.value === "asc" ? -1 : 1;
    if (av > bv) return sortDir.value === "asc" ? 1 : -1;
    return 0;
  });
});

const paginatedCustomers = computed(() => {
  const start = (page.value - 1) * itemsPerPage.value;
  return sortedFilteredCustomers.value.slice(start, start + itemsPerPage.value);
});

const totalPages = computed(
  () =>
    Math.ceil(sortedFilteredCustomers.value.length / itemsPerPage.value) || 1,
);
const visiblePages = computed(() => {
  const current = page.value;
  const last = totalPages.value;
  const delta = 1;
  const left = current - delta;
  const right = current + delta + 1;
  const range = [];
  const rangeWithDots = [];
  let l;

  for (let i = 1; i <= last; i++) {
    if (i === 1 || i === last || (i >= left && i < right)) {
      range.push(i);
    }
  }

  for (let i = 0; i < range.length; i++) {
    if (l) {
      if (range[i] - l === 2) {
        rangeWithDots.push(l + 1);
      } else if (range[i] - l !== 1) {
        rangeWithDots.push("...");
      }
    }
    rangeWithDots.push(range[i]);
    l = range[i];
  }

  return rangeWithDots;
});
const rangeStart = computed(() =>
  sortedFilteredCustomers.value.length
    ? (page.value - 1) * itemsPerPage.value + 1
    : 0,
);
const rangeEnd = computed(() =>
  Math.min(
    page.value * itemsPerPage.value,
    sortedFilteredCustomers.value.length,
  ),
);

// Reset page when filtering
watch([statusFilter, serviceFilter, searchQuery, itemsPerPage], () => {
  page.value = 1;
});

function makeInitials(value) {
  const words = String(value || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  return (
    words
      .slice(0, 2)
      .map((word) => word[0])
      .join("")
      .toUpperCase() || "NA"
  );
}

function resetFilters() {
  isResetting.value = true;
  statusFilter.value = "";
  serviceFilter.value = "";
  searchQuery.value = "";
  page.value = 1;
  sortKey.value = "";
  sortDir.value = "asc";
  toast.success("Semua filter, pengurutan, dan pencarian berhasil direset");
  setTimeout(() => {
    isResetting.value = false;
  }, 600);
}

function formatDate(value) {
  if (!value) return "-";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("id-ID", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

function parseDateValue(value) {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function isSameMonth(value, date) {
  const parsed = parseDateValue(value);
  if (!parsed) return false;
  return (
    parsed.getFullYear() === date.getFullYear() &&
    parsed.getMonth() === date.getMonth()
  );
}

function formatAddress(address) {
  if (!address) return "-";

  return (
    [address.alamat, address.kelurahan, address.kecamatan, address.kota]
      .filter(Boolean)
      .join(", ") || "-"
  );
}

function formatService(customer) {
  if (!customer.pkg) return "-";
  return customer.pkg.name || "-";
}

const mapCustomer = (customer, index) => {
  const name =
    customer.org ||
    customer.nama ||
    customer.name ||
    customer.custId ||
    `Customer ${customer.id}`;
  const agentName = customer.agent?.nama || "-";

  return {
    dbId: customer.id,
    id: customer.custId || customer.id,
    custId: customer.custId,
    org: name,
    initials: makeInitials(name),
    location: formatAddress(customer.address),
    service: formatService(customer),
    packageName: customer.pkg?.name || "-",
    regDate: formatDate(customer.tanggalRegistrasi),
    activationDate: formatDate(customer.tanggalAktivasi),
    registrationRaw: customer.tanggalRegistrasi,
    activationRaw: customer.tanggalAktivasi,
    renewal: formatDate(customer.tanggalAktivasi || customer.tanggalRegistrasi),
    status: customer.status || "ACTIVE",
    agent: agentName,
    agentInitial: makeInitials(agentName),
    color: rowColors[index % rowColors.length],
  };
};

const fetchCustomers = async () => {
  loading.value = true;
  try {
    const [data, pkgs, overview] = await Promise.all([
      getCustomers(),
      getPackages(),
      getDashboardOverview().catch(() => ({})),
    ]);

    if (overview) {
      if (overview.cumulativeGrowth)
        cumulativeGrowthData.value = overview.cumulativeGrowth;
      if (overview.statusDistribution)
        statusDist.value = overview.statusDistribution;
      overviewData.value = overview;
    }

    customers.value = data.map(mapCustomer);
    packagesList.value = pkgs || [];

    if (!newCustomer.value.packageId && pkgs.length > 0) {
      newCustomer.value.packageId = pkgs[0].id;
    }
  } catch (error) {
    console.error("Gagal memuat pelanggan:", error);
  } finally {
    loading.value = false;
  }
};

const filteredCumulativeChartData = computed(() => {
  let sliceCount = cumulativeGrowthData.value.labels.length;
  if (growthFilter.value === "6M") sliceCount = 6;
  else if (growthFilter.value === "1Y") sliceCount = 12;

  const startIdx = Math.max(
    0,
    cumulativeGrowthData.value.labels.length - sliceCount,
  );
  return {
    labels: cumulativeGrowthData.value.labels.slice(startIdx),
    datasets: [
      {
        label: "Subscribers",
        data: cumulativeGrowthData.value.data.slice(startIdx),
        borderColor: "#0D1340",
        backgroundColor: "rgba(13, 19, 64, 0.05)",
        borderWidth: 2.5,
        pointBackgroundColor: "#0D1340",
        pointBorderColor: "#fff",
        pointBorderWidth: 2,
        pointRadius: 3,
        fill: true,
        tension: 0.4,
      },
    ],
  };
});

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: { font: { family: "Inter", size: 10 }, color: "#9BA3BF" },
    },
    y: { display: false },
  },
};

const handleTopbarSearch = (e) => {
  searchQuery.value = e.detail || "";
};

const capacityInsight = computed(() => {
  const total = customers.value.length;
  const isolir = customers.value.filter((c) => c.status === "ISOLIR").length;
  const active = customers.value.filter((c) => c.status === "ACTIVE").length;
  const missingActivation = customers.value.filter(
    (c) => !c.activationRaw,
  ).length;
  const activeRate = total ? Math.round((active / total) * 100) : 0;

  if (isolir > 0) {
    return {
      title: "Customer Health Follow-up Report",
      description: `${isolir} pelanggan sedang isolir dari ${total} total subscriber. Prioritaskan follow-up pembayaran atau reaktivasi supaya active rate tetap di atas target.`,
      action: "Review Isolir",
    };
  }
  if (missingActivation > 0) {
    return {
      title: "Activation Data Completion Report",
      description: `${missingActivation} pelanggan belum punya tanggal aktivasi. Lengkapi tanggalnya agar dashboard revenue dan unit bulanan makin akurat.`,
      action: "Review Dates",
    };
  }
  return {
    title: "Customer Base Health Report",
    description: `Active rate berada di ${activeRate}% dengan ${total} pelanggan tersinkron. Data registrasi dan aktivasi siap dipakai untuk analytics bulanan.`,
    action: "View Active Base",
  };
});

const queue = computed(() => {
  const items = [];
  const isolir = customers.value.filter((c) => c.status === "ISOLIR").length;
  const missingActivation = customers.value.filter(
    (c) => !c.activationRaw,
  ).length;
  const directAccounts = customers.value.filter(
    (c) => c.agent === "-" || c.agent === "Direct",
  ).length;

  if (isolir > 0) {
    items.push({
      name: "Isolir Follow-up",
      time: `${isolir} customers`,
      id: "ISO",
      color: "var(--red-warn)",
    });
  }
  if (missingActivation > 0) {
    items.push({
      name: "Activation Date",
      time: `${missingActivation} missing`,
      id: "DAT",
      color: "var(--gold)",
    });
  }
  if (directAccounts > 0) {
    items.push({
      name: "Assign Agent",
      time: `${directAccounts} direct`,
      id: "AGN",
      color: "var(--teal)",
    });
  }
  return items.slice(0, 3);
});

function focusHealthQueue() {
  if (customers.value.some((c) => c.status === "ISOLIR")) {
    statusFilter.value = "ISOLIR";
  } else {
    resetFilters();
  }
}

function goToTickets() {
  router.push("/support");
}

onMounted(() => {
  window.addEventListener("topbar-search", handleTopbarSearch);
  fetchCustomers();
});

onUnmounted(() => {
  window.removeEventListener("topbar-search", handleTopbarSearch);
});
</script>

<style scoped>
.customers {
  display: flex;
  flex-direction: column;
  gap: 18px;
  max-width: 100%;
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
  font-size: 28px;
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
  cursor: pointer;
  white-space: nowrap;
}
.btn svg {
  width: 15px;
  height: 15px;
}
.btn--primary {
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0;
}
.btn--primary:hover {
  background: var(--navy-mid);
}
.btn--ghost {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-1);
  transition: all 0.15s;
}
.btn--ghost:hover {
  background: var(--bg);
  border-color: var(--text-3);
}
.page-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* KPIs */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  width: 100%;
}
.kpi-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.kpi-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0;
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
  background: #eef2ff;
  color: var(--navy);
}
.kpi-icon--teal {
  background: #e0f8f6;
  color: var(--teal);
}
.kpi-icon--green {
  background: #e8f8ef;
  color: var(--green-ok);
}
.kpi-icon svg {
  width: 15px;
  height: 15px;
}
.kpi-value {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 0;
  line-height: 1;
}
.kpi-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.badge-inline {
  font-size: 10px;
  font-weight: 700;
  font-family: var(--font-display);
  padding: 2px 7px;
  border-radius: 99px;
}
.badge-inline--green {
  background: #e8f8ef;
  color: var(--green-ok);
}
.badge-inline--teal {
  background: #e0f8f6;
  color: var(--teal);
}
.badge-inline--gold {
  background: #fef3e0;
  color: var(--gold);
}
.kpi-sub {
  font-size: 11px;
  color: var(--text-3);
}

/* Table Card */
.table-card {
  overflow: hidden;
  padding: 0;
}
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border);
  gap: 10px;
  flex-wrap: wrap;
}
.filters-left {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.filters-right {
  display: flex;
  gap: 8px;
}

.filter-select {
  position: relative;
  display: flex;
  align-items: center;
}
.filter-select select {
  appearance: none;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 7px 30px 7px 12px;
  font-size: 12px;
  font-family: var(--font-body);
  color: var(--text-1);
  cursor: pointer;
  outline: none;
}
.filter-select svg {
  position: absolute;
  right: 8px;
  width: 14px;
  height: 14px;
  color: var(--text-3);
  pointer-events: none;
}

.search-input {
  position: relative;
  display: flex;
  align-items: center;
}
.search-icon {
  position: absolute;
  left: 10px;
  width: 14px;
  height: 14px;
  color: var(--text-3);
  pointer-events: none;
}
.form-input-sm {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 7px 12px 7px 32px;
  font-size: 12px;
  color: var(--text-1);
  outline: none;
  width: 220px;
  transition: border-color 0.2s;
}
.form-input-sm:focus {
  border-color: var(--navy-mid);
}

.btn-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: none;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-2);
  cursor: pointer;
  font-family: var(--font-body);
}
.btn-filter svg {
  width: 14px;
  height: 14px;
}
.btn-filter:hover {
  background: var(--bg);
}
.rotate-icon {
  animation: spin 0.6s ease-in-out;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 80px;
  margin-bottom: -80px;
}
.cust-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}
.cust-table th {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
  text-align: left;
  padding: 10px 16px;
  border-bottom: 1px solid var(--border);
  background: var(--surface-2);
  white-space: nowrap;
}
.cust-table td {
  padding: var(--table-row-padding, 14px 16px);
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
}
.cust-row:last-child td {
  border-bottom: none;
}
.cust-row:hover td {
  background: var(--bg);
}

.cust-id-badge {
  display: inline-block;
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 6px;
}

.org-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.org-avatar {
  width: 34px;
  height: 34px;
  border-radius: var(--r-sm);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.org-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}
.org-loc {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}

.svc-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.svc-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--navy);
}
.svc-renewal {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
  white-space: nowrap;
}

.agent-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.agent-avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--bg);
  border: 1px solid var(--border);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 10px;
  color: var(--text-2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.agent-name-sm {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-1);
}

.action-cell {
  position: relative;
}
.action-btn {
  background: none;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  cursor: pointer;
}
.action-btn:hover {
  background: var(--bg);
  color: var(--text-1);
}
.action-btn svg {
  width: 16px;
  height: 16px;
}

.action-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 4px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-lg);
  z-index: 50;
  min-width: 135px;
  padding: 6px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.action-dropdown button {
  background: none;
  border: none;
  padding: 8px 12px;
  font-size: 12px;
  text-align: left;
  cursor: pointer;
  color: var(--text-2);
  border-radius: 4px;
}
.action-dropdown button:hover {
  background: var(--bg);
  color: var(--text-1);
}
.action-dropdown button.danger {
  color: var(--red-warn);
}
.action-dropdown button.danger:hover {
  background: #fdeceb;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  flex-wrap: wrap;
  gap: 10px;
}
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
  color: var(--text-1);
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
.pagination {
  display: flex;
  gap: 4px;
}
.page-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
  font-family: var(--font-display);
  transition: all 0.15s;
}
.page-btn svg {
  width: 14px;
  height: 14px;
}
.page-btn:hover:not(:disabled) {
  background: var(--bg);
}
.page-btn:disabled {
  opacity: 0.3;
  cursor: default;
}
.page-btn--active {
  background: var(--navy);
  color: #fff;
  border-color: var(--navy);
}

/* Bottom */
.bottom-row {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
}

.insight-card {
  background: var(--navy);
  border-color: transparent;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.insight-tag {
  display: inline-block;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.15);
  padding: 3px 10px;
  border-radius: 99px;
  font-family: var(--font-display);
  width: fit-content;
}
.insight-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  line-height: 1.3;
}
.insight-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.7;
}
.btn-analyze {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--gold);
  color: var(--navy);
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
  padding: 11px 20px;
  border-radius: var(--r-sm);
  border: none;
  cursor: pointer;
  width: fit-content;
  margin-top: 4px;
}
.btn-analyze svg {
  width: 16px;
  height: 16px;
}
.btn-analyze:hover {
  background: var(--gold-light);
}

.queue-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.queue-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.queue-title {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
  text-transform: uppercase;
  letter-spacing: 0;
}
.queue-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.queue-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.queue-bar {
  width: 4px;
  height: 36px;
  border-radius: 99px;
  flex-shrink: 0;
}
.queue-info {
  flex: 1;
}
.queue-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}
.queue-time {
  font-size: 10px;
  color: var(--text-3);
  text-transform: uppercase;
  letter-spacing: 0;
  font-family: var(--font-display);
}
.queue-id {
  font-family: var(--font-display);
  font-size: 11px;
  color: var(--text-3);
}
.all-tickets-btn {
  background: none;
  border: 1px solid var(--border);
  padding: 10px;
  border-radius: var(--r-sm);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
  font-family: var(--font-display);
  cursor: pointer;
  letter-spacing: 0;
}
.all-tickets-btn:hover {
  background: var(--bg);
}

/* Modals */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal-card {
  background: var(--surface);
  border-radius: var(--r-md);
  width: 100%;
  max-width: 400px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}
.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.modal-header h2 {
  font-family: var(--font-display);
  font-size: 16px;
  color: var(--text-1);
}
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-3);
}
.modal-body {
  padding: 20px;
  font-size: 14px;
  color: var(--text-2);
  line-height: 1.5;
}
.modal-body b {
  color: var(--navy);
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.modal-footer {
  padding: 14px 20px;
  background: var(--surface-2);
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .kpi-row {
    grid-template-columns: 1fr 1fr;
  }
  .bottom-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 640px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
  .form-grid {
    grid-template-columns: 1fr;
  }
}

.td-empty {
  padding: 40px !important;
  text-align: center;
}
.empty-state {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  background: rgba(13, 19, 64, 0.03);
  border: 1px dashed var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
}
.empty-glow {
  position: absolute;
  width: 150px;
  height: 150px;
  background: radial-gradient(
    circle,
    rgba(0, 180, 166, 0.1) 0%,
    transparent 70%
  );
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  filter: blur(10px);
}
.empty-icon-wrap {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: rgba(13, 19, 64, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--navy);
  margin-bottom: 12px;
}
.empty-state h3 {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 6px;
}
.empty-state p {
  font-size: 12px;
  color: var(--text-3);
  max-width: 320px;
  line-height: 1.5;
  margin: 0;
}
.pulsing-svg {
  width: 24px;
  height: 24px;
  animation: pulseNode 2s infinite ease-in-out;
}
@keyframes pulseNode {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.11);
    opacity: 1;
    color: var(--teal);
  }
}
.time-filters {
  display: flex;
  gap: 4px;
  background: rgba(13, 19, 64, 0.03);
  padding: 4px;
  border-radius: 8px;
}
.time-filter-btn {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-3);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}
.time-filter-btn:hover {
  color: var(--navy);
}
.time-filter-btn.active {
  background: #fff;
  color: var(--navy);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* =========== SLIDE-OVER PANEL =========== */
.slideover-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  backdrop-filter: blur(2px);
  z-index: 900;
  display: flex;
  justify-content: flex-end;
}
.slideover-panel {
  width: 420px;
  max-width: 100vw;
  height: 100%;
  background: var(--surface);
  box-shadow: var(--shadow-xl);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.slideover-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(135deg, var(--navy) 0%, var(--navy-mid) 100%);
}
.slideover-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--r-md);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
.slideover-title-group {
  flex: 1;
  min-width: 0;
}
.slideover-name {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.slideover-id {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  margin-top: 3px;
  font-family: var(--font-display);
}
.slideover-close {
  width: 36px;
  height: 36px;
  border-radius: var(--r-sm);
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
  flex-shrink: 0;
}
.slideover-close:hover {
  background: rgba(255, 255, 255, 0.2);
}
.slideover-close svg {
  width: 18px;
  height: 18px;
}
.slideover-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.slideover-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: var(--surface-2);
  border-radius: var(--r-md);
  border: 1px solid var(--border);
}
.btn-toggle-status {
  background: none;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2);
  cursor: pointer;
  font-family: var(--font-display);
  transition: all 0.15s;
}
.btn-toggle-status:hover {
  background: var(--navy);
  color: #fff;
  border-color: var(--navy);
}
.slideover-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.slideover-section-title {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-3);
  font-family: var(--font-display);
}
.slideover-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.slideover-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px;
  background: var(--surface-2);
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
}
.slideover-info-label {
  font-size: 10px;
  color: var(--text-3);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.slideover-info-val {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  font-family: var(--font-display);
}
.slideover-location {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px;
  background: var(--surface-2);
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.5;
}
.slideover-location svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: var(--text-3);
  margin-top: 2px;
}
.slideover-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.slideover-action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: var(--surface);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  cursor: pointer;
  font-family: var(--font-display);
  transition: all 0.15s;
  text-align: left;
  width: 100%;
}
.slideover-action-btn svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}
.slideover-action-btn:hover {
  background: var(--surface-2);
  border-color: var(--border-2);
}
.slideover-action-btn--danger {
  color: var(--red-warn);
}
.slideover-action-btn--danger:hover {
  background: var(--red-bg);
  border-color: var(--red-warn);
}
.slideover-enter-active {
  transition: opacity 0.35s ease;
}
.slideover-leave-active {
  transition: opacity 0.25s ease;
}
.slideover-enter-from,
.slideover-leave-to {
  opacity: 0;
}

.page-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.btn--ghost {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-1);
}
.btn--ghost:hover {
  background: var(--bg);
  border-color: var(--text-3);
}

.modal-card--confirm {
  max-width: 440px !important;
  border-top: 4px solid var(--red-warn, #ef4444) !important;
}
.confirm-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.confirm-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fdeceb;
  color: var(--red-warn, #ef4444);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.confirm-icon svg {
  width: 20px;
  height: 20px;
}
.confirm-text {
  font-size: 14.5px;
  color: var(--text-2);
  line-height: 1.5;
  margin: 0;
}
.btn--danger {
  background: var(--red-warn, #ef4444) !important;
  color: #fff !important;
  border: none !important;
}
.btn--danger:hover {
  background: #dc2626 !important;
  box-shadow: 0 0 12px rgba(239, 68, 68, 0.4) !important;
}

/* Responsive Overrides */
@media (max-width: 1024px) {
  .charts-row {
    grid-template-columns: 1fr !important;
  }
  .bottom-row {
    grid-template-columns: 1fr !important;
  }
}
@media (max-width: 768px) {
  .kpi-row {
    grid-template-columns: 1fr !important;
  }
  .table-toolbar {
    flex-direction: column !important;
    align-items: flex-start !important;
    gap: 12px !important;
  }
  .filters-left, .filters-right {
    width: 100% !important;
    justify-content: flex-start !important;
  }
  .filters-right {
    justify-content: space-between !important;
  }
}
@media (max-width: 640px) {
  .page-header {
    flex-direction: column !important;
    align-items: flex-start !important;
    gap: 12px !important;
  }
  .page-actions {
    width: 100% !important;
  }
  .page-actions .btn {
    width: 100% !important;
    justify-content: center !important;
  }
  .table-footer {
    flex-direction: column !important;
    gap: 12px !important;
    align-items: center !important;
    text-align: center !important;
  }
}

/* Print Styles */
@media print {
  .page-header,
  .page-actions,
  .table-toolbar,
  .table-footer,
  .kpi-row,
  .charts-row,
  .bottom-row,
  .slideover-backdrop,
  .modal-overlay,
  .btn {
    display: none !important;
  }
  .customers {
    padding: 0 !important;
  }
  .card.table-card {
    box-shadow: none !important;
    border: none !important;
  }
  table {
    width: 100% !important;
    font-size: 11px !important;
  }
  thead {
    background: #f3f4f6 !important;
    -webkit-print-color-adjust: exact;
  }
}
</style>
