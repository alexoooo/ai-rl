package ao.ai.rl.model;


import com.google.auto.value.AutoValue;
import com.google.common.collect.Multiset;
import com.google.common.primitives.Doubles;

import java.util.Random;
import java.util.Set;


@AutoValue
public abstract class MixedAction {
    public static MixedAction createUniform(int size) {
        return create(ProbabilityMass.createUniform(size));
    }
    public static MixedAction createWeighted(double[] weights, int size) {
        return create(ProbabilityMass.createWeighted(Doubles.asList(weights), size));
    }
    public static MixedAction createUniform(Set<Integer> indexes, int size) {
        return create(ProbabilityMass.createUniform(indexes, size));
    }
    public static MixedAction createDeterministic(int index, int size) {
        return create(ProbabilityMass.createDeterministic(index, size));
    }
    public static MixedAction createHistogram(Multiset<Integer> histogram, int size) {
        return create(ProbabilityMass.createHistogram(histogram, size));
    }
    public static MixedAction createLiteral(double[] probabilities) {
        return create(ProbabilityMass.create(Probability.createAll(probabilities)));
    }
    public static MixedAction create(ProbabilityMass probabilityMass) {
        return new AutoValue_MixedAction(probabilityMass);
    }

    public abstract ProbabilityMass actionProbabilities();


    public int size() {
        return actionProbabilities().size();
    }

    public ActionId firstMostLikely() {
        int max = actionProbabilities().firstMaxIndex();
        return ActionId.create(max);
    }

    public ActionId sample(Random random) {
        int sample = actionProbabilities().sample(random);
        return ActionId.create(sample);
    }
}
