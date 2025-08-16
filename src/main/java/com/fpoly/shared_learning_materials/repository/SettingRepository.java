package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    // Tìm setting theo key
    Optional<Setting> findByKey(String key);

    // Tìm tất cả settings theo category
    List<Setting> findByCategory(String category);

    // Tìm tất cả settings public (có thể truy cập từ frontend)
    List<Setting> findByIsPublicTrue();

    // Tìm settings theo category và public
    List<Setting> findByCategoryAndIsPublicTrue(String category);

    // Kiểm tra setting có tồn tại không
    boolean existsByKey(String key);

    // Xóa setting theo key
    void deleteByKey(String key);

    // Lấy tất cả settings theo danh sách keys
    @Query("SELECT s FROM Setting s WHERE s.key IN :keys")
    List<Setting> findByKeyIn(@Param("keys") List<String> keys);

    // Lấy settings theo category với phân trang
    @Query("SELECT s FROM Setting s WHERE s.category = :category ORDER BY s.key")
    List<Setting> findByCategoryOrderByKey(@Param("category") String category);
}