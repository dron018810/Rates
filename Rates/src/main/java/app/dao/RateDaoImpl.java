package app.dao;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import app.model.RateObject;

@Repository
@Transactional
public class RateDaoImpl implements RateDao {

	@Autowired
	private SessionFactory sessionFactory;
	 
	@Override
	public void save(RateObject rate) {
		sessionFactory.getCurrentSession().saveOrUpdate(rate);
	}
	 
	@Override
	public List<RateObject> listRates() {
		@SuppressWarnings("unchecked")
	    TypedQuery<RateObject> query = sessionFactory.getCurrentSession().createQuery("from RateObject order by date desc");
	    return query.getResultList();
	}

	@Override
	public List<RateObject> listRates(LocalDate startDate, LocalDate endDate) {
		CriteriaBuilder builder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<RateObject> criteria = builder.createQuery(RateObject.class);
		Root<RateObject> from = criteria.from(RateObject.class);
		Path<LocalDate> datePath = from.get("date");
		Predicate predicate = builder.between(datePath, startDate, endDate);
		criteria.select(from).where(predicate);		
		criteria.orderBy(builder.desc(from.get("date")));	
		List<RateObject> categories = sessionFactory.getCurrentSession().createQuery(criteria).getResultList();
	    return categories;
	}

}
