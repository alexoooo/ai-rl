package ao.ai.rl.model;


import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class Probability implements Comparable<Probability> {
    public static final Probability ZERO = create(0);
    public static final Probability ONE = create(1);

    public static Probability create(double value) {
        checkArgument(0 <= value && value <= 1, "probability must be in [0, 1]: %s", value);
        return new AutoValue_Probability(value);
    }


    public abstract double value();


    @Override
    public int compareTo(Probability o) {
        return Double.compare(value(), o.value());
    }
}
