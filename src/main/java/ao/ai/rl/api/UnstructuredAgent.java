package ao.ai.rl.api;


import ao.ai.rl.model.*;


public interface UnstructuredAgent
{
    ActionId learn(ObservationId observationId, Feedback feedback, ActionRange actionRange);

    MixedAction exploit(ObservationId observationId, ActionRange actionRange);

//    default MixedAction exploit(ObservationId observationId, ActionRange actionRange) {
//        if (actionRange.size() == 1) {
//            return MixedAction.create(ProbabilityMass.createDeterministic(0, actionRange.size()));
//        }
//
//        ImmutableSortedMultiset.Builder<Integer> histogram = ImmutableSortedMultiset.naturalOrder();
//
//        int sampleSize = Ints.checkedCast((long) Math.max(16, Math.pow(actionRange.size(), 2)));
//        for (int i = 0; i < sampleSize; i++) {
//            ActionId actionId = learn(observationId, Feedback.ABSENT, actionRange);
//            histogram.add(actionId.index());
//        }
//
//        return MixedAction.createHistogram(histogram.build(), actionRange.size());
//    }
}

