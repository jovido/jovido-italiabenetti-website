package biz.jovido.fenicesfa;

import biz.jovido.seed.configuration.EnableSeed;
import biz.jovido.seed.content.Configurer;
import biz.jovido.seed.content.HierarchyService;
import biz.jovido.seed.content.StructureService;
import biz.jovido.seed.net.HostService;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
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
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(Application.class, args);
        Assert.isTrue(context.isRunning());
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Scope("prototype")
    @ConfigurationProperties(prefix = "server.ajp", ignoreInvalidFields = true)
    protected Connector ajpConnector() {
        return new Connector("AJP/1.3");
    }

    @Bean
    @ConditionalOnProperty(name = "server.ajp.port")
    public EmbeddedServletContainerCustomizer servletContainerCustomizer(Connector ajpConnector) {
        return container -> {
            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                ((TomcatEmbeddedServletContainerFactory) container)
                        .addAdditionalTomcatConnectors(ajpConnector);
            }
        };
    }

    private void prepare() {

        HierarchyService hierarchyService = applicationContext.getBean(HierarchyService.class);
        StructureService structureService = applicationContext.getBean(StructureService.class);

        new Configurer(hierarchyService, structureService)
                .createHierarchy("primaryMenu")

                // Carousel section/item
                .createStructure("carouselItem").setNestedOnly(true)
                    .addImageAttribute("image")
                    .addTextAttribute("title")
                    .addTextAttribute("subtitle")
                    .addLinkAttribute("button").setRequired(0).setCapacity(2)
                .createStructure("carouselSection").setNestedOnly(true)
                    .addItemAttribute("carouselItems").setCapacity(5)
                        .addAcceptedStructure("carouselItem")

                // Highlight section
                .createStructure("highlightSection").setNestedOnly(true)
                    .addTextAttribute("title")
                    .addTextAttribute("text").setCapacity(3).setMultiline(true)

                // Devider section
                .createStructure("textOnlySection").setNestedOnly(true)
                    .addTextAttribute("text").setMultiline(true)

                // Devider section
                .createStructure("dividerSection").setNestedOnly(true)
                    .addTextAttribute("text")

                // Simple grid section
                .createStructure("simpleGridItem").setNestedOnly(true)
                    .addImageAttribute("image")
                    .addTextAttribute("text").setMultiline(true)
                .createStructure("simpleGridSection").setNestedOnly(true)
                    .addItemAttribute("simpleGridItems").setCapacity(8)
                        .addAcceptedStructure("simpleGridItem")

                // Feature grid section
                .createStructure("featureGridItem").setNestedOnly(true)
                    .addTextAttribute("heading")
                    .addIconAttribute("icon")
                    .addTextAttribute("description").setMultiline(true)
                    .addLinkAttribute("link")
                .createStructure("featureGridSection").setNestedOnly(true)
                    .addItemAttribute("featureGridItems").setCapacity(6)
                        .addAcceptedStructure("featureGridItem")

                // Feature list section
                .createStructure("featureListItem").setNestedOnly(true)
                    .addTextAttribute("heading")
                    .addTextAttribute("description").setMultiline(true)
                    .addLinkAttribute("link")
                .createStructure("featureListSection").setNestedOnly(true)
                    .addImageAttribute("image")
                    .addItemAttribute("featureListItems").setCapacity(5)
                        .addAcceptedStructure("featureListItem")
                    .addYesNoAttribute("rtl")

                // Sections page
                .createStructure("sectionsPage").setPublishable(true)
                    .addTextAttribute("title")
                    .addTextAttribute("subtitle")
                    .addItemAttribute("sections").setCapacity(Integer.MAX_VALUE)
                        .addAcceptedStructure("carouselSection")
                        .addAcceptedStructure("highlightSection")
                        .addAcceptedStructure("textOnlySection")
                        .addAcceptedStructure("dividerSection")
                        .addAcceptedStructure("featureListSection")
                        .addAcceptedStructure("featureGridSection")
                        .addAcceptedStructure("simpleGridSection")
                    .setLabelAttribute("title")
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
