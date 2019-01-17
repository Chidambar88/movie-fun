package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${VCAP_SERVICES}") String VCAP_SERVICES) {
        return new DatabaseServiceCredentials(VCAP_SERVICES);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource albumsDataSource = new MysqlDataSource();
        albumsDataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        HikariConfig config = new HikariConfig();
        config.setDataSource(albumsDataSource);
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource moviesDataSource = new MysqlDataSource();
        moviesDataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        HikariConfig config = new HikariConfig();
        config.setDataSource(moviesDataSource);
        return new HikariDataSource(config);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){
         HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
         hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
         hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
         hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactoryBean(DataSource moviesDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter){

        LocalContainerEntityManagerFactoryBean moviesEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        moviesEntityManagerFactoryBean.setDataSource(moviesDataSource);
        moviesEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        moviesEntityManagerFactoryBean.setPackagesToScan(Application.class.getPackage().getName());
        moviesEntityManagerFactoryBean.setPersistenceUnitName("movies");

        return moviesEntityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactoryBean(DataSource albumsDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter){

        LocalContainerEntityManagerFactoryBean albumsEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        albumsEntityManagerFactoryBean.setDataSource(albumsDataSource);
        albumsEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        albumsEntityManagerFactoryBean.setPackagesToScan(Application.class.getPackage().getName());
        albumsEntityManagerFactoryBean.setPersistenceUnitName("albums");

        return albumsEntityManagerFactoryBean;
    }

    @Bean//(name = "moviesTransactionManager")
  //  @Qualifier("moviesTransactionManager")
    public PlatformTransactionManager moviesTransactionManager(EntityManagerFactory moviesEntityManagerFactoryBean){
       return new JpaTransactionManager(moviesEntityManagerFactoryBean);
        /* PlatformTransactionManager moviesTransactionManager = new JpaTransactionManager(moviesEntityManagerFactoryBean);
        return moviesTransactionManager;*/
    }

    @Bean //(name="albumsTransactionManager")
  //  @Qualifier ("albumsTransactionManager")
    public PlatformTransactionManager albumsTransactionManager(EntityManagerFactory albumsEntityManagerFactoryBean){
       /* PlatformTransactionManager albumsTransactionManager = new JpaTransactionManager(albumsEntityManagerFactoryBean);
        return albumsTransactionManager;*/
        return new JpaTransactionManager(albumsEntityManagerFactoryBean);
    }




}
