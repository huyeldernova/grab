package com.example.api.repository.jpa;

import com.example.api.entity.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findByCustomerProfileId (Long id);

    @Modifying

    @Query("UPDATE SavedPlace s SET s.isDefault = false " +
            "WHERE s.customerProfile.id = :userId")
    void resetDefaultByUserId(@Param("userId") Long userId);

}
