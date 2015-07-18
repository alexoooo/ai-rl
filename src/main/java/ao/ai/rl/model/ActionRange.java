package ao.ai.rl.model;

import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class ActionRange {
    public static ActionRange create(int size) {
        checkArgument(size > 0);
        return new AutoValue_ActionRange(size);
    }

    public abstract int size();
}
