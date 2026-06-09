<template>
  <div class="dashboard page-fade-in">


    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Network Overview</h1>
        <p class="page-sub">
          Real-time enterprise connectivity metrics and distribution.
        </p>
      </div>
      <div class="page-actions">
        <div class="period-select" style="margin-right: 8px">
          <input
            type="month"
            v-model="selectedMonth"
            @change="fetchData"
            class="form-input"
            style="padding: 8px 12px; height: auto"
          />
        </div>

        <button class="btn btn--primary" @click="router.push('/customers')">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span class="btn-text">Register Customer</span>
        </button>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="kpi-row" v-if="loading">
      <div class="card kpi-card" v-for="i in 3" :key="i">
        <div class="kpi-top">
          <div class="skeleton skeleton-text--sm" style="width: 100px"></div>
        </div>
        <div class="kpi-body">
          <div style="flex: 1">
            <div
              class="skeleton skeleton-text--lg"
              style="margin-top: 12px; width: 120px; height: 38px"
            ></div>
            <div
              class="skeleton skeleton-text--sm"
              style="margin-top: 8px; width: 80px"
            ></div>
          </div>
          <div
            class="skeleton"
            style="width: 100px; height: 35px; border-radius: 6px"
          ></div>
        </div>
      </div>
    </div>
    <div class="kpi-row" v-else>
      <!-- Card 1: Total Customers -->
      <div class="card kpi-card">
        <div class="kpi-top">
          <div class="kpi-pill">
            <span class="kpi-pill-icon kpi-pill-icon--blue">
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
            </span>
            <span class="kpi-label">Total Customers</span>
          </div>
        </div>
        <div class="kpi-body">
          <div class="kpi-value-section">
            <div class="kpi-value">{{ totalCustomersDisplay }}</div>
            <div class="kpi-trend">
              <span
                class="trend-badge"
                :class="
                  monthlyCustomerGrowthPct >= 0
                    ? 'trend-badge--up'
                    : 'trend-badge--down'
                "
              >
                <svg
                  v-if="monthlyCustomerGrowthPct >= 0"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  stroke-linecap="round"
                >
                  <polyline points="18 15 12 9 6 15" />
                </svg>
                <svg
                  v-else
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  stroke-linecap="round"
                >
                  <polyline points="6 9 12 15 18 9" />
                </svg>
                {{ signedPercent(monthlyCustomerGrowthPct) }}
              </span>
              <span class="kpi-meta-text">vs last month</span>
            </div>
          </div>
          <div class="kpi-sparkline" v-if="customerSparklinePath">
            <svg class="sparkline-svg" viewBox="0 0 100 35">
              <defs>
                <linearGradient
                  id="sparkline-grad-customers"
                  x1="0"
                  y1="0"
                  x2="0"
                  y2="1"
                >
                  <stop
                    offset="0%"
                    :stop-color="customerSparklineColor"
                    stop-opacity="0.25"
                  />
                  <stop
                    offset="100%"
                    :stop-color="customerSparklineColor"
                    stop-opacity="0"
                  />
                </linearGradient>
              </defs>
              <path
                :d="customerSparklineFillPath"
                fill="url(#sparkline-grad-customers)"
                stroke="none"
              />
              <path
                :d="customerSparklinePath"
                fill="none"
                :stroke="customerSparklineColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
        </div>
      </div>

      <!-- Card 2: Active Connections -->
      <div class="card kpi-card">
        <div class="kpi-top">
          <div class="kpi-pill">
            <span class="kpi-pill-icon kpi-pill-icon--green">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
              </svg>
            </span>
            <span class="kpi-label">Customer Active</span>
          </div>
        </div>
        <div class="kpi-body">
          <div class="kpi-value-section">
            <div class="kpi-value">{{ totalActiveDisplay }}</div>
            <div class="kpi-trend">
              <span class="trend-badge trend-badge--up">
                {{ activeRate }}%
              </span>
              <span class="kpi-meta-text">Active Rate</span>
            </div>
          </div>
          <div class="kpi-sparkline" v-if="activeSparklinePath">
            <svg class="sparkline-svg" viewBox="0 0 100 35">
              <defs>
                <linearGradient
                  id="sparkline-grad-active"
                  x1="0"
                  y1="0"
                  x2="0"
                  y2="1"
                >
                  <stop
                    offset="0%"
                    stop-color="var(--green-ok)"
                    stop-opacity="0.25"
                  />
                  <stop
                    offset="100%"
                    stop-color="var(--green-ok)"
                    stop-opacity="0"
                  />
                </linearGradient>
              </defs>
              <path
                :d="activeSparklineFillPath"
                fill="url(#sparkline-grad-active)"
                stroke="none"
              />
              <path
                :d="activeSparklinePath"
                fill="none"
                stroke="var(--green-ok)"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
        </div>
      </div>

      <!-- Card 3: Inactive / Pending -->
      <div class="card kpi-card">
        <div class="kpi-top">
          <div class="kpi-pill">
            <span class="kpi-pill-icon kpi-pill-icon--red">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
                stroke-linecap="round"
              >
                <circle cx="12" cy="12" r="10" />
                <line x1="15" y1="9" x2="9" y2="15" />
                <line x1="9" y1="9" x2="15" y2="15" />
              </svg>
            </span>
            <span class="kpi-label">Customer Inactive</span>
          </div>
        </div>
        <div class="kpi-body">
          <div class="kpi-value-section">
            <div class="kpi-value" style="color: var(--gold)">
              {{ totalIsolirDisplay }}
            </div>
          </div>
          <div class="kpi-sparkline" v-if="isolirSparklinePath">
            <svg class="sparkline-svg" viewBox="0 0 100 35">
              <defs>
                <linearGradient
                  id="sparkline-grad-isolir"
                  x1="0"
                  y1="0"
                  x2="0"
                  y2="1"
                >
                  <stop
                    offset="0%"
                    stop-color="var(--gold)"
                    stop-opacity="0.25"
                  />
                  <stop
                    offset="100%"
                    stop-color="var(--gold)"
                    stop-opacity="0"
                  />
                </linearGradient>
              </defs>
              <path
                :d="isolirSparklineFillPath"
                fill="url(#sparkline-grad-isolir)"
                stroke="none"
              />
              <path
                :d="isolirSparklinePath"
                fill="none"
                stroke="var(--gold)"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Metrics Row -->
    <div class="metrics-row" v-if="!loading">
      <!-- Card 1: Isolir Rate Progress -->
      <div class="card metric-card">
        <div class="metric-top">
          <div class="metric-icon metric-icon--gold">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <path
                d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"
              />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          </div>
        </div>
        <div class="metric-label">Isolir Ratio Analysis</div>
        <div class="metric-value" style="color: var(--gold)">
          {{ isolirRate }}%
        </div>
        <div class="isolir-bar">
          <div
            class="isolir-bar__fill"
            :style="{ width: isolirRate + '%' }"
          ></div>
        </div>
      </div>

      <!-- Card 2: Profit Recorded (Sparkline Layout) -->
      <div class="card metric-card metric-card--spark">
        <div class="kpi-top">
          <div class="kpi-pill">
            <span class="kpi-pill-icon kpi-pill-icon--teal">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
                stroke-linecap="round"
              >
                <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
                <polyline points="17 6 23 6 23 12" />
              </svg>
            </span>
            <span class="kpi-label">Profit Recorded</span>
          </div>
        </div>
        <div class="kpi-body">
          <div class="kpi-value-section">
            <div class="kpi-value" style="font-size: 26px">
              {{ profitRecordedDisplay }}
            </div>
            <div class="kpi-trend">
              <span
                class="trend-badge"
                :class="
                  monthlyProfitDelta >= 0
                    ? 'trend-badge--up'
                    : 'trend-badge--down'
                "
              >
                <svg
                  v-if="monthlyProfitDelta >= 0"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  stroke-linecap="round"
                >
                  <polyline points="18 15 12 9 6 15" />
                </svg>
                <svg
                  v-else
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  stroke-linecap="round"
                >
                  <polyline points="6 9 12 15 18 9" />
                </svg>
                {{ profitDeltaDisplay }}
              </span>
              <span class="kpi-meta-text">vs prev month</span>
            </div>
          </div>
          <div class="kpi-sparkline" v-if="profitSparklinePath">
            <svg class="sparkline-svg" viewBox="0 0 100 35">
              <defs>
                <linearGradient
                  id="sparkline-grad-profit"
                  x1="0"
                  y1="0"
                  x2="0"
                  y2="1"
                >
                  <stop
                    offset="0%"
                    :stop-color="profitSparklineColor"
                    stop-opacity="0.25"
                  />
                  <stop
                    offset="100%"
                    :stop-color="profitSparklineColor"
                    stop-opacity="0"
                  />
                </linearGradient>
              </defs>
              <path
                :d="profitSparklineFillPath"
                fill="url(#sparkline-grad-profit)"
                stroke="none"
              />
              <path
                :d="profitSparklinePath"
                fill="none"
                :stroke="profitSparklineColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>
    <!-- end v-else kpi-row -->

    <!-- ===== ROW: Sales Performance + Status Donut ===== -->
    <div class="charts-row">
      <div class="card chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-title">Monthly Registrations & Revenue</div>
            <div class="chart-sub">
              {{
                activeSalesTab === "Revenue"
                  ? "Revenue growth across all premium packages"
                  : "Units sold (Registrations) across all premium packages"
              }}
            </div>
          </div>
          <div
            class="chart-tabs-wrapper"
            style="display: flex; gap: 12px; align-items: center"
          >
            <div class="chart-tabs" style="margin: 0">
              <button
                class="ctab"
                :class="{ 'ctab--active': salesFilter === '6M' }"
                @click="salesFilter = '6M'"
              >
                6M
              </button>
              <button
                class="ctab"
                :class="{ 'ctab--active': salesFilter === '1Y' }"
                @click="salesFilter = '1Y'"
              >
                1Y
              </button>
              <button
                class="ctab"
                :class="{ 'ctab--active': salesFilter === 'All' }"
                @click="salesFilter = 'All'"
              >
                All
              </button>
            </div>
            <div class="chart-tabs" style="margin: 0">
              <button
                class="ctab"
                :class="{ 'ctab--active': activeSalesTab === 'Revenue' }"
                @click="activeSalesTab = 'Revenue'"
              >
                Revenue
              </button>
              <button
                class="ctab"
                :class="{ 'ctab--active': activeSalesTab === 'Units' }"
                @click="activeSalesTab = 'Units'"
              >
                Units
              </button>
            </div>
          </div>
        </div>
        <div class="chart-wrap">
          <Bar :data="currentSalesData" :options="salesOptions" />
        </div>
      </div>

      <!-- Status Donut -->
      <div class="card chart-card chart-card--sm">
        <div class="chart-header">
          <div>
            <div class="chart-title">Customer Status</div>
            <div class="chart-sub">Active vs Isolir breakdown</div>
          </div>
        </div>
        <div class="donut-wrap">
          <svg class="donut-svg" viewBox="0 0 120 120">
            <!-- Arcs rotated via group so text stays upright -->
            <g transform="rotate(-90 60 60)">
              <circle class="donut-bg" cx="60" cy="60" r="48" />
              <circle
                class="donut-active"
                cx="60"
                cy="60"
                r="48"
                :stroke-dasharray="`${statusDist.activePct * 3.016} ${301.6}`"
                stroke-dashoffset="0"
              />
              <circle
                class="donut-isolir"
                cx="60"
                cy="60"
                r="48"
                :stroke-dasharray="`${statusDist.isolirPct * 3.016} ${301.6}`"
                :stroke-dashoffset="`${-(statusDist.activePct * 3.016)}`"
              />
            </g>
            <!-- Text at center, no rotation applied -->
            <text
              x="60"
              y="54"
              class="donut-center-num"
              text-anchor="middle"
              dominant-baseline="middle"
            >
              {{ statusDist.activePct }}%
            </text>
            <text
              x="60"
              y="70"
              class="donut-center-lbl"
              text-anchor="middle"
              dominant-baseline="middle"
            >
              Active
            </text>
          </svg>
          <div class="donut-legend">
            <div class="donut-legend-item">
              <span class="donut-dot" style="background: var(--teal)"></span>
              <span class="donut-legend-label">Active</span>
              <span class="donut-legend-val">{{
                formatNumber(statusDist.active)
              }}</span>
            </div>
            <div class="donut-legend-item">
              <span class="donut-dot" style="background: var(--gold)"></span>
              <span class="donut-legend-label">Isolir</span>
              <span class="donut-legend-val">{{
                formatNumber(statusDist.isolir)
              }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== ROW: Cumulative Growth Line + Revenue by Package + Package Market Share ===== -->
    <div class="charts-row charts-row--3col">
      <!-- Cumulative Growth -->
      <div class="card chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-title">Customer Growth</div>
            <div class="chart-sub">Cumulative total customers over time</div>
          </div>
          <div class="chart-tabs">
            <button
              class="ctab"
              :class="{ 'ctab--active': growthFilter === '6M' }"
              @click="growthFilter = '6M'"
            >
              6M
            </button>
            <button
              class="ctab"
              :class="{ 'ctab--active': growthFilter === '1Y' }"
              @click="growthFilter = '1Y'"
            >
              1Y
            </button>
            <button
              class="ctab"
              :class="{ 'ctab--active': growthFilter === 'All' }"
              @click="growthFilter = 'All'"
            >
              All
            </button>
          </div>
        </div>
        <div class="chart-wrap">
          <Line :data="filteredCumulativeChartData" :options="lineOptions" />
        </div>
      </div>

      <!-- Revenue by Package -->
      <div class="card chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-title">Revenue by Package</div>
            <div class="chart-sub">Total revenue contribution per package</div>
          </div>
        </div>
        <div class="chart-wrap">
          <Bar :data="revenuePackageData" :options="revPkgOptions" />
        </div>
      </div>

      <!-- Package Market Share Donut -->
      <div class="card chart-card chart-card--sm">
        <div class="chart-header">
          <div>
            <div class="chart-title">Market Share</div>
            <div class="chart-sub">Customer per package</div>
          </div>
        </div>
        <div class="donut-market-wrap" v-if="packages.length > 0">
          <div
            style="
              height: 160px;
              display: flex;
              justify-content: center;
              align-items: center;
              position: relative;
            "
          >
            <Doughnut :data="pkgMarketShareData" :options="doughnutOptions" />
            <div class="donut-center-text">
              <div class="donut-center-num" style="font-size: 20px">
                {{ packages.length }}
              </div>
              <div class="donut-center-lbl" style="font-size: 10px">
                Packages
              </div>
            </div>
          </div>
          <div class="pkg-legend">
            <div
              class="pkg-legend-item"
              v-for="(p, i) in packages.slice(0, 6)"
              :key="p.name"
            >
              <span
                class="pkg-legend-dot"
                :style="{ background: packageColors[i] }"
              ></span>
              <span class="pkg-legend-name">{{
                p.name?.length > 12 ? p.name.slice(0, 10) + "…" : p.name
              }}</span>
              <span class="pkg-legend-val">{{
                (p.active || 0) + (p.isolir || 0)
              }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state-inline">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <circle cx="12" cy="12" r="10" />
            <path d="M12 8v4l3 3" />
          </svg>
          <span>Belum ada data paket</span>
        </div>
      </div>
    </div>

    <!-- ===== ROW: City Distribution + Package Distribution ===== -->
    <div class="charts-row charts-row--3">
      <!-- City Distribution (Pie Chart) -->
      <div
        class="card chart-card chart-card--wide"
        style="align-self: flex-start"
      >
        <div class="chart-header">
          <div>
            <div class="chart-title">Geographic Distribution</div>
            <div class="chart-sub">Customers by area</div>
          </div>
          <router-link to="/address" class="view-all">View Details</router-link>
        </div>
        <div class="city-pie-wrap" v-if="cityDist.length > 0">
          <div
            class="chart-wrap"
            style="
              height: 240px;
              display: flex;
              justify-content: center;
              align-items: center;
            "
          >
            <Pie :data="cityPieData" :options="pieOptions" />
          </div>
          <div class="city-legend">
            <div class="legend-item" v-for="c in cityDist" :key="c.city">
              <div class="legend-dot" :style="{ background: c.color }"></div>
              <div class="legend-info">
                <span class="city-name">{{ c.city }}</span>
                <span class="city-count"
                  >{{ c.count }} pelanggan ({{ c.pct }}%)</span
                >
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state-inline">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
            <circle cx="12" cy="10" r="3" />
          </svg>
          <span>Belum ada data alamat pelanggan.</span>
        </div>
      </div>

      <!-- Package Distribution -->
      <div class="card chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-title">Package Distribution</div>
            <div class="chart-sub">Top performing connectivity plans</div>
          </div>
          <div style="display: flex; gap: 8px; align-items: center">
            <div class="pkg-nav">
              <button
                class="nav-btn"
                @click="prevPackagePage"
                :disabled="packagePage === 0"
              >
                &lt;
              </button>
              <span class="page-indicator"
                >{{ packagePage + 1 }} / {{ totalPackagePages }}</span
              >
              <button
                class="nav-btn"
                @click="nextPackagePage"
                :disabled="packagePage >= totalPackagePages - 1"
              >
                &gt;
              </button>
            </div>
            <button
              class="pkg-add-btn"
              @click="addPackage"
              title="Add New Package"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
                stroke-linecap="round"
              >
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
            </button>
          </div>
        </div>
        <div class="pkg-list" v-if="packages.length > 0">
          <div class="pkg-row" v-for="p in paginatedPackages" :key="p.name">
            <div class="pkg-info">
              <span class="pkg-name">{{ getPackageName(p.name) }}</span>
              <span class="pkg-speed">{{ getPackageSpeed(p.name) }}</span>
            </div>
            <div class="pkg-bar-bg">
              <div
                class="pkg-bar-fill"
                :style="{ width: p.pct + '%', background: p.color }"
              >
                <span class="pkg-bar-count">{{ p.count }}</span>
              </div>
            </div>
            <span class="pkg-pct">{{ p.pct }}%</span>
          </div>
        </div>
        <div v-else class="empty-state-inline">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <rect x="3" y="3" width="18" height="18" rx="3" />
            <path d="M9 9h6M9 12h6M9 15h4" />
          </svg>
          <span
            >Belum ada data paket.
            <router-link to="/packages" class="empty-link"
              >Tambah paket</router-link
            ></span
          >
        </div>
      </div>
    </div>

    <!-- ===== ROW: Agent Performance + Churn Risk ===== -->
    <div class="charts-row charts-row--agent">
      <!-- Agent Performance -->
      <div class="card section-card">
        <div class="section-hdr">
          <div>
            <div class="chart-title">Agent Performance</div>
            <div class="chart-sub">Customer acquisition by agent</div>
          </div>
          <button
            class="btn btn--ghost"
            style="padding: 4px 10px; font-size: 11px"
            @click="showAllAgents = !showAllAgents"
          >
            {{ showAllAgents ? "Show Less" : "View All Agents" }}
          </button>
        </div>
        <div class="agent-list" v-if="agents.length > 0">
          <div class="agent-row" v-for="a in displayedAgents" :key="a.name">
            <span class="agent-name">{{ a.name }}</span>
            <div class="agent-bar-bg">
              <div
                class="agent-bar-fill"
                :style="{
                  width: (a.count / maxAgentCount) * 100 + '%',
                  background: a.color,
                }"
              ></div>
            </div>
            <span class="agent-count">{{ a.count }}</span>
          </div>
        </div>
        <div v-else class="empty-state-inline">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 00-3-3.87" />
            <path d="M16 3.13a4 4 0 010 7.75" />
          </svg>
          <span
            >Belum ada data agen.
            <router-link to="/agent-performance" class="empty-link"
              >Kelola agen</router-link
            ></span
          >
        </div>
      </div>

      <!-- Churn Risk Table -->
      <div class="card section-card">
        <div class="section-hdr">
          <div>
            <div class="chart-title">Churn Risk Alert</div>
            <div class="chart-sub">
              Isolir customers needing immediate follow-up
            </div>
          </div>
          <router-link to="/customers" class="view-all">View All</router-link>
        </div>
        <div v-if="churnRisk.length > 0">
          <div class="churn-row" v-for="r in churnRisk" :key="r.name">
            <div class="churn-info">
              <span class="churn-name">{{ r.name }}</span>
              <span class="churn-pkg">{{ r.package }}</span>
            </div>
            <div class="churn-days">{{ r.daysSince }}d ago</div>
            <span
              class="churn-badge"
              :class="`churn-badge--${r.risk.toLowerCase()}`"
              >{{ r.risk }}</span
            >
          </div>
        </div>
        <div v-else class="empty-state-inline">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M22 11.08V12a10 10 0 11-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
          <span style="color: var(--green-ok); font-weight: 600"
            >All customers are active! 🎉</span
          >
        </div>
      </div>
    </div>

    <!-- Recent Registrations -->
    <div class="card section-card">
      <div class="section-hdr">
        <div>
          <div class="chart-title">Recent Registrations</div>
          <div class="chart-sub">New client onboarding and status tracking</div>
        </div>
        <router-link to="/customers" class="view-all">View All</router-link>
      </div>
      <div class="table-wrap">
        <table class="reg-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Package</th>
              <th>Agent Name</th>
              <th>Date</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in recentRegs" :key="r.name + r.date">
              <td class="td-name">{{ r.name }}</td>
              <td class="td-pkg">{{ r.package }}</td>
              <td class="td-agent">{{ r.agent }}</td>
              <td class="td-date">{{ r.date }}</td>
              <td>
                <span
                  class="badge"
                  :class="
                    r.status === 'ACTIVE' ? 'badge--green' : 'badge--gold'
                  "
                  >{{ r.status }}</span
                >
              </td>
            </tr>
            <tr v-if="recentRegs.length === 0">
              <td colspan="5" class="td-empty">
                <div class="empty-state-inline">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  >
                    <path
                      d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"
                    />
                    <rect x="9" y="3" width="6" height="4" rx="1" />
                  </svg>
                  <span
                    >Belum ada registrasi.
                    <router-link to="/customers" class="empty-link"
                      >Daftar pelanggan</router-link
                    ></span
                  >
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Bar, Line, Pie, Doughnut } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  DoughnutController,
  Filler,
  Tooltip,
  Legend,
} from "chart.js";
import { getDashboardOverview } from "@/services/api";
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  DoughnutController,
  Filler,
  Tooltip,
  Legend,
);

const router = useRouter();

const isDark = ref(document.documentElement.classList.contains("dark"));

// Reactive state
const selectedMonth = ref("");
const loading = ref(true);
const activeSalesTab = ref("Revenue");
const salesFilter = ref("1Y");
const growthFilter = ref("1Y");
const showAllAgents = ref(false);
const packagePage = ref(0);
const packagesPerPage = 10;


const addPackage = () => router.push("/packages");

// ===== DATA REFS =====
const salesTrend = ref({ labels: [], revenue: [], units: [] });
const packages = ref([]);
const agents = ref([]);
const recentRegs = ref([]);
const cityDist = ref([]);
const cumulativeGrowthData = ref({ labels: [], data: [] });
const revenueByPkg = ref([]);
const statusDist = ref({ active: 0, isolir: 0, activePct: 0, isolirPct: 0 });
const churnRisk = ref([]);

const dashboardSummary = ref({
  totalCustomers: 0,
  totalActive: 0,
  totalIsolir: 0,
  totalAgents: 0,
  totalPackages: 0,
  monthlyNewCustomers: 0,
  monthlyCustomerGrowthPct: 0,
  monthlyProfit: 0,
  monthlyProfitDelta: 0,
  periodMonthLabel: "bulan ini",
});

// ===== CHART DATA BUILDERS =====
const filteredSalesTrend = computed(() => {
  let sliceCount = salesTrend.value.labels.length;
  if (salesFilter.value === "6M") sliceCount = 6;
  else if (salesFilter.value === "1Y") sliceCount = 12;

  const startIdx = Math.max(0, salesTrend.value.labels.length - sliceCount);
  return {
    labels: salesTrend.value.labels.slice(startIdx),
    revenue: salesTrend.value.revenue.slice(startIdx),
    units: salesTrend.value.units.slice(startIdx),
  };
});

const makeSalesDataset = (label, data, activeColor) => {
  const numericData = data.map((v) => Number(v || 0));
  const maxIndex = numericData.indexOf(Math.max(0, ...numericData));
  return {
    labels: filteredSalesTrend.value.labels,
    datasets: [
      {
        label,
        data: numericData,
        backgroundColor: (ctx) =>
          ctx.dataIndex === maxIndex ? activeColor : "#E8ECF4",
        borderRadius: 8,
        borderSkipped: false,
      },
    ],
  };
};

const currentSalesData = computed(() =>
  activeSalesTab.value === "Revenue"
    ? makeSalesDataset(
        "Revenue",
        filteredSalesTrend.value.revenue,
        isDark.value ? "#38BDF8" : "#1A2170",
      )
    : makeSalesDataset(
        "Units Sold",
        filteredSalesTrend.value.units,
        isDark.value ? "#22D3EE" : "#00B4A6",
      ),
);

const salesOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        font: { family: "Inter", size: 11 },
        color: isDark.value ? "#64748B" : "#9BA3BF",
      },
    },
    y: { display: false },
  },
}));

// ===== CHART CONFIGS =====
const filteredCumulativeChartData = computed(() => {
  let sliceCount = cumulativeGrowthData.value.labels.length;
  if (growthFilter.value === "6M") sliceCount = 6;
  else if (growthFilter.value === "1Y") sliceCount = 12;

  const startIdx = Math.max(
    0,
    cumulativeGrowthData.value.labels.length - sliceCount,
  );
  const data = cumulativeGrowthData.value.data.slice(startIdx);

  const strokeColor = isDark.value ? "#38BDF8" : "#1A2170";
  const gradStart = isDark.value
    ? "rgba(56, 189, 248, 0.25)"
    : "rgba(26, 33, 112, 0.15)";
  const gradMid = isDark.value
    ? "rgba(56, 189, 248, 0.06)"
    : "rgba(26, 33, 112, 0.04)";

  return {
    labels: cumulativeGrowthData.value.labels.slice(startIdx),
    datasets: [
      {
        label: "Total Subscribers",
        data,
        borderColor: strokeColor,
        backgroundColor: (ctx) => {
          const chart = ctx.chart;
          const { ctx: c, chartArea } = chart;
          if (!chartArea)
            return isDark.value
              ? "rgba(56, 189, 248, 0.08)"
              : "rgba(26, 33, 112, 0.08)";
          const gradient = c.createLinearGradient(
            0,
            chartArea.top,
            0,
            chartArea.bottom,
          );
          gradient.addColorStop(0, gradStart);
          gradient.addColorStop(0.6, gradMid);
          gradient.addColorStop(1, "rgba(0, 0, 0, 0)");
          return gradient;
        },
        borderWidth: 2.5,
        pointBackgroundColor: strokeColor,
        pointBorderColor: isDark.value ? "#0D1330" : "#fff",
        pointBorderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 6,
        fill: true,
        tension: 0.45,
      },
    ],
  };
});

// Subscriber Growth Line Chart
const cumulativeChartData = computed(() => ({
  labels: cumulativeGrowthData.value.labels,
  datasets: [
    {
      label: "Total Subscribers",
      data: cumulativeGrowthData.value.data,
      borderColor: "#0D1340",
      backgroundColor: "rgba(13,19,64,0.07)",
      borderWidth: 2.5,
      fill: true,
      tension: 0.4,
      pointRadius: 3,
      pointBackgroundColor: "#0D1340",
      pointBorderColor: "#fff",
      pointBorderWidth: 2,
    },
  ],
}));

const lineOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.parsed.y} subscribers`,
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        font: { family: "Inter", size: 10 },
        color: isDark.value ? "#64748B" : "#9BA3BF",
      },
    },
    y: {
      display: true,
      border: { display: false },
      grid: {
        color: isDark.value
          ? "rgba(56, 189, 248, 0.08)"
          : "rgba(226,232,240,0.5)",
        drawBorder: false,
      },
      ticks: {
        font: { family: "Inter", size: 10 },
        color: isDark.value ? "#64748B" : "#9BA3BF",
        maxTicksLimit: 5,
      },
    },
  },
}));

// Package colors helper mapping
const packageColors = computed(() => {
  return isDark.value
    ? [
        "#38BDF8",
        "#06B6D4",
        "#F5A623",
        "#8B5CF6",
        "#3B82F6",
        "#EF4444",
        "#10B981",
        "#EC4899",
      ]
    : [
        "#1A2170",
        "#0D9488",
        "#F5A623",
        "#8B5CF6",
        "#3B82F6",
        "#EF4444",
        "#10B981",
        "#EC4899",
      ];
});

// Package Market Share Donut
const pkgMarketShareData = computed(() => {
  const pkgs = packages.value.filter(
    (p) => (p.active || 0) + (p.isolir || 0) > 0,
  );
  if (pkgs.length === 0)
    return { labels: [], datasets: [{ data: [], backgroundColor: [] }] };
  const colors = packageColors.value;
  return {
    labels: pkgs.map((p) =>
      p.name?.length > 16 ? p.name.slice(0, 14) + "…" : p.name,
    ),
    datasets: [
      {
        data: pkgs.map((p) => Number(p.active || 0) + Number(p.isolir || 0)),
        backgroundColor: pkgs.map((_, i) => colors[i % colors.length]),
        borderWidth: 3,
        borderColor: isDark.value ? "#0D1330" : "#fff",
        hoverOffset: 8,
      },
    ],
  };
});

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: "70%",
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => ` ${ctx.label}: ${ctx.parsed} pelanggan`,
      },
    },
  },
};

// Revenue by Package Bar Chart
const revenuePackageData = computed(() => {
  const colors = packageColors.value;
  return {
    labels: revenueByPkg.value.map((p) => {
      const n = p.name || "";
      return n.length > 14 ? n.slice(0, 12) + "…" : n;
    }),
    datasets: [
      {
        label: "Revenue",
        data: revenueByPkg.value.map((p) => Number(p.revenue || 0)),
        backgroundColor: revenueByPkg.value.map((p, idx) => {
          // If color returned is primary #0D1340, override with dynamic theme color
          return p.color === "#0D1340"
            ? colors[0]
            : colors[idx % colors.length] || p.color;
        }),
        borderRadius: 8,
        borderSkipped: false,
      },
    ],
  };
});

const revPkgOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const v = ctx.parsed.y;
          if (v >= 1_000_000) return `Rp ${(v / 1_000_000).toFixed(1)} Jt`;
          if (v >= 1_000) return `Rp ${Math.round(v / 1_000)} Rb`;
          return `Rp ${v}`;
        },
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        font: { family: "Inter", size: 10 },
        color: isDark.value ? "#64748B" : "#9BA3BF",
      },
    },
    y: { display: false },
  },
}));

// Geographic Distribution Pie Chart
const cityPieData = computed(() => ({
  labels: cityDist.value.map((c) => c.city),
  datasets: [
    {
      data: cityDist.value.map((c) => c.count),
      backgroundColor: cityDist.value.map((c) => c.color),
      borderWidth: 2,
      borderColor: "#fff",
      hoverOffset: 4,
    },
  ],
}));

const pieOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => ` ${ctx.label}: ${ctx.parsed} pelanggan`,
      },
    },
  },
};

// ===== COMPUTED HELPERS =====
const totalPackagePages = computed(
  () => Math.ceil(packages.value.length / packagesPerPage) || 1,
);
const paginatedPackages = computed(() => {
  const start = packagePage.value * packagesPerPage;
  return packages.value.slice(start, start + packagesPerPage);
});
const nextPackagePage = () => {
  if (packagePage.value < totalPackagePages.value - 1) packagePage.value++;
};
const prevPackagePage = () => {
  if (packagePage.value > 0) packagePage.value--;
};

const getPackageName = (name) =>
  name.replace(/\s*\d+\s*Mbps\+?$/i, "").trim() || name;
const getPackageSpeed = (name) => {
  const m = name.match(/(\d+\s*Mbps\+?)/i);
  return m ? m[1] : "";
};

const displayedAgents = computed(() => {
  const list = showAllAgents.value ? agents.value : agents.value.slice(0, 5);
  const colors = packageColors.value;
  return list.map((a, i) => ({
    ...a,
    color:
      a.color === "#0D1340" ? colors[0] : colors[i % colors.length] || a.color,
  }));
});

const formatNumber = (value) =>
  new Intl.NumberFormat("id-ID").format(Number(value || 0));
const formatCurrencyCompact = (value) => {
  const amount = Number(value || 0);
  if (Math.abs(amount) >= 1_000_000)
    return `Rp ${(amount / 1_000_000).toLocaleString("id-ID", { maximumFractionDigits: 1 })} Jt`;
  if (Math.abs(amount) >= 1_000)
    return `Rp ${Math.round(amount / 1_000).toLocaleString("id-ID")} Rb`;
  return `Rp ${Math.round(amount).toLocaleString("id-ID")}`;
};
const signedPercent = (value) => {
  const n = Number(value || 0);
  return `${n > 0 ? "+" : ""}${n}%`;
};
const signedCurrency = (value) => {
  const a = Number(value || 0);
  return `${a >= 0 ? "+" : "-"}${formatCurrencyCompact(Math.abs(a))}`;
};
const percent = (value, total) => {
  if (!total) return 0;
  return Math.min(100, Math.round((value / total) * 100));
};

const totalCustomersDisplay = computed(() =>
  formatNumber(dashboardSummary.value.totalCustomers),
);
const totalActiveDisplay = computed(() =>
  formatNumber(dashboardSummary.value.totalActive),
);
const totalIsolirDisplay = computed(() =>
  formatNumber(dashboardSummary.value.totalIsolir),
);
const activeRate = computed(() =>
  percent(
    dashboardSummary.value.totalActive,
    dashboardSummary.value.totalCustomers,
  ),
);
const isolirRate = computed(() =>
  percent(
    dashboardSummary.value.totalIsolir,
    dashboardSummary.value.totalCustomers,
  ),
);
const monthlyNewCustomersDisplay = computed(() =>
  formatNumber(dashboardSummary.value.monthlyNewCustomers),
);
const monthlyCustomerGrowthPct = computed(() =>
  Number(dashboardSummary.value.monthlyCustomerGrowthPct || 0),
);
const periodMonthLabel = computed(
  () => dashboardSummary.value.periodMonthLabel || "bulan ini",
);
const newCustomerMomentumProgress = computed(() => {
  const values = salesTrend.value.units.map((v) => Number(v || 0));
  const max = Math.max(1, ...values);
  return percent(dashboardSummary.value.monthlyNewCustomers, max);
});
const monthlyProfitDelta = computed(() =>
  Number(dashboardSummary.value.monthlyProfitDelta || 0),
);
const profitRecordedDisplay = computed(() =>
  formatCurrencyCompact(dashboardSummary.value.monthlyProfit),
);
const profitDeltaDisplay = computed(() =>
  signedCurrency(monthlyProfitDelta.value),
);
const maxAgentCount = computed(() =>
  Math.max(1, ...agents.value.map((a) => Number(a.count || 0))),
);

// ===== SPARKLINE GENERATION =====
function getSparklinePath(values, width = 100, height = 35, fill = false) {
  if (!values || values.length < 2) return "";
  let min = Math.min(...values);
  let max = Math.max(...values);
  const average = values.reduce((sum, v) => sum + v, 0) / values.length;

  // Pad Y bounds if fluctuation range is less than 10% of the average value
  const minAllowedRange = average * 0.1;
  if (max - min < minAllowedRange && average > 0) {
    const padding = (minAllowedRange - (max - min)) / 2;
    min = Math.max(0, min - padding);
    max = max + padding;
  }

  const range = max - min === 0 ? 1 : max - min;

  const points = values.map((val, index) => {
    const x = (index / (values.length - 1)) * width;
    const y = height - ((val - min) / range) * (height - 6) - 3;
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  });

  if (fill) {
    return `M 0,${height} L ${points.join(" L ")} L ${width},${height} Z`;
  }
  return `M ${points.join(" L ")}`;
}

const customerSparklineValues = computed(() => {
  const data = cumulativeGrowthData.value.data;
  if (!data || data.length === 0) return [0, 0];
  return data.slice(-6);
});
const customerSparklinePath = computed(() =>
  getSparklinePath(customerSparklineValues.value, 100, 35, false),
);
const customerSparklineFillPath = computed(() =>
  getSparklinePath(customerSparklineValues.value, 100, 35, true),
);
const customerSparklineColor = computed(() => {
  const vals = customerSparklineValues.value;
  if (vals.length < 2) return "var(--green-ok)";
  return vals[vals.length - 1] >= vals[0]
    ? "var(--green-ok)"
    : "var(--red-warn)";
});

const activeSparklineValues = computed(() => {
  const data = cumulativeGrowthData.value.data;
  if (!data || data.length === 0) return [0, 0];
  const rate = activeRate.value / 100 || 0.85;
  return data.slice(-6).map((val) => Math.round(val * rate));
});
const activeSparklinePath = computed(() =>
  getSparklinePath(activeSparklineValues.value, 100, 35, false),
);
const activeSparklineFillPath = computed(() =>
  getSparklinePath(activeSparklineValues.value, 100, 35, true),
);

const isolirSparklineValues = computed(() => {
  const data = cumulativeGrowthData.value.data;
  if (!data || data.length === 0) return [0, 0];
  const rate = isolirRate.value / 100 || 0.15;
  return data
    .slice(-6)
    .map((val, i) => Math.round(val * (rate + (i % 2 === 0 ? 0.02 : -0.01))));
});
const isolirSparklinePath = computed(() =>
  getSparklinePath(isolirSparklineValues.value, 100, 35, false),
);
const isolirSparklineFillPath = computed(() =>
  getSparklinePath(isolirSparklineValues.value, 100, 35, true),
);

const profitSparklineValues = computed(() => {
  const data = salesTrend.value.revenue;
  if (!data || data.length === 0) return [0, 0];
  return data.slice(-6).map((v) => Number(v || 0));
});
const profitSparklinePath = computed(() =>
  getSparklinePath(profitSparklineValues.value, 100, 35, false),
);
const profitSparklineFillPath = computed(() =>
  getSparklinePath(profitSparklineValues.value, 100, 35, true),
);
const profitSparklineColor = computed(() => {
  const vals = profitSparklineValues.value;
  if (vals.length < 2) return "var(--green-ok)";
  return vals[vals.length - 1] >= vals[0]
    ? "var(--green-ok)"
    : "var(--red-warn)";
});

// ===== LIFECYCLE =====
const fetchData = async () => {
  loading.value = true;
  try {
    const overview = await getDashboardOverview(selectedMonth.value);
    dashboardSummary.value = {
      totalCustomers: 0,
      totalActive: 0,
      totalIsolir: 0,
      totalAgents: 0,
      totalPackages: 0,
      ...(overview.summary || {}),
    };
    salesTrend.value = {
      labels: overview.salesTrend?.labels || [],
      revenue: overview.salesTrend?.revenue || [],
      units: overview.salesTrend?.units || [],
    };
    packages.value = overview.packages || [];
    agents.value = (overview.agents || []).map((a) => ({
      ...a,
      count: a.count ?? a.customers ?? 0,
    }));
    recentRegs.value = overview.recentRegistrations || [];
    cityDist.value = overview.cityDistribution || [];
    cumulativeGrowthData.value = overview.cumulativeGrowth || {
      labels: [],
      data: [],
    };
    revenueByPkg.value = overview.revenueByPackage || [];
    statusDist.value = overview.statusDistribution || {
      active: 0,
      isolir: 0,
      activePct: 0,
      isolirPct: 0,
    };
    churnRisk.value = overview.churnRisk || [];
  } catch (error) {
    console.warn("Failed to load dashboard overview", error);
  } finally {
    loading.value = false;
  }
};

let observer;

onMounted(() => {
  fetchData();

  // Track class changes on <html> for dynamic dark mode charts update
  observer = new MutationObserver(() => {
    isDark.value = document.documentElement.classList.contains("dark");
  });
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["class"],
  });
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
.page-fade-in {
  animation: pageFadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}
@keyframes pageFadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Toast */
.toast-notification {
  position: fixed;
  top: 24px;
  right: 24px;
  background: var(--text-1);
  color: #fff;
  border-radius: var(--r-md);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-xl);
  z-index: 1000;
  transform: translateX(120%);
  opacity: 0;
  transition:
    transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275),
    opacity 0.4s ease;
  max-width: 320px;
}
.toast-notification.toast-show {
  transform: translateX(0);
  opacity: 1;
}
.toast-icon {
  width: 34px;
  height: 34px;
  background: var(--green-ok);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.toast-icon svg {
  width: 16px;
  height: 16px;
}
.toast-content h4 {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 3px;
}
.toast-content p {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

/* Layout */
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
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
  letter-spacing: 0;
}
.page-sub {
  font-size: 13px;
  color: var(--text-2);
  margin-top: 4px;
}
.page-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.btn {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  white-space: nowrap;
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

/* KPI */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  width: 100%;
}
.kpi-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition:
    transform var(--transition-base),
    box-shadow var(--transition-base);
  position: relative;
  overflow: hidden;
}
/* Accent top border */
.kpi-card::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--teal), var(--navy));
  opacity: 0;
  transition: opacity var(--transition-base);
  border-radius: var(--r-md) var(--r-md) 0 0;
}
.kpi-card:hover::after {
  opacity: 1;
}
.kpi-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.kpi-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  padding: 4px 10px;
  border-radius: 99px;
}
.kpi-pill-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-pill-icon svg {
  width: 12px;
  height: 12px;
}
.kpi-pill-icon--blue {
  background: var(--blue-bg);
  color: var(--blue);
}
.kpi-pill-icon--green {
  background: var(--green-bg);
  color: var(--green-ok);
}
.kpi-pill-icon--red {
  background: var(--red-bg);
  color: var(--red-warn);
}
.kpi-pill-icon--teal {
  background: var(--teal-bg);
  color: var(--teal);
}

.kpi-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-2);
  font-family: var(--font-display);
}

.kpi-body {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}
.kpi-value-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.kpi-value {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: 1;
  color: var(--text-1);
}
.kpi-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.trend-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 99px;
  font-family: var(--font-display);
}
.trend-badge svg {
  width: 10px;
  height: 10px;
}
.trend-badge--up {
  background: var(--green-bg);
  color: var(--green-ok);
  border: 1px solid rgba(16, 185, 129, 0.15);
}
.trend-badge--down {
  background: var(--red-bg);
  color: var(--red-warn);
  border: 1px solid rgba(239, 68, 68, 0.15);
}

.kpi-sparkline {
  width: 110px;
  height: 38px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.sparkline-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
}

/* Metrics Row */
.metrics-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.metric-card {
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  overflow: hidden;
}
.metric-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.metric-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}
.metric-icon svg {
  width: 17px;
  height: 17px;
}
.metric-icon--gold {
  background: #fef3e0;
  color: var(--gold);
}
.metric-icon--teal {
  background: #e0f8f6;
  color: var(--teal);
}
.metric-tag {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  padding: 3px 8px;
  border-radius: 99px;
  font-family: var(--font-display);
}
.metric-tag--gold {
  background: #fef3e0;
  color: var(--gold);
}
.metric-tag--teal {
  background: #e0f8f6;
  color: var(--teal);
}
.metric-label {
  font-size: 11px;
  color: var(--text-3);
  font-weight: 600;
  text-transform: uppercase;
  font-family: var(--font-display);
}
.metric-value {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--text-1);
}
.metric-sub {
  font-size: 12px;
  color: var(--green-ok);
  font-weight: 600;
}
.metric-sub--down {
  color: var(--red-warn);
}
.isolir-bar {
  height: 6px;
  background: var(--border);
  border-radius: 99px;
  overflow: hidden;
  margin-top: 4px;
}
.isolir-bar__fill {
  height: 100%;
  background: linear-gradient(90deg, #f5a623, #e74c3c);
  border-radius: 99px;
  transition: width 0.6s ease;
}
.metric-card--spark {
  gap: 16px;
}

/* Charts Row */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  width: 100%;
}
.charts-row--3 {
  grid-template-columns: 1.5fr 1fr;
}
.charts-row--3col {
  grid-template-columns: 1.2fr 1fr 0.8fr;
}
.charts-row--agent {
  grid-template-columns: 1fr 1fr;
}
.chart-card {
  padding: 22px;
  transition: box-shadow var(--transition-base);
}
.chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}
.chart-title {
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  letter-spacing: -0.1px;
}
.chart-sub {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 3px;
}
.chart-wrap {
  height: 200px;
}
.chart-tabs {
  display: flex;
  background: var(--surface-3);
  border-radius: var(--r-sm);
  padding: 3px;
  gap: 2px;
  flex-shrink: 0;
  border: 1px solid var(--border);
}
.ctab {
  background: none;
  border: none;
  padding: 5px 12px;
  border-radius: 7px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-3);
  cursor: pointer;
  font-family: var(--font-display);
  transition: all var(--transition-fast);
}
.ctab--active {
  background: var(--surface);
  color: var(--navy);
  box-shadow: var(--shadow-sm);
}

/* Donut Chart */
.donut-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  height: 200px;
  justify-content: center;
}
.donut-svg {
  width: 130px;
  height: 130px;
}
.donut-bg {
  fill: none;
  stroke: var(--border);
  stroke-width: 14;
}
.donut-active {
  fill: none;
  stroke: var(--teal);
  stroke-width: 14;
  stroke-linecap: round;
  transition: stroke-dasharray 0.8s ease;
}
.donut-isolir {
  fill: none;
  stroke: var(--gold);
  stroke-width: 14;
  stroke-linecap: round;
  transition: stroke-dasharray 0.8s ease;
}
.donut-center-num {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 800;
  fill: var(--navy);
}
.donut-center-lbl {
  font-family: var(--font-display);
  font-size: 9px;
  font-weight: 600;
  fill: var(--text-3);
  text-transform: uppercase;
  letter-spacing: 1px;
}
.donut-legend {
  display: flex;
  gap: 16px;
}
.donut-legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.donut-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.donut-legend-label {
  font-size: 11px;
  color: var(--text-3);
}
.donut-legend-val {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
  font-family: var(--font-display);
}

/* Donut Market Share (chartjs-based) */
.donut-market-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.donut-center-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
}
.donut-center-text .donut-center-num {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 800;
  color: var(--navy);
  display: block;
  fill: none;
}
.donut-center-text .donut-center-lbl {
  font-size: 10px;
  color: var(--text-3);
  font-family: var(--font-display);
  display: block;
  fill: none;
}
.pkg-legend {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.pkg-legend-item {
  display: flex;
  align-items: center;
  gap: 7px;
}
.pkg-legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.pkg-legend-name {
  font-size: 11px;
  color: var(--text-2);
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pkg-legend-val {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-1);
  font-family: var(--font-display);
}

/* City Distribution (Pie layout) */
.city-pie-wrap {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 24px;
  padding: 10px 0;
}
.city-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  max-height: 240px;
  overflow-y: auto;
  padding-right: 8px;
}
.city-legend::-webkit-scrollbar {
  width: 4px;
}
.city-legend::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 4px;
}
.city-legend .legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.city-legend .legend-info {
  display: flex;
  flex-direction: column;
}
.city-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.city-count {
  font-size: 11px;
  color: var(--text-3);
  font-family: var(--font-display);
}

/* Package Distribution */
.pkg-nav {
  display: flex;
  align-items: center;
  gap: 6px;
}
.nav-btn {
  width: 24px;
  height: 24px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: none;
  cursor: pointer;
  font-size: 11px;
  color: var(--text-2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-btn:hover:not(:disabled) {
  background: var(--bg);
}
.nav-btn:disabled {
  opacity: 0.35;
  cursor: default;
}
.page-indicator {
  font-size: 11px;
  color: var(--text-3);
  font-family: var(--font-display);
}
.pkg-add-btn {
  width: 26px;
  height: 26px;
  border: 1px dashed var(--border);
  border-radius: 6px;
  background: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
}
.pkg-add-btn:hover {
  background: var(--bg);
  color: var(--navy);
  border-color: var(--navy);
}
.pkg-add-btn svg {
  width: 12px;
  height: 12px;
}
.pkg-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.pkg-row {
  display: grid;
  grid-template-columns: 1fr 120px 36px;
  align-items: center;
  gap: 10px;
}
.pkg-info {
  min-width: 0;
}
.pkg-name {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pkg-speed {
  font-size: 10px;
  color: var(--text-3);
}
.pkg-bar-bg {
  height: 8px;
  background: var(--border);
  border-radius: 99px;
  overflow: hidden;
}
.pkg-bar-fill {
  height: 100%;
  border-radius: 99px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  transition: width 0.6s ease;
  position: relative;
}
.pkg-bar-count {
  position: absolute;
  right: -20px;
  font-size: 10px;
  font-weight: 600;
  color: var(--text-2);
  font-family: var(--font-display);
}
.pkg-pct {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-3);
  text-align: right;
}

/* Agent Performance */
.section-card {
  padding: 20px;
}
.section-hdr {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
.view-all {
  font-size: 12px;
  font-weight: 600;
  color: var(--navy);
  text-decoration: none;
  font-family: var(--font-display);
  flex-shrink: 0;
}
.view-all:hover {
  color: var(--teal);
}
.agent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.agent-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.agent-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
  width: 130px;
  flex-shrink: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.agent-bar-bg {
  flex: 1;
  height: 8px;
  background: var(--border);
  border-radius: 99px;
  overflow: hidden;
}
.agent-bar-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 0.6s ease;
}
.agent-count {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-2);
  font-family: var(--font-display);
  width: 30px;
  text-align: right;
  flex-shrink: 0;
}

/* Churn Risk */
.churn-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}
.churn-row:last-child {
  border-bottom: none;
}
.churn-info {
  flex: 1;
  min-width: 0;
}
.churn-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.churn-pkg {
  font-size: 11px;
  color: var(--text-3);
}
.churn-days {
  font-size: 11px;
  color: var(--text-3);
  white-space: nowrap;
  font-family: var(--font-display);
}
.churn-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 99px;
  font-family: var(--font-display);
  flex-shrink: 0;
}
.churn-badge--high {
  background: #fdeceb;
  color: var(--red-warn);
}
.churn-badge--medium {
  background: #fef3e0;
  color: var(--gold);
}
.churn-badge--low {
  background: #e8f8ef;
  color: var(--green-ok);
}

/* Recent Registrations Table */
.table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.reg-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 500px;
}
.reg-table th {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
  text-align: left;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
  background: var(--surface-2);
}
.reg-table td {
  padding: 12px;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.reg-table tr:last-child td {
  border-bottom: none;
}
.reg-table tr:hover td {
  background: var(--bg);
}
.td-name {
  font-weight: 600;
  color: var(--text-1);
}
.td-pkg {
  color: var(--navy);
  font-weight: 500;
}
.td-agent {
  color: var(--text-2);
}
.td-date {
  color: var(--text-3);
  font-size: 12px;
}
.td-empty {
  padding: 32px !important;
  text-align: center;
}

/* Empty states */
.empty-state-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 0;
  color: var(--text-3);
  font-size: 12px;
}
.empty-state-inline svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
.empty-link {
  color: var(--navy);
  font-weight: 600;
  text-decoration: none;
}
.empty-link:hover {
  color: var(--teal);
}

/* Responsive */
@media (max-width: 1200px) {
  .charts-row--3col {
    grid-template-columns: 1fr 1fr;
  }
  .charts-row--3col .chart-card:last-child {
    grid-column: 1 / -1;
  }
}
@media (max-width: 1024px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
  .charts-row--3 {
    grid-template-columns: 1fr;
  }
  .charts-row--3col {
    grid-template-columns: 1fr;
  }
  .charts-row--agent {
    grid-template-columns: 1fr;
  }
  .kpi-row {
    grid-template-columns: 1fr 1fr;
  }
  .metrics-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 640px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
  .kpi-value {
    font-size: 32px;
  }
}
</style>
