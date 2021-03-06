package ao.ai.rl;


import ao.ai.rl.algo.BanditUnstructuredAgent;
import ao.ai.rl.algo.bandit.Exp3BanditAgent;
import ao.ai.rl.algo.bandit.SucbBanditAgent;
import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;
import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class RockPaperScissorsTest {
    private static final Random random = new Random();
    private static final ObservationId observation = ObservationId.create(0);
    private static final ActionRange actionRange = ActionRange.create(3);


    UnstructuredAgent agentA;
    UnstructuredAgent agentB;


    @Before
    public void init() {
        agentA = new BanditUnstructuredAgent(
                Exp3BanditAgent::new
//                SucbBanditAgent::new
        );
        agentB = new BanditUnstructuredAgent(
                Exp3BanditAgent::new
//                SucbBanditAgent::new
        );
    }


    @Test
    public void shouldRandomizeActions() {
        train();

        MixedAction mixedActionA = agentA.exploit(observation, actionRange);
        MixedAction mixedActionB = agentB.exploit(observation, actionRange);

        Probability minimumA = mixedActionA.actionProbabilities().min();
        Probability minimumB = mixedActionB.actionProbabilities().min();

        assertThat(minimumA.value()).isGreaterThan(0.28);
        assertThat(minimumB.value()).isGreaterThan(0.28);
    }


    void train() {
        Feedback feedbackA = Feedback.ZERO;
        Feedback feedbackB = Feedback.ZERO;

        for (int i = 1; i <= 40_000; i++) {
            MixedAction strategy = agentA.exploit(observation, actionRange);

            if (i % 10_000 == 0) {
                System.out.println("strategy: " + strategy + " - " + i);
            }

            Act actionA = Act.VALUES.get(agentA.learn(observation, feedbackA, actionRange).index());
            Act actionB = Act.VALUES.get(agentB.learn(observation, feedbackB, actionRange).index());

            feedbackA = Feedback.create(actionA.utility(actionB));
            feedbackB = Feedback.create(actionB.utility(actionA));
        }
    }

    enum Act {
        ROCK {
            @Override
            public double utility(Act opponent) {
                return opponent == SCISSORS ? 1 :
                        opponent == PAPER ? -1 : 0;
            }
        },
        PAPER {
            @Override
            public double utility(Act opponent) {
                return opponent == ROCK ? 1 :
                        opponent == SCISSORS ? -1 : 0;
            }
        },
        SCISSORS {
            @Override
            public double utility(Act opponent) {
                return opponent == PAPER ? 1 :
                        opponent == ROCK ? -1 : 0;
            }
        };

        public static ImmutableList<Act> VALUES = ImmutableList.copyOf(values());


        public abstract double utility(Act opponent);
    }
}
