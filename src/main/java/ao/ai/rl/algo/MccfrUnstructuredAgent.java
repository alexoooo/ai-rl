package ao.ai.rl.algo;


import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;


public class MccfrUnstructuredAgent implements UnstructuredAgent {
    @Override
    public ActionId learn(ObservationId observationId, Feedback feedback, ActionRange actionRange) {
        return null;
    }

    @Override
    public MixedAction exploit(ObservationId observationId, ActionRange actionRange) {
        return null;
    }
}
