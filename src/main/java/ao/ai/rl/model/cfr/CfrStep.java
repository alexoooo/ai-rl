package ao.ai.rl.model.cfr;


import ao.ai.rl.model.Probability;

public abstract class CfrStep {

    public abstract Probability chosenProbability();

    public abstract Probability exploitationProbability();
}
