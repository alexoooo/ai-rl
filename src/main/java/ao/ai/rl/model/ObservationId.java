package ao.ai.rl.model;

import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class ObservationId {
    public static ObservationId create(long index) {
        checkArgument(index >= 0);
        return new AutoValue_ObservationId(index);
    }

    public abstract long index();
}
