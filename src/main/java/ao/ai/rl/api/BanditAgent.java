package ao.ai.rl.api;


import ao.ai.rl.model.*;


public interface BanditAgent
{
    ActionId choose(ActionRange actionRange);

    void observe(ActionId actionId, Feedback feedback);

    MixedAction exploit(ActionRange actionRange);
}
