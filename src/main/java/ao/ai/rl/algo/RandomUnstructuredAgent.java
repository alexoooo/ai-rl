package ao.ai.rl.algo;


import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;

import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;


public class RandomUnstructuredAgent implements UnstructuredAgent
{
    private final Random random;

    public RandomUnstructuredAgent(Random random) {
        this.random = checkNotNull(random);
    }

    @Override
    public ActionId learn(ObservationId observationId, Feedback feedback, ActionRange actionRange) {
        return policy(actionRange).sample(random);
    }

    @Override
    public MixedAction exploit(ObservationId observationId, ActionRange actionRange) {
        return policy(actionRange);
    }

    private MixedAction policy(ActionRange actionRange) {
        return MixedAction.createUniform(actionRange.size());
    }
}
