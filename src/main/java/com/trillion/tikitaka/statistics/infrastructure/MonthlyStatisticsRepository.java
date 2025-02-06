package com.trillion.tikitaka.statistics.infrastructure;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.statistics.domain.MonthlyStatistics;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyStatisticsRepository extends JpaRepository<MonthlyStatistics, Long> {

    boolean existsByStatYearAndStatMonthAndCategoryIdAndUserIdAndTypeId(int statYear, int statMonth, Long categoryId, Long userId, Long typeId);
    Optional<MonthlyStatistics> findByStatYearAndStatMonthAndCategoryIdAndUserIdAndTypeId(
            int year, int month, Long categoryId, Long userId, Long typeId);




}






