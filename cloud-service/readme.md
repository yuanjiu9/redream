#cloud-service

微服务化及其demo

##各个module说明


**CalmRouter-proxy**:
  http://calm.keruyun.com/CalmRouter 的代理入口，用于代理所有该url路径的服务


**cloud-CalmRouter**: 
微服务化的CalmRouter.war，用于整体打包切入微服务，阶段性的，将来废弃。


 **这是一个过度或验证阶段：**

![](<doc/CalmRouter.png>)


**cloud-sync.v5**:
具体的微服务化完整例子参考，也可以用于微服务化模板工程。

**override-config**:
需要覆盖原来的的配置文件


##CalmRouter 微服务化后


![](<doc/CalmRouter2.png>)


##微服务化配置参考

###依赖

1. 包括spring-cloud,spring-boot依赖，如下（详细参考cloud-sync.v5工程）：

	
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>${curator.version}</version>
            <exclusions>
                <exclusion>
                    <artifactId>log4j</artifactId>
                    <groupId>log4j</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        
        <!-- spring boot deps-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>log4j-over-slf4j</artifactId>
                    <groupId>org.slf4j</groupId>
                </exclusion>
                <exclusion>
                    <artifactId>logback-classic</artifactId>
                    <groupId>ch.qos.logback</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        
     **业务依赖**
     
  		<dependency>
            <groupId>com.keruyun.calm</groupId>
            <artifactId>sync.v5</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.keruyun.calm</groupId>
            <artifactId>infrastructure</artifactId>
            <version>${project.version}</version>
        </dependency>

2. 添加spring依赖配置：applicationContext-root.xml
	import所需的spring配置
	
	
	 	<import resource="applicationContext.xml"/>
		<import resource="applicationContext-ds.xml"/>
		<import resource="applicationContext-filequeue.xml"/>
    
    
3. 添加bootstrap main类

		
		import com.calm.third.Ch5PayConfig;
		import com.keruyun.calm.boot.config.Ch5PayConfigProperties;
		import org.springframework.boot.SpringApplication;
		import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
		import org.springframework.boot.autoconfigure.SpringBootApplication;
		import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
		import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
		import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
		import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
		import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
		import org.springframework.cloud.netflix.feign.EnableFeignClients;
		import org.springframework.context.annotation.*;
		import org.springframework.jmx.support.MBeanServerFactoryBean;
		
		import javax.management.MBeanServer;
	 
		@SpringBootApplication
		@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class})
		@ImportResource(value = {"applicationContext-root.xml"})
		@ComponentScan("com.keruyun")
		@EnableDiscoveryClient
		@EnableFeignClients
		@EnableCircuitBreaker 
		public class SyncV5AppMain {
		
		    @Bean
		    @Primary
		    public Ch5PayConfig ch5PayConfig() {
		        return new Ch5PayConfigProperties();
		    }
		
		    @Bean
		    @ConditionalOnMissingBean(MBeanServer.class)
		    public MBeanServer mbeanServer() {
		        MBeanExportConfiguration.SpecificPlatform platform = MBeanExportConfiguration.SpecificPlatform.get();
		        if (platform != null) {
		            return platform.getMBeanServer();
		        }
		        MBeanServerFactoryBean factory = new MBeanServerFactoryBean();
		        factory.setLocateExistingServerIfPossible(true);
		        factory.afterPropertiesSet();
		        return new MBeanServerDelegate(factory.getObject());
		
		    }
		
		    public static void main(String[] args) {
		        SpringApplication.run(SyncV5Bootstrap.class, args);
		    }
	
		}
	
4. 添加配置

		spring:
		  application:
		    
		    name: CalmRouter #替换为自己的app name
		  profiles:
		  	
		    active: ${profile.env},config,push,sync,ocs,on_mobile,mysql
		    #激活的profiles
		  cloud:
		    zookeeper:
		      connectString: 120.26.53.156:2181,115.29.243.165:2181,121.43.115.106:2181
		      enabled: true
		      config:
		        enabled: true
		        root: /configurations/${profile.env}
		        defaultContext: apps
		        profileSeparator: ':'
		
		---
		spring:
		  profiles: dev
		  cloud:
		    zookeeper:
		      connectString: 120.26.53.156:2181,115.29.243.165:2181,121.43.115.106:2181
		
		---
		spring:
		  profiles:  joint
		  cloud:
		    zookeeper:
		      #10.117.42.187  10.161.175.124 10.165.98.102
		      connectString: 10.117.42.187:2181,10.161.175.124:2181,10.165.98.102:2181
		
		
		---
		spring:
		  profiles: test
		  cloud:
		    zookeeper:
		      connectString: 10.117.42.187:2181,10.161.175.124:2181,10.165.98.102:2181
		
		---
		spring:
		  profiles: gld
		  cloud:
		    zookeeper:
		      connectString: 10.117.42.187:2181,10.161.175.124:2181,10.165.98.102:2181
		
		---
		spring:
		  profiles: prod
		  cloud:
		    zookeeper:
		      connectString: iZ23xtjojg3Z:2181,iZ23kqh4i23Z:2181,iZ23y1xqix6Z:2181

	
5. run com.keruyun.calm.boot.SyncV5AppMain.main
6. 打包部署

		mnv clean install
		cd target/
		java -jar ${projec.t.artifactId}-${profile.active}-${project.version}-exec.jar
		#例如 java -jar cloud-sync.v5-dev-4.0.0-SNAPSHOT-exec.jar

7. 在Zookeeper中修改配置

	+ 访问并登陆 http://zkui.shishike.com/
	+ 点击进入需要修改的path，添加node"Add Node"或者添加属性"Add Property"

	如下图：
	![](<doc/zkui1.png>)
	
	![](<doc/zkui2.png>)

    
