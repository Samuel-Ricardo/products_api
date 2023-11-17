package com.example.springboot.controllers;

import com.example.springboot.dto.ProductRecordDTO;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController()
public class ProductController {

    @Autowired
    ProductRepository repository;

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAll() {
        List<ProductModel> products = repository.findAll();

        products.forEach( product -> {
            UUID id = product.getIdProduct();
            product.add(linkTo(methodOn(ProductController.class).getOne(id)).withSelfRel());
        });

        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOne(@PathVariable(value = "id") UUID id) {
        Optional<ProductModel> productO = repository.findById(id);

        if (productO.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found");

        productO.get().add(linkTo(methodOn(ProductController.class).getAll()).withRel("Product_List"));

        return ResponseEntity.status(HttpStatus.OK).body(productO.get());
    }

    @PostMapping("/products")
    public ResponseEntity<ProductModel> save(@RequestBody @Valid ProductRecordDTO dto){

        var model = new ProductModel();
        BeanUtils.copyProperties(dto, model);

        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(model));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> delete(@PathVariable(value = "id") UUID id) {

        Optional<ProductModel> productO = repository.findById(id);

        if(productO.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found");

        repository.delete(productO.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product Deleted!");
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Object> update(@PathVariable(value = "id") UUID id, @RequestBody @Valid ProductRecordDTO dto) {

        var model = repository.findById(id);

        if (model.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");

        var product = model.get();
        BeanUtils.copyProperties(dto, product);

        return ResponseEntity.status(HttpStatus.OK).body(repository.save(product));
    }

}
