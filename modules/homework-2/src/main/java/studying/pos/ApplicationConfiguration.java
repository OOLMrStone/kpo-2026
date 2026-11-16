package studying.pos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.pos.adapters.CsvResultExporter;
import studying.pos.adapters.InMemoryGradebookRepository;
import studying.pos.application.PosFacade;
import studying.pos.application.ResultExporter;
import studying.pos.domain.FinalScoreService;
import studying.pos.domain.GradebookRepository;
import studying.pos.domain.Id;
import studying.pos.domain.MissingScorePolicy;
import studying.pos.domain.RequireCompleteScores;
import studying.pos.domain.ZeroMissingScore;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public GradebookRepository repository() {
        return new InMemoryGradebookRepository();
    }

    @Bean
    public ResultExporter exporter() {
        return new CsvResultExporter();
    }

    @Bean
    public MissingScorePolicy missingScorePolicy(org.springframework.core.env.Environment environment) {
        return environment.getProperty("pos.strict", Boolean.class, false)
                ? new RequireCompleteScores() : new ZeroMissingScore();
    }

    @Bean
    public FinalScoreService scores(MissingScorePolicy policy) {
        return new FinalScoreService(policy);
    }

    @Bean
    public PosFacade facade(GradebookRepository repository, FinalScoreService scores, ResultExporter exporter) {
        return new PosFacade(repository, scores, exporter, new Id("POS"));
    }
}
