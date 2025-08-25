package com.fpoly.shared_learning_materials.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.Favorite;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.*;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;

import jakarta.transaction.Transactional;

@Service
public class FavoriteService {

	@Autowired
    private FavoriteRepository favoriteRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private DocumentRepository documentRepository;

    @Transactional
    public boolean toggleFavorite(String username, Long documentId) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                                  .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Document document = documentRepository.findById(documentId)
                                              .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));

        Optional<Favorite> existing = favoriteRepository.findByUserAndDocument(user, document);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false; // Đã xóa
        } else {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setDocument(document);
            favoriteRepository.save(favorite);
            return true; // Đã thêm
        }
    }
}
