package com.jbk.dao.impl;

import java.util.List;

import javax.persistence.RollbackException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jbk.dao.ProductDao;
import com.jbk.entity.ProductEntity;
import com.jbk.exceptions.ResourceAlreadyExistsException;
import com.jbk.exceptions.ResourceNotExistsException;
import com.jbk.exceptions.SomethingWentWrongException;

//@Component
@Repository
public class ProductDaoImpl implements ProductDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public boolean addProduct(ProductEntity productEntity) {
		boolean isAdded = false;
		
		try(Session session=sessionFactory.openSession()) {
			Transaction transaction;
			

			// check its exists or not
			ProductEntity dbEntity = getProductByName(productEntity.getProductName());

			if (dbEntity == null) {
				session.save(productEntity);
				transaction = session.beginTransaction();
				transaction.commit();
				isAdded = true;
			} else {
				throw new ResourceAlreadyExistsException(
						"Product Already Exits with Id : " + productEntity.getProductId());

			}
		} catch (RollbackException e) {
			e.printStackTrace();
			throw new SomethingWentWrongException("Something went wrong in during add product,check unique field");
		} 
		

		return isAdded;

	}

	@Override
	public ProductEntity getProductById(long productId) {
		ProductEntity productEntity = null;
		try {
			Session session = sessionFactory.openSession();

			productEntity = session.get(ProductEntity.class, productId);

		} catch (HibernateException e) {
			throw new SomethingWentWrongException("Connection Failure !!");

		}
		return productEntity;

	}

	@Override
	public boolean deleteProductById(ProductEntity productEntity) {
//		boolean isDeleted=false;
		Session session=null;
		Transaction transaction=null;
		try {
			session = sessionFactory.openSession();
			transaction=session.beginTransaction();
			session.delete(session.contains(productEntity)?productEntity:session.merge(productEntity));
//			session.delete(productEntity);;
//			transaction.commit();
			return true;
		} catch (Exception e) {
			if(transaction!=null) {
				transaction.rollback();
			}
			e.printStackTrace();
			throw new SomethingWentWrongException("Something went wrong during product deletion");
		}
		finally {
			if(session !=null) {
				session.close();
			}
		}
//		return isDeleted;
	}

	@Override
	public boolean updateProduct(ProductEntity productEntity) {
//		boolean isUpdated = false;
		Session session=null;
		Transaction transaction=null;
		try {
			session = sessionFactory.openSession();
			transaction=session.beginTransaction();
			session.update(productEntity);
			transaction.commit();
			return true;
		}catch (Exception e) {
			if(transaction!=null) {
				transaction.rollback();
			}
			e.printStackTrace();
			 throw new SomethingWentWrongException("Something went wrong during product update");
		}finally {
			if(session !=null) {
				session.close();
			}
		}
//			ProductEntity dbProduct = getProductById(productEntity.getProductId());
//
//			if (dbProduct != null) {
//				session.update(productEntity);
//				session.beginTransaction().commit();
//				isUpdated = true;
//			} else {
//				isUpdated = false;
//				// throw new ResourceNotExistsException("Product not exists with id :" +
//				// productEntity.getProductId());
//			}
//		}
////		catch (ResourceNotExistsException e) {
////			throw new ResourceNotExistsException("product not exists with id : " + productEntity.getProductId());
//		// }
//		catch (Exception e) {
//			throw new SomethingWentWrongException("something went wrong during update product ");
//		}
//
//		return isUpdated;

	}

	@Override
	public List<ProductEntity> getAllProducts() {
		List<ProductEntity> list = null;
		try {
			Session session = sessionFactory.openSession();

			Criteria criteria = session.createCriteria(ProductEntity.class);

			list = criteria.list();

		} catch (Exception e) {
			e.printStackTrace();
			throw new SomethingWentWrongException("Something Went Wrong During retrive all Product");

		}
		return list;
	}

	@Override
	public List<ProductEntity> sortProduct(String orderType, String property) {
		List<ProductEntity> list = null;
		try {
			Session session = sessionFactory.openSession();
			Criteria criteria = session.createCriteria(ProductEntity.class);

			if (orderType.equalsIgnoreCase("desc")) {
				criteria.addOrder(Order.desc(property));
			} else {
				criteria.addOrder(Order.asc(property));
			}

			list = criteria.list();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public double getMaxProductPrice() {
		double maxPrice = 0;
		try {
			Session session = sessionFactory.openSession();

			Criteria criteria = session.createCriteria(ProductEntity.class);

			Projection productPriceProjection = Projections.max("productPrice");

			criteria.setProjection(productPriceProjection); /// select max(productPrice) from ProductEntity

			List list = criteria.list();
			if (!list.isEmpty()) {
				maxPrice = (double) list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return maxPrice;
	}

	@Override
	public List<ProductEntity> getMaxPriceProduct() {
		double maxProductPrice = getMaxProductPrice();
		List<ProductEntity> list = null;
		if (maxProductPrice > 0) {

			// find max price product
			Session session = sessionFactory.openSession();
			Criteria criteria = session.createCriteria(ProductEntity.class);

			// select * from product where product_price=?
			criteria.add(Restrictions.eq("productPrice", maxProductPrice)); // filter query

			list = criteria.list();

		} else {
			throw new ResourceNotExistsException("Product Not Exists");
		}

		return list;

	}

	@Override
	public ProductEntity getProductByName(String productName) {

		// select * from product where product_name=? // Restrictions

		// HQL: from ProductEntity where productName= :parametername (parametername :
		// pname)
		List<ProductEntity> list = null;
		ProductEntity productEntity = null;
		try {
			Session session = sessionFactory.openSession();

			Query<ProductEntity> query = session.createQuery("FROM ProductEntity WHERE productName= :name");

			query.setParameter("name", productName);

			list = query.list();

			if (!list.isEmpty()) {
				productEntity = list.get(0);
			} else {
				return null;
			}

		} catch (ResourceNotExistsException e) {
			throw new ResourceNotExistsException("Product Not Exists");
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return productEntity;
	}

	@Override
	public List<ProductEntity> getAllProducts(double low, double high) {
		List<ProductEntity> list = null;
		try {
			Session session = sessionFactory.openSession();
			Criteria criteria = session.createCriteria(ProductEntity.class);
			criteria.add(Restrictions.between("productPrice", low, high));
			list = criteria.list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SomethingWentWrongException(
					"Something Went Wrong During retrieving products within the price range");
		}
		return list;
	}

//	@Override
//	public List<ProductEntity> getProductStartWith(String expression) {
//		List<ProductEntity> productList = null;
//		try {
//			Session session = sessionFactory.getCurrentSession();
//			String queryString = "FROM ProductEntity WHERE name LIKE :expression";
//			productList = session.createQuery(queryString).setParameter("expression", expression + "%").list();
//		} catch (Exception e) {
//			e.printStackTrace();
//			
//		}
//		return productList;
//	}
	
	 @Override
	    public List<ProductEntity> getProductStartWith(String expression) {
	        List<ProductEntity> list = null;
	        try {
	            Session session = sessionFactory.openSession();
	            Query<ProductEntity> query = session.createQuery("FROM ProductEntity WHERE productName LIKE :expression");
	            query.setParameter("expression", expression + "%");
	            list = query.list();
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new SomethingWentWrongException("Something Went Wrong During retrieving products starting with the given expression");
	        }
	        return list;
	    }

	@Override
	public double productPriceAverage() {
		   double average = 0.0;
	        try {
	            Session session = sessionFactory.openSession();
	            average = (Double) session.createQuery("SELECT AVG(productPrice) FROM ProductEntity").uniqueResult();
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new SomethingWentWrongException("Something Went Wrong During calculating product price average");
	        }
	        return average;
	}

	@Override
	public double countOfTotalProducts() {
		
	    try {
            Session session = sessionFactory.openSession();
            long count = (Long) session.createQuery("SELECT COUNT(*) FROM ProductEntity").uniqueResult();
            return (double) count;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SomethingWentWrongException("Something Went Wrong During counting total products");
        }
	}

	@Override
	public List<ProductEntity> getAllProducts(long category, long supplier) {
		try {
			Session session = sessionFactory.openSession();
			String hql="FROM ProductEntity WHERE category_id = :category AND supplier_id = :supplier";
			Query<ProductEntity> query = session.createQuery(hql,ProductEntity.class);
			query.setParameter("category", category);
			query.setParameter("supplier", supplier);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SomethingWentWrongException("Something went wrong while retrieving products.");
		}
		
	}

//	@Override
//	public List<ProductEntity> getAllProducts(String supplier,String category) {
//		List<ProductEntity> productList=null;
//		
//		Session session = sessionFactory.openSession();
//		try {
//			Criteria criteria = session.createCriteria(ProductEntity.class);
//			criteria.add(Restrictions.eq("supplier", supplier));
//			productList=criteria.list();
//		} 
//		finally {
//			session.close();
//		}
//		return productList;
//	}

}
