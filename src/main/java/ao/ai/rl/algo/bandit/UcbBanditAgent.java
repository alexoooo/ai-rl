package ao.ai.rl.algo.bandit;


import ao.ai.rl.api.BanditAgent;
import ao.ai.rl.model.ActionId;
import ao.ai.rl.model.ActionRange;
import ao.ai.rl.model.Feedback;
import ao.ai.rl.model.MixedAction;
import com.google.common.collect.ImmutableSet;

import java.util.*;

public class UcbBanditAgent implements BanditAgent
{
    DoubleSummaryStatistics allUtilities = new DoubleSummaryStatistics();
    List<ActionStat> actionStats = new ArrayList<>();

    @Override
    public ActionId choose(ActionRange actionRange) {
        while (actionStats.size() < actionRange.size()) {
            actionStats.add(new ActionStat());
        }

        int maxIndex = 0;
        double maxIndexValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < actionStats.size(); i++) {
            double ucb = actionStats.get(i).ucb(allUtilities.getCount());
            if (maxIndexValue < ucb) {
                maxIndexValue = ucb;
                maxIndex = i;
            }
        }

        return ActionId.create(maxIndex);
    }


    @Override
    public void observe(ActionId actionId, Feedback feedback) {
        allUtilities.accept(feedback.utility());

        double range = allUtilities.getMax() - allUtilities.getMin();
        double normalized = range == 0.0 ? 0 : (feedback.utility() - allUtilities.getMin()) / range;

        ActionStat actionStat = actionStats.get(actionId.index());
        actionStat.update(normalized);
    }


    @Override
    public MixedAction exploit(ActionRange actionRange) {
        if (allUtilities.getCount() == 0) {
            return MixedAction.createUniform(actionRange.size());
        }

        ImmutableSet<Integer> maxCountIndexes = maxCountIndexes();
        return MixedAction.createUniform(maxCountIndexes, actionRange.size());
    }

    ImmutableSet<Integer> maxCountIndexes() {
        long maxCount = Long.MIN_VALUE;
        Set<Integer> maxCountIndexes = new LinkedHashSet<>();

        for (int i = 0; i < actionStats.size(); i++) {
            long actionCount = actionStats.get(i).count();
            if (maxCount < actionCount) {
                maxCountIndexes.clear();
            }

            if (maxCount <= actionCount) {
                maxCount = actionCount;
                maxCountIndexes.add(i);
            }
        }

        return ImmutableSet.copyOf(maxCountIndexes);
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
