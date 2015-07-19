package ao.ai.rl.model;


import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import com.google.auto.value.AutoValue;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class Probability implements Comparable<Probability> {
    public static final Probability ZERO = create(0);
    public static final Probability ONE = create(1);

    public static Probability create(double value) {
        checkArgument(0 <= value && value <= 1, "probability must be in [0, 1]: %s", value);
        return new AutoValue_Probability(value);
    }

    public static List<Probability> createAll(double[] values) {
        ImmutableList.Builder<Probability> probabilities = ImmutableList.builder();
        for (double value : values) {
            probabilities.add(create(value));
        }
        return probabilities.build();
    }


    public abstract double value();


    @Override
    public int compareTo(Probability o) {
        return Double.compare(value(), o.value());
    }
}
