package ao.ai.rl.model;

import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class ActionId {
    public static ActionId create(int index) {
        checkArgument(index >= 0);
        return new AutoValue_ActionId(index);
    }

    public abstract int index();
}
