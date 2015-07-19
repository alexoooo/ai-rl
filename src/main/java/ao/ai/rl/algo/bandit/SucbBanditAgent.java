package ao.ai.rl.algo.bandit;


import ao.ai.rl.api.BanditAgent;
import ao.ai.rl.model.ActionId;
import ao.ai.rl.model.ActionRange;
import ao.ai.rl.model.Feedback;
import ao.ai.rl.model.MixedAction;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class SucbBanditAgent implements BanditAgent
{
    double paramY = 0.1;
    double paramN = 0.9;
    double paramD = 0.001;
    Random random;

    DoubleSummaryStatistics allUtilities = new DoubleSummaryStatistics();
    List<ActionStat> actionStats = new ArrayList<>();


    public SucbBanditAgent() {
        this(new Random());
    }
    public SucbBanditAgent(Random random) {
        this.random = checkNotNull(random);
    }


    private void createActionsIfRequired(ActionRange actionRange) {
        createActionsIfRequired(actionRange.size());
    }

    private void createActionsIfRequired(int actionCount) {
        while (actionStats.size() < actionCount) {
            actionStats.add(new ActionStat());
        }
    }


    @Override
    public ActionId choose(ActionRange actionRange) {
        return exploit(actionRange).sample(random);
    }


    @Override
    public void observe(ActionId actionId, Feedback feedback) {
        createActionsIfRequired(actionId.index() + 1);

        allUtilities.accept(feedback.utility());

        double range = allUtilities.getMax() - allUtilities.getMin();
        double normalized = range == 0.0 ? 0 : (feedback.utility() - allUtilities.getMin()) / range;

        ActionStat actionStat = actionStats.get(actionId.index());
        actionStat.update(normalized);
    }


    @Override
    public MixedAction exploit(ActionRange actionRange) {
        long totalCount = allUtilities.getCount();

        if (totalCount == 0) {
            return MixedAction.createUniform(actionRange.size());
        }

        createActionsIfRequired(actionRange);

        double nK = Math.max(paramY, paramN / (1 + paramD * Math.sqrt(totalCount)));

//        double sampledProbability = random.nextDouble();
        double sampledProbability = nK;

        double[] weights = new double[actionRange.size()];

        for (int i = 0; i < actionRange.size(); i++) {
            double ucb = actionStats.get(i).ucb(totalCount);

            double smoother = (double) actionStats.get(i).count() / totalCount;

            double weight = sampledProbability * ucb + (1 - sampledProbability) * smoother;

            weights[i] = weight;
        }

        return MixedAction.createWeighted(weights, actionRange.size());
    }



    private static class ActionStat {
        private static final double EXPLORATION = Math.sqrt(2);

        DoubleSummaryStatistics utilities = new DoubleSummaryStatistics();

        public void update(double utility) {
            utilities.accept(utility);
        }

        public double ucb(long totalCount) {
            if (utilities.getCount() == 0) {
                return Double.MAX_VALUE;
            }

            double explorationUrgency = Math.sqrt(Math.log(totalCount) / utilities.getCount());

            return utilities.getAverage() + EXPLORATION * explorationUrgency;
        }

        public long count() {
            return utilities.getCount();
        }
    }
}
