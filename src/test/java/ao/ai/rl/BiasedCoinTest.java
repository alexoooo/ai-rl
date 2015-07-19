package ao.ai.rl;


import ao.ai.rl.algo.BanditUnstructuredAgent;
import ao.ai.rl.algo.bandit.Exp3BanditAgent;
import ao.ai.rl.algo.bandit.Exp3ppBanditAgent;
import ao.ai.rl.algo.bandit.SucbBanditAgent;
import ao.ai.rl.algo.bandit.UcbBanditAgent;
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
                Exp3BanditAgent::new
//                SucbBanditAgent::new
//                UcbBanditAgent::new
//                Exp3ppBanditAgent::new
        );
    }


    @Test
    public void luckySideShouldBePicked() {
        ObservationId observationId = ObservationId.create(0);
        ActionRange actionRange = ActionRange.create(2);

        Feedback feedback = Feedback.ZERO;
        for (int i = 1; i <= 30_000; i++) {
            boolean isHeads = random.nextDouble() < 0.51;

            MixedAction mixedAction = agent.exploit(observationId, actionRange);

            if (i % 10_000 == 0) {
                System.out.println("current guess: " + mixedAction + " - " + i);
            }

            ActionId actionId = agent.learn(observationId, feedback, actionRange);
            boolean guessIsHeads = actionId.index() == 0;

            feedback = Feedback.create(isHeads == guessIsHeads ? 1 : -1);
        }

        MixedAction mixedAction = agent.exploit(observationId, actionRange);
        assertThat(mixedAction.firstMostLikely().index()).isEqualTo(0);

//        MixedAction unknownMixedAction = agent.exploit(ObservationId.create(1), ActionRange.create(5));
//        System.out.println(unknownMixedAction);
    }

}
