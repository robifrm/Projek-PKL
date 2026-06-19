<template>
  <div class="agent-perf">
    <!-- Header -->
    <div class="page-header">
      <div class="header-text">
        <h1 class="page-title">Agent Performance</h1>
        <p class="page-sub">
          Track acquisition metrics, commission targets, and individual agent
          efficiency.
        </p>
      </div>
      <div class="page-actions">
        <div class="period-select">
          <select v-model="periodType" @change="fetchData">
            <option value="month">Specific Month</option>
            <option value="quarter">This Quarter</option>
            <option value="year">This Year</option>
            <option value="all">All Time</option>
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

        <div class="period-select" v-if="periodType === 'month'">
          <input
            type="month"
            v-model="selectedMonth"
            @change="fetchData"
            class="form-input"
            style="padding: 8px 12px; height: auto"
          />
        </div>

        <button class="btn btn--primary" @click="addAgent">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          >
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span class="btn-text">Add Agent</span>
        </button>
      </div>
    </div>

    <!-- KPI Row -->
    <div class="kpi-row" v-if="loading">
      <div class="card kpi-card" v-for="i in 4" :key="i">
        <div class="kpi-top">
          <div class="skeleton skeleton-text--sm" style="width: 80px"></div>
          <div
            class="skeleton"
            style="width: 32px; height: 32px; border-radius: 8px"
          ></div>
        </div>
        <div
          class="skeleton skeleton-text--lg"
          style="margin-top: 12px; width: 100px"
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
          <span class="kpi-label">Total Agents</span>
          <div class="kpi-icon kpi-icon--blue">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 00-3-3.87" />
              <path d="M16 3.13a4 4 0 010 7.75" />
            </svg>
          </div>
        </div>
        <div class="kpi-value">{{ totalAgents }}</div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--green"
            >{{ activeAgentCount }} aktif</span
          >
          <span class="kpi-meta-text">{{ periodLabel }}</span>
        </div>
        <div class="kpi-note">
          {{ totalAgents - activeAgentCount }} belum aktif periode ini
        </div>
      </div>

      <div class="card kpi-card kpi-card--dark">
        <div class="kpi-top">
          <span class="kpi-label" style="color: rgba(255, 255, 255, 0.7)"
            >Top Performer</span
          >
          <span
            v-if="topPerformer.customers > 0"
            class="badge badge--gold"
            style="font-size: 9px; padding: 3px 6px"
            >GOLD</span
          >
        </div>
        <div
          v-if="topPerformer.customers > 0"
          style="
            display: flex;
            flex-direction: column;
            height: 100%;
            gap: 12px;
            margin-top: auto;
          "
        >
          <div class="top-performer">
            <div
              class="logo-avatar top-performer__logo"
              :class="{ 'logo-avatar--image': hasLogo(topPerformer) }"
              :style="{
                background: hasLogo(topPerformer) ? '#fff' : topPerformer.color,
              }"
            >
              <img
                v-if="hasLogo(topPerformer)"
                :src="topPerformer.logo"
                :alt="`${topPerformer.name} logo`"
                @error="markLogoBroken(topPerformer.logo)"
              />
              <span v-else>{{ topPerformer.initials }}</span>
            </div>
            <div class="top-performer__meta">
              <div class="top-performer__name" :title="topPerformer.name">
                {{ topPerformer.name }}
              </div>
              <div class="top-performer__type">{{ topPerformer.type }}</div>
            </div>
          </div>
          <div class="kpi-meta" style="margin-top: auto">
            <span class="badge-inline badge-inline--gold"
              >{{ topPerformer.customers }} customers</span
            >
          </div>
          <div class="success-rate">
            Rp {{ topPerformer.commission }} estimated
          </div>
        </div>
        <div
          v-else
          class="chart-empty-state"
          style="
            padding: 10px;
            color: rgba(255, 255, 255, 0.5);
            font-size: 12px;
            margin-top: auto;
            margin-bottom: auto;
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            text-align: center;
          "
        >
          <span>Belum ada pencapaian agen pada periode ini.</span>
        </div>
      </div>

      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Avg. per Agent</span>
          <div class="kpi-icon kpi-icon--teal">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
            </svg>
          </div>
        </div>
        <div class="kpi-value" style="color: var(--teal)">
          {{ avgPerAgent }}
        </div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--teal">{{
            signedPercent(periodCustomerGrowthPct)
          }}</span>
          <span class="kpi-meta-text">customers/agent</span>
        </div>
        <div class="kpi-note">
          Rata-rata akuisisi per agent {{ periodLabel }}
        </div>
      </div>

      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Total Commission</span>
          <div class="kpi-icon kpi-icon--gold">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <line x1="12" y1="1" x2="12" y2="23" />
              <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6" />
            </svg>
          </div>
        </div>
        <div class="kpi-value" style="color: var(--gold)">
          {{ totalCommissionDisplay }}
        </div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--gold">{{
            commissionDeltaDisplay
          }}</span>
          <span class="kpi-meta-text">vs periode lalu</span>
        </div>
      </div>
    </div>

    <!-- Chart + Leaderboard -->
    <div class="main-row">
      <div class="card chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-title">Acquisition by Agent</div>
            <div class="chart-sub">Total customers acquired this period</div>
          </div>
          <div class="chart-tabs">
            <button
              class="ctab"
              :class="{ 'ctab--active': chartTab === 'customers' }"
              @click="chartTab = 'customers'"
            >
              Customers
            </button>
            <button
              class="ctab"
              :class="{ 'ctab--active': chartTab === 'revenue' }"
              @click="chartTab = 'revenue'"
            >
              Revenue
            </button>
          </div>
        </div>
        <div v-if="loading" class="chart-scroll-wrap" style="padding: 20px">
          <div
            class="skeleton"
            style="height: 240px; width: 100%; border-radius: 12px"
          ></div>
        </div>
        <template v-else>
          <div class="chart-scroll-wrap" v-if="agents.length > 0">
            <div class="chart-wrap" style="height: 240px; min-width: 500px">
              <Bar :data="agentChartData" :options="agentChartOptions" />
            </div>
          </div>
          <div v-else class="chart-empty-state">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M18 20V10" />
              <path d="M12 20V4" />
              <path d="M6 20v-6" />
            </svg>
            <p>Belum ada data agen untuk ditampilkan</p>
            <small
              >Tambah agen atau import data pelanggan terlebih dahulu</small
            >
          </div>
          <div class="chart-legend">
            <div
              class="legend-item"
              v-for="a in agents.slice(0, 4)"
              :key="a.name"
            >
              <div class="legend-dot" :style="{ background: a.color }"></div>
              <span>{{ a.short }}</span>
            </div>
          </div>
        </template>
      </div>

      <div class="card leaderboard-card">
        <div class="lb-header">
          <div class="chart-title">Leaderboard</div>
          <span class="badge badge--navy">Live</span>
        </div>
        <div class="lb-list" v-if="sortedAgents.length > 0">
          <div
            class="lb-item"
            v-for="(a, i) in sortedAgents.slice(0, 5)"
            :key="a.name"
          >
            <div class="lb-rank" :class="rankClass(i)">{{ i + 1 }}</div>
            <div
              class="logo-avatar lb-avatar"
              :class="{ 'logo-avatar--image': hasLogo(a) }"
              :style="{ background: hasLogo(a) ? '#fff' : a.color }"
            >
              <img
                v-if="hasLogo(a)"
                :src="a.logo"
                :alt="`${a.name} logo`"
                @error="markLogoBroken(a.logo)"
              />
              <span v-else>{{ a.initials }}</span>
            </div>
            <div class="lb-info">
              <div class="lb-name">{{ a.name }}</div>
              <div class="lb-prog-bg">
                <div
                  class="lb-prog-fill"
                  :style="{
                    width: (a.count / maxAgent) * 100 + '%',
                    background: a.color,
                  }"
                ></div>
              </div>
            </div>
            <div class="lb-count">{{ a.count }}</div>
          </div>
        </div>
        <div v-else class="lb-empty">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            width="32"
            height="32"
          >
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
            <circle cx="9" cy="7" r="4" />
          </svg>
          <p>Belum ada agen terdaftar</p>
        </div>
      </div>
    </div>

    <!-- Agent Cards (Horizontally Scrollable) -->
    <div class="section-title-row">
      <div>
        <h2 class="section-title">Agent Directory</h2>
        <p class="section-sub">Scroll right to view all active agents</p>
      </div>
    </div>

    <div class="agents-scroll-wrap" v-if="mappedAgentDetails.length > 0">
      <div class="agents-grid">
        <div
          class="card agent-card"
          v-for="a in mappedAgentDetails"
          :key="a.name"
        >
          <div class="ac-header">
            <div
              class="logo-avatar ac-avatar"
              :class="{ 'logo-avatar--image': hasLogo(a) }"
              :style="{ background: hasLogo(a) ? '#fff' : a.color }"
            >
              <img
                v-if="hasLogo(a)"
                :src="a.logo"
                :alt="`${a.name} logo`"
                @error="markLogoBroken(a.logo)"
              />
              <span v-else>{{ a.initials }}</span>
            </div>
            <div class="ac-meta">
              <div class="ac-name" :title="a.name">{{ a.name }}</div>
              <div class="ac-type">{{ a.type }}</div>
            </div>
            <span
              class="badge"
              :class="a.status === 'ACTIVE' ? 'badge--green' : 'badge--gold'"
              >{{ a.status }}</span
            >
          </div>
          <div class="ac-stats">
            <div class="ac-stat">
              <div class="ac-stat-val">{{ a.customers }}</div>
              <div class="ac-stat-lbl">Customers</div>
            </div>
            <div class="ac-stat-divider"></div>
            <div class="ac-stat">
              <div class="ac-stat-val" style="color: var(--green-ok)">
                {{ a.active }}
              </div>
              <div class="ac-stat-lbl">Active</div>
            </div>
            <div class="ac-stat-divider"></div>
            <div class="ac-stat">
              <div
                class="ac-stat-val"
                style="color: var(--gold); font-size: 13px"
              >
                {{ a.commission }}
              </div>
              <div class="ac-stat-lbl">
                {{ a.isCompany ? "Rev. Sharing" : "Commission" }}
              </div>
            </div>
          </div>

          <div class="ac-progress" style="margin-top: 8px">
            <div class="ac-prog-label">
              <span>Activation Rate (Lead Quality)</span>
              <span class="ac-prog-pct" style="color: var(--teal)"
                >{{ Math.round((a.active / (a.customers || 1)) * 100) }}%</span
              >
            </div>
            <div class="ac-prog-bg">
              <div
                class="ac-prog-fill"
                :style="{
                  width:
                    Math.round((a.active / (a.customers || 1)) * 100) + '%',
                  background: 'var(--teal)',
                }"
              ></div>
            </div>
          </div>
          <div class="ac-footer">
            <span class="ac-joined">Joined {{ a.joined }}</span>
            <button class="ac-view-btn">
              <span>Profile</span>
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="agents-empty-state">
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
      <h3>Belum Ada Agen</h3>
      <p>
        Klik <strong>Add Agent</strong> untuk menambahkan agen pertama, atau
        import data pelanggan melalui Data Import.
      </p>
    </div>

    <!-- Performance Table -->
    <div class="card section-card">
      <div class="section-hdr">
        <div>
          <div class="chart-title">Performance List</div>
          <div class="chart-sub">
            Detailed breakdown of rate, commission, and growth
          </div>
        </div>
        <div class="table-tabs-row">
          <button
            class="ctab"
            :class="{ 'ctab--active': activeTableTab === 'all' }"
            @click="activeTableTab = 'all'"
          >
            All
          </button>
          <button
            class="ctab"
            :class="{ 'ctab--active': activeTableTab === 'top5' }"
            @click="activeTableTab = 'top5'"
          >
            Top 5
          </button>
          <button
            class="ctab"
            :class="{ 'ctab--active': activeTableTab === 'under' }"
            @click="activeTableTab = 'under'"
          >
            Under Target
          </button>
        </div>
      </div>
      <div class="table-wrap">
        <table class="perf-table">
          <thead>
            <tr>
              <th class="th-sort" @click="toggleAgentSort('name')">
                Agent
                <span class="sort-icon">
                  <svg
                    v-if="agentSortKey === 'name'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        agentSortDir === 'asc'
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
              <th class="th-sort" @click="toggleAgentSort('type')">
                Type
                <span class="sort-icon">
                  <svg
                    v-if="agentSortKey === 'type'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        agentSortDir === 'asc'
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
              <th
                class="th-right th-sort"
                @click="toggleAgentSort('customers')"
              >
                Customers
                <span class="sort-icon">
                  <svg
                    v-if="agentSortKey === 'customers'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        agentSortDir === 'asc'
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
              <th class="th-right th-sort" @click="toggleAgentSort('active')">
                Active
                <span class="sort-icon">
                  <svg
                    v-if="agentSortKey === 'active'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        agentSortDir === 'asc'
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
              <th class="th-sort" @click="toggleAgentSort('isolirRate')">
                Isolir Rate
                <span class="sort-icon">
                  <svg
                    v-if="agentSortKey === 'isolirRate'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        agentSortDir === 'asc'
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
              <th
                class="th-right th-sort"
                @click="toggleAgentSort('commissionValue')"
              >
                Commission
                <span class="sort-icon">
                  <svg
                    v-if="agentSortKey === 'commissionValue'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        agentSortDir === 'asc'
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
              <th class="th-sort" @click="toggleAgentSort('growth')">
                Growth
                <span class="sort-icon">
                  <svg
                    v-if="agentSortKey === 'growth'"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                  >
                    <polyline
                      :points="
                        agentSortDir === 'asc'
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
              <th>Status</th>
              <th class="th-center">Action</th>
            </tr>
          </thead>
          <tbody v-if="loading">
            <tr v-for="i in 5" :key="i">
              <td colspan="8">
                <div
                  class="skeleton"
                  style="height: 36px; width: 100%; border-radius: 6px"
                ></div>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <!-- Show 10 rows maximum by default if needed, or let API dictate. We will limit it to 10 conceptually if it aligns with user request to display "nampilin 10 aja" -->
            <tr
              v-for="(a, idx) in filteredTableAgents"
              :key="a.name"
              class="perf-row"
            >
              <td>
                <div class="pa-cell">
                  <div
                    class="logo-avatar pa-avatar"
                    :class="{ 'logo-avatar--image': hasLogo(a) }"
                    :style="{ background: hasLogo(a) ? '#fff' : a.color }"
                  >
                    <img
                      v-if="hasLogo(a)"
                      :src="a.logo"
                      :alt="`${a.name} logo`"
                      @error="markLogoBroken(a.logo)"
                    />
                    <span v-else>{{ a.initials }}</span>
                  </div>
                  <span class="pa-name">{{ a.name }}</span>
                </div>
              </td>
              <td>
                <span
                  class="type-tag"
                  :class="a.isCompany ? 'type-tag--corp' : 'type-tag--ind'"
                >
                  {{ a.isCompany ? "Corporate" : "Individual" }}
                </span>
              </td>
              <td class="td-num td-right">{{ a.customers }}</td>
              <td class="td-num td-right" style="color: var(--green-ok)">
                {{ a.active }}
              </td>
              <td>
                <div class="isolir-cell">
                  <span
                    class="isolir-pct"
                    :class="{ 'isolir-warn': a.isolirRate > 15 }"
                    >{{ a.isolirRate }}%</span
                  >
                  <div class="mini-bar">
                    <div
                      class="mini-bar__fill"
                      :style="{
                        width: a.isolirRate + '%',
                        background:
                          a.isolirRate > 15 ? 'var(--gold)' : 'var(--teal)',
                      }"
                    ></div>
                  </div>
                </div>
              </td>
              <td class="td-commission td-right">{{ a.commission }}</td>
              <td>
                <span
                  class="growth-tag"
                  :class="a.growth > 0 ? 'growth-tag--up' : 'growth-tag--down'"
                >
                  {{ a.growth > 0 ? "+" : "" }}{{ a.growth }}%
                </span>
              </td>
              <td>
                <span
                  class="badge"
                  :class="
                    a.status === 'ACTIVE'
                      ? 'badge--green'
                      : a.status === 'SUSPENDED'
                        ? 'badge--red'
                        : 'badge--gold'
                  "
                  >{{ a.status }}</span
                >
              </td>
              <td class="td-center">
                <div class="action-menu-wrap">
                  <button
                    class="btn-icon"
                    title="Options"
                    @click.stop="toggleActionMenu(a.name)"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                    >
                      <circle cx="12" cy="12" r="1"></circle>
                      <circle cx="19" cy="12" r="1"></circle>
                      <circle cx="5" cy="12" r="1"></circle>
                    </svg>
                  </button>
                  <Transition name="dropdown">
                    <div
                      class="action-dropdown"
                      :class="{
                        'action-dropdown--up':
                          idx >= filteredTableAgents.length - 3 &&
                          filteredTableAgents.length > 3,
                      }"
                      v-if="activeActionMenu === a.name"
                    >
                      <button
                        class="action-dropdown-item"
                        @click="editAgent(a)"
                      >
                        Edit Details
                      </button>
                      <button
                        class="action-dropdown-item"
                        @click="suspendAgent(a)"
                      >
                        {{
                          a.status === "SUSPENDED"
                            ? "Activate Agent"
                            : "Suspend Agent"
                        }}
                      </button>
                      <button
                        class="action-dropdown-item action-dropdown-item--danger"
                        @click="deleteAgent(a)"
                      >
                        Remove
                      </button>
                    </div>
                  </Transition>
                </div>
              </td>
            </tr>
            <tr v-if="filteredTableAgents.length === 0">
              <td
                colspan="9"
                style="
                  text-align: center;
                  padding: 32px;
                  color: var(--text-3);
                  font-size: 13px;
                "
              >
                Belum ada agen. Klik <strong>Add Agent</strong> untuk
                menambahkan agen baru.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Pagination for Performance Table -->
      <div
        class="table-footer"
        style="
          padding: 14px 20px;
          border-top: 1px solid var(--border);
          display: flex;
          align-items: center;
          justify-content: space-between;
        "
      >
        <div class="footer-left">
          <span
            class="table-count"
            style="font-size: 12px; color: var(--text-3)"
            >Showing
            <strong
              >{{ (agentPage - 1) * agentItemsPerPage + 1 }}–{{
                Math.min(agentPage * agentItemsPerPage, totalTableAgents)
              }}</strong
            >
            of <strong>{{ totalTableAgents }}</strong></span
          >
          <select
            class="rows-select"
            v-model="agentItemsPerPage"
            @change="agentPage = 1"
          >
            <option :value="10">10 / page</option>
            <option :value="25">25 / page</option>
            <option :value="50">50 / page</option>
          </select>
        </div>
        <div class="pagination">
          <button
            class="page-btn"
            :disabled="agentPage === 1"
            @click="agentPage--"
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
          <span
            class="page-btn page-num page-btn--active"
            style="cursor: default; min-width: 32px"
            >{{ agentPage }}</span
          >
          <button
            class="page-btn"
            :disabled="agentPage >= agentTotalPages"
            @click="agentPage++"
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

    <!-- Add Agent Modal -->
    <div
      class="modal-backdrop"
      v-if="isAddModalOpen"
      @click.self="isAddModalOpen = false"
    >
      <div class="modal-card card">
        <div class="modal-header">
          <h2 class="chart-title">
            {{
              modalMode === "edit" ? "Edit Agent Details" : "Register New Agent"
            }}
          </h2>
          <button class="btn-icon" @click="isAddModalOpen = false">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Agent Name</label>
            <input
              class="form-input"
              v-model="newAgentForm.name"
              placeholder="e.g. PT. Maju Bersama or John Doe"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Type</label>
            <select class="form-input" v-model="newAgentForm.isCompany">
              <option :value="false">Individual</option>
              <option :value="true">Corporate Partner</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn--ghost" @click="isAddModalOpen = false">
            Cancel
          </button>
          <button class="btn btn--primary" @click="submitAddAgent">
            Save Agent
          </button>
        </div>
      </div>
    </div>

    <!-- Custom Confirm Modal -->
    <div
      class="modal-backdrop"
      v-if="showConfirmModal"
      @click.self="showConfirmModal = false"
      style="z-index: 110"
    >
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
          <div class="delete-modal-title">Hapus Agen</div>
          <div class="delete-modal-desc">
            Anda akan menghapus agen
            <strong>{{ selectedAgentForDelete?.name }}</strong>.
            Tindakan ini permanen dan tidak dapat dibatalkan.
          </div>
        </div>
        <div class="delete-modal-footer">
          <button class="btn btn--secondary" @click="showConfirmModal = false">Batal</button>
          <button class="btn btn--danger" @click="executeConfirmDelete">Ya, Hapus</button>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { Bar } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
} from "chart.js";
import {
  createAgent,
  deleteAgentRecord,
  getAgentPerformanceByPeriod,
  updateAgent,
  updateAgentStatus,
} from "@/services/api";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip);

const loading = ref(true);
const isDark = ref(document.documentElement.classList.contains("dark"));

const agentColors = computed(() => {
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
      ];
});
const periodType = ref("month");
const selectedMonth = ref(new Date().toISOString().slice(0, 7)); // e.g. "2026-06"
const period = computed(() => {
  return periodType.value === "month" ? selectedMonth.value : periodType.value;
});
const agentSearchQuery = ref("");
const activeTableTab = ref("all");
const chartTab = ref("customers");
const brokenLogos = ref(new Set());
const agentSortKey = ref("");
const agentSortDir = ref("asc");
const agentItemsPerPage = ref(10);
const agentPage = ref(1);

function toggleAgentSort(key) {
  if (agentSortKey.value === key) {
    agentSortDir.value = agentSortDir.value === "asc" ? "desc" : "asc";
  } else {
    agentSortKey.value = key;
    agentSortDir.value = "asc";
  }
  agentPage.value = 1;
}

// Action Menu State
const activeActionMenu = ref(null);
function toggleActionMenu(agentName) {
  activeActionMenu.value =
    activeActionMenu.value === agentName ? null : agentName;
}
// Close menu when clicking outside (simple approach for now)
if (typeof window !== "undefined") {
  window.addEventListener("click", () => {
    activeActionMenu.value = null;
  });
}

// Modal State
const isAddModalOpen = ref(false);
const showConfirmModal = ref(false);
const selectedAgentForDelete = ref(null);
const modalMode = ref("add");
const activeEditOriginalName = ref(null);
const newAgentForm = ref({
  name: "",
  isCompany: false,
});

const companyLogos = {
  zanara: "/agent-logos/zanara-tech.png",
  awalul: "/agent-logos/awalul-tricipta.png",
  juragan: "/agent-logos/juragan-ht.png",
};

function hasLogo(agent) {
  return Boolean(agent?.logo && !brokenLogos.value.has(agent.logo));
}

function markLogoBroken(logo) {
  if (!logo) return;
  brokenLogos.value = new Set([...brokenLogos.value, logo]);
}

function rankClass(index) {
  return {
    "lb-rank--gold": index === 0,
    "lb-rank--silver": index === 1,
    "lb-rank--bronze": index === 2,
  };
}

// All refs start empty — data is loaded entirely from MySQL API
const agents = ref([]);
const performanceSummary = ref({
  totalAgents: 0,
  activeAgents: 0,
  totalCustomers: 0,
  avgPerAgent: 0,
  totalCommission: 0,
  commissionDelta: 0,
  periodCustomerGrowthPct: 0,
  agentTarget: 10,
  avgTarget: 200,
  commissionTarget: 140000000,
  agentTargetProgress: 0,
  avgTargetProgress: 0,
  commissionTargetProgress: 0,
  periodLabel: "",
});
const mappedAgents = computed(() => {
  const colors = agentColors.value;
  return agents.value.map((a, i) => ({
    ...a,
    color:
      a.color === "#0D1340" ? colors[0] : colors[i % colors.length] || a.color,
  }));
});

const maxAgent = computed(() =>
  Math.max(1, ...mappedAgents.value.map((a) => a.count)),
);
const maxAgentRevenue = computed(() =>
  Math.max(
    1,
    ...mappedAgents.value.map((a) => a.commissionValue || a.count * 100000),
  ),
);
const sortedAgents = computed(() => {
  return [...mappedAgents.value].sort((a, b) => b.count - a.count);
});

// Chart.js Computed Data
const agentChartData = computed(() => {
  const isCustomers = chartTab.value === "customers";
  const list = mappedAgents.value;
  return {
    labels: list.map((a) => a.short),
    datasets: [
      {
        label: isCustomers ? "Customers Acquired" : "Commission Earned",
        data: list.map((a) => (isCustomers ? a.count : a.commissionValue)),
        backgroundColor: list.map((a) => a.color),
        borderRadius: 6,
        borderSkipped: false,
      },
    ],
  };
});

const agentChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const v = ctx.parsed.y;
          if (chartTab.value === "revenue") {
            if (v >= 1_000_000) return `Rp ${(v / 1_000_000).toFixed(1)} Jt`;
            if (v >= 1_000) return `Rp ${Math.round(v / 1_000)} Rb`;
            return `Rp ${v}`;
          }
          return `${v} customers`;
        },
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        font: { family: "Inter", size: 11 },
        color: isDark.value ? "#64748B" : "#9BA3BF",
      },
      offset: true,
    },
    y: { display: false, beginAtZero: true },
  },
  datasets: {
    bar: {
      maxBarThickness: 56,
      barPercentage: 0.55,
      categoryPercentage: 0.7,
    },
  },
}));

const agentDetails = ref([]);

const mappedAgentDetails = computed(() => {
  const colors = agentColors.value;
  return agentDetails.value.map((a, i) => ({
    ...a,
    color:
      a.color === "#0D1340" ? colors[0] : colors[i % colors.length] || a.color,
  }));
});

// Safe topPerformer — guard against empty list to prevent TypeError crash
const topPerformer = computed(() => {
  const top = sortedAgents.value[0];
  if (!top)
    return {
      name: "Belum Ada Agen",
      initials: "--",
      color: "#0D1340",
      type: "-",
      customers: 0,
      commission: "0",
      progress: 0,
    };
  return (
    mappedAgentDetails.value.find((agent) => agent.name === top.name) ||
    top ||
    {}
  );
});

const tableAgents = ref([]);

const filteredTableAgents = computed(() => {
  const colors = agentColors.value;
  let list = tableAgents.value.map((a, i) => ({
    ...a,
    color:
      a.color === "#0D1340" ? colors[0] : colors[i % colors.length] || a.color,
  }));
  if (agentSearchQuery.value.trim()) {
    const q = agentSearchQuery.value.toLowerCase();
    list = list.filter(
      (a) =>
        a.name?.toLowerCase().includes(q) || a.type?.toLowerCase().includes(q),
    );
  }
  if (activeTableTab.value === "top5") {
    list.sort((a, b) => b.customers - a.customers);
    return list.slice(0, 5);
  }
  if (activeTableTab.value === "under") {
    list = list.filter((a) => a.status === "PENDING" || a.customers < 50);
  }
  // Apply sort
  if (agentSortKey.value) {
    list.sort((a, b) => {
      let av = a[agentSortKey.value];
      let bv = b[agentSortKey.value];
      if (
        ["customers", "active", "growth", "isolirRate"].includes(
          agentSortKey.value,
        )
      ) {
        av = Number(av) || 0;
        bv = Number(bv) || 0;
      } else if (typeof av === "string") {
        av = av.toLowerCase();
        bv = (bv || "").toLowerCase();
      }
      if (av < bv) return agentSortDir.value === "asc" ? -1 : 1;
      if (av > bv) return agentSortDir.value === "asc" ? 1 : -1;
      return 0;
    });
  }
  // Pagination
  const start = (agentPage.value - 1) * agentItemsPerPage.value;
  return list.slice(start, start + agentItemsPerPage.value);
});

const totalTableAgents = computed(() => {
  let list = [...tableAgents.value];
  if (agentSearchQuery.value.trim()) {
    const q = agentSearchQuery.value.toLowerCase();
    list = list.filter(
      (a) =>
        a.name?.toLowerCase().includes(q) || a.type?.toLowerCase().includes(q),
    );
  }
  if (activeTableTab.value === "under")
    return list.filter((a) => a.status === "PENDING" || a.customers < 50)
      .length;
  if (activeTableTab.value === "top5") return Math.min(5, list.length);
  return list.length;
});

const agentTotalPages = computed(
  () => Math.ceil(totalTableAgents.value / agentItemsPerPage.value) || 1,
);

// fetchData: period filter — only re-applies multiplier to real API data
function fetchData() {
  // Period filter is cosmetic for now; actual period filtering should come from API.
  // Re-trigger API load when period changes.
  loadAgents();
}

function addAgent() {
  modalMode.value = "add";
  newAgentForm.value = { name: "", isCompany: false };
  isAddModalOpen.value = true;
}

async function submitAddAgent() {
  if (!newAgentForm.value.name.trim()) return;
  const name = newAgentForm.value.name.trim();

  try {
    const payload = {
      nama: name,
      status: "AKTIF",
      komisi: 50000,
    };

    if (modalMode.value === "edit") {
      const existing = tableAgents.value.find(
        (a) => a.name === activeEditOriginalName.value,
      );
      if (!existing) return;
      await updateAgent(existing.id, {
        ...payload,
        status: existing.status === "SUSPENDED" ? "NONAKTIF" : "AKTIF",
      });
    } else {
      await createAgent(payload);
    }

    isAddModalOpen.value = false;
    await loadAgents();
  } catch (error) {
    console.warn("Failed to submit agent details", error);
  } finally {
    loading.value = false;
  }
}

function editAgent(agent) {
  modalMode.value = "edit";
  activeEditOriginalName.value = agent.name;
  newAgentForm.value = {
    name: agent.name,
    isCompany: agent.isCompany ?? false,
  };
  isAddModalOpen.value = true;
}

async function suspendAgent(agent) {
  const newStatus = agent.status === "SUSPENDED" ? "AKTIF" : "NONAKTIF";
  try {
    loading.value = true;
    await updateAgentStatus(agent.id, newStatus);
    await loadAgents();
  } catch (err) {
    console.error("Gagal mengubah status agen:", err);
  } finally {
    loading.value = false;
  }
}

function deleteAgent(agent) {
  selectedAgentForDelete.value = agent;
  showConfirmModal.value = true;
}

async function executeConfirmDelete() {
  if (!selectedAgentForDelete.value) return;
  try {
    loading.value = true;
    await deleteAgentRecord(selectedAgentForDelete.value.id);
    showConfirmModal.value = false;
    selectedAgentForDelete.value = null;
    await loadAgents();
  } catch (err) {
    console.error("Gagal menghapus agen:", err);
  } finally {
    loading.value = false;
  }
}

const handleTopbarSearch = (e) => {
  agentSearchQuery.value = e.detail || "";
};

const totalAgents = computed(() => performanceSummary.value.totalAgents);
const activeAgentCount = computed(() => performanceSummary.value.activeAgents);
const periodLabel = computed(() => {
  if (periodType.value === "month") {
    const currentMonth = new Date().toISOString().slice(0, 7);
    if (selectedMonth.value === currentMonth) {
      return "Bulan Ini";
    }
    return performanceSummary.value.periodLabel || "Bulan Ini";
  }
  if (periodType.value === "quarter") return "Kuartal Ini";
  if (periodType.value === "year") return "Tahun Ini";
  return "Semua Waktu";
});
const avgPerAgent = computed(() => performanceSummary.value.avgPerAgent);
const periodCustomerGrowthPct = computed(
  () => performanceSummary.value.periodCustomerGrowthPct,
);
const totalCommissionDisplay = computed(() => {
  const val = performanceSummary.value.totalCommission;
  if (val >= 1_000_000_000)
    return "Rp " + (val / 1_000_000_000).toFixed(1) + " M";
  if (val >= 1_000_000) return "Rp " + (val / 1_000_000).toFixed(1) + " Jt";
  return "Rp " + val.toLocaleString("id-ID");
});
const commissionDeltaDisplay = computed(() => {
  const val = performanceSummary.value.commissionDelta;
  const prefix = val > 0 ? "+" : "";
  if (Math.abs(val) >= 1_000_000)
    return prefix + "Rp " + (val / 1_000_000).toFixed(1) + " Jt";
  return prefix + "Rp " + val.toLocaleString("id-ID");
});
const signedPercent = (val) => {
  return val > 0 ? "+" + val + "%" : val + "%";
};

const loadAgents = async () => {
  loading.value = true;
  try {
    const data = await getAgentPerformanceByPeriod(period.value);
    const list = data?.agents || [];
    agents.value = list;
    agentDetails.value = list;
    tableAgents.value = list;

    if (data?.summary) {
      performanceSummary.value = data.summary;
    }
  } catch (error) {
    console.warn("Failed to load agent performance", error);
  } finally {
    loading.value = false;
  }
};

let observer;

onMounted(() => {
  window.addEventListener("topbar-search", handleTopbarSearch);

  // Track class changes on <html> for dynamic dark mode charts update
  observer = new MutationObserver(() => {
    isDark.value = document.documentElement.classList.contains("dark");
  });
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["class"],
  });

  loadAgents();
});

onUnmounted(() => {
  window.removeEventListener("topbar-search", handleTopbarSearch);
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
.agent-perf {
  display: flex;
  flex-direction: column;
  gap: 22px;
  max-width: 1400px;
}

/* Base Components */
.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.02));
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.page-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.02em;
}
.page-sub {
  font-size: 13px;
  color: var(--text-2);
}
.page-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.period-select {
  position: relative;
  display: flex;
  align-items: center;
}
.period-select select {
  appearance: none;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: 9px 32px 9px 14px;
  font-size: 13px;
  font-weight: 500;
  font-family: var(--font-body);
  color: var(--text-1);
  cursor: pointer;
  outline: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: border-color 0.2s;
}
.period-select select:hover {
  border-color: var(--text-3);
}
.period-select svg {
  position: absolute;
  right: 10px;
  width: 14px;
  height: 14px;
  color: var(--text-2);
  pointer-events: none;
}
.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 600;
  border: none;
  transition: all 0.15s;
  white-space: nowrap;
  cursor: pointer;
  font-family: var(--font-body);
}
.btn svg {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}
.btn--ghost {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.btn--ghost:hover {
  background: var(--bg);
  border-color: var(--text-3);
}
.btn--primary {
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.02em;
  box-shadow: 0 4px 12px rgba(13, 19, 64, 0.15);
}
.btn--primary:hover {
  background: var(--navy-mid);
  transform: translateY(-1px);
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.kpi-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.kpi-card:hover {
  box-shadow: var(--shadow-md, 0 4px 12px rgba(0, 0, 0, 0.05));
  transform: translateY(-2px);
}
.kpi-card--dark {
  background: linear-gradient(135deg, var(--navy) 0%, #1a235a 100%);
  border: none;
  color: #fff;
  box-shadow: 0 4px 16px rgba(13, 19, 64, 0.2);
}
.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.kpi-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
}
.kpi-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}
.kpi-icon svg {
  width: 18px;
  height: 18px;
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
  letter-spacing: -0.02em;
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
  padding: 3px 8px;
  border-radius: 99px;
  white-space: nowrap;
}
.badge-inline--green {
  background: #e8f8ef;
  color: var(--green-ok);
}
.badge-inline--gold {
  background: #fef3e0;
  color: var(--gold);
}
.badge-inline--teal {
  background: #e0f8f6;
  color: var(--teal);
}

.kpi-meta-text {
  font-size: 12px;
  color: var(--text-3);
}
.kpi-bar {
  height: 6px;
  background: var(--bg);
  border-radius: 99px;
  overflow: hidden;
  margin-top: 4px;
}
.kpi-bar__fill {
  height: 100%;
  background: var(--navy);
  border-radius: 99px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}
.kpi-bar-label {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-3);
  font-weight: 500;
}
.kpi-pct {
  color: var(--navy);
  font-weight: 700;
}

.success-rate {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 12px;
  border-radius: var(--r-sm);
  margin-top: auto;
}
.top-performer {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  margin-top: 4px;
}
.top-performer__logo {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 10px;
}
.top-performer__meta {
  min-width: 0;
}
.top-performer__name {
  color: #fff;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 800;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.top-performer__type {
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  margin-top: 3px;
  font-weight: 500;
}

.logo-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #fff;
  font-family: var(--font-display);
  font-weight: 800;
  flex-shrink: 0;
}
.logo-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 6px;
  background: #fff;
}
.logo-avatar--image {
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: inset 0 0 0 1px rgba(13, 19, 64, 0.03);
}

.main-row {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 16px;
  align-items: start;
}

.chart-card {
  padding: 24px;
  overflow: hidden;
}
.chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 12px;
}
.chart-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
}
.chart-sub {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 4px;
}
.chart-tabs {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  background: var(--bg);
  padding: 4px;
  border-radius: var(--r-sm);
}
.ctab {
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  background: transparent;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2);
  cursor: pointer;
  font-family: var(--font-body);
  transition: all 0.2s;
}
.ctab--active {
  background: #fff;
  color: var(--navy);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.ctab:hover:not(.ctab--active) {
  color: var(--text-1);
}

.chart-scroll-wrap {
  overflow-x: auto;
  padding-bottom: 12px;
  margin: 0 -8px;
  padding-left: 8px;
  padding-right: 8px;
}
.chart-scroll-wrap::-webkit-scrollbar {
  height: 6px;
}
.chart-scroll-wrap::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 4px;
}
.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 220px;
  position: relative;
  padding-bottom: 30px;
  border-bottom: 1px solid var(--border);
}
.bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  height: 100%;
}
.bar-col {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  position: relative;
  border-radius: 6px 6px 0 0;
}
.bar-col:hover .bar-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(-4px);
}
.bar-col:hover .bar-fill {
  filter: brightness(1.1);
}
.bar-tooltip {
  position: absolute;
  top: -34px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 6px;
  white-space: nowrap;
  opacity: 0;
  transition: all 0.2s;
  pointer-events: none;
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.bar-tooltip::after {
  content: "";
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 4px 4px 0;
  border-style: solid;
  border-color: var(--navy) transparent transparent transparent;
}
.bar-fill {
  width: 100%;
  border-radius: 6px 6px 0 0;
  transition:
    height 0.8s cubic-bezier(0.4, 0, 0.2, 1),
    filter 0.2s;
  min-height: 4px;
  box-shadow: inset 0 -2px 0 rgba(0, 0, 0, 0.1);
}
.bar-label {
  font-size: 10px;
  color: var(--text-2);
  font-weight: 600;
  font-family: var(--font-display);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}
.chart-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-2);
}
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.leaderboard-card {
  padding: 24px;
}
.lb-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.lb-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.lb-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px;
  border-radius: var(--r-sm);
  transition: background 0.2s;
}
.lb-item:hover {
  background: var(--bg);
}
.lb-rank {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  text-align: center;
  font-size: 12px;
  font-family: var(--font-display);
  font-weight: 800;
  color: var(--text-2);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.lb-rank--gold {
  background: #fef3e0;
  color: var(--gold);
}
.lb-rank--silver {
  background: #eef2ff;
  color: var(--navy);
}
.lb-rank--bronze {
  background: #fdeceb;
  color: var(--red-warn);
}

.lb-avatar {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  color: #fff;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 11px;
}
.lb-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.lb-name {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.lb-prog-bg {
  height: 5px;
  background: var(--border);
  border-radius: 99px;
  overflow: hidden;
}
.lb-prog-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}
.lb-count {
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 800;
  color: var(--text-1);
  flex-shrink: 0;
}

/* Horizontal Scroll Agent Cards */
.section-title-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 8px;
}
.section-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 800;
  color: var(--text-1);
}
.section-sub {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 4px;
}

.agents-scroll-wrap {
  overflow-x: auto;
  padding-bottom: 20px;
  margin: 0 -4px;
  padding-left: 4px;
  padding-right: 4px;
}
.agents-scroll-wrap::-webkit-scrollbar {
  height: 8px;
}
.agents-scroll-wrap::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
.agents-scroll-wrap::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.agents-grid {
  display: flex;
  gap: 16px;
  width: max-content;
}
.agent-card {
  width: 300px;
  flex: 0 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.agent-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}
.ac-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ac-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  font-size: 14px;
}
.ac-meta {
  flex: 1;
  min-width: 0;
}
.ac-name {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: var(--font-display);
}
.ac-type {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
  font-weight: 500;
}
.ac-stats {
  display: flex;
  align-items: stretch;
  background: var(--bg);
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  overflow: hidden;
}
.ac-stat {
  flex: 1;
  padding: 12px 8px;
  text-align: center;
}
.ac-stat-val {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
}
.ac-stat-lbl {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--text-3);
  margin-top: 4px;
  font-family: var(--font-display);
}
.ac-stat-divider {
  width: 1px;
  background: var(--border);
  flex-shrink: 0;
}
.ac-progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ac-prog-label {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-2);
  font-weight: 500;
}
.ac-prog-pct {
  font-weight: 800;
  font-family: var(--font-display);
}
.ac-prog-bg {
  height: 6px;
  background: var(--border);
  border-radius: 99px;
  overflow: hidden;
}
.ac-prog-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 1s;
}
.ac-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px dashed var(--border);
}
.ac-joined {
  font-size: 11px;
  color: var(--text-3);
}
.ac-view-btn {
  background: none;
  border: none;
  font-size: 12px;
  font-weight: 700;
  color: var(--navy);
  font-family: var(--font-display);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: opacity 0.2s;
}
.ac-view-btn:hover {
  opacity: 0.7;
}
.ac-view-btn svg {
  width: 14px;
  height: 14px;
}

/* Performance Table */
.section-card {
  padding: 24px;
}
.section-hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}
.table-tabs-row {
  display: flex;
  gap: 6px;
  background: var(--bg);
  padding: 4px;
  border-radius: var(--r-sm);
}
.table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.perf-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 860px;
}
.perf-table th {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  background: var(--surface-2);
  white-space: nowrap;
}
.th-right {
  text-align: right !important;
}
.th-center {
  text-align: center !important;
}
.td-right {
  text-align: right;
}
.td-center {
  text-align: center;
}

/* Sort Icons */
.th-sort {
  cursor: pointer !important;
  user-select: none;
}
.th-sort:hover {
  background: rgba(13, 19, 64, 0.025) !important;
  color: var(--navy) !important;
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

/* Table Footer */
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
.page-btn {
  min-width: 32px;
  height: 32px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2);
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
.page-num {
  font-size: 12px;
}
.pagination {
  display: flex;
  gap: 4px;
  align-items: center;
}

.perf-table td {
  padding: var(--table-row-padding, 16px);
  border-bottom: 1px solid var(--border);
  font-size: 13px;
  vertical-align: middle;
}
.perf-row:last-child td {
  border-bottom: none;
}
.perf-row:hover td {
  background: var(--bg);
}
.pa-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pa-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  font-size: 11px;
}
.pa-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}
.type-tag {
  font-size: 10px;
  font-weight: 700;
  font-family: var(--font-display);
  padding: 4px 8px;
  border-radius: 6px;
}
.type-tag--corp {
  background: #eef2ff;
  color: var(--navy);
}
.type-tag--ind {
  background: #f4f6fb;
  color: var(--text-2);
}
.td-num {
  font-family: var(--font-display);
  font-weight: 700;
}
.isolir-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.isolir-pct {
  font-weight: 600;
  width: 32px;
}
.isolir-warn {
  color: var(--gold);
  font-weight: 700;
}
.mini-bar {
  width: 60px;
  height: 5px;
  background: var(--border);
  border-radius: 99px;
  overflow: hidden;
}
.mini-bar__fill {
  height: 100%;
  border-radius: 99px;
  transition: width 1s;
}
.td-commission {
  font-family: var(--font-display);
  font-weight: 800;
  color: var(--green-ok);
  font-size: 13px;
}
.growth-tag {
  font-size: 11px;
  font-weight: 700;
  font-family: var(--font-display);
  padding: 4px 8px;
  border-radius: 99px;
}
.growth-tag--up {
  background: #e8f8ef;
  color: var(--green-ok);
}
.growth-tag--down {
  background: #fdeceb;
  color: var(--red-warn);
}
.badge {
  font-size: 10px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: var(--font-display);
  letter-spacing: 0.05em;
}

.btn-icon {
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-3);
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-icon:hover {
  background: var(--bg);
  color: var(--navy);
  border-color: var(--border);
}
.btn-icon svg {
  width: 16px;
  height: 16px;
}

/* Modal CSS */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(13, 19, 64, 0.4);
  backdrop-filter: blur(4px);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.modal-card {
  width: 100%;
  max-width: 440px;
  display: flex;
  flex-direction: column;
  animation: modalIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: var(--surface-2);
  border-radius: 0 0 var(--r-lg) var(--r-lg);
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-2);
  font-family: var(--font-display);
}
.form-input {
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  font-size: 13px;
  font-family: var(--font-body);
  outline: none;
  transition: border-color 0.2s;
  background: var(--surface);
}
.form-input:focus {
  border-color: var(--navy);
}

/* Action Menu CSS */
.action-menu-wrap {
  position: relative;
  display: inline-block;
}
.action-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 4px);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-lg);
  min-width: 140px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  padding: 6px;
  gap: 2px;
  transform-origin: top right;
}
.action-dropdown--up {
  top: auto;
  bottom: calc(100% + 4px);
  transform-origin: bottom right;
}
.action-dropdown-item {
  background: none;
  border: none;
  padding: 8px 12px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2);
  border-radius: 4px;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all var(--transition-fast, 0.15s);
}
.action-dropdown-item:hover {
  background: var(--bg);
  color: var(--text-1);
}
.action-dropdown-item--danger {
  color: var(--red-warn);
}
.action-dropdown-item--danger:hover {
  background: rgba(231, 76, 60, 0.1);
  color: var(--red-warn);
}

/* Dropdown Animation using Vue Transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition:
    opacity 0.15s cubic-bezier(0.16, 1, 0.3, 1),
    transform 0.15s cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-8px);
}
.action-dropdown--up.dropdown-enter-from,
.action-dropdown--up.dropdown-leave-to {
  transform: scale(0.95) translateY(8px);
}

@media (max-width: 1024px) {
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .main-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 640px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
  .btn-text {
    display: none;
  }
}

/* Empty States */
.chart-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 24px;
  color: var(--text-3);
  text-align: center;
}
.chart-empty-state svg {
  width: 40px;
  height: 40px;
  margin-bottom: 4px;
}
.chart-empty-state p {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-2);
}
.chart-empty-state small {
  font-size: 12px;
}
.lb-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 32px 16px;
  color: var(--text-3);
  text-align: center;
  font-size: 13px;
}
.agents-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 24px;
  text-align: center;
  border: 2px dashed var(--border);
  border-radius: var(--r-lg);
  margin: 8px 0;
}
.agents-empty-state svg {
  width: 48px;
  height: 48px;
  color: var(--text-3);
}
.agents-empty-state h3 {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}
.agents-empty-state p {
  font-size: 13px;
  color: var(--text-3);
  max-width: 320px;
}
.kpi-note {
  font-size: 10px;
  color: var(--text-3);
  margin-top: 6px;
  line-height: 1.4;
  font-style: italic;
}
.kpi-note--pending {
  color: var(--gold);
  font-style: normal;
  font-weight: 500;
}

/* ── Delete Modal (same as PackageView) ── */
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
  to   { transform: scale(1);    opacity: 1; }
}
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
</style>
