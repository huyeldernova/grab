package com.example.api.repository.redis;


import com.example.api.entity.WsSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WsSessionRepository extends CrudRepository<WsSession, String> {

    // Tìm tất cả session của 1 user
    List<WsSession> findByUserId(String userId);
}