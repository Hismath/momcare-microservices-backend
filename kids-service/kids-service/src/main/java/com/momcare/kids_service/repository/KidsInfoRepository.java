package com.momcare.kids_service.repository;

import com.momcare.kids_service.model.KidsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KidsInfoRepository extends JpaRepository<KidsInfo, Long> {

    // Find all kids info records for a specific user email
    List<KidsInfo> findByUserEmail(String userEmail);

}
