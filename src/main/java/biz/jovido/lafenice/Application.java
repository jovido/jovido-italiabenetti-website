package biz.jovido.lafenice;

import biz.jovido.seed.configuration.EnableSeed;
import biz.jovido.seed.configuration.WebSecurityConfiguration;
import biz.jovido.seed.content.Configurer;
import biz.jovido.seed.content.HierarchyService;
import biz.jovido.seed.content.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * @author Stephan Grundner
 */
@EnableSeed
@SpringBootApplication
@EntityScan("biz.jovido.lafenice")
@Import(WebSecurityConfiguration.class)
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(Application.class, args);
        Assert.isTrue(context.isRunning());
    }

    @Autowired
    private ApplicationContext applicationContext;

    private void prepare() {

        HierarchyService hierarchyService = applicationContext.getBean(HierarchyService.class);
        TypeService typeService = applicationContext.getBean(TypeService.class);

        new Configurer(hierarchyService, typeService)
                .forHierarchy("primaryMenu")
                .forType("basicPage", 1).setPublishable(true)
                    .addTextAttribute("title")
                        .setRequired(1)
                        .setCapacity(1)
                    .addTextAttribute("text")
                        .setRequired(1)
                        .setCapacity(1)
                        .setMultiline(true)
                .apply();

    }

    @PostConstruct
    void init() {
        PlatformTransactionManager transactionManager = applicationContext
                .getBean(PlatformTransactionManager.class);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute((TransactionStatus status) -> {

            try {
                prepare();
                status.flush();
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException(e);
            }

            return null;
        });
    }
}
