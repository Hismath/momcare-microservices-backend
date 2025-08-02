package com.momcare.kids_service.service;

import com.momcare.kids_service.model.KidsInfo;
import com.momcare.kids_service.repository.KidsInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KidsInfoServiceImpl implements KidsInforService {

    @Autowired
    private KidsInfoRepository kidsInfoRepository;

    @Override
    public KidsInfo createKidsInfo(KidsInfo kidsInfo) {
        return kidsInfoRepository.save(kidsInfo); // Save a new KidsInfo object to the database
    }

    @Override
    public List<KidsInfo> getByUserEmail(String userEmail) {
        return kidsInfoRepository.findByUserEmail(userEmail); // Fetch KidsInfo based on user email
    }

    @Override
    public List<KidsInfo> getAllKidsInfo() {
        return kidsInfoRepository.findAll(); // Fetch all KidsInfo records
    }

    @Override
    public Optional<KidsInfo> getById(Long id) {
        return kidsInfoRepository.findById(id); // Fetch a specific KidsInfo record by ID
    }

    @Override
    public KidsInfo updateKidsInfo(Long id, KidsInfo updatedKidsInfo) {
        Optional<KidsInfo> existingOpt = kidsInfoRepository.findById(id);
        if (existingOpt.isPresent()) {
            KidsInfo existing = existingOpt.get();
            existing.setName(updatedKidsInfo.getName());
            existing.setAge(updatedKidsInfo.getAge());
            existing.setGender(updatedKidsInfo.getGender());
            existing.setUserEmail(updatedKidsInfo.getUserEmail());
            return kidsInfoRepository.save(existing); // Save the updated record
        } else {
            return null; // If the record does not exist, return null
        }
    }

    @Override
    public void deleteKidsInfo(Long id) {
        kidsInfoRepository.deleteById(id); // Delete the KidsInfo record by ID
    }
}
