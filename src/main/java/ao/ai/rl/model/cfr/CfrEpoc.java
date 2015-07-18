package ao.ai.rl.model.cfr;


import ao.ai.rl.model.Feedback;
import autovalue.shaded.com.google.common.common.collect.ImmutableList;

public abstract class CfrEpoc {
    public abstract Feedback cumulativeUtility();

    public abstract ImmutableList<CfrStep> steps();
}
