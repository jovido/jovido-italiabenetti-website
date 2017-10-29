package biz.jovido.lafenice;

import biz.jovido.seed.configuration.EnableSeed;
import biz.jovido.seed.configuration.WebSecurityConfiguration;
import biz.jovido.seed.content.Configurer;
import biz.jovido.seed.content.HierarchyService;
import biz.jovido.seed.content.StructureService;
import biz.jovido.seed.content.HostService;
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
        StructureService structureService = applicationContext.getBean(StructureService.class);

        new Configurer(hierarchyService, structureService)
                .createHierarchy("primaryMenu")

                .createStructure("carouselItem")
                    .addImageAttribute("image")
                    .addTextAttribute("title")
                    .addTextAttribute("subtitle")
                .createStructure("carouselSection")
                    .addItemAttribute("carouselItems").setCapacity(5)
                        .addAcceptedStructure("carouselItem")

                .createStructure("highlightSection")
                    .addTextAttribute("title")
                    .addTextAttribute("text")

                .createStructure("sectionsPage").setPublishable(true)
                    .addTextAttribute("title")
                    .addItemAttribute("sections").setCapacity(Integer.MAX_VALUE)
                        .addAcceptedStructure("carouselSection")
                        .addAcceptedStructure("highlightSection")

                .apply();


        HostService hostService = applicationContext.getBean(HostService.class);

        hostService.getOrCreateHost("localhost");


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
