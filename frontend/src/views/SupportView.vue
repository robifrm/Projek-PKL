<template>
  <div class="support">
    <div class="page-header">
      <div>
        <h1 class="page-title">Support Center</h1>
        <p class="page-sub">
          Submit tickets, browse knowledge base, and reach the VNet technical
          team.
        </p>
      </div>
      <button class="btn btn--primary" @click="showNewTicket = true">
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
        New Ticket
      </button>
    </div>

    <!-- KPI Row -->
    <div class="kpi-row">
      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Open Tickets</span>
          <div class="kpi-icon kpi-icon--gold">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
          </div>
        </div>
        <div class="kpi-value" style="color: var(--gold)">3</div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--gold">2 Urgent</span>
        </div>
      </div>
      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Resolved (30d)</span>
          <div class="kpi-icon kpi-icon--green">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </div>
        </div>
        <div class="kpi-value" style="color: var(--green-ok)">47</div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--green">96% resolved</span>
        </div>
      </div>
      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">Avg. Response</span>
          <div class="kpi-icon kpi-icon--blue">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            >
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
          </div>
        </div>
        <div class="kpi-value">2.4h</div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--green">Within SLA</span>
        </div>
      </div>
      <div class="card kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">System Status</span>
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
        <div
          class="kpi-value"
          style="color: var(--green-ok); font-size: 20px"
        >
          All Operational
        </div>
        <div class="kpi-meta">
          <span class="badge-inline badge-inline--green">99.8% uptime</span>
        </div>
      </div>
    </div>

    <!-- Tickets + New Ticket Form -->
    <div class="main-row">
      <!-- Ticket List -->
      <div class="card tickets-card">
        <div class="card-header">
          <div class="chart-title">Active Tickets</div>
          <div class="tab-row">
            <button
              class="ctab"
              :class="{ 'ctab--active': ticketTab === 'open' }"
              @click="ticketTab = 'open'"
            >
              Open (3)
            </button>
            <button
              class="ctab"
              :class="{ 'ctab--active': ticketTab === 'closed' }"
              @click="ticketTab = 'closed'"
            >
              Resolved
            </button>
          </div>
        </div>
        <div class="ticket-list">
          <div
            class="ticket-item"
            v-for="t in filteredTickets"
            :key="t.id"
            @click="selectedTicket = t"
          >
            <div class="ticket-top">
              <span class="ticket-id">#{{ t.id }}</span>
              <span
                class="badge"
                :class="
                  t.priority === 'URGENT'
                    ? 'badge--red'
                    : t.priority === 'HIGH'
                      ? 'badge--gold'
                      : 'badge--navy'
                "
                >{{ t.priority }}</span
              >
            </div>
            <div class="ticket-title">{{ t.title }}</div>
            <div class="ticket-meta">
              <span class="ticket-cat">{{ t.category }}</span>
              <span class="ticket-time">{{ t.time }}</span>
            </div>
            <div class="ticket-status-row">
              <span
                class="ticket-status"
                :class="
                  t.status === 'OPEN'
                    ? 'ticket-status--open'
                    : t.status === 'IN_PROGRESS'
                      ? 'ticket-status--progress'
                      : 'ticket-status--done'
                "
              >
                {{
                  t.status === "IN_PROGRESS"
                    ? "In Progress"
                    : t.status.charAt(0) + t.status.slice(1).toLowerCase()
                }}
              </span>
              <span class="ticket-assignee">{{ t.assignee }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar: New Ticket + Contacts -->
      <div class="sidebar-col">
        <!-- New Ticket Form -->
        <div
          class="card new-ticket-card"
          :class="{ 'new-ticket-card--open': showNewTicket }"
        >
          <div
            class="card-header"
            style="cursor: pointer"
            @click="showNewTicket = !showNewTicket"
          >
            <div class="chart-title">Submit New Ticket</div>
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              style="
                width: 16px;
                height: 16px;
                color: var(--text-3);
                transition: transform 0.2s;
              "
              :style="{ transform: showNewTicket ? 'rotate(180deg)' : 'none' }"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>
          <div class="ticket-form" v-show="showNewTicket">
            <div class="form-group">
              <label class="form-label">Issue Title</label>
              <input
                class="form-input"
                v-model="newTicket.title"
                placeholder="Describe the issue briefly"
              />
            </div>
            <div class="form-group">
              <label class="form-label">Category</label>
              <div class="select-wrap">
                <select class="form-input" v-model="newTicket.category">
                  <option value="">Select category...</option>
                  <option>Database / Import</option>
                  <option>Network Connectivity</option>
                  <option>Agent Management</option>
                  <option>Billing / Commission</option>
                  <option>Other</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Priority</label>
              <div class="priority-btns">
                <button
                  v-for="p in ['LOW', 'HIGH', 'URGENT']"
                  :key="p"
                  class="prio-btn"
                  :class="{
                    'prio-btn--active': newTicket.priority === p,
                    ['prio-btn--' + p.toLowerCase()]: true,
                  }"
                  @click="newTicket.priority = p"
                >
                  {{ p }}
                </button>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Description</label>
              <textarea
                class="form-input form-textarea"
                v-model="newTicket.desc"
                placeholder="Provide full details about the issue..."
                rows="4"
              ></textarea>
            </div>
            <button
              class="btn btn--primary"
              style="width: 100%; justify-content: center"
              @click="submitTicket"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              >
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
              Submit Ticket
            </button>
          </div>
        </div>

        <!-- Contact & Status -->
        <div class="card contact-card">
          <div class="chart-title" style="margin-bottom: 12px">
            Technical Contact
          </div>
          <div class="contact-list">
            <a href="mailto:support@victorynetwork.id" class="contact-item">
              <div class="contact-icon contact-icon--blue">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linecap="round"
                >
                  <path
                    d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"
                  />
                  <polyline points="22,6 12,13 2,6" />
                </svg>
              </div>
              <div>
                <div class="contact-label">Email Support</div>
                <div class="contact-val">support@victorynetwork.id</div>
              </div>
            </a>
            <a href="tel:+622112345678" class="contact-item">
              <div class="contact-icon contact-icon--green">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linecap="round"
                >
                  <path
                    d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81a19.79 19.79 0 01-3.07-8.63A2 2 0 012 .18h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L6.09 7.91a16 16 0 006 6l1.19-1.19a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 14.92z"
                  />
                </svg>
              </div>
              <div>
                <div class="contact-label">Phone (08.00-17.00 WIB)</div>
                <div class="contact-val">+62 21 1234 5678</div>
              </div>
            </a>
            <a href="#" class="contact-item">
              <div class="contact-icon contact-icon--teal">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linecap="round"
                >
                  <path
                    d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"
                  />
                </svg>
              </div>
              <div>
                <div class="contact-label">Live Chat</div>
                <div class="contact-val">Available now - ~5 min wait</div>
              </div>
            </a>
          </div>
        </div>

        <!-- System Status -->
        <div class="card status-card">
          <div class="chart-title" style="margin-bottom: 12px">
            System Status
          </div>
          <div class="status-list">
            <div class="status-row" v-for="s in systemStatus" :key="s.name">
              <div class="status-dot" :class="'status-dot--' + s.status"></div>
              <span class="status-name">{{ s.name }}</span>
              <span class="status-uptime">{{ s.uptime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- FAQ -->
    <div class="card faq-card">
      <div class="section-hdr">
        <div>
          <div class="chart-title">Knowledge Base</div>
          <div class="chart-sub">Common issues and troubleshooting guides</div>
        </div>
      </div>
      <div class="faq-grid">
        <div
          class="faq-item"
          v-for="f in faqs"
          :key="f.q"
          @click="f.open = !f.open"
        >
          <div class="faq-q">
            <span>{{ f.q }}</span>
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              class="faq-icon"
              :class="{ 'faq-icon--open': f.open }"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>
          <div class="faq-a" v-show="f.open">{{ f.a }}</div>
        </div>
      </div>
    </div>

    <!-- Submit success toast -->
    <div class="save-toast" :class="{ 'save-toast--show': ticketSubmitted }">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
        stroke-linecap="round"
      >
        <polyline points="20 6 9 17 4 12" />
      </svg>
      Ticket submitted! Team will respond within 2 hours.
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from "vue";

const showNewTicket = ref(true);
const ticketTab = ref("open");
const ticketSubmitted = ref(false);
const selectedTicket = ref(null);

const newTicket = ref({ title: "", category: "", priority: "LOW", desc: "" });

const tickets = reactive([
  {
    id: 442,
    title: "Database sync failing on import >500 rows",
    category: "Database / Import",
    priority: "URGENT",
    status: "IN_PROGRESS",
    assignee: "VNet Tech Team",
    time: "12 min ago",
    open: true,
  },
  {
    id: 445,
    title: "New agent account setup required",
    category: "Agent Management",
    priority: "HIGH",
    status: "OPEN",
    assignee: "Unassigned",
    time: "45 min ago",
    open: true,
  },
  {
    id: 447,
    title: "Fiber Node config review for Sector G",
    category: "Network Connectivity",
    priority: "NORMAL",
    status: "OPEN",
    assignee: "VNet NOC",
    time: "2 hours ago",
    open: true,
  },
  {
    id: 438,
    title: "Commission calculation discrepancy April",
    category: "Billing / Commission",
    priority: "HIGH",
    status: "RESOLVED",
    assignee: "Finance Team",
    time: "2 days ago",
    open: false,
  },
  {
    id: 432,
    title: "Customer import schema error on batch TX-81",
    category: "Database / Import",
    priority: "URGENT",
    status: "RESOLVED",
    assignee: "VNet Tech Team",
    time: "5 days ago",
    open: false,
  },
]);

const filteredTickets = computed(() =>
  tickets.filter((t) => (ticketTab.value === "open" ? t.open : !t.open)),
);

function submitTicket() {
  if (!newTicket.value.title) return;
  ticketSubmitted.value = true;
  newTicket.value = { title: "", category: "", priority: "LOW", desc: "" };
  setTimeout(() => {
    ticketSubmitted.value = false;
  }, 3000);
}

const systemStatus = [
  { name: "Database Cluster", status: "ok", uptime: "99.9%" },
  { name: "Sync Engine", status: "ok", uptime: "99.8%" },
  { name: "API Gateway", status: "ok", uptime: "100%" },
  { name: "Import Service", status: "warn", uptime: "98.4%" },
  { name: "Report Service", status: "ok", uptime: "99.7%" },
];

const faqs = reactive([
  {
    q: 'How do I fix "schema mismatch" during import?',
    a: "Ensure your Excel file columns match exactly: Nama, Cust ID, Package, Status, Agen, Tanggal Registrasi. Download the template from Data Import for the correct format.",
    open: false,
  },
  {
    q: "What is the Isolir rate and how is it calculated?",
    a: "Isolir Rate = (Isolir customers / Total customers) x 100. A rate above 20% triggers a WARNING. Isolir means the customer has been suspended, usually due to unpaid bills.",
    open: false,
  },
  {
    q: "How do I add a new agent/reseller?",
    a: 'Go to Agent Performance, then click "Add Agent". Fill in the agent name, type (Individual or Corporate), and contact details. The system assigns a unique agent code automatically.',
    open: false,
  },
  {
    q: "What file formats does Data Import support?",
    a: "Only .xlsx (Excel) files are supported. CSV is not supported due to encoding issues with Indonesian characters. Maximum file size is 50MB and up to 1,200 rows per import.",
    open: false,
  },
  {
    q: "How do I reset a customer's password or status?",
    a: 'Navigate to Customer Management, find the customer, click the action button, then select "Edit Status". You can toggle between Active and Isolir from there.',
    open: false,
  },
  {
    q: "Why is my commission calculation different from expected?",
    a: "Commission is estimated based on active customers only. Isolir customers are excluded. The calculation runs nightly. Contact finance@victorynetwork.id for disputes.",
    open: false,
  },
]);
</script>

<style scoped>
.support {
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
  transition: all 0.15s;
  cursor: pointer;
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
}
.btn--primary:hover {
  background: var(--navy-mid);
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
}
.kpi-icon svg {
  width: 16px;
  height: 16px;
}
.kpi-icon--gold {
  background: #fef3e0;
  color: var(--gold);
}
.kpi-icon--green {
  background: #e8f8ef;
  color: var(--green-ok);
}
.kpi-icon--blue {
  background: #eef2ff;
  color: var(--navy);
}
.kpi-icon--teal {
  background: #e0f8f6;
  color: var(--teal);
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
.badge-inline--gold {
  background: #fef3e0;
  color: var(--gold);
}

.main-row {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  align-items: start;
}

.tickets-card {
  padding: 0;
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}
.chart-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-1);
}
.chart-sub {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}
.tab-row {
  display: flex;
  gap: 4px;
}
.ctab {
  padding: 5px 12px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: none;
  font-size: 12px;
  color: var(--text-2);
  cursor: pointer;
  font-family: var(--font-body);
  transition: all 0.15s;
}
.ctab--active {
  background: var(--navy);
  color: #fff;
  border-color: var(--navy);
}

.ticket-list {
  display: flex;
  flex-direction: column;
}
.ticket-item {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.15s;
}
.ticket-item:last-child {
  border-bottom: none;
}
.ticket-item:hover {
  background: var(--bg);
}
.ticket-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.ticket-id {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-3);
}
.ticket-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  line-height: 1.4;
  margin-bottom: 6px;
}
.ticket-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.ticket-cat {
  font-size: 11px;
  color: var(--navy);
  font-weight: 600;
}
.ticket-time {
  font-size: 11px;
  color: var(--text-3);
}
.ticket-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.ticket-status {
  font-size: 11px;
  font-weight: 600;
  font-family: var(--font-display);
}
.ticket-status--open {
  color: var(--gold);
}
.ticket-status--progress {
  color: var(--teal);
}
.ticket-status--done {
  color: var(--green-ok);
}
.ticket-assignee {
  font-size: 11px;
  color: var(--text-3);
}

.sidebar-col {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.new-ticket-card {
  padding: 0;
  overflow: hidden;
}
.ticket-form {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: var(--text-3);
  font-family: var(--font-display);
}
.form-input {
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  font-size: 13px;
  font-family: var(--font-body);
  color: var(--text-1);
  background: var(--surface);
  outline: none;
  transition: border-color 0.15s;
  width: 100%;
}
.form-input:focus {
  border-color: var(--navy);
}
.form-textarea {
  resize: vertical;
  min-height: 80px;
}
.select-wrap select {
  width: 100%;
  appearance: none;
}
.priority-btns {
  display: flex;
  gap: 6px;
}
.prio-btn {
  flex: 1;
  padding: 7px;
  border-radius: var(--r-sm);
  border: 1px solid var(--border);
  background: none;
  font-size: 11px;
  font-weight: 700;
  font-family: var(--font-display);
  cursor: pointer;
  transition: all 0.15s;
  letter-spacing: 0;
}
.prio-btn--low.prio-btn--active {
  background: #eef2ff;
  border-color: var(--navy);
  color: var(--navy);
}
.prio-btn--high.prio-btn--active {
  background: #fef3e0;
  border-color: var(--gold);
  color: var(--gold);
}
.prio-btn--urgent.prio-btn--active {
  background: #fdeceb;
  border-color: var(--red-warn);
  color: var(--red-warn);
}

.contact-card {
  padding: 20px;
}
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.contact-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.contact-icon svg {
  width: 16px;
  height: 16px;
}
.contact-icon--blue {
  background: #eef2ff;
  color: var(--navy);
}
.contact-icon--green {
  background: #e8f8ef;
  color: var(--green-ok);
}
.contact-icon--teal {
  background: #e0f8f6;
  color: var(--teal);
}
.contact-label {
  font-size: 11px;
  color: var(--text-3);
}
.contact-val {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
  margin-top: 1px;
}

.status-card {
  padding: 20px;
}
.status-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.status-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-dot--ok {
  background: var(--green-ok);
}
.status-dot--warn {
  background: var(--gold);
}
.status-dot--down {
  background: var(--red-warn);
}
.status-name {
  flex: 1;
  font-size: 13px;
  color: var(--text-1);
}
.status-uptime {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-3);
}

.faq-card {
  padding: 20px;
}
.section-hdr {
  margin-bottom: 18px;
}
.faq-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.faq-item {
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
  cursor: pointer;
}
.faq-q {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  gap: 10px;
}
.faq-q span {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  line-height: 1.4;
}
.faq-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: var(--text-3);
  transition: transform 0.2s;
}
.faq-icon--open {
  transform: rotate(180deg);
}
.faq-a {
  padding: 0 16px 14px;
  font-size: 12px;
  color: var(--text-2);
  line-height: 1.7;
  border-top: 1px solid var(--border);
  padding-top: 12px;
}

.save-toast {
  position: fixed;
  bottom: 28px;
  right: 28px;
  background: var(--navy);
  color: #fff;
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  padding: 12px 20px;
  border-radius: var(--r-md);
  display: flex;
  align-items: center;
  gap: 8px;
  transform: translateY(80px);
  opacity: 0;
  transition: all 0.3s;
  z-index: 999;
  box-shadow: var(--shadow-lg);
}
.save-toast svg {
  width: 16px;
  height: 16px;
}
.save-toast--show {
  transform: translateY(0);
  opacity: 1;
}

@media (max-width: 1024px) {
  .main-row {
    grid-template-columns: 1fr;
  }
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .faq-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 640px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
}
</style>
