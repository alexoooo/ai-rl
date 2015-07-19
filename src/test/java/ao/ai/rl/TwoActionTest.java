package ao.ai.rl;


import ao.ai.rl.algo.BanditUnstructuredAgent;
import ao.ai.rl.algo.bandit.Exp3BanditAgent;
import ao.ai.rl.algo.bandit.SucbBanditAgent;
import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class TwoActionTest
{
    private static final ObservationId observation = ObservationId.create(0);
    private static final ActionRange actionRange = ActionRange.create(2);


    UnstructuredAgent agent;


    @Before
    public void init() {
        agent = new BanditUnstructuredAgent(
//                SucbBanditAgent::new
                Exp3BanditAgent::new
        );
    }


    @Test
    public void shouldSelectDominantAction() {
        train();

        MixedAction mixedAction = agent.exploit(observation, actionRange);
        ActionId action = mixedAction.firstMostLikely();

        assertThat(action.index()).isEqualTo(1);
    }


    void train() {
        Feedback feedback = Feedback.ZERO;
        for (int i = 0; i < 1_000; i++) {
            ActionId action = agent.learn(observation, feedback, actionRange);
            double utility = (action.index() == 0) ? -1 : 1;
            feedback = Feedback.create(utility);
        }
    }
}
