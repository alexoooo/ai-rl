package ao.ai.rl.model;


import autovalue.shaded.com.google.common.common.base.Joiner;
import com.google.auto.value.AutoValue;
import com.google.common.collect.*;

import java.util.*;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;

@AutoValue
public abstract class ProbabilityMass {
    private static final double EPSILON = 10e-6;

    public static ProbabilityMass createUniform(int size) {
        Probability probability = Probability.create(1.0 / size);
        Iterable<Probability> probabilities = Iterables.limit(Iterables.cycle(probability), size);
        return create(ImmutableList.copyOf(probabilities));
    }

    public static ProbabilityMass createUniform(Set<Integer> indexes, int size) {
        Probability probability = Probability.create(1.0 / indexes.size());

        ImmutableList.Builder<Probability> probabilities = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            if (indexes.contains(i)) {
                probabilities.add(probability);
            } else {
                probabilities.add(Probability.ZERO);
            }
        }
        return create(probabilities.build());
    }

    public static ProbabilityMass createDeterministic(int index, int size) {
        ImmutableList.Builder<Probability> probabilities = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            Probability probability = (i == index) ? Probability.ONE : Probability.ZERO;
            probabilities.add(probability);
        }
        return create(probabilities.build());
    }

    public static ProbabilityMass createHistogram(Multiset<Integer> histogram, int size) {
        checkArgument(size > 0);
        checkArgument(!histogram.isEmpty());

        ImmutableSortedMultiset<Integer> sortedForChecking = ImmutableSortedMultiset.copyOf(histogram);
        checkArgument(sortedForChecking.firstEntry().getElement() >= 0);
        checkArgument(sortedForChecking.lastEntry().getElement() < size);

        ImmutableList.Builder<Integer> counts = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            int count = histogram.count(i);
            counts.add(count);
        }
        return createHistogram(counts.build());
    }

    public static ProbabilityMass createHistogram(List<Integer> counts) {
        long total = counts.stream().mapToLong(Integer::intValue).sum();
        ImmutableList.Builder<Probability> probabilities = ImmutableList.builder();
        for (Integer count : counts) {
            Probability probability = Probability.create((double) count / total);
            probabilities.add(probability);
        }
        return create(probabilities.build());
    }

    public static ProbabilityMass createWeighted(List<Double> weights, int size) {
        DoubleSummaryStatistics stats = weights.stream().mapToDouble(Double::doubleValue).summaryStatistics();

        double range = stats.getMax() - stats.getMin();
        if (range < EPSILON) {
            return createUniform(size);
        }

        double[] normalizedWeights = weights.stream().mapToDouble(w -> (w - stats.getMin()) / range).toArray();
        double normalizedSum = StreamSupport.doubleStream(Spliterators.spliterator(normalizedWeights, 0), false).sum();
        checkState(Double.isFinite(normalizedSum));

        ImmutableList.Builder<Probability> probabilities = ImmutableList.builder();
        for (double normalizedWeight : normalizedWeights) {
            double normalized = normalizedWeight / normalizedSum;

            Probability probability = Probability.create(normalized);
            probabilities.add(probability);
        }
        return create(probabilities.build());
    }



    public static ProbabilityMass create(List<Probability> probabilities) {
        checkArgument(!probabilities.isEmpty());

        double probabilitySum = probabilities.stream().mapToDouble(Probability::value).sum();
        checkArgument(1.0 - probabilitySum < EPSILON, "must add up to 1: %s", probabilities);

        return new AutoValue_ProbabilityMass(ImmutableList.copyOf(probabilities));
    }


    //-----------------------------------------------------------------------------------------------------------------
    public abstract ImmutableList<Probability> probabilities();


    //-----------------------------------------------------------------------------------------------------------------
    public Probability get(int index) {
        return probabilities().get(index);
    }

    public double getValue(int index) {
        return get(index).value();
    }

    public int size() {
        return probabilities().size();
    }


    public int firstMaxIndex() {
        int maxIndex = -1;
        double maxProbability = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < size(); i++) {
            double probability = getValue(i);
            if (maxProbability < probability) {
                maxIndex = i;
                maxProbability = probability;
            }
        }

        return maxIndex;
    }
    public int maxIndex() {
        return Iterables.getOnlyElement(maxIndexes());
    }
    public ImmutableSortedSet<Integer> maxIndexes() {
        Probability max = max();
        SortedSet<Integer> maxIndexes = Sets.filter(indexes(), i -> get(i).equals(max));
        return ImmutableSortedSet.copyOf(maxIndexes);
    }

    ContiguousSet<Integer> indexes() {
        return ContiguousSet.create(Range.closedOpen(0, size()), DiscreteDomain.integers());
    }


    public Probability min() {
        return Ordering.natural().min(probabilities());
    }
    public Probability max() {
        return Ordering.natural().max(probabilities());
    }


    public int sample(Random random) {
        double sample = random.nextDouble();

        double cumulativeProbability = 0;
        for (int i = 0; i < size(); i++) {
            cumulativeProbability += getValue(i);

            if (sample < cumulativeProbability) {
                return i;
            }
        }

        throw new Error();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        List<Double> probabilityValues =  probabilities().stream().map(Probability::value).collect(toList());
        return "[" + Joiner.on(", ").join(probabilityValues) + "]";
    }
}
