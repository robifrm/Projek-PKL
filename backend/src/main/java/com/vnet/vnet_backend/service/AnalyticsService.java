package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.Address;
import com.vnet.vnet_backend.entity.Agent;
import com.vnet.vnet_backend.entity.Customer;
import com.vnet.vnet_backend.entity.CustomerStatus;
import com.vnet.vnet_backend.entity.InternetPackage;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.AgentRepository;
import com.vnet.vnet_backend.repository.CustomerRepository;
import com.vnet.vnet_backend.repository.InternetPackageRepository;
import com.vnet.vnet_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private static final List<String> COLORS = List.of(
            "#0D1340", "#00B4A6", "#8B5CF6", "#F5A623", "#E91E8C", "#27AE60", "#6B9EF5", "#E74C3C"
    );
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final InternetPackageRepository packageRepository;
    private final UserRepository userRepository;

    public AnalyticsService(
            CustomerRepository customerRepository,
            AgentRepository agentRepository,
            InternetPackageRepository packageRepository,
            UserRepository userRepository
    ) {
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    private long lastCacheTimeOverview = 0;
    private Map<String, Object> cachedOverview = null;
    private long lastCacheTimeAgent = 0;
    private Map<String, Object> cachedAgent = null;
    private long lastCacheTimeAddress = 0;
    private Map<String, Object> cachedAddress = null;
    private final Object lock = new Object();

    // Cache TTL: 5 minutes — recompute only after mutation (invalidateCache) or TTL expiry
    private static final long CACHE_TTL = 300_000L;

    /** Call this after any data mutation (import, delete) to force fresh computation. */
    public void invalidateCache() {
        synchronized (lock) {
            cachedOverview = null;
            cachedAgent = null;
            cachedAddress = null;
            lastCacheTimeOverview = 0;
            lastCacheTimeAgent = 0;
            lastCacheTimeAddress = 0;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardOverview() {
        return getDashboardOverview(null);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardOverview(String monthParam) {
        synchronized (lock) {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> currentUserOpt = userRepository.findByUsernameIgnoreCase(currentUsername);
            User currentUser = currentUserOpt.orElse(null);

            boolean isAgent = currentUser != null && currentUser.getRole() == Role.AGENT;
            Long agentId = isAgent && currentUser.getAgent() != null ? currentUser.getAgent().getId() : null;

            long now = System.currentTimeMillis();
            if (!isAgent && monthParam == null && cachedOverview != null && (now - lastCacheTimeOverview < CACHE_TTL)) {
                return cachedOverview;
            }
            List<Customer> customers = customerRepository.findAllWithRelations();
            if (isAgent && agentId != null) {
                customers = customers.stream()
                        .filter(c -> c.getAgent() != null && c.getAgent().getId().equals(agentId))
                        .collect(Collectors.toList());
            }
            
            LocalDate customAnchor = null;
            if (monthParam != null && !monthParam.isBlank()) {
                try {
                    YearMonth ym = YearMonth.parse(monthParam);
                    customAnchor = ym.atEndOfMonth();
                } catch (Exception e) {
                    // ignore
                }
            }

            Map<String, Object> overview = new LinkedHashMap<>();
            overview.put("summary", getSummary(customers, customAnchor));
            overview.put("salesTrend", salesTrend(customers, customAnchor));
            overview.put("packages", packageDistribution(customers));

            List<Agent> agentsToQuery = agentRepository.findAll();
            if (isAgent && currentUser.getAgent() != null) {
                agentsToQuery = List.of(currentUser.getAgent());
            }
            overview.put("agents", agentPerformanceRows(customers, agentsToQuery, customAnchor));
            
            overview.put("recentRegistrations", recentRegistrations(customers));
            overview.put("cityDistribution", cityDistribution(customers));
            overview.put("cumulativeGrowth", cumulativeGrowth(customers, customAnchor));
            overview.put("revenueByPackage", revenueByPackage(customers));
            overview.put("statusDistribution", statusDistribution(customers));
            overview.put("churnRisk", churnRisk(customers));

            if (!isAgent && monthParam == null) {
                cachedOverview = overview;
                lastCacheTimeOverview = now;
            }
            return overview;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAgentPerformance() {
        return getAgentPerformance("month");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAgentPerformance(String period) {
        synchronized (lock) {
            List<Customer> customers = customerRepository.findAllWithRelations();
            DateRange currentRange = periodRange(period, customers, 0);
            DateRange previousRange = periodRange(period, customers, 1);
            List<Map<String, Object>> rows = agentPerformanceRows(customers, agentRepository.findAll(), currentRange, previousRange);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("agents", rows);
            payload.put("summary", agentSummary(rows, currentRange, previousRange));
            payload.put("period", normalizePeriod(period));
            payload.put("periodLabel", currentRange.label());
            return payload;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAddressInsights() {
        synchronized (lock) {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> currentUserOpt = userRepository.findByUsernameIgnoreCase(currentUsername);
            User currentUser = currentUserOpt.orElse(null);

            boolean isAgent = currentUser != null && currentUser.getRole() == Role.AGENT;
            Long agentId = isAgent && currentUser.getAgent() != null ? currentUser.getAgent().getId() : null;

            long now = System.currentTimeMillis();
            if (!isAgent && cachedAddress != null && (now - lastCacheTimeAddress < CACHE_TTL)) {
                return cachedAddress;
            }
            List<Customer> customers = customerRepository.findAllWithRelations();
            if (isAgent && agentId != null) {
                customers = customers.stream()
                        .filter(c -> c.getAgent() != null && c.getAgent().getId().equals(agentId))
                        .collect(Collectors.toList());
            }
            Map<Long, List<Customer>> grouped = customers.stream()
                    .filter(customer -> customer.getAddress() != null)
                    .collect(Collectors.groupingBy(customer -> customer.getAddress().getId(), LinkedHashMap::new, Collectors.toList()));

            List<Map<String, Object>> areas = new ArrayList<>();
            int index = 0;
            for (List<Customer> group : grouped.values()) {
                if (group.isEmpty()) {
                    continue;
                }

                Address address = group.get(0).getAddress();
                long total = group.size();
                long active = group.stream().filter(this::isActive).count();
                long isolir = group.stream().filter(this::isIsolir).count();
                long totalBandwidth = Math.round(group.stream()
                        .map(Customer::getPkg)
                        .filter(Objects::nonNull)
                        .mapToDouble(pkg -> Optional.ofNullable(pkg.getSpeed()).orElse(0.0))
                        .sum());
                long avgMbps = total == 0 ? 0 : Math.round((double) totalBandwidth / total);
                long isolirRate = percent(isolir, total);
                Map<String, Long> packageCounts = group.stream()
                        .map(Customer::getPkg)
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(InternetPackage::getName, LinkedHashMap::new, Collectors.counting()));

                String dominantPackage = packageCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("-");
                String topAgent = group.stream()
                        .map(Customer::getAgent)
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(Agent::getNama, Collectors.counting()))
                        .entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("-");

                String risk = isolirRate >= 20 ? "risk" : isolirRate >= 10 ? "watch" : "healthy";

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", "area-" + address.getId());
                row.put("street", fallback(address.getAlamat(), "Unassigned Street"));
                row.put("village", fallback(address.getKelurahan(), "-"));
                row.put("district", fallback(address.getKecamatan(), "-"));
                row.put("city", fallback(address.getKota(), "-"));
                row.put("customers", total);
                row.put("active", active);
                row.put("isolir", isolir);
                row.put("avgMbps", avgMbps);
                row.put("totalBandwidth", totalBandwidth);
                row.put("dominantPackage", dominantPackage);
                row.put("topAgent", topAgent);
                row.put("revenue", formatCurrency(group.stream().mapToDouble(customer -> Optional.ofNullable(customer.getPrice()).orElse(0.0)).sum()));
                row.put("risk", risk);
                row.put("riskLabel", risk.equals("risk") ? "Risk" : risk.equals("watch") ? "Watch" : "Healthy");
                row.put("color", color(index++));
                row.put("packages", packageCounts.entrySet().stream().map(entry -> {
                    Map<String, Object> pkg = new LinkedHashMap<>();
                    pkg.put("name", entry.getKey());
                    pkg.put("count", entry.getValue());
                    pkg.put("color", color(packageCounts.keySet().stream().toList().indexOf(entry.getKey())));
                    return pkg;
                }).toList());
                areas.add(row);
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("areas", areas);
            cachedAddress = payload;
            lastCacheTimeAddress = now;
            return payload;
        }
    }

    private Map<String, Object> getSummary(List<Customer> customers, LocalDate customAnchor) {
        long totalCustomers = customers.size();
        long totalActive = customers.stream().filter(this::isActive).count();
        long totalIsolir = customers.stream().filter(this::isIsolir).count();
        double totalProfit = customers.stream().mapToDouble(this::customerProfit).sum();
        LocalDate anchor = customAnchor != null ? customAnchor : anchorDate(customers);
        YearMonth currentMonth = YearMonth.from(anchor);
        YearMonth previousMonth = currentMonth.minusMonths(1);
        long monthlyNewCustomers = customers.stream()
                .filter(customer -> isSameMonth(customer.getTanggalRegistrasi(), currentMonth))
                .count();
        long previousMonthlyNewCustomers = customers.stream()
                .filter(customer -> isSameMonth(customer.getTanggalRegistrasi(), previousMonth))
                .count();
        double monthlyProfit = customers.stream()
                .filter(customer -> isSameMonth(activityDate(customer), currentMonth))
                .mapToDouble(this::customerProfit)
                .sum();
        double previousMonthlyProfit = customers.stream()
                .filter(customer -> isSameMonth(activityDate(customer), previousMonth))
                .mapToDouble(this::customerProfit)
                .sum();
        double monthlyRevenue = customers.stream()
                .filter(customer -> isSameMonth(activityDate(customer), currentMonth))
                .mapToDouble(this::customerRevenue)
                .sum();
        long monthlyUnits = customers.stream()
                .filter(customer -> isSameMonth(activityDate(customer), currentMonth))
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCustomers", totalCustomers);
        summary.put("totalActive", totalActive);
        summary.put("totalIsolir", totalIsolir);
        summary.put("totalAgents", agentRepository.count());
        summary.put("totalPackages", packageRepository.count());
        summary.put("totalProfit", totalProfit);
        summary.put("activeRate", percent(totalActive, totalCustomers));
        summary.put("isolirRate", percent(totalIsolir, totalCustomers));
        summary.put("monthlyNewCustomers", monthlyNewCustomers);
        summary.put("previousMonthlyNewCustomers", previousMonthlyNewCustomers);
        
        long previousTotalCustomers = totalCustomers - monthlyNewCustomers;
        long baseGrowthPct = 0;
        if (previousTotalCustomers > 0) {
            baseGrowthPct = Math.round((monthlyNewCustomers * 100.0) / previousTotalCustomers);
        } else if (monthlyNewCustomers > 0) {
            baseGrowthPct = 100;
        }
        summary.put("monthlyCustomerGrowthPct", baseGrowthPct);
        summary.put("monthlyProfit", monthlyProfit);
        summary.put("previousMonthlyProfit", previousMonthlyProfit);
        summary.put("monthlyProfitDelta", monthlyProfit - previousMonthlyProfit);
        summary.put("monthlyProfitGrowthPct", growthPct(monthlyProfit, previousMonthlyProfit));
        summary.put("monthlyRevenue", monthlyRevenue);
        summary.put("monthlyUnits", monthlyUnits);
        summary.put("periodMonthLabel", currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("id-ID"))));
        return summary;
    }

    private Map<String, Object> salesTrend(List<Customer> customers, LocalDate customAnchor) {
        LocalDate anchor = customAnchor != null ? customAnchor : anchorDate(customers);
        YearMonth currentMonth = YearMonth.from(anchor);

        List<String> labels = new ArrayList<>();
        List<Double> revenue = new ArrayList<>();
        List<Long> units = new ArrayList<>();
        List<Double> profit = new ArrayList<>();

        LocalDate earliest = customers.stream()
                .map(this::acquisitionDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(anchor.minusMonths(11));
        YearMonth startMonth = YearMonth.from(earliest);
        if (startMonth.isAfter(currentMonth)) startMonth = currentMonth;
        int totalMonths = (int) java.time.temporal.ChronoUnit.MONTHS.between(startMonth, currentMonth);
        if (totalMonths < 5) totalMonths = 5;

        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("id-ID"));
        for (int i = totalMonths; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            labels.add(month.format(labelFormat));
            List<Customer> monthCustomers = customers.stream()
                    .filter(customer -> isSameMonth(activityDate(customer), month))
                    .toList();
            units.add((long) monthCustomers.size());
            revenue.add(monthCustomers.stream().mapToDouble(this::customerRevenue).sum());
            profit.add(monthCustomers.stream().mapToDouble(this::customerProfit).sum());
        }

        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("labels", labels);
        trend.put("revenue", revenue);
        trend.put("units", units);
        trend.put("profit", profit);
        return trend;
    }

    private List<Map<String, Object>> packageDistribution(List<Customer> customers) {
        Map<String, List<Customer>> byPackage = customers.stream()
                .filter(c -> c.getPkg() != null)
                .collect(Collectors.groupingBy(c -> c.getPkg().getName()));

        long total = byPackage.values().stream().mapToInt(List::size).sum();
        int[] index = {0};
        
        return byPackage.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .map(entry -> {
                    long count = entry.getValue().size();
                    long active = entry.getValue().stream().filter(this::isActive).count();
                    long isolir = entry.getValue().stream().filter(this::isIsolir).count();
                    
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", entry.getKey());
                    row.put("count", count);
                    row.put("active", active);
                    row.put("isolir", isolir);
                    row.put("pct", percent(count, total));
                    row.put("color", color(index[0]++));
                    return row;
                })
                .toList();
    }

    private List<Map<String, Object>> agentPerformanceRows(List<Customer> customers, List<Agent> agents, LocalDate customAnchor) {
        YearMonth current = YearMonth.from(customAnchor != null ? customAnchor : anchorDate(customers));
        DateRange currentRange = new DateRange(current.atDay(1), current.atEndOfMonth(), current.toString());
        DateRange previousRange = new DateRange(current.minusMonths(1).atDay(1), current.minusMonths(1).atEndOfMonth(), current.minusMonths(1).toString());
        return agentPerformanceRows(customers, agents, currentRange, previousRange);
    }

    private List<Map<String, Object>> agentPerformanceRows(List<Customer> customers, List<Agent> agents, DateRange currentRange, DateRange previousRange) {
        Map<Long, List<Customer>> customersByAgent = customers.stream()
                .filter(customer -> customer.getAgent() != null)
                .collect(Collectors.groupingBy(customer -> customer.getAgent().getId()));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            List<Customer> assignedCustomers = customersByAgent.getOrDefault(agent.getId(), List.of());
            List<Customer> periodCustomers = filterByRange(assignedCustomers, currentRange);
            List<Customer> previousCustomers = filterByRange(assignedCustomers, previousRange);
            long total = assignedCustomers.size();
            long periodTotal = currentRange == null ? total : periodCustomers.size();
            long active = periodCustomers.stream().filter(this::isActive).count();
            long isolir = periodCustomers.stream().filter(this::isIsolir).count();
            double commission = commission(agent, periodCustomers);
            double previousCommission = commission(agent, previousCustomers);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", agent.getId());
            row.put("name", agent.getNama());
            row.put("short", shortName(agent.getNama()));
            row.put("initials", initials(agent.getNama()));
            boolean isCorp = isCompany(agent.getNama());
            row.put("type", isCorp ? "Corporate Partner" : "Individual Agent");
            row.put("commissionScheme", isCorp ? "Revenue Sharing 50%" : "Belum ada skema");
            row.put("isCompany", isCompany(agent.getNama()));
            row.put("color", color(i));
            row.put("status", agentStatus(agent));
            row.put("count", periodTotal);
            row.put("customers", periodTotal);
            row.put("previousCustomers", previousCustomers.size());
            row.put("totalCustomers", total);
            row.put("active", active);
            row.put("isolirRate", percent(isolir, periodTotal));
            row.put("commissionValue", commission);
            row.put("previousCommissionValue", previousCommission);
            row.put("commission", compactCurrency(commission));
            row.put("commissionText", formatCurrency(commission));
            row.put("progress", Math.min(100, percent(periodTotal, 200)));
            row.put("joined", "-");
            rows.add(row);
        }

        return rows.stream()
                .sorted(Comparator.comparingLong(row -> -asLong(row.get("customers"))))
                .toList();
    }

    private Map<String, Object> agentSummary(List<Map<String, Object>> rows, DateRange currentRange, DateRange previousRange) {
        long totalAgents = rows.size();
        long activeAgents = rows.stream().filter(row -> asLong(row.get("customers")) > 0).count();
        long totalCustomers = sumLong(rows, "customers");
        long totalActive = sumLong(rows, "active");
        double totalCommission = rows.stream()
                .mapToDouble(row -> asDouble(row.get("commissionValue")))
                .sum();
        double previousCommission = rows.stream()
                .mapToDouble(row -> asDouble(row.get("previousCommissionValue")))
                .sum();
        long previousCustomers = sumLong(rows, "previousCustomers");

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAgents", totalAgents);
        summary.put("activeAgents", activeAgents);
        summary.put("totalCustomers", totalCustomers);
        summary.put("totalActive", totalActive);
        summary.put("avgPerAgent", totalAgents == 0 ? 0 : Math.round((double) totalCustomers / totalAgents));
        summary.put("totalCommission", totalCommission);
        summary.put("previousCommission", previousCommission);
        summary.put("commissionDelta", totalCommission - previousCommission);
        summary.put("commissionGrowthPct", growthPct(totalCommission, previousCommission));
        summary.put("totalCommissionText", formatCurrency(totalCommission));
        summary.put("commissionCompact", compactCurrency(totalCommission));
        summary.put("periodLabel", currentRange.label());
        summary.put("previousPeriodLabel", previousRange.label());
        summary.put("agentTarget", 10);
        summary.put("avgTarget", 200);
        summary.put("commissionTarget", 140_000_000);
        summary.put("agentTargetProgress", Math.min(100, percent(totalAgents, 10)));
        summary.put("avgTargetProgress", Math.min(100, percent(totalAgents == 0 ? 0 : Math.round((double) totalCustomers / totalAgents), 200)));
        summary.put("commissionTargetProgress", Math.min(100, percent(Math.round(totalCommission), 140_000_000)));
        summary.put("periodCustomerGrowthPct", growthPct(totalCustomers, previousCustomers));
        return summary;
    }

    private DateRange periodRange(String period, List<Customer> customers, int offset) {
        LocalDate anchor = anchorDate(customers);
        if (period != null && period.matches("\\d{4}-\\d{2}")) {
            try {
                YearMonth targetMonth = YearMonth.parse(period).minusMonths(offset);
                DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("id-ID"));
                return new DateRange(targetMonth.atDay(1), targetMonth.atEndOfMonth(), targetMonth.format(labelFormat));
            } catch (Exception e) {
                // fallback
            }
        }
        String normalized = normalizePeriod(period);
        if ("all".equals(normalized)) {
            if (offset > 0) {
                 return new DateRange(LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 1), "N/A");
            }
            return new DateRange(LocalDate.of(2000, 1, 1), anchor.plusYears(1), "Semua Waktu");
        }
        if ("year".equals(normalized)) {
            LocalDate yearAnchor = anchor.minusYears(offset);
            LocalDate start = yearAnchor.withDayOfYear(1);
            LocalDate end = yearAnchor.with(TemporalAdjusters.lastDayOfYear());
            return new DateRange(start, end, String.valueOf(yearAnchor.getYear()));
        }
        if ("quarter".equals(normalized)) {
            LocalDate quarterAnchor = anchor.minusMonths((long) offset * 3);
            int quarter = quarterAnchor.get(IsoFields.QUARTER_OF_YEAR);
            int firstMonth = ((quarter - 1) * 3) + 1;
            LocalDate start = LocalDate.of(quarterAnchor.getYear(), firstMonth, 1);
            LocalDate end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
            return new DateRange(start, end, "Q" + quarter + " " + quarterAnchor.getYear());
        }

        YearMonth month = YearMonth.from(anchor).minusMonths(offset);
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("id-ID"));
        return new DateRange(month.atDay(1), month.atEndOfMonth(), month.format(labelFormat));
    }

    private String normalizePeriod(String period) {
        if (period != null && period.matches("\\d{4}-\\d{2}")) {
            return period;
        }
        if ("all".equalsIgnoreCase(period)) {
            return "all";
        }
        if ("quarter".equalsIgnoreCase(period)) {
            return "quarter";
        }
        if ("year".equalsIgnoreCase(period)) {
            return "year";
        }
        return "month";
    }

    private List<Customer> filterByRange(List<Customer> customers, DateRange range) {
        if (range == null) {
            return customers;
        }
        return customers.stream()
                .filter(customer -> range.contains(acquisitionDate(customer)))
                .toList();
    }

    private double commission(Agent agent, List<Customer> customers) {
        // Jika ada komisi per-customer yang dikonfigurasikan manual, gunakan itu
        Double perCustomerCommission = agent.getKomisi();
        if (perCustomerCommission != null && perCustomerCommission > 0) {
            return perCustomerCommission * customers.size();
        }
        // Corporate Agent: Revenue Sharing = 50% dari total harga paket
        if (isCompany(agent.getNama())) {
            return customers.stream()
                    .filter(c -> c.getPkg() != null && c.getPkg().getPrice() != null)
                    .mapToDouble(c -> c.getPkg().getPrice() * 0.50)
                    .sum();
        }
        // Individual Agent: skema belum dikonfirmasi, return 0
        return 0.0;
    }

    private String agentStatus(Agent agent) {
        if (agent.getStatus() == null) {
            return "PENDING";
        }
        return switch (agent.getStatus()) {
            case AKTIF -> "ACTIVE";
            case NONAKTIF -> "SUSPENDED";
            case RESIGN -> "PENDING";
        };
    }

    private List<Map<String, Object>> recentRegistrations(List<Customer> customers) {
        return customers.stream()
                .sorted(Comparator.comparing(Customer::getTanggalRegistrasi, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(customer -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", customer.getNama());
                    row.put("package", customer.getPkg() != null ? customer.getPkg().getName() : "-");
                    row.put("agent", customer.getAgent() != null ? customer.getAgent().getNama() : "-");
                    row.put("date", customer.getTanggalRegistrasi() != null ? customer.getTanggalRegistrasi().format(DATE_FORMAT) : "-");
                    row.put("status", customer.getStatus() != null ? customer.getStatus().name() : "-");
                    return row;
                })
                .toList();
    }

    /** Top cities by customer count */
    private List<Map<String, Object>> cityDistribution(List<Customer> customers) {
        Map<String, Long> counts = customers.stream()
                .filter(c -> c.getAddress() != null)
                .collect(Collectors.groupingBy(
                        c -> {
                            Address addr = c.getAddress();
                            if (addr.getKota() != null && !addr.getKota().isBlank()) return addr.getKota().trim();
                            if (addr.getKecamatan() != null && !addr.getKecamatan().isBlank()) return addr.getKecamatan().trim();
                            if (addr.getKelurahan() != null && !addr.getKelurahan().isBlank()) return addr.getKelurahan().trim();
                            return "Lainnya";
                        },
                        Collectors.counting()
                ));
        long total = customers.size();
        int[] idx = {0};
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("city", e.getKey());
                    row.put("count", e.getValue());
                    row.put("pct", percent(e.getValue(), total));
                    row.put("color", color(idx[0]++));
                    return row;
                })
                .toList();
    }

    /** Month-by-month cumulative subscriber growth */
    private Map<String, Object> cumulativeGrowth(List<Customer> customers, LocalDate customAnchor) {
        LocalDate anchor = customAnchor != null ? customAnchor : anchorDate(customers);
        YearMonth currentMonth = YearMonth.from(anchor);
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("id-ID"));

        LocalDate earliest = customers.stream()
                .map(this::acquisitionDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(anchor.minusMonths(11));
        YearMonth startMonth = YearMonth.from(earliest);
        if (startMonth.isAfter(currentMonth)) startMonth = currentMonth;
        int totalMonths = (int) java.time.temporal.ChronoUnit.MONTHS.between(startMonth, currentMonth);
        if (totalMonths < 5) totalMonths = 5;
        final int finalTotalMonths = totalMonths;

        List<String> labels = new ArrayList<>();
        List<Long> cumulative = new ArrayList<>();
        long running = 0;
        for (int i = totalMonths; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            labels.add(month.format(labelFmt));
            long newThisMonth = customers.stream()
                    .filter(c -> isSameMonth(acquisitionDate(c), month))
                    .count();
            running += newThisMonth;
            cumulative.add(running);
        }
        // Offset so chart starts from actual first recorded, not 0
        long baseline = customers.stream()
                .filter(c -> {
                    LocalDate d = acquisitionDate(c);
                    return d != null && YearMonth.from(d).isBefore(currentMonth.minusMonths(finalTotalMonths));
                }).count();
        List<Long> adjusted = cumulative.stream().map(v -> v + baseline).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", adjusted);
        return result;
    }

    /** Revenue grouped by package name (top 8) */
    private List<Map<String, Object>> revenueByPackage(List<Customer> customers) {
        Map<String, Double> revMap = customers.stream()
                .filter(c -> c.getPkg() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getPkg().getName(),
                        Collectors.summingDouble(this::customerRevenue)
                ));
        int[] idx = {0};
        return revMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(8)
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", e.getKey());
                    row.put("revenue", Math.round(e.getValue()));
                    row.put("revenueLabel", compactCurrency(e.getValue()));
                    row.put("color", color(idx[0]++));
                    return row;
                })
                .toList();
    }

    /** Active vs Isolir counts for donut chart */
    private Map<String, Object> statusDistribution(List<Customer> customers) {
        long active = customers.stream().filter(this::isActive).count();
        long isolir = customers.stream().filter(this::isIsolir).count();
        long total = customers.size();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("active", active);
        result.put("isolir", isolir);
        result.put("activePct", percent(active, total));
        result.put("isolirPct", percent(isolir, total));
        return result;
    }

    /** Churn risk: isolir customers sorted by how long they've been inactive */
    private List<Map<String, Object>> churnRisk(List<Customer> customers) {
        LocalDate today = anchorDate(customers);
        return customers.stream()
                .filter(this::isIsolir)
                .sorted(Comparator.comparing(
                        c -> c.getTanggalRegistrasi() != null ? c.getTanggalRegistrasi() : LocalDate.of(2000, 1, 1),
                        Comparator.naturalOrder()
                ))
                .limit(5)
                .map(c -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", c.getNama());
                    row.put("package", c.getPkg() != null ? c.getPkg().getName() : "-");
                    row.put("agent", c.getAgent() != null ? c.getAgent().getNama() : "-");
                    long daysSince = c.getTanggalRegistrasi() != null
                            ? java.time.temporal.ChronoUnit.DAYS.between(c.getTanggalRegistrasi(), today)
                            : 0;
                    row.put("daysSince", daysSince);
                    String risk = daysSince > 60 ? "HIGH" : daysSince > 30 ? "MEDIUM" : "LOW";
                    row.put("risk", risk);
                    return row;
                })
                .toList();
    }

    private LocalDate anchorDate(List<Customer> customers) {
        // Always use today's real date — period filters ("This Month", "This Quarter", "This Year")
        // must reflect the actual current calendar, not just the latest entry in the database.
        return LocalDate.now();
    }

    private LocalDate activityDate(Customer customer) {
        if (customer.getTanggalAktivasi() != null) {
            return customer.getTanggalAktivasi();
        }
        return customer.getTanggalRegistrasi();
    }

    private LocalDate acquisitionDate(Customer customer) {
        if (customer.getTanggalRegistrasi() != null) {
            return customer.getTanggalRegistrasi();
        }
        return customer.getTanggalAktivasi();
    }

    private boolean isSameMonth(LocalDate date, YearMonth month) {
        return date != null && YearMonth.from(date).equals(month);
    }

    private double customerRevenue(Customer customer) {
        if (customer.getPrice() != null && customer.getPrice() > 0) {
            return customer.getPrice();
        }
        if (customer.getPkg() != null && customer.getPkg().getPrice() != null && customer.getPkg().getPrice() > 0) {
            return customer.getPkg().getPrice();
        }
        return customerProfit(customer);
    }

    private double customerProfit(Customer customer) {
        return 0.0;
    }

    private boolean isActive(Customer customer) {
        return customer.getStatus() == CustomerStatus.ACTIVE;
    }

    private boolean isIsolir(Customer customer) {
        return customer.getStatus() == CustomerStatus.ISOLIR || Boolean.TRUE.equals(customer.getIsolir());
    }

    private String color(int index) {
        return COLORS.get(Math.floorMod(index, COLORS.size()));
    }

    private long percent(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return Math.round((value * 100.0) / total);
    }

    private long growthPct(long current, long previous) {
        return growthPct((double) current, (double) previous);
    }

    private long growthPct(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100 : 0;
        }
        return Math.round(((current - previous) / previous) * 100);
    }

    private long sumLong(List<Map<String, Object>> rows, String key) {
        return rows.stream().mapToLong(row -> asLong(row.get(key))).sum();
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0;
    }

    private double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean isCompany(String value) {
        String name = Optional.ofNullable(value).orElse("").toUpperCase(Locale.ROOT).trim();
        return name.startsWith("PT") || name.startsWith("CV") || name.contains(" CORP") || name.contains(" LTD");
    }

    private String shortName(String value) {
        String name = fallback(value, "-").replace("PT.", "").replace("PT ", "").trim();
        String[] words = name.split("\\s+");
        return words.length == 0 ? name : words[0];
    }

    private String initials(String value) {
        String[] words = fallback(value, "NA").trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank() && builder.length() < 2) {
                builder.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return builder.isEmpty() ? "NA" : builder.toString();
    }

    private String compactCurrency(double value) {
        if (value >= 1_000_000) {
            return String.format(Locale.US, "%.1f Jt", value / 1_000_000);
        }
        if (value >= 1_000) {
            return String.format(Locale.US, "%.0f Rb", value / 1_000);
        }
        return String.format(Locale.US, "%.0f", value);
    }

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(value);
    }

    private record DateRange(LocalDate start, LocalDate end, String label) {
        private boolean contains(LocalDate date) {
            return date != null && !date.isBefore(start) && !date.isAfter(end);
        }
    }
}
