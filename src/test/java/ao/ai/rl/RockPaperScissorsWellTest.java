package ao.ai.rl;


import ao.ai.rl.algo.BanditUnstructuredAgent;
import ao.ai.rl.algo.bandit.Exp3ppBanditAgent;
import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;
import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RockPaperScissorsWellTest {
    private static final double EPSILON = 0.05;

//    private static final Random random = new Random();
    private static final ObservationId observation = ObservationId.create(0);
    private static final ActionRange actionRange = ActionRange.create(Act.values().length);


    UnstructuredAgent agentA;
    UnstructuredAgent agentB;


    @Before
    public void init() {
        agentA = new BanditUnstructuredAgent(
//                UcbBanditAgent::new
                Exp3ppBanditAgent::new
        );
        agentB = new BanditUnstructuredAgent(
                Exp3ppBanditAgent::new
        );
    }


    @Test
    public void shouldRandomizeActions() {
        train();

        MixedAction mixedActionA = agentA.exploit(observation, actionRange);
        MixedAction mixedActionB = agentA.exploit(observation, actionRange);

        checkEquilibrium(mixedActionA);
        checkEquilibrium(mixedActionB);
    }

    void checkEquilibrium(MixedAction mixedAction) {
        // rock is dominated
        Probability rockProbability = mixedAction.actionProbabilities().get(Act.ROCK.ordinal());
        assertThat(rockProbability.value()).isLessThan(EPSILON);

        // mixing evenly between paper, scissors, and well is an equilibrium
        Probability max = mixedAction.actionProbabilities().max();
        assertThat(max.value()).isLessThan(1.0 / 3 + EPSILON);
    }


    void train() {
        Feedback feedbackA = Feedback.ZERO;
        Feedback feedbackB = Feedback.ZERO;

        for (int i = 0; i < 100_000; i++) {
            MixedAction mixed = agentA.exploit(observation, actionRange);
            System.out.println("mixed: " + mixed);

            Act actionA = Act.VALUES.get(agentA.learn(observation, feedbackA, actionRange).index());
            Act actionB = Act.VALUES.get(agentB.learn(observation, feedbackB, actionRange).index());

            feedbackA = Feedback.create(actionA.utility(actionB));
            feedbackB = Feedback.create(actionB.utility(actionA));
        }
    }


    enum Act {
        ROCK, PAPER, SCISSORS, WELL;

        public static ImmutableList<Act> VALUES = ImmutableList.copyOf(values());


        public static Optional<Act> winner(Act a, Act b) {
            if (a == b) {
                return Optional.empty();
            }

            return Optional.of(distinctWinner(a, b));
        }

        public static Act distinctWinner(Act a, Act b) {
            return  (a == ROCK     && b == SCISSORS) ? ROCK     : // rock beats scissors
                    (a == SCISSORS && b == PAPER   ) ? SCISSORS : // scissors beat paper
                    (a == PAPER    && b == ROCK    ) ? PAPER    : // paper beats rock
                    (a == PAPER    && b == WELL    ) ? PAPER    : // paper covers well
                    (a == WELL     && b == ROCK    ) ? WELL     : // rock drowns in well
                    (a == WELL     && b == SCISSORS) ? WELL     : // scissors drown in well
                    distinctWinner(b, a);
        }

        public double utility(Act opponent) {
            Optional<Act> winner = winner(this, opponent);

            if (! winner.isPresent()) {
                return 0;
            }

            return (this == winner.get()) ? 1 : -1;
        }
    }
}
