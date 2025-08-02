package com.momcare.kids_service.service;

import com.momcare.kids_service.model.KidsInfo;

import java.util.List;
import java.util.Optional;

public interface KidsInforService {  // Corrected from KidsInforService

    // Method to create a new KidsInfo record
    KidsInfo createKidsInfo(KidsInfo kidsInfo);

    // Method to retrieve KidsInfo records for a specific user by email
    List<KidsInfo> getByUserEmail(String userEmail);

    // Method to retrieve all KidsInfo records
    List<KidsInfo> getAllKidsInfo();

    // Method to retrieve KidsInfo by ID
    Optional<KidsInfo> getById(Long id);

    // Method to update an existing KidsInfo record
    KidsInfo updateKidsInfo(Long id, KidsInfo updatedKidsInfo);

    // Method to delete a KidsInfo record by ID
    void deleteKidsInfo(Long id);
}
