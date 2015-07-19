package ao.ai.rl.algo.bandit;


import ao.ai.rl.api.BanditAgent;
import ao.ai.rl.model.*;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Exp3BanditAgent implements BanditAgent {
    double discount = 1e6;
    double gamma = 0.1;
    Random random;


    DoubleSummaryStatistics allUtilities = new DoubleSummaryStatistics();
    List<ArmState> arms = new ArrayList<>();
    long choicesMade;


    public Exp3BanditAgent() {
        this(new Random());
    }
    public Exp3BanditAgent(Random random) {
        this.random = checkNotNull(random);
    }



    private void createActionsIfRequired(ActionRange actionRange) {
        createActionsIfRequired(actionRange.size());
    }

    private void createActionsIfRequired(int actionCount) {
        while (arms.size() < actionCount) {
            arms.add(new ArmState());
        }
    }


    @Override
    public ActionId choose(ActionRange actionRange) {
        MixedAction probabilities = explore(actionRange);

        double weightSum = weightSum();
        for (ArmState arm : arms) {
            arm.strategySum += arm.weight / weightSum;
        }
//        for (int i = 0; i < arms.size(); i++) {
//            arms.get(i).strategySum += probabilities.actionProbabilities().getValue(i);
//        }
        choicesMade++;

        return probabilities.sample(random);
    }



    @Override
    public void observe(ActionId actionId, Feedback feedback) {
        createActionsIfRequired(actionId.index() + 1);

        allUtilities.accept(feedback.utility());

        double range = allUtilities.getMax() - allUtilities.getMin();
        double normalized = range == 0.0 ? 0 : (feedback.utility() - allUtilities.getMin()) / range;

        ArmState arm = arms.get(actionId.index());

        double weightSum = weightSum();
        double probability = arm.probability(weightSum);

        double estimatedReward = normalized / probability;
        double factor = Math.exp(estimatedReward * gamma / arms.size());

        if (Double.isInfinite(arm.weight * factor)) {
            arms.forEach(a -> a.weight /= discount);
            arm.weight *= factor / discount;
        }

        arm.weight *= factor;

        if (Double.isInfinite(weightSum())) {
            arms.forEach(a -> a.weight /= discount);
        }
    }


    @Override
    public MixedAction exploit(ActionRange actionRange) {
        if (choicesMade == 0) {
            return MixedAction.createUniform(actionRange.size());
        }
        createActionsIfRequired(actionRange);

        double[] averageStrategy = new double[actionRange.size()];

        for (int i = 0; i < actionRange.size(); i++) {
            averageStrategy[i] = arms.get(i).strategySum / choicesMade;
        }

        return MixedAction.createWeighted(averageStrategy, actionRange.size());

//        if (arms.isEmpty()) {
//            return MixedAction.createUniform(actionRange.size());
//        }
//        createActionsIfRequired(actionRange);
//
//        double[] weights = new double[actionRange.size()];
//
//        for (int i = 0; i < actionRange.size(); i++) {
//            weights[i] = arms.get(i).weight;
//        }
//
//        return MixedAction.createWeighted(weights, actionRange.size());
    }


    MixedAction explore(ActionRange actionRange) {
        createActionsIfRequired(actionRange);

        double weightSum = weightSum();

        double probabilitySum = 0;
        double[] probabilities = new double[actionRange.size()];
        for (int i = 0; i < actionRange.size(); i++) {
            probabilities[i] = arms.get(i).probability(weightSum);
            probabilitySum += probabilities[i];
        }

        if (probabilitySum < 0.999) {
            System.out.println("wtf?");
        }

        try {
            return MixedAction.createLiteral(probabilities);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }


    private double weightSum() {
        double weightSum = 0;
        for (ArmState arm : arms) {
            weightSum += arm.weight;
        }
        return weightSum;
    }


    class ArmState {
        double weight = 1;
        double strategySum;

        double probability(double weightSum) {
            return (1.0 - gamma) * (weight / weightSum) + gamma / arms.size();
        }
    }
}
