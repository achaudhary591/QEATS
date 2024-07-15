package com.crio.qeats.repositories;

import com.crio.qeats.models.ItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<ItemEntity, String> {

}
