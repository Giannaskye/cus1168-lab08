package academy.javapro.lab08;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class InsuranceRatingEngine {
    // Knowledge base (facts about insurance rates)
    Map<String, Object> knowledgeBase = new HashMap<>();
    // Rules list
    private List<Rule> rules = new ArrayList<>();

    // Constructor initializes the knowledge base and rules
    public InsuranceRatingEngine() {
        initializeKnowledgeBase();
        initializeRules();
    }

    private void initializeKnowledgeBase() {
        // Base rates by vehicle category
        knowledgeBase.put("baseRate.sedan", 1000.0);
        knowledgeBase.put("baseRate.suv", 1200.0);
        knowledgeBase.put("baseRate.luxury", 1500.0);
        knowledgeBase.put("baseRate.sports", 1800.0);

        // Age risk factors
        knowledgeBase.put("ageFactor.16-19", 2.0);
        knowledgeBase.put("ageFactor.20-24", 1.5);
        knowledgeBase.put("ageFactor.25-65", 1.0);
        knowledgeBase.put("ageFactor.66+", 1.3);

        // Accident surcharges
        knowledgeBase.put("accidentSurcharge.0", 0.0);
        knowledgeBase.put("accidentSurcharge.1", 300.0);
        knowledgeBase.put("accidentSurcharge.2", 600.0);
    }

    private void initializeRules()

    {
        // Base rate rule - determines the starting premium based on vehicle type

        Predicate<DriverProfile> baseRateCondition = profile -> true;

        BiConsumer<DriverProfile, Premium> baseRateAction = (profile, premium) -> {
            String vehicleCategory = determineVehicleCategory(profile);
            double baseRate = (Double) knowledgeBase.get("baseRate." + vehicleCategory);
            premium.setBaseRate(baseRate);

        };
        Rule newRule = new Rule("base rate", baseRateCondition, baseRateAction);
        rules.add(newRule);

        // age factor rule

        Predicate<DriverProfile> ageFactorCondition = profile -> true;

        BiConsumer<DriverProfile, Premium> ageFactorAction = (profile, premium) -> {
            int age = profile.getAge();
            double factor;
            String explanation;

            if (age < 20) {
                factor = (Double) knowledgeBase.get("ageFactor.16-19");
                explanation = ("Drivers under 20 have higher statistical risk");
            }

            if (age < 25) {
                factor = (Double) knowledgeBase.get("ageFactor.20-24");
                explanation = "Drivers 20-24 have moderately higher risk";
            }

            if (age < 66) {
                factor = (Double) knowledgeBase.get("ageFactor.25-65");
                explanation = "Standard rate for drivers 25-65";
            } else {
                factor = (Double) knowledgeBase.get("ageFactor.66+");
                explanation = "Slight increase for senior drivers";
            }
            double adjustment = (premium.getBaseRate()) * (factor - 1.0);

            premium.addAdjustment("Age Factor", adjustment, explanation);
        };
        Rule newRule2 = new Rule("Age Factor", ageFactorCondition, ageFactorAction);
        rules.add(newRule2);

        // Accident history rule - adds surcharges for recent accidents

        Predicate<DriverProfile> accidentCondition = profile -> profile.getAccidentsInLastFiveYears() > 0;
        BiConsumer<DriverProfile, Premium> accidentAction = (profile, premium) -> {
            int accidents = profile.getAccidentsInLastFiveYears();
            double surcharge;
            String explanation;
            if (accidents == 1) {
                surcharge = (Double) knowledgeBase.get("accidentSurcharge.1");
            } else {
                surcharge = (Double) knowledgeBase.get("accidentSurcharge.2+");
            }
            explanation = "Major surcharge for 2+ accidents in past 5 years";

            premium.addAdjustment("Accident history", surcharge, explanation);
        };
        Rule newRule3 = new Rule("Accident History", accidentCondition, accidentAction);
        rules.add(newRule3);

    }

    // Helper method to determine vehicle category
    private String determineVehicleCategory(DriverProfile profile) {

        String make = profile.getVehicleMake();
        String model = profile.getVehicleModel();
        // Simple classification logic

        if (make.equals("bmw") || make.equals("mercedes") || make.equals("lexus") || make.equals("audi")) {
            return "luxury";
        }

        if (make.equals("ferrari") || make.equals("porsche") || make.equals("mustang") || make.equals("corvette")) {
            return "sports";
        }
        if (model.equals("suv") || model.equals("explorer") || model.equals("tahoe") || model.equals("highlander")) {
            return "suv";
        } else {
            return "sedan";
        }
    }

    // Calculate premium by applying all applicable rules
    public Premium calculatePremium(DriverProfile profile) {
        Premium premium = new Premium();

        for (Rule rule : rules) {
            if (rule.matches(profile)) {
                rule.apply(profile, premium);
            }
        }
        return premium;
    }

    // Rule class
    static class Rule {
        private String name;
        private Predicate<DriverProfile> condition;
        private BiConsumer<DriverProfile, Premium> action;

        public Rule(String name) {
            this.name = name;
        }

        public Rule(String name, Predicate<DriverProfile> condition, BiConsumer<DriverProfile, Premium> action) {
            this.name = name;
            this.condition = condition;
            this.action = action;
        }

        public boolean matches(DriverProfile profile) {
            return condition.test(profile);
        }

        public void apply(DriverProfile profile, Premium premium) {
            action.accept(profile, premium);

        }

        public String getName() {
            return name;
        }

    }
}
