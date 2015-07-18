package ao.ai.rl.model;

import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class Feedback {
    public static final Feedback ZERO = create(0.0);

//    public static final Feedback ABSENT = createAbsent();
//
//    private static Feedback createAbsent() {
//        return new AutoValue_Feedback(Double.NaN);
//    }

    public static Feedback create(double utility) {
        checkArgument(Double.isFinite(utility));
        return new AutoValue_Feedback(utility);
    }


    public abstract double utility();


//    public boolean isAbsent() {
//        return Double.isNaN(utility());
//    }
//    public boolean isPresent() {
//        return ! isAbsent();
//    }
}
