package com.udacity.vehicles.client.prices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

/**
 * Implements a class to interface with the Pricing Client for price data.
 */
@Component
public class PriceClient {

    private static final Logger log = LoggerFactory.getLogger(PriceClient.class);

    private final WebClient client;

    public PriceClient(WebClient pricing) {
        this.client = pricing;
    }

    // In a real-world application we'll want to add some resilience
    // to this method with retries/CB/failover capabilities
    // We may also want to cache the results so we don't need to
    // do a request every time
    /**
     * Gets a vehicle price from the pricing client, given vehicle ID.
     * @param vehicleId ID number of the vehicle for which to get the price
     * @return Currency and price of the requested vehicle,
     *   error message that the vehicle ID is invalid, or note that the
     *   service is down.
     */

    //https://spring.io/guides/gs/spring-cloud-loadbalancer/ I used this guide to get parts of the code below to work.

    //Note: No longer using the 8082 port endpoint of the API "pricing service", but instead using port 8090 for the new
    //API "pricing service client" - it is the new "pricing service client" that discovers on the Eureka server the
    //IP address and port number for the pricing-service (returned as a string below), and that IP and port is then used
    //below to create a new rest template object to return the required price object from the price service. This workaround
    //had to be implemented because - despite numerous attempts - it was not possible to add the Eureka client into the POM for
    //the Vehicles-API project (for use in the PriceClient code) to access the Eureak server for discovery purposes (see further
    //notes below).
    @RequestMapping
    public String getPrice(Long vehicleId) {
        try {
            ////The 3 commented out lines of code below should have been used here as a Eureka discovery on pricing-service
            ////instead of the API call to the new "bridging" pricing-service-client - that in turn connects to Eureka,
            ////but this has not been possible due to POM issues described above and below ...

            //List<ServiceInstance> list = discoveryClient.getInstances("pricing-service");
            //ServiceInstance service2 = list.get(0);
            //URI micro2URI = service2.getUri();
            String micro2URI = client.get().uri(uriBuilder -> uriBuilder.path("pricing-service/currency").build())
                                     .retrieve().bodyToMono(String.class).block();

            Price price = new RestTemplate().getForObject(micro2URI.toString() + "/prices/" + vehicleId.toString(), Price.class);

            return String.format("%s %s", price.getPrice(), price.getCurrency());

        } catch (Exception e) {
            log.error("Unexpected error retrieving price for vehicle {}", vehicleId, e);
        }
        return "(consult price)";
    }
}

//Extra notes to the above: despite using the POM and application.properties below, the Vehicle-api - despite
//weeks of trying - would not start as a Eureka client (n.b. possibly due to existing POM entries and the new
//POM entries for Eureka client clashing). Hence, the need above to create a separate "bridging" pricing-service-client
//to act as an API (that the Vehicles api can connect to), and a Eureka client that the pricing-service-client can connect
//to get the pricing-service IP address and port number to pass back to the vehicles-api to access the price object details
//(via a rest template in PriceClient - ideally the rest template and accessing the Eureka client should have happened in
//the PriceClient of vehicles-api, but this was not possible for the reasons outlined above.

    //List<ServiceInstance> list =
    //        discoveryClient.getInstances("pricing-service");
    //ServiceInstance service2 = list.get(0);
    //URI micro2URI = service2.getUri();
    //////http://localhost:8082/prices/1   <--- string format
    //Price price = new RestTemplate().getForObject(micro2URI.toString() + "/prices/" + vehicleId.toString(), Price.class);

//<dependency>
//<groupId>org.springframework.boot</groupId>
//<artifactId>spring-boot-starter-web</artifactId>
//</dependency>

//<dependency>
//<groupId>org.springframework.boot</groupId>
//<artifactId>spring-boot-starter-data-rest</artifactId>
//</dependency>


//<dependency>
//<groupId>org.springframework.cloud</groupId>
//<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
//</dependency>

//<dependency>
//<groupId>org.springframework.cloud</groupId>
//<artifactId>spring-cloud-starter-config</artifactId>
//</dependency>


//<dependencyManagement>
//<dependencies>
//<dependency>
//<groupId>org.springframework.cloud</groupId>
//<artifactId>spring-cloud-dependencies</artifactId>
//<version>${spring-cloud.version}</version>
//<type>pom</type>
//<scope>import</scope>
//</dependency>
//</dependencies>

//<dependencies>
//<dependency>
//<groupId>org.springframework.cloud</groupId>
//<artifactId>spring-cloud-starter-parent</artifactId>
//<version>Greenwich.RELEASE</version>
//<type>pom</type>
//<scope>import</scope>
//</dependency>
//</dependencies>
//</dependencyManagement>

        //####Add these in for Eureka server discovery of the microservice pricing service
        //eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
        //eureka.client.service-url.default-zone=http://localhost:8761/eureka/
        //eureka.instance.prefer-ip-address=true
        //spring.cloud.config.enabled=false

        //###Set this to false below to stop start up errors when creating a microservice client
        //eureka.client.enabled=true

        //###Added these lines as advised by https://stackoverflow.com/questions/65660850/error-starting-up-eureka-client-with-spring-boot-2-4-1
        //eureka.client.register-with-eureka=true
        //fetch-registry=true
        //service-url.defaultZone=http://localhost:8761/eureka/
        //instance.hostname=localhost

//OLD API code for the price service, now no longer needed once the price service was converted to a microservice
//@RequestMapping
//public String getPrice(Long vehicleId) {
//    try {
//        Price price = client
//                .get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("services/price/")
//                       // .path("prices/")
//                        .queryParam("vehicleId", vehicleId)
//                        .build()
//                )
//                .retrieve().bodyToMono(Price.class).block();
//
//        //return String.format("%s %s", 11111, 22222);   //test string
//        return String.format("%s %s", price.getCurrency(), price.getPrice());
//
//    } catch (Exception e) {
//        log.error("Unexpected error retrieving price for vehicle {}", vehicleId, e);
//    }
//    return "(consult price)";
//}

//Full error log from starting the VehiclesApiApplication project with netflix-eureka-client, cloud-starter-config and
//dependency management POM entries above (as well as application.properties entries for Eureka client discovery):

// :: Spring Boot ::        (v2.1.5.RELEASE)
//
//         2021-05-15 13:03:45.935  INFO 17072 --- [  restartedMain] c.c.c.ConfigServicePropertySourceLocator : Fetching config from server at : http://localhost:8888
//         2021-05-15 13:03:46.033  INFO 17072 --- [  restartedMain] c.c.c.ConfigServicePropertySourceLocator : Connect Timeout Exception on Url - http://localhost:8888. Will be trying the next url if available
//         2021-05-15 13:03:46.033  WARN 17072 --- [  restartedMain] c.c.c.ConfigServicePropertySourceLocator : Could not locate PropertySource: I/O error on GET request for "http://localhost:8888/vehicles-api/default": Connection refused: connect; nested exception is java.net.ConnectException: Connection refused: connect
//         2021-05-15 13:03:46.033  INFO 17072 --- [  restartedMain] c.u.vehicles.VehiclesApiApplication      : No active profile set, falling back to default profiles: default
//2021-05-15 13:03:46.749  INFO 17072 --- [  restartedMain] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data repositories in DEFAULT mode.
//        2021-05-15 13:03:46.902  INFO 17072 --- [  restartedMain] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 137ms. Found 2 repository interfaces.
//        2021-05-15 13:03:47.202  INFO 17072 --- [  restartedMain] o.s.cloud.context.scope.GenericScope     : BeanFactory id=a1d35f0c-2cd1-3d92-aa93-5f6edf4a18d4
//        2021-05-15 13:03:47.335  INFO 17072 --- [  restartedMain] trationDelegate$BeanPostProcessorChecker : Bean 'org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration' of type [org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration$$EnhancerBySpringCGLIB$$3a6b1811] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
//        2021-05-15 13:03:47.351  INFO 17072 --- [  restartedMain] trationDelegate$BeanPostProcessorChecker : Bean 'org.springframework.hateoas.config.HateoasConfiguration' of type [org.springframework.hateoas.config.HateoasConfiguration$$EnhancerBySpringCGLIB$$b9eb6543] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
//        2021-05-15 13:03:47.351  INFO 17072 --- [  restartedMain] trationDelegate$BeanPostProcessorChecker : Bean 'org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration' of type [org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration$$EnhancerBySpringCGLIB$$56851b0e] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
//        2021-05-15 13:03:47.796  INFO 17072 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
//        2021-05-15 13:03:47.819  INFO 17072 --- [  restartedMain] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
//        2021-05-15 13:03:47.819  INFO 17072 --- [  restartedMain] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.19]
//        2021-05-15 13:03:47.950  INFO 17072 --- [  restartedMain] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
//        2021-05-15 13:03:47.950  INFO 17072 --- [  restartedMain] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 1902 ms
//        2021-05-15 13:03:48.266  INFO 17072 --- [  restartedMain] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
//        2021-05-15 13:03:48.404  INFO 17072 --- [  restartedMain] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
//        2021-05-15 13:03:48.466  INFO 17072 --- [  restartedMain] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [
//        name: default
//	...]
//            2021-05-15 13:03:48.551  INFO 17072 --- [  restartedMain] org.hibernate.Version                    : HHH000412: Hibernate Core {5.3.10.Final}
//            2021-05-15 13:03:48.551  INFO 17072 --- [  restartedMain] org.hibernate.cfg.Environment            : HHH000206: hibernate.properties not found
//            2021-05-15 13:03:48.704  INFO 17072 --- [  restartedMain] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.0.4.Final}
//            2021-05-15 13:03:48.852  INFO 17072 --- [  restartedMain] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.H2Dialect
//            2021-05-15 13:03:49.420  INFO 17072 --- [  restartedMain] o.h.t.schema.internal.SchemaCreatorImpl  : HHH000476: Executing import script 'org.hibernate.tool.schema.internal.exec.ScriptSourceInputNonExistentImpl@50569f16'
//            2021-05-15 13:03:49.420  INFO 17072 --- [  restartedMain] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
//            2021-05-15 13:03:49.436  WARN 17072 --- [  restartedMain] o.s.b.d.a.OptionalLiveReloadServer       : Unable to start LiveReload server
//            2021-05-15 13:03:49.883  WARN 17072 --- [  restartedMain] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
//            2021-05-15 13:03:49.883  INFO 17072 --- [  restartedMain] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
//            2021-05-15 13:03:49.883  WARN 17072 --- [  restartedMain] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
//            2021-05-15 13:03:49.883  INFO 17072 --- [  restartedMain] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
//            2021-05-15 13:03:50.105  INFO 17072 --- [  restartedMain] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
//            2021-05-15 13:03:50.146  WARN 17072 --- [  restartedMain] aWebConfiguration$JpaWebMvcConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
//        2021-05-15 13:03:50.753  INFO 17072 --- [  restartedMain] o.s.c.n.eureka.InstanceInfoFactory       : Setting initial instance status as: STARTING
//        2021-05-15 13:03:50.806  INFO 17072 --- [  restartedMain] com.netflix.discovery.DiscoveryClient    : Initializing Eureka in region us-east-1
//        2021-05-15 13:03:50.954  INFO 17072 --- [  restartedMain] c.n.d.provider.DiscoveryJerseyProvider   : Using JSON encoding codec LegacyJacksonJson
//        2021-05-15 13:03:50.954  INFO 17072 --- [  restartedMain] c.n.d.provider.DiscoveryJerseyProvider   : Using JSON decoding codec LegacyJacksonJson
//        2021-05-15 13:03:51.106 ERROR 17072 --- [  restartedMain] o.s.c.n.e.s.EurekaRegistration           : error getting CloudEurekaClient
//
//        org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'scopedTarget.eurekaClient' defined in class path resource [org/springframework/cloud/netflix/eureka/EurekaClientAutoConfiguration$RefreshableEurekaClientConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.netflix.discovery.EurekaClient]: Factory method 'eurekaClient' threw exception; nested exception is java.lang.RuntimeException: Failed to initialize DiscoveryClient!
//        at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:627) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:607) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1288) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1127) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:538) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:498) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$1(AbstractBeanFactory.java:356) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.cloud.context.scope.GenericScope$BeanLifecycleWrapper.getBean(GenericScope.java:390) ~[spring-cloud-context-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.context.scope.GenericScope.get(GenericScope.java:184) ~[spring-cloud-context-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:353) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:199) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.aop.target.SimpleBeanTargetSource.getTarget(SimpleBeanTargetSource.java:35) ~[spring-aop-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration.getTargetObject(EurekaRegistration.java:171) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration.getEurekaClient(EurekaRegistration.java:160) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:78) ~[na:na]
//        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
//        at java.base/java.lang.reflect.Method.invoke(Method.java:567) ~[na:na]
//        at org.springframework.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:282) ~[spring-core-5.1.7.RELEASE.jar:5.1.7.RELEASE]
//        at org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean.invoke(GenericScope.java:494) ~[spring-cloud-context-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:688) ~[spring-aop-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration$$EnhancerBySpringCGLIB$$6efdad8a.getEurekaClient(<generated>) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry.maybeInitializeClient(EurekaServiceRegistry.java:57) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry.register(EurekaServiceRegistry.java:39) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration.start(EurekaAutoServiceRegistration.java:82) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.doStart(DefaultLifecycleProcessor.java:182) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.access$200(DefaultLifecycleProcessor.java:53) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor$LifecycleGroup.start(DefaultLifecycleProcessor.java:360) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.startBeans(DefaultLifecycleProcessor.java:158) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.onRefresh(DefaultLifecycleProcessor.java:122) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.AbstractApplicationContext.finishRefresh(AbstractApplicationContext.java:879) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.finishRefresh(ServletWebServerApplicationContext.java:163) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:549) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:142) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:775) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:397) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.run(SpringApplication.java:316) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1260) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1248) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at com.udacity.vehicles.VehiclesApiApplication.main(VehiclesApiApplication.java:26) ~[classes/:na]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:78) ~[na:na]
//        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
//        at java.base/java.lang.reflect.Method.invoke(Method.java:567) ~[na:na]
//        at org.springframework.boot.devtools.restart.RestartLauncher.run(RestartLauncher.java:49) ~[spring-boot-devtools-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.netflix.discovery.EurekaClient]: Factory method 'eurekaClient' threw exception; nested exception is java.lang.RuntimeException: Failed to initialize DiscoveryClient!
//        at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:185) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:622) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        ... 45 common frames omitted
//        Caused by: java.lang.RuntimeException: Failed to initialize DiscoveryClient!
//        at com.netflix.discovery.DiscoveryClient.<init>(DiscoveryClient.java:411) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.DiscoveryClient.<init>(DiscoveryClient.java:269) ~[eureka-client-1.9.8.jar:1.9.8]
//        at org.springframework.cloud.netflix.eureka.CloudEurekaClient.<init>(CloudEurekaClient.java:63) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration$RefreshableEurekaClientConfiguration.eurekaClient(EurekaClientAutoConfiguration.java:302) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration$RefreshableEurekaClientConfiguration$$EnhancerBySpringCGLIB$$5dee142d.CGLIB$eurekaClient$0(<generated>) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration$RefreshableEurekaClientConfiguration$$EnhancerBySpringCGLIB$$5dee142d$$FastClassBySpringCGLIB$$682a9646.invoke(<generated>) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cglib.proxy.MethodProxy.invokeSuper(MethodProxy.java:244) ~[spring-core-5.1.7.RELEASE.jar:5.1.7.RELEASE]
//        at org.springframework.context.annotation.ConfigurationClassEnhancer$BeanMethodInterceptor.intercept(ConfigurationClassEnhancer.java:363) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration$RefreshableEurekaClientConfiguration$$EnhancerBySpringCGLIB$$5dee142d.eurekaClient(<generated>) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:78) ~[na:na]
//        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
//        at java.base/java.lang.reflect.Method.invoke(Method.java:567) ~[na:na]
//        at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:154) ~[spring-beans-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        ... 46 common frames omitted
//        Caused by: java.lang.ExceptionInInitializerError: null
//        at com.thoughtworks.xstream.XStream.setupConverters(XStream.java:989) ~[xstream-1.4.10.jar:1.4.10]
//        at com.thoughtworks.xstream.XStream.<init>(XStream.java:592) ~[xstream-1.4.10.jar:1.4.10]
//        at com.thoughtworks.xstream.XStream.<init>(XStream.java:514) ~[xstream-1.4.10.jar:1.4.10]
//        at com.thoughtworks.xstream.XStream.<init>(XStream.java:483) ~[xstream-1.4.10.jar:1.4.10]
//        at com.thoughtworks.xstream.XStream.<init>(XStream.java:429) ~[xstream-1.4.10.jar:1.4.10]
//        at com.thoughtworks.xstream.XStream.<init>(XStream.java:396) ~[xstream-1.4.10.jar:1.4.10]
//        at com.netflix.discovery.converters.XmlXStream.<init>(XmlXStream.java:51) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.converters.XmlXStream.<clinit>(XmlXStream.java:42) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.converters.wrappers.CodecWrappers$XStreamXml.<init>(CodecWrappers.java:358) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.converters.wrappers.CodecWrappers.create(CodecWrappers.java:133) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.converters.wrappers.CodecWrappers.getEncoder(CodecWrappers.java:75) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.converters.wrappers.CodecWrappers.getEncoder(CodecWrappers.java:66) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.provider.DiscoveryJerseyProvider.<init>(DiscoveryJerseyProvider.java:77) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.shared.transport.jersey.EurekaJerseyClientImpl$EurekaJerseyClientBuilder$MyDefaultApacheHttpClient4Config.<init>(EurekaJerseyClientImpl.java:202) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.shared.transport.jersey.EurekaJerseyClientImpl$EurekaJerseyClientBuilder.build(EurekaJerseyClientImpl.java:178) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.shared.transport.jersey.JerseyEurekaHttpClientFactory$JerseyEurekaHttpClientFactoryBuilder.buildLegacy(JerseyEurekaHttpClientFactory.java:230) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.shared.transport.jersey.JerseyEurekaHttpClientFactory$JerseyEurekaHttpClientFactoryBuilder.build(JerseyEurekaHttpClientFactory.java:204) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.shared.transport.jersey.JerseyEurekaHttpClientFactory.create(JerseyEurekaHttpClientFactory.java:161) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.shared.transport.jersey.Jersey1TransportClientFactories.newTransportClientFactory(Jersey1TransportClientFactories.java:59) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.DiscoveryClient.scheduleServerEndpointTask(DiscoveryClient.java:485) ~[eureka-client-1.9.8.jar:1.9.8]
//        at com.netflix.discovery.DiscoveryClient.<init>(DiscoveryClient.java:398) ~[eureka-client-1.9.8.jar:1.9.8]
//        ... 59 common frames omitted
//        Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make field private final java.util.Comparator java.util.TreeMap.comparator accessible: module java.base does not "opens java.util" to unnamed module @58a90037
//        at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:357) ~[na:na]
//        at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:297) ~[na:na]
//        at java.base/java.lang.reflect.Field.checkCanSetAccessible(Field.java:177) ~[na:na]
//        at java.base/java.lang.reflect.Field.setAccessible(Field.java:171) ~[na:na]
//        at com.thoughtworks.xstream.core.util.Fields.locate(Fields.java:40) ~[xstream-1.4.10.jar:1.4.10]
//        at com.thoughtworks.xstream.converters.collections.TreeMapConverter.<clinit>(TreeMapConverter.java:50) ~[xstream-1.4.10.jar:1.4.10]
//        ... 80 common frames omitted
//
//        2021-05-15 13:03:51.106  WARN 17072 --- [  restartedMain] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.context.ApplicationContextException: Failed to start bean 'eurekaAutoServiceRegistration'; nested exception is java.lang.NullPointerException: Cannot invoke "org.springframework.cloud.netflix.eureka.CloudEurekaClient.getApplications()" because the return value of "org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration.getEurekaClient()" is null
//        2021-05-15 13:03:51.122  INFO 17072 --- [  restartedMain] o.s.s.concurrent.ThreadPoolTaskExecutor  : Shutting down ExecutorService 'applicationTaskExecutor'
//        2021-05-15 13:03:51.122  INFO 17072 --- [  restartedMain] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
//        2021-05-15 13:03:51.122  INFO 17072 --- [  restartedMain] .SchemaDropperImpl$DelayedDropActionImpl : HHH000477: Starting delayed evictData of schema as part of SessionFactory shut-down'
//        2021-05-15 13:03:51.138  INFO 17072 --- [  restartedMain] o.s.b.f.support.DisposableBeanAdapter    : Invocation of destroy method failed on bean with name 'inMemoryDatabaseShutdownExecutor': org.h2.jdbc.JdbcSQLNonTransientConnectionException: Database is already closed (to disable automatic closing at VM shutdown, add ";DB_CLOSE_ON_EXIT=FALSE" to the db URL) [90121-199]
//        2021-05-15 13:03:51.138  INFO 17072 --- [  restartedMain] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
//        2021-05-15 13:03:51.138  INFO 17072 --- [  restartedMain] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
//        2021-05-15 13:03:51.138  INFO 17072 --- [  restartedMain] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
//        2021-05-15 13:03:51.153  INFO 17072 --- [  restartedMain] ConditionEvaluationReportLoggingListener :
//
//        Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
//        2021-05-15 13:03:51.153 ERROR 17072 --- [  restartedMain] o.s.boot.SpringApplication               : Application run failed
//
//        org.springframework.context.ApplicationContextException: Failed to start bean 'eurekaAutoServiceRegistration'; nested exception is java.lang.NullPointerException: Cannot invoke "org.springframework.cloud.netflix.eureka.CloudEurekaClient.getApplications()" because the return value of "org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration.getEurekaClient()" is null
//        at org.springframework.context.support.DefaultLifecycleProcessor.doStart(DefaultLifecycleProcessor.java:185) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.access$200(DefaultLifecycleProcessor.java:53) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor$LifecycleGroup.start(DefaultLifecycleProcessor.java:360) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.startBeans(DefaultLifecycleProcessor.java:158) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.onRefresh(DefaultLifecycleProcessor.java:122) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.context.support.AbstractApplicationContext.finishRefresh(AbstractApplicationContext.java:879) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.finishRefresh(ServletWebServerApplicationContext.java:163) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:549) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:142) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:775) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:397) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.run(SpringApplication.java:316) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1260) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1248) ~[spring-boot-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        at com.udacity.vehicles.VehiclesApiApplication.main(VehiclesApiApplication.java:26) ~[classes/:na]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
//        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:78) ~[na:na]
//        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
//        at java.base/java.lang.reflect.Method.invoke(Method.java:567) ~[na:na]
//        at org.springframework.boot.devtools.restart.RestartLauncher.run(RestartLauncher.java:49) ~[spring-boot-devtools-2.1.5.RELEASE.jar:2.1.5.RELEASE]
//        Caused by: java.lang.NullPointerException: Cannot invoke "org.springframework.cloud.netflix.eureka.CloudEurekaClient.getApplications()" because the return value of "org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration.getEurekaClient()" is null
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry.maybeInitializeClient(EurekaServiceRegistry.java:57) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry.register(EurekaServiceRegistry.java:39) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration.start(EurekaAutoServiceRegistration.java:82) ~[spring-cloud-netflix-eureka-client-2.1.0.RELEASE.jar:2.1.0.RELEASE]
//        at org.springframework.context.support.DefaultLifecycleProcessor.doStart(DefaultLifecycleProcessor.java:182) ~[spring-context-5.1.4.RELEASE.jar:5.1.4.RELEASE]
//        ... 19 common frames omitted
//
//
//        Process finished with exit code 0
//
