import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

enum PlanType { BASIC, PREMIUM, ENTERPRISE }
enum BillingCycle { MONTHLY, ANNUAL }

class PricingConfig {
    private final double basePrice;
    private final double annualDiscount;

    public PricingConfig(double basePrice, double annualDiscount) {
        this.basePrice = basePrice;
        this.annualDiscount = annualDiscount;
    }

    public double getBasePrice() { return basePrice; }
    public double getAnnualDiscount() { return annualDiscount; }
}

class MonthlyBill {
    private final YearMonth month;
    private final double amount;

    public MonthlyBill(YearMonth month, double amount) {
        this.month = month;
        this.amount = amount;
    }

    public YearMonth getMonth() { return month; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("%s: $%.2f", month, amount);
    }
}

interface PricingStrategy {
    default List<MonthlyBill> calculateMonthlyBills(PricingConfig config, LocalDate startDate) {
        return Collections.emptyList();
    }
    default double calculateAnnualCost(PricingConfig config) {
        return 0.0;
    }
}

class MonthlyPricingStrategy implements PricingStrategy {
    @Override
    public List<MonthlyBill> calculateMonthlyBills(PricingConfig config, LocalDate startDate) {
        List<MonthlyBill> bills = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        for (int i = 0; i < 12; i++) {
            bills.add(new MonthlyBill(current, config.getBasePrice()));
            current = current.plusMonths(1);
        }
        return bills;
    }
}

class AnnualPricingStrategy implements PricingStrategy {
    @Override
    public double calculateAnnualCost(PricingConfig config) {
        double yearlyCost = config.getBasePrice() * 12;
        return yearlyCost * (1 - config.getAnnualDiscount());
    }
}

class Product {
    private final String name;
    private final Map<PlanType, PricingConfig> pricingMap;

    public Product(String name, Map<PlanType, PricingConfig> pricingMap) {
        this.name = name;
        this.pricingMap = pricingMap;
    }

    public String getName() { return name; }
    public PricingConfig getConfig(PlanType planType) { return pricingMap.get(planType); }
}

class Subscription {
    private final Product product;
    private final PlanType planType;
    private final LocalDate startDate;
    private final BillingCycle billingCycle;
    private List<MonthlyBill> monthlyBills;
    private double annualCost;

    public Subscription(Product product, PlanType planType, LocalDate startDate, BillingCycle billingCycle) {
        this.product = product;
        this.planType = planType;
        this.startDate = startDate;
        this.billingCycle = billingCycle;
    }

    public void generateCost(PricingStrategy strategy, PricingConfig config) {
        if (billingCycle == BillingCycle.MONTHLY) {
            monthlyBills = strategy.calculateMonthlyBills(config, startDate);
        } else {
            annualCost = strategy.calculateAnnualCost(config);
        }
    }

    public void printReport() {
        System.out.println("=".repeat(60));
        System.out.printf("Product: %s | Plan: %s | Cycle: %s%n", product.getName(), planType, billingCycle);
        if (billingCycle == BillingCycle.MONTHLY) {
            for (MonthlyBill bill : monthlyBills) System.out.println("  " + bill);
        } else {
            System.out.printf("  Annual Cost: $%.2f%n", annualCost);
        }
        System.out.println("=".repeat(60) + "\n");
    }

    public Product getProduct() { return product; }
    public PlanType getPlanType() { return planType; }
    public LocalDate getStartDate() { return startDate; }
    public BillingCycle getBillingCycle() { return billingCycle; }
}

class CostExplorer {
    private final Map<BillingCycle, PricingStrategy> strategies = new EnumMap<>(BillingCycle.class);

    public CostExplorer() {
        strategies.put(BillingCycle.MONTHLY, new MonthlyPricingStrategy());
        strategies.put(BillingCycle.ANNUAL, new AnnualPricingStrategy());
    }

    public void processSubscription(Subscription subscription) {
        PricingStrategy strategy = strategies.get(subscription.getBillingCycle());
        PricingConfig config = subscription.getProduct().getConfig(subscription.getPlanType());
        subscription.generateCost(strategy, config);
    }
}

public class SimplifiedCostExplorer {
    public static void main(String[] args) {
        Map<PlanType, PricingConfig> jiraPricing = new EnumMap<>(PlanType.class);
        jiraPricing.put(PlanType.BASIC, new PricingConfig(10.0, 0.05));
        jiraPricing.put(PlanType.PREMIUM, new PricingConfig(20.0, 0.05));
        Product jira = new Product("Jira", jiraPricing);

        CostExplorer explorer = new CostExplorer();

        Subscription monthlySub = new Subscription(
                jira, PlanType.BASIC, LocalDate.of(2024, 10, 1), BillingCycle.MONTHLY);
        explorer.processSubscription(monthlySub);
        monthlySub.printReport();

        Subscription annualSub = new Subscription(
                jira, PlanType.PREMIUM, LocalDate.of(2024, 10, 1), BillingCycle.ANNUAL);
        explorer.processSubscription(annualSub);
        annualSub.printReport();
    }
}
