package biz.jovido.fenicesfa;

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
@EntityScan("biz.jovido.fenicesfa")
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

                // Carousel section/item
                .createStructure("carouselItem")
                    .addImageAttribute("image")
                    .addTextAttribute("title")
                    .addTextAttribute("subtitle")
                .createStructure("carouselSection")
                    .addItemAttribute("carouselItems").setCapacity(5)
                        .addAcceptedStructure("carouselItem")

                // Highlight section
                .createStructure("highlightSection")
                    .addTextAttribute("title")
                    .addTextAttribute("text").setCapacity(3)
                        .setMultiline(true)

                // Devider section
                .createStructure("dividerSection")
                    .addTextAttribute("text")
                        .setMultiline(true)

                // Feature list section
                .createStructure("featureListItem")
                    .addTextAttribute("heading")
                    .addTextAttribute("description")
                        .setMultiline(true)
                .createStructure("featureListSection")
                    .addImageAttribute("image")
                    .addTextAttribute("title")
                    .addTextAttribute("text")
                        .setMultiline(true)
                    .addItemAttribute("featureListItems")
                        .addAcceptedStructure("featureListItem")
                    .addBooleanAttribute("rtl")

                // Sections page
                .createStructure("sectionsPage").setPublishable(true)
                    .addTextAttribute("title")
                    .addTextAttribute("subtitle")
                        .setRequired(1)
                        .setCapacity(3)
                    .addItemAttribute("sections").setCapacity(Integer.MAX_VALUE)
                        .addAcceptedStructure("carouselSection")
                        .addAcceptedStructure("highlightSection")
                        .addAcceptedStructure("dividerSection")
                        .addAcceptedStructure("featureListSection")

                .apply();


        HostService hostService = applicationContext.getBean(HostService.class);

        hostService.getOrCreateHost("localhost");
        hostService.getOrCreateHost("fenicesfa.it");
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
