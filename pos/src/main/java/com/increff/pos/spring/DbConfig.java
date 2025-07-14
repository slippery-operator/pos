package com.increff.pos.spring;

import java.util.Properties;

import javax.sql.DataSource;

import com.increff.pos.model.Constants;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
public class DbConfig {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		BasicDataSource bean = new BasicDataSource();
		bean.setDriverClassName(applicationProperties.getJdbcDriver());
		bean.setUrl(applicationProperties.getJdbcUrl());
		bean.setUsername(applicationProperties.getJdbcUsername());
		bean.setPassword(applicationProperties.getJdbcPassword());

		bean.setInitialSize(Constants.DB_INITIAL_SIZE); //
		bean.setDefaultAutoCommit(Constants.DB_DEFAULT_AUTO_COMMIT);
		bean.setMinIdle(Constants.DB_MIN_IDLE);
		bean.setValidationQuery(Constants.DB_VALIDATION_QUERY);
		bean.setTestWhileIdle(Constants.DB_TEST_WHILE_IDLE);
		bean.setTimeBetweenEvictionRunsMillis(Constants.DB_EVICTION_RUN_INTERVAL_MS);
		return bean;
	}

	@Bean(name = "entityManagerFactory")
	@Autowired
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
		bean.setDataSource(dataSource);
		bean.setPackagesToScan(Constants.PACKAGE_POJO_LOCATION);
		HibernateJpaVendorAdapter jpaAdapter = new HibernateJpaVendorAdapter();

		bean.setJpaVendorAdapter(jpaAdapter);
		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.dialect", applicationProperties.getHibernateDialect());
		jpaProperties.put("hibernate.show_sql", applicationProperties.getHibernateShowSql());
		jpaProperties.put("hibernate.format_sql", applicationProperties.getHibernateFormatSql());
		jpaProperties.put("hibernate.hbm2ddl.auto", applicationProperties.getHibernateHbm2ddl());
		jpaProperties.put("hibernate.physical_naming_strategy", applicationProperties.getNamingStrategy());
		jpaProperties.put("hibernate.jdbc.time_zone", applicationProperties.getHibernateTimeZone());

		bean.setJpaProperties(jpaProperties);
		return bean;
	}

	@Bean(name = "transactionManager")
	@Autowired
	public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
		JpaTransactionManager bean = new JpaTransactionManager();
		bean.setEntityManagerFactory(emf.getObject());
		return bean;
	}
}
