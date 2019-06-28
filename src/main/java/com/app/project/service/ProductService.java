package com.app.project.service;

import com.app.project.dto.CustomerDto;
import com.app.project.model.Category;
import com.app.project.model.Customer;
import com.app.project.model.Product;
import com.app.project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

  @PersistenceContext
  private EntityManager entityManager;

  private ProductRepository productRepository;

  @Autowired
  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Transactional
  public void addProduct(Product product) {
    Customer customer = entityManager.merge(product.getCustomer());
    product.setCustomer(customer);
    entityManager.persist(product);
  }

  public List<Product> getCustomerProductsList(String email) {
    return productRepository.findAllProductsByCustomerEmail(email);
  }

  public Map<Category, BigDecimal> allProductsSumPriceByCategory() {

    return productRepository.findProductsByCategory().stream()
            .collect(Collectors.groupingBy(arr -> Category.valueOf(String.valueOf(arr[0])),
                    Collectors.mapping(arr -> new BigDecimal(String.valueOf(arr[1])), Collectors.reducing(BigDecimal.ZERO,
                            BigDecimal::add))));
  }


  public Map<Category, BigDecimal> productsSumPriceByCategoryByCustomerEmail(String email) {

    return productRepository.findProductsByCategoryByClientEmail(email).stream()
            .collect(Collectors.groupingBy(arr -> Category.valueOf(String.valueOf(arr[0])),
                    Collectors.mapping(arr -> new BigDecimal(String.valueOf(arr[1])), Collectors.reducing(BigDecimal.ZERO,
                            BigDecimal::add))));
  }

  public Map<Category, Map<CustomerDto, BigDecimal>> sumPriceByAllCategoryByAllClients() {

    return productRepository.joinTablesResult()
            .stream()
            .map(arr -> Category.valueOf(String.valueOf(arr[0])))
            .distinct()
            .collect(Collectors.toMap(
                    Function.identity(),
                    category -> productRepository.joinTablesResult().stream().filter(arr -> Category.valueOf(String.valueOf(arr[0])) == category).collect(
                            Collectors.groupingBy(arr ->
                                            CustomerDto.builder()
                                                    .firstName((String) arr[3])
                                                    .lastName((String) arr[4])
                                                    .emailAddress(String.valueOf(arr[5]))
                                                    .build(),
                                    Collectors.mapping(arr -> new BigDecimal(String.valueOf(arr[1])), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))));

  }

}
