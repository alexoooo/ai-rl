package ao.ai.rl;


import ao.ai.rl.algo.RandomUnstructuredAgent;
import ao.ai.rl.api.UnstructuredAgent;
import ao.ai.rl.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class OneActionTest
{
    Random random = new Random();
    UnstructuredAgent agent;


    @Before
    public void init() {
        agent = new RandomUnstructuredAgent(random);
    }


    @Test
    public void shouldPerformOnlyAvailableAction() {
        MixedAction action = agent.exploit(ObservationId.create(0), ActionRange.create(1));
        assertThat(action.size()).isEqualTo(1);
        assertThat(action.firstMostLikely().index()).isEqualTo(0);
    }
}
