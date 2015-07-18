package ao.ai.rl.algo.bandit;


import ao.ai.rl.api.BanditAgent;
import ao.ai.rl.model.*;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class Exp3ppBanditAgent implements BanditAgent {
    double learningRate = 0;
    Random random;

    private List<ArmState> arms = new ArrayList<>();

    long t;


    public Exp3ppBanditAgent() {
        this(new Random());
    }
    public Exp3ppBanditAgent(Random random) {
        this.random = checkNotNull(random);
    }


    @Override
    public ActionId choose(ActionRange actionRange) {
        t++;

        while (arms.size() < actionRange.size()) {
            arms.add(new ArmState());
        }


        int k = actionRange.size();
        double beta = 0.5 / Math.sqrt(Math.log(k) / (t * k));
        learningRate = beta;

        arms.forEach(arm -> arm.explorationParameter =
                Math.min(1.0 / 2 * k,
                Math.min(beta,
                         arm.explorationFactor)));

        double expTotalLoss = arms.stream().mapToDouble(ArmState::expLoss).sum();

        arms.forEach(arm -> arm.probability = arm.expLoss() / expTotalLoss);

        double explorationSum = arms.stream().mapToDouble(ArmState::explorationParameter).sum();
        double oneMinusExplorationSum = 1.0 - explorationSum;

        arms.forEach(arm -> arm.explorationProbability =
                oneMinusExplorationSum * arm.probability + arm.explorationParameter);

        return exploit(actionRange).sample(random);
    }



    @Override
    public void observe(ActionId actionId, Feedback feedback) {
        ArmState arm = arms.get(actionId.index());

        double pointLoss = - feedback.utility() / arm.explorationProbability;

        arm.loss.accept(pointLoss);
    }

    @Override
    public MixedAction exploit(ActionRange actionRange) {
        if (t == 0) {
            return MixedAction.createUniform(actionRange.size());
        }

        double[] weights = arms.stream().mapToDouble(ArmState::explorationProbability).toArray();
        if (weights.length < actionRange.size()) {
            weights = Arrays.copyOf(weights, actionRange.size());
        }
        return MixedAction.createWeighted(weights, actionRange.size());
    }


    class ArmState {
        double explorationFactor = Math.sqrt(2);
        double explorationParameter = 0;
        double probability;
        double explorationProbability;

        DoubleSummaryStatistics loss = new DoubleSummaryStatistics();

        double expLoss() {
            return Math.exp(-learningRate * loss.getSum());
        }
        double explorationParameter() {
            return explorationParameter;
        }
        double explorationProbability() {
            return explorationProbability;
        }
    }
}
