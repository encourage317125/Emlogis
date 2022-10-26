package com.emlogis.engine.sqlserver.loader;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;

import com.emlogis.engine.sqlserver.loader.SQLServerDatabaseLoader;

public abstract class LoadDataTestBase {
	protected EntityManagerFactory emf;
	protected EntityManager em;
	protected SQLServerDatabaseLoader loader;
	
	@Before
	public void setUp() throws Exception {
		emf = Persistence
				.createEntityManagerFactory("EmLogisOptaDB");
		em = emf.createEntityManager();
		loader = new SQLServerDatabaseLoader();
		loader.setEntityManager(em);
	}

	@After
	public void tearDown() throws Exception {
		if(em != null){
			em.close();
		} 
		
		if(emf != null){
			emf.close();
		}
	}
	
	
}
