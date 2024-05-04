package com.jbk.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jbk.entity.ProductEntity;
import com.jbk.model.ProductModel;
import com.jbk.model.Product_Supplier_Category;
import com.jbk.service.ProductService;

@RestController
@RequestMapping("product")
public class ProductController {

//	ProductServiceImpl service=new ProductServiceImpl();
//	ProductService service = new ProductServiceImpl();

	@Autowired
	ProductService service;

	@PostMapping("/add-product")
	public ResponseEntity<String> addProduct(@RequestBody @Valid ProductModel product) {
		service.addProduct(product);

		return ResponseEntity.ok("Product Added !!!");

	}

	@GetMapping("/get-product-by-id/{productId}")
	public ResponseEntity<ProductModel> getProductById(@PathVariable long productId) {
		ProductModel productModel = service.getProductById(productId);

		return ResponseEntity.ok(productModel);

	}
	
	@GetMapping("/get-product-with-sc/{productId}")
	public ResponseEntity<Product_Supplier_Category> getProductByIdWithSC(@PathVariable long productId) {
		Product_Supplier_Category  psc= service.getProductWithSCByPId(productId);

		return ResponseEntity.ok(psc);

	}

	@DeleteMapping("/delete-product-by-id/{productId}")
	public ResponseEntity<String>  deleteProductById(@PathVariable long productId) {
		service.deleteProductById(productId);
		
		return ResponseEntity.ok("Product deleted successfully");

	}

	@PutMapping("/update-product/{productId}")
	public ResponseEntity<String> updateProduct(@PathVariable long productId, @RequestBody ProductEntity productEntity) {
		productEntity.setProductId(productId);
		
		return ResponseEntity.ok("Product updated successfully");
	}

	@GetMapping("/get-allProducts")
	public ResponseEntity<List<ProductModel>> getAllProducts() {
		return ResponseEntity.ok(service.getAllProducts());

	}

	@GetMapping("sort-products")
	public ResponseEntity<List<ProductModel>> sortProducts(@RequestParam String orderType,
			@RequestParam String propertyName) {
		return ResponseEntity.ok(service.sortProduct(orderType, propertyName));
	}

	@GetMapping("max-price")
	public ResponseEntity<Double> maxPrice() {
		return ResponseEntity.ok(service.getMaxProductPrice());
	}

	@GetMapping("get-product-by-name/{productName}")
	public Object getMaxPriceProduct(@PathVariable String productName) {

		return ResponseEntity.ok(service.getProductByName(productName));

	}

//	@GetMapping("/max-price-product")
//	public Object getMaxPriceProduct() {
//
//		Product maxPriceProduct = service.getMaxPriceProduct();
//		if (maxPriceProduct != null) {
//			return maxPriceProduct;
//		} else {
//			return "List is empty";
//		}
//
//	}

	@GetMapping("/get-products-by-price-range")
	public ResponseEntity<List<ProductModel>> getProductsByPriceRange(@RequestParam("minPrice") double minPrice,
			@RequestParam("maxPrice") double maxPrice) {
		List<ProductModel> products = service.getAllProducts(minPrice, maxPrice);
		return ResponseEntity.ok(products);

	}

	@GetMapping("/start-with")
	public List<ProductModel> getProductStartWith(@RequestParam String expression) {
		return service.getProductStartWith(expression);
	}
	
	 @GetMapping("/price-average")
	    public double productPriceAverage() {
	        return service.productPriceAverage();
	    }
	 
	 @GetMapping("/count")
	    public ResponseEntity<Double> countTotalProducts() {
	        double count = service.countOfTotalProducts();
	        return  ResponseEntity.ok(count);
	    }
	 
	 @GetMapping("/category/{categoryId}/supplier/{supplierId}")
	    public ResponseEntity<List<ProductModel>> getAllProductsByCategoryAndSupplier(
	            @PathVariable("categoryId") long categoryId,
	            @PathVariable("supplierId") long supplierId) {
	        List<ProductModel> products = service.getAllProducts(categoryId, supplierId);
	        return  ResponseEntity.ok(products);
	    }
	 
//	 @GetMapping("/products")
//	 public ResponseEntity<List<ProductModel>>  getAllProducts(@RequestParam(name="supplier") String supplier,@RequestParam(name="category") String category){
//		  List<ProductModel> products = service.getAllProducts(supplier,category);
//		  return ResponseEntity.ok(products);
//	 }

	@PostMapping("upload-sheet")
	public ResponseEntity<Map<String,Object>> uploadSheet(@RequestParam MultipartFile myfile) {
		 System.out.println(myfile.getOriginalFilename());

		 Map<String,Object> finalMap=service.uploadSheet(myfile);
//		String msg = service.uploadSheet(myfile);

		return ResponseEntity.ok(finalMap);
	}

}
