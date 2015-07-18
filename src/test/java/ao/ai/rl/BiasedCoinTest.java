package ao.ai.rl;


import ao.ai.rl.algo.BanditUnstructuredAgent;
import ao.ai.rl.algo.bandit.Exp3ppBanditAgent;
import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class BiasedCoinTest
{
    Random random = new Random();
    UnstructuredAgent agent;


    @Before
    public void init() {
        agent = new BanditUnstructuredAgent(
//                UcbBanditAgent::new
                Exp3ppBanditAgent::new
        );
    }


    @Test
    public void luckySideShouldBePicked() {
        ObservationId observationId = ObservationId.create(0);
        ActionRange actionRange = ActionRange.create(2);

        Feedback feedback = Feedback.ZERO;
        for (int i = 0; i < 1500; i++) {
            boolean isHeads = random.nextDouble() < 0.51;

            MixedAction mixedAction = agent.exploit(observationId, actionRange);
            System.out.println("current guess: " + mixedAction);

            ActionId actionId = agent.learn(observationId, feedback, actionRange);
            boolean guessIsHeads = actionId.index() == 0;

            feedback = Feedback.create(isHeads == guessIsHeads ? 1 : -1);
        }

        MixedAction mixedAction = agent.exploit(observationId, actionRange);
        assertThat(mixedAction.firstMostLikely().index()).isEqualTo(0);

        MixedAction unknownMixedAction = agent.exploit(ObservationId.create(1), ActionRange.create(5));
        System.out.println(unknownMixedAction);
    }

}
