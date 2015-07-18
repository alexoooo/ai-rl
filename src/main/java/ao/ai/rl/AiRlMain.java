package ao.ai.rl;


import ao.ai.rl.model.ObservationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

public class AiRlMain implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AiRlMain.class);

    public static void main(String[] args) {
        SpringApplication.run(AiRlMain.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        ObservationId id = ObservationId.create(123);

        logger.info("id: {}", id);
    }
}
