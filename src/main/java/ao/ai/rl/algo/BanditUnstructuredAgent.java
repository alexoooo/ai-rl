package ao.ai.rl.algo;


import ao.ai.rl.algo.bandit.UcbBanditAgent;
import ao.ai.rl.api.BanditAgent;
import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;
import autovalue.shaded.com.google.common.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;


public class BanditUnstructuredAgent implements UnstructuredAgent
{
    private final Supplier<BanditAgent> banditAgentSupplier;


    private ActionId prevAction;
    private int prevObservationIndex = -1;

    private List<BanditAgent> banditAgents = new ArrayList<>();


    public BanditUnstructuredAgent() {
        this(UcbBanditAgent::new);
    }

    public BanditUnstructuredAgent(Supplier<BanditAgent> banditAgentSupplier) {
        this.banditAgentSupplier = checkNotNull(banditAgentSupplier);
    }



    @Override
    public ActionId learn(ObservationId observationId, Feedback feedback, ActionRange actionRange) {
        int observationIndex = Ints.checkedCast(observationId.index());

        addAgentsIfRequired(observationIndex);

        if (prevObservationIndex != -1) {
            BanditAgent previousBanditAgent = banditAgents.get(prevObservationIndex);
            previousBanditAgent.observe(prevAction, feedback);
        }

        BanditAgent banditAgent = banditAgents.get(observationIndex);
        ActionId action = banditAgent.choose(actionRange);

        prevAction = action;
        prevObservationIndex = observationIndex;

        return action;
    }


    @Override
    public MixedAction exploit(ObservationId observationId, ActionRange actionRange) {
        int observationIndex = Ints.checkedCast(observationId.index());

        addAgentsIfRequired(observationIndex);

        BanditAgent banditAgent = banditAgents.get(observationIndex);

        return banditAgent.exploit(actionRange);
    }


    private void addAgentsIfRequired(int observationIndex) {
        while (banditAgents.size() <= observationIndex) {
            banditAgents.add(banditAgentSupplier.get());
        }
    }
}
