package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.DrugInteraction;
import com.pharmadesk.backend.model.DrugInteractionCheck;
import com.pharmadesk.backend.pharmacy.repository.DrugInteractionCheckRepository;
import com.pharmadesk.backend.pharmacy.repository.DrugInteractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DrugInteractionService {

    private final DrugInteractionRepository interactionRepository;
    private final DrugInteractionCheckRepository checkRepository;

    public DrugInteractionService(DrugInteractionRepository interactionRepository,
                                  DrugInteractionCheckRepository checkRepository) {
        this.interactionRepository = interactionRepository;
        this.checkRepository = checkRepository;
    }

    public List<DrugInteraction> checkInteractions(List<Long> medicineIds) {
        return interactionRepository.checkInteractions(medicineIds);
    }

    public List<DrugInteraction> getAllInteractions() {
        return interactionRepository.findAll();
    }

    @Transactional
    public DrugInteraction createInteraction(DrugInteraction interaction) {
        interaction.setInteractionId(UUID.randomUUID().toString());
        return interactionRepository.save(interaction);
    }

    @Transactional
    public DrugInteractionCheck logInteractionCheck(DrugInteractionCheck check) {
        check.setCheckId(UUID.randomUUID().toString());
        check.setCheckedAt(LocalDateTime.now());
        return checkRepository.save(check);
    }

    public List<DrugInteractionCheck> getIncidentReport() {
        return checkRepository.findAll();
    }
}
